package org.faudroids.distributedmemory.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.R;
import org.faudroids.distributedmemory.common.BaseActivity;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends BaseActivity {

    private WifiTest wifiTest = new WifiTest();
    private WifiChangedReceiver wifiChangedReceiver = new WifiChangedReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		ButterKnife.inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
        registerReceiver(wifiChangedReceiver, intentFilter);
        if(wifiTest.isConnected()==false) {
            Button hostGameBtn = (Button) findViewById(R.id
                    .host_game);
            hostGameBtn.setEnabled(false);
            Button joinGameBtn = (Button) findViewById(R.id.join_game);
            joinGameBtn.setEnabled(false);
        }
    }

    @Override
    public void onPause() {
        unregisterReceiver(wifiChangedReceiver);
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


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new UiModule());
	}


    private final class WifiChangedReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            if(wifiTest.isConnected()) {
                Button hostGameBtn = (Button) findViewById(R.id.host_game);
                hostGameBtn.setEnabled(true);
                Button joinGameBtn = (Button) findViewById(R.id.join_game);
                joinGameBtn.setEnabled(true);
            }
        }

    }


    private class WifiTest {

        public boolean isConnected() {
            ConnectivityManager connManager;
            connManager = (ConnectivityManager) MainActivity
                    .this
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            return mWifi.isConnected();
        }


        private final void goToSettings() {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.missing_wifi_alert_title)
                    .setMessage(R.string.missing_wifi_alert)
                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Button hostGameBtn = (Button) findViewById(R.id.host_game);
                            hostGameBtn.setEnabled(false);
                            Button joinGameBtn = (Button) findViewById(R.id.join_game);
                            joinGameBtn.setEnabled(false);
                        }
                    })
                    .show();
        }
    }
}
