package edu.ut.dsi.tickets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.ut.dsi.tickets.Message;
import edu.ut.dsi.tickets.MethodRequest;
import edu.ut.dsi.tickets.MethodResponse;
import edu.ut.dsi.tickets.mutex.MutexReq;

public class Comms {

  private static class ClientRequestHandler implements Runnable {

    private final Socket       socket;
    private final TicketServer server;

    public ClientRequestHandler(TicketServer server, Socket socket) {
      this.socket = socket;
      this.server = server;
    }

    public void run() {
      try {
        while (true) {
          MethodRequest request = new MethodRequest();
          request.read(new DataInputStream(socket.getInputStream()));
          System.out.println("Request: " + request);
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
          }
          MethodResponse r = new MethodResponse(response);
          System.out.println("Response[Req:" + request + "]: " + r);
          r.write(new DataOutputStream(socket.getOutputStream()));
        }
      } catch (IOException e) {
        System.out.println("IO Exception in socket." + e.getClass().getSimpleName() + " message: " + e.getMessage());
        throw new RuntimeException(e);
      }
    }
  }

  private final TicketServer server;
  private String             address;
  private int                clientPort;
  private int                serverPort;
  private ExecutorService    clientHandlers;
  private ExecutorService    serverHandlers;
  private ServerSocket       clientSS;
  private ServerSocket       serverSS;
  private ExecutorService    clientAcceptorExecutor;
  private ExecutorService    serverAcceptorExecutor;

  public Comms(TicketServer server, String address, int clientPort) {
    this(server, address, clientPort, -1);
  }

  public Comms(TicketServer server, String address, int clientPort, int serverPort) {
    this.server = server;
    this.clientPort = clientPort;
    this.serverPort = serverPort;
    this.address = address;
    this.clientHandlers = Executors.newCachedThreadPool();
  }

  public void start() throws IOException {
    startClientComms();
    if (this.serverPort != -1) {
      startServerComms();
    }
  }

  public void startClientComms() throws IOException {
    this.clientSS = new ServerSocket();
    clientSS.bind(new InetSocketAddress(Inet4Address.getByName(this.address), this.clientPort));
    System.out.println("TicketServer[" + server.id() + "] listening for clients on: " + this.address + ":"
        + this.clientPort);
    this.clientAcceptorExecutor = Executors.newSingleThreadExecutor();
    this.clientAcceptorExecutor.submit(new Runnable() {
      public void run() {
        try {
          while (true) {
            Socket socket = clientSS.accept();
            InetSocketAddress sa = (InetSocketAddress) socket.getRemoteSocketAddress();
            System.out.println("New client from: " + sa.getHostName() + ":" + sa.getPort());
            clientHandlers.submit(new ClientRequestHandler(server, socket));
          }
        } catch (IOException e) {
          throw new RuntimeException("Error in server acceptor thread.", e);
        }
      }
    });
  }

  public void startServerComms() throws IOException {
    this.serverSS = new ServerSocket();
    serverSS.bind(new InetSocketAddress(Inet4Address.getByName(this.address), this.serverPort));
    System.out.println("TicketServer[" + server.id() + "] listening for servers on: " + this.address + ":"
        + this.serverPort);
    this.serverAcceptorExecutor = Executors.newSingleThreadExecutor();
    this.serverAcceptorExecutor.submit(new Runnable() {
      public void run() {
        try {
          while (true) {
            Socket socket = serverSS.accept();
            InetSocketAddress sa = (InetSocketAddress) socket.getRemoteSocketAddress();
            System.out.println("New server from: " + sa.getHostName() + ":" + sa.getPort());
            serverHandlers.submit(new ClientRequestHandler(server, socket));
          }
        } catch (IOException e) {
          throw new RuntimeException("Error in client acceptor thread.", e);
        }
      }
    });
  }

  public void stop() throws IOException {
    stopClientComms();
    if (this.serverPort != -1) {
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

  public void sendToAll(Message<MutexReq> msg) {

  }

  public void send(int senderId, Message<?> msg) {
    // TODO Auto-generated method stub

  }

}
