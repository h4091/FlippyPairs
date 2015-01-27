package org.faudroids.distributedmemory.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.core.HostGameManager;
import org.faudroids.distributedmemory.core.Player;
import org.faudroids.distributedmemory.core.PlayerListListener;
import org.faudroids.distributedmemory.utils.ServiceUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemLongClick;
import timber.log.Timber;


public class HostGameActivity extends BaseActivity implements PlayerListListener {

	@Inject ServiceUtils serviceUtils;
    @Inject
    HostGameManager hostGameManager;

	@InjectView(R.id.start_hosting) Button startHostingButton;
	@InjectView(R.id.stop_hosting) Button stopHostingButton;

	private final BroadcastReceiver serverStateReceiver = new ServerStateBroadcastReceiver();

    private ArrayList<String> playerList = new ArrayList<>();
    private ArrayAdapter<String> adapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_game);
		ButterKnife.inject(this);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playerList);
        ListView lv = (ListView)findViewById(R.id.playersList);
        lv.setAdapter(adapter);
	}


	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(serverStateReceiver, new IntentFilter(HostService.ACTION_SERVER_STATE_CHANGED));
		toggleStartStopButtons(serviceUtils.isServiceRunning(HostService.class));
        hostGameManager.registerPlayerListListener(this);
	}


	@Override
	public void onPause() {
		unregisterReceiver(serverStateReceiver);
        hostGameManager.unregisterPlayerListListener();
		super.onPause();
	}


	@OnClick(R.id.start_hosting)
	public void startHosting() {
        if(playerList.size()>1) {
            startHostingButton.setEnabled(false);

            Intent hostIntent = new Intent(this, HostService.class);
            startService(hostIntent);

            Intent lobbyIntent = new Intent(this, LobbyActivity.class);
            startActivity(lobbyIntent);
        } else {
            Toast errorToast = Toast.makeText(getApplicationContext(),
                    "Players missing!",
                    Toast.LENGTH_SHORT);
            errorToast.show();
        }
	}


    @OnItemLongClick(R.id.playersList)
    public boolean removePlayer(final ListView lv, final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove player?");

        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hostGameManager.removePlayer(index);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
        return true;
    }


    @OnClick(R.id.add_player)
    public void addPlayer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter player name:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String playerName = input.getText().toString();
                if(!playerName.isEmpty()) {
                    hostGameManager.addPlayer(playerName);
                } else {
                    Toast errorToast = Toast.makeText(getApplicationContext(),
                            "Discarded invalid input!",
                            Toast.LENGTH_SHORT);
				errorToast.show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


	@OnClick(R.id.stop_hosting)
	public void stopHosting() {
		stopHostingButton.setEnabled(false);

		Intent hostIntent = new Intent(this, HostService.class);
		stopService(hostIntent);
	}


	private void toggleStartStopButtons(boolean serverRunning) {
		if (serverRunning) {
			startHostingButton.setEnabled(false);
			stopHostingButton.setEnabled(true);
		} else {
			startHostingButton.setEnabled(true);
			stopHostingButton.setEnabled(false);
		}
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


    @Override
    public void onListChanged() {
        List<Player> players = hostGameManager.getPlayers();
        adapter.clear();

        for(Player p : players) {
            adapter.add(p.getName());
        }
    }


    private class ServerStateBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean started = intent.getBooleanExtra(HostService.EXTRA_SERVER_RUNNING, false);
			toggleStartStopButtons(started);
		}

	}

}
