package com.s16.widget;

import com.s16.engmyan.R;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.support.v7.widget.PopupMenu;
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
	private PopupMenu mPopupMenu;
	private boolean mEnabled = true;
	
	public MoreMenuActionProvider(Context context, Menu menu, MenuItem menuItem, int subMenuId) {
		super(context);
		mMenu = menu;
		mMenuItem = menuItem;
		mSubMenuId = subMenuId;
		mIsActionBarSupport = (android.os.Build.VERSION.SDK_INT > 10);
	}
	
	protected Activity getActivity() {
		return (Activity)getContext();
	}
	
	protected void showPopupMenu(View v) {
		if(mPopupMenu == null) {
			//final PopupMenu popupMenu = new PopupMenu(getContext(), v);
			mPopupMenu = new PopupMenu(new ContextThemeWrapper(getContext(), R.style.ActionMenuTheme), v);
			mPopupMenu.inflate(mSubMenuId);
			mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				
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

	@Override
	public View onCreateActionView() {
		if (!mIsActionBarSupport) {
			mButton = new ImageButton(new ContextThemeWrapper(getContext(), R.style.ActionMenuButtonTheme));
			mButton.setBackgroundResource(R.drawable.more_menu_button);
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
			if (mEnabled) 
				getActivity().getMenuInflater().inflate(mSubMenuId, subMenu);
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
