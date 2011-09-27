package edu.ut.dsi.tickets.server.reservations;

public class UnknownReservationException extends Exception {

  public UnknownReservationException(String name) {
    super(name);
  }
}
