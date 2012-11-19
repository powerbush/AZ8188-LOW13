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
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.mms.util;

import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.MessagingPreferenceActivity;
import android.database.sqlite.SqliteWrapper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.Telephony;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Conversations;
import android.util.Log;
import com.google.android.mms.pdu.PduHeaders;

import android.provider.Telephony.WapPush;
import android.provider.Telephony.Threads;

/**
 * The recycler is responsible for deleting old messages.
 */
public abstract class Recycler {
    private static final boolean LOCAL_DEBUG = false;
    private static final String TAG = "Recycler";

    // Default preference values
    private static final boolean DEFAULT_AUTO_DELETE  = false;

    private static SmsRecycler sSmsRecycler;
    private static MmsRecycler sMmsRecycler;

    public static SmsRecycler getSmsRecycler() {
        if (sSmsRecycler == null) {
            sSmsRecycler = new SmsRecycler();
        }
        return sSmsRecycler;
    }

    public static MmsRecycler getMmsRecycler() {
        if (sMmsRecycler == null) {
            sMmsRecycler = new MmsRecycler();
        }
        return sMmsRecycler;
    }
    
    //Recycler for wap push message.
    private static WapPushRecycler sWapPushRecycler;
    public static WapPushRecycler getWapPushRecycler() {
        if (sWapPushRecycler == null) {
        	sWapPushRecycler = new WapPushRecycler();
        }
        return sWapPushRecycler;
    }

    public static boolean checkForThreadsOverLimit(Context context) {
        Recycler smsRecycler = getSmsRecycler();
        Recycler mmsRecycler = getMmsRecycler();

        return smsRecycler.anyThreadOverLimit(context) || mmsRecycler.anyThreadOverLimit(context);
    }

    public void deleteOldMessages(Context context) {
        if (LOCAL_DEBUG) {
            Log.v(TAG, "Recycler.deleteOldMessages this: " + this);
        }
        if (!isAutoDeleteEnabled(context)) {
            return;
        }

        Cursor cursor = getAllThreads(context);
        try {
            int limit = getMessageLimit(context);
            long threadId = 0;
            while (cursor.moveToNext()) {
            	threadId = getThreadId(cursor);
                deleteMessagesForThread(context, threadId, limit);
            }
        } finally {
            cursor.close();
        }
    }

    public void deleteOldMessagesByThreadId(Context context, long threadId) {
        if (LOCAL_DEBUG) {
            Log.v(TAG, "Recycler.deleteOldMessagesByThreadId this: " + this +
                    " threadId: " + threadId);
        }
        if (!isAutoDeleteEnabled(context)) {
            return;
        }

        deleteMessagesForThread(context, threadId, getMessageLimit(context));
    }

