package com.s16.widget.popupmenu;

import android.content.Context;
import android.content.res.Configuration;

/**
 * User: mcxiaoke
 * Date: 13-10-3
 * Time: ä¸‹å�ˆ2:08
 */
class ListPopupWindowHelper {

    public static ListPopupWindow newListPopupWindow(Context context) {
    	ListPopupWindow window = null;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        	window = new ListPopupWindowCompat(context);
        } else {
        	window = new ListPopupWindowNative(context);
        }
        if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4) {
    		window.setVerticalOffset(-5); // TAB 10
    	} else if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
    		window.setVerticalOffset(-5); // TAB 7
    	}
        return window;
    }
}
