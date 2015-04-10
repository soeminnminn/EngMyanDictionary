package com.s16.widget;

import com.s16.engmyan.R;
import com.s16.widget.popupmenu.PopupMenuCompat;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.ContextThemeWrapper;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;

public class MoreMenuActionProvider extends ActionProvider 
			implements OnMenuItemClickListener {

	private final int mSubMenuId;
	private final boolean mIsActionBarSupport;
	private final Menu mMenu;
	private final MenuItem mMenuItem;
	private ImageButton mButton;
	private PopupMenuCompat mPopupMenu;
	private boolean mEnabled = true;
	
	private static final int[] BUTTON_ATTRS = {
        android.R.attr.background,
        android.R.attr.src
	};
	
	public MoreMenuActionProvider(Context context, Menu menu, MenuItem menuItem, int subMenuId) {
		super(context);
		mMenu = menu;
		mMenuItem = menuItem;
		mSubMenuId = subMenuId;
		//mIsActionBarSupport = (android.os.Build.VERSION.SDK_INT > 10);
		mIsActionBarSupport = (android.os.Build.VERSION.SDK_INT > 20);
	}
	
	protected Activity getActivity() {
		return (Activity)getContext();
	}
	
	protected void showPopupMenu(View v) {
		if(mPopupMenu == null) {
			mPopupMenu = new PopupMenuCompat(getContext(), v);
			mPopupMenu.inflate(mSubMenuId);
			mPopupMenu.setOnMenuItemClickListener(new PopupMenuCompat.OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					return MoreMenuActionProvider.this.onMenuItemClick(item);
				}
			});			
		}
		
		mPopupMenu.show();
	}
	
	protected void onEnableChanged() {
		if (mButton != null) {
			mButton.setEnabled(mEnabled);
		}
		if (mMenuItem != null) {
			mMenuItem.setEnabled(mEnabled);
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		return getActivity().onOptionsItemSelected(item);
	}

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateActionView() {
		if (!mIsActionBarSupport) {
			final Context context = new ContextThemeWrapper(getContext(), R.style.ActionMenuButtonTheme);
			mButton = new ImageButton(context);
			
			final TypedArray appearance = context.getTheme().obtainStyledAttributes(BUTTON_ATTRS);
			mButton.setBackgroundDrawable(appearance.getDrawable(0));
			mButton.setImageDrawable(appearance.getDrawable(1));
			appearance.recycle();
			
			mButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			mButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					// PopupMenu is shown.
					MoreMenuActionProvider.this.showPopupMenu(view);
				}
			});
			return mButton;
		}
		return null;
	}
	
	@Override
	public boolean onPerformDefaultAction() {
		if (!mEnabled) return true;
		if (!mIsActionBarSupport && (mButton != null)) {
			showPopupMenu(mButton);
			return true;
		}
		return super.onPerformDefaultAction();
	}
	
	@Override
	public void onPrepareSubMenu(SubMenu subMenu) {
		if (mIsActionBarSupport) {
			subMenu.clear();
			if (mEnabled) {
				getActivity().getMenuInflater().inflate(mSubMenuId, subMenu);
			}
		}
    }

	public boolean hasSubMenu() {
        return mIsActionBarSupport && mEnabled;
    }
	
	public boolean isEnabled() {
		return mEnabled;
	}
	
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
		onEnableChanged();
	}
	
	public Menu getParentMenu() {
		return mMenu;
	}
	
	public MenuItem getParentMenuItem() {
		return mMenuItem;
	}
	
	public void performClick() {
		if (!mEnabled) {
			onEnableChanged();
			return;
		}
		
		if ((mMenu != null) && (mMenuItem != null)) {
			mMenu.performIdentifierAction(mMenuItem.getItemId(), 0);
		} else {
			onPerformDefaultAction();
		}
	}
}
