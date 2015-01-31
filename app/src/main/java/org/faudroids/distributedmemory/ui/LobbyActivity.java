package org.faudroids.distributedmemory.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
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
	@InjectView(R.id.empty) View emptyView;
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
		toggleEmptyView();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_lobby, menu);
		return true;
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
        adapter.clear();
        List<Device> devices = hostGameManager.getConnectedDevices();
        for (Device d : devices) adapter.add(d.getName());
        adapter.notifyDataSetChanged();
		toggleEmptyView();
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
		toggleEmptyView();
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


	private void toggleEmptyView() {
		if (adapter.getCount() == 0) {
			Timber.d("showing empty view");
			emptyView.setVisibility(View.VISIBLE);
		} else {
			Timber.d("hiding empty view");
			emptyView.setVisibility(View.GONE);
		}
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
