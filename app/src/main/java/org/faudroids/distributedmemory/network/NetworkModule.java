package org.faudroids.distributedmemory.network;


import dagger.Module;

@Module(
		complete = false,
		library = true,
		injects = {
				NetworkManager.class,
				HostSocketHandler.class
		}
)
public final class NetworkModule {

}
