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
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.contacts;

import com.google.android.collect.Lists;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Handler.Callback;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Contacts.Photo;
import android.widget.ImageView;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.mediatek.client.SnsClientAPI;
import com.mediatek.client.SnsRequest.REQUESTSTATUS;
import com.mediatek.client.UI_Request;
import com.mediatek.client.SendListener;
import com.mediatek.client.DataManager.SNSAccountInfo;
import android.util.Log;
import com.android.contacts.ViewContactActivity;
import android.provider.ContactsContract.RawContacts;
import android.widget.ListView;
import com.android.contacts.mtk.ContactsManager;
import com.mediatek.client.DataManager;
import com.mediatek.client.DataManager.WidgetEvent;
import com.mediatek.client.DataManager.SnsUser;
import com.mediatek.client.SnsRequest;
import com.mediatek.wsp.RequestManager;

public class ContactEventLoader implements Callback {

    private static final String TAG = "ContactEventLoader";
    private static final String LOADER_THREAD_NAME = "ContactEventLoader";

    private static final int MESSAGE_REQUEST_LOADING = 1;

    private static final int MESSAGE_EVENT_LOADED = 2;

    private final Handler mMainThreadHandler = new Handler(this);

    private LoaderThread mLoaderThread;

    private boolean mLoadingRequested;

    private boolean mPaused;

    private final Context mContext;

    private static int mReceivedCount = 0;
    private static int mSuccessCount = 0;
    private static int mProgramErrorCount = 0;
    private static int mNoLatestEventCount = 0;
    private static int mNetworkFailCount = 0;
    private SNSAccountInfo[] mAllAccountInfo = null;
    private ListView mEventListView = null;
    private UI_Request request = null;
    private static final int MESSAGE_ANIM_START = 1;
    private static final int MESSAGE_ANIM_STOP = 2;
    private int mResourceId = 0;
    private int[] mResource = null;
    private long mContactId = (long)-1; 
    static final String[] RAW_CONTACTS_PROJECTION2 = new String[] {
	RawContacts._ID, // 0
	RawContacts.CONTACT_ID, // 1
	RawContacts.ACCOUNT_TYPE, // 2
	RawContacts.ACCOUNT_NAME, // 3
	RawContacts.SYNC3, // 4
	RawContacts.SYNC4, // 5
    };
    Handler mAnimHandler = null;
    private ViewContactActivity mActivity = null;

    public ContactEventLoader(Context context, ListView view) {
        mEventListView = view;
        mContext = context;
    }

    public void loadEvent(Handler animHandler, int resourceId, int[] resource, long contactId, ViewContactActivity activity) {
	mReceivedCount = 0;
	mSuccessCount = 0;
	mProgramErrorCount = 0;
	mNetworkFailCount = 0;
	mNoLatestEventCount = 0;
        request = null;
	mAnimHandler = animHandler;
	if(null == mAnimHandler) return;
	mAnimHandler.sendEmptyMessage(MESSAGE_ANIM_START);
	mResourceId = resourceId;
	mResource = resource;
	mContactId = contactId;
	mActivity = activity;
	SnsRequest.setRequestManager((RequestManager)activity.getApplicationContext());
	UI_Request.setContentResolver(mContext.getContentResolver());
	request = UI_Request.getInstance();
	if(null == request) {
		mAnimHandler.sendEmptyMessage(MESSAGE_ANIM_STOP);
		return;
	}
	mAllAccountInfo = DataManager.getAllSnsAccountInfo();
	if(null == mAllAccountInfo || 0 == mAllAccountInfo.length){
		mAnimHandler.sendEmptyMessage(MESSAGE_ANIM_STOP);
		return;
	}
	requestLoading();
    }

    public void stop() {
        pause();

        if (mLoaderThread != null) {
            mLoaderThread.quit();
            mLoaderThread = null;
        }
    }

    public void pause() {
        mPaused = true;
    }

    public void resume() {
        mPaused = false;
        requestLoading();
    }

    private void requestLoading() {
        if (!mLoadingRequested) {
            mLoadingRequested = true;
            mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_REQUEST_LOADING: {
                mLoadingRequested = false;
                if (!mPaused) {
                    if (mLoaderThread == null) {
                        mLoaderThread = new LoaderThread(mContext.getContentResolver());
                        mLoaderThread.start();
                    }

                    if (mLoaderThread != null) mLoaderThread.requestLoading();
                }
                return true;
            }

            case MESSAGE_EVENT_LOADED: {
                if (!mPaused) {
			mAnimHandler.sendEmptyMessage(MESSAGE_ANIM_STOP);
                }
                return true;
            }
        }
        return false;
    }

    private class LoaderThread extends HandlerThread implements Callback {
        private final ContentResolver mResolver;
        private final StringBuilder mStringBuilder = new StringBuilder();
        private final ArrayList<Long> mPhotoIds = Lists.newArrayList();
        private final ArrayList<String> mPhotoIdsAsStrings = Lists.newArrayList();
        private Handler mLoaderThreadHandler;

	private SendListener sendListener = new SendListener()
    	{

		public void onSend(REQUESTSTATUS arg0) {
			mReceivedCount++;
			// if update successful
			switch(arg0)
			{
			case SUCCESS:
				mSuccessCount++;
				break;
			case NODATAGET:
				mNoLatestEventCount++;
				break;
			case ERROR:
				mProgramErrorCount++;
				break;
			case NETWORKFAIL:
				mNetworkFailCount++;
				break;
			}
			if(mReceivedCount == mAllAccountInfo.length)
			{
				mMainThreadHandler.sendEmptyMessage(MESSAGE_EVENT_LOADED);
			}
		}

    	};

        public LoaderThread(ContentResolver resolver) {
            super(LOADER_THREAD_NAME);
            mResolver = resolver;
        }

        public void requestLoading() {
            if (mLoaderThreadHandler == null) 
            {
            	Looper looper =  getLooper();
            	if(looper != null)
            	{
            		mLoaderThreadHandler = new Handler(looper, this);
            	}
            }
            mLoaderThreadHandler.sendEmptyMessage(0);
        }

        public boolean handleMessage(Message msg) {
            loadEventFromWeb();
            return true;
        }

        private void loadEventFromWeb() 
        {
        	if(null == mAllAccountInfo || 0 == mAllAccountInfo.length)
        	{
        		if(mMainThreadHandler != null)
        		{
        			mMainThreadHandler.sendEmptyMessage(MESSAGE_EVENT_LOADED);
        		}
        		return;
        	}
        	
		    for(int i = 0; i < mAllAccountInfo.length; i++) 
		    {
		    	try 
		    	{
		    		if(mAllAccountInfo[i] != null && mAllAccountInfo[i].account_id != null)
					request.updateEvents(mAllAccountInfo[i].account_id, null, sendListener);
		    	}
		    	catch (Exception e) 
		    	{
		    		Log.i(TAG, "------------------------> updateEventList Exception");
		    		e.printStackTrace();
		    	}
		    }
		}
    }
}
