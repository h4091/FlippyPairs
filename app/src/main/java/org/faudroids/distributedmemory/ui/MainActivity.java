package org.faudroids.distributedmemory.ui;

import android.content.Intent;
import android.os.Bundle;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.network_old.P2pManager;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends BaseActivity {

	@Inject P2pManager p2pManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		ButterKnife.inject(this);
    }


	@OnClick(R.id.host_game)
	public void hostGame() {
		startActivity(new Intent(this, HostGameActivity.class));
	}


	@OnClick(R.id.join_game)
	public void joinGame() {
		startActivity(new Intent(this, JoinGameActivity.class));
	}


    @OnClick(R.id.test_game)
    public void startGameActivity() {
        startActivity(new Intent(this, GameActivity.class));
    }


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
