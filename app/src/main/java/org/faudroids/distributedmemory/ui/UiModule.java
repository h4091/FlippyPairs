package org.faudroids.distributedmemory.ui;


import org.faudroids.distributedmemory.app.AppModule;

import dagger.Module;

@Module(
		addsTo = AppModule.class,
		injects = {
				MainActivity.class,
				HostGameActivity.class,
				JoinGameActivity.class,
				ClientGameActivity.class
		}
)
public final class UiModule {

}
