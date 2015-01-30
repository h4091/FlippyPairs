package org.faudroids.distributedmemory.network;


import org.faudroids.distributedmemory.utils.Assert;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class BroadcastMessage<T> {

	private final List<ConnectionHandler<T>> connectionHandlers = new LinkedList<>();
	private final List<T> messages = new LinkedList<>();

	private int ackCount = 0;
	private boolean messageSent = false;


	public BroadcastMessage(Collection<ConnectionHandler<T>> connectionHandlers, List<T> messages) {
		Assert.assertEquals(connectionHandlers.size(), messages.size(), "messages size must be equal to number of connections");
		this.connectionHandlers.addAll(connectionHandlers);
		this.messages.addAll(messages);
	}


	public BroadcastMessage(Collection<ConnectionHandler<T>> connectionHandlers, T message) {
		this.connectionHandlers.addAll(connectionHandlers);
		for (int i = 0; i < connectionHandlers.size(); ++i) messages.add(message);
	}


	/**
	 * Forwards the message(s) to the connection handlers.
	 */
	public void sendMessage() {
		if (messageSent) throw new IllegalStateException("message has already been sent");
		messageSent = true;
		for (int i = 0; i < messages.size(); ++i) {
			connectionHandlers.get(i).sendMessage(messages.get(i));
		}
	}


	/**
	 * Call this method for every ack that has been received from a connection handler.
	 * @return same as {@link #allAcksReceived()}.
	 */
	public boolean onAckReceived() {
		if (!messageSent) throw new IllegalStateException("message has not been sent");
		Assert.assertTrue(ackCount < connectionHandlers.size(), "already received all acks");
		++ackCount;
		return allAcksReceived();
	}


	/**
	 * @return true if all acks for the last message have been received, false otherwise.
	 */
	public boolean allAcksReceived() {
		return ackCount == connectionHandlers.size();
	}

}
