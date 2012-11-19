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
 * Copyright (C) 2007 The Android Open Source Project
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

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.Intents.Insert;
import android.provider.Telephony.SIMInfo;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;
import android.content.IntentFilter;

import com.android.contacts.ContactsUtils.ItemHolder;
import com.android.contacts.ContactsUtils.SIMInfoWrapper;
import com.android.internal.telephony.CallerInfo;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.mediatek.CellConnService.CellConnMgr;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.telephony.PhoneNumberFormatUtilEx;
import com.android.internal.telephony.TelephonyIntents;
import android.os.SystemProperties;
/**
 * Displays a list of call log entries.
 */
public class RecentCallsListActivity extends ListActivity implements
		View.OnCreateContextMenuListener, View.OnClickListener, ContactsUtils.CellConnMgrClient {

	private boolean bLog = false;

	void mylog(String tag, String msg) {
		if (bLog) {
			Log.i(tag, msg);
		}
	}

	private static final String TAG = "RecentCallsList";
	
	private static final String LIST_STATE_KEY = "liststate";
	private Parcelable mListState = null;
	
	private static final boolean DEBUG = true;
	private static AlertDialog mSimFilterAlertDialog = null;
	private static AlertDialog mDelAllAlertDialog = null;
	private static AlertDialog mAddOrEditContactDialog = null;

	/** The projection to use when querying the call log table */
	static final String[] CALL_LOG_PROJECTION = new String[] { Calls._ID,
			Calls.NUMBER, Calls.DATE, Calls.DURATION, Calls.TYPE,
			Calls.CACHED_NAME, Calls.CACHED_NUMBER_TYPE,
			Calls.CACHED_NUMBER_LABEL, Calls.SIM_ID, Calls.VTCALL};
	
	static final String[] CALL_LOG_PROJECTION2 = new String[] { Calls._ID,
		Calls.NUMBER, Calls.DATE, Calls.DURATION, Calls.TYPE,
		Calls.CACHED_NAME, Calls.CACHED_NUMBER_TYPE,
		Calls.CACHED_NUMBER_LABEL, Calls.SIM_ID, Calls.VTCALL,
		Calls.NEW,
		ContactsContract.RawContacts.INDICATE_PHONE_SIM,
		Calls.NEW,
		Calls.NEW};
	
	
    static final int ID_COLUMN_INDEX = 0;
    static final int NUMBER_COLUMN_INDEX = 1;
    static final int DATE_COLUMN_INDEX = 2;
    static final int DURATION_COLUMN_INDEX = 3;
    static final int CALL_TYPE_COLUMN_INDEX = 4;
    static final int CALLER_NAME_COLUMN_INDEX = 5;
    static final int CALLER_NUMBERTYPE_COLUMN_INDEX = 6;
    static final int CALLER_NUMBERLABEL_COLUMN_INDEX = 7;
    static final int CALLER_SIMID_INDEX = 8;
    static final int CALLER_VT_INDEX = 9;
    static final int CALLER_PHOTO_ID_COLUMN_INDEX = 10;
    static final int CALLER_INDICATE_PHONE_SIM_COLUMN_INDEX = 11;
    static final int CALLER_CONTACT_ID_COLUMN_INDEX = 12;
    static final int CALLER_CONTACT_LOOK_UP_KEY_COLUMN_INDEX = 13;

    static final int SIM_SLOT_FIRST = 0;
	static final int SIM_SLOT_SECOND = 1;

	/** The projection to use when querying the phones table */
	static final String[] PHONES_PROJECTION = new String[] { PhoneLookup._ID,
			PhoneLookup.DISPLAY_NAME, PhoneLookup.TYPE, PhoneLookup.LABEL,
			PhoneLookup.NUMBER, PhoneLookup.PHOTO_ID, PhoneLookup.INDICATE_PHONE_SIM, PhoneLookup.LOOKUP_KEY};

	static final int PERSON_ID_COLUMN_INDEX = 0;
	static final int NAME_COLUMN_INDEX = 1;
	static final int PHONE_TYPE_COLUMN_INDEX = 2;
	static final int LABEL_COLUMN_INDEX = 3;
	static final int MATCHED_NUMBER_COLUMN_INDEX = 4;
	static final int PHOTO_ID_COLUMN_INDEX = 5;
	static final int INDICATE_PHONE_SIM_COLUMN_INDEX = 6;
	static final int INDICATE_PHONE_LOOK_UP_KEY_INDEX = 7;

	private static final int MENU_ITEM_DELETE_ALL = 1;
	private static final int CONTEXT_MENU_ITEM_DELETE = 1;
	private static final int CONTEXT_MENU_CALL_CONTACT = 2;
	private static final int CONTEXT_MENU_CALL_SIM_VIA = 3;
	private static final int CONTEXT_MENU_NEW_OR_EDIT_CONTACT = 4;
	private static final int CONTEXT_MENU_SPEED_DIAL = 5;
	private static final int CONTEXT_MENU_VT_CALL_SIM_VIA = 6;

	// mtk80909 for Speed Dial
    private static final int MENU_ITEM_SPEED_DIAL = 2;
    
	private static final int QUERY_TOKEN = 53;
	private static final int UPDATE_TOKEN = 54;

	private static final int DIALOG_CONFIRM_DELETE_ALL = 1;

	RecentCallsAdapter mAdapter;
	private QueryHandler mQueryHandler;
	String mVoiceMailNumber;
	String mVoiceMailNumber2 = "";

    // show on the listview, indicating no call logs found
    private TextView mEmptyView;

	// mtk80736 for call log filter
	public static final String SIM_FILTER_PREF = "calllog_sim_filter";
	public static final String TYPE_FILTER_PREF = "calllog_type_filter";

	private static final int FILTER_BASE = 20000;
    private static final int FILTER_SIM_ALL = FILTER_BASE + 1;
    private static final int FILTER_SIM_1 = FILTER_BASE + 2;
    private static final int FILTER_SIM_2 = FILTER_BASE + 3;
    static final int FILTER_SIP_CALL = FILTER_BASE + 4;
    static final int FILTER_ALL_RESOURCES = FILTER_BASE + 5;
    
    private static final int FILTER_TYPE_ALL = FILTER_BASE + 11;
    private static final int FILTER_TYPE_INCOMING = FILTER_BASE + 12;
    private static final int FILTER_TYPE_MISSED = FILTER_BASE + 13;
    private static final int FILTER_TYPE_OUTGOING = FILTER_BASE + 14;


	private static final int FILTER_SIM_DEFAULT = FILTER_SIM_ALL;
	private static final int FILTER_TYPE_DEFAULT = FILTER_TYPE_ALL;

	int mSimFilter;
	int mTypeFilter;
	private boolean mScrollToTop;
	ContactsUtils.SIMInfoWrapper mSIMInfoWrapperAll;
	List<SIMInfo> mInsertedSIMInfoList = null;
	List<SIMInfo> mAllSIMInfoList = null;
	HashMap<Integer,SIMInfo> mAllSimInfoMap = null;
	
	private ContactPhotoLoader mPhotoLoader;
	
	private StatusBarManager mStatusBarMgr;
    //private final BroadcastReceiver mReceiver = new SimIndicatorBroadcastReceiver();
    private Button typeFilter_all;
    private Button typeFilter_outgoing;
    private Button typeFilter_incoming;
    private Button typeFilter_missed;

	static final class ContactInfo {
		public long personId;
		public String name;
		public int type;
		public String label;
		public String number;
		public String formattedNumber;
		public long photoid;
		public String lookupKey;
		public ArrayList<Integer> associatedSimIdAry;
		public int iSimContactsInd;

		public static ContactInfo EMPTY = new ContactInfo();
	}

	public static final class RecentCallsListItemViews {
		TextView line1View;
		TextView labelView;
		TextView numberView;
		TextView dateView;
		ImageView iconView;
		View callView;
		ImageView groupIndicator;
		TextView groupSize;
	}

	static final class CallerInfoQuery {
		String number;
		int position;
		String name;
		int numberType;
		String numberLabel;
	}

	static final class CallerDetailQuery {
		long id;
		long lDataStart;
		long lDataEnd;
		long simId;
		long vtcall;
		long photoid;
		int iSimContactsInd;
		String number;
	}

	boolean sim1Radio = true;
	boolean sim2Radio = true;
	boolean sim1Idle = true;
	boolean sim2Idle = true;
	boolean simReady = true;
	boolean sim1Ready = true;
	boolean sim2Ready = true;
	boolean shouldEnable = true;
	boolean isEmergencyNumber;
	
	String  sim1Number = null;
	String  sim2Number = null;
	String  sim1Name = null;
	String  sim2Name = null;
	long    sim1ID = 0;
	long    sim2ID = 0;
	long    insertedSimCount = 0;
	private boolean istrace = false;
	
	AlertDialog mSelectResDialog = null;
	
	private ContentObserver mContentObserver;

	/**
	 * Shared builder used by {@link #formatPhoneNumber(String)} to minimize
	 * allocations when formatting phone numbers.
	 */
	private static final SpannableStringBuilder sEditable = new SpannableStringBuilder();

	/**
	 * Invalid formatting type constant for {@link #sFormattingType}.
	 */
	private static final int FORMATTING_TYPE_INVALID = -1;

	/**
	 * Cached formatting type for current {@link Locale}, as provided by
	 * {@link PhoneNumberUtils#getFormatTypeForLocale(Locale)}.
	 */
	private static int sFormattingType = FORMATTING_TYPE_INVALID;

	CallLogSectionIndexer mSecIndexer;
	CellConnMgr mCellConnMgr;
    boolean mShowSimIndicator = false;

	/** Adapter class to fill in data for the Call Log */
	final class RecentCallsAdapter extends CursorAdapter implements Runnable,
			ViewTreeObserver.OnPreDrawListener, View.OnClickListener {
		public HashMap<String, ContactInfo> mContactInfo;
		private final LinkedList<CallerInfoQuery> mRequests;
		// save a list info to list the detail information 
		public final ArrayList<CallerDetailQuery> mDetailQuery;
		private LayoutInflater mInflater = null;

		private volatile boolean mDone;
		private boolean mLoading = true;
		ViewTreeObserver.OnPreDrawListener mPreDrawListener;
		private static final int REDRAW = 1;
		private static final int START_THREAD = 2;
		private boolean mFirst;
		private Thread mCallerIdThread;

		private CharSequence[] mLabelArray;

		private Drawable mDrawableIncoming;
		private Drawable mDrawableOutgoing;
		private Drawable mDrawableMissed;

		private Drawable mDrawableIncomingVT;
		private Drawable mDrawableOutgoingVT;
		private Drawable mDrawableMissedVT;

		private Drawable mDrawableIncomingSim1;
		private Drawable mDrawableIncomingSim2;
		private Drawable mDrawableOutgoingSim1;
		private Drawable mDrawableOutgoingSim2;
		private Drawable mDrawableMissedSim1;
		private Drawable mDrawableMissedSim2;

		private static final int TYPE_LIST_ITEM = 0;
		private static final int TYPE_LIST_ITEM_WITH_HEADER = 1;
		private static final int TYPE_LIST_ITEM_COUNT = 2;
		
		/**
		 * Reusable char array buffers.
		 */
		private CharArrayBuffer mBuffer1 = new CharArrayBuffer(128);
		private CharArrayBuffer mBuffer2 = new CharArrayBuffer(128);

		public void onClick(View view) {
			int position = (Integer) view.getTag();
			
			Intent intent = new Intent(RecentCallsListActivity.this,
					CallDetailActivity.class);

			intent.setData(ContentUris.withAppendedId(
					CallLog.Calls.CONTENT_URI, mDetailQuery.get(position).id));

			StickyTabs.saveTab(RecentCallsListActivity.this, getIntent());
			if ((null != mDetailQuery) && (position < mDetailQuery.size())) {
				Bundle bundle = new Bundle();
				bundle.putLong("com.android.contacts.date_start", mDetailQuery
						.get(position).lDataStart);
				bundle.putLong("com.android.contacts.date_end", mDetailQuery
						.get(position).lDataEnd);
				String number = mDetailQuery.get(position).number;
				bundle.putString("com.android.contacts.number", number);
				
				long simid = mDetailQuery.get(position).simId;
				bundle.putLong("com.android.contacts.simid", simid);
				
				long photoid = mDetailQuery.get(position).photoid;
				bundle.putLong("com.android.contacts.photoid", photoid);
				
				int isSimInd = mDetailQuery.get(position).iSimContactsInd;
				bundle.putInt("com.android.contacts.issimind", isSimInd);
				
				intent.putExtras(bundle);
				
				Log.i(TAG, "startDetailActivity startTime:" + mDetailQuery.get(position).lDataStart);
				Log.i(TAG, "startDetailActivity lDataEnd:" + mDetailQuery.get(position).lDataEnd);
				Log.i(TAG, "startDetailActivity number:" + mDetailQuery.get(position).number);
				Log.i(TAG, "startDetailActivity simId:" + mDetailQuery.get(position).simId);
				Log.i(TAG, "startDetailActivity isSimInd:" + isSimInd);
			
				startActivity(intent);
			} else {
				Log.e(TAG, "--Error!!-- : Adapter mDetailQuery:" + mDetailQuery);
				Log.e(TAG, "--Error!!-- : Adapter position:" + position);
				if (null != mDetailQuery) {
					Log.e(TAG, "--Error!!-- : Adapter mDetailQuery.size():" + mDetailQuery.size());
				}
			}
		}

//		private EditPinPreference mButtonEnableFDN;
		
		public boolean onPreDraw() {
			if (mFirst) {
				Log.i(TAG, TAG + " RCLA : onPreDraw ");
				mHandler.sendEmptyMessageDelayed(START_THREAD, 10);
				mFirst = false;
			}
			return true;
		}

		private Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case REDRAW:
					notifyDataSetChanged();
					break;
				case START_THREAD:
					startRequestProcessing();
					break;
				}
			}
		};

		public RecentCallsAdapter(Context context) {
			super(context, null, false);
			
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (null == mInflater) {
				Log.e(TAG, "RecentCallsAdapter : Error!!! : mInflater is null");
			}

			mContactInfo = new HashMap<String, ContactInfo>();
			mRequests = new LinkedList<CallerInfoQuery>();
			mDetailQuery = new ArrayList<CallerDetailQuery>();

			mPreDrawListener = null;

			mDrawableIncoming = getResources().getDrawable(
					R.drawable.ic_call_log_list_incoming_call);
			mDrawableOutgoing = getResources().getDrawable(
					R.drawable.ic_call_log_list_outgoing_call);
			mDrawableMissed = getResources().getDrawable(
					R.drawable.ic_call_log_list_missed_call);

			mDrawableIncomingVT = getResources().getDrawable(
					R.drawable.ic_call_log_list_incoming_vtcall);
			mDrawableOutgoingVT = getResources().getDrawable(
					R.drawable.ic_call_log_list_outgoing_vtcall);
			mDrawableMissedVT = getResources().getDrawable(
					R.drawable.ic_call_log_list_missed_vtcall);

			mDrawableIncomingSim1 = getResources().getDrawable(
					R.drawable.ic_call_log_list_incoming_call1);
			mDrawableIncomingSim2 = getResources().getDrawable(
					R.drawable.ic_call_log_list_incoming_call2);
			mDrawableOutgoingSim1 = getResources().getDrawable(
					R.drawable.ic_call_log_list_outgoing_call1);
			mDrawableOutgoingSim2 = getResources().getDrawable(
					R.drawable.ic_call_log_list_outgoing_call2);
			mDrawableMissedSim1 = getResources().getDrawable(
					R.drawable.ic_call_log_list_missed_call1);
			mDrawableMissedSim2 = getResources().getDrawable(
					R.drawable.ic_call_log_list_missed_call2);
			mLabelArray = getResources().getTextArray(
					com.android.internal.R.array.phoneTypes);
		}

		/**
		 * Requery on background thread when {@link Cursor} changes.
		 */
		@Override
		protected void onContentChanged() {
			// Start async requery
			startQuery();
		}

		void setLoading(boolean loading) {
			mLoading = loading;
		}

		@Override
		public boolean isEmpty() {
			if (mLoading) {
				// We don't want the empty state to show when loading.
				return false;
			} else {
				return super.isEmpty();
			}
		}

		public ContactInfo getContactInfo(String number) {
			return mContactInfo.get(number);
		}

		public void startRequestProcessing() {
			Log.i(TAG, TAG + "RCLA : startRequestProcessing ");
			mDone = false;
			mCallerIdThread = new Thread(this);
			mCallerIdThread.setPriority(Thread.MIN_PRIORITY);
			mCallerIdThread.start();
		}

		public void stopRequestProcessing() {
			mDone = true;
			if (mCallerIdThread != null)
				mCallerIdThread.interrupt();
		}

		public void clearCache() {
			synchronized (mContactInfo) {
				mContactInfo.clear();
			}
		}

		private void updateCallLog(CallerInfoQuery ciq, ContactInfo ci) {
			// Check if they are different. If not, don't update.
			
			Log.i(TAG, TAG + " updateCallLog : Name:  ciq[" + ciq.name + "] ci[" + ci.name + "]");
			Log.i(TAG, TAG + " updateCallLog : Label: ciq[" + ciq.numberLabel + "] ci[" + ci.label + "]");
			Log.i(TAG, TAG + " updateCallLog : Number:ciq[" + ciq.number + "] ci[" + ci.number + "]");
			Log.i(TAG, TAG + " updateCallLog : Type:  ciq[" + ciq.numberType + "] ci[" + ci.type + "]");
			
			if (TextUtils.equals(ciq.name, ci.name)
					&& TextUtils.equals(ciq.numberLabel, ci.label)
					&& ciq.numberType == ci.type) {
				return;
			}
			// He Zhang once commented the above lines to force the call log to
			// update
			// but this caused redundant queries and unnecessary view movements.
			// After discussing with QA, we decided to recover these lines.
			// -- Han Jiang (mtk80909)

			ContentValues values = new ContentValues(3);
			values.put(Calls.CACHED_NAME, ci.name);
			values.put(Calls.CACHED_NUMBER_TYPE, ci.type);
			values.put(Calls.CACHED_NUMBER_LABEL, ci.label);

			try {
				RecentCallsListActivity.this.getContentResolver().update(
						Calls.CONTENT_URI, values,
						Calls.NUMBER + "='" + ciq.number + "'", null);
			} catch (SQLiteDiskIOException e) {
				Log.w(TAG, "Exception while updating call info", e);
			} catch (SQLiteFullException e) {
				Log.w(TAG, "Exception while updating call info", e);
			} catch (SQLiteDatabaseCorruptException e) {
				Log.w(TAG, "Exception while updating call info", e);
			}
		}

		private void enqueueRequest(String number, int position, String name,
				int numberType, String numberLabel) {
			CallerInfoQuery ciq = new CallerInfoQuery();
			
			Log.i(TAG, TAG + " enqueueRequest");
			
			ciq.number = number;
			ciq.position = position;
			ciq.name = name;
			ciq.numberType = numberType;
			ciq.numberLabel = numberLabel;
			synchronized (mRequests) {
				mRequests.add(ciq);
				mRequests.notifyAll();
			}
		}

		private boolean queryContactInfo(CallerInfoQuery ciq) {
            // First check if there was a prior request for the same number
            // that was already satisfied
            ContactInfo info = mContactInfo.get(ciq.number);

            Log.i(TAG, TAG + " queryContactInfo : ciq.number[" + ciq.number + "]");

            boolean needNotify = false;
            if (info != null && info != ContactInfo.EMPTY) {
                return true;
            } else {
                // Ok, do a fresh Contacts lookup for ciq.number.
                boolean infoUpdated = false;

                if (PhoneNumberUtils.isUriNumber(ciq.number)) {
                    // This "number" is really a SIP address.
                    // TODO: This code is duplicated from the
                    // CallerInfoAsyncQuery class. To avoid that, could the
                    // code here just use CallerInfoAsyncQuery, rather than
                    // manually running ContentResolver.query() itself?

                    // We look up SIP addresses directly in the Data table:
                    Uri contactRef = Data.CONTENT_URI;

                    // Note Data.DATA1 and SipAddress.SIP_ADDRESS are
                    // equivalent.
                    //
                    // Also note we use "upper(data1)" in the WHERE clause, and
                    // uppercase the incoming SIP address, in order to do a
                    // case-insensitive match.
                    //
                    // TODO: May also need to normalize by adding "sip:" as a
                    // prefix, if we start storing SIP addresses that way in the
                    // database.
                    String selection = "upper(" + Data.DATA1 + ")=?" + " AND " + Data.MIMETYPE
                            + "='" + SipAddress.CONTENT_ITEM_TYPE + "'";
                    String[] selectionArgs = new String[] {
                        ciq.number.toUpperCase()
                    };

                    Cursor dataTableCursor = RecentCallsListActivity.this.getContentResolver()
                            .query(contactRef, null, // projection
                                    selection, // selection
                                    selectionArgs, // selectionArgs
                                    null); // sortOrder

                    if ((dataTableCursor != null) && (dataTableCursor.getCount() > 0)) {
                        Log.i(TAG, TAG + " queryContactInfo : dataTableCursor is not null. count:"
                                + dataTableCursor.getCount());

                        if (dataTableCursor.moveToFirst()) {
                            info = new ContactInfo();

                            // TODO: we could slightly speed this up using an
                            // explicit projection (and thus not have to do
                            // those getColumnIndex() calls) but the benefit is
                            // very minimal.

                            // Note the Data.CONTACT_ID column here is
                            // equivalent to the PERSON_ID_COLUMN_INDEX column
                            // we use with "phonesCursor" below.
                            info.personId = dataTableCursor.getLong(dataTableCursor
                                    .getColumnIndex(Data.CONTACT_ID));
                            info.name = dataTableCursor.getString(dataTableCursor
                                    .getColumnIndex(Data.DISPLAY_NAME));
                            // "type" and "label" are currently unused for SIP
                            // addresses
                            info.type = 0;
                            info.label = null;

                            // And "number" is the SIP address.
                            // Note Data.DATA1 and SipAddress.SIP_ADDRESS are
                            // equivalent.
                            info.number = dataTableCursor.getString(dataTableCursor
                                    .getColumnIndex(Data.DATA1));

                            long tmpphotoid = dataTableCursor.getLong(dataTableCursor
                                    .getColumnIndex(Data.PHOTO_ID));

                            if (info.photoid != tmpphotoid) {
                                info.photoid = tmpphotoid;
                            }

                            info.iSimContactsInd = 0;

                            Log.i(TAG, TAG + " queryContactInfo SIM : personId:" + info.personId);
                            Log.i(TAG, TAG + " queryContactInfo SIM : name:    " + info.name);
                            Log.i(TAG, TAG + " queryContactInfo SIM : type:    " + info.type);
                            Log.i(TAG, TAG + " queryContactInfo SIM : label:   " + info.label);
                            Log.i(TAG, TAG + " queryContactInfo SIM : number:  " + info.number);
                            Log.i(TAG, TAG + " queryContactInfo SIM : photoid: " + info.photoid);

                            infoUpdated = true;
                        }
                    } else {
                        Log.i(TAG, TAG + " queryContactInfo : dataTableCursor"
                                + ((dataTableCursor == null) ? " is null" : ".count is 0"));
                    }
                    if (null != dataTableCursor) {
                        dataTableCursor.close();
                    }
                } else {
                    // "number" is a regular phone number, so use the
                    // PhoneLookup table:
                    Cursor phonesCursor = RecentCallsListActivity.this.getContentResolver().query(
                            Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri
                                    .encode(ciq.number)), PHONES_PROJECTION, null, null, null);

                    if ((phonesCursor != null) && (phonesCursor.moveToFirst())) {
                        Log.i(TAG, TAG + " queryContactInfo : phonesCursor is not null count:"
                                + phonesCursor.getCount());
                        String tmpNumber = phonesCursor.getString(MATCHED_NUMBER_COLUMN_INDEX);
                        while ((!ciq.number.equals(tmpNumber)) && (phonesCursor.moveToNext())) {
                            tmpNumber = phonesCursor.getString(MATCHED_NUMBER_COLUMN_INDEX);
                        }
                        if (!phonesCursor.isAfterLast()) {
                            info = new ContactInfo();
                            info.personId = phonesCursor.getLong(PERSON_ID_COLUMN_INDEX);
                            info.name = phonesCursor.getString(NAME_COLUMN_INDEX);
                            info.type = phonesCursor.getInt(PHONE_TYPE_COLUMN_INDEX);
                            info.label = phonesCursor.getString(LABEL_COLUMN_INDEX);
                            info.number = phonesCursor.getString(MATCHED_NUMBER_COLUMN_INDEX);
                            info.lookupKey = phonesCursor.getString(INDICATE_PHONE_LOOK_UP_KEY_INDEX);

                            long tmpphotoid = phonesCursor.getLong(PHOTO_ID_COLUMN_INDEX);
                            if (tmpphotoid != info.photoid) {
                                info.photoid = tmpphotoid;
                            }
                            // simid
                            info.iSimContactsInd = phonesCursor
                                    .getInt(INDICATE_PHONE_SIM_COLUMN_INDEX);

                            Log.i(TAG, TAG + " queryContactInfo Num : personId:" + info.personId);
                            Log.i(TAG, TAG + " queryContactInfo Num : name:    " + info.name);
                            Log.i(TAG, TAG + " queryContactInfo Num : type:    " + info.type);
                            Log.i(TAG, TAG + " queryContactInfo Num : label:   " + info.label);
                            Log.i(TAG, TAG + " queryContactInfo Num : number:  " + info.number);
                            Log.i(TAG, TAG + " queryContactInfo Num : photoid: " + info.photoid);
                            Log.i(TAG, TAG + " queryContactInfo Num : lookupKey: " + info.lookupKey);
                            Log.i(TAG, TAG + " queryContactInfo Num : iSimContactsInd: " + info.iSimContactsInd);

                            infoUpdated = true;
                        }
                    } else {
                        Log.i(TAG, TAG + " queryContactInfo : phonesCursor"
                                + ((phonesCursor == null) ? " is null" : ".count is 0"));
                    }
                    if (null != phonesCursor) {
                        phonesCursor.close();
                    }

                    if (null != info) {
                        List simIdList = new ArrayList<Integer>();
                        Cursor associateSIMCursor = RecentCallsListActivity.this
                                .getContentResolver().query(
                                        Data.CONTENT_URI,
                                        new String[] {
                                            Data.SIM_ID
                                        },
                                        Data.MIMETYPE + "='"
                                                + CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                                                + "' AND (" + Data.DATA1 + "='" + ciq.number
                                                + "') AND (" + Data.SIM_ID + ">0)", null, null);

                        if (null == associateSIMCursor) {
                            Log.i(TAG, TAG + " queryContactInfo : associateSIMCursor is null");
                        } else {
                            Log.i(TAG, TAG
                                    + " queryContactInfo : associateSIMCursor is not null. Count["
                                    + associateSIMCursor.getCount() + "]");
                        }

                        if ((null != associateSIMCursor) && (associateSIMCursor.getCount() > 0)) {
                            associateSIMCursor.moveToFirst();
                            // Get only one record is OK
                            info.associatedSimIdAry = new ArrayList<Integer>();
                            for (int j = 0; j < associateSIMCursor.getCount(); j++) {
                                info.associatedSimIdAry.add(associateSIMCursor.getInt(0));
                                associateSIMCursor.moveToNext();
                            }

                        } else {
                            info.associatedSimIdAry = null;
                        }

                        if (null != associateSIMCursor) {
                            associateSIMCursor.close();
                        }
                    }
                }

                Log.i(TAG, TAG + " queryContactInfo : infoUpdated:" + infoUpdated);

                if (infoUpdated) {
                    // New incoming phone number invalidates our formatted
                    // cache. Any cache fills happen only on the GUI thread.
                    info.formattedNumber = null;
                    mContactInfo.put(ciq.number, info);
                    // Inform list to update this item, if in view
                    needNotify = true;
                }
            }

            Log.i(TAG, TAG + " queryContactInfo : info:" + info);
            if (info != null) {
                updateCallLog(ciq, info);
            }
            return needNotify;
        }

		/*
		 * Handles requests for contact name and number type
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			boolean needNotify = false;

			Log.i(TAG, "RCLA : run ");
			while (!mDone) {
				CallerInfoQuery ciq = null;
				synchronized (mRequests) {
					if (!mRequests.isEmpty()) {
						ciq = mRequests.removeFirst();
						if (needNotify) {
                            needNotify = false;
                            Message msg = mHandler.obtainMessage(REDRAW);
                            mHandler.sendMessageAtFrontOfQueue(msg);
//                          mHandler.sendEmptyMessage(REDRAW);
                        }
					} else {
						if (needNotify) {
							needNotify = false;
							Message msg = mHandler.obtainMessage(REDRAW);
							mHandler.sendMessageAtFrontOfQueue(msg);
//							mHandler.sendEmptyMessage(REDRAW);
						}
						try {
							mRequests.wait(1000);
						} catch (InterruptedException ie) {
							// Ignore and continue processing requests
						}
					}
				}
				if (null != ciq) {
//				    Log.i(TAG, TAG + " RCLA : run ciq.number[" + ciq.number);
//				    Log.i(TAG, TAG + " RCLA : run ciq.name[" + ciq.name);
//				    Log.i(TAG, TAG + " RCLA : run ciq.position[" + ciq.position);
//				    Log.i(TAG, TAG + " RCLA : run ciq.numberType[" + ciq.numberType);
				} else {
				    Log.i(TAG, TAG + " RCLA : run ciq is null.");
				}
				
				if (ciq != null && queryContactInfo(ciq)) {
					needNotify = true;
				}
			}
		}
		
		protected boolean equalPhoneNumbers(CharArrayBuffer buffer1,
				CharArrayBuffer buffer2) {

			// TODO add PhoneNumberUtils.compare(CharSequence, CharSequence) to
			// avoid
			// string allocation
			return PhoneNumberUtils.compare(new String(buffer1.data, 0,
					buffer1.sizeCopied), new String(buffer2.data, 0,
					buffer2.sizeCopied));
		}

		@Override
		public void changeCursor(Cursor cursor) {
			// TODO Auto-generated method stub
			mylog(TAG, "RCLA : changeCursor");

			super.changeCursor(cursor);
			updateIndexer(cursor);
		}

		@Override
		public int getItemViewType(int position) {
			// TODO Auto-generated method stub
			int itemViewType = TYPE_LIST_ITEM;
			int section = mSecIndexer.getSectionForPosition(position);
			if (position == mSecIndexer.getPositionForSection(section)) {
				itemViewType = TYPE_LIST_ITEM_WITH_HEADER;
			}
			
			mylog(TAG, "RCLA : getItemViewType position[" + position
						+ "] section[" + section + "] itemViewType[" + itemViewType + "]");
			
			return itemViewType;
		}

		@Override
		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return TYPE_LIST_ITEM_COUNT;
		}
		
		// Today, Yesterday, MM/dd/yyyy
		private String getSectionHeadText(long lDate) {
		    String retDate = null;
		    if (lDate <= 0) {
		        Log.e(TAG, TAG + " getSectionHeadText lDate:" + lDate);
		        return retDate;
		    }
		    
		    String sToday = DateFormat.format("MM/dd/yyyy",
                    System.currentTimeMillis()).toString();

            String sYesterday = DateFormat.format("MM/dd/yyyy",
                    System.currentTimeMillis() - 86400 * 1000).toString();
            
            String sDate = DateFormat.format("MM/dd/yyyy", lDate).toString();
		    
		    if (sDate.equals(sToday)) {
                // sDate = "Today"; // ToDo: to get from source
		        retDate = mContext.getResources().getString(
                        R.string.calllog_today);
            } else if (sDate.equals(sYesterday)) {
                retDate = mContext.getResources().getString(
                        R.string.calllog_yesterday);
            } else {
                retDate = sDate;
            }
		    
		    Log.e(TAG, TAG + " getSectionHeadText      lDate:" + lDate);
		    Log.e(TAG, TAG + " getSectionHeadText      sDate:" + sDate);
		    Log.e(TAG, TAG + " getSectionHeadText     sToday:" + sToday);
		    Log.e(TAG, TAG + " getSectionHeadText sYesterday:" + sYesterday);
		    
		    return retDate;
		}

		private void updateIndexer(Cursor cursor) {
			int count = 0;
			mSecIndexer = null;
			ContactInfo info = null;

			if (cursor == null || ((count = cursor.getCount()) == 0)) {
				mylog(TAG, "RCLA : updateIndexer Error:: cursor:"
						+ cursor);
				return;
			}

			int iListItemCount = 0;
			int isimid1 = -1;
			int isimid2 = -1;
			long lDate1 = 0;
			long lDate2 = 0;
			long vtCall = 0;
			long lPhotoId1 = 0;
			long lPhotoId2 = 0;
			int iSimIndicator1 = -1;
			int iSimIndicator2 = -1;

			// to update data in mDetailQuery
			mDetailQuery.clear();

			CharArrayBuffer currentValue = mBuffer1;
			CharArrayBuffer value = mBuffer2;

			ArrayList<String> alSections = new ArrayList<String>();
			ArrayList<Integer> alCounts = new ArrayList<Integer>();

			cursor.moveToFirst();
			cursor.copyStringToBuffer(NUMBER_COLUMN_INDEX, currentValue);
			lDate1 = cursor.getLong(DATE_COLUMN_INDEX);
			String sDate = DateFormat.format("MM/dd/yyyy", lDate1).toString();
			isimid1 = cursor.getInt(CALLER_SIMID_INDEX);
			vtCall = cursor.getLong(CALLER_VT_INDEX);
			lPhotoId1 = cursor.getLong(CALLER_PHOTO_ID_COLUMN_INDEX);
			iSimIndicator1 = cursor.getInt(CALLER_INDICATE_PHONE_SIM_COLUMN_INDEX);

			String sToday = DateFormat.format("MM/dd/yyyy",
					System.currentTimeMillis()).toString();

			String sYesterday = DateFormat.format("MM/dd/yyyy",
					System.currentTimeMillis() - 86400 * 1000).toString();
			
			info = getContactInfo(new String(currentValue.data, 0,
					currentValue.sizeCopied));
			Log.i(TAG, "RCLA : updateIndexer : to searchContactInfo 1" + currentValue);
			if (info == null) {
				info = searchContactInfo(cursor);
			}

//			int currentCallType = cursor.getInt(CALL_TYPE_COLUMN_INDEX);
			for (int i = 1; i < count; i++) {
				cursor.moveToNext();
				cursor.copyStringToBuffer(NUMBER_COLUMN_INDEX, value);
				boolean sameGroup = equalPhoneNumbers(value, currentValue);
				isimid2 = cursor.getInt(CALLER_SIMID_INDEX);
				sameGroup = (sameGroup && (isimid1 == isimid2));
				lDate2 = cursor.getLong(DATE_COLUMN_INDEX);
				String sDate2 = DateFormat.format("MM/dd/yyyy", lDate2)
						.toString();
				lPhotoId2 = cursor.getLong(CALLER_PHOTO_ID_COLUMN_INDEX);
				iSimIndicator2 = cursor.getInt(CALLER_INDICATE_PHONE_SIM_COLUMN_INDEX);
				
				info = getContactInfo(new String(value.data, 0,
						value.sizeCopied));
				Log.i(TAG, "RCLA : updateIndexer : to searchContactInfo 2" + value);
				if (info == null) {
					info = searchContactInfo(cursor);
				}
				
				mylog(TAG, "RCLA : updateIndexer Begin: Date:"
						+ DateFormat.format("MM/dd/yyyy", lDate1).toString()
						+ " sameGroup:" + sameGroup);

				// Group adjacent calls with the same number. Make an exception
				// for the latest item if it was a missed call. We don't want
				// a missed call to be hidden inside a group.
				// if (sameNumber && currentCallType != Calls.MISSED_TYPE) {
				// number and id is the same
				// if (sameGroup) {
				//
				// } else {
				iListItemCount++;

				mylog(TAG, "RCLA : updateIndexer sDate:" + sDate
						+ " sToday:" + sToday);

				CallerDetailQuery sDetailQuery = new CallerDetailQuery();
				sDetailQuery.id = cursor.getLong(ID_COLUMN_INDEX);
				sDetailQuery.lDataStart = lDate2;
				sDetailQuery.lDataEnd = lDate1;
				sDetailQuery.number = new String(currentValue.data, 0,
						currentValue.sizeCopied);
				sDetailQuery.simId = isimid1;
				sDetailQuery.vtcall = vtCall;
				sDetailQuery.iSimContactsInd = iSimIndicator1;
				sDetailQuery.photoid = lPhotoId1;
				mDetailQuery.add(sDetailQuery);

				vtCall = cursor.getLong(CALLER_VT_INDEX);
				
				info = getContactInfo(sDetailQuery.number);
				
				if (info == null) {
					info = searchContactInfo(cursor);
				}

				if (sDate.equals(sDate2)) {

				} else {
//					if (sDate.equals(sToday)) {
//						// sDate = "Today"; // ToDo: to get from source
//						sDate = mContext.getResources().getString(
//								R.string.calllog_today);
//						mylog(TAG,
//								"RCLA : updateIndexer sToday == sDate : Res:"
//										+ sDate);
//					} else {
//						mylog(TAG, "RCLA : updateIndexer sYesterday:"
//								+ sYesterday);
//						if (sDate.equals(sYesterday)) {
//							sDate = mContext.getResources().getString(
//									R.string.calllog_yesterday);
//							mylog(TAG,
//									"RCLA : updateIndexer sYesterday == sDate : Res:"
//											+ sYesterday);
//						}
//					}
					sDate = getSectionHeadText(lDate1);
					alSections.add(sDate);
					alCounts.add(iListItemCount);
					
					sDate = sDate2;
					iListItemCount = 0;
				}
				
				// Swap buffers
				CharArrayBuffer temp = currentValue;
				currentValue = value;
				value = temp;
				isimid1 = isimid2;
				lDate1 = lDate2;
				lPhotoId1 = lPhotoId2;
				iSimIndicator1 = iSimIndicator2;
			}

			CallerDetailQuery sDetailQuery = new CallerDetailQuery();
			sDetailQuery.id = cursor.getLong(ID_COLUMN_INDEX);
			sDetailQuery.lDataStart = 0;
			sDetailQuery.lDataEnd = lDate1;
			sDetailQuery.number = new String(currentValue.data, 0,
					currentValue.sizeCopied);
			sDetailQuery.simId = isimid1;
			sDetailQuery.vtcall = cursor.getLong(CALLER_VT_INDEX);
			sDetailQuery.iSimContactsInd = iSimIndicator1;
			sDetailQuery.photoid = lPhotoId1;
			mDetailQuery.add(sDetailQuery);

			Log.i(TAG, "RCLA : updateIndexer mDetailQuery.size:"
					+ mDetailQuery.size());

			// Add the latest value:
			sDate = getSectionHeadText(lDate1);
			alSections.add(sDate);
			alCounts.add(iListItemCount + 1);

			alSections.trimToSize();
			alCounts.trimToSize();

			int iSecLen = alSections.size();
			int iCountLen = alCounts.size();

			if (iSecLen != iCountLen) {
				mylog(TAG,
						"RCLA : updateIndexer ------ Error!!! igroupcount:"
								+ " alSections Size:" + iSecLen
								+ " alCounts Size:" + iCountLen);
			}

			String sections[] = new String[iSecLen];
			int counts[] = new int[iSecLen];

			for (int iIndex = 0; iIndex < iSecLen; iIndex++) {
				sections[iIndex] = alSections.get(iIndex);
				counts[iIndex] = alCounts.get(iIndex);
			}

			mylog(TAG, "RCLA : changeCursor sections[0]" + sections[0]);
			mylog(TAG, "RCLA : changeCursor counts[0]" + counts[0]);
			mSecIndexer = new CallLogSectionIndexer(sections, counts);

			if (sections != null && counts != null) {
				mylog(TAG, "RCLA : updateIndexer sections Len:"
						+ sections.length + " counts Len:" + counts.length);

			} else {
				mylog(TAG, "RCLA : updateIndexer sections:" + sections
						+ "counts:" + counts);
			}

			for (int i = 0; i < iSecLen; i++) {
				mylog(TAG, "RCLA : updateIndexer completed sections:["
						+ i + "]:[" + sections[i] + "] count[" + i + "]:["
						+ counts[i] + "]");
			}

			alSections.clear();
			alSections = null;
			alCounts.clear();
			alCounts = null;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder vholder;
			int section = mSecIndexer.getSectionForPosition(position);
			
//			if (null != convertView) {
//				vholder = (ViewHolder)convertView.getTag();
//				if (vholder.position != position) {
//					convertView = null;
//				}
//			}
			
			if (null == convertView) {
//				convertView = LayoutInflater.from(mContext).inflate(R.layout.recent_calls_list_item_with_head_layout, null);
//				
//				vholder = new ViewHolder();
//				vholder.headTextView = (TextView)convertView.findViewById(R.id.calllog_head);
//				vholder.nameTextView = (TextView)convertView.findViewById(R.id.calllog_name);
//				vholder.labelTextView = (TextView)convertView.findViewById(R.id.calllog_label);
//				vholder.numberTextView = (TextView)convertView.findViewById(R.id.calllog_number);
//				vholder.simNameTextView = (TextView)convertView.findViewById(R.id.calllog_sim_name);
//				vholder.timeTextView = (TextView)convertView.findViewById(R.id.calllog_time);
//		        
//				vholder.photoImageView = (ImageView)convertView.findViewById(R.id.calllog_photo);
//				vholder.detailImageView = (ImageView)convertView.findViewById(R.id.calllog_detail);
//				vholder.calltypeImageView = (ImageView)convertView.findViewById(R.id.calllog_type);
//				
//				vholder.detailImageView.setOnClickListener(this);
				vholder = new ViewHolder();
				//Just use similar code for both single SIM and dual SIM.
				//TBD: check if needs to merge the 2 part when they work well.
				if (FeatureOption.MTK_GEMINI_SUPPORT) {
					convertView = getLayoutInflater().inflate(
							R.layout.recent_calls_list_item_with_head_layout, parent, false);
					Log.i(TAG, "[Dual SIM]RCLA : newView position:[" + position + "]");
					
					vholder.headTextView = (TextView)convertView.findViewById(R.id.calllog_head);
					vholder.nameTextView = (TextView)convertView.findViewById(R.id.calllog_name);
					vholder.labelTextView = (TextView)convertView.findViewById(R.id.calllog_label);
					vholder.numberTextView = (TextView)convertView.findViewById(R.id.calllog_number);
					vholder.simNameTextView = (TextView)convertView.findViewById(R.id.calllog_sim_name);
					vholder.timeTextView = (TextView)convertView.findViewById(R.id.calllog_time);
					vholder.photoImageView = (QuickContactBadge)convertView.findViewById(R.id.calllog_photo);
					vholder.detailImageView = (ImageView)convertView.findViewById(R.id.calllog_detail);
					vholder.calltypeImageView = (ImageView)convertView.findViewById(R.id.calllog_type);
					vholder.detailImageView.setOnClickListener(this);
					vholder.position = position;
				} else {
					convertView = getLayoutInflater().inflate(
							R.layout.recent_calls_list_item_with_head_layout_single, parent, false);
					Log.i(TAG, "[Single SIM]RCLA : newView position:[" + position + "]");
					
					vholder.headTextView = (TextView)convertView.findViewById(R.id.calllog_head);
					vholder.nameTextView = (TextView)convertView.findViewById(R.id.calllog_name);
					vholder.labelTextView = (TextView)convertView.findViewById(R.id.calllog_label);
					vholder.numberTextView = (TextView)convertView.findViewById(R.id.calllog_number);
					vholder.simNameTextView = null;
					vholder.timeTextView = (TextView)convertView.findViewById(R.id.calllog_time);
					vholder.photoImageView = (QuickContactBadge)convertView.findViewById(R.id.calllog_photo);
					vholder.detailImageView = (ImageView)convertView.findViewById(R.id.calllog_detail);
					vholder.calltypeImageView = (ImageView)convertView.findViewById(R.id.calllog_type);
					vholder.detailImageView.setOnClickListener(this);
					vholder.position = position;
				}
				
				convertView.setTag(vholder);
			} else {
				vholder = (ViewHolder)convertView.getTag();
			} 
			
			vholder.position = position;
			
			Cursor cursor = mCursor;
			if (position <= cursor.getCount()) {
				cursor.moveToPosition(position);
			}
			
			bindView(convertView, mContext, cursor);
			
			return convertView;
			
			/* ========================================== */

