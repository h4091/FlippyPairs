package org.faudroids.distributedmemory.network;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import timber.log.Timber;

public final class ClientHandler implements Runnable {

	private final Socket socket;
	private boolean alive = true;

	public ClientHandler(Socket socket) {
		this.socket = socket;
	}


	@Override
	public void run() {
		Timber.i("handling client side of communication!");
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			int count = 0;
			while (alive) {
				writer.write("Hello there " + count);
				writer.newLine();
				writer.flush();
				++count;
				Thread.sleep(2000);
			}
			socket.close();
		} catch (IOException | InterruptedException e) {
			Timber.e(e, "server communication failed");
		}
	}


	public void shutdown() {
		alive = false;
	}

}
