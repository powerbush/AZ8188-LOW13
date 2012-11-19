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

package com.android.mms.ui;

import com.android.mms.R;
import android.R.color;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.Phone;
import android.provider.Telephony;
import android.os.SystemProperties;

public class AdvancedCheckBoxPreference extends CheckBoxPreference{

	public interface GetSimInfo {

		CharSequence getSimNumber(int i);

		CharSequence getSimName(int i);

		int getSimColor(int i);
		
		int getNumberFormat(int i);
		
		int getSimStatus(int i);
		
		boolean is3G(int i);
	}

	private static int currentId = 0; // for object reference count;
	private static int maxCount = 0;
	private static final String TAG = "AdvancedCheckBoxPreference";
	
	private static TextView simName[];
	private static TextView simNumber[];
	private static TextView simNumberShort[];
	private static TextView sim3G[];
	private static ImageView simStatus[];
	private static ImageView simColor[];
	static GetSimInfo simInfo;
	public static void init(Context context, int count) {
		simInfo = (GetSimInfo) context;
		maxCount = count;
	}
	//private final GetSimInfo simInfo = ;
    public AdvancedCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.CheckBoxPreference, defStyle, 0);
        a.recycle();
    }
    
    public AdvancedCheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.checkBoxPreferenceStyle);
    }
    
    public AdvancedCheckBoxPreference(Context context) {
        this(context, null);
    }
    
    @Override
    protected View onCreateView(ViewGroup parent) {
        final LayoutInflater layoutInflater =
            (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.advanced_checkbox_preference, parent, false); 

		simName = new TextView[maxCount];
		simNumber = new TextView[maxCount];
		simNumberShort = new TextView[maxCount];
		simStatus = new ImageView[maxCount];
        simColor = new ImageView[maxCount];
		sim3G = new TextView[maxCount];
        if (currentId < maxCount) {
        	currentId++;
        } else {
        	//reset to 1
        	currentId = 1;
        }
        return layout;
    }
	
    @Override
    //called when we're binding the view to the preference.
    protected void onBindView(View view) {
        super.onBindView(view);
        simName[currentId-1] = (TextView) view.findViewById(R.id.simName);
        simNumber[currentId-1] = (TextView) view.findViewById(R.id.simNumber);
        simNumberShort[currentId-1] = (TextView) view.findViewById(R.id.simNumberShort);
        simStatus[currentId-1] = (ImageView) view.findViewById(R.id.simStatus);
        simColor[currentId-1] = (ImageView) view.findViewById(R.id.simIcon);
        sim3G[currentId-1] = (TextView) view.findViewById(R.id.sim3g);
        // here need change to common usage
        simName[currentId-1].setText(simInfo.getSimName(currentId-1));
        simNumber[currentId-1].setText(simInfo.getSimNumber(currentId-1));
        String numShow = (String) simInfo.getSimNumber(currentId-1);
        
        if (simInfo.getNumberFormat(currentId-1) == android.provider.Telephony.SimInfo.DISPLAY_NUMBER_FIRST) {
            if (numShow != null && numShow.length()>4) {
                simNumberShort[currentId-1].setText(numShow.substring(0, 4));
            } else {
            	simNumberShort[currentId-1].setText(numShow);
            }
        } else if (simInfo.getNumberFormat(currentId-1) == android.provider.Telephony.SimInfo.DISPLAY_NUMBER_LAST) {
        	if (numShow != null && numShow.length()>4) {
        	    simNumberShort[currentId-1].setText(numShow.substring(numShow.length() - 4));
            } else {
            	simNumberShort[currentId-1].setText(numShow);
            }
        } else {
        	simNumberShort[currentId-1].setText("");
        }
        
        int simStatusResourceId = MessageUtils.getSimStatusResource(simInfo.getSimStatus(currentId-1));
        if (-1 != simStatusResourceId) {
            simStatus[currentId-1].setImageResource(simStatusResourceId);
        }
        simColor[currentId-1].setBackgroundResource(simInfo.getSimColor(currentId-1));
        // show the first 3G slot
        if (simInfo.is3G(currentId-1)) {
        	String optr = SystemProperties.get("ro.operator.optr");
            if (optr.equals("OP02")) {
        	    sim3G[currentId-1].setVisibility(View.VISIBLE);
            } else {
            	sim3G[currentId-1].setVisibility(View.GONE);
            }
        }
    }
}
