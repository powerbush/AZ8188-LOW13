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
import android.os.Message;
import android.os.Handler.Callback;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Contacts.Photo;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import com.android.contacts.mtk.*;
import com.mediatek.client.DataManager;
import com.mediatek.client.DataManager.WidgetEvent;
import com.mediatek.client.DataManager.SnsUser;
import android.util.Log;
import android.content.res.Resources;
import com.mediatek.wsp.util.EmotionParser;

/**
 * Asynchronously loads contact photos and maintains cache of photos.  The class is
 * mostly single-threaded.  The only two methods accessed by the loader thread are
 * {@link #cacheBitmap} and {@link #obtainPhotoIdsToLoad}. Those methods access concurrent
 * hash maps shared with the main thread.
 */
public class ContactSnsLoader implements Callback {

    private static final String TAG = "ContactSnsLoader";
    private static final String LOADER_THREAD_NAME = "ContactSnsLoader";

    /**
     * Type of message sent by the UI thread to itself to indicate that some photos
     * need to be loaded.
     */
    private static final int MESSAGE_REQUEST_SNS_LOADING = 1;

    /**
     * Type of message sent by the loader thread to indicate that some photos have
     * been loaded.
     */
    private static final int MESSAGE_SNS_LOADED = 2;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Maintains the state of a particular photo.
     */
    private static class StatusHolder {
        private static final int NEEDED = 0;
        private static final int LOADING = 1;
        private static final int LOADED = 2;

        int state;
        SoftReference<String> statusRef;
        SoftReference<String> logoRef;
    }

    /**
     * A soft cache for photos.
     */
    public final ConcurrentHashMap<Long, StatusHolder> mStatusCache =
            new ConcurrentHashMap<Long, StatusHolder>();

    /**
     * A map from ImageView to the corresponding photo ID. Please note that this
     * photo ID may change before the photo loading request is started.
     */
    private final ConcurrentHashMap<TextView, ImageView> mPendingRequests =
            new ConcurrentHashMap<TextView, ImageView>();
    private final ConcurrentHashMap<TextView, Long> mPendingRequests2 = 
	    new ConcurrentHashMap<TextView, Long>();

    /**
     * Handler for messages sent to the UI thread.
     */
    private final Handler mMainThreadHandler = new Handler(this);

    private LoaderThread mLoaderThread;

    private boolean mLoadingRequested;

    private boolean mPaused;

    private final Context mContext;
    
    private static boolean wait = false;

    private static final int KAIXIN_TYPE = 1;
    private static final int RENREN_TYPE = 2;
    private static final int TWITTER_TYPE = 3;
    private static final int FLICKR_TYPE = 4;
    private static final int FACEBOOK_TYPE = 5;

    public ContactSnsLoader(Context context) {
        mContext = context;
	//EmotionParser.init(0, context);
    }

    public void loadSns(ImageView logoView, TextView statusView, long contactID) {
	if(contactID == 0){
		logoView.setVisibility(View.GONE);
		statusView.setVisibility(View.GONE);
		mPendingRequests.remove(statusView);
		mPendingRequests2.remove(statusView);
	} else {
	    	boolean loaded = loadCachedSns(logoView, statusView, contactID);
		if(loaded){
			mPendingRequests.remove(statusView);
			mPendingRequests2.remove(statusView);
			//mStatusCache.remove(contactID);
		} else {
		  	mPendingRequests.put(statusView, logoView);
			mPendingRequests2.put(statusView, contactID);
	                if (!mPaused) {
	                    // Send a request to start loading photos
	                    requestLoading();
	                }
		}
	}
    }

    private boolean loadCachedSns(ImageView logoView, TextView statusView, long contactID) {
    	StatusHolder holder = mStatusCache.get(contactID);
        if (holder == null) {
            holder = new StatusHolder();
            mStatusCache.put(contactID, holder);
        } else if (holder.state == StatusHolder.LOADED) {
            if (holder.statusRef == null || holder.logoRef == null) {
		logoView.setVisibility(View.GONE);
		statusView.setVisibility(View.GONE);
                return true;
            }
	
            String logo = holder.logoRef.get();
            String status = holder.statusRef.get();
            if (logo != null) {
		if(KAIXIN_TYPE == Integer.parseInt(logo)){
                	logoView.setImageResource(R.drawable.logo_kaixin);
		}
		else if(RENREN_TYPE == Integer.parseInt(logo)){
			logoView.setImageResource(R.drawable.logo_renren);
		} 
		else if (TWITTER_TYPE == Integer.parseInt(logo)) {
			logoView.setImageResource(R.drawable.logo_twitter);
		} 
		else if (FLICKR_TYPE == Integer.parseInt(logo)) {
			logoView.setImageResource(R.drawable.logo_flickr);
		} 
		else if (FACEBOOK_TYPE == Integer.parseInt(logo)) {
			logoView.setImageResource(R.drawable.logo_facebook);
		} 
		else {
			// for other sns!
		}
		logoView.setVisibility(View.VISIBLE);
            }
	    if (status != null) {
		statusView.setText(ContactsManager.parserEmotion(status, Integer.parseInt(logo)));
		statusView.setVisibility(View.VISIBLE);
		return true;
	    }
            holder.logoRef = null;
            holder.statusRef = null;
        }

        // The bitmap has not been loaded - should display the placeholder image.
	logoView.setVisibility(View.GONE);
	statusView.setVisibility(View.GONE);
        holder.state = StatusHolder.NEEDED;
        return false;
    }

    /**
     * Stops loading images, kills the image loader thread and clears all caches.
     */
    public void stop() {
        pause();

        if (mLoaderThread != null) {
            mLoaderThread.quit();
            mLoaderThread = null;
        }

        mPendingRequests.clear();
	mPendingRequests2.clear();
        mStatusCache.clear();
    }

