package edu.ut.dsi.tickets;

import org.junit.Test;

import edu.ut.dsi.tickets.client.TicketClient;
import edu.ut.dsi.tickets.client.RemoteServer;
import edu.ut.dsi.tickets.server.TicketServer;

public class DeadlockTest {

  @Test(timeout = 200000)
  public void testDeadlock() throws Exception {
    ServerMain.main(1 + "", "localhost", "60000");
    TicketServer client1 = new RemoteServer(new TicketClient("localhost", 60000));
    TicketServer client2 = new RemoteServer(new TicketClient("localhost", 60000));
    client1.reserve("david", 10);
    client2.reserve("john", 3);
    client1.delete("david");
    client2.reserve("john", 3);
    ServerMain.stop();
  }
}
