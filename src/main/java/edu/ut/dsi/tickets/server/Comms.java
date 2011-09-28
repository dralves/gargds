package edu.ut.dsi.tickets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import edu.ut.dsi.tickets.FailureContingencyCallback;
import edu.ut.dsi.tickets.FailureDetector;
import edu.ut.dsi.tickets.Message;
import edu.ut.dsi.tickets.Message.MsgType;
import edu.ut.dsi.tickets.MethodRequest;
import edu.ut.dsi.tickets.MethodResponse;
import edu.ut.dsi.tickets.NamedThreadFactory;
import edu.ut.dsi.tickets.PerfectFailureDetector;
import edu.ut.dsi.tickets.client.TicketClient;
import edu.ut.dsi.tickets.mutex.Clock.Timestamp;
import edu.ut.dsi.tickets.mutex.LamportMutexLock;

public class Comms {

  private static final Logger LOG = LoggerFactory.getLogger(Comms.class);

  private class ClientRequestHandler implements Runnable {

    private final Socket             socket;
    private final ReservationManager server;

    public ClientRequestHandler(ReservationManager server, Socket socket) {
      this.socket = socket;
      this.server = server;
    }

    public void run() {
      initMDCforCurrentThread();
      MethodRequest request = null;
      try {
        while (true) {
          request = new MethodRequest();
          request.read(new DataInputStream(socket.getInputStream()));
          LOG.debug("Client Request: " + request);
          int[] response = null;
          switch (request.method()) {
            case RESERVE:
              response = this.server.reserve(request.name(), request.count());
              break;
            case SEARCH:
              response = this.server.search(request.name());
              break;
            case DELETE:
              response = this.server.delete(request.name());
              break;
            default:
              throw new IllegalStateException();
          }
          MethodResponse r = new MethodResponse(response);
          LOG.debug("Response[Req:" + request + "]: " + r);
          r.write(new DataOutputStream(socket.getOutputStream()));
        }
      } catch (Exception e) {
        LOG.error(
            "Exception handling request [" + request + "]." + e.getClass().getSimpleName() + " message: "
                + e.getMessage(), e);
        throw new RuntimeException(e);
      }
    }
  }

  private class ServerRequestHandler implements Runnable {
    private final Socket             socket;
    private final ReservationManager local;
    private ServerInfo               remote;

    public ServerRequestHandler(ReservationManager server, Socket socket) {
      this.socket = socket;
      this.local = server;
    }

    public void run() {
      initMDCforCurrentThread();
      MethodRequest request;
      try {
        while (true) {
          request = new MethodRequest();
          LOG.debug("Ready to accept server requests");
          request.read(new DataInputStream(socket.getInputStream()));
          LOG.debug("Server Request: " + request);
          Message<?> response = null;
          switch (request.method()) {
            case REPLICATE_PUT:
              this.local.replicatePut(request.name(), request.count());
              break;
            case REPLICATE_DELETE:
              this.local.replicateDelete(request.name());
              break;
            case RECEIVE:
              response = lock.receive(request.msg());
              this.remote = getInfoById(request.msg().senderId());
              break;
            default:
              throw new IllegalStateException();
          }
          MethodResponse r;
          if (response != null) {
            r = new MethodResponse(response);
          } else {
            r = new MethodResponse(new int[0]);
          }
          LOG.debug("Server Response[Req:" + request + "]: " + r);
          r.write(new DataOutputStream(socket.getOutputStream()));
        }
      } catch (Exception e) {
        LOG.error("Exception handling request " + e.getClass().getSimpleName() + " message: " + e.getMessage(), e);
        fd.suspect(remote.id, e);
      }
    }
  }

