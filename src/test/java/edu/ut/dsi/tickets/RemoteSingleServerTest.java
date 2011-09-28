package edu.ut.dsi.tickets;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import edu.ut.dsi.tickets.client.RemoteServer;
import edu.ut.dsi.tickets.client.TicketClient;

public class RemoteSingleServerTest extends TicketServerTest {

  @BeforeClass
  public static void setUp() throws Exception {
    new ServerMain().start(1 + "", "localhost", "50000");
    server = new RemoteServer(new TicketClient("localhost", 50000));
  }

  @AfterClass
  public static void tearDown() throws IOException {
    ServerMain.stop();
  }
}
