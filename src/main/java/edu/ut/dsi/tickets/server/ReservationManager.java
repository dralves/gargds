package edu.ut.dsi.tickets.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;

import edu.ut.dsi.tickets.MethodResponse;

public class ReservationManager implements TicketServer {

  private static class Seat {

    final int        number;
    volatile boolean vacant;

    public Seat(int number) {
      this.number = number;
      this.vacant = true;
    }
  }

  private final List<Seat>         allSeats;
  private final Map<String, int[]> reservations = new HashMap<String, int[]>();
  private final ReadWriteLock      lock;
  private final AtomicInteger      freeSeats;
  private int                      id;

  public ReservationManager(int numSeats, int id, ReadWriteLock lock) {
    allSeats = new ArrayList<Seat>();
    for (int i = 0; i < numSeats; i++) {
      allSeats.add(new Seat(i));
    }
    freeSeats = new AtomicInteger(numSeats);
    this.id = id;
    this.lock = lock;
  }

  public int[] reserve(String name, int count) {
    lock.writeLock().lock();
    try {
      if (reservations.containsKey(name)) {
        return MethodResponse.ERROR;
      }
      if (freeSeats.get() < count) {
        return MethodResponse.NOT_FOUND;
      }
      int[] seats = findVacant(count);
      reservations.put(name, seats);
      return seats;
    } finally {
      lock.writeLock().unlock();
    }
  }

  public int[] search(String name) {
    lock.readLock().lock();
    try {
      int[] seats = reservations.get(name);
      return seats == null ? MethodResponse.NOT_FOUND : seats;
    } finally {
      lock.readLock().unlock();
    }

  }

  public int[] delete(String name) {
    lock.writeLock().lock();
    try {
      if (reservations.get(name) != MethodResponse.NOT_FOUND) {
        return freeSeats(reservations.remove(name));
      }
      return MethodResponse.NOT_FOUND;
    } finally {
      lock.writeLock().unlock();
    }
  }

  private int[] freeSeats(int[] seats) {
    for (Integer index : seats) {
      allSeats.get(index).vacant = true;
    }
    freeSeats.addAndGet(seats.length);
    return seats;
  }

  private int[] findVacant(int count) {
    int[] vacant = new int[count];
    int index = 0;
    for (Seat seat : allSeats) {
      if (seat.vacant) {
        seat.vacant = false;
        vacant[index++] = seat.number;
      }
      if (index == count) {
        break;
      }
    }
    freeSeats.addAndGet(-count);
    return vacant;
  }

  public int id() {
    return this.id;
  }
}
