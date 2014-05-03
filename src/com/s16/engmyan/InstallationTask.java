package com.s16.engmyan;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class InstallationTask extends AsyncTask<String, Void, String> {
	
	protected static String TAG = InstallationTask.class.getSimpleName();
	
	public interface InstallationHandler {
		
		public void preInstall();
		
		public void postInstall();
		
		public void installError(String message);
	}
	
	private final Context mContext;
	private final File mDataFolder;
	private final InstallationHandler mHandler;
	private boolean mHasError;
	
	public InstallationTask(Context context, File dataFolder, InstallationHandler handler) {
		mContext = context;
		mDataFolder = dataFolder;
		mHandler = handler;
		mHasError = false;
	}

	@Override
	protected String doInBackground(String... params) {
		
		if ((mDataFolder == null) || (!mDataFolder.exists())) {
			mHasError = true;
			return "Installation failed. Data folder does not exists";
		}
		
		String message = null;
		String assetsName = params[0];
        String dataFolder = mDataFolder.getPath();
		
		InputStream is;
	    ZipInputStream zis;
	    try {
	    	is = mContext.getAssets().open(assetsName);
	        zis = new ZipInputStream(new BufferedInputStream(is));
	        ZipEntry ze;

	        while ((ze = zis.getNextEntry()) != null) {
	            String filename = ze.getName();
	            Log.i(TAG, dataFolder + "/" + filename);
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            byte[] buffer = new byte[1024];
	            int count;

	            FileOutputStream fout = new FileOutputStream(dataFolder + "/" + filename);
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
	     } 
	     catch(IOException e) {
	    	 e.printStackTrace();
	    	 mHasError = true;
	    	 message = "Installation failed. " + e.getMessage();
	     }
	    
		return message;
	}
	
	@Override
	protected void onPreExecute() { 
		super.onPreExecute();
		if (mHandler != null) {
			mHandler.preInstall();
		}
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if (mHandler != null) {
			if (mHasError) {
				mHandler.installError(result);
			} else {
				mHandler.postInstall();
			}
		}
	}
}