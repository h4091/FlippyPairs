package org.faudroids.distributedmemory.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;

import java.util.List;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		findViewById(R.id.game_host).setOnClickListener(this);
		findViewById(R.id.game_join).setOnClickListener(this);
    }


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


	@Override
	public void onClick(View view) {
		switch(view.getId()) {
			case R.id.game_host:
				startActivity(new Intent(this, HostGameActivity.class));
				break;

			case R.id.game_join:
				startActivity(new Intent(this, ClientGameActivity.class));
				break;
		}
	}
}
