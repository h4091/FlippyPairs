package org.faudroids.distributedmemory.network;


import android.os.Handler;

/**
 * Handler for communication with another peer / host / client.
 */
public interface ConnectionHandler {

	public void start();
	public void stop();

	public void sendMessage(String msg);
	public void registerMessageListener(MessageListener listener, Handler handler);
	public void unregisterMessageListener();


	static interface MessageListener {

		public void onNewMessage(String msg);

	}
}

