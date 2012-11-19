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

import com.android.internal.telephony.CallerInfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.app.StatusBarManager;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.Settings;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.Contacts.Intents.Insert;
import android.provider.Settings;
import android.provider.Telephony;
import android.provider.Telephony.SIMInfo;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ITelephony;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.telephony.PhoneNumberFormatUtilEx;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.CellConnService.CellConnMgr ;
import android.os.SystemProperties;

/**
 * Displays the details of a specific call log entry.
 */
public class CallDetailActivity extends ListActivity implements
        AdapterView.OnItemClickListener {
    private static final String TAG = "CallDetail";

    /* Added by xingping.zheng start */
    private static final int QUERY_TOKEN_0                 = 0;
    private static final int QUERY_TOKEN_1                 = 1;

    private static final int FILTER_BASE = 20000;
    private static final int FILTER_SIM_ALL                = -1;
    private static final int FILTER_TYPE_ALL               = FILTER_BASE + 11;
    private static final int FILTER_TYPE_INCOMING          = FILTER_BASE + 12;
    private static final int FILTER_TYPE_MISSED            = FILTER_BASE + 13;
    private static final int FILTER_TYPE_OUTGOING          = FILTER_BASE + 14;
    
    private static final String SIM_FILTER_PREF            = "calllog_sim_filter";
    private static final String TYPE_FILTER_PREF           = "calllog_type_filter";
    private static final String CALL_DETAIL_KEY_DATE_START = "com.android.contacts.date_start";
    private static final String CALL_DETAIL_KEY_DATE_END   = "com.android.contacts.date_end";
    private static final String CALL_DETAIL_KEY_SIM_ID     = "com.android.contacts.simid";
    private static final String CALL_DETAIL_KEY_SIM_PHOTOID = "com.android.contacts.photoid";
    private static final String CALL_DETAIL_KEY_SIM_IND     = "com.android.contacts.issimind";
    
    private static final String CONTACT_INFO_NAME          = "contact_info_name";
    private static final String CONTACT_INFO_LABEL         = "contact_info_label";
    private static final String CONTACT_INFO_NUMBER        = "com.android.contacts.number";
    private static final String CONTACT_INFO_OPERATOR      = "contact_info_operator";
    private static final String ACTION_CALL_VIA            = "call_via";
    /* Added by xingping.zhend end   */
    
    private TextView mCallType;
    private ImageView mCallTypeIcon;
    private TextView mCallTime;
    private TextView mCallDuration;

    private static boolean mIsSipCallLog = false;
 
    /* Added by xingping.zheng start */
    private TextView mName;
    private TextView mLabelNumber;
    private TextView mSimName;
    private QueryHandler mQueryHandler;
    private ContactPhotoLoader mPhotoLoader;
    private Dialog mDialog;
    private Dialog mTurnOnSipDialog;
    private boolean mIsVTCall;
    ArrayList<Integer> mAssociateSims = new ArrayList<Integer>();
    private StatusBarManager mStatusBarMgr;
    private final BroadcastReceiver mSimIndicatorReceiver = new SimIndicatorBroadcastReceiver();
    boolean mShowSimIndicator = false;
    
    private Dialog mTurnOn3GServiceDialog;
    /* Added by xingping.zhend end   */
    
    private String mNumber;
    private String mContactName;
    private long mSimId;
    private long mlCallType;
    private long mPhotoId;
    private int mSimInd;
    private Uri mPersonUri;
    private TextView mEmptyTextView;
    
    String mVoiceMailNumber;
    String mVoiceMailNumber2 = "";
    boolean mIsVoiceMailNumber;
    
    /* package */ LayoutInflater mInflater;
    /* package */ Resources mResources;

    private int mSlot;
    private CellConnMgr mCellConnMgr;
    static final String[] CALL_LOG_PROJECTION = new String[] {
        CallLog.Calls.DATE,
        CallLog.Calls.DURATION,
        CallLog.Calls.NUMBER,
        CallLog.Calls.TYPE,
        CallLog.Calls.SIM_ID,
        CallLog.Calls.VTCALL
    };

    static final int DATE_COLUMN_INDEX = 0;
    static final int DURATION_COLUMN_INDEX = 1;
    static final int NUMBER_COLUMN_INDEX = 2;
    static final int CALL_TYPE_COLUMN_INDEX = 3;
    static final int CALL_SIMID_COLUMN_INDEX = 4;
    static final int CALL_VT_COLUMN_INDEX = 5;

    static final String[] PHONES_PROJECTION = new String[] {
        PhoneLookup._ID,
        PhoneLookup.DISPLAY_NAME,
        PhoneLookup.TYPE,
        PhoneLookup.LABEL,
        PhoneLookup.NUMBER,
        PhoneLookup.PHOTO_ID,
        PhoneLookup.INDICATE_PHONE_SIM
    };
    static final int COLUMN_INDEX_ID = 0;
    static final int COLUMN_INDEX_NAME = 1;
    static final int COLUMN_INDEX_TYPE = 2;
    static final int COLUMN_INDEX_LABEL = 3;
    static final int COLUMN_INDEX_NUMBER = 4;
    static final int COLUMN_INDEX_PHOTO_ID = 5;
    static final int COLUMN_INDICATE_PHONE_SIM = 6;

    private Runnable mServiceComplete = new Runnable() {
        public void run() {
            int result = mCellConnMgr.getResult();
            log("mServiceComplete, result = "+result);
            if(result == com.mediatek.CellConnService.CellConnMgr.RESULT_STATE_NORMAL) {
                mSlot = mCellConnMgr.getPreferSlot();
                Intent intent = ContactsUtils.generateDialIntent(false, mSlot, mNumber);
                intent.putExtra("is_vt_call", mIsVTCall);
                CallDetailActivity.this.sendBroadcast(intent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if(false)
            setContentView(R.layout.call_detail);
        else
            setContentView(R.layout.call_detail_ge);

        mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mResources = getResources();

        mCallType = (TextView) findViewById(R.id.type);
        mCallTypeIcon = (ImageView) findViewById(R.id.icon);
        mCallTime = (TextView) findViewById(R.id.time);
        mCallDuration = (TextView) findViewById(R.id.duration);
        
        /* Added by xingping.zheng start */
        mName = (TextView) findViewById(R.id.name);
        mLabelNumber = (TextView) findViewById(R.id.label_number);
        mSimName = (TextView) findViewById(R.id.simName);
        mEmptyTextView = (TextView) findViewById(R.id.emptyText);
        mEmptyTextView.setText(R.string.unknown);
        mEmptyTextView.setVisibility(View.GONE);
        mQueryHandler = new QueryHandler(getContentResolver());
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture);
        /* Added by xingping.zheng end   */
        getListView().setDivider(null);
        getListView().setOnItemClickListener(this);
        mCellConnMgr = new CellConnMgr(mServiceComplete);
        mCellConnMgr.register(getApplicationContext());
        mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mVoiceMailNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
                    .getVoiceMailNumberGemini(Phone.GEMINI_SIM_1);
            mVoiceMailNumber2 = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
                    .getVoiceMailNumberGemini(Phone.GEMINI_SIM_2);
        } else {
            mVoiceMailNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
                    .getVoiceMailNumber();
        }
        
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
            registerReceiver(mSimIndicatorReceiver, intentFilter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhotoLoader.stop();
        mCellConnMgr.unregister();
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            unregisterReceiver(mSimIndicatorReceiver);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mPhotoLoader.clear();
        mPhotoLoader.resume();
        if(false)
            updateData(getIntent().getData());
        else
            updateData(getIntent());
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            mShowSimIndicator = true;
            setSimIndicatorVisibility(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        dimissAllDialogs();
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            mShowSimIndicator = false;
            setSimIndicatorVisibility(false);
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                // Make sure phone isn't already busy before starting direct call
                TelephonyManager tm = (TelephonyManager)
                        getSystemService(Context.TELEPHONY_SERVICE);
                if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL,
                            Uri.fromParts("tel", mNumber, null));
                    startActivity(callIntent);
                    StickyTabs.saveTab(this, getIntent());
                    return true;
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private long generateSuggestedSim() {
        boolean hasAssociateSim = mAssociateSims.size() > 0;
        boolean hasMoreAssociateSim = mAssociateSims.size() > 1;
        long suggestedSim = Settings.System.DEFAULT_SIM_NOT_SET;
        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        log("mSimId = "+mSimId);
        if(hasAssociateSim) {
            if(!hasMoreAssociateSim)
                suggestedSim = mAssociateSims.get(0).intValue();
        } else {
            try {
                SIMInfo simInfo = SIMInfo.getSIMInfoById(this, mSimId);
                if(simInfo != null && telephony != null && simInfo.mSlot >= 0 && telephony.isSimInsert(simInfo.mSlot))
                    suggestedSim = mSimId;
                else
                    suggestedSim = Settings.System.getLong(getContentResolver(), Settings.System.VOICE_CALL_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET);
            } catch(Exception e) {}
        }
        log("generateSuggestedSim, hasAssociateSim = "+hasAssociateSim+" hasMoreAssociateSim = "+hasMoreAssociateSim+" suggestedSim = "+suggestedSim);
        return suggestedSim;
    }

    private void generateCommonActions(List<ViewEntry> actions, Cursor cursor) {
        ViewEntry entry = null;
        boolean isEmergencyNumber = PhoneNumberUtils.isEmergencyNumber(mNumber);
        String text = getString(R.string.call_detail_call_via, mContactName);
        if(!isEmergencyNumber && FeatureOption.MTK_GEMINI_SUPPORT) {
            int inserted = 0;
            int associateSim = (int)Settings.System.DEFAULT_SIM_NOT_SET;
            ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
            mAssociateSims.clear();
            if(cursor != null && cursor.moveToFirst()) {
                int slot;
                do {
                    if(cursor.getInt(0) > 0) {
                        slot = SIMInfo.getSlotById(this, cursor.getInt(0));
                        if(slot >= 0) {
                            try {
                                if (telephony != null){
                                if(telephony.isSimInsert(slot)) {
                                    inserted++;
                                    associateSim = cursor.getInt(0);
                                    mAssociateSims.add(associateSim);
                                }
                                }
                            } catch(RemoteException e) {
                                // ignore
                            }
                        }
                    }
                } while (cursor.moveToNext());
                log("generateCommonActions, inserted = "+inserted+" associateSim = "+associateSim);
            }
            long defaultSim = Settings.System.getLong(getContentResolver(), Settings.System.VOICE_CALL_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET);
            log("generateCommonActions, defaultSim = "+defaultSim+" originalSim = "+mSimId);
            if((inserted == 0 && (defaultSim == mSimId)) ||
               (inserted == 1 && (defaultSim == mSimId && defaultSim == associateSim))) {
                Intent temp = null;
                boolean sip = defaultSim == Settings.System.VOICE_CALL_SIM_SETTING_INTERNET;
                if(!sip) {
                    SIMInfo simInfo = SIMInfo.getSIMInfoById(this, defaultSim);
                    if(simInfo != null) {
                        temp = ContactsUtils.generateDialIntent(false, simInfo.mSlot, mNumber);
                        entry = new ViewEntry(0, text, temp, simInfo.mDisplayName, simInfo.mColor);
                        actions.add(entry);
                    }
                } else {
                    temp = ContactsUtils.generateDialIntent(true, 0, mNumber);
                    temp.putExtra("sip_preferred", true);
                    entry = new ViewEntry(0, text, temp, this.getResources().getString(R.string.sipcall), com.mediatek.internal.R.drawable.sim_background_sip);
                    actions.add(entry);
                }
            }
            if(cursor != null)
                cursor.close();
        }
        
        /* Call XXX via */
        if(!isEmergencyNumber && FeatureOption.MTK_GEMINI_SUPPORT) {
            Intent emptyIntent = new Intent(ACTION_CALL_VIA);
            entry = new ViewEntry(R.drawable.contact_calllog_dialerseach_down_detail, text, emptyIntent);
        } else {
            Intent callIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED, Uri.fromParts("tel", mNumber, null));
            entry = new ViewEntry(R.drawable.sym_action_call, getString(R.string.menu_callNumber, mContactName), callIntent);
        }
        actions.add(entry);
        
        /* Video Call */
        boolean sim1Radio = true;
        boolean sim2Radio = true;
        boolean sim1Idle = true;
        boolean sim2Idle = true;
        boolean sim1Ready = true;
        boolean sim2Ready = true;
        boolean bVTEnabled = true;
        boolean isVTIdle = false;
        
        if (true == FeatureOption.MTK_VT3G324M_SUPPORT) { // for VT IT, last
            try {
                ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                        .getService(Context.TELEPHONY_SERVICE));
                if (iTel != null){
                    isVTIdle = iTel.isVTIdle();
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            try {
                ITelephony iTel = ITelephony.Stub
                        .asInterface(ServiceManager
                                .getService(Context.TELEPHONY_SERVICE));
                if (iTel != null){
                    sim1Radio = iTel.isRadioOnGemini(Phone.GEMINI_SIM_1);
                    sim2Radio = iTel.isRadioOnGemini(Phone.GEMINI_SIM_2);
                    sim1Idle = iTel.isIdleGemini(Phone.GEMINI_SIM_1);
                    sim2Idle = iTel.isIdleGemini(Phone.GEMINI_SIM_2);
                }

                sim1Ready = (TelephonyManager.SIM_STATE_READY == TelephonyManager
                        .getDefault().getSimStateGemini(Phone.GEMINI_SIM_1));
                sim2Ready = (TelephonyManager.SIM_STATE_READY == TelephonyManager
                        .getDefault().getSimStateGemini(Phone.GEMINI_SIM_2));
                Log.d(TAG, "generateCommonActions sim1 raido on " + sim1Radio);
                Log.d(TAG, "generateCommonActions sim2 Radio on " + sim2Radio);
                Log.d(TAG, "generateCommonActions sim1 sim1Idle on " + sim1Idle);
                Log.d(TAG, "generateCommonActions sim2 sim2Idle on " + sim2Idle);
                Log.d(TAG, "generateCommonActions sim1 sim1Ready on " + sim1Ready);
                Log.d(TAG, "generateCommonActions sim2 sim2Ready on " + sim2Ready);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
			if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
				bVTEnabled = ((sim1Radio && sim1Ready) || (sim2Radio && sim2Ready))
						&& sim1Idle && sim2Idle;
			} else {
				bVTEnabled = sim1Radio && sim1Ready && sim1Idle
						&& sim2Idle;
			}
        } else {
            // Single Card
            boolean simRadio = true;
            boolean simIdle = true;
            try {
                ITelephony iTel = ITelephony.Stub
                        .asInterface(ServiceManager
                                .getService(Context.TELEPHONY_SERVICE));
                if (iTel != null){
                    simRadio = iTel.isRadioOn();
                    simIdle = iTel.isIdle();
                }
                Log.d(TAG, "generateCommonActions sim raido on " + simRadio);
                Log.d(TAG, "generateCommonActions simIdle is  " + simIdle);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
            bVTEnabled = simRadio && simIdle;
        }

        if(PhoneNumberUtils.isUriNumber(mNumber))
            bVTEnabled = false;

        if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
        	//long originalSimId = mAdapter.mDetailQuery.get(menuInfo.position).simId;
        	int mSlot = ContactsUtils.get3GCapabilitySIM();
        	Log.d(TAG, "[VT Call][mSlot] : " + mSlot);
            if(mSlot == -1) {
                entry = new ViewEntry(R.drawable.call_detail_dial_vt_button, getString(R.string.video_call), 
                        null, null, 0, bVTEnabled);
                actions.add(entry);
            }else{
                Intent videoCallIntent = ContactsUtils.generateDialIntent(false, mSlot, mNumber);
                videoCallIntent.putExtra("is_vt_call", true);
                entry = new ViewEntry(R.drawable.call_detail_dial_vt_button, getString(R.string.video_call), 
                        videoCallIntent, null, 0, bVTEnabled);
                actions.add(entry);
            }

        }
        
        /* Send text message */
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", mNumber, null));
        entry = new ViewEntry(R.drawable.sym_action_sms, getString(R.string.menu_sendTextMessage), smsIntent);
        actions.add(entry);
        
        /* View contact or Add to contacts */
        if (mPersonUri != null) {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW, mPersonUri);
            StickyTabs.setTab(viewIntent, getIntent());
            entry = new ViewEntry(R.drawable.sym_action_view_contact,
                    getString(R.string.menu_viewContact), viewIntent);
            actions.add(entry);
        } else {
            Intent createIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
            createIntent.setType(Contacts.CONTENT_ITEM_TYPE);
            createIntent.putExtra(Insert.PHONE, mNumber);
            entry = new ViewEntry(R.drawable.sym_action_add_contact,
                    getString(R.string.recentCalls_addToContact), createIntent);
            actions.add(entry);
        }
    }
    
    private void queryNumberAssociation(List<ViewEntry> actions) {
  
        if(!TextUtils.isEmpty(mNumber)) {
            String number = mNumber;
            if(!PhoneNumberUtils.isUriNumber(number))
                number = PhoneNumberUtils.stripSeparators(number);
            log("queryNumberAssociation, mNumber = "+mNumber+" number = "+number);
            Uri uri = Uri.withAppendedPath(CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(mNumber));
            mQueryHandler.startQuery(QUERY_TOKEN_1,
                                     actions,
                                     uri,
                                     new String[]{Data.SIM_ID},
                                     Data.MIMETYPE + "='" + CommonDataKinds.Phone.CONTENT_ITEM_TYPE 
                                     + "' AND (" + Data.DATA1 + "='" + number + "') AND (" + Data.SIM_ID + ">0)",
                                     null,
                                     null);
        }
    }
    
    /* Added by xingping.zheng start */
    private void generateCallLogActions(List<ViewEntry> actions, Cursor cursor) {
        
        long date;
        ViewEntry entry;
        String previousFormatDate = "";
        String formatDate;
        String formatTime;
        long duration;
        int callType;
        int previousCallType = -1;
        
        log("generateCallLogActions");
        
        if(cursor != null && cursor.moveToFirst()) {
            mlCallType = cursor.getLong(CALL_TYPE_COLUMN_INDEX);
            
            do {
                date = cursor.getLong(DATE_COLUMN_INDEX);
                duration = cursor.getLong(DURATION_COLUMN_INDEX);
                callType = cursor.getInt(CALL_TYPE_COLUMN_INDEX);
                
//                formatDate = DateUtils.formatDateRange(this, date, date, 
//                        DateUtils.FORMAT_SHOW_WEEKDAY |  
//                        DateUtils.FORMAT_SHOW_DATE |
//                        DateUtils.FORMAT_SHOW_YEAR |
//                        DateUtils.FORMAT_ABBREV_MONTH);
                
//                formatDate = DateFormat.format("E MM/dd/yyyy", date).toString();
                java.text.DateFormat df = DateFormat.getDateFormat(CallDetailActivity.this);
                formatDate = df.format(date);
                formatTime = DateUtils.formatDateRange(this, date, date, DateUtils.FORMAT_SHOW_TIME);

                log("formatDate:"+formatDate + " formatTime:" + formatTime);
                
                if(previousCallType != callType || !formatDate.equals(previousFormatDate)) {
                    /* add header */
                    log("add header");
                    entry = new ViewEntry(callType, formatDate);
                    actions.add(entry);
                    
                    previousFormatDate = formatDate;
                    previousCallType = callType;
                } 
                
                entry = new ViewEntry(formatDuration(duration), formatTime);
                actions.add(entry);
            } while(cursor.moveToNext());
            
            if(cursor != null)
                cursor.close();
        }
    }
    
    private void updateData(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int callType = prefs.getInt(TYPE_FILTER_PREF, FILTER_TYPE_ALL);
        int contactType = -1;
        
        Bundle bundle =  intent.getExtras();
        
        /* get information from Intent */
        long dateStart  = 0;
        long dateEnd    = 0;
        String number   = null;
        long simId      = 0;
        
        log("updateData, simId = "+simId);
        if (null != bundle) {
            /* get information from Intent */
            dateStart  = intent.getExtras().getLong(CALL_DETAIL_KEY_DATE_START);
            dateEnd    = intent.getExtras().getLong(CALL_DETAIL_KEY_DATE_END);
            number     = intent.getExtras().getString(CONTACT_INFO_NUMBER);
            simId      = intent.getExtras().getLong(CALL_DETAIL_KEY_SIM_ID);
            mPhotoId   = intent.getExtras().getLong(CALL_DETAIL_KEY_SIM_PHOTOID);
            mSimInd    = intent.getExtras().getInt(CALL_DETAIL_KEY_SIM_IND);
            /*
            String label    = intent.getExtras().getString(CONTACT_INFO_LABEL);
            String name     = intent.getExtras().getString(CONTACT_INFO_NAME);
            */
            mSimId = simId;
            mNumber = number;
            mPersonUri = null;
            mContactName = null;
            /* update contact information */
            if (isVoiceMail(number)) {
                mIsVoiceMailNumber = true;
            } else {
                mIsVoiceMailNumber = false;
            }
            if (isSpecialNumber(mNumber)) {
                // List is empty, let the empty view show instead.
//                TextView emptyText = (TextView) findViewById(R.id.emptyText);
//                if (emptyText != null) {
//                    emptyText.setText(mNumber.equals(CallerInfo.PRIVATE_NUMBER)
//                            ? R.string.private_num : R.string.unknown);
//                }
                String selection;
                if(FeatureOption.MTK_GEMINI_SUPPORT) {
                    selection = generateSelection((int)mSimId, callType, dateStart, dateEnd);
                } else {
                    selection = generateSelection(callType, dateStart, dateEnd);
                }
                if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
                    mName.setText(R.string.unknown);
                } else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
                    mName.setText(R.string.private_num);
                } else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
                    mName.setText(R.string.payphone);
                } else {
                    mName.setVisibility(View.GONE);
                }
                
                mLabelNumber.setVisibility(View.GONE);
                mPhotoLoader.loadPhoto(mCallTypeIcon, 0, -1);

                List<ViewEntry> actions = new ArrayList<ViewEntry>();
                Uri uri = Uri.withAppendedPath(Calls.CONTENT_FILTER_URI, Uri.encode(mNumber));
                mQueryHandler.startQuery(QUERY_TOKEN_0,             // token
                                         actions,                   // cookie
                                         uri,                       // uri 
                                         CALL_LOG_PROJECTION,       // projection
                                         selection,                 // selection
                                         null,                      // selection args
                                         Calls.DEFAULT_SORT_ORDER); // order
            } else {
                // Perform a reverse-phonebook lookup to find the PERSON_ID
                String callLabel = null;
                long photoId = -1;
                long personId;
                Uri personUri = null;
                Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(mNumber));
                Cursor phonesCursor = null;
                Cursor dataCursor = null;
                // do not query phone lookup table when it's a sip call log
                if(mSimId != ContactsUtils.CALL_TYPE_SIP || !PhoneNumberUtils.isUriNumber(mNumber))
                    phonesCursor = getContentResolver().query(phoneUri, PHONES_PROJECTION, null, null, null);
                try {
                    if (phonesCursor != null && phonesCursor.moveToFirst()) {
                        personId = phonesCursor.getLong(COLUMN_INDEX_ID);
                        photoId = phonesCursor.getLong(COLUMN_INDEX_PHOTO_ID);
                        contactType = phonesCursor.getInt(COLUMN_INDICATE_PHONE_SIM);
                        
                        personUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, personId);
                        mNumber = PhoneNumberFormatUtilEx.formatNumber(phonesCursor.getString(COLUMN_INDEX_NUMBER));
                        mPersonUri = personUri;
                        callLabel = CommonDataKinds.Phone.getDisplayLabel(this,
                                                                          phonesCursor.getInt(COLUMN_INDEX_TYPE),
                                                                          phonesCursor.getString(COLUMN_INDEX_LABEL)).toString();
                        mContactName = phonesCursor.getString(COLUMN_INDEX_NAME);
                        mName.setText(mContactName);
                        mLabelNumber.setVisibility(View.VISIBLE);
                        if(callLabel != null)
                            mLabelNumber.setText(callLabel+" "+PhoneNumberFormatUtilEx.formatNumber(number));
                        else
                            mLabelNumber.setText(PhoneNumberFormatUtilEx.formatNumber(number));
                    } else {
                        if(PhoneNumberUtils.isUriNumber(mNumber)) {
                            Uri contactRef = Data.CONTENT_URI;
                            String selection = "upper(" + Data.DATA1 + ")=?" + " AND " + Data.MIMETYPE
                            + "='" + SipAddress.CONTENT_ITEM_TYPE + "'";
                            String[] selectionArgs = new String[] { mNumber.toUpperCase() };
                            dataCursor = getContentResolver().query(contactRef, null, selection, selectionArgs, null);
                        }

                        if(dataCursor != null && dataCursor.getCount() > 0 && dataCursor.moveToFirst()) {
                            personId = dataCursor.getLong(dataCursor.getColumnIndex(Data.CONTACT_ID));
                            personUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, personId);
                            mPersonUri = personUri;
                            mContactName = dataCursor.getString(dataCursor.getColumnIndex(Data.DISPLAY_NAME));
                            mName.setText(mContactName);
                            mLabelNumber.setVisibility(View.VISIBLE);
                            callLabel = dataCursor.getString(dataCursor.getColumnIndex(Data.DATA2));
                            if(!TextUtils.isEmpty(callLabel))
                                mLabelNumber.setText(callLabel+" "+mNumber);
                            else
                                mLabelNumber.setText(mNumber);
                        } else {
                            mNumber = PhoneNumberFormatUtilEx.formatNumber(mNumber);
                            mName.setText(mNumber);
                            mContactName = mNumber;
                            mLabelNumber.setVisibility(View.GONE);
                        }
                    }
                    photoId = mPhotoId;
                    if (mIsVoiceMailNumber) {
                        photoId = 0;
                    }
                    log("phonesCursor = " + phonesCursor + "photoId = " + photoId);
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        contactType = mSimInd;
                        if (contactType != -1)
                            contactType = SIMInfo.getSlotById(this, contactType);
                    } 
                    mPhotoLoader.loadPhoto(mCallTypeIcon, photoId, contactType);
                } finally {
                    if (phonesCursor != null) 
                        phonesCursor.close();
                    if(dataCursor != null)
                        dataCursor.close();
                }

                String selection;
                if(FeatureOption.MTK_GEMINI_SUPPORT) {
                    selection = generateSelection((int)mSimId, callType, dateStart, dateEnd);
                } else {
                    selection = generateSelection(callType, dateStart, dateEnd);
                }

                List<ViewEntry> actions = new ArrayList<ViewEntry>();
                Uri uri = Uri.withAppendedPath(Calls.CONTENT_FILTER_URI, Uri.encode(mNumber));
                mQueryHandler.startQuery(QUERY_TOKEN_0,             // token
                                         actions,                   // cookie
                                         uri,                       // uri 
                                         CALL_LOG_PROJECTION,       // projection
                                         selection,                 // selection
                                         null,                      // selection args
                                         Calls.DEFAULT_SORT_ORDER); // order
                // To handle voice mail number
                if (mIsVoiceMailNumber) {
                    mContactName = getString(R.string.voicemail);
                    mName.setText(mContactName);
                    mLabelNumber.setVisibility(View.GONE);
                }
            }
        } else {
        	// for globle search
        	Uri callUri = intent.getData();
        	
        	mSimId = simId;
	        mNumber = number;
	        mPersonUri = null;
	        mContactName = null;
        	
	        ContentResolver resolver = getContentResolver();
            Cursor calllogCursor = resolver.query(callUri, CALL_LOG_PROJECTION, null, null, null);
	        if (null != calllogCursor && calllogCursor.moveToFirst()) {
	        	number = calllogCursor.getString(NUMBER_COLUMN_INDEX);
	        	mNumber = number;
	        	simId = calllogCursor.getLong(CALL_SIMID_COLUMN_INDEX);
	        	mSimId = simId;
	        }
	        
	        if (null != calllogCursor) {
	        	calllogCursor.close();
	        }
	        
	        if (isVoiceMail(mNumber)) {
                mIsVoiceMailNumber = true;
            } else {
                mIsVoiceMailNumber = false;
            }
	        
            if (mNumber.equals(CallerInfo.UNKNOWN_NUMBER)
                    || mNumber.equals(CallerInfo.PRIVATE_NUMBER)) {
                // List is empty, let the empty view show instead.
                TextView emptyText = (TextView) findViewById(R.id.emptyText);
                if (emptyText != null) {
                    emptyText
                            .setText(mNumber.equals(CallerInfo.PRIVATE_NUMBER) ? R.string.private_num
                                    : R.string.unknown);
                }
            } else {
                // Perform a reverse-phonebook lookup to find the PERSON_ID
                String callLabel = null;
                long photoId = -1;
                long personId;
                Uri personUri = null;
                Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri
                        .encode(mNumber));

                Cursor phonesCursor = null;
                Cursor dataCursor = null;
                if (mSimId != ContactsUtils.CALL_TYPE_SIP)
                    phonesCursor = getContentResolver().query(phoneUri, PHONES_PROJECTION, null,
                            null, null);
                try {
                    if (phonesCursor != null && phonesCursor.moveToFirst()) {

                        personId = phonesCursor.getLong(COLUMN_INDEX_ID);
                        photoId = phonesCursor.getLong(COLUMN_INDEX_PHOTO_ID);
                        contactType = phonesCursor.getInt(COLUMN_INDICATE_PHONE_SIM);

                        personUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, personId);
                        mNumber = PhoneNumberFormatUtilEx.formatNumber(phonesCursor
                                .getString(COLUMN_INDEX_NUMBER));
                        mPersonUri = personUri;
                        callLabel = CommonDataKinds.Phone.getDisplayLabel(this,
                                phonesCursor.getInt(COLUMN_INDEX_TYPE),
                                phonesCursor.getString(COLUMN_INDEX_LABEL)).toString();
                        mContactName = phonesCursor.getString(COLUMN_INDEX_NAME);
                        mName.setText(mContactName);
                        mLabelNumber.setVisibility(View.VISIBLE);
                        if (callLabel != null)
                            mLabelNumber.setText(callLabel + " " + PhoneNumberFormatUtilEx.formatNumber(number));
                        else
                            mLabelNumber.setText(PhoneNumberFormatUtilEx.formatNumber(number));
                    } else {
                        if (PhoneNumberUtils.isUriNumber(mNumber)) {
                            Uri contactRef = Data.CONTENT_URI;
                            String selection = "upper(" + Data.DATA1 + ")=?" + " AND "
                                    + Data.MIMETYPE + "='" + SipAddress.CONTENT_ITEM_TYPE + "'";
                            String[] selectionArgs = new String[] {
                                mNumber.toUpperCase()
                            };
                            dataCursor = getContentResolver().query(contactRef, null, selection,
                                    selectionArgs, null);
                        }

                        if (dataCursor != null && dataCursor.getCount() > 0
                                && dataCursor.moveToFirst()) {
                            personId = dataCursor.getLong(dataCursor
                                    .getColumnIndex(Data.CONTACT_ID));
                            personUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, personId);
                            mPersonUri = personUri;
                            mContactName = dataCursor.getString(dataCursor
                                    .getColumnIndex(Data.DISPLAY_NAME));
                            mName.setText(mContactName);
                            mLabelNumber.setVisibility(View.VISIBLE);
                            callLabel = dataCursor.getString(dataCursor.getColumnIndex(Data.DATA2));
                            if (!TextUtils.isEmpty(callLabel))
                                mLabelNumber.setText(callLabel + " " + mNumber);
                            else
                                mLabelNumber.setText(mNumber);
                        } else {
                            mNumber = PhoneNumberFormatUtilEx.formatNumber(mNumber);
                            mName.setText(mNumber);
                            mContactName = mNumber;
                            mLabelNumber.setVisibility(View.GONE);
                        }
                    }
                    
                    photoId = mPhotoId;
                    if (mIsVoiceMailNumber) {
                        photoId = 0;
                    }
                    log("phonesCursor = " + phonesCursor + "photoId = " + photoId);
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        contactType = mSimInd;
                        if (contactType != -1)
                            contactType = SIMInfo.getSlotById(this, contactType);
                    } 
                    mPhotoLoader.loadPhoto(mCallTypeIcon, photoId, contactType);
                } finally {
                    if (phonesCursor != null)
                        phonesCursor.close();
                    if (dataCursor != null)
                        dataCursor.close();
                }

                List<ViewEntry> actions = new ArrayList<ViewEntry>();
                Uri uri = Uri.withAppendedPath(Calls.CONTENT_FILTER_URI, Uri.encode(mNumber));
                mQueryHandler.startQuery(QUERY_TOKEN_0, // token
                        actions, // cookie
                        uri, // uri
                        CALL_LOG_PROJECTION, // projection
                        null, // selection
                        null, // selection args
                        Calls.DEFAULT_SORT_ORDER); // order
            }
            
            if (mIsVoiceMailNumber) {
                mContactName = getString(R.string.voicemail);
                mName.setText(mContactName);
                mLabelNumber.setVisibility(View.GONE);
            }
        }

        /* update sim name */
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            if(mSimId == ContactsUtils.CALL_TYPE_SIP) {
                mSimName.setBackgroundResource(com.mediatek.internal.R.drawable.sim_background_sip);
                mSimName.setText(R.string.sipcall);
                mSimName.setPadding(3, 2, 3, 0);
                return;
            }

            SIMInfo simInfo = SIMInfo.getSIMInfoById(this, simId);
            if(simInfo != null) {
                ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                int id = 0;
                int slot = SIMInfo.getSlotById(this, simInfo.mSimId);
                if (telephony != null){
                try {
                    if(slot >= 0 && telephony.isSimInsert(slot))
                        id = Telephony.SIMBackgroundRes[simInfo.mColor];
                    else
                        id = com.mediatek.internal.R.drawable.sim_background_locked;
                } catch(RemoteException e) {
                    id = com.mediatek.internal.R.drawable.sim_background_locked;
                }
                }else{
                    id = com.mediatek.internal.R.drawable.sim_background_locked;
                }
                
                mSimName.setBackgroundResource(id);
                mSimName.setText(simInfo.mDisplayName);
                mSimName.setPadding(3, 2, 3, 0);
            }
        }
    }

    private String generateSelection(int callType, long dateStart, long dateEnd) {
        StringBuilder builder = new StringBuilder();

        if(callType != FILTER_TYPE_ALL) {
            int t;
            switch(callType) {
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
                t = Calls.INCOMING_TYPE;
                break;
            }

            builder.append(Calls.TYPE + "=" + t);
        }

        if(builder.length() > 0) {
            builder.append(" and ");
        }
        
        if(dateStart < dateEnd) {
            builder.append("(" + Calls.DATE + ">" + dateStart+" and " + Calls.DATE + "<=" + dateEnd + ")");
        } else if(dateStart == dateEnd) {
            builder.append(Calls.DATE + "=" + dateStart);
        }
        
        log("generateSelection, selection : "+builder.toString());
        if(builder.length() > 0)
            return builder.toString();
        else
            return null;
    }

    private String generateSelection(int simId, int callType, long dateStart, long dateEnd) {
        StringBuilder builder = new StringBuilder();
        
        log("generateSelection, simId = "+simId+" callType = "+callType+" dateStart = "+dateStart+" dateEnd = "+dateEnd);
        
        if(simId != FILTER_SIM_ALL)
            builder.append(CallLog.Calls.SIM_ID + "="+simId);
        
        if(callType != FILTER_TYPE_ALL) {
            int t;
            switch(callType) {
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
                t = Calls.INCOMING_TYPE;
                break;
            }
            
            if (builder.length() > 0) {
                builder.append(" and ");
            }
            
            builder.append(Calls.TYPE + "=" + t);
        }

        if(builder.length() > 0) {
            if(dateStart < dateEnd) {
                builder.append(" and ");
                builder.append("(" + Calls.DATE + ">" + dateStart+" and " + Calls.DATE + "<=" + dateEnd + ")");
            } else if(dateStart == dateEnd) {
            	builder.append(" and ");
                builder.append(Calls.DATE + "=" + dateStart);
            }
        }
        log("generateSelection, selection : "+builder.toString());
        if(builder.length() > 0)
            return builder.toString();
        else
            return null;
    }
    /* Added by xingping.zheng end   */
    
    /**
     * Update user interface with details of given call.
     *
     * @param callUri Uri into {@link CallLog.Calls}
     */
    private void updateData(Uri callUri) {
        mIsSipCallLog = false;
        ContentResolver resolver = getContentResolver();
        Cursor callCursor = resolver.query(callUri, CALL_LOG_PROJECTION, null, null, null);
        try {
            if (callCursor != null && callCursor.moveToFirst()) {
                // Read call log specifics
                mNumber = callCursor.getString(NUMBER_COLUMN_INDEX);
                long date = callCursor.getLong(DATE_COLUMN_INDEX);
                long duration = callCursor.getLong(DURATION_COLUMN_INDEX);
                int callType = callCursor.getInt(CALL_TYPE_COLUMN_INDEX);
                int simId = callCursor.getInt(CALL_SIMID_COLUMN_INDEX);
                int vtCall = callCursor.getInt(CALL_VT_COLUMN_INDEX);  //for VT call
                Log.d(TAG, "call Detail, updateData, vtCall="+vtCall);
                
                // Pull out string in format [relative], [date]
                CharSequence dateClause = DateUtils.formatDateRange(this, date, date,
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                        DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR);
                mCallTime.setText(dateClause);

                // Set the duration
                if (callType == Calls.MISSED_TYPE) {
                    mCallDuration.setVisibility(View.GONE);
                } else {
                    mCallDuration.setVisibility(View.VISIBLE);
                    mCallDuration.setText(formatDuration(duration));
                }

                // Set the call type icon and caption
                String callText = null;
                switch (callType) {
                    case Calls.INCOMING_TYPE:
                        mCallType.setText(R.string.type_incoming);
                        callText = getString(R.string.callBack);
                        if (!com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
                            if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
                                if (vtCall == 1) {
                                    mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_incoming_vtcall);
                                } else {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_incoming_call);
                                }
                            }else {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_incoming_call);
                            }
                            break;
                        }
                        if (simId == Phone.GEMINI_SIM_1) {
                            if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
                                if (vtCall == 1) {
                                    mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_incoming_vtcall);
                                } else {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_incoming_call1);
                                }
                            } else {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_incoming_call1);
                            }
                        } else if (simId == Phone.GEMINI_SIM_2){
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_incoming_call2);
                        } else {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_incoming_call);
                        }
                        break;

                    case Calls.OUTGOING_TYPE:
                        mCallType.setText(R.string.type_outgoing);
                        callText = getString(R.string.callAgain);
                        if (!com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
                            if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
                                if (vtCall == 1) {
                                    mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_outgoing_vtcall);
                                } else {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_outgoing_call);
                                }
                            }else {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_outgoing_call);
                            }
                            break;
                        }
                        if (simId == Phone.GEMINI_SIM_1) {
                            if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
                                if (vtCall == 1) {
                                    mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_outgoing_vtcall);
                                } else {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_outgoing_call1);
                                }
                            } else {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_outgoing_call1);
                            }
                        } else if (simId == Phone.GEMINI_SIM_2){
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_outgoing_call2);
                        } else {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_outgoing_call);
                        }
                        
                        break;

                    case Calls.MISSED_TYPE:
                        mCallType.setText(R.string.type_missed);
                        callText = getString(R.string.returnCall);
                        if (!com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
                            if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
                                if (vtCall == 1) {
                                    mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_missed_vtcall);
                                } else {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_missed_call);
                                }
                            }else {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_missed_call);
                            }
                            break;
                        }
                        if (simId == Phone.GEMINI_SIM_1) {
                            if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
                                if (vtCall == 1) {
                                    mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_missed_vtcall);
                                } else {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_missed_call1);
                                }
                            } else {
                                mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_missed_call1);
                            }                            
                        } else if (simId == Phone.GEMINI_SIM_2) {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_missed_call2);
                        } else {
                            mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_missed_call);
                        }
                        
                        break;
                }

                if (mNumber.equals(CallerInfo.UNKNOWN_NUMBER) ||
                        mNumber.equals(CallerInfo.PRIVATE_NUMBER)) {
                    // List is empty, let the empty view show instead.
                    TextView emptyText = (TextView) findViewById(R.id.emptyText);
                    if (emptyText != null) {
                        emptyText.setText(mNumber.equals(CallerInfo.PRIVATE_NUMBER)
                                ? R.string.private_num : R.string.unknown);
                    }
                } else {
                    // Perform a reverse-phonebook lookup to find the PERSON_ID
                    String callLabel = null;
                    Uri personUri = null;
                    Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                            Uri.encode(mNumber));
                    Cursor phonesCursor = resolver.query(phoneUri, PHONES_PROJECTION, null, null, null);
                    try {
                        if (phonesCursor != null && phonesCursor.moveToFirst()) {
                            long personId = phonesCursor.getLong(COLUMN_INDEX_ID);
                            personUri = ContentUris.withAppendedId(
                                    Contacts.CONTENT_URI, personId);
                            callText = getString(R.string.recentCalls_callNumber,
                                    phonesCursor.getString(COLUMN_INDEX_NAME));
                            mNumber = PhoneNumberFormatUtilEx.formatNumber(
                                    phonesCursor.getString(COLUMN_INDEX_NUMBER));
                            callLabel = CommonDataKinds.Phone.getDisplayLabel(this,
                                    phonesCursor.getInt(COLUMN_INDEX_TYPE),
                                    phonesCursor.getString(COLUMN_INDEX_LABEL)).toString();
                        } else {
                            mNumber = PhoneNumberFormatUtilEx.formatNumber(mNumber);
                        }
                    } finally {
                        if (phonesCursor != null) phonesCursor.close();
                    }

                    // Build list of various available actions
                    List<ViewEntry> actions = new ArrayList<ViewEntry>();

                    // mtk80909, 2010-9-26 start
                    String lang = getResources().getConfiguration().locale.getLanguage();
                    Log.i(TAG, "language: " + lang);
                    /*  Attention:
                     *      The language code for both simplified and traditional Chinese
                     *      is "zh".
                     */
                    // mtk80909, 2010-9-26 end

                    Intent callIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                            Uri.fromParts("tel", mNumber, null));
                    if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
                        if (-1 == simId) { //MTK added for SIP call
                            mIsSipCallLog = true;
                            ViewEntry entry = new ViewEntry(android.R.drawable.sym_action_call, callText,
                                    callIntent);
                            entry.number = mNumber;
                            entry.label = callLabel;
                            actions.add(entry);

                        } else {
                    	// mtk80909 modified 2010-9-26
                    	String viewEntryString;
                    	if (lang.equals("zh")) {
                    		viewEntryString = getString(R.string.via, " " + getString(R.string.sim1)) + " "
                    				+ callText;
                    	} else {
                    		viewEntryString = callText + " " 
                    				+ getString(R.string.via, getString(R.string.sim1));
                    	}
                        ViewEntry entry = new ViewEntry(R.drawable.badge_action_call1, viewEntryString,
                            callIntent.putExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1));
                        entry.number = mNumber;
                        entry.label = callLabel;
                        actions.add(entry);
                    
                    	String viewEntryString2;
                    	if (lang.equals("zh")) {
                    		viewEntryString2 = getString(R.string.via, " " + getString(R.string.sim2)) + " "
                    				+ callText;
                    	} else {
                    		viewEntryString2 = callText + " " 
                    				+ getString(R.string.via, getString(R.string.sim2));
                    	}
                        ViewEntry entrySim2 = new ViewEntry(R.drawable.badge_action_call2, viewEntryString2,
                            new Intent(callIntent).putExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_2));
                        entrySim2.number = mNumber;
                        entrySim2.label = callLabel;
                        actions.add(entrySim2);

                        if (true == FeatureOption.MTK_VT3G324M_SUPPORT){
                            String viewEntryString3;
                            if (lang.equals("zh")) {
                                viewEntryString3 = getString(R.string.via, " " + getString(R.string.video_call)) + " "
                                        + callText;
                            } else {
                                viewEntryString3 = callText + " " 
                                        + getString(R.string.via, getString(R.string.video_call));
                            }
                            ViewEntry entryVT = new ViewEntry(R.drawable.badge_action_vtcall, viewEntryString3,
                                new Intent(callIntent).putExtra("is_vt_call", true));
                            entryVT.number = mNumber;
                            entryVT.label = callLabel;
                            actions.add(entryVT);

                        }
                         }
                    } else {
                        ViewEntry entry = new ViewEntry(android.R.drawable.sym_action_call, callText,
                                callIntent);
                        entry.number = mNumber;
                        entry.label = callLabel;
                        actions.add(entry);
                        if (true == FeatureOption.MTK_VT3G324M_SUPPORT){

                            ViewEntry entryVT = new ViewEntry(R.drawable.badge_action_vtcall, callText,
                                    new Intent(callIntent).putExtra("is_vt_call", true)); 
                            entryVT.number = mNumber;
                            entryVT.label = callLabel;
                            actions.add(entryVT);
                        }
                    }
                    Intent smsIntent = new Intent(Intent.ACTION_SENDTO,
                            Uri.fromParts("sms", mNumber, null));
                    actions.add(new ViewEntry(R.drawable.sym_action_sms,
                            getString(R.string.menu_sendTextMessage), smsIntent));

                    // Let user view contact details if they exist, otherwise add option
                    // to create new contact from this number.
                    if (personUri != null) {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW, personUri);
                        StickyTabs.setTab(viewIntent, getIntent());
                        actions.add(new ViewEntry(R.drawable.sym_action_view_contact,
                                getString(R.string.menu_viewContact), viewIntent));
                    } else {
                        Intent createIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                        createIntent.setType(Contacts.CONTENT_ITEM_TYPE);
                        //if (!mIsSipCallLog) { //not SIP call, add phone number
                        createIntent.putExtra(Insert.PHONE, mNumber);
                       // } else {        // SIP call, add SIP address
                       //     createIntent.putExtra(Insert.SIP_ADDRESS, mNumber);
                       // }
                        actions.add(new ViewEntry(R.drawable.sym_action_add,
                                getString(R.string.recentCalls_addToContact), createIntent));
                    }

                    ViewAdapter adapter = new ViewAdapter(this, actions);
                    setListAdapter(adapter);
                }
            } else {
                // Something went wrong reading in our primary data, so we're going to
                // bail out and show error to users.
                Toast.makeText(this, R.string.toast_call_detail_error,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        } finally {
            if (callCursor != null) {
                callCursor.close();
            }
        }
    }

    private String formatDuration(long elapsedSeconds) {
        long minutes = 0;
        long seconds = 0;

        if (elapsedSeconds >= 60) {
            minutes = elapsedSeconds / 60;
            elapsedSeconds -= minutes * 60;
        }
        seconds = elapsedSeconds;

        return getString(R.string.callDetailsDurationFormat, minutes, seconds);
    }

    /* Added by xingping.zheng start */
    private class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
            // TODO Auto-generated constructor stub
        }
        
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            
            List<ViewEntry> actions = (List<ViewEntry>)cookie;
            
            switch(token) {
                case QUERY_TOKEN_0: {
                    log("onQueryComplete : QUERY_TOKEN_0");
                    CallDetailActivity.this.generateCallLogActions(actions, cursor);
                    if (isSpecialNumber(mNumber)) {
                        CallDetailAdapter adapter = new CallDetailAdapter(CallDetailActivity.this, actions);
                        CallDetailActivity.this.setListAdapter(adapter);
                    } else {
                        if(FeatureOption.MTK_GEMINI_SUPPORT) {
                            CallDetailActivity.this.queryNumberAssociation(actions);
                        } else {
                            CallDetailActivity.this.generateCommonActions(actions, cursor);
                            CallDetailAdapter adapter = new CallDetailAdapter(CallDetailActivity.this, actions);
                            CallDetailActivity.this.setListAdapter(adapter);
                        }
                    }
                }
                break;
                
                case QUERY_TOKEN_1: {
                    log("onQueryComplete : QUERY_TOKEN_1");
                    CallDetailActivity.this.generateCommonActions(actions, cursor);
                    CallDetailAdapter adapter = new CallDetailAdapter(CallDetailActivity.this, actions);
                    CallDetailActivity.this.setListAdapter(adapter);
                }
                break;
            }
        } 
    }
    /* Added by xingping.zheng end   */
    
    static final class ViewEntry {
        
        public final static int VIEW_ENTRY_TYPE_HEADER = 0;
        public final static int VIEW_ENTRY_TYPE_INFO   = 1;
        public final static int VIEW_ENTRY_TYPE_ACTION = 2;
        
        public int icon = -1;
        public String text = null;
        public Intent intent = null;
        public String label = null;
        public String number = null;

        /* Added by xingping.zheng start */
        public String date;
        public String time;
        public String duration;
        public String simName;
        public int type;
        public int simColor;
        public int callType;
        public boolean enabled;
        /* Added by xingping.zheng end   */
        
        /* for VIEW_ENTRY_TYPE_ACTION */
        public ViewEntry(int icon, String text, Intent intent) {
            this(icon, text, intent, null, 0);
        }
        
        /* for VIEW_ENTRY_TYPE_ACTION */
        public ViewEntry(int icon, String text, Intent intent, String simName, int simColor) {
            this(icon, text, intent, simName, simColor, true);
        }
        
        public ViewEntry(int icon, String text, Intent intent, String simName, int simColor, boolean bEnabled) {
            this.icon = icon;
            this.text = text;
            this.intent = intent;
            this.type = VIEW_ENTRY_TYPE_ACTION;
            this.simName = simName;
            this.simColor = simColor;
            this.enabled = bEnabled;
        }
        
        /* for VIEW_ENTRY_TYPE_HEADER */
        public ViewEntry(int callType, String date) {
            this.date = date;
            this.type = VIEW_ENTRY_TYPE_HEADER;
            
            Log.d(TAG, "ViewEntry callType = "+callType);
            
            switch(callType) {
                case CallLog.Calls.INCOMING_TYPE:
                    this.icon = R.drawable.contact_calllog_dialerseach_incoming_call;
                    this.callType = R.string.actionIncomingCall;
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    this.icon = R.drawable.contact_calllog_dialerseach_outing_call;
                    this.callType = R.string.outgoing_calls;
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    this.icon = R.drawable.contact_calllog_dialerseach_missing_call;
                    this.callType = R.string.missed_calls;
                    break;
            }
            this.enabled = true;
        }
        
        /* for VIEW_ENTRY_TYPE_INFO */
        public ViewEntry(String duration, String time) {
            Log.d(TAG, "ViewEntry duration = "+duration+" time = "+time);
            this.duration = duration;
            this.time = time;
            this.type = VIEW_ENTRY_TYPE_INFO;
            this.enabled = true;
        }
        
        public int getType() {
            return type;
        }
    }

    /* Added by xingping.zheng start */
    static final class CallDetailAdapter extends BaseAdapter {
        
        private final List<ViewEntry> mActions;
        
        private final LayoutInflater mInflater;
        
        private Context mContext;
        
        private final static int VIEW_TAG_KEY = -1;
        
        public CallDetailAdapter(Context context, List<ViewEntry> actions) {
            mActions = actions;
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        public int getCount() {
            Log.d(TAG, "getCount = "+mActions.size());
            return mActions.size();
        }
        
        public Object getItem(int position) {
            return mActions.get(position);
        }
        
        public long getItemId(int position) {
            return position;
        }
        
        public int getViewTypeCount() {
            return 3;
        }
        
        public int getItemViewType(int position) {
            return mActions.get(position).getType();
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            
            ViewHolder viewHolder = null;
            
            ViewEntry entry = mActions.get(position);
            View view = convertView;
            
            int type = entry.getType();
            
            Log.d(TAG, "getView : position = "+position);
            Log.d(TAG, "entry.icon = "+entry.icon+" entry.date = "+entry.date);
            
            if(view == null) {
                viewHolder = new ViewHolder();
                switch(type) {
                    case ViewEntry.VIEW_ENTRY_TYPE_HEADER: {
                        view = mInflater.inflate(R.layout.call_detail_list_item_header, parent, false);
                        
                        viewHolder.icon = (ImageView)view.findViewById(R.id.icon);
                        viewHolder.date = (TextView)view.findViewById(R.id.date);
                        viewHolder.type = (TextView)view.findViewById(R.id.type);
                    }
                    break;

                    case ViewEntry.VIEW_ENTRY_TYPE_INFO: {
                        view = mInflater.inflate(R.layout.call_detail_list_item_info, parent, false);
                        
                        viewHolder.duration = (TextView)view.findViewById(R.id.duration);
                        viewHolder.time = (TextView)view.findViewById(R.id.time);
                    }
                    break;
                        
                    case ViewEntry.VIEW_ENTRY_TYPE_ACTION: {
                        view = mInflater.inflate(R.layout.call_detail_list_item_action, parent, false);
                        
                        viewHolder.text = (TextView)view.findViewById(R.id.text);
                        viewHolder.icon = (ImageView)view.findViewById(R.id.icon);
                        viewHolder.footer = (ImageView)view.findViewById(R.id.footer);
                        viewHolder.simName = (TextView)view.findViewById(R.id.simName);
                    }
                    break;
                }

                view.setTag(VIEW_TAG_KEY, viewHolder);
            }
            
            view.setTag(entry);
            switch(type) {
                case ViewEntry.VIEW_ENTRY_TYPE_HEADER:
                    bindHeaderView(view, mContext, entry);
                    break;
                case ViewEntry.VIEW_ENTRY_TYPE_INFO:
                    bindInfoView(view, mContext, entry);
                    break;
                case ViewEntry.VIEW_ENTRY_TYPE_ACTION:
                    bindActionView(position, view, mContext, entry);
            }

            viewHolder = (ViewHolder)view.getTag(VIEW_TAG_KEY);

            Log.d(TAG, "getView : position:" + position + " entry.enabled = " + entry.enabled);
            if (null != viewHolder && null != viewHolder.text) {
                if (!entry.enabled) {
                    viewHolder.text.setTextColor(0xff7e7e7e);
                } else {
                    viewHolder.text.setTextColor(0xff000000);
                }
            }

            return view;
        }
        
        @Override
        public boolean isEnabled(int position) {
            // TODO Auto-generated method stub
            boolean enabled = true;
            ViewEntry entry = mActions.get(position);
            if (null != entry) {
                enabled = entry.enabled; 
            }
            
            Log.d(TAG, "isEnabled :" + enabled);
            return enabled;
        }

        protected void bindHeaderView(View view, Context context, ViewEntry entry) {
            ViewHolder viewHolder = (ViewHolder)view.getTag(VIEW_TAG_KEY);
            
            viewHolder.icon.setBackgroundResource(entry.icon);
            viewHolder.date.setText(entry.date);
            viewHolder.type.setText(entry.callType);
        }
        
        protected void bindInfoView(View view, Context context, ViewEntry entry) {
            ViewHolder viewHolder = (ViewHolder)view.getTag(VIEW_TAG_KEY);
            
            viewHolder.duration.setText(entry.duration);
            viewHolder.time.setText(entry.time);
        }
        
        protected void bindActionView(int position, View view, Context context, ViewEntry entry) {
            ViewHolder viewHolder = (ViewHolder)view.getTag(VIEW_TAG_KEY);
            
            viewHolder.text.setText(entry.text);
            viewHolder.icon.setBackgroundResource(entry.icon);
            
            if(position == mActions.size()-1)
                viewHolder.footer.setVisibility(View.VISIBLE);
            else
                viewHolder.footer.setVisibility(View.INVISIBLE);
            
            if(!TextUtils.isEmpty(entry.simName)) {
                viewHolder.simName.setVisibility(View.VISIBLE);
                viewHolder.simName.setText(entry.simName);
                if(entry.simColor == com.mediatek.internal.R.drawable.sim_background_sip) {
                    viewHolder.simName.setBackgroundResource(entry.simColor);
                } else {
                    if(entry.simColor >= 0)
                        viewHolder.simName.setBackgroundResource(Telephony.SIMBackgroundRes[entry.simColor]);
                }
            } else
                viewHolder.simName.setVisibility(View.INVISIBLE);
        }
        
        private class ViewHolder {
            TextView  date;
            TextView  type;
            
            TextView  time;
            TextView  duration;
            
            TextView  text;
            ImageView icon;
            ImageView footer;
            
            TextView simName;
        }
    }
    
    protected void log(String msg) {
        Log.d(TAG, msg);
    }
    
    protected void dimissAllDialogs() {
        if(mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
        
        if(mTurnOnSipDialog != null && mTurnOnSipDialog.isShowing()) {
            mTurnOnSipDialog.dismiss();
            mTurnOnSipDialog = null;
        }
    }
    
    /* Added by xingping.zheng end   */
    
    static final class ViewAdapter extends BaseAdapter {

        private final List<ViewEntry> mActions;

        private final LayoutInflater mInflater;

        public ViewAdapter(Context context, List<ViewEntry> actions) {
            mActions = actions;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return mActions.size();
        }

        public Object getItem(int position) {
            return mActions.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // Make sure we have a valid convertView to start with
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.call_detail_list_item, parent, false);
            }

            // Fill action with icon and text.
            ViewEntry entry = mActions.get(position);
            convertView.setTag(entry);
            boolean simRadioOn = true;
            boolean simEnable = true;
            boolean simIdle = true;
            boolean sim1Idle = true;
            boolean sim2Idle = true;
            boolean isVTIdle = true;
            boolean simReady = true;
            if(!mIsSipCallLog) {  //not SIP call
    
            if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {  // for VT IT, last code
            	
                try {
                ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                if (iTel != null){
                    isVTIdle = iTel.isVTIdle();
                }
              } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
            Log.d(TAG, "VT call, isVTIdle="+isVTIdle);

            boolean isEmergencyNumber = PhoneNumberUtils.isEmergencyNumber(entry.number);
            Log.i(TAG, "isEmergencyNumber ="+isEmergencyNumber);

	        boolean sim1Ready = (TelephonyManager.SIM_STATE_READY 
	        		== TelephonyManager.getDefault()
	        		.getSimStateGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1));
	        boolean sim2Ready = (TelephonyManager.SIM_STATE_READY 
	        		== TelephonyManager.getDefault()
	        		.getSimStateGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2));
            if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
                try {
                    ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                        .getService(Context.TELEPHONY_SERVICE));
                    if (iTel != null){
                    sim1Idle = iTel.isIdleGemini(Phone.GEMINI_SIM_1);
                    sim2Idle = iTel.isIdleGemini(Phone.GEMINI_SIM_2);
                    //VT support
                    if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
                    if (position == 0) {
                        simRadioOn = iTel.isRadioOnGemini(Phone.GEMINI_SIM_1);
                            if (isEmergencyNumber) {
                                simEnable = (sim1Idle && sim2Idle) ? true : sim2Idle;
                            } else {
                            simEnable = isVTIdle && ((sim1Idle && sim2Idle) ? true : sim2Idle);
                            }
                        simReady = (sim1Ready && sim2Ready) ? true : sim1Ready;
                            Log.i(TAG, "sim 1 radio on " + simRadioOn+"; simEnable="+simEnable+";simReady"+simReady);
                    } else if (position == 1) {
                        simRadioOn = iTel.isRadioOnGemini(Phone.GEMINI_SIM_2);
                            if (isEmergencyNumber) {
                                simEnable = (sim1Idle && sim2Idle) ? true : sim1Idle;
                            } else {
                            simEnable = isVTIdle && ((sim1Idle && sim2Idle) ? true : sim1Idle);
                            }
                        simReady = (sim1Ready && sim2Ready) ? true : sim2Ready;
                            Log.i(TAG, "sim 2 radio on " + simRadioOn+"; simEnable="+simEnable+";simReady"+simReady);
                        } else if (position == 2) {
                            simRadioOn = iTel.isRadioOnGemini(Phone.GEMINI_SIM_1);
                            if (isEmergencyNumber) {
                                simEnable = (sim1Idle && sim2Idle) ? true : sim2Idle;
                            } else {
                                simEnable = isVTIdle && sim1Idle && sim2Idle;
                            }  
                            simReady = (sim1Ready && sim2Ready) ? true : sim1Ready;
                            Log.i(TAG, "sim 1 radio on " + simRadioOn+"; simEnable="+simEnable+";simReady"+simReady);
                        }
                    } else {
                    if (position == 0) {
                        simRadioOn = iTel.isRadioOnGemini(Phone.GEMINI_SIM_1);
                             simEnable = (sim1Idle && sim2Idle) ? true : sim2Idle;
                             simReady = (sim1Ready && sim2Ready) ? true : sim1Ready;
                             Log.i(TAG, "sim 1 radio on " + simRadioOn+"; simEnable="+simEnable+";simReady"+simReady);
                    } else if (position == 1) {
                        simRadioOn = iTel.isRadioOnGemini(Phone.GEMINI_SIM_2);
                             simEnable = (sim1Idle && sim2Idle) ? true : sim1Idle;
                             simReady = (sim1Ready && sim2Ready) ? true : sim2Ready;
                             Log.i(TAG, "sim 2 radio on " + simRadioOn+"; simEnable="+simEnable+";simReady"+simReady);
                         } 
                    }
                    }
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }else {
                try {
                    ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                        .getService(Context.TELEPHONY_SERVICE));
                    if (iTel != null){
                    simIdle = iTel.isIdle();
                    if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
                        if (position == 0) {
                            simRadioOn = iTel.isRadioOn();
                            Log.i(TAG, "sim radio on " + simRadioOn);
                            simEnable = isVTIdle;
                            Log.i(TAG, "Voice call enable= " + simEnable);
                        } else if (position == 1) {
                            simRadioOn = iTel.isRadioOn();
                            Log.i(TAG, "sim radio on " + simRadioOn);
                            simEnable = simIdle;
                            Log.i(TAG, "VT call enable= " + simEnable);
                        }
                    } else {
                        if (position == 0) {
                            simRadioOn = iTel.isRadioOn();
                            Log.i(TAG, "sim radio on " + simRadioOn);
                        }
                    }
                    }
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }

            //mtk80736 enable the list item if needed
            if (isEmergencyNumber) {
            convertView.setEnabled(isEmergencyNumber && simEnable);            
            if (!(isEmergencyNumber || simRadioOn) || !simEnable) {
            	//TODO need change the background color
            	entry.intent = null;
            }
            } else {
            	convertView.setEnabled( simRadioOn && simReady && simEnable);   
            	 if (!simRadioOn || !simReady || !simEnable) {
                 	//TODO need change the background color
                 	entry.intent = null;
                 }
            }
                
