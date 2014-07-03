package com.s16.engmyan;

import android.app.Application;

public class MainApp extends Application {
	
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
