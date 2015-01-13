package org.faudroids.distributedmemory.common;


import android.app.Application;

import java.util.List;

import dagger.ObjectGraph;

public abstract class BaseApplication extends Application implements Injector {

	private ObjectGraph objectGraph;

	@Override
	public void onCreate() {
		super.onCreate();
		List<Object> modules = getModules();
		modules.add(new CommonModule(getApplicationContext()));

		objectGraph = ObjectGraph.create(modules.toArray());
		objectGraph.inject(this);
	}


	@Override
	public ObjectGraph createScopedGraph(Object... modules) {
		return objectGraph.plus(modules);
	}


	protected abstract List<Object> getModules();

}
