package org.faudroids.distributedmemory.ui;

import android.content.Intent;
import android.os.Bundle;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.network.ClientSocketHandler;
import org.faudroids.distributedmemory.network.Host;
import org.faudroids.distributedmemory.network.HostSocketHandler;
import org.faudroids.distributedmemory.network.NetworkListener;
import org.faudroids.distributedmemory.network.NetworkManager;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;


public class HostGameActivity extends BaseActivity implements NetworkListener {

	private static final String SERVICE_NAME = "serviceTest";

	@Inject NetworkManager networkManager;
	@Inject HostSocketHandler hostSocketHandler;
	ClientSocketHandler clientSocketHandler;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_game);
		ButterKnife.inject(this);
	}


	@Override
	protected void onDestroy() {
		if (clientSocketHandler != null) clientSocketHandler.shutdown();
		hostSocketHandler.shutdown();
		networkManager.unregisterService();
		super.onDestroy();
	}


	@OnClick(R.id.start_hosting)
	public void startHosting() {
		try {
			int hostPort = hostSocketHandler.start();
			clientSocketHandler = new ClientSocketHandler(InetAddress.getByName("127.0.0.1"), hostPort);
			clientSocketHandler.start();

			networkManager.registerService(SERVICE_NAME, hostPort, this);
			Intent intent = new Intent(this, LobbyActivity.class);
			intent.putExtra(LobbyActivity.KEY_IS_HOST, true);
			startActivity(intent);
		} catch (IOException ioe) {
			Timber.e(ioe, "failed to start host");
		}
	}


	@Override
	public void onRegistrationSuccess() {  }


	@Override
	public void onRegistrationError() {  }


	@Override
	public void onServiceDiscovered(Host host) { }


	@Override
	public void onServiceLost(String hostName) { }


	@Override
	public void onServiceDiscoveryError() {  }


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
