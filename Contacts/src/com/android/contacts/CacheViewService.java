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

package com.android.contacts;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.os.IBinder;
import android.util.Log;
import com.mediatek.featureoption.FeatureOption;

public class CacheViewService extends Service {
	private static final String TAG = "CacheViewService";
	static View CachedDialerView;
	
	public void onCreate(Bundle icicle) {
		Log.i(TAG, "onCreate()");
	}

	public void onStart(Intent intent, int StartId) {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            Log.e(TAG, "getContentViewResource : gemini  ");
			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT) {
				CachedDialerView = View.inflate(this, R.layout.twelve_key_dialer_with_search_gemini, null);
			} else if(FeatureOption.MTK_VT3G324M_SUPPORT) {
				CachedDialerView = View.inflate(this, R.layout.twelve_key_dialer_gemini_vt, null);
            } else {
            	CachedDialerView = View.inflate(this, R.layout.twelve_key_dialer_gemini, null);
            }
        } else {
            Log.e(TAG, "getContentViewResource : single sim ");
			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT) {
				CachedDialerView = View.inflate(this, R.layout.twelve_key_dialer_with_search_gemini, null);
			} else {            
            	CachedDialerView = View.inflate(this, R.layout.twelve_key_dialer, null);
        	}
        }
	}
	public void onDestroy() {
		super.onDestroy();
		CachedDialerView = null;
	}
	public IBinder onBind(Intent intent) {
		return null;
	}
}