//			mylog(TAG, "RCLA : getView : convertView:" + convertView);
//			CallLogListItemView vgCallLogListItem = null;
//			if (null == convertView) {
//				vgCallLogListItem = (CallLogListItemView) new CallLogListItemView(
//						mContext, null, parent);
//				// vgCallLogListItem.setTag(new Integer(position));
//			} else {
//				vgCallLogListItem = (CallLogListItemView) convertView;
//			}
//
//			mylog(TAG, "RCLA : getView : ptst position:" + position);
//
//			Cursor cursor = mCursor;
//			if (position <= cursor.getCount()) {
//				cursor.moveToPosition(position);
//			}
//			
//			bindView(vgCallLogListItem, mContext, cursor);
//			return vgCallLogListItem;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			mylog(TAG, "RCLA : newView");

			int position = cursor.getPosition();
			Log.i(TAG, "RCLA : newView position:" + position);
			
			View listItemView = null;
			ViewHolder vholder;
			vholder = new ViewHolder();
			int section = mSecIndexer.getSectionForPosition(position);
			
			if (FeatureOption.MTK_GEMINI_SUPPORT) {
				listItemView = getLayoutInflater().inflate(
						R.layout.recent_calls_list_item_with_head_layout, parent, false);
				Log.i(TAG, "[Dual SIM]RCLA : newView position:[" + position + "]");
				
				vholder.headTextView = (TextView)listItemView.findViewById(R.id.calllog_head);
				vholder.nameTextView = (TextView)listItemView.findViewById(R.id.calllog_name);
				vholder.labelTextView = (TextView)listItemView.findViewById(R.id.calllog_label);
				vholder.numberTextView = (TextView)listItemView.findViewById(R.id.calllog_number);
				vholder.simNameTextView = (TextView)listItemView.findViewById(R.id.calllog_sim_name);
				vholder.timeTextView = (TextView)listItemView.findViewById(R.id.calllog_time);
				vholder.photoImageView = (QuickContactBadge)listItemView.findViewById(R.id.calllog_photo);
				vholder.detailImageView = (ImageView)listItemView.findViewById(R.id.calllog_detail);
				vholder.calltypeImageView = (ImageView)listItemView.findViewById(R.id.calllog_type);
				vholder.detailImageView.setOnClickListener(this);
				vholder.position = position;
			} else {
				listItemView = getLayoutInflater().inflate(
						R.layout.recent_calls_list_item_with_head_layout_single, parent, false);
				Log.i(TAG, "[Single SIM]RCLA : newView position:[" + position + "]");
				
				vholder.headTextView = (TextView)listItemView.findViewById(R.id.calllog_head);
				vholder.nameTextView = (TextView)listItemView.findViewById(R.id.calllog_name);
				vholder.labelTextView = (TextView)listItemView.findViewById(R.id.calllog_label);
				vholder.numberTextView = (TextView)listItemView.findViewById(R.id.calllog_number);
				vholder.simNameTextView = null;
				vholder.timeTextView = (TextView)listItemView.findViewById(R.id.calllog_time);
				vholder.photoImageView = (QuickContactBadge)listItemView.findViewById(R.id.calllog_photo);
				vholder.detailImageView = (ImageView)listItemView.findViewById(R.id.calllog_detail);
				vholder.calltypeImageView = (ImageView)listItemView.findViewById(R.id.calllog_type);
				vholder.calltypeImageView.setVisibility(View.VISIBLE);
				vholder.detailImageView.setOnClickListener(this);
				vholder.position = position;
			}
			
			listItemView.setTag(vholder);
			
			return listItemView;
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			Log.i(TAG, "RCLA : bindView view()+ ");

			if (null == view) {
				return;
			}
			
			ViewHolder vholder;
			vholder = (ViewHolder) view.getTag();

			int position = cursor.getPosition();

			mylog(TAG, "RCLA : bindView : ptst view:" + position);

			// Header
			int section = mSecIndexer.getSectionForPosition(position);

			Log.i(TAG, "RCLA : bindView position:" + position
					+ " Sec:" + section
					+ " Pos:" + mSecIndexer.getPositionForSection(section));

			String txt = null; // = mStrings[position];
			if (position == mSecIndexer.getPositionForSection(section)) {

				txt = (String) mSecIndexer.getSections()[section];
				mylog(TAG, "RCLA : bindView section[" + section
						+ "] position[" + position + "] title["
						+ mSecIndexer.getSections()[section] + "]");
				vholder.headTextView.setText(txt);
//				vgCallLogListItem.setSectionHeader(txt);
			} else {
//				vgCallLogListItem.setSectionHeader(null);
				mylog(TAG, "RCLA : bindView section[" + section
						+ "] position[" + position + "] title[no title]");
				vholder.headTextView.setVisibility(View.GONE);
			}

			String number = cursor.getString(NUMBER_COLUMN_INDEX);
			ContactInfo info = getContactInfo(number);
			
			if (info == null) {
				info = searchContactInfo(cursor);
			}
			
			String callerName = info.name;
			int callerNumberType = info.type;
			String callerNumberLabel = info.label;
			
			String calllogName = cursor.getString(CALLER_NAME_COLUMN_INDEX);
			
			// if (TextUtils.isEmpty(callerName) && !TextUtils.isEmpty(calllogName)) {
			    callerName = calllogName;
			    callerNumberType = cursor.getInt(CALLER_NUMBERTYPE_COLUMN_INDEX);
			    callerNumberLabel = cursor.getString(CALLER_NUMBERLABEL_COLUMN_INDEX);
			// }

			int icallerType = cursor.getInt(CALL_TYPE_COLUMN_INDEX);
			int simId = cursor.getInt(CALLER_SIMID_INDEX);
			long callDate = cursor.getLong(DATE_COLUMN_INDEX);

			// Photo
			long tmpPhotoId = 0;
			if(!Intent.ACTION_SEARCH.equals(RecentCallsListActivity.this.getIntent().getAction())){
				tmpPhotoId = cursor.getInt(CALLER_PHOTO_ID_COLUMN_INDEX);
			}
			Log.i(TAG, "bindView : tmpPhotoId:" + tmpPhotoId);
			if ((tmpPhotoId <=0) && (0 != info.photoid)) {
			    tmpPhotoId = info.photoid;
			}

            int phoneIndicator = cursor.getInt(CALLER_INDICATE_PHONE_SIM_COLUMN_INDEX);
            info.iSimContactsInd = phoneIndicator;
            int slotId = SIMInfo.getSlotById(RecentCallsListActivity.this, phoneIndicator);

            Log.i(TAG, "bindView : phoneIndicator:" + phoneIndicator + " slotId:" + slotId);
        	int contactId = cursor.getInt(CALLER_CONTACT_ID_COLUMN_INDEX);
        	
        	Log.i(TAG, "bindView: contactId:" + contactId + " info.personId:" + info.personId);
        	if ((contactId <= 0) && (info.personId > 0)) {
        	    contactId = (int)info.personId;
        	}
            if (contactId > 0) {
            	vholder.photoImageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                    	((QuickContactBadge)v).onClick(v);
                    }
            	});
            	String lookupKey = cursor.getString(CALLER_CONTACT_LOOK_UP_KEY_COLUMN_INDEX);
            	Log.i(TAG, "bindView: lookupKey:" + lookupKey + " info.lookupKey:" + info.lookupKey);
            	if (TextUtils.isEmpty(lookupKey) && (!TextUtils.isEmpty(info.lookupKey))) {
            	    lookupKey = info.lookupKey;
            	}
            	vholder.photoImageView.setTag(null);
                vholder.photoImageView.setExcludeMimes(new String[] { Contacts.CONTENT_ITEM_TYPE });
                vholder.photoImageView.assignContactUri(Contacts.getLookupUri(contactId, lookupKey));
            } else {
            	if (number != null) {
                	vholder.photoImageView.setTag(number);
                	vholder.photoImageView.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							// TODO Auto-generated method stub
	                    	String number = (String)v.getTag();
	                    	if ((number != null) && (!isSpecialNumber(number)))
	                    		addOReditContactPromtion(number);
	                    }
					});
            	}
            }
            Log.i(TAG, "bindView: tmpPhotoId:" + tmpPhotoId + " slotId:" + slotId);
            mPhotoLoader.loadPhoto(vholder.photoImageView, tmpPhotoId, slotId);

