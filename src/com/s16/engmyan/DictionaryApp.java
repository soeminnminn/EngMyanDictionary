package com.s16.engmyan;

import android.app.Application;

public class DictionaryApp extends Application {
	
	protected static String TAG = DictionaryApp.class.getSimpleName();

	public DictionaryApp() {
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
