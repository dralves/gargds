package edu.ut.dsi.tickets;

import org.junit.BeforeClass;

import edu.ut.dsi.tickets.server.SingleTicketServer;

public class SingleTicktetServerTest extends TicketServerTest {

  @BeforeClass
  public static void setUp() {
    server = new SingleTicketServer(10);
  }

}
