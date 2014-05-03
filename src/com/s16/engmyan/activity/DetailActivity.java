package com.s16.engmyan.activity;

import java.io.File;

import com.s16.engmyan.Constants;
import com.s16.engmyan.data.DataProvider;
import com.s16.engmyan.fragment.DetailViewFragment;
import com.s16.engmyan.R;

import android.database.SQLException;
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

public class DetailActivity extends ActionBarActivity {
	
	protected static String TAG = DetailActivity.class.getSimpleName();
	
	private DetailViewFragment mDetailView;
	private MenuItem mMenuItemSound;
	private MenuItem mMenuItemPicture;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		FragmentManager manager = getSupportFragmentManager();
		Fragment fragment = manager.findFragmentById(R.id.detailContainer);
		if (fragment != null) {
			mDetailView = (DetailViewFragment)fragment;
		}
		
		Bundle extras = getIntent().getExtras();
		long id = extras.getLong(Constants.DETAIL_ID_KEY);
		CharSequence dbFilePath = extras.getCharSequence(Constants.DATABASE_FILE_KEY);
		if(id > -1) {
			initialize(actionBar, dbFilePath, id);			
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_detail, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
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
	public void onBackPressed() {
		if ((mDetailView != null) && (mDetailView.getImageVisible())) {
			toggleImageView();
			return;
		}
		
		super.onBackPressed();
	}
	
	private void initialize(ActionBar actionBar, CharSequence dbFilePath, long id) {
		if ((dbFilePath == null) || (dbFilePath == "")) return;
		if (mDetailView == null) return;
		
		File dbFile = new File(dbFilePath.toString());
		if (!dbFile.exists()) return;
		
		DataProvider dataProvider = new DataProvider(getBaseContext(), dbFile);
		try {
			if(!dataProvider.isOpen()) 
				dataProvider.open();
		} catch(SQLException ex) {
			ex.printStackTrace();
		}
		
		mDetailView.setData(dataProvider, id);
		String title = mDetailView.getTitle();
		if ((actionBar != null) && (!TextUtils.isEmpty(title))) {
			actionBar.setTitle(title);
		}
		
		dataProvider.close();
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();    
    }
}
