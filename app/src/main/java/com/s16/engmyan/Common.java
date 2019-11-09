package com.s16.engmyan;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.File;

public class Common {
	
	private static Typeface ZAWGYI_TYPEFACE;

    public static File getDataFolder(Context context) {
        return ContextCompat.getDataDir(context);
    }

	
	@Nullable
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
