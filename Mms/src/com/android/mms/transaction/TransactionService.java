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
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.transaction;

import com.android.common.NetworkConnectivityListener;
import com.android.mms.R;
import com.android.mms.LogTag;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.RateController;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPersister;
import com.android.internal.telephony.Phone;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.SIMInfo;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

// add for gemini
import com.mediatek.featureoption.FeatureOption;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.Settings;
import android.database.sqlite.SqliteWrapper;
import com.android.mms.MmsApp;
import android.provider.Settings;


/**
 * The TransactionService of the MMS Client is responsible for handling requests
 * to initiate client-transactions sent from:
 * <ul>
 * <li>The Proxy-Relay (Through Push messages)</li>
 * <li>The composer/viewer activities of the MMS Client (Through intents)</li>
 * </ul>
 * The TransactionService runs locally in the same process as the application.
 * It contains a HandlerThread to which messages are posted from the
 * intent-receivers of this application.
 * <p/>
 * <b>IMPORTANT</b>: This is currently the only instance in the system in
 * which simultaneous connectivity to both the mobile data network and
 * a Wi-Fi network is allowed. This makes the code for handling network
 * connectivity somewhat different than it is in other applications. In
 * particular, we want to be able to send or receive MMS messages when
 * a Wi-Fi connection is active (which implies that there is no connection
 * to the mobile data network). This has two main consequences:
 * <ul>
 * <li>Testing for current network connectivity ({@link android.net.NetworkInfo#isConnected()} is
 * not sufficient. Instead, the correct test is for network availability
 * ({@link android.net.NetworkInfo#isAvailable()}).</li>
 * <li>If the mobile data network is not in the connected state, but it is available,
 * we must initiate setup of the mobile data connection, and defer handling
 * the MMS transaction until the connection is established.</li>
 * </ul>
 */
public class TransactionService extends Service implements Observer {
    private static final String TAG = "TransactionService";

    /**
     * Used to identify notification intents broadcasted by the
     * TransactionService when a Transaction is completed.
     */
    public static final String TRANSACTION_COMPLETED_ACTION =
            "android.intent.action.TRANSACTION_COMPLETED_ACTION";

    /**
     * Action for the Intent which is sent by Alarm service to launch
     * TransactionService.
     */
    public static final String ACTION_ONALARM = "android.intent.action.ACTION_ONALARM";

    /**
     * Used as extra key in notification intents broadcasted by the TransactionService
     * when a Transaction is completed (TRANSACTION_COMPLETED_ACTION intents).
     * Allowed values for this key are: TransactionState.INITIALIZED,
     * TransactionState.SUCCESS, TransactionState.FAILED.
     */
    public static final String STATE = "state";

    /**
     * Used as extra key in notification intents broadcasted by the TransactionService
     * when a Transaction is completed (TRANSACTION_COMPLETED_ACTION intents).
     * Allowed values for this key are any valid content uri.
     */
    public static final String STATE_URI = "uri";

    /**
     * Used to identify notification intents broadcasted by the
     * TransactionService when a Transaction is Start. add for gemini smart 
     */
    public static final String TRANSACTION_START = "com.android.mms.transaction.START";

    /**
     * Used to identify notification intents broadcasted by the
     * TransactionService when a Transaction is Stop. add for gemini smart
     */
    public static final String TRANSACTION_STOP = "com.android.mms.transaction.STOP";

    private static final int EVENT_TRANSACTION_REQUEST = 1;
    private static final int EVENT_DATA_STATE_CHANGED = 2;
    private static final int EVENT_CONTINUE_MMS_CONNECTIVITY = 3;
    private static final int EVENT_HANDLE_NEXT_PENDING_TRANSACTION = 4;
    private static final int EVENT_QUIT = 100;

    private static final int TOAST_MSG_QUEUED = 1;
    private static final int TOAST_DOWNLOAD_LATER = 2;
    private static final int TOAST_NONE = -1;

    private static final int FAILE_TYPE_PERMANENT = 1;
    private static final int FAILE_TYPE_TEMPORARY = 2;

    private static final int REQUEST_SIM_NONE = -1;

    // temp for distinguish smart switch or dialog
    private static final boolean SMART = true;

    // 
    private static boolean bWaitingConxn = false;

    // How often to extend the use of the MMS APN while a transaction
    // is still being processed.
    private static final int APN_EXTENSION_WAIT = 30 * 1000;

    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    private final ArrayList<Transaction> mProcessing  = new ArrayList<Transaction>();
    private final ArrayList<Transaction> mPending  = new ArrayList<Transaction>();
    private ConnectivityManager mConnMgr;
    private NetworkConnectivityListener mConnectivityListener;
    private PowerManager.WakeLock mWakeLock;

    private long triggerMsgId = 0;
    
    private int mMaxServiceId = Integer.MIN_VALUE;

    // add for gemini
    private int mSimIdForEnd = 0;

    // for handling framework sticky intent issue
    private Intent mFWStickyIntent = null;

    public Handler mToastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String str = null;

            if (msg.what == TOAST_MSG_QUEUED) {
                str = getString(R.string.message_queued);
            } else if (msg.what == TOAST_DOWNLOAD_LATER) {
                str = getString(R.string.download_later);
            }

