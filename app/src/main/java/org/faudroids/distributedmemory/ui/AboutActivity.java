package org.faudroids.distributedmemory.ui;

import android.os.Bundle;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;

import java.util.List;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AboutActivity extends BaseActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		ButterKnife.inject(this);
	}


	@Override
	public void onPause() {
		super.onPause();
	}


	@OnClick(R.id.activity_about_back)
	public void back() {
        finish();
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}
}