//			if (0 != tmpPhotoId) {
//				mPhotoLoader.loadPhoto(vholder.photoImageView, tmpPhotoId, slotId);
//			} else {
////				Drawable drawPhoto = mContext.getResources().getDrawable(
////						R.drawable.calllog_photo);
////				mPhotoImageView.setImageDrawable(drawPhoto);
//				if (slotId > 0) { // ToDo: to check the SIM Contact Photo
//					mPhotoLoader.loadPhoto(vholder.photoImageView, 0, info.iSimContactsInd);
//				} else {
//				    vholder.photoImageView.setImageResource(R.drawable.contacts_unknow_image);
//				}
//			}

            // Name, number, label: if no name then set number as name
            boolean bVoiceMailNumber = false;
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                bVoiceMailNumber = ((number.equals(mVoiceMailNumber) && (simId == (int) sim1ID)) 
                        || (number.equals(mVoiceMailNumber2) && (simId == (int) sim2ID)));
            } else {
                bVoiceMailNumber = number.equals(mVoiceMailNumber);
            }
            if (bVoiceMailNumber){
                tmpPhotoId = 0;
            }
            Log.i(TAG, "bindView: tmpPhotoId:" + tmpPhotoId + " slotId:" + slotId);
            mPhotoLoader.loadPhoto(vholder.photoImageView, tmpPhotoId, slotId);
            if ((null == callerName) || bVoiceMailNumber) {
                if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
                    vholder.nameTextView.setText(R.string.unknown);
                } else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
                    vholder.nameTextView.setText(R.string.private_num);
                } else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
                    vholder.nameTextView.setText(R.string.payphone);
                } else if(bVoiceMailNumber) {
                    vholder.nameTextView.setText(R.string.voicemail);
                } else {
                    vholder.nameTextView.setText(formatPhoneNumber(number));
                }
                vholder.labelTextView.setVisibility(View.GONE);
                vholder.numberTextView.setVisibility(View.GONE);
                // no label and number TextView
            } else {
				vholder.nameTextView.setText(callerName);
				// Label
				int iResLabel = CommonDataKinds.Phone
						.getTypeLabelResource(callerNumberType);
				String sResLabel = mContext.getResources().getString(iResLabel);
				vholder.labelTextView.setText(sResLabel);

				// number
				vholder.numberTextView.setText(PhoneNumberFormatUtilEx.formatNumber(number));
				vholder.labelTextView.setVisibility(View.GONE);
                vholder.numberTextView.setVisibility(View.GONE);
			}
			// Restore number in name TextView;
			vholder.nameTextView.setTag(number);
			
			int ivtCall = cursor.getInt(CALLER_VT_INDEX);

			// CallType / I
			int lCallTypeRes = getCallTypeRes(ivtCall, icallerType);
			if (lCallTypeRes > 0) {
				Drawable drawCallType = mContext.getResources().getDrawable(
						lCallTypeRes);
				mylog(TAG, "RCLA : bindView lCallTypeRes:["
						+ lCallTypeRes + "]");
				vholder.calltypeImageView.setImageDrawable(drawCallType);
			} else {
				mylog(TAG,
						"RCLA : bindView -------- Error!!! callerType:["
								+ callerNumberType + "] lCallTypeRes["
								+ lCallTypeRes + "]");
			}

			if (vholder.simNameTextView != null){
				// SIMName
				if (ContactsUtils.CALL_TYPE_SIP == simId) {
				    // Sip Call log
				    int res = com.mediatek.internal.R.drawable.sim_background_sip;
				    vholder.simNameTextView.setBackgroundResource(res);
	                vholder.simNameTextView.setPadding(3, 0, 3, 0);
	                vholder.simNameTextView.setText(R.string.sipcall);
	                vholder.simNameTextView.setVisibility(View.VISIBLE);
	                Log.i(TAG, TAG + "bindView: SIP Call Indicator position:" + position + " simId:" + simId);
				} else if (ContactsUtils.CALL_TYPE_NONE == simId) {
				    // Call log with no SIM inserted
				    vholder.simNameTextView.setVisibility(View.GONE);
				} else {
				    // Call log with SIM inserted
				    SIMInfo simInfo = null;
	                if (null != mAllSIMInfoList && mAllSIMInfoList.size() > 0) {
	                    for (int j = 0; j < mAllSIMInfoList.size(); j++) {
	                        if (simId == mAllSIMInfoList.get(j).mSimId) {
	                            simInfo = mAllSIMInfoList.get(j);
	                            break;
	                        }
	                    }
	                }
	
	                if (null != simInfo) {
	                    int res = Telephony.SIMBackgroundRes[simInfo.mColor];
	                    // sim not inserted!
	                    if (simInfo.mSlot < 0) {
	                        Log.i(TAG, "SIM[" + simInfo.mNumber + "] Name[" + simInfo.mDisplayName + 
	                                "] is not inserted! simSlot[" + simInfo.mSlot + "]");
	                        res = com.mediatek.internal.R.drawable.sim_background_locked;
	                    }
	                    vholder.simNameTextView.setBackgroundResource(res);
	                    vholder.simNameTextView.setPadding(3, 0, 3, 0);
	                    vholder.simNameTextView.setText(simInfo.mDisplayName);
	                    vholder.simNameTextView.setVisibility(View.VISIBLE);
	                    Log.i(TAG, TAG + " No Error!! - Cannot find SIMInfo by SIMid. position:" + position + " simId:" + simId);
	                } else {
	                    vholder.simNameTextView.setVisibility(View.GONE);
	                    Log.e(TAG, TAG + " Error!! - Cannot find SIMInfo by SIMid. position:" + position + " simId:" + simId);
	                }
				}
				// Restore simId
				vholder.simNameTextView.setTag(simId);
			}
			// Time
			String time = formatTime(callDate);
			vholder.timeTextView.setText(time);

			vholder.detailImageView.setTag(position);
			vholder.detailImageView.setOnClickListener(this);

			// Listen for the first draw
			if (mPreDrawListener == null) {
				mFirst = true;
				mPreDrawListener = this;
				view.getViewTreeObserver().addOnPreDrawListener(this);
			}
			Log.i(TAG, "RCLA : bindView view()- ");
		}

		private ContactInfo searchContactInfo(Cursor c) {
			String number = c.getString(NUMBER_COLUMN_INDEX);
			ContactInfo info = mContactInfo.get(number);
			String formattedNumber = null;
			String callerName = c.getString(CALLER_NAME_COLUMN_INDEX);
			int callerNumberType = c.getInt(CALLER_NUMBERTYPE_COLUMN_INDEX);
			String callerNumberLabel = c
					.getString(CALLER_NUMBERLABEL_COLUMN_INDEX);
			int simId = c.getInt(CALLER_SIMID_INDEX);

//			Log.i(TAG, "searchContactInfo number:  [" + number + "]");
//			Log.i(TAG, "searchContactInfo Position:[" + c.getPosition() + "]");
//			Log.i(TAG, "searchContactInfo Name:    [" + callerName + "]");
//			Log.i(TAG, "searchContactInfo Type:    [" + callerNumberType + "]");
//			Log.i(TAG, "searchContactInfo Label:   [" + callerNumberLabel + "]");
//			Log.i(TAG, "searchContactInfo simId:   [" + simId + "]");

			if (info == null) {
				// Mark it as empty and queue up a request to find the name
				// The db request should happen on a non-UI thread
				info = ContactInfo.EMPTY;
				mContactInfo.put(number, info);
				enqueueRequest(number, c.getPosition(), callerName,
						callerNumberType, callerNumberLabel);
			} else if (info != ContactInfo.EMPTY) { // Has been queried
				// Check if any data is different from the data cached in the
				// calls db. If so, queue the request so that we can update
				// the calls db.
				if (!TextUtils.equals(info.name, callerName)
						|| info.type != callerNumberType
						|| !TextUtils.equals(info.label, callerNumberLabel)) {
					// Something is amiss, so sync up.
					enqueueRequest(number, c.getPosition(), callerName,
							callerNumberType, callerNumberLabel);
				}

				// Format and cache phone number for found contact
				if (info.formattedNumber == null) {
					info.formattedNumber = formatPhoneNumber(info.number);
				}
				formattedNumber = info.formattedNumber;
			}

			return info;
		}

		private String getSIMNameById(int simId) {
			mylog(TAG, "RCLA : getSIMNameById simId:" + simId);
			String sRet = null;
			if ((null != mSIMInfoWrapperAll) 
			    && (null != mSIMInfoWrapperAll.getSimInfoMap())
			    && (null != mSIMInfoWrapperAll.getSimInfoMap().get(simId))) {
			    sRet = mSIMInfoWrapperAll.getSimInfoMap().get(simId).mDisplayName;
			}
			
			return sRet;
//			switch (simId) {
//			case 0:
//				return "SIM1";
//			case 1:
//				return "SIM2";
//			default:
//				return "Unknown SIM";
//			}
			// return Integer.toString(simId);
		}

		private int getCallTypeRes(int vtCall, int ict) {
			int iRes = 0;
			mylog(TAG, "RCLA : getCallTypeRes ict:" + ict);

			switch (ict) {
			case Calls.INCOMING_TYPE:
			    if (vtCall == 1) {
			        iRes = R.drawable.contact_calllog_dialerseach_video_incoming_call;
			    } else {
			        iRes = R.drawable.contact_calllog_dialerseach_incoming_call;
			    }
				break;
			case Calls.OUTGOING_TYPE:
			    if (vtCall == 1) {
			        iRes = R.drawable.contact_calllog_dialerseach_video_outing_call;
                } else {
                    iRes = R.drawable.contact_calllog_dialerseach_outing_call;
                }
				
				break;
			case Calls.MISSED_TYPE:
			    if (vtCall == 1) {
			        iRes = R.drawable.contact_calllog_dialerseach_video_missing_call;
                } else {
                    iRes = R.drawable.contact_calllog_dialerseach_missing_call;
                }
				
				break;
			default:
				mylog(TAG, "RCLA : getCallTypeRes Error!!! ict:" + ict);
				break;
			}
			return iRes;
		}

		private String formatTime(long l) {
			mylog(TAG, "RCLA : formatTime date:" + l);

			String sRet = null;
			
			if (0 != l) {
//				SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
//				sRet = sdf.format(l);
				java.text.DateFormat df = DateFormat.getTimeFormat(RecentCallsListActivity.this);
				sRet = df.format(l);
			}

			mylog(TAG, "RCLA : formatTime latest datetime:" + sRet);
			return sRet;
		}

		private boolean compareDateByDay(long l1, long l2) {
			boolean bEqual = false;
			mylog(TAG, "RCLA : compareDateByDay l1[" + l1 + "] l2" + l2
					+ "]");

			SimpleDateFormat sdf = new SimpleDateFormat("YYYYmmdd");

			mylog(TAG, "RCLA : compareDateByDay sd11[" + sdf.format(l1)
					+ "] sd2" + sdf.format(l2) + "]");

			int id1 = Integer.getInteger(sdf.format(l1), 0);
			int id2 = Integer.getInteger(sdf.format(l2), 0);

			mylog(TAG, "RCLA : compareDateByDay id11[" + id1 + "] id2"
					+ id2 + "]");
			bEqual = (id1 == id2);

			return bEqual;
		}

		@Override
		public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
			// TODO Auto-generated method stub
			return super.runQueryOnBackgroundThread(constraint);
		}
	}

	private static final class QueryHandler extends AsyncQueryHandler {
		private final WeakReference<RecentCallsListActivity> mActivity;

		/**
		 * Simple handler that wraps background calls to catch
		 * {@link SQLiteException}, such as when the disk is full.
		 */
		protected class CatchingWorkerHandler extends
				AsyncQueryHandler.WorkerHandler {
			public CatchingWorkerHandler(Looper looper) {
				super(looper);
			}

			@Override
			public void handleMessage(Message msg) {
				try {
					// Perform same query while catching any exceptions
					super.handleMessage(msg);
				} catch (SQLiteDiskIOException e) {
					Log.w(TAG, "Exception on background worker thread", e);
				} catch (SQLiteFullException e) {
					Log.w(TAG, "Exception on background worker thread", e);
				} catch (SQLiteDatabaseCorruptException e) {
					Log.w(TAG, "Exception on background worker thread", e);
				}
			}
		}

		@Override
		protected Handler createHandler(Looper looper) {
			// Provide our special handler that catches exceptions
			return new CatchingWorkerHandler(looper);
		}

		public QueryHandler(Context context) {
			super(context.getContentResolver());
			mActivity = new WeakReference<RecentCallsListActivity>(
					(RecentCallsListActivity) context);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			final RecentCallsListActivity activity = mActivity.get();
			if (activity != null && !activity.isFinishing()) {
				final RecentCallsListActivity.RecentCallsAdapter callsAdapter = activity.mAdapter;
				callsAdapter.setLoading(false);
			    callsAdapter.clearCache();
				callsAdapter.changeCursor(cursor);
                if (cursor == null){   
                    Toast.makeText(activity, R.string.fail_reason_unknown, Toast.LENGTH_SHORT).show();
                    return;
                }
                activity.setSearchResultCount(cursor.getCount());

                Log.i(TAG, "onQueryCompleted - Count:" + cursor.getCount());
   
                // Now that the cursor is populated again, it's possible to restore the list state
                if (activity.mListState != null) {
                    activity.mList.onRestoreInstanceState(activity.mListState);
                    activity.mListState = null;
                }
                
//				if (activity.mScrollToTop) {
//					if (activity.mList.getFirstVisiblePosition() > 5) {
//						activity.mList.setSelection(5);
//					}
//					activity.mList.smoothScrollToPosition(0);
//					activity.mScrollToTop = false;
//				}
			} else {
				cursor.close();
			}
		}
	}

    private static final Uri CALLLOG_SEARCH_URI_BASE = 
    	Uri.parse("content://" + CallLog.AUTHORITY + "/calls/search_filter/");

	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
        IntentFilter intentFilter =
            new IntentFilter(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
        registerReceiver(mReceiver, intentFilter);   
//		mSIMInfoWrapperAll = ContactsUtils.SIMInfoWrapper.getDefault(this);

		setContentView(R.layout.recent_calls);
		Button simFilter = (Button) findViewById(R.id.btn_sim_filter);
		// simFilter.setHighFocusPriority(true);
	    typeFilter_all = (Button) findViewById(R.id.btn_type_filter_all);
	    typeFilter_outgoing = (Button) findViewById(R.id.btn_type_filter_outgoing);
	    typeFilter_incoming = (Button) findViewById(R.id.btn_type_filter_incoming);
	    typeFilter_missed = (Button) findViewById(R.id.btn_type_filter_missed);
		simFilter.setOnClickListener(this);
		typeFilter_all.setOnClickListener(this);
		typeFilter_outgoing.setOnClickListener(this);
		typeFilter_incoming.setOnClickListener(this);
		typeFilter_missed.setOnClickListener(this);

		SharedPreferences.Editor editor = PreferenceManager
		.getDefaultSharedPreferences(
				RecentCallsListActivity.this).edit();
		editor.putInt(TYPE_FILTER_PREF, FILTER_TYPE_ALL);
		changeButton(typeFilter_all);
		editor.commit();

        mEmptyView = (TextView)getListView().getEmptyView();
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
        	Uri uri = getIntent().getData();
        	Intent newIntent = new Intent(this, CallDetailActivity.class);
        	newIntent.setData(uri);
        	startActivity(newIntent);
        	finish();
        } else if (Intent.ACTION_SEARCH.equals(action)) {
        	String data = intent.getStringExtra(SearchManager.USER_QUERY);
        	ViewGroup searchResult = (LinearLayout)findViewById(R.id.calllog_search_result);
        	searchResult.setVisibility(View.VISIBLE);
        	ViewGroup searchButtonCluster = (LinearLayout)findViewById(R.id.calllog_search_button_cluster);
        	searchButtonCluster.setVisibility(View.GONE);
        	TextView searchResultFor = (TextView)findViewById(R.id.calllog_search_results_for);
        	searchResultFor.setText(Html.fromHtml(getString(R.string.search_results_for,
                    "<b>" + data + "</b>")));
        	String searching = getResources().getString(R.string.search_results_searching);
        	TextView searchResultFound = (TextView)findViewById(R.id.calllog_search_results_found);
        	searchResultFound.setText(searching);
        } else {
			simFilter.setHighFocusPriority(true);
			simFilter.setOnClickListener(this);
			typeFilter_all.setOnClickListener(this);
			typeFilter_outgoing.setOnClickListener(this);
			typeFilter_incoming.setOnClickListener(this);
			typeFilter_missed.setOnClickListener(this);
			
			editor.putInt(TYPE_FILTER_PREF, FILTER_TYPE_ALL);
			changeButton(typeFilter_all);
			editor.commit();
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(RecentCallsListActivity.this);
			int sim = prefs.getInt(SIM_FILTER_PREF, FILTER_SIM_DEFAULT);
			int type = prefs.getInt(TYPE_FILTER_PREF, FILTER_TYPE_DEFAULT);
			simFilter.setText(getChoiceText(sim));
			typeFilter_all.setText(getString(R.string.all_tab_label));
        }
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(RecentCallsListActivity.this);
		int sim = prefs.getInt(SIM_FILTER_PREF, FILTER_ALL_RESOURCES);
		int type = prefs.getInt(TYPE_FILTER_PREF, FILTER_TYPE_DEFAULT);

		simFilter.setText(getChoiceText(sim));
		typeFilter_all.setText(getString(R.string.all_tab_label));
		// Typing here goes to the dialer
		setDefaultKeyMode(DEFAULT_KEYS_DIALER);

		ListView listview = getListView();
		listview.setOnCreateContextMenuListener(this);

		listview.setItemsCanFocus(true);
		listview.setFocusable(true);
		listview.setFocusableInTouchMode(true);
		listview.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		
		listview.setHeaderDividersEnabled(true);

		mAdapter = new RecentCallsAdapter(this);
		setListAdapter(mAdapter);

		// getListView().setBackgroundColor(0xffffffff);

		mylog(TAG, "MTK_GEMINI_SUPPORT" + FeatureOption.MTK_GEMINI_SUPPORT);
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			mVoiceMailNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
					.getVoiceMailNumberGemini(Phone.GEMINI_SIM_1);
			mVoiceMailNumber2 = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
					.getVoiceMailNumberGemini(Phone.GEMINI_SIM_2);
		} else {
			mVoiceMailNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
					.getVoiceMailNumber();
			simFilter.setVisibility(View.GONE);
			View simFilterIcon = (View) findViewById(R.id.btn_sim_filter_icon);
			simFilterIcon.setVisibility(View.GONE);
		}

		mQueryHandler = new QueryHandler(this);

		// Reset locale-based formatting cache
		sFormattingType = FORMATTING_TYPE_INVALID;
		
		mContentObserver = new CallLogChangeObserver();
        this.getContentResolver().registerContentObserver(
                Uri.parse("content://call_log/calls/"), 
                true, mContentObserver);
		
		mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture);
		mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
		mCellConnMgr = new CellConnMgr();
		mCellConnMgr.register(getApplicationContext());
		if (istrace) {
		    Debug.startMethodTracing("calllog");
		}
        getListView().setCacheColorHint(Color.WHITE);
	}

    public void setSearchResultCount(int count) {
    	String text = getQuantityText(count, R.string.listFoundAllCalllogZero,
                R.plurals.searchFoundCalllogs);
    	TextView searchResultFound = (TextView)findViewById(R.id.calllog_search_results_found);
    	searchResultFound.setText(text);
    }
    
    private String getQuantityText(int count, int zeroResourceId, int pluralResourceId) {
        if (count == 0) {
            return getString(zeroResourceId);
        } else {
            String format = getResources().getQuantityText(pluralResourceId, count).toString();
            return String.format(format, count);
        }
    }

	private String getChoiceText(int type) {
		final Resources res = getResources();
		
		getValidSIMInfo(false);
		
//		int iInsertedSimCount = -2;
//		if (null != mInsertedSIMInfoList) {
//			iInsertedSimCount = mInsertedSIMInfoList.size();
//		}
		
		switch (type) {
//		case FILTER_SIM_1:
//			if (iInsertedSimCount > 0) {
//				return mInsertedSIMInfoList.get(0).mDisplayName;
//			} else {
//				Log.e(TAG, "--- Error!! --- InsertedSimCount[" 
//						+ mInsertedSIMInfoList.size() + "] Want SIMid[0]");
//				return null;
//			}
////			return res.getString(R.string.sim1);
//		case FILTER_SIM_2:
//			if (iInsertedSimCount > 1) {
//				return mInsertedSIMInfoList.get(1).mDisplayName;
//			} else {
//				Log.e(TAG, "--- Error!! --- InsertedSimCount[" 
//						+ mInsertedSIMInfoList.size() + "] Want SIMid[1]");
//				return null;
//			}
//			return res.getString(R.string.sim2);
		case FILTER_SIM_ALL:
			//return res.getString(R.string.both_sims);
			return null;
		case FILTER_ALL_RESOURCES:
			//return res.getString(R.string.all_resources);
			return null;
        case FILTER_SIP_CALL:
            //return res.getString(R.string.sipcall); 
            return null;
		case FILTER_TYPE_ALL:
			//return res.getString(R.string.all_calls);
			return null;
		case FILTER_TYPE_INCOMING:
			//return res.getString(R.string.received_calls);
			return null;
		case FILTER_TYPE_OUTGOING:
			//return res.getString(R.string.outgoing_calls);
			return null;
		case FILTER_TYPE_MISSED:
			//return res.getString(R.string.missed_calls);
			return null;
		default:
            // handle SIM Id read from preference.
            /*
            if ((null != mAllSimInfoMap) && (type > 0)) {
                SIMInfo ci = mAllSimInfoMap.get(type);
                if (null != ci) {
                    return ci.mDisplayName;
                } else {
                    Log.e(TAG, " Error!! - getChoiceText() no SIMinfo of id:" + type);
                }
            } else {
                Log.e(TAG, " Error!! - getChoiceText() Error id:" + type);
            }
            */
            return null;
        }
    }

    @Override
    protected void onStart() {
        mScrollToTop = true;
        Log.i(TAG, TAG + " onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        // The adapter caches looked up numbers, clear it so they will get
        // looked up again.
        Log.i(TAG, TAG + " onResume");
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            setSimIndicatorVisibility(true);
            mShowSimIndicator = true;
        }

        getValidSIMInfo(false);
        if (mAdapter != null) {
            mAdapter.clearCache();
        }

        startQuery();
        resetNewCallsFlag();
        mPhotoLoader.update();
        mPhotoLoader.resume();

        super.onResume();

        mAdapter.mPreDrawListener = null; // Let it restart the thread after
        // next draw
        //Add by mtk80908. Get or update voice mail number
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			mVoiceMailNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
					.getVoiceMailNumberGemini(Phone.GEMINI_SIM_1);
			mVoiceMailNumber2 = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
					.getVoiceMailNumberGemini(Phone.GEMINI_SIM_2);
		} else {
			mVoiceMailNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
					.getVoiceMailNumber();
		}
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        // TODO Auto-generated method stub
        super.onRestoreInstanceState(state);
        // Retrieve list state. This will be applied after the QueryHandler has run
        mListState = state.getParcelable(LIST_STATE_KEY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        // Save list state in the bundle so we can restore it after the QueryHandler has run
        if (mList != null) {
            outState.putParcelable(LIST_STATE_KEY, mList.onSaveInstanceState());
        }
    }

	private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent) {
	    	   String action = intent.getAction();
	          if (action.equals(TelephonyIntents.ACTION_SIM_INFO_UPDATE)){
	        	  Log.i(TAG, TAG + "[Reset][Start query]");
	              if (FeatureOption.MTK_GEMINI_SUPPORT) {
	                  setSimIndicatorVisibility(true);
	                  mShowSimIndicator = true;
	              }
	        	  getValidSIMInfo(true);
	        	  startQuery();
	           }
	    	}	    	
    };


	@Override
	protected void onPause() {
		super.onPause();
		if (istrace) {
		    Debug.stopMethodTracing();
		}
		Log.i(TAG, TAG + " onPause");
		closeContextMenu();
		if (mSimFilterAlertDialog != null && mSimFilterAlertDialog.isShowing()) {
			mSimFilterAlertDialog.dismiss();
			mSimFilterAlertDialog = null;
		}
		if (mDelAllAlertDialog != null && mDelAllAlertDialog.isShowing()) {
			mDelAllAlertDialog.dismiss();
			mDelAllAlertDialog = null;
		}
		if (mAddOrEditContactDialog != null && mAddOrEditContactDialog.isShowing()) {
			mAddOrEditContactDialog.dismiss();
			mAddOrEditContactDialog = null;
		}
//		mAdapter.changeCursor(null);
		ContactsUtils.dispatchActivityOnPause();
		// Kill the requests thread
		mAdapter.stopRequestProcessing();
        /*if (FeatureOption.MTK_GEMINI_SUPPORT) {
		    setSimIndicatorVisibility(false);
            mShowSimIndicator = false;
	    }*/
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, TAG + " onDestroy");
		mCellConnMgr.unregister();
		mPhotoLoader.clear();
		mPhotoLoader.stop();
		mAdapter.stopRequestProcessing();
