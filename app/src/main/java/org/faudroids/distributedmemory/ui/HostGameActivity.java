package org.faudroids.distributedmemory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.core.HostGameManager;
import org.faudroids.distributedmemory.core.Player;
import org.faudroids.distributedmemory.utils.ServiceUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class HostGameActivity extends BaseActivity {

	@Inject ServiceUtils serviceUtils;
    @Inject HostGameManager hostGameManager;

	@InjectView(R.id.start_hosting) Button startHostingButton;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_game);
		ButterKnife.inject(this);
	}


	@Override
	public void onResume() {
		super.onResume();
        NumberPicker np = (NumberPicker)findViewById(R.id.playerCountPicker);
        np.setMinValue(2);
        np.setMaxValue(100);
	}


	@Override
	public void onPause() {
		super.onPause();
	}


	@OnClick(R.id.start_hosting)
	public void startHosting() {
		hostGameManager.initGame();
        NumberPicker np = (NumberPicker)findViewById(R.id.playerCountPicker);
        for(int i=0; i < np.getValue(); ++i) {
            hostGameManager.addPlayer(new Player(i, "Player" + (i + 1)));
        }
        Intent hostIntent = new Intent(this, HostService.class);
        startService(hostIntent);
        Intent lobbyIntent = new Intent(this, LobbyActivity.class);
        startActivity(lobbyIntent);
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


}
