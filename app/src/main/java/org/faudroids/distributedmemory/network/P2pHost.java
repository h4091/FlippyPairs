package org.faudroids.distributedmemory.network;


import com.google.common.base.Objects;

public final class P2pHost {

	private final String serviceName;
	private final String deviceAddress;

	public P2pHost(
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
		if (other == null || !(other instanceof P2pHost)) return false;
		P2pHost service = (P2pHost) other;
		return Objects.equal(serviceName, service.serviceName)
				&& Objects.equal(deviceAddress, service.deviceAddress);
	}


	@Override
	public int hashCode() {
		return Objects.hashCode(serviceName, deviceAddress);
	}

}
