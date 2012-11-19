/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
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

import java.util.ArrayList;

import com.android.internal.telephony.CommandsInterface;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import com.android.internal.telephony.CallForwardInfo;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CommandsInterface;

/* Fion add start */
import com.android.internal.telephony.gemini.GeminiPhone;
import com.mediatek.featureoption.FeatureOption;
/* Fion add end */

import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;


public class GsmUmtsCallForwardOptions extends TimeConsumingPreferenceActivity {
    private static final String LOG_TAG = "GsmUmtsCallForwardOptions";
    private final boolean DBG = true;//(PhoneApp.DBG_LEVEL >= 2);

    private static final String NUM_PROJECTION[] = {Phone.NUMBER};

    private static final String BUTTON_CFU_KEY   = "button_cfu_key";
    private static final String BUTTON_CFB_KEY   = "button_cfb_key";
    private static final String BUTTON_CFNRY_KEY = "button_cfnry_key";
    private static final String BUTTON_CFNRC_KEY = "button_cfnrc_key";

    private static final String KEY_TOGGLE = "toggle";
    private static final String KEY_STATUS = "status";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_ITEM_STATUS = "item_status";

    private CallForwardEditPreference mButtonCFU;
    private CallForwardEditPreference mButtonCFB;
    private CallForwardEditPreference mButtonCFNRy;
    private CallForwardEditPreference mButtonCFNRc;

    private boolean isFinished = false;
    
    private boolean isVtSetting = false;

/* Fion add start */
    public static final int DEFAULT_SIM = 2; /* 0: SIM1, 1: SIM2 */

    private int mSimId = DEFAULT_SIM;
/* Fion add end */

    private final ArrayList<CallForwardEditPreference> mPreferences =
            new ArrayList<CallForwardEditPreference> ();
    private int mInitIndex= 0;

    private boolean mFirstResume = false;
    private Bundle mIcicle;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
		            //Log.d("GsmUmtsCallForwardoptions", "Received airplane changed");
                            finish();
		        }
		    }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

/* Fion add start */
        if (CallSettings.isMultipleSim())
        {
            PhoneApp app = PhoneApp.getInstance();
            mSimId = getIntent().getIntExtra(app.phone.GEMINI_SIM_ID_KEY, -1);
        }
        
        isVtSetting = getIntent().getBooleanExtra("ISVT", false);
        Log.d("GsmUmtsCallForwardoptions", "Sim Id : " + mSimId + "  for VT setting = " + isVtSetting);		
