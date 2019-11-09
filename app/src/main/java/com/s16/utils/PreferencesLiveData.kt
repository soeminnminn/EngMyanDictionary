package com.s16.utils

import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

class PreferencesLiveData(private val sharedPreferences: SharedPreferences, vararg keys: String)
    : MediatorLiveData<Map<String, *>>(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val versionData = MutableLiveData<Int>()
    private val mKeys = keys

    init {
        addSource(versionData) {
            value = sharedPreferences.all.toMap()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun toBundle(): Bundle {
        return value?.let { all ->
            Bundle().apply {
                for (k in mKeys) {
                    when (val v = all[k]) {
                        is String -> putString(k, v)
                        is Boolean -> putBoolean(k, v)
                        is Float -> putFloat(k, v)
                        is Int -> putInt(k, v)
                        is Long -> putLong(k, v)
                        is Set<*> -> {
                            val set = v as Set<String>
                            putStringArray(k, set.toTypedArray())
                        }
                        else -> {}
                    }
                }
            }
        } ?: Bundle()
    }

    override fun onActive() {
        super.onActive()

        value = sharedPreferences.all

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onInactive() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onInactive()
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        if (mKeys.contains(key)) {
            val v = (versionData.value ?: 0) + 1
            versionData.value = v
        }
    }
}