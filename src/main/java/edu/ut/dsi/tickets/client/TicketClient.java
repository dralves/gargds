package edu.ut.dsi.tickets.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import edu.ut.dsi.tickets.MethodRequest;
import edu.ut.dsi.tickets.MethodResponse;

public class TicketClient {

	private int    port;
	private String address;
	private Socket socket;
	private int timeOut;

	public TicketClient(String serverAddress, int port, int timeOut) {
		this.address = serverAddress;
		this.port = port;
		this.timeOut = timeOut;
	}

	public TicketClient(String address, int port) {
		this(address,port,0);
	}

	private void connect() throws UnknownHostException, IOException {
		if (socket == null) {
			this.socket = new Socket();
			this.socket.connect(new InetSocketAddress(this.address, this.port));
		}
	}

	public MethodResponse send(MethodRequest request) throws IOException {
		Timer timer = new Timer();
		MethodResponse response = null;
		timer.schedule(new EmptyTask() , timeOut*1000);
		connect();
		request.write(new DataOutputStream(socket.getOutputStream()));
		response = new MethodResponse();
		response.read(new DataInputStream(socket.getInputStream()));
		timer.cancel();
		return response;
	}

	public int getTimeOut() {
		return timeOut;
	}
}
class EmptyTask extends TimerTask{

	@Override
	public void run() {
		// do nothing 
	}
	
}
