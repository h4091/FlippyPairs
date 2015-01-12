/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.faudroids.distributedmemory.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = "RECEIVER";

	private final P2pConnectionListener connectionListener;
    private final WifiP2pManager manager;
    private final Channel channel;

    public WiFiDirectBroadcastReceiver(
			P2pConnectionListener connectionListener,
			WifiP2pManager manager,
			Channel channel) {

		this.connectionListener = connectionListener;
        this.manager = manager;
        this.channel = channel;
    }


    @Override
    public void onReceive(final Context context, Intent intent) {
		switch (intent.getAction()) {
			case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
				NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
				if (networkInfo.isConnected()) {
					Log.i(TAG, "P2P connected, time to fetch connection info!");
					manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
						@Override
						public void onConnectionInfoAvailable(WifiP2pInfo info) {
							if (info.groupFormed) {
								if (info.isGroupOwner) Log.i(TAG, "Ignoring connected due to current peer being the group owner");
								else connectionListener.onConnected(info.groupOwnerAddress);
							} else {
								Log.i(TAG, "Ignoring connected due to missing group formation");
							}
						}
					});

				} else {
					Log.i(TAG, "P2P disconnected");
				}
				break;

			case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
				Log.i(TAG, "This peer changed");
				break;
		}
    }

}
