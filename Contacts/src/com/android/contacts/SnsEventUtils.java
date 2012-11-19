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
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.text.SpannableStringBuilder;
import com.mediatek.client.DataManager;
import com.mediatek.client.SnsClientAPI;
import com.mediatek.client.DataManager.SnsUser;
import com.mediatek.client.DataManager.WidgetEvent;
import com.mediatek.wsp.util.*;
import android.content.res.Resources;

import static com.android.contacts.SnsEventDataBase.AdapterFromType;

public class SnsEventUtils {
	
	static ArrayList<HashMap<String, Object>> mSavedListPro = null;
	public static Resources resources=null;
	public static String TAG="SnsEventUtils::";
	public static boolean parseEventData(Context context,WidgetEvent[] events, boolean isAppended,ArrayList<HashMap<String, Object>> list) 
	{
		if(context==null)
		{
			SNSLog.write_ERROR(TAG+"parseEventData context is null");
			return false;
		}
		resources=context.getResources();
		if(resources==null)
		{
			SNSLog.write_ERROR(TAG+"parseEventData resources is null");
			return false;
		}
		if(list == null || events == null || events.length == 0)
		{
			list = null;
			return false;
		}

		if(!isAppended)
		{
			list.clear();
		}

		for (int i = 0; i < events.length; i++) 
		{
			// if this event is null, skip the below statement
			if(events[i] == null)
			{
				continue;
			}
			HashMap<String, Object> map = new HashMap<String, Object>();

			if(events[i].photo_url == null)
			{
				map.put(SnsEventDataBase.adapterFrom[0],"default");
			}
			else
			{
				map.put(SnsEventDataBase.adapterFrom[0],events[i].photo_url);
			}
			
			int logoResId = 0;
    		switch(events[i].sns_id)
    		{
    		case 1: logoResId = R.drawable.kaixin_logo_icon_48x48;
    				break;
    		case 2: logoResId = R.drawable.renren_logo_icon_48x48;
    				break;
    		case 3: logoResId = R.drawable.twitter_logo_48;
    				break;
    		case 4: logoResId = R.drawable.flickr_logo_48;
    				break;
    		case 5: logoResId = R.drawable.facebook_logo_48;
    				break;
    		default:logoResId = R.drawable.renren_logo_icon_48x48;
    				break;
    		}
      		
			map.put(SnsEventDataBase.adapterFrom[1], logoResId);

			
			
			if(events[i].event_time != null)
			{
				String time = events[i].event_time.substring(5,events[i].event_time.length()-3);
				map.put(SnsEventDataBase.adapterFrom[3], time);
			}
			else
			{
				map.put(SnsEventDataBase.adapterFrom[3], "");
			}

			String title,userName,act=null,pic;
			title = events[i].title;
			userName = events[i].user_name;
			//act = events[i].action;
			Integer snsId=events[i].sns_id;
			Integer typeId=events[i].event_type;
			if(snsId!=null&&snsId>0&&typeId!=null&&typeId>0)
			{
				switch(snsId)
				{
				case 1:
					act=kaixinEventType(typeId);
				    break;
				case 2:
					act=renrenEventType(typeId);
					break;
				case 3:
					act=twitterEventType(typeId);
					break;
				case 4:
					act=flickrEventType(typeId);
					break;
				case 5:
					act=fbEventType(typeId);
					break;
				}
			}
			pic = events[i].event_pic;

			if(title == null )
			{
				title = "";
			}
			else
			{
				title = formatHtmlString(Html.toHtml(new SpannableStringBuilder(title)));
			}
			if(userName == null)
			{
				Log.e("","WidgetTools:parseEventData  events.user_name==null,so reset it \""+SnsEventDataBase.defaultName+"\"");
				userName = SnsEventDataBase.defaultName;
			}
			if(act == null)
			{
				Log.e("","WidgetTools:parseEventData  events.action==null,so reset it a space string");
				act = "";
			}
			else 
			{
				act = formatHtmlString(Html.toHtml(new SpannableStringBuilder(act)));
			}
			
			userName = formatHtmlString(Html.toHtml(new SpannableStringBuilder(userName)));
			
			String s = null;
			String color = Integer.toHexString(context.getResources().getColor(R.color.EventFriendNameColor)&0x00ffffff);
			s = "<font color=\"#"+color+"\" >"+userName+"</font>        "+act+((title.equals("")||act.equals(""))?"":":")+title;
			
			if(null != s)
				map.put(SnsEventDataBase.adapterFrom[4], Html.fromHtml(s));
			
			String eventPhotoPath = events[i].event_pic;
			int viewType = 0;
			if(eventPhotoPath == null || eventPhotoPath.length()<1)
			{
				eventPhotoPath = null;
				viewType = 0;
			}
			else
			{
				if(eventPhotoPath.trim().startsWith("/sdcard/"))
				{
					viewType = 1;
				}
				else
				{
					viewType = 0;
				}
			}
			map.put(SnsEventDataBase.adapterFrom[2], eventPhotoPath);
			map.put(SnsEventDataBase.adapterFrom[5], viewType);
			
			map.put(SnsEventDataBase.adapterFrom[6], events[i].sns_id);
			
			map.put("account_id", events[i].account_id);
			map.put("sns_id", events[i].sns_id);
			map.put("event_id",events[i].event_id);
			map.put("event_type", events[i].event_type);
			
			list.add(map);
		}
		return true;
	}
	
	
	public static String formatHtmlString(String toHtmlString)
	{
		if(null == toHtmlString)
			return "";
		String sub = null;
		try
		{
			sub = toHtmlString.substring(3, toHtmlString.length()-5);
		}
		catch(Exception e)
		{
			sub = "";
			e.printStackTrace();
		}
		return sub;
	}
	
