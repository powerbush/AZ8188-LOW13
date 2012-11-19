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

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

public class PhoneOwner {

	private static PhoneOwner mOwner = null;
	private String mName = null;
	private String mStatus = null;
	private String mSnsLogo = null;
	private String mPhoto = null;
	private Long mOwnerID = null;
	private String mOwnerLookupKey = null;
	private Bitmap mPhotoBitmap = null;
	private Uri mPhotoUri = null;
	
	private PhoneOwner(){
		
	}
	
	static public void initPhoneOwner(Long ownerID, String ownerLookupKey){
		if((long)-1 != ownerID){
			if(null == mOwner)
				mOwner = new PhoneOwner();
			mOwner.setOwnerID(ownerID);
			mOwner.setOwnerLookupKey(ownerLookupKey);
		}
		else{
			mOwner = null;
			return;
		}
	}
	
	static public PhoneOwner getInstance(){
		return mOwner;
	}

	public static PhoneOwner getOwner() {
		return mOwner;
	}

	public Long getOwnerID() {
		return mOwnerID;
	}

	public void setOwnerID(Long mOwnerID) {
		this.mOwnerID = mOwnerID;
	}

	public String getOwnerLookupKey() {
		return mOwnerLookupKey;
	}

	public void setOwnerLookupKey(String mOwnerLookupKey) {
		this.mOwnerLookupKey = mOwnerLookupKey;
	}

	public static void setOwner(PhoneOwner mOwner) {
		PhoneOwner.mOwner = mOwner;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public String getStatus() {
		return mStatus;
	}

	public void setStatus(String mStatus) {
		this.mStatus = mStatus;
	}

	public String getPhoto() {
		return mPhoto;
	}

	public void setPhoto(String mPhoto) {
		this.mPhoto = mPhoto;
	}
	
	public void setPhoto(Bitmap bitmap){
		mPhotoBitmap = bitmap;
	}
	
	public String getSnsLogo(){
		return mSnsLogo;
	}
	
	public void setSnsLogo(String mSnsLogo){
		this.mSnsLogo = mSnsLogo;
	}
	
	public Uri getPhotoUri(){
		return mPhotoUri;
	}

	public void setPhotoUri(Uri photoUri){
		this.mPhotoUri = photoUri;
	}
}
