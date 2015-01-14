package org.faudroids.distributedmemory.ui;


import org.faudroids.distributedmemory.app.AppModule;

import dagger.Module;

@Module(
		addsTo = AppModule.class,
		injects = {
				MainActivity.class,
				GameActivity.class,
				HostGameActivity.class,
				JoinGameActivity.class,
				LobbyActivity.class,
				P2pActivity.class
		}
)
public final class UiModule {

}