	public static ArrayList<HashMap<String, Object>> parseEventData(Context context,WidgetEvent[] events, boolean isAppended) 
	{
		ArrayList<HashMap<String, Object>> list;
		
		//if no data saved
		if(mSavedListPro == null)
		{
			mSavedListPro = new ArrayList<HashMap<String, Object>>();
		}
		
		
		if(events == null)
		{
			return null;
		}

		if(events.length == 0)
		{
			return null;
		}

		if(isAppended)
		{
			list = mSavedListPro;
		}
		else
		{
			list = new ArrayList<HashMap<String, Object>> ();
			SnsEventDataBase.events.clear();
		}
		//saved for later using
		for(int i=0; i<events.length; i++)
		{
			if(events[i] != null)
			{
				SnsEventDataBase.events.add(events[i]);
			}
			else
			{
				Log.i("SNS","WidgetTools:parseEventData  events["+i+"]==null");
			}
		}
		
		
		for (int i = 0; i < events.length; i++) 
		{
			// if this event is null, skip the below statement
			if(events[i] == null)
			{
				continue;
			}
			HashMap<String, Object> map = createMapFromEvent(context,events[i]);
			list.add(map);
		}
		
		mSavedListPro = list;
		
		Log.i("SNS","WidgetTools:parseEventData leave");
		return list;
	}
	
	
	private static HashMap<String, Object> createMapFromEvent(Context context,WidgetEvent event)
	{
		Log.i("SNS","WidgetTools:createMapFromEvent entry");
		HashMap<String, Object> map = new HashMap<String, Object>();

		Bitmap logBitmap = null;

		if(event.sns_id == null)
		{
			Log.i("SNS","WidgetTools:parseEventData  events.sns_id==null, so reset it 1");
			event.sns_id = 1;
		}
		
		Log.i("SNS","-------------------------->event.sns_id is "+ event.sns_id);		
		
		String from[] = SnsEventDataBase.adapterFrom;
	
		Log.i("SNS","-------------->event.photo_url:"+event.photo_url);
		Log.i("SNS","-------------->event_pic:"+event.event_pic);
		if(event.photo_url == null)
		{
			Log.i("SNS","WidgetTools:parseEventData  events.photo_url==null,so reset it \"default\"");
			map.put(from[AdapterFromType.HEADPORTRAIT], "default");
		}
		else
		{
			map.put(from[AdapterFromType.HEADPORTRAIT], event.photo_url);
		}


		map.put(from[AdapterFromType.WEBLOGO], event.sns_id==1?R.drawable.logo_kaixin:R.drawable.logo_renren);
		
		if(event.event_time != null)
		{
			String time = event.event_time.substring(5,event.event_time.length()-3);
			map.put(from[AdapterFromType.PUBLISHTIME], time);
		}
		else
		{
			Log.i("SNS","WidgetTools:parseEventData  events.event_time==null,so reset it a space string");
			map.put(from[AdapterFromType.PUBLISHTIME], "");
		}

		//name+space+title
		String title,userName,act=null;
		title = event.title;
		userName = event.user_name;
		//act = event.action;
		Integer snsId=event.sns_id;
		Integer typeId=event.event_type;
		if(snsId!=null&&snsId>0&&typeId!=null&&typeId>0)
		{
			switch(snsId)
			{
			case 1:
				act=kaixinEventType(typeId);
			    break;
			case 2:
				act=renrenEventType(typeId);
				break;
			case 3:
				act=twitterEventType(typeId);
				break;
			case 4:
				act=flickrEventType(typeId);
				break;
			case 5:
				act=fbEventType(typeId);
				break;
			}
		}
		if(title == null )
		{
			Log.i("SNS","WidgetTools:parseEventData  events.titie==null,so reset it a space string");
			title = "";
		}
		if(userName == null)
		{
			Log.i("SNS","WidgetTools:parseEventData  events.user_name==null,so reset it \""+SnsEventDataBase.defaultName+"\"");
			userName = SnsEventDataBase.defaultName;
		}
		if(act == null)
		{
			Log.i("SNS","WidgetTools:parseEventData  events.action==null,so reset it a space string");
			act = "";
		}
		
		
		String s = null;
		switch(event.is_read)
		{
		case 0:
			String color = Integer.toHexString(context.getResources().getColor(R.color.EventFriendNameColor)&0x00ffffff);
			s = "<font color=\"#"+color+"\" >"+userName+"</font>\t"+act+((title.equals("")||act.equals(""))?"":":")+title;
			break;
		case 1:
			String color1 = Integer.toHexString(context.getResources().getColor(R.color.EventReadColor)&0x00ffffff);
			s = "<font color=\"#"+color1+"\" >"+userName+"\t"+act+((title.equals("")||act.equals(""))?"":":")+title+"  </font>  ";
			break;
		}
		if(null != s)
			map.put(from[AdapterFromType.EVENTTITLE], Html.fromHtml(s));
		
		map.put(from[AdapterFromType.EVENTPHOTO], (event.event_pic==null || event.event_pic.length() <= 0 || event.event_pic.equals(" "))?null:Uri.parse(event.event_pic));

		map.put("account_id", event.account_id);
		map.put("sns_id", event.sns_id);
		map.put("event_id",event.event_id);
		map.put("event_type", event.event_type);		

		Log.i("SNS","WidgetTools:createMapFromEvent leave");
		return map;
	}
	
	
	public static int[] getMatachedLayout(ArrayList<HashMap<String,Object>> myList)
	{
		Log.i("SNS","WidgetTools:getMatachedLayout entry");
		
//		int eCount = myList.size();
//		int []matchedLayout = new int[eCount];
//		for(int i=0;i<eCount;i++)
//		{
//			Uri uri = (Uri) myList.get(i).get((SnsEventDataBase.adapterFrom)[4]);
//			
//			if(uri == null)
//				matchedLayout[i]=0;
//			else
//			{
//				String path = uri.toString();
//				if(path.contains("http"))
//				{
//					matchedLayout[i] = 0;
//				}
//				else
//				{
//					matchedLayout[i] = 1;
//				}
//			}
//		}
//		
//		Log.i("SNS","WidgetTools:getMatachedLayout leave");
//	
//		return matchedLayout;
		
		return new int[/*myList.size()*/ 200];
	}

	
	
