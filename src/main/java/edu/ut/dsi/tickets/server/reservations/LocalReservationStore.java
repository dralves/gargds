package edu.ut.dsi.tickets.server.reservations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A local implementation of a store. Methods are synchronized to ensure memory visibility only.
 * 
 * @author dralves
 * 
 */
public class LocalReservationStore implements ReservationStore {

  private HashMap<String, Reservation> map;
  private ArrayList<Seat>              seats;

  public LocalReservationStore(int numSeats) {
    this.seats = new ArrayList<Seat>();
    for (int i = 0; i < numSeats; i++) {
      seats.add(new Seat(i));
    }
    this.map = new HashMap<String, Reservation>();
  }

  public synchronized Reservation get(String name) throws UnknownReservationException {
    Reservation reservation = map.get(name);
    if (reservation == null) {
      throw new UnknownReservationException(name);
    }
    return reservation;
  }

  public synchronized Reservation put(String name, int count) throws NotEnoughSeatsException, DuplicateNameException {
    if (map.get(name) != null) {
      throw new DuplicateNameException(name);
    }
    List<Seat> newSeats = new ArrayList<Seat>();
    for (Seat seat : seats) {
      if (seat.reservation() == null) {
        newSeats.add(seat);
      }
      if (newSeats.size() >= count) {
        break;
      }
    }
    if (newSeats.size() < count) {
      throw new NotEnoughSeatsException();
    }
    Reservation reservation = new Reservation(name, newSeats);
    map.put(name, reservation);
    return reservation;
  }

  public synchronized Reservation remove(String name) throws UnknownReservationException {
    Reservation reservation = map.get(name);
    if (reservation == null) {
      throw new UnknownReservationException(name);
    }
    for (Seat seat : reservation.seats) {
      seat.release();
    }
    return reservation;
  }

  public Reservation updateReplicaPut(String name, int count) throws NotEnoughSeatsException, DuplicateNameException {
    throw new UnsupportedOperationException();
  }

  public Reservation updateReplicaRemove(String name) throws UnknownReservationException {
    throw new UnsupportedOperationException();
  }

}
