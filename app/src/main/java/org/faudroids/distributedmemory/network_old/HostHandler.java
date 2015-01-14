package org.faudroids.distributedmemory.network_old;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import timber.log.Timber;

public final class HostHandler implements Runnable {

	private final Socket socket;
	private boolean alive = true;

	public HostHandler(Socket socket) {
		this.socket = socket;
	}


	@Override
	public void run() {
		Timber.i("handling host side of communication!");

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while (alive) {
				Timber.i(reader.readLine());
			}
			socket.close();
		} catch (IOException ioe) {
			Timber.e(ioe, "client communication failed");
		}
	}


	public void shutdown() {
		alive = false;
	}

}
