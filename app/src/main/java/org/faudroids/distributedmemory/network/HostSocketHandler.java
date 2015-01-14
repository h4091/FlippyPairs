package org.faudroids.distributedmemory.network;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public final class HostSocketHandler {

	private final List<String> connectedClients = Collections.synchronizedList(new LinkedList<String>());
	private ServerRunnable serverRunnable;

	@Inject
	public HostSocketHandler() { }


	public int start() throws IOException {
		ServerSocket serverSocket = new ServerSocket(0);
		serverRunnable = new ServerRunnable(
				serverSocket,
				new ThreadPoolExecutor(10, 10, 10, TimeUnit.DAYS.SECONDS, new LinkedBlockingDeque<Runnable>()),
				connectedClients);
		new Thread(serverRunnable).start();
		Timber.i("Hosting on port " + serverSocket.getLocalPort());
		return serverSocket.getLocalPort();
	}


	public void shutdown() {
		if (serverRunnable != null) serverRunnable.shutdown();
		serverRunnable = null;
	}


	public List<String> getConnectedClients() {
		return connectedClients;
	}


	private static final class ServerRunnable implements Runnable {

		private final ServerSocket serverSocket;
		private final ExecutorService executorService;
		private final List<HostHandler> hostHandlers = new LinkedList<>();
		private final List<String> connectedClients;

		private boolean alive = true;

		public ServerRunnable(ServerSocket serverSocket, ExecutorService executorService, List<String> connectedClients) {
			this.serverSocket = serverSocket;
			this.executorService = executorService;
			this.connectedClients = connectedClients;
		}


		public void shutdown() {
			this.alive = false;
			for (HostHandler handler : hostHandlers) handler.shutdown();
		}


		@Override
		public void run() {
			while (alive) {
				try {
					HostHandler handler = new HostHandler(serverSocket.accept(), connectedClients);
					hostHandlers.add(handler);
					executorService.execute(handler);
				} catch(IOException ioe) {
					Timber.e(ioe, "failed to accept client");
				}
			}
			executorService.shutdownNow();
			try {
				serverSocket.close();
			} catch (IOException ioe) {
				Timber.e(ioe, "failed to close server socket");
			}
		}

	}

}
