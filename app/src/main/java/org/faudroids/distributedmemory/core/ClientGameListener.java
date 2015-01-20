package org.faudroids.distributedmemory.core;


public interface ClientGameListener {

	/**
	 * Called once when the host hast started the game and this client
	 * is about to receive select the first card.
	 */
	public void onGameStarted();

}
