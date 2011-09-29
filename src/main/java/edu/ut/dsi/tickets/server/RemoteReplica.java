package edu.ut.dsi.tickets.server;

import java.io.IOException;
import java.util.Map;

import edu.ut.dsi.tickets.Message;
import edu.ut.dsi.tickets.MethodRequest;
import edu.ut.dsi.tickets.MethodRequest.Method;
import edu.ut.dsi.tickets.Writable;
import edu.ut.dsi.tickets.client.TicketClient;
import edu.ut.dsi.tickets.server.reservations.Reservation;

/**
 * Funny and confusing name for a simple class: a rpc facade for servers to communicate among themselver.
 * 
 * @author dralves
 * 
 */
public class RemoteReplica implements TicketServerReplica {

  private TicketClient client;
  private ServerInfo   remote;

  public RemoteReplica(TicketClient client, ServerInfo remote, ServerInfo local) throws IOException {
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

  public <T extends Writable> Message<T> receive(Message<?> msg) throws IOException {
    return (Message<T>) this.client.send(new MethodRequest(Method.LOCK_MSG, msg)).msg();
  }

  @Override
  public String toString() {
    return this.remote.toString();
  }

  public TicketClient client() {
    return this.client;
  }

  public Map<? extends String, ? extends Reservation> replicaUpdate() throws IOException {
    return this.client.send(new MethodRequest(Method.REPLICA_UPDATE)).getSeatMap();
  }
}
