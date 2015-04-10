package android.support.v4.app;


import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class SystemUiUtils {
	
	public static final int SYSTEM_UI_FLAG_FULLSCREEN = 0x00000004;
	public static final int SYSTEM_UI_FLAG_HIDE_NAVIGATION = 0x00000002;
	public static final int SYSTEM_UI_FLAG_IMMERSIVE = 0x00000800;
	public static final int SYSTEM_UI_FLAG_IMMERSIVE_STICKY = 0x00001000;
	public static final int SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN = 0x00000400;
	public static final int SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION = 0x00000200;
	public static final int SYSTEM_UI_FLAG_LAYOUT_STABLE = 0x00000100;
	public static final int SYSTEM_UI_FLAG_LOW_PROFILE = 0x00000001;
	public static final int SYSTEM_UI_FLAG_VISIBLE = 0x00000000;
	
	private static final Method METHOD_setSystemUiVisibility = ReflectionUtils.getMethod(
			View.class, "switchToNextInputMethod", Integer.TYPE);
	
	private static final Method METHOD_setStatusBarColor = ReflectionUtils.getMethod(Window.class, "setStatusBarColor");
	private static final Method METHOD_setNavigationBarColor = ReflectionUtils.getMethod(Window.class, "setNavigationBarColor");
	
	/**
	 * Request that the visibility of the status bar or other screen/window decorations be changed.
	 */
	public static void setSystemUiVisibility(Activity activity, int visibility) {
		if (Build.VERSION.SDK_INT >= 11) { 
			View view = activity.getWindow().getDecorView();
			ReflectionUtils.invoke(view, 0, METHOD_setSystemUiVisibility, visibility);
		}
	}
	
	public static boolean isTablet(Context context) {
	    boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
	    boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
	    return (xlarge || large);
	}
	
	@SuppressLint("InlinedApi")
	public static int getScreenOrientation(Context context) {
		WindowManager manager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		int rotation = manager.getDefaultDisplay().getRotation();
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        	if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270) {
        		return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        	} else {
        		return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        	}
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
        		return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        	} else {
        		return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        	}
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }
	
	@SuppressLint("InlinedApi")
	public static void setStatusBarColor(Activity activity, int color) {
		final Window window = activity.getWindow();
		
		if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ReflectionUtils.invoke(window, 0, METHOD_setStatusBarColor, color);
		} else if (Build.VERSION.SDK_INT >= 19) {
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			SystemBarTintManager systemBarTintManager = new SystemBarTintManager(activity);
			systemBarTintManager.setStatusBarTintEnabled(true);
			systemBarTintManager.setStatusBarTintColor(color);
			
			ViewGroup decor = (ViewGroup)window.getDecorView();
			ViewGroup contentView = (ViewGroup)decor.findViewById(android.R.id.content);
			if (contentView != null) {
				int statusBarHeight = systemBarTintManager.getConfig().getStatusBarHeight();
				int actionbarHeight = systemBarTintManager.getConfig().getActionBarDefaultHeight(activity);
				contentView.setPadding(0, statusBarHeight + actionbarHeight, 0, 0);
			}
		}
	}
	
	@SuppressLint("InlinedApi")
	public static void setNavigationBarColor(Activity activity, int color) {
		final Window window = activity.getWindow();
		if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            ReflectionUtils.invoke(window, 0, METHOD_setNavigationBarColor, color);
		} else if (Build.VERSION.SDK_INT >= 19) {
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			SystemBarTintManager systemBarTintManager = new SystemBarTintManager(activity);
			systemBarTintManager.setNavigationBarTintEnabled(true);
			systemBarTintManager.setNavigationBarTintColor(color);
		}
	}
	
	/**
	 * Hides the soft keyboard
	 */
	public static void hideSoftKeyboard(Context context, View view) {
	    if(view != null) {
	        InputMethodManager inputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
	        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}

	/**
	 * Shows the soft keyboard
	 */
	public static void showSoftKeyboard(Context context, View view) {
	    InputMethodManager inputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
	    view.requestFocus();
	    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}
}
