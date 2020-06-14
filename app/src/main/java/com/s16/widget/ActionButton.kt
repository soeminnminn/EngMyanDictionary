package com.s16.widget

import android.content.Context
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import com.google.android.material.button.MaterialButton


class ActionButton: MaterialButton, View.OnLongClickListener {

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int)
            : super(context, attrs, defStyle) {
        initialize(context)
    }

    private fun initialize(context: Context) {
        isLongClickable = true
        setOnLongClickListener(this)
    }

    override fun onLongClick(v: View?): Boolean {
        if (v == null || !isEnabled) return false

        val description = contentDescription
        if (TextUtils.isEmpty(description)) return false

        val screenPos = IntArray(2)
        val displayFrame = Rect()
        v.getLocationOnScreen(screenPos)
        v.getWindowVisibleDisplayFrame(displayFrame)

        val width = v.width
        val height = v.height
        val midy = screenPos[1] + height / 2
        var referenceX = screenPos[0] + width / 2

        if (ViewCompat.getLayoutDirection(v) == ViewCompat.LAYOUT_DIRECTION_LTR) {
            val screenWidth = context.resources.displayMetrics.widthPixels
            referenceX = screenWidth - referenceX // mirror
        }

        val cheatSheet = Toast.makeText(context, description, Toast.LENGTH_SHORT)
        if (midy < displayFrame.height()) {
            // Show along the top; follow action buttons
            cheatSheet.setGravity(Gravity.TOP or GravityCompat.END, referenceX, height)

        } else {
            // Show along the bottom center
            cheatSheet.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, height)
        }
        cheatSheet.show()
        return true
    }
}