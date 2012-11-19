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

import java.util.ArrayList;


import com.mediatek.client.DataManager.SNSAccountInfo;
import com.mediatek.client.DataManager.SnsUser;
import com.mediatek.client.DataManager.WidgetEvent;

public class SnsEventDataBase 
{
	
	public interface AdapterFromType
	{
		int HEADPORTRAIT = 0;
		int WEBLOGO = 1;
		int PUBLISHTIME = 2;
		int EVENTTITLE = 3; 
		int EVENTPHOTO = 4; 
	}
	
	
	
	public static ArrayList< WidgetEvent> events = new ArrayList<WidgetEvent>();
	public static int footPosition;
	public static boolean isNoEventTipVisible = false;
	public static SnsUser lastDisplayAccount = null;
	
	public static SNSAccountInfo allAccountInfo[] ;
	public static int allMessageNumber[] ;
	
	public static int initEventNumber = 20;
	public static int appendEventNumber = 20;
	public static SnsUser favoriteUser = null;
	
	public static boolean isRefreshNow = false;
	
	public static boolean NoMoreEventLeft = false;
	
	public static boolean isFavoriteDisplayNow = false;
	public static Integer[] currentShowAccountId = null;
	public static Long[] currentShowUserId = null;
	
	public static boolean isFirstSetFootView = true;
	
	public static int refreshManuallyNumber = 20;
	
	public static String allShowName = "Events from all Favorite Friends";
	public static String allShowStatus = "Enjoy yourself!";
	
	public static int defaultPicUrl = R.drawable.wgt_sns_normal_avatar;
	public static String defaultStatus = "No news is good news!";
	public static String defaultName = "NameSecreter";

	public static String adapterFrom[] = new String[]{"HeadPortrait","WebLogo","EventPhoto","PublishTime","Title","ViewLayoutType","EventsDisplayerLVItemType"};
	public static int adapterTo[] = new int[]{R.id.EventFriendHeadPortrait,R.id.EventWebLog,R.id.EventAttachPhoto,R.id.EventPublishTimeTV,R.id.EventTitleTV,0,0};
	
}
