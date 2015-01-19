package org.faudroids.distributedmemory.core;


import dagger.Module;

@Module(
		complete = false,
		library = true,
		injects = {
				ClientGameManager.class,
				HostGameManager.class
		}
)
public final class CoreModule {

}
