package edu.ut.dsi.tickets.server.reservations;

import java.util.HashMap;

/**
 * An interface for the storage medium for the reservations. For assigment1 a simple Map was enough but assignement 2
 * requires that writes to the store be replicated before the locks are released.
 * 
 * @author dralves
 * 
 */
public interface ReservationStore {

  public Reservation get(String name) throws UnknownReservationException;

  public Reservation put(String name, int count) throws NotEnoughSeatsException, DuplicateNameException;

  public Reservation remove(String name) throws UnknownReservationException;

  public Reservation updateReplicaRemove(String name) throws UnknownReservationException;

  public Reservation updateReplicaPut(String name, int count) throws NotEnoughSeatsException, DuplicateNameException;

  public void replicaUpdate();

  public HashMap<String, Reservation> rawMap();

}