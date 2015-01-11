package org.faudroids.distributedmemory;

import android.app.ListActivity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import org.faudroids.distributedmemory.network.P2pManager;
import org.faudroids.distributedmemory.network.ServiceDiscoveryListener;

import java.util.Comparator;


public class ClientGameActivity extends ListActivity implements ServiceDiscoveryListener {

	private ArrayAdapter<String> adapter;
	private P2pManager p2pManager;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WifiP2pManager wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		this.p2pManager = new P2pManager(wifiP2pManager, wifiP2pManager.initialize(this, getMainLooper(), null));
		this.p2pManager.register(this);
		this.p2pManager.startServiceDiscovery();

		this.adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1);
		adapter.addAll(p2pManager.getAllDiscoveredServices());
		setListAdapter(adapter);
	}


	@Override
	public void onNewService(String serviceName) {
		adapter.clear();
		adapter.addAll(p2pManager.getAllDiscoveredServices());
		adapter.sort(new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return lhs.compareTo(rhs);
			}
		});
		adapter.notifyDataSetChanged();
		onContentChanged();
	}

}
