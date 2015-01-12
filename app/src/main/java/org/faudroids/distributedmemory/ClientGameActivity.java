package org.faudroids.distributedmemory;

import android.app.ListActivity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.faudroids.distributedmemory.network.P2pConnectionListener;
import org.faudroids.distributedmemory.network.P2pManager;
import org.faudroids.distributedmemory.network.P2pService;
import org.faudroids.distributedmemory.network.ServiceDiscoveryListener;

import java.net.InetAddress;
import java.util.Comparator;


public class ClientGameActivity
		extends ListActivity
		implements ServiceDiscoveryListener,
		P2pConnectionListener {

	private ArrayAdapter<P2pService> adapter;
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
	protected void onListItemClick(ListView list, View v, int position, long id) {
		P2pService service = adapter.getItem(position);
		p2pManager.connectTo(service, false);
	}


	@Override
	public void onNewService(P2pService service) {
		adapter.clear();
		adapter.addAll(p2pManager.getAllDiscoveredServices());
		adapter.sort(new Comparator<P2pService>() {
			@Override
			public int compare(P2pService lhs, P2pService rhs) {
				return lhs.toString().compareTo(rhs.toString());
			}
		});
		adapter.notifyDataSetChanged();
		onContentChanged();
	}


	@Override
	public void onResume() {
		super.onResume();
		p2pManager.register(this, this);
	}


	@Override
	public void onPause() {
		super.onPause();
		p2pManager.unregister(this, this);
	}


	@Override
	public void onConnected(InetAddress hostAddress) {
		Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();
	}

}
