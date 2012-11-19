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

import com.mediatek.client.DataManager;
import com.mediatek.client.DataManager.WidgetEvent;
import com.mediatek.client.DataManager.SnsUser;
import android.util.Log;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.AggregationExceptions;
import android.provider.ContactsContract;
import java.io.InputStream;
import com.android.contacts.mtk.*;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.ContentProviderOperation;
import com.mediatek.client.DataManager.SnsUser;
import com.mediatek.client.DataManager.SNSAccountInfo;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import com.android.internal.widget.ContactHeaderWidget;
import android.widget.QuickContactBadge;
import com.mediatek.wsp.util.EmotionParser;

public class ContactOwnerLoader implements Callback {

    private static final String TAG = "ContactOwnerLoader";
    private static final String LOADER_THREAD_NAME = "ContactOwnerLoader";

    private static final String CREATOR_THREAD_NAME = "ContactOwnerCreator";

    private static final int MESSAGE_REQUEST_OWNER_LOADING = 1;

    private static final int MESSAGE_OWNER_LOADED = 2;

    private static final int MESSAGE_REQUEST_OWNER_CREATING = 3;

    private static final int MESSAGE_OWNER_CREATED = 4;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final int mDefaultResourceId;

    private final String mDefaultName;

    private final Handler mMainThreadHandler = new Handler(this);

    private LoaderThread mLoaderThread;

    private CreatorThread mCreatorThread;

    private boolean mLoadingRequested;

    private boolean mPaused;

    private final Context mContext;

    private View mOwnerView;
    private ContactHeaderWidget mHeaderView;
    private View mSnsAccountPad;
    private View mSnsPhoto;
    private View mSnsUpdate;
    private View mStatuEditor;
    private Handler mUpdateLayoutHandler;

    private static final int KAIXIN_TYPE = 1;
    private static final int RENREN_TYPE = 2;
    private static final int TWITTER_TYPE = 3;
    private static final int FLICKR_TYPE = 4;
    private static final int FACEBOOK_TYPE = 5;
    private int switcher = 0;
    private int MESSAGE_UPDATE_LAYOUT = 1;

    public ContactOwnerLoader(Context context, int defaultResourceId, String defaultName) {
	mDefaultResourceId = defaultResourceId;
	mDefaultName = defaultName;
        mContext = context;
	//EmotionParser.init(0, context);
    }

    public boolean loadOwner(View ownerView, PhoneOwner owner) {
	mOwnerView = ownerView;
	switcher = 1;
	Log.i(TAG, "owner: " + owner);
	if(owner == null){
		requestCreating();
	} else {
		requestLoading();
	}
	return true;
    }

    public boolean loadOwnerHeader(ContactHeaderWidget headerView, View snsPad, View snsUpdate, 
		View snsPhoto, View statuEditor, PhoneOwner owner, Handler updateLayoutHandler) {
	mHeaderView = headerView;
	mSnsAccountPad = snsPad;
	mStatuEditor = statuEditor;
	mSnsPhoto = snsPhoto;
	mSnsUpdate = snsUpdate;
	mUpdateLayoutHandler = updateLayoutHandler;

	switcher = 2;
	Log.i(TAG, "owner: " + owner);
	if(owner == null){
		requestCreating();
	} else {
		requestLoading();
	}
	return true;
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

    /**
     * Resumes loading photos from the database.
     */
    public void resume() {
        mPaused = false;
        //requestLoading();
    }

    private void requestLoading() {
        if (!mLoadingRequested) {
            mLoadingRequested = true;
            mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_OWNER_LOADING);
        }
    }

