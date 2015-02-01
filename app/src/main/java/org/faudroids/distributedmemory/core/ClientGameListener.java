package org.faudroids.distributedmemory.core;


import java.util.Collection;

public interface ClientGameListener {

	/**
	 * Called once when the host hast started the game and this client
	 * can start selecting cards.
	 */
	public void onGameStarted();


	/**
	 * Called when a local (!) card has been matched with either another local
	 * card or a remote one. Use this to notify the user about the great news.
	 * @param matchedCards Local (!) cards that matched. Can contain one or two
	 *                     elements.
	 */
	public void onCardsMatch(Collection<Card> matchedCards);


	/**
	 * Called when a local (!) card has failed to match with anther one.
	 * @param mismatchedCards Local (!) cards that matched. Can contain one or two
	 *                     elements.
	 */
	public void onCardsMismatch(Collection<Card> mismatchedCards);


	/**
	 * Called when a new round is about to start.
	 */
	public void onNewRound(Player currentPlayer, int playerPoints);


	/**
	 * Called once when the game has finished (no errors).
	 */
	public void onGameFinished();


	/**
	 * Indicates that the host was unable to process the last request (e.g. select a card).
	 * Client should refresh the UI to get a confirmed state.
	 */
	public void onHostBusy();


	/**
	 * There was an error communicating with the host.
	 */
	public void onHostLost();
}
