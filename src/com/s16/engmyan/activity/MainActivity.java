package com.s16.engmyan.activity;

import java.io.File;

import com.s16.engmyan.Constants;
import com.s16.engmyan.InstallationTask;
import com.s16.engmyan.Utility;
import com.s16.engmyan.InstallationTask.InstallationHandler;
import com.s16.engmyan.data.DictionaryDataProvider;
import com.s16.engmyan.data.DictionaryItem;
import com.s16.engmyan.data.UserDataProvider;
import com.s16.engmyan.fragment.FavoritesFragment;
import com.s16.engmyan.fragment.DetailViewFragment;
import com.s16.engmyan.fragment.ProgressWheelFragment;
import com.s16.engmyan.fragment.RecentsFragment;
import com.s16.engmyan.fragment.SearchListFragment;
import com.s16.engmyan.R;
import com.s16.widget.ActionBarNavigationButtons;
import com.s16.widget.MoreMenuActionProvider;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.SystemUiUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity 
		implements InstallationHandler
		, SearchListFragment.OnSearchListItemClickListener
		, ActionBarNavigationButtons.ActionBarContentViewActivity {

	protected static String TAG = MainActivity.class.getSimpleName();
	
	private DictionaryDataProvider mDictDataProvider;
	
	private ProgressWheelFragment mLoading;
	private SearchListFragment mSearchList;
	private DetailViewFragment mDetailView;
	private FavoritesFragment mFavoritesView;
	private RecentsFragment mRecentsView;
	
	private Menu mMenu;
	private MoreMenuActionProvider mMoreMenu;
	private MenuItem mMenuItemFavorite;
	private MenuItem mMenuItemSound;
	private MenuItem mMenuItemPicture;
	private boolean mIsMenuEnabled = true;
	
	private ActionBarNavigationButtons mActionBarContent; 
	
	private final FavoritesFragment.OnVisibilityChangeListener mOnFavoritesVisibilityChangeListener = 
			new FavoritesFragment.OnVisibilityChangeListener() {

				@Override
				public void onVisibilityChanged(int visible) {
					setViewEnabled(visible == View.GONE);
				}
	};
	
	private final FavoritesFragment.OnFavoritesListItemClickListener mOnFavoritesListItemClickListener =
		new FavoritesFragment.OnFavoritesListItemClickListener() {

			@Override
			public void onFavoritesListItemClick(View view, long id, long refId) {
				onSearchListItemClick(refId, null);
			}
		
	};
	
	private final RecentsFragment.OnVisibilityChangeListener mOnRecentsVisibilityChangeListener = 
			new RecentsFragment.OnVisibilityChangeListener() {

				@Override
				public void onVisibilityChanged(int visible) {
					setViewEnabled(visible == View.GONE);
				}
	};
	
	private final RecentsFragment.OnRecentsListItemClickListener mOnRecentsListItemClickListener =
		new RecentsFragment.OnRecentsListItemClickListener() {

			@Override
			public void onRecentsListItemClick(View view, long id, long refId) {
				onSearchListItemClick(refId, null);
			}
		
	};
	
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
	
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		SystemUiUtils.setStatusBarColor(this, getResources().getColor(R.color.title_background_dark));
		
		initialize();
		performInstall();
	}
	
	private void initialize() {
		FragmentManager manager = getSupportFragmentManager();
		Fragment listFragment = manager.findFragmentById(R.id.listContainer);
		if (listFragment != null) {
			mSearchList = (SearchListFragment)listFragment;
			mSearchList.setOnSearchListItemClickListener(this);
		}
		
		Fragment favoritesFragment = manager.findFragmentById(R.id.favoritesContainer);
		if (favoritesFragment != null) {
			mFavoritesView = (FavoritesFragment)favoritesFragment;
			mFavoritesView.setVisibility(View.GONE);
			mFavoritesView.setOnVisibilityChangeListener(mOnFavoritesVisibilityChangeListener);
			mFavoritesView.setOnFavoritesListItemClickListener(mOnFavoritesListItemClickListener);
		}
		
		Fragment recentsFragment = manager.findFragmentById(R.id.recentsContainer);
		if (recentsFragment != null) {
			mRecentsView = (RecentsFragment)recentsFragment;
			mRecentsView.setVisibility(View.GONE);
			mRecentsView.setOnVisibilityChangeListener(mOnRecentsVisibilityChangeListener);
			mRecentsView.setOnRecentsListItemClickListener(mOnRecentsListItemClickListener);
		}
		
		View detailView = findViewById(R.id.detailContainer);
		if (detailView != null) {
			FragmentTransaction transaction = manager.beginTransaction();
			mDetailView = new DetailViewFragment(this);
			transaction.replace(R.id.detailContainer, mDetailView);
			transaction.commit();
			
			ViewGroup content = (ViewGroup)findViewById(R.id.mainContent);
			mActionBarContent = (ActionBarNavigationButtons)content.findViewById(R.id.detailActionBar);
			if (mActionBarContent != null) {
				content.removeView(mActionBarContent);
				
				final ActionBar actionBar = getSupportActionBar();
				actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
				actionBar.setCustomView(mActionBarContent);
				
				mActionBarContent.setVisibility(View.VISIBLE);
				mActionBarContent.setNavigationVisible(true);
			}
			
			mDetailView.setDetailDataChangeListener(mDataChangeListener);
		}
	}
	
	@Override
    protected void onStart() {
        super.onStart();
    }
	
	@Override
    public void onStop() {
        super.onStop();
    }
    
    @Override
    protected void onDestroy() {
    	performCleanAndSave();
        super.onDestroy();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mMenu == null) {
			mMenu = menu;
		}
		
		if (mDetailView == null) {
			getMenuInflater().inflate(R.menu.menu_main, menu);
		} else {
			getMenuInflater().inflate(R.menu.menu_main_two_pane, menu);
		}
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		//final MenuItem actionSettings = menu.findItem(R.id.action_settings);
		//if (actionSettings != null) MenuItemCompat.setShowAsAction(actionSettings, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		
		mMenuItemFavorite = menu.findItem(R.id.action_favorite);
		if (mMenuItemFavorite != null) {
			MenuItemCompat.setShowAsAction(mMenuItemFavorite, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			setIfFavorties();
			mMenuItemFavorite.setEnabled(mIsMenuEnabled);
		}
		
		mMenuItemSound = menu.findItem(R.id.action_sound);
		if (mMenuItemSound != null) {
			MenuItemCompat.setShowAsAction(mMenuItemSound, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			if (mDetailView != null) 
				mMenuItemSound.setVisible(mDetailView.getHasSound());
			else
				mMenuItemSound.setVisible(false);
		}
		
		mMenuItemPicture = menu.findItem(R.id.action_picture);
		if (mMenuItemPicture != null) {
			MenuItemCompat.setShowAsAction(mMenuItemPicture, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			if (mDetailView != null) 
				mMenuItemPicture.setVisible(mDetailView.getHasPicture());
			else
				mMenuItemPicture.setVisible(false);
		}
		
		if (android.os.Build.VERSION.SDK_INT < 21) {
			final MenuItem actionMoreoverflow = menu.findItem(R.id.action_moreoverflow);
			if (actionMoreoverflow != null) {
				MenuItemCompat.setShowAsAction(actionMoreoverflow, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
				mMoreMenu = new MoreMenuActionProvider(this, menu, actionMoreoverflow, R.menu.menu_overflow_light);
				mMoreMenu.setEnabled(mIsMenuEnabled);
				MenuItemCompat.setActionProvider(actionMoreoverflow, mMoreMenu);
				actionMoreoverflow.setEnabled(mIsMenuEnabled);
			}
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (!mIsMenuEnabled) {
			return super.onOptionsItemSelected(item);
		}
		
		switch (item.getItemId()) {
			case R.id.action_favorite:
				performFavorite();
				break;
			case R.id.action_settings:
				performSettings();
				break;
			case R.id.action_sound:
				doSpeak();
				break;
			case R.id.action_picture:
				toggleImageView();
				break;
			case R.id.action_recent:
				performRecents();
				break;
			case R.id.action_manage_favorites:
				performManageFavorites();
				break;
			case R.id.actionbar_item_nav_back:
				performNavBack();
				break;
			case R.id.actionbar_item_nav_forward:
				performNavForward();
				break;
			case R.id.action_about:
				performAbout();
				break;
			case R.id.action_exit:
				performExit();
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if ((mRecentsView != null) && (mRecentsView.isVisible())) {
			mRecentsView.hide();
			return;
		}
		
		if ((mFavoritesView != null) && (mFavoritesView.isVisible())) {
			if (!mFavoritesView.performBackPress()) {
				mFavoritesView.hide();
			}
			return;
		}
		
		if ((mDetailView != null) && (mDetailView.getImageVisible())) {
			toggleImageView();
			return;
		}
		
		super.onBackPressed();
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (!mIsMenuEnabled) {
				return true;
			}
			
			if (mMoreMenu != null) {
				mMoreMenu.performClick();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	public void onSearchListItemClick(long id, CharSequence searchText) {
		if (id < 0) return;
		if (mDictDataProvider == null) return;
		
		final DictionaryItem itemData = DictionaryItem.getFrom(mDictDataProvider, id);
		if (itemData != null) {
			UserDataProvider.createHistory(this, itemData.word, id);
			
			if (mDetailView == null) {
				
				Intent intent = new Intent(getBaseContext(), DetailActivity.class);
				intent.putExtra(Constants.DETAIL_ID_KEY, id);
				//intent.putExtra(Constants.DETAIL_DATA_KEY, itemData);
				
				ActivityCompat.startActivity(this, intent, null);
			} else {
				setDetailData(itemData);
			}
		}
	}
	
	protected void setViewEnabled(boolean enabled) {
		mIsMenuEnabled = enabled;
		if (mSearchList != null) {
			if (mSearchList.getSearchView() != null) {
				if (!enabled) {
					SystemUiUtils.hideSoftKeyboard(this, mSearchList.getSearchView());
				}
				mSearchList.getSearchView().setEnabled(enabled);
			}
			mSearchList.setEnabled(enabled);
		}
		if ((!enabled) && isActionBarHideOnView()) {
			getSupportActionBar().show();
		}
		/*
		if (mMenu != null) {
			int menuCount = mMenu.size();
			for(int i=0; i<menuCount;i++) {
				mMenu.getItem(i).setVisible(enabled);
			}
		} else {
			if (mMenuItemFavorite != null) {
				mMenuItemFavorite.setEnabled(enabled);
			}
		}
		if (mMoreMenu != null) {
			mMoreMenu.setEnabled(enabled);
		}
		
		if (mActionBarContent != null) {
			mActionBarContent.setEnabled(enabled);
		} */
	}
	
	protected boolean isActionBarHideOnView() {
		return (Utility.getConfigScreenSize(this) == 1);
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

	protected void performSettings() {
		Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
		ActivityCompat.startActivity(this, intent, null);
	}
	
	protected void performRecents() {
		if ((mFavoritesView != null) 
				&& (mFavoritesView.getVisibility() == View.VISIBLE)) return; 
		
		if (mRecentsView != null) {
			if (isActionBarHideOnView()) {
				getSupportActionBar().hide();
			}
			mRecentsView.show();
		}
	}
	
	protected void performManageFavorites() {
		if ((mRecentsView != null) 
				&& (mRecentsView.getVisibility() == View.VISIBLE)) return;
		
		if (mFavoritesView != null) {
			if (isActionBarHideOnView()) {
				getSupportActionBar().hide();
			}
			mFavoritesView.show();
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
	
	protected void performAbout() {
		Utility.showAboutDialog(this);
	}
	
	protected void performExit() {
		finish();
		System.exit(0);
	}
	
	protected void performCleanAndSave() {
		saveState();
    	
    	if (mDictDataProvider != null) {
			mDictDataProvider.close();
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
	
	protected void showLoading() {
		mLoading = new ProgressWheelFragment(this);
		FragmentManager manager = getSupportFragmentManager();
		mLoading.show(manager, "loadingDialog");
	}
	
	protected void hideLoading() {
		if(mLoading != null) {
			mLoading.dismiss();
		}
		setViewEnabled(true);
	}
	
	protected synchronized void performInstall() {
		File dbFile = Constants.getDatabaseFile(this);
		if(dbFile == null) {
			installError(getString(R.string.install_error_folder_create));
			return;
		}
		
		boolean isSuccess = dbFile.exists();
		//isSuccess = (isSuccess && dbFile.length() > Constants.DATABASE_FILE_MIN_LENGTH);
		//isSuccess = (isSuccess && Checksum.checkMD5(Constants.DATABASE_FILE_MD5, dbFile));
		isSuccess = (isSuccess && DictionaryDataProvider.versionCheck(this, dbFile));
		
		if(!isSuccess && dbFile.exists() && !dbFile.delete()) {
			installError(getString(R.string.install_error_data_load));
			return;
		}
		
		if(!isSuccess || !dbFile.exists()) {
			final File dataFolder = dbFile.getParentFile();
			InstallationTask task = new InstallationTask(this, dataFolder, this);
			task.execute(Constants.ASSERT_ZIP_PKG);
		} else {
			openDatabase();
		}
	}
	
	protected void openDatabase() {
		
		if (mDictDataProvider == null) {
			mDictDataProvider = Constants.getDataProvider(this);
		}
		
		try {
			if(!mDictDataProvider.isOpen()) 
				mDictDataProvider.open();
		} catch(SQLException ex) {
			ex.printStackTrace();
		}
		
		prepareSearch();
	}
	
	protected void prepareSearch() {
		if (mSearchList != null) {
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String constraint = prefs.getString(Constants.SEARCH_TEXT_KEY, "");
			long id = prefs.getLong(Constants.DETAIL_ID_KEY, -1);
			
			if (constraint != null) {
				mSearchList.setSearchText(constraint);
				mSearchList.setSelection();
			}
			mSearchList.setDataProvider(mDictDataProvider);
			mSearchList.prepareSearch();
			
			if (id >= 0) {
				if ((mDictDataProvider != null) && (mDictDataProvider.isOpen())) {
					setDetailData(DictionaryItem.getFrom(mDictDataProvider, id));
				}
			}
		}
	}
	
	protected void setDetailData(DictionaryItem itemData) {
		if (mDetailView == null) return;
		if (itemData == null) return;
		
		mDetailView.setData(itemData);
		setDetailTitle();
		
		if (mMenuItemPicture != null) {
			mMenuItemPicture.setVisible(mDetailView.getHasPicture());
		}
		
		if (mMenuItemSound != null) {
			mMenuItemSound.setVisible(mDetailView.getHasSound());
		}
		
		if (mActionBarContent != null) {
			mActionBarContent.setNavBackEnabled(mDetailView.getCanGoBack());
			mActionBarContent.setNavForwardEnabled(mDetailView.getCanGoForward());
		}
		
		setIfFavorties();
	}
	
	protected void setDetailTitle() {
		if (mDetailView == null) return;
		
		String detailTitle = mDetailView.getTitle();
		final ActionBar actionBar = getSupportActionBar();
		if ((actionBar != null) && (!TextUtils.isEmpty(detailTitle))) {
			String title = getString(R.string.app_name) + " [ " + detailTitle + " ]";
			actionBar.setTitle(title);
			if (mActionBarContent != null) {
				mActionBarContent.setTitle(title);
			}
		}
	}
	
	protected void saveState() {
		if (mSearchList == null) return;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		
		CharSequence constraint = mSearchList.getSearchText();
		if (constraint == null) {
			editor.putString(Constants.SEARCH_TEXT_KEY, null);
		} else {
			editor.putString(Constants.SEARCH_TEXT_KEY, constraint.toString());
		}
		
		if (mDetailView != null) {
			long id = mDetailView.getDetailId();
			editor.putLong(Constants.DETAIL_ID_KEY, id);
		} else {
			editor.putLong(Constants.DETAIL_ID_KEY, -1);
		}
		
		editor.commit();
	}

	@Override
	public void preInstall() {
		showLoading();
	}
	
	@Override
	public void postInstall() {
		hideLoading();
		Toast.makeText(this, getText(R.string.install_complete_message), Toast.LENGTH_LONG).show();
		openDatabase();
	}

	@Override
	public void installError(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		System.exit(0);
	}

	@Override
	public void setActionBarContentView(ActionBarNavigationButtons view) {
		mActionBarContent = view;
		mActionBarContent.setTitleVisible(true);
		if (mDetailView != null) {
			mActionBarContent.setNavBackEnabled(mDetailView.getCanGoBack());
			mActionBarContent.setNavForwardEnabled(mDetailView.getCanGoForward());
		}
	}
}