//		mAdapter.changeCursor(null);
		mSIMInfoWrapperAll.release();
		mSIMInfoWrapperAll = null;
		getContentResolver().unregisterContentObserver(mContentObserver);

        unregisterReceiver(mReceiver);    
	}

    @Override
    public void onBackPressed() {
    	Log.e(TAG, "------------------------------------begin onBackPressed(): getParent():" + getParent()); 
    	if (null != getParent()) {
            getParent().moveTaskToBack(false);
    	} else {
            finish();
    	}
        Log.e(TAG, "------------------------------------end onBackPressed()"); 
    }
    
	public void onClick(View view) {
		Log.i(TAG, TAG + " RCLA : onClick");
		int id = view.getId();
		
		SharedPreferences.Editor editor = PreferenceManager
		.getDefaultSharedPreferences(
				RecentCallsListActivity.this).edit();
		
		switch (id) {
		case R.id.btn_sim_filter:
			showChoiceResourceDialog();
			break;
		case R.id.btn_type_filter_all:
			Log.d("wgao","R.id.btn_type_filter_all");
			editor.putInt(TYPE_FILTER_PREF, FILTER_TYPE_ALL);
			changeButton(view);
			break;
		case R.id.btn_type_filter_outgoing:
			Log.d("wgao","R.id.btn_type_filter_outgoing");					
			editor.putInt(TYPE_FILTER_PREF, FILTER_TYPE_OUTGOING);
			changeButton(view);
			break;
		case R.id.btn_type_filter_incoming:
			Log.d("wgao","R.id.btn_type_filter_incoming");					
			editor.putInt(TYPE_FILTER_PREF, FILTER_TYPE_INCOMING);
			changeButton(view);
			break;
		case R.id.btn_type_filter_missed:
			Log.d("wgao","R.id.btn_type_filter_missed");					
			editor.putInt(TYPE_FILTER_PREF, FILTER_TYPE_MISSED);
			changeButton(view);
			break;
		default:
			break;
		}
		editor.commit();
		startQuery();
	}
	private void changeButton(View view){
		if (view != typeFilter_all ){
			typeFilter_all.setBackgroundResource(R.drawable.btn_calllog_all);
		}else{
			typeFilter_all.setBackgroundResource(R.drawable.btn_calllog_all_sel);
		}
		
		if (view != typeFilter_outgoing ){
			typeFilter_outgoing.setBackgroundResource(R.drawable.btn_calllog_incoming);
		}else{
			typeFilter_outgoing.setBackgroundResource(R.drawable.btn_calllog_incoming_sel);
		}
		
		if (view != typeFilter_incoming ){
			typeFilter_incoming.setBackgroundResource(R.drawable.btn_calllog_incoming);
		}else{
			typeFilter_incoming.setBackgroundResource(R.drawable.btn_calllog_incoming_sel);
		}
		
		if (view != typeFilter_missed ){
			typeFilter_missed.setBackgroundResource(R.drawable.btn_calllog_missed);
		}else{
			typeFilter_missed.setBackgroundResource(R.drawable.btn_calllog_missed_sel);
		}
		
	}

	private void showChoiceResourceDialog() {
		final Resources res = getResources();
		final String title = res.getString(R.string.choose_resources_header);
		final String allResourceStr = res.getString(R.string.all_resources);
		
		final List<ItemHolder> items = ContactsUtils.createItemHolder(this, allResourceStr, true, null);
		
		final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(RecentCallsListActivity.this);
				int oriSim = prefs.getInt(SIM_FILTER_PREF, -1);
				SharedPreferences.Editor editor = PreferenceManager
						.getDefaultSharedPreferences(
								RecentCallsListActivity.this).edit();

				AlertDialog alertDialog = (AlertDialog) dialog;
				Object obj = alertDialog.getListView().getAdapter().getItem(which);
				
				Log.i(TAG, "showChoiceResourceDialog OnClick oriSim:" + oriSim + " return:" + obj);
				
				int resId = 0;
				if (obj instanceof String) {
					resId = R.string.all_resources;
				} else if (obj instanceof Integer) {
					if ((Integer)obj == Integer.valueOf((int)Settings.System.VOICE_CALL_SIM_SETTING_INTERNET)) {
						resId = R.string.sipcall;
					} else if ((Integer)obj == 0) { // Slot 0;
						resId = R.string.sim1;
					} else if ((Integer)obj == 1) {
						resId = R.string.sim2;
					} else {
						Log.e(TAG, "OnClick Error! return:" + (Integer)obj);
					}
				}
				
				Button btn = (Button) RecentCallsListActivity.this
						.findViewById(R.id.btn_sim_filter);
				
				switch (resId) {
				case R.string.sim1:
					//btn.setText(sim1Name);
					break;
				case R.string.sim2:
					//btn.setText(sim2Name);
					break;
				default:
					//btn.setText(resId);
					break;
				}
				
				long newsimid = (long)Settings.System.DEFAULT_SIM_NOT_SET;
				switch (resId) {
				case R.string.all_resources:
					if (oriSim == FILTER_ALL_RESOURCES) {
						Log.d(TAG, "The current sim " + FILTER_ALL_RESOURCES);
						return;
					}
					editor.putInt(SIM_FILTER_PREF, FILTER_ALL_RESOURCES);
					newsimid = FILTER_ALL_RESOURCES;
					break;

				case R.string.sim1:
					if (oriSim == sim1ID) {
						Log.d(TAG, "The current sim " + sim1ID);
						return;
					}
					editor.putInt(SIM_FILTER_PREF, (int)sim1ID);
					newsimid = sim1ID;
					break;
				case R.string.sim2:
					if (oriSim == sim2ID) {
						Log.d(TAG, "The current sim " + sim2ID);
						return;
					}
					editor.putInt(SIM_FILTER_PREF, (int)sim2ID);
					newsimid = sim2ID;
					break;
				case R.string.sipcall:
                     if (oriSim == FILTER_SIP_CALL) {
                         Log.d(TAG, "The current sim " + FILTER_SIP_CALL);
                         return;
                     }
                     editor.putInt(SIM_FILTER_PREF, FILTER_SIP_CALL);
                     newsimid = FILTER_SIP_CALL;
                     break;

				default: {
					Log.e(TAG, "Unexpected resource: "
							+ getResources().getResourceEntryName(resId));
				}
				}
				Log.e(TAG, "showChoiceResourceDialog OnClick user selected:" + newsimid);
				editor.commit();
				startQuery();
			}
		};
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
		mSelectResDialog = ContactsUtils.createSimSelectionDialog(this, title, -5, items, true, preference.getInt(SIM_FILTER_PREF, FILTER_ALL_RESOURCES), clickListener);
		mSelectResDialog.show();
	}
	
	private void showChoiceDialog(final int id) {
		final Context dialogContext = new ContextThemeWrapper(this,
				android.R.style.Theme_Light);
		final Resources res = dialogContext.getResources();
		final LayoutInflater dialogInflater = (LayoutInflater) dialogContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		getValidSIMInfo(false);
		// Adapter that shows a list of string resources
		final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
				android.R.layout.simple_list_item_1) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
					convertView = dialogInflater.inflate(
							android.R.layout.simple_list_item_1, parent, false);
				}
				
				final int resId = this.getItem(position);
				Log.i(TAG, "showChoiceDialog() resId:" + resId + " sim1Name:"
				        + sim1Name + " sim2Name" + sim2Name);

				switch (resId) {
				case R.string.sim1:
					//((TextView) convertView).setText(sim1Name);
					break;
				case R.string.sim2:
					//((TextView) convertView).setText(sim2Name);
					break;
				default:
					//((TextView) convertView).setText(resId);
					break;
				}
				
				return convertView;
			}
		};

		switch (id) {
		case R.id.btn_sim_filter:
//			adapter.add(R.string.both_sims);
//			adapter.add(R.string.sim1);
//			adapter.add(R.string.sim2);
			
			adapter.add(R.string.all_resources);
			int iInsertedCount = -1;
			
			mInsertedSIMInfoList = mSIMInfoWrapperAll.getInsertedSimInfoList();
			if (null != mInsertedSIMInfoList) {
				iInsertedCount = mInsertedSIMInfoList.size();
			}
			
			Log.i(TAG, TAG + "showChoiceDialog: Inserted SIM Count:" + iInsertedCount);
			int simSlot = -1;
			
			for (int i = 0; i < iInsertedCount; i++) {
				simSlot = mSIMInfoWrapperAll.getInsertedSimInfoList().get(i).mSlot;
				switch (simSlot) {
				case 0:
					//adapter.add(R.string.sim1);
					break;
				case 1:
					//adapter.add(R.string.sim2);
					break;
				default:
					Log.e(TAG, TAG + " Error!! - showChoiceDialog: Inserted SIM Slot Error! slot:" + simSlot);
					break;
				}
			}
			
			adapter.add(R.string.sipcall);
			break;
			
		case R.id.btn_type_filter_all:
			adapter.add(R.string.all_calls);
			adapter.add(R.string.received_calls);
			adapter.add(R.string.outgoing_calls);
			adapter.add(R.string.missed_calls);
			break;
		}

		final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(RecentCallsListActivity.this);
				int oriSim = prefs.getInt(SIM_FILTER_PREF, -1);
				int oriType = prefs.getInt(TYPE_FILTER_PREF, -1);
				SharedPreferences.Editor editor = PreferenceManager
						.getDefaultSharedPreferences(
								RecentCallsListActivity.this).edit();

				final int resId = adapter.getItem(which);
				Button btn = (Button) RecentCallsListActivity.this
						.findViewById(id);
				
				switch (resId) {
				case R.string.sim1:
					//btn.setText(sim1Name);
					break;
				case R.string.sim2:
					//btn.setText(sim2Name);
					break;
				default:
					//btn.setText(resId);
					break;
				}
