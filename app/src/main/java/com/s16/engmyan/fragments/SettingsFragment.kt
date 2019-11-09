package com.s16.engmyan.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.s16.engmyan.Constants
import com.s16.engmyan.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>(Constants.PREFS_CREDIT)?.setOnPreferenceClickListener {
            CreditFragment.newInstance().show(childFragmentManager, "creditFragment")
            true
        }
    }
}