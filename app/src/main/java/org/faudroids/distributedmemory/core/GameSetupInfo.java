package org.faudroids.distributedmemory.core;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Info for clients to setup the local playing field.
 */
final class GameSetupInfo {

	private final Map<Integer, Integer> cards;
	private final int startingPlayerIdx;
	private final List<Player> players;

	@JsonCreator
	public GameSetupInfo(
			@JsonProperty("cards") Map<Integer, Integer> cards,
			@JsonProperty("startingPlayerIdx") int startingPlayerIdx,
			@JsonProperty("players") List<Player> players) {

		this.cards = cards;
		this.startingPlayerIdx = startingPlayerIdx;
		this.players = players;
	}


	public Map<Integer, Integer> getCards() {
		return cards;
	}


	public int getStartingPlayerIdx() {
		return startingPlayerIdx;
	}


	public List<Player> getPlayers() {
		return players;
	}

}
