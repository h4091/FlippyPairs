package org.faudroids.distributedmemory.ui;

import org.faudroids.distributedmemory.network.HostInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Inject;

final class QRCodeUtils {

	@Inject
	public QRCodeUtils() { }


	public HostInfo readHostInfo(String scanResult) {
		if (scanResult == null) return null;
		String[] items = scanResult.split("-");
		if (items.length != 3) return null;

		try {
			String name = items[0];
			InetAddress address = InetAddress.getByName(items[1]);
			int port = Integer.valueOf(items[2]);
			return new HostInfo(name, address, port);
		} catch (UnknownHostException | NumberFormatException e) {
			return null;
		}
	}


	public String writeHostInfo(HostInfo hostInfo) {
		return hostInfo.getName() + "-" + hostInfo.getAddress().getHostAddress() + "-" + hostInfo.getPort();
	}

}
