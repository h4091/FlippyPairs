package org.faudroids.distributedmemory.utils;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

public final class NotificationUtils {

	private final Context context;

	@Inject
	public NotificationUtils(Context context) {
		this.context = context;
	}


	public Notification createOngoingNotification(
			String title,
			String message,
			int iconResource,
			Class<?> activityClass) {

		Intent intent = new Intent(context, activityClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return new Notification.Builder(context)
				.setContentTitle(title)
				.setContentText(message)
				.setSmallIcon(iconResource)
				.setOngoing(true)
				.setContentIntent(PendingIntent.getActivity(context, 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT))
				.build();
	}

}
