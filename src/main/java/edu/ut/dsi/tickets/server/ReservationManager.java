package edu.ut.dsi.tickets.server;

import java.io.IOException;
import java.util.concurrent.locks.Lock;

import edu.ut.dsi.tickets.Message;
import edu.ut.dsi.tickets.MethodResponse;
import edu.ut.dsi.tickets.server.reservations.DuplicateNameException;
import edu.ut.dsi.tickets.server.reservations.NotEnoughSeatsException;
import edu.ut.dsi.tickets.server.reservations.ReservationStore;
import edu.ut.dsi.tickets.server.reservations.UnknownReservationException;

public class ReservationManager implements TicketServer, TicketServerReplica {

  private final ReservationStore store;
  private final Lock             lock;
  private ServerInfo             info;

  public ReservationManager(ReservationStore store, Lock lock, ServerInfo info) {
    this.lock = lock;
    this.store = store;
    this.info = info;
  }

  public int[] reserve(String name, int count) {
    lock.lock();
    try {
      return store.put(name, count).seatMap();
    } catch (NotEnoughSeatsException e) {
      return MethodResponse.NOT_FOUND;
    } catch (DuplicateNameException e) {
      return MethodResponse.ERROR;
    } finally {
      lock.unlock();
    }
  }

  public int[] search(String name) {
    lock.lock();
    try {
      return store.get(name).seatMap();
    } catch (UnknownReservationException e) {
      return MethodResponse.NOT_FOUND;
    } finally {
      lock.unlock();
    }

  }

  public int[] delete(String name) {
    lock.lock();
    try {
      return store.remove(name).seatMap();
    } catch (UnknownReservationException e) {
      return MethodResponse.NOT_FOUND;
    } finally {
      lock.unlock();
    }
  }

  public int[] replicateDelete(String name) {
    try {
      return store.updateReplicaRemove(name).seatMap();
    } catch (UnknownReservationException e) {
      throw new IllegalStateException("A replicate delete command has failed as server: " + info.id
          + " did not have the reservation in question.");
    }
  }

  public int[] replicatePut(String name, int count) {
    try {
      return store.updateReplicaPut(name, count).seatMap();
    } catch (NotEnoughSeatsException e) {
      throw new IllegalStateException("Inconsistent State", e);
    } catch (DuplicateNameException e) {
      throw new IllegalStateException("Inconsistent State", e);
    }
  }

  public void receive(Message<?> msg) throws IOException {
    throw new UnsupportedOperationException();
  }

  public ServerInfo getInfo() {
    return this.info;
  }
}
