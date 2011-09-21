package edu.ut.dsi.tickets;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import edu.ut.dsi.tickets.client.TicketClient;
import edu.ut.dsi.tickets.client.TicketServerClient;

public class ClientServerTicketServerTest extends TicketServerTest {

  @BeforeClass
  public static void setUp() throws Exception {
    ServerMain.main("localhost", "60000");
    server = new TicketServerClient(new TicketClient("localhost", 60000), 0);
  }

  @AfterClass
  public static void tearDown() throws IOException {
    ServerMain.stop();
  }
}
