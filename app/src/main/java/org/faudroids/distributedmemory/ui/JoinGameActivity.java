package org.faudroids.distributedmemory.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.common.BaseListActivity;
import org.faudroids.distributedmemory.network.P2pConnectionListener;
import org.faudroids.distributedmemory.network.P2pHost;
import org.faudroids.distributedmemory.network.P2pManager;
import org.faudroids.distributedmemory.network.ServiceDiscoveryListener;

import java.net.InetAddress;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;


public class JoinGameActivity
		extends BaseListActivity
		implements ServiceDiscoveryListener {

	private ArrayAdapter<P2pHost> adapter;
	@Inject P2pManager p2pManager;
	@Inject Context appContext;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1);
		adapter.addAll(p2pManager.getAllDiscoveredServices());
		setListAdapter(adapter);
	}


	@Override
	protected void onListItemClick(ListView list, View v, int position, long id) {
		P2pHost service = adapter.getItem(position);
		p2pManager.connectTo(service);
	}


	@Override
	public void onNewService(P2pHost service) {
		adapter.clear();
		adapter.addAll(p2pManager.getAllDiscoveredServices());
		adapter.sort(new Comparator<P2pHost>() {
			@Override
			public int compare(P2pHost lhs, P2pHost rhs) {
				return lhs.toString().compareTo(rhs.toString());
			}
		});
		adapter.notifyDataSetChanged();
		onContentChanged();
	}


	@Override
	public void onResume() {
		super.onResume();

		// p2pManager.registerServiceDiscoveryListener((ServiceDiscoveryListener) this);
		// p2pManager.startServiceDiscovery();

		p2pManager.registerP2pConnectionListener((P2pConnectionListener) this);
	}


	@Override
	public void onPause() {
		super.onPause();

		p2pManager.stopServiceDiscovery();
		// p2pManager.unregisterServiceDiscoveryListener((ServiceDiscoveryListener) this);

		p2pManager.unregisterP2pConnectionListener((P2pConnectionListener) this);
	}


	// @Override
	public void onClientConnected(InetAddress hostAddress) {
		Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();
		startActivity(new Intent(appContext, ClientGameActivity.class));
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
