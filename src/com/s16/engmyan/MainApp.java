package com.s16.engmyan;

import android.app.Application;

public class MainApp extends Application {
	
	protected static String TAG = MainApp.class.getSimpleName();

	public MainApp() {
		super();
	}
	
	@Override
	public void onCreate() {
		ExpansionManager.tryMountExpansion(this);
		super.onCreate();
    }
	
	@Override
	public void onTerminate() {
		ExpansionManager.unmountExpansion(this, true);
		super.onTerminate();
    }
}
