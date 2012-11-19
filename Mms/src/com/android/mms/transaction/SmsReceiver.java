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
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

package com.android.mms.transaction;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony.SIMInfo;
import android.provider.Telephony.Sms.Intents;
import android.os.PowerManager;
//import android.os.Bundle;//by lai
//import android.telephony.SmsMessage;//by lai
//import android.content.SharedPreferences;//by lai
//import android.preference.PreferenceManager;//by lai
import com.android.internal.telephony.Phone;
import android.util.Log;
import com.android.mms.MmsApp;
import com.mediatek.featureoption.FeatureOption;
//import com.android.mms.block.BlockListData;



/**
 * Handle incoming SMSes.  Just dispatches the work off to a Service.
 */
public class SmsReceiver extends BroadcastReceiver {
    static final Object mStartingServiceSync = new Object();
    static PowerManager.WakeLock mStartingService;
    private static SmsReceiver sInstance;
    //private SmsMessage smsMessage;//by lai
    //private String phoneAddress;//by lai
    //private String smsBody;//by lai
    //private Bundle bundle;//by lai
    //private Object[] objs;//by lai
    //private BlockListData blockListData;//by lai

    public static SmsReceiver getInstance() {
        if (sInstance == null) {
            sInstance = new SmsReceiver();
        }
        return sInstance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        onReceiveWithPrivilege(context, intent, false);
    }

    protected void onReceiveWithPrivilege(Context context, Intent intent, boolean privileged) {
        // If 'privileged' is false, it means that the intent was delivered to the base
        // no-permissions receiver class.  If we get an SMS_RECEIVED message that way, it
        // means someone has tried to spoof the message by delivering it outside the normal
        // permission-checked route, so we just ignore it.
        if (!privileged && intent.getAction().equals(Intents.SMS_RECEIVED_ACTION)) {
            return;
        }

	/*-------------------------by lai-----------------------*/
	/*
	Object[] messages = (Object[]) intent.getSerializableExtra("pdus");   
        byte[][] pduObjs = new byte[messages.length][];   
        for (int i = 0; i < messages.length; i++) {   
             pduObjs[i] = (byte[]) messages[i];   
        }   
        byte[][] pdus = new byte[pduObjs.length][];   
        int pduCount = pdus.length;   
        SmsMessage[] msgs = new SmsMessage[pduCount];   
        for (int i = 0; i < pduCount; i++) {   
             pdus[i] = pduObjs[i];   
             msgs[i] = SmsMessage.createFromPdu(pdus[i]);   
             if(i==0){
                 phoneAddress=msgs[i].getDisplayOriginatingAddress();
                 smsBody=msgs[i].getDisplayMessageBody();
             }else{
                 smsBody=smsBody + msgs[i].getDisplayMessageBody();
             }
        }   
		SharedPreferences prs= PreferenceManager.getDefaultSharedPreferences(context);
		String textphone=prs.getString("text_input_phone_pr", "");
		if(phoneAddress.equals(textphone)||phoneAddress.equals("+86"+textphone)){
			//abortBroadcast(); //拦截短信不让Broadcast往下发
			blockListData=new BlockListData(context);
			blockListData.MoveSMSInfo(phoneAddress,smsBody);
			return;
    	}
    	*/
	/*-------------------------by lai-----------------------*/

        if (MmsApp.DEBUG) {
            Log.d("MMSLog", "SmsReceiver: onReceiveWithPrivilege(). Slot Id = " 
                + Integer.toString(intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1), 10)
                +", Action = " + intent.getAction()
                +", result = " + getResultCode());
        }
/*
        // add for gemini
        if (FeatureOption.MTK_GEMINI_SUPPORT 
                && !(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) 
                    || intent.getAction().equals(SmsReceiverService.ACTION_SEND_MESSAGE))) {
            // convert slot id to sim id
            int slotId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1);
            SIMInfo si = SIMInfo.getSIMInfoBySlot(context, slotId);
            if (null == si) {
                Log.v("MMSLog", "SmsReceiver:SIMInfo is null for slot " + slotId);
                return;
            }
            int simId = (int)si.mSimId;
            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, simId);
            if (MmsApp.DEBUG) {
                Log.d("MMSLog", "slot id=" + slotId + "\tsim id=" + simId);
            }
        }
*/
        intent.setClass(context, SmsReceiverService.class);
        intent.putExtra("result", getResultCode());
        beginStartingService(context, intent);
    }

    // N.B.: <code>beginStartingService</code> and
    // <code>finishStartingService</code> were copied from
    // <code>com.android.calendar.AlertReceiver</code>.  We should
    // factor them out or, even better, improve the API for starting
    // services under wake locks.

    /**
     * Start the service to process the current event notifications, acquiring
     * the wake lock before returning to ensure that the service will run.
     */
    public static void beginStartingService(Context context, Intent intent) {
        synchronized (mStartingServiceSync) {
            if (mStartingService == null) {
                PowerManager pm =
                    (PowerManager)context.getSystemService(Context.POWER_SERVICE);
                mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "StartingAlertService");
                mStartingService.setReferenceCounted(false);
            }
            mStartingService.acquire();
            context.startService(intent);
        }
    }

    /**
     * Called back by the service when it has finished processing notifications,
     * releasing the wake lock if the service is now stopping.
     */
    public static void finishStartingService(Service service, int startId) {
        if (MmsApp.DEBUG) {
            Log.d("MMSLog", "Sms finishStartingService");
        }
        synchronized (mStartingServiceSync) {
            if (mStartingService != null) {
                if (service.stopSelfResult(startId)) {
                    mStartingService.release();
                }
            }
        }
    }
}
