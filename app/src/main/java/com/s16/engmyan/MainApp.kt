package com.s16.engmyan

import android.app.Application
import androidx.preference.PreferenceManager
import com.s16.engmyan.utils.UIManager

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        UIManager.setTheme(preferences.getString(Constants.PREFS_THEMES, ""))
    }
}