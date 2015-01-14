package org.faudroids.distributedmemory.network;


public interface NetworkListener {

	public void onRegistrationSuccess();
	public void onRegistrationError();

	public void onServiceDiscovered(Host host);
	public void onServiceLost(String hostName);
	public void onServiceDiscoveryError();

}