/* Fion add end */
        
        isReady();
        addPreferencesFromResource(R.xml.callforward_options);

        PreferenceScreen prefSet = getPreferenceScreen();
        mButtonCFU   = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFU_KEY);
        mButtonCFB   = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFB_KEY);
        mButtonCFNRy = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFNRY_KEY);
        mButtonCFNRc = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFNRC_KEY);

        mButtonCFU.setParentActivity(this, mButtonCFU.reason);
        mButtonCFB.setParentActivity(this, mButtonCFB.reason);
        mButtonCFNRy.setParentActivity(this, mButtonCFNRy.reason);
        mButtonCFNRc.setParentActivity(this, mButtonCFNRc.reason);
        
        /*mButtonCFU.setVtCFoward(isVtSetting);
        mButtonCFB.setVtCFoward(isVtSetting);
        mButtonCFNRy.setVtCFoward(isVtSetting);
        mButtonCFNRc.setVtCFoward(isVtSetting);*/
        
        if (isVtSetting)
        {
        	mButtonCFU.setServiceClass(CommandsInterface.SERVICE_CLASS_VIDEO);
        	mButtonCFB.setServiceClass(CommandsInterface.SERVICE_CLASS_VIDEO);
        	mButtonCFNRy.setServiceClass(CommandsInterface.SERVICE_CLASS_VIDEO);
        	mButtonCFNRc.setServiceClass(CommandsInterface.SERVICE_CLASS_VIDEO);
        }
        
        mPreferences.add(mButtonCFU);
        mPreferences.add(mButtonCFB);
        mPreferences.add(mButtonCFNRy);
        mPreferences.add(mButtonCFNRc);

        //Set the toggle in order to meet the recover dialog in correct status
        if (null != icicle)
        {
            for (CallForwardEditPreference pref : mPreferences) {
            	if (null != pref) {
                    Bundle bundle = icicle.getParcelable(pref.getKey());
                    if (null != bundle) {
                        pref.setToggled(bundle.getBoolean(KEY_TOGGLE));
                    }
            	}
            }
        }

        // we wait to do the initialization until onResume so that the
        // TimeConsumingPreferenceActivity dialog can display as it
        // relies on onResume / onPause to maintain its foreground state.

        mFirstResume = true;
        PhoneUtils.setMmiFinished(false);
        mIcicle = icicle;
        if (null != getIntent().getStringExtra(MultipleSimActivity.SUB_TITLE_NAME))
        {
            setTitle(getIntent().getStringExtra(MultipleSimActivity.SUB_TITLE_NAME));
        }
        registerReceiver(mIntentReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
    }

    @Override
    public void onResume() {
        super.onResume();
        mInitIndex = 0;
        if (mFirstResume == true)
        {
        mPreferences.get(mInitIndex).init(this, false, mSimId);	  	
            mFirstResume = false;
                }
        else
        {
        	if (PhoneUtils.getMmiFinished() == true)
        	{
        		mPreferences.get(mInitIndex).init(this, false, mSimId);
        		PhoneUtils.setMmiFinished(false);
            }
        	else
        	{
        		mInitIndex = mPreferences.size()-1;
        	}
        	Log.d(LOG_TAG, "No change, so don't query!");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        for (CallForwardEditPreference pref : mPreferences) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(KEY_TOGGLE, pref.isToggled());
            bundle.putBoolean(KEY_ITEM_STATUS, pref.isEnabled());
            if (pref.callForwardInfo != null) {
                bundle.putString(KEY_NUMBER, pref.callForwardInfo.number);
                bundle.putInt(KEY_STATUS, pref.callForwardInfo.status);
            }
            outState.putParcelable(pref.getKey(), bundle);
        }
    }

    @Override
    public void onFinished(Preference preference, boolean reading) {
        if (mInitIndex < mPreferences.size()-1 && !isFinishing()) {
            //mInitIndex++;
            if (mPreferences.get(mInitIndex++).isSuccess())
            {
/* Fion add start */
            mPreferences.get(mInitIndex).init(this, false, mSimId);
/* Fion add end */
        }
            else
            {
                for (int i = mInitIndex; i < mPreferences.size(); ++i)
                {
                    mPreferences.get(i).setEnabled(false);
                }
                mInitIndex = mPreferences.size();
            }
        }

        super.onFinished(preference, reading);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DBG) Log.d(LOG_TAG, "onActivityResult: done");
        if (resultCode != RESULT_OK) {
            if (DBG) Log.d(LOG_TAG, "onActivityResult: contact picker result not OK.");
            return;
        }
        Cursor cursor = getContentResolver().query(data.getData(),
                NUM_PROJECTION, null, null, null);
        if ((cursor == null) || (!cursor.moveToFirst())) {
            if (DBG) Log.d(LOG_TAG, "onActivityResult: bad contact data, no results found.");
            return;
        }

        switch (requestCode) {
            case CommandsInterface.CF_REASON_UNCONDITIONAL:
                mButtonCFU.onPickActivityResult(cursor.getString(0));
                break;
            case CommandsInterface.CF_REASON_BUSY:
                mButtonCFB.onPickActivityResult(cursor.getString(0));
                break;
            case CommandsInterface.CF_REASON_NO_REPLY:
                mButtonCFNRy.onPickActivityResult(cursor.getString(0));
                break;
            case CommandsInterface.CF_REASON_NOT_REACHABLE:
                mButtonCFNRc.onPickActivityResult(cursor.getString(0));
                break;
            default:
                // TODO: may need exception here.
        }
    }
    public void onDestroy(){
        mButtonCFU.setStatus(true);
        mButtonCFB.setStatus(true);
        mButtonCFNRy.setStatus(true);
        mButtonCFNRc.setStatus(true);
        unregisterReceiver(mIntentReceiver);
        super.onDestroy();
    }
    
    private void isReady() {
        com.android.internal.telephony.Phone  phone = com.android.internal.telephony.PhoneFactory.getDefaultPhone();
/* Fion add start */
        int state;
        if (CallSettings.isMultipleSim())
        {
            state=((GeminiPhone)phone).getServiceStateGemini(mSimId).getState();
        }
        else
        {
            state=phone.getServiceState().getState();
        }
/* Fion add end */

        if(state!=android.telephony.ServiceState.STATE_IN_SERVICE) {
        	finish();
        	Toast.makeText(this,getString(R.string.net_or_simcard_busy),Toast.LENGTH_SHORT).show();
        }
    }
    
    //Refresh the settings when disable CFU
    public void refreshSettings(boolean bNeed)
    {
        if (bNeed)
        {
            mInitIndex = 1;
            mPreferences.get(mInitIndex).init(this, false, mSimId);
        }
    }
}
