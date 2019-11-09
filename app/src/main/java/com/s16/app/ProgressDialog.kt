package com.s16.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable

import java.text.NumberFormat

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT

class ProgressDialog : AlertDialog {

    private var mProgress: ProgressBar? = null
    private var mMessageView: AppCompatTextView? = null

    private var mProgressStyle = STYLE_SPINNER
    private var mProgressNumber: AppCompatTextView? = null
    private var mProgressNumberFormat: String? = null
    private var mProgressPercent: AppCompatTextView? = null
    private var mProgressPercentFormat: NumberFormat? = null

    private var mMax: Int = 0
    private var mProgressVal: Int = 0
    private var mSecondaryProgressVal: Int = 0
    private var mIncrementBy: Int = 0
    private var mIncrementSecondaryBy: Int = 0
    private var mProgressDrawable: Drawable? = null
    private var mIndeterminateDrawable: Drawable? = null
    private var mMessage: CharSequence? = null
    private var mIndeterminate: Boolean = false

    private var mHasStarted: Boolean = false
    private var mViewUpdateHandler: Handler? = null

    private val colorPrimary: Int
        get() {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
            return typedValue.data
        }
    /**
     * Gets the current progress.
     *
     * @return the current progress, a value between 0 and [.getMax]
     */
    /**
     * Sets the current progress.
     *
     * @param value the current progress, a value between 0 and [.getMax]
     *
     * @see ProgressBar.setProgress
     */
    var progress: Int
        get() = if (mProgress != null) {
            mProgress!!.progress
        } else mProgressVal
        set(value) = if (mHasStarted) {
            mProgress!!.progress = value
            onProgressChanged()
        } else {
            mProgressVal = value
        }
    /**
     * Gets the current secondary progress.
     *
     * @return the current secondary progress, a value between 0 and [.getMax]
     */
    /**
     * Sets the secondary progress.
     *
     * @param secondaryProgress the current secondary progress, a value between 0 and
     * [.getMax]
     *
     * @see ProgressBar.setSecondaryProgress
     */
    var secondaryProgress: Int
        get() {
            return if (mProgress != null) {
                mProgress!!.secondaryProgress
            } else mSecondaryProgressVal
        }
        set(secondaryProgress) = if (mProgress != null) {
            mProgress!!.secondaryProgress = secondaryProgress
            onProgressChanged()
        } else {
            mSecondaryProgressVal = secondaryProgress
        }
    /**
     * Gets the maximum allowed progress value. The default value is 100.
     *
     * @return the maximum value
     */
    /**
     * Sets the maximum allowed progress value.
     */
    var max: Int
        get() {
            return if (mProgress != null) {
                mProgress!!.max
            } else mMax
        }
        set(max) = if (mProgress != null) {
            mProgress!!.max = max
            onProgressChanged()
        } else {
            mMax = max
        }
    /**
     * Whether this ProgressDialog is in indeterminate mode.
     *
     * @return true if the dialog is in indeterminate mode, false otherwise
     */
    /**
     * Change the indeterminate mode for this ProgressDialog. In indeterminate
     * mode, the progress is ignored and the dialog shows an infinite
     * animation instead.
     *
     *
     * **Note:** A ProgressDialog with style [.STYLE_SPINNER]
     * is always indeterminate and will ignore this setting.
     *
     * @param indeterminate true to enable indeterminate mode, false otherwise
     *
     * @see .setProgressStyle
     */
    var isIndeterminate: Boolean
        get() {
            return if (mProgress != null) {
                mProgress!!.isIndeterminate
            } else mIndeterminate
        }
        set(indeterminate) = if (mProgress != null) {
            mProgress!!.isIndeterminate = indeterminate
        } else {
            mIndeterminate = indeterminate
        }

    /**
     * Creates a Progress dialog.
     *
     * @param context the parent context
     */
    constructor(context: Context) : super(context) {
        initFormats()
    }

    /**
     * Creates a Progress dialog.
     *
     * @param context the parent context
     * @param theme the resource ID of the theme against which to inflate
     * this dialog, or `0` to use the parent
     * `context`'s default alert dialog theme
     */
    constructor(context: Context, theme: Int) : super(context, theme) {
        initFormats()
    }

    private fun initFormats() {
        mProgressNumberFormat = "%1d/%2d"
        mProgressPercentFormat = NumberFormat.getPercentInstance()
        mProgressPercentFormat!!.maximumFractionDigits = 0
    }

    private fun dpToPixel(dp: Int): Int {
        val metrics = context.resources.displayMetrics
        val px = dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        return px.toInt()
    }

    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (mProgressStyle == STYLE_HORIZONTAL) {

            /* Use a separate handler to update the text views as they
            * must be updated on the same thread that created them.
            */
            mViewUpdateHandler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)