    private void requestCreating(){
	if(!mLoadingRequested) {
		mLoadingRequested = true;
		mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_OWNER_CREATING);
	}
    }
    /**
     * Processes requests on the main thread.
     */
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_REQUEST_OWNER_LOADING: {
                mLoadingRequested = false;
                if (!mPaused) {
                    if (mLoaderThread == null) {
                        mLoaderThread = new LoaderThread(mContext.getContentResolver());
                        mLoaderThread.start();
                    }

                    mLoaderThread.requestLoading();
                }
                return true;
            }

            case MESSAGE_OWNER_LOADED: {
                if (!mPaused) {
                    processOwner();
                }
                return true;
            }
		
	    case MESSAGE_REQUEST_OWNER_CREATING: {
                mLoadingRequested = false;
		if(!mPaused){
                    if (mCreatorThread == null) {
                        mCreatorThread = new CreatorThread(mContext.getContentResolver());
                        mCreatorThread.start();
                    }
                    mCreatorThread.requestCreating();
		}
                return true;
	    }

	    case MESSAGE_OWNER_CREATED: {
                if (!mPaused) {
                    processOwner();
                }
                return true;
	    }
        }
        return false;
    }

    private boolean loadCachedOwner(View ownerView, PhoneOwner owner){
	QuickContactBadge quickContact = ((ContactListItemView)ownerView).getQuickContact();
	if(null != quickContact) quickContact.assignContactUri(Contacts.getLookupUri(owner == null ? (long)-1 : owner.getOwnerID(), 
		owner == null ? null : owner.getOwnerLookupKey()));
	ImageView ownerPhoto = quickContact;
	TextView info = (TextView) ((ContactListItemView)ownerView).getNameTextView();
 	ImageView snsLogo = (ImageView) ((ContactListItemView)ownerView).getSnsLogo();
	TextView snsStatu = (TextView) ((ContactListItemView)ownerView).getSnsStatus();
	ContentResolver cr = null;
	Cursor cursor = null;
	String contactId = "";
	Uri uri = null;
	Bitmap ownerIcon = null;
	String name = "";
	Bitmap bitmap = null;

	if(ownerPhoto != null)
		ownerPhoto.setImageResource(mDefaultResourceId);
        if(info != null){
		info.setText(mDefaultName);
        	info.setVisibility(View.VISIBLE);
	}
        if(snsLogo != null)
		snsLogo.setVisibility(View.GONE);
        if(snsStatu != null)
		snsStatu.setVisibility(View.GONE);
	
	cr = mContext.getContentResolver();  
	if(null == owner) 
		return true;
	cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, new String[]{Contacts._ID, Contacts.DISPLAY_NAME},  
		Contacts._ID + " = '" + owner.getOwnerID() + "'", null, null);
	if(null == cursor || !cursor.moveToFirst()){
		PhoneOwner.setOwner(null);
		if(null != cursor) cursor.close();
		return true;
	} else {
		contactId = cursor.getString(0);
		if(null == contactId)
			return true;
		name = cursor.getString(1);
		uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,  
			Long.parseLong(contactId));
		InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);  
		ownerIcon = BitmapFactory.decodeStream(input);
		if(null != cursor) cursor.close();
		SnsUser curContact = ContactsManager.readStatusFromWSP(owner.getOwnerID(), 
        			mContext, ContactsListActivity.RAW_CONTACTS_PROJECTION2);
		if(null == name || name.length() == 0) {
			name = mContext.getText(android.R.string.unknownName) + "";
		}
		owner.setName(name);
		if(null != info){
			info.setText(owner.getName());
        		info.setVisibility(View.VISIBLE);
		}
		if(null != ownerPhoto){
			if(null == ownerIcon){
				ownerPhoto.setImageResource(R.drawable.ic_contact_list_picture);
			}
			else{
				ownerPhoto.setImageBitmap(ownerIcon);
			}
		}
		if(null == curContact || null == curContact.status || 
			curContact.status.length() == 0 || curContact.status.equals(""))
			return true;
		owner.setStatus(curContact.status);
        	owner.setSnsLogo(curContact.snsUrl);
        	
        	if(null != snsLogo){
        		if(KAIXIN_TYPE == curContact.sns_id){
				snsLogo.setImageResource(R.drawable.logo_kaixin);
			} else if (RENREN_TYPE == curContact.sns_id) {
				snsLogo.setImageResource(R.drawable.logo_renren);
			} else if (TWITTER_TYPE == curContact.sns_id) {
				snsLogo.setImageResource(R.drawable.logo_twitter);
			} else if (FLICKR_TYPE == curContact.sns_id) {
				snsLogo.setImageResource(R.drawable.logo_flickr);
			} else if (FACEBOOK_TYPE == curContact.sns_id) {
				snsLogo.setImageResource(R.drawable.logo_facebook);
			} else {
				// for other sns!
			}
			snsLogo.setVisibility(View.VISIBLE);
		}
        	if(null != snsStatu){
			snsStatu.setVisibility(View.VISIBLE);
			snsStatu.setText(ContactsManager.parserEmotion(owner.getStatus(),curContact.sns_id));
		}
                return true;
	}
    }

    private boolean loadCachedOwnerHeader(ContactHeaderWidget headerView, PhoneOwner owner){
	ContentResolver cr = null;
	Cursor cursor = null;
	String contactId = "";
	Uri uri = null;
	Bitmap ownerIcon = null;
	String name = "";
	Bitmap bitmap = null;

	if(null != headerView){
		ownerIcon = BitmapFactory.decodeResource(mContext.getResources(), mDefaultResourceId);
		headerView.setPhoto(ownerIcon);
		headerView.setDisplayName(mDefaultName, "");
		headerView.setSnsPadVisibility(View.GONE);
	}
	
	cr = mContext.getContentResolver();  
	if(null == owner) return true;
	cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, new String[]{Contacts._ID, Contacts.DISPLAY_NAME},  
		Contacts._ID + " = '" + owner.getOwnerID() + "'", null, null);
	if(null == cursor || !cursor.moveToFirst()){
		PhoneOwner.setOwner(null);
		if(null != cursor) cursor.close();
		return true;
	} else {
		contactId = cursor.getString(0);
		if(null == contactId)
			return true;
		name = cursor.getString(1);
		uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,  
			Long.parseLong(contactId));
		InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);  
		ownerIcon = BitmapFactory.decodeStream(input);
		if(null != cursor) cursor.close();
		SnsUser curContact = ContactsManager.readStatusFromWSP(owner.getOwnerID(), 
        			mContext, ContactsListActivity.RAW_CONTACTS_PROJECTION2);
		if(null == name || name.length() == 0) {
			name = mContext.getText(android.R.string.unknownName) + "";
		}
		owner.setName(name);
		if(null != headerView){
			headerView.setDisplayName(owner.getName(), "");
		}
		    if(null == curContact || null == curContact.status || curContact.status.length() == 0 || !curContact.status.equals("")) {
			    
		    }
		    else {
		if(null != mSnsAccountPad) mSnsAccountPad.setVisibility(View.GONE);
		if(null != mStatuEditor) mStatuEditor.setVisibility(View.VISIBLE);
		if(null != mSnsPhoto) mSnsPhoto.setVisibility(View.VISIBLE);
		if(null != mSnsUpdate) mSnsUpdate.setVisibility(View.VISIBLE);

		owner.setStatus(curContact.status);
        	owner.setSnsLogo(curContact.snsUrl);
        	if(null != headerView){
			if(null == ownerIcon){
				ownerIcon = BitmapFactory.decodeResource(
					mContext.getResources(), mDefaultResourceId);
				headerView.setPhoto(ownerIcon);
			}
			else{
				headerView.setPhoto(ownerIcon);
			}
			Bitmap logo;
        		if(KAIXIN_TYPE == curContact.sns_id){
				logo = BitmapFactory.decodeResource(
					mContext.getResources(), R.drawable.logo_kaixin);
				headerView.setSnsLog(logo);
			} else if (RENREN_TYPE == curContact.sns_id) {
				logo = BitmapFactory.decodeResource(
					mContext.getResources(), R.drawable.logo_renren);
				headerView.setSnsLog(logo);
			} else if (TWITTER_TYPE == curContact.sns_id) {
				logo = BitmapFactory.decodeResource(
					mContext.getResources(), R.drawable.logo_twitter);
				headerView.setSnsLog(logo);
			} else if (FLICKR_TYPE == curContact.sns_id) {
				logo = BitmapFactory.decodeResource(
					mContext.getResources(), R.drawable.logo_flickr);
				headerView.setSnsLog(logo);
			} else if (FACEBOOK_TYPE == curContact.sns_id) {
				logo = BitmapFactory.decodeResource(
					mContext.getResources(), R.drawable.logo_facebook);
				headerView.setSnsLog(logo);
			} else {
				// for other sns!
			}
			headerView.setStatusText(ContactsManager.parserEmotion(owner.getStatus(),curContact.sns_id));
			headerView.setSnsPadVisibility(View.VISIBLE);
		}
		}
		
		mUpdateLayoutHandler.sendEmptyMessage(MESSAGE_UPDATE_LAYOUT);
                return true;
	}
    }

    private void processOwner() {
	PhoneOwner owner = PhoneOwner.getInstance();
	boolean loaded;
	if(1 == switcher) {
		loaded = loadCachedOwner(mOwnerView, owner);
	} else if(2 == switcher) {
		loaded = loadCachedOwnerHeader(mHeaderView, owner);
	}
    }

    private class LoaderThread extends HandlerThread implements Callback {
        private final ContentResolver mResolver;
        private final StringBuilder mStringBuilder = new StringBuilder();
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
            loadOwnerFromDatabase();
            mMainThreadHandler.sendEmptyMessage(MESSAGE_OWNER_LOADED);
            return true;
        }

	private boolean checkOwnerAccount(SNSAccountInfo[] accounts){
		PhoneOwner owner = PhoneOwner.getInstance();
		if(null == owner) return false;
		Cursor tempCursor = mResolver.query(RawContacts.CONTENT_URI,
			new String[] {RawContacts.SYNC4}, 
			RawContacts.CONTACT_ID + "=" + owner.getOwnerID(), null, null);
		ArrayList<String> userIDs = new ArrayList<String>();
		String userID = null;
		if(tempCursor == null){
			owner.setOwner(null);
			return false;
		}
		while (tempCursor.moveToNext()) {
			userIDs.add(tempCursor.getString(0) + ""); 
		}
		if(null != tempCursor) tempCursor.close();
		for(int i = 0; accounts != null && i < accounts.length; i++){
			if(accounts[i] != null && accounts[i].user_id != null)
				userID = accounts[i].user_id;
			else 
				userID = "-1";
			if(!userIDs.contains(userID))
				return false;
		}
		return true;
	}

        private void loadOwnerFromDatabase() {
	    Cursor cursor = null;
            try {
		Long ownerID = (long)-1;
		DataManager.setContentResolver(mResolver);
		SNSAccountInfo[] accounts = DataManager.getAllSnsAccountInfo();
		SNSAccountInfo[] accountsWithoutTwitter = null;
		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		ContentProviderOperation.Builder builder = null;
		StringBuilder stringBuilder = new StringBuilder();
		ArrayList<String> userIdAsStrings = Lists.newArrayList();
		Long[] rawIds = null;
		Long rawId1;
		Long rawId2;
		int i = 0;
		String oneID;
		Uri rawContactUri = null;
		Uri contactLookupUri = null;
		if(null == accounts || 0 >= accounts.length){
			return;
		}
		else{
			int tmpIndex = 0;
			int tmpSize = 0;
			SNSAccountInfo tmpAccount = null;
			for(i = 0; accounts != null && i < accounts.length; i++) {
				tmpAccount = accounts[i];
				if(tmpAccount.sns_id == TWITTER_TYPE) continue;
				tmpSize++;
			}
			accountsWithoutTwitter = new SNSAccountInfo[tmpSize];
			for(i = 0; accounts != null && i < accounts.length; i++) {
				tmpAccount = accounts[i];
				if(tmpAccount.sns_id == TWITTER_TYPE) continue;
				accountsWithoutTwitter[tmpIndex] = tmpAccount;
				tmpIndex++;
			}
			if(checkOwnerAccount(accountsWithoutTwitter))
				return;
			if(accountsWithoutTwitter[0] != null && accountsWithoutTwitter[0].user_id != null)
				oneID = accountsWithoutTwitter[0].user_id;
			else
				oneID = "-1";

			stringBuilder.setLength(0);
			stringBuilder.append(RawContacts.SYNC4 + " IN(");
			for (i = 0; accountsWithoutTwitter != null && i < accountsWithoutTwitter.length; i++) {
				if (i != 0) {
					stringBuilder.append(',');
				}
				stringBuilder.append('?');
				if(accountsWithoutTwitter[i] == null || accountsWithoutTwitter[i].user_id == null)
					userIdAsStrings.add("-1");
				else
				userIdAsStrings.add(accountsWithoutTwitter[i].user_id);
			}
			stringBuilder.append(')');
			cursor = mResolver.query(RawContacts.CONTENT_URI,
                        	new String[] { RawContacts._ID },
                        	stringBuilder.toString() + " and " + RawContacts.DELETED + " = 0",
                        	userIdAsStrings.toArray(EMPTY_STRING_ARRAY),
                        	null);
			if (null != cursor) {
				Long rawId;
				Long contactId;
				rawIds = new Long[cursor.getCount()];
				i = 0;
				
		            	while (cursor.moveToNext()) {
		                	rawId = cursor.getLong(0);
					rawIds[i++] = rawId;
		            	}
               	 	} else 
				return;
			i = 0;
			if (cursor != null) cursor.close();
			
			while(i < rawIds.length - 1){
				rawId1 = rawIds[i];
				rawId2 = rawIds[i + 1];
  				operationList.add(ContentProviderOperation.newUpdate(AggregationExceptions.CONTENT_URI)
					.withValue(AggregationExceptions.RAW_CONTACT_ID1, rawId1)
					.withValue(AggregationExceptions.RAW_CONTACT_ID2, rawId2)
					.withValue(AggregationExceptions.TYPE, AggregationExceptions.TYPE_KEEP_TOGETHER)
					.build());
				i++;
			}
			mResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
			cursor = mResolver.query(RawContacts.CONTENT_URI, new String[] { RawContacts.CONTACT_ID },
                        	RawContacts.SYNC4 + " IN (?) and " + RawContacts.DELETED + " = 0", 
				new String[]{ oneID.toString() }, null);
			if (null != cursor) {
		            	if (cursor.moveToFirst()) {
		                	ownerID = cursor.getLong(0);
		            	} else {
                			if (cursor != null) cursor.close();
					return;
				}
               	 	} else{
				return;
			}
			if (cursor != null) cursor.close();
			if(null == ownerID || (long)-1 == ownerID) return;
			rawContactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, ownerID);
			if(null == rawContactUri) return;
                        contactLookupUri = RawContacts.getContactLookupUri(mResolver, rawContactUri);
			if(null == contactLookupUri) {
				if (cursor != null) cursor.close();
				return;
			}
			PhoneOwner.initPhoneOwner(ownerID, contactLookupUri.getPathSegments().get(2));
		}

            } catch(Exception e) {
	    	e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
            }
        }
    }

    private class CreatorThread extends HandlerThread implements Callback {
        private final ContentResolver mResolver;
        private final StringBuilder mStringBuilder = new StringBuilder();
        private Handler mCreatorThreadHandler;

        public CreatorThread(ContentResolver resolver) {
            super(CREATOR_THREAD_NAME);
            mResolver = resolver;
        }

        /**
         * Sends a message to this thread to load requested photos.
         */
        public void requestCreating() {
            if (null == mCreatorThreadHandler) {
                mCreatorThreadHandler = new Handler(getLooper(), this);
            }
            mCreatorThreadHandler.sendEmptyMessage(0);
        }

        /**
         * Receives the above message, loads photos and then sends a message
         * to the main thread to process them.
         */
        public boolean handleMessage(Message msg) {
            createOwnerFromDatabase();
            mMainThreadHandler.sendEmptyMessage(MESSAGE_OWNER_CREATED);
            return true;
        }

        private void createOwnerFromDatabase() {
            //obtainContactIdsToLoad(mContactIds, mContactIdsAsStrings);
	    Cursor cursor = null;
            try {
		Long ownerID = (long)-1;
		DataManager.setContentResolver(mResolver);
		SNSAccountInfo[] accounts = DataManager.getAllSnsAccountInfo();
		SNSAccountInfo[] accountsWithoutTwitter = null;
		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		ContentProviderOperation.Builder builder = null;
		StringBuilder stringBuilder = new StringBuilder();
		ArrayList<String> userIdAsStrings = Lists.newArrayList();
		Long[] rawIds = null;
		Long rawId1;
		Long rawId2;
		int i = 0;
		String oneID;
		Uri rawContactUri = null;
		Uri contactLookupUri = null;
		if(null == accounts || 0 >= accounts.length){
			return;
		}
		else{
			int tmpIndex = 0;
			int tmpSize = 0;
			SNSAccountInfo tmpAccount = null;
			for(i = 0; accounts != null && i < accounts.length; i++) {
				tmpAccount = accounts[i];
				if(tmpAccount.sns_id == TWITTER_TYPE) continue;
				tmpSize++;
			}
			accountsWithoutTwitter = new SNSAccountInfo[tmpSize];
			for(i = 0; accounts != null && i < accounts.length; i++) {
				tmpAccount = accounts[i];
				if(tmpAccount.sns_id == TWITTER_TYPE) continue;
				accountsWithoutTwitter[tmpIndex] = tmpAccount;
				tmpIndex++;
			}
			if(accountsWithoutTwitter[0] != null && accountsWithoutTwitter[0].user_id != null)
				oneID = accountsWithoutTwitter[0].user_id;
			else
				oneID = "-1";
			stringBuilder.setLength(0);
			stringBuilder.append(RawContacts.SYNC4 + " IN(");
			for (i = 0; accountsWithoutTwitter != null && i < accountsWithoutTwitter.length; i++) {
				if (i != 0) {
					stringBuilder.append(',');
				}
				stringBuilder.append('?');
				if(accountsWithoutTwitter[i] == null || accountsWithoutTwitter[i].user_id == null)
					userIdAsStrings.add("-1");
				else
				userIdAsStrings.add(accountsWithoutTwitter[i].user_id);
			}
			stringBuilder.append(')');
			cursor = mResolver.query(RawContacts.CONTENT_URI,
                        	new String[] { RawContacts._ID },
                        	stringBuilder.toString() + " and " + RawContacts.DELETED + " = 0",
                        	userIdAsStrings.toArray(EMPTY_STRING_ARRAY),
                        	null);
			if (null != cursor) {
				Long rawId;
				Long contactId;
				rawIds = new Long[cursor.getCount()];
				i = 0;
		            	while (cursor.moveToNext()) {
		                	rawId = cursor.getLong(0);
					rawIds[i++] = rawId;
		            	}
               	 	} else 
				return;
			i = 0;
			if (cursor != null) cursor.close();
			while(i < rawIds.length - 1){
				rawId1 = rawIds[i];
				rawId2 = rawIds[i + 1];
  				operationList.add(ContentProviderOperation.newUpdate(AggregationExceptions.CONTENT_URI)
					.withValue(AggregationExceptions.RAW_CONTACT_ID1, rawId1)
					.withValue(AggregationExceptions.RAW_CONTACT_ID2, rawId2)
					.withValue(AggregationExceptions.TYPE, AggregationExceptions.TYPE_KEEP_TOGETHER)
					.build());
				i++;
			}
			mResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
			cursor = mResolver.query(RawContacts.CONTENT_URI, new String[] { RawContacts.CONTACT_ID },
                        	RawContacts.SYNC4 + " IN (?) and " + RawContacts.DELETED + " = 0", 
				new String[]{ oneID.toString() }, null);
			if (null != cursor) {
		            	if (cursor.moveToFirst()) {
		                	ownerID = cursor.getLong(0);
		            	} else {
                			if (cursor != null) cursor.close();
					return;
				}
               	 	} else{
				return;
			}
			if (cursor != null) cursor.close();
			if(null == ownerID || (long)-1 == ownerID) return;
			rawContactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, ownerID);
			if(null == rawContactUri) return;
                        contactLookupUri = RawContacts.getContactLookupUri(mResolver, rawContactUri);
			if(null == contactLookupUri) {
				if (cursor != null) cursor.close();
				return;
			}
			PhoneOwner.initPhoneOwner(ownerID, contactLookupUri.getPathSegments().get(2));
		}

            } catch(Exception e) {
	    	e.printStackTrace();
            } finally {
                if (cursor != null) cursor.close();
            }
        }
    }
}
