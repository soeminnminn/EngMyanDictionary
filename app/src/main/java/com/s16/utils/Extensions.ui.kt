package com.s16.utils

import android.app.Activity
import android.content.res.Resources
import android.util.TypedValue
import android.view.*
import java.lang.Exception


/*
 * Resources.Theme
 */
val Resources.Theme.defaultTextColor : Int
     get() {
        val typedValue = TypedValue()
        resolveAttribute(com.google.android.material.R.attr.itemTextColor, typedValue, true)
        return typedValue.data
    }

/*
 * MenuItem
 */
fun MenuItem.backPressed(activity: Activity): Boolean {
    if (itemId == android.R.id.home) {
        activity.onBackPressed()
        return true
    }
    return false
}

/*
 * View
 */
fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

inline fun <reified T> View.tagAs(): T? {
    return if (tag == null) {
        null
    } else {
        try {
            tag as T
        } catch(e: Exception) {
            null
        }
    }
}