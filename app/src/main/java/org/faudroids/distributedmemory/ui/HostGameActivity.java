package org.faudroids.distributedmemory.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.utils.ServiceUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class HostGameActivity extends BaseActivity {

	@Inject ServiceUtils serviceUtils;

	@InjectView(R.id.start_hosting) Button startHostingButton;
	@InjectView(R.id.stop_hosting) Button stopHostingButton;

	private final BroadcastReceiver serverStateReceiver = new ServerStateBroadcastReceiver();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_game);
		ButterKnife.inject(this);
	}


	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(serverStateReceiver, new IntentFilter(HostService.ACTION_SERVER_STATE_CHANGED));
		toggleStartStopButtons(serviceUtils.isServiceRunning(HostService.class));
	}


	@Override
	public void onPause() {
		unregisterReceiver(serverStateReceiver);
		super.onPause();
	}


	@OnClick(R.id.start_hosting)
	public void startHosting() {
		startHostingButton.setEnabled(false);

		Intent hostIntent = new Intent(this, HostService.class);
		startService(hostIntent);

		Intent lobbyIntent = new Intent(this, LobbyActivity.class);
		startActivity(lobbyIntent);
	}


	@OnClick(R.id.stop_hosting)
	public void stopHosting() {
		stopHostingButton.setEnabled(false);

		Intent hostIntent = new Intent(this, HostService.class);
		stopService(hostIntent);
	}


	private void toggleStartStopButtons(boolean serverRunning) {
		if (serverRunning) {
			startHostingButton.setEnabled(false);
			stopHostingButton.setEnabled(true);
		} else {
			startHostingButton.setEnabled(true);
			stopHostingButton.setEnabled(false);
		}
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


	private class ServerStateBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean started = intent.getBooleanExtra(HostService.EXTRA_SERVER_RUNNING, false);
			toggleStartStopButtons(started);
		}

	}

}
