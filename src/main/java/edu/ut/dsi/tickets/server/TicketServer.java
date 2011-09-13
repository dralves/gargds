package edu.ut.dsi.tickets.server;

/**
 * A an interface for a ticket server as defined in the first two assignments.
 * 
 * @author dralves
 * 
 */
public interface TicketServer {

  public int[] reserve(String name, int count);

  public int[] search(String name);

  public int[] delete(String name);

}
