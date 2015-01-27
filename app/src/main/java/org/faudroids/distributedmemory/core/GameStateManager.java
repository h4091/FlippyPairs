package org.faudroids.distributedmemory.core;


import org.faudroids.distributedmemory.utils.Assert;

import javax.inject.Inject;

class GameStateManager {

	private GameState currentState;

	@Inject
	public GameStateManager() {
		this.currentState = GameState.CONNECTING;
	}


	public final GameState getState() {
		return currentState;
	}


	/**
	 * Sets the new local game state.
	 */
	public final void changeState(GameState state) {
		Assert.assertTrue(currentState.isValidNextState(state), "cannot change state from " + currentState + " to " + state);
		this.currentState = state;
	}

}
