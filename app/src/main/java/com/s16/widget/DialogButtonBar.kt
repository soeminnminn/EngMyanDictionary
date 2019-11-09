package com.s16.widget

import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.ColorUtils

open class DialogButtonBar : LinearLayout, DialogInterface {

    private val colorAccent: Int
        get() {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorAccent, typedValue, true)
            return typedValue.data
        }

    private val colorControlHighlight: Int
        get() {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorControlHighlight, typedValue, true)
            return typedValue.data
        }

    private val selectableBackground: Int
        get() {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.selectableItemBackground, typedValue, true)
            return typedValue.resourceId
        }

    var dialog: DialogInterface? = null

    private var mPositiveButtonTextId = 0
    private var mPositiveButtonText: CharSequence? = null
    private var mPositiveButtonListener: DialogInterface.OnClickListener? = null
    private var mNegativeButtonTextId = 0
    private var mNegativeButtonText: CharSequence? = null
    private var mNegativeButtonListener: DialogInterface.OnClickListener? = null
    private var mNeutralButtonTextId = 0
    private var mNeutralButtonText: CharSequence? = null
    private var mNeutralButtonListener: DialogInterface.OnClickListener? = null

    private lateinit var neutralButton: DialogButton
    private lateinit var negativeButton: DialogButton
    private lateinit var positiveButton: DialogButton

    constructor(context: Context) : super(context) {
        initialize(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        initialize(context, attrs, defStyleAttr)
    }

    private fun initialize(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        orientation = HORIZONTAL
        gravity = Gravity.BOTTOM
        setPadding(dpToPixel(context, 12), dpToPixel(context, 10), dpToPixel(context, 12), dpToPixel(context, 10))

        val buttonParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        buttonParams.rightMargin = dpToPixel(context, 4)

        neutralButton = DialogButton(context).apply {
            id = AlertDialog.BUTTON_NEUTRAL
            setText(android.R.string.no)
            setBackgroundResource(selectableBackground)
            rippleColor = colorControlHighlight
            layoutParams = buttonParams
            visibility = View.GONE
        }
        neutralButton.setOnClickListener {
            if (mNeutralButtonListener != null) {
                mNeutralButtonListener!!.onClick(this, AlertDialog.BUTTON_NEUTRAL)
            }
        }
        addView(neutralButton)

        val space = Space(context).apply {
            visibility = View.INVISIBLE
        }
        val spaceParams = LayoutParams(0, 0).apply {
            weight = 1f
        }
        addView(space, spaceParams)

        negativeButton = DialogButton(context).apply {
            id = AlertDialog.BUTTON_NEGATIVE
            setText(android.R.string.cancel)
            setTextColor(colorAccent)
            setBackgroundResource(selectableBackground)
            rippleColor = ColorUtils.setAlphaComponent(colorAccent, 60)
            layoutParams = buttonParams
        }
        negativeButton.setOnClickListener {
            if (mNegativeButtonListener != null) {
                mNegativeButtonListener!!.onClick(this, AlertDialog.BUTTON_NEGATIVE)
            }
        }
        addView(negativeButton)

        val positiveButtonParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        positiveButton = DialogButton(context).apply {
            id = AlertDialog.BUTTON_POSITIVE
            setText(android.R.string.ok)
            setTextColor(colorAccent)
            setBackgroundResource(selectableBackground)
            rippleColor = ColorUtils.setAlphaComponent(colorAccent, 60)
            layoutParams = positiveButtonParams
        }
        positiveButton.setOnClickListener {
            if (mPositiveButtonListener != null) {
                mPositiveButtonListener!!.onClick(this, AlertDialog.BUTTON_POSITIVE)
            }
        }
        addView(positiveButton)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateLayout()
    }

    override fun dismiss() {
        if (dialog != null) {
            dialog!!.dismiss()
        }
    }

    override fun cancel() {
        if (dialog != null) {
            dialog!!.cancel()
        }
    }

    private fun updateLayout() {
        when {
            mPositiveButtonTextId != 0 -> {
                positiveButton.setText(mPositiveButtonTextId)
                positiveButton.visibility = View.VISIBLE

            }
            mPositiveButtonText != null -> {
                positiveButton.text = mPositiveButtonText
                positiveButton.visibility = View.VISIBLE

            }
            else -> positiveButton.visibility = View.GONE
        }

        when {
            mNegativeButtonTextId != 0 -> {
                negativeButton.setText(mNegativeButtonTextId)
                negativeButton.visibility = View.VISIBLE

            }
            mNegativeButtonText != null -> {
                negativeButton.text = mPositiveButtonText
                negativeButton.visibility = View.VISIBLE

            }
            else -> negativeButton.visibility = View.GONE
        }

        when {
            mNeutralButtonTextId != 0 -> {
                neutralButton.setText(mNeutralButtonTextId)
                neutralButton.visibility = View.VISIBLE

            }
            mNeutralButtonText != null -> {
                neutralButton.text = mPositiveButtonText
                neutralButton.visibility = View.VISIBLE

            }
            else -> neutralButton.visibility = View.GONE
        }

        requestLayout()
    }

    /**
     * Set a listener to be invoked when the positive image_button of the dialog is pressed.
     * @param textId The resource id of the text to onDisplayRebuyList in the positive image_button
     * @param listener The [DialogInterface.OnClickListener] to use.
     */
    fun setPositiveButton(@StringRes textId: Int, listener: DialogInterface.OnClickListener) {
        mPositiveButtonTextId = textId
        mPositiveButtonText = null
        mPositiveButtonListener = listener
        updateLayout()
    }

    /**
     * Set a listener to be invoked when the positive image_button of the dialog is pressed.
     * @param text The text to onDisplayRebuyList in the positive image_button
     * @param listener The [DialogInterface.OnClickListener] to use.
     */
    fun setPositiveButton(text: CharSequence?, listener: DialogInterface.OnClickListener?) {
        mPositiveButtonTextId = 0
        mPositiveButtonText = text
        mPositiveButtonListener = listener
        updateLayout()
    }

    /**
     * Set a listener to be invoked when the negative image_button of the dialog is pressed.
     * @param textId The resource id of the text to onDisplayRebuyList in the negative image_button
     * @param listener The [DialogInterface.OnClickListener] to use.
     */
    fun setNegativeButton(@StringRes textId: Int, listener: DialogInterface.OnClickListener) {
        mNegativeButtonTextId = textId
        mNegativeButtonText = null
        mNegativeButtonListener = listener
        updateLayout()
    }

    /**
     * Set a listener to be invoked when the negative image_button of the dialog is pressed.
     * @param text The text to onDisplayRebuyList in the negative image_button
     * @param listener The [DialogInterface.OnClickListener] to use.
     */
    fun setNegativeButton(text: CharSequence?, listener: DialogInterface.OnClickListener?) {
        mNegativeButtonTextId = 0
        mNegativeButtonText = text
        mNegativeButtonListener = listener
        updateLayout()
    }

    /**
     * Set a listener to be invoked when the neutral image_button of the dialog is pressed.
     * @param textId The resource id of the text to onDisplayRebuyList in the neutral image_button
     * @param listener The [DialogInterface.OnClickListener] to use.
     */
    fun setNeutralButton(@StringRes textId: Int, listener: DialogInterface.OnClickListener) {
        mNeutralButtonTextId = textId
        mNeutralButtonText = null
        mNeutralButtonListener = listener
        updateLayout()
    }

    /**
     * Set a listener to be invoked when the neutral image_button of the dialog is pressed.
     * @param text The text to onDisplayRebuyList in the neutral image_button
     * @param listener The [DialogInterface.OnClickListener] to use.
     */
    fun setNeutralButton(text: CharSequence?, listener: DialogInterface.OnClickListener?) {
        mNeutralButtonTextId = 0
        mNeutralButtonText = text
        mNeutralButtonListener = listener
        updateLayout()
    }

    private class DialogButton: TextView {

        constructor(context: Context) : super(context) {
            initialize(context, null, 0)
        }

        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            initialize(context, attrs, 0)
        }

        constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
                : super(context, attrs, defStyleAttr) {
            initialize(context, attrs, defStyleAttr)
        }

        private fun initialize(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
            gravity = Gravity.CENTER
            isClickable = true
            minEms = 1
            maxLines = 1
            minWidth = dpToPixel(context,52)
            isAllCaps = true
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dpToPixel(context,12), dpToPixel(context,8), dpToPixel(context,12), dpToPixel(context,8))
        }

        var rippleColor: Int = 0
            set(color) {
                field = color
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val sd = ShapeDrawable()
                    val radii = FloatArray(8)
                    radii.fill(12f)
                    sd.shape = RoundRectShape(radii, null, null)
                    background = RippleDrawable(ColorStateList.valueOf(color), null, sd)
                }
            }
    }

    companion object {
        private fun dpToPixel(context: Context, dp: Int): Int {
            val metrics = context.resources.displayMetrics
            val px = dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
            return px.toInt()
        }
    }
}

