package org.faudroids.distributedmemory.app;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.faudroids.distributedmemory.common.CommonModule;
import org.faudroids.distributedmemory.core.CoreModule;
import org.faudroids.distributedmemory.network.NetworkModule;
import org.faudroids.distributedmemory.utils.UtilsModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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

	@Provides
	@Singleton
	public ObjectMapper provideObjectMapper() {
		return new ObjectMapper();
	}

}
