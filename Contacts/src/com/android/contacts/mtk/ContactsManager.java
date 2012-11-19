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

package com.android.contacts.mtk;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import com.android.contacts.R;
import com.mediatek.client.DataManager.SnsUser;
import com.mediatek.client.DataManager;
import com.android.contacts.mtk.MySnsUser;
import com.mediatek.banyan.widget.MTKSNSEmotionParser;

public class ContactsManager {
	
	private static final String TAG = "ContactsManager";
	public static ArrayList<String> SNS_TYPE_LIST;
	
	public static SnsUser readStatusFromWSP(long contactID, Context activity, 
			String[] projection){
		if(contactID == (long)-1)
			return null;
    	Cursor rawc = activity.getContentResolver().query(RawContacts.CONTENT_URI, 
    			projection, RawContacts.CONTACT_ID + "=" + contactID, 
				null, null);
		ArrayList<SnsUser> raws = new ArrayList<SnsUser>();
		
		if (rawc != null) {
			String[] item = null;
			
            while (rawc.moveToNext()) {
                final String accountType = rawc.getString(2);
                final long rawContactId = rawc.getLong(0);
                if(accountType != null && SNS_TYPE_LIST.contains(accountType.toLowerCase())){
                	SnsUser temp = new SnsUser();
                	temp.account_id = rawc.getInt(4); // accountID 
                	temp.user_id = rawc.getString(5); // userID
                	raws.add(temp);
                }
            }
	    if(rawc != null) rawc.close();
            if(raws.size() == 0){
            	return null;
            }
            DataManager.setContentResolver(activity.getContentResolver());
            return DataManager.getSnsStatus(raws);
		}
    	return null;
    }
    
    	public static MySnsUser[] readMultiStatusFromWSP(ArrayList<Long> mContactIds, Context activity, 
			String[] projection){
		if(mContactIds == null || mContactIds.size() <= 0)
			return null;
		MySnsUser[] snsUsers = new MySnsUser[mContactIds.size()];
    		for(int i = 0; i < mContactIds.size(); i++){
			snsUsers[i] = new MySnsUser();
			snsUsers[i].contactID = mContactIds.get(i);
			snsUsers[i].mSnsUser = readStatusFromWSP(snsUsers[i].contactID, activity, projection);
		}
    		return snsUsers;
    	}

	public static void readSnsTypeList(Resources res){
		CharSequence[] strings;
		strings = res.getStringArray(R.array.sns_type_list);
		
		SNS_TYPE_LIST = new ArrayList<String>();
		for(int i = 0; i < strings.length; i++){
			SNS_TYPE_LIST.add((strings[i] + "").toLowerCase()); 
		}
	}
	
	public static CharSequence parserEmotion(CharSequence text, int textType) {
		if(text == null)
			return null;
		CharSequence textEmo;
		if (textType == 1)
		{
			
	    	if (MTKSNSEmotionParser.getInstance() != null)
	    	{
	            textEmo = MTKSNSEmotionParser.getInstance().parserKaixin(text);
	        }
	    	else
	    	{
	    		textEmo = text;
	    	}
		}
		else if (textType == 2)
		{
			if (MTKSNSEmotionParser.getInstance() != null)
	    	{
	            textEmo = MTKSNSEmotionParser.getInstance().parserRenren(text);
	        }
	    	else
	    	{
	    		textEmo = text;
	    	}
		}
		else
		{
			textEmo = text;
		}
		return textEmo;
	}
}
