package android.support.v4.app;


import java.lang.reflect.Method;

import android.app.Activity;
import android.os.Build;
import android.view.View;

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
	
	/**
	 * Request that the visibility of the status bar or other screen/window decorations be changed.
	 */
	public static void setSystemUiVisibility(Activity activity, int visibility) {
		if (Build.VERSION.SDK_INT >= 11) { 
			View view = activity.getWindow().getDecorView();
			ReflectionUtils.invoke(view, 0, METHOD_setSystemUiVisibility, visibility);
		}
	}
}
