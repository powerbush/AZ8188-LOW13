/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.Sms.Intents;
import android.telephony.SmsMessage;
import android.telephony.gemini.GeminiSmsMessage;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Config;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.telephony.Phone;
import com.android.mms.R;
import com.android.mms.transaction.SmsReceiverService;
import com.android.mms.transaction.MessagingNotification;
import com.mediatek.featureoption.FeatureOption;
import android.database.sqlite.SqliteWrapper;

import com.android.mms.util.Recycler;
/**
 * Display a class-zero SMS message to the user. Wait for the user to dismiss
 * it.
 */
public class ClassZeroActivity extends Activity {
    private static final String BUFFER = "         ";
    private static final int BUFFER_OFFSET = BUFFER.length() * 2;
    private static final String TAG = "display_00";
    private static final int ON_AUTO_SAVE = 1;
    private static final String[] REPLACE_PROJECTION = new String[] { Sms._ID,
            Sms.ADDRESS, Sms.PROTOCOL };
    private static final int REPLACE_COLUMN_ID = 0;

    /** Default timer to dismiss the dialog. */
    private static final long DEFAULT_TIMER = 5 * 60 * 1000;

    /** To remember the exact time when the timer should fire. */
    private static final String TIMER_FIRE = "timer_fire";

    private SmsMessage[] mMessages = null;
    private int mMsgLen = 0;

    /** Is the message read. */
    private boolean mRead = false;