  private final ReservationManager       resMgmt;
  private ExecutorService                clientHandlers;
  private ExecutorService                serverHandlers;
  private ServerSocket                   clientSS;
  private ServerSocket                   serverSS;
  private ExecutorService                clientAcceptorExecutor;
  private ExecutorService                serverAcceptorExecutor;
  private ServerInfo                     me;
  private Map<ServerInfo, RemoteReplica> otherServers;
  private FailureDetector                fd;
  private LamportMutexLock               lock;

  public Comms(ReservationManager resMgmt, ServerInfo me) throws IOException {
    this(resMgmt, me, null);
  }

  public Comms(ReservationManager resMgmt, ServerInfo me, List<ServerInfo> others) throws IOException {
    this.resMgmt = resMgmt;
    this.me = me;
    this.fd = new PerfectFailureDetector();
    this.fd.setFailureContingencyCallback(new ServerFC());
    this.otherServers = Collections.synchronizedMap(new HashMap<ServerInfo, RemoteReplica>());
    for (ServerInfo server : others) {
      if (server.id != me.id) {
        this.otherServers.put(server, null);
      }
    }
  }

  public List<Message<?>> sendToAll(Message<?> msg) {
    LOG.debug("Sending message to all servers.");
    List<Message<?>> responses = new ArrayList<Message<?>>();
    for (ServerInfo ticketServer : remoteServers()) {
      responses.add(send(ticketServer.id, msg));
    }
    return responses;
  }

  public Message<?> send(int targetId, Message<?> msg) {
    ServerInfo remote = getInfoById(targetId);
    try {
      LOG.debug("Sending Message to server. [Msg: " + msg + ", Server: " + remote + "]");
      return getReplica(remote).receive(msg);
      // LOG.debug("Message sent without error. [Msg: " + msg + ", Server: " + remote + "]");
    } catch (IOException e) {
      LOG.error("Error sending to process: " + remote, e);
      fd.suspect(remote.id, e);
    }
    return null;
  }

  public void putAll(String name, int count) {
    LOG.debug("Sending PUT messages to replicas. [" + name + "]");
    for (ServerInfo ticketServer : remoteServers()) {
      try {
        LOG.debug("Sending PUT messages to replica: " + ticketServer);
        getReplica(ticketServer).replicatePut(name, count);
      } catch (IOException e) {
        LOG.error("Error sending to process: " + ticketServer, e);
        fd.suspect(ticketServer.id, e);
      }
    }
  }

  public void removeAll(String name) {
    LOG.debug("Sending DELETE messages to replicas. [" + name + "]");
    for (ServerInfo ticketServer : remoteServers()) {
      try {
        getReplica(ticketServer).replicateDelete(name);
      } catch (IOException e) {
        LOG.error("Error sending to process: " + ticketServer, e);
        fd.suspect(ticketServer.id, e);
      }
    }
  }

  private RemoteReplica getReplica(ServerInfo remote) throws IOException {
    synchronized (otherServers) {
      RemoteReplica replica = otherServers.get(remote);
      if (replica == null) {
        TicketClient client = new TicketClient(remote.address, remote.serverPort);
        client.connect();
        LOG.debug("Created new RemoteReplica for " + remote);
        replica = new RemoteReplica(client, remote, me);
        otherServers.put(remote, replica);
      }
      return replica;
    }
  }

  private class ServerFC implements FailureContingencyCallback {

    public void failed(int pid) {
      synchronized (otherServers) {
        ServerInfo info = getInfoById(pid);
        info.failed = true;
        otherServers.put(info, null);
      }
    }
  }

