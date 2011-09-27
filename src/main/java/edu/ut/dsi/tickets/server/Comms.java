package edu.ut.dsi.tickets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import edu.ut.dsi.tickets.Message;
import edu.ut.dsi.tickets.MethodRequest;
import edu.ut.dsi.tickets.MethodResponse;
import edu.ut.dsi.tickets.NamedThreadFactory;
import edu.ut.dsi.tickets.client.TicketClient;
import edu.ut.dsi.tickets.mutex.LamportMutexLock;

public class Comms {

  private static final Logger LOG = LoggerFactory.getLogger(Comms.class);

  private class ClientRequestHandler implements Runnable {

    private final Socket       socket;
    private final TicketServer server;

    public ClientRequestHandler(TicketServer server, Socket socket) {
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
        // handle FailureDetector here
        LOG.error(
            "Exception handling request [" + request + "]." + e.getClass().getSimpleName() + " message: "
                + e.getMessage(), e);
        throw new RuntimeException(e);
      }
    }
  }

  private class ServerRequestHandler implements Runnable {
    private final Socket              socket;
    private final TicketServerReplica server;

    public ServerRequestHandler(TicketServerReplica server, Socket socket) {
      this.socket = socket;
      this.server = server;
    }

    public void run() {
      initMDCforCurrentThread();
      try {
        while (true) {
          MethodRequest request = new MethodRequest();
          request.read(new DataInputStream(socket.getInputStream()));
          LOG.debug("Server Request: " + request);
          switch (request.method()) {
            case REPLICATE_PUT:
              this.server.replicatePut(request.name(), request.count());
              break;
            case REPLICATE_DELETE:
              this.server.replicateDelete(request.name());
              break;
            case RECEIVE:
              lock.receive(request.msg());
              break;
            default:
              throw new IllegalStateException();
          }
          MethodResponse r = new MethodResponse(new int[0]);
          LOG.debug("Server Response[Req:" + request + "]: " + r);
          r.write(new DataOutputStream(socket.getOutputStream()));
        }
      } catch (Exception e) {
        LOG.error("Exception handling request" + e.getClass().getSimpleName() + " message: " + e.getMessage(), e);
        throw new RuntimeException(e);
      }
    }
  }

  private final ReservationManager             resMgmt;
  private ExecutorService                      clientHandlers;
  private ExecutorService                      serverHandlers;
  private ServerSocket                         clientSS;
  private ServerSocket                         serverSS;
  private ExecutorService                      clientAcceptorExecutor;
  private ExecutorService                      serverAcceptorExecutor;
  private ServerInfo                           me;
  private Map<ServerInfo, TicketServerReplica> others;
  private LamportMutexLock                     lock;

  public Comms(ReservationManager resMgmt, ServerInfo me) {
    this(resMgmt, me, null);
  }

  public Comms(ReservationManager resMgmt, ServerInfo me, List<ServerInfo> others) {
    this.resMgmt = resMgmt;
    this.me = me;
    if (others != null) {
      this.others = new LinkedHashMap<ServerInfo, TicketServerReplica>();
      for (ServerInfo server : others) {
        if (server.id != me.id) {
          this.others.put(server, new RemoteReplica(new TicketClient(server.address, server.serverPort), server));
        }
      }
    }
  }

  public void setLock(LamportMutexLock lock) {
    this.lock = lock;
  }

  public void start() throws IOException {
    startClientComms();
    if (others != null) {
      startServerComms();
    }
    System.out.println("Server " + me.id + " started.");
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
          // handle FailureDetector here
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
          // handle FailureDetector here
          throw new RuntimeException("Error in client acceptor thread.", e);
        }
      }
    });
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

  public void sendToAll(Message<?> msg) {
    for (TicketServerReplica replica : others.values()) {
      try {
        LOG.debug("Sending msg[" + msg + "] to server: " + replica);
        replica.receive(msg);
      } catch (IOException e) {
        // HandleFailureDetector here
        LOG.error("Error sending to all.", e);
        throw new RuntimeException(e);
      }
    }
  }

  public void send(int senderId, Message<?> msg) {
    try {
      others.get(new ServerInfo(senderId)).receive(msg);
    } catch (IOException e) {
      // HandleFailureDetector here
      LOG.error("Error sending to process: " + senderId, e);
      throw new RuntimeException(e);
    }
  }

  public Collection<ServerInfo> remoteServers() {
    return this.others.keySet();
  }

  public void putAll(String name, int count) {
    LOG.debug("Sending PUT messages to replicas.");
    for (TicketServerReplica replica : others.values()) {
      try {
        LOG.debug("Sending PUT messages to replica: " + replica.getInfo());
        replica.replicatePut(name, count);
      } catch (IOException e) {
        // HandleFailureDetector here
        LOG.error("Error sending to process: " + replica.getInfo(), e);
        throw new RuntimeException(e);
      }
    }
  }

  public void removeAll(String name) {
    for (TicketServerReplica replica : others.values()) {
      try {
        replica.replicateDelete(name);
      } catch (IOException e) {
        // HandleFailureDetector here
        LOG.error("Error sending to process: " + replica.getInfo(), e);
        throw new RuntimeException(e);
      }
    }
  }

}
