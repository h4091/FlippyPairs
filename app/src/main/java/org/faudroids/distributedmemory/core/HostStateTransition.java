package org.faudroids.distributedmemory.core;

import android.os.Handler;
import android.os.Looper;

import org.faudroids.distributedmemory.network.BroadcastMessage;

/**
 * Manages the transition from one state to another by processing acks and setting
 * the new state once all clients have acknowledge the new state.
 */
final class HostStateTransition {

	// Used to postpone callbacks to the listener
	private static final Handler handler = new Handler(Looper.myLooper());

	private final HostStateTransitionListener transitionListener;
	private final GameState nextState;
	private final BroadcastMessage broadcastMessage;

	public HostStateTransition(HostStateTransitionListener transitionListener, GameState nextState, BroadcastMessage broadcastMessage) {
		this.transitionListener = transitionListener;
		this.nextState = nextState;
		this.broadcastMessage = broadcastMessage;
	}


	public void startTransition() {
		broadcastMessage.sendMessage();
	}


	public boolean onAckReceived() {
		boolean allAcksReceived = broadcastMessage.onAckReceived();
		if (allAcksReceived) handler.post(new Runnable() {
			@Override
			public void run() {
				transitionListener.onTransitionFinished(nextState);
			}
		});
		return allAcksReceived;
	}


	public boolean isComplete() {
		return broadcastMessage.allAcksReceived();
	}


}
