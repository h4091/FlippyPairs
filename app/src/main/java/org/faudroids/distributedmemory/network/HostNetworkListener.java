package org.faudroids.distributedmemory.network;


public interface HostNetworkListener<T> {

	public void onServerStartSuccess();
	public void onServerStartError();
	public void onConnectedToClient(ConnectionHandler<T> connectionHandler);
	public void onServerStoppedSuccess();
	public void onServerStoppedError();

}
