package org.faudroids.distributedmemory.ui;


import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import timber.log.Timber;

final class BitmapCache {

	private final AssetManager assetManager;
	private final Map<String, Bitmap> cache = new HashMap<>();

	@Inject
	public BitmapCache(AssetManager assetManager) {
		this.assetManager = assetManager;
	}


	public Bitmap getBitmap(String fileName) {
		if (cache.containsKey(fileName)) return cache.get(fileName);
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(assetManager.open(fileName));
			cache.put(fileName, bitmap);
			return bitmap;
		} catch (IOException ioe) {
			Timber.e("failed to load bitmap", ioe);
			return null;
		}
	}

}
