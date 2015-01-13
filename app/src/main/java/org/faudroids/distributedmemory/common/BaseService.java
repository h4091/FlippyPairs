package org.faudroids.distributedmemory.common;

import android.app.Service;

import java.util.List;

import dagger.ObjectGraph;


public abstract class BaseService extends Service {

	private ObjectGraph objectGraph;

	@Override
	public void onCreate() {
		super.onCreate();
		this.objectGraph = ((Injector) getApplication()).createScopedGraph(getModules().toArray());
		this.objectGraph.inject(this);
	}


	@Override
	public void onDestroy() {
		objectGraph = null;
		super.onDestroy();
	}


	protected abstract List<Object> getModules();

}
