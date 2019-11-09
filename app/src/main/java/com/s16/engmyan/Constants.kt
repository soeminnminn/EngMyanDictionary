package com.s16.engmyan

import android.content.Context
import android.graphics.Typeface

object Constants {
    const val ARG_PARAM_ID = "id"
    const val TEXT_SIZE = 16f

    const val PREFS_FONT_SIZE = "prefs_font_size"
    const val PREFS_FORCE_ZAWGYI = "prefs_force_zawgyi"
    const val PREFS_WORD_CLICKABLE = "prefs_used_word_clickable"
    const val PREFS_SHOW_SYNONYM = "prefs_show_synonym"

    const val PREFS_CREDIT = "prefs_credit"
    const val PREFS_ABOUT = "prefs_about"

    const val URL_CREDIT = "file:///android_asset/credit.html"

    @Volatile
    private var zawgyiTypeFace: Typeface? = null
    fun getZawgyiTypeface(context: Context): Typeface =
        zawgyiTypeFace ?: Typeface.createFromAsset(context.assets, "fonts/zawgyi.ttf")

    @Volatile
    private var mmTypeFace: Typeface? = null
    fun getMMTypeFace(context: Context): Typeface =
        mmTypeFace ?: Typeface.createFromAsset(context.assets, "fonts/mmrtext.ttf")
}