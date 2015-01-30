package org.faudroids.distributedmemory.network;


import android.os.Handler;

/**
 * Handler for communication with another peer / host / client.
 */
public interface ConnectionHandler<T> {

	public void start();
	public void stop();

	public void sendMessage(T msg);
	public void registerMessageListener(MessageListener<T> listener, Handler handler);
	public void unregisterMessageListener();


	static interface MessageListener<T> {

		/**
		 * A new message from the other side is available.
		 */
		public void onNewMessage(T msg);

		/**
		 * The connection to the other side is broken and will be closed.
		 */
		public void onConnectionError();

	}
}

