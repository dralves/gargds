package edu.ut.dsi.tickets.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ut.dsi.tickets.MethodRequest;
import edu.ut.dsi.tickets.MethodResponse;

public class TicketClient {

  private static final Logger LOG = LoggerFactory.getLogger(TicketClient.class);

  private int                 port;
  private String              address;
  private Socket              socket;
  private boolean             existingSocket;

  public TicketClient(String serverAddress, int port) throws UnknownHostException, IOException {
    this.address = serverAddress;
    this.port = port;
    this.existingSocket = false;
  }

  public TicketClient(Socket socket) {
    this.socket = socket;
    this.existingSocket = true;
  }

  public void connect() throws UnknownHostException, IOException {
    this.socket = new Socket();
    this.socket.connect(new InetSocketAddress(this.address, this.port));
  }

  public boolean isConnected() {
    return this.socket != null ? this.socket.isConnected() : false;
  }

  public MethodResponse send(MethodRequest request) throws IOException {
    request.write(new DataOutputStream(socket.getOutputStream()));
    LOG.error("REQ WROTE: " + request);
    MethodResponse response = new MethodResponse();
    response.read(new DataInputStream(socket.getInputStream()));
    LOG.error("RESP READ: " + request);
    return response;
  }

  public boolean isExistingSocket() {
    return existingSocket;
  }

  public Socket socket() {
    return this.socket;
  }

}
