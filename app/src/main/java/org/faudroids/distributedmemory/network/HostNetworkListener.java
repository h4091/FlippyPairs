package org.faudroids.distributedmemory.network;


public interface HostNetworkListener<T> {

	public void onServerStartSuccess(HostInfo  hostInfo);
	public void onServerStartError(boolean serviceNameAlreadyTaken);
	public void onConnectedToClient(ConnectionHandler<T> connectionHandler);
	public void onServerStoppedSuccess();
	public void onServerStoppedError();

}
