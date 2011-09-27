package edu.ut.dsi.tickets.client;

import java.io.IOException;
import java.net.UnknownHostException;

import edu.ut.dsi.tickets.MethodRequest;
import edu.ut.dsi.tickets.MethodRequest.Method;
import edu.ut.dsi.tickets.server.TicketServer;

/**
 * Simple facade to emulate RPC calls. Can be used instead of the (lower level) {@link TicketClient}.
 * 
 * @author dralves
 * 
 */
public class RemoteServer implements TicketServer {

  private TicketClient client;

  public RemoteServer(TicketClient client) throws IOException {
    this.client = client;
  }

  private void checkConnected() throws UnknownHostException, IOException {
    if (!client.isConnected()) {
      client.connect();
    }
  }

  public int[] delete(String name) throws IOException {
    checkConnected();
    return this.client.send(new MethodRequest(Method.DELETE, name)).values();
  }

  public int[] reserve(String name, int count) throws IOException {
    checkConnected();
    return this.client.send(new MethodRequest(Method.RESERVE, name, count)).values();
  }

  public int[] search(String name) throws IOException {
    checkConnected();
    return this.client.send(new MethodRequest(Method.SEARCH, name)).values();
  }

}