package com.s16.app

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.HtmlCompat
import androidx.preference.DialogPreference
import androidx.preference.PreferenceViewHolder

class AboutDialogPreference : DialogPreference,
    DialogInterface.OnClickListener, DialogInterface.OnDismissListener {

    private var mMessage: CharSequence? = null
    private var mDialog: Dialog? = null

    private val messageTextColor: Int
        get() {
            val a = context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.textColorSecondary))
            val color = a.getColor(0, 0x808080)
            a.recycle()
            return color
        }

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
        mMessage = summary
        setNegativeButtonText(android.R.string.ok)
        updateSummary(context)
    }

    override fun onAttached() {
        super.onAttached()
        createDialog()
    }

    override fun onDetached() {
        mDialog?.dismiss()
        mDialog = null
        super.onDetached()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val imageView: ImageView = holder.findViewById(android.R.id.icon) as ImageView
        val textColor = messageTextColor
        if (textColor != -1) {
            DrawableCompat.setTint(icon, textColor)
            imageView.setImageDrawable(icon)
        }
    }

    override fun onClick() {
        showDialog()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {

    }

    override fun onDismiss(dialog: DialogInterface?) {
        mDialog = null
    }

    private fun getVersionName(context: Context): String {
        val pInfo = context.packageManager?.getPackageInfo(context.packageName, 0)
        if (pInfo != null) {
            var versionName = pInfo.versionName
            val pattern = "^([\\d]+).([\\d]+).([\\d]+).([\\d]{4,})([\\d]{2,})([\\d]{2,})$".toPattern()
            val matcher =pattern.matcher(versionName)
            if (matcher.matches()) {
                versionName = "${matcher.group(1)}.${matcher.group(2)}.${matcher.group(3)}"
                versionName += " (Date: ${matcher.group(6)}/${matcher.group(5)}/${matcher.group(4)})"
            }
            return versionName
        }
        return ""
    }

    private fun updateSummary(context: Context) {
        val versionName = getVersionName(context)
        if (versionName.isNotEmpty()) {
            summary = "Version - $versionName"
        }
    }

    @Suppress("DEPRECATION")
    private fun createDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(dialogTitle)
        builder.setIcon(dialogIcon)

        val messageView = TextView(context)
        val paddingVert = dpToPixel(context, PADDING_VERT)
        val paddingHoriz = dpToPixel(context, PADDING_HORIZ)
        messageView.setPadding(paddingHoriz, paddingVert, paddingHoriz, 0)
        messageView.movementMethod = LinkMovementMethod.getInstance()
        messageView.text = HtmlCompat.fromHtml(dialogMessage.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT)

        val textAppearanceId = android.R.style.TextAppearance_Medium
        if (Build.VERSION.SDK_INT >= 23) {
            messageView.setTextAppearance(textAppearanceId)
        } else {
            messageView.setTextAppearance(context, textAppearanceId)
        }

        val textColor = messageTextColor
        if (textColor != -1) {
            messageView.setTextColor(textColor)
        }

        builder.setView(messageView)

        builder.setPositiveButton(null, null)
        builder.setNegativeButton(android.R.string.ok, this)

        mDialog = builder.create()
    }

    private fun showDialog() {
        if (mDialog == null) {
            createDialog()
        }
        mDialog?.let {
            if (!it.isShowing) {
                it.setOnDismissListener(this)
                it.show()
            }
        }
    }

    companion object {
        private const val PADDING_VERT = 16
        private const val PADDING_HORIZ = 24

        private fun dpToPixel(context: Context, dp: Int): Int {
            val metrics = context.resources.displayMetrics
            val px = dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
            return px.toInt()
        }
    }
}