// MARK: Extensions

inline fun DialogButtonBar.setPositiveButton(
    @StringRes textId: Int, crossinline listener: (dialog: DialogInterface?, which: Int) -> Unit) {
    setPositiveButton(textId,
        DialogInterface.OnClickListener { dialog, which -> listener.invoke(dialog, which) })
}

inline fun DialogButtonBar.setPositiveButton(
    text: CharSequence, crossinline listener: (dialog: DialogInterface?, which: Int) -> Unit) {
    setPositiveButton(text,
        DialogInterface.OnClickListener { dialog, which -> listener.invoke(dialog, which) })
}

inline fun DialogButtonBar.setNegativeButton(
    @StringRes textId: Int, crossinline listener: (dialog: DialogInterface?, which: Int) -> Unit) {
    setNegativeButton(textId,
        DialogInterface.OnClickListener { dialog, which -> listener.invoke(dialog, which) })
}

inline fun DialogButtonBar.setNegativeButton(
    text: CharSequence, crossinline listener: (dialog: DialogInterface?, which: Int) -> Unit) {
    setNegativeButton(text,
        DialogInterface.OnClickListener { dialog, which -> listener.invoke(dialog, which) })
}

inline fun DialogButtonBar.setNeutralButton(
    @StringRes textId: Int, crossinline listener: (dialog: DialogInterface?, which: Int) -> Unit) {
    setNeutralButton(textId,
        DialogInterface.OnClickListener { dialog, which -> listener.invoke(dialog, which) })
}

inline fun DialogButtonBar.setNeutralButton(
    text: CharSequence, crossinline listener: (dialog: DialogInterface?, which: Int) -> Unit) {
    setNeutralButton(text,
        DialogInterface.OnClickListener { dialog, which -> listener.invoke(dialog, which) })
}