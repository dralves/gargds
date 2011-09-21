package edu.ut.dsi.tickets;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import edu.ut.dsi.tickets.server.ReservationManager;

public class SingleTicktetServerTest extends TicketServerTest {

  @BeforeClass
  public synchronized static void setUp() {
    server = new ReservationManager(10, 0);
  }

  @AfterClass
  public synchronized static void tearDown() {
    server = null;
  }

}
