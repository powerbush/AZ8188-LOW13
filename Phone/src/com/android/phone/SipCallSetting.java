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

package com.android.phone;

import java.util.List;

import com.android.phone.sip.SipProfileDb;
import com.android.phone.sip.SipSharedPreferences;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;

public class SipCallSetting extends PreferenceActivity implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener
{
	private static final String BUTTON_SIP_CALL_OPTIONS =
        "sip_call_options_key";
    private static final String BUTTON_SIP_CALL_OPTIONS_WIFI_ONLY =
        "sip_call_options_wifi_only_key";
    private static final String SIP_SETTINGS_CATEGORY_KEY =
        "sip_settings_category_key";
    
    private static final String TAG = "SipCallSetting";

    private SipManager mSipManager;
    private CallManager mCallManager;
    private SipProfileDb mProfileDb;
    private SipSharedPreferences mSipSharedPreferences;
    private ListPreference mListSipCallOptions;
    private CheckBoxPreference mButtonSipCallOptions;
    private Preference mAccountPreference;
    
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (SipManager.isVoipSupported(this)) {
            mSipManager = SipManager.newInstance(this);
            mCallManager = CallManager.getInstance();
            mSipSharedPreferences = new SipSharedPreferences(this);
            addPreferencesFromResource(R.xml.sip_settings_category);
            mButtonSipCallOptions = (CheckBoxPreference)this.findPreference("open_sip_call_option_key");
            mButtonSipCallOptions.setOnPreferenceClickListener(this);
            mListSipCallOptions = this.getSipCallOptionPreference();
            
            mAccountPreference = this.findPreference("sip_account_settings_key");
            mProfileDb = new SipProfileDb(this);
            if (CallSettings.isMultipleSim()) {
                this.getPreferenceScreen().removePreference(mListSipCallOptions);
                mListSipCallOptions = null;
            } else {
                mListSipCallOptions.setOnPreferenceChangeListener(this);
                mListSipCallOptions.setValueIndex(
                        mListSipCallOptions.findIndexOfValue(
                                mSipSharedPreferences.getSipCallOption()));
                mListSipCallOptions.setSummary(mListSipCallOptions.getEntry());
            }
            /*mButtonSipCallOptions = getSipCallOptionPreference();
            mButtonSipCallOptions.setOnPreferenceChangeListener(this);
            mButtonSipCallOptions.setValueIndex(
                    mButtonSipCallOptions.findIndexOfValue(
                            mSipSharedPreferences.getSipCallOption()));
            mButtonSipCallOptions.setSummary(mButtonSipCallOptions.getEntry());*/
        }
	}
	
	protected void onResume() {
	    super.onResume();
	    int enable = android.provider.Settings.System.getInt(PhoneApp.getInstance().getContentResolver(),
                android.provider.Settings.System.ENABLE_INTERNET_CALL, 0);
	    
	    if (mCallManager.getState() != Phone.State.IDLE) {
	        mAccountPreference.setEnabled(false);
	        mButtonSipCallOptions.setEnabled(false);
	        if (mListSipCallOptions != null) mListSipCallOptions.setEnabled(false);
	    } else { 
	        mAccountPreference.setEnabled(enable == 1);
	        mButtonSipCallOptions.setEnabled(true);
	        if (mListSipCallOptions != null) mListSipCallOptions.setEnabled(enable == 1);
	    }
	    
	    if (enable == 1 && !mButtonSipCallOptions.isChecked()) {
	        mButtonSipCallOptions.setChecked(true);
	    }else if (enable == 0 && mButtonSipCallOptions.isChecked()) {
	        mButtonSipCallOptions.setChecked(false);
	    }
	}
	
	// Gets the call options for SIP depending on whether SIP is allowed only
    // on Wi-Fi only; also make the other options preference invisible.
    private ListPreference getSipCallOptionPreference() {
        ListPreference wifiAnd3G = (ListPreference)
                findPreference(BUTTON_SIP_CALL_OPTIONS);
        ListPreference wifiOnly = (ListPreference)
                findPreference(BUTTON_SIP_CALL_OPTIONS_WIFI_ONLY);
        PreferenceScreen prefSet = getPreferenceScreen();
        /*PreferenceGroup sipSettings = (PreferenceGroup)
                findPreference(SIP_SETTINGS_CATEGORY_KEY);*/
        if (SipManager.isSipWifiOnly(this)) {
            //sipSettings.removePreference(wifiAnd3G);
        	prefSet.removePreference(wifiAnd3G);
            return wifiOnly;
        } else {
            //sipSettings.removePreference(wifiOnly);
        	prefSet.removePreference(wifiOnly);
            return wifiAnd3G;
        }
    }
    
    /*private void handleSipCallOptionsChange(Object objValue) {
        String option = objValue.toString();
        mSipSharedPreferences.setSipCallOption(option);
        mButtonSipCallOptions.setValueIndex(
                mButtonSipCallOptions.findIndexOfValue(option));
        mButtonSipCallOptions.setSummary(mButtonSipCallOptions.getEntry());
    }*/
    
    public boolean onPreferenceClick(Preference preference)
    {
        if (preference == mButtonSipCallOptions) {
            //handleSipCallOptionsChange(objValue);
            CheckBoxPreference cp = (CheckBoxPreference)mButtonSipCallOptions;
            final int intEnable = cp.isChecked() ? 1:0;
            android.provider.Settings.System.putInt(PhoneApp.getInstance().getContentResolver(),
                     android.provider.Settings.System.ENABLE_INTERNET_CALL,
                     intEnable);
            new Thread(new Runnable() {
                public void run() {
                    handleSipReceiveCallsOption(intEnable == 1);
                }
            }).start();
            if (intEnable == 1)
            {
                mAccountPreference.setEnabled(true);
                if (mListSipCallOptions != null) mListSipCallOptions.setEnabled(true);
                
            } else {
                mAccountPreference.setEnabled(false);
                if (mListSipCallOptions != null) mListSipCallOptions.setEnabled(false);
            }
        }
        return false;
    }
    
    private synchronized void handleSipReceiveCallsOption(boolean enabled) {
        boolean isReceiveCall = mSipSharedPreferences.getReceivingCallsEnabled();
        if ((enabled && !isReceiveCall) || (!enabled && !isReceiveCall))
        {
            return ;
        }
        List<SipProfile> sipProfileList = mProfileDb.retrieveSipProfileList();
        for (SipProfile p : sipProfileList) {
            String sipUri = p.getUriString();
            p = updateAutoRegistrationFlag(p, enabled);
            try {
                if (enabled) {
                    mSipManager.open(p,
                            SipUtil.createIncomingCallPendingIntent(), null);
                } else {
                    mSipManager.close(sipUri);
                    if (mSipSharedPreferences.isPrimaryAccount(sipUri)) {
                        // re-open in order to make calls
                        mSipManager.open(p);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "register failed", e);
            }
        }
    }
    
    private SipProfile updateAutoRegistrationFlag(
            SipProfile p, boolean enabled) {
        SipProfile newProfile = new SipProfile.Builder(p)
                .setAutoRegistration(enabled)
                .build();
        try {
            mProfileDb.deleteProfile(p);
            mProfileDb.saveProfile(newProfile);
        } catch (Exception e) {
            Log.e(TAG, "updateAutoRegistrationFlag error", e);
        }
        return newProfile;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        if (preference == mListSipCallOptions) {
            handleSipCallOptionsChange(newValue);
        }
        return true;
    }
    
    private void handleSipCallOptionsChange(Object objValue) {
        String option = objValue.toString();
        mSipSharedPreferences.setSipCallOption(option);
        mListSipCallOptions.setValueIndex(
                mListSipCallOptions.findIndexOfValue(option));
        mListSipCallOptions.setSummary(mListSipCallOptions.getEntry());
    }

}
