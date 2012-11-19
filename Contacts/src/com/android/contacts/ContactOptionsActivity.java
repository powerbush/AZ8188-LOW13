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

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * An activity for selecting options for a given contact: custom ringtone and send-to-voicemail.
 */
public class ContactOptionsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "ContactOptionsActivity";

    private static final String[] AGGREGATES_PROJECTION = new String[] {
            Contacts.CUSTOM_RINGTONE, Contacts.SEND_TO_VOICEMAIL
    };

    private static final int COL_CUSTOM_RINGTONE = 0;
    private static final int COL_SEND_TO_VOICEMAIL = 1;

    /** The launch code when picking a ringtone */
    private static final int RINGTONE_PICKED = 3023;

    private String mCustomRingtone;
    private boolean mSendToVoicemail;
    private TextView mRingtoneTitle;
    private CheckBox mSendToVoicemailCheckbox;

    private Uri mLookupUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLookupUri = getIntent().getData();

        setContentView(R.layout.contact_options);

        int indicate = getIntent().getIntExtra(RawContacts.INDICATE_PHONE_SIM, RawContacts.INDICATE_PHONE);

        View ringtoneLayout = findViewById(R.id.ringtone);
        TextView labelRingtone = (TextView)ringtoneLayout.findViewById(R.id.label);
        labelRingtone.setText(getString(R.string.label_ringtone));
        mRingtoneTitle = (TextView)ringtoneLayout.findViewById(R.id.data);
        View sendToVoicemailLayout = findViewById(R.id.voicemail);
        TextView labelSendToVoicemail = (TextView)sendToVoicemailLayout.findViewById(R.id.label);
        labelSendToVoicemail.setText(getString(R.string.actionIncomingCall));
        mSendToVoicemailCheckbox = (CheckBox)sendToVoicemailLayout.findViewById(R.id.checkbox);
        ringtoneLayout.setFocusable(true);
        //ringtoneLayout.setHighFocusPriority(true);
        ringtoneLayout.setNextFocusDownId(R.id.data);
        sendToVoicemailLayout.setNextFocusUpId(R.id.ringtone);
        
        if (indicate > RawContacts.INDICATE_PHONE) {
        	ringtoneLayout.setVisibility(View.GONE);
        } else {
        ringtoneLayout.setOnClickListener(this);
        }
        sendToVoicemailLayout.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!loadData()) {
            finish();
        }

        updateView();
    }

    private void updateView() {
        if (mCustomRingtone == null) {
            mRingtoneTitle.setText(getString(R.string.default_ringtone));
        } else {
            Uri ringtoneUri = Uri.parse(mCustomRingtone);
            Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            if (ringtone == null) {
                Log.w(TAG, "ringtone's URI doesn't resolve to a Ringtone");
                return;
            }
            mRingtoneTitle.setText(ringtone.getTitle(this));
        }

        mSendToVoicemailCheckbox.setChecked(mSendToVoicemail);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ringtone: {
                doPickRingtone();
                break;
            }
            case R.id.voicemail: {
                doToggleSendToVoicemail();
                break;
            }
        }
    }

    private void doPickRingtone() {

        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        // Allow user to pick 'Default'
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        // Show only ringtones
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        // Don't show 'Silent'
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);

        Uri ringtoneUri;
        if (mCustomRingtone != null) {
            ringtoneUri = Uri.parse(mCustomRingtone);
        } else {
            // Otherwise pick default ringtone Uri so that something is selected.
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        // Put checkmark next to the current ringtone for this contact
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri);

        // Launch!
        startActivityForResult(intent, RINGTONE_PICKED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case RINGTONE_PICKED: {
                Uri pickedUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                handleRingtonePicked(pickedUri);
                break;
            }
        }
    }

    private void handleRingtonePicked(Uri pickedUri) {
        if (pickedUri == null || RingtoneManager.isDefault(pickedUri)) {
            mCustomRingtone = null;
        } else {
            mCustomRingtone = pickedUri.toString();
        }
        saveData();
        updateView();
    }

    private void doToggleSendToVoicemail() {
        mSendToVoicemailCheckbox.toggle();
        mSendToVoicemail = mSendToVoicemailCheckbox.isChecked();
        saveData();
        updateView();
    }

    private boolean loadData() {
        Cursor c =
                getContentResolver().query(mLookupUri, AGGREGATES_PROJECTION, null, null, null);
        try {
            if (!c.moveToFirst()) {
                return false;
            }

            mCustomRingtone = c.getString(COL_CUSTOM_RINGTONE);
            mSendToVoicemail = c.getInt(COL_SEND_TO_VOICEMAIL) != 0;

        } finally {
            c.close();
        }
        return true;
    }

    private void saveData() {
        ContentValues values = new ContentValues(2);
        values.put(Contacts.CUSTOM_RINGTONE, mCustomRingtone);
        values.put(Contacts.SEND_TO_VOICEMAIL, mSendToVoicemail);
        getContentResolver().update(mLookupUri, values, null, null);
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
}


