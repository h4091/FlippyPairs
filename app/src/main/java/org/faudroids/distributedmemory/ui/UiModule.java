package org.faudroids.distributedmemory.ui;


import org.faudroids.distributedmemory.main.MainModule;

import dagger.Module;

@Module(
		addsTo = MainModule.class,
		injects = {
				MainActivity.class,
				HostGameActivity.class,
				ClientGameActivity.class
		}
)
public final class UiModule {

}
