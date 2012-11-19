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

package com.android.contacts;

import com.android.contacts.model.EntityModifier;
import com.android.contacts.util.Constants;

import android.accounts.Account;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;

/**
 * This class contains utility functions for determining the precedence of
 * different types associated with contact data items.
 *
 * @deprecated use {@link EntityModifier#getTypePrecedence} instead, since this
 *             list isn't {@link Account} based.
 */
@Deprecated
public final class TypePrecedence {

    /* This utility class has cannot be instantiated.*/
    private TypePrecedence() {}

    //TODO These may need to be tweaked.
    private static final int[] TYPE_PRECEDENCE_PHONES = {
            Phone.TYPE_CUSTOM,
            Phone.TYPE_MOBILE,
            Phone.TYPE_HOME,
            Phone.TYPE_WORK,
            Phone.TYPE_OTHER,
            Phone.TYPE_FAX_HOME,
            Phone.TYPE_FAX_WORK,
            Phone.TYPE_PAGER};

    private static final int[] TYPE_PRECEDENCE_EMAIL = {
            Email.TYPE_CUSTOM,
            Email.TYPE_HOME,
            Email.TYPE_WORK,
            Email.TYPE_OTHER};

    private static final int[] TYPE_PRECEDENCE_POSTAL = {
            StructuredPostal.TYPE_CUSTOM,
            StructuredPostal.TYPE_HOME,
            StructuredPostal.TYPE_WORK,
            StructuredPostal.TYPE_OTHER};

    private static final int[] TYPE_PRECEDENCE_IM = {
            Im.TYPE_CUSTOM,
            Im.TYPE_HOME,
            Im.TYPE_WORK,
            Im.TYPE_OTHER};

    private static final int[] TYPE_PRECEDENCE_ORG = {
            Organization.TYPE_CUSTOM,
            Organization.TYPE_WORK,
            Organization.TYPE_OTHER};

    /**
     * Returns the precedence (1 being the highest) of a type in the context of it's mimetype.
     *
     * @param mimetype The mimetype of the data with which the type is associated.
     * @param type The integer type as defined in {@Link ContactsContract#CommonDataKinds}.
     * @return The integer precedence, where 1 is the highest.
     */
    @Deprecated
    public static int getTypePrecedence(String mimetype, int type) {
        int[] typePrecedence = getTypePrecedenceList(mimetype);
        if (typePrecedence == null) {
            return -1;
        }

        for (int i = 0; i < typePrecedence.length; i++) {
            if (typePrecedence[i] == type) {
                return i;
            }
        }
        return typePrecedence.length;
    }

    @Deprecated
    private static int[] getTypePrecedenceList(String mimetype) {
        if (mimetype.equals(Phone.CONTENT_ITEM_TYPE)) {
            return TYPE_PRECEDENCE_PHONES;
        } else if (mimetype.equals(Constants.MIME_SMS_ADDRESS)) {
            return TYPE_PRECEDENCE_PHONES;
        } else if (mimetype.equals(Email.CONTENT_ITEM_TYPE)) {
            return TYPE_PRECEDENCE_EMAIL;
        } else if (mimetype.equals(StructuredPostal.CONTENT_ITEM_TYPE)) {
            return TYPE_PRECEDENCE_POSTAL;
        } else if (mimetype.equals(Im.CONTENT_ITEM_TYPE)) {
            return TYPE_PRECEDENCE_IM;
        } else if (mimetype.equals(Organization.CONTENT_ITEM_TYPE)) {
            return TYPE_PRECEDENCE_ORG;
        } else {
            return null;
        }
    }


}
