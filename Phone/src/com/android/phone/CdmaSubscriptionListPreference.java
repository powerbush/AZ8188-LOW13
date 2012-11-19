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

package com.android.phone;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.provider.Settings.Secure;
import android.util.AttributeSet;
import android.util.Log;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

public class CdmaSubscriptionListPreference extends ListPreference {

    private static final String LOG_TAG = "CdmaSubscriptionListPreference";

    // Used for CDMA subscription mode
    private static final int CDMA_SUBSCRIPTION_RUIM_SIM = 0;
    private static final int CDMA_SUBSCRIPTION_NV = 1;

    //preferredSubscriptionMode  0 - RUIM/SIM, preferred
    //                           1 - NV
    static final int preferredSubscriptionMode = CDMA_SUBSCRIPTION_NV;

    private Phone mPhone;
    private CdmaSubscriptionButtonHandler mHandler;

    public CdmaSubscriptionListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPhone = PhoneFactory.getDefaultPhone();
        mHandler = new CdmaSubscriptionButtonHandler();
        setCurrentCdmaSubscriptionModeValue();
    }

    private void setCurrentCdmaSubscriptionModeValue() {
        int cdmaSubscriptionMode = Secure.getInt(mPhone.getContext().getContentResolver(),
                android.provider.Settings.Secure.CDMA_SUBSCRIPTION_MODE, preferredSubscriptionMode);
        setValue(Integer.toString(cdmaSubscriptionMode));
    }

    public CdmaSubscriptionListPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void showDialog(Bundle state) {
        setCurrentCdmaSubscriptionModeValue();

        super.showDialog(state);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (!positiveResult) {
            //The button was dismissed - no need to set new value
            return;
        }

        int buttonCdmaSubscriptionMode = Integer.valueOf(getValue()).intValue();
        Log.d(LOG_TAG, "Setting new value " + buttonCdmaSubscriptionMode);
        int statusCdmaSubscriptionMode;
        switch(buttonCdmaSubscriptionMode) {
            case CDMA_SUBSCRIPTION_NV:
                statusCdmaSubscriptionMode = Phone.CDMA_SUBSCRIPTION_NV;
                break;
            case CDMA_SUBSCRIPTION_RUIM_SIM:
                statusCdmaSubscriptionMode = Phone.CDMA_SUBSCRIPTION_RUIM_SIM;
                break;
            default:
                statusCdmaSubscriptionMode = Phone.PREFERRED_CDMA_SUBSCRIPTION;
        }

        // Set the CDMA subscription mode, when mode has been successfully changed
        // handleSetCdmaSubscriptionMode will be invoked and the value saved.
        mPhone.setCdmaSubscription(statusCdmaSubscriptionMode, mHandler
                .obtainMessage(CdmaSubscriptionButtonHandler.MESSAGE_SET_CDMA_SUBSCRIPTION,
                        getValue()));

    }

    private class CdmaSubscriptionButtonHandler extends Handler {

        private static final int MESSAGE_SET_CDMA_SUBSCRIPTION = 0;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SET_CDMA_SUBSCRIPTION:
                    handleSetCdmaSubscriptionMode(msg);
                    break;
            }
        }

        private void handleSetCdmaSubscriptionMode(Message msg) {
            mPhone = PhoneFactory.getDefaultPhone();
            AsyncResult ar = (AsyncResult) msg.obj;

            if (ar.exception == null) {
                // Get the original string entered by the user
                int cdmaSubscriptionMode = Integer.valueOf((String) ar.userObj).intValue();
                Secure.putInt(mPhone.getContext().getContentResolver(),
                        Secure.CDMA_SUBSCRIPTION_MODE,
                        cdmaSubscriptionMode );
            } else {
                Log.e(LOG_TAG, "Setting Cdma subscription source failed");
            }
        }
    }
}