package org.faudroids.distributedmemory.ui;

import android.content.Intent;
import android.os.Bundle;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		ButterKnife.inject(this);
    }


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


	@OnClick(R.id.game_host)
	public void startHostGameActivity() {
		startActivity(new Intent(this, HostGameActivity.class));
	}


	@OnClick(R.id.game_join)
	public void startClientGameActivity() {
		startActivity(new Intent(this, ClientGameActivity.class));
	}

}
