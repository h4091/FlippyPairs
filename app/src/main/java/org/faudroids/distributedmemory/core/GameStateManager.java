package org.faudroids.distributedmemory.core;


import org.faudroids.distributedmemory.utils.Assert;

import javax.inject.Inject;

class GameStateManager {

	private GameState currentState;

	@Inject
	public GameStateManager() {
		currentState = GameState.FINISHED;
	}


	public void reset() {
		currentState = GameState.CONNECTING;
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


	public void setEndState() {
		this.currentState = GameState.FINISHED;
	}

}
