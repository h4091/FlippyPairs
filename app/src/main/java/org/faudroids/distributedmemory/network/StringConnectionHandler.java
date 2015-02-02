package org.faudroids.distributedmemory.network;


import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import timber.log.Timber;

final class StringConnectionHandler implements ConnectionHandler<String> {

	private final Socket socket;
	private final MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter();

	private final OutputThread outputThread;
	private final InputThread inputThread;


	StringConnectionHandler(Socket socket) throws IOException {
		this.socket = socket;
		this.outputThread = new OutputThread(socket.getOutputStream(), messageListenerAdapter);
		this.inputThread = new InputThread(socket.getInputStream(), messageListenerAdapter);
	}


	StringConnectionHandler(InetAddress inetAddress, int port) throws IOException {
		this.socket = new Socket();
		this.socket.bind(null);
		this.socket.connect(new InetSocketAddress(inetAddress.getHostAddress(), port));
		this.outputThread = new OutputThread(socket.getOutputStream(), messageListenerAdapter);
		this.inputThread = new InputThread(socket.getInputStream(), messageListenerAdapter);
	}


	@Override
	public void start() {
		outputThread.start();
		inputThread.start();
	}


	@Override
	public void stop() {
		outputThread.interrupt();
		inputThread.interrupt();
		try {
			socket.close();
		} catch (IOException ioe) {
			Timber.w(ioe, "failed to close socket");
		}
	}


	@Override
	public void sendMessage(String msg) {
		outputThread.sendMessage(msg);
	}


	@Override
	public void registerMessageListener(MessageListener listener, Handler handler) {
		messageListenerAdapter.registerTargetListener(listener, handler);
	}


	@Override
	public void unregisterMessageListener() {
		messageListenerAdapter.unregisterTargetListener();
	}


	/**
	 * Returns messages that were not delivered to any {@link org.faudroids.distributedmemory.network.ConnectionHandler.MessageListener}
	 * due to none being registered via {@link #registerMessageListener(MessageListener, android.os.Handler)}.
	 */
	public List<String> getUndeliveredMessages() {
		return messageListenerAdapter.getUndeliveredMessages();
	}


	/**
	 * Returns whether there was a connection error that has not been delivered due to no listener
	 * beeing registered.
	 */
	public boolean getUnsentConnectionError() {
		return messageListenerAdapter.getUnsentConnectionError();
	}


	private static final class MessageListenerAdapter implements MessageListener<String> {

		private final List<String> unsentMessages = new LinkedList<>();
		private boolean unsentConnectionError = false;

		private Handler handler = null;
		private MessageListener<String> targetListener = null;

		@Override
		public void onNewMessage(final String msg) {
			synchronized (this) {
				if (handler != null && targetListener != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							targetListener.onNewMessage(msg);
						}
					});
				} else {
					unsentMessages.add(msg);
				}
			}
		}

		@Override
		public void onConnectionError() {
			synchronized (this) {
				unsentMessages.clear();
				if (handler != null && targetListener != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							targetListener.onConnectionError();
						}
					});
				} else {
					unsentConnectionError = true;
				}
			}
		}

		public void registerTargetListener(MessageListener<String> targetListener, Handler handler) {
			synchronized (this) {
				this.targetListener = targetListener;
				this.handler = handler;
			}
		}

		public void unregisterTargetListener() {
			synchronized (this) {
				this.targetListener = null;
				this.handler = null;
			}
		}

		public List<String> getUndeliveredMessages() {
			synchronized (this) {
				List<String> messages = new LinkedList<>(unsentMessages);
				unsentMessages.clear();
				return messages;
			}
		}


		public boolean getUnsentConnectionError() {
			boolean connectionError = unsentConnectionError;
			unsentConnectionError = false;
			return connectionError;
		}

	}


	private static final class OutputThread extends Thread {

		private final MessageListener messageListener;
		private final ObjectOutputStream objectOutputStream;
		private final BlockingQueue<String> messageQueue;
		private final BackoffStrategy strategy;

		public OutputThread(final OutputStream outputStream, final MessageListener messageListener) throws IOException {
			this.objectOutputStream = new ObjectOutputStream(outputStream);
			this.messageListener = messageListener;
			this.messageQueue = new LinkedBlockingDeque<>();
			this.strategy = new BackoffStrategy() {
				@Override
				protected void doWork() throws Throwable {
					try {
						String msg = messageQueue.take();
						objectOutputStream.writeObject(msg);
						objectOutputStream.flush();
					} catch (InterruptedException ie) {
						interrupt();
					}
				}
			};
		}

		@Override
		public void run() {
			try {
				while (!isInterrupted()) {
					strategy.work();
				}
			} catch (Throwable t) {
				try {
					objectOutputStream.close();
				} catch (IOException ioe) {
					Timber.w("failed to close connection", ioe);
				}
				messageListener.onConnectionError();
			}
		}

		public void sendMessage(String msg) {
			messageQueue.add(msg);
		}

	}


	private static final class InputThread extends Thread {

		private final MessageListener<String> messageListener;
		private final ObjectInputStream objectInputStream;
		private final BackoffStrategy strategy;

		public InputThread(final InputStream inputStream, final MessageListener messageListener) throws IOException {
			this.objectInputStream = new ObjectInputStream(inputStream);
			this.messageListener = messageListener;
			this.strategy = new BackoffStrategy() {
				@Override
				protected void doWork() throws Throwable {
					String msg = (String) objectInputStream.readObject();
					messageListener.onNewMessage(msg);
				}
			};
		}

		@Override
		public void run() {
			try {
				while (!isInterrupted()) {
					strategy.work();
				}
			} catch (Throwable t) {
				try {
					objectInputStream.close();
				} catch (IOException ioe) {
					Timber.w("failed to close connection", ioe);
				}
				messageListener.onConnectionError();
			}
		}

	}


	/**
	 * Implements a simple backoff strategy in case of errors.
	 */
	private static abstract class BackoffStrategy {

		private static final int MAX_RETRY = 3;
		private static final int INITIAL_BACKOFF = 20;
		private static final int BACKOFF_EXPONENT = 2;

		private int currentRetry = 0;
		private int currentBackoff = INITIAL_BACKOFF;

		public void work() throws Throwable {
			try {
				doWork();

				// reset since doWork was successful
				currentRetry = 0;
				currentBackoff = 0;

			} catch (Throwable t) {
				Timber.d(t, "failed to perform work");
				if (currentRetry > MAX_RETRY) throw new Exception("failed after " + MAX_RETRY + "retries", t);

				// backoff and retry
				++currentRetry;
				currentBackoff *= BACKOFF_EXPONENT;
				try {
					Thread.sleep(currentBackoff);
				} catch (InterruptedException e) {
					throw new Exception(e);
				}
			}
		}

		protected abstract void doWork() throws Throwable;

	}

}
