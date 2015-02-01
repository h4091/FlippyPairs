package org.faudroids.distributedmemory.utils;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.LinkedList;
import java.util.List;

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
			Class<?> activityClass,
			List<Action> actions) {

		Intent intent = new Intent(context, activityClass);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Notification.Builder builder = new Notification.Builder(context)
				.setContentTitle(title)
				.setContentText(message)
				.setSmallIcon(iconResource)
				.setOngoing(true)
				.setContentIntent(PendingIntent.getActivity(context, 1000, intent, 0));

		for (Action action : actions) {
			builder.addAction(action.iconResource, action.title, action.pendingIntent);
		}
		return builder.build();
	}


	public Notification createOngoingNotification(
			String title,
			String message,
			int iconResource,
			Class<?> activityClass) {

		return createOngoingNotification(title, message, iconResource, activityClass, new LinkedList<Action>());
	}


	public static final class Action {

		private final int iconResource;
		private final String title;
		private final PendingIntent pendingIntent;

		public Action(int iconResource, String title, PendingIntent pendingIntent) {
			this.iconResource = iconResource;
			this.title = title;
			this.pendingIntent = pendingIntent;
		}

	}

}
