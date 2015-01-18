package org.faudroids.distributedmemory.ui;

import android.os.Bundle;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;
import org.faudroids.distributedmemory.network.NetworkManager;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class HostGameActivity extends BaseActivity {

	private static final String SERVICE_NAME = "serviceTest";

	@Inject NetworkManager networkManager;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_game);
		ButterKnife.inject(this);
	}


	@OnClick(R.id.start_hosting)
	public void startHosting() {
		// TODO something smart here
	}



	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
