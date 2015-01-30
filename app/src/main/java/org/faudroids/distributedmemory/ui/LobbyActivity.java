package org.faudroids.distributedmemory.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.core.Device;
import org.faudroids.distributedmemory.core.HostGameListener;
import org.faudroids.distributedmemory.core.HostGameManager;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;


public class LobbyActivity extends BaseActivity implements  HostGameListener {

	@Inject HostGameManager hostGameManager;
	@InjectView(R.id.peers_list) ListView peersList;
	private ArrayAdapter<String> adapter;
    private final BroadcastReceiver serverStateReceiver = new ServerStateBroadcastReceiver();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);
		ButterKnife.inject(this);
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		peersList.setAdapter(adapter);
	}


	@OnClick(R.id.start_game)
	public void startGame() {
		hostGameManager.startGame();
	}


    @Override
    public void onPause() {
        hostGameManager.unregisterHostGameListener(this);
        unregisterReceiver(serverStateReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(serverStateReceiver, new IntentFilter(HostService.ACTION_HOST_STATE_CHANGED));
        hostGameManager.registerHostGameListener(this);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_lobby, menu);
		return true;
	}


	@Override
	public void onClientAdded(Device device) {
        adapter.clear();
        List<Device> devices = hostGameManager.getConnectedDevices();
        for (Device d : devices) adapter.add(d.getName());
        adapter.notifyDataSetChanged();
        Timber.i("Called refresh and found " + adapter.getCount() + " elements");
    }


	@Override
	public void onGameStarted() {
		// TODO go to client screen?
	}


	@Override
	public void onGameStopped() { }


	@Override
	public void onClientLost(Device device) { }


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


    private class ServerStateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean started = intent.getBooleanExtra(HostService.EXTRA_HOST_RUNNING, false);
            if(!started) {
                finish();
            }
        }
    }
}
