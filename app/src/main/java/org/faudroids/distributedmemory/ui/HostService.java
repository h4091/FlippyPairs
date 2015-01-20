package org.faudroids.distributedmemory.ui;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.common.BaseService;
import org.faudroids.distributedmemory.core.HostGameManager;
import org.faudroids.distributedmemory.network.ConnectionHandler;
import org.faudroids.distributedmemory.network.HostNetworkListener;
import org.faudroids.distributedmemory.network.NetworkManager;
import org.faudroids.distributedmemory.utils.NotificationUtils;

import java.util.List;

import javax.inject.Inject;

public final class HostService extends BaseService implements HostNetworkListener {

	private static final int NOTIFICATION_ID = 422;

	static final String ACTION_SERVER_STATE_CHANGED = "org.faudroids.distributedmemory.ACTION_SERVER_STATE_CHANGED";
	static final String EXTRA_SERVER_RUNNING = "EXTRA_SERVER_RUNNING";


	@Inject NetworkManager networkManager;
	@Inject NotificationManager notificationManager;
	@Inject NotificationUtils notificationUtils;
	@Inject HostGameManager hostGameManager;


	@Override
	public void onCreate() {
		super.onCreate();
		networkManager.startServer("AwesomeGame", this, new Handler(getMainLooper()));
		Notification notification = notificationUtils.createOngoingNotification(
				"Game starting",
				"Distributed memory game is about to be hosted ...",
				HostGameActivity.class);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}


	@Override
	public void onDestroy() {
		notificationManager.cancel(NOTIFICATION_ID);
		if (networkManager.isServerRunning()) networkManager.stopServer();
		super.onDestroy();
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public void onServerStartSuccess() {
		Notification notification = notificationUtils.createOngoingNotification(
				"Game Running",
				"You are hosting a distributed memory game!",
				HostGameActivity.class);
		notificationManager.notify(NOTIFICATION_ID, notification);
		sendServerStartedBroadcast();
	}


	@Override
	public void onServerStartError() {
		Toast.makeText(this, "Failed to start server!", Toast.LENGTH_LONG).show();
		stopSelf();
	}


	@Override
	public void onConnectedToClient(ConnectionHandler connectionHandler) {
		hostGameManager.addDevice(connectionHandler);
	}


	@Override
	public void onServerStoppedSuccess() {
		sendServerStoppedBroadcast();
	}


	@Override
	public void onServerStoppedError() {
		sendServerStoppedBroadcast();
	}


	private void sendServerStartedBroadcast() {
		Intent intent = new Intent(ACTION_SERVER_STATE_CHANGED);
		intent.putExtra(EXTRA_SERVER_RUNNING, true);
		sendBroadcast(intent);
	}


	private void sendServerStoppedBroadcast() {
		Intent intent = new Intent(ACTION_SERVER_STATE_CHANGED);
		intent.putExtra(EXTRA_SERVER_RUNNING, false);
		sendBroadcast(intent);
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}