package org.faudroids.distributedmemory.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseListActivity;
import org.faudroids.distributedmemory.core.ClientGameManager;
import org.faudroids.distributedmemory.network.ClientNetworkListener;
import org.faudroids.distributedmemory.network.ConnectionHandler;
import org.faudroids.distributedmemory.network.HostInfo;
import org.faudroids.distributedmemory.network.NetworkManager;

import java.util.List;

import javax.inject.Inject;


public class JoinGameActivity extends BaseListActivity implements ClientNetworkListener {

	@Inject ClientGameManager clientGameManager;
	@Inject NetworkManager networkManager;
	private ArrayAdapter<HostInfo> adapter;
	private ProgressDialog connectingToHostDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		setListAdapter(adapter);
	}


	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		HostInfo hostInfo =  adapter.getItem(position);
		networkManager.connectToHost(hostInfo, this, new Handler(getMainLooper()));
		connectingToHostDialog = ProgressDialog.show(this, "Connecting to host", "Please wait ...", false);
	}


	@Override
	public void onResume() {
		super.onResume();
		networkManager.startDiscovery(this, new Handler(getMainLooper()));
	}


	@Override
	public void onPause() {
		networkManager.stopDiscovery();
		super.onPause();
	}


	@Override
	public void onServiceDiscovered(HostInfo hostInfo) {
		adapter.add(hostInfo);
		adapter.notifyDataSetChanged();
	}


	@Override
	public void onServiceLost(String hostName) {
		for (int i = 0; i < adapter.getCount(); ++i) {
			if (adapter.getItem(i).getName().equals(hostName)) {
				adapter.remove(adapter.getItem(i));
				break;
			}
		}
		adapter.notifyDataSetChanged();
	}


	@Override
	public void onServiceDiscoveryError() {  }



	@Override
	public void onConnectedToHostSuccess(ConnectionHandler connectionHandler) {
		connectingToHostDialog.cancel();
		connectingToHostDialog = null;

		/*
		Intent serviceIntent = new Intent(this, ClientService.class);
		startService(serviceIntent);
		*/

		int cardsCount = getResources().getInteger(R.integer.grid_column_count)
				* getResources().getInteger(R.integer.grid_row_count);
		clientGameManager.registerDevice(connectionHandler, Build.DEVICE, cardsCount / 2);

		Intent intent = new Intent(this, GameActivity.class);
		startActivity(intent);
	}


	@Override
	public void onConnectedToHostError() {
		connectingToHostDialog.cancel();
		connectingToHostDialog = null;
		Toast.makeText(this, "Failed to join game!", Toast.LENGTH_LONG).show();
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
