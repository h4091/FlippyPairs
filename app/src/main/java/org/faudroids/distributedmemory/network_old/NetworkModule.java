package org.faudroids.distributedmemory.network_old;


import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
		complete = false,
		library = true
)
public final class NetworkModule {

	@Provides
	@Singleton
	public WifiP2pManager provideWifiP2pManager(Context context) {
		return (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
	}


	@Provides
	@Singleton
	WifiP2pManager.Channel provideChannel(Context context, WifiP2pManager wifiP2pManager, Looper looper) {
		return wifiP2pManager.initialize(context, looper, null);
	}

}
