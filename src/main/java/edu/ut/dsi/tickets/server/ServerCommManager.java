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

import edu.ut.dsi.tickets.Request;
import edu.ut.dsi.tickets.Response;

public class ServerCommManager {

  private static class RequestHandler implements Runnable {

    private final Socket       socket;
    private final TicketServer server;

    public RequestHandler(TicketServer server, Socket socket) {
      this.socket = socket;
      this.server = server;
    }

    public void run() {
      try {
        while (true) {
          Request request = new Request();
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
          Response r = new Response(response);
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
  private int                port;
  private ExecutorService    handlers;
  private ServerSocket       ss;

  public ServerCommManager(TicketServer server, String address, int port) {
    this.server = server;
    this.port = port;
    this.address = address;
    this.handlers = Executors.newCachedThreadPool();
  }

  public void start() throws IOException {
    this.ss = new ServerSocket();
    ss.bind(new InetSocketAddress(Inet4Address.getByName(this.address), this.port));
    System.out.println("TicketServer listening on: " + this.address + ":" + this.port);
    Executors.newSingleThreadExecutor().submit(new Runnable() {
      public void run() {
        try {
          while (true) {
            Socket socket = ss.accept();
            InetSocketAddress sa = (InetSocketAddress) socket.getRemoteSocketAddress();
            System.out.println("New client from: " + sa.getHostName() + ":" + sa.getPort());
            handlers.submit(new RequestHandler(server, socket));
          }
        } catch (IOException e) {
          throw new RuntimeException("Error in acceptor thread.", e);
        }
      }
    });

  }

  public void stop() throws IOException {
    ss.close();
  }

}
