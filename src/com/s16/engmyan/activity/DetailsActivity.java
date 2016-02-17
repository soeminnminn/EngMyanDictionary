package com.s16.engmyan.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.SystemUiUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.s16.engmyan.Common;
import com.s16.engmyan.Constants;
import com.s16.engmyan.R;
import com.s16.engmyan.data.DictionaryItem;
import com.s16.engmyan.data.SearchQueryHelper;
import com.s16.engmyan.data.UserQueryHelper;
import com.s16.engmyan.fragment.DetailsFragment;
import com.s16.widget.ActionBarNavigationButtons;

public class DetailsActivity extends AppCompatActivity {
	
	protected static final String TAG = DetailsActivity.class.getSimpleName();
	
	private ActionBarNavigationButtons.OnActionBarNavigationClickListener mNavigationClickListener = 
			new ActionBarNavigationButtons.OnActionBarNavigationClickListener() {
		
		@Override
		public void onNavigationForward(View v) {
			performNavForward();
		}
		
		@Override
		public void onNavigationBack(View v) {
			performNavBack();
		}
	}; 
	
	private DetailsFragment.DetailsDataChangeListener mDataChangeListener = new DetailsFragment.DetailsDataChangeListener() {
		
		@Override
		public void onNavigationChanged(boolean navBackEnabled,
				boolean navForwardEnabled) {
			if (mActionBarContent != null) {
				mActionBarContent.setNavBackEnabled(navBackEnabled);
				mActionBarContent.setNavForwardEnabled(navForwardEnabled);
			}
		}
		
		@Override
		public void onLoadFinished() {
			setDetailsTitle();
			updateMenu();
			setIfFavorties();
		}
		
		@Override
		public DictionaryItem onLoadDetailData(long id, String word) {
			if (id > -1) {
				return DictionaryItem.getFrom(SearchQueryHelper.getInstance(getContext()), id);
			}
			return DictionaryItem.getFrom(SearchQueryHelper.getInstance(getContext()), word);
		}
	};
	
	private DetailsFragment mDetailsFragment;
	private ActionBarNavigationButtons mActionBarContent;
	
	private MenuItem mMenuItemFavorite;
	private MenuItem mMenuItemSound;
	private MenuItem mMenuItemPicture;
	
	protected Context getContext() {
		return this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		
		SystemUiUtils.setStatusBarColor(this, getResources().getColor(R.color.app_color_primary_dark));
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		
		long id = getIntent().getLongExtra(Constants.DETAIL_ID_KEY, -1);
		
		FragmentManager manager = getSupportFragmentManager();
		mDetailsFragment = (DetailsFragment)manager.findFragmentById(R.id.detailsContainer);
		if (mDetailsFragment != null) {
			mDetailsFragment.setDetailsDataChangeListener(mDataChangeListener);
			
			if (id >= 0) {
				DictionaryItem itemData = DictionaryItem.getFrom(SearchQueryHelper.getInstance(getContext()), id);
				mDetailsFragment.setData(itemData);
			}
		}
		
		mActionBarContent = (ActionBarNavigationButtons)toolbar.findViewById(R.id.frameToolbarContent);
		if (mActionBarContent != null) {
			mActionBarContent.setNavigationVisible(true);
			mActionBarContent.setNavigationClickListener(mNavigationClickListener);
			
			if (mDetailsFragment != null) {
				mActionBarContent.setNavBackEnabled(mDetailsFragment.getCanGoBack());
				mActionBarContent.setNavForwardEnabled(mDetailsFragment.getCanGoForward());
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.details, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		mMenuItemFavorite = menu.findItem(R.id.action_favorite);
		if (mMenuItemFavorite != null) {
			MenuItemCompat.setShowAsAction(mMenuItemFavorite, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			setIfFavorties();
		}
		
		mMenuItemSound = menu.findItem(R.id.action_sound);
		if (mMenuItemSound != null) {
			MenuItemCompat.setShowAsAction(mMenuItemSound, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		}
		mMenuItemPicture = menu.findItem(R.id.action_picture);
		if (mMenuItemPicture != null) {
			MenuItemCompat.setShowAsAction(mMenuItemPicture, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		}
		
		updateMenu();
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			case R.id.action_favorite:
				performFavorite();
				break;
			case R.id.action_sound:
				performSpeak();
				break;
			case R.id.action_picture:
				toggleImageView();
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		if ((mDetailsFragment != null) && (mDetailsFragment.getImageVisible())) {
			toggleImageView();
			return;
		}
		
		super.onBackPressed();
		finish();
	}
	
	private void updateMenu() {
		if (mDetailsFragment != null) {
			if (mMenuItemSound != null) {
				mMenuItemSound.setVisible(mDetailsFragment.getHasSound());
			}
			
			if (mMenuItemPicture != null) {
				mMenuItemPicture.setVisible(mDetailsFragment.getHasPicture());
			}
		}
	}
	
	private void setDetailsTitle() {
		if (mDetailsFragment == null) return;
		String title = mDetailsFragment.getTitle();
		if (!TextUtils.isEmpty(title) && mActionBarContent != null) {
			mActionBarContent.setTitle(title);
		}
	}
	
	private void performNavBack() {
		if (mDetailsFragment != null) {
			mDetailsFragment.performNavBack();
		}
	}
	
	private void performNavForward() {
		if (mDetailsFragment != null) {
			mDetailsFragment.performNavForward();
		}
	}
	
	private void setIfFavorties() {
		if ((mDetailsFragment != null) && (mMenuItemFavorite != null)) {
			long id = mDetailsFragment.getDetailId();
			if ((id > -1) && (UserQueryHelper.getInstance(getContext()).isFavorited(id))) {
				mMenuItemFavorite.setIcon(R.drawable.ic_favorite_on_36dp);
			} else {
				mMenuItemFavorite.setIcon(R.drawable.ic_favorite_off_36dp);
			}
		}
	}
	
	private void performFavorite() {
		if (mDetailsFragment != null) {
			long id = mDetailsFragment.getDetailId();
			if (id > -1) {
				UserQueryHelper queryHelper = UserQueryHelper.getInstance(getContext());
				if (!queryHelper.isFavorited(id)) {
					queryHelper.createFavorite(mDetailsFragment.getTitle(), id);
					
					if (queryHelper.isFavorited(id)) {
						Common.showMessage(getContext(), R.string.add_favorites_message);
						mMenuItemFavorite.setIcon(R.drawable.ic_favorite_on_36dp);
					}
				} else {
					int result = queryHelper.removeFavoriteByRef(id);
					if (result == 1) {
						Common.showMessage(getContext(), R.string.remove_favorites_message);
						mMenuItemFavorite.setIcon(R.drawable.ic_favorite_off_36dp);
					}
				}
			}
		}
	}
	
	private void toggleImageView() {
		if (mDetailsFragment != null) {
			mDetailsFragment.toggleImageView();
		}
	}
	
	private void performSpeak() {
		if (mDetailsFragment != null) {
			mDetailsFragment.doSpeak();
		}
	}
}
