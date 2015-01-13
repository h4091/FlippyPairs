package org.faudroids.distributedmemory.common;


import dagger.ObjectGraph;

public interface Injector {

	public ObjectGraph createScopedGraph(Object... modules);

}
