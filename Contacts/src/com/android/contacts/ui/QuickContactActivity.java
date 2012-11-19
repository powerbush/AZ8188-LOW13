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

package com.android.contacts.ui;

import com.android.contacts.StickyTabs;
import com.android.contacts.ContactsUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.QuickContact;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import com.mediatek.CellConnService.CellConnMgr;

/**
 * Stub translucent activity that just shows {@link QuickContactWindow} floating
 * above the caller. This temporary hack should eventually be replaced with
 * direct framework support.
 */
public final class QuickContactActivity extends Activity implements
        QuickContactWindow.OnDismissListener, ContactsUtils.CellConnMgrClient {
    private static final String TAG = "QuickContactActivity";

    static final boolean LOGV = true; //false;
    static final boolean FORCE_CREATE = false;

	static boolean mActivityOn = false;
    CellConnMgr mCellConnMgr;

    private QuickContactWindow mQuickContact;

    @Override
    protected void onCreate(Bundle icicle) {

		if (mActivityOn) return;
		mActivityOn = true;

        super.onCreate(icicle);
        if (LOGV) Log.d(TAG, "onCreate");
        mCellConnMgr = new CellConnMgr();
		mCellConnMgr.register(getApplicationContext());

        this.onNewIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (LOGV) Log.d(TAG, "onNewIntent");

        if (QuickContactWindow.TRACE_LAUNCH) {
            android.os.Debug.startMethodTracing(QuickContactWindow.TRACE_TAG);
        }

        if (mQuickContact == null || FORCE_CREATE) {
            if (LOGV) Log.d(TAG, "Preparing window");
            mQuickContact = new QuickContactWindow(this, this);
        }
        mQuickContact.setLastSelectedContactsAppTab(StickyTabs.getTab(intent));

        // Use our local window token for now
        Uri lookupUri = intent.getData();
        // Check to see whether it comes from the old version.
        if (android.provider.Contacts.AUTHORITY.equals(lookupUri.getAuthority())) {
            final long rawContactId = ContentUris.parseId(lookupUri);
            lookupUri = RawContacts.getContactLookupUri(getContentResolver(),
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId));
        }
        final Bundle extras = intent.getExtras();

        // Read requested parameters for displaying
        final Rect target = intent.getSourceBounds();
        final int mode = extras.getInt(QuickContact.EXTRA_MODE, QuickContact.MODE_MEDIUM);
        final String[] excludeMimes = extras.getStringArray(QuickContact.EXTRA_EXCLUDE_MIMES);

        mQuickContact.show(lookupUri, target, mode, excludeMimes);
    }

    /** {@inheritDoc} */
    @Override
    public void onBackPressed() {
        if (LOGV) Log.w(TAG, "Unexpected back captured by stub activity");
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (LOGV) Log.d(TAG, "onPause");

        // Dismiss any dialog when pausing
        mQuickContact.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mQuickContact.destroy();
        if (LOGV) Log.d(TAG, "onDestroy");
        // Notify any listeners that we've been dismissed
    	if (mCellConnMgr != null) mCellConnMgr.unregister();
        
    }

    /** {@inheritDoc} */
    public void onDismiss(QuickContactWindow dialog) {
        if (LOGV) Log.d(TAG, "onDismiss");

		mActivityOn = false;
		
		// mtk80909 2010-9-16 start
		AlertDialog callDialog = ContactsUtils.getCallDialog();
		if (callDialog != null && callDialog.isShowing()) {
			callDialog.dismiss();
		}
		// mtk80909 2010-9-16 end

        if (isTaskRoot() && !FORCE_CREATE) {
            
            if (LOGV) Log.d(TAG, "onDismiss, isTaskRoot()");
            // Instead of stopping, simply push this to the back of the stack.
            // This is only done when running at the top of the stack;
            // otherwise, we have been launched by someone else so need to
            // allow the user to go back to the caller.
            //moveTaskToBack(false);
            finish(); //modify moveTaskToBack() to finish() for ALPS00031076
        } else {
            finish();
        }
    }

	public CellConnMgr getCellConnMgr() {
        // TODO Auto-generated method stub
        return mCellConnMgr;
    }    
}
