package org.faudroids.distributedmemory.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public final class StopServerBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent stopServiceIntent = new Intent(context, HostService.class);
		context.stopService(stopServiceIntent);
	}

}
