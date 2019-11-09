package com.s16.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GravityCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.s16.engmyan.R;

public class ActionBarNavigationButtons extends LinearLayout
		implements View.OnLongClickListener {
	
	protected static String TAG = ActionBarNavigationButtons.class.getSimpleName();
	
	public interface ActionBarContentViewActivity {
		void setActionBarContentView(ActionBarNavigationButtons view);
		CharSequence getTitle();
		boolean onOptionsItemSelected(MenuItem item);
	}
	
	public interface OnActionBarNavigationClickListener {
		public void onNavigationBack(View v);
		public void onNavigationForward(View v);
	}
	
	private TextView mTitle;
	private View mNavBack;
	private View mNavForward;
	private View mNavigation;
	private ActionBarContentViewActivity mContentViewActivity;
	private OnActionBarNavigationClickListener mNavigationClickListener;
	
	private boolean mTitleVisible;
	private boolean mNavigationVisible;
	private CharSequence mTitleText;
	private Typeface mTitleTypeFace;
	private boolean mNavBackEnabled;
	private boolean mNavForwardEnabled;
	
	private final OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.isEnabled()) {
				if (mNavigationClickListener != null) {
					if (v.getId() == R.id.actionbar_item_nav_back) {
						mNavigationClickListener.onNavigationBack(v);
						
					} else if (v.getId() == R.id.actionbar_item_nav_forward) {
						mNavigationClickListener.onNavigationForward(v);
					}
					
				} else if (mContentViewActivity != null) {
					final MenuItem item = new ActionMenuItem(v, v.getId());
					mContentViewActivity.onOptionsItemSelected(item);
				}
			}
		}
    	
    };
	
	public ActionBarNavigationButtons(Context context) {
		super(context);
		initialize();
	}
	
	public ActionBarNavigationButtons(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}
	
	public ActionBarNavigationButtons(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		initialize();
	}
	
	private void initialize() {
		mTitleVisible = true;
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		if (isInEditMode()) {
			return;
		}
		
		mTitle = (TextView)findViewById(R.id.actionbar_title);
		mNavBack = findViewById(R.id.actionbar_item_nav_back);
		mNavForward = findViewById(R.id.actionbar_item_nav_forward);
		mNavigation = findViewById(R.id.actionbar_navigation);
		
		if (mNavBack != null) {
			mNavBack.setLongClickable(true);
	        mNavBack.setOnLongClickListener(this);
	        mNavBack.setOnClickListener(mOnClickListener);
	        mNavBack.setEnabled(mNavBackEnabled);
		}
		
		if (mNavForward != null) {
			mNavForward.setLongClickable(true);
	        mNavForward.setOnLongClickListener(this);
	        mNavForward.setOnClickListener(mOnClickListener);
	        mNavForward.setEnabled(mNavForwardEnabled);
		}
		
		if (getContext() instanceof ActionBarContentViewActivity) {
			
			mContentViewActivity = (ActionBarContentViewActivity)getContext();
			if (mContentViewActivity != null) {
				mContentViewActivity.setActionBarContentView(this);
			}
		}
		onViewChanged();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		if (isInEditMode()) {
			super.setEnabled(enabled);
			return;
		}
		
		if (mNavBack != null) {
			mNavBack.setEnabled(mNavBackEnabled && enabled);
		}
		
		if (mNavForward != null) {
			mNavForward.setEnabled(mNavForwardEnabled && enabled);
		}
		super.setEnabled(enabled);
	}
	
	protected void onViewChanged() {
		if ((mTitle != null) && (mNavigation != null)) {
			if (mTitleTypeFace != null) {
				mTitle.setTypeface(mTitleTypeFace);
			}
			CharSequence title = getTitle();
			if (!TextUtils.isEmpty(title)) {
				mTitle.setText(title);
			}
			mTitle.setVisibility(mTitleVisible ? View.VISIBLE : View.GONE);
			
			if (mNavBack != null) {
				mNavBack.setEnabled(mNavBackEnabled);
			}
			if (mNavForward != null) {
				mNavForward.setEnabled(mNavForwardEnabled);
			}
			mNavigation.setVisibility(mNavigationVisible ? View.VISIBLE : View.GONE);
		}
	}
	
	public void setNavigationVisible(boolean visible) {
		mNavigationVisible = visible;
		onViewChanged();
	}
	
	public boolean getNavigationVisible() {
		return mNavigationVisible;
	}
	
	public void setTitleVisible(boolean visible) {
		mTitleVisible = visible;
		onViewChanged();
	}
	
	public boolean getTitleVisible() {
		return mTitleVisible;
	}
	
	public void setTitle(CharSequence title) {
		mTitleText = title;
		onViewChanged();
	}
	
	public void setTitle(int resId) {
		setTitle(getContext().getResources().getText(resId));
	}
	
	public CharSequence getTitle() {
		if (mTitleText == null) {
			if (mContentViewActivity != null) {
				mTitleText = mContentViewActivity.getTitle();
			} else if (mTitle != null) {
				mTitleText = mTitle.getText();
			}
		}
		return mTitleText;
	}
	
	public void setTitleTypeface(Typeface typeface) {
		mTitleTypeFace = typeface;
		onViewChanged();
	}
	
	public void setNavBackEnabled(boolean enabled) {
		if (mNavBackEnabled != enabled) {
			mNavBackEnabled = enabled;
			onViewChanged();
		}
	}
	
	public void setNavForwardEnabled(boolean enabled) {
		if (mNavForwardEnabled != enabled) {
			mNavForwardEnabled = enabled;
			onViewChanged();
		}
	}
	
	public void setNavigationClickListener(OnActionBarNavigationClickListener listener) {
		mNavigationClickListener = listener;
	}
	
	@Override
	public boolean onLongClick(View v) {
		if (!v.isEnabled()) return false;
		final CharSequence description = v.getContentDescription();
		if (TextUtils.isEmpty(description)) return false;
		
		final int[] screenPos = new int[2];
        final Rect displayFrame = new Rect();
        v.getLocationOnScreen(screenPos);
        v.getWindowVisibleDisplayFrame(displayFrame);

		final Context context = getContext();
        final int width = v.getWidth();
        final int height = v.getHeight();
        final int midy = screenPos[1] + height / 2;
        final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        Toast cheatSheet = Toast.makeText(context, description, Toast.LENGTH_SHORT);
        if (midy < displayFrame.height()) {
            // Show along the top; follow action buttons
            cheatSheet.setGravity(Gravity.TOP | GravityCompat.END, screenWidth - screenPos[0] - width / 2, height);
        } else {
            // Show along the bottom center
            cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
        }
        cheatSheet.show();
		return true;
	}
	
	private class ActionMenuItem implements MenuItem {

		private View mActionView;
		private final int mId;
		
		public ActionMenuItem(View view, int id) {
			mActionView = view;
			mId = id;
		}
		
		@Override
		public int getItemId() {
			return mId;
		}

		@Override
		public int getGroupId() {
			return 0;
		}

		@Override
		public int getOrder() {
			return 0;
		}

		@Override
		public MenuItem setTitle(CharSequence title) {
			return this;
		}

		@Override
		public MenuItem setTitle(int title) {
			return this;
		}

		@Override
		public CharSequence getTitle() {
			return null;
		}

		@Override
		public MenuItem setTitleCondensed(CharSequence title) {
			return this;
		}

		@Override
		public CharSequence getTitleCondensed() {
			return null;
		}

		@Override
		public MenuItem setIcon(Drawable icon) {
			return null;
		}

		@Override
		public MenuItem setIcon(int iconRes) {
			return null;
		}

		@Override
		public Drawable getIcon() {
			return null;
		}

		@Override
		public MenuItem setIntent(Intent intent) {
			return this;
		}

		@Override
		public Intent getIntent() {
			return null;
		}

		@Override
		public MenuItem setShortcut(char numericChar, char alphaChar) {
			return this;
		}

		@Override
		public MenuItem setNumericShortcut(char numericChar) {
			return null;
		}

		@Override
		public char getNumericShortcut() {
			return 0;
		}

		@Override
		public MenuItem setAlphabeticShortcut(char alphaChar) {
			return this;
		}

		@Override
		public char getAlphabeticShortcut() {
			return 0;
		}

		@Override
		public MenuItem setCheckable(boolean checkable) {
			return this;
		}

		@Override
		public boolean isCheckable() {
			return false;
		}

		@Override
		public MenuItem setChecked(boolean checked) {
			return this;
		}

		@Override
		public boolean isChecked() {
			return false;
		}

		@Override
		public MenuItem setVisible(boolean visible) {
			return this;
		}

		@Override
		public boolean isVisible() {
			return false;
		}

		@Override
		public MenuItem setEnabled(boolean enabled) {
			return this;
		}

		@Override
		public boolean isEnabled() {
			return false;
		}

		@Override
		public boolean hasSubMenu() {
			return false;
		}

		@Override
		public SubMenu getSubMenu() {
			return null;
		}

		@Override
		public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
			return this;
		}

		@Override
		public ContextMenuInfo getMenuInfo() {
			return null;
		}

		@Override
		public void setShowAsAction(int actionEnum) {
		}

		@Override
		public MenuItem setShowAsActionFlags(int actionEnum) {
			return this;
		}

		@Override
		public MenuItem setActionView(View view) {
			mActionView = view;
			return this;
		}

		@Override
		public MenuItem setActionView(int resId) {
			return this;
		}

		@Override
		public View getActionView() {
			return mActionView;
		}

		@Override
		public MenuItem setActionProvider(ActionProvider actionProvider) {
			return this;
		}

		@Override
		public ActionProvider getActionProvider() {
			return null;
		}

		@Override
		public boolean expandActionView() {
			return false;
		}

		@Override
		public boolean collapseActionView() {
			return false;
		}

		@Override
		public boolean isActionViewExpanded() {
			return false;
		}

		@Override
		public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
			return this;
		}
		
	}
}
