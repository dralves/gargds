package edu.ut.dsi.tickets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Arrays;

import edu.ut.dsi.tickets.MethodRequest.Method;
import edu.ut.dsi.tickets.client.TicketClient;

public class ClientMain {

	public static void main(String[] args) {
		String allServers = args[0];
		String timeOut="5"; // in seconds
		if(args.length>1)
		 timeOut = args[1];
		if(allServers ==null)
			allServers = "localhost:60000:61000;localhost:60010:61010;localhost:60020:61020";
		String[] servers = allServers.split(";");
		//		for(String s : servers)
		//			knownServers.add(s);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			Method method;
			String name;
			MethodRequest request;
			MethodResponse response;
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
			
			String[] serverConfig = servers[0].split(":");
			String serverName = serverConfig[0];
			String serverPort = serverConfig[1];
			TicketClient client = new TicketClient(serverName, Integer.parseInt(serverPort));
			 response = processRequest(request,client,servers);
			} catch (Exception e) {
				System.out.println("Error building request.");
				continue;
			}
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

	private static MethodResponse processRequest(MethodRequest request,TicketClient client,String[] knownServers) throws NumberFormatException, UnknownHostException, IOException {
		MethodResponse response = null;
		try{
			response = client.send(request);
		}catch(IOException e){
			// handle time out here too 
			response = null;
		}
		if(response==null){
			int i = knownServers.length-1;
			String[] servers = new String[knownServers.length];
			for(;i>=0;i--){
				if(knownServers[i]!=null){
					String[] serverConfig = knownServers[i].split(":");
					String serverName = serverConfig[0];
					String serverPort = serverConfig[1];
					client = new TicketClient(serverName, Integer.parseInt(serverPort));
					servers[i] = null;
					response = processRequest(request, client,servers);
				}
			}
		}
		return response;
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
