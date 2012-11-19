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

package com.android.contacts;

import com.android.contacts.model.ContactsSource;
import com.android.contacts.model.EntitySet;
import com.android.contacts.ui.widget.SimIconView;
import com.android.contacts.util.Constants;
import com.android.contacts.util.Constants.SimInfo;
import android.accounts.Account;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.sip.SipManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.Telephony;
import android.provider.Telephony.SIMInfo;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.android.internal.telephony.ITelephony;
import android.content.ComponentName;

import com.mediatek.CellConnService.CellConnMgr;
import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.telephony.PhoneNumberFormatUtilEx;
import com.mediatek.telephony.PhoneNumberFormattingTextWatcherEx;
import android.os.SystemProperties;

import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.TelephonyIntents;

public class ContactsUtils {
    private static final String TAG = "ContactsUtils";
    private static final String WAIT_SYMBOL_AS_STRING = String.valueOf(PhoneNumberUtils.WAIT);
    private static final String PREFERENCE_SIM_CONTACTS_READY = "sim_contacts_ready";
    private static final String PREFERENCE_KEY_SIM_CONTACTS_1 = "sim_contacts_1";
    private static final String PREFERENCE_KEY_SIM_CONTACTS_2 = "sim_contacts_2";
    private static AlertDialog mCallAlertDialog = null;
    private static final int TITLE_LENGTH = 15;	

    // dial voice call
    public static final int DIAL_TYPE_VOICE = 0;
    // dial video call
    public static final int DIAL_TYPE_VIDEO = 1;
    // dial sip call
    public static final int DIAL_TYPE_SIP   = 2;
    // internal use for dialer
    public static final int DIAL_TYPE_AUTO  = 3;

    // sim id of sip call in the call log database
    public static final int CALL_TYPE_SIP   = -2;
    // empty sim id
    public static final int CALL_TYPE_NONE  = 0;

    public static String mOptr = null;
    public static String mSpec = null;
    public static String mSeg = null;

    private static int mPHB1_ready = 0;
    private static int mPHB2_ready = 0;
    private static int mPHB_ready = 0;
    
    private static int mSIM1_start = 0;
    private static int mSIM2_start = 0;
    private static int mSIM_start = 0;
    
    // mtk80909 for Speed Dial
    public static boolean SPEED_DIAL = true;
    static boolean[] isServiceRunning = {false, false};

        static final Uri mIccUri = Uri.parse("content://icc/adn/");//80794   
	static final Uri mIccUri1 = Uri.parse("content://icc/adn1/");
	static final Uri mIccUri2 = Uri.parse("content://icc/adn2/");
	
	static final Uri mIccUsimUri = Uri.parse("content://icc/pbr");//80794   for CU operator
	static final Uri mIccUsim1Uri = Uri.parse("content://icc/pbr1/");
	static final Uri mIccUsim2Uri = Uri.parse("content://icc/pbr2/");
	
	private static boolean mMoreButtonShouldExpand = false;
	
	private static Intent mIntent = null;
	
	private static ArrayList<Account> mAccounts = null;
	
	private static String mAccountName = null;
	private static EntitySet mState = null;
	private static boolean mMode = false;
	
	private static ContentValues mValues = null;
	
	private static long mRawContactId = -1;
	private static long mContactId = -1;
    
