package edu.ut.dsi.tickets.server;

public class LamportReservationManager extends ReservationManager {

  private TicketServer[] otherServers;

  public LamportReservationManager(TicketServer[] otherServers, int numSeats) {
    super(otherServers.length, numSeats);
    this.otherServers = otherServers;
  }
}
