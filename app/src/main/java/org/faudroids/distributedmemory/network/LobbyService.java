package org.faudroids.distributedmemory.network;


import android.content.Intent;
import android.os.IBinder;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.common.BaseService;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

public final class LobbyService extends BaseService implements P2pConnectionListener {

	@Inject P2pManager p2pManager;
	private final List<InetAddress> peers = new LinkedList<>();


	@Override
	public int onStartCommand(Intent intent,int flags, int startId) {
		p2pManager.registerP2pConnectionListener(this);
		return START_STICKY;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public void onDestroy() {
		p2pManager.shutdown();
	}


	@Override
	protected List<Object> getModules() {
		return Lists.newArrayList();
	}


	@Override
	public void onConnected(InetAddress hostAddress) {
		this.peers.add(hostAddress);
	}

}
