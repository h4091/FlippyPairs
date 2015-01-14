package org.faudroids.distributedmemory.network;


import java.io.IOException;
import java.net.ServerSocket;
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

	static final int PORT = 4582;
	private ServerRunnable serverRunnable;

	@Inject
	public HostSocketHandler() { }


	public void start() {
		try {
			serverRunnable = new ServerRunnable(
					new ServerSocket(PORT),
					new ThreadPoolExecutor(10, 10, 10, TimeUnit.DAYS.SECONDS, new LinkedBlockingDeque<Runnable>()));
			new Thread(serverRunnable).start();
		} catch(IOException ioe) {
			Timber.e(ioe, "failed to start server");
		}
	}


	public void shutdown() {
		if (serverRunnable != null) serverRunnable.shutdown();
		serverRunnable = null;
	}


	private static final class ServerRunnable implements Runnable {

		private final ServerSocket serverSocket;
		private final ExecutorService executorService;
		private final List<HostHandler> hostHandlers = new LinkedList<>();

		private boolean alive = true;

		public ServerRunnable(ServerSocket serverSocket, ExecutorService executorService) {
			this.serverSocket = serverSocket;
			this.executorService = executorService;
		}


		public void shutdown() {
			this.alive = false;
			for (HostHandler handler : hostHandlers) handler.shutdown();
		}


		@Override
		public void run() {
			while (alive) {
				try {
					HostHandler handler = new HostHandler(serverSocket.accept());
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