package edu.ut.dsi.tickets.server;

import java.io.IOException;

import edu.ut.dsi.tickets.Message;
import edu.ut.dsi.tickets.Writable;

public interface TicketServerReplica {

  public ServerInfo getInfo();

  public int[] replicatePut(String name, int count) throws IOException;

  public int[] replicateDelete(String name) throws IOException;

  public <T extends Writable> Message<T> receive(Message<?> msg) throws IOException;

}
