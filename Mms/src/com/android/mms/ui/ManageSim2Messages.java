/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
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
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.ui;

import com.android.mms.R;
import android.database.sqlite.SqliteWrapper;
import com.android.mms.transaction.MessagingNotification;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony.Sms;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.telephony.Phone;
import android.telephony.TelephonyManager;
import com.mediatek.featureoption.FeatureOption;
import com.android.mms.util.Recycler;
import android.app.Dialog;
import android.app.ProgressDialog;
import com.android.mms.data.Contact;
import com.android.internal.telephony.ITelephony;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.widget.Toast;
import com.android.internal.telephony.Phone;
import android.provider.Telephony.SIMInfo;
import com.mediatek.telephony.TelephonyManagerEx;
import android.content.ContentUris;

/**
 * Displays a list of the SMS messages stored on the ICC.
 */
public class ManageSim2Messages extends Activity
        implements View.OnCreateContextMenuListener {
    private static final int DIALOG_REFRESH = 1;        
    private static final Uri ICC_URI_GEMINI = Uri.parse("content://sms/icc2");
    private static final String TAG = "ManageSim2Messages";
    private static final int MENU_COPY_TO_PHONE_MEMORY = 0;
    private static final int MENU_DELETE_FROM_SIM = 1;
    private static final int MENU_VIEW = 2;
    private static final int OPTION_MENU_DELETE_ALL = 0;

    private static final int SHOW_LIST = 0;
    private static final int SHOW_EMPTY = 1;
    private static final int SHOW_BUSY = 2;
    private int mState;
    ProgressDialog dialog;
    private static final String ALL_SMS = "999999";
    private static final String FOR_MULTIDELETE = "ForMultiDelete";

    private ContentResolver mContentResolver;
    private Cursor mCursor = null;
    private ListView mSimList;
    private TextView mMessage;
    private MessageListAdapter mListAdapter = null;
    private AsyncQueryHandler mQueryHandler = null;

    public static final int SIM_FULL_NOTIFICATION_ID = 234;

    public boolean isQuerying = false;
    public boolean isDeleting = false;    
    public static int observerCount = 0; 
    private final ContentObserver simChangeObserver =
            new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfUpdate) {
        Log.e(TAG, "onChange");
            if(!isQuerying){
               refreshMessageList();
            }else{
                if(isDeleting == false){
                    observerCount ++;
                }
            }
        }
    };
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    protected void onCreate(Bundle icicle) {
        Log.e(TAG, "onCreate");    
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        mContentResolver = getContentResolver();
        mQueryHandler = new QueryHandler(mContentResolver, this);
        setContentView(R.layout.sim2_list);
        mSimList = (ListView) findViewById(R.id.messages);
        mMessage = (TextView) findViewById(R.id.empty_message);

        ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
        if(FeatureOption.MTK_GEMINI_SUPPORT == true &&
           !TelephonyManager.getDefault().hasIccCardGemini(Phone.GEMINI_SIM_2)){
            Log.e(TAG, "no sim 2");           
            mSimList.setVisibility(View.GONE);
            mMessage.setText(R.string.no_sim_2);
            mMessage.setVisibility(View.VISIBLE);
            setTitle(getString(R.string.sim_manage_messages_title));
            setProgressBarIndeterminateVisibility(false);
        }else if(FeatureOption.MTK_GEMINI_SUPPORT == true){
            try{
                boolean mIsSim2Ready = iTelephony.isRadioOnGemini(Phone.GEMINI_SIM_2);
                if(!mIsSim2Ready){
                    mSimList.setVisibility(View.GONE);
                    mMessage.setText(com.mediatek.R.string.sim_close);
                    mMessage.setVisibility(View.VISIBLE);
                    setTitle(getString(R.string.sim_manage_messages_title));
                    setProgressBarIndeterminateVisibility(false);
                }else{
                    init();
                }
            }catch(RemoteException e){
                Log.i(TAG, "RemoteException happens......");
            }
        }else{
            init();
        }        
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.e(TAG, "onNewIntent");    
        setIntent(intent);

//        init();
    }

    private void init() {
        Log.e(TAG, "init");            
        updateState(SHOW_BUSY);
        startQuery();
    }

    private class QueryHandler extends AsyncQueryHandler {
        private final ManageSim2Messages mParent;

        public QueryHandler(
                ContentResolver contentResolver, ManageSim2Messages parent) {
            super(contentResolver);
            mParent = parent;
        }

        @Override
        protected void onQueryComplete(
                int token, Object cookie, Cursor cursor) {
            Log.e(TAG, "onQueryComplete");                                    
            if(FeatureOption.MTK_GEMINI_SUPPORT == true){            
                dialog.dismiss();
            }                
            if(isDeleting){
                isDeleting = false;
            }
            if(observerCount > 0){
                ManageSim2Messages.this.startQuery();
                observerCount = 0;
                return;
            }else{
                isQuerying = false;
            }
            if (mCursor != null && !mCursor.isClosed()) {
                mCursor.close();
            }
            mCursor = cursor;
            if (mCursor != null) {
                Log.e(TAG, "onQueryComplete, mCursor != null");                                    
                if (!mCursor.moveToFirst()) {
                    // Let user know the SIM is empty
                    updateState(SHOW_EMPTY);
                } else if (mListAdapter == null) {
                    // Note that the MessageListAdapter doesn't support auto-requeries. If we
                    // want to respond to changes we'd need to add a line like:
                    //   mListAdapter.setOnDataSetChangedListener(mDataSetChangedListener);
                    // See ComposeMessageActivity for an example.
                    mListAdapter = new MessageListAdapter(
                            mParent, mCursor, mSimList, false, null);
                    mSimList.setAdapter(mListAdapter);
                    mSimList.setOnCreateContextMenuListener(mParent);
                    updateState(SHOW_LIST);
                } else {
                    mListAdapter.changeCursor(mCursor);
                    updateState(SHOW_LIST);
                }
                startManagingCursor(mCursor);
                registerSimChangeObserver();
            } else {
                Log.e(TAG, "onQueryComplete, mCursor == null");
                // Let user know the SIM is empty
                updateState(SHOW_EMPTY);
            }
        }
    }


    @Override 
    protected Dialog onCreateDialog(int id){
        switch(id){
            case DIALOG_REFRESH: {
                dialog = new ProgressDialog(this);
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.setMessage(getString(R.string.refreshing));
                return dialog;
            }
        }
        return null;
    }

    private void startQuery() {
        Log.e(TAG, "startQuery");                            
        if(FeatureOption.MTK_GEMINI_SUPPORT == true){
            showDialog(DIALOG_REFRESH);
        }

        try {
            Log.e(TAG, "onQueryComplete, startQuery");            
            isQuerying = true;
            mQueryHandler.startQuery(0, null, ICC_URI_GEMINI, null, null, null, null);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    private void refreshMessageList() {
        Log.e(TAG, "refreshMessageList");                                    
        updateState(SHOW_BUSY);
        if (mCursor != null) {
            stopManagingCursor(mCursor);
            // mCursor.close();
        }
        startQuery();
    }

    @Override
    public void onCreateContextMenu(
            ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, MENU_COPY_TO_PHONE_MEMORY, 0,
                 R.string.sim_copy_to_phone_memory);
        menu.add(0, MENU_DELETE_FROM_SIM, 0, R.string.sim_delete);

        // TODO: Enable this once viewMessage is written.
        // menu.add(0, MENU_VIEW, 0, R.string.sim_view);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException exception) {
            Log.e(TAG, "Bad menuInfo.", exception);
            return false;
        }

        final Cursor cursor = (Cursor) mListAdapter.getItem(info.position);

        switch (item.getItemId()) {
            case MENU_COPY_TO_PHONE_MEMORY:
                copyToPhoneMemory(cursor);
                return true;
            case MENU_DELETE_FROM_SIM:
                final String msgIndex = getMsgIndexByCursor(cursor);
                confirmDeleteDialog(new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updateState(SHOW_BUSY);
                        new Thread(new Runnable() {
                            public void run() {
                                deleteFromSim(msgIndex);
                            }
                        }, "ManageSim2Messages").start();
                        dialog.dismiss();
                    }
                }, R.string.confirm_delete_SIM_message);
                return true;
            case MENU_VIEW:
                viewMessage(cursor);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");                                        
        super.onResume();
        registerSimChangeObserver();
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");                                            
        super.onPause();
        //invalidate cache to refresh contact data        
        Contact.invalidateCache();        
        mContentResolver.unregisterContentObserver(simChangeObserver);
    }

    private void registerSimChangeObserver() {
        mContentResolver.registerContentObserver(
                ICC_URI_GEMINI, true, simChangeObserver);
    }

    private void copyToPhoneMemory(Cursor cursor) {
        String address = cursor.getString(
                cursor.getColumnIndexOrThrow("address"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        Long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));

        try {
            if (isIncomingMessage(cursor)) {
                Log.d("MMSLog", "Coty incoming sms to phone");
                Log.d("MMSLog", "\t address \t=" + address);
                Log.d("MMSLog", "\t body \t=" + body);
                Log.d("MMSLog", "\t date \t=" + date);
                
                if(FeatureOption.MTK_GEMINI_SUPPORT == true){
                    SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(this, Phone.GEMINI_SIM_2);
                    if (simInfo != null) {
                        Sms.Inbox.addMessage(mContentResolver, address, body, null, date, true, (int)simInfo.mSimId);
                    } else {
                        Sms.Inbox.addMessage(mContentResolver, address, body, null, date, true, -1);
                    }
                }else{
                    Sms.Inbox.addMessage(mContentResolver, address, body, null, date, true);
                }
            } else {
                // outgoing sms has not date info
                date = System.currentTimeMillis();
                Log.d("MMSLog", "Coty outgoing sms to phone");
                Log.d("MMSLog", "\t address \t=" + address);
                Log.d("MMSLog", "\t body \t=" + body);
                Log.d("MMSLog", "\t date \t=" + date);
                
                if(FeatureOption.MTK_GEMINI_SUPPORT == true){
                    SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(this, Phone.GEMINI_SIM_2);
                    if (simInfo != null) {
                        Sms.Sent.addMessage(mContentResolver, address, body, null, date, (int)simInfo.mSimId);
                    } else {
                        Sms.Sent.addMessage(mContentResolver, address, body, null, date, -1);
                    }
                }else{
                    Sms.Sent.addMessage(mContentResolver, address, body, null, date);
                }
            }
            Recycler.getSmsRecycler().deleteOldMessages(getApplicationContext());            
            
            String msg = getString(R.string.done);
            Toast.makeText(ManageSim2Messages.this, msg, Toast.LENGTH_SHORT).show();
            
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }

    private boolean isIncomingMessage(Cursor cursor) {
        int messageStatus = cursor.getInt(
                cursor.getColumnIndexOrThrow("status"));
        //if(messageStatus == -1) {
        //    return true;
        //}
        Log.d("MMSLog", "message status:" + messageStatus);
        return (messageStatus == SmsManager.STATUS_ON_ICC_READ) ||
               (messageStatus == SmsManager.STATUS_ON_ICC_UNREAD);
    }

    private String getMsgIndexByCursor(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow("index_on_icc"));
    }

    private void deleteFromSim(String msgIndex) {
        /* 1. Non-Concatenated SMS's message index string is like "1"
         * 2. Concatenated SMS's message index string is like "1;2;3;".
         * 3. If a concatenated SMS only has one segment stored in SIM Card, its message 
         *    index string is like "1;".
         */
        String[] index = msgIndex.split(";");
        Uri simUri = ICC_URI_GEMINI.buildUpon().build();
        if (SqliteWrapper.delete(this, mContentResolver, simUri, FOR_MULTIDELETE, index) == 1) {
            MessagingNotification.cancelNotification(getApplicationContext(),
                    SIM_FULL_NOTIFICATION_ID);
        }
        isDeleting = true;
    }

    private void deleteFromSim(Cursor cursor) {
        String msgIndex = getMsgIndexByCursor(cursor);
        deleteFromSim(msgIndex);
    }

    private void deleteAllFromSim() {
        // For Delete all,MTK FW support delete all using messageIndex = -1, here use 999999 instead of -1;
        String messageIndexString = ALL_SMS;;
        //cursor.getString(cursor.getColumnIndexOrThrow("index_on_icc"));
        Uri simUri = ICC_URI_GEMINI.buildUpon().appendPath(messageIndexString).build();
        Log.i(TAG, "delete simUri: " + simUri);
        if (SqliteWrapper.delete(this, mContentResolver, simUri, null, null) == 1) {
            MessagingNotification.cancelNotification(getApplicationContext(),
                    SIM_FULL_NOTIFICATION_ID);
        }
        isDeleting = true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if ((null != mCursor) && (mCursor.getCount() > 0) && mState == SHOW_LIST) {
            menu.add(0, OPTION_MENU_DELETE_ALL, 0, R.string.menu_delete_messages).setIcon(
                    android.R.drawable.ic_menu_delete);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case OPTION_MENU_DELETE_ALL:
                confirmDeleteDialog(new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updateState(SHOW_BUSY);
                        deleteAllFromSim();
                        dialog.dismiss();
                    }
                }, R.string.confirm_delete_all_SIM_messages);
                break;
        }

        return true;
    }

    private void confirmDeleteDialog(OnClickListener listener, int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_dialog_title);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.yes, listener);
        builder.setNegativeButton(R.string.no, null);
        builder.setMessage(messageId);

        builder.show();
    }

    private void updateState(int state) {
        Log.e(TAG, "updateState, state = "+ state);            
        if (mState == state) {
            return;
        }

        mState = state;
        switch (state) {
            case SHOW_LIST:
                mSimList.setVisibility(View.VISIBLE);
                mSimList.requestFocus();
                mSimList.setSelection(mSimList.getCount()-1);
                mMessage.setVisibility(View.GONE);
                setTitle(getString(R.string.sim_manage_messages_title));
                setProgressBarIndeterminateVisibility(false);
                break;
            case SHOW_EMPTY:
                mSimList.setVisibility(View.GONE);
                mMessage.setVisibility(View.VISIBLE);
                setTitle(getString(R.string.sim_manage_messages_title));
                setProgressBarIndeterminateVisibility(false);
                break;
            case SHOW_BUSY:
                mSimList.setVisibility(View.GONE);
                mMessage.setVisibility(View.GONE);
                setTitle(getString(R.string.refreshing));
                setProgressBarIndeterminateVisibility(true);
                break;
            default:
                Log.e(TAG, "Invalid State");
        }
    }

    private void viewMessage(Cursor cursor) {
        // TODO: Add this.
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode)
    {
        if (null != intent && null != intent.getData()
                && intent.getData().getScheme().equals("mailto")) {
            try {
                super.startActivityForResult(intent, requestCode);
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "Failed to startActivityForResult: " + intent);
                Intent i = new Intent().setClassName("com.android.email", "com.android.email.activity.setup.AccountSetupBasics");
                this.startActivity(i);
                finish();
            } catch (Exception e) {
                Log.e(TAG, "Failed to startActivityForResult: " + intent);
                Toast.makeText(this,getString(R.string.message_open_email_fail),
                      Toast.LENGTH_SHORT).show();
          }
        } else {
            super.startActivityForResult(intent, requestCode);
        }
    }
}

