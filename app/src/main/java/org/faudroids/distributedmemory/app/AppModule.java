package org.faudroids.distributedmemory.app;


import org.faudroids.distributedmemory.common.CommonModule;
import org.faudroids.distributedmemory.core.CoreModule;
import org.faudroids.distributedmemory.network.NetworkModule;
import org.faudroids.distributedmemory.utils.UtilsModule;

import dagger.Module;

@Module(
		addsTo = CommonModule.class,
		includes = {
				NetworkModule.class,
				UtilsModule.class,
				CoreModule.class
		},
		injects = {
				DistributedMemoryApplication.class
		}
)
public final class AppModule {

}
