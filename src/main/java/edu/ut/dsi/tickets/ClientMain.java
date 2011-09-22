package edu.ut.dsi.tickets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Arrays;

import edu.ut.dsi.tickets.MethodRequest.Method;
import edu.ut.dsi.tickets.client.TicketClient;

public class ClientMain {

  public static void main(String[] args) throws UnknownHostException, IOException {
    TicketClient client = new TicketClient(args[0], Integer.parseInt(args[1]));
    client.connect();
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      Method method;
      String name;
      MethodRequest request;
      try {
        System.out.print("Method? (RESERVE,SEARCH,DELETE): ");
        method = Method.valueOf(reader.readLine().trim().toUpperCase());
        System.out.print("Name: ");
        name = reader.readLine().trim().toLowerCase();
        if (method == Method.RESERVE) {
          System.out.print("Number of seats: ");
          request = new MethodRequest(method, name, Integer.parseInt(reader.readLine().trim()));
        } else {
          request = new MethodRequest(method, name);
        }
      } catch (Exception e) {
        System.out.println("Error building request.");
        continue;
      }
      MethodResponse response = client.send(request);
      if (arrayEquals(response.values(), MethodResponse.NOT_FOUND)) {
        switch (method) {
          case RESERVE:
            System.out.println("There weren't enough seats available to go through with your reservation");
            break;
          default:
            System.out.println("There was no reservation under the name: " + name);
        }
      } else if (arrayEquals(response.values(), MethodResponse.ERROR)) {
        switch (method) {
          case RESERVE:
            System.out.println("There is already a reservation under name: " + name);
            break;
          default:
            System.out.println("An error occurred in the system.");
        }
      } else {
        switch (method) {
          case RESERVE:
            System.out.println("A reservation was placed under the name: " + name + " seats: "
                + Arrays.toString(response.values()));
            break;
          case SEARCH:
            System.out.println("A reservation was found under the name: " + name + " seats: "
                + Arrays.toString(response.values()));
            break;
          case DELETE:
            System.out.println("A reservation under the name: " + name + " was deleted. seats: "
                + Arrays.toString(response.values()));
            break;
        }
      }
    }
  }

  private static boolean arrayEquals(int[] a1, int[] a2) {
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