	public static boolean isBitmapFormat(String path)
	{
		Bitmap bitmap = BitmapFactory.decodeFile(path);

		return bitmap==null?false:true;
	}


	public static Bitmap getBitmapFromLocalFile(String path)
	{
		Log.i("SNS","WidgetTools:getBitmapFromLocalFile entry");
		if(path == null)
			return null;
		Bitmap bitmap = null;
		bitmap = BitmapFactory.decodeFile(path);
		
		Log.i("SNS","WidgetTools:getBitmapFromLocalFile leave");
		return bitmap;
	}
	
	public static Bitmap getLogoBitmapBySnsId(Context context,int snsId)
	{
		if(context == null)
		{
			Log.i("SNS","WidgetTools:getLogoBitmapBySnsId  param:context is null!!!");
			return null;
		}
		
		Bitmap bitmap = null;
		switch(snsId)
		{
		case 1:
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_kaixin);
			break;
		case 2:
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_renren);
			break;
		default:
			Log.i("SNS","WidgetTools:getLogoBitmapBySnsId    unsupported snsId("+snsId+")");
			
		}
		Log.i("SNS","WidgetTools:getLogoBitmapBySnsId leave");
		return bitmap;
	}
	
	public static String kaixinEventType(Integer typeId)
	{
		switch(typeId)
		{
		case 1:
			return resources.getString(R.string.kaixin1);			
		case 2:
			return resources.getString(R.string.kaixin2);			
		case 3:
			return resources.getString(R.string.kaixin3);
		case 1018:
			return resources.getString(R.string.kaixin1018);
		case 1088:
			return resources.getString(R.string.kaixin1088);
		case 1016:
			return resources.getString(R.string.kaixin1016);
			default:
				return null;
		}
	}
	public static String renrenEventType(Integer typeId)
	{
		switch(typeId)
		{
		case 10:
		case 11:
			return resources.getString(R.string.renren10or11);
		case 20:
		case 22:
			return resources.getString(R.string.renren20or22);
		case 21:
		case 23:
			return resources.getString(R.string.renren21or23);
		case 30:
		case 31:
			return resources.getString(R.string.renren30or31);
		case 32:
		case 36:
			return resources.getString(R.string.renren32or36);
		case 33:
			return resources.getString(R.string.renren33);
		case 34:
		case 35:
			return resources.getString(R.string.renren34or35);
		case 40:
			return resources.getString(R.string.renren40);
		case 41:
			return resources.getString(R.string.renren41);
		case 51:
		case 54:
			return resources.getString(R.string.renren51or54);
			default:
				return null;
			
		}
	}
	public static String twitterEventType(Integer typeId)
	{
		switch(typeId)
		{
		case 100:
			return resources.getString(R.string.twitter100);
		case 200:
			return resources.getString(R.string.twitter200);
			default:
				return null;
		}
	}
	public static String fbEventType(Integer typeId)
	{
		switch(typeId)
		{
		case 80:
			return resources.getString(R.string.fb80);
		case 66:
			return resources.getString(R.string.fb66);
		case 12:
			return resources.getString(R.string.fb12);
		case 46:
			return resources.getString(R.string.fb46);
		case 247:
			return resources.getString(R.string.fb247);
		case 100:
			return resources.getString(R.string.fb100);
			default:
				return null;
		}
	}
public static String flickrEventType(Integer typeId)
{
	return resources.getString(R.string.flickr100);
}

}
