package org.faudroids.distributedmemory.ui;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.faudroids.distributedmemory.R;

import javax.inject.Inject;

public final class UiUtils {

	private final Context context;

	@Inject
	public UiUtils(Context context) {
		this.context = context;
	}


	public Notification createGameRunningNotification(
			String title,
			String message,
			Class<?> activityClass) {

		Intent intent = new Intent(context, activityClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return new Notification.Builder(context)
				.setContentTitle(title)
				.setContentText(message)
				.setSmallIcon(R.drawable.ic_launcher)
				.setOngoing(true)
				.setContentIntent(PendingIntent.getActivity(context, 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT))
				.build();
	}

}
