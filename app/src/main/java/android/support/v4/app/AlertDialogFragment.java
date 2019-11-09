package android.support.v4.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

/**
 * Created by SMM on 9/20/2016.
 */
public class AlertDialogFragment extends DialogFragment {

    private static final String SAVED_DIALOG_STATE_TAG = "android:savedDialogState";
    private final AlertParams P;

    public AlertDialogFragment() {
        P = new AlertParams();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!getShowsDialog()) {
            return;
        }

        final AlertDialog dialog = (AlertDialog)getDialog();
        View view = getView();
        if (view != null) {
            dialog.setView(view);
        }
        final Activity activity = getActivity();
        if (activity != null) {
            dialog.setOwnerActivity(activity);
        }
        dialog.setCancelable(isCancelable());
        dialog.setOnCancelListener(this);
        dialog.setOnDismissListener(this);
        if (savedInstanceState != null) {
            Bundle dialogState = savedInstanceState.getBundle(SAVED_DIALOG_STATE_TAG);
            if (dialogState != null) {
                dialog.onRestoreInstanceState(dialogState);
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), getTheme());
        P.apply(builder);
        return builder.create();
    }

    /**
     * Set the title using the given resource id.
     */
    public void setTitle(@StringRes int titleId) {
        P.mTitleId = titleId;
    }

    /**
     * Set the title displayed in the {@link Dialog}.
     */
    public void setTitle(CharSequence title) {
        P.mTitle = title;
    }

    /**
     * Set the title using the custom view {@code customTitleView}.
     * <p>
     * The methods {@link #setTitle(int)} and {@link #setIcon(int)} should
     * be sufficient for most titles, but this is provided if the title
     * needs more customization. Using this will replace the title and icon
     * set via the other methods.
     * <p>
     * <strong>Note:</strong> To ensure consistent styling, the custom view
     * should be inflated or constructed using the alert dialog's themed
     * context obtained via {@link #getContext()}.
     *
     * @param customTitleView the custom view to use as the title
     */
    public void setCustomTitle(View customTitleView) {
        P.mCustomTitleView = customTitleView;
    }

    /**
     * Set the message to display using the given resource id.
     */
    public void setMessage(@StringRes int messageId) {
        P.mMessageId = messageId;
    }

    /**
     * Set the message to display.
     */
    public void setMessage(CharSequence message) {
        P.mMessage = message;
    }

    /**
     * Set the resource id of the {@link Drawable} to be used in the title.
     * <p>
     * Takes precedence over values set using {@link #setIcon(Drawable)}.
     */
    public void setIcon(@DrawableRes int iconId) {
        P.mIconId = iconId;
    }

    /**
     * Set the {@link Drawable} to be used in the title.
     * <p>
     * <strong>Note:</strong> To ensure consistent styling, the drawable
     * should be inflated or constructed using the alert dialog's themed
     * context obtained via {@link #getContext()}.
     */
    public void setIcon(Drawable icon) {
        P.mIcon = icon;
    }

    /**
     * Set an icon as supplied by a theme attribute. e.g.
     * {@link android.R.attr#alertDialogIcon}.
     * <p>
     * Takes precedence over values set using {@link #setIcon(int)} or
     * {@link #setIcon(Drawable)}.
     *
     * @param attrId ID of a theme attribute that points to a drawable resource.
     */
    public void setIconAttribute(@AttrRes int attrId) {
        P.mIconAttrId = attrId;
    }

    /**
     * Set a listener to be invoked when the positive button of the dialog is pressed.
     * @param textId The resource id of the text to display in the positive button
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     */
    public void setPositiveButton(@StringRes int textId, final DialogInterface.OnClickListener listener) {
        P.mPositiveButtonTextId = textId;
        P.mPositiveButtonListener = listener;
    }

    /**
     * Set a listener to be invoked when the positive button of the dialog is pressed.
     * @param text The text to display in the positive button
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     */
    public void setPositiveButton(CharSequence text, final DialogInterface.OnClickListener listener) {
        P.mPositiveButtonText = text;
        P.mPositiveButtonListener = listener;
    }

    /**
     * Set a listener to be invoked when the negative button of the dialog is pressed.
     * @param textId The resource id of the text to display in the negative button
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     */
    public void setNegativeButton(@StringRes int textId, final DialogInterface.OnClickListener listener) {
        P.mNegativeButtonTextId = textId;
        P.mNegativeButtonListener = listener;
    }

    /**
     * Set a listener to be invoked when the negative button of the dialog is pressed.
     * @param text The text to display in the negative button
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     */
    public void setNegativeButton(CharSequence text, final DialogInterface.OnClickListener listener) {
        P.mNegativeButtonText = text;
        P.mNegativeButtonListener = listener;
    }

    /**
     * Set a listener to be invoked when the neutral button of the dialog is pressed.
     * @param textId The resource id of the text to display in the neutral button
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     */
    public void setNeutralButton(@StringRes int textId, final DialogInterface.OnClickListener listener) {
        P.mNeutralButtonTextId = textId;
        P.mNeutralButtonListener = listener;
    }

    /**
     * Set a listener to be invoked when the neutral button of the dialog is pressed.
     * @param text The text to display in the neutral button
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     */
    public void setNeutralButton(CharSequence text, final DialogInterface.OnClickListener listener) {
        P.mNeutralButtonText = text;
        P.mNeutralButtonListener = listener;
    }

    /**
     * Sets the callback that will be called if a key is dispatched to the dialog.
     */
    public void setOnKeyListener(DialogInterface.OnKeyListener onKeyListener) {
        P.mOnKeyListener = onKeyListener;
    }

    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of the
     * selected item via the supplied listener. This should be an array type i.e. R.array.foo
     */
    public void setItems(@ArrayRes int itemsId, final DialogInterface.OnClickListener listener) {
        P.mItemsId = itemsId;
        P.mOnClickListener = listener;
    }

    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of the
     * selected item via the supplied listener.
     */
    public void setItems(CharSequence[] items, final DialogInterface.OnClickListener listener) {
        P.mItems = items;
        P.mOnClickListener = listener;
    }

    /**
     * Set a list of items, which are supplied by the given {@link ListAdapter}, to be
     * displayed in the dialog as the content, you will be notified of the
     * selected item via the supplied listener.
     *
     * @param adapter The {@link ListAdapter} to supply the list of items
     * @param listener The listener that will be called when an item is clicked.
     */
    public void setAdapter(final ListAdapter adapter, final DialogInterface.OnClickListener listener) {
        P.mAdapter = adapter;
        P.mOnClickListener = listener;
    }

    /**
     * Set a list of items, which are supplied by the given {@link Cursor}, to be
     * displayed in the dialog as the content, you will be notified of the
     * selected item via the supplied listener.
     *
     * @param cursor The {@link Cursor} to supply the list of items
     * @param listener The listener that will be called when an item is clicked.
     * @param labelColumn The column name on the cursor containing the string to display
     *          in the label.
     */
    public void setCursor(final Cursor cursor, final DialogInterface.OnClickListener listener,
                             String labelColumn) {
        P.mCursor = cursor;
        P.mLabelColumn = labelColumn;
        P.mOnClickListener = listener;
    }

    /**
     * Set a list of items to be displayed in the dialog as the content,
     * you will be notified of the selected item via the supplied listener.
     * This should be an array type, e.g. R.array.foo. The list will have
     * a check mark displayed to the right of the text for each checked
     * item. Clicking on an item in the list will not dismiss the dialog.
     * Clicking on a button will dismiss the dialog.
     *
     * @param itemsId the resource id of an array i.e. R.array.foo
     * @param checkedItems specifies which items are checked. It should be null in which case no
     *        items are checked. If non null it must be exactly the same length as the array of
     *        items.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     *        dismissed when an item is clicked. It will only be dismissed if clicked on a
     *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
     */
    public void setMultiChoiceItems(@ArrayRes int itemsId, boolean[] checkedItems,
                                       final DialogInterface.OnMultiChoiceClickListener listener) {
        P.mItemsId = itemsId;
        P.mOnCheckboxClickListener = listener;
        P.mCheckedItems = checkedItems;
        P.mIsMultiChoice = true;
    }

    /**
     * Set a list of items to be displayed in the dialog as the content,
     * you will be notified of the selected item via the supplied listener.
     * The list will have a check mark displayed to the right of the text
     * for each checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a button will dismiss the dialog.
     *
     * @param items the text of the items to be displayed in the list.
     * @param checkedItems specifies which items are checked. It should be null in which case no
     *        items are checked. If non null it must be exactly the same length as the array of
     *        items.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     *        dismissed when an item is clicked. It will only be dismissed if clicked on a
     *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
     */
    public void setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems,
                                       final DialogInterface.OnMultiChoiceClickListener listener) {
        P.mItems = items;
        P.mOnCheckboxClickListener = listener;
        P.mCheckedItems = checkedItems;
        P.mIsMultiChoice = true;
    }

    /**
     * Set a list of items to be displayed in the dialog as the content,
     * you will be notified of the selected item via the supplied listener.
     * The list will have a check mark displayed to the right of the text
     * for each checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a button will dismiss the dialog.
     *
     * @param cursor the cursor used to provide the items.
     * @param isCheckedColumn specifies the column name on the cursor to use to determine
     *        whether a checkbox is checked or not. It must return an integer value where 1
     *        means checked and 0 means unchecked.
     * @param labelColumn The column name on the cursor containing the string to display in the
     *        label.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     *        dismissed when an item is clicked. It will only be dismissed if clicked on a
     *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
     */
    public void setMultiChoiceItems(Cursor cursor, String isCheckedColumn, String labelColumn,
                                       final DialogInterface.OnMultiChoiceClickListener listener) {
        P.mCursor = cursor;
        P.mOnCheckboxClickListener = listener;
        P.mIsCheckedColumn = isCheckedColumn;
        P.mLabelColumn = labelColumn;
        P.mIsMultiChoice = true;
    }

    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of
     * the selected item via the supplied listener. This should be an array type i.e.
     * R.array.foo The list will have a check mark displayed to the right of the text for the
     * checked item. Clicking on an item in the list will not dismiss the dialog. Clicking on a
     * button will dismiss the dialog.
     *
     * @param itemsId the resource id of an array i.e. R.array.foo
     * @param checkedItem specifies which item is checked. If -1 no items are checked.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     *        dismissed when an item is clicked. It will only be dismissed if clicked on a
     *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
     */
    public void setSingleChoiceItems(@ArrayRes int itemsId, int checkedItem,
                                        final DialogInterface.OnClickListener listener) {
        P.mItemsId = itemsId;
        P.mOnClickListener = listener;
        P.mCheckedItem = checkedItem;
        P.mIsSingleChoice = true;
    }

    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of
     * the selected item via the supplied listener. The list will have a check mark displayed to
     * the right of the text for the checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a button will dismiss the dialog.
     *
     * @param cursor the cursor to retrieve the items from.
     * @param checkedItem specifies which item is checked. If -1 no items are checked.
     * @param labelColumn The column name on the cursor containing the string to display in the
     *        label.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     *        dismissed when an item is clicked. It will only be dismissed if clicked on a
     *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
     */
    public void setSingleChoiceItems(Cursor cursor, int checkedItem, String labelColumn,
                                        final DialogInterface.OnClickListener listener) {
        P.mCursor = cursor;
        P.mOnClickListener = listener;
        P.mCheckedItem = checkedItem;
        P.mLabelColumn = labelColumn;
        P.mIsSingleChoice = true;
    }

    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of
     * the selected item via the supplied listener. The list will have a check mark displayed to
     * the right of the text for the checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a button will dismiss the dialog.
     *
     * @param items the items to be displayed.
     * @param checkedItem specifies which item is checked. If -1 no items are checked.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     *        dismissed when an item is clicked. It will only be dismissed if clicked on a
     *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
     */
    public void setSingleChoiceItems(CharSequence[] items, int checkedItem, final DialogInterface.OnClickListener listener) {
        P.mItems = items;
        P.mOnClickListener = listener;
        P.mCheckedItem = checkedItem;
        P.mIsSingleChoice = true;
    }

    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of
     * the selected item via the supplied listener. The list will have a check mark displayed to
     * the right of the text for the checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a button will dismiss the dialog.
     *
     * @param adapter The {@link ListAdapter} to supply the list of items
     * @param checkedItem specifies which item is checked. If -1 no items are checked.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     *        dismissed when an item is clicked. It will only be dismissed if clicked on a
     *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
     */
    public void setSingleChoiceItems(ListAdapter adapter, int checkedItem, final DialogInterface.OnClickListener listener) {
        P.mAdapter = adapter;
        P.mOnClickListener = listener;
        P.mCheckedItem = checkedItem;
        P.mIsSingleChoice = true;
    }

    /**
     * Sets a listener to be invoked when an item in the list is selected.
     *
     * @param listener the listener to be invoked
     * @see AdapterView#setOnItemSelectedListener(android.widget.AdapterView.OnItemSelectedListener)
     */
    public void setOnItemSelectedListener(final AdapterView.OnItemSelectedListener listener) {
        P.mOnItemSelectedListener = listener;
    }

    private static class AlertParams {
        public int mIconId = 0;
        public Drawable mIcon;
        public int mIconAttrId = 0;
        public int mTitleId = 0;
        public CharSequence mTitle;
        public View mCustomTitleView;
        public int mMessageId = 0;
        public CharSequence mMessage;
        public int mPositiveButtonTextId = 0;
        public CharSequence mPositiveButtonText;
        public DialogInterface.OnClickListener mPositiveButtonListener;
        public int mNegativeButtonTextId = 0;
        public CharSequence mNegativeButtonText;
        public DialogInterface.OnClickListener mNegativeButtonListener;
        public int mNeutralButtonTextId = 0;
        public CharSequence mNeutralButtonText;
        public DialogInterface.OnClickListener mNeutralButtonListener;
        public DialogInterface.OnKeyListener mOnKeyListener;
        public int mItemsId;
        public CharSequence[] mItems;
        public ListAdapter mAdapter;
        public DialogInterface.OnClickListener mOnClickListener;
        public boolean[] mCheckedItems;
        public boolean mIsMultiChoice;
        public boolean mIsSingleChoice;
        public int mCheckedItem = -1;
        public DialogInterface.OnMultiChoiceClickListener mOnCheckboxClickListener;
        public Cursor mCursor;
        public String mLabelColumn;
        public String mIsCheckedColumn;
        public AdapterView.OnItemSelectedListener mOnItemSelectedListener;

        protected AlertParams() {

        }

        protected void apply(AlertDialog.Builder dialog) {
            if (mIconId != 0) {
                dialog.setIcon(mIconId);
            } else if (mIcon != null) {
                dialog.setIcon(mIcon);
            } else if (mIconAttrId != 0) {
                dialog.setIconAttribute(mIconAttrId);
            }

            if (mCustomTitleView != null) {
                dialog.setCustomTitle(mCustomTitleView);
            } else if (mTitleId != 0) {
                dialog.setTitle(mTitleId);
            } else if (mTitle != null) {
                dialog.setTitle(mTitle);
            }

            if (mMessageId != 0) {
                dialog.setMessage(mMessageId);
            } else if (mMessage != null) {
                dialog.setMessage(mMessage);
            }

            if (mPositiveButtonTextId != 0) {
                dialog.setPositiveButton(mPositiveButtonTextId, mPositiveButtonListener);
            } else if (mPositiveButtonText != null) {
                dialog.setPositiveButton(mPositiveButtonText, mPositiveButtonListener);
            }
            if (mNegativeButtonTextId != 0) {
                dialog.setNegativeButton(mNegativeButtonTextId, mNegativeButtonListener);
            } else if (mNegativeButtonText != null) {
                dialog.setNegativeButton(mNegativeButtonText, mNegativeButtonListener);
            }
            if (mNeutralButtonTextId != 0) {
                dialog.setNeutralButton(mNeutralButtonTextId, mNeutralButtonListener);
            } else if (mNeutralButtonText != null) {
                dialog.setNegativeButton(mNeutralButtonText, mNeutralButtonListener);
            }

            if (mOnKeyListener != null) {
                dialog.setOnKeyListener(mOnKeyListener);
            }

            if (mIsMultiChoice) {
                if (mItemsId != 0) {
                    dialog.setMultiChoiceItems(mItemsId, mCheckedItems, mOnCheckboxClickListener);
                } else if (mItems != null) {
                    dialog.setMultiChoiceItems(mItems, mCheckedItems, mOnCheckboxClickListener);
                } else if (mCursor != null) {
                    dialog.setMultiChoiceItems(mCursor, mIsCheckedColumn, mLabelColumn, mOnCheckboxClickListener);
                }

            } else if (mIsSingleChoice) {
                if (mItemsId != 0) {
                    dialog.setSingleChoiceItems(mItemsId, mCheckedItem, mOnClickListener);
                } else if (mItems != null) {
                    dialog.setSingleChoiceItems(mItems, mCheckedItem, mOnClickListener);
                } else if (mCursor != null) {
                    dialog.setSingleChoiceItems(mCursor, mCheckedItem, mLabelColumn, mOnClickListener);
                } else if (mAdapter != null) {
                    dialog.setSingleChoiceItems(mAdapter, mCheckedItem, mOnClickListener);
                }

            } else {
                if (mItemsId != 0) {
                    dialog.setItems(mItemsId, mOnClickListener);
                } else if (mItems != null) {
                    dialog.setItems(mItems, mOnClickListener);
                } else if (mCursor != null) {
                    dialog.setCursor(mCursor, mOnClickListener, mLabelColumn);
                } else if (mAdapter != null) {
                    dialog.setAdapter(mAdapter, mOnClickListener);
                }
            }

            if (mOnItemSelectedListener != null) {
                dialog.setOnItemSelectedListener(mOnItemSelectedListener);
            }
        }
    }
}
