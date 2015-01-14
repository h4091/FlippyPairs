package org.faudroids.distributedmemory.network_old;


public interface ServiceRegistrationListener {

	public void onRegistrationSuccess(String serviceName);
	public void onRegistrationError(String serviceName);

}
