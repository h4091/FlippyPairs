package org.faudroids.distributedmemory.core;


import android.content.Context;

import org.faudroids.distributedmemory.R;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module(
		complete = false,
		library = true,
		injects = {
				ClientGameManager.class,
				HostGameManager.class
		}
)
public final class CoreModule {

	@Provides
	@Named(HostGameManager.TOTAL_CARD_IMAGES)
	public int provideTotalImagesCount(Context context) {
		return context.getResources().getInteger(R.integer.card_images_count);
	}

}
