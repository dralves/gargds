package edu.ut.dsi.tickets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import edu.ut.dsi.tickets.mutex.Clock;
import edu.ut.dsi.tickets.mutex.LamportMutexLock;
import edu.ut.dsi.tickets.server.Comms;
import edu.ut.dsi.tickets.server.ReservationManager;
import edu.ut.dsi.tickets.server.ServerInfo;
import edu.ut.dsi.tickets.server.reservations.LocalReservationStore;
import edu.ut.dsi.tickets.server.reservations.ReplicatedReservationStore;

public class ServerMain {

  private static Comms manager;

  public synchronized static void main(String... args) throws Exception {
    Comms comms = new ServerMain().start(args);
    synchronized (comms) {
      comms.wait();
    }
  }

  public Comms start(String... args) throws IOException {
    int assgnmt = Integer.parseInt(args[0]);
    String address = args[1];
    int clientPort = Integer.parseInt(args[2]);
    int numSeats = 10;
    ReservationManager server;
    ServerInfo me;
    switch (assgnmt) {
      case 1:
        me = new ServerInfo(address, clientPort, -1, 0);
        server = new ReservationManager(new LocalReservationStore(numSeats), new ReentrantLock(), me);
        manager = new Comms(server, me);
        break;
      case 2:
        int serverPort = Integer.parseInt(args[3]);
        List<ServerInfo> servers = servers(args[4]);
        me = findMe(servers, address, serverPort);
        Clock clock = new Clock(me.id, servers.size());
        LamportMutexLock lamportLock = new LamportMutexLock(clock);
        ReplicatedReservationStore store = new ReplicatedReservationStore(numSeats);
        server = new ReservationManager(store, lamportLock, me);
        manager = new Comms(server, me, servers);
        lamportLock.setComms(manager);
        manager.setLock(lamportLock);
        store.setComms(manager);
        break;
      default:
        throw new IllegalStateException();
    }
    manager.start();
    return manager;
  }

  public synchronized static void stop() throws IOException {
    manager.stop();
  }

  private static List<ServerInfo> servers(String serversString) {
    String[] addressAndPortTriplets = serversString.split(";");
    List<ServerInfo> servers = new ArrayList<ServerInfo>();
    for (int i = 0; i < addressAndPortTriplets.length; i++) {
      String[] addressAndPorts = addressAndPortTriplets[i].split(":");
      servers.add(new ServerInfo(addressAndPorts[0], Integer.parseInt(addressAndPorts[1]), Integer
          .parseInt(addressAndPorts[2]), i));
    }
    return servers;
  }

  private static ServerInfo findMe(List<ServerInfo> servers, String address, int serverPort) {
    for (ServerInfo server : servers) {
      if (server.address.equals(address) && server.serverPort == serverPort) {
        return server;
      }
    }
    throw new IllegalStateException();
  }
}