//				btn.setText(res.getString(resId));
				
				switch (resId) {
				case R.string.all_resources:
					if (oriSim == FILTER_ALL_RESOURCES) {
						Log.d(TAG, "The current sim " + FILTER_ALL_RESOURCES);
						return;
					}
					editor.putInt(SIM_FILTER_PREF, FILTER_ALL_RESOURCES);
					break;
				case R.string.both_sims:
					if (oriSim == FILTER_SIM_ALL) {
						Log.d(TAG, "The current sim " + FILTER_SIM_ALL);
						return;
					}
					editor.putInt(SIM_FILTER_PREF, FILTER_SIM_ALL);
					break;
				case R.string.sim1:
					if (oriSim == sim1ID) {
						Log.d(TAG, "The current sim " + sim1ID);
						return;
					}
					editor.putInt(SIM_FILTER_PREF, (int)sim1ID);
					break;
				case R.string.sim2:
					if (oriSim == sim2ID) {
						Log.d(TAG, "The current sim " + sim2ID);
						return;
					}
					editor.putInt(SIM_FILTER_PREF, (int)sim2ID);
					break;
				case R.string.all_calls:
					if (oriType == FILTER_TYPE_ALL) {
						Log.d(TAG, "The current type " + FILTER_TYPE_ALL);
						return;
					}
					editor.putInt(TYPE_FILTER_PREF, FILTER_TYPE_ALL);
					break;
				case R.string.sipcall:
                     if (oriSim == FILTER_SIP_CALL) {
                         Log.d(TAG, "The current sim " + FILTER_SIP_CALL);
                         return;
                     }
                     editor.putInt(SIM_FILTER_PREF, FILTER_SIP_CALL);
                     break;
				case R.string.received_calls:
					if (oriType == FILTER_TYPE_INCOMING) {
						Log.d(TAG, "The current type " + FILTER_TYPE_INCOMING);
						return;
					}
					editor.putInt(TYPE_FILTER_PREF, FILTER_TYPE_INCOMING);
					break;
				case R.string.outgoing_calls:
					if (oriType == FILTER_TYPE_OUTGOING) {
						Log.d(TAG, "The current type " + FILTER_TYPE_OUTGOING);
						return;
					}
					editor.putInt(TYPE_FILTER_PREF, FILTER_TYPE_OUTGOING);
					break;
				case R.string.missed_calls:
					if (oriType == FILTER_TYPE_MISSED) {
						Log.d(TAG, "The current type " + FILTER_TYPE_MISSED);
						return; 
					}
					editor.putInt(TYPE_FILTER_PREF, FILTER_TYPE_MISSED);
					break;
				default: {
					Log.e(TAG, "Unexpected resource: "
							+ getResources().getResourceEntryName(resId));
				}
				}
				editor.commit();
				startQuery();
			}
		};

		if ((mSimFilterAlertDialog == null)
				|| ((mSimFilterAlertDialog != null) && !(mSimFilterAlertDialog
						.isShowing()))) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(id == R.id.btn_sim_filter ? res
					.getString(R.string.choose_resources_header) : res
					.getString(R.string.choose_call_type_header));
			builder.setIcon(android.R.drawable.ic_menu_more);
			builder.setSingleChoiceItems(adapter, -1, clickListener);
			builder.setCancelable(true);
			AlertDialog dialog = builder.create();
			mSimFilterAlertDialog = dialog;
			dialog.show();
		} else {
			mSimFilterAlertDialog.show();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		// Clear notifications only when window gains focus. This activity won't
		// immediately receive focus if the keyguard screen is above it.
		if (hasFocus) {
			try {
				ITelephony iTelephony = ITelephony.Stub
						.asInterface(ServiceManager.getService("phone"));
				if (iTelephony != null) {
					iTelephony.cancelMissedCallsNotification();
				} else {
					Log.w(TAG, "Telephony service is null, can't call "
							+ "cancelMissedCallsNotification");
				}
			} catch (RemoteException e) {
				Log
						.e(TAG,
								"Failed to clear missed calls notification due to remote exception");
			}
		}
	}

	/**
	 * Format the given phone number using
	 * {@link PhoneNumberUtils#formatNumber(android.text.Editable, int)}. This
	 * helper method uses {@link #sEditable} and {@link #sFormattingType} to
	 * prevent allocations between multiple calls.
	 * <p>
	 * Because of the shared {@link #sEditable} builder, <b>this method is not
	 * thread safe</b>, and should only be called from the GUI thread.
	 * <p>
	 * If the given String object is null or empty, return an empty String.
	 */
	private String formatPhoneNumber(String number) {
		if (TextUtils.isEmpty(number)) {
			return "";
		}

		// If "number" is really a SIP address, don't try to do any formatting
		// at all.
		if (PhoneNumberUtils.isUriNumber(number)) {
			return number;
		}

		// Cache formatting type if not already present
		if (sFormattingType == FORMATTING_TYPE_INVALID) {
			sFormattingType = PhoneNumberUtils.getFormatTypeForLocale(Locale
					.getDefault());
		}

		sEditable.clear();
		sEditable.append(number);

		PhoneNumberUtils.formatNumber(sEditable, sFormattingType);
		return sEditable.toString();
	}

	private void resetNewCallsFlag() {
		// Mark all "new" missed calls as not new anymore
		StringBuilder where = new StringBuilder("type=");
		where.append(Calls.MISSED_TYPE);
		where.append(" AND new=1");

		ContentValues values = new ContentValues(1);
		values.put(Calls.NEW, "0");
		mQueryHandler.startUpdate(UPDATE_TOKEN, null, Calls.CONTENT_URI,
				values, where.toString(), null);
	}

	private void startQuery() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		int simFilter = prefs.getInt(SIM_FILTER_PREF, FILTER_SIM_DEFAULT);
		int typeFilter = prefs.getInt(TYPE_FILTER_PREF, FILTER_TYPE_DEFAULT);
		
		String selection = getSelection(simFilter, typeFilter);
		if (DEBUG) {
			Log.d(TAG, "simFilter " + simFilter);
			Log.d(TAG, "typeFilter " + typeFilter);
			Log.d(TAG, "selection " + selection);
		}
		mAdapter.setLoading(true);

		// Cancel any pending queries
		mQueryHandler.cancelOperation(QUERY_TOKEN);
        Intent intent = getIntent();
    	if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
    		mEmptyView.setText(R.string.noMatchingCalllogs);
    		String data = intent.getStringExtra(SearchManager.USER_QUERY);
    		Uri uri = Uri.withAppendedPath(CALLLOG_SEARCH_URI_BASE, data);
    		mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                CALL_LOG_PROJECTION2, null, null, Calls.DEFAULT_SORT_ORDER);
    		return;
    	}

		mylog(TAG, "RecentCalls:startQuery =============== ");
		// mQueryHandler.startQuery(QUERY_TOKEN, null, Calls.CONTENT_URI,
		// CALL_LOG_PROJECTION, selection, null, Calls.DEFAULT_SORT_ORDER);
		mylog(TAG, "RCLA : startQuery:");
