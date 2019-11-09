package com.s16.engmyan.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;

import com.s16.engmyan.Constants;
import com.s16.engmyan.R;

public class SettingsFragment extends PreferenceFragment {
	
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.settings);
        
        Preference prefsCredit = findPreference(Constants.PREFS_CREDIT);
        prefsCredit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        	@Override
			public boolean onPreferenceClick(Preference preference) {
        		showCreditDialog();
				return false;
			}
        });
	}
	
	protected void showCreditDialog() {
		CreditFragment credit = new CreditFragment();
		credit.show(getFragmentManager(), "CreditFragment");
	}
}