    /* added by xingping.zheng start */
	private static final int MAX_SIM_ID = 99;
    private static int[] mSlotsMap = new int[MAX_SIM_ID+1];
    private static Dialog mCallSelectionDialog;
    private static Dialog mCallSelectionDialogOther;
    private static Dialog mSimSelectionDialog;
    private static Dialog mTurnOn3GServiceDialog;
    /*final*/ static ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));

    private static Dialog mTurnOnSipDialog;
    /* added by xingping.zheng end   */
	
	public static void setSim1Ready(int PHB1_ready) {
    	mPHB1_ready = PHB1_ready;
    }
    
    public static void setSim2Ready(int PHB2_ready) {
    	mPHB2_ready = PHB2_ready;
    }
    
    public static void setSimReady(int PHB_ready) {
    	mPHB_ready = PHB_ready;
    }
    
    public static int getSim1Ready() {
    	return mPHB1_ready;
    }
    
    public static int getSim2Ready() {
    	return mPHB2_ready;
    }
    
    public static int getSimReady() {
    	return mPHB_ready;
    }
    
    public static void setSim1Start(int SIM1_start) {
    	mSIM1_start = SIM1_start;
    }
    
    public static void setSim2Start(int SIM2_start) {
    	mSIM2_start = SIM2_start;
    }
    
    public static void setSimStart(int SIM_start) {
    	mSIM_start = SIM_start;
    }
    
    public static int getSim1Start() {
    	return mSIM1_start;
    }
    
    public static int getSim2Start() {
    	return mSIM2_start;
    }
    
    public static int getSimStart() {
    	return mSIM_start;
    }
    
    public static void setMoreButtonStatus(boolean shouldExpand) {//if shouldExpand is true, More button should expand
    	mMoreButtonShouldExpand = shouldExpand;
    }
    
    public static boolean getMoreButtonStatus() {
    	return mMoreButtonShouldExpand;
    }

	public static int getSlotId(Context context, int simId) {
		SIMInfo simInfo = SIMInfo.getSIMInfoById(context, simId);
		int slotId = -1;
		if (simInfo != null) {
			slotId = simInfo.mSlot;
			Log.i(TAG, "slotId is " + slotId);
    	}
		return slotId;
	}

	public static Uri getUri(int slotId) {
		Uri uri = null;
                //we must certify the ITelephony is Alive, so now we get the ITelephony again.
		iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
		try {
			if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
				if (slotId == com.android.internal.telephony.Phone.GEMINI_SIM_1) {
					if (iTel != null && iTel.getIccCardTypeGemini(slotId).equals("USIM")) {
						uri = mIccUsim1Uri;
					} else {
						uri = mIccUri1;
					}
				} else {
					if (iTel != null && iTel.getIccCardTypeGemini(slotId).equals("USIM")) {
						uri = mIccUsim2Uri;
					} else {
						uri = mIccUri2;
					}
				}
			} else {
            	if (iTel.getIccCardType().equals("USIM")) {
            		uri = mIccUsimUri;
            	} else {
            		uri = mIccUri;
            	}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return uri;
    }
    
    public static void setIntent (Intent intent) {
    	mIntent = intent;
    }
    
    public static Intent getIntent () {
    	return mIntent;
    }
    
    public static boolean pukRequest () {
        boolean simPUKReq = (TelephonyManager.SIM_STATE_PUK_REQUIRED 
                == TelephonyManager.getDefault().getSimState());    
        return simPUKReq;
    }
    
    public static boolean pukRequest (int simId) {
        boolean simPUKReq = (TelephonyManager.SIM_STATE_PUK_REQUIRED 
                == TelephonyManager.getDefault().getSimStateGemini(simId));
        return simPUKReq;
    }
    
    public static boolean pinRequest () {
        boolean simPINReq = (TelephonyManager.SIM_STATE_PIN_REQUIRED 
                == TelephonyManager.getDefault().getSimState());
        return simPINReq;           
    }
    
    public static boolean pinRequest (int simId) {
        boolean simPINReq = (TelephonyManager.SIM_STATE_PIN_REQUIRED 
                == TelephonyManager.getDefault().getSimStateGemini(simId));
        return simPINReq;
    }
    
    public static boolean simStateReady () {
        boolean simReady = (TelephonyManager.SIM_STATE_READY 
                == TelephonyManager.getDefault().getSimState());
        return simReady;            
    }
    
    public static boolean simStateReady (int simId) {
        boolean simReady = (TelephonyManager.SIM_STATE_READY 
                == TelephonyManager.getDefault().getSimStateGemini(simId));
        return simReady;
    }

    
    public static String deleteSimContact(long rawContactId, ContentResolver resolver) {   
    	String name = null;
    	String number = null;
		String phoneTypeSuffix = null;
		Log.i(TAG, "rawContactId is " + rawContactId);
		Cursor c = resolver.query(Data.CONTENT_URI,new String[] { Data.DATA1 },
				Data.RAW_CONTACT_ID + "=" + rawContactId + " AND "
						+ Data.MIMETYPE + "=?", new String[]{StructuredName.CONTENT_ITEM_TYPE}, null);
		Log.i(TAG,"After query name");
		try {
		if (null != c && c.moveToFirst()) {
			name = c.getString(0);
			}
		} finally {
			if (null != c)
				c.close();
		}
		Log.i(TAG,"After query name name is " + name);
		
		Cursor mc = resolver.query(Data.CONTENT_URI, new String[] { Data.DATA1, Data.DATA15 },
				Data.RAW_CONTACT_ID + "=" + rawContactId + " AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'"
				/*+ Data.IS_ADDITIONAL_NUMBER + "=0"*/, null, null);
		try {
		if (null != mc && mc.moveToFirst()) {
			number = mc.getString(0); // DATA1
			number = number.replaceAll("-", "");
			number = number.replaceAll(" ", "");
			phoneTypeSuffix = mc.getString(1); // DATA15
			}
		} finally {
			if (null != mc)
				mc.close();
		}
//		if (!TextUtils.isEmpty(phoneTypeSuffix)) {
//			name = name + "/" + phoneTypeSuffix;
//		}
		
		// mtk80909 modified for ALPS00023212
//		String[] phoneNumberAndTypeSuffix = getPhoneNumber(cursor);
//		String number = phoneNumberAndTypeSuffix[0];
		Log.i(TAG,"number is " + number);
//		String phoneTypeSuffix = phoneNumberAndTypeSuffix[1];
	    if (!TextUtils.isEmpty(phoneTypeSuffix)) {
		    if (TextUtils.isEmpty(name)) name = "/" + phoneTypeSuffix;
		    else name = name + "/" + phoneTypeSuffix;
		}

		String where;					
		if (TextUtils.isEmpty(name)) {
			Log.i(TAG,"name is empty");
			where = "number = '" + number + "'";
		} else if (TextUtils.isEmpty(number)) {
			where = "tag = '" + name + "'";
		} else {
			where = "tag = '" + name
			+ "' AND number = '" + number + "'";
		}
        return where;
    }
    
	public static void insertToDB(String name, String number, String email,
			String additionalNumber, ContentResolver resolver, long indicate,
			String simType) {
    	final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(RawContacts.CONTENT_URI);
    	ContentValues contactvalues = new ContentValues();
		contactvalues.put(RawContacts.INDICATE_PHONE_SIM, indicate);
		contactvalues.put(RawContacts.AGGREGATION_MODE,
				RawContacts.AGGREGATION_MODE_DISABLED);
		builder.withValues(contactvalues);

		operationList.add(builder.build());

		int phoneType = 7;
		String phoneTypeSuffix = "";
		// mtk80909 for ALPS00023212
		if (!TextUtils.isEmpty(name)) {
		    final NamePhoneTypePair namePhoneTypePair = new NamePhoneTypePair(name);
	    name = namePhoneTypePair.name;
		        phoneType = namePhoneTypePair.phoneType;
		        phoneTypeSuffix = namePhoneTypePair.phoneTypeSuffix;
		}


        
        //insert number
        if (!TextUtils.isEmpty(number)) {
        	number = PhoneNumberFormatUtilEx.formatNumber(number);
        	builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
    		builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
    		builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
    	    builder.withValue(Phone.NUMBER, number);
    		// mtk80909 for ALPS00023212
//    		builder.withValue(Phone.TYPE, phoneType);
    		builder.withValue(Data.DATA2, 2);
    		if (!TextUtils.isEmpty(phoneTypeSuffix)) {
    			builder.withValue(Data.DATA15, phoneTypeSuffix);
    		}
    		operationList.add(builder.build());
	        } 
		
	
        //insert name
        if (!TextUtils.isEmpty(name)) {
        	builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
    		builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
    		builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
    		builder.withValue(StructuredName.GIVEN_NAME, name);
    		operationList.add(builder.build());
        }
		

        //if USIM
		if (simType.equals("USIM")) {
			//insert email			
    	if (!TextUtils.isEmpty(email)) {       	
//            for (String emailAddress : emailAddressArray) {
    		    Log.i(TAG,"In actuallyImportOneSimContact email is " + email);
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Email.RAW_CONTACT_ID, 0);
                builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
                builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
                builder.withValue(Email.DATA, email);
                operationList.add(builder.build());
//            }
            }    	       	
        	if (!TextUtils.isEmpty(additionalNumber)) {
        	additionalNumber = PhoneNumberFormatUtilEx.formatNumber(additionalNumber);
            Log.i(TAG,"additionalNumber is " + additionalNumber);
        	builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        	builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
	        builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
//	        builder.withValue(Phone.TYPE, phoneType);	
	        builder.withValue(Data.DATA2, 7);	
	        builder.withValue(Phone.NUMBER, additionalNumber);
	        builder.withValue(Data.IS_ADDITIONAL_NUMBER, 1);
	        operationList.add(builder.build());
	        }       
		}		

		try {
			resolver.applyBatch(ContactsContract.AUTHORITY,
					operationList);// saved in database
		} catch (RemoteException e) {
			Log.e(TAG, String.format("%s: %s", e.toString(), e
					.getMessage()));
		} catch (OperationApplicationException e) {
			Log.e(TAG, String.format("%s: %s", e.toString(), e
					.getMessage()));
		}
    }
    
	// mtk80909 for ALPS00023212
    public static class NamePhoneTypePair {
        public String name;
        public int phoneType;
        public String phoneTypeSuffix;
        public NamePhoneTypePair(String nameWithPhoneType) {
            // Look for /W /H /M or /O at the end of the name signifying the type
            int nameLen = nameWithPhoneType.length();
            if (nameLen - 2 >= 0 && nameWithPhoneType.charAt(nameLen - 2) == '/') {
                char c = Character.toUpperCase(nameWithPhoneType.charAt(nameLen - 1));
                phoneTypeSuffix = String.valueOf(nameWithPhoneType.charAt(nameLen - 1));
                if (c == 'W') {
                    phoneType = Phone.TYPE_WORK;
                } else if (c == 'M' || c == 'O') {
                    phoneType = Phone.TYPE_MOBILE;
                } else if (c == 'H') {
                    phoneType = Phone.TYPE_HOME;
                } else {
                    phoneType = Phone.TYPE_OTHER;
                }
                name = nameWithPhoneType.substring(0, nameLen - 2);
            } else {
            	phoneTypeSuffix = "";
                phoneType = Phone.TYPE_OTHER;
                name = nameWithPhoneType;
            }
        }
    }
    
    
    public static void setAccount(ArrayList<Account> accounts) {
    	mAccounts = accounts;
    }
    
    public static ArrayList<Account> getAccount() {
    	return mAccounts;
    }
    
    public static void setSpinnerName(String accountName) {
    	mAccountName = accountName;
    }
    
    public static String getSpinnerName() {
    	return mAccountName;
    }
    
    public static void setState(EntitySet state) {
    	mState = state;
    }
    
    public static EntitySet getState() {
    	return mState;
    }
    
    public static void setEditMode(boolean isEditMode) {
    	mMode = isEditMode;
    }
    
    public static boolean getEditMode() {
    	return mMode;
    }
    
    public static void setValues(ContentValues values) {
    	mValues = values;
    }
    
    public static ContentValues getValues() {
    	return mValues;
    }
    
    
    public static long getRawContactId() {
    	return mRawContactId;
    }
    
    public static void setRawContactId(long rawContactId) {
    	mRawContactId = rawContactId;
    }
    
    public static long getContactId() {
    	return mContactId;
    }
    
    public static void setContactId(long ContactId) {
    	mContactId = ContactId;
    }
    
    	public static boolean mSavingGroup = false;
        public static HashMap<Long, ArrayData> mContactMap = new HashMap<Long, ArrayData>();
        public static class ArrayData {
        long mId = 0;
        CharSequence mTitle = "";
        long mPhotoId = 0;
        int mSimId = ContactsContract.RawContacts.INDICATE_PHONE;
        String mLookupKey = ""; 
        String mSortkey = "";
        int hasPhoneNumber = 0;
        int starState = 0;
        public ArrayData () {
        }
        
        public ArrayData (long id, long photoId, CharSequence title) {
            mId = id;
            mTitle = title;
            mPhotoId = photoId;
        }
        public ArrayData fromCursor(Cursor cursor ) {
            if (cursor == null) {
                return this;
            }
            mId = cursor.getLong(cursor.getColumnIndexOrThrow(Contacts._ID));
            mPhotoId = cursor.getLong(cursor.getColumnIndexOrThrow(Contacts.PHOTO_ID));
            mTitle = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME));
            mSimId = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.INDICATE_PHONE_SIM));
            mLookupKey = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.LOOKUP_KEY));
            mSortkey = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.SORT_KEY_PRIMARY));
            hasPhoneNumber = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.HAS_PHONE_NUMBER));
            starState = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.STARRED));
            return this;
        }
        
        @Override
        public String toString() {
            return String.format("id : %s photo_id : %s title : %s", mId, mPhotoId, mTitle);
        }
        
        
    }
    /**
     * Build the display title for the {@link Data#CONTENT_URI} entry in the
     * provided cursor, assuming the given mimeType.
     */
    public static final CharSequence getDisplayLabel(Context context,
            String mimeType, Cursor cursor) {
        // Try finding the type and label for this mimetype
        int colType;
        int colLabel;

        if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)
                || Constants.MIME_SMS_ADDRESS.equals(mimeType)) {
            // Reset to phone mimetype so we generate a label for SMS case
            mimeType = Phone.CONTENT_ITEM_TYPE;
            colType = cursor.getColumnIndex(Phone.TYPE);
            colLabel = cursor.getColumnIndex(Phone.LABEL);
        } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
            colType = cursor.getColumnIndex(Email.TYPE);
            colLabel = cursor.getColumnIndex(Email.LABEL);
        } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType)) {
            colType = cursor.getColumnIndex(StructuredPostal.TYPE);
            colLabel = cursor.getColumnIndex(StructuredPostal.LABEL);
        } else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
            colType = cursor.getColumnIndex(Organization.TYPE);
            colLabel = cursor.getColumnIndex(Organization.LABEL);
        } else {
            return null;
        }

        final int type = cursor.getInt(colType);
        final CharSequence label = cursor.getString(colLabel);

        return getDisplayLabel(context, mimeType, type, label);
    }

    public static final CharSequence getDisplayLabel(Context context, String mimetype, int type,
            CharSequence label) {
        CharSequence display = "";
        final int customType;
        final int defaultType;
        final int arrayResId;

        if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
            defaultType = Phone.TYPE_HOME;
            customType = Phone.TYPE_CUSTOM;
            arrayResId = com.android.internal.R.array.phoneTypes;
        } else if (Email.CONTENT_ITEM_TYPE.equals(mimetype)) {
            defaultType = Email.TYPE_HOME;
            customType = Email.TYPE_CUSTOM;
            arrayResId = com.android.internal.R.array.emailAddressTypes;
        } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimetype)) {
            defaultType = StructuredPostal.TYPE_HOME;
            customType = StructuredPostal.TYPE_CUSTOM;
            arrayResId = com.android.internal.R.array.postalAddressTypes;
        } else if (Organization.CONTENT_ITEM_TYPE.equals(mimetype)) {
            defaultType = Organization.TYPE_WORK;
            customType = Organization.TYPE_CUSTOM;
            arrayResId = com.android.internal.R.array.organizationTypes;
        } else {
            // Can't return display label for given mimetype.
            return display;
        }

        if (type != customType) {
            CharSequence[] labels = context.getResources().getTextArray(arrayResId);
            try {
                display = labels[type - 1];
            } catch (ArrayIndexOutOfBoundsException e) {
                display = labels[defaultType - 1];
            }
        } else {
            if (!TextUtils.isEmpty(label)) {
                display = label;
            }
        }
        return display;
    }

    /**
     * Opens an InputStream for the person's photo and returns the photo as a Bitmap.
     * If the person's photo isn't present returns null.
     *
     * @param aggCursor the Cursor pointing to the data record containing the photo.
     * @param bitmapColumnIndex the column index where the photo Uri is stored.
     * @param options the decoding options, can be set to null
     * @return the photo Bitmap
     */
    public static Bitmap loadContactPhoto(Cursor cursor, int bitmapColumnIndex,
            BitmapFactory.Options options) {
        if (cursor == null) {
            return null;
        }

        byte[] data = cursor.getBlob(bitmapColumnIndex);
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    /**
     * Loads a placeholder photo.
     *
     * @param placeholderImageResource the resource to use for the placeholder image
     * @param context the Context
     * @param options the decoding options, can be set to null
     * @return the placeholder Bitmap.
     */
    public static Bitmap loadPlaceholderPhoto(int placeholderImageResource, Context context,
            BitmapFactory.Options options) {
        if (placeholderImageResource == 0) {
            return null;
        }
        return BitmapFactory.decodeResource(context.getResources(),
                placeholderImageResource, options);
    }

    public static Bitmap loadContactPhoto(Context context, long photoId,
            BitmapFactory.Options options) {
        Cursor photoCursor = null;
        Bitmap photoBm = null;

        try {
            photoCursor = context.getContentResolver().query(
                    ContentUris.withAppendedId(Data.CONTENT_URI, photoId),
                    new String[] { Photo.PHOTO },
                    null, null, null);

            if (photoCursor.moveToFirst() && !photoCursor.isNull(0)) {
                byte[] photoData = photoCursor.getBlob(0);
                photoBm = BitmapFactory.decodeByteArray(photoData, 0,
                        photoData.length, options);
            }
        } catch(OutOfMemoryError e) {
           try {
        	BitmapFactory.Options optionsBackup = new BitmapFactory.Options();
        	optionsBackup.inSampleSize = 8;
			byte[] photoData = photoCursor.getBlob(0);
			photoBm = BitmapFactory.decodeByteArray(photoData, 0,
                    photoData.length, optionsBackup);
              } catch(OutOfMemoryError e1) {
              	BitmapFactory.Options optionsBackup = new BitmapFactory.Options();
            	optionsBackup.inSampleSize = 12;
    			byte[] photoData = photoCursor.getBlob(0);
    			photoBm = BitmapFactory.decodeByteArray(photoData, 0,
                        photoData.length, optionsBackup);
              }
        }
        finally {
            if (photoCursor != null) {
                photoCursor.close();
            }
        }

        return photoBm;
    }

    // TODO find a proper place for the canonical version of these
    public interface ProviderNames {
        String YAHOO = "Yahoo";
        String GTALK = "GTalk";
        String MSN = "MSN";
        String ICQ = "ICQ";
        String AIM = "AIM";
        String XMPP = "XMPP";
        String JABBER = "JABBER";
        String SKYPE = "SKYPE";
        String QQ = "QQ";
    }

    /**
     * This looks up the provider name defined in
     * ProviderNames from the predefined IM protocol id.
     * This is used for interacting with the IM application.
     *
     * @param protocol the protocol ID
     * @return the provider name the IM app uses for the given protocol, or null if no
     * provider is defined for the given protocol
     * @hide
     */
    public static String lookupProviderNameFromId(int protocol) {
        switch (protocol) {
            case Im.PROTOCOL_GOOGLE_TALK:
                return ProviderNames.GTALK;
            case Im.PROTOCOL_AIM:
                return ProviderNames.AIM;
            case Im.PROTOCOL_MSN:
                return ProviderNames.MSN;
            case Im.PROTOCOL_YAHOO:
                return ProviderNames.YAHOO;
            case Im.PROTOCOL_ICQ:
                return ProviderNames.ICQ;
            case Im.PROTOCOL_JABBER:
                return ProviderNames.JABBER;
            case Im.PROTOCOL_SKYPE:
                return ProviderNames.SKYPE;
            case Im.PROTOCOL_QQ:
                return ProviderNames.QQ;
        }
        return null;
    }

    /**
     * Build {@link Intent} to launch an action for the given {@link Im} or
     * {@link Email} row. Returns null when missing protocol or data.
     */
    public static Intent buildImIntent(ContentValues values) {
        final boolean isEmail = Email.CONTENT_ITEM_TYPE.equals(values.getAsString(Data.MIMETYPE));

        if (!isEmail && !isProtocolValid(values)) {
            return null;
        }

        final int protocol = isEmail ? Im.PROTOCOL_GOOGLE_TALK : values.getAsInteger(Im.PROTOCOL);

        String host = values.getAsString(Im.CUSTOM_PROTOCOL);
        String data = values.getAsString(isEmail ? Email.DATA : Im.DATA);
        if (protocol != Im.PROTOCOL_CUSTOM) {
            // Try bringing in a well-known host for specific protocols
            host = ContactsUtils.lookupProviderNameFromId(protocol);
        }

        if (!TextUtils.isEmpty(host) && !TextUtils.isEmpty(data)) {
            final String authority = host.toLowerCase();
            final Uri imUri = new Uri.Builder().scheme(Constants.SCHEME_IMTO).authority(
                    authority).appendPath(data).build();
            return new Intent(Intent.ACTION_SENDTO, imUri);
        } else {
            return null;
        }
    }

    private static boolean isProtocolValid(ContentValues values) {
        String protocolString = values.getAsString(Im.PROTOCOL);
        if (protocolString == null) {
            return false;
        }
        try {
            Integer.valueOf(protocolString);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static long queryForContactId(ContentResolver cr, long rawContactId) {
        Cursor contactIdCursor = null;
        long contactId = -1;
        try {
            contactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts.CONTACT_ID},
                    RawContacts._ID + "=" + rawContactId, null, null);
            if (contactIdCursor != null && contactIdCursor.moveToFirst()) {
                contactId = contactIdCursor.getLong(0);
            }
        } finally {
            if (contactIdCursor != null) {
                contactIdCursor.close();
            }
        }
        return contactId;
    }

    public static String querySuperPrimaryPhone(ContentResolver cr, long contactId) {
        Cursor c = null;
        String phone = null;
        try {
            Uri baseUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
            Uri dataUri = Uri.withAppendedPath(baseUri, Contacts.Data.CONTENT_DIRECTORY);

            c = cr.query(dataUri,
                    new String[] {Phone.NUMBER},
                    Data.MIMETYPE + "=" + Phone.MIMETYPE +
                        " AND " + Data.IS_SUPER_PRIMARY + "=1",
                    null, null);
            if (c != null && c.moveToFirst()) {
                // Just return the first one.
                phone = c.getString(0);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return phone;
    }

    public static long queryForRawContactId(ContentResolver cr, long contactId) {
        Cursor rawContactIdCursor = null;
        long rawContactId = -1;
        try {
            rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts._ID},
                    RawContacts.CONTACT_ID + "=" + contactId, null, null);
            if (rawContactIdCursor != null && rawContactIdCursor.moveToFirst()) {
                // Just return the first one.
                rawContactId = rawContactIdCursor.getLong(0);
            }
        } finally {
            if (rawContactIdCursor != null) {
                rawContactIdCursor.close();
            }
        }
        return rawContactId;
    }

    public static ArrayList<Long> queryForAllRawContactIds(ContentResolver cr, long contactId) {
        Cursor rawContactIdCursor = null;
        ArrayList<Long> rawContactIds = new ArrayList<Long>();
        try {
            rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts._ID},
                    RawContacts.CONTACT_ID + "=" + contactId, null, null);
            if (rawContactIdCursor != null) {
                while (rawContactIdCursor.moveToNext()) {
                    rawContactIds.add(rawContactIdCursor.getLong(0));
                }
            }
        } finally {
            if (rawContactIdCursor != null) {
                rawContactIdCursor.close();
            }
        }
        return rawContactIds;
    }


    /**
     * Utility for creating a standard tab indicator view.
     *
     * @param parent The parent ViewGroup to attach the new view to.
     * @param label The label to display in the tab indicator. If null, not label will be displayed.
     * @param icon The icon to display. If null, no icon will be displayed.
     * @return The tab indicator View.
     */
    public static View createTabIndicatorView(ViewGroup parent, CharSequence label, Drawable icon) {
        final LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View tabIndicator = inflater.inflate(R.layout.tab_indicator, parent, false);
        tabIndicator.getBackground().setDither(true);

        final TextView tv = (TextView) tabIndicator.findViewById(R.id.tab_title);
        tv.setText(label);

        final ImageView iconView = (ImageView) tabIndicator.findViewById(R.id.tab_icon);
        iconView.setImageDrawable(icon);

        return tabIndicator;
    }

    /**
     * Utility for creating a standard tab indicator view.
     *
     * @param parent The parent ViewGroup to attach the new view to.
     * @param source The {@link ContactsSource} to build the tab view from.
     * @return The tab indicator View.
     */
    public static View createTabIndicatorView(ViewGroup parent, ContactsSource source) {
        Drawable icon = null;
        if (source != null) {
            icon = source.getDisplayIcon(parent.getContext());
        }
        return createTabIndicatorView(parent, null, icon);
    }

    /**
     * Kick off an intent to initiate a call.
     *
     * @param phoneNumber must not be null.
     * @throws NullPointerException when the given argument is null.
     */
    public static void initiateCall(Context context, CharSequence phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                Uri.fromParts("tel", phoneNumber.toString(), null));
        context.startActivity(intent);
    }
    /**
     * Kick off an intent to initiate a call.
     */
    public static void initiateCall(Context context, CharSequence phoneNumber, int sim) {
        Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                Uri.fromParts("tel", phoneNumber.toString(), null));
        intent.putExtra(com.android.internal.telephony.Phone.GEMINI_SIM_ID_KEY, sim);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void enterDialer(Context context,final CharSequence phoneNumber) {
        Log.i(TAG, "enterDialer number " + phoneNumber);
        Uri phoneUri = Uri.fromParts("tel", phoneNumber.toString(), null);
        Intent EnterDialerIntent = new Intent(Intent.ACTION_DIAL, phoneUri);
        context.startActivity(EnterDialerIntent); 
    }

    public static void initiateCallWithSim(Context context, final CharSequence phoneNumber) {
    	initiateCallWithSim(context, phoneNumber, null);
    }
    /**
     * Kick off an intent to initiate a call && show the SIM selector dialog.
     */
    public static void initiateCallWithSim(Context context, final CharSequence phoneNumber, DialogInterface.OnDismissListener dismissListener) {
        Log.i(TAG, "initateCallWithSim number " + phoneNumber);
//        Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
//                Uri.fromParts("tel", phoneNumber.toString(), null));
//        context.startActivity(intent);
        final Context dialogContext = new ContextThemeWrapper(context, android.R.style.Theme_Light);
        final LayoutInflater dialogInflater = LayoutInflater.from(dialogContext);
        final Context ctx = context;
        final TelephonyManager tm = TelephonyManager.getDefault();

        class MyEntry {
        	public int simId;
        	public boolean enable;
        	public String number;
        	public MyEntry(int simId) {
        		this.simId = simId;
        	}
        	
        	public MyEntry(int simId, boolean enable, String number) {
        		this.simId = simId;
        		this.enable = enable;
        		this.number = number;
        	}
        }

        final ArrayAdapter<MyEntry> myAdapter = new ArrayAdapter<MyEntry>(ctx, android.R.layout.simple_list_item_2) {

            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = dialogInflater.inflate(android.R.layout.simple_list_item_2,
                            parent, false);
                }
                MyEntry entry = getItem(position);
                convertView.setEnabled(entry.enable);
                final TextView text1 = (TextView)convertView.findViewById(android.R.id.text1);
                final TextView text2 = (TextView)convertView.findViewById(android.R.id.text2);
                text1.setEnabled(entry.enable);
                text2.setEnabled(entry.enable);
                if (entry.simId == com.android.internal.telephony.Phone.GEMINI_SIM_1) {
                    text1.setText(ctx.getString(R.string.sim1));
                    text2.setText(entry.number);
                } else if (entry.simId == com.android.internal.telephony.Phone.GEMINI_SIM_2) {
                    text1.setText(ctx.getString(R.string.sim2));
                    text2.setText(entry.number);
                }
                return convertView;
            }
            
        };
        MyEntry entry1 = new MyEntry(com.android.internal.telephony.Phone.GEMINI_SIM_1);
        MyEntry entry2 = new MyEntry(com.android.internal.telephony.Phone.GEMINI_SIM_2);
        try {
            ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                    .getService(Context.TELEPHONY_SERVICE));
            // mtk80909 modify start 2010-10-12
            boolean sim1Idle = iTel.isIdleGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1);
            boolean sim2Idle = iTel.isIdleGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2);
            
            boolean sim1RadioOn = iTel
                    .isRadioOnGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1);
            boolean sim1Ready = (TelephonyManager.SIM_STATE_READY 
            		== TelephonyManager.getDefault()
            		.getSimStateGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1));
            boolean isEmergencyNumber = PhoneNumberUtils.isEmergencyNumber(phoneNumber.toString());
            entry1.enable = (isEmergencyNumber || (sim1RadioOn && sim1Ready)) && ((sim1Idle && sim2Idle) ? true : sim2Idle) ;
            boolean sim2RadioOn = iTel
                    .isRadioOnGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2);
            boolean sim2Ready = (TelephonyManager.SIM_STATE_READY 
            		== TelephonyManager.getDefault()
            		.getSimStateGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2));
            entry2.enable = (isEmergencyNumber || (sim2RadioOn && sim2Ready)) && ((sim1Idle && sim2Idle) ? true : sim1Idle);
            // mtk80909 modify end 2010-10-12
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        entry1.number = tm
                .getLine1NumberGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1);
        entry2.number = tm
                .getLine1NumberGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2);
        
        myAdapter.add(entry1);
        myAdapter.add(entry2);
        
        final DialogInterface.OnClickListener clickListener =
            new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	MyEntry entry = myAdapter.getItem(which);
            	Log.i(TAG, "Switch sim card " + entry.simId);
            	if (entry.enable) {
            		initiateCall(ctx, phoneNumber, entry.simId);
            		dialog.dismiss();  // added by mtk80909 for CR ALPS00124478
            	} else {
            		Log.w(TAG, "sim is radio off " + entry.simId);
            	}
            	// dialog.dismiss(); // commented by mtk80909 for CR ALPS00124478
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setSingleChoiceItems(myAdapter, 0, clickListener);
        if (phoneNumber.length()>TITLE_LENGTH){
        	String strTemp = phoneNumber.subSequence(0,TITLE_LENGTH).toString();
        	strTemp = strTemp + "...";
          builder.setTitle(ctx.getString(R.string.call_via, strTemp));        	
        }
        else{	
        builder.setTitle(ctx.getString(R.string.call_via, phoneNumber));
        }
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        if (dismissListener != null) {
        	dialog.setOnDismissListener(dismissListener);
        }
        // mtk80909 2010-9-16
        if (mCallAlertDialog == null || !mCallAlertDialog.isShowing())
        {
        	dialog.show();
        	mCallAlertDialog = dialog;   
        }

    }
    
    /**
     * Kick off an intent to initiate an Sms/Mms message.
     *
     * @param phoneNumber must not be null.
     * @throws NullPointerException when the given argument is null.
     */
    public static void initiateSms(Context context, CharSequence phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("sms", phoneNumber.toString(), null));
        context.startActivity(intent);
    }

    /**
     * Test if the given {@link CharSequence} contains any graphic characters,
     * first checking {@link TextUtils#isEmpty(CharSequence)} to handle null.
     */
    public static boolean isGraphic(CharSequence str) {
        return !TextUtils.isEmpty(str) && TextUtils.isGraphic(str);
    }

    /**
     * Returns true if two objects are considered equal.  Two null references are equal here.
     */
    public static boolean areObjectsEqual(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    /**
     * Returns true if two data with mimetypes which represent values in contact entries are
     * considered equal for collapsing in the GUI. For caller-id, use
     * {@link PhoneNumberUtils#compare(Context, String, String)} instead
     */
    public static final boolean shouldCollapse(Context context, CharSequence mimetype1,
            CharSequence data1, CharSequence mimetype2, CharSequence data2) {
        if (TextUtils.equals(Phone.CONTENT_ITEM_TYPE, mimetype1)
                && TextUtils.equals(Phone.CONTENT_ITEM_TYPE, mimetype2)) {
            if (data1 == data2) {
                return true;
            }
            if (data1 == null || data2 == null) {
                return false;
            }

            // If the number contains semicolons, PhoneNumberUtils.compare
            // only checks the substring before that (which is fine for caller-id usually)
            // but not for collapsing numbers. so we check each segment indidually to be more strict
            // TODO: This should be replaced once we have a more robust phonenumber-library
            String[] dataParts1 = data1.toString().split(WAIT_SYMBOL_AS_STRING);
            String[] dataParts2 = data2.toString().split(WAIT_SYMBOL_AS_STRING);
            if (dataParts1.length != dataParts2.length) {
                return false;
            }
            for (int i = 0; i < dataParts1.length; i++) {
                if (!PhoneNumberUtils.compare(context, dataParts1[i], dataParts2[i])) {
                    return false;
                }
            }

            return true;
        } else {
            if (mimetype1 == mimetype2 && data1 == data2) {
                return true;
            }
            return TextUtils.equals(mimetype1, mimetype2) && TextUtils.equals(data1, data2);
        }
    }

    /**
     * Returns true if two {@link Intent}s are both null, or have the same action.
     */
    public static final boolean areIntentActionEqual(Intent a, Intent b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return TextUtils.equals(a.getAction(), b.getAction());
    }
    
    /**
     * Returns mCallAlertDialog for external dimissing operation
     * mtk80909 2010-9-16
     */
    public static AlertDialog getCallDialog() {
    	return mCallAlertDialog;
    }
//MTK
    public static String getGroupsName(Context context, String name) {
        if (name == null) {
            return null;
        }
        Resources res = context.getResources();
        if ("Co-worker".equals(name)) {
            return res.getString(R.string.groups_coworker);
        }

        if ("Family".equals(name)) {
            return res.getString(R.string.groups_family);
        }
        if ("Friends".equals(name)) {
            return res.getString(R.string.groups_friends);
        }
        if ("Schoolmate".equals(name)) {
            return res.getString(R.string.groups_schoolmate);
        }
        if ("VIP".equals(name)) {
            return res.getString(R.string.groups_vip);
        }
        if ("All Contacts".equals(name)) {
            return res.getString(R.string.showAllGroups);
        }
        if ("SIM".equals(name)) {
            return res.getString(R.string.sim);
        }
        return name;
    }
    
    //MTK
    public static int getGroupsIcon(Context context, String name) {
        if (name == null) {
            return R.drawable.contact_group_custom;
        }
        if ("Co-worker".equals(name)) {
            return R.drawable.contact_group_coworker;
        }
        
        if ("Family".equals(name)) {
            return R.drawable.contact_group_family;
        }
        if ("Friends".equals(name)) {
            return R.drawable.contact_group_friends;
        }
        if ("Schoolmate".equals(name)) {
            return R.drawable.contact_group_classmates;
        }
        if ("VIP".equals(name)) {
            return R.drawable.contact_group_vip;
        }

        return R.drawable.contact_group_custom;
    }
    
    public static String getSmsAddressFromGroup(Context context, String groupTitle) {
        StringBuilder builder = new StringBuilder();
        ContentResolver resolver = context.getContentResolver();
        Cursor contactCursor = resolver.query(Uri.withAppendedPath(Contacts.CONTENT_GROUP_URI, groupTitle),
                new String[]{Contacts._ID}, null, null, null);
        StringBuilder ids = new StringBuilder();
        HashMap<Long,Long> allContacts = new HashMap<Long,Long>();
        if (contactCursor != null) {
            while (contactCursor.moveToNext()) {
                if (ids.length() > 0) {
                    ids.append(",");
                }
                Long contactId = contactCursor.getLong(0);
                ids.append(contactId);
                allContacts.put(contactId, contactId);
            }
            contactCursor.close();
        }
        StringBuilder where = new StringBuilder();
        if (ids.length() > 0) {
            where.append(Data.CONTACT_ID + " IN(");
            where.append(ids.toString());
            where.append(")");
        } else {
            return "";
        }
        where.append(" AND ");
        where.append(Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'");
        Log.i(TAG, "getSmsAddressFromGroup where " + where);
        Cursor cursor = resolver.query(Data.CONTENT_URI, new String[]{Data.DATA1,Phone.TYPE, Data.CONTACT_ID}, where.toString(), null, Data.CONTACT_ID + " ASC ");
        if (cursor != null) {
            long candidateContactId = -1;
            String candidateAddress = "";
            int candidateType = -1;
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(2);
                if(allContacts.containsKey(id))allContacts.remove(id);
                int type = cursor.getInt(1);
                String number = cursor.getString(0);
                if (candidateContactId == -1) {
                    candidateContactId = id;
                    candidateType = type;
                    candidateAddress = number;
                } else {
                    if (candidateContactId != id) {
                        if (candidateAddress != null && candidateAddress.length() > 0) {
                            if (builder.length() > 0) {
                                builder.append(",");
                            }
                            builder.append(candidateAddress);
                        }
                        candidateContactId = id;
                        candidateType = type;
                        candidateAddress = number;
                    } else {
                        if (candidateType != Phone.TYPE_MOBILE && type == Phone.TYPE_MOBILE) {
                            candidateContactId = id;
                            candidateType = type;
                            candidateAddress = number;
                        }
                    }
                }
                if (cursor.isLast()) {
                    if (candidateAddress != null && candidateAddress.length() > 0) {
                        if (builder.length() > 0) {
                            builder.append(",");
                        }
                        builder.append(candidateAddress);
                    }
                }
                
            }
            cursor.close();
        }
        Log.i(TAG, "getSmsAddressFromGroup address " + builder);
        
        ////////////////////
        ids = new StringBuilder();
        where = new StringBuilder();
        List<String> noNumberContactList = new ArrayList<String>();
        for(Long id:allContacts.keySet()){
        	if (ids.length() > 0) {
                ids.append(",");
            }
            ids.append(id.toString());
        }
        if (ids.length() > 0) {
            where.append(Data.CONTACT_ID + " IN(");
            where.append(ids.toString());
            where.append(")");
        } else {
            return builder.toString();
        }
        where.append(" AND ");
        where.append(Data.MIMETYPE + "='" + StructuredName.CONTENT_ITEM_TYPE + "'");
        Cursor cursor2 = resolver.query(Data.CONTENT_URI, new String[]{Data.DATA1}, where.toString(), null, Data.CONTACT_ID + " ASC ");
        if(cursor2 != null){
        	while(cursor2.moveToNext()){
        		noNumberContactList.add(cursor2.getString(0));
        	}
        	cursor2.close();
        }
        String str = "";
        if(noNumberContactList.size() == 1){
        	str = context.getString(R.string.send_groupsms_no_number_1, noNumberContactList.get(0));
        }else if(noNumberContactList.size() == 2){
        	str = context.getString(R.string.send_groupsms_no_number_2, noNumberContactList.get(0),noNumberContactList.get(1));
        }else if(noNumberContactList.size() > 2){
        	str = context.getString(R.string.send_groupsms_no_number_more, noNumberContactList.get(0),String.valueOf(noNumberContactList.size() - 1));
        }
        //////////////////////
        String result = builder.toString();
        if(str != null && str.length()>0){
        	return result + ";" + str;
        }else 
        	return result;
    }
    
    public static String getEmailAddressFromGroup(Context context, String groupTitle) {
        StringBuilder builder = new StringBuilder();
        ContentResolver resolver = context.getContentResolver();
        Cursor contactCursor = resolver.query(Uri.withAppendedPath(Contacts.CONTENT_GROUP_URI, groupTitle),
                new String[]{Contacts._ID}, null, null, null);
        StringBuilder ids = new StringBuilder();
        if (contactCursor != null) {
            while (contactCursor.moveToNext()) {
                if (ids.length() > 0) {
                    ids.append(",");
                }
                ids.append(contactCursor.getLong(0));
            }
            contactCursor.close();
        }
        StringBuilder where = new StringBuilder();
        if (ids.length() > 0) {
            where.append(Data.CONTACT_ID + " IN(");
            where.append(ids.toString());
            where.append(")");
        } else {
            return "";
        }
        where.append(" AND ");
        where.append(Data.MIMETYPE + "='" + Email.CONTENT_ITEM_TYPE + "'");
        Log.i(TAG, "getSmsAddressFromGroup where " + where);
        Cursor cursor = resolver.query(Data.CONTENT_URI, new String[]{Data.DATA1,Phone.TYPE, Data.CONTACT_ID}, where.toString(), null, Data.CONTACT_ID + " ASC ");
        if (cursor != null) {
            long candidateContactId = -1;
            String candidateAddress = "";
            while (cursor.moveToNext()) {
                long id = cursor.getLong(2);
                int type = cursor.getInt(1);
                String email = cursor.getString(0);
                if (candidateContactId == -1) {
                    candidateContactId = id;
                    candidateAddress = email;
                } else {
                    if (candidateContactId != id) {
                        if (candidateAddress != null && candidateAddress.length() > 0) {
                            if (builder.length() > 0) {
                                builder.append(",");
                            }
                            builder.append(candidateAddress);
                        }
                        candidateContactId = id;
                        candidateAddress = email;
                    }
                }
                if (cursor.isLast()) {
                    if (candidateAddress != null && candidateAddress.length() > 0) {
                        if (builder.length() > 0) {
                            builder.append(",");
                        }
                        builder.append(candidateAddress);
                    }
                }
            }
            cursor.close();
        }
        
        return builder.toString();
    }
    //add by qilong.sun

	public static List<SimInfo> mSimList;  
    public static List<SimInfo> mSelectedSimList = new ArrayList<SimInfo>();

    public static AlertDialog buildSimListDialog(Context context, final long suggestedSimId, final boolean hasInternetOption, DialogInterface.OnClickListener clickListener) {
        Log.i(TAG, "buildSimList " );
        final Context dialogContext = new ContextThemeWrapper(context, android.R.style.Theme_Light);
        final LayoutInflater dialogInflater = LayoutInflater.from(dialogContext);
        final Context ctx = context;
        mSimList = Constants.getInsertedSimList(context);

        class SimHolder {
            public TextView title;
            public TextView info;
            public TextView suggested;
            public SimIconView simIcon;
            public CheckBox mCheckBox;
        }
        
        final BaseAdapter mAdapter = new  BaseAdapter() {
            
            public int getCount() {
                // TODO Auto-generated method stub
                int count = mSimList.size();
                
                if(hasInternetOption) {
                    //
                }
                
                return count;
            }

            public Object getItem(int position) {
                // TODO Auto-generated method stub
                if(position < mSimList.size())
                    return Integer.valueOf(mSimList.get(position).slot);
                else
                    return Integer.valueOf((int)Settings.System.VOICE_CALL_SIM_SETTING_INTERNET);
            }

            public long getItemId(int position) {
                // TODO Auto-generated method stub
                return 0;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                SimHolder holder = null;
                if (convertView == null) {
                    holder = new SimHolder();
                    convertView = dialogInflater.inflate(R.layout.sim_list_item,
                            parent, false);
                    holder.title = (TextView) convertView.findViewById(R.id.text1);
                    holder.info = (TextView) convertView.findViewById(R.id.text2);
                    holder.simIcon = (SimIconView) convertView.findViewById(R.id.sim_icon);
                    holder.mCheckBox = (CheckBox) convertView.findViewById(R.id.checked);
                    holder.suggested = (TextView) convertView.findViewById(R.id.suggested);
                    convertView.setTag(holder);
                } else {
                    holder = (SimHolder) convertView.getTag();
                }
                holder.simIcon.updateSimIcon(mSimList.get(position));
                holder.mCheckBox.setVisibility(View.GONE);
                holder.title.setText((String) mSimList.get(position).label);
                holder.info.setText((String) mSimList.get(position).number);
                
                if(mSimList.get(position).simId == suggestedSimId)
                    holder.suggested.setVisibility(View.VISIBLE);
                else
                    holder.suggested.setVisibility(View.INVISIBLE);
                
                return convertView;
            }
        };
        
//        String titleStr = "Associate SIM";
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setSingleChoiceItems(mAdapter, -1, clickListener)
            .setTitle(R.string.associate_SIM)
            .setIcon(android.R.drawable.ic_menu_more);
        AlertDialog mDetailListDialog = builder.create();
//        mDetailListDialog.show();
        return mDetailListDialog;
    }

    public static AlertDialog buildSimListDialog(Context context, DialogInterface.OnClickListener clickListener) {
        Log.i(TAG, "buildSimList " );
        final Context dialogContext = new ContextThemeWrapper(context, android.R.style.Theme_Light);
        final LayoutInflater dialogInflater = LayoutInflater.from(dialogContext);
        final Context ctx = context;
        mSimList = Constants.getInsertedSimList(context);

        class SimHolder {
    		public TextView title;
    		public TextView info;
    		public SimIconView simIcon;
    		public CheckBox mCheckBox;
    	}
        
        final BaseAdapter mAdapter = new  BaseAdapter() {
        	
			public int getCount() {
				// TODO Auto-generated method stub
				return mSimList.size();
			}

			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return mSimList.get(position);
			}

			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return 0;
			}

			public View getView(int position, View convertView, ViewGroup parent) {
				SimHolder holder = null;
    			if (convertView == null) {
    				holder = new SimHolder();
    				convertView = dialogInflater.inflate(R.layout.sim_list_item,
                            parent, false);
    				holder.title = (TextView) convertView.findViewById(R.id.text1);
    				holder.info = (TextView) convertView.findViewById(R.id.text2);
    				holder.simIcon = (SimIconView) convertView.findViewById(R.id.sim_icon);
    				holder.mCheckBox = (CheckBox) convertView.findViewById(R.id.checked);
    				convertView.setTag(holder);
    			} else {
    				holder = (SimHolder) convertView.getTag();
    			}
    			holder.simIcon.updateSimIcon(mSimList.get(position));
    			holder.mCheckBox.setVisibility(View.GONE);
    			holder.title.setText((String) mSimList.get(position).label);
    			String number = mSimList.get(position).number;
    			if(number!=null && number.length() > 0)holder.info.setText(number);
    			else holder.info.setVisibility(View.GONE);
    			return convertView;
			}
        };
        
