package com.s16.widget.popupmenu.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

public class MenuItemCompat implements MenuItem {

	private CharSequence mTitle;
	private CharSequence mTitleCondensed;
	private int mGroupId;
	private int mItemId;
	private int mOrder;
	private Drawable mIcon;
	private Intent mIntent;
	private SubMenu mSubMenu;
	private final Context mContext;
	private boolean mVisible = true;
	private boolean mEnabled = true;
	private boolean mCheckable;
	private boolean mChecked;
	private View mActionView;
	private char mAlphabeticShortcut;
	private char mNumericShortcut;
	protected int mShowAsAction;
	protected int mShowAsActionFlags;
	private MenuHelper.OnMenuChangedListener mOnMenuChangedListener;
	private OnMenuItemClickListener mOnMenuItemClickListener;

	public MenuItemCompat(final Context context) {
		this.mContext = context;
	}
	
	public MenuItemCompat(final Context context, MenuHelper.OnMenuChangedListener listener) {
		this.mContext = context;
		mOnMenuChangedListener = listener;
	}
	
	private void onMenuItemChanged() {
		if (mOnMenuChangedListener != null) {
			mOnMenuChangedListener.onMenuItemChanged(this);
		}
	}
	
	protected LayoutInflater getLayoutInflater() {
		return LayoutInflater.from(mContext);
	}
	
	public void setOnMenuChangedListener(MenuHelper.OnMenuChangedListener listener) {
    	mOnMenuChangedListener = listener;
    }

	@Override
	public boolean collapseActionView() {
		return false;
	}

	@Override
	public boolean expandActionView() {
		return false;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public ActionProvider getActionProvider() {
		return null;
	}

	@Override
	public View getActionView() {
		return mActionView;
	}

	@Override
	public char getAlphabeticShortcut() {
		return mAlphabeticShortcut;
	}

	@Override
	public int getGroupId() {
		return mGroupId;
	}

	@Override
	public Drawable getIcon() {
		return mIcon;
	}

	@Override
	public Intent getIntent() {
		return mIntent;
	}

	@Override
	public int getItemId() {
		return mItemId;
	}

	@Override
	public ContextMenuInfo getMenuInfo() {
		return null;
	}

	@Override
	public char getNumericShortcut() {
		return mNumericShortcut;
	}

	@Override
	public int getOrder() {
		return mOrder;
	}

	@Override
	public SubMenu getSubMenu() {
		return mSubMenu;
	}

	@Override
	public CharSequence getTitle() {
		return mTitle;
	}

	@Override
	public CharSequence getTitleCondensed() {
		return mTitleCondensed;
	}
	
	public OnMenuItemClickListener getOnMenuItemClickListener() {
		return mOnMenuItemClickListener;
	}

	@Override
	public boolean hasSubMenu() {
		return mSubMenu != null;
	}

	@Override
	public boolean isActionViewExpanded() {
		return false;
	}

	@Override
	public boolean isCheckable() {
		return mCheckable;
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public boolean isEnabled() {
		return mEnabled;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean isVisible() {
		return mVisible;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public MenuItem setActionProvider(final ActionProvider actionProvider) {
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setActionView(final int resId) {
		mActionView = resId == 0 ? null : getLayoutInflater().inflate(resId, null);
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setActionView(final View view) {
		mActionView = view;
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setAlphabeticShortcut(final char alphaChar) {
		mAlphabeticShortcut = alphaChar;
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setCheckable(final boolean checkable) {
		mCheckable = checkable;
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setChecked(final boolean checked) {
		mChecked = checked;
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setEnabled(final boolean enabled) {
		mEnabled = enabled;
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setIcon(final Drawable icon) {
		mIcon = icon;
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setIcon(final int iconRes) {
		mIcon = iconRes == 0 ? null : mContext.getResources().getDrawable(iconRes);
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setIntent(final Intent intent) {
		mIntent = intent;
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setNumericShortcut(final char numericChar) {
		mNumericShortcut = numericChar;
		onMenuItemChanged();
		return this;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public MenuItem setOnActionExpandListener(final OnActionExpandListener listener) {
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setOnMenuItemClickListener(final OnMenuItemClickListener menuItemClickListener) {
		mOnMenuItemClickListener = menuItemClickListener;
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setShortcut(final char numericChar, final char alphaChar) {
		mNumericShortcut = numericChar;
		mAlphabeticShortcut = alphaChar;
		onMenuItemChanged();
		return this;
	}

	@Override
	public void setShowAsAction(final int actionEnum) {
		mShowAsAction = actionEnum;
		onMenuItemChanged();
	}

	@Override
	public MenuItem setShowAsActionFlags(final int actionEnum) {
		mShowAsActionFlags = actionEnum;
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setTitle(final CharSequence title) {
		mTitle = title;
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setTitle(final int titleRes) {
		mTitle = mContext.getString(titleRes);
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setTitleCondensed(final CharSequence titleCondensed) {
		mTitleCondensed = titleCondensed;
		onMenuItemChanged();
		return this;
	}

	@Override
	public MenuItem setVisible(final boolean visible) {
		mVisible = visible;
		onMenuItemChanged();
		return this;
	}

    @Override
    public String toString() {
        return "MenuItemCompat{" +
                "title=" + mTitle +
                ", itemId=" + mItemId +
                ", order=" + mOrder +
                ", groupId=" + mGroupId +
                ", enabled=" + mEnabled +
                ", icon=" + mIcon +
                '}';
    }

	MenuItemCompat setGroupId(final int groupId) {
		mGroupId = groupId;
		onMenuItemChanged();
		return this;
	}

	MenuItemCompat setItemId(final int itemId) {
		mItemId = itemId;
		onMenuItemChanged();
		return this;
	}

	MenuItemCompat setOrder(final int order) {
		mOrder = order;
		onMenuItemChanged();
		return this;
	}

	MenuItemCompat setSubMenu(final SubMenu subMenu) {
		mSubMenu = subMenu;
		mSubMenu.setHeaderTitle(getTitle());
		onMenuItemChanged();
		return this;
	}

	public static MenuItem createItem(final Context context, final int id) {
		return new MenuItemCompat(context).setItemId(id);
	}
}
