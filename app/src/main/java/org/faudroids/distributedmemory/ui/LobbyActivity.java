package org.faudroids.distributedmemory.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.core.Device;
import org.faudroids.distributedmemory.core.HostGameManager;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;


public class LobbyActivity extends BaseActivity {

	@Inject HostGameManager hostGameManager;
	@InjectView(R.id.peers_list) ListView peersList;
	private ArrayAdapter<String> adapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);
		ButterKnife.inject(this);
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		peersList.setAdapter(adapter);
	}


	@OnClick(R.id.start_game)
	public void startGame() {
		hostGameManager.startGame();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_lobby, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.refresh:
				adapter.clear();
				List<Device> devices = hostGameManager.getConnectedDevices();
				for (Device device : devices) adapter.add(device.getName());
				adapter.notifyDataSetChanged();
				Timber.i("Called refresh and found " + adapter.getCount() + " elements");
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
