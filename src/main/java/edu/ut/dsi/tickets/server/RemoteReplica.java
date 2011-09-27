package edu.ut.dsi.tickets.server;

import java.io.IOException;

import edu.ut.dsi.tickets.Message;
import edu.ut.dsi.tickets.MethodRequest;
import edu.ut.dsi.tickets.MethodRequest.Method;
import edu.ut.dsi.tickets.client.TicketClient;

/**
 * Funny and confusing name for a simple class: a rpc facade for servers to communicate among themselver.
 * 
 * @author dralves
 * 
 */
public class RemoteReplica implements TicketServerReplica {

  private TicketClient client;
  private ServerInfo   remote;

  public RemoteReplica(TicketClient client, ServerInfo remote) {
    this.client = client;
    this.remote = remote;
  }

  public ServerInfo getInfo() {
    return this.remote;
  }

  public int[] replicateDelete(String name) throws IOException {
    return this.client.send(new MethodRequest(Method.REPLICATE_DELETE, name)).values();
  }

  public int[] replicatePut(String name, int count) throws IOException {
    return this.client.send(new MethodRequest(Method.REPLICATE_PUT, name, count)).values();
  }

  public void receive(Message<?> msg) throws IOException {
    this.client.send(new MethodRequest(Method.RECEIVE, msg));
  }

  @Override
  public String toString() {
    return this.remote.toString();
  }
}
