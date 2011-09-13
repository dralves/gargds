package edu.ut.dsi.tickets;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

import org.junit.BeforeClass;

import edu.ut.dsi.tickets.Request.Method;
import edu.ut.dsi.tickets.client.TicketClient;
import edu.ut.dsi.tickets.server.TicketServer;

public class ClientServerTicketServerTest extends TicketServerTest {

  public static class TicketServerClient implements TicketServer {

    private TicketClient client;

    public TicketServerClient(TicketClient client) throws IOException {
      this.client = client;
      this.client.connect();
    }

    public int[] delete(String name) {
      try {
        return this.client.send(new Request(Method.DELETE, name)).values();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public int[] reserve(String name, int count) {
      try {
        return this.client.send(new Request(Method.RESERVE, name, count)).values();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public int[] search(String name) {
      try {
        return this.client.send(new Request(Method.SEARCH, name)).values();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

  }

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
    server = new TicketServerClient(new TicketClient("localhost", 60000));
  }

}
