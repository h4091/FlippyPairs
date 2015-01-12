package org.faudroids.distributedmemory.main;


import android.app.Application;

import timber.log.Timber;

public final class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Timber.plant(new Timber.DebugTree());
	}

}
