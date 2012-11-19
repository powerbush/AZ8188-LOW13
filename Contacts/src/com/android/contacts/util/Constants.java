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

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Telephony.SIMInfo;
import android.content.Context;
import android.content.res.Resources;

import android.graphics.Color;

import java.util.Iterator;
/**
 * Background {@link Service} that is used to keep our process alive long enough
 * for background threads to finish. Started and stopped directly by specific
 * background tasks when needed.
 */
public class Constants {
    /**
     * Specific MIME-type for {@link Phone#CONTENT_ITEM_TYPE} entries that
     * distinguishes actions that should initiate a text message.
     */
    public static final String MIME_SMS_ADDRESS = "vnd.android.cursor.item/sms-address";
    public static final String MIME_PHONE_VT = "vnd.android.cursor.item/phone_vt";

    public static final String SCHEME_TEL = "tel";
    public static final String SCHEME_SMSTO = "smsto";
    public static final String SCHEME_MAILTO = "mailto";
    public static final String SCHEME_IMTO = "imto";
    public static final String SCHEME_SIP = "sip";

    public static SimInfo getSimInfoById(Context context, int simId){
    	SIMInfo info = SIMInfo.getSIMInfoById(context, simId);
    	SimInfo mSimInfo = null;
    	if( info != null){
    		String name = info.mDisplayName;
			String number = info.mNumber;
			int mSimId = (int)info.mSimId;
			int color = info.mColor;
			int slot = info.mSlot;
			int displayNumFormat = info.mDispalyNumberFormat;
			mSimInfo = new SimInfo(name,number,mSimId,color,slot,displayNumFormat);
    	}
    	return mSimInfo;
    }

    public static List<SimInfo> getInsertedSimList(Context context){    	
    	List<SimInfo> mSimList;
    	mSimList = new ArrayList<SimInfo>();
    	
    	List<SIMInfo> simInfos = SIMInfo.getInsertedSIMList(context);
    	
		if(simInfos.isEmpty())return mSimList;
		for(SIMInfo info:simInfos){
			String name = info.mDisplayName;
			String number = info.mNumber;
			int simId = (int)info.mSimId;
			int color = info.mColor;
			int slot = info.mSlot;
			int displayNumFormat = info.mDispalyNumberFormat;
			mSimList.add(new SimInfo(name,number,simId,color,slot,displayNumFormat));
		}
		return mSimList;
    }
    
    public static final class SimInfo {
		public int simId;
		public String label;
		public String number;
		public int color;
		public int slot;
		public int displayNumberFormat;

		public SimInfo() {
		};

		public SimInfo(String label, String number) {
			this.label = label;
			this.number = number;
		}
		
		public SimInfo(String label, String number,int simId) {
			this.label = label;
			this.number = number;
			this.simId = simId;
		}
		public SimInfo(String label, String number,int simId,int color) {
			this.label = label;
			this.number = number;
			this.simId = simId;
			this.color = color;
		}

		public SimInfo(String label, String number,int simId,int color,int slot) {
			this.label = label;
			this.number = number;
			this.simId = simId;
			this.color = color;
			this.slot = slot;
		}
		
		public SimInfo(String label, String number,int simId,int color,int slot,int displayNumberFormat) {
			this.label = label;
			this.number = number;
			this.simId = simId;
			this.color = color;
			this.slot = slot;
			this.displayNumberFormat = displayNumberFormat;
		}

	}

    public static final class NumberInfo {
		public String name;
		public String number;
		public int simId;
		public int dataId;

		public NumberInfo() {
			this.simId = 0;
		};

		public NumberInfo(String name, String number) {
			this.name = name;
			this.number = number;
			this.simId = 0;
		}
		public NumberInfo(String name, String number,int simId) {
			this.name = name;
			this.number = number;
			this.simId = simId;
		}

	}

}
