package org.faudroids.distributedmemory.ui;

import android.app.NotificationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.network.P2pManager;
import org.faudroids.distributedmemory.utils.ServiceUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class ClientGameActivity extends BaseActivity {

	private static final int NOTIFICATION_ID = 44;

	@Inject UiUtils uiUtils;
	@Inject ServiceUtils serviceUtils;
	@Inject NotificationManager notificationManager;
	@Inject P2pManager p2pManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_client_game);
		ButterKnife.inject(this);

		notificationManager.notify(
				NOTIFICATION_ID,
				uiUtils.createGameRunningNotification(
						"Game running",
						"Distributed memory game in progress",
						ClientGameActivity.class));
	}


	@OnClick(R.id.disconnect)
	public void disconnectClient() {
		Toast.makeText(this, "Not much to do here ...", Toast.LENGTH_SHORT).show();
		notificationManager.cancel(NOTIFICATION_ID);
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