//            convertView.setEnabled((isEmergencyNumber || simRadioOn || simReady) && simEnable);   
//            if (!(isEmergencyNumber || simRadioOn) || !simEnable) {
//            	//TODO need change the background color
//            	entry.intent = null;
//            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            TextView text = (TextView) convertView.findViewById(android.R.id.text1);
           /* if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
                if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
                    if (position == 2) {
                        text.setEnabled(simRadioOn && simEnable && simReady);
                    } else {
            text.setEnabled((isEmergencyNumber || simRadioOn) && simEnable);
                    }

                } else {
                    if (position == 1) {
                        text.setEnabled(simRadioOn && simEnable);
                    } else {
                        text.setEnabled((isEmergencyNumber || simRadioOn) && simEnable);
                    }
                }
            } else {
           */
            if (isEmergencyNumber) {
            text.setEnabled(isEmergencyNumber && simEnable);
            } else {
            	text.setEnabled( simRadioOn && simReady && simEnable);
            }
            //}
            icon.setImageResource(entry.icon);
            text.setText(entry.text);
                }else {  //Is SIP call
                  //mIsSipCallLog = false;
              
                  ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
                  TextView text = (TextView) convertView.findViewById(android.R.id.text1);
                  icon.setImageResource(entry.icon);
                  text.setText(entry.text);

                }

            View line2 = convertView.findViewById(R.id.line2);
            boolean numberEmpty = TextUtils.isEmpty(entry.number);
            boolean labelEmpty = TextUtils.isEmpty(entry.label) || numberEmpty;
            if (labelEmpty && numberEmpty) {
                line2.setVisibility(View.GONE);
            } else {
                line2.setVisibility(View.VISIBLE);

                TextView label = (TextView) convertView.findViewById(R.id.label);
                if (labelEmpty) {
                    label.setVisibility(View.GONE);
                } else {
                    label.setText(entry.label);
                    label.setVisibility(View.VISIBLE);
                }

                TextView number = (TextView) convertView.findViewById(R.id.number);
                number.setText(entry.number);
            }

            return convertView;
        }
    }

    public void onItemClick(AdapterView parent, View view, int position, long id) {
        // Handle passing action off to correct handler.
        if (view.getTag() instanceof ViewEntry) {
            ViewEntry entry = (ViewEntry) view.getTag();
            if (entry.intent != null) {
                String action = entry.intent.getAction();
                if (Intent.ACTION_CALL_PRIVILEGED.equals(action)) {
                    StickyTabs.saveTab(this, getIntent());
                    // sendBroadcast(entry.intent);
                    boolean bVTCall = entry.intent.getBooleanExtra("is_vt_call", false);
                    int iCallType = ContactsUtils.DIAL_TYPE_VOICE;
                    if (bVTCall) {
                        iCallType = ContactsUtils.DIAL_TYPE_VIDEO;
                    } else if (ContactsUtils.CALL_TYPE_SIP == mlCallType) {
                        iCallType = ContactsUtils.DIAL_TYPE_SIP;
                    }
                    log("mNumber:" + mNumber + " CallType:" + iCallType);
                    ContactsUtils.dial(CallDetailActivity.this, mNumber, iCallType,
                            ContactsUtils.CALL_TYPE_NONE, null);
                } else if(ACTION_CALL_VIA.equals(action)) {
                    //mtk71029 for CR: ALPS00044402
                    //if the mDialog  is showing, we do nothing.
                    if(mDialog != null && mDialog.isShowing()){
                          return ;
                    }
                    mDialog = ContactsUtils.createSimSelectionDialog(this, 
                                                                     getResources().getString(R.string.title_call_via), 
                                                                     generateSuggestedSim(),
                                                                     ContactsUtils.createItemHolder(this, true),
                                                                     new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            AlertDialog alertDialog = (AlertDialog) dialog;
                            int slot = ((Integer)alertDialog.getListView().getAdapter().getItem(which)).intValue();
                            if(slot == (int)Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
                                int enabled = Settings.System.getInt(getContentResolver(), Settings.System.ENABLE_INTERNET_CALL, 0);
                                if(enabled==1) {
                                    Intent intent = ContactsUtils.generateDialIntent(true, 0, mNumber);
                                    CallDetailActivity.this.sendBroadcast(intent);
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CallDetailActivity.this);
                                    builder.setTitle(R.string.reminder)
                                           .setMessage(R.string.enable_sip_dialog_message)
                                           .setNegativeButton(android.R.string.no, (DialogInterface.OnClickListener)null)
                                           .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int which) {
                                                // TODO Auto-generated method stub
                                                Intent intent = new Intent();
                                                intent.setClassName("com.android.phone", "com.android.phone.SipCallSetting");
                                                startActivity(intent);
                                            }
                                        });
                                    mTurnOnSipDialog = builder.create();
                                    if(mDialog != null && mDialog.isShowing()) {
                                        mDialog.dismiss();
                                        mDialog = null;
                                    }
                                    mTurnOnSipDialog.show();
                                }
                            } else {
                                mSlot = slot;
                                mIsVTCall = false;
                                int result = mCellConnMgr.handleCellConn(mSlot, CellConnMgr.REQUEST_TYPE_ROAMING);
                                log("dial complete, slot = "+slot);
                            }
                        }

                    });
                    mDialog.show();
                } else if("out_going_call_to_phone_app".equals(action)) {
                    if(entry.intent.getBooleanExtra("sip_preferred", false)) {
                        int enabled = Settings.System.getInt(getContentResolver(), Settings.System.ENABLE_INTERNET_CALL, 0);
                        if(enabled==1) {
                            Intent intent = ContactsUtils.generateDialIntent(true, 0, mNumber);
                            CallDetailActivity.this.sendBroadcast(intent);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(CallDetailActivity.this);
                            builder.setTitle(R.string.reminder)
                                   .setMessage(R.string.enable_sip_dialog_message)
                                   .setNegativeButton(android.R.string.no, (DialogInterface.OnClickListener)null)
                                   .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub
                                        Intent intent = new Intent();
                                        intent.setClassName("com.android.phone", "com.android.phone.SipCallSetting");
                                        startActivity(intent);
                                    }
                                });
                            mTurnOnSipDialog = builder.create();
                            if(mDialog != null && mDialog.isShowing()) {
                                mDialog.dismiss();
                                mDialog = null;
                            }
                            mTurnOnSipDialog.show();
                        }
                    } else {
                        mSlot = SIMInfo.getSlotById(this, mSimId);
                        mIsVTCall = entry.intent.getBooleanExtra("is_vt_call", false);
                        int reqType = CellConnMgr.REQUEST_TYPE_ROAMING;
                        if(mIsVTCall) {
                            if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
                                mSlot = ContactsUtils.get3GCapabilitySIM();
                            } else { 
                                mSlot = Phone.GEMINI_SIM_1;
                            }
                            reqType |= CellConnMgr.FLAG_REQUEST_NOPREFER;
                        }
                        int result = mCellConnMgr.handleCellConn(mSlot, reqType);
                        log("out_going_call_to_phone_app, result = "+result);
                    }
                } else if(action.equals(Intent.ACTION_INSERT_OR_EDIT)) {
                    String message = getResources().getString(R.string.add_contact_dialog_message, mNumber);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                                                 .setTitle(mNumber)
                                                                 .setMessage(message);
                    
                    AlertDialog dialog = builder.create();
                    
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.add_contact_dialog_existing), new DialogInterface.OnClickListener() {
                        
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                            intent.setType(Contacts.CONTENT_ITEM_TYPE);
                            intent.putExtra(Insert.PHONE, mNumber);
                            startActivity(intent);
                        }
                    });
                    
                    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.add_contact_dialog_new), new DialogInterface.OnClickListener() {
                        
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                            intent.putExtra(Insert.PHONE, mNumber);
                            startActivity(intent);
                        }
                        
                    });
                    
                    mDialog = dialog;
                    dialog.show();
                } else
                    startActivity(entry.intent);
            } else if( entry.type == ViewEntry.VIEW_ENTRY_TYPE_ACTION ){
                /* tap this option will display another dialog for SIM selection */
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.reminder)
                       .setMessage(R.string.turn_on_3g_service_message)
                       .setNegativeButton(android.R.string.no,(DialogInterface.OnClickListener) null)
                       .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                Intent intent = new Intent();
                                intent.setClassName("com.android.phone", "com.android.phone.Modem3GCapabilitySwitch");
                                startActivity(intent);
                            }
                        });
                mTurnOn3GServiceDialog = builder.create();
                mTurnOn3GServiceDialog.show();
                return;
