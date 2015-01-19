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

final class SimpleConnectionHandler implements ConnectionHandler {

	private final Socket socket;
	private final MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter();

	private final OutputThread outputThread;
	private final InputThread inputThread;


	SimpleConnectionHandler(Socket socket) throws IOException {
		this.socket = socket;
		this.outputThread = new OutputThread(socket.getOutputStream());
		this.inputThread = new InputThread(socket.getInputStream(), messageListenerAdapter);
	}


	SimpleConnectionHandler(InetAddress inetAddress, int port) throws IOException {
		this.socket = new Socket();
		this.socket.bind(null);
		this.socket.connect(new InetSocketAddress(inetAddress.getHostAddress(), port));
		this.outputThread = new OutputThread(socket.getOutputStream());
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
			Timber.e(ioe, "failed to close socket");
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


	private static final class MessageListenerAdapter implements MessageListener {

		private final List<String> unsentMessages = new LinkedList<>();

		private Handler handler = null;
		private MessageListener targetListener = null;

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

		public void registerTargetListener(MessageListener targetListener, Handler handler) {
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

	}


	private static final class OutputThread extends Thread {

		private final ObjectOutputStream outputStream;
		private final BlockingQueue<String> messageQueue;

		public OutputThread(OutputStream outputStream) throws IOException {
			this.outputStream = new ObjectOutputStream(outputStream);
			this.messageQueue = new LinkedBlockingDeque<>();
		}

		@Override
		public void run() {
			while (!isInterrupted()) {
				try {
					String msg = messageQueue.take();
					outputStream.writeObject(msg);
					outputStream.flush();

				} catch (InterruptedException ie) {
					interrupt();
				} catch (IOException ioe) {
					Timber.e(ioe, "failed send mesage");
				}
			}
		}

		public void sendMessage(String msg) {
			messageQueue.add(msg);
		}

	}


	private static final class InputThread extends Thread {

		private final ObjectInputStream inputStream;
		private final MessageListener messageListener;

		public InputThread(InputStream inputStream, MessageListener messageListener) throws IOException {
			this.inputStream = new ObjectInputStream(inputStream);
			this.messageListener = messageListener;
		}

		@Override
		public void run() {
			while (!isInterrupted()) {
				try {
					String msg = (String) inputStream.readObject();
					messageListener.onNewMessage(msg);

				} catch (IOException ioe) {
					Timber.e(ioe, "failed to receive message");
				} catch (ClassNotFoundException cnfe) {
					Timber.e(cnfe, "failed to deserialize message");
				}
			}
		}

	}

}
