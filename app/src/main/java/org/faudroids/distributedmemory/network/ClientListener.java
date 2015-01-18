package org.faudroids.distributedmemory.network;


public interface ClientListener {

	public void onServiceDiscovered(HostInfo hostInfo);
	public void onServiceLost(String hostName);
	public void onServiceDiscoveryError();
	public void onConnectedToHostSuccess(ConnectionHandler connectionHandler);
	public void onConnectedToHostError();

}
