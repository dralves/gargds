package edu.ut.dsi.tickets;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import edu.ut.dsi.tickets.server.ReservationManager;
import edu.ut.dsi.tickets.server.Comms;
import edu.ut.dsi.tickets.server.TicketServer;

public class ServerMain {

  private static Comms manager;

  public synchronized static void main(String... args) throws IOException {
    String address = args[0];
    int port = Integer.parseInt(args[1]);
    int numSeats = 10;
    if (args.length > 2) {
      numSeats = Integer.parseInt(args[2]);
    }
    TicketServer server = new ReservationManager(numSeats, 0, new ReentrantReadWriteLock());
    manager = new Comms(server, address, port);
    manager.start();
  }

  public synchronized static void stop() throws IOException {
    manager.stop();
  }
}
