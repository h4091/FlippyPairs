package org.faudroids.distributedmemory.network;


import org.faudroids.distributedmemory.utils.Assert;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
final class HostSocketHandler {

	private ServerRunnable serverRunnable;


	@Inject
	public HostSocketHandler() { }


	public int start(ClientConnectionListener clientConnectionListener) throws IOException {
		Assert.assertFalse(isRunning(), "server already running");

		ServerSocket serverSocket = new ServerSocket(0);
		this.serverRunnable = new ServerRunnable(
				serverSocket,
				clientConnectionListener);
		new Thread(serverRunnable).start();
		Timber.d("Hosting on port " + serverSocket.getLocalPort());
		return serverSocket.getLocalPort();
	}


	public boolean isRunning() {
		return serverRunnable != null;
	}


	public void shutdown() {
		Assert.assertTrue(isRunning(), "server not running");

		serverRunnable.shutdown();
		serverRunnable = null;
	}


	private static final class ServerRunnable implements Runnable {

		private final ServerSocket serverSocket;
		private final ClientConnectionListener clientConnectionListener;

		private boolean alive = true;

		public ServerRunnable(
				ServerSocket serverSocket,
				ClientConnectionListener clientConnectionListener) {

			this.serverSocket = serverSocket;
			this.clientConnectionListener = clientConnectionListener;
		}


		public void shutdown() {
			alive = false;
			try {
				serverSocket.close();
			} catch (IOException ioe) {
				Timber.w("failed to close server socket");
			}
		}


		@Override
		public void run() {
			while (alive) {
				try {
					Socket socket = serverSocket.accept();
					clientConnectionListener.onClientConnected(socket);
				} catch(IOException ioe) {
					if (alive) Timber.e(ioe, "failed to accept client");
				}
			}
			try {
				serverSocket.close();
			} catch (IOException ioe) {
				Timber.w(ioe, "failed to close server socket");
			}
		}

	}


	static interface ClientConnectionListener {

		public void onClientConnected(Socket hostSocket);

	}
}
