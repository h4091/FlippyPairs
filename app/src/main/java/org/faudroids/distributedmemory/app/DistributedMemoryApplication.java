package org.faudroids.distributedmemory.app;


import com.crashlytics.android.Crashlytics;
import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.BuildConfig;
import org.faudroids.distributedmemory.common.BaseApplication;

import java.util.List;

import dagger.ObjectGraph;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public final class DistributedMemoryApplication extends BaseApplication {

	private ObjectGraph objectGraph;

	@Override
	public void onCreate() {
		super.onCreate();

		if (BuildConfig.DEBUG) {
			Timber.plant(new Timber.DebugTree());
		} else {
			Fabric.with(this, new Crashlytics());
			Timber.plant(new CrashReportingTree());
		}

	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new AppModule());
	}


	private static final class CrashReportingTree extends Timber.HollowTree {

		@Override
		public void e(String message, Object... args) {
			Crashlytics.log(message);
		}


		@Override
		public void e(Throwable t, String message, Object... args) {
			Crashlytics.log(message);
			Crashlytics.logException(t);
		}

	}
}
