package org.faudroids.distributedmemory.network;


public interface ClientNetworkListener {

	public void onServiceDiscovered(HostInfo hostInfo);
	public void onServiceLost(String hostName);
	public void onServiceDiscoveryError();
	public void onConnectedToHostSuccess(ConnectionHandler<String> connectionHandler);
	public void onConnectedToHostError();

}
