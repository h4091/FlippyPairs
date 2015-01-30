package org.faudroids.distributedmemory.core;


public interface ClientGameListener {

	/**
	 * Called once when the host hast started the game and this client
	 * can start selecting cards.
	 */
	public void onGameStarted();


	/**
	 * Called when a local (!) card has change, e.g. from closed to opened or
	 * from opened to matched. Use this to update the UI.
	 */
	public void onCardsChanged();


	/**
	 * Called when a local (!) card has been matched with either another local
	 * card or a remote one. Use this to notify the user about the great news.
	 */
	public void onCardsMatch();


	/**
	 * Called when a local (!) card has failed to match with anther one.
	 */
	public void onCardsMismatch();


	/**
	 * Called when a new round is about to start.
	 */
	public void onNewRound();


	/**
	 * Called once when the game has finished (no errors).
	 */
	public void onGameFinished();


	/**
	 * There was an error communicating with the host.
	 */
	public void onHostLost();
}
