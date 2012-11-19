package com.android.contacts;

import java.text.NumberFormat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * <p>
 * A dialog showing a progress indicator and an optional text message or view.
 * Only a text message or a view can be used at the same time.
 * </p>
 * <p>
 * The dialog can be made cancelable on back key press.
 * </p>
 * <p>
 * The progress range is 0..10000.
 * </p>
 */
public class CustomProgressDialog extends AlertDialog {

    /**
     * Creates a ProgressDialog with a ciruclar, spinning progress bar. This is
     * the default.
     */
    public static final int STYLE_SPINNER = 0;

    /**
     * Creates a ProgressDialog with a horizontal progress bar.
     */
    public static final int STYLE_HORIZONTAL = 1;

    private ProgressBar mProgress;
    private TextView mMessageView;
    private TextView mProgressSubTitleView;
    private TextView mProgressSubMsgLine1;
    private TextView mProgressSubMsgLine2;

    private String mProgressSubTitle;
    private String mProgressSubMsg1;
    private String mProgressSubMsg2;

    private int mProgressStyle = STYLE_SPINNER;
    private TextView mProgressNumber;
    private String mProgressNumberFormat;
    private TextView mProgressPercent;
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

    private Context mContext;

    public CustomProgressDialog(Context context) {
        this(context, com.android.internal.R.style.Theme_Dialog_Alert);
        mContext = context;
    }

