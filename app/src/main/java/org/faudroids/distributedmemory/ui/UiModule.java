package org.faudroids.distributedmemory.ui;


import android.content.Context;
import android.content.res.AssetManager;

import org.faudroids.distributedmemory.app.AppModule;

import dagger.Module;
import dagger.Provides;

@Module(
		addsTo = AppModule.class,
		injects = {
				MainActivity.class,
				HostGameActivity.class,
				JoinGameActivity.class,
				LobbyActivity.class,
				GameActivity.class,
				HostService.class,
                AboutActivity.class,
                HelpActivity.class,
                HelpDialogActivity.class
		}
)
public final class UiModule {

	@Provides
	public AssetManager provideAssetManager(Context context) {
		return context.getAssets();
	}

}
