package org.faudroids.distributedmemory.core;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public enum GameState {

	CONNECTING,
	SETUP,
	SELECT_1ST_CARD,
	SELECT_2ND_CARD,
	UPDATE_CARDS,
	FINISHED;

	static {
		CONNECTING.nextStates.addAll(EnumSet.of(GameState.SETUP));
		SETUP.nextStates.addAll(EnumSet.of(GameState.SELECT_1ST_CARD));
		SELECT_1ST_CARD.nextStates.addAll(EnumSet.of(GameState.SELECT_2ND_CARD));
		SELECT_2ND_CARD.nextStates.addAll(EnumSet.of(GameState.UPDATE_CARDS));
		UPDATE_CARDS.nextStates.addAll(EnumSet.of(GameState.SELECT_1ST_CARD, GameState.FINISHED));
	}

	private final Set<GameState> nextStates = new HashSet<>();


	public boolean isValidNextState(GameState state) {
		return nextStates.contains(state);
	}


	public Set<GameState> getNextStates() {
		return nextStates;
	}

}
