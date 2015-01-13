package org.faudroids.distributedmemory.utils;

import android.app.ActivityManager;
import android.content.Context;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ServiceUtils {

	private final Context context;

	@Inject
	public ServiceUtils(Context context) {
		this.context = context;
	}


	public boolean isServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
