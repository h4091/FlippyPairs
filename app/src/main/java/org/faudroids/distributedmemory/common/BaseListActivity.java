package org.faudroids.distributedmemory.common;

import android.app.ListActivity;
import android.os.Bundle;

import java.util.List;

import butterknife.ButterKnife;
import dagger.ObjectGraph;


public abstract class BaseListActivity extends ListActivity {

	private ObjectGraph objectGraph;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.objectGraph = ((Injector) getApplication()).createScopedGraph(getModules().toArray());
		this.objectGraph.inject(this);
		ButterKnife.inject(this);
	}


	@Override
	protected void onDestroy() {
		objectGraph = null;
		super.onDestroy();
	}


	protected abstract List<Object> getModules();

}
