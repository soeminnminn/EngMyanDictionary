package com.s16.engmyan;

import java.io.File;
import java.io.IOException;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Environment;
import android.widget.Toast;

public class Common {
	
	private static final String ANDROID_DATA = "/Android/data/";
	private static String TEMP_FOLDER;
	private static String DATA_FOLDER;
	private static Typeface ZAWGYI_TYPEFACE;
	
	public static File getDataFolder(Context context) {
		if(DATA_FOLDER == null) {
			DATA_FOLDER = ANDROID_DATA + context.getPackageName() + File.separator  + "files" + File.separator;
		}
		
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + DATA_FOLDER;
			File folder = new File(path);
			if (!folder.exists()) {
				if (folder.mkdirs()) {
					File noMedia = new File(folder.getPath(), ".nomedia");
					if (!noMedia.exists()) {
						try {
							noMedia.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			return folder;
	    }
		
		return null;
	}
	
	public static File getTempFolder(Context context) {
		if(TEMP_FOLDER == null) {
			TEMP_FOLDER = ANDROID_DATA + context.getPackageName() + File.separator + "temp" + File.separator;
		}
		
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + TEMP_FOLDER;
			File folder = new File(path);
			if (!folder.exists()) {
				if (folder.mkdirs()) {
					File noMedia = new File(folder.getPath(), ".nomedia");
					if (!noMedia.exists()) {
						try {
							noMedia.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			return folder;
	    }
		
		return null;
	}
	
	public static File getDatabaseFile(Context context) {
		File dataFolder = getDataFolder(context);
		if(dataFolder != null) {
			boolean success = true;
			if(!dataFolder.exists()) {
				success = dataFolder.mkdirs();
			}
			
			if (success) {
				String dbPath = dataFolder.getPath() + File.separator + Constants.DATABASE_FILE;
				return new File(dbPath);
			}
		}
		
		return null;
	}
	
	public static Typeface getZawgyiTypeface(Context context) {
		if (ZAWGYI_TYPEFACE == null) {
			ZAWGYI_TYPEFACE = Typeface.createFromAsset(context.getAssets(), "fonts/zawgyi.ttf");
		}
		return ZAWGYI_TYPEFACE;
	}
	
	public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
             if (serviceClass.getName().equals(service.service.getClassName())) {
                 return true;
             }
         }
         return false;
	}
	
	public static void showMessage(Context context, int messageId) {
		Toast.makeText(context, messageId, Toast.LENGTH_LONG).show();
	}
}
