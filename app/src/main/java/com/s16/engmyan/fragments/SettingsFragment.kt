package com.s16.engmyan.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.s16.engmyan.Constants
import com.s16.engmyan.R
import com.s16.engmyan.utils.UIManager

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val selectedOption = preferenceScreen.sharedPreferences.getString(Constants.PREFS_THEMES, "")
        setThemeSummary(selectedOption)

        findPreference<Preference>(Constants.PREFS_CREDIT)?.setOnPreferenceClickListener {
            CreditFragment.newInstance().show(childFragmentManager, "creditFragment")
            true
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == Constants.PREFS_THEMES) {
            val selectedOption = sharedPreferences.getString(Constants.PREFS_THEMES, "")
            UIManager.setTheme(selectedOption)
            setThemeSummary(selectedOption)
        }
    }

    private fun setThemeSummary(selected: String?) {
        findPreference<Preference>(Constants.PREFS_THEMES)?.apply {
            summary = when(selected) {
                Constants.PREFS_THEMES_LIGHT -> getString(R.string.prefs_theme_light)
                Constants.PREFS_THEMES_DARK -> getString(R.string.prefs_theme_dark)
                Constants.PREFS_THEMES_BATTERY -> getString(R.string.prefs_theme_battery)
                else -> getString(R.string.prefs_theme_system)
            }
        }
    }
}