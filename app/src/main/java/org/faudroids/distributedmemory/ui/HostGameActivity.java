package org.faudroids.distributedmemory.ui;

import android.content.Intent;
import android.os.Bundle;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class HostGameActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_game);
		ButterKnife.inject(this);
	}


	@OnClick(R.id.start_hosting)
	public void startHosting() {
		Intent hostIntent = new Intent(this, HostService.class);
		startService(hostIntent);

		Intent lobbyIntent = new Intent(this, LobbyActivity.class);
		startActivity(lobbyIntent);
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
