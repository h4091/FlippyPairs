package org.faudroids.distributedmemory.network;


public interface ServiceDiscoveryListener {

	public void onNewService(P2pService service);

}
