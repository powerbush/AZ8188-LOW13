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
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.mms.ui;

import com.android.mms.R;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.app.TabActivity;
import android.content.res.Resources;
import android.widget.TabHost;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ITelephony;
import android.os.ServiceManager;
import android.os.RemoteException;


/**
 * Displays a list of the SMS messages stored on the ICC.
 */
public class SimMessagesTab extends TabActivity{


	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

        Intent invokeIntent = getIntent();
        int simId = invokeIntent.getIntExtra(
                Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);

	    setContentView(R.layout.sim_tab);
	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab    

        ITelephony iTelephony = ITelephony.Stub.asInterface(ServiceManager.getService("phone"));

        

	    // Create an Intent to launch an Activity for the tab (to be reused)    
	    intent = new Intent().setClass(this, ManageSimMessages.class);    
	    // Initialize a TabSpec for each tab and add it to the TabHost    
	    spec = tabHost.newTabSpec("Sim1").setIndicator("SIM 1",                      
	            res.getDrawable(R.drawable.tab_manage_sim1))                  
	        .setContent(intent);    
        tabHost.addTab(spec);    // Do the same for the other tabs    

	    intent = new Intent().setClass(this, ManageSim2Messages.class);    
	    spec = tabHost.newTabSpec("Sim2").setIndicator("SIM 2",                      
	            res.getDrawable(R.drawable.tab_manage_sim2))                  
	        .setContent(intent);    
	    tabHost.addTab(spec);

        try{
            if(simId == Phone.GEMINI_SIM_2){
        	    tabHost.setCurrentTab(Phone.GEMINI_SIM_2);
            }else if(TelephonyManager.getDefault().hasIccCardGemini(Phone.GEMINI_SIM_1) && 
                     iTelephony.isRadioOnGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1)){
        	    tabHost.setCurrentTab(Phone.GEMINI_SIM_1);
            }else if(TelephonyManager.getDefault().hasIccCardGemini(Phone.GEMINI_SIM_2) && 
                     iTelephony.isRadioOnGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2)){
        	    tabHost.setCurrentTab(Phone.GEMINI_SIM_2);
            }else{
                tabHost.setCurrentTab(Phone.GEMINI_SIM_1);
            }
        }catch(RemoteException e){
            tabHost.setCurrentTab(Phone.GEMINI_SIM_1);        
        }
    }



}

