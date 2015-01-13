package org.faudroids.distributedmemory.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.network.HostService;
import org.faudroids.distributedmemory.network.P2pManager;
import org.faudroids.distributedmemory.network.ServiceRegistrationListener;
import org.faudroids.distributedmemory.utils.ServiceUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class HostGameActivity extends BaseActivity implements
		ServiceRegistrationListener {

	private static final int NOTIFICATION_ID = 42;

	@Inject Context appContext;
	@Inject NotificationManager notificationManager;
	@Inject P2pManager p2pManager;
	@Inject ServiceUtils serviceUtils;

	@InjectView(R.id.host_start) Button startHostButton;
	@InjectView(R.id.host_stop) Button stopHostButton;
	@InjectView(R.id.host_name) EditText serviceNameEditText;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_game);
		ButterKnife.inject(this);

		WifiP2pManager wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		this.p2pManager = new P2pManager(wifiP2pManager, wifiP2pManager.initialize(this, getMainLooper(), null));

		if (serviceUtils.isServiceRunning(HostService.class)) {
			startHostButton.setEnabled(false);
			stopHostButton.setEnabled(true);
		} else {
			startHostButton.setEnabled(true);
			stopHostButton.setEnabled(false);
		}
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


	@OnClick(R.id.host_start)
	public void startHost() {
		startHostButton.setEnabled(false);
		p2pManager.startServiceRegistration(serviceNameEditText.getText().toString(), this);
		p2pManager.startServiceDiscovery();
	}


	@OnClick(R.id.host_stop)
	public void stopHost() {
		stopHostButton.setEnabled(false);
		p2pManager.stopServiceRegistration();
		startHostButton.setEnabled(true);
		stopService(new Intent(appContext, HostService.class));
		stopRunningNotification();
	}


	@Override
	public void onRegistrationSuccess(String serviceName) {
		startService(new Intent(appContext, HostService.class));
		showRunningNotification();
		stopHostButton.setEnabled(true);
	}


	@Override
	public void onRegistrationError(String serviceName) {
		startHostButton.setEnabled(true);
	}


	private void showRunningNotification() {
		Intent intent = new Intent(this, HostGameActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Notification.Builder builder = new Notification.Builder(this)
				.setContentTitle("Hosting game")
				.setContentText("Hosting Distributed Memory Game in progress")
				.setSmallIcon(R.drawable.ic_launcher)
				.setOngoing(true)
				.setContentIntent(PendingIntent.getActivity(appContext, 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT));

		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}


	private void stopRunningNotification() {
		notificationManager.cancel(NOTIFICATION_ID);
	}

}
