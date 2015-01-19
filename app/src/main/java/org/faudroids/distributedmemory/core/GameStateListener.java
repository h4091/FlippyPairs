package org.faudroids.distributedmemory.core;

public interface GameStateListener {

	public void onNewGameState(GameState oldState, GameState newState);

}