    public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
	}

    public static CustomProgressDialog show(Context context,
            CharSequence title, CharSequence message) {
        return show(context, title, message, false);
    }

    public static CustomProgressDialog show(Context context,
            CharSequence title, CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static CustomProgressDialog show(Context context,
            CharSequence title, CharSequence message, boolean indeterminate,
            boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static CustomProgressDialog show(Context context,
            CharSequence title, CharSequence message, boolean indeterminate,
            boolean cancelable, OnCancelListener cancelListener) {
        CustomProgressDialog dialog = new CustomProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (mProgressStyle == STYLE_HORIZONTAL) {

            /*
             * Use a separate handler to update the text views as they must be
             * updated on the same thread that created them.
             */
            mViewUpdateHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    /* Update the number and percent */
                    int progress = mProgress.getProgress();
                    int max = mProgress.getMax();
                    double percent = (double) progress / (double) max;
                    String format = mProgressNumberFormat;
                    mProgressNumber.setText(String
                            .format(format, progress, max));
                    SpannableString tmp = new SpannableString(
                            mProgressPercentFormat.format(percent));
                    tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, tmp.length(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mProgressPercent.setText(tmp);
                }
            };
            View view = inflater.inflate(R.layout.custom_progress_dialog, null);
            mProgress = (ProgressBar) view.findViewById(R.id.progress);
            mProgressNumber = (TextView) view
                    .findViewById(R.id.progress_number);
            mProgressNumber.setTextColor(android.graphics.Color.argb(255, 190, 190, 190));
            mProgressNumberFormat = "%d/%d";
            mProgressPercent = (TextView) view
                    .findViewById(R.id.progress_percent);
            mProgressPercent.setTextColor(android.graphics.Color.argb(255, 190, 190, 190));
            mProgressPercentFormat = NumberFormat.getPercentInstance();
            mProgressPercentFormat.setMaximumFractionDigits(0);
            mProgressSubTitleView = (TextView)view.findViewById(R.id.progress_subTitle);
            mProgressSubTitleView.setTextColor(android.graphics.Color.WHITE);
            mProgressSubMsgLine1 = (TextView)view.findViewById(R.id.progress_subMsgLine1);
            mProgressSubMsgLine1.setTextColor(android.graphics.Color.WHITE);
            mProgressSubMsgLine2 = (TextView)view.findViewById(R.id.progress_subMsgLine2);
            mProgressSubMsgLine2.setTextColor(android.graphics.Color.WHITE);
            setView(view);
        }
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

        if (mProgressSubMsg1 != null) {
            setProgressSubMsg1(mProgressSubMsg1);
        }

        if (mProgressSubMsg2 != null) {
            setProgressSubMsg2(mProgressSubMsg2);
        }

        if (this.mProgressSubTitle != null) {
            this.setProgressSubTilte(mProgressSubTitle);
        }

        setIndeterminate(mIndeterminate);
        onProgressChanged();
        super.onCreate(savedInstanceState);
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

    public void setProgress(int value) {
        if (mHasStarted) {
            mProgress.setProgress(value);
            onProgressChanged();
        } else {
            mProgressVal = value;
        }
    }

    public void setSecondaryProgress(int secondaryProgress) {
        if (mProgress != null) {
            mProgress.setSecondaryProgress(secondaryProgress);
            onProgressChanged();
        } else {
            mSecondaryProgressVal = secondaryProgress;
        }
    }

    public int getProgress() {
        if (mProgress != null) {
            return mProgress.getProgress();
        }
        return mProgressVal;
    }

    public int getSecondaryProgress() {
        if (mProgress != null) {
            return mProgress.getSecondaryProgress();
        }
        return mSecondaryProgressVal;
    }

    public int getMax() {
        if (mProgress != null) {
            return mProgress.getMax();
        }
        return mMax;
    }

    public void setMax(int max) {
        if (mProgress != null) {
            mProgress.setMax(max);
            onProgressChanged();
        } else {
            mMax = max;
        }
    }

    public void incrementProgressBy(int diff) {
        if (mProgress != null) {
            mProgress.incrementProgressBy(diff);
            onProgressChanged();
        } else {
            mIncrementBy += diff;
        }
    }

    public void incrementSecondaryProgressBy(int diff) {
        if (mProgress != null) {
            mProgress.incrementSecondaryProgressBy(diff);
            onProgressChanged();
        } else {
            mIncrementSecondaryBy += diff;
        }
    }

    public void setProgressDrawable(Drawable d) {
        if (mProgress != null) {
            mProgress.setProgressDrawable(d);
        } else {
            mProgressDrawable = d;
        }
    }

    public void setIndeterminateDrawable(Drawable d) {
        if (mProgress != null) {
            mProgress.setIndeterminateDrawable(d);
        } else {
            mIndeterminateDrawable = d;
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        if (mProgress != null) {
            mProgress.setIndeterminate(indeterminate);
        } else {
            mIndeterminate = indeterminate;
        }
    }

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

    @Override
    public boolean onSearchRequested() {
        return false;
    }

    public void setProgressStyle(int style) {
        mProgressStyle = style;
    }

    /**
     * Change the format of Progress Number. The default is "current/max".
     * Should not be called during the number is progressing.
     * 
     * @param format
     *            Should contain two "%d". The first is used for current number
     *            and the second is used for the maximum.
     * @hide
     */
    public void setProgressNumberFormat(String format) {
        mProgressNumberFormat = format;
    }

    public void setProgressSubTilte(String subTitle) {
        this.mProgressSubTitle = subTitle;
        if (this.mProgressSubTitleView != null) {
            this.mProgressSubTitleView.setText(mProgressSubTitle);
        }
    }

    public void setProgressSubMsg1(String subMsg1) {
        this.mProgressSubMsg1 = subMsg1;
        if (this.mProgressSubMsgLine1 != null) {
            this.mProgressSubMsgLine1.setText(subMsg1);
        }
    }

    public void setProgressSubMsg2(String subMsg2) {
        this.mProgressSubMsg2 = subMsg2;
        if (this.mProgressSubMsgLine2 != null) {
            this.mProgressSubMsgLine2.setText(subMsg2);
        }
    }

    private void onProgressChanged() {
        if (mProgressStyle == STYLE_HORIZONTAL) {
            mViewUpdateHandler.sendEmptyMessage(0);
        }
    }
}
