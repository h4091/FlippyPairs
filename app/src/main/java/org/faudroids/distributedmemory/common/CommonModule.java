package org.faudroids.distributedmemory.common;


import android.app.NotificationManager;
import android.content.Context;
import android.os.Looper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
		library = true
)
public final class CommonModule {

	private final Context context;

	public CommonModule(Context context) {
		this.context = context;
	}


	@Provides
	@Singleton
	Context provideApplicationContext() {
		return context;
	}


	@Provides
	@Singleton
	Looper provideMainLooper(Context context) {
		return context.getMainLooper();
	}


	@Provides
	@Singleton
	NotificationManager provideNotificationManager(Context context) {
		return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

}
