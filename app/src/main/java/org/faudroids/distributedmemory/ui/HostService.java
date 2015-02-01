package org.faudroids.distributedmemory.ui;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseService;
import org.faudroids.distributedmemory.core.Device;
import org.faudroids.distributedmemory.core.HostGameListener;
import org.faudroids.distributedmemory.core.HostGameManager;
import org.faudroids.distributedmemory.network.ClientNetworkListener;
import org.faudroids.distributedmemory.network.ConnectionHandler;
import org.faudroids.distributedmemory.network.HostInfo;
import org.faudroids.distributedmemory.network.HostNetworkListener;
import org.faudroids.distributedmemory.network.NetworkManager;
import org.faudroids.distributedmemory.utils.NotificationUtils;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public final class HostService extends BaseService {

	private static final int NOTIFICATION_ID = 422;

	static final String ACTION_HOST_STATE_CHANGED = "org.faudroids.distributedmemory.ACTION_HOST_STATE_CHANGED";
	static final String EXTRA_HOST_RUNNING = "EXTRA_HOST_RUNNING";

	static final String ACTION_STOP_GAME = "org.faudroids.distributedmemory.ACTION_STOP_GAME";

	private final HostNetworkListener<JsonNode> networkListener = new NetworkListener();
	private final HostGameListener gameListener = new GameListener();


	@Inject NetworkManager networkManager;
	@Inject NotificationManager notificationManager;
	@Inject NotificationUtils notificationUtils;
	@Inject HostGameManager hostGameManager;
	@Inject ClientUtils clientUtils;


	@Override
	public void onCreate() {
		super.onCreate();
		networkManager.startServer("AwesomeGame", networkListener, new Handler(getMainLooper()));
		hostGameManager.registerHostGameListener(gameListener);

		Notification notification = notificationUtils.createOngoingNotification(
				getString(R.string.service_host_starting_title),
				getString(R.string.service_host_start_message),
				R.drawable.ic_notify_host,
				HostGameActivity.class);
		notificationManager.notify(NOTIFICATION_ID, notification);

		sendHostStartedBroadcast();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}


	@Override
	public void onDestroy() {
		notificationManager.cancel(NOTIFICATION_ID);
		if (networkManager.isServerRunning()) networkManager.stopServer();
		hostGameManager.unregisterHostGameListener(gameListener);
		hostGameManager.stopGame();
		sendHostStoppedBroadcast();
		super.onDestroy();
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	private void sendHostStartedBroadcast() {
		Intent intent = new Intent(ACTION_HOST_STATE_CHANGED);
		intent.putExtra(EXTRA_HOST_RUNNING, true);
		sendBroadcast(intent);
	}


	private void sendHostStoppedBroadcast() {
		Intent intent = new Intent(ACTION_HOST_STATE_CHANGED);
		intent.putExtra(EXTRA_HOST_RUNNING, false);
		sendBroadcast(intent);
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


	private final class NetworkListener implements HostNetworkListener<JsonNode>, ClientNetworkListener<JsonNode> {

		@Override
		public void onServerStartSuccess(HostInfo hostInfo) {
			// start host notification
			Context context = HostService.this;
			Intent stopGameIntent = new Intent(ACTION_STOP_GAME);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, stopGameIntent, 0);
			NotificationUtils.Action action = new NotificationUtils.Action(R.drawable.ic_notify_host_stop, getString(R.string.service_host_running_stop), pendingIntent);
			Notification notification = notificationUtils.createOngoingNotification(
					getString(R.string.service_host_running_title),
					getString(R.string.service_host_running_message),
					R.drawable.ic_notify_host,
					LobbyActivity.class,
					Arrays.asList(action));
			notificationManager.notify(NOTIFICATION_ID, notification);

			// start local client
			networkManager.connectToHost(hostInfo, this, new Handler(Looper.getMainLooper()));
		}

		@Override
		public void onServerStartError() {
			Toast.makeText(HostService.this, "Failed to start server!", Toast.LENGTH_LONG).show();
			stopSelf();
		}

		@Override
		public void onConnectedToClient(ConnectionHandler<JsonNode> connectionHandler) {
			hostGameManager.addDevice(connectionHandler);
		}

		@Override
		public void onServerStoppedSuccess() {
			// server socket is closed, nothing to do
		}

		@Override
		public void onServerStoppedError() {
			// server socket is closed, nothing to do
		}

		@Override
		public void onServiceDiscovered(HostInfo hostInfo) {
			// not used since there is no discovery for the local client
		}

		@Override
		public void onServiceLost(String hostName) {
			// not used since there is no discovery for the local client
		}

		@Override
		public void onServiceDiscoveryError() {
			// not used since there is no discovery for the local client
		}

		@Override
		public void onConnectedToHostSuccess(ConnectionHandler<JsonNode> connectionHandler) {
			clientUtils.setupClient(connectionHandler);
		}

		@Override
		public void onConnectedToHostError() {
			Toast.makeText(HostService.this, "Failed to join game!", Toast.LENGTH_LONG).show();
			stopSelf();
		}
	}


	private final class GameListener implements HostGameListener {

		@Override
		public void onClientAdded(Device device) { }

		@Override
		public void onGameStarted() {
			// stop accepting new client connections --> close server socket
			networkManager.stopServer();
		}

		@Override
		public void onGameStopped() {
			stopSelf();
		}


		@Override
		public void onClientLost(Device device) {
			// TODO Panic! For now simply shut down server ...
			// stops the host game manager and notifies clients about loosing connection to the host
			stopSelf();
		}

	}

}
