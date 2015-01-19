package org.faudroids.distributedmemory.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseListActivity;
import org.faudroids.distributedmemory.core.Device;
import org.faudroids.distributedmemory.core.HostGameManager;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;


public class LobbyActivity extends BaseListActivity {

	@Inject HostGameManager hostGameManager;
	private ArrayAdapter<String> adapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		setListAdapter(adapter);
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
