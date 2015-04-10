package com.s16.widget.popupmenu;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow.OnDismissListener;

import com.s16.engmyan.BuildConfig;
import com.s16.engmyan.R;
import com.s16.widget.popupmenu.internal.MenuHelper;
import com.s16.widget.popupmenu.internal.PopupMenuAdapter;

public class PopupMenuCompat implements OnDismissListener, OnItemClickListener, OnTouchListener {
    public static final String TAG = PopupMenuCompat.class.getSimpleName();
    public static final boolean DEBUG = BuildConfig.DEBUG;

    public enum Style {
        DARK, LIGHT
    }

    private OnMenuItemClickListener mItemClickListener;
    private OnDismissListener mDismissListener;

    private final Context mContext;
    private Menu mMenu;
    private final View mView;
    private final ListPopupWindow mWindow;

    private boolean mDidAction;

    private final PopupMenuAdapter mAdapter;

    /**
     * Constructor for default vertical layout
     *
     * @param context Context
     */
    public PopupMenuCompat(final Context context, final View view) {
        if (DEBUG) {
            Log.v(TAG, "PopupMenuCompat()");
        }
        mContext = context;
        mView = view;
        mMenu = MenuHelper.createMenu(context);
        mAdapter = new PopupMenuAdapter(context);
        mWindow = ListPopupWindowHelper.newListPopupWindow(context);
        mWindow.setInputMethodMode(ListPopupWindowCompat.INPUT_METHOD_NOT_NEEDED);
        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mWindow.setAnchorView(mView);
        mWindow.setWidth(mContext.getResources().getDimensionPixelSize(R.dimen.popup_window_width));
        mWindow.setAdapter(mAdapter);
        mWindow.setOnItemClickListener(this);
        mWindow.setModel(true);
    }
    
    protected Context getContext() {
    	return mContext;
    }

    /**
     * Dismiss the popup window.
     */
    public void dismiss() {
        if (DEBUG) {
            Log.v(TAG, "dismiss()");
        }
        if (isShowing()) {
            mWindow.dismiss();
        }
    }

    public Menu getMenu() {
        return mMenu;
    }

    public MenuInflater getMenuInflater() {
        return new MenuInflater(getContext());
    }

    public void inflate(final int menuRes) {
        if (DEBUG) {
            Log.v(TAG, "inflate() menuRes=" + menuRes);
        }
        new MenuInflater(mContext).inflate(menuRes, mMenu);
    }

    public boolean isShowing() {
        return mWindow != null && mWindow.isShowing();
    }

    @Override
    public void onDismiss() {
        if (DEBUG) {
            Log.v(TAG, "onDismiss()");
        }
        if (!mDidAction && mDismissListener != null) {
            mDismissListener.onDismiss(this);
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
        if (DEBUG) {
            Log.v(TAG, "onItemClick() position=" + position + " id=" + id);
        }
        mDidAction = true;
        dismiss();
        final MenuItem item = mAdapter.getItem(position);
        if (item.hasSubMenu()) {
            if (item.getSubMenu().size() == 0) {
                return;
            }
            showMenu(item.getSubMenu());
        } else {
            if (mItemClickListener != null) {
                mItemClickListener.onMenuItemClick(item);
            } else if (getContext() instanceof Activity) {
    			((Activity)getContext()).onOptionsItemSelected(item);
    		}
        }
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            mWindow.dismiss();
            return true;
        }
        return false;
    }

    public void setMenu(final Menu menu) {
        if (DEBUG) {
            Log.v(TAG, "setMenu()");
        }
        mMenu = menu;
    }

    /**
     * Set listener for window dismissed. This listener will only be fired if
     * the popupmenu dialog is dismissed by clicking outside the dialog or
     * clicking on sticky item.
     */
    public void setOnDismissListener(final PopupMenuCompat.OnDismissListener listener) {
        mWindow.setOnDismissListener(listener != null ? this : null);
        mDismissListener = listener;
    }

    /**
     * Set listener for action item clicked.
     *
     * @param listener Listener
     */
    public void setOnMenuItemClickListener(final OnMenuItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void show() {
        if (isShowing()) {
            dismiss();
        }
        showMenu(getMenu());
    }

    private void showMenu(final Menu menu) {
        if (DEBUG) {
            Log.v(TAG, "showMenu() menu=" + menu);
        }
        mAdapter.setMenu(menu);
        if (DEBUG) {
            Log.v(TAG, "showMenu() items=" + mAdapter.getAllItems());
        }
        try {
            mWindow.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Listener for window dismiss
     */
    public static interface OnDismissListener {
        public void onDismiss(PopupMenuCompat PopupMenu);
    }

    /**
     * Listener for item click
     */
    public static interface OnMenuItemClickListener {
        public boolean onMenuItemClick(MenuItem item);
    }

}
