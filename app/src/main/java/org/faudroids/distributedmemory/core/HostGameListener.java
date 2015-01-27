package org.faudroids.distributedmemory.core;


public interface HostGameListener {

	/**
	 * A client has connected to the game manager.
	 */
    public void onClientAdded(Device device);


	/**
	 * The game has started and no new clients are accepted.
	 */
	public void onGameStarted();


	/**
	 * Game over.
	 */
	public void onGameStopped();


	/**
	 * There was an error communicating with a client.
	 */
	public void onClientLost(Device device);

}
