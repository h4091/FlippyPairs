package org.faudroids.distributedmemory.network;


import java.net.Socket;

interface ClientConnectionListener {

	public void onClientConnected(Socket hostSocket);

}
