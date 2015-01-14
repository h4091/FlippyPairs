package org.faudroids.distributedmemory.ui;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.network.P2pHost;
import org.faudroids.distributedmemory.network.P2pManager;
import org.faudroids.distributedmemory.network.ServiceDiscoveryListener;
import org.faudroids.distributedmemory.network.ServiceRegistrationListener;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;


public class HostGameActivity extends BaseActivity implements
		ServiceDiscoveryListener,
		ServiceRegistrationListener {

	private final String SERVICE_NAME = "serviceTest";

	@Inject Context appContext;
	@Inject P2pManager p2pManager;

	@InjectView(R.id.host_start) Button startHostButton;
	@InjectView(R.id.host_stop) Button stopHostButton;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_game);
		ButterKnife.inject(this);

		if (p2pManager.isServiceRegistrationStarted(SERVICE_NAME)) {
			startHostButton.setEnabled(false);
			stopHostButton.setEnabled(true);
		} else {
			startHostButton.setEnabled(true);
			stopHostButton.setEnabled(false);
		}
	}


	@OnClick(R.id.host_start)
	public void startHost() {
		startHostButton.setEnabled(false);
		// p2pManager.registerServiceDiscoveryListener(this);
		p2pManager.startServiceRegistration(SERVICE_NAME, this);
		// p2pManager.startServiceDiscovery();
	}


	@OnClick(R.id.host_stop)
	public void stopHost() {
		stopHostButton.setEnabled(false);
		p2pManager.stopServiceRegistration();
		startHostButton.setEnabled(true);
		// stopService(new Intent(appContext, LobbyService.class));
	}


	@Override
	public void onRegistrationSuccess(String serviceName) {
		// startService(new Intent(appContext, LobbyService.class));
		stopHostButton.setEnabled(true);
	}


	@Override
	public void onRegistrationError(String serviceName) {
		startHostButton.setEnabled(true);
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


	@Override
	public void onNewService(P2pHost service) {
		Timber.i("discovered service " + service.getServiceName());
	}
}
