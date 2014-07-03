package com.s16.engmyan.activity;

import com.s16.engmyan.Constants;
import com.s16.widget.ActionBarNavigationButtons;
import com.s16.engmyan.data.DictionaryDataProvider;
import com.s16.engmyan.data.DictionaryItem;
import com.s16.engmyan.data.UserDataProvider;
import com.s16.engmyan.fragment.DetailViewFragment;
import com.s16.engmyan.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class DetailActivity extends ActionBarActivity
		implements ActionBarNavigationButtons.ActionBarContentViewActivity {
	
	protected static String TAG = DetailActivity.class.getSimpleName();
	
	private DetailViewFragment mDetailView;
	private MenuItem mMenuItemFavorite;
	private MenuItem mMenuItemSound;
	private MenuItem mMenuItemPicture;
	private ActionBarNavigationButtons mActionBarContent;
	
	private DictionaryDataProvider mDictDataProvider;
	
	private DetailViewFragment.DetailDataChangeListener mDataChangeListener =
			new DetailViewFragment.DetailDataChangeListener() {

				@Override
				public void onNavigationChanged(boolean navBackEnabled,
						boolean navForwardEnabled) {
					if (mActionBarContent != null) {
						mActionBarContent.setNavBackEnabled(navBackEnabled);
						mActionBarContent.setNavForwardEnabled(navForwardEnabled);
					}
				}

				@Override
				public DictionaryItem onLoadDetailData(long id, String word) {
					if (mDictDataProvider != null) {
						if (id > -1) {
							return DictionaryItem.getFrom(mDictDataProvider, id);
						}
						return DictionaryItem.getFrom(mDictDataProvider, word);
					}
					return null;
				}

				@Override
				public void onLoadFinished() {
					setDetailTitle();
					setIfFavorties();
				}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		mDictDataProvider = Constants.getDataProvider(this);
		
		FragmentManager manager = getSupportFragmentManager();
		Fragment fragment = manager.findFragmentById(R.id.detailContainer);
		if (fragment != null) {
			mDetailView = (DetailViewFragment)fragment;
			mDetailView.setDetailDataChangeListener(mDataChangeListener);
		}
		
		long id = getIntent().getLongExtra(Constants.DETAIL_ID_KEY, -1);
		//final DictionaryItem itemData = getIntent().getParcelableExtra(Constants.DETAIL_DATA_KEY);
		//if ((id >= 0) && (itemData != null)) {
		//	mDetailView.setData(itemData);
		//}
		if ((id >= 0) && (mDetailView != null)) {
			DictionaryItem itemData = DictionaryItem.getFrom(mDictDataProvider, id);
			mDetailView.setData(itemData);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_detail, menu);
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
			MenuItemCompat.setShowAsAction(mMenuItemSound, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			if (mDetailView != null) mMenuItemSound.setVisible(mDetailView.getHasSound());
		}
		
		mMenuItemPicture = menu.findItem(R.id.action_picture);
		if (mMenuItemPicture != null) {
			MenuItemCompat.setShowAsAction(mMenuItemPicture, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			if (mDetailView != null) mMenuItemPicture.setVisible(mDetailView.getHasPicture());
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				break;
			case R.id.action_favorite:
				performFavorite();
				break;
			case R.id.action_sound:
				doSpeak();
				break;
			case R.id.action_picture:
				toggleImageView();
				break;
			case R.id.actionbar_item_nav_back:
				performNavBack();
				break;
			case R.id.actionbar_item_nav_forward:
				performNavForward();
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		if ((mDetailView != null) && (mDetailView.getImageVisible())) {
			toggleImageView();
			return;
		}
		
		super.onBackPressed();
	}
	
	protected void setDetailTitle() {
		if (mDetailView == null) return;
		
		String title = mDetailView.getTitle();
		final ActionBar actionBar = getSupportActionBar();
		if ((actionBar != null) && (!TextUtils.isEmpty(title))) {
			actionBar.setTitle(title);
		}
	}
	
	protected void performNavBack() {
		if (mDetailView != null) {
			mDetailView.performNavBack();
		}
	}
	
	protected void performNavForward() {
		if (mDetailView != null) {
			mDetailView.performNavForward();
		}
	}
	
	protected void performFavorite() {
		if (mDetailView != null) {
			long id = mDetailView.getDetailId();
			if (id > -1) {
				if (!UserDataProvider.isFavorited(this, id)) {
					UserDataProvider.createFavorite(this, mDetailView.getTitle(), id);
					
					if (UserDataProvider.isFavorited(this, id)) {
						Toast.makeText(this, R.string.add_favorites_message, Toast.LENGTH_LONG).show();
						mMenuItemFavorite.setIcon(R.drawable.ic_action_star_on);
					}
				}
			}
		}
	}
	
	protected void setIfFavorties() {
		if ((mDetailView != null) && (mMenuItemFavorite != null)) {
			long id = mDetailView.getDetailId();
			if ((id > -1) && (UserDataProvider.isFavorited(this, id))) {
				mMenuItemFavorite.setIcon(R.drawable.ic_action_star_on);
			} else {
				mMenuItemFavorite.setIcon(R.drawable.ic_action_star);
			}
		}
	}
	
	protected void toggleImageView() {
		if (mDetailView != null) {
			mDetailView.toggleImageView();
		}
	}
	
	protected void doSpeak() {
		if (mDetailView != null) {
			mDetailView.doSpeak();
		}
	}

	@Override
	public void setActionBarContentView(ActionBarNavigationButtons view) {
		mActionBarContent = view;
		mActionBarContent.setTitleVisible(false);
		mActionBarContent.setNavigationVisible(true);
		if (mDetailView != null) {
			mActionBarContent.setNavBackEnabled(mDetailView.getCanGoBack());
			mActionBarContent.setNavForwardEnabled(mDetailView.getCanGoForward());
		}
	}
}
