package org.faudroids.distributedmemory.network;


import android.os.Handler;

import java.io.IOException;

/**
 * Handler for communication with another peer / host / client.
 */
public interface ConnectionHandler {

	public void start() throws IOException;
	public void stop() throws IOException;

	public void sendMessage(String msg);
	public void registerMessageListener(MessageListener listener, Handler handler);
	public void unregisterMessageListener();

}

