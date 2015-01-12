package org.faudroids.distributedmemory;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.faudroids.distributedmemory.network.P2pManager;
import org.faudroids.distributedmemory.network.ServiceRegistrationListener;


public class HostGameActivity extends Activity implements
		ServiceRegistrationListener,
		View.OnClickListener {

	private P2pManager p2pManager;

	private Button startHostButton;
	private Button stopHostButton;
	private EditText serviceNameEditText;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_game);

		WifiP2pManager wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		this.p2pManager = new P2pManager(wifiP2pManager, wifiP2pManager.initialize(this, getMainLooper(), null));

		this.startHostButton = (Button) findViewById(R.id.host_start);
		this.stopHostButton = (Button) findViewById(R.id.host_stop);
		this.serviceNameEditText = (EditText) findViewById(R.id.host_name);

		startHostButton.setOnClickListener(this);
		stopHostButton.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		v.setEnabled(false);
		switch(v.getId()) {
			case R.id.host_start:
				p2pManager.startServiceRegistration(serviceNameEditText.getText().toString(), this);
				p2pManager.startServiceDiscovery();
				break;

			case R.id.host_stop:
				p2pManager.stopServiceRegistration();
				startHostButton.setEnabled(true);
				break;
		}
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
