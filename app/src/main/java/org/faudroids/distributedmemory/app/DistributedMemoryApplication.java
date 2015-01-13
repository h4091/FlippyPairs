package org.faudroids.distributedmemory.app;


import com.google.common.collect.Lists;

import org.faudroids.distributedmemory.common.BaseApplication;

import java.util.List;

import dagger.ObjectGraph;
import timber.log.Timber;

public final class DistributedMemoryApplication extends BaseApplication {

	private ObjectGraph objectGraph;

	@Override
	public void onCreate() {
		super.onCreate();
		Timber.plant(new Timber.DebugTree());
	}


	@Override
	protected List<Object> getModules() {
		return Lists.<Object>newArrayList(new AppModule());
	}

}
