package org.faudroids.distributedmemory.network;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

import timber.log.Timber;

public final class HostHandler implements Runnable {

	private final Socket socket;
	private boolean alive = true;

	// for testing
	private final List<String> connectedClients;

	public HostHandler(Socket socket, List<String> connectedClients) {
		this.socket = socket;
		this.connectedClients = connectedClients;
	}


	@Override
	public void run() {
		Timber.i("handling host side of communication!");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			connectedClients.add(reader.readLine());
			reader.close();
			Timber.i("Finished host side");
		} catch (IOException ioe) {
			Timber.e(ioe, "client communication failed");
		}
	}


	public void shutdown() {
		alive = false;
	}

}
