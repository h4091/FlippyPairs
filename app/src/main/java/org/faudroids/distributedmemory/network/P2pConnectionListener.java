package org.faudroids.distributedmemory.network;


import java.net.InetAddress;

public interface P2pConnectionListener {

	public void onConnected(InetAddress hostAddress);

}
