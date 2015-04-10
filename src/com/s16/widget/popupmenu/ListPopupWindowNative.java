package com.s16.widget.popupmenu;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

/**
 * User: mcxiaoke
 * Date: 13-10-3
 * Time: ä¸‹å�ˆ2:09
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class ListPopupWindowNative implements ListPopupWindow {

    private android.widget.ListPopupWindow mListPopupWindow;

    /**
     * Create a new, empty popup window capable of displaying items from a ListAdapter.
     * Backgrounds should be set using {@link #setBackgroundDrawable(Drawable)}.
     *
     * @param context Context used for contained views.
     */

    public ListPopupWindowNative(Context context) {
        mListPopupWindow = new android.widget.ListPopupWindow(context);
    }

    /**
     * Create a new, empty popup window capable of displaying items from a ListAdapter.
     * Backgrounds should be set using {@link #setBackgroundDrawable(Drawable)}.
     *
     * @param context Context used for contained views.
     * @param attrs   Attributes from inflating parent views used to style the popup.
     */
    public ListPopupWindowNative(Context context, AttributeSet attrs) {
        mListPopupWindow = new android.widget.ListPopupWindow(context, attrs);
    }

    /**
     * Create a new, empty popup window capable of displaying items from a ListAdapter.
     * Backgrounds should be set using {@link #setBackgroundDrawable(Drawable)}.
     *
     * @param context      Context used for contained views.
     * @param attrs        Attributes from inflating parent views used to style the popup.
     * @param defStyleAttr Default style attribute to use for popup content.
     */
    public ListPopupWindowNative(Context context, AttributeSet attrs, int defStyleAttr) {
        mListPopupWindow = new android.widget.ListPopupWindow(context, attrs, defStyleAttr);
    }

    /**
     * Create a new, empty popup window capable of displaying items from a ListAdapter.
     * Backgrounds should be set using {@link #setBackgroundDrawable(Drawable)}.
     *
     * @param context      Context used for contained views.
     * @param attrs        Attributes from inflating parent views used to style the popup.
     * @param defStyleAttr Style attribute to read for default styling of popup content.
     * @param defStyleRes  Style resource ID to use for default styling of popup content.
     */
    public ListPopupWindowNative(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mListPopupWindow = new android.widget.ListPopupWindow(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        mListPopupWindow.setAdapter(adapter);
    }

    @Override
    public void setPromptPosition(int position) {
        mListPopupWindow.setPromptPosition(position);
    }

    @Override
    public int getPromptPosition() {
        return mListPopupWindow.getPromptPosition();
    }

    @Override
    public void setModel(boolean modal) {
        mListPopupWindow.setModal(modal);
    }

    @Override
    public boolean isModel() {
        return mListPopupWindow.isModal();
    }

    @Override
    public void setDropDownAlwaysVisible(boolean dropDownAlwaysVisible) {
    }

    @Override
    public boolean isDropDownAlwaysVisible() {
        return false;
    }

    @Override
    public void setSoftInputMode(int mode) {
        mListPopupWindow.setSoftInputMode(mode);
    }

    @Override
    public int getSoftInputMode() {
        return mListPopupWindow.getSoftInputMode();
    }

    @Override
    public void setListSelector(Drawable selector) {
        mListPopupWindow.setListSelector(selector);
    }

    @Override
    public Drawable getBackground() {
        return mListPopupWindow.getBackground();
    }

    @Override
    public void setBackgroundDrawable(Drawable d) {
        mListPopupWindow.setBackgroundDrawable(d);
    }

    @Override
    public void setAnimationStyle(int animationStyle) {
        mListPopupWindow.setAnimationStyle(animationStyle);
    }

    @Override
    public int getAnimationStyle() {
        return mListPopupWindow.getAnimationStyle();
    }

    @Override
    public View getAnchorView() {
        return mListPopupWindow.getAnchorView();
    }

    @Override
    public void setAnchorView(View anchor) {
        mListPopupWindow.setAnchorView(anchor);
    }

    @Override
    public int getHorizontalOffset() {
        return mListPopupWindow.getHorizontalOffset();
    }

    @Override
    public void setHorizontalOffset(int offset) {
        mListPopupWindow.setHorizontalOffset(offset);
    }

    @Override
    public int getVerticalOffset() {
        return mListPopupWindow.getVerticalOffset();
    }

    @Override
    public void setVerticalOffset(int offset) {
        mListPopupWindow.setVerticalOffset(offset);
    }

    @Override
    public int getWidth() {
        return mListPopupWindow.getWidth();
    }

    @Override
    public void setWidth(int width) {
        mListPopupWindow.setWidth(width);
    }

    @Override
    public void setContentWidth(int width) {
        mListPopupWindow.setContentWidth(width);
    }

    @Override
    public int getHeight() {
        return mListPopupWindow.getHeight();
    }

    @Override
    public void setHeight(int height) {
        mListPopupWindow.setHeight(height);
    }

    @Override
    public void setOnItemClickListener(AdapterView.OnItemClickListener clickListener) {
        mListPopupWindow.setOnItemClickListener(clickListener);
    }

    @Override
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener selectedListener) {
        mListPopupWindow.setOnItemSelectedListener(selectedListener);
    }

    @Override
    public void setPromptView(View prompt) {
        mListPopupWindow.setPromptView(prompt);
    }

    @Override
    public void postShow() {
        mListPopupWindow.postShow();
    }

    @Override
    public void show() {
        mListPopupWindow.show();
    }

    @Override
    public void dismiss() {
        mListPopupWindow.dismiss();
    }

    @Override
    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        mListPopupWindow.setOnDismissListener(listener);
    }

    @Override
    public void setInputMethodMode(int mode) {
        mListPopupWindow.setInputMethodMode(mode);
    }

    @Override
    public int getInputMethodMode() {
        return mListPopupWindow.getInputMethodMode();
    }

    @Override
    public void setSelection(int position) {
        mListPopupWindow.setSelection(position);
    }

    @Override
    public void clearListSelection() {
        mListPopupWindow.clearListSelection();
    }

    @Override
    public boolean isShowing() {
        return mListPopupWindow.isShowing();
    }

    @Override
    public boolean isInputMethodNotNeeded() {
        return mListPopupWindow.isInputMethodNotNeeded();
    }

    @Override
    public boolean performItemClick(int position) {
        return mListPopupWindow.performItemClick(position);
    }

    @Override
    public Object getSelectedItem() {
        return mListPopupWindow.getSelectedItem();
    }

    @Override
    public int getSelectedItemPosition() {
        return mListPopupWindow.getSelectedItemPosition();
    }

    @Override
    public long getSelectedItemId() {
        return mListPopupWindow.getSelectedItemId();
    }

    @Override
    public View getSelectedView() {
        return mListPopupWindow.getSelectedView();
    }

    @Override
    public ListView getListView() {
        return mListPopupWindow.getListView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mListPopupWindow.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mListPopupWindow.onKeyUp(keyCode, event);
    }

}
