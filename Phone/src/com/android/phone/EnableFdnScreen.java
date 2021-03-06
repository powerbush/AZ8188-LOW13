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

/*
 * Copyright (C) 2006 The Android Open Source Project
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

import android.app.Activity;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

/**
 * UI to enable/disable FDN.
 */
public class EnableFdnScreen extends Activity {
    private static final String LOG_TAG = PhoneApp.LOG_TAG;
    private static final boolean DBG = false;

    private static final int ENABLE_FDN_COMPLETE = 100;

    private LinearLayout mPinFieldContainer;
    private EditText mPin2Field;
    private TextView mStatusField;
    private boolean mEnable;
    private Phone mPhone;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ENABLE_FDN_COMPLETE:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    handleResult(ar);
                    break;
            }

            return;
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.enable_fdn_screen);
        setupView();

        mPhone = PhoneFactory.getDefaultPhone();
        mEnable = !mPhone.getIccCard().getIccFdnEnabled();

        int id = mEnable ? R.string.enable_fdn : R.string.disable_fdn;
        setTitle(getResources().getText(id));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPhone = PhoneFactory.getDefaultPhone();
    }

    private void setupView() {
        mPin2Field = (EditText) findViewById(R.id.pin);
        mPin2Field.setKeyListener(DigitsKeyListener.getInstance());
        mPin2Field.setMovementMethod(null);
        mPin2Field.setOnClickListener(mClicked);

        mPinFieldContainer = (LinearLayout) findViewById(R.id.pinc);
        mStatusField = (TextView) findViewById(R.id.status);
    }

    private void showStatus(CharSequence statusMsg) {
        if (statusMsg != null) {
            mStatusField.setText(statusMsg);
            mStatusField.setVisibility(View.VISIBLE);
            mPinFieldContainer.setVisibility(View.GONE);
        } else {
            mPinFieldContainer.setVisibility(View.VISIBLE);
            mStatusField.setVisibility(View.GONE);
        }
    }

    private String getPin2() {
        return mPin2Field.getText().toString();
    }

    private void enableFdn() {
        Message callback = Message.obtain(mHandler, ENABLE_FDN_COMPLETE);
        mPhone.getIccCard().setIccFdnEnabled(mEnable, getPin2(), callback);
        if (DBG) log("enableFdn: please wait...");
    }

    private void handleResult(AsyncResult ar) {
        if (ar.exception == null) {
            if (DBG) log("handleResult: success!");
            showStatus(getResources().getText(mEnable ?
                            R.string.enable_fdn_ok : R.string.disable_fdn_ok));
        } else if (ar.exception instanceof CommandException
                /* && ((CommandException)ar.exception).getCommandError() ==
                    CommandException.Error.GENERIC_FAILURE */ ) {
            if (DBG) log("handleResult: failed!");
            showStatus(getResources().getText(
                    R.string.pin_failed));
        }

        mHandler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 3000);
    }

    private View.OnClickListener mClicked = new View.OnClickListener() {
        public void onClick(View v) {
            if (TextUtils.isEmpty(mPin2Field.getText())) {
                return;
            }

            showStatus(getResources().getText(
                    R.string.enable_in_progress));

            enableFdn();
        }
    };

    private void log(String msg) {
        Log.d(LOG_TAG, "[EnableSimPin] " + msg);
    }
}
