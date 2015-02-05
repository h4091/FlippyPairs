package org.faudroids.distributedmemory.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;

import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;


public class MainActivity extends BaseActivity {

	@Inject ConnectivityManager connectivityManager;
	private Dialog wifiDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		ButterKnife.inject(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!isWifiConnected() && !isHotSpotActive()) openWifiDialog();
    }


	@Override
	public void onPause() {
		if (wifiDialog != null) {
			wifiDialog.dismiss();
			wifiDialog = null;
		}
		super.onPause();
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


    private boolean isHotSpotActive() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        Method[] wmMethods = wifi.getClass().getDeclaredMethods();
        for(Method method: wmMethods) {
            if (method.getName().equals("isWifiApEnabled")) {
                try {
                    return (boolean) method.invoke(wifi);
                } catch (Exception e) {
                    Timber.w(e, "Failed to read Wi-Fi hotspot status");
                }
            }
        }
        return false;
    }


	private void openWifiDialog() {
		wifiDialog = new AlertDialog.Builder(MainActivity.this)
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
