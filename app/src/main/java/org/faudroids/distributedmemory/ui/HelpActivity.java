package org.faudroids.distributedmemory.ui;

import android.content.Intent;
import android.os.Bundle;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;


public class HelpActivity extends BaseActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		ButterKnife.inject(this);
	}


    @OnClick(R.id.activity_help_host)
    public void onHelpHostClicked() {
        Intent dialogActivity = new Intent(this, HelpDialogActivity.class);
        String helpText = getResources().getString(R.string.activity_help_host_text);
        Timber.d("Sending helpText: " + helpText);
        dialogActivity.putExtra("helpText", helpText);
        startActivity(new Intent(this, HelpDialogActivity.class));
    }


    @OnClick(R.id.activity_help_join)
    public void onHelpJoinClick() {
        Intent dialogActivity = new Intent(this, HelpDialogActivity.class);
        String helpText = getResources().getString(R.string.activity_help_join_text);
        dialogActivity.putExtra("helpText", helpText);
        dialogActivity.setType("text/plain");
        Timber.d("Sending helpText: " + helpText);
        startActivity(new Intent(this, HelpDialogActivity.class));
    }


    @OnClick(R.id.activity_help_troubleshooting)
    public void onHelpTroubleshootingClick() {
        Intent dialogActivity = new Intent(this, HelpDialogActivity.class);
        String helpText = getResources().getString(R.string.activity_help_troubleshooting_text);
        Timber.d("Sending helpText: " + helpText);
        dialogActivity.putExtra("helpText", helpText);
        startActivity(new Intent(this, HelpDialogActivity.class));
    }


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}
}
