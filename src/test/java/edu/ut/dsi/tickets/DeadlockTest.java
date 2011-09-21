package edu.ut.dsi.tickets;

import org.junit.Test;

import edu.ut.dsi.tickets.client.TicketClient;
import edu.ut.dsi.tickets.client.TicketServerClient;
import edu.ut.dsi.tickets.server.TicketServer;

public class DeadlockTest {

  @Test(timeout = 2000)
  public void testDeadlock() throws Exception {
    ServerMain.main("localhost", "60000");
    TicketServer client1 = new TicketServerClient(new TicketClient("localhost", 60000), 0);
    TicketServer client2 = new TicketServerClient(new TicketClient("localhost", 60000), 0);
    client1.reserve("david", 10);
    client2.reserve("john", 3);
    client1.delete("david");
    client2.reserve("john", 3);
    ServerMain.stop();
  }

}