    public void clear() {
        mPendingRequests.clear();
	mPendingRequests2.clear();
        mStatusCache.clear();
    }

    /**
     * Temporarily stops loading photos from the database.
     */
    public void pause() {
        mPaused = true;
    }

    /**
     * Resumes loading photos from the database.
     */
    public void resume() {
        mPaused = false;
        if (!mPendingRequests.isEmpty() && !mPendingRequests.isEmpty()) {
            requestLoading();
        }
    }

    /**
     * Sends a message to this thread itself to start loading images.  If the current
     * view contains multiple image views, all of those image views will get a chance
     * to request their respective photos before any of those requests are executed.
     * This allows us to load images in bulk.
     */
    private void requestLoading() {
        if (!mLoadingRequested) {
            mLoadingRequested = true;
            mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_SNS_LOADING);
        }
    }

    /**
     * Processes requests on the main thread.
     */
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_REQUEST_SNS_LOADING: {
                mLoadingRequested = false;
                if (!mPaused) {
                    if (mLoaderThread == null) {
                        mLoaderThread = new LoaderThread(mContext.getContentResolver());
			mLoaderThread.setPriority(Thread.MIN_PRIORITY);
                        mLoaderThread.start();
                    }

                    mLoaderThread.requestLoading();
                }
                return true;
            }

            case MESSAGE_SNS_LOADED: {
                if (!mPaused) {
                    processLoadedSns();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Goes over pending loading requests and displays loaded photos.  If some of the
     * photos still haven't been loaded, sends another request for image loading.
     */
    private void processLoadedSns() {
	Iterator<TextView> iterator2 = mPendingRequests2.keySet().iterator();
	ImageView logoView = null;
	TextView statusView = null;
        while (iterator2.hasNext()) {
	    statusView = iterator2.next();
	    logoView = mPendingRequests == null ? null : mPendingRequests.get(statusView);
	    if(null == logoView) continue;
            long contactId = mPendingRequests2 == null ? -1 : mPendingRequests2.get(statusView);
            boolean loaded = loadCachedSns(logoView, statusView, contactId);
            if (loaded) {
		iterator2.remove();
            }
        }
        if (!mPendingRequests.isEmpty() && !mPendingRequests2.isEmpty()) {
            requestLoading();
        }
    }

    private void cacheSns(long id, String logo, String status) {
        if (mPaused) {
            return;
        }

        StatusHolder holder = new StatusHolder();
        holder.state = StatusHolder.LOADED;
        if (logo != null && status != null && status.length() != 0 && !status.equals("")) {
            try {
		holder.statusRef = new SoftReference<String>(status);
                holder.logoRef = new SoftReference<String>(logo);
            } catch (OutOfMemoryError e) {
                // Do nothing - the photo will appear to be missing
            }
        }
        mStatusCache.put(id, holder);
    }

    private void obtainContactIdsToLoad(ArrayList<Long> contactIds,
            ArrayList<String> contactIdsAsStrings) {
        contactIds.clear();
        contactIdsAsStrings.clear();

        Iterator<Long> iterator = mPendingRequests2.values().iterator();
        while (iterator.hasNext()) {
            Long id = iterator.next();
            StatusHolder holder = mStatusCache.get(id);
            if (holder != null && holder.state == StatusHolder.NEEDED) {
                holder.state = StatusHolder.LOADING;
                contactIds.add(id);
                contactIdsAsStrings.add(id.toString());
            }
        }
    }

    private class LoaderThread extends HandlerThread implements Callback {
        private final ContentResolver mResolver;
        private final StringBuilder mStringBuilder = new StringBuilder();
        private final ArrayList<Long> mContactIds = Lists.newArrayList();
        private final ArrayList<String> mContactIdsAsStrings = Lists.newArrayList();
        private Handler mLoaderThreadHandler;

        public LoaderThread(ContentResolver resolver) {
            super(LOADER_THREAD_NAME);
            mResolver = resolver;
        }

        /**
         * Sends a message to this thread to load requested photos.
         */
        public void requestLoading() {
            if (mLoaderThreadHandler == null) {
                mLoaderThreadHandler = new Handler(getLooper(), this);
            }
            mLoaderThreadHandler.sendEmptyMessage(0);
        }

        /**
         * Receives the above message, loads photos and then sends a message
         * to the main thread to process them.
         */
        public boolean handleMessage(Message msg) {
            loadSnsFromDatabase();
            mMainThreadHandler.sendEmptyMessage(MESSAGE_SNS_LOADED);
            return true;
        }

        private void loadSnsFromDatabase() {
            obtainContactIdsToLoad(mContactIds, mContactIdsAsStrings);

            int count = mContactIds.size();
            if (count == 0) {
                return;
            }

            try {
                MySnsUser[] snsUsers = ContactsManager.readMultiStatusFromWSP(mContactIds, mContext, 
            		ContactsListActivity.RAW_CONTACTS_PROJECTION2);

                if (snsUsers != null) {
			for(int i = 0; i < snsUsers.length; i++){
				Long contactID = snsUsers[i].contactID;
				String logo = null;
				String status = null;
				SnsUser user = snsUsers[i].mSnsUser;
				if(user != null && user.sns_id != null){
					logo = user.sns_id.toString();
					status = user.status;
				}
                        	cacheSns(contactID, logo, status);
                        	mContactIds.remove(contactID);
			}
                }
            } finally {
                
            }

            // Remaining photos were not found in the database - mark the cache accordingly.
            count = mContactIds.size();
            for (int i = 0; i < count; i++) {
                cacheSns(mContactIds.get(i), null, null);
            }
            
        }
    }
}
