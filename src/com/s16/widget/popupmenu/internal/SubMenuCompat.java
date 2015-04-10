package com.s16.widget.popupmenu.internal;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

final class SubMenuCompat extends MenuCompat implements SubMenu {
    private final MenuItem mMenuItem;

    SubMenuCompat(final Context context, final MenuItem menuItem) {
        super(context);
        mMenuItem = menuItem;
    }

    @Override
    public MenuItem getItem() {
        return mMenuItem;
    }


    @Override
    public void clearHeader() {
    }

    @Override
    public SubMenu setHeaderIcon(final Drawable icon) {
        return this;
    }

    @Override
    public SubMenu setHeaderIcon(final int iconRes) {
        return this;
    }

    @Override
    public SubMenu setHeaderTitle(final CharSequence title) {

        return this;
    }

    @Override
    public SubMenu setHeaderTitle(final int titleRes) {

        return this;
    }

    @Override
    public SubMenu setHeaderView(final View view) {

        return this;
    }

    @Override
    public SubMenu setIcon(final Drawable icon) {

        return this;
    }

    @Override
    public SubMenu setIcon(final int iconRes) {

        return this;
    }

}
