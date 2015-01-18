package org.faudroids.distributedmemory.network;


public interface NetworkListener {

	public void onServerStartSuccess();
	public void onServerStartError();
	public void onConnectedToClient(ConnectionHandler connectionHandler);

	public void onServiceDiscovered(HostInfo hostInfo);
	public void onServiceLost(String hostName);
	public void onServiceDiscoveryError();
	public void onConnectedToHostSuccess(ConnectionHandler connectionHandler);
	public void onConnectedToHostError();

}
