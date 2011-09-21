package edu.ut.dsi.tickets.client;

import java.io.IOException;

import edu.ut.dsi.tickets.Request;
import edu.ut.dsi.tickets.Request.Method;
import edu.ut.dsi.tickets.server.TicketServer;

/**
 * Simple facade to emulate RPC calls. Can be used instead of the (lower level) {@link TicketClient}.
 * 
 * @author dralves
 * 
 */
public class TicketServerClient implements TicketServer {

  private TicketClient client;
  private int          id;

  public TicketServerClient(TicketClient client, int id) throws IOException {
    this.client = client;
    this.client.connect();
    this.id = id;
  }

  public int[] delete(String name) {
    try {
      return this.client.send(new Request(Method.DELETE, name)).values();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public int[] reserve(String name, int count) {
    try {
      return this.client.send(new Request(Method.RESERVE, name, count)).values();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public int[] search(String name) {
    try {
      return this.client.send(new Request(Method.SEARCH, name)).values();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public int getId() {
    return this.id;
  }

}