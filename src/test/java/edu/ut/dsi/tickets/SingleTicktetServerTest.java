package edu.ut.dsi.tickets;

import java.util.concurrent.locks.ReentrantLock;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import edu.ut.dsi.tickets.server.ReservationManager;
import edu.ut.dsi.tickets.server.ServerInfo;
import edu.ut.dsi.tickets.server.reservations.LocalReservationStore;

public class SingleTicktetServerTest extends TicketServerTest {

  @BeforeClass
  public synchronized static void setUp() {
    server = new ReservationManager(new LocalReservationStore(10), new ReentrantLock(), new ServerInfo(null, 0, 0, 0));
  }

  @AfterClass
  public synchronized static void tearDown() {
    server = null;
  }

}
