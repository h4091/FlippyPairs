package org.faudroids.distributedmemory.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.network.ClientSocketHandler;
import org.faudroids.distributedmemory.network.HostSocketHandler;
import org.faudroids.distributedmemory.network.P2pConnectionListener;
import org.faudroids.distributedmemory.network.P2pHost;
import org.faudroids.distributedmemory.network.P2pManager;
import org.faudroids.distributedmemory.network.ServiceDiscoveryListener;
import org.faudroids.distributedmemory.network.ServiceRegistrationListener;

import java.net.InetAddress;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import timber.log.Timber;


public class P2pActivity extends BaseActivity implements
		ServiceDiscoveryListener,
		P2pConnectionListener {

	private static final String SERVICE_NAME = "serviceTest";

	@Inject Context appContext;
	@Inject P2pManager p2pManager;
	@Inject HostSocketHandler hostSocketHandler;
	ClientSocketHandler clientSocketHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_p2p);
		ButterKnife.inject(this);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.shutdown:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onDestroy() {
		if (hostSocketHandler != null) hostSocketHandler.shutdown();
		if (clientSocketHandler != null) clientSocketHandler.shutdown();
		p2pManager.shutdown();
		super.onDestroy();
	}


	@Override
	public void onResume() {
		p2pManager.startServiceRegistration(SERVICE_NAME, new ServiceRegistrationListener() {
					@Override
					public void onRegistrationSuccess(String serviceName) {
						Timber.i("registration successful");
					}

					@Override
					public void onRegistrationError(String serviceName) {
						Timber.i("registration failed");
					}
				});
		p2pManager.startServiceDiscovery(this);
		p2pManager.registerP2pConnectionListener(this);
		super.onResume();
	}


	@Override
	public void onPause() {
		p2pManager.stopServiceDiscovery();
		p2pManager.stopServiceDiscovery();
		p2pManager.unregisterP2pConnectionListener(this);
		super.onPause();
	}


	@Override
	public void onNewService(P2pHost service) {
		Timber.i("discovered service " + service.getServiceName());
		p2pManager.connectTo(service);
	}


	@Override
	public void onClientConnected(InetAddress hostAddress) {
		Timber.i("Connected to host " + hostAddress);
		// TODO this should be inject as well -> factory?
		new ClientSocketHandler(hostAddress).start();
	}


	@Override
	public void onHostConnected() {
		Timber.i("Host connected");
		hostSocketHandler.start();
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
