package org.faudroids.distributedmemory.core;


import org.faudroids.distributedmemory.network.BroadcastMessage;
import org.faudroids.distributedmemory.utils.Assert;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Same as {@link org.faudroids.distributedmemory.core.GameStateManager} except it can
 * also handle state transition that require acks from clients before being executed.
 */
final class HostGameStateManager extends GameStateManager {

	private HostStateTransition stateTransition = null;
	private HostStateTransitionListener stateTransitionListener = null;

	@Inject
	public HostGameStateManager() { }


	@Override
	public void reset() {
		super.reset();
		stateTransition = null;
	}


	public void registerStateTransitionListener(HostStateTransitionListener stateTransitionListener) {
		Assert.assertTrue(this.stateTransitionListener == null, "already registered");
		this.stateTransitionListener = stateTransitionListener;
	}


	public void unregisterStateTransitionListener() {
		Assert.assertTrue(this.stateTransitionListener != null, "not registered");
		this.stateTransitionListener = null;
	}


	/**
	 * Starts a state transition by sending the broadcast and waiting for all acks from clients
	 * before actually changing the state.
	 */
	public void startStateTransition(BroadcastMessage<?> broadcastMessage, GameState nextState) {
		Assert.assertTrue(stateTransition == null || stateTransition.isComplete(), "previous state transition not yet finished!");
		Timber.d("Starting host game state transition to " + nextState);
		stateTransition = new HostStateTransition(new HostStateTransitionListener() {
			@Override
			public void onTransitionFinished(GameState nextState) {
				stateTransition = null;
				changeState(nextState);
				if (stateTransitionListener != null) stateTransitionListener.onTransitionFinished(nextState);
			}
		},  nextState,  broadcastMessage);
		stateTransition.startTransition();
	}


	public boolean onAckReceived() {
		Assert.assertTrue(stateTransition != null && !stateTransition.isComplete(), "no state transition in progress");
		return stateTransition.onAckReceived();
	}


	public boolean isStateTransitionComplete() {
		return stateTransition == null;
	}

}
