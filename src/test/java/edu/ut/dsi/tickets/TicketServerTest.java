package edu.ut.dsi.tickets;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import edu.ut.dsi.tickets.server.TicketServer;

public abstract class TicketServerTest {

  protected static TicketServer server;

  @Test
  public void testReserveMoreSeatsThatAvailable() throws Exception {
    int[] seats = server.reserve("test", 11);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
        arrayEquals(seats, Response.NOT_FOUND));
  }

  @Test
  public void testReserve() {
    int[] seats = server.reserve("alice", 1);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")", arrayEquals(seats, new int[] { 0 }));
    seats = server.reserve("bob", 2);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
        arrayEquals(seats, new int[] { 1, 2 }));
    seats = server.reserve("carl", 3);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
        arrayEquals(seats, new int[] { 3, 4, 5 }));
    seats = server.reserve("david", 4);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
        arrayEquals(seats, new int[] { 6, 7, 8, 9 }));
  }

  @Test
  public void testReservationsFull() {
    int[] result = server.reserve("eva", 1);
    assertTrue("The arrays did not match (actual: " + Arrays.toString(result) + ")",
        arrayEquals(result, Response.NOT_FOUND));
  }

  @Test
  public void testSearch() {
    int[] seats = server.search("alice");
    assertNotNull(seats);
    assertSame(1, seats.length);
    assertSame(0, seats[0]);
  }

  @Test
  public void testDelete() {
    // empty some seats (from a full server)
    int[] seats = server.delete("david");
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
        arrayEquals(result, Response.NOT_FOUND));
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
