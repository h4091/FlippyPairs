package org.faudroids.distributedmemory.ui;


import android.content.Intent;
import android.os.IBinder;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.common.BaseService;
import org.faudroids.distributedmemory.core.ClientGameManager;

import java.util.List;

import javax.inject.Inject;

public final class ClientService extends BaseService {

	private static final int NOTIFICATION_ID = 423;

	@Inject ClientGameManager clientGameManager;


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
