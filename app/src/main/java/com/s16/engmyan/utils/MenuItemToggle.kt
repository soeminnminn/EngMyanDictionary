package com.s16.engmyan.utils

import android.view.MenuItem
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

class MenuItemToggle(private val isCheckable: Boolean = false) {

    private var mIsChecked: Boolean = false

    var menuItem: MenuItem? = null
        set(value) {
            field = value
            field?.isCheckable = isCheckable
            field?.isVisible = isVisible
            setCheck(mIsChecked)
        }

    private var iconResId: Int? = null
    private var checkedIconResId: Int? = null

    private var titleResId: Int? = null
    private var checkedTitleResId: Int? = null

    var isVisible: Boolean = true
        set(value) {
            field = value
            menuItem?.isVisible = value
        }

    fun toggle() {
        setCheck(!mIsChecked)
    }

    fun setCheck(check: Boolean) {
        if (!isCheckable) return
        menuItem?.let {
            if (it.isChecked != check) {
                it.isChecked = check

                if (check) {
                    if (checkedIconResId != null) it.setIcon(checkedIconResId!!)
                    if (checkedTitleResId != null) it.setTitle(checkedTitleResId!!)
                } else {
                    if (iconResId != null) it.setIcon(iconResId!!)
                    if (titleResId != null) it.setTitle(titleResId!!)
                }
            }
        }
        mIsChecked = check
    }

    fun isChecked(): Boolean = mIsChecked

    fun setIcon(@DrawableRes resId: Int) {
        iconResId = resId
        setCheck(mIsChecked)
    }

    fun setIconChecked(@DrawableRes resId: Int) {
        checkedIconResId = resId
        setCheck(mIsChecked)
    }

    fun setTitle(@StringRes resId: Int) {
        titleResId = resId
        setCheck(mIsChecked)
    }

    fun setTitleChecked(@StringRes resId: Int) {
        checkedTitleResId = resId
        setCheck(mIsChecked)
    }
}