//		Exception e = new Exception("Print Backtrace");
//		e.printStackTrace();

		Log.i(TAG, "RCLA : startQuery: simFilter:" + simFilter
				+ " typeFilter:" + typeFilter);
		Log.i(TAG, "RCLA : startQuery: selection:" + selection);

		// Uri uri = Calls.CONTENT_DATEGROUP_URI;
		// Uri uri = Uri.parse("content://call_log/calls/dategroup");
		// uri =
		// uri.buildUpon().appendQueryParameter("CALL_LOG_DATE_GROUP_EXTRA",
		// "true").build();
		//
		// mylog(TAG, "RecentCalls:startQuery Uri " + uri.getQuery());
		//
		// mQueryHandler.startQuery(QUERY_TOKEN, null, uri, CALL_LOG_PROJECTION,
		// selection, null, Calls.DEFAULT_SORT_ORDER);

        mEmptyView.setText(R.string.recentCalls_empty);
		Uri uri = Uri.parse("content://call_log/calls/dategroup");
		mQueryHandler.startQuery(QUERY_TOKEN, null, uri, CALL_LOG_PROJECTION,
				selection, null, Calls.DEFAULT_SORT_ORDER);
	}

	private String getSelection(int sim, int type) {
		StringBuilder builder = new StringBuilder();
		
		// FILTER_ALL_RESOURCES - null: no condition
		if (FILTER_SIP_CALL == sim) {
		    builder.append(CallLog.Calls.SIM_ID + "=" + ContactsUtils.CALL_TYPE_SIP);
		} else if (sim < FILTER_BASE) {
		    builder.append(CallLog.Calls.SIM_ID + "=" + sim);
		}

		if (type != FILTER_TYPE_ALL) {
			int t;
			switch (type) {
			case FILTER_TYPE_INCOMING:
				t = Calls.INCOMING_TYPE;
				break;
			case FILTER_TYPE_MISSED:
				t = Calls.MISSED_TYPE;
				break;
			case FILTER_TYPE_OUTGOING:
				t = Calls.OUTGOING_TYPE;
				break;
			default:
				t = FILTER_TYPE_INCOMING;
				break;
			}

			if (builder.length() > 0) {
				builder.append(" and ");
			}

			builder.append(Calls.TYPE + "=" + t);
		}
		if (DEBUG) {
			Log.d(TAG, "query selection " + builder);
		}
		if (builder.length() > 0) {
			return builder.toString();
		} else {
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ITEM_DELETE_ALL, 0, R.string.recentCalls_deleteAll)
				.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
	
    	// mtk80909 for Speed Dial
        menu.add(0, MENU_ITEM_SPEED_DIAL, 0, R.string.menu_speed_dial)
        		.setIcon(R.drawable.contact_icon_speed_dial);
        
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfoIn) {
        AdapterView.AdapterContextMenuInfo menuInfo;
        boolean isVTIdle = true;
        if (true == FeatureOption.MTK_VT3G324M_SUPPORT) { // for VT IT, last
            // code
            try {
                ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                        .getService(Context.TELEPHONY_SERVICE));
                if (null != iTel) {
                    isVTIdle = iTel.isVTIdle();
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
		Log.d(TAG, "VT call, isVTIdle=" + isVTIdle);
		try {
			menuInfo = (AdapterView.AdapterContextMenuInfo) menuInfoIn;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfoIn", e);
			return;
		}

		Cursor cursor = (Cursor) mAdapter.getItem(menuInfo.position);
        int simId = cursor.getInt(CALLER_SIMID_INDEX);
		String number = cursor.getString(NUMBER_COLUMN_INDEX);
		isEmergencyNumber = PhoneNumberUtils.isEmergencyNumber(number);
		Uri numberUri = null;
		boolean isVoicemail = false;
		boolean isSipNumber = false;
		if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
			number = getString(R.string.unknown);
		} else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
			number = getString(R.string.private_num);
		} else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
			number = getString(R.string.payphone);
		} else if ( (number.equals(mVoiceMailNumber)  && (simId == sim1ID)) || (number.equals(mVoiceMailNumber2)&& (simId == sim2ID))) {
			number = getString(R.string.voicemail);
			numberUri = Uri.parse("voicemail:x");
			isVoicemail = true;
		} else if (PhoneNumberUtils.isUriNumber(number)) {
			numberUri = Uri.fromParts("sip", number, null);
			isSipNumber = true;
		} else {
			numberUri = Uri.fromParts("tel", number, null);
		}

        ContactInfo info = mAdapter.getContactInfo(number);
        String numberFormatted = PhoneNumberFormatUtilEx.formatNumber(number);
        boolean contactInfoPresent = (info != null && info != ContactInfo.EMPTY);
        if (contactInfoPresent) {
            menu.setHeaderTitle(info.name);
        } else {
            menu.setHeaderTitle(numberFormatted);
        }

        getValidSIMInfo(false);

		if (numberUri != null) {
            if (!isSipNumber) {
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    boolean bVTEnabled = true;
                    try {
                        ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                                .getService(Context.TELEPHONY_SERVICE));
                        if (null != iTel) {
                            sim1Radio = iTel.isRadioOnGemini(Phone.GEMINI_SIM_1);
                            sim2Radio = iTel.isRadioOnGemini(Phone.GEMINI_SIM_2);
                            sim1Idle = iTel.isIdleGemini(Phone.GEMINI_SIM_1);
                            sim2Idle = iTel.isIdleGemini(Phone.GEMINI_SIM_2);
                        }

                        sim1Ready = (TelephonyManager.SIM_STATE_READY == TelephonyManager
                                .getDefault().getSimStateGemini(Phone.GEMINI_SIM_1));
                        sim2Ready = (TelephonyManager.SIM_STATE_READY == TelephonyManager
                                .getDefault().getSimStateGemini(Phone.GEMINI_SIM_2));
                        if (DEBUG) {
                            Log.d(TAG, "sim1 raido on " + sim1Radio);
                            Log.d(TAG, "sim2 Radio on " + sim2Radio);
                            Log.d(TAG, "sim1 sim1Idle on " + sim1Idle);
                            Log.d(TAG, "sim2 sim2Idle on " + sim2Idle);
                            Log.d(TAG, "sim1 sim1Ready on " + sim1Ready);
                            Log.d(TAG, "sim2 sim2Ready on " + sim2Ready);
                        }
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }

                    if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
                        bVTEnabled = ((sim1Radio && sim1Ready) || (sim2Radio && sim2Ready))
                                && sim1Idle && sim2Idle;
                    } else {
                        bVTEnabled = sim1Radio && sim1Ready && sim1Idle && sim2Idle;
                    }
                    if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
                        // For Gemini Enhancement
                        if (insertedSimCount > 0) {
                            // Dual SIM Inserted - call number via...
                            String menuTxt = null;
                            if (PhoneNumberUtils.isEmergencyNumber(number)) {
                                menuTxt = getResources().getString(R.string.recentCalls_callNumber,
                                        number);
                            } else {
                                menuTxt = getResources().getString(R.string.call_detail_call_via,
                                        numberFormatted);
                            }
                            menu.add(0, CONTEXT_MENU_CALL_SIM_VIA, 0, menuTxt);
                        }

                        // for VT
                        menu.add(0, CONTEXT_MENU_VT_CALL_SIM_VIA, 0, getResources().getString(
                                R.string.recentCalls_vt_callNumber, numberFormatted)).setEnabled(sim1Idle && sim2Idle);
                    } else {
                        // For Gemini Enhancement
                        if (insertedSimCount > 0) {
                            // Dual SIM Inserted - call number via...
                            String menuTxt = null;
                            if (PhoneNumberUtils.isEmergencyNumber(number)) {
                                menuTxt = getResources().getString(R.string.recentCalls_callNumber,
                                        number);
                            } else {
                                menuTxt = getResources().getString(R.string.call_detail_call_via,
                                        numberFormatted);
                            }

                            menu.add(0, CONTEXT_MENU_CALL_SIM_VIA, 0, menuTxt);
                        }
                    }

                } else {
                    boolean simRadio = true;
                    boolean simIdle = true;
                    try {
                        ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                                .getService(Context.TELEPHONY_SERVICE));
                        if (null != iTel) {
                            simRadio = iTel.isRadioOn();
                            simIdle = iTel.isIdle();
                        }
                        if (DEBUG) {
                            Log.d(TAG, "sim raido on " + simRadio);
                            Log.d(TAG, "simIdle is  " + simIdle);
                        }
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                    Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, numberUri);
                    if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
                        menu.add(
                                0,
                                0,
                                0,
                                getResources().getString(R.string.recentCalls_callNumber,
                                        numberFormatted)).setIntent(intent).setEnabled(
                                simRadio && isVTIdle);

                        Intent intentVt = new Intent(Intent.ACTION_CALL_PRIVILEGED, numberUri);
                        menu.add(
                                0,
                                0,
                                0,
                                getResources().getString(R.string.recentCalls_vt_callNumber,
                                        numberFormatted)).setIntent(
                                intentVt.putExtra("is_vt_call", true)).setEnabled(
                                simRadio && simIdle); // intent.putExtra("is_vt_call",
                        // true)
                    } else {
                        menu.add(
                                0,
                                CONTEXT_MENU_CALL_CONTACT,
                                0,
                                getResources().getString(R.string.recentCalls_callNumber,
                                        numberFormatted)).setIntent(intent);
                    }
                }
            } else { // SIP call
                // Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                // numberUri);
                // menu.add(0, CONTEXT_MENU_CALL_CONTACT, 0,
                // getResources().getString(R.string.recentCalls_callNumber,
                // number))
                // .setIntent(intent);
                menu.add(0, CONTEXT_MENU_CALL_SIM_VIA, 0, getResources().getString(
                        R.string.recentCalls_callNumber, number));
            }
        }

        if (contactInfoPresent) {
            Intent intent = new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(
                    Contacts.CONTENT_URI, info.personId));
            StickyTabs.setTab(intent, getIntent());
            menu.add(0, 0, 0, R.string.menu_viewContact).setIntent(intent);
            // menu.add(0, CONTEXT_MENU_NEW_OR_EDIT_CONTACT, 0,
            // R.string.recentCalls_addToContact);
        }

        if (numberUri != null && !isVoicemail && !isSipNumber) {
            menu.add(0, 0, 0, R.string.menu_sendTextMessage).setIntent(
                    new Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", numberFormatted, null)));
            menu.add(0, 0, 0, R.string.recentCalls_editNumberBeforeCall).setIntent(
                    new Intent(Intent.ACTION_DIAL, numberUri));
        }

		// "Add to contacts" item, if this entry isn't already associated with a
		// contact
		if (!contactInfoPresent && numberUri != null && !isVoicemail) {
			// TODO: This item is currently disabled for SIP addresses, because
			// the Insert.PHONE extra only works correctly for PSTN numbers.
			//
			// To fix this for SIP addresses, we need to:
			// - define ContactsContract.Intents.Insert.SIP_ADDRESS, and use it
			// here if
			// the current number is a SIP address
			// - update the contacts UI code to handle Insert.SIP_ADDRESS by
			// updating the SipAddress field
			// and then we can remove the "!isSipNumber" check above.

//			Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
//			intent.setType(Contacts.CONTENT_ITEM_TYPE);
//			intent.putExtra(Insert.PHONE, number);
//			menu.add(0, 0, 0, R.string.recentCalls_addToContact).setIntent(
//					intent);
		    menu.add(0, CONTEXT_MENU_NEW_OR_EDIT_CONTACT, 0, R.string.recentCalls_addToContact);
		}
		
		// Speed Dial
		if (contactInfoPresent && (!isSipNumber)) {
		    menu.add(0, CONTEXT_MENU_SPEED_DIAL, 0, R.string.speed_dial_view);
		}
		
		menu.add(0, CONTEXT_MENU_ITEM_DELETE, 0,
				R.string.recentCalls_removeFromRecentList);
	}
	
	private void addOReditContactPromtion(final String number) {
        if(!TextUtils.isEmpty(number)) {
            String message = getResources().getString(R.string.add_contact_dialog_message, number);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                                         .setTitle(number)
                                                         .setMessage(message);
            
            AlertDialog dialog = builder.create();
            
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.add_contact_dialog_existing), new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    intent.setType(Contacts.CONTENT_ITEM_TYPE);
                    intent.putExtra(Insert.PHONE, number);
                    startActivity(intent);
                }
            });
            
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.add_contact_dialog_new), new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                    intent.putExtra(Insert.PHONE, number);
                    startActivity(intent);
                }
                
            });
            mAddOrEditContactDialog = dialog;
            dialog.show();
        }
        startQuery();
	}

	protected boolean getGeminiEnabled(int simId) {
        boolean isVTIdle = true;
        try {
            ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                    .getService(Context.TELEPHONY_SERVICE));
            if (null != iTel) {
                isVTIdle = iTel.isVTIdle();
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        if (simId == Phone.GEMINI_SIM_1) {
            if (isEmergencyNumber) {
                shouldEnable = isEmergencyNumber && ((sim1Idle && sim2Idle) ? true : sim2Idle);
            } else {
                shouldEnable = isVTIdle && sim1Radio && sim1Ready
                        && ((sim1Idle && sim2Idle) ? true : sim2Idle);
            }
            return shouldEnable;
        } else {
            if (isEmergencyNumber) {
                shouldEnable = isEmergencyNumber && ((sim1Idle && sim2Idle) ? true : sim1Idle);
            } else {
                shouldEnable = isVTIdle && sim2Radio && sim2Ready
                        && ((sim1Idle && sim2Idle) ? true : sim1Idle);
            }

            return shouldEnable;
        }
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case DIALOG_CONFIRM_DELETE_ALL:
			mDelAllAlertDialog = new AlertDialog.Builder(this).setTitle(
					R.string.clearCallLogConfirmation_title).setIcon(
					android.R.drawable.ic_dialog_alert).setMessage(
					R.string.clearCallLogConfirmation).setNegativeButton(
					android.R.string.cancel, null).setPositiveButton(
					android.R.string.ok, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							SharedPreferences prefs = PreferenceManager
									.getDefaultSharedPreferences(RecentCallsListActivity.this);
							int simFilter = prefs.getInt(SIM_FILTER_PREF,
									FILTER_SIM_DEFAULT);
							int typeFilter = prefs.getInt(TYPE_FILTER_PREF,
									FILTER_TYPE_DEFAULT);
							String selection = getSelection(simFilter,
									typeFilter);
							if (DEBUG) {
								mylog(TAG, "delete call log selection "
										+ selection);
							}
							getContentResolver().delete(Calls.CONTENT_URI,
									selection, null);
							// getContentResolver().delete(Calls.CONTENT_URI,
							// null, null);
							// TODO The change notification should do this
							// automatically, but it
							// isn't working right now. Remove this when the
							// change notification
							// is working properly.
							startQuery();
						}
					}).setCancelable(true).create();
			return mDelAllAlertDialog;
		}
		return null;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Cursor c = mAdapter.getCursor();
		// boolean enable = c == null || c.getCount() <= 0;
		// menu.findItem(MENU_ITEM_DELETE_ALL).setEnabled(!enable);
		// menu.findItem(MENU_ITEM_DELETE_ALL).setInvisible(!enable);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_DELETE_ALL: {

                Cursor c = mAdapter.getCursor();
                boolean enable = c == null || c.getCount() <= 0;
                if (!enable) {
                    showDialog(DIALOG_CONFIRM_DELETE_ALL);
                }
                return true;
            }
                // mtk80909 for Speed Dial
            case MENU_ITEM_SPEED_DIAL: {
                final Intent intent = new Intent(this, SpeedDialManageActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo;
        try {
            menuInfo = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfoIn - Call SIM Via:", e);
            Log.e(TAG, "bad menuInfoIn - item Selected:" + item.getItemId());
            e.printStackTrace();
            return false;
        }

        String number = null;
        number = mAdapter.mDetailQuery.get(menuInfo.position).number;
        int icallType = ContactsUtils.DIAL_TYPE_AUTO;

        switch (item.getItemId()) {
            case CONTEXT_MENU_ITEM_DELETE: {
                // Convert the menu info to the proper type
                // getContentResolver().delete(Calls.CONTENT_URI,
                // Calls._ID + " IN (" + sb + ")", null);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                int simFilter = prefs.getInt(SIM_FILTER_PREF, FILTER_SIM_DEFAULT);
                int typeFilter = prefs.getInt(TYPE_FILTER_PREF, FILTER_TYPE_DEFAULT);
                String selection = getSelection(simFilter, typeFilter);

                int position = menuInfo.position;

                String where = null;
                int tmpCount = mAdapter.mDetailQuery.size();
                if ((position >= 0) && (position < tmpCount)) {
                    where = "(" + Calls.NUMBER + "='" + mAdapter.mDetailQuery.get(position).number
                            + "') AND (" + Calls.DATE + " > "
                            + mAdapter.mDetailQuery.get(position).lDataStart + " AND " + Calls.DATE
                            + " <= " + mAdapter.mDetailQuery.get(position).lDataEnd + ")";
                }

                if (null != selection) {
                    where += " AND (" + selection + ")";
                }

                Log.i(TAG, TAG + " onContextItemSelected : position=" + position + " delete where="
                        + where);
                getContentResolver().delete(Calls.CONTENT_URI, where, null);

                startQuery();
                return true;
            }

            case CONTEXT_MENU_CALL_CONTACT: {
                StickyTabs.saveTab(this, getIntent());
                startActivity(item.getIntent());
                return true;
            }

            case CONTEXT_MENU_VT_CALL_SIM_VIA:
                icallType = ContactsUtils.DIAL_TYPE_VIDEO;
            case CONTEXT_MENU_CALL_SIM_VIA: {
                StickyTabs.saveTab(this, getIntent());
                long originalSimId = mAdapter.mDetailQuery.get(menuInfo.position).simId;
                Log.i(TAG, "onContextItemSelected: number:" + number + " originalSim:"
                        + originalSimId + " icallType:" + icallType);
                ContactsUtils.dial(RecentCallsListActivity.this, number, icallType, originalSimId,
                        null);
                return true;
            }

            case CONTEXT_MENU_NEW_OR_EDIT_CONTACT: {
                addOReditContactPromtion(number);
                return true;
            }

            case CONTEXT_MENU_SPEED_DIAL: {
                final Intent intent = new Intent(this, AddSpeedDialActivity.class);
                ContactInfo ci = mAdapter.getContactInfo(number);
                if (null != ci) {
                    Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, ci.personId);
                    Log.e(TAG, "onContextItemSelected: get ContactInfo from number personid:"
                            + ci.personId);
                    intent.setData(contactUri);
                    startActivity(intent);
                } else {
                    Log.e(TAG, "Error!! -- get ContactInfo from number error! number:" + number);
                }

                return true;
            }

            default: {
                return super.onContextItemSelected(item);
            }
        }
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		// TODO support dual SIM
		case KeyEvent.KEYCODE_CALL: {
			long callPressDiff = SystemClock.uptimeMillis()
					- event.getDownTime();
			if (callPressDiff >= ViewConfiguration.getLongPressTimeout()) {
				// Launch voice dialer
				Intent intent = new Intent(Intent.ACTION_VOICE_COMMAND);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try {
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
				}
				return true;
			}
		}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_CALL:
			try {
				ITelephony phone = ITelephony.Stub.asInterface(ServiceManager
						.checkService("phone"));
				if (phone != null && !phone.isIdle()) {
					// Let the super class handle it
					break;
				}
			} catch (RemoteException re) {
				// Fall through and try to call the contact
			}
			// MTK , for OFN: only focus tab
			if (!getListView().isFocused()) {
				if (DEBUG) {
					Log.d(TAG, "onKeyUp, not focus");
				}
				// getListView().requestFocus();
				break;
			}
			int position;
			position = getListView().getSelectedItemPosition();
			if (DEBUG) {
				Log.d(TAG, "onKeyUp, position=" + position);
			}
			// if nothing focus, position should be -1
			if (position < 0) {
				break;
			}
			callEntry(position);
			return true;
		}

		if (DEBUG) {
			Log.d(TAG, "super.onKeyUp");
		}
		return super.onKeyUp(keyCode, event);
	}

	/*
	 * Get the number from the Contacts, if available, since sometimes the
	 * number provided by caller id may not be formatted properly depending on
	 * the carrier (roaming) in use at the time of the incoming call. Logic : If
	 * the caller-id number starts with a "+", use it Else if the number in the
	 * contacts starts with a "+", use that one Else if the number in the
	 * contacts is longer, use that one
	 */
	private String getBetterNumberFromContacts(String number) {
		String matchingNumber = null;
		// Look in the cache first. If it's not found then query the Phones db
		ContactInfo ci = mAdapter.mContactInfo.get(number);
		if (ci != null && ci != ContactInfo.EMPTY) {
			matchingNumber = ci.number;
		} else {
			try {
				Cursor phonesCursor = RecentCallsListActivity.this
						.getContentResolver()
						.query(
								Uri.withAppendedPath(
										PhoneLookup.CONTENT_FILTER_URI, number),
								PHONES_PROJECTION, null, null, null);
				if (phonesCursor != null) {
					if (phonesCursor.moveToFirst()) {
						matchingNumber = phonesCursor
								.getString(MATCHED_NUMBER_COLUMN_INDEX);
					}
					phonesCursor.close();
				}
			} catch (Exception e) {
				// Use the number from the call log
			}
		}
		if (!TextUtils.isEmpty(matchingNumber)
				&& (matchingNumber.startsWith("+") || matchingNumber.length() > number
						.length())) {
			number = matchingNumber;
		}
		return number;
	}

	private void callEntry(int position) {
		if (DEBUG) {
			Log.d("Calllog", "callEntry, position=" + position);
		}
		if (position < 0) {
			// In touch mode you may often not have something selected, so
			// just call the first entry to make sure that [send] [send] calls
			// the
			// most recent entry.
			position = 0;
		}
		final Cursor cursor = (Cursor) mAdapter.getItem(position);
		if (cursor != null) {
			String number = cursor.getString(NUMBER_COLUMN_INDEX);
			if (TextUtils.isEmpty(number)
					|| number.equals(CallerInfo.UNKNOWN_NUMBER)
					|| number.equals(CallerInfo.PRIVATE_NUMBER)
					|| number.equals(CallerInfo.PAYPHONE_NUMBER)) {
				// This number can't be called, do nothing
				return;
			}
			Intent intent = null;
			// If "number" is really a SIP address, construct a sip: URI.
			if (PhoneNumberUtils.isUriNumber(number)) {
				intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, Uri
						.fromParts("sip", number, null));
			} else {
				// We're calling a regular PSTN phone number.
				// Construct a tel: URI, but do some other possible cleanup
				// first.
				int callType = cursor.getInt(CALL_TYPE_COLUMN_INDEX);
				if (!number.startsWith("+")
						&& (callType == Calls.INCOMING_TYPE || callType == Calls.MISSED_TYPE)) {
					// If the caller-id matches a contact with a better
					// qualified number, use it
					number = getBetterNumberFromContacts(number);
				}
				if (FeatureOption.MTK_GEMINI_SUPPORT) {
					// ContactsUtils.initiateCallWithSim(this, number);
					Uri phoneUri = Uri.fromParts("tel", number, null);
					Intent CallKeyIntent = new Intent(Intent.ACTION_DIAL,
							phoneUri);
					startActivity(CallKeyIntent);
				} else {
					intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, Uri
							.fromParts("tel", number, null));
				}
			}
			StickyTabs.saveTab(this, getIntent());
			if (intent != null) {
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				startActivity(intent);
			}
		}
	}

	private void hideSoftKeyboard() {
		// Hide soft keyboard, if visible
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(mList.getWindowToken(), 0);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.i(TAG, TAG + " RCLA : onListItemClick: position:"
				+ position);

		StickyTabs.saveTab(this, getIntent());
		hideSoftKeyboard();
		String number = null;
		int simId = -1;

		// ToDo: to add SimInfo and SIM Association Judgement
		
		ViewHolder vholder = (ViewHolder)v.getTag();
		
		if (null != vholder) {
			number = (String)vholder.nameTextView.getTag();
			if (vholder.simNameTextView != null)
				simId = (Integer)vholder.simNameTextView.getTag();

			Log.i(TAG, "onListItemClick : number " + number + ", sim " + simId);

			boolean bSpecialNumber = (number.equals(CallerInfo.UNKNOWN_NUMBER) ||
	                number.equals(CallerInfo.PRIVATE_NUMBER) || 
	                number.equals(CallerInfo.PAYPHONE_NUMBER));
			
			if ((!bSpecialNumber) && (!TextUtils.isEmpty(number))) {
                long originalSimId = mAdapter.mDetailQuery.get(position).simId;
                int icallType = ContactsUtils.DIAL_TYPE_AUTO;
//                if (mAdapter.mDetailQuery.get(position).vtcall > 0) {
//                    icallType = ContactsUtils.DIAL_TYPE_VIDEO;
//                }
//                if (ContactsUtils.CALL_TYPE_SIP == mAdapter.mDetailQuery.get(position).simId) {
//                    icallType = ContactsUtils.DIAL_TYPE_SIP;
//                }

                Log.i(TAG, "onListItemClick: number:" + number + " originalSim:" + originalSimId
                        + " icallType:" + icallType + " position:" + position);
                ContactsUtils.dial(RecentCallsListActivity.this, number, icallType, originalSimId,
                        null);

//				Uri telUri = null;
//				
//				telUri = Uri.fromParts("tel", number, null);
//				
//				Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, telUri);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				
//				if (FeatureOption.MTK_GEMINI_SUPPORT) {
//					intent.putExtra(Phone.GEMINI_SIM_ID_KEY, simId);
//				}
//				startActivity(intent);
			}
		} else {
			Log.e(TAG, "--Error!!-- onListItemClick : vholder is null");
		}
	}

	@Override
	public void startSearch(String initialQuery, boolean selectInitialQuery,
			Bundle appSearchData, boolean globalSearch) {
		if (globalSearch) {
			super.startSearch(initialQuery, selectInitialQuery, appSearchData,
					globalSearch);
		} else {
			ContactsSearchManager.startSearch(this, initialQuery);
		}
	}
	
	private long getValidSIMInfo(boolean bForce) {
		
		if (null == mSIMInfoWrapperAll) {
			mSIMInfoWrapperAll = SIMInfoWrapper.getDefault(this);
		}
		if (bForce == true) {
			mSIMInfoWrapperAll.release();
			mSIMInfoWrapperAll = SIMInfoWrapper.getDefault(this);
		}
		mInsertedSIMInfoList = mSIMInfoWrapperAll.getInsertedSimInfoList();
		mAllSIMInfoList = mSIMInfoWrapperAll.getSimInfoList();
		mAllSimInfoMap = mSIMInfoWrapperAll.getSimInfoMap();
		
		insertedSimCount = mInsertedSIMInfoList.size();
		
		if ((insertedSimCount > 1) && (null != mInsertedSIMInfoList)) {
			SIMInfo si1 = null;
			SIMInfo si2 = null;
			
			if (Long.valueOf(SIM_SLOT_FIRST) == mInsertedSIMInfoList.get(0).mSlot) {
				si1 = mInsertedSIMInfoList.get(0);
				si2 = mInsertedSIMInfoList.get(1);
			} else {
				si1 = mInsertedSIMInfoList.get(1);
				si2 = mInsertedSIMInfoList.get(0);
			}
			sim2Number = si2.mNumber;
			sim2ID = si2.mSimId;
			sim2Name = si2.mDisplayName;
			sim1Number = si1.mNumber;
			sim1ID = si1.mSimId;
			sim1Name = si1.mDisplayName;
		} else if ((insertedSimCount > 0) && (null != mInsertedSIMInfoList)) {
			if (Long.valueOf(SIM_SLOT_FIRST) == mInsertedSIMInfoList.get(0).mSlot) {
				sim1Number = mInsertedSIMInfoList.get(0).mNumber;
				sim1ID = mInsertedSIMInfoList.get(0).mSimId;
				sim1Name = mInsertedSIMInfoList.get(0).mDisplayName;
			} else {
				sim2Number = mInsertedSIMInfoList.get(0).mNumber;
				sim2ID = mInsertedSIMInfoList.get(0).mSimId;
				sim2Name = mInsertedSIMInfoList.get(0).mDisplayName;
			}
		} else {
			insertedSimCount = 0;
			
			sim1Number = null;
			sim1Name = null;
			sim1ID = 0;
			
			sim2Number = null;
			sim2Name = null;
			sim2ID = 0;
		}
		return insertedSimCount;
	}
	
	private boolean checkSIMConflict(int pos) {
		boolean bRet = false;
		
		int simId = (int)mAdapter.mDetailQuery.get(pos).id;
		long defaultSim = Settings.System.getLong(this.getContentResolver(), 
                Settings.System.VOICE_CALL_SIM_SETTING,
                Settings.System.DEFAULT_SIM_NOT_SET);
		
		long defaultSlot = mSIMInfoWrapperAll.getSimSlotById(simId);
		bRet = (defaultSim == defaultSlot);
		
		return bRet;
	}
	
	void setSimIndicatorVisibility(boolean visible) {
        if(visible)
            mStatusBarMgr.showSIMIndicator(getComponentName(), Settings.System.VOICE_CALL_SIM_SETTING);
        else
            mStatusBarMgr.hideSIMIndicator(getComponentName());
    }

	static class ViewHolder {
		TextView headTextView;
        TextView nameTextView;
        TextView labelTextView;
        TextView numberTextView;
        TextView simNameTextView;
        TextView timeTextView;
        
        QuickContactBadge photoImageView;
        ImageView detailImageView;
        ImageView calltypeImageView;
        int       position;
	}
	
	private class CallLogChangeObserver extends ContentObserver {
        public CallLogChangeObserver() {
            super(new Handler());
        }
        @Override
        public void onChange(boolean selfChange) {
            Log.e(TAG, "zjtest onChange");
            startQuery();
        }
    }
	
	public CellConnMgr getCellConnMgr() {
        // TODO Auto-generated method stub
        return mCellConnMgr;
    }
	
	boolean isSpecialNumber(String number) {
        boolean bSpecialNumber = false;
        if (null == number) {
            return bSpecialNumber;
        }
        bSpecialNumber = (number.equals(CallerInfo.UNKNOWN_NUMBER) ||
                number.equals(CallerInfo.PRIVATE_NUMBER) || 
                number.equals(CallerInfo.PAYPHONE_NUMBER));
        return bSpecialNumber; 
    }
}