    /** The timer to dismiss the dialog automatically. */
    private long mTimerSet = 0;
    private AlertDialog mDialog = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Do not handle an invalid message.
            if (msg.what == ON_AUTO_SAVE) {
                mRead = false;
                mDialog.dismiss();
                saveMessage();
                finish();
            }
        }
    };

    private void saveMessage() {
        Uri messageUri = null;
        if (mMessages[0].isReplace()) {
            messageUri = replaceMessage(mMessages);
        } else {
            messageUri = storeMessage(mMessages);
        }
        if (!mRead && messageUri != null) {
            MessagingNotification.nonBlockingUpdateNewMessageIndicator(this, true, false);
        }
        cancelMessageNotification();
        Recycler.getSmsRecycler().deleteOldMessages(getApplicationContext());
    }
    private void cancelMessageNotification() {

       MessagingNotification.cancelNotification(this,
             MessagingNotification.CLASS_ZERO_NOTIFICATION_ID);
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(
                R.drawable.class_zero_background);

        Intent intent = getIntent();
        mMessages = Intents.getMessagesFromIntent(intent);
        int SimId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);

        mMsgLen = mMessages.length;
        CharSequence messageChars = null;
        if (1 == mMsgLen){
            messageChars = mMessages[0].getMessageBody();
        } else if (mMsgLen > 1){
            // Build up the body from the parts.
            StringBuilder body = new StringBuilder();
            SmsMessage sms = null;
            for (int i = 0; i < mMsgLen; i++) {
                sms = mMessages[i];
                body.append(sms.getDisplayMessageBody());
            }
            messageChars = body.toString().subSequence(0,body.toString().length());
        }
        
        String message = messageChars.toString();
        //if (TextUtils.isEmpty(message)) {
        //    finish();
        //    return;
        //}
        long now = SystemClock.uptimeMillis();
        
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
        	
        	LayoutInflater inflater = LayoutInflater.from(this);
            ClassZeroView classZeroView = (ClassZeroView)inflater.inflate(R.layout.class_zero_view_gemini, null, false);
            classZeroView.bind(message, SimId);  
            mDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.new_message)
        			.setPositiveButton(R.string.save, mSaveListener)
		            .setNegativeButton(android.R.string.cancel, mCancelListener)
		            .setCancelable(false)
		    		.setView(classZeroView).show();
        } else {
        // TODO: The following line adds an emptry string before and after a message.
        // This is not the correct way to layout a message. This is more of a hack
        // to work-around a bug in AlertDialog. This needs to be fixed later when
        // Android fixes the bug in AlertDialog.
            if (message.length() < BUFFER_OFFSET) message = message + BUFFER;
                
            mDialog = new AlertDialog.Builder(this).setMessage(message)
                    .setTitle(R.string.new_message)
                .setPositiveButton(R.string.save, mSaveListener)
                .setNegativeButton(android.R.string.cancel, mCancelListener)
                .setCancelable(false).show();
        }
        mTimerSet = now + DEFAULT_TIMER;
        if (icicle != null) {
            mTimerSet = icicle.getLong(TIMER_FIRE, mTimerSet);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        long now = SystemClock.uptimeMillis();
        if (mTimerSet <= now) {
            // Save the message if the timer already expired.
            mHandler.sendEmptyMessage(ON_AUTO_SAVE);
        } else {
            mHandler.sendEmptyMessageAtTime(ON_AUTO_SAVE, mTimerSet);
            if (Config.DEBUG) {
                Log.d(TAG, "onRestart time = " + Long.toString(mTimerSet) + " "
                        + this.toString());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(TIMER_FIRE, mTimerSet);
        if (Config.DEBUG) {
            Log.d(TAG, "onSaveInstanceState time = " + Long.toString(mTimerSet)
                    + " " + this.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeMessages(ON_AUTO_SAVE);
        if (Config.DEBUG) {
            Log.d(TAG, "onStop time = " + Long.toString(mTimerSet)
                    + " " + this.toString());
        }
    }

    private final OnClickListener mCancelListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
        	cancelMessageNotification();
            finish();
        }
    };

    private final OnClickListener mSaveListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            mRead = true;
            new Thread(new Runnable() {
                public void run() {
                	saveMessage();
                }
            }).start();
            dialog.dismiss();
            finish();
        }
    };

    private ContentValues extractContentValues(SmsMessage sms) {
        // Store the message in the content provider.
        ContentValues values = new ContentValues();

        values.put(Inbox.ADDRESS, sms.getDisplayOriginatingAddress());

        // Use now for the timestamp to avoid confusion with clock
        // drift between the handset and the SMSC.
        values.put(Inbox.DATE, new Long(System.currentTimeMillis()));
        values.put(Inbox.PROTOCOL, sms.getProtocolIdentifier());
        values.put(Inbox.READ, Integer.valueOf(mRead ? 1 : 0));
        values.put(Inbox.SEEN, Integer.valueOf(mRead ? 1 : 0));
        // add for gemini
        if (FeatureOption.MTK_GEMINI_SUPPORT){
        	values.put(Inbox.SIM_ID, sms.getMessageSimId());
        }
        //
        if (sms.getPseudoSubject().length() > 0) {
            values.put(Inbox.SUBJECT, sms.getPseudoSubject());
        }
        values.put(Inbox.REPLY_PATH_PRESENT, sms.isReplyPathPresent() ? 1 : 0);
        values.put(Inbox.SERVICE_CENTER, sms.getServiceCenterAddress());
        return values;
    }

    private Uri replaceMessage(SmsMessage[] Msgs) {
        ContentValues values = extractContentValues(Msgs[0]);

        String body = "";
        for (int i = 0; i < mMsgLen; ++i) {
            body += Msgs[i].getMessageBody();
        }

        values.put(Inbox.BODY, body);

        ContentResolver resolver = getContentResolver();
        String originatingAddress = Msgs[0].getOriginatingAddress();
        int protocolIdentifier = Msgs[0].getProtocolIdentifier();
        String selection = Sms.ADDRESS + " = ? AND " + Sms.PROTOCOL + " = ?";
        String[] selectionArgs = null;
        // add for gemini
        if (FeatureOption.MTK_GEMINI_SUPPORT){
        	selection += " AND " + Sms.SIM_ID + " = ?";
            selectionArgs = new String[] { originatingAddress,
                    Integer.toString(protocolIdentifier),  
                    Integer.toString(Msgs[0].getMessageSimId())};
        } else {
            selectionArgs = new String[] { originatingAddress,
                Integer.toString(protocolIdentifier) };
        }

        Cursor cursor = SqliteWrapper.query(this, resolver, Inbox.CONTENT_URI,
                REPLACE_PROJECTION, selection, selectionArgs, null);

        try {
            if (cursor.moveToFirst()) {
                long messageId = cursor.getLong(REPLACE_COLUMN_ID);
                Uri messageUri = ContentUris.withAppendedId(
                        Sms.CONTENT_URI, messageId);

                SqliteWrapper.update(this, resolver, messageUri, values,
                        null, null);
                return messageUri;
            }
        } finally {
            cursor.close();
        }
        return storeMessage(Msgs);
    }

    private Uri storeMessage(SmsMessage[] Msgs) {
        // Store the message in the content provider.
        ContentValues values = extractContentValues(Msgs[0]);
        String body = "";
        for (int i = 0; i < mMsgLen; ++i) {
            body += Msgs[i].getDisplayMessageBody();
        }
        values.put(Inbox.BODY, body);
        ContentResolver resolver = getContentResolver();
        if (Config.DEBUG) {
            Log.d(TAG, "storeMessage " + this.toString());
        }
        return SqliteWrapper.insert(this, resolver, Inbox.CONTENT_URI, values);
    }
}

// add for gemini
class ClassZeroView extends LinearLayout {
    private static final String TAG  = "ClassZeroView";
    
    private TextView mMessageView;
    private TextView mTimestamp;
    private Context  mContext;

    public ClassZeroView(Context context) {
        super(context);
        mContext = context;
    }
    
    public ClassZeroView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMessageView = (TextView) findViewById(R.id.body);
        mTimestamp = (TextView) findViewById(R.id.wpms_timestamp);
        

    } 
 
    public void bind(String messageBody, int simId) {
    	Log.i(TAG, "Class zero message:" + messageBody + "; From SIM " + simId);
    	mMessageView.setText(messageBody);
    	SpannableStringBuilder buf = new SpannableStringBuilder();       
        //Add sim info
        int simInfoStart = buf.length();
        CharSequence simInfo = MessageUtils.getSimInfo(mContext, simId);
        if(simInfo.length() > 0) {
        	buf.append(" ");
            buf.append(mContext.getString(R.string.via_without_time_for_recieve));
            buf.append(" ");
            simInfoStart = buf.length();
            buf.append(simInfo);
        }      
        mTimestamp.setText(buf);
    }  
}
