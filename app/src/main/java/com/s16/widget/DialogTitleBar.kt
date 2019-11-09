package com.s16.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.DialogTitle
import androidx.core.view.GravityCompat

open class DialogTitleBar : LinearLayout {

    private val dialogPreferredPadding: Int
        get() {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.dialogPreferredPadding, typedValue, true)
            return typedValue.data
        }

    private val dialogPaddingTopMaterial: Int
        get() {
            val metrics = context.resources.displayMetrics
            val px = 18 * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
            return px.toInt()
        }

    private val dialogTitleDividerMaterial: Int
        get() {
            val metrics = context.resources.displayMetrics
            val px = 8 * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
            return px.toInt()
        }

    private var mTitle: CharSequence? = null
    private var mIconId: Int = 0
    private var mIcon: Drawable? = null
    private var mShowDivider: Boolean = false
    private var mDivider: Drawable? = null
    private var mTypeface: Typeface? = null

    private lateinit var titleTemplate : LinearLayout
    private lateinit var mIconView: ImageView
    private lateinit var mTitleView: DialogTitle

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
        orientation = VERTICAL
        // id = androidx.appcompat.R.id.topPanel

        resolveAttrs(context)
        val textAppearanceId = android.R.style.TextAppearance_WindowTitle

        titleTemplate = LinearLayout(context).apply {
            id = androidx.appcompat.R.id.title_template
            gravity = Gravity.CENTER_VERTICAL or GravityCompat.START
            orientation = HORIZONTAL
            setPadding(dialogPreferredPadding, dialogPaddingTopMaterial, dialogPreferredPadding, 0)
        }
        val titleTemplateParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(titleTemplate, titleTemplateParams)

        mIconView = ImageView(context).apply {
            id = android.R.id.icon
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        val iconSize = dpToPixel(context, 32)
        val iconParams = LayoutParams(iconSize, iconSize).apply {
            rightMargin = dpToPixel(context, 8)
        }
        titleTemplate.addView(mIconView, iconParams)

        mTitleView = DialogTitle(context).apply {
            ellipsize = TextUtils.TruncateAt.END
            setSingleLine(true)

            if (Build.VERSION.SDK_INT >= 23) {
                setTextAppearance(textAppearanceId)
            } else {
                setTextAppearance(context, textAppearanceId)
            }

            if (Build.VERSION.SDK_INT >= 17) {
                textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            }

            if (mTypeface != null) {
                typeface = mTypeface
            }
        }

        val alertTitleParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = GravityCompat.START
        }
        titleTemplate.addView(mTitleView, alertTitleParams)

        val space = Space(context).apply {
            visibility = View.GONE
        }
        if (mShowDivider) {
            space.visibility = View.VISIBLE
        }
        val spaceParams = LayoutParams(LayoutParams.MATCH_PARENT, dialogTitleDividerMaterial)
        addView(space, spaceParams)

        setupTitle()
    }

    private fun resolveAttrs(context: Context) {
        val titleValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.title, titleValue, true)
        mTitle = titleValue.string

        val iconValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.icon, iconValue, true)
        mIconId = iconValue.resourceId

        val dividerValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.divider, dividerValue, false)
        mShowDivider = dividerValue.string == "true"

        val a = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
        mDivider = a.getDrawable(0)
        a.recycle()
    }

    private fun setupTitle() {
        val hasTextTitle = !TextUtils.isEmpty(mTitle)
        if (hasTextTitle) {
            // Display the title if a title is supplied, else hide it.
            mTitleView.text = mTitle

            // Do this last so that if the user has supplied any icons we
            // use them instead of the default ones. If the user has
            // specified 0 then make it disappear.
            when {
                mIconId != 0 -> mIconView.setImageResource(mIconId)
                mIcon != null -> mIconView.setImageDrawable(mIcon)
                else -> {
                    // Apply the padding from the icon to ensure the title is
                    // aligned correctly.
                    mTitleView.setPadding(mIconView.paddingLeft,
                        mIconView.paddingTop,
                        mIconView.paddingRight,
                        mIconView.paddingBottom
                    )
                    mIconView.visibility = View.GONE
                }
            }
        } else {
            // Hide the title template
            titleTemplate.visibility = View.GONE
            mIconView.visibility = View.GONE
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    /**
     * Set the title using the given resource id.
     */
    fun setTitle(@StringRes titleId: Int) {
        setTitle(context.getText(titleId))
    }

    /**
     * Set the title displayed in the {@link Dialog}.
     */
    fun setTitle(title: CharSequence) {
        mTitle = title
        if (::mTitleView.isInitialized) {
            mTitleView.text = title
        }
    }

    /**
     * Set resId to 0 if you don't want an mIconView.
     * @param resId the resourceId of the drawable to use as the mIconView or 0
     * if you don't want an mIconView.
     */
    fun setIcon(@DrawableRes resId: Int) {
        mIcon = null
        mIconId = resId

        if (::mIconView.isInitialized) {
            if (resId != 0) {
                mIconView.setImageResource(resId)
            } else {
                mIconView.visibility = View.GONE
            }
        }
    }

    /**
     * Set the {@link Drawable} to be used in the title.
     *
     * @param icon Drawable to use as the mIconView or null if you don't want an mIconView.
     */
    fun setIcon(icon: Drawable?) {
        mIcon = icon
        mIconId = 0
        if (::mIconView.isInitialized) {
            if (icon != null) {
                mIconView.setImageDrawable(icon)
            } else {
                mIconView.visibility = View.GONE
            }
        }
    }

    fun setTypeFace(typeface: Typeface) {
        mTypeface = typeface
        if (::mTitleView.isInitialized) {
            mTitleView.typeface = typeface
        }
    }

    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)

        if (mShowDivider && canvas != null && mDivider != null) {
            canvas.save()
            val top = bottom - mDivider!!.intrinsicHeight
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(canvas)
            canvas.restore()
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