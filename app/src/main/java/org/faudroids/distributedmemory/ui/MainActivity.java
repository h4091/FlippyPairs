package org.faudroids.distributedmemory.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;


public class MainActivity extends BaseActivity {

	@Inject ConnectivityManager connectivityManager;
	@InjectView(R.id.app_version) TextView appVersionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		ButterKnife.inject(this);
		try {
			appVersionView.setText(getString(R.string.version, getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
		} catch (PackageManager.NameNotFoundException nnfe) {
			Timber.e("failed to find version", nnfe);
		}
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!isWifiConnected()) openWifiSettings();
    }


	@OnClick(R.id.host_game)
	public void hostGame() {
        startActivity(new Intent(this, HostGameActivity.class));
	}


	@OnClick(R.id.join_game)
	public void joinGame() {
		startActivity(new Intent(this, JoinGameActivity.class));
	}


    @OnClick(R.id.about_game)
    public void aboutGame() {
        startActivity(new Intent(this, AboutActivity.class));
    }


    @OnClick(R.id.help_game)
    public void helpGame() {
        startActivity(new Intent(this, HelpActivity.class));
    }


        private boolean isWifiConnected() {
		NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifiInfo.isConnected();
	}


	private void openWifiSettings() {
		new AlertDialog.Builder(MainActivity.this)
				.setTitle(R.string.missing_wifi_alert_title)
				.setMessage(R.string.missing_wifi_alert)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
									  @Override
									  public void onClick(DialogInterface dialog, int which) {
										  startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
									  }})
				.setNegativeButton(android.R.string.no, null)
				.show();
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}

}
