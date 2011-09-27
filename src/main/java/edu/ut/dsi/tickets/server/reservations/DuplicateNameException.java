package edu.ut.dsi.tickets.server.reservations;

public class DuplicateNameException extends Exception {

  public DuplicateNameException(String name) {
    super(name);
  }
}
