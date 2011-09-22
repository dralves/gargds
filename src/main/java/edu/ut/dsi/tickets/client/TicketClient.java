package edu.ut.dsi.tickets.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.ut.dsi.tickets.MethodRequest;
import edu.ut.dsi.tickets.MethodResponse;

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

  public MethodResponse send(MethodRequest request) throws IOException {
    request.write(new DataOutputStream(socket.getOutputStream()));
    MethodResponse response = new MethodResponse();
    response.read(new DataInputStream(socket.getInputStream()));
    return response;
  }

}