  public void startClientComms() throws IOException {
    this.clientSS = new ServerSocket();
    clientSS.bind(new InetSocketAddress(Inet4Address.getByName(me.address), me.clientPort));
    this.clientAcceptorExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("Pid: " + this.me.id
        + " Client Acceptor"));
    this.clientHandlers = Executors
        .newCachedThreadPool(new NamedThreadFactory("Pid: " + this.me.id + " Client Handler"));
    LOG.debug("TicketServer[" + me.id + "] listening for clients on: " + me.address + ":" + me.clientPort);
    this.clientAcceptorExecutor.submit(new Runnable() {
      public void run() {
        try {
          initMDCforCurrentThread();
          while (true) {
            Socket socket = clientSS.accept();
            InetSocketAddress sa = (InetSocketAddress) socket.getRemoteSocketAddress();
            LOG.debug("New client from: " + sa.getHostName() + ":" + sa.getPort());
            clientHandlers.submit(new ClientRequestHandler(resMgmt, socket));
          }
        } catch (IOException e) {
          LOG.error("Error in Client Acceptor", e);
          throw new RuntimeException("Error in server acceptor thread.", e);
        }
      }
    });
  }

  public void initMDCforCurrentThread() {
    MDC.put("server", this.me.id + "");
  }

  public void startServerComms() throws IOException {
    this.serverSS = new ServerSocket();
    serverSS.bind(new InetSocketAddress(Inet4Address.getByName(me.address), me.serverPort));
    LOG.debug("TicketServer[" + me.id + "] listening for servers on: " + me.address + ":" + me.serverPort);
    this.serverAcceptorExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("Pid: " + this.me.id
        + " Server Acceptor"));
    this.serverHandlers = Executors
        .newCachedThreadPool(new NamedThreadFactory("Pid: " + this.me.id + " Server Handler"));
    this.serverAcceptorExecutor.submit(new Runnable() {
      public void run() {
        try {
          initMDCforCurrentThread();
          while (true) {
            Socket socket = serverSS.accept();
            InetSocketAddress sa = (InetSocketAddress) socket.getRemoteSocketAddress();
            LOG.debug("New server from: " + sa.getHostName() + ":" + sa.getPort());
            serverHandlers.submit(new ServerRequestHandler(resMgmt, socket));
          }
        } catch (IOException e) {
          LOG.error("Error in Server Acceptor", e);
          throw new RuntimeException("Error in client acceptor thread.", e);
        }
      }
    });
  }

  public void setLock(LamportMutexLock lock) {
    this.lock = lock;
  }

  public void start() throws IOException {
    startClientComms();
    if (otherServers != null) {
      startServerComms();
    }
    System.out.println("Server " + me.id + " started.");
  }

  public void join() throws IOException {
    for (ServerInfo info : remoteServers()) {
      RemoteReplica replica = getReplica(info);
      replica.receive(new Message<ServerInfo>(MsgType.JOIN, new Timestamp(0), me.id, me));
      // serverHandlers.submit(new ServerRequestHandler(resMgmt, replica.client().socket()));
    }
  }

  public void stop() throws IOException {
    stopClientComms();
    if (me.serverPort != -1) {
      stopServerComms();
    }
  }

  public void stopClientComms() throws IOException {
    clientAcceptorExecutor.shutdownNow();
    clientSS.close();
  }

  public void stopServerComms() throws IOException {
    serverAcceptorExecutor.shutdownNow();
    serverSS.close();
  }

  public Collection<ServerInfo> remoteServers() {
    return this.otherServers.keySet();
  }

  /**
   * Returns non-failed remote servers
   * 
   * @return
   */
  public Collection<ServerInfo> remoteAliveServers() {
    synchronized (otherServers) {
      Collection<ServerInfo> remoteServers = Maps.filterEntries(otherServers,
          new Predicate<Map.Entry<ServerInfo, RemoteReplica>>() {
            public boolean apply(Entry<ServerInfo, RemoteReplica> input) {
              return !input.getKey().failed;
            }
          }).keySet();
      if (remoteServers.size() < 1) {
        LOG.error("At least one server must be alive.");
        throw new IllegalStateException("At least one server must be alive.");
      }
      return remoteServers;
    }
  }

  public ServerInfo getInfoById(int id) {
    for (ServerInfo server : remoteServers()) {
      if (server.id == id) {
        return server;
      }
    }
    return null;

  }
}
