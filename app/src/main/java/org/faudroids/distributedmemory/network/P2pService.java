package org.faudroids.distributedmemory.network;


import com.google.common.base.Objects;

public final class P2pService {

	private final String serviceName;
	private final String deviceAddress;

	public P2pService(
			String serviceName,
			String deviceAddress) {

		this.serviceName = serviceName;
		this.deviceAddress = deviceAddress;
	}


	public String getServiceName() {
		return serviceName;
	}


	public String getDeviceAddress() {
		return deviceAddress;
	}


	@Override
	public String toString() {
		return serviceName + " (" + deviceAddress + ")";
	}


	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof P2pService)) return false;
		P2pService service = (P2pService) other;
		return Objects.equal(serviceName, service.serviceName)
				&& Objects.equal(deviceAddress, service.deviceAddress);
	}


	@Override
	public int hashCode() {
		return Objects.hashCode(serviceName, deviceAddress);
	}

}
