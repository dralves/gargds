package edu.ut.dsi.tickets;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import edu.ut.dsi.tickets.client.RemoteServer;
import edu.ut.dsi.tickets.client.TicketClient;
import edu.ut.dsi.tickets.server.Comms;
import edu.ut.dsi.tickets.server.TicketServer;

/**
 * This test hides three remote replicated server instances behind a single server interface. Should behave as a single
 * server, but in fact calls are spread round robin across servers.
 * 
 * @author dralves
 * 
 */
public class NormalOperationReplicatedTicketServerTest extends TicketServerTest {

  public static final class MultiServer implements TicketServer {

    private Iterator<TicketServer> iterator;

    public MultiServer(List<TicketServer> servers) {
      this.iterator = Iterators.cycle(servers);
    }

    public int[] delete(String name) throws IOException {
      return this.iterator.next().delete(name);
    }

    public int[] reserve(String name, int count) throws IOException {
      return this.iterator.next().reserve(name, count);
    }

    public int[] search(String name) throws IOException {
      return this.iterator.next().search(name);
    }
  }

  private static Comms comms1;
  private static Comms comms2;
  private static Comms comms3;

  @BeforeClass
  public static void setUp() throws Exception {
    String servers = "localhost:60000:61000;localhost:60010:61010;localhost:60020:61020";
    comms1 = new ServerMain().start(2 + "", "localhost", "60000", "61000", servers);
    comms2 = new ServerMain().start(2 + "", "localhost", "60010", "61010", servers);
    comms3 = new ServerMain().start(2 + "", "localhost", "60020", "61020", servers);
    comms1.join(false);
    comms2.join(false);
    comms3.join(false);
    TicketServer server1 = new RemoteServer(new TicketClient("localhost", 60000));
    TicketServer server2 = new RemoteServer(new TicketClient("localhost", 60010));
    TicketServer server3 = new RemoteServer(new TicketClient("localhost", 60020));
    server = new MultiServer(Lists.newArrayList(server1, server2, server3));// , server3));// , server2, server3));
  }

}
