package org.faudroids.distributedmemory.app;


import org.faudroids.distributedmemory.common.CommonModule;
import org.faudroids.distributedmemory.network.NetworkModule;

import dagger.Module;

@Module(
		addsTo = CommonModule.class,
		includes = {
				NetworkModule.class,
		},
		injects = {
				DistributedMemoryApplication.class
		}
)
public final class AppModule {

}
