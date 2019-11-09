package com.s16.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.text.NumberFormat;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ProgressDialog extends AlertDialog {

    /**
     * Creates a ProgressDialog with a circular, spinning progress
     * bar. This is the default.
     */
    public static final int STYLE_SPINNER = 0;

    /**
     * Creates a ProgressDialog with a horizontal progress bar.
     */
    public static final int STYLE_HORIZONTAL = 1;

    private ProgressBar mProgress;
    private AppCompatTextView mMessageView;

    private int mProgressStyle = STYLE_SPINNER;
    private AppCompatTextView mProgressNumber;
    private String mProgressNumberFormat;
    private AppCompatTextView mProgressPercent;
    private NumberFormat mProgressPercentFormat;

    private int mMax;
    private int mProgressVal;
    private int mSecondaryProgressVal;
    private int mIncrementBy;
    private int mIncrementSecondaryBy;
    private Drawable mProgressDrawable;
    private Drawable mIndeterminateDrawable;
    private CharSequence mMessage;
    private boolean mIndeterminate;

    private boolean mHasStarted;
    private Handler mViewUpdateHandler;

    private int[] COLOR_ATTRS = new int[] {
            android.support.design.R.attr.colorAccent,
            android.support.design.R.attr.colorPrimary,
            android.support.design.R.attr.colorPrimaryDark
    };

    /**
     * Creates a Progress dialog.
     *
     * @param context the parent context
     */
    public ProgressDialog(@NonNull Context context) {
        super(context);
        initFormats();
    }

    /**
     * Creates a Progress dialog.
     *
     * @param context the parent context
     * @param theme the resource ID of the theme against which to inflate
     *              this dialog, or {@code 0} to use the parent
     *              {@code context}'s default alert dialog theme
     */
    public ProgressDialog(@NonNull Context context, int theme) {
        super(context, theme);
        initFormats();
    }

    private void initFormats() {
        mProgressNumberFormat = "%1d/%2d";
        mProgressPercentFormat = NumberFormat.getPercentInstance();
        mProgressPercentFormat.setMaximumFractionDigits(0);
    }

    /**
     * Creates and shows a ProgressDialog.
     *
     * @param context the parent context
     * @param title the title text for the dialog's window
     * @param message the text to be displayed in the dialog
     * @return the ProgressDialog
     */
    public static ProgressDialog show(Context context, CharSequence title,
                                      CharSequence message) {
        return show(context, title, message, false);
    }

    /**
     * Creates and shows a ProgressDialog.
     *
     * @param context the parent context
     * @param title the title text for the dialog's window
     * @param message the text to be displayed in the dialog
     * @param indeterminate true if the dialog should be {@link #setIndeterminate(boolean)
     *        indeterminate}, false otherwise
     * @return the ProgressDialog
     */
    public static ProgressDialog show(Context context, CharSequence title,
                                      CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    /**
     * Creates and shows a ProgressDialog.
     *
     * @param context the parent context
     * @param title the title text for the dialog's window
     * @param message the text to be displayed in the dialog
     * @param indeterminate true if the dialog should be {@link #setIndeterminate(boolean)
     *        indeterminate}, false otherwise
     * @param cancelable true if the dialog is {@link #setCancelable(boolean) cancelable},
     *        false otherwise
     * @return the ProgressDialog
     */
    public static ProgressDialog show(Context context, CharSequence title,
                                      CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }
    /**
     * Creates and shows a ProgressDialog.
     *
     * @param context the parent context
     * @param title the title text for the dialog's window
     * @param message the text to be displayed in the dialog
     * @param indeterminate true if the dialog should be {@link #setIndeterminate(boolean)
     *        indeterminate}, false otherwise
     * @param cancelable true if the dialog is {@link #setCancelable(boolean) cancelable},
     *        false otherwise
     * @param cancelListener the {@link #setOnCancelListener(OnCancelListener) listener}
     *        to be invoked when the dialog is canceled
     * @return the ProgressDialog
     */
    public static ProgressDialog show(Context context, CharSequence title,
                                      CharSequence message, boolean indeterminate,
                                      boolean cancelable, OnCancelListener cancelListener) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }

    private int dpToPixel(int dp){
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int)px;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        LayoutInflater inflater = LayoutInflater.from(getContext());
//        TypedArray a = getContext().obtainStyledAttributes(null,
//                com.android.internal.R.styleable.AlertDialog,
//                com.android.internal.R.attr.alertDialogStyle, 0);

        TypedArray a = getContext().obtainStyledAttributes(COLOR_ATTRS);

        if (mProgressStyle == STYLE_HORIZONTAL) {

            /* Use a separate handler to update the text views as they
             * must be updated on the same thread that created them.
             */
            mViewUpdateHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    /* Update the number and percent */
                    int progress = mProgress.getProgress();
                    int max = mProgress.getMax();
                    if (mProgressNumberFormat != null) {
                        String format = mProgressNumberFormat;
                        mProgressNumber.setText(String.format(format, progress, max));
                    } else {
                        mProgressNumber.setText("");
                    }
                    if (mProgressPercentFormat != null) {
                        double percent = (double) progress / (double) max;
                        SpannableString tmp = new SpannableString(mProgressPercentFormat.format(percent));
                        tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                                0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mProgressPercent.setText(tmp);
                    } else {
                        mProgressPercent.setText("");
                    }
                }
            };

