package org.faudroids.distributedmemory.network;


public interface HostNetworkListener {

	public void onServerStartSuccess();
	public void onServerStartError();
	public void onConnectedToClient(ConnectionHandler<String> connectionHandler);
	public void onServerStoppedSuccess();
	public void onServerStoppedError();

}
