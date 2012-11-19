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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.RemoteException;
import android.util.Log;
import android.app.ActivityManagerNative;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class VTCallUtils {
	
	private static final String LOG_TAG = "VTCallUtils";
    private static final boolean DBG = true;// (PhoneApp.DBG_LEVEL >= 2);
    
    /**
     * Video Call will control some resource, such as Camera, Media.
     * So Phone App will broadcast Intent to other APPs before acquire and after release the resource.
     * Intent action:
     * Before - "android.phone.extra.VT_CALL_START"
     * After - "android.phone.extra.VT_CALL_END"
     */
    public static final String VT_CALL_START = "android.phone.extra.VT_CALL_START";
    public static final String VT_CALL_END = "android.phone.extra.VT_CALL_END";
    
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
	
    /**
     * for InCallScreen to update VT UI
     */
	static enum VTScreenMode{
		VT_SCREEN_CLOSE,
		VT_SCREEN_RINGING,
		VT_SCREEN_HIDING,
		VT_SCREEN_OPEN
	}
	
	static void showVTIncomingCallUi() {
        if (DBG) log("showVTIncomingCallUi()...");
        
        VTSettingUtils.getInstance().updateVTEngineerModeValues();
        
        PhoneApp app = PhoneApp.getInstance();

        try {
            ActivityManagerNative.getDefault().closeSystemDialogs("call");
        } catch (RemoteException e) {
        }

        app.preventScreenOn(true);
        app.requestWakeState(PhoneApp.WakeState.FULL);

        if (DBG) log("- updating notification from showVTIncomingCall()...");
        NotificationMgr.getDefault().updateInCallNotification();

        app.displayVTCallScreen();
    }
	
	public static void checkVTFile()
	{
		if (DBG) log("checkVTFile() ! ");
		if( !(new File( VTAdvancedSetting.getPicPathDefault() ).exists()) )
    	{
			if (DBG) log("checkVTFile() : the default pic file not exists , create it ! ");
    		
    		try {
    			Bitmap btp1 = BitmapFactory.decodeResource(PhoneApp.getInstance().getResources(), R.drawable.vt_incall_pic_qcif);
    			VTCallUtils.saveMyBitmap( VTAdvancedSetting.getPicPathDefault() , btp1 );
    			btp1.recycle();
    			if (DBG) log(" - Bitmap.isRecycled() : " + btp1.isRecycled() );
			} catch (IOException e) {
				e.printStackTrace();
			}
   		
    	}
    	
    	if( !(new File( VTAdvancedSetting.getPicPathUserselect() ).exists()) )
    	{
    		if (DBG) log("checkVTFile() : the default user select pic file not exists , create it ! ");
    		
    		try {
    			Bitmap btp2 = BitmapFactory.decodeResource(PhoneApp.getInstance().getResources(), R.drawable.vt_incall_pic_qcif);
        		VTCallUtils.saveMyBitmap( VTAdvancedSetting.getPicPathUserselect() , btp2 );
        		btp2.recycle();
        		if (DBG) log(" - Bitmap.isRecycled() : " + btp2.isRecycled() );
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
	}
	
	public static void saveMyBitmap(String bitName, Bitmap bitmap) throws IOException {
	
		if (DBG) log("saveMyBitmap()...");
		
        File f = new File( bitName );
        f.createNewFile();
        FileOutputStream fOut = null;
        
        try {
                fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
                e.printStackTrace();
        }
        
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
                fOut.flush();
        } catch (IOException e) {
                e.printStackTrace();
        }
        
        try {
                fOut.close();
        } catch (IOException e) {
                e.printStackTrace();
        }
	} 

}