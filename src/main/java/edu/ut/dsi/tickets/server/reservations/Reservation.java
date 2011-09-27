package edu.ut.dsi.tickets.server.reservations;

import java.util.Collections;
import java.util.List;

public class Reservation {
  public final String     name;
  public final List<Seat> seats;

  public Reservation(String name, List<Seat> seats) {
    this.name = name;
    this.seats = Collections.unmodifiableList(seats);
    for (Seat seat : seats) {
      seat.reserve(this);
    }
  }

  public int[] seatMap() {
    int[] seatNums = new int[seats.size()];
    for (int i = 0; i < seatNums.length; i++) {
      seatNums[i] = seats.get(i).number;
    }
    return seatNums;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Reservation)) {
      return false;
    }
    Reservation otherR = (Reservation) other;
    if (!this.name.equals(otherR.name)) {
      return false;
    }
    if (seats == null && otherR.seats != null) {
      return false;
    }
    if (otherR.seats != null && seats == null) {
      return false;
    }
    if (seats == null && otherR.seats == null) {
      return true;
    }

    if (seats.size() != otherR.seats.size()) {
      return false;
    }

    for (int i = 0; i < seats.size(); i++) {
      if (seats.get(i).number != otherR.seats.get(i).number) {
        return false;
      }
    }
    return true;
  }
}