package com.s16.engmyan.utils

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.s16.engmyan.Constants
import com.s16.engmyan.R
import com.s16.engmyan.activity.DetailsActivity
import com.s16.engmyan.data.FavoriteItem

object UIManager {
    fun setTheme(selectedOption: String?) {
        val mode = when(selectedOption) {
            Constants.PREFS_THEMES_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            Constants.PREFS_THEMES_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            Constants.PREFS_THEMES_BATTERY -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun createShortcuts(context: Context, list: List<FavoriteItem>) {
        ShortcutManagerCompat.removeAllDynamicShortcuts(context)

        val shortcuts : MutableList<ShortcutInfoCompat> = mutableListOf()
        for(fav in list) {
            val shortcutIntent = Intent(context.applicationContext, DetailsActivity::class.java)
            shortcutIntent.action = Intent.ACTION_VIEW
            shortcutIntent.putExtra(Constants.ARG_PARAM_ID, fav.refId)

            val shortcut = ShortcutInfoCompat.Builder(context, "${fav.refId})")
                .setShortLabel("${fav.word}")
                .setLongLabel(context.getString(R.string.title_shortcut, "${fav.word}"))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_definition))
                .setIntent(shortcutIntent)
                .build()
            shortcuts.add(shortcut)
        }
        ShortcutManagerCompat.addDynamicShortcuts(context, shortcuts)
    }
}