//            View view = inflater.inflate(a.getResourceId(
//                    com.android.internal.R.styleable.AlertDialog_horizontalProgressLayout,
//                    R.layout.alert_dialog_progress), null);
//            mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
//            mProgressNumber = (TextView) view.findViewById(R.id.progress_number);
//            mProgressPercent = (TextView) view.findViewById(R.id.progress_percent);
//            setView(view);

            View view = createAlertDialogProgress(a);
            setView(view);

        } else {
//            View view = inflater.inflate(a.getResourceId(
//                    com.android.internal.R.styleable.AlertDialog_progressLayout,
//                    R.layout.progress_dialog), null);
//            mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
//            mMessageView = (TextView) view.findViewById(android.R.id.message);
//            setView(view);

            View view = createProgressDialog(a);
            setView(view);
        }
        a.recycle();

        if (mMax > 0) {
            setMax(mMax);
        }
        if (mProgressVal > 0) {
            setProgress(mProgressVal);
        }
        if (mSecondaryProgressVal > 0) {
            setSecondaryProgress(mSecondaryProgressVal);
        }
        if (mIncrementBy > 0) {
            incrementProgressBy(mIncrementBy);
        }
        if (mIncrementSecondaryBy > 0) {
            incrementSecondaryProgressBy(mIncrementSecondaryBy);
        }
        if (mProgressDrawable != null) {
            setProgressDrawable(mProgressDrawable);
        }
        if (mIndeterminateDrawable != null) {
            setIndeterminateDrawable(mIndeterminateDrawable);
        }
        if (mMessage != null) {
            setMessage(mMessage);
        }
        setIndeterminate(mIndeterminate);
        onProgressChanged();
        super.onCreate(savedInstanceState);
    }

    private int getColorPrimary() {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.support.design.R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    private ViewGroup createAlertDialogProgress(TypedArray a) {
        RelativeLayout rootView = new RelativeLayout(getContext());
        rootView.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT));

        mProgress = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        mProgress.setId(android.R.id.progress);
        RelativeLayout.LayoutParams progressParams = new RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        progressParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        progressParams.topMargin = dpToPixel(12);
        progressParams.bottomMargin = dpToPixel(1);
        progressParams.leftMargin = dpToPixel(10);
        progressParams.rightMargin = dpToPixel(10);
        mProgress.setLayoutParams(progressParams);
        rootView.addView(mProgress);

        mProgressNumber = new AppCompatTextView(getContext());
        RelativeLayout.LayoutParams numberParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        numberParams.leftMargin = dpToPixel(10);
        numberParams.rightMargin = dpToPixel(10);
        numberParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        numberParams.addRule(RelativeLayout.BELOW, android.R.id.progress);
        mProgressNumber.setLayoutParams(numberParams);
        mProgressNumber.setPadding(0, 0, 0, dpToPixel(12));
        rootView.addView(mProgressNumber);

        mProgressPercent = new AppCompatTextView(getContext());
        RelativeLayout.LayoutParams percentParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        percentParams.leftMargin = dpToPixel(10);
        percentParams.rightMargin = dpToPixel(10);
        percentParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        percentParams.addRule(RelativeLayout.BELOW, android.R.id.progress);
        mProgressPercent.setLayoutParams(percentParams);
        mProgressPercent.setPadding(0, 0, 0, dpToPixel(12));
        rootView.addView(mProgressPercent);

        return rootView;
    }

    private ViewGroup createProgressDialog(TypedArray a) {
        FrameLayout rootView = new FrameLayout(getContext());
        rootView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        LinearLayout contentView = new LinearLayout(getContext());
        contentView.setOrientation(LinearLayout.HORIZONTAL);
        contentView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        contentView.setBaselineAligned(false);
        contentView.setPadding(dpToPixel(8), dpToPixel(10), dpToPixel(8), dpToPixel(10));

        mProgress = new ProgressBar(getContext(), null, android.R.attr.progressBarStyle);
        mProgress.setId(android.R.id.progress);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        progressParams.rightMargin = dpToPixel(12);
        mProgress.setLayoutParams(progressParams);
        mProgress.setMax(10000);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CircularProgressDrawable progressDrawable = new CircularProgressDrawable(getContext());
            progressDrawable.setColorSchemeColors(a.getColor(1, getColorPrimary()));
            progressDrawable.setCenterRadius((float) dpToPixel(16));
            progressDrawable.setStrokeWidth((float) dpToPixel(4));
            mProgress.setIndeterminateDrawable(progressDrawable);
        }
        contentView.addView(mProgress);

        mMessageView = new AppCompatTextView(getContext());
        mMessageView.setId(android.R.id.message);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        messageParams.gravity = Gravity.CENTER_VERTICAL;
        mMessageView.setLayoutParams(messageParams);
        contentView.addView(mMessageView);

        rootView.addView(contentView);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mHasStarted = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHasStarted = false;
    }
    /**
     * Sets the current progress.
     *
     * @param value the current progress, a value between 0 and {@link #getMax()}
     *
     * @see ProgressBar#setProgress(int)
     */
    public void setProgress(int value) {
        if (mHasStarted) {
            mProgress.setProgress(value);
            onProgressChanged();
        } else {
            mProgressVal = value;
        }
    }
    /**
     * Sets the secondary progress.
     *
     * @param secondaryProgress the current secondary progress, a value between 0 and
     * {@link #getMax()}
     *
     * @see ProgressBar#setSecondaryProgress(int)
     */
    public void setSecondaryProgress(int secondaryProgress) {
        if (mProgress != null) {
            mProgress.setSecondaryProgress(secondaryProgress);
            onProgressChanged();
        } else {
            mSecondaryProgressVal = secondaryProgress;
        }
    }
    /**
     * Gets the current progress.
     *
     * @return the current progress, a value between 0 and {@link #getMax()}
     */
    public int getProgress() {
        if (mProgress != null) {
            return mProgress.getProgress();
        }
        return mProgressVal;
    }
    /**
     * Gets the current secondary progress.
     *
     * @return the current secondary progress, a value between 0 and {@link #getMax()}
     */
    public int getSecondaryProgress() {
        if (mProgress != null) {
            return mProgress.getSecondaryProgress();
        }
        return mSecondaryProgressVal;
    }
    /**
     * Gets the maximum allowed progress value. The default value is 100.
     *
     * @return the maximum value
     */
    public int getMax() {
        if (mProgress != null) {
            return mProgress.getMax();
        }
        return mMax;
    }
    /**
     * Sets the maximum allowed progress value.
     */
    public void setMax(int max) {
        if (mProgress != null) {
            mProgress.setMax(max);
            onProgressChanged();
        } else {
            mMax = max;
        }
    }
    /**
     * Increments the current progress value.
     *
     * @param diff the amount by which the current progress will be incremented,
     * up to {@link #getMax()}
     */
    public void incrementProgressBy(int diff) {
        if (mProgress != null) {
            mProgress.incrementProgressBy(diff);
            onProgressChanged();
        } else {
            mIncrementBy += diff;
        }
    }
    /**
     * Increments the current secondary progress value.
     *
     * @param diff the amount by which the current secondary progress will be incremented,
     * up to {@link #getMax()}
     */
    public void incrementSecondaryProgressBy(int diff) {
        if (mProgress != null) {
            mProgress.incrementSecondaryProgressBy(diff);
            onProgressChanged();
        } else {
            mIncrementSecondaryBy += diff;
        }
    }
    /**
     * Sets the drawable to be used to display the progress value.
     *
     * @param d the drawable to be used
     *
     * @see ProgressBar#setProgressDrawable(Drawable)
     */
    public void setProgressDrawable(Drawable d) {
        if (mProgress != null) {
            mProgress.setProgressDrawable(d);
        } else {
            mProgressDrawable = d;
        }
    }
    /**
     * Sets the drawable to be used to display the indeterminate progress value.
     *
     * @param d the drawable to be used
     *
     * @see ProgressBar#setProgressDrawable(Drawable)
     * @see #setIndeterminate(boolean)
     */
    public void setIndeterminateDrawable(Drawable d) {
        if (mProgress != null) {
            mProgress.setIndeterminateDrawable(d);
        } else {
            mIndeterminateDrawable = d;
        }
    }
    /**
     * Change the indeterminate mode for this ProgressDialog. In indeterminate
     * mode, the progress is ignored and the dialog shows an infinite
     * animation instead.
     *
     * <p><strong>Note:</strong> A ProgressDialog with style {@link #STYLE_SPINNER}
     * is always indeterminate and will ignore this setting.</p>
     *
     * @param indeterminate true to enable indeterminate mode, false otherwise
     *
     * @see #setProgressStyle(int)
     */
    public void setIndeterminate(boolean indeterminate) {
        if (mProgress != null) {
            mProgress.setIndeterminate(indeterminate);
        } else {
            mIndeterminate = indeterminate;
        }
    }
    /**
     * Whether this ProgressDialog is in indeterminate mode.
     *
     * @return true if the dialog is in indeterminate mode, false otherwise
     */
    public boolean isIndeterminate() {
        if (mProgress != null) {
            return mProgress.isIndeterminate();
        }
        return mIndeterminate;
    }

    @Override
    public void setMessage(CharSequence message) {
        if (mProgress != null) {
            if (mProgressStyle == STYLE_HORIZONTAL) {
                super.setMessage(message);
            } else {
                mMessageView.setText(message);
            }
        } else {
            mMessage = message;
        }
    }
    /**
     * Sets the style of this ProgressDialog, either {@link #STYLE_SPINNER} or
     * {@link #STYLE_HORIZONTAL}. The default is {@link #STYLE_SPINNER}.
     *
     * <p><strong>Note:</strong> A ProgressDialog with style {@link #STYLE_SPINNER}
     * is always indeterminate and will ignore the {@link #setIndeterminate(boolean)
     * indeterminate} setting.</p>
     *
     * @param style the style of this ProgressDialog, either {@link #STYLE_SPINNER} or
     * {@link #STYLE_HORIZONTAL}
     */
    public void setProgressStyle(int style) {
        mProgressStyle = style;
    }
    /**
     * Change the format of the small text showing current and maximum units
     * of progress.  The default is "%1d/%2d".
     * Should not be called during the number is progressing.
     * @param format A string passed to {@link String#format String.format()};
     * use "%1d" for the current number and "%2d" for the maximum.  If null,
     * nothing will be shown.
     */
    public void setProgressNumberFormat(String format) {
        mProgressNumberFormat = format;
        onProgressChanged();
    }
    /**
     * Change the format of the small text showing the percentage of progress.
     * The default is
     * {@link NumberFormat#getPercentInstance() NumberFormat.getPercentageInstnace().}
     * Should not be called during the number is progressing.
     * @param format An instance of a {@link NumberFormat} to generate the
     * percentage text.  If null, nothing will be shown.
     */
    public void setProgressPercentFormat(NumberFormat format) {
        mProgressPercentFormat = format;
        onProgressChanged();
    }

    private void onProgressChanged() {
        if (mProgressStyle == STYLE_HORIZONTAL) {
            if (mViewUpdateHandler != null && !mViewUpdateHandler.hasMessages(0)) {
                mViewUpdateHandler.sendEmptyMessage(0);
            }
        }
    }
}
