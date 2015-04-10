package com.s16.engmyan.activity;

import com.s16.engmyan.R;
import com.s16.engmyan.fragment.SettingsFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.SystemUiUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

public class SettingsActivity extends ActionBarActivity {

	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		// get the action bar
		final ActionBar actionBar = getSupportActionBar();

		// Enabling Back navigation on Action Bar icon
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.action_settings);
		
		SystemUiUtils.setStatusBarColor(this, getResources().getColor(R.color.title_background_dark));
		
		FragmentManager manager = getSupportFragmentManager();
		if (manager != null) {
			FragmentTransaction transaction = manager.beginTransaction();
			
			SettingsFragment fragment = new SettingsFragment(this); 
			transaction.replace(R.id.settings_content, fragment);
			transaction.commit();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
