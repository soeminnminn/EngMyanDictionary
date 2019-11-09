package com.s16.utils

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

/*
 * Fragment
 */
inline fun <reified T: Activity> Fragment.startActivity(extras: Bundle? = null) {
    val intent = Intent(activity, T::class.java)
    if (extras != null) {
        intent.putExtras(extras)
    }
    startActivity(intent)
}

inline fun <reified T: Activity> Fragment.startActivity(vararg params: Pair<String, Any?>) {
    val intent = Intent(activity, T::class.java)
    if (params.isNotEmpty()) {
        intent.putExtras(bundleOf(*params))
    }
    startActivity(intent)
}


inline fun <reified T: Activity> Fragment.startActivityForResult(requestCode: Int, extras: Bundle? = null) {
    val intent = Intent(activity!!, T::class.java)
    if (extras != null) {
        intent.putExtras(extras)
    }
    startActivityForResult(intent, requestCode)
}

inline fun <reified T: Activity> Fragment.startActivityForResult(requestCode: Int, vararg params: Pair<String, Any?>) {
    val intent = Intent(activity!!, T::class.java)
    if (params.isNotEmpty()) {
        intent.putExtras(bundleOf(*params))
    }
    startActivityForResult(intent, requestCode)
}

inline fun <reified T: Service> Fragment.startService(vararg params: Pair<String, Any?>) {
    val intent = Intent(activity!!, T::class.java)
    if (params.isNotEmpty()) {
        intent.putExtras(bundleOf(*params))
    }
    activity!!.startService(intent)
}

inline fun <reified T : Service> Fragment.stopService(vararg params: Pair<String, Any?>) {
    val intent = Intent(activity!!, T::class.java)
    if (params.isNotEmpty()) {
        intent.putExtras(bundleOf(*params))
    }
    activity!!.stopService(intent)
}