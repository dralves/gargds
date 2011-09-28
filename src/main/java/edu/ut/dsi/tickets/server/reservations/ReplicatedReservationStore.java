package edu.ut.dsi.tickets.server.reservations;

import java.util.HashMap;

import edu.ut.dsi.tickets.server.Comms;

public class ReplicatedReservationStore extends LocalReservationStore {

  private Comms comms;

  public ReplicatedReservationStore(int numSeats) {
    super(numSeats);
  }

  public void setComms(Comms comms) {
    this.comms = comms;
  }

  @Override
  public Reservation put(String name, int count) throws NotEnoughSeatsException, DuplicateNameException {
    Reservation reservation = super.put(name, count);
    comms.putAll(name, count);
    return reservation;
  }

  @Override
  public Reservation remove(String name) throws UnknownReservationException {
    Reservation reservation = super.remove(name);
    comms.removeAll(name);
    return reservation;
  }

  @Override
  public Reservation updateReplicaPut(String name, int count) throws NotEnoughSeatsException, DuplicateNameException {
    return super.put(name, count);
  }

  @Override
  public Reservation updateReplicaRemove(String name) throws UnknownReservationException {
    return super.remove(name);
  }

  @Override
  public void replicaUpdate() {
    super.map = new HashMap<String, Reservation>(comms.getSeatMap());
  }

}
