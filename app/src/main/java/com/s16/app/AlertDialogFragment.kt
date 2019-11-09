package com.s16.app

import android.app.Dialog
import android.content.DialogInterface
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.AdapterView
import android.widget.ListAdapter

import java.lang.ref.WeakReference

import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

/**
 * Created by SMM on 9/20/2016.
 */
open class AlertDialogFragment : DialogFragment(), DialogInterface.OnShowListener {
    private val P: AlertParams
    private var mButtonHandler: ButtonHandler? = null

    val cursorId: String
        get() = P.mCursor!!.getString(P.mCursor!!.getColumnIndex("_id"))

    private class ButtonHandler(dialog: DialogInterface) : Handler() {

        private val mDialog: WeakReference<DialogInterface> = WeakReference(dialog)

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE, DialogInterface.BUTTON_NEUTRAL -> (msg.obj as DialogInterface.OnClickListener).onClick(
                    mDialog.get(),
                    msg.what
                )

                MSG_DISMISS_DIALOG -> (msg.obj as DialogInterface).dismiss()
            }
        }

        companion object {
            // Button clicks have Message.what as the BUTTON{1,2,3} constant
            private const val MSG_DISMISS_DIALOG = 1
        }
    }

    init {
        P = AlertParams()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!showsDialog) {
            return
        }

        val dialog = dialog as AlertDialog
        val view = view
        if (view != null) {
            dialog.setView(view)
        }
        val activity = activity
        if (activity != null) {
            dialog.ownerActivity = activity
        }
        dialog.setCancelable(isCancelable)
        dialog.setOnShowListener(this)
        dialog.setOnCancelListener(this)
        dialog.setOnDismissListener(this)
        if (savedInstanceState != null) {
            val dialogState = savedInstanceState.getBundle(SAVED_DIALOG_STATE_TAG)
            if (dialogState != null) {
                dialog.onRestoreInstanceState(dialogState)
            }
        }
        mButtonHandler = ButtonHandler(dialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!, theme)
        P.apply(builder)
        return builder.create()
    }

    override fun onShow(dialog: DialogInterface) {
        val alertDialog = dialog as AlertDialog
        if (P.mPositiveButtonListener != null) {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                mButtonHandler!!.obtainMessage(
                    AlertDialog.BUTTON_POSITIVE,
                    P.mPositiveButtonListener
                ).sendToTarget()
            }
        }
        if (P.mNeutralButtonListener != null) {
            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                mButtonHandler!!.obtainMessage(
                    AlertDialog.BUTTON_NEUTRAL,
                    P.mNeutralButtonListener
                ).sendToTarget()
            }
        }
        if (P.mNegativeButtonListener != null) {
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                mButtonHandler!!.obtainMessage(
                    AlertDialog.BUTTON_NEGATIVE,
                    P.mNegativeButtonListener
                ).sendToTarget()
            }
        }
    }

    /**
     * Set the title using the given resource id.
     */
    fun setTitle(@StringRes titleId: Int) {
        P.mTitleId = titleId
    }

    /**
     * Set the title displayed in the [Dialog].
     */
    fun setTitle(title: CharSequence) {
        P.mTitle = title
    }

    /**
     * Set the title using the custom view `customTitleView`.
     *
     *
     * The methods [.setTitle] and [.setIcon] should
     * be sufficient for most titles, but this is provided if the title
     * needs more customization. Using this will replace the title and icon
     * set via the other methods.
     *
     *
     * **Note:** To ensure consistent styling, the custom view
     * should be inflated or constructed using the alert dialog's themed
     * context obtained via [.getContext].
     *
     * @param customTitleView the custom view to use as the title
     */
    fun setCustomTitle(customTitleView: View) {
        P.mCustomTitleView = customTitleView
    }

    /**
     * Set the message to onDisplayRebuyList using the given resource id.
     */
    fun setMessage(@StringRes messageId: Int) {
        P.mMessageId = messageId
    }

    /**
     * Set the message to onDisplayRebuyList.
     */
    fun setMessage(message: CharSequence) {
        P.mMessage = message
    }

    /**
     * Set the resource id of the [Drawable] to be used in the title.
     *
     *
     * Takes precedence over values set using [.setIcon].
     */
    fun setIcon(@DrawableRes iconId: Int) {
        P.mIconId = iconId
    }

    /**
     * Set the [Drawable] to be used in the title.
     *
     *
     * **Note:** To ensure consistent styling, the drawable
     * should be inflated or constructed using the alert dialog's themed
     * context obtained via [.getContext].
     */
    fun setIcon(icon: Drawable) {
        P.mIcon = icon
    }

    /**
     * Set an icon as supplied by a theme attribute. e.g.
     * [android.R.attr.alertDialogIcon].
     *
     *
     * Takes precedence over values set using [.setIcon] or
     * [.setIcon].
     *
     * @param attrId ID of a theme attribute that points to a drawable resource.
     */
    fun setIconAttribute(@AttrRes attrId: Int) {
        P.mIconAttrId = attrId
    }

    /**
     * Set a listener to be invoked when the positive image_button of the dialog is pressed.
     * @param textId The resource id of the text to onDisplayRebuyList in the positive image_button
     * @param listener The [DialogInterface.OnClickListener] to use.
     */
    fun setPositiveButton(@StringRes textId: Int, listener: DialogInterface.OnClickListener?) {
        P.mPositiveButtonTextId = textId
        P.mPositiveButtonListener = listener
    }

    /**
     * Set a listener to be invoked when the positive image_button of the dialog is pressed.
     * @param text The text to onDisplayRebuyList in the positive image_button
     * @param listener The [DialogInterface.OnClickListener] to use.
     */
    fun setPositiveButton(text: CharSequence?, listener: DialogInterface.OnClickListener?) {
        P.mPositiveButtonText = text
        P.mPositiveButtonListener = listener
    }

    /**
     * Set a listener to be invoked when the negative image_button of the dialog is pressed.
     * @param textId The resource id of the text to onDisplayRebuyList in the negative image_button
     * @param listener The [DialogInterface.OnClickListener] to use.
     */
    fun setNegativeButton(@StringRes textId: Int, listener: DialogInterface.OnClickListener?) {
        P.mNegativeButtonTextId = textId
        P.mNegativeButtonListener = listener
    }

    /**
     * Set a listener to be invoked when the negative image_button of the dialog is pressed.
     * @param text The text to onDisplayRebuyList in the negative image_button
     * @param listener The [DialogInterface.OnClickListener] to use.
     */
    fun setNegativeButton(text: CharSequence?, listener: DialogInterface.OnClickListener?) {
        P.mNegativeButtonText = text
        P.mNegativeButtonListener = listener
    }

    /**
     * Set a listener to be invoked when the neutral image_button of the dialog is pressed.
     * @param textId The resource id of the text to onDisplayRebuyList in the neutral image_button
     * @param listener The [DialogInterface.OnClickListener] to use.
     */
    fun setNeutralButton(@StringRes textId: Int, listener: DialogInterface.OnClickListener?) {
        P.mNeutralButtonTextId = textId
        P.mNeutralButtonListener = listener
    }

    /**
     * Set a listener to be invoked when the neutral image_button of the dialog is pressed.
     * @param text The text to onDisplayRebuyList in the neutral image_button
     * @param listener The [DialogInterface.OnClickListener] to use.
     */
    fun setNeutralButton(text: CharSequence?, listener: DialogInterface.OnClickListener?) {
        P.mNeutralButtonText = text
        P.mNeutralButtonListener = listener
    }

    /**
     * Sets the callback that will be called if a key is dispatched to the dialog.
     */
    fun setOnKeyListener(onKeyListener: DialogInterface.OnKeyListener) {
        P.mOnKeyListener = onKeyListener
    }

    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of the
     * selected item via the supplied listener. This should be an array type i.e. R.array.foo
     */
    fun setItems(@ArrayRes itemsId: Int, listener: DialogInterface.OnClickListener) {
        P.mItemsId = itemsId
        P.mOnClickListener = listener
    }

    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of the
     * selected item via the supplied listener.
     */
    fun setItems(items: Array<CharSequence>, listener: DialogInterface.OnClickListener) {
        P.mItems = items
        P.mOnClickListener = listener
    }

    /**
     * Set a list of items, which are supplied by the given [ListAdapter], to be
     * displayed in the dialog as the content, you will be notified of the
     * selected item via the supplied listener.
     *
     * @param adapter The [ListAdapter] to supply the list of items
     * @param listener The listener that will be called when an item is clicked.
     */
    fun setAdapter(adapter: ListAdapter, listener: DialogInterface.OnClickListener) {
        P.mAdapter = adapter
        P.mOnClickListener = listener
    }

    /**
     * Set a list of items, which are supplied by the given [Cursor], to be
     * displayed in the dialog as the content, you will be notified of the
     * selected item via the supplied listener.
     *
     * @param cursor The [Cursor] to supply the list of items
     * @param listener The listener that will be called when an item is clicked.
     * @param labelColumn The column name on the cursor containing the string to onDisplayRebuyList
     * in the label.
     */
    fun setCursor(
        cursor: Cursor, listener: DialogInterface.OnClickListener,
        labelColumn: String
    ) {
        P.mCursor = cursor
        P.mLabelColumn = labelColumn
        P.mOnClickListener = listener
    }

    fun getCursorName(columnName: String): String {
        return P.mCursor!!.getString(P.mCursor!!.getColumnIndex(columnName))
    }

    /**
     * Set a list of items to be displayed in the dialog as the content,
     * you will be notified of the selected item via the supplied listener.
     * This should be an array type, e.g. R.array.foo. The list will have
     * a check mark displayed to the right of the text for each checked
     * item. Clicking on an item in the list will not dismiss the dialog.
     * Clicking on a image_button will dismiss the dialog.
     *
     * @param itemsId the resource id of an array i.e. R.array.foo
     * @param checkedItems specifies which items are checked. It should be null in which case no
     * items are checked. If non null it must be exactly the same length as the array of
     * items.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     * dismissed when an item is clicked. It will only be dismissed if clicked on a
     * image_button, if no buttons are supplied it's up to the user to dismiss the dialog.
     */
    fun setMultiChoiceItems(
        @ArrayRes itemsId: Int, checkedItems: BooleanArray,
        listener: DialogInterface.OnMultiChoiceClickListener
    ) {
        P.mItemsId = itemsId
        P.mOnCheckboxClickListener = listener
        P.mCheckedItems = checkedItems
        P.mIsMultiChoice = true
    }

    /**
     * Set a list of items to be displayed in the dialog as the content,
     * you will be notified of the selected item via the supplied listener.
     * The list will have a check mark displayed to the right of the text
     * for each checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a image_button will dismiss the dialog.
     *
     * @param items the text of the items to be displayed in the list.
     * @param checkedItems specifies which items are checked. It should be null in which case no
     * items are checked. If non null it must be exactly the same length as the array of
     * items.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     * dismissed when an item is clicked. It will only be dismissed if clicked on a
     * image_button, if no buttons are supplied it's up to the user to dismiss the dialog.
     */
    fun setMultiChoiceItems(
        items: Array<CharSequence>, checkedItems: BooleanArray,
        listener: DialogInterface.OnMultiChoiceClickListener
    ) {
        P.mItems = items
        P.mOnCheckboxClickListener = listener
        P.mCheckedItems = checkedItems
        P.mIsMultiChoice = true
    }

    /**
     * Set a list of items to be displayed in the dialog as the content,
     * you will be notified of the selected item via the supplied listener.
     * The list will have a check mark displayed to the right of the text
     * for each checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a image_button will dismiss the dialog.
     *
     * @param cursor the cursor used to provide the items.
     * @param isCheckedColumn specifies the column name on the cursor to use to determine
     * whether a checkbox is checked or not. It must return an integer value where 1
     * means checked and 0 means unchecked.
     * @param labelColumn The column name on the cursor containing the string to onDisplayRebuyList in the
     * label.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     * dismissed when an item is clicked. It will only be dismissed if clicked on a
     * image_button, if no buttons are supplied it's up to the user to dismiss the dialog.
     */
    fun setMultiChoiceItems(
        cursor: Cursor, isCheckedColumn: String, labelColumn: String,
        listener: DialogInterface.OnMultiChoiceClickListener
    ) {
        P.mCursor = cursor
        P.mOnCheckboxClickListener = listener
        P.mIsCheckedColumn = isCheckedColumn
        P.mLabelColumn = labelColumn
        P.mIsMultiChoice = true
    }

    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of
     * the selected item via the supplied listener. This should be an array type i.e.
     * R.array.foo The list will have a check mark displayed to the right of the text for the
     * checked item. Clicking on an item in the list will not dismiss the dialog. Clicking on a
     * image_button will dismiss the dialog.
     *
     * @param itemsId the resource id of an array i.e. R.array.foo
     * @param checkedItem specifies which item is checked. If -1 no items are checked.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     * dismissed when an item is clicked. It will only be dismissed if clicked on a
     * image_button, if no buttons are supplied it's up to the user to dismiss the dialog.
     */
    fun setSingleChoiceItems(
        @ArrayRes itemsId: Int, checkedItem: Int,
        listener: DialogInterface.OnClickListener
    ) {
        P.mItemsId = itemsId
        P.mOnClickListener = listener
        P.mCheckedItem = checkedItem
        P.mIsSingleChoice = true
    }

    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of
     * the selected item via the supplied listener. The list will have a check mark displayed to
     * the right of the text for the checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a image_button will dismiss the dialog.
     *
     * @param cursor the cursor to retrieve the items from.
     * @param checkedItem specifies which item is checked. If -1 no items are checked.
     * @param labelColumn The column name on the cursor containing the string to onDisplayRebuyList in the
     * label.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     * dismissed when an item is clicked. It will only be dismissed if clicked on a
     * image_button, if no buttons are supplied it's up to the user to dismiss the dialog.
     */
    fun setSingleChoiceItems(
        cursor: Cursor, checkedItem: Int, labelColumn: String,
        listener: DialogInterface.OnClickListener
    ) {
        P.mCursor = cursor
        P.mOnClickListener = listener
        P.mCheckedItem = checkedItem
        P.mLabelColumn = labelColumn
        P.mIsSingleChoice = true
    }

    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of
     * the selected item via the supplied listener. The list will have a check mark displayed to
     * the right of the text for the checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a image_button will dismiss the dialog.
     *
     * @param items the items to be displayed.
     * @param checkedItem specifies which item is checked. If -1 no items are checked.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     * dismissed when an item is clicked. It will only be dismissed if clicked on a
     * image_button, if no buttons are supplied it's up to the user to dismiss the dialog.
     */
    fun setSingleChoiceItems(items: Array<CharSequence>, checkedItem: Int, listener: DialogInterface.OnClickListener) {
        P.mItems = items
        P.mOnClickListener = listener
        P.mCheckedItem = checkedItem
        P.mIsSingleChoice = true
    }

    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of
     * the selected item via the supplied listener. The list will have a check mark displayed to
     * the right of the text for the checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a image_button will dismiss the dialog.
     *
     * @param adapter The [ListAdapter] to supply the list of items
     * @param checkedItem specifies which item is checked. If -1 no items are checked.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     * dismissed when an item is clicked. It will only be dismissed if clicked on a
     * image_button, if no buttons are supplied it's up to the user to dismiss the dialog.
     */
    fun setSingleChoiceItems(adapter: ListAdapter, checkedItem: Int, listener: DialogInterface.OnClickListener) {
        P.mAdapter = adapter
        P.mOnClickListener = listener
        P.mCheckedItem = checkedItem
        P.mIsSingleChoice = true
    }

    /**
     * Sets a listener to be invoked when an item in the list is selected.
     *
     * @param listener the listener to be invoked
     * @see AdapterView.setOnItemSelectedListener
     */
    fun setOnItemSelectedListener(listener: AdapterView.OnItemSelectedListener) {
        P.mOnItemSelectedListener = listener
    }

    private class AlertParams () {
        var mIconId = 0
        var mIcon: Drawable? = null
        var mIconAttrId = 0
        var mTitleId = 0
        var mTitle: CharSequence? = null
        var mCustomTitleView: View? = null
        var mMessageId = 0
        var mMessage: CharSequence? = null
        var mPositiveButtonTextId = 0
        var mPositiveButtonText: CharSequence? = null
        var mPositiveButtonListener: DialogInterface.OnClickListener? = null
        var mNegativeButtonTextId = 0
        var mNegativeButtonText: CharSequence? = null
        var mNegativeButtonListener: DialogInterface.OnClickListener? = null
        var mNeutralButtonTextId = 0
        var mNeutralButtonText: CharSequence? = null
        var mNeutralButtonListener: DialogInterface.OnClickListener? = null
        var mOnKeyListener: DialogInterface.OnKeyListener? = null
        var mItemsId: Int = 0
        var mItems: Array<CharSequence>? = null
        var mAdapter: ListAdapter? = null
        var mOnClickListener: DialogInterface.OnClickListener? = null
        var mCheckedItems: BooleanArray? = null
        var mIsMultiChoice: Boolean = false
        var mIsSingleChoice: Boolean = false
        var mCheckedItem = -1
        var mOnCheckboxClickListener: DialogInterface.OnMultiChoiceClickListener? = null
        var mCursor: Cursor? = null
        var mLabelColumn: String? = null
        var mIsCheckedColumn: String? = null
        var mOnItemSelectedListener: AdapterView.OnItemSelectedListener? = null

        fun apply(dialog: AlertDialog.Builder) {
            when {
                mIconId != 0 -> dialog.setIcon(mIconId)
                mIcon != null -> dialog.setIcon(mIcon)
                mIconAttrId != 0 -> dialog.setIconAttribute(mIconAttrId)
            }

            when {
                mCustomTitleView != null -> dialog.setCustomTitle(mCustomTitleView)
                mTitleId != 0 -> dialog.setTitle(mTitleId)
                mTitle != null -> dialog.setTitle(mTitle)
            }

            if (mMessageId != 0) {
                dialog.setMessage(mMessageId)
            } else if (mMessage != null) {
                dialog.setMessage(mMessage)
            }

            if (mPositiveButtonTextId != 0) {
                dialog.setPositiveButton(mPositiveButtonTextId, mPositiveButtonListener)
            } else if (mPositiveButtonText != null) {
                dialog.setPositiveButton(mPositiveButtonText, mPositiveButtonListener)
            }
            if (mNegativeButtonTextId != 0) {
                dialog.setNegativeButton(mNegativeButtonTextId, mNegativeButtonListener)
            } else if (mNegativeButtonText != null) {
                dialog.setNegativeButton(mNegativeButtonText, mNegativeButtonListener)
            }
            if (mNeutralButtonTextId != 0) {
                dialog.setNeutralButton(mNeutralButtonTextId, mNeutralButtonListener)
            } else if (mNeutralButtonText != null) {
                dialog.setNeutralButton(mNeutralButtonText, mNeutralButtonListener)
            }

            if (mOnKeyListener != null) {
                dialog.setOnKeyListener(mOnKeyListener)
            }

            if (mIsMultiChoice) {
                when {
                    mItemsId != 0 -> dialog.setMultiChoiceItems(mItemsId, mCheckedItems, mOnCheckboxClickListener)
                    mItems != null -> dialog.setMultiChoiceItems(mItems, mCheckedItems, mOnCheckboxClickListener)
                    mCursor != null -> dialog.setMultiChoiceItems(mCursor, mIsCheckedColumn, mLabelColumn, mOnCheckboxClickListener)
                }

            } else if (mIsSingleChoice) {
                when {
                    mItemsId != 0 -> dialog.setSingleChoiceItems(mItemsId, mCheckedItem, mOnClickListener)
                    mItems != null -> dialog.setSingleChoiceItems(mItems, mCheckedItem, mOnClickListener)
                    mCursor != null -> dialog.setSingleChoiceItems(mCursor, mCheckedItem, mLabelColumn, mOnClickListener)
                    mAdapter != null -> dialog.setSingleChoiceItems(mAdapter, mCheckedItem, mOnClickListener)
                }

            } else {
                when {
                    mItemsId != 0 -> dialog.setItems(mItemsId, mOnClickListener)
                    mItems != null -> dialog.setItems(mItems, mOnClickListener)
                    mCursor != null -> dialog.setCursor(mCursor, mOnClickListener, mLabelColumn)
                    mAdapter != null -> dialog.setAdapter(mAdapter, mOnClickListener)
                }
            }

            if (mOnItemSelectedListener != null) {
                dialog.setOnItemSelectedListener(mOnItemSelectedListener)
            }
        }
    }

    companion object {
        private const val SAVED_DIALOG_STATE_TAG = "android:savedDialogState"
    }
}
