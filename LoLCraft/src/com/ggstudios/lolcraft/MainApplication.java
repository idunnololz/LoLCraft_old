package com.ggstudios.lolcraft;

import android.app.Application;

public class MainApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		LibraryManager.initInstance(getApplicationContext());
		SplashFetcher.initInstance(getApplicationContext());
		StateManager.initInstance();
		
	}
}
