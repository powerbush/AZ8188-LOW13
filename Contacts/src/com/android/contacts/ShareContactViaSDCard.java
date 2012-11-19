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

import com.android.contacts.ContactsMarkListActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.net.Uri;
import android.util.Log;
import android.database.Cursor;
import android.content.ContentResolver;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.app.AlertDialog;
import android.os.Environment;
import java.io.File;
import android.content.DialogInterface;
import android.os.StatFs;

public class ShareContactViaSDCard extends Activity {
	
	private static final String TAG = "ShareContactViaSDCard";
	private String mAction;
	private Uri dataUri;
	private int singleContactId = -1;
	
	static final String[] CONTACTS_PROJECTION = new String[] { Contacts._ID, // 0
		Contacts.DISPLAY_NAME_PRIMARY, // 1
		Contacts.DISPLAY_NAME_ALTERNATIVE, // 2
		Contacts.SORT_KEY_PRIMARY, // 3
		Contacts.DISPLAY_NAME, // 4
    };
	
	static final int PHONE_ID_COLUMN_INDEX = 0;
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        mAction = intent.getAction();
        singleContactId = intent.getIntExtra("contactId", -1);
        Log.i(TAG,"mAction is " + mAction);
        if (Intent.ACTION_SEND.equals(mAction) && intent.hasExtra(Intent.EXTRA_STREAM)) {
        	String type = intent.getType();
        	dataUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        	Log.i(TAG,"dataUri is " + dataUri);
        	Log.i(TAG,"type is " + type);        
            if (dataUri != null && type != null) {
            	shareViaSDCard();
            }
        }
			finish();
			return;
    }
    
    public void shareViaSDCard() {
		StringBuilder contactsID = new StringBuilder();
		int curIndex = 0;
		Cursor cursor = null;
		String id = null;
		if (singleContactId == -1) {
			cursor = getContentResolver().query(/*dataUri*/Contacts.CONTENT_URI, CONTACTS_PROJECTION, null, null, null);
			Log.i(TAG,"cursor is " + cursor);
			if (null != cursor) {
				while (cursor.moveToNext()) {				
					if (cursor != null) id = cursor.getString(PHONE_ID_COLUMN_INDEX);
					Log.i(TAG,"id is " + id);
//					if (null == id) {
//						id = "";
//						Log.i(TAG, "OnClick contactId is null");
//						return;
//					}
//					if (TextUtils.isEmpty(id)) {
//						Log.i(TAG, "OnClick contactId is empty");
//						return;
//					}
					if (curIndex++ != 0) {
						contactsID.append("," + id);
					} else {
						contactsID.append(id);
					}
				}
				cursor.close();
			}
		} else {			
			id = Integer.toString(singleContactId);
			contactsID.append(id);
		}
		Intent it = new Intent(this, ExportVCardActivity.class);
		it.putExtra("multi_export_type", 1);
		it.putExtra("multi_export_contacts", contactsID.toString());
		this.startActivity(it);
			finish();
			return;
        }

	private boolean checkSDCardAvaliable() {
		return (Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED));
	}

	private boolean isSDCardFull() {
        String state = Environment.getExternalStorageState(); 
               if(Environment.MEDIA_MOUNTED.equals(state)) { 
                   File sdcardDir = Environment.getExternalStorageDirectory(); 
                   StatFs sf = new StatFs(sdcardDir.getPath());
                   long availCount = sf.getAvailableBlocks(); 
                   if(availCount>0){
                       return false;
                   } else {
                       return true;
                   }
               } 

		return true;
	}
	
    private class CancelListener
    implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
	public void onClick(DialogInterface dialog, int which) {
	    finish();
	}
	public void onCancel(DialogInterface dialog) {
		finish();
	    }
	}

	private CancelListener mCancelListener = new CancelListener();

}