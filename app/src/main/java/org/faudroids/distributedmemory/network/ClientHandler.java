package org.faudroids.distributedmemory.network;


import android.os.Build;

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
			writer.write(Build.MODEL);
			writer.newLine();
			writer.flush();
			writer.close();
			Timber.i("Finished client side");
		} catch (IOException e) {
			Timber.e(e, "server communication failed");
		}
	}


	public void shutdown() {
		alive = false;
	}

}
