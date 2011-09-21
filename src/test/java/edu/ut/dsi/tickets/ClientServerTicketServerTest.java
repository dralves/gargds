package edu.ut.dsi.tickets;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.junit.BeforeClass;

import edu.ut.dsi.tickets.client.TicketClient;
import edu.ut.dsi.tickets.client.TicketServerClient;

public class ClientServerTicketServerTest extends TicketServerTest {

  @BeforeClass
  public static void setUp() throws Exception {
    Executors.newSingleThreadExecutor().submit(new Runnable() {
      public void run() {
        try {
          ServerMain.main("localhost", "60000");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });
    Thread.sleep(500);
    server = new TicketServerClient(new TicketClient("localhost", 60000), 0);
  }
  
  @Override
  public void testReserve() {
    super.testReserve();
  }

}
