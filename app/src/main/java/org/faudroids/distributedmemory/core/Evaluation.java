package org.faudroids.distributedmemory.core;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

final class Evaluation {

	private final boolean cardsMatched;
	private final boolean continueGame;
	private final int nextPlayerId;
	private final List<Integer> playerPoints; // mapped to index of players list


	@JsonCreator
	public Evaluation(
			@JsonProperty("cardsMatched") boolean cardsMatched,
			@JsonProperty("continueGame") boolean continueGame,
			@JsonProperty("nextPlayerId") int nextPlayerId,
			@JsonProperty("playerPoints") List<Integer> playerPoints) {

		this.cardsMatched = cardsMatched;
		this.continueGame = continueGame;
		this.nextPlayerId = nextPlayerId;
		this.playerPoints = playerPoints;
	}


	public boolean getCardsMatched() {
		return cardsMatched;
	}


	public boolean getContinueGame() {
		return continueGame;
	}


	public int getNextPlayerId() {
		return nextPlayerId;
	}


	public List<Integer> getPlayerPoints() {
		return playerPoints;
	}

}
