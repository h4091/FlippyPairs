package org.faudroids.distributedmemory.ui;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.network.HostSocketHandler;
import org.faudroids.distributedmemory.network.NetworkListener;
import org.faudroids.distributedmemory.network.NetworkManager;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;


public class HostGameActivity extends BaseActivity implements NetworkListener {

	private static final String SERVICE_NAME = "serviceTest";

	@Inject Context appContext;
	@Inject NetworkManager networkManager;
	@Inject HostSocketHandler hostSocketHandler;

	@InjectView(R.id.peers_list) ListView peersList;
	private ArrayAdapter<String> peersAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_game);
		ButterKnife.inject(this);

		peersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		peersList.setAdapter(peersAdapter);
	}


	@Override
	protected void onDestroy() {
		hostSocketHandler.shutdown();
		networkManager.unregisterService();
		super.onDestroy();
	}


	@Override
	public void onResume() {
		super.onResume();
		networkManager.startDiscovery(this);
	}


	@Override
	public void onPause() {
		networkManager.stopDiscovery();
		super.onPause();
	}


	@OnClick(R.id.start_hosting)
	public void startHosting() {
		try {
			int hostPort = hostSocketHandler.start();
			networkManager.registerService(SERVICE_NAME, hostPort, this);
		} catch (IOException ioe) {
			Timber.e(ioe, "failed to start host");
		}
	}


	@Override
	public void onRegistrationSuccess() {  }


	@Override
	public void onRegistrationError() {  }


	@Override
	public void onServiceDiscovered(String hostName, InetAddress hostAddress, int hostPort) {
		peersAdapter.add(hostName);
		peersAdapter.notifyDataSetChanged();
	}


	@Override
	public void onServiceLost(String hostName) {
		peersAdapter.remove(hostName);
		peersAdapter.notifyDataSetChanged();
	}


	@Override
	public void onServiceDiscoveryError() {  }


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
