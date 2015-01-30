package org.faudroids.distributedmemory.core;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

final class Evaluation {

	private final boolean cardsMatched;
	private final boolean continueGame;
	private final int nextPlayerId;
	private final List<Player> winners;


	@JsonCreator
	public Evaluation(
			@JsonProperty("cardsMatched") boolean cardsMatched,
			@JsonProperty("continueGame") boolean continueGame,
			@JsonProperty("nextPlayerId") int nextPlayerId,
			@JsonProperty("winners") List<Player> winners) {

		this.cardsMatched = cardsMatched;
		this.continueGame = continueGame;
		this.nextPlayerId = nextPlayerId;
		this.winners = winners;
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


	public List<Player> getWinners() {
		return winners;
	}

}
