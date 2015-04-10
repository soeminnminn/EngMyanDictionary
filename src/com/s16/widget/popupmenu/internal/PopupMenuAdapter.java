package com.s16.widget.popupmenu.internal;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.s16.engmyan.BuildConfig;
import com.s16.engmyan.R;

public final class PopupMenuAdapter extends ArrayAdapterCompat<MenuItem> {
    public static final String TAG = PopupMenuAdapter.class.getSimpleName();
    public static final boolean DEBUG = BuildConfig.DEBUG;

    private Menu mMenu;
    private LayoutInflater mInflater;

    public PopupMenuAdapter(final Context context) {
        super(context);
        if (DEBUG) {
            Log.v(TAG, "PopupMenuAdapter()");
        }
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public long getItemId(final int index) {
        return getItem(index).getItemId();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.pm_list_item_popup_menu, parent, false);
        } else {
            view = convertView;
        }
        final TextView text = (TextView) view.findViewById(android.R.id.text1);
        final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
        final MenuItem item = getItem(position);
        text.setText(item.getTitle());
        text.setVisibility(View.VISIBLE);
        icon.setImageDrawable(item.getIcon());
        icon.setVisibility(item.getIcon() != null ? View.VISIBLE : View.GONE);
        return view;
    }

    @Override
    public boolean isEnabled(final int position) {
        return getItem(position).isEnabled();
    }

    public void setMenu(final Menu menu) {
        mMenu = menu;
        setMenuItems();
        if (DEBUG) {
            Log.v(TAG, "setMenu()");
        }
    }

    public void setMenuItems() {
        if (mMenu instanceof MenuCompat) {
            MenuCompat menuCompat = (MenuCompat) mMenu;
            setMenuItems(menuCompat.getMenuItems());
        }
    }

    public void setMenuItems(List<MenuItem> items) {
        clear();
        if (items == null) {
            return;
        }
        for (final MenuItem item : items) {
            if (item.isVisible()) {
                add(item);
            }
        }
    }

}
