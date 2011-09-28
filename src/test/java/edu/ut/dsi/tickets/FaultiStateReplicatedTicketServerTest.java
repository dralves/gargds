package edu.ut.dsi.tickets;

import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import edu.ut.dsi.tickets.client.RemoteServer;
import edu.ut.dsi.tickets.client.TicketClient;
import edu.ut.dsi.tickets.server.Comms;
import edu.ut.dsi.tickets.server.TicketServer;

public class FaultiStateReplicatedTicketServerTest {

  public static final class MultiServer implements TicketServer {

    private Iterator<TicketServer> iterator;

    public MultiServer(List<TicketServer> servers) {
      this.iterator = Iterators.cycle(servers);
    }

    public int[] delete(String name) throws IOException {
      TicketServer server;
      while (true) {
        try {
          server = iterator.next();
          return server.delete(name);
        } catch (Exception e) {
          server = iterator.next();
        }
      }
    }

    public int[] reserve(String name, int count) throws IOException {
      TicketServer server;
      while (true) {
        try {
          server = iterator.next();
          return server.reserve(name, count);
        } catch (Exception e) {
          server = iterator.next();
        }
      }
    }

    public int[] search(String name) throws IOException {
      TicketServer server;
      while (true) {
        try {
          server = iterator.next();
          return server.search(name);
        } catch (Exception e) {
          server = iterator.next();
        }
      }
    }
  }

  private static Comms       comms1;
  private static Comms       comms2;
  private static Comms       comms3;
  private static MultiServer server;
  private static String      servers;

  @BeforeClass
  public static void setUp() throws Exception {
    servers = "localhost:60000:61000;localhost:60010:61010;localhost:60020:61020";
    comms1 = new ServerMain().start(2 + "", "localhost", "60000", "61000", servers);
    comms2 = new ServerMain().start(2 + "", "localhost", "60010", "61010", servers);
    comms3 = new ServerMain().start(2 + "", "localhost", "60020", "61020", servers);
    comms1.join(false);
    comms2.join(false);
    comms3.join(false);
    TicketServer server1 = new RemoteServer(new TicketClient("localhost", 60000));
    TicketServer server2 = new RemoteServer(new TicketClient("localhost", 60010));
    TicketServer server3 = new RemoteServer(new TicketClient("localhost", 60020));
    server = new MultiServer(Lists.newArrayList(server1, server2, server3));
  }

  @AfterClass
  public static void tearDown() throws Exception {
    comms1.stop();
    comms2.stop();
    comms3.stop();
  }

  @Test
  public void testFaultiOperation() throws IOException, InterruptedException {
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

    Thread.sleep(500);
    comms2.stop();
    Thread.sleep(1000);

    // server.delete("alice");
    // server.delete("bob");
    // server.delete("carl");
    // server.delete("david");
    //
    // seats = server.reserve("alice", 1);
    // assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")", arrayEquals(seats, new int[] { 0
    // }));
    // System.out.println("ALICE");
    // seats = server.reserve("bob", 2);
    // assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
    // arrayEquals(seats, new int[] { 1, 2 }));
    // System.out.println("BOB");
    // seats = server.reserve("carl", 3);
    // assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
    // arrayEquals(seats, new int[] { 3, 4, 5 }));
    // System.out.println("CARL");
    // seats = server.reserve("david", 4);
    // assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
    // arrayEquals(seats, new int[] { 6, 7, 8, 9 }));

    comms2 = new ServerMain().start(2 + "", "localhost", "60010", "61010", servers);
    comms2.join(true);
    Thread.sleep(1000);

    // server.delete("alice");
    // server.delete("bob");
    // server.delete("carl");
    // server.delete("david");
    // seats = server.reserve("alice", 1);
    // assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")", arrayEquals(seats, new int[] { 0
    // }));
    // System.out.println("ALICE");
    // seats = server.reserve("bob", 2);
    // assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
    // arrayEquals(seats, new int[] { 1, 2 }));
    // System.out.println("BOB");
    // seats = server.reserve("carl", 3);
    // assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
    // arrayEquals(seats, new int[] { 3, 4, 5 }));
    // System.out.println("CARL");
    // seats = server.reserve("david", 4);
    // assertTrue("The arrays did not match (actual: " + Arrays.toString(seats) + ")",
    // arrayEquals(seats, new int[] { 6, 7, 8, 9 }));
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
