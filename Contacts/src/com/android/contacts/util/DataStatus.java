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

package com.android.contacts.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.text.format.DateUtils;

/**
 * Storage for a social status update. Holds a single update, but can use
 * {@link #possibleUpdate(Cursor)} to consider updating when a better status
 * exists. Statuses with timestamps, or with newer timestamps win.
 */
public class DataStatus {
    private int mPresence = -1;
    private String mStatus = null;
    private long mTimestamp = -1;

    private String mResPackage = null;
    private int mIconRes = -1;
    private int mLabelRes = -1;

    public DataStatus() {
    }

    public DataStatus(Cursor cursor) {
        // When creating from cursor row, fill normally
        fromCursor(cursor);
    }

    /**
     * Attempt updating this {@link DataStatus} based on values at the
     * current row of the given {@link Cursor}.
     */
    public void possibleUpdate(Cursor cursor) {
        final boolean hasStatus = !isNull(cursor, Data.STATUS);
        final boolean hasTimestamp = !isNull(cursor, Data.STATUS_TIMESTAMP);

        // Bail early when not valid status, or when previous status was
        // found and we can't compare this one.
        if (!hasStatus) return;
        if (isValid() && !hasTimestamp) return;

        if (hasTimestamp) {
            // Compare timestamps and bail if older status
            final long newTimestamp = getLong(cursor, Data.STATUS_TIMESTAMP, -1);
            if (newTimestamp < mTimestamp) return;

            mTimestamp = newTimestamp;
        }

        // Fill in remaining details from cursor
        fromCursor(cursor);
    }

    private void fromCursor(Cursor cursor) {
        mPresence = getInt(cursor, Data.PRESENCE, -1);
        mStatus = getString(cursor, Data.STATUS);
        mTimestamp = getLong(cursor, Data.STATUS_TIMESTAMP, -1);
        mResPackage = getString(cursor, Data.STATUS_RES_PACKAGE);
        mIconRes = getInt(cursor, Data.STATUS_ICON, -1);
        mLabelRes = getInt(cursor, Data.STATUS_LABEL, -1);
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(mStatus);
    }

    public int getPresence() {
        return mPresence;
    }

    public CharSequence getStatus() {
        return mStatus;
    }

    /**
     * Build any timestamp and label into a single string.
     */
    public CharSequence getTimestampLabel(Context context) {
        final PackageManager pm = context.getPackageManager();

        // Use local package for resources when none requested
        if (mResPackage == null) mResPackage = context.getPackageName();

        final boolean validTimestamp = mTimestamp > 0;
        final boolean validLabel = mResPackage != null && mLabelRes != -1;

        final CharSequence timeClause = validTimestamp ? DateUtils.getRelativeTimeSpanString(
                mTimestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE) : null;
        final CharSequence labelClause = validLabel ? pm.getText(mResPackage, mLabelRes,
                null) : null;

        if (validTimestamp && validLabel) {
            return context.getString(
                    com.android.internal.R.string.contact_status_update_attribution_with_date,
                    timeClause, labelClause);
        } else if (validLabel) {
            return context.getString(
                    com.android.internal.R.string.contact_status_update_attribution,
                    labelClause);
        } else if (validTimestamp) {
            return timeClause;
        } else {
            return null;
        }
    }

    public Drawable getIcon(Context context) {
        final PackageManager pm = context.getPackageManager();

        // Use local package for resources when none requested
        if (mResPackage == null) mResPackage = context.getPackageName();

        final boolean validIcon = mResPackage != null && mIconRes != -1;
        return validIcon ? pm.getDrawable(mResPackage, mIconRes, null) : null;
    }

    private static String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    private static int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    private static int getInt(Cursor cursor, String columnName, int missingValue) {
        final int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.isNull(columnIndex) ? missingValue : cursor.getInt(columnIndex);
    }

    private static long getLong(Cursor cursor, String columnName, long missingValue) {
        final int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.isNull(columnIndex) ? missingValue : cursor.getLong(columnIndex);
    }

    private static boolean isNull(Cursor cursor, String columnName) {
        return cursor.isNull(cursor.getColumnIndex(columnName));
    }
}
