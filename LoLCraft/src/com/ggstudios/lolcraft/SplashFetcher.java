package com.ggstudios.lolcraft;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.ggstudios.utils.DebugLog;
import com.ggstudios.utils.DiskLruImageCache;
import com.ggstudios.utils.Utils;

public class SplashFetcher {
	private static final String TAG = "SplashFetcher";

	private static final String CACHE_NAME = "SplashCache";

	private static SplashFetcher instance;

	private DiskLruImageCache diskCache = null;

	private Object cacheLock = new Object();
	private Context context;

	private SplashFetcher(final Context context) {
		this.context = context;
	}

	private void initialize() {
		diskCache = new DiskLruImageCache(context, CACHE_NAME, 
				Utils.MB_BYTES * 10, CompressFormat.JPEG, 70);
	}

	public static void initInstance(Context context) {
		instance = new SplashFetcher(context);
	}

	public static SplashFetcher getInstance() {
		return instance;
	}

	public void fetchChampionSplash(final String key, final OnDrawableRetrievedListener listener) {
		if (diskCache != null) {
			if (diskCache.isInErrorState()) return;
			final String sanatizedKey = key.toLowerCase(Locale.US);

			if (diskCache.containsKey(sanatizedKey)) {
				listener.onDrawableRetrieved(new BitmapDrawable(context.getResources(), 
						diskCache.getBitmap(sanatizedKey)));
				return;
			}
		}

		AsyncTask<String, Void, Drawable> task = new AsyncTask<String, Void, Drawable>(){

			@Override
			protected Drawable doInBackground(String... params) {
				if (diskCache == null) {
					synchronized(cacheLock) {
						if (diskCache == null) {
							initialize();
						}
					}
				}			
				
				if (diskCache.isInErrorState()) return null;

				final String sanatizedKey = key.toLowerCase(Locale.US);

				if (diskCache.containsKey(sanatizedKey)) {
					return new BitmapDrawable(context.getResources(), 
							diskCache.getBitmap(sanatizedKey));
				}

				try {
					URL url = new URL("http://ddragon.leagueoflegends.com/cdn/img/champion/splash/" 
							+ params[0] + "_0.jpg");

					Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
					diskCache.put(sanatizedKey, bmp);
					return new BitmapDrawable(context.getResources(), bmp);
				} catch (MalformedURLException e) {
					DebugLog.e(TAG, e);
				} catch (IOException e) {
					DebugLog.e(TAG, e);
				}
				return null;
			}

			protected void onPostExecute(Drawable d) {
				listener.onDrawableRetrieved(d);
			}

		};
		
		Utils.executeAsyncTask(task, key);
	}

	public static interface OnDrawableRetrievedListener {
		public void onDrawableRetrieved(Drawable d);
	}
}
