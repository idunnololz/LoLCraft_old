package com.ggstudios.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.jakewharton.disklrucache.DiskLruCache;

public class DiskLruImageCache {
	private static final String TAG = "DiskLruImageCache";

	private static final int IO_BUFFER_SIZE = Utils.MB_BYTES * 2;

	private DiskLruCache mDiskCache;
	private CompressFormat mCompressFormat = CompressFormat.JPEG;
	private int mCompressQuality = 70;
	private static final int APP_VERSION = 1;
	private static final int VALUE_COUNT = 1;

	private boolean errorState = false;

	public DiskLruImageCache( Context context,String uniqueName, int diskCacheSize,
			CompressFormat compressFormat, int quality ) {
		try {
			final File diskCacheDir = getDiskCacheDir(context, uniqueName );
			mDiskCache = DiskLruCache.open( diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize );
			mCompressFormat = compressFormat;
			mCompressQuality = quality;
		} catch (IOException e) {
			e.printStackTrace();
			errorState = true;
		}
	}
	
	public boolean isInErrorState() {
		return errorState;
	}

	private boolean writeBitmapToFile( Bitmap bitmap, DiskLruCache.Editor editor )
			throws IOException, FileNotFoundException {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream( editor.newOutputStream( 0 ), IO_BUFFER_SIZE );
			return bitmap.compress( mCompressFormat, mCompressQuality, out );
		} finally {
			if ( out != null ) {
				out.close();
			}
		}
	}

	private File getDiskCacheDir(Context context, String uniqueName) {
		return new File(context.getExternalFilesDir(null), "SplashCache");
	}

	public void put( String key, Bitmap data ) {

		DiskLruCache.Editor editor = null;
		try {
			editor = mDiskCache.edit( key );
			if ( editor == null ) {
				return;
			}

			if( writeBitmapToFile( data, editor ) ) {               
				mDiskCache.flush();
				editor.commit();
				DebugLog.d(TAG, "image put on disk cache " + key );
			} else {
				editor.abort();
				DebugLog.d(TAG, "ERROR on: image put on disk cache " + key );
			}   
		} catch (IOException e) {
			DebugLog.d(TAG, "ERROR on: image put on disk cache " + key );
			try {
				if ( editor != null ) {
					editor.abort();
				}
			} catch (IOException ignored) {
			}           
		}

	}

	public Bitmap getBitmap( String key ) {

		Bitmap bitmap = null;
		DiskLruCache.Snapshot snapshot = null;
		try {

			snapshot = mDiskCache.get( key );
			if ( snapshot == null ) {
				return null;
			}
			final InputStream in = snapshot.getInputStream( 0 );
			if ( in != null ) {
				final BufferedInputStream buffIn = 
						new BufferedInputStream( in, IO_BUFFER_SIZE );
				bitmap = BitmapFactory.decodeStream( buffIn );              
			}   
		} catch ( IOException e ) {
			e.printStackTrace();
		} finally {
			if ( snapshot != null ) {
				snapshot.close();
			}
		}

		DebugLog.d(TAG, bitmap == null ? "" : "image read from disk " + key);

		return bitmap;

	}

	public boolean containsKey( String key ) {

		boolean contained = false;
		DiskLruCache.Snapshot snapshot = null;
		try {
			snapshot = mDiskCache.get( key );
			contained = snapshot != null;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if ( snapshot != null ) {
				snapshot.close();
			}
		}

		return contained;

	}

	public void clearCache() {
		DebugLog.d(TAG, "disk cache CLEARED");
		try {
			mDiskCache.delete();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	public File getCacheFolder() {
		return mDiskCache.getDirectory();
	}

}
