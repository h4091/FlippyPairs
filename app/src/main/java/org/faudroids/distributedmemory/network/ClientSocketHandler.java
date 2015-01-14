package org.faudroids.distributedmemory.network;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import timber.log.Timber;

public final class ClientSocketHandler {

	private final InetAddress hostAddress;
	private ClientHandler clientHandler;

	public ClientSocketHandler(InetAddress hostAddress) {
		this.hostAddress = hostAddress;
	}


	public void start() {
		new Thread() {
			@Override
			public void run() {
				try {
					Socket socket = new Socket();
					socket.bind(null);
					socket.connect(new InetSocketAddress(hostAddress.getHostAddress(), HostSocketHandler.PORT));
					clientHandler = new ClientHandler(socket);
					new Thread(clientHandler).start();

				} catch(IOException ioe) {
					Timber.e(ioe, "failed to connect to host");
				}
			}
		}.start();
	}


	public void shutdown() {
		Timber.i("stopping client socket handler");
		clientHandler.shutdown();
	}

}
