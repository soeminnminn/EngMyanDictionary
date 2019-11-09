package com.s16.utils

import android.app.Activity
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

/*
 * Context
 */
val Context.screenOrientation: Int
    get() {
        val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation = manager.defaultDisplay.rotation
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            }
        }
        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
        } else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

val Context.isTablet: Boolean
    get() {
        val xlarge =
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == 4
        val large =
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE
        return xlarge || large
    }

val Context.isPortrait: Boolean
    get() {
        return screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ||
                screenOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
    }

inline fun <reified T: Any> Context.intentFor(vararg params: Pair<String, Any?>): Intent
        = Intent(this, T::class.java).also {
    if (params.isNotEmpty()) {
        it.putExtras(bundleOf(*params))
    }
}

inline fun <reified T: Any> Fragment.intentFor(vararg params: Pair<String, Any?>): Intent
        = Intent(this.activity, T::class.java).also {
    if (params.isNotEmpty()) {
        it.putExtras(bundleOf(*params))
    }
}

inline fun <reified T: Activity> Context.startActivity(extras: Bundle? = null) {
    val intent = Intent(this, T::class.java)
    if (extras != null) {
        intent.putExtras(extras)
    }
    startActivity(intent)
}

inline fun <reified T: Activity> Context.startActivity(vararg params: Pair<String, Any?>) {
    val intent = Intent(this, T::class.java)
    if (params.isNotEmpty()) {
        intent.putExtras(bundleOf(*params))
    }
    startActivity(intent)
}

inline fun <reified T: Service> Context.startService(vararg params: Pair<String, Any?>) {
    val intent = Intent(this, T::class.java)
    if (params.isNotEmpty()) {
        intent.putExtras(bundleOf(*params))
    }
    startService(intent)
}

inline fun <reified T : Service> Context.stopService(vararg params: Pair<String, Any?>) {
    val intent = Intent(this, T::class.java)
    if (params.isNotEmpty()) {
        intent.putExtras(bundleOf(*params))
    }
    stopService(intent)
}

fun Context.share(text: String, subject: String = "", title: String? = null): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, title))
        true
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        false
    }
}

fun Context.browse(url: String, newTask: Boolean = false): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        if (newTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        false
    }
}

fun Context.getColorCompat(@ColorRes resId: Int): Int {
    return ContextCompat.getColor(this, resId)
}

fun Context.getDrawableCompat(@DrawableRes resId: Int): Drawable? {
    return ContextCompat.getDrawable(this, resId)
}

fun Context.dpToPixel(dp: Int): Int {
    val metrics = resources.displayMetrics
    val px = dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    return px.toInt()
}

fun Context.dpToPixel(dp: Float): Float {
    val metrics = resources.displayMetrics
    return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Context.pixelToDp(px: Int): Float {
    val metrics = resources.displayMetrics
    return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}