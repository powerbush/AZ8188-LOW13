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

/**
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

package com.android.phone;

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.phone.sip.SipProfileDb;
import com.android.phone.sip.SipSettings;
import com.android.phone.sip.SipSharedPreferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.mediatek.CellConnService.CellConnMgr;
import com.mediatek.featureoption.FeatureOption;
import java.util.List;

/**
 * SipCallOptionHandler select the sip phone based on the call option.
 */
public class SipCallOptionHandler extends Activity implements
        DialogInterface.OnClickListener, DialogInterface.OnCancelListener,
        CompoundButton.OnCheckedChangeListener {

    static final String TAG = "SipCallOptionHandler";
    static final int DIALOG_SELECT_PHONE_TYPE = 0;
    static final int DIALOG_SELECT_OUTGOING_SIP_PHONE = 1;
    static final int DIALOG_START_SIP_SETTINGS = 2;
    static final int DIALOG_NO_INTERNET_ERROR = 3;
    static final int DIALOG_NO_VOIP = 4;
    static final int DIALOG_NOT_SUPPORT_SIP_VT = 5;
    static final int DIALOG_SIZE = 6;

    private Intent mIntent;
    private List<SipProfile> mProfileList;
    private String mCallOption;
    private String mNumber;
    private SipSharedPreferences mSipSharedPreferences;
    private SipProfileDb mSipProfileDb;
    private Dialog[] mDialogs = new Dialog[DIALOG_SIZE];
    private SipProfile mOutgoingSipProfile;
    private TextView mUnsetPriamryHint;
    private boolean mUseSipPhone = false;
    private boolean mMakePrimary = false;
    /* added by xingping.zheng start */
    private CellConnMgr mCellConnMgr;
    /* added by xingping.zheng end   */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIntent = (Intent)getIntent().getParcelableExtra
            (OutgoingCallBroadcaster.EXTRA_NEW_CALL_INTENT);
        if (mIntent == null) {
            finish();
            return;
        }
        /* added by xingping.zheng start */
        mCellConnMgr = new CellConnMgr();
        mCellConnMgr.register(this);
        /* added by xingping.zheng end   */
        // set this flag so this activity will stay in front of the keyguard
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        
        
        if(mIntent.getBooleanExtra("is_vt_call", false)) {
            startActivity(mIntent);
            finish();
            Log.d(TAG, "IS VT CALL, SEND INTENT DIRECTLY");
            return;
        }

        // If we're trying to make a SIP call, return a SipPhone if one is
        // available.
        //
        // - If it's a sip: URI, this is definitely a SIP call, regardless
        //   of whether the data is a SIP address or a regular phone
        //   number.
        //
        // - If this is a tel: URI but the data contains an "@" character
        //   (see PhoneNumberUtils.isUriNumber()) we consider that to be a
        //   SIP number too.
        //
        // TODO: Eventually we may want to disallow that latter case
        //       (e.g. "tel:foo@example.com").
        //
        // TODO: We should also consider moving this logic into the
        //       CallManager, where it could be made more generic.
        //       (For example, each "telephony provider" could be allowed
        //       to register the URI scheme(s) that it can handle, and the
        //       CallManager would then find the best match for every
        //       outgoing call.)

        boolean voipSupported = SipManager.isVoipSupported(this);
        mSipProfileDb = new SipProfileDb(this);
        mSipSharedPreferences = new SipSharedPreferences(this);
        mCallOption = mSipSharedPreferences.getSipCallOption();
        Log.v(TAG, "Call option is " + mCallOption);
        Uri uri = mIntent.getData();
        String scheme = uri.getScheme();
        mNumber = PhoneNumberUtils.getNumberFromIntent(mIntent, this);
        
        //Add protect for VT
        if ((PhoneNumberUtils.isUriNumber(mNumber) && mIntent.getBooleanExtra("is_vt_call", false))
             || (mIntent.getBooleanExtra("is_vt_call", false) && mCallOption.equals(Settings.System.SIP_ALWAYS)))
        {
        	showDialog(DIALOG_NOT_SUPPORT_SIP_VT);
        	//finish();
        	return ;
        }
        
        boolean isInCellNetwork = PhoneApp.getInstance().phoneMgr.isRadioOn();
        boolean isKnownCallScheme= "tel".equals(scheme) || "sip".equals(scheme);
        boolean isRegularCall =
                "tel".equals(scheme) && !PhoneNumberUtils.isUriNumber(mNumber);
        Log.d(TAG, "isInCellNetwork = "+isInCellNetwork+" isRegularCall = "+isRegularCall+" isKnownCallScheme = "+isKnownCallScheme+" voipSupported = "+voipSupported);
        //For performance tune: Put this extra to tell IncallScreen this intent is from us
        mIntent.putExtra("launch_from_SipCallHandlerEx", true);

        // Bypass the handler if the call scheme is not sip or tel.
        if (!isKnownCallScheme) {
            setResultAndFinish();
            return;
        }

        // Check if VoIP feature is supported.
        if (!voipSupported) {
            if (!isRegularCall) {
                showDialog(DIALOG_NO_VOIP);
            } else {
                setResultAndFinish();
            }
            return;
        }
        
        //First check the internet call is enable/disable
        if (mCallOption.equals(Settings.System.SIP_ALWAYS) || (!isRegularCall) && mCallOption.equals(Settings.System.SIP_ADDRESS_ONLY)) {
            if (!isEnableInternetCall()) {
                this.showOpenInternet();
                return;
            }
        }

        // Since we are not sure if anyone has touched the number during
        // the NEW_OUTGOING_CALL broadcast, we just check if the provider
        // put their gateway information in the intent. If so, it means
        // someone has changed the destination number. We then make the
        // call via the default pstn network. However, if one just alters
        // the destination directly, then we still let it go through the
        // Internet call option process.
        if (!PhoneUtils.hasPhoneProviderExtras(mIntent)) {
            if (!isNetworkConnected()) {
                if (!isRegularCall) {
                    if (!this.isEnableInternetCall()) {
                        this.showOpenInternet();
                        return;
                    }
                    showDialog(DIALOG_NO_INTERNET_ERROR);
                    return;
                }
            } else {
                if (mCallOption.equals(Settings.System.SIP_ASK_ME_EACH_TIME)
                        && isRegularCall && isInCellNetwork) {
                    showDialog(DIALOG_SELECT_PHONE_TYPE);
                    return;
                }
                if (!mCallOption.equals(Settings.System.SIP_ADDRESS_ONLY)
                        || !isRegularCall) {
                    mUseSipPhone = true;
                }
            }
        }

        if (mUseSipPhone) {
            // If there is no sip profile and it is a regular call, then we
            // should use pstn network instead.
            if ((mSipProfileDb.getProfilesCount() > 0) || !isRegularCall) {
                startGetPrimarySipPhoneThread();
                return;
            } else {
                mUseSipPhone = false;
            }
        }
        Log.v(TAG, "mUseSipPhone = "+mUseSipPhone);
        setResultAndFinish();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mCellConnMgr.unregister();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isFinishing()) return;
        for (Dialog dialog : mDialogs) {
            if (dialog != null) dialog.dismiss();
        }
        finish();
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case DIALOG_SELECT_PHONE_TYPE:
            dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.pick_outgoing_call_phone_type)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setSingleChoiceItems(R.array.phone_type_values, -1, this)
                    .setNegativeButton(android.R.string.cancel, this)
                    .setOnCancelListener(this)
                    .create();
            break;
        case DIALOG_SELECT_OUTGOING_SIP_PHONE:
            dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.pick_outgoing_sip_phone)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setSingleChoiceItems(getProfileNameArray(), -1, this)
                    .setNegativeButton(android.R.string.cancel, this)
                    .setOnCancelListener(this)
                    .create();
            addMakeDefaultCheckBox(dialog);
            break;
        case DIALOG_START_SIP_SETTINGS:
            dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.no_sip_account_found_title)
                    .setMessage(R.string.no_sip_account_found)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(R.string.sip_menu_add, this)
                    .setNegativeButton(android.R.string.cancel, this)
                    .setOnCancelListener(this)
                    .create();
            break;
        case DIALOG_NO_INTERNET_ERROR:
            boolean wifiOnly = SipManager.isSipWifiOnly(this);
            dialog = new AlertDialog.Builder(this)
                    .setTitle(wifiOnly ? R.string.no_wifi_available_title
                                       : R.string.no_internet_available_title)
                    .setMessage(wifiOnly ? R.string.no_wifi_available
                                         : R.string.no_internet_available)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, this)
                    .setOnCancelListener(this)
                    .create();
            break;
        case DIALOG_NO_VOIP:
            dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.no_voip)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, this)
                    .setOnCancelListener(this)
                    .create();
            break;
        case DIALOG_NOT_SUPPORT_SIP_VT:
        	dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.no_voip_vt_title)
            .setMessage(R.string.no_voip_vt)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.ok, this)
            .setOnCancelListener(this)
            .create();
        	break;
        default:
            dialog = null;
        }
        if (dialog != null) {
            mDialogs[id] = dialog;
        }
        return dialog;
    }

    private void addMakeDefaultCheckBox(Dialog dialog) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(
                com.android.internal.R.layout.always_use_checkbox, null);
        CheckBox makePrimaryCheckBox =
                (CheckBox)view.findViewById(com.android.internal.R.id.alwaysUse);
        makePrimaryCheckBox.setText(R.string.remember_my_choice);
        makePrimaryCheckBox.setOnCheckedChangeListener(this);
        mUnsetPriamryHint = (TextView)view.findViewById(
                com.android.internal.R.id.clearDefaultHint);
        mUnsetPriamryHint.setText(R.string.reset_my_choice_hint);
        mUnsetPriamryHint.setVisibility(View.GONE);
        ((AlertDialog)dialog).setView(view);
    }

    private CharSequence[] getProfileNameArray() {
        CharSequence[] entries = new CharSequence[mProfileList.size()];
        int i = 0;
        for (SipProfile p : mProfileList) {
            entries[i++] = p.getProfileName();
        }
        return entries;
    }

    public void onClick(DialogInterface dialog, int id) {
        if (id == DialogInterface.BUTTON_NEGATIVE) {
            // button negative is cancel
            finish();
            return;
        } else if(dialog == mDialogs[DIALOG_SELECT_PHONE_TYPE]) {
            String selection = getResources().getStringArray(
                    R.array.phone_type_values)[id];
            Log.v(TAG, "User pick phone " + selection);
            if (selection.equals(getString(R.string.internet_phone))) {
                //first check internet call is enable
                if (!isEnableInternetCall()) {
                    this.showOpenInternet();
                    return ;
                }
                mUseSipPhone = true;
                startGetPrimarySipPhoneThread();
                return;
            }
        } else if (dialog == mDialogs[DIALOG_SELECT_OUTGOING_SIP_PHONE]) {
            mOutgoingSipProfile = mProfileList.get(id);
        } else if ((dialog == mDialogs[DIALOG_NO_INTERNET_ERROR])
                || (dialog == mDialogs[DIALOG_NO_VOIP])) {
            finish();
            return;
        } else {
            if (id == DialogInterface.BUTTON_POSITIVE && dialog != mDialogs[DIALOG_NOT_SUPPORT_SIP_VT]) {
                // Redirect to sip settings and drop the call.
                Intent newIntent = new Intent(this, SipSettings.class);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(newIntent);
            }else if (id == DialogInterface.BUTTON_POSITIVE && dialog == mDialogs[DIALOG_NOT_SUPPORT_SIP_VT])
            {
            	Intent newIntent = new Intent();
            	newIntent.setAction("com.android.phone.action.TOUCH_DIALER");
            	newIntent.putExtra("phone_number", mNumber);
            	startActivity(newIntent);
            }
            finish();
            return;
        }
        setResultAndFinish();
    }

    public void onCancel(DialogInterface dialog) {
        finish();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mMakePrimary = isChecked;
        if (isChecked) {
            mUnsetPriamryHint.setVisibility(View.VISIBLE);
        } else {
            mUnsetPriamryHint.setVisibility(View.INVISIBLE);
        }
    }

    private void createSipPhoneIfNeeded(SipProfile p) {
        CallManager cm = PhoneApp.getInstance().mCM;
        if (PhoneUtils.getSipPhoneFromUri(cm, p.getUriString()) != null) return;

        // Create the phone since we can not find it in CallManager
        try {
            SipManager.newInstance(this).open(p);
            Phone phone = PhoneFactory.makeSipPhone(p.getUriString());
            if (phone != null) {
                cm.registerPhone(phone);
            } else {
                Log.e(TAG, "cannot make sipphone profile" + p);
            }
        } catch (SipException e) {
            Log.e(TAG, "cannot open sip profile" + p, e);
        }
    }

    private void setResultAndFinish() {
        runOnUiThread(new Runnable() {
            public void run() {
                if (mOutgoingSipProfile != null) {
                    
                    if (!isEnableInternetCall()) {
                        showOpenInternet();
                        return;
                    }
                    if (!isNetworkConnected()) {
                        showDialog(DIALOG_NO_INTERNET_ERROR);
                        return;
                    }
                    Log.v(TAG, "primary SIP URI is " +
                            mOutgoingSipProfile.getUriString());
                    createSipPhoneIfNeeded(mOutgoingSipProfile);
                    mIntent.putExtra(OutgoingCallBroadcaster.EXTRA_SIP_PHONE_URI,
                            mOutgoingSipProfile.getUriString());
                    if (mMakePrimary) {
                        mSipSharedPreferences.setPrimaryAccount(
                                mOutgoingSipProfile.getUriString());
                    }
                }
                
                if (mUseSipPhone && !isEnableInternetCall()) {
                    showOpenInternet();
                    return;
                }
                
                if (mUseSipPhone && mOutgoingSipProfile == null) {
                    showDialog(DIALOG_START_SIP_SETTINGS);
                    return;
                } else {
                    /* added by xingping.zheng start */
                    if(mUseSipPhone) {
                        startActivity(mIntent);
                    }
                    else {
                        log("setResultAndFinish");
                        int level = 302;
                        if(FeatureOption.MTK_GEMINI_SUPPORT)
                            level = 306; 
                        int result = mCellConnMgr.handleCellConn(0, level, new Runnable() {
                            public void run() {
                                int result = mCellConnMgr.getResult();
                                log("setResultAndFinish, run result = "+result);
                                if(result == com.mediatek.CellConnService.CellConnMgr.RESULT_STATE_NORMAL) {
                                    log("setResultAndFinish, startActivity");
                                    startActivity(mIntent);
                                }
                                finish();
                            }
                        });
                        return;
                    }
                    /* added by xingping.zheng end   */
                }
                finish();
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if ((ni == null) || !ni.isConnected()) return false;

            return ((ni.getType() == ConnectivityManager.TYPE_WIFI)
                    || !SipManager.isSipWifiOnly(this));
        }
        return false;
    }

    private void startGetPrimarySipPhoneThread() {
        new Thread(new Runnable() {
            public void run() {
                getPrimarySipPhone();
            }
        }).start();
    }

    private void getPrimarySipPhone() {
        String primarySipUri = mSipSharedPreferences.getPrimaryAccount();

        mOutgoingSipProfile = getPrimaryFromExistingProfiles(primarySipUri);
        if (mOutgoingSipProfile == null) {
            if ((mProfileList != null) && (mProfileList.size() > 0)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        showDialog(DIALOG_SELECT_OUTGOING_SIP_PHONE);
                    }
                });
                return;
            }
        }
        setResultAndFinish();
    }

    private SipProfile getPrimaryFromExistingProfiles(String primarySipUri) {
        mProfileList = mSipProfileDb.retrieveSipProfileList();
        if (mProfileList == null) return null;
        for (SipProfile p : mProfileList) {
            if (p.getUriString().equals(primarySipUri)) return p;
        }
        return null;
    }
    
    public boolean isEnableInternetCall() {
        return 1 == Settings.System.getInt(getContentResolver(), Settings.System.ENABLE_INTERNET_CALL, 0);
    }
    
    public void showOpenInternet() {
        Dialog mTurnOnSipDialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Reminder")
                   .setMessage("Internet call is disabled, would you like to enable it?")
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
            mTurnOnSipDialog.show();
            return ;
    }

    void log(String msg) {
        Log.d(TAG, msg);
    }
}
