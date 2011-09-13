package edu.ut.dsi.tickets.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.ut.dsi.tickets.Request;
import edu.ut.dsi.tickets.Response;

public class TicketClient {

  private int    port;
  private String address;
  private Socket socket;

  public TicketClient(String serverAddress, int port) {
    this.address = serverAddress;
    this.port = port;
  }

  public void connect() throws UnknownHostException, IOException {
    this.socket = new Socket();
    this.socket.connect(new InetSocketAddress(this.address, this.port));
  }

  public Response send(Request request) throws IOException {
    request.write(new DataOutputStream(socket.getOutputStream()));
    Response response = new Response();
    response.read(new DataInputStream(socket.getInputStream()));
    return response;
  }

}
