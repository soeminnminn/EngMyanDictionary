package com.s16.widget.popupmenu.internal;

import android.view.MenuItem;

import java.util.ArrayList;

@SuppressWarnings("serial")
final class MenuItemList extends ArrayList<MenuItem> {

    @Override
    public void add(final int index, final MenuItem object) {
        super.add(index, object);
    }

    @Override
    public boolean add(final MenuItem object) {
        add(findInsertIndex(object.getOrder()), object);
        return true;
    }

    private int findInsertIndex(final int order) {
        for (int i = size() - 1; i >= 0; i--) {
            final MenuItem item = get(i);
            if (item.getOrder() <= order) return i + 1;
        }
        return 0;
    }

}