//        String titleStr = "Associate SIM";
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setSingleChoiceItems(mAdapter, -1, clickListener)
        	.setTitle(R.string.associate_SIM)
			.setIcon(android.R.drawable.ic_menu_more);
        AlertDialog mDetailListDialog = builder.create();
//        mDetailListDialog.show();
        return mDetailListDialog;
        
    }
    public static AlertDialog.Builder buildSimListDialogMultChoice(Context context, DialogInterface.OnClickListener clickListener) {
        Log.i(TAG, "buildSimListDialogMultChoice " );
        final Context dialogContext = new ContextThemeWrapper(context, android.R.style.Theme_Light);
        final LayoutInflater dialogInflater = LayoutInflater.from(dialogContext);
        final Context ctx = context;
        List<Integer> simList = new ArrayList<Integer>();
        mSimList = Constants.getInsertedSimList(context);
        mSelectedSimList = new ArrayList<SimInfo>();
        
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String simIds = pref.getString("simIds", "");
        String[] simIdList = null;
        if(simIds != null){
        	simIdList = simIds.split(",");
        }
        if(simIdList != null){
        	for(String s:simIdList){
        		if(s.length()>0)simList.add(Integer.parseInt(s));
        	}
        }
        for(SimInfo info:mSimList){
        	if(simList.contains(info.simId) && !mSelectedSimList.contains(info))
        		mSelectedSimList.add(info);
        }
        
        class SimHolder {
    		public TextView title;
    		public TextView info;
    		public SimIconView simIcon;
    		public CheckBox mCheckBox;
    	}
        
        final BaseAdapter mAdapter = new  BaseAdapter() {
        	
			public int getCount() {
				return mSimList.size();
			}
			public Object getItem(int position) {
				return null;
			}
			public long getItemId(int position) {
				return 0;
			}
			public View getView(int position, View convertView, ViewGroup parent) {
				SimHolder holder = null;
    			if (convertView == null) {
    				holder = new SimHolder();
    				convertView = dialogInflater.inflate(R.layout.sim_list_item,
                            parent, false);
    				holder.title = (TextView) convertView.findViewById(R.id.text1);
    				holder.info = (TextView) convertView.findViewById(R.id.text2);
    				holder.simIcon = (SimIconView) convertView.findViewById(R.id.sim_icon);
    				holder.mCheckBox = (CheckBox) convertView.findViewById(R.id.checked);
    				convertView.setTag(holder);
    			} else {
    				holder = (SimHolder) convertView.getTag();
    			}
//    			int simId = mSimList.get(position).simId;
    			holder.simIcon.updateSimIcon(mSimList.get(position));
    			holder.mCheckBox.setVisibility(View.VISIBLE);
    			if(mSelectedSimList.contains(mSimList.get(position)))
    					holder.mCheckBox.setChecked(true);
    			else holder.mCheckBox.setChecked(false);
    			holder.title.setText((String) mSimList.get(position).label);
    			holder.info.setText((String) mSimList.get(position).number);
    			return convertView;
			}
        };
        
//        String titleStr = "Select SIM card";        
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setSingleChoiceItems(mAdapter, -1, clickListener)
        	.setTitle(R.string.pin_dialog_title)
			.setIcon(android.R.drawable.ic_menu_more);
        return builder;
        
    }

    public static void dispatchActivityOnPause() {
        if(mCallSelectionDialog != null && mCallSelectionDialog.isShowing()) {
            mCallSelectionDialog.dismiss();
            mCallSelectionDialog = null;
        }
        
        if(mCallSelectionDialogOther != null && mCallSelectionDialogOther.isShowing()) {
            mCallSelectionDialogOther.dismiss();
            mCallSelectionDialogOther = null;
        }
        
        if(mSimSelectionDialog != null && mSimSelectionDialog.isShowing()) {
            mSimSelectionDialog.dismiss();
            mSimSelectionDialog = null;
        }

        if(mTurnOnSipDialog != null && mTurnOnSipDialog.isShowing()) {
            mTurnOnSipDialog.dismiss();
            mTurnOnSipDialog = null;
        }

        if(mTurnOn3GServiceDialog != null && mTurnOn3GServiceDialog.isShowing()) {
            mTurnOn3GServiceDialog.dismiss();
            mTurnOnSipDialog = null;
        }
    }

    static int get3GCapabilitySIM() {
        int retval = 0;
        if(FeatureOption.MTK_GEMINI_3G_SWITCH) {
            ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
            try {
                retval = ((telephony == null) ? -1 : telephony.get3GCapabilitySIM());
            } catch(RemoteException e) {
                retval = 0;
            }
        }
        return retval;
    }

    protected static void dialSingle(final Context context, final String number, final int type, long originalSim, final OnDialCompleteListener listener) {
        Intent intent = null;
        switch(type) {
            case DIAL_TYPE_SIP:
                intent = ContactsUtils.generateDialIntent(true, 0, number);
                context.startActivity(intent);
                if(listener != null)
                    listener.onDialComplete(true);
                break;
            case DIAL_TYPE_VIDEO: {
                    if(context instanceof CellConnMgrClient) {
                        final CellConnMgr cellConnMgr = ((CellConnMgrClient)((Object)context)).getCellConnMgr();
                        int result = cellConnMgr.handleCellConn(0, CellConnMgr.REQUEST_TYPE_SIMLOCK, new Runnable() {
                            public void run() {
                                int result = cellConnMgr.getResult();
                                boolean dialed = false;
                                log("ContactsUtis.dialSingle, mServiceComplete result = "+result);
                                if(result == com.mediatek.CellConnService.CellConnMgr.RESULT_STATE_NORMAL) {
                                    Intent intent = ContactsUtils.generateDialIntent(false, com.android.internal.telephony.Phone.GEMINI_SIM_1, number);
                                    intent.putExtra("is_vt_call", true);
                                    context.startActivity(intent);
                                    dialed = true;
                                }
                                if(listener != null)
                                    listener.onDialComplete(dialed);
                            }
                        });
                    } else {
                        intent = ContactsUtils.generateDialIntent(false, 0, number);
                        if(type == DIAL_TYPE_VIDEO)
                            intent.putExtra("is_vt_call", true);
                        context.startActivity(intent);
                        if(listener != null)
                            listener.onDialComplete(true);
                    }
                }
                break;
            case DIAL_TYPE_AUTO:
            case DIAL_TYPE_VOICE:
                intent = ContactsUtils.generateDialIntent(false, 0, number);
                context.startActivity(intent);
                if(listener != null)
                    listener.onDialComplete(true);
                break;
        }
    }

    protected static void dialMultiple(final Context context, final String number, int type, long originalSim, final OnDialCompleteListener listener) {
        Intent intent = null;
        switch(type) {
            case DIAL_TYPE_SIP:
                int enabled = Settings.System.getInt(context.getContentResolver(), Settings.System.ENABLE_INTERNET_CALL, 0);
                if(enabled == 1) {
                    intent = ContactsUtils.generateDialIntent(true, 0, number);
                    context.sendBroadcast(intent);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.reminder)
                           .setMessage(R.string.enable_sip_dialog_message)
                           .setNegativeButton(R.string.dial_reminder_dialog_no, (DialogInterface.OnClickListener)null)
                           .setPositiveButton(R.string.dial_reminder_dialog_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                Intent intent = new Intent();
                                intent.setClassName("com.android.phone", "com.android.phone.SipCallSetting");
                                context.startActivity(intent);
                            }
                        });
                    if(mTurnOnSipDialog != null && mTurnOnSipDialog.isShowing()) {
                        mTurnOnSipDialog.dismiss();
                        mTurnOnSipDialog = null;
                    }
                    if(mSimSelectionDialog != null && mSimSelectionDialog.isShowing()) {
                        mSimSelectionDialog.dismiss();
                        mSimSelectionDialog = null;
                    }
                    mTurnOnSipDialog = builder.create();
                    mTurnOnSipDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            // TODO Auto-generated method stub
                            if(listener != null)
                                listener.onDialComplete(false);
                        }
                    });
                   if (mTurnOnSipDialog != null) mTurnOnSipDialog.show();
                }
                break;
            case DIAL_TYPE_VIDEO:{
                    // dial emergency number directly
                    if(PhoneNumberUtils.isEmergencyNumber(number)) {
                        intent = ContactsUtils.generateDialIntent(false, com.android.internal.telephony.Phone.GEMINI_SIM_1, number);
                        context.sendBroadcast(intent);
                        return;
                    }
                    final int slot = ContactsUtils.get3GCapabilitySIM();
                    if (slot == -1) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.reminder)
                               .setMessage(R.string.turn_on_3g_service_message)
                               .setNegativeButton(android.R.string.no,(DialogInterface.OnClickListener) null)
                               .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub
                                        Intent intent = new Intent();
                                        intent.setClassName("com.android.phone", "com.android.phone.Modem3GCapabilitySwitch");
                                        context.startActivity(intent);
                                    }
                                });
                        mTurnOn3GServiceDialog = builder.create();
                        mTurnOn3GServiceDialog.show();
                        if (listener != null)
                            listener.onDialComplete(false);
                        return;
                    }
                    if(context instanceof CellConnMgrClient) {
                        final CellConnMgr cellConnMgr = ((CellConnMgrClient)((Object)context)).getCellConnMgr();
                        int result = cellConnMgr.handleCellConn(slot, CellConnMgr.REQUEST_TYPE_ROAMING | CellConnMgr.FLAG_REQUEST_NOPREFER, new Runnable() {
                            public void run() {
                                int result = cellConnMgr.getResult();
                                boolean dialed = false;
                                if(result == com.mediatek.CellConnService.CellConnMgr.RESULT_STATE_NORMAL) {
                                    Intent intent = ContactsUtils.generateDialIntent(false, slot, number);
                                    intent.putExtra("is_vt_call", true);
                                    context.sendBroadcast(intent);
                                    dialed = true;
                                }
                                if(listener != null)
                                    listener.onDialComplete(dialed);
                            }
                        });
                    } else {
                        intent = ContactsUtils.generateDialIntent(false, slot, number);
                        if(type == DIAL_TYPE_VIDEO)
                            intent.putExtra("is_vt_call", true);
                        context.sendBroadcast(intent);
                        if(listener != null)
                            listener.onDialComplete(true);
                    }
                }
                break;
            case DIAL_TYPE_VOICE:
            case DIAL_TYPE_AUTO: {
                    Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, 
                                                                       new String[] {Data.SIM_ID}, 
                                                                       Data.MIMETYPE + "='" + CommonDataKinds.Phone.CONTENT_ITEM_TYPE 
                                                                       + "' AND (" + Data.DATA1 + "='" + number + "') AND (" + Data.SIM_ID + ">0)", 
                                                                       null, 
                                                                       null);
                    ArrayList associateSims = new ArrayList();
                    try {
                        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                        if(cursor != null && cursor.moveToFirst()) {
                            do {
                                if(cursor.getInt(0) > 0) {
                                    associateSims.add(Integer.valueOf(cursor.getInt(0)));
                                }
                            } while (cursor.moveToNext());
                        }
                    } catch(Exception e) {
                        log("dialMultiple, cursor exception");
                    } finally {
                        if(cursor != null)
                            cursor.close();
                    }
                    
                    log("dialMultiple, associateSims = "+associateSims);

                    ContactsUtils.dial(context, 
                                       number, 
                                       originalSim,
                                       associateSims, 
                                       listener);
                }
                break;
        }
    }

    /* do not check sim status */
    public static void dial(Context context, String number, int type, int slot, OnDialCompleteListener listener) {
        Intent intent = ContactsUtils.generateDialIntent(false, slot, number);
        if(type == DIAL_TYPE_VIDEO)
            intent.putExtra("is_vt_call", true);
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            context.sendBroadcast(intent);
        } else {
            context.startActivity(intent);
        }
        if(listener != null)
            listener.onDialComplete(true);
    }

    public static void dial(Context context, String number, int type, OnDialCompleteListener listener) {
        ContactsUtils.dial(context, number, type, Settings.System.DEFAULT_SIM_NOT_SET, listener);
    }

    public static void dial(Context context, String number, int type, long originalSim, OnDialCompleteListener listener) {
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            dialMultiple(context, number, type, originalSim, listener);
        } else {
            dialSingle(context, number, type, originalSim, listener);
        }
    }

    public static void dial(final Context context, final String number, ArrayList associateSims, final OnDialCompleteListener listener) {
        dial(context, number, Settings.System.DEFAULT_SIM_NOT_SET, associateSims, listener);
    }

    public static void dial(final Context context, final String number, final long originalSim, ArrayList associateSims, final OnDialCompleteListener listener) {
        log("ContactsUtils.dial");
        OnDialListener sDefaultDialListener = new OnDialListener() {
            public boolean onDial(Context context, int slot, String number) {
                log("sDefaultDialListener onDial");
                boolean retval = true;
                final Context _context = context;
                if(slot == (int)Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
                    int enabled = Settings.System.getInt(context.getContentResolver(), Settings.System.ENABLE_INTERNET_CALL, 0);
                    log("enabled = "+enabled);
                    if(enabled == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.reminder)
                               .setMessage(R.string.enable_sip_dialog_message)
                               .setNegativeButton(android.R.string.no, (DialogInterface.OnClickListener)null)
                               .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                   
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Auto-generated method stub
                                    Intent intent = new Intent();
                                    intent.setClassName("com.android.phone", "com.android.phone.SipCallSetting");
                                    _context.startActivity(intent);
                                    //if(listener != null)
                                    //    listener.onDialComplete();
                                }
                            });
                        mTurnOnSipDialog = builder.create();
                        mTurnOnSipDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            public void onDismiss(DialogInterface dialog) {
                                // TODO Auto-generated method stub
                                if(listener != null)
                                    listener.onDialComplete(false);
                            }
                        });
                        if(mTurnOnSipDialog != null && mTurnOnSipDialog.isShowing()) {
                            mTurnOnSipDialog.dismiss();
                            mTurnOnSipDialog = null;
                        }
                        if(mSimSelectionDialog != null && mSimSelectionDialog.isShowing()) {
                            mSimSelectionDialog.dismiss();
                            mSimSelectionDialog = null;
                        }
                        mTurnOnSipDialog.show();
                        retval = false;
                    } else {
                        log("dial sip call, number = "+number);
                        Intent intent = ContactsUtils.generateDialIntent(true, slot, number);
                        //context.startActivity(intent);
                        context.sendBroadcast(intent);
                        if(listener != null)
                            listener.onDialComplete(true);
                        log("dial sip call, sendOrderedBroadcast");
                    }
                } else {
                    log("dial general call, number = "+number);
                    Intent intent = ContactsUtils.generateDialIntent(false, slot, number);
                    //context.startActivity(intent);
                    context.sendBroadcast(intent);
                    if(listener != null)
                        listener.onDialComplete(true);
                    log("dial general call, sendOrderedBroadcast");
                    log("sDefaultDialListener onDial");
                }
                return retval;
            }
        };
        OnDialListener sDefaultDialListenerCheck = new OnDialListener() {
            public boolean onDial(Context context, int slot, String number) {
                log("ContactsUtils.dial, onDial");
                boolean retval = true;
                final Context _context = context;
                final int _slot = slot;
                final String _number = number;
                final CellConnMgr cellConnMgr = ((CellConnMgrClient)((Object)_context)).getCellConnMgr();
                if(slot == (int)Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
                    int enabled = Settings.System.getInt(context.getContentResolver(), Settings.System.ENABLE_INTERNET_CALL, 0);
                    log("ContactsUtils.dial, enabled = "+enabled);
                    if(enabled == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.reminder)
                               .setMessage(R.string.enable_sip_dialog_message)
                               .setNegativeButton(android.R.string.no, (DialogInterface.OnClickListener)null)
                               .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                   
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Auto-generated method stub
                                    Intent intent = new Intent();
                                    intent.setClassName("com.android.phone", "com.android.phone.SipCallSetting");
                                    _context.startActivity(intent);
                                }
                            });
                        mTurnOnSipDialog = builder.create();
                        mTurnOnSipDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            
                            public void onDismiss(DialogInterface dialog) {
                                // TODO Auto-generated method stub
                                if(listener != null)
                                    listener.onDialComplete(false);
                            }
                        });
                        if(mTurnOnSipDialog != null && mTurnOnSipDialog.isShowing()) {
                            mTurnOnSipDialog.dismiss();
                            mTurnOnSipDialog = null;
                        }
                        if(mSimSelectionDialog != null && mSimSelectionDialog.isShowing()) {
                            mSimSelectionDialog.dismiss();
                            mSimSelectionDialog = null;
                        }
                        mTurnOnSipDialog.show();
                        retval = false;
                    } else {
                        log("dial sip call, number = "+number);
                        Intent intent = ContactsUtils.generateDialIntent(true, slot, number);
                        //_context.startActivity(intent);
                        context.sendBroadcast(intent);
                        if(listener != null)
                            listener.onDialComplete(true);
                    }
                } else {
                    if(cellConnMgr != null) {
                        int result = cellConnMgr.handleCellConn(slot, CellConnMgr.REQUEST_TYPE_ROAMING, new Runnable() {
                            public void run() {
                                int result = cellConnMgr.getResult();
                                boolean dialed = false;
                                int slot = cellConnMgr.getPreferSlot();
                                log("ContactsUtis.mServiceComplete, result = "+result+" slot = "+slot);
                                if(result == com.mediatek.CellConnService.CellConnMgr.RESULT_STATE_NORMAL) {
                                    Intent intent = ContactsUtils.generateDialIntent(false, slot, _number);
                                    //_context.startActivity(intent);
                                    _context.sendBroadcast(intent);
                                    dialed = true;
                                }
                                if(listener != null)
                                    listener.onDialComplete(dialed);
                            }
                        });
                        log("result = "+result);
                    }
                    retval = false;
                }
                return retval;
            }
        };
        if(context instanceof CellConnMgrClient)
            dial(context, number, originalSim, associateSims, listener, sDefaultDialListenerCheck);
        else
            dial(context, number, originalSim, associateSims, listener, sDefaultDialListener);
    }
    
    public static void dial(final Context context, final String number, final long originalSim, ArrayList associateSims, final OnDialCompleteListener completeListener,
                            final OnDialListener dialListener) {
        
        DialogInterface.OnKeyListener sOnKeyListener = new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    if(completeListener != null) 
                        completeListener.onDialComplete(false);
                }
                return false;
            }
        };
        
        int associateSlot = -1;
        int associateSim = (int)Settings.System.DEFAULT_SIM_NOT_SET;
        
        boolean hasAssociateSims = associateSims.size() > 0;
        boolean hasMoreAssociateSims = associateSims.size() > 1;
        boolean noSims;
        boolean internetCallOn;
        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        if (telephony == null) {
        	Log.i(TAG,"telephony == null");
        	return;
        }
        log("hasAssociateSims = "+hasAssociateSims+" hasMoreAssociateSims = "+hasMoreAssociateSims);
        /*
        if(hasAssociateSims) {
            associateSim = ((Integer)associateSims.get(0)).intValue();
            if(associateSim >= 0) {
                associateSlot = mSlotsMap[associateSim];
                if(associateSlot == 0) {
                    associateSlot = (int)SIMInfo.getSlotById(context, associateSim);
                    mSlotsMap[associateSim] = associateSlot;
                }
            }
            log("associateSim = "+associateSim+" associateSlot = "+associateSlot);
        }
        */
        final long defaultSim = Settings.System.getLong(context.getContentResolver(), 
                                                        Settings.System.VOICE_CALL_SIM_SETTING,
                                                        Settings.System.DEFAULT_SIM_NOT_SET);
        final int defaultSlot = SIMInfo.getSlotById(context, defaultSim);
        
        noSims = SIMInfo.getInsertedSIMList(context).size() == 0;
        internetCallOn = Settings.System.getInt(context.getContentResolver(), Settings.System.ENABLE_INTERNET_CALL, 0) == 1;
        log("defaultSim = "+defaultSim+" defaultSlot = "+defaultSlot+" noSims = "+noSims+" internetCallOn = "+internetCallOn);
        
        // emergency call will not go through internet
        if(PhoneNumberUtils.isEmergencyNumber(number)) {
            log("number is emergency");
            Intent intent = ContactsUtils.generateDialIntent(false, defaultSlot, number);
            //context.startActivity(intent);
            context.sendBroadcast(intent);
            if(completeListener != null)
                completeListener.onDialComplete(true);
            return;
        }

        // for cta case
        if(noSims && defaultSim != Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
            // using sim 0
            if(!internetCallOn) {
                log("cta case match, dial directly...");
                Intent intent = ContactsUtils.generateDialIntent(false, 0, number);
                context.sendBroadcast(intent);
                //context.startActivity(intent);
                if(completeListener != null)
                    completeListener.onDialComplete(true);
            } else {
                // show sim select dialog
                if (!(mSimSelectionDialog != null && mSimSelectionDialog.isShowing())) {
                    mSimSelectionDialog = ContactsUtils.createSimSelectionDialog(context, context
                            .getResources().getString(R.string.title_call_via),
                            Settings.System.DEFAULT_SIM_NOT_SET, ContactsUtils.createItemHolder(
                                    context, true), new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    AlertDialog alertDialog = (AlertDialog) dialog;
                                    final int slot = ((Integer) alertDialog.getListView()
                                            .getAdapter().getItem(which)).intValue();
                                    boolean dialed = dialListener.onDial(context, slot, number);
                                    log("call onDialComplete slot = " + slot + " dialed = "
                                            + dialed);
                                }

                            });
                    mSimSelectionDialog.setOnKeyListener(sOnKeyListener);
                    mSimSelectionDialog.show();
                }
            }
            return;
        }

        if(defaultSim == Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK){
            log("DEFAULT_SIM_SETTING_ALWAYS_ASK");
            long suggestedSim = Settings.System.DEFAULT_SIM_NOT_SET;
            //if(hasAssociateSims && !hasMoreAssociateSims)
            //    suggestedSim = associateSim;
            int inserted = 0;
            SIMInfo simInfo;
            // buggy
            for(Object item : associateSims) {
                int temp = ((Integer)item).intValue();
                int slot = SIMInfo.getSlotById(context, temp);
                try {
                    if(slot >= 0 && telephony.isSimInsert(slot)) {
                        log("associate sim id : "+temp);
                        inserted++;
                        associateSim = temp;
                    }
                } catch(Exception e) {
                        //
                }
            }
            if(inserted > 1) {
                suggestedSim = Settings.System.DEFAULT_SIM_NOT_SET;
            } else if(inserted == 1) {
                suggestedSim = associateSim;
            } else if(originalSim != Settings.System.DEFAULT_SIM_NOT_SET)
                suggestedSim = originalSim;
            log("inserted = "+inserted+" suggestedSim = "+suggestedSim);
            // show sim select dialog
            if (!(mSimSelectionDialog != null && mSimSelectionDialog.isShowing())) {
                mSimSelectionDialog = ContactsUtils.createSimSelectionDialog(context, context
                        .getResources().getString(R.string.title_call_via), suggestedSim,
                        ContactsUtils.createItemHolder(context, true),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog alertDialog = (AlertDialog) dialog;
                                final int slot = ((Integer) alertDialog.getListView().getAdapter()
                                        .getItem(which)).intValue();
                                boolean dialed = dialListener.onDial(context, slot, number);
                                log("call onDialComplete slot = " + slot + " dialed = " + dialed);
                            }

                        });
                mSimSelectionDialog.setOnKeyListener(sOnKeyListener);
                mSimSelectionDialog.show();
            }

        } else if(defaultSim != Settings.System.DEFAULT_SIM_NOT_SET) {
            log("NOT DEFAULT_SIM_NOT_SET");
            if(hasAssociateSims) {
                log("hasAssociateSims");
                int inserted = 0;
                SIMInfo simInfo;
                // buggy
                for(Object item : associateSims) {
                    int temp = ((Integer)item).intValue();
                    int slot = SIMInfo.getSlotById(context, temp);
                    try {
                        if(slot >= 0 && telephony.isSimInsert(slot)) {
                            log("associate sim id : "+temp);
                            inserted++;
                            associateSim = temp;
                        }
                    } catch(Exception e) {
                        //
                    }
                }
                if(inserted == 0)
                    associateSim = ((Integer)associateSims.get(0)).intValue();
                log("inserted = "+inserted+"associateSim = "+associateSim);
                if(hasMoreAssociateSims) {
                    log("hasMoreAssociateSims");
                    if(inserted >= 2) {
                        if (!(mSimSelectionDialog != null && mSimSelectionDialog.isShowing())) {
                            mSimSelectionDialog = ContactsUtils.createSimSelectionDialog(context,
                                    context.getResources().getString(R.string.title_call_via),
                                    Settings.System.DEFAULT_SIM_NOT_SET, ContactsUtils
                                            .createItemHolder(context, true),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            AlertDialog alertDialog = (AlertDialog) dialog;
                                            final int slot = ((Integer) alertDialog.getListView()
                                                    .getAdapter().getItem(which)).intValue();
                                            boolean dialed = dialListener.onDial(context, slot,
                                                    number);
                                            log("call onDialComplete with slot = " + slot);
                                        }
                                    });
                            mSimSelectionDialog.setOnKeyListener(sOnKeyListener);
                            mSimSelectionDialog.show();
                        }
                        return;
                    }
                }
                // dial directly
                if(defaultSim == associateSim) {
                    log("defaultSim == associateSim");
                    if(originalSim == Settings.System.DEFAULT_SIM_NOT_SET || originalSim == defaultSim) {
                        log("originalSim == defaultSim");
                        boolean dialed = dialListener.onDial(context, defaultSlot, number);
                    } else {
                        if (!(mSimSelectionDialog != null && mSimSelectionDialog.isShowing())) {
                            mSimSelectionDialog = ContactsUtils.createSimSelectionDialog(context,
                                    context.getResources().getString(R.string.title_call_via),
                                    associateSim, ContactsUtils.createItemHolder(context, true),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            AlertDialog alertDialog = (AlertDialog) dialog;
                                            final int slot = ((Integer) alertDialog.getListView()
                                                    .getAdapter().getItem(which)).intValue();
                                            boolean dialed = dialListener.onDial(context, slot,
                                                    number);
                                            log("call onDialComplete with slot = " + slot);
                                        }
                                    });
                            mSimSelectionDialog.setOnKeyListener(sOnKeyListener);
                            mSimSelectionDialog.show();
                        }
                    }
                } else {
                    log("defaultSim != associateSim");
                    if(inserted == 1) {
                        log("inserted == 1");
                        // associate sim is inserted
                        // show sim select dialog, mark associate sim
                        // `suggested`
                        if (!(mSimSelectionDialog != null && mSimSelectionDialog.isShowing())) {
                            mSimSelectionDialog = ContactsUtils.createSimSelectionDialog(context,
                                    context.getResources().getString(R.string.title_call_via),
                                    associateSim, ContactsUtils.createItemHolder(context, true),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            AlertDialog alertDialog = (AlertDialog) dialog;
                                            final int slot = ((Integer) alertDialog.getListView()
                                                    .getAdapter().getItem(which)).intValue();
                                            boolean dialed = dialListener.onDial(context, slot,
                                                    number);
                                            log("call onDialComplete with slot = " + slot);
                                        }
                                    });
                            mSimSelectionDialog.setOnKeyListener(sOnKeyListener);
                            mSimSelectionDialog.show();
                        }
                    } else {
                        // associate sim is not inserted
                        SIMInfo viaSimInfo = null;
                        if(originalSim != Settings.System.DEFAULT_SIM_NOT_SET)
                            viaSimInfo = SIMInfo.getSIMInfoById(context, originalSim);
                        else
                            viaSimInfo = SIMInfo.getSIMInfoById(context, defaultSim);
                        log("inserted == 0");
                        boolean originalSimInserted = false;
                        int slot = SIMInfo.getSlotById(context, originalSim);
                        if(slot >= 0) {
                            try {
                                originalSimInserted = telephony.isSimInsert(slot);
                            } catch(Exception e) {
                                originalSimInserted = false;
                            }
                        }
                        if(originalSim == Settings.System.DEFAULT_SIM_NOT_SET || !originalSimInserted) {
                            log("originalSim is not set");
                            String viaSimString = null;
                            Resources resources = context.getResources();
                            SIMInfo associateSimInfo = SIMInfo.getSIMInfoById(context, associateSim);
                            if (defaultSim == Settings.System.VOICE_CALL_SIM_SETTING_INTERNET)
                                viaSimString = resources.getString(R.string.internet);
                            else if(viaSimInfo != null)
                                viaSimString = viaSimInfo.mDisplayName;

                            String message = "";
                            if (associateSimInfo != null) 
                                message = resources.getString(R.string.dial_reminder_dialog_message, associateSimInfo.mDisplayName, viaSimString);
                            AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(number).setMessage(message);
                            AlertDialog dialog = builder.create();
                            dialog.setButton(AlertDialog.BUTTON_POSITIVE, resources.getString(R.string.dial_reminder_dialog_yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    boolean dialed;
                                    int slot = defaultSlot;
                                    if(defaultSim == Settings.System.VOICE_CALL_SIM_SETTING_INTERNET)
                                        slot = (int)Settings.System.VOICE_CALL_SIM_SETTING_INTERNET;
                                    dialed = dialListener.onDial(context, slot, number);
                                    log("dial complete, slot = " + defaultSlot);
                                }
                            });

                            if (SIMInfo.getInsertedSIMList(context).size() <= 1) {
                                mCallSelectionDialog = dialog;
                                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, resources.getString(R.string.dial_reminder_dialog_no), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub
                                        if(completeListener != null)
                                            completeListener.onDialComplete(false);
                                    }
                                });
                            } else {
                                mCallSelectionDialogOther = dialog;
                                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, resources.getString(R.string.dial_reminder_dialog_other), new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        mSimSelectionDialog = ContactsUtils.createSimSelectionDialog(context, context.getResources().getString(R.string.title_call_via),
                                                defaultSim, ContactsUtils.createItemHolder(context, true), new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        AlertDialog alertDialog = (AlertDialog) dialog;
                                                        int slot = ((Integer) alertDialog.getListView().getAdapter().getItem(which)).intValue();
                                                        boolean dialed = dialListener.onDial(context, slot, number);
                                                        log("dial complete, slot = " + slot);
                                                    }
                                        });
                                        mSimSelectionDialog.show();
                                    }
                                });
                            }
                            dialog.setOnKeyListener(sOnKeyListener);
                            dialog.show();
                        } else {
                            log("originalSim is set");
                            if (!(mSimSelectionDialog != null && mSimSelectionDialog.isShowing())) {
                                mSimSelectionDialog = ContactsUtils.createSimSelectionDialog(context, context.getResources().getString(R.string.title_call_via),
                                        originalSim, ContactsUtils.createItemHolder(context, true), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                AlertDialog alertDialog = (AlertDialog) dialog;
                                                int slot = ((Integer) alertDialog.getListView().getAdapter().getItem(which)).intValue();
                                                boolean dialed = dialListener.onDial(context, slot, number);
                                                log("dial complete, slot = " + slot);
                                            }
                                });
                                mSimSelectionDialog.setOnKeyListener(sOnKeyListener);
                                mSimSelectionDialog.show();
                            }
                        }
                    }
                }
            } else {
                log("hasAssociateSim false");
                if(originalSim != Settings.System.DEFAULT_SIM_NOT_SET && originalSim != defaultSim) {
                    log("originalSim is set");
                    long suggestedSim = defaultSim;
                    int slot = SIMInfo.getSlotById(context, originalSim);
                    try {
                        if(slot >= 0 && telephony.isSimInsert(slot))
                            suggestedSim = originalSim;
                    } catch(Exception e) {}
                    if (!(mSimSelectionDialog != null && mSimSelectionDialog.isShowing())) {
                        mSimSelectionDialog = ContactsUtils.createSimSelectionDialog(context, context.getResources().getString(R.string.title_call_via),
                                suggestedSim, ContactsUtils.createItemHolder(context, true), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        AlertDialog alertDialog = (AlertDialog) dialog;
                                        int slot = ((Integer) alertDialog.getListView().getAdapter().getItem(which)).intValue();
                                        boolean dialed = dialListener.onDial(context, slot, number);
                                        log("dial complete, slot = " + slot);
                                    }
                        });
                        mSimSelectionDialog.setOnKeyListener(sOnKeyListener);
                        mSimSelectionDialog.show();
                    }
                } else {
                    int slot = defaultSlot;
                    
                    if(defaultSim == Settings.System.VOICE_CALL_SIM_SETTING_INTERNET)
                        slot = (int)Settings.System.VOICE_CALL_SIM_SETTING_INTERNET;
                    
                    boolean dialed = dialListener.onDial(context, slot, number);
                }
            }
        } else {
            // Settings.System.DEFAULT_SIM_NOT_SET
            log("bial out...");
        }
    }
    
    public static Intent generateDialIntent(boolean sip, int slot, String number) {
        Intent intent = new Intent();
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            intent.setAction("out_going_call_to_phone_app");
            if(sip) {
                intent.putExtra("number", number);
                intent.putExtra("launch_from_dialer", true);
                intent.putExtra("is_sip_call", true);
            } else {
                intent.putExtra("number", number);
                intent.putExtra(com.android.internal.telephony.Phone.GEMINI_SIM_ID_KEY, slot);
                intent.putExtra("launch_from_dialer", true);
                intent.putExtra("is_sip_call", false);
            }
            log("generateDialIntent, intent = "+intent);
        } else {
            intent.setAction(Intent.ACTION_CALL_PRIVILEGED);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String scheme;
            if(sip)
                scheme = "sip";
            else
                scheme = "tel";
            Uri uri = Uri.fromParts(scheme, number, null);
            intent.setData(uri);
            log("generateDialIntent, intent = "+intent);
        }
        log("generateDialIntent, intent = "+intent);
        return intent;
    }
    
    public static void log(String msg) {
        Log.d(TAG, msg);
    }
    
    public interface OnDialListener {
        public boolean onDial(Context context, int slot, String number);
    }
    
    public interface OnDialCompleteListener {
        public void onDialComplete(boolean dialed);
    }
    
    public interface CellConnMgrClient {
        public CellConnMgr getCellConnMgr();
    }


    //add by MTK80908 begin
	/**
	 * SimInfo wrapper for framework SimInfo API. Use in contacts package
	 */
	public static class SIMInfoWrapper {
		private final static boolean DBG = true;
		private Context mContext;
		private List<SIMInfo> mAllSimInfoList = null;
		private List<SIMInfo> mInsertedSimInfoList = null;
		private HashMap<Integer,SIMInfo> mAllSimInfoMap = null;
		private HashMap<Integer,SIMInfo> mInsertedSimInfoMap = null;
		private boolean mInsertSim = false;
		private int mAllSimCount = 0;
		private int mInsertedSimCount = 0;
		private CursorAdapter mAdapter = null; 
		/**
		 * Receiver used to update cached simInfo if changes.  
		 */
		
		private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
	        public void onReceive(Context context, Intent intent) {
		    	   String action = intent.getAction();
		           if(action.equals(Intent.SIM_SETTINGS_INFO_CHANGED)){
		        	   updateSimInfo(mContext);
		           }else if (action.equals(TelephonyIntents.ACTION_SIM_INFO_UPDATE)){//Deal with Modem Reset
		        	   log("[updateSimInfo][ACTION_RADIO_OFF]");
		        	   updateSimInfo(mContext);
		        	   if (mAdapter != null) {
		        		   if (mAdapter != null && mAdapter instanceof CursorAdapter) {
		        			   mAdapter.notifyDataSetChanged();
		        		   }
		        		   log("[]adapter" + mAdapter);
		        	   }
		           }
		    	}	    	
	    };
	        
		private void updateSimInfo(Context context){
			
			if (mAllSimInfoList != null) {
				mAllSimInfoList = SIMInfo.getAllSIMList(context);
				if (mAllSimInfoList !=null) {
					mAllSimCount = mAllSimInfoList.size();
					mAllSimInfoMap = new HashMap<Integer,SIMInfo>();
					for(SIMInfo item: mAllSimInfoList){
						int mSimId = getCheckedSimId(item);
						if( mSimId != -1)
							mAllSimInfoMap.put(mSimId, item);
						
					}
					log("[updateSimInfo] update mAllSimInfoList");
				} else {
					log("[updateSimInfo] updated mAllSimInfoList is null");
					return;
				}
			}
			
			if (mInsertedSimInfoList != null) {
				
				mInsertedSimInfoList = SIMInfo.getInsertedSIMList(context);
				if (mInsertedSimInfoList !=null) {
					mInsertedSimCount = mInsertedSimInfoList.size();
					mInsertedSimInfoMap = new HashMap<Integer,SIMInfo>();
					for(SIMInfo item: mInsertedSimInfoList){
						int simId = getCheckedSimId(item);
						if( simId != -1)
							mInsertedSimInfoMap.put(simId, item);
					}
					log("[updateSimInfo] update mInsertedSimInfoList");					
				} else {
					log("[updateSimInfo] updated mInsertedSimInfoList is null");
					return;
				}
			}
		}
		
		/**
		 * SIMInfo wrapper constructor. Build simInfo according to input type
		 * @param context
		 * @param isInsertSimOrAll
		 */
		private SIMInfoWrapper(Context context){
			mContext = context;
			mAllSimInfoList = SIMInfo.getAllSIMList(context);
			mInsertedSimInfoList = SIMInfo.getInsertedSIMList(context);
			
			if (mAllSimInfoList == null || mInsertedSimInfoList == null) {
				log("[SIMInfoWrapper] mSimInfoList OR mInsertedSimInfoList is nulll");
				return;
			}
			
			mAllSimCount = mAllSimInfoList.size();
			mInsertedSimCount = mInsertedSimInfoList.size();
			
			mAllSimInfoMap = new HashMap<Integer,SIMInfo>();
			mInsertedSimInfoMap = new HashMap<Integer,SIMInfo>();
			
			for(SIMInfo item: mAllSimInfoList){
				int simId  = getCheckedSimId(item);
				if (simId != -1)
					mAllSimInfoMap.put(simId, item);
			}
			
			for(SIMInfo item: mInsertedSimInfoList){
				int simId  = getCheckedSimId(item);
				if (simId != -1)
					mInsertedSimInfoMap.put(simId, item);
			}
			
	        IntentFilter iFilter=new IntentFilter();
	        iFilter.addAction(Intent.SIM_SETTINGS_INFO_CHANGED);
	        iFilter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
			mContext.registerReceiver(mReceiver, iFilter);
		}
		
		private int getCheckedSimId(SIMInfo item) {
			if (item != null && item.mSimId > 0) {
				return (int)item.mSimId;
			} else {
				log("[getCheckedSimId]Wrong simId is " + (item == null?-1:item.mSimId));
				return -1;
			}
		}
		/**
		 * SIMInfo wrapper constructor. Build simInfo according to input type
		 * @param context
		 * @param isInsertSimOrAll
		 */
		private SIMInfoWrapper(Context context,boolean isInsertSim){
			mContext = context;
			mInsertSim = isInsertSim;
			if(isInsertSim) {
				mInsertedSimInfoList = SIMInfo.getInsertedSIMList(context);
				if (mInsertedSimInfoList != null) {
					mInsertedSimCount = mInsertedSimInfoList.size();
					mInsertedSimInfoMap = new HashMap<Integer,SIMInfo>();
					for(SIMInfo item: mInsertedSimInfoList){
						int simId = getCheckedSimId(item);
						if( simId != -1)
							mInsertedSimInfoMap.put(simId, item);
					}
				} else {
					log("[SIMInfoWrapper] mInsertedSimInfoList is null");
					return;
				}
			} else {
				mAllSimInfoList = SIMInfo.getAllSIMList(context);
				if (mAllSimInfoList !=null) {
					mAllSimCount = mAllSimInfoList.size();
					mAllSimInfoMap = new HashMap<Integer,SIMInfo>();
					for(SIMInfo item: mAllSimInfoList){
						int simId = getCheckedSimId(item);
						if( simId != -1)
							mAllSimInfoMap.put(simId, item);
					}
				} else {
					log("[SIMInfoWrapper] mAllSimInfoList is null");
					return;
				}
			}
			
	        IntentFilter iFilter=new IntentFilter();
	        iFilter.addAction(Intent.SIM_SETTINGS_INFO_CHANGED);
	        iFilter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
			mContext.registerReceiver(mReceiver, iFilter);
		}
		
		/**
		 * Public API to get SIMInfoWrapper instance
		 * @param context
		 * @param isInsertSim
		 * @return SIMInfoWrapper instance
		 */
	    static SIMInfoWrapper getDefault(Context context) {
	        return new SIMInfoWrapper(context);
	    }
		
		/**
		 * Public API to get SIMInfoWrapper instance
		 * @param context
		 * @param isInsertSimOrAll. True if to get InsertedSimList
		 * @return SIMInfoWrapper instance
		 */
	    static SIMInfoWrapper getDefault(Context context,boolean isInsertSimOrAll) {
	        return new SIMInfoWrapper(context, isInsertSimOrAll);
	    }
	    
	    /**
	     * Unregister context receiver
	     * Should called when the context is end of life.
	     */
		public void release(){
			if(mContext != null)
				mContext.unregisterReceiver(mReceiver);
		}
		
		
		public void setListNotifyDataChanged(CursorAdapter adapter) {
			if (adapter != null)
			    mAdapter = adapter;
		}
		/**
		 * get cached SIM info list
		 * @return 
		 */
	    public List<SIMInfo> getSimInfoList(){
	    	if (mInsertSim) {
	    		return mInsertedSimInfoList;
	    	} else {
	    		return mAllSimInfoList;
	    	}
		}
	    
		/**
		 * get cached SIM info list
		 * @return 
		 */
	    public List<SIMInfo> getAllSimInfoList(){
	    	return mAllSimInfoList;
		}
	    /**
		 * get cached SIM info list
		 * @return 
		 */
	    public List<SIMInfo> getInsertedSimInfoList(){
			return mInsertedSimInfoList;
		}
		
	    /**
	     * get SimInfo cached HashMap
	     * @return 
	     */
	    public HashMap<Integer,SIMInfo> getSimInfoMap(){
			return mAllSimInfoMap;
		}
		
	    
	    /**
	     * get SimInfo cached HashMap
	     * @return 
	     */
	    public HashMap<Integer,SIMInfo> getInsertedSimInfoMap(){
			return mInsertedSimInfoMap;
		}
	    
	    /**
	     * get cached SimInfo from HashMap 
	     * @param id
	     * @return
	     */
		public SIMInfo getSimInfoById(int id){
			return mAllSimInfoMap.get(id);
		}
		
		/**
		 * get SIM color according to input id
		 * @param id
		 * @return
		 */
		public int getSimColorById(int id){
			SIMInfo simInfo = mAllSimInfoMap.get(id);
			return (simInfo == null)?-1:simInfo.mColor;
		}
		
		/**
		 * get SIM display name according to input id 
		 * @param id
		 * @return
		 */
		public String getSimDisplayNameById(int id){
			SIMInfo simInfo = mAllSimInfoMap.get(id);
			return (simInfo == null)?null:simInfo.mDisplayName;
		}
		
		/**
		 * get SIM slot according to input id
		 * @param id
		 * @return 
		 */
		public int getSimSlotById(int id){
			SIMInfo simInfo = mAllSimInfoMap.get(id);
			return (simInfo == null)?-1:simInfo.mSlot;
		}
		
	    /**
	     * get cached SimInfo from HashMap 
	     * @param id
	     * @return
	     */
		public SIMInfo getInsertedSimInfoById(int id){
			return mInsertedSimInfoMap.get(id);
		}
		
		/**
		 * get SIM color according to input id
		 * @param id
		 * @return
		 */
		public int getInsertedSimColorById(int id){
			SIMInfo simInfo = mInsertedSimInfoMap.get(id);
			return (simInfo == null)?-1:simInfo.mColor;
		}
		
		/**
		 * get SIM display name according to input id 
		 * @param id
		 * @return
		 */
		public String getInsertedSimDisplayNameById(int id){
			SIMInfo simInfo = mInsertedSimInfoMap.get(id);
			return (simInfo == null)?null:simInfo.mDisplayName;
		}
		
		/**
		 * get SIM slot according to input id
		 * @param id
		 * @return 
		 */
		public int getInsertedSimSlotById(int id){
			SIMInfo simInfo = mInsertedSimInfoMap.get(id);
			return (simInfo == null)?-1:simInfo.mSlot;
		}
		
		/**
		 * get all SIM count according to Input
		 * @return 
		 */
		public int getAllSimCount(){
			return mAllSimCount;
		}
		
		/**
		 * get inserted SIM count according to Input
		 * @return 
		 */
		public int getInsertedSimCount(){
			return mInsertedSimCount;
		}
		
		
		private void notifyDataChange() {
			
		}
		
		private void log (String msg) {
			if(DBG)
				Log.i(TAG,msg);
		}
	 
	}
    //add by MTK80908 end
    
    /* added by xingping.zheng start */
    public static String getOptrProperties() {
        if (null == mOptr) {
            mOptr = SystemProperties.get("ro.operator.optr");
            if (null == mOptr) {
                mOptr = "";
            }
        }
        return mOptr;   
    }

    public static String getSpecProperties() {
        if (null == mSpec) {
            mSpec = SystemProperties.get("ro.operator.spec");
            if (null == mSpec) {
                mSpec = "";
            }
        }
        return mSpec;
    }
    public static String getSegProperties() {
        if (null == mSeg) {
            mSeg = SystemProperties.get("ro.operator.seg");
            if (null == mSeg) {
                mSeg = "";
            }
        }
        return mSeg;
    }

    public static AlertDialog createSimSelectionDialog(Context context, String title, long suggestedSimId, List<ItemHolder> items, boolean selected, long selectedId, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        SimAdapter simAdapter = new SimAdapter(context, items, suggestedSimId, selected, selectedId);
        builder.setSingleChoiceItems(simAdapter, -1, listener)
               .setTitle(title);
        return builder.create();
    }

    public static AlertDialog createSimSelectionDialog(Context context, String title, long suggestedSimId, List<ItemHolder> items, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        SimAdapter simAdapter = new SimAdapter(context, items, suggestedSimId);
        builder.setSingleChoiceItems(simAdapter, -1, listener)
               .setTitle(title);
//               .setNegativeButton(R.string.imexport_bridge_dialog_cancel, null);
        return builder.create();
    }

    public static List<ItemHolder> createItemHolder(Context context, boolean internet) {
        return createItemHolder(context, null, internet, null);
    }
    
    public static List<ItemHolder> createItemHolder(Context context, String phone, boolean internet, ArrayList<Account> accounts) {
        List<SIMInfo> simInfos = SIMInfo.getInsertedSIMList(context);
        ArrayList<ItemHolder> itemHolders = new ArrayList<ItemHolder>();
        ItemHolder temp = null;
        if(!TextUtils.isEmpty(phone)) {
            temp = new ItemHolder(phone, SimAdapter.ITEM_TYPE_TEXT);
            itemHolders.add(temp);
        }
        
        for(SIMInfo simInfo : simInfos) {
            temp = new ItemHolder(simInfo, SimAdapter.ITEM_TYPE_SIM);
            itemHolders.add(temp);
        }
        
        if(internet && SipManager.isVoipSupported(context)) {
            temp = new ItemHolder(context.getResources().getText(R.string.internet), SimAdapter.ITEM_TYPE_INTERNET);
            itemHolders.add(temp);
        }
        
        if(accounts != null) {
            for(Account account : accounts) {
                temp = new ItemHolder(account, SimAdapter.ITEM_TYPE_ACCOUNT);
                itemHolders.add(temp);
            }
        }
        
        return itemHolders;
    }

    private static class SimAdapter extends BaseAdapter {

        public static final int ITEM_TYPE_UNKNOWN  = -1;
        public static final int ITEM_TYPE_SIM      =  0;
        public static final int ITEM_TYPE_INTERNET =  1;
        public static final int ITEM_TYPE_TEXT     =  2;
        public static final int ITEM_TYPE_ACCOUNT  =  3;
        
        Context mContext;
        long mSuggestedSimId;
        long mSelectedId;
        boolean mSelected;
        List<ItemHolder> mItems;
        
        public SimAdapter(Context context, List<ItemHolder> items, long suggestedSimId) {
            mContext = context;
            mSuggestedSimId = suggestedSimId;
            mItems = items;
            mSelected = false;
        }
        
        public SimAdapter(Context context, List<ItemHolder> items, long suggestedSimId, boolean selected, long selectedId) {
            mContext = context;
            mSuggestedSimId = suggestedSimId;
            mItems = items;
            mSelected = selected;
            mSelectedId = selectedId;
        }
        
        public int getCount() {
            // TODO Auto-generated method stub
            return mItems.size();
        }

        @Override
        public int getViewTypeCount() {
            return 4;
        }
        
        @Override
        public int getItemViewType(int position) {
            // TODO Auto-generated method stub
            ItemHolder itemHolder = mItems.get(position);
            return itemHolder.type;
        }

        public Object getItem(int position) {
            // TODO Auto-generated method stub
            ItemHolder itemHolder = mItems.get(position);
            if(itemHolder.type == ITEM_TYPE_SIM) {
                return Integer.valueOf(((SIMInfo)itemHolder.data).mSlot);
            } else if(itemHolder.type == ITEM_TYPE_INTERNET) {
                return Integer.valueOf((int)Settings.System.VOICE_CALL_SIM_SETTING_INTERNET);
            } else if(itemHolder.type == ITEM_TYPE_TEXT || itemHolder.type == ITEM_TYPE_ACCOUNT) {
                return itemHolder.data;
            } else {
                return null;
            }
        }

        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View view = convertView;
            ViewHolder holder = null;
            int viewType = getItemViewType(position);
            if(view == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                holder = new ViewHolder();
                
                if(viewType == ITEM_TYPE_SIM) {
                    view = inflater.inflate(R.layout.sim_selection_item, null);
                    holder.mSimSignal = (TextView)view.findViewById(R.id.simSignal);
                    holder.mSimStatus = (ImageView)view.findViewById(R.id.simStatus);
                    holder.mShortPhoneNumber = (TextView)view.findViewById(R.id.shortPhoneNumber);
                    holder.mDisplayName = (TextView)view.findViewById(R.id.displayName);
                    holder.mPhoneNumber = (TextView)view.findViewById(R.id.phoneNumber);
                    holder.mSimIcon = view.findViewById(R.id.simIcon);
                    holder.mSuggested = (TextView)view.findViewById(R.id.suggested);
                } else if(viewType == ITEM_TYPE_INTERNET) {
                    view = inflater.inflate(R.layout.sim_selection_item_internet, null);
                    holder.mInternetIcon = (ImageView)view.findViewById(R.id.internetIcon);
                } else if(viewType == ITEM_TYPE_TEXT || viewType == ITEM_TYPE_ACCOUNT) {
                    view = inflater.inflate(R.layout.sim_selection_item_text, null);
                    holder.mText = (TextView)view.findViewById(R.id.text);
                }
                holder.mRadioButton = (RadioButton)view.findViewById(R.id.select);
                view.setTag(holder);
            }
            
            holder = (ViewHolder)view.getTag();

            if(holder.mRadioButton != null) {
                if(mSelected) {
                    holder.mRadioButton.setVisibility(View.VISIBLE);
                } else
                    holder.mRadioButton.setVisibility(View.GONE);
            }

            if(viewType == ITEM_TYPE_SIM) {
                SIMInfo simInfo = (SIMInfo)mItems.get(position).data;
                holder.mDisplayName.setText(simInfo.mDisplayName);
                //holder.mPhoneNumber.setText(simInfo.mNumber);
                holder.mSimIcon.setBackgroundResource(Telephony.SIMBackgroundRes[simInfo.mColor]);

                log("mDisplayName         = "+simInfo.mDisplayName);
                log("mPhoneNumber         = "+simInfo.mNumber);
                log("mDisplayNumberFormat = "+simInfo.mDispalyNumberFormat);

                if(simInfo.mSimId == mSuggestedSimId)
                    holder.mSuggested.setVisibility(View.VISIBLE);
                else
                    holder.mSuggested.setVisibility(View.GONE);

                if(mSelected && holder.mRadioButton != null) {
                    holder.mRadioButton.setChecked(mSelectedId == simInfo.mSimId);
                }

                try {
                    String shortNumber = "";
                    //RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)holder.mDisplayName.getLayoutParams();
                    ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                    if(!TextUtils.isEmpty(simInfo.mNumber)) {
                        switch(simInfo.mDispalyNumberFormat) {
                            //case android.provider.Telephony.SimInfo.DISPLAY_NUMBER_DEFAULT:
                            case android.provider.Telephony.SimInfo.DISPLAY_NUMBER_FIRST:
                                if(simInfo.mNumber.length() <= 4)
                                    shortNumber = simInfo.mNumber;
                                else
                                    shortNumber = simInfo.mNumber.substring(0, 4);
                                break;
                            case android.provider.Telephony.SimInfo.DISPLAY_NUMBER_LAST:
                                if(simInfo.mNumber.length() <= 4)
                                    shortNumber = simInfo.mNumber;
                                else
                                    shortNumber = simInfo.mNumber.substring(simInfo.mNumber.length()-4, simInfo.mNumber.length());
                                break;
                            case 0://android.provider.Telephony.SimInfo.DISPLAY_NUMBER_NONE:
                                shortNumber = "";
                                break;
                        }
                        if(!TextUtils.isEmpty(shortNumber)) {
                            holder.mPhoneNumber.setText(simInfo.mNumber);
                            holder.mPhoneNumber.setVisibility(View.VISIBLE);
                            //layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, 0);
                        } else {
                            //layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                            holder.mPhoneNumber.setVisibility(View.GONE);
                        }
                    } else {
                        //layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                        holder.mPhoneNumber.setVisibility(View.GONE);
                    }
                    holder.mShortPhoneNumber.setText(shortNumber);
                    //holder.mDisplayName.setLayoutParams(layoutParams);
                    holder.mSimSignal.setVisibility(View.INVISIBLE);
                    String result = SystemProperties.get("gsm.baseband.capability"); 
                    String result2 = SystemProperties.get("gsm.baseband.capability2");
                    Log.i(TAG,"result is " + result);
                    Log.i(TAG,"result2 is " + result2);
                    if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
                        if("OP02".equals(ContactsUtils.getOptrProperties())) {
                            if(simInfo.mSlot == com.android.internal.telephony.Phone.GEMINI_SIM_1)
                                holder.mSimSignal.setVisibility(View.VISIBLE);
                        } else {
                            /*
                            if (simInfo.mSlot == com.android.internal.telephony.Phone.GEMINI_SIM_1) {
                                if (!TextUtils.isEmpty(result) && Integer.valueOf(result) > 3) {
                                    holder.mSimSignal.setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (!TextUtils.isEmpty(result2) && Integer.valueOf(result2) > 3) {
                                        holder.mSimSignal.setVisibility(View.VISIBLE);
                                } 
                            }
                            */
                        }
                    } else {
//                        if (!TextUtils.isEmpty(result) && Integer.valueOf(result) > 3) {
                                holder.mSimSignal.setVisibility(View.INVISIBLE);
//                        }
                    }
                } catch(Exception e) {
                    holder.mShortPhoneNumber.setText("");
                }
                holder.mSimStatus.setImageResource(getSimStatusIcon(simInfo.mSlot));
            } else if(viewType == ITEM_TYPE_INTERNET) {
                if(mSelected && holder.mRadioButton != null) {
                    holder.mRadioButton.setChecked(mSelectedId == RecentCallsListActivity.FILTER_SIP_CALL);
                }
                holder.mInternetIcon.setBackgroundResource(com.mediatek.internal.R.drawable.sim_background_sip);
            } else if(viewType == ITEM_TYPE_TEXT) {
                if(mSelected && holder.mRadioButton != null) {
                    holder.mRadioButton.setChecked(mSelectedId == RecentCallsListActivity.FILTER_ALL_RESOURCES);
                }
                String text = (String)mItems.get(position).data;
                holder.mText.setText(text);
            } else if(viewType == ITEM_TYPE_ACCOUNT) {
                Account account = (Account)mItems.get(position).data;
                holder.mText.setText((String)account.name);
            }

            return view;
        }
        
        protected int getSimStatusIcon(int slot) {
            TelephonyManagerEx telephonyManager = TelephonyManagerEx.getDefault();
            int state = telephonyManager.getSimIndicatorStateGemini(slot);
            int resourceId = 0;
            switch(state) {
                case com.android.internal.telephony.Phone.SIM_INDICATOR_LOCKED:
                    resourceId = com.mediatek.internal.R.drawable.sim_locked;
                    break;
                case com.android.internal.telephony.Phone.SIM_INDICATOR_RADIOOFF:
                    resourceId = com.mediatek.internal.R.drawable.sim_radio_off;
                    break;
                case com.android.internal.telephony.Phone.SIM_INDICATOR_ROAMING:
                    resourceId = com.mediatek.internal.R.drawable.sim_roaming;
                    break;
                case com.android.internal.telephony.Phone.SIM_INDICATOR_SEARCHING:
                    resourceId = com.mediatek.internal.R.drawable.sim_searching;
                    break;
                case com.android.internal.telephony.Phone.SIM_INDICATOR_INVALID:
                    resourceId = com.mediatek.internal.R.drawable.sim_invalid;
                    break;
                case com.android.internal.telephony.Phone.SIM_INDICATOR_CONNECTED:
                    resourceId = com.mediatek.internal.R.drawable.sim_connected;
                    break;
                case com.android.internal.telephony.Phone.SIM_INDICATOR_ROAMINGCONNECTED:
                    resourceId = com.mediatek.internal.R.drawable.sim_roaming_connected;
                    break;
            }
            log("getSimStatusIcon, state = "+state);
            return resourceId;
        }
        
        private class ViewHolder {
            View      mSimIcon;
            ImageView mSimStatus;
            TextView mSimSignal;
            TextView  mShortPhoneNumber;
            TextView  mDisplayName;
            TextView  mPhoneNumber;
            TextView  mSuggested;
            TextView  mText;
            ImageView mInternetIcon;
            RadioButton mRadioButton;
        }
    }
    
    public static class ItemHolder {
        public Object data;
        public int type;
        
        public ItemHolder(Object data, int type) {
            this.data = data;
            this.type = type; 
        }
    }
    
    public static boolean getSimContactsReady(ContextWrapper context, int slotId) {
        boolean retval = false;
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_SIM_CONTACTS_READY, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
        retval = preferences.getBoolean(slotId == 0 ? PREFERENCE_KEY_SIM_CONTACTS_1 : PREFERENCE_KEY_SIM_CONTACTS_2, false);
        return retval;
    }
    
    public static void setSimContactsReady(ContextWrapper context, int slotId, boolean isReady) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_SIM_CONTACTS_READY, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor editor = preferences.edit();
        String key = PREFERENCE_KEY_SIM_CONTACTS_1;
        if(slotId == 1)
            key = PREFERENCE_KEY_SIM_CONTACTS_2;
        editor.putBoolean(key, isReady);
        editor.apply();
    }
    /* added by xingping.zheng end   */
}
