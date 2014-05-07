package com.s16.engmyan.fragment;

import com.s16.engmyan.Constants;
import com.s16.engmyan.R;
import com.s16.engmyan.Utility;
import com.s16.engmyan.data.UserDataProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragment {
	
	private Context mContext;
	
	public SettingsFragment(Context context) {
		mContext = context;
	}
	
	protected Context getContext() {
		return mContext;
	}
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
        
        Preference prefsClearRecents = findPreference(Constants.PREFS_CLEAR_RECENT);
        prefsClearRecents.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        	@Override
			public boolean onPreferenceClick(Preference preference) {
        		performClearRecent();
				return false;
			}
        });
        
        Preference prefsCredit = findPreference(Constants.PREFS_CREDIT);
        prefsCredit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        	@Override
			public boolean onPreferenceClick(Preference preference) {
        		showCreditDialog();
				return false;
			}
        });
        
        Preference prefsAbout = findPreference(Constants.PREFS_ABOUT);
        try {
        	PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			prefsAbout.setSummary(pInfo.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
        
        prefsAbout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Utility.showAboutDialog(mContext);
				return false;
			}
        });
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if (mContext == null) {
			mContext = inflater.getContext();
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	protected void performClearRecent() {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.DialogTheme));
		dialogBuilder.setIcon(android.R.drawable.ic_dialog_info);
		dialogBuilder.setTitle(R.string.clear_recent_title);
		dialogBuilder.setMessage(R.string.clear_recent_message);
		
		dialogBuilder.setNegativeButton(getText(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialogBuilder.setPositiveButton(getText(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				UserDataProvider.removeAllHistory(getContext());
			}
		});
		dialogBuilder.show();
	}
	
	protected void showCreditDialog() {
		CreditFragment credit = new CreditFragment(getContext());
		credit.show(getFragmentManager(), Constants.PREFS_CREDIT);
	}
}
