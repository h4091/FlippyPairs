package org.faudroids.distributedmemory.network;


import java.net.InetAddress;

public final class Host {

	private final String name;
	private final InetAddress address;
	private final int port;

	public Host(String name, InetAddress address, int port) {
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

}
