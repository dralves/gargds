package edu.ut.dsi.tickets.server;

import java.io.IOException;

/**
 * A an interface for a ticket server as defined in the first two assignments.
 * 
 * @author dralves
 * 
 */
public interface TicketServer {

  public int[] reserve(String name, int count) throws IOException;

  public int[] search(String name) throws IOException;

  public int[] delete(String name) throws IOException;

}
