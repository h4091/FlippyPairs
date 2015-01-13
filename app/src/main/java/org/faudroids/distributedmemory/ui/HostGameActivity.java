package org.faudroids.distributedmemory.ui;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.network.P2pManager;
import org.faudroids.distributedmemory.network.ServiceRegistrationListener;

import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;


public class HostGameActivity extends BaseActivity implements
		ServiceRegistrationListener {

	@Inject P2pManager p2pManager;
	@InjectView(R.id.host_start) Button startHostButton;
	@InjectView(R.id.host_stop) Button stopHostButton;
	@InjectView(R.id.host_name) EditText serviceNameEditText;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_game);

		WifiP2pManager wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		this.p2pManager = new P2pManager(wifiP2pManager, wifiP2pManager.initialize(this, getMainLooper(), null));
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


	@OnClick(R.id.host_start)
	public void startHost() {
		startHostButton.setEnabled(false);
		p2pManager.startServiceRegistration(serviceNameEditText.getText().toString(), this);
		p2pManager.startServiceDiscovery();
	}


	@OnClick(R.id.host_stop)
	public void stopHost() {
		stopHostButton.setEnabled(false);
		p2pManager.stopServiceRegistration();
		startHostButton.setEnabled(true);
	}


	@Override
	public void onRegistrationSuccess(String serviceName) {
		stopHostButton.setEnabled(true);
	}


	@Override
	public void onRegistrationError(String serviceName) {
		startHostButton.setEnabled(true);
	}

}
