package org.faudroids.distributedmemory.core;


import org.faudroids.distributedmemory.utils.Assert;

import javax.inject.Inject;

public final class GameStateManager {

	private GameState currentState;

	@Inject
	public GameStateManager() {
		this.currentState = GameState.CONNECTING;
	}


	public GameState getState() {
		return currentState;
	}


	public void changeState(GameState state) {
		Assert.assertTrue(currentState.isValidNextState(state), "invalid next state");
		this.currentState = state;
	}

}
