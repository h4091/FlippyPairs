package org.faudroids.distributedmemory.network;


public interface ClientNetworkListener<T> {

	public void onServiceDiscovered(HostInfo hostInfo);
	public void onServiceLost(String hostName);
	public void onServiceDiscoveryError();
	public void onConnectedToHostSuccess(ConnectionHandler<T> connectionHandler);
	public void onConnectedToHostError();

}
