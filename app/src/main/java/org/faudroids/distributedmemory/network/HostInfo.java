package org.faudroids.distributedmemory.network;


import com.google.common.base.Objects;

import java.net.InetAddress;

public final class HostInfo {

	private final String name;
	private final InetAddress address;
	private final int port;

	public HostInfo(String name, InetAddress address, int port) {
		this.name = name;
		this.address = address;
		this.port = port;
	}


	public String getName() {
		return name;
	}


	public InetAddress getAddress() {
		return address;
	}


	public int getPort() {
		return port;
	}


	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof HostInfo)) return false;
		if (other == this) return true;
		HostInfo info = (HostInfo) other;
		return Objects.equal(name, info.name)
				&& Objects.equal(address, info.address)
				&& Objects.equal(port, info.port);
	}


	@Override
	public int hashCode() {
		return Objects.hashCode(name, address, port);
	}


	@Override
	public String toString() {
		return name;
	}

}