//            	StickyTabs.saveTab(this, getIntent());
//            	int iCallType = ContactsUtils.DIAL_TYPE_VIDEO;
//    			ContactsUtils.dial(CallDetailActivity.this, mNumber, iCallType,
//    					Phone.GEMINI_SIM_1, null);
            }
        }
    }

    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData,
            boolean globalSearch) {
        if (globalSearch) {
            super.startSearch(initialQuery, selectInitialQuery, appSearchData, globalSearch);
        } else {
            ContactsSearchManager.startSearch(this, initialQuery);
        }
    }

    void setSimIndicatorVisibility(boolean visible) {
        if(visible)
            mStatusBarMgr.showSIMIndicator(getComponentName(), Settings.System.VOICE_CALL_SIM_SETTING);
        else
            mStatusBarMgr.hideSIMIndicator(getComponentName());
    }
    
    private class SimIndicatorBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                Log.d(TAG, "SimIndicatorBroadcastReceiver, onReceive() ");
                if (action.equals(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED)) {
                    Log.d(TAG, "SimIndicatorBroadcastReceiver, onReceive(), mShowSimIndicator= " + mShowSimIndicator);
                    if (mShowSimIndicator) {
                        setSimIndicatorVisibility(true);
                    }
                }
            }
        }
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
    
    boolean isVoiceMail(String number) {
        boolean bVoiceMail = false;
        if (null == number) {
            return bVoiceMail;
        }
        int slotId = SIMInfo.getSlotById(CallDetailActivity.this, mSimId);
        bVoiceMail = ((number.equals(mVoiceMailNumber) && (slotId == 0))|| (number.equals(mVoiceMailNumber2) && (slotId == 1)));
        return bVoiceMail;
    }
}
