package com.ggstudios.utils;

import java.util.List;
import java.util.Stack;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Parcelable;
import android.util.DisplayMetrics;

public class Utils {
	private static final String TAG = Utils.class.getSimpleName();
	
	private static final String EMAIL_FEEDBACK = "feedback@idunnololz.com";
	
	public static final int MB_BYTES = 1000000;

	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/**
	 * This method converts dp unit to equivalent pixels, depending on device density. 
	 * 
	 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public static float convertDpToPixel(float dp, Context context){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

	@TargetApi(11)
	public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> task,
			T... params) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else {
			task.execute(params);
		}
	}

	public static Intent createEmailOnlyChooserIntent(Context context, Intent source,
			CharSequence chooserTitle) {
		Stack<Intent> intents = new Stack<Intent>();
		Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
				"info@domain.com", null));
		List<ResolveInfo> activities = context.getPackageManager()
				.queryIntentActivities(i, 0);

		for(ResolveInfo ri : activities) {
			Intent target = new Intent(source);
			target.setPackage(ri.activityInfo.packageName);
			intents.add(target);
		}

		if(!intents.isEmpty()) {
			Intent chooserIntent = Intent.createChooser(intents.remove(0),
					chooserTitle);
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
					intents.toArray(new Parcelable[intents.size()]));

			return chooserIntent;
		} else {
			return Intent.createChooser(source, chooserTitle);
		}
	}
	
	public static void startFeedbackIntent(Context context) {
		try {
			String versionName = context.getPackageManager()
				    .getPackageInfo(context.getPackageName(), 0).versionName;
			
			StringBuilder footer = new StringBuilder();
			footer.append("<Enter feedback here>\n\n\nLoLCraft v");
			footer.append(versionName);
			footer.append("\nOS v");
			footer.append(android.os.Build.VERSION.RELEASE);
			footer.append("\nDevice ");
			footer.append(android.os.Build.MODEL);
			
			Intent i = new Intent(Intent.ACTION_SEND);
	        i.setType("*/*");
	        //i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(crashLogFile));
	        i.putExtra(Intent.EXTRA_EMAIL, new String[] {
	        		EMAIL_FEEDBACK
	        });
	        i.putExtra(Intent.EXTRA_SUBJECT, "Android app feedback");
	        i.putExtra(Intent.EXTRA_TEXT, footer.toString());

	        context.startActivity(Utils.createEmailOnlyChooserIntent(context, i, "Send via email"));
			
		} catch (NameNotFoundException e) {
			DebugLog.e(TAG, e);
		}
	}
	
	public static void executeInBackground(final Runnable r) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				r.run();
				return null;
			}
		}.execute();
	}
}
