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

package com.android.contacts.ui;

import com.android.contacts.R;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

/**
 * Manages user preferences for contacts.
 */
public final class ContactsPreferences extends ContentObserver {
    private Context mContext;
    private int mSortOrder = -1;
    private int mDisplayOrder = -1;
    private ChangeListener mListener = null;
    private Handler mHandler;

    public ContactsPreferences(Context context) {
        super(null);
        mContext = context;
        mHandler = new Handler();
    }

    public boolean isSortOrderUserChangeable() {
        return mContext.getResources().getBoolean(R.bool.config_sort_order_user_changeable);
    }

    private int getDefaultSortOrder() {
        if (mContext.getResources().getBoolean(R.bool.config_default_sort_order_primary)) {
            return ContactsContract.Preferences.SORT_ORDER_PRIMARY;
        } else {
            return ContactsContract.Preferences.SORT_ORDER_ALTERNATIVE;
        }
    }

    public int getSortOrder() {
        if (!isSortOrderUserChangeable()) {
            return getDefaultSortOrder();
        }

        if (mSortOrder == -1) {
            try {
                mSortOrder = Settings.System.getInt(mContext.getContentResolver(),
                        ContactsContract.Preferences.SORT_ORDER);
            } catch (SettingNotFoundException e) {
                mSortOrder = getDefaultSortOrder();
            }
        }
        return mSortOrder;
    }

    public void setSortOrder(int sortOrder) {
        mSortOrder = sortOrder;
        Settings.System.putInt(mContext.getContentResolver(),
                ContactsContract.Preferences.SORT_ORDER, sortOrder);
    }

    public boolean isDisplayOrderUserChangeable() {
        return mContext.getResources().getBoolean(R.bool.config_display_order_user_changeable);
    }

    private int getDefaultDisplayOrder() {
        if (mContext.getResources().getBoolean(R.bool.config_default_display_order_primary)) {
            return ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY;
        } else {
            return ContactsContract.Preferences.DISPLAY_ORDER_ALTERNATIVE;
        }
    }

    public int getDisplayOrder() {
        if (!isDisplayOrderUserChangeable()) {
            return getDefaultDisplayOrder();
        }

        if (mDisplayOrder == -1) {
            try {
                mDisplayOrder = Settings.System.getInt(mContext.getContentResolver(),
                        ContactsContract.Preferences.DISPLAY_ORDER);
            } catch (SettingNotFoundException e) {
                mDisplayOrder = getDefaultDisplayOrder();
            }
        }
        return mDisplayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        mDisplayOrder = displayOrder;
        Settings.System.putInt(mContext.getContentResolver(),
                ContactsContract.Preferences.DISPLAY_ORDER, displayOrder);
    }

    public void registerChangeListener(ChangeListener listener) {
        if (mListener != null) unregisterChangeListener();

        // We didn't watch before, so ensure that we actually forget our cache here
        mSortOrder = -1;
        mDisplayOrder = -1;

        mListener = listener;
        final ContentResolver contentResolver = mContext.getContentResolver();
        contentResolver.registerContentObserver(
                Settings.System.getUriFor(
                        ContactsContract.Preferences.SORT_ORDER), false, this);
        contentResolver.registerContentObserver(
                Settings.System.getUriFor(
                        ContactsContract.Preferences.DISPLAY_ORDER), false, this);
    }

    public void unregisterChangeListener() {
        if (mListener != null) {
            mContext.getContentResolver().unregisterContentObserver(this);
            mListener = null;
        }
    }

    @Override
    public void onChange(boolean selfChange) {
        // This notification is not sent on the Ui thread. Use the previously created Handler
        // to switch to the Ui thread
        mHandler.post(new Runnable() {
            //@Override
            public void run() {
                mSortOrder = -1;
                mDisplayOrder = -1;
                if (mListener != null) mListener.onChange();
            }
        });
    }

    public interface ChangeListener {
        void onChange();
    }
}
