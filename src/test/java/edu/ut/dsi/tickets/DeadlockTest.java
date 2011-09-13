package edu.ut.dsi.tickets;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.junit.Test;

import edu.ut.dsi.tickets.ClientServerTicketServerTest.TicketServerClient;
import edu.ut.dsi.tickets.client.TicketClient;
import edu.ut.dsi.tickets.server.TicketServer;

public class DeadlockTest {

  @Test(timeout = 2000)
  public void testDeadlock() throws Exception {
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
    TicketServer client1 = new TicketServerClient(new TicketClient("localhost", 60000));
    TicketServer client2 = new TicketServerClient(new TicketClient("localhost", 60000));
    client1.reserve("david", 10);
    client2.reserve("john", 3);
    client1.delete("david");
    client2.reserve("john", 3);
    // client1.reserve("david", 10);
  }

}
