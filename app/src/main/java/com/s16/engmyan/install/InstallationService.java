package com.s16.engmyan.install;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

public class InstallationService extends IntentService {

	public static final String TAG = InstallationService.class.getSimpleName();
	
	public static final String INSTALL_ASSETS_NAME = "install.ASSETSNAME";
	public static final String INSTALL_FOLDER = "install.FOLDERPATH";
	
	public static final String BROADCAST_ACTION = "com.s16.engmyan.install.BROADCAST";
	public static final String EXTENDED_DATA_STATUS = "com.s16.engmyan.install.STATUS";
	public static final String EXTENDED_PROGRESS_STATUS = "com.s16.engmyan.install.PROGRESS";
	
	public static final int STATE_ACTION_STARTED = 0;
	public static final int STATE_ACTION_ERROR = 2;
	public static final int STATE_ACTION_COMPLETE = 4;
	
	private LocalBroadcastManager mBroadcaster;
	
	public InstallationService() {
		super(TAG);
		
		try {
    		mBroadcaster = LocalBroadcastManager.getInstance(this);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
	}
	
	protected Context getContext() {
		return (Context)this;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		broadcastIntentWithState(STATE_ACTION_STARTED);
		
		String installFolder = intent.getStringExtra(INSTALL_FOLDER);
		
		if (TextUtils.isEmpty(installFolder)) {
			broadcastIntentWithState(STATE_ACTION_ERROR);
			return;
		}
		
		String assetsName = intent.getStringExtra(INSTALL_ASSETS_NAME);
		if (TextUtils.isEmpty(assetsName)) {
			broadcastIntentWithState(STATE_ACTION_ERROR);
			return;
		}
		
		File dataFolder = new File(installFolder);
		if(!dataFolder.exists() && !dataFolder.mkdirs()) {
			broadcastIntentWithState(STATE_ACTION_ERROR);
			return;
		}
		
		InputStream is;
	    ZipInputStream zis;
	    try {
	    	is = getContext().getAssets().open(assetsName);
	        zis = new ZipInputStream(new BufferedInputStream(is));
	        ZipEntry ze;

	        while ((ze = zis.getNextEntry()) != null) {
	            String filename = ze.getName();
	            Log.i(TAG, dataFolder.getAbsolutePath() + "/" + filename);
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            byte[] buffer = new byte[1024];
	            int count;

	            FileOutputStream fout = new FileOutputStream(dataFolder.getAbsolutePath() + "/" + filename);
	            while ((count = zis.read(buffer)) != -1) {
	                baos.write(buffer, 0, count);
	                byte[] bytes = baos.toByteArray();
	                fout.write(bytes);
	                baos.reset();
	            }

	            fout.close();
	            zis.closeEntry();
	        }
	        zis.close();
	        broadcastIntentWithState(STATE_ACTION_COMPLETE);
	     } 
	     catch(IOException e) {
	    	 e.printStackTrace();
	    	 broadcastIntentWithState(STATE_ACTION_ERROR);
	     }
	}

	protected void broadcastIntentWithState(int status) {
        Intent localIntent = new Intent();

        // The Intent contains the custom broadcast action for this app
        localIntent.setAction(BROADCAST_ACTION);

        // Puts the status into the Intent
        localIntent.putExtra(EXTENDED_DATA_STATUS, status);
        localIntent.putExtra(EXTENDED_PROGRESS_STATUS, -1);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);

        // Broadcasts the Intent
        if (mBroadcaster != null) {
        	mBroadcaster.sendBroadcast(localIntent);
        }
    }
	
}
