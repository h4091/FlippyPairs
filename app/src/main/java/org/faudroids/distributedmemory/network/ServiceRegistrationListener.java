package org.faudroids.distributedmemory.network;


public interface ServiceRegistrationListener {

	public void onRegistrationSuccess(String serviceName);
	public void onRegistrationError(String serviceName);

}
