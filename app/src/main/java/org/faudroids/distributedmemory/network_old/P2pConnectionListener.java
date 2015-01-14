package org.faudroids.distributedmemory.network_old;


import java.net.InetAddress;
import java.util.List;

public interface P2pConnectionListener {

	/**
	 * Called when this device is a client peer.
	 */
	public void onClientConnected(InetAddress groupOwnerAddress);

	/**
	 * Called when this device is the group leader.
	 */
	public void onHostConnected();


	/**
	 * Called when the list of clients that this device is connected to becomes available.
	 */
	public void onClientsListChanged(List<String> clients);

}
