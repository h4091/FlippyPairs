package org.faudroids.distributedmemory.core;

/**
 * Called whenever the current state transition has finished.
 */
public interface HostStateTransitionListener {

	/**
	 * @param nextState the next state that was associated with the transition.
	 */
	public void onTransitionFinished(GameState nextState);

}
