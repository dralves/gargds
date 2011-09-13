package edu.ut.dsi.tickets;

import java.io.IOException;

import edu.ut.dsi.tickets.server.ServerCommManager;
import edu.ut.dsi.tickets.server.SingleTicketServer;
import edu.ut.dsi.tickets.server.TicketServer;

public class ServerMain {

  public static void main(String... args) throws IOException {
    String address = args[0];
    int port = Integer.parseInt(args[1]);
    int numSeats = 10;
    if (args.length > 2) {
      numSeats = Integer.parseInt(args[2]);
    }
    TicketServer server = new SingleTicketServer(numSeats);
    ServerCommManager manager = new ServerCommManager(server, address, port);
    manager.start();
  }
}
