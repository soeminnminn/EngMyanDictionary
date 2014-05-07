package com.s16.engmyan.activity;

import com.s16.engmyan.Constants;
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

public class DetailActivity extends ActionBarActivity {
	
	protected static String TAG = DetailActivity.class.getSimpleName();
	
	private DetailViewFragment mDetailView;
	private MenuItem mMenuItemFavorite;
	private MenuItem mMenuItemSound;
	private MenuItem mMenuItemPicture;
	private long mDetailId = -1;
	
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
		
		mDetailId = getIntent().getLongExtra(Constants.DETAIL_ID_KEY, -1);
		final DictionaryItem itemData = getIntent().getParcelableExtra(Constants.DETAIL_DATA_KEY);
		if ((mDetailId >= 0) && (itemData != null)) {
			initialize(actionBar, itemData);
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
	
	private void initialize(ActionBar actionBar, DictionaryItem itemData) {
		if ((itemData == null) || (itemData.id < 0)) return;
		if (mDetailView == null) return;
		
		mDetailView.setData(itemData);
		String title = mDetailView.getTitle();
		if ((actionBar != null) && (!TextUtils.isEmpty(title))) {
			actionBar.setTitle(title);
		}
	}
	
	protected void performFavorite() {
		if ((mDetailId > -1) && (mDetailView != null)) {
			if (!UserDataProvider.isFavorited(this, mDetailId)) {
				UserDataProvider.createFavorite(this, mDetailView.getTitle(), mDetailId);
				
				if (UserDataProvider.isFavorited(this, mDetailId)) {
					Toast.makeText(this, R.string.add_favorites_message, Toast.LENGTH_LONG).show();
					mMenuItemFavorite.setIcon(R.drawable.ic_action_star_on);
				}
			}
		}
	}
	
	protected void setIfFavorties() {
		if ((mDetailId > -1) && (mMenuItemFavorite != null)) {
			if (UserDataProvider.isFavorited(this, mDetailId)) {
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
}
