package org.faudroids.distributedmemory.main;


import org.faudroids.distributedmemory.common.CommonModule;
import org.faudroids.distributedmemory.network.NetworkModule;

import dagger.Module;

@Module(
		addsTo = CommonModule.class,
		includes = {
				NetworkModule.class,
		},
		injects = {
				MainApplication.class
		}
)
public final class MainModule {

}
