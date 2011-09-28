package edu.ut.dsi.tickets;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ut.dsi.tickets.server.TicketServer;

public abstract class TicketServerTest {

  private static final Logger   LOG       = LoggerFactory.getLogger(TicketServerTest.class);

  protected static TicketServer server;

  static {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, Throwable e) {
        LOG.error("Uncaught Exception in thread: " + t.getName(), e);

      }
    });
  }

  @Rule
  public ErrorCollector         collector = new ErrorCollector();

  // @After
  public void dump() {
    StringBuilder sb = new StringBuilder();
    Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
    for (Thread t : stacks.keySet()) {
      sb.append(t.toString()).append('\n');
      for (StackTraceElement ste : t.getStackTrace()) {
        sb.append("\tat ").append(ste.toString()).append('\n');
      }
      sb.append('\n');
    }
    LOG.info(sb.toString());

  }

  @Test(timeout = 10000)
  public void testReserveMoreSeatsThatAvailable() throws Exception {
    int[] seats = server.reserve("test", 11);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
        arrayEquals(seats, MethodResponse.NOT_FOUND));
  }

  @Test(timeout = 2000)
  public void testReserve() throws IOException {
    int[] seats = server.reserve("alice", 1);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")", arrayEquals(seats, new int[] { 0 }));
    System.out.println("ALICE");
    seats = server.reserve("bob", 2);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
        arrayEquals(seats, new int[] { 1, 2 }));
    System.out.println("BOB");
    seats = server.reserve("carl", 3);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
        arrayEquals(seats, new int[] { 3, 4, 5 }));
    System.out.println("CARL");
    seats = server.reserve("david", 4);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
        arrayEquals(seats, new int[] { 6, 7, 8, 9 }));
  }

  @Test(timeout = 100000)
  public void testReservationsFull() throws IOException {
    int[] result = server.reserve("eva", 1);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(result) + ")",
        arrayEquals(result, MethodResponse.NOT_FOUND));
  }

  @Test(timeout = 100000)
  public void testSearch() throws IOException {
    int[] seats = server.search("alice");
    assertNotNull(seats);
    assertSame(1, seats.length);
    assertSame(0, seats[0]);
  }

  @Test(timeout = 100000)
  public void testDelete() throws Exception {

    server.delete("alice");
    int[] seats = server.search("alice");
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
        arrayEquals(seats, new int[] { -1 }));

    server.reserve("alice", 1);
    seats = server.search("alice");
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
        arrayEquals(seats, new int[] { -0 }));

    // empty some seats (from a full server)
    seats = server.delete("david");
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
        arrayEquals(seats, new int[] { 6, 7, 8, 9 }));

    // should be able to do two two seat reservations
    seats = server.reserve("frank", 2);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
        arrayEquals(seats, new int[] { 6, 7 }));

    seats = server.reserve("gray", 2);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
        arrayEquals(seats, new int[] { 8, 9 }));

    // should be full again
    int[] result = server.reserve("harry", 1);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(result) + ")",
        arrayEquals(result, MethodResponse.NOT_FOUND));
  }

  @AfterClass
  public static void sleep() throws Exception {
    Thread.sleep(1000);
  }

  private boolean arrayEquals(int[] a1, int[] a2) {
    if (a1.length != a2.length) {
      return false;
    }
    for (int i = 0; i < a1.length; i++) {
      if (a1[i] != a2[i]) {
        return false;
      }
    }
    return true;
  }
}
