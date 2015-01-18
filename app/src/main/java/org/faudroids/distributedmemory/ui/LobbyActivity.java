package org.faudroids.distributedmemory.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseListActivity;

import java.util.List;


public class LobbyActivity extends BaseListActivity {

	static final String KEY_IS_HOST = "IS_HOST";

	private ArrayAdapter<String> adapter;
	private boolean isHost;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		setListAdapter(adapter);
		isHost = getIntent().getBooleanExtra(KEY_IS_HOST, false);
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
				//adapter.addAll(hostSocketHandler.getConnectedClients());
				// adapter.notifyDataSetChanged();
				// Timber.i("Called refresh and found " + hostSocketHandler.getConnectedClients().size() + " elements");
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
