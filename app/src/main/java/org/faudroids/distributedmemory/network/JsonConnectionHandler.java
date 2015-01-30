package org.faudroids.distributedmemory.network;


import android.os.Handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Handler for communication with another peer / host / client.
 */
final class JsonConnectionHandler implements ConnectionHandler<JsonNode> {

	private static final ObjectMapper mapper = new ObjectMapper();

	private final ConnectionHandler<String> connectionHandler;

	public JsonConnectionHandler(ConnectionHandler<String> connectionHandler) {
		this.connectionHandler = connectionHandler;
	}

	@Override
	public void start() {
		connectionHandler.start();
	}


	@Override
	public void stop() {
		connectionHandler.stop();
	}


	@Override
	public void sendMessage(JsonNode msg) {
		connectionHandler.sendMessage(msg.toString());
	}


	@Override
	public void registerMessageListener(MessageListener<JsonNode> listener, Handler handler) {
		connectionHandler.registerMessageListener(new MessageListenerAdapter(mapper, listener), handler);
	}


	@Override
	public void unregisterMessageListener() {
		connectionHandler.unregisterMessageListener();
	}


	private static final class MessageListenerAdapter implements MessageListener<String> {

		private final ObjectMapper mapper;
		private final MessageListener<JsonNode> targetListener;

		public MessageListenerAdapter(ObjectMapper mapper, MessageListener<JsonNode> targetListener) {
			this.mapper = mapper;
			this.targetListener = targetListener;
		}

		@Override
		public void onNewMessage(String msg) {
			try {
				JsonNode data = mapper.readTree(msg);
				targetListener.onNewMessage(data);
			} catch (IOException jpe) {
				throw new RuntimeException("failed to read JSON", jpe);
			}
		}

		@Override
		public void onConnectionError() {
			targetListener.onConnectionError();
		}

	}
}

