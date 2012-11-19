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

package com.android.phone;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.os.Bundle;

import android.widget.ImageView;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.Phone;

public class MobileNetworksGeminiTabActivity extends TabActivity implements
		OnTabChangeListener {
	private static final String TAG = "MobileNetworksGeminiTabActivity";
	private static final boolean DBG = true;

	private static final int INTENT_SIM1_INT_EXTRA = 0;
	private static final int INTENT_SIM2_INT_EXTRA = 1;
	private static final int TAB_SIM_1 = 0;
	private static final int TAB_SIM_2 = 1;

	private TabHost mTabHost;
	private GeminiPhone mGeminiPhone;
	private boolean Sim1State = false;
	private boolean Sim2State = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGeminiPhone = (GeminiPhone)PhoneFactory.getDefaultPhone();
		Sim1State = mGeminiPhone.isSimInsert(Phone.GEMINI_SIM_1);
		Sim2State = mGeminiPhone.isSimInsert(Phone.GEMINI_SIM_2);

		mTabHost = getTabHost();
		mTabHost.setOnTabChangedListener(this);

		SetupSIM1Tab();
		SetupSIM2Tab();
		SetCurrentTab();
	}

	private void SetCurrentTab() {
		ImageView view;
		Intent invokeIntent = getIntent();
        int simId = invokeIntent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, 3);

        if (simId == 0 || simId == 1)
        {
            mTabHost.setCurrentTab(simId);
        }
        else
        {
		if (!Sim1State && Sim2State) {
			if(DBG) log("SIM1 is inserted");
			mTabHost.setCurrentTab(TAB_SIM_2);
		} else {
			mTabHost.setCurrentTab(TAB_SIM_1);
		}
	}
	}
	
	private void SetupSIM1Tab() {
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.setClass(this, Settings.class);
		intent.putExtra(Phone.GEMINI_SIM_ID_KEY, INTENT_SIM1_INT_EXTRA);
		mTabHost.addTab((mTabHost.newTabSpec(getString(R.string.gemini_mobile_network_sim1_tab_tag)))
				.setIndicator(getString(R.string.gemini_mobile_network_sim1_tab_label), getResources().getDrawable(R.drawable.tab_manage_sim1))
				.setContent(intent));
	}

	private void SetupSIM2Tab() {
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.setClass(this, Settings.class);
		intent.putExtra(Phone.GEMINI_SIM_ID_KEY, INTENT_SIM2_INT_EXTRA);
		mTabHost.addTab((mTabHost.newTabSpec(getString(R.string.gemini_mobile_network_sim2_tab_tag)))
				.setIndicator(getString(R.string.gemini_mobile_network_sim2_tab_label), getResources().getDrawable(R.drawable.tab_manage_sim2))
				.setContent(intent));
	}

	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		Activity activity = getLocalActivityManager().getActivity(tabId);
		if(activity != null){
		        activity.onWindowFocusChanged(true);
		}
	}
	
	private void log(String msg){
		Log.d(TAG, msg);
	}
}