                    /* Update the number and percent */
                    val progress = mProgress!!.progress
                    val max = mProgress!!.max
                    if (mProgressNumberFormat != null) {
                        val format = mProgressNumberFormat
                        mProgressNumber!!.text = String.format(format!!, progress, max)
                    } else {
                        mProgressNumber!!.text = ""
                    }
                    if (mProgressPercentFormat != null) {
                        val percent = progress.toDouble() / max.toDouble()
                        val tmp = SpannableString(mProgressPercentFormat!!.format(percent))
                        tmp.setSpan(
                            StyleSpan(android.graphics.Typeface.BOLD),
                            0, tmp.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        mProgressPercent!!.text = tmp
                    } else {
                        mProgressPercent!!.text = ""
                    }
                }
            }

            val view = createAlertDialogProgress()
            setView(view)

        } else {
            val view = createProgressDialog()
            setView(view)
        }

        if (mMax > 0) {
            max = mMax
        }
        if (mProgressVal > 0) {
            progress = mProgressVal
        }
        if (mSecondaryProgressVal > 0) {
            secondaryProgress = mSecondaryProgressVal
        }
        if (mIncrementBy > 0) {
            incrementProgressBy(mIncrementBy)
        }
        if (mIncrementSecondaryBy > 0) {
            incrementSecondaryProgressBy(mIncrementSecondaryBy)
        }
        if (mProgressDrawable != null) {
            setProgressDrawable(mProgressDrawable!!)
        }
        if (mIndeterminateDrawable != null) {
            setIndeterminateDrawable(mIndeterminateDrawable!!)
        }
        if (mMessage != null) {
            setMessage(mMessage!!)
        }
        isIndeterminate = mIndeterminate
        onProgressChanged()
        super.onCreate(savedInstanceState)
    }

    private fun createAlertDialogProgress(): ViewGroup {
        val rootView = RelativeLayout(context)
        rootView.layoutParams = RelativeLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)

        mProgress = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        mProgress!!.id = android.R.id.progress
        val progressParams = RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        progressParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        progressParams.topMargin = dpToPixel(12)
        progressParams.bottomMargin = dpToPixel(1)
        progressParams.leftMargin = dpToPixel(10)
        progressParams.rightMargin = dpToPixel(10)
        mProgress!!.layoutParams = progressParams
        rootView.addView(mProgress)

        mProgressNumber = AppCompatTextView(context)
        val numberParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        numberParams.leftMargin = dpToPixel(10)
        numberParams.rightMargin = dpToPixel(10)
        numberParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        numberParams.addRule(RelativeLayout.BELOW, android.R.id.progress)
        mProgressNumber!!.layoutParams = numberParams
        mProgressNumber!!.setPadding(0, 0, 0, dpToPixel(12))
        rootView.addView(mProgressNumber)

        mProgressPercent = AppCompatTextView(context)
        val percentParams = RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        percentParams.leftMargin = dpToPixel(10)
        percentParams.rightMargin = dpToPixel(10)
        percentParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        percentParams.addRule(RelativeLayout.BELOW, android.R.id.progress)
        mProgressPercent!!.layoutParams = percentParams
        mProgressPercent!!.setPadding(0, 0, 0, dpToPixel(12))
        rootView.addView(mProgressPercent)

        return rootView
    }

    private fun createProgressDialog(): ViewGroup {
        val rootView = FrameLayout(context)
        rootView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

        val contentView = LinearLayout(context)
        contentView.orientation = LinearLayout.HORIZONTAL
        contentView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        contentView.isBaselineAligned = false
        contentView.setPadding(dpToPixel(8), dpToPixel(10), dpToPixel(8), dpToPixel(10))

        mProgress = ProgressBar(context, null, android.R.attr.progressBarStyle)
        mProgress!!.id = android.R.id.progress
        val progressParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        progressParams.rightMargin = dpToPixel(12)
        mProgress!!.layoutParams = progressParams
        mProgress!!.max = 10000

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val progressDrawable = CircularProgressDrawable(context)
            progressDrawable.setColorSchemeColors(colorPrimary)
            progressDrawable.centerRadius = dpToPixel(16).toFloat()
            progressDrawable.strokeWidth = dpToPixel(4).toFloat()
            mProgress!!.indeterminateDrawable = progressDrawable
        }
        contentView.addView(mProgress)

        mMessageView = AppCompatTextView(context)
        mMessageView!!.id = android.R.id.message
        val messageParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        messageParams.gravity = Gravity.CENTER_VERTICAL
        mMessageView!!.layoutParams = messageParams
        contentView.addView(mMessageView)

        rootView.addView(contentView)

        return rootView
    }

    public override fun onStart() {
        super.onStart()
        mHasStarted = true
    }

    override fun onStop() {
        super.onStop()
        mHasStarted = false
    }

    /**
     * Increments the current progress value.
     *
     * @param diff the amount by which the current progress will be incremented,
     * up to [.getMax]
     */
    fun incrementProgressBy(diff: Int) {
        if (mProgress != null) {
            mProgress!!.incrementProgressBy(diff)
            onProgressChanged()
        } else {
            mIncrementBy += diff
        }
    }

    /**
     * Increments the current secondary progress value.
     *
     * @param diff the amount by which the current secondary progress will be incremented,
     * up to [.getMax]
     */
    fun incrementSecondaryProgressBy(diff: Int) {
        if (mProgress != null) {
            mProgress!!.incrementSecondaryProgressBy(diff)
            onProgressChanged()
        } else {
            mIncrementSecondaryBy += diff
        }
    }

    /**
     * Sets the drawable to be used to display the progress value.
     *
     * @param d the drawable to be used
     *
     * @see ProgressBar.setProgressDrawable
     */
    fun setProgressDrawable(d: Drawable) {
        if (mProgress != null) {
            mProgress!!.progressDrawable = d
        } else {
            mProgressDrawable = d
        }
    }

    /**
     * Sets the drawable to be used to display the indeterminate progress value.
     *
     * @param d the drawable to be used
     *
     * @see ProgressBar.setProgressDrawable
     * @see .setIndeterminate
     */
    fun setIndeterminateDrawable(d: Drawable) {
        if (mProgress != null) {
            mProgress!!.indeterminateDrawable = d
        } else {
            mIndeterminateDrawable = d
        }
    }

    override fun setMessage(message: CharSequence) {
        if (mProgress != null) {
            if (mProgressStyle == STYLE_HORIZONTAL) {
                super.setMessage(message)
            } else {
                mMessageView!!.text = message
            }
        } else {
            mMessage = message
        }
    }

    /**
     * Sets the style of this ProgressDialog, either [.STYLE_SPINNER] or
     * [.STYLE_HORIZONTAL]. The default is [.STYLE_SPINNER].
     *
     *
     * **Note:** A ProgressDialog with style [.STYLE_SPINNER]
     * is always indeterminate and will ignore the [indeterminate][.setIndeterminate] setting.
     *
     * @param style the style of this ProgressDialog, either [.STYLE_SPINNER] or
     * [.STYLE_HORIZONTAL]
     */
    fun setProgressStyle(style: Int) {
        mProgressStyle = style
    }

    /**
     * Change the format of the small text showing current and maximum units
     * of progress.  The default is "%1d/%2d".
     * Should not be called during the number is progressing.
     * @param format A string passed to [String.format()][String.format];
     * use "%1d" for the current number and "%2d" for the maximum.  If null,
     * nothing will be shown.
     */
    fun setProgressNumberFormat(format: String) {
        mProgressNumberFormat = format
        onProgressChanged()
    }

    /**
     * Change the format of the small text showing the percentage of progress.
     * The default is
     * [NumberFormat.getPercentageInstnace().][NumberFormat.getPercentInstance]
     * Should not be called during the number is progressing.
     * @param format An instance of a [NumberFormat] to generate the
     * percentage text.  If null, nothing will be shown.
     */
    fun setProgressPercentFormat(format: NumberFormat) {
        mProgressPercentFormat = format
        onProgressChanged()
    }

    private fun onProgressChanged() {
        if (mProgressStyle == STYLE_HORIZONTAL) {
            if (mViewUpdateHandler != null && !mViewUpdateHandler!!.hasMessages(0)) {
                mViewUpdateHandler!!.sendEmptyMessage(0)
            }
        }
    }

    companion object {

        /**
         * Creates a ProgressDialog with a circular, spinning progress
         * bar. This is the default.
         */
        const val STYLE_SPINNER = 0

        /**
         * Creates a ProgressDialog with a horizontal progress bar.
         */
        const val STYLE_HORIZONTAL = 1

        /**
         * Creates and shows a ProgressDialog.
         *
         * @param context the parent context
         * @param title the title text for the dialog's window
         * @param message the text to be displayed in the dialog
         * @param indeterminate true if the dialog should be [indeterminate][.setIndeterminate], false otherwise
         * @param cancelable true if the dialog is [cancelable][.setCancelable],
         * false otherwise
         * @param cancelListener the [listener][.setOnCancelListener]
         * to be invoked when the dialog is canceled
         * @return the ProgressDialog
         */
        @JvmOverloads
        fun show(
            context: Context, title: CharSequence,
            message: CharSequence, indeterminate: Boolean = false,
            cancelable: Boolean = false, cancelListener: DialogInterface.OnCancelListener? = null
        ): ProgressDialog {
            val dialog = ProgressDialog(context)
            dialog.setTitle(title)
            dialog.setMessage(message)
            dialog.isIndeterminate = indeterminate
            dialog.setCancelable(cancelable)
            dialog.setOnCancelListener(cancelListener)
            dialog.show()
            return dialog
        }
    }
}