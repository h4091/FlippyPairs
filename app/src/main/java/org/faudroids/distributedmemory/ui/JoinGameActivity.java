package org.faudroids.distributedmemory.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.common.BaseListActivity;
import org.faudroids.distributedmemory.network.ConnectionHandler;
import org.faudroids.distributedmemory.network.HostInfo;
import org.faudroids.distributedmemory.network.NetworkListener;
import org.faudroids.distributedmemory.network.NetworkManager;

import java.util.List;

import javax.inject.Inject;


public class JoinGameActivity extends BaseListActivity implements NetworkListener {

	@Inject NetworkManager networkManager;
	private ArrayAdapter<HostInfo> adapter;


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
	public void onServerStartSuccess() {  }


	@Override
	public void onServerStartError()  { }


	@Override
	public void onConnectedToClient(ConnectionHandler connectionHandler) { }


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
		// TODO something smart here
	}


	@Override
	public void onConnectedToHostError() {
		// TODO something smart here
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