    public static boolean isAutoDeleteEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(MessagingPreferenceActivity.AUTO_DELETE,
                DEFAULT_AUTO_DELETE);
    }

    abstract public int getMessageLimit(Context context);

    abstract public void setMessageLimit(Context context, int limit);

    public int getMessageMinLimit() {
        return MmsConfig.getMinMessageCountPerThread();
    }

    public int getMessageMaxLimit() {
        return MmsConfig.getMaxMessageCountPerThread();
    }

    abstract protected long getThreadId(Cursor cursor);

    abstract protected Cursor getAllThreads(Context context);

    abstract protected void deleteMessagesForThread(Context context, long threadId, int keep);

    abstract protected void dumpMessage(Cursor cursor, Context context);

    abstract protected boolean anyThreadOverLimit(Context context);

    public static class SmsRecycler extends Recycler {
        private static final String[] ALL_SMS_THREADS_PROJECTION = {
            Telephony.Sms.Conversations.THREAD_ID,
            Telephony.Sms.Conversations.MESSAGE_COUNT
        };

        private static final int ID             = 0;
        private static final int MESSAGE_COUNT  = 1;

        static private final String[] SMS_MESSAGE_PROJECTION = new String[] {
            BaseColumns._ID,
            Conversations.THREAD_ID,
            Sms.ADDRESS,
            Sms.BODY,
            Sms.DATE,
            Sms.READ,
            Sms.TYPE,
            Sms.STATUS,
        };

        // The indexes of the default columns which must be consistent
        // with above PROJECTION.
        static private final int COLUMN_ID                  = 0;
        static private final int COLUMN_THREAD_ID           = 1;
        static private final int COLUMN_SMS_ADDRESS         = 2;
        static private final int COLUMN_SMS_BODY            = 3;
        static private final int COLUMN_SMS_DATE            = 4;
        static private final int COLUMN_SMS_READ            = 5;
        static private final int COLUMN_SMS_TYPE            = 6;
        static private final int COLUMN_SMS_STATUS          = 7;

        private final String MAX_SMS_MESSAGES_PER_THREAD = "MaxSmsMessagesPerThread";

        public int getMessageLimit(Context context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getInt(MAX_SMS_MESSAGES_PER_THREAD,
                    MmsConfig.getDefaultSMSMessagesPerThread());
        }

        public void setMessageLimit(Context context, int limit) {
            SharedPreferences.Editor editPrefs =
                PreferenceManager.getDefaultSharedPreferences(context).edit();
            editPrefs.putInt(MAX_SMS_MESSAGES_PER_THREAD, limit);
            editPrefs.apply();
        }

        protected long getThreadId(Cursor cursor) {
            return cursor.getLong(ID);
        }

        protected Cursor getAllThreads(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = SqliteWrapper.query(context, resolver,
                    Telephony.Sms.Conversations.CONTENT_URI,
                    ALL_SMS_THREADS_PROJECTION, null, null, Conversations.DEFAULT_SORT_ORDER);

            return cursor;
        }

        protected void deleteMessagesForThread(Context context, long threadId, int keep) {
            if (LOCAL_DEBUG) {
                Log.v(TAG, "SMS: deleteMessagesForThread");
            }
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            String notDraft = Sms.TYPE + "<>" + Sms.MESSAGE_TYPE_DRAFT;
            try {
                cursor = SqliteWrapper.query(context, resolver,
                        ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, threadId),
                        SMS_MESSAGE_PROJECTION,
                        "locked=0 AND " + notDraft,
                        null, "date ASC");     // get in oldest to newest order
                if (cursor == null) {
                    Log.e(TAG, "SMS: deleteMessagesForThread got back null cursor");
                    return;
                }
                int count = cursor.getCount();
                int numberToDelete = count - keep;
                if (LOCAL_DEBUG) {
                    Log.v(TAG, "SMS: deleteMessagesForThread keep: " + keep +
                            " count: " + count +
                            " numberToDelete: " + numberToDelete);
                }
                if (numberToDelete <= 0) {
                    return;
                }
               // Move to the keep limit and then delete everything older than that one.
                cursor.moveToPosition(numberToDelete);
                long latestDate = cursor.getLong(COLUMN_SMS_DATE);

                long cntDeleted = SqliteWrapper.delete(context, resolver,
                        ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, threadId),
                        "locked=0 AND " + notDraft + " AND date<" + latestDate,
                        null);
                if (LOCAL_DEBUG) {
                    Log.v(TAG, "SMS: deleteMessagesForThread cntDeleted: " + cntDeleted);
                }

                if(cntDeleted != numberToDelete){
                    cursor = SqliteWrapper.query(context, resolver,
                            ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, threadId),
                            SMS_MESSAGE_PROJECTION,
                            "locked=0 AND " + notDraft,
                            null, "_id ASC");     // get in oldest to newest order
                    numberToDelete = numberToDelete - (int)cntDeleted;
                    if (LOCAL_DEBUG) {
                        Log.v(TAG, "SMS: numberToDelete: " + numberToDelete);
                    }
                    if (numberToDelete <= 0) {
                        return;
                    }

                    cursor.moveToPosition(numberToDelete);
                    long delId = cursor.getLong(COLUMN_ID);
                    cntDeleted = SqliteWrapper.delete(context, resolver,
                            ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, threadId),
                            "locked=0 AND " + notDraft + " AND _id<" + delId,
                            null);

                    if (LOCAL_DEBUG) {
                        Log.v(TAG, "SMS: deleteMessagesForThread cntDeleted: " + cntDeleted);
                    }
                
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        protected void dumpMessage(Cursor cursor, Context context) {
            long date = cursor.getLong(COLUMN_SMS_DATE);
            String dateStr = MessageUtils.formatTimeStampString(context, date, true);
            if (LOCAL_DEBUG) {
                Log.v(TAG, "Recycler message " +
                        "\n    address: " + cursor.getString(COLUMN_SMS_ADDRESS) +
                        "\n    body: " + cursor.getString(COLUMN_SMS_BODY) +
                        "\n    date: " + dateStr +
                        "\n    date: " + date +
                        "\n    read: " + cursor.getInt(COLUMN_SMS_READ));
            }
        }

        @Override
        protected boolean anyThreadOverLimit(Context context) {
            Cursor cursor = getAllThreads(context);
            if (cursor == null) {
                return false;
            }
            int limit = getMessageLimit(context);
            try {
            	long threadId = 0;
                while (cursor.moveToNext()) {
                	threadId = getThreadId(cursor);
                    ContentResolver resolver = context.getContentResolver();
                    Cursor msgs = SqliteWrapper.query(context, resolver,
                            ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, threadId),
                            SMS_MESSAGE_PROJECTION,
                            "locked=0",
                            null, "date DESC");     // get in newest to oldest order
                    if (msgs == null) {
                        return false;
                    }
                    try {
                        if (msgs.getCount() >= limit) {
                            return true;
                        }
                    } finally {
                        msgs.close();
                    }
                }
            } finally {
                cursor.close();
            }
            return false;
        }
    }

    public static class MmsRecycler extends Recycler {
        private static final String[] ALL_MMS_THREADS_PROJECTION = {
            "thread_id", "count(*) as msg_count"
        };

        private static final int ID             = 0;
        private static final int MESSAGE_COUNT  = 1;

        static private final String[] MMS_MESSAGE_PROJECTION = new String[] {
            BaseColumns._ID,
            Conversations.THREAD_ID,
            Mms.DATE,
        };

        // The indexes of the default columns which must be consistent
        // with above PROJECTION.
        static private final int COLUMN_ID                  = 0;
        static private final int COLUMN_THREAD_ID           = 1;
        static private final int COLUMN_MMS_DATE            = 2;
        static private final int COLUMN_MMS_READ            = 3;

        private final String MAX_MMS_MESSAGES_PER_THREAD = "MaxMmsMessagesPerThread";

        public int getMessageLimit(Context context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getInt(MAX_MMS_MESSAGES_PER_THREAD,
                    MmsConfig.getDefaultMMSMessagesPerThread());
        }

        public void setMessageLimit(Context context, int limit) {
            SharedPreferences.Editor editPrefs =
                PreferenceManager.getDefaultSharedPreferences(context).edit();
            editPrefs.putInt(MAX_MMS_MESSAGES_PER_THREAD, limit);
            editPrefs.apply();
        }

        protected long getThreadId(Cursor cursor) {
            return cursor.getLong(ID);
        }

        protected Cursor getAllThreads(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = SqliteWrapper.query(context, resolver,
                    Uri.withAppendedPath(Telephony.Mms.CONTENT_URI, "threads"),
                    ALL_MMS_THREADS_PROJECTION, null, null, Conversations.DEFAULT_SORT_ORDER);

            return cursor;
        }

        public void deleteOldMessagesInSameThreadAsMessage(Context context, Uri uri) {
            if (LOCAL_DEBUG) {
                Log.v(TAG, "MMS: deleteOldMessagesByUri");
            }
            if (!isAutoDeleteEnabled(context)) {
                return;
            }
            Cursor cursor = null;
            long latestDate = 0;
            long threadId = 0;
            try {
                String msgId = uri.getLastPathSegment();
                ContentResolver resolver = context.getContentResolver();
                cursor = SqliteWrapper.query(context, resolver,
                        Telephony.Mms.CONTENT_URI,
                        MMS_MESSAGE_PROJECTION,
                        "thread_id in (select thread_id from pdu where _id=" + msgId +
                        ") AND locked=0",
                        null, "date DESC");     // get in newest to oldest order
                if (cursor == null) {
                    Log.e(TAG, "MMS: deleteOldMessagesInSameThreadAsMessage got back null cursor");
                    return;
                }

                int count = cursor.getCount();
                int keep = getMessageLimit(context);
                int numberToDelete = count - keep;
                if (LOCAL_DEBUG) {
                    Log.v(TAG, "MMS: deleteOldMessagesByUri keep: " + keep +
                            " count: " + count +
                            " numberToDelete: " + numberToDelete);
                }
                if (numberToDelete <= 0) {
                    return;
                }
                // Move to the keep limit and then delete everything older than that one.
                cursor.move(keep);
                latestDate = cursor.getLong(COLUMN_MMS_DATE);
                threadId = cursor.getLong(COLUMN_THREAD_ID);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (threadId != 0) {
                deleteMessagesOlderThanDate(context, threadId, latestDate);
            }
        }

        protected void deleteMessagesForThread(Context context, long threadId, int keep) {
            if (LOCAL_DEBUG) {
                Log.v(TAG, "MMS: deleteMessagesForThread");
            }
            if (threadId == 0) {
                return;
            }
            Cursor cursor = null;
            long latestDate = 0;
            try {
                ContentResolver resolver = context.getContentResolver();
                cursor = SqliteWrapper.query(context, resolver,
                        Telephony.Mms.CONTENT_URI,
                        MMS_MESSAGE_PROJECTION,
                        "thread_id=" + threadId + " AND locked=0",
                        null, "date DESC");     // get in newest to oldest order
                if (cursor == null) {
                    Log.e(TAG, "MMS: deleteMessagesForThread got back null cursor");
                    return;
                }

                int count = cursor.getCount();
                int numberToDelete = count - keep;
                if (LOCAL_DEBUG) {
                    Log.v(TAG, "MMS: deleteMessagesForThread keep: " + keep +
                            " count: " + count +
                            " numberToDelete: " + numberToDelete);
                }
                if (numberToDelete <= 0) {
                    return;
                }
                // Move to the keep limit and then delete everything older than that one.
                cursor.move(keep);
                latestDate = cursor.getLong(COLUMN_MMS_DATE);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            deleteMessagesOlderThanDate(context, threadId, latestDate);
        }

        private void deleteMessagesOlderThanDate(Context context, long threadId,
                long latestDate) {
            long cntDeleted = SqliteWrapper.delete(context, context.getContentResolver(),
                    Telephony.Mms.CONTENT_URI,
                    "thread_id=" + threadId + " AND locked=0 AND date<" + latestDate,
                    null);
            if (LOCAL_DEBUG) {
                Log.v(TAG, "MMS: deleteMessagesOlderThanDate cntDeleted: " + cntDeleted);
            }
        }

        protected void dumpMessage(Cursor cursor, Context context) {
            long id = cursor.getLong(COLUMN_ID);
            if (LOCAL_DEBUG) {
                Log.v(TAG, "Recycler message " +
                        "\n    id: " + id
                );
            }
        }

        @Override
        protected boolean anyThreadOverLimit(Context context) {
            Cursor cursor = getAllThreads(context);
            if (cursor == null) {
                return false;
            }
            int limit = getMessageLimit(context);
            try {
            	long threadId = 0L;
                while (cursor.moveToNext()) {
                	threadId = getThreadId(cursor);
                    ContentResolver resolver = context.getContentResolver();
                    Cursor msgs = SqliteWrapper.query(context, resolver,
                            Telephony.Mms.CONTENT_URI,
                            MMS_MESSAGE_PROJECTION,
                            "thread_id=" + threadId + " AND locked=0",
                            null, "date DESC");     // get in newest to oldest order
                    if (msgs == null) {
                        return false;
                    }
                    try {

                    if (msgs.getCount() >= limit) {
                        msgs.close();
                        return true;
                    }
                    } finally {

                    msgs.close();
                    }

                }
            } finally {
                cursor.close();
            }
            return false;
        }
    }

    public static class WapPushRecycler extends Recycler {
        private static final String[] ALL_WAPPUSH_THREADS_PROJECTION = {
            Threads._ID,
            Threads.MESSAGE_COUNT
        };

        private static final int ID             = 0;
        private static final int MESSAGE_COUNT  = 1;

        static private final String[] WAPPUSH_MESSAGE_PROJECTION = new String[] {
            WapPush._ID,
            WapPush.THREAD_ID,
            WapPush.ADDR,
            WapPush.URL,
            WapPush.DATE,
            WapPush.READ,
            WapPush.TYPE,
        };

        // The indexes of the default columns which must be consistent
        // with above PROJECTION.
        static private final int COLUMN_ID                  = 0;
        static private final int COLUMN_THREAD_ID           = 1;
        static private final int COLUMN_WAPPUSH_ADDRESS         = 2;
        static private final int COLUMN_WAPPUSH_URL             = 3;
        static private final int COLUMN_WAPPUSH_DATE            = 4;
        static private final int COLUMN_WAPPUSH_READ            = 5;
        static private final int COLUMN_WAPPUSH_TYPE            = 6;

        private static final Uri THREAD_URI =
            Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
        
        private final String MAX_SMS_MESSAGES_PER_THREAD = "MaxSmsMessagesPerThread";

        public int getMessageLimit(Context context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getInt(MAX_SMS_MESSAGES_PER_THREAD,
                    MmsConfig.getDefaultSMSMessagesPerThread());
        }

        public void setMessageLimit(Context context, int limit) {
            SharedPreferences.Editor editPrefs =
                PreferenceManager.getDefaultSharedPreferences(context).edit();
            editPrefs.putInt(MAX_SMS_MESSAGES_PER_THREAD, limit);
            editPrefs.commit();
        }

        protected long getThreadId(Cursor cursor) {
            return cursor.getLong(ID);
        }

        protected Cursor getAllThreads(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = SqliteWrapper.query(context, resolver,
            		THREAD_URI,
                    ALL_WAPPUSH_THREADS_PROJECTION, Threads.TYPE +" = "+ Threads.WAPPUSH_THREAD, null, WapPush.DEFAULT_SORT_ORDER);

            return cursor;
        }

        protected void deleteMessagesForThread(Context context, long threadId, int keep) {
            if (LOCAL_DEBUG) {
                Log.v(TAG, "WAPPUSH: deleteMessagesForThread");
            }
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = SqliteWrapper.query(context, resolver,
                        ContentUris.withAppendedId(WapPush.CONTENT_URI_THREAD, threadId),
                        WAPPUSH_MESSAGE_PROJECTION,
                        "locked=0",
                        null, "date ASC");     // get in oldest to newest order
                if (cursor == null) {
                    Log.e(TAG, "WAPPUSH: deleteMessagesForThread got back null cursor");
                    return;
                }
                int count = cursor.getCount();
                int numberToDelete = count - keep;
                if (LOCAL_DEBUG) {
                    Log.v(TAG, "WAPPUSH: deleteMessagesForThread keep: " + keep +
                            " count: " + count +
                            " numberToDelete: " + numberToDelete);
                }
                if (numberToDelete <= 0) {
                    return;
                }
               // Move to the keep limit and then delete everything older than that one.
                cursor.moveToPosition(numberToDelete);
                long latestDate = cursor.getLong(COLUMN_WAPPUSH_DATE);
                long delId = cursor.getLong(COLUMN_ID);

                long cntDeleted = SqliteWrapper.delete(context, resolver,
                        ContentUris.withAppendedId(WapPush.CONTENT_URI_THREAD, threadId),
                        "locked=0 AND date<" + latestDate,
                        null);
                if (LOCAL_DEBUG) {
                    Log.v(TAG, "WAPPUSH: deleteMessagesForThread cntDeleted: " + cntDeleted);
                }

                if(cntDeleted != numberToDelete){
                    cntDeleted = SqliteWrapper.delete(context, resolver,
                            ContentUris.withAppendedId(WapPush.CONTENT_URI_THREAD, threadId),
                            "locked=0 AND _id<" + delId,
                            null);
                }
                if (LOCAL_DEBUG) {
                    Log.v(TAG, "WAPPUSH: deleteMessagesForThread cntDeleted: " + cntDeleted);
                }
                
                    
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        protected void dumpMessage(Cursor cursor, Context context) {
            long date = cursor.getLong(COLUMN_WAPPUSH_DATE);
            String dateStr = MessageUtils.formatTimeStampString(context, date, true);
            if (LOCAL_DEBUG) {
                Log.v(TAG, "Recycler message " +
                        "\n    address: " + cursor.getString(COLUMN_WAPPUSH_ADDRESS) +
                        "\n    url: " + cursor.getString(COLUMN_WAPPUSH_URL) +
                        "\n    date: " + dateStr +
                        "\n    read: " + cursor.getInt(COLUMN_WAPPUSH_READ));
            }
        }

        @Override
        protected boolean anyThreadOverLimit(Context context) {
            Cursor cursor = getAllThreads(context);
            int limit = getMessageLimit(context);
            try {
            	long threadId = 0L;
                while (cursor.moveToNext()) {
                	threadId = getThreadId(cursor);
                    ContentResolver resolver = context.getContentResolver();
                    Cursor msgs = SqliteWrapper.query(context, resolver,
                            ContentUris.withAppendedId(WapPush.CONTENT_URI_THREAD, threadId),
                            WAPPUSH_MESSAGE_PROJECTION,
                            "locked=0",
                            null, "date DESC");     // get in newest to oldest order

                    if (msgs.getCount() >= limit) {
                        return true;
                    }
                }
            } finally {
                cursor.close();
            }
            return false;
        }
    }

}