            if (str != null) {
                Toast.makeText(TransactionService.this, str,
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onCreate() {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "Creating TransactionService");
        }

        if (MmsApp.DEBUG) {
            Log.d("MMSLog", "Creating Transaction Service");
        }

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        HandlerThread thread = new HandlerThread("TransactionService");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mConnectivityListener = new NetworkConnectivityListener();
        mConnectivityListener.registerHandler(mServiceHandler, EVENT_DATA_STATE_CHANGED);
        mConnectivityListener.startListening(this);
        mFWStickyIntent = mConnectivityListener.getStickyIntent();
        if (MmsApp.DEBUG && mFWStickyIntent != null) {
            Log.d("MMSLog", "Sticky Intent would be received");
        }
    }

/*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("MMSLog", "onStartCommand");
        if (intent == null) {
            return Service.START_NOT_STICKY;
        }
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean noNetwork = !isNetworkAvailable();

        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "onStart: #" + startId + ": " + intent.getExtras() + " intent=" + intent);
            Log.v(TAG, "    networkAvailable=" + !noNetwork);
        }

        if (ACTION_ONALARM.equals(intent.getAction()) || (intent.getExtras() == null)) {
            // Scan database to find all pending operations.
            Cursor cursor = PduPersister.getPduPersister(this).getPendingMessages(
                    System.currentTimeMillis());
            if (cursor != null) {
                try {
                    int count = cursor.getCount();
                    Log.v("MMSLog", "onStartCommand: Pending Message Size="+count);

                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "onStart: cursor.count=" + count);
                    }

                    if (count == 0) {
                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "onStart: no pending messages. Stopping service.");
                        }
                        RetryScheduler.setRetryAlarm(this);
                        stopSelfIfIdle(startId);
                        return Service.START_NOT_STICKY;
                    }

                    int columnIndexOfMsgId = cursor.getColumnIndexOrThrow(PendingMessages.MSG_ID);
                    int columnIndexOfMsgType = cursor.getColumnIndexOrThrow(
                            PendingMessages.MSG_TYPE);

                    // add for gemini :do not check network state
                    if (FeatureOption.MTK_GEMINI_SUPPORT == false) {
                        if (noNetwork) {
                            // Make sure we register for connection state changes.
                            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                                Log.v(TAG, "onStart: registerForConnectionStateChanges");
                            }
                            MmsSystemEventReceiver.registerForConnectionStateChanges(
                                    getApplicationContext());
                        }
                    }

                    while (cursor.moveToNext()) {
                        // TODO: compare sim_id in intent with the one saved in Transaction
                        int msgType = cursor.getInt(columnIndexOfMsgType);
                        int transactionType = getTransactionType(msgType);

                        // add for gemini :do not check network state
                        if (FeatureOption.MTK_GEMINI_SUPPORT == false) {
                            if (noNetwork) {
                                onNetworkUnavailable(startId, transactionType);
                                return Service.START_NOT_STICKY;
                            }
                        }
                        
                        switch (transactionType) {
                            case -1:
                                break;
                            case Transaction.RETRIEVE_TRANSACTION:
                                // If it's a transiently failed transaction,
                                // we should retry it in spite of current
                                // downloading mode.
                                int failureType = cursor.getInt(
                                        cursor.getColumnIndexOrThrow(
                                                PendingMessages.ERROR_TYPE));
                                if (!isTransientFailure(failureType)) {
                                    break;
                                }
                                // fall-through
                            default:
                                Uri uri = ContentUris.withAppendedId(
                                        Mms.CONTENT_URI,
                                        cursor.getLong(columnIndexOfMsgId));
                                TransactionBundle args = new TransactionBundle(
                                        transactionType, uri.toString());
                                // FIXME: We use the same startId for all MMs.
                                launchTransaction(startId, args, false);
                                break;
                        }
                    }
                } finally {
                    cursor.close();
                }
            } else {
                if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                    Log.v(TAG, "onStart: no pending messages. Stopping service.");
                }
                RetryScheduler.setRetryAlarm(this);
                stopSelfIfIdle(startId);
            }
        } else {
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "onStart: launch transaction...");
            }
            // For launching NotificationTransaction and test purpose.
            TransactionBundle args = null;
            if(FeatureOption.MTK_GEMINI_SUPPORT == true) {
                args = new TransactionBundle(intent.getIntExtra(TransactionBundle.TRANSACTION_TYPE, 0), 
                                             intent.getStringExtra(TransactionBundle.URI));
                // for gemini, do not cear noNetwork param
                launchTransactionGemini(startId, intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1), args);
            }else {
                args = new TransactionBundle(intent.getExtras());
                launchTransaction(startId, args, noNetwork);
            }
            
        }
        return Service.START_NOT_STICKY;
    }
*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (MmsApp.DEBUG) {
            Log.d("MMSLog", "onStartCommand");
        }
        if (intent == null) {
            return Service.START_NOT_STICKY;
        }
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean noNetwork = !isNetworkAvailable();

        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "onStart: #" + startId + ": " + intent.getExtras() + " intent=" + intent);
            Log.v(TAG, "    networkAvailable=" + !noNetwork);
        }

        Uri uri = null;
        String str = intent.getStringExtra(TransactionBundle.URI);
        if (null != str) {
            if (MmsApp.DEBUG) {
                Log.d("MMSLog", "onStartCommand, URI in Bundle.");
            }
            uri = Uri.parse(str);
            if (null != uri) {
                triggerMsgId = ContentUris.parseId(uri);
                if (MmsApp.DEBUG) {
                    Log.d("MMSLog", "Trigger Message ID = " + triggerMsgId);
                }
            }
        }
        
        mMaxServiceId = (startId > mMaxServiceId)?startId:mMaxServiceId;

        if (ACTION_ONALARM.equals(intent.getAction()) || (intent.getExtras() == null)) {
            if (ACTION_ONALARM.equals(intent.getAction())) {
                if (MmsApp.DEBUG) {
                    Log.d("MMSLog", "onStartCommand: ACTION_ONALARM");
                }
            } else {
                if (MmsApp.DEBUG) {
                    Log.d("MMSLog", "onStartCommand: Intent has no Extras data.");
                }
            }
            // add for gemini
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (SMART) {
                    return scanPendingMessagesGemini(startId, noNetwork, -1);
                } else {
                    // 0: no data connect, 1:sim1,  2:sim2
                    int simId = Settings.System.getInt(getContentResolver(), Settings.System.GPRS_CONNECTION_SETTING, Settings.System.GPRS_CONNECTION_SETTING_DEFAULT); 
                    Log.v("MMSLog", "onStartCommand:  0:no data connect, 1:sim1,  2:sim2,  current="+simId);
                    if (0 != simId) {
                        return scanPendingMessagesGemini(startId, noNetwork, simId-1);
                    }
                }
            } else {
                return scanPendingMessages(startId, noNetwork);
            }
        } else {
            // For launching NotificationTransaction and test purpose.
            TransactionBundle args = null;
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                args = new TransactionBundle(intent.getIntExtra(TransactionBundle.TRANSACTION_TYPE, 0), 
                                             intent.getStringExtra(TransactionBundle.URI));
                // 1. for gemini, do not cear noNetwork param
                // 2. check URI
                if (null != intent.getStringExtra(TransactionBundle.URI)) {
                    int simId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1);
                    if (-1 != simId) {
                        launchTransactionGemini(startId, simId, args);
                    } else {
                        // for handling third party 
                        long connectSimId = Settings.System.getLong(getContentResolver(), Settings.System.GPRS_CONNECTION_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET); 
                        Log.v("MMSLog", "onStartCommand before launch transaction:  current data settings: " + connectSimId);
                        if (Settings.System.DEFAULT_SIM_NOT_SET != connectSimId && Settings.System.GPRS_CONNECTION_SIM_SETTING_NEVER != connectSimId) {
                            launchTransactionGemini(startId, (int)connectSimId, args);
                        }  
                    }
                }

                // handle pending message
                if (SMART) {
                    if ((Transaction.SEND_TRANSACTION == args.getTransactionType()||Transaction.RETRIEVE_TRANSACTION == args.getTransactionType()) 
                        && mPending.size() == 0) {
                        return scanPendingMessagesGemini(startId, noNetwork, intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1));
                    }
                } else {
                    if (Transaction.SEND_TRANSACTION == args.getTransactionType() && mPending.size() == 0) {
                        return scanPendingMessagesGemini(startId, noNetwork, intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1));
                    }
                }
                
            }else {
                args = new TransactionBundle(intent.getExtras());
                launchTransaction(startId, args, noNetwork);

                // add by gx, handle pending message
                if (Transaction.SEND_TRANSACTION == args.getTransactionType() && mPending.size() == 0) {
                    return scanPendingMessages(startId, noNetwork);
                }
            }
        }
        return Service.START_NOT_STICKY;
    }

    private int scanPendingMessages(int startId, boolean noNetwork) {
        // Scan database to find all pending operations.
        Cursor cursor = PduPersister.getPduPersister(this).getPendingMessages(
                //System.currentTimeMillis());
                SystemClock.elapsedRealtime());
        if (cursor != null) {
            try {
                int count = cursor.getCount();
                if (MmsApp.DEBUG) {
                    Log.d("MMSLog", "scanPendingMessages: Pending Message Size=" + count);
                }
                if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                    Log.v(TAG, "scanPendingMessages: cursor.count=" + count);
                }

                if (count == 0 && triggerMsgId == 0) {
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "scanPendingMessages: no pending messages. Stopping service.");
                    }
                    RetryScheduler.setRetryAlarm(this);
                    stopSelfIfIdle(startId);
                    return Service.START_NOT_STICKY;
                }

                int columnIndexOfMsgId = cursor.getColumnIndexOrThrow(PendingMessages.MSG_ID);
                int columnIndexOfMsgType = cursor.getColumnIndexOrThrow(
                        PendingMessages.MSG_TYPE);

                if (noNetwork) {
                    // Make sure we register for connection state changes.
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "scanPendingMessages: registerForConnectionStateChanges");
                    }
                    MmsSystemEventReceiver.registerForConnectionStateChanges(
                            getApplicationContext());
                }
                int msgType = 0;
                int transactionType = 0;
                while (cursor.moveToNext()) {
                    msgType = cursor.getInt(columnIndexOfMsgType);
                    transactionType = getTransactionType(msgType);
                    if (noNetwork) {
                        onNetworkUnavailable(startId, transactionType);
                        return Service.START_NOT_STICKY;
                    }
                    switch (transactionType) {
                        case -1:
                            break;
                        case Transaction.RETRIEVE_TRANSACTION:
                            // If it's a transiently failed transaction,
                            // we should retry it in spite of current
                            // downloading mode.
                            int failureType = cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            PendingMessages.ERROR_TYPE));
                            if (!isTransientFailure(failureType)) {
                                break;
                            }
                            // fall-through
                        default:
                            Uri uri = ContentUris.withAppendedId(
                                    Mms.CONTENT_URI,
                                    cursor.getLong(columnIndexOfMsgId));
                            if (MmsApp.DEBUG) {
                                Log.d("MMSLog", "scanPendingMessages: Pending Message uri=" + uri);
                            }
                            
                            TransactionBundle args = new TransactionBundle(
                                    transactionType, uri.toString());
                            // FIXME: We use the same startId for all MMs.
                            launchTransaction(startId, args, false);
                            break;
                    }
                }
            } finally {
                cursor.close();
            }
        } else {
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "scanPendingMessages: no pending messages. Stopping service.");
            }
            RetryScheduler.setRetryAlarm(this);
            stopSelfIfIdle(startId);
        }

        return Service.START_NOT_STICKY;
    }

    // add for gemini
    private int scanPendingMessagesGemini(int startId, boolean noNetwork, int simId) {
        // Scan database to find all pending operations.
        if (MmsApp.DEBUG) {
            Log.d("MMSLog", "scanPendingMessagesGemini: startid=" + startId 
                + ", Request simId=" + simId+ ", noNetwork=" + noNetwork);
        }
        Cursor cursor = PduPersister.getPduPersister(this).getPendingMessages(
                //System.currentTimeMillis());
                SystemClock.elapsedRealtime());
        if (cursor != null) {
            try {
                int count = cursor.getCount();
                if (MmsApp.DEBUG) {
                    Log.d("MMSLog", "scanPendingMessagesGemini: Pending Message Size=" + count);
                }

                if (count == 0 && triggerMsgId == 0) {
                    RetryScheduler.setRetryAlarm(this);
                    stopSelfIfIdle(startId);
                    return Service.START_NOT_STICKY;
                }

                int columnIndexOfMsgId = cursor.getColumnIndexOrThrow(PendingMessages.MSG_ID);
                int columnIndexOfMsgType = cursor.getColumnIndexOrThrow(PendingMessages.MSG_TYPE);
                int columnIndexOfSimId = cursor.getColumnIndexOrThrow(PendingMessages.SIM_ID);
                int columnIndexOfErrorType = cursor.getColumnIndexOrThrow(PendingMessages.ERROR_TYPE);

                if (noNetwork) {
                    // Make sure we register for connection state changes.
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "scanPendingMessagesGemini: registerForConnectionStateChanges");
                    }
                    MmsSystemEventReceiver.registerForConnectionStateChanges(getApplicationContext());
                }
                int pendingMsgSimId = 0;
                while (cursor.moveToNext()) {
                    pendingMsgSimId = cursor.getInt(columnIndexOfSimId);
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "scanPendingMessagesGemini: pendingMsgSimId=" + pendingMsgSimId);
                    }
                    if (!SMART) {
                        if (pendingMsgSimId != simId) {
                            if (MmsApp.DEBUG) {
                                Log.d("MMSLog", "scanPendingMessagesGemini: pendingMsgSimId!=simId, Continue!");
                            }
                            continue;
                        }
                    }
                    if (MmsSms.ERR_TYPE_GENERIC_PERMANENT == cursor.getInt(columnIndexOfErrorType)) {
                        if (MmsApp.DEBUG) {
                            Log.d("MMSLog", "scanPendingMessagesGemini: Error type = Permanent, Continue!");
                        }
                        continue;
                    }
                    if (triggerMsgId == cursor.getLong(columnIndexOfMsgId)) {
                        if (MmsApp.DEBUG) {
                            Log.d("MMSLog", "scanPendingMessagesGemini: Message ID = Trigger message ID, Continue!");
                        }
                        continue;
                    }
                    
                    int msgType = cursor.getInt(columnIndexOfMsgType);
                    int transactionType = getTransactionType(msgType);
                    switch (transactionType) {
                        case -1:
                            if (MmsApp.DEBUG) {
                                Log.d("MMSLog", "scanPendingMessagesGemini: transaction Type= -1");
                            }
                            break;
                        case Transaction.RETRIEVE_TRANSACTION:
                            if (MmsApp.DEBUG) {
                                Log.d("MMSLog", "scanPendingMessagesGemini: transaction Type= RETRIEVE");
                            }
                            // If it's a transiently failed transaction,
                            // we should retry it in spite of current
                            // downloading mode.
                            int failureType = cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                            PendingMessages.ERROR_TYPE));
                            if (!isTransientFailure(failureType)) {
                                if (MmsApp.DEBUG) {
                                    Log.d("MMSLog", cursor.getLong(columnIndexOfMsgId) +  "this RETRIEVE not transient failure");
                                }
                                break;
                            }
                            // fall-through
                        default:
                            Uri uri = ContentUris.withAppendedId(
                                    Mms.CONTENT_URI,
                                    cursor.getLong(columnIndexOfMsgId));
                            if (MmsApp.DEBUG) {
                                Log.d("MMSLog", "scanPendingMessagesGemini: Pending Message uri=" + uri);
                            }
                            
                            TransactionBundle args = new TransactionBundle(
                                    transactionType, uri.toString());
                            // FIXME: We use the same startId for all MMs.
                            if (SMART) {
                                if (pendingMsgSimId > 0) {
                                    launchTransactionGemini(startId, pendingMsgSimId, args);
                                } else {
                                    // for handling third party 
                                    long connectSimId = Settings.System.getLong(getContentResolver(), Settings.System.GPRS_CONNECTION_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET); 
                                    Log.v("MMSLog", "Scan Pending message:  current data settings: " + connectSimId);
                                    if (Settings.System.DEFAULT_SIM_NOT_SET != connectSimId && Settings.System.GPRS_CONNECTION_SIM_SETTING_NEVER != connectSimId) {
                                        launchTransactionGemini(startId, (int)connectSimId, args);
                                    }
                                }
                            } else {
                                launchTransactionGemini(startId, simId, args);
                            }
                            break;
                    }
                }
            } finally {
                cursor.close();
            }
        } else {
            if (MmsApp.DEBUG) Log.d("MMSLog", "scanPendingMessagesGemini: no pending messages. Stopping service.");
            RetryScheduler.setRetryAlarm(this);
            stopSelfIfIdle(startId);
        }

        return Service.START_NOT_STICKY;
    }

    private void stopSelfIfIdle(int startId) {
        synchronized (mProcessing) {
            if (mProcessing.isEmpty() && mPending.isEmpty()) {
                if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                    Log.v(TAG, "stopSelfIfIdle: STOP!");
                }
                // Make sure we're no longer listening for connection state changes.
                if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                    Log.v(TAG, "stopSelfIfIdle: unRegisterForConnectionStateChanges");
                }
                MmsSystemEventReceiver.unRegisterForConnectionStateChanges(getApplicationContext());

                stopSelf(startId);
            }
        }
    }

    private static boolean isTransientFailure(int type) {
        return (type < MmsSms.ERR_TYPE_GENERIC_PERMANENT) && (type > MmsSms.NO_ERROR);
    }

    private boolean isNetworkAvailable() {
        return mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS).
                isAvailable();
    }

    private int getTransactionType(int msgType) {
        switch (msgType) {
            case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                return Transaction.RETRIEVE_TRANSACTION;
            case PduHeaders.MESSAGE_TYPE_READ_REC_IND:
                return Transaction.READREC_TRANSACTION;
            case PduHeaders.MESSAGE_TYPE_SEND_REQ:
                return Transaction.SEND_TRANSACTION;
            default:
                Log.w(TAG, "Unrecognized MESSAGE_TYPE: " + msgType);
                return -1;
        }
    }

    private void launchTransaction(int serviceId, TransactionBundle txnBundle, boolean noNetwork) {
        if (noNetwork) {
            Log.w(TAG, "launchTransaction: no network error!");
            onNetworkUnavailable(serviceId, txnBundle.getTransactionType());
            return;
        }
        Message msg = mServiceHandler.obtainMessage(EVENT_TRANSACTION_REQUEST);
        msg.arg1 = serviceId;
        msg.obj = txnBundle;

        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "launchTransaction: sending message " + msg);
        }
        mServiceHandler.sendMessage(msg);
    }

    private void onNetworkUnavailable(int serviceId, int transactionType) {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "onNetworkUnavailable: sid=" + serviceId + ", type=" + transactionType);
        }

        int toastType = TOAST_NONE;
        if (transactionType == Transaction.RETRIEVE_TRANSACTION) {
            toastType = TOAST_DOWNLOAD_LATER;
        } else if (transactionType == Transaction.SEND_TRANSACTION) {
            toastType = TOAST_MSG_QUEUED;
        }
        if (toastType != TOAST_NONE) {
            mToastHandler.sendEmptyMessage(toastType);
        }
        stopSelf(serviceId);
    }

    @Override
    public void onDestroy() {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "Destroying TransactionService");
        }
        if (MmsApp.DEBUG) {
            Log.d("MMSLog", "Destroying Transaction Service");
        }
        if (!mPending.isEmpty() && MmsApp.DEBUG) {
            Log.w("MMSLog", "onDestroy: TransactionService exiting with transaction still pending");
        }

        mConnectivityListener.unregisterHandler(mServiceHandler);
        mConnectivityListener.stopListening();
        mConnectivityListener = null;

        if (FeatureOption.MTK_GEMINI_SUPPORT && SMART) {
            bWaitingConxn = false;
        }
        releaseWakeLock();

        mServiceHandler.sendEmptyMessage(EVENT_QUIT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Handle status change of Transaction (The Observable).
     */
    public void update(Observable observable) {
        if (MmsApp.DEBUG) {
            Log.d("MMSLog", "Transaction Service update");
        }
        Transaction transaction = (Transaction) observable;
        int serviceId = transaction.getServiceId();

        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "update transaction " + serviceId);
        }

        try {
            synchronized (mProcessing) {
                mProcessing.remove(transaction);
                if (mPending.size() > 0) {
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "update: handle next pending transaction...");
                    }
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "TransactionService: update: mPending.size()=" + mPending.size());
                    }
                    Message msg = mServiceHandler.obtainMessage(
                            EVENT_HANDLE_NEXT_PENDING_TRANSACTION,
                            transaction.getConnectionSettings());
                    // add for gemini
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        msg.arg2 = transaction.mSimId;
                    }
                    mServiceHandler.sendMessage(msg);
                }
                //else {
                else if (0 == mProcessing.size()) {
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "update: endMmsConnectivity");
                    }

                    // add for gemini
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        endMmsConnectivityGemini(transaction.mSimId);
                        if (MmsApp.DEBUG) {
                            Log.d("MMSLog", "update endMmsConnectivityGemini Param = " + transaction.mSimId);
                        }
                    } else {
                        if (MmsApp.DEBUG) {
                            Log.d("MMSLog", "update call endMmsConnectivity");
                        }
                        endMmsConnectivity();
                    }
                }
            }

            Intent intent = new Intent(TRANSACTION_COMPLETED_ACTION);
            TransactionState state = transaction.getState();
            int result = state.getState();
            intent.putExtra(STATE, result);

            switch (result) {
                case TransactionState.SUCCESS:
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "update: result=SUCCESS");
                    }
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "Transaction complete: " + serviceId);
                    }

                    intent.putExtra(STATE_URI, state.getContentUri());

                    // Notify user in the system-wide notification area.
                    switch (transaction.getType()) {
                        case Transaction.NOTIFICATION_TRANSACTION:
                        case Transaction.RETRIEVE_TRANSACTION:
                            // We're already in a non-UI thread called from
                            // NotificationTransacation.run(), so ok to block here.
                            MessagingNotification.blockingUpdateNewMessageIndicator(this, true,
                                    false);
                            MessagingNotification.updateDownloadFailedNotification(this);
                            break;
                        case Transaction.SEND_TRANSACTION:
                            RateController.getInstance().update();
                            break;
                    }
                    break;
                case TransactionState.FAILED:
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "update: result=FAILED");
                    }
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "Transaction failed: " + serviceId);
                    }
                    break;
                default:
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "update: result=default");
                    }
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "Transaction state unknown: " +
                                serviceId + " " + result);
                    }
                    break;
            }

            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "update: broadcast transaction result " + result);
            }
            // Broadcast the result of the transaction.
            sendBroadcast(intent);
        } finally {
            transaction.detach(this);
            MmsSystemEventReceiver.unRegisterForConnectionStateChanges(getApplicationContext());
            //stopSelf(serviceId);
            stopSelfIfIdle(mMaxServiceId);
        }
    }

    private synchronized void createWakeLock() {
        // Create a new wake lock if we haven't made one yet.
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MMS Connectivity");
            mWakeLock.setReferenceCounted(false);
        }
    }

    private void acquireWakeLock() {
        // It's okay to double-acquire this because we are not using it
        // in reference-counted mode.
        mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        // Don't release the wake lock if it hasn't been created and acquired.
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    protected int beginMmsConnectivity() throws IOException {
        // Take a wake lock so we don't fall asleep before the message is downloaded.
        createWakeLock();

        int result = mConnMgr.startUsingNetworkFeature(
                ConnectivityManager.TYPE_MOBILE, Phone.FEATURE_ENABLE_MMS);
        if (MmsApp.DEBUG) {
            Log.d("MMSLog", "startUsingNetworkFeature: result=" + result);
        }

        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            Log.v(TAG, "beginMmsConnectivity: result=" + result);
        }

        switch (result) {
            case Phone.APN_ALREADY_ACTIVE:
            case Phone.APN_REQUEST_STARTED:
                acquireWakeLock();
                return result;
            case Phone.APN_REQUEST_FAILED:
                return result;
        }

        throw new IOException("Cannot establish MMS connectivity");
    }

    protected void endMmsConnectivity() {
        try {
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "endMmsConnectivity");
            }

            // cancel timer for renewal of lease
            mServiceHandler.removeMessages(EVENT_CONTINUE_MMS_CONNECTIVITY);
            if (mConnMgr != null) {
                mConnMgr.stopUsingNetworkFeature(
                        ConnectivityManager.TYPE_MOBILE,
                        Phone.FEATURE_ENABLE_MMS);
                if (MmsApp.DEBUG) {
                    Log.d("MMSLog", "stopUsingNetworkFeature");
                }
            }
        } finally {
            releaseWakeLock();
            triggerMsgId = 0;
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        /**
         * Handle incoming transaction requests.
         * The incoming requests are initiated by the MMSC Server or by the
         * MMS Client itself.
         */
        @Override
        public void handleMessage(Message msg) {
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "Handling incoming message: " + msg);
            }
            if (MmsApp.DEBUG) {
                Log.d("MMSLog", "handleMessage :" + msg);
            }

            Transaction transaction = null;

            switch (msg.what) {
                case EVENT_QUIT:
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "EVENT_QUIT");
                    }
                    if (FeatureOption.MTK_GEMINI_SUPPORT && SMART) {
                        bWaitingConxn = false;
                    }
                    releaseWakeLock();
                    getLooper().quit();
                    return;

                case EVENT_CONTINUE_MMS_CONNECTIVITY:
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "EVENT_CONTINUE_MMS_CONNECTIVITY");
                    }
                    synchronized (mProcessing) {
                        if (mProcessing.isEmpty()) {
                            return;
                        }
                    }

                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "handle EVENT_CONTINUE_MMS_CONNECTIVITY event...");
                    }

                    try {
                        // add for gemini
                        int result = 0;
                        if (FeatureOption.MTK_GEMINI_SUPPORT) {
                            SIMInfo si = SIMInfo.getSIMInfoBySlot(getApplicationContext(), msg.arg2);
                            if (null == si) {
                                Log.v("MMSLog", "TransactionService:SIMInfo is null for slot " + msg.arg2);
                                return;
                            }
                            int simId = (int)si.mSimId;
                            result = beginMmsConnectivityGemini(simId/*msg.arg2*/);
                        } else{
                            result = beginMmsConnectivity();
                        }

                        if (result != Phone.APN_ALREADY_ACTIVE) {
                            if (MmsApp.DEBUG) {
                                Log.d("MMSLog", "Extending MMS connectivity returned " + result +
                                    " instead of APN_ALREADY_ACTIVE");
                            }
                            // Just wait for connectivity startup without
                            // any new request of APN switch.
                            return;
                        }
                    } catch (IOException e) {
                        Log.w(TAG, "Attempt to extend use of MMS connectivity failed");
                        return;
                    }

                    //// Restart timer
                    //sendMessageDelayed(obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY),
                    //                   APN_EXTENSION_WAIT);
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        // Set a timer to keep renewing our "lease" on the MMS connection
                        sendMessageDelayed(obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY, 0, msg.arg2),
                                           APN_EXTENSION_WAIT);
                    } else {
                        // Set a timer to keep renewing our "lease" on the MMS connection
                        sendMessageDelayed(obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY),
                                           APN_EXTENSION_WAIT);
                    }
                    return;

                case EVENT_DATA_STATE_CHANGED:
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "EVENT_DATA_STATE_CHANGED! slot=" + msg.arg2);
                    }
                    /*
                     * If we are being informed that connectivity has been established
                     * to allow MMS traffic, then proceed with processing the pending
                     * transaction, if any.
                     */
                    if (mConnectivityListener == null) {
                        if (MmsApp.DEBUG) {
                            Log.d("MMSLog", "handleMessage : mConnectivityListener == null");
                        }
                        return;
                    }

                    // check sticky intent
                    if (mFWStickyIntent != null) {
                        //NetworkInfo stickyInfo = (NetworkInfo)mFWStickyIntent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                        //NetworkInfo nowInfo = mConnectivityListener.getNetworkInfo();
                        //if (stickyInfo != null && nowInfo != null && )
                        if (MmsApp.DEBUG) {
                            Log.d("MMSLog", "get sticky intent");
                        }
                        mFWStickyIntent = null;
                        return;
                    }

                    //NetworkInfo info = mConnectivityListener.getNetworkInfo();
                    if (mConnMgr == null) {                        
                        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);                    
                    }                    
                    if (mConnMgr == null) {                        
                        Log.d("MMSLog", "mConnMgr == null ");                      
                        return;                    
                    }
                    NetworkInfo info = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
                    
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "Handle DATA_STATE_CHANGED event: " + info);
                    }
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "Newwork info: " + info);
                    }

                    // Check connection state : connect or disconnect
                    if (info != null && !info.isConnected()) {
                        if (FeatureOption.MTK_GEMINI_SUPPORT 
                                && (ConnectivityManager.TYPE_MOBILE == info.getType()
                                    ||ConnectivityManager.TYPE_MOBILE_MMS == info.getType()) 
                                && mPending.size() != 0) {
                            if (SMART) {
                                Transaction trxn = mPending.get(0);
                                int slotId = SIMInfo.getSlotById(getApplicationContext(), trxn.mSimId);
                                if (slotId != msg.arg2) {
                                    return;
                                }
                            } else {
                                int simId = Settings.System.getInt(getContentResolver(), Settings.System.GPRS_CONNECTION_SETTING, Settings.System.GPRS_CONNECTION_SETTING_DEFAULT); 
                                Transaction trxn = mPending.get(0);
                                if (trxn.mSimId != simId - 1){
                                    trxn = mPending.remove(0);
                                    setTransactionFail(trxn, FAILE_TYPE_PERMANENT);
                                }
                            }
                        }
                        
                        // check type and reason, 
                        if (ConnectivityManager.TYPE_MOBILE_MMS == info.getType() 
                            && Phone.REASON_NO_SUCH_PDP.equals(info.getReason())) {
                            if (0 != mPending.size()){
                                setTransactionFail(mPending.remove(0), FAILE_TYPE_PERMANENT);
                                return;
                            }
                        } else if (ConnectivityManager.TYPE_MOBILE_MMS == info.getType()
                            && NetworkInfo.State.DISCONNECTED == info.getState()) {
                             if (0 != mPending.size()){
                                Log.d("MMSLog", "setTransactionFail TEMPORARY because NetworkInfo.State.DISCONNECTED");
                                setTransactionFail(mPending.remove(0), FAILE_TYPE_TEMPORARY);
                                return;
                             }                        
                        } else if ((ConnectivityManager.TYPE_MOBILE_MMS == info.getType() 
                                && Phone.REASON_APN_FAILED.equals(info.getReason())) 
                                || Phone.REASON_RADIO_TURNED_OFF.equals(info.getReason())) {
                            if (0 != mPending.size()){
                                setTransactionFail(mPending.remove(0), FAILE_TYPE_TEMPORARY);
                                return;
                            }
                            if (MmsApp.DEBUG) {
                                Log.d("MMSLog", "No pending message.");
                            }
                        }
                        return;
                    }

                    if (info != null && Phone.REASON_VOICE_CALL_ENDED.equals(info.getReason())){
                        if (0 != mPending.size()){
                            Transaction trxn = mPending.get(0);
                            // add for gemini
                            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                processPendingTransactionGemini(transaction,trxn.getConnectionSettings(),trxn.mSimId);
                            } else {
                                processPendingTransaction(transaction, trxn.getConnectionSettings());
                            }
                        }
                    }

                    // Check availability of the mobile network.
                    if ((info == null) || (info.getType() != ConnectivityManager.TYPE_MOBILE_MMS)) {
                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "   type is not TYPE_MOBILE_MMS, bail");
                        }
                        if (MmsApp.DEBUG) {
                            Log.d("MMSLog", "EVENT_DATA_STATE_CHANGED: type is not TYPE_MOBILE_MMS, bail");
                        }
                        return;
                    }

                    //TransactionSettings settings = new TransactionSettings(
                            //TransactionService.this, info.getExtraInfo());

                    TransactionSettings settings = null;
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        if (SMART) {
                            if (Phone.GEMINI_SIM_1 == msg.arg2 || Phone.GEMINI_SIM_2 == msg.arg2) {
                                settings = new TransactionSettings(TransactionService.this, info.getExtraInfo(), msg.arg2);
                            } else {
                                return;
                            }
                        } else {
                            int simId = Settings.System.getInt(getContentResolver(), Settings.System.GPRS_CONNECTION_SETTING, Settings.System.GPRS_CONNECTION_SETTING_DEFAULT);
                            if (MmsApp.DEBUG) {
                                Log.d("MMSLog", "handleMessage:  0:no data connect, 1:sim1,  2:sim2,  current=" + simId);
                            }
                            if (0 != simId) {
                                settings = new TransactionSettings(TransactionService.this, info.getExtraInfo(), simId-1);
                            } else {
                                return;
                            }
                        }
                    } else {
                        settings = new TransactionSettings(TransactionService.this, info.getExtraInfo());
                    }

                    // If this APN doesn't have an MMSC, wait for one that does.
                    if (TextUtils.isEmpty(settings.getMmscUrl())) {
                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "   empty MMSC url, bail");
                        }
                        if (MmsApp.DEBUG) {
                            Log.d("MMSLog", "   empty MMSC url, bail");
                        }
                        if (0 != mPending.size()){
                            setTransactionFail(mPending.remove(0), FAILE_TYPE_TEMPORARY);
                        }
                        return;
                    }

                    //// Set a timer to keep renewing our "lease" on the MMS connection
                    //sendMessageDelayed(obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY),
                    //                   APN_EXTENSION_WAIT);
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        if (SMART) {
                            if (Phone.GEMINI_SIM_1 == msg.arg2 || Phone.GEMINI_SIM_2 == msg.arg2) {
                                // Set a timer to keep renewing our "lease" on the MMS connection
                                sendMessageDelayed(obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY, 0, msg.arg2),
                                                   APN_EXTENSION_WAIT);
                                SIMInfo si = SIMInfo.getSIMInfoBySlot(getApplicationContext(), msg.arg2);
                                if (null == si) {
                                    Log.v("MMSLog", "TransactionService:SIMInfo is null for slot " + msg.arg2);
                                    return;
                                }
                                int simId = (int)si.mSimId;
                                processPendingTransactionGemini(transaction, settings, simId/*msg.arg2*/);
                            }else {
                                return;
                            }
                        } else {
                            int simId = Settings.System.getInt(getContentResolver(), Settings.System.GPRS_CONNECTION_SETTING, Settings.System.GPRS_CONNECTION_SETTING_DEFAULT);
                            Log.v("MMSLog", "handleMessage:  0:no data connect, 1:sim1,  2:sim2,  current="+simId);
                            if (0 != simId) {
                                // Set a timer to keep renewing our "lease" on the MMS connection                            
                                sendMessageDelayed(obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY, 0, simId-1), APN_EXTENSION_WAIT);                            
                                processPendingTransactionGemini(transaction, settings, simId-1);                        
                            } else {                            
                                return;                        
                            }
                        }
                    }else {
                        // Set a timer to keep renewing our "lease" on the MMS connection
                        sendMessageDelayed(obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY),
                                           APN_EXTENSION_WAIT);
                        processPendingTransaction(transaction, settings);
                    }
                    return;

                case EVENT_TRANSACTION_REQUEST:
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "EVENT_TRANSACTION_REQUEST");
                    }

                    int serviceId = msg.arg1;
                    try {
                        TransactionBundle args = (TransactionBundle) msg.obj;
                        TransactionSettings transactionSettings;

                        // Set the connection settings for this transaction.
                        // If these have not been set in args, load the default settings.
                        String mmsc = args.getMmscUrl();
                        if (mmsc != null) {
                            transactionSettings = new TransactionSettings(
                                    mmsc, args.getProxyAddress(), args.getProxyPort());
                        } else {
                            // add for gemini
                            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                // convert sim id to slot id
                                int slotId = SIMInfo.getSlotById(getApplicationContext(), msg.arg2);
                                transactionSettings = new TransactionSettings(
                                                    TransactionService.this, null, slotId/*msg.arg2*/);
                            } else {
                                transactionSettings = new TransactionSettings(
                                                    TransactionService.this, null);
                            }
                        }

                        int transactionType = args.getTransactionType();

                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "handle EVENT_TRANSACTION_REQUEST: transactionType=" +
                                    transactionType);
                        }

                        // Create appropriate transaction
                        switch (transactionType) {
                            case Transaction.NOTIFICATION_TRANSACTION:
                                if (MmsApp.DEBUG) {
                                    Log.d("MMSLog", "TRANSACTION REQUEST: NOTIFICATION_TRANSACTION");
                                }
                                String uri = args.getUri();
                                if (MmsApp.DEBUG) {
                                    Log.d("MMSLog", "uri="+uri);
                                }
                                if (uri != null) {
                                    // add for gemini
                                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                        transaction = new NotificationTransaction(
                                            TransactionService.this, serviceId, msg.arg2,
                                            transactionSettings, uri);
                                    } else {
                                        transaction = new NotificationTransaction(
                                            TransactionService.this, serviceId,
                                            transactionSettings, uri);
                                    }
                                } else {
                                    // Now it's only used for test purpose.
                                    byte[] pushData = args.getPushData();
                                    PduParser parser = new PduParser(pushData);
                                    GenericPdu ind = parser.parse();

                                    int type = PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
                                    if ((ind != null) && (ind.getMessageType() == type)) {
                                        // add for gemini
                                        if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                            transaction = new NotificationTransaction(
                                                TransactionService.this, serviceId, msg.arg2,
                                                transactionSettings, (NotificationInd) ind);
                                        } else {
                                            transaction = new NotificationTransaction(
                                                TransactionService.this, serviceId,
                                                transactionSettings, (NotificationInd) ind);
                                        }
                                    } else {
                                        Log.e("MMSLog", "Invalid PUSH data.");
                                        transaction = null;
                                        return;
                                    }
                                }
                                break;
                            case Transaction.RETRIEVE_TRANSACTION:
                                if (MmsApp.DEBUG) {
                                    Log.d("MMSLog", "TRANSACTION REQUEST: RETRIEVE_TRANSACTION uri=" + args.getUri());
                                }
                                // add for gemini
                                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                    transaction = new RetrieveTransaction(
                                        TransactionService.this, serviceId, msg.arg2,
                                        transactionSettings, args.getUri());
                                } else {
                                    transaction = new RetrieveTransaction(
                                        TransactionService.this, serviceId,
                                        transactionSettings, args.getUri());
                                }
                                break;
                            case Transaction.SEND_TRANSACTION:
                                if (MmsApp.DEBUG) {
                                    Log.d("MMSLog", "TRANSACTION REQUEST: SEND_TRANSACTION");
                                }
                                // add for gemini
                                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                    transaction = new SendTransaction(
                                        TransactionService.this, serviceId, msg.arg2,
                                        transactionSettings, args.getUri());
                                } else {
                                    transaction = new SendTransaction(
                                        TransactionService.this, serviceId,
                                        transactionSettings, args.getUri());
                                }
                                break;
                            case Transaction.READREC_TRANSACTION:
                                if (MmsApp.DEBUG) {
                                    Log.d("MMSLog", "TRANSACTION REQUEST: READREC_TRANSACTION");
                                }
                                // add for gemini
                                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                    transaction = new ReadRecTransaction(
                                        TransactionService.this, serviceId, msg.arg2,
                                        transactionSettings, args.getUri());
                                } else {
                                    transaction = new ReadRecTransaction(
                                        TransactionService.this, serviceId,
                                        transactionSettings, args.getUri());
                                }
                                break;
                            default:
                                if (MmsApp.DEBUG) {
                                    Log.w("MMSLog", "Invalid transaction type: " + serviceId);
                                }
                                transaction = null;
                                return;
                        }

                        if (!processTransaction(transaction)) {
                            // add for gemini
                            if (FeatureOption.MTK_GEMINI_SUPPORT && null != transaction) {
                                mSimIdForEnd = transaction.mSimId;
                            }
                            transaction = null;
                            return;
                        }

                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "Started processing of incoming message: " + msg);
                        }
                    } catch (Exception ex) {
                        if (MmsApp.DEBUG) {
                            Log.w("MMSLog", "Exception occurred while handling message: " + msg, ex);
                        }

                        if (transaction != null) {
                            // add for gemini
                            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                mSimIdForEnd = transaction.mSimId;
                            }
                            try {
                                transaction.detach(TransactionService.this);
                                if (mProcessing.contains(transaction)) {
                                    synchronized (mProcessing) {
                                        mProcessing.remove(transaction);
                                    }
                                }
                            } catch (Throwable t) {
                                Log.e(TAG, "Unexpected Throwable.", t);
                            } finally {
                                // Set transaction to null to allow stopping the
                                // transaction service.
                                transaction = null;
                            }
                        }
                    } finally {
                        if (transaction == null) {
                            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                                Log.v(TAG, "Transaction was null. Stopping self: " + serviceId);
                            }
                            if (MmsApp.DEBUG) {
                                Log.d("MMSLog", "finally call endMmsConnectivity");
                            }
                            if (mProcessing.size() == 0){
                                // add for gemini
                                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                                    endMmsConnectivityGemini(mSimIdForEnd);
                                } else {
                                    endMmsConnectivity();
                                }
                            }
                            stopSelf(serviceId);
                        }
                    }
                    return;
                case EVENT_HANDLE_NEXT_PENDING_TRANSACTION:
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "EVENT_HANDLE_NEXT_PENDING_TRANSACTION");
                    }
                    // add for gemini
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        processPendingTransactionGemini(transaction, (TransactionSettings) msg.obj, msg.arg2);
                    } else {
                        processPendingTransaction(transaction, (TransactionSettings) msg.obj);
                    }
                    return;
                default:
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "handleMessage : default");
                    }
                    Log.w(TAG, "what=" + msg.what);
                    return;
            }
        }

        private void processPendingTransaction(Transaction transaction,
                                               TransactionSettings settings) {

            if (MmsApp.DEBUG) {
                Log.d("MMSLog", "processPendingTxn: transaction=" + transaction);
            }
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "processPendingTxn: transaction=" + transaction);
            }

            int numProcessTransaction = 0;
            synchronized (mProcessing) {
                if (mPending.size() != 0) {
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "processPendingTransaction: mPending.size()=" + mPending.size());
                    }
                    transaction = mPending.remove(0);
                }
                numProcessTransaction = mProcessing.size();
            }

            if (transaction != null) {
                if (settings != null) {
                    transaction.setConnectionSettings(settings);
                }

                /*
                 * Process deferred transaction
                 */
                try {
                    int serviceId = transaction.getServiceId();

                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "processPendingTxn: process " + serviceId);
                    }

                    if (processTransaction(transaction)) {
                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "Started deferred processing of transaction  "
                                    + transaction);
                        }
                    } else {
                        transaction = null;
                        stopSelf(serviceId);
                    }
                } catch (IOException e) {
                    Log.w(TAG, e.getMessage(), e);
                }
            } else {
                if (numProcessTransaction == 0) {
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "processPendingTxn: no more transaction, endMmsConnectivity");
                    }
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "processPendingTransaction:no more transaction, endMmsConnectivity");
                    }
                    endMmsConnectivity();
                }
            }
        }

        // add for gemini
        private void processPendingTransactionGemini(Transaction transaction,
                                               TransactionSettings settings, int simId) {

            if (MmsApp.DEBUG) {
                Log.d("MMSLog", "processPendingTxn for Gemini: transaction=" + transaction + " sim ID="+simId);
            }

            int numProcessTransaction = 0;
            synchronized (mProcessing) {
                if (mPending.size() != 0) {
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "processPendingTxn for Gemini: Pending size=" + mPending.size());
                    }
                    Transaction transactiontemp = null;
                    int pendingSize = mPending.size();
                    for (int i = 0; i < pendingSize; ++i){
                        transactiontemp = mPending.remove(0);
                        if (simId == transactiontemp.mSimId){
                            transaction = transactiontemp;
                            if (MmsApp.DEBUG) {
                                Log.d("MMSLog", "processPendingTxn for Gemini, get transaction with same simId");
                            }
                            break;
                        }else{
                            mPending.add(transactiontemp);
                            if (MmsApp.DEBUG) {
                                Log.d("MMSLog", "processPendingTxn for Gemini, diffrent simId, add to tail");
                            }
                        }
                    }
                    if (SMART) {
                        if (null == transaction) {
                            transaction = mPending.remove(0);
                            endMmsConnectivityGemini(simId);
                            if (MmsApp.DEBUG) {
                                Log.d("MMSLog", "Another SIM:" + transaction.mSimId);
                            }
                        }
                    }
                }
                numProcessTransaction = mProcessing.size();
            }

            if (transaction != null) {
                if (settings != null) {
                    transaction.setConnectionSettings(settings);
                }

                if (FeatureOption.MTK_GEMINI_SUPPORT && SMART) {
                    bWaitingConxn = false;
                }

                try {
                    int serviceId = transaction.getServiceId();
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "processPendingTxnGemini: process " + serviceId);
                    }

                    if (processTransaction(transaction)) {
                        if (MmsApp.DEBUG) {
                            Log.d("MMSLog", "Started deferred processing of transaction  " + transaction);
                        }
                    } else {
                        transaction = null;
                        stopSelf(serviceId);
                    }
                } catch (IOException e) {
                    Log.w("MMSLog", e.getMessage(), e);
                }
            } else {
                if (numProcessTransaction == 0) {
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "processPendingTxnGemini:no more transaction, endMmsConnectivity");
                    }
                    endMmsConnectivityGemini(simId);
                }
            }
        }

        /**
         * Internal method to begin processing a transaction.
         * @param transaction the transaction. Must not be {@code null}.
         * @return {@code true} if process has begun or will begin. {@code false}
         * if the transaction should be discarded.
         * @throws IOException if connectivity for MMS traffic could not be
         * established.
         */
        private boolean processTransaction(Transaction transaction) throws IOException {
            if (MmsApp.DEBUG) {
                Log.d("MMSLog", "process Transaction");
            }
            // Check if transaction already processing
            synchronized (mProcessing) {
                for (Transaction t : mPending) {
                    if (t.isEquivalent(transaction)) {
                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "Transaction already pending: " +
                                    transaction.getServiceId());
                        }
                        if (MmsApp.DEBUG) {
                            Log.d("MMSLog", "Process Transaction: already pending " + transaction.getServiceId());
                        }
                        return true;
                    }
                }
                for (Transaction t : mProcessing) {
                    if (t.isEquivalent(transaction)) {
                        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                            Log.v(TAG, "Duplicated transaction: " + transaction.getServiceId());
                        }
                        if (MmsApp.DEBUG) {
                            Log.d("MMSLog", "Process Transaction: Duplicated transaction" + transaction.getServiceId());
                        }
                        return true;
                    }
                }

                // add for gemini
                //if(FeatureOption.MTK_GEMINI_SUPPORT && SMART && (mProcessing.size() > 0 || mPending.size() > 0)){
                if (FeatureOption.MTK_GEMINI_SUPPORT && SMART && (mProcessing.size() > 0 || bWaitingConxn)) {
                    mPending.add(transaction);
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "add to pending, Processing size=" + mProcessing.size() 
                            + ",is waiting conxn=" + bWaitingConxn);
                    }
                    return true;
                }

                /*
                * Make sure that the network connectivity necessary
                * for MMS traffic is enabled. If it is not, we need
                * to defer processing the transaction until
                * connectivity is established.
                */
                if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                    Log.v(TAG, "processTransaction: call beginMmsConnectivity...");
                }
                
                int connectivityResult = 0;
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    connectivityResult = beginMmsConnectivityGemini(transaction.mSimId);
                } else {
                    connectivityResult = beginMmsConnectivity();
                }
                if (connectivityResult == Phone.APN_REQUEST_STARTED) {
                    mPending.add(transaction);
                    if (FeatureOption.MTK_GEMINI_SUPPORT && SMART) {
                        bWaitingConxn = true;
                    }
                    if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                        Log.v(TAG, "processTransaction: connResult=APN_REQUEST_STARTED, " +
                                "defer transaction pending MMS connectivity");
                    }
                    if (MmsApp.DEBUG) {
                        Log.d("MMSLog", "mPending.size()=" + mPending.size());
                    }
                    return true;
                } 
                // add for gemini and open
                else if (/*FeatureOption.MTK_GEMINI_SUPPORT == true && */connectivityResult == Phone.APN_REQUEST_FAILED){
                    if (transaction instanceof SendTransaction
                        || transaction instanceof RetrieveTransaction){
                        setTransactionFail(transaction, FAILE_TYPE_PERMANENT);
                        return false;
                    }
                }

                if (MmsApp.DEBUG) {
                    Log.d("MMSLog", "Adding Processing list: " + transaction);
                }
                mProcessing.add(transaction);
            }

            //// Set a timer to keep renewing our "lease" on the MMS connection
            //sendMessageDelayed(obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY),
            //                   APN_EXTENSION_WAIT);
            
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                // Set a timer to keep renewing our "lease" on the MMS connection
                // convert sim id to slot id
                int slotId = SIMInfo.getSlotById(getApplicationContext(), transaction.mSimId);
                sendMessageDelayed(obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY, 0, slotId/*transaction.mSimId*/),
                                   APN_EXTENSION_WAIT);
            } else {
                // Set a timer to keep renewing our "lease" on the MMS connection
                sendMessageDelayed(obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY),
                                   APN_EXTENSION_WAIT);
            }

            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                Log.v(TAG, "processTransaction: starting transaction " + transaction);
            }

            // Attach to transaction and process it
            transaction.attach(TransactionService.this);
            transaction.process();
            return true;
        }
    }


    // add for gemini and open
    private void setTransactionFail(Transaction txn, int failType) {
        if (MmsApp.DEBUG) {
            Log.d("MMSLog", "set Transaction Fail. fail Type=" + failType);
        }

        if (FeatureOption.MTK_GEMINI_SUPPORT && SMART) {
            bWaitingConxn = false;
        }
        
        Uri uri = null;
        if (txn instanceof SendTransaction) {
            if (MmsApp.DEBUG) {
                Log.d("MMSLog", "set Transaction Fail. :Send");
            }
            uri = ((SendTransaction)txn).getSendReqUri();
        } else if (txn instanceof NotificationTransaction) {
            if (MmsApp.DEBUG) {
                Log.d("MMSLog", "set Transaction Fail. :Notification");
            }
            uri = ((NotificationTransaction)txn).getNotTrxnUri();
        } else if (txn instanceof RetrieveTransaction) {
            if (MmsApp.DEBUG) {
                Log.d("MMSLog", "set Transaction Fail. :Retrieve");
            }
            uri = ((RetrieveTransaction)txn).getRtrTrxnUri();
        } else if (txn instanceof ReadRecTransaction) {
            if (MmsApp.DEBUG) {
                Log.d("MMSLog", "set Transaction Fail. :ReadRec");
            }
            uri = ((ReadRecTransaction)txn).getRrecTrxnUri();
        } else {
            if (MmsApp.DEBUG) {
                Log.d("MMSLog", "set Transaction Fail. type cann't be recognised");
            }
        }

        if (null != uri) {
            txn.mTransactionState.setContentUri(uri);
        }

        if (txn instanceof NotificationTransaction) {
            DownloadManager downloadManager = DownloadManager.getInstance();
            boolean autoDownload = false;
            // add for gemini
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                autoDownload = downloadManager.isAuto(txn.mSimId);
            } else {
                autoDownload = downloadManager.isAuto();
            }

            if (!autoDownload) {
                txn.mTransactionState.setState(TransactionState.SUCCESS);
            } else {
                txn.mTransactionState.setState(TransactionState.FAILED);
            }
        } else {
            txn.mTransactionState.setState(TransactionState.FAILED);
        }

        txn.attach(TransactionService.this);
        if (MmsApp.DEBUG) {
            Log.d("MMSLog", "attach this transaction.");
        }
        
        long msgId = ContentUris.parseId(uri);

        Uri.Builder uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter("protocol", "mms");
        uriBuilder.appendQueryParameter("message", String.valueOf(msgId));

        Cursor cursor = SqliteWrapper.query(getApplicationContext(), 
                                            getApplicationContext().getContentResolver(),
                                            uriBuilder.build(), 
                                            null, null, null, null);

        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    // Mark the failed message as unread.
                    ContentValues readValues = new ContentValues(1);
                    readValues.put(Mms.READ, 0);
                    SqliteWrapper.update(getApplicationContext(), getApplicationContext().getContentResolver(),
                                    uri, readValues, null, null);
                            
                    DefaultRetryScheme scheme = new DefaultRetryScheme(getApplicationContext(), 100);
                    
                    ContentValues values = null;
                    if (FAILE_TYPE_PERMANENT == failType) {
                        values = new ContentValues(2);
                        values.put(PendingMessages.ERROR_TYPE,  MmsSms.ERR_TYPE_GENERIC_PERMANENT);
                        values.put(PendingMessages.RETRY_INDEX, scheme.getRetryLimit());

                        int columnIndex = cursor.getColumnIndexOrThrow(PendingMessages._ID);
                        long id = cursor.getLong(columnIndex);
                                            
                        SqliteWrapper.update(getApplicationContext(), 
                                            getApplicationContext().getContentResolver(),
                                            PendingMessages.CONTENT_URI,
                                            values, PendingMessages._ID + "=" + id, null);
                    }
                }
            }finally {
                cursor.close();
            }
        }

        txn.notifyObservers();
    }
    

    // add for gemini
    private void launchTransactionGemini(int serviceId, int simId, TransactionBundle txnBundle) {
        Message msg = mServiceHandler.obtainMessage(EVENT_TRANSACTION_REQUEST);
        msg.arg1 = serviceId;
        msg.arg2 = simId;
        msg.obj = txnBundle;

        if (MmsApp.DEBUG) {
            Log.d("MMSLog", "launchTransactionGemini: sending message " + msg);
        }
        mServiceHandler.sendMessage(msg);
    }

    // add for gemini
    protected int beginMmsConnectivityGemini(int simId) throws IOException {
        // Take a wake lock so we don't fall asleep before the message is downloaded.
        createWakeLock();

        // convert sim id to slot id
        int slotId = SIMInfo.getSlotById(getApplicationContext(), simId);

        int result = mConnMgr.startUsingNetworkFeatureGemini(
                ConnectivityManager.TYPE_MOBILE, Phone.FEATURE_ENABLE_MMS, slotId);

        if (MmsApp.DEBUG) {
            Log.d("MMSLog", "beginMmsConnectivityGemini: simId=" + simId + "\t slotId=" + slotId + "\t result=" + result);
        }

        switch (result) {
            case Phone.APN_ALREADY_ACTIVE:
            case Phone.APN_REQUEST_STARTED:
                acquireWakeLock();
                if (SMART) {
                    sendBroadcast(new Intent(TRANSACTION_START));
                }
                return result;

            case Phone.APN_REQUEST_FAILED:
                return result;
            default:
                throw new IOException("Cannot establish MMS connectivity");
        }
    }

    // add for gemini
    protected void endMmsConnectivityGemini(int simId) {
        try {
            // convert sim id to slot id
            int slotId = SIMInfo.getSlotById(getApplicationContext(), simId);
            
            if (MmsApp.DEBUG) {
                Log.d("MMSLog", "endMmsConnectivityGemini: slot id = " + slotId);
            }

            // cancel timer for renewal of lease
            mServiceHandler.removeMessages(EVENT_CONTINUE_MMS_CONNECTIVITY);
            if (mConnMgr != null) {
                mConnMgr.stopUsingNetworkFeatureGemini(
                        ConnectivityManager.TYPE_MOBILE,
                        Phone.FEATURE_ENABLE_MMS, slotId);
                if (SMART) {
                    sendBroadcast(new Intent(TRANSACTION_STOP));
                }
            }
        } finally {
            releaseWakeLock();
            triggerMsgId = 0;
        }
    }


    
}
