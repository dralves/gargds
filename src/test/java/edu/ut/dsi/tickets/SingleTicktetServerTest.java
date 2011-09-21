package edu.ut.dsi.tickets;

import org.junit.BeforeClass;

import edu.ut.dsi.tickets.server.ReservationManager;

public class SingleTicktetServerTest extends TicketServerTest {

  @BeforeClass
  public static void setUp() {
    server = new ReservationManager(10);
  }

}
