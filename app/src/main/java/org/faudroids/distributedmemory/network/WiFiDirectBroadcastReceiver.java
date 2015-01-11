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
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.widget.Toast;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private final WifiP2pManager manager;
    private final Channel channel;
	private final PeerListListener listListener;

    public WiFiDirectBroadcastReceiver(
			WifiP2pManager manager,
			Channel channel,
			PeerListListener listListener) {

        this.manager = manager;
        this.channel = channel;
		this.listListener = listListener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
		switch (intent.getAction()) {
			case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
				int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
				if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
					// Wifi Direct mode is enabled
					Toast.makeText(context, "Wifi enabled", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, "Wifi disabled", Toast.LENGTH_SHORT).show();
				}
				break;

			case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
				// manager.requestPeers(channel, listListener);
				Toast.makeText(context, "P2P peers changed", Toast.LENGTH_SHORT).show();
				break;

			case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
				NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
				if (networkInfo.isConnected()) {
					// manager.requestConnectionInfo(channel, infoListener);
					Toast.makeText(context, "P2P connected, time to fetch connection info!", Toast.LENGTH_SHORT).show();

				} else {
					Toast.makeText(context, "P2P disconnected", Toast.LENGTH_SHORT).show();
				}
				break;

			case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
				Toast.makeText(context, "This peer changed: " + intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE), Toast.LENGTH_LONG).show();
				// TODO do we care?
				break;
		}
    }

}
