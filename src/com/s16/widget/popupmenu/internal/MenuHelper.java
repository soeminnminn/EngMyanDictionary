package com.s16.widget.popupmenu.internal;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

/**
 * User: mcxiaoke
 * Date: 13-10-2
 * Time: ä¸‹å�ˆ11:57
 */
public final class MenuHelper {

    private MenuHelper() {
    }

    public static Menu createMenu(Context context) {
        return new MenuCompat(context);
    }
    
    public static Menu createMenu(Context context, OnMenuChangedListener listener) {
    	MenuCompat menu = new MenuCompat(context);
    	menu.setOnMenuChangedListener(listener);
    	return menu;
    }
    
    public interface OnMenuChangedListener {
    	public void onMenuChanged(Menu menu);
    	public void onMenuItemChanged(MenuItem menuItem);
    }
}
