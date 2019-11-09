package com.s16.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat


class ClearableEditText : EditText, View.OnTouchListener, View.OnFocusChangeListener {

    enum class Location private constructor(internal val idx: Int) {
        LEFT(0), RIGHT(2)
    }

    private var loc: Location? = Location.RIGHT
    private var xD: Drawable? = null
    private var l: OnTouchListener? = null
    private var f: OnFocusChangeListener? = null

    init {
        super.setOnTouchListener(this)
        super.setOnFocusChangeListener(this)
        initIcon()
        setClearIconVisible(false)
    }

    private val displayedDrawable: Drawable?
        get() = if (this.loc != null) compoundDrawables[loc!!.idx] else null

    constructor(context: Context)
            : super(context) { }

    constructor(context: Context, attrs: AttributeSet)
            : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int)
            : super(context, attrs) {}

    fun setIconLocation(loc: Location?) {
        this.loc = loc
        initIcon()
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        this.l = l
    }

    override fun setOnFocusChangeListener(l: OnFocusChangeListener?) {
        this.f = l
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (displayedDrawable != null && event != null && xD != null) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            val left = if (loc === Location.LEFT) 0 else width - paddingRight - xD!!.intrinsicWidth
            val right = if (loc === Location.LEFT) paddingLeft + xD!!.intrinsicWidth else width
            val tappedX = x in left..right && y >= 0 && y <= bottom - top
            if (tappedX) {
                if (event.action == MotionEvent.ACTION_UP) {
                    setText("")
                }
                return true
            }
        }
        return l?.onTouch(v, event) ?: false
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus) {
            setClearIconVisible((text ?: "").isNotEmpty());
        } else {
            setClearIconVisible(false);
        }
        f?.onFocusChange(v, hasFocus);
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        if (isFocused) {
            setClearIconVisible((text ?: "").isNotEmpty());
        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
    }

    override fun setCompoundDrawables(
        left: Drawable?,
        top: Drawable?,
        right: Drawable?,
        bottom: Drawable?
    ) {
        super.setCompoundDrawables(left, top, right, bottom)
        initIcon()
    }

    private fun initIcon() {
        this.xD = null
        if (this.loc != null) {
            this.xD = compoundDrawables[loc!!.idx]
        }
        if (this.xD == null) {
            this.xD = ContextCompat.getDrawable(context, android.R.drawable.presence_offline)
        }
        this.xD?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            val min = paddingTop + it.intrinsicHeight + paddingBottom
            if (suggestedMinimumHeight < min) {
                minimumHeight = min
            }
        }
    }

    private fun setClearIconVisible(visible: Boolean) {
        val cd = compoundDrawables
        val displayed = this.displayedDrawable
        val wasVisible = displayed != null
        if (visible != wasVisible) {
            val x = if (visible) this.xD else null
            super.setCompoundDrawables(
                if (this.loc === Location.LEFT) x else cd[0],
                cd[1],
                if (this.loc === Location.RIGHT) x else cd[2],
                cd[3]
            )
        }
    }
}