package com.s16.widget.popupmenu;

import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

/**
 * User: mcxiaoke
 * Date: 13-10-3
 * Time: ä¸‹å�ˆ2:06
 */
interface ListPopupWindow {
    void setAdapter(ListAdapter adapter);

    void setPromptPosition(int position);

    int getPromptPosition();

    void setModel(boolean model);

    boolean isModel();

    void setDropDownAlwaysVisible(boolean dropDownAlwaysVisible);

    boolean isDropDownAlwaysVisible();

    void setSoftInputMode(int mode);

    int getSoftInputMode();

    void setListSelector(Drawable selector);

    Drawable getBackground();

    void setBackgroundDrawable(Drawable d);

    void setAnimationStyle(int animationStyle);

    int getAnimationStyle();

    View getAnchorView();

    void setAnchorView(View anchor);

    int getHorizontalOffset();

    void setHorizontalOffset(int offset);

    int getVerticalOffset();

    void setVerticalOffset(int offset);

    int getWidth();

    void setWidth(int width);

    void setContentWidth(int width);

    int getHeight();

    void setHeight(int height);

    void setOnItemClickListener(AdapterView.OnItemClickListener clickListener);

    void setOnItemSelectedListener(AdapterView.OnItemSelectedListener selectedListener);

    void setPromptView(View prompt);

    void postShow();

    void show();

    void dismiss();

    void setOnDismissListener(PopupWindow.OnDismissListener listener);

    void setInputMethodMode(int mode);

    int getInputMethodMode();

    void setSelection(int position);

    void clearListSelection();

    boolean isShowing();

    boolean isInputMethodNotNeeded();

    boolean performItemClick(int position);

    Object getSelectedItem();

    int getSelectedItemPosition();

    long getSelectedItemId();

    View getSelectedView();

    ListView getListView();

    boolean onKeyDown(int keyCode, KeyEvent event);

    boolean onKeyUp(int keyCode, KeyEvent event);
}
