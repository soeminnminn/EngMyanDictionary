package com.s16.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

val Context.defaultSharedPreferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

fun SharedPreferences.getStringAsBoolean(key: String, defValue: Boolean): Boolean {
    return getString(key, "$defValue") == true.toString()
}

fun SharedPreferences.getStringAsInt(key: String, defValue: Int): Int {
    return (getString(key, "$defValue") ?: "$defValue").toInt()
}

fun SharedPreferences.getStringAsFloat(key: String, defValue: Float): Float {
    return (getString(key, "$defValue") ?: "$defValue").toFloat()
}

fun SharedPreferences.getStringAsLong(key: String, defValue: Long): Long {
    return (getString(key, "$defValue") ?: "$defValue").toLong()
}

infix fun SharedPreferences.booleanOf(key: String): Boolean {
    return getBoolean(key, false)
}

infix fun SharedPreferences.intOf(key: String): Int {
    return getInt(key, 0)
}

infix fun SharedPreferences.floatOf(key: String): Float {
    return getFloat(key, 0.0F)
}

infix fun SharedPreferences.longOf(key: String): Long {
    return getLong(key, 0)
}

infix fun SharedPreferences.stringOf(key: String): String {
    return getString(key, "") ?: ""
}

infix fun SharedPreferences.nullableStringOf(key: String): String? {
    return getString(key, null)
}

infix fun SharedPreferences.stringSetOf(key: String): Set<String> {
    return getStringSet(key, null) ?: mutableSetOf()
}

infix fun SharedPreferences.nullableStringSetOf(key: String): Set<String>? {
    return getStringSet(key, null)
}

fun Context.putBooleanPreference(key: String, value: Boolean) {
    val editor = defaultSharedPreferences.edit()
    editor.putBoolean(key, value)
    editor.apply()
}

fun Context.putIntPreference(key: String, value: Int) {
    val editor = defaultSharedPreferences.edit()
    editor.putInt(key, value)
    editor.apply()
}

fun Context.putFloatPreference(key: String, value: Float) {
    val editor = defaultSharedPreferences.edit()
    editor.putFloat(key, value)
    editor.apply()
}

fun Context.putLongPreference(key: String, value: Long) {
    val editor = defaultSharedPreferences.edit()
    editor.putLong(key, value)
    editor.apply()
}

fun Context.putStringPreference(key: String, value: String?) {
    val editor = defaultSharedPreferences.edit()
    editor.putString(key, value)
    editor.apply()
}

fun Context.putStringSetPreference(key: String, value: MutableSet<String>) {
    val editor = defaultSharedPreferences.edit()
    editor.putStringSet(key, value)
    editor.apply()
}