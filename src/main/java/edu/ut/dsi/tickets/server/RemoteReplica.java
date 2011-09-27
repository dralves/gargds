package edu.ut.dsi.tickets.server;

import java.io.IOException;

import edu.ut.dsi.tickets.Message;
import edu.ut.dsi.tickets.Message.MsgType;
import edu.ut.dsi.tickets.MethodRequest;
import edu.ut.dsi.tickets.MethodRequest.Method;
import edu.ut.dsi.tickets.client.TicketClient;
import edu.ut.dsi.tickets.mutex.Clock.Timestamp;

/**
 * Funny and confusing name for a simple class: a rpc facade for servers to communicate among themselver.
 * 
 * @author dralves
 * 
 */
public class RemoteReplica implements TicketServerReplica {

  private TicketClient client;
  private ServerInfo   remote;
  private ServerInfo   local;

  public RemoteReplica(TicketClient client, ServerInfo remote, ServerInfo local) throws IOException {
    this.client = client;
    this.remote = remote;
    this.local = local;
    if (!client.isExistingSocket()) {
      MethodRequest request = new MethodRequest(Method.JOIN, new Message<ServerInfo>(MsgType.JOIN, new Timestamp(0),
          this.local.id, this.local));
      this.client.send(request);
    }
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
