package edu.ut.dsi.tickets.server.reservations;

import java.io.Serializable;

public class Seat implements Serializable{

  public final int    number;
  private Reservation reservation;

  public Seat(int number) {
    this.number = number;
  }

  public Reservation reservation() {
    return reservation;
  }

  public void reserve(Reservation reservation) {
    this.reservation = reservation;
  }

  public void release() {
    this.reservation = null;
  }

}