package org.faudroids.distributedmemory.network;


import java.net.InetAddress;

public interface P2pConnectionListener {

	/**
	 * Called when this device is a client peer.
	 */
	public void onClientConnected(InetAddress groupOwnerAddress);

	/**
	 * Called when this device is the group leader.
	 */
	public void onHostConnected();

}
