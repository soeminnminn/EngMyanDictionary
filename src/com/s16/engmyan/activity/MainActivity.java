package com.s16.engmyan.activity;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.SystemUiUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.s16.app.AboutPreference;
import com.s16.engmyan.Common;
import com.s16.engmyan.Constants;
import com.s16.engmyan.R;
import com.s16.engmyan.data.DictionaryDataProvider;
import com.s16.engmyan.data.DictionaryItem;
import com.s16.engmyan.data.SearchQueryHelper;
import com.s16.engmyan.data.UserQueryHelper;
import com.s16.engmyan.fragment.DetailsFragment;
import com.s16.engmyan.fragment.FavoritesFragment;
import com.s16.engmyan.fragment.LoadingFragment;
import com.s16.engmyan.fragment.MainListFragment;
import com.s16.engmyan.fragment.RecentsFragment;
import com.s16.engmyan.install.InstallationService;
import com.s16.widget.ActionBarNavigationButtons;
import com.s16.widget.SearchBarView;

public class MainActivity extends AppCompatActivity 
		implements MainListFragment.OnListItemClickListener  {
	
	protected static final String TAG = MainActivity.class.getSimpleName();
	
	private class InstallBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int dataStatus = intent.getIntExtra(InstallationService.EXTENDED_DATA_STATUS, -1);
			if (dataStatus == InstallationService.STATE_ACTION_STARTED) {
				showLoading();
			} else if (dataStatus == InstallationService.STATE_ACTION_COMPLETE) {
				hideLoading();
				Common.showMessage(getContext(), R.string.install_complete_message);
				openDatabase(Common.getDatabaseFile(getContext()));
			} else {
				Common.showMessage(getContext(), R.string.install_error_message);
				System.exit(0);
			}
		}
		
	}
	
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
	
	private final FavoritesFragment.OnFavoritesListItemClickListener mOnFavoritesListItemClickListener =
			new FavoritesFragment.OnFavoritesListItemClickListener() {

			@Override
			public void onFavoritesListItemClick(DialogInterface dialog, View view, long id, long refId) {
				if (!isTwoPanes()) {
					dialog.dismiss();
				}
				onListItemClick(refId, null);
			}
		
	};
	
	private final RecentsFragment.OnRecentsListItemClickListener mOnRecentsListItemClickListener =
			new RecentsFragment.OnRecentsListItemClickListener() {

			@Override
			public void onRecentsListItemClick(DialogInterface dialog, View view, long id, long refId) {
				if (!isTwoPanes()) {
					dialog.dismiss();
				}
				onListItemClick(refId, null);
			}
		
	};
	
	private InstallBroadcastReceiver mInstallBroadcastReceiver;
	
	private SearchBarView mTextSearch;
	private LoadingFragment mLoadingDialog;
	private MainListFragment mListFragment;
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
		setContentView(R.layout.activity_main);
		
		SystemUiUtils.setStatusBarColor(this, getResources().getColor(R.color.app_color_primary_dark));
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		
		mTextSearch = (SearchBarView)findViewById(R.id.searchBarView);
		mTextSearch.setOnQueryTextListener(new SearchBarView.OnQueryTextListener() {
			
			@Override
			public void onQueryTextChanged(CharSequence query, int count) {
				performSearch(query);
			}
			
			@Override
			public boolean onQuerySubmit(CharSequence query) {
				return submitSearch(query);
			}
		});
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String constraint = prefs.getString(Constants.SEARCH_TEXT_KEY, null);
		
		mTextSearch.setText(constraint);
		mTextSearch.requestFocus();
		
		FragmentManager manager = getSupportFragmentManager();
		mListFragment = (MainListFragment)manager.findFragmentById(R.id.listContainer);
		mListFragment.setOnListItemClickListener(this);
		
		initializeDetails(toolbar);
		performInstall();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
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
			if (mDetailsFragment != null) 
				mMenuItemSound.setVisible(mDetailsFragment.getHasSound());
			else
				mMenuItemSound.setVisible(false);
		}
		
		mMenuItemPicture = menu.findItem(R.id.action_picture);
		if (mMenuItemPicture != null) {
			MenuItemCompat.setShowAsAction(mMenuItemPicture, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			if (mDetailsFragment != null) 
				mMenuItemPicture.setVisible(mDetailsFragment.getHasPicture());
			else
				mMenuItemPicture.setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.action_favorite:
				performFavorite();
				break;
			case R.id.action_sound:
				performSpeak();
				break;
			case R.id.action_picture:
				toggleImageView();
				break;
			case R.id.action_recent:
				performManageRecents();
				break;
			case R.id.action_manage_favorites:
				performManageFavorites();
				break;
			case R.id.action_settings:
				performSettings();
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
    protected void onPause() {
        super.onPause();
    }
	
	@Override
    protected void onResume() {
        super.onResume();
        if (Common.isServiceRunning(getContext(), InstallationService.class)) {
        	registerInstallReceiver();
        }
    }
	
	@Override
    protected void onDestroy() {
    	performCleanAndSave();
        super.onDestroy();
    }
	
	@Override
	public void onBackPressed() {
		if ((mDetailsFragment != null) && (mDetailsFragment.getImageVisible())) {
			toggleImageView();
			return;
		}
		
		super.onBackPressed();
	}
	
	private void initializeDetails(Toolbar toolbar) {
		
		FragmentManager manager = getSupportFragmentManager();
		Fragment fragment = manager.findFragmentById(R.id.detailsContainer);
		if (fragment != null) {
			mDetailsFragment = (DetailsFragment)fragment;
			getSupportActionBar().setDisplayShowTitleEnabled(false);
			
			mActionBarContent = (ActionBarNavigationButtons)toolbar.findViewById(R.id.frameToolbarContent);
			if (mActionBarContent != null) {
				mActionBarContent.setNavigationVisible(true);
				mActionBarContent.setNavBackEnabled(mDetailsFragment.getCanGoBack());
				mActionBarContent.setNavForwardEnabled(mDetailsFragment.getCanGoForward());
				mActionBarContent.setNavigationClickListener(mNavigationClickListener);
			}
			
			mDetailsFragment.setDetailsDataChangeListener(mDataChangeListener);
		}
	}

	@Override
	public void onListItemClick(long id, CharSequence searchText) {
		if (id < 0) return;

		DictionaryItem itemData = DictionaryItem.getFrom(SearchQueryHelper.getInstance(getContext()), id);
		UserQueryHelper.getInstance(getContext()).createHistory(itemData.word, id);
		
		if (mDetailsFragment == null) {
			Intent intent = new Intent(getBaseContext(), DetailsActivity.class);
			intent.putExtra(Constants.DETAIL_ID_KEY, id);
			ActivityCompat.startActivity(this, intent, null);
			
		} else {
			setDetailsData(itemData);
		}
	}
	
	private boolean isTwoPanes() {
		return (mDetailsFragment != null);
	}
	
	private void setDetailsData(DictionaryItem itemData) {
		if (!isTwoPanes()) return;
		if (mDetailsFragment == null) return;
		if (itemData == null) return;
		
		mDetailsFragment.setData(itemData);
		setDetailsTitle();
		
		if (mMenuItemPicture != null) {
			mMenuItemPicture.setVisible(mDetailsFragment.getHasPicture());
		}
		
		if (mMenuItemSound != null) {
			mMenuItemSound.setVisible(mDetailsFragment.getHasSound());
		}
		
		if (mActionBarContent != null) {
			mActionBarContent.setNavBackEnabled(mDetailsFragment.getCanGoBack());
			mActionBarContent.setNavForwardEnabled(mDetailsFragment.getCanGoForward());
		}
		
		setIfFavorties();
	}
	
	private void setDetailsTitle() {
		if (!isTwoPanes()) return;
		if (mDetailsFragment == null) return;
		
		if (mActionBarContent != null) {
			String detailTitle = mDetailsFragment.getTitle();
			String title = getString(R.string.app_name) + " [ " + detailTitle + " ]";
			mActionBarContent.setTitle(title);
		}
	}
	
	private void showLoading() {
		FragmentManager manager = getSupportFragmentManager();
		if (mLoadingDialog == null) {
			mLoadingDialog = new LoadingFragment();
			mLoadingDialog.setMessage(R.string.install_message);
			mLoadingDialog.setCancelable(false);
			mLoadingDialog.show(manager, "loadingDialog");
		}
	}
	
	private void hideLoading() {
		if(mLoadingDialog != null) {
			mLoadingDialog.dismiss();
			mLoadingDialog = null;
		}	
	}
	
	private void performSearch(CharSequence query) {
		if (mListFragment != null) {
			mListFragment.performSearch(query);
		}
	}
	
	private boolean submitSearch(CharSequence query) {
		if (mListFragment != null) {
			return mListFragment.submitSearch(query);
		}
		return false;
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
	
	private void performSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	private void performAbout() {
		AboutPreference.showAboutDialog(getContext());
	}
	
	private void performExit() {
		finish();
		System.exit(0);
	}
	
	private void performManageRecents() {
		RecentsFragment fragment = new RecentsFragment();
		fragment.setOnRecentsListItemClickListener(mOnRecentsListItemClickListener);
		fragment.show(getSupportFragmentManager(), "RecentsFragment");
	}
	
	private void performManageFavorites() {
		FavoritesFragment fragment = new FavoritesFragment();
		fragment.setOnFavoritesListItemClickListener(mOnFavoritesListItemClickListener);
		fragment.show(getSupportFragmentManager(), "FavoritesFragment");
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
	
	private void performCleanAndSave() {
		saveState();
    	
		if (mInstallBroadcastReceiver != null) {
			LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mInstallBroadcastReceiver);
			mInstallBroadcastReceiver = null;
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
	
	private synchronized void performInstall() {
		File dbFile = Common.getDatabaseFile(getContext());
		if(dbFile == null) {
			Common.showMessage(getContext(), R.string.install_error_folder_create);
			return;
		}
		
		boolean isSuccess = dbFile.exists();
		//isSuccess = (isSuccess && dbFile.length() > Constants.DATABASE_FILE_MIN_LENGTH);
		//isSuccess = (isSuccess && Checksum.checkMD5(Constants.DATABASE_FILE_MD5, dbFile));
		isSuccess = (isSuccess && DictionaryDataProvider.versionCheck(this, dbFile));
		
		if(!isSuccess && dbFile.exists() && !dbFile.delete()) {
			Common.showMessage(getContext(), R.string.install_error_data_load);
			return;
		}
		
		if(!isSuccess || !dbFile.exists()) {
			final File dataFolder = dbFile.getParentFile();
			registerInstallReceiver();
			
			Bundle args = new Bundle();
			args.putString(InstallationService.INSTALL_ASSETS_NAME, Constants.ASSERT_ZIP_PKG);
			args.putString(InstallationService.INSTALL_FOLDER, dataFolder.getAbsolutePath());
			
			Intent serviceIntent = new Intent(getContext(), InstallationService.class);
			serviceIntent.putExtras(args);
			startService(serviceIntent);
			
		} else {
			openDatabase(dbFile);
		}
	}
	
	private void registerInstallReceiver() {
		if (mInstallBroadcastReceiver == null) {
			mInstallBroadcastReceiver = new InstallBroadcastReceiver();
			IntentFilter filter = new IntentFilter(InstallationService.BROADCAST_ACTION);
			filter.addCategory(Intent.CATEGORY_DEFAULT);
			LocalBroadcastManager.getInstance(getContext()).registerReceiver(mInstallBroadcastReceiver, filter);
		}
	}
	
	private void openDatabase(File dbFile) {
		if (dbFile == null) return;
		getContentResolver().call(DictionaryDataProvider.CONTENT_URI, DictionaryDataProvider.METHOD_OPEN, 
				dbFile.getAbsolutePath(), Bundle.EMPTY);
		
		if (mListFragment != null) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String constraint = prefs.getString(Constants.SEARCH_TEXT_KEY, "");
			long id = prefs.getLong(Constants.DETAIL_ID_KEY, -1);
			
			mListFragment.prepareSearch(constraint);
			
			if (id > 0) {
				DictionaryItem itemData = DictionaryItem.getFrom(SearchQueryHelper.getInstance(getContext()), id);
				setDetailsData(itemData);
			}
		}
	}
	
	private void saveState() {
		if (mTextSearch == null) return;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		
		CharSequence constraint = mTextSearch.getText();
		if (constraint == null) {
			editor.putString(Constants.SEARCH_TEXT_KEY, null);
		} else {
			editor.putString(Constants.SEARCH_TEXT_KEY, constraint.toString());
		}
		
		if (mDetailsFragment != null) {
			long id = mDetailsFragment.getDetailId();
			editor.putLong(Constants.DETAIL_ID_KEY, id);
		} else {
			editor.putLong(Constants.DETAIL_ID_KEY, -1);
		}
		
		editor.commit();
	}
}
