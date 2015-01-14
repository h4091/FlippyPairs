package org.faudroids.distributedmemory.network;


import java.net.InetAddress;

public interface NetworkListener {

	public void onRegistrationSuccess();
	public void onRegistrationError();

	public void onServiceDiscovered(String hostName, InetAddress hostAddress, int hostPort);
	public void onServiceLost(String hostName);
	public void onServiceDiscoveryError();

}
