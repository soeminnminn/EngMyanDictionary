package com.s16.engmyan.activity;

import java.io.File;

import com.s16.engmyan.Constants;
import com.s16.engmyan.InstallationTask;
import com.s16.engmyan.InstallationTask.InstallationHandler;
import com.s16.engmyan.data.DataProvider;
import com.s16.engmyan.fragment.DetailViewFragment;
import com.s16.engmyan.fragment.ProgressWheelFragment;
import com.s16.engmyan.fragment.SearchListFragment;
import com.s16.engmyan.R;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity 
		implements InstallationHandler, SearchListFragment.OnSearchListItemClickListener {

	protected static String TAG = MainActivity.class.getSimpleName();
	
	private ProgressWheelFragment mLoading;
	private DataProvider mDataProvider;
	private SearchListFragment mSearchList;
	private DetailViewFragment mDetailView;
	private MenuItem mMenuItemSound;
	private MenuItem mMenuItemPicture;
	private long mDetailId = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
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
		
		View detailView = findViewById(R.id.detailContainer);
		if (detailView != null) {
			FragmentTransaction transaction = manager.beginTransaction();
			mDetailView = new DetailViewFragment(this);
			transaction.replace(R.id.detailContainer, mDetailView);
			transaction.commit();
		}
	}
	
	@Override
    protected void onStart() {
        super.onStart();
    }
	
	@Override
    public void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onDestroy() {
    	performCleanAndSave();
        super.onDestroy();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mDetailView == null) {
			getMenuInflater().inflate(R.menu.menu_main, menu);
		} else {
			getMenuInflater().inflate(R.menu.menu_main_two_pane, menu);
		}	
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final MenuItem actionSettings = menu.findItem(R.id.action_settings);
		if (actionSettings != null) MenuItemCompat.setShowAsAction(actionSettings, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		
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
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				performSettings();
				break;
			case R.id.action_sound:
				doSpeak();
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
	public void onSearchListItemClick(long id, CharSequence searchText) {
		mDetailId = id;
		
		if (mDetailView == null) {
			
			Intent intent = new Intent(getBaseContext(), DetailActivity.class);
			intent.putExtra(Constants.DETAIL_ID_KEY, id);
			
			final File dbFile = mDataProvider.getDatabaseFile();
			intent.putExtra(Constants.DATABASE_FILE_KEY, dbFile.getPath());
			
			intent.putExtra(Constants.SEARCH_TEXT_KEY, searchText);
			ActivityCompat.startActivity(this, intent, null);
		
		} else {
			setDetailData(mDetailId);
		}
	}

	protected void performSettings() {
		Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
		ActivityCompat.startActivity(this, intent, null);
	}
	
	protected void performCleanAndSave() {
		saveState();
    	
    	if (mDataProvider != null) {
			mDataProvider.close();
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
	}
	
	protected synchronized void performInstall() {
		
		File dbFile = Constants.getDatabase(this);
		if(dbFile == null) {
			installError("Does not create data folder!");
			return;
		}
		
		if((dbFile.exists()) 
			&& (!DataProvider.versionCheck(this, dbFile, Constants.DATA_VERSION)) 
			&& (!dbFile.delete())) {
			
			installError("Does not load data!");
			return;
		}
		
		if(!dbFile.exists()) {
			final File dataFolder = dbFile.getParentFile();
			InstallationTask task = new InstallationTask(this, dataFolder, this);
			task.execute(Constants.ASSERT_ZIP_PKG);
		} else {
			openDatabase();
		}
	}
	
	protected void openDatabase() {
		
		final File dbFile = Constants.getDatabase(this);
		if (mDataProvider == null) {
			mDataProvider = new DataProvider(this, dbFile);
		}
		
		try {
			if(!mDataProvider.isOpen()) 
				mDataProvider.open();
		} catch(SQLException ex) {
			ex.printStackTrace();
		}
		
		prepareSearch();
	}
	
	protected void prepareSearch() {
		if (mSearchList != null) {
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String constraint = prefs.getString(Constants.SEARCH_TEXT_KEY, "");
			mDetailId = prefs.getLong(Constants.DETAIL_ID_KEY, -1);
			
			if (constraint != null) {
				mSearchList.setSearchText(constraint);
				mSearchList.setSelection();
			}
			mSearchList.setDataProvider(mDataProvider);
			mSearchList.prepareSearch();
			
			setDetailData(mDetailId);
		}
	}
	
	protected void setDetailData(long id) {
		if (mDetailView == null) return;
		
		mDetailView.setData(mDataProvider, id);
		String title = mDetailView.getTitle();
		
		final ActionBar actionBar = getSupportActionBar();
		if ((actionBar != null) && (!TextUtils.isEmpty(title))) {
			actionBar.setTitle(getString(R.string.app_name) + " [ " + title + " ]");
		}
		
		if (mMenuItemPicture != null) {
			mMenuItemPicture.setVisible(mDetailView.getHasPicture());
		}
		
		if (mMenuItemSound != null) {
			mMenuItemSound.setVisible(mDetailView.getHasSound());
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
		
		editor.putLong(Constants.DETAIL_ID_KEY, mDetailId);
		
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
}
