package org.faudroids.distributedmemory.network;


public interface HostListener {

	public void onServerStartSuccess();
	public void onServerStartError();
	public void onConnectedToClient(ConnectionHandler connectionHandler);

}
