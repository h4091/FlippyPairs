package org.faudroids.distributedmemory.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.core.Device;
import org.faudroids.distributedmemory.core.HostGameListener;
import org.faudroids.distributedmemory.core.HostGameManager;
import org.faudroids.distributedmemory.utils.ServiceUtils;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class LobbyActivity extends BaseActivity implements  HostGameListener {

	@Inject HostGameManager hostGameManager;
	@Inject ServiceUtils serviceUtils;
	@InjectView(R.id.start_game) Button startGameButton;
	@InjectView(R.id.peers_list) ListView peersList;
	@InjectView(R.id.empty) View emptyView;
	private ArrayAdapter<String> adapter;
    private final BroadcastReceiver serverStateReceiver = new ServerStateBroadcastReceiver();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if this activity is called from the host notification check if game is running --> continue
		if (hostGameManager.isGameRunning()) {
			Intent intent = new Intent(this, GameActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			finish();
			return;
		}

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

		// if lobby is started after having shut down host via notification
		if (!serviceUtils.isServiceRunning(HostService.class)) finish();

		registerReceiver(serverStateReceiver, new IntentFilter(HostService.ACTION_HOST_STATE_CHANGED));
        hostGameManager.registerHostGameListener(this);
		onClientsChanged();
    }


	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.activity_lobby_back_pressed_title)
				.setMessage(R.string.activity_lobby_back_pressed_message)
				.setPositiveButton(R.string.btn_continue, null)
				.setNegativeButton(R.string.btn_stop, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(LobbyActivity.this, HostService.class);
						stopService(intent);
						finish();
					}
				})
				.show();
	}


	@Override
	public void onClientAdded(Device device) {
		onClientsChanged();
    }


	@Override
	public void onGameStarted() {
		Intent intent = new Intent(this, GameActivity.class);
		startActivity(intent);
		finish();
	}


	@Override
	public void onGameStopped() { }


	@Override
	public void onClientLost(Device device) {
		onClientsChanged();
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


	private void onClientsChanged() {
		// update clients list
		adapter.clear();
		boolean foundHostClient = false;
		for (Device device : hostGameManager.getConnectedDevices()) {
			String clientName = device.getName();
			if (!foundHostClient && clientName.equals(Build.MODEL)) {
				foundHostClient = true;
				clientName = getString(R.string.activity_lobby_host_client, clientName);
			}
			adapter.add(clientName);
		}
		adapter.sort(new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return lhs.compareTo(rhs);
			}
		});
		adapter.notifyDataSetChanged();

		// update waiting spinner and start game button
		if (adapter.getCount() == 0) {
			emptyView.setVisibility(View.VISIBLE);
			startGameButton.setEnabled(false);
		} else {
			emptyView.setVisibility(View.GONE);
			startGameButton.setEnabled(true);
		}
	}


    private class ServerStateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
			boolean started = intent.getBooleanExtra(HostService.EXTRA_HOST_RUNNING, false);
			if(!started) finish();
        }

    }
}
