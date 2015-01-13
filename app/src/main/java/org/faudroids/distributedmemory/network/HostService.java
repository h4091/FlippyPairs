package org.faudroids.distributedmemory.network;


import android.content.Intent;
import android.os.IBinder;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.common.BaseService;

import java.util.List;

public final class HostService extends BaseService {


	@Override
	public int onStartCommand(Intent intent,int flags, int startId) {
		return START_STICKY;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	protected List<Object> getModules() {
		return Lists.newArrayList();
	}
}
