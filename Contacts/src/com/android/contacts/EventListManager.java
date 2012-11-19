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

import java.io.IOException;
import com.android.contacts.Collapser.Collapsible;
import com.android.contacts.model.ContactsSource;
import com.android.contacts.model.Sources;
import com.android.contacts.model.ContactsSource.DataKind;
import com.android.contacts.ui.EditContactActivity;
import com.android.contacts.util.Constants;
import com.android.contacts.util.DataStatus;
import com.android.contacts.util.NotifyingAsyncQueryHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.ParseException;
import android.net.Uri;
import android.net.WebAddress;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.text.Html;
import android.text.SpannableStringBuilder;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.client.DataManager;
import com.mediatek.client.DataManager.WidgetEvent;
import com.mediatek.client.DataManager.SnsUser;
import android.graphics.BitmapFactory;
import android.view.animation.AnimationUtils;
import java.util.Map;
import android.widget.AdapterView.OnItemClickListener;
import com.mediatek.client.SnsClientAPI;
import android.widget.SimpleAdapter;
import android.content.Intent;
import com.mediatek.wsp.util.Util;

public class EventListManager 
	 {
		 Activity								mActivity;
		 ListView								mSnsContactEventList = null;
		 Integer 								mCurrentPage;
		 ArrayList<HashMap<String,Object>> 		myList = null;
		 Integer 								mLimitNum;
		 private static final int 				DEFAULT_LIMIT = 20;
		 private static final int				ERROR = -1;
		 private static final int 				SUCCESS = 0;
		 private static final int 				ERROR_PARAM_INVALID = 1;
		 private static final int 				ERROR_NO_EVENT = 2;
		 private static final int 				ERROR_END_EVENT = 3;
		 private static final int 				ERROR_PARSER_ERROR = 4;
		 private int 							mFootViewPosition = 0;
		 private boolean 						mLoadingFlag = false;
		 MySimpleAdapter						mAdapter = null;
		 ArrayList<HashMap<String,Object>> 		tmpList = null;
		 private int[]							resource = null;
		 private int[] 							sr = null;
		 private Context 						mContext;
		 private Integer[] 						accountid;
		 private String[] 						userId;
		 
		 
		 public EventListManager()
		 {
			 mCurrentPage = 0;
			 mLimitNum = DEFAULT_LIMIT;
		 }

		 public EventListManager(Context context,Activity activity,int ListresourceId,int[] itemresourceId,int limit)
		 {
			 this.mActivity = activity;
			 this.mContext = context;
		         
			 DataManager.setContentResolver(mContext.getContentResolver());
			 
			 this.mSnsContactEventList = (ListView)mActivity.findViewById(ListresourceId);
			 if(this.mSnsContactEventList == null)
			 {
				Log.i("EventListManager","this.mSnsContactEventList == null");
			 }
			
			 if(mSnsContactEventList != null) 
			 mSnsContactEventList.setOnItemClickListener(new OnItemClickListener()
			 {
				 public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) 
				 {
					 
					 if(arg2 == mFootViewPosition)
					 {
						 if(mLoadingFlag)
						 {
							 return;
						 }
						 mLoadingFlag = true;
						 
						 final TextView tv = (TextView)arg1.findViewById(R.id.foot_tv);
						 if(tv != null)
						 {
							 tv.setText(R.string.event_loading);
						 }	
						 
						 
						 final Handler handler = new Handler()
						 {
							 @Override
							 public void handleMessage(Message msg)
							 {
								 switch(msg.what)
								 {
								 case SUCCESS:
									 if(tv != null)
									 {
										 tv.setText(R.string.loadmore);
									 }
									 show(false);
									 break;
								 case ERROR_NO_EVENT:
								 case ERROR_END_EVENT:
									 if(tv != null)
									 {
										 tv.setText(R.string.No_more_event_to_load);
									 }	
									 break;
								 }
								 mLoadingFlag = false;
									
							 }
						 };
						 
						 new Thread()
						 {
							 public void run()
							 {
								 int ret = getSnsEvent(accountid,userId,false);
								 handler.sendEmptyMessage(ret);
							 }
						 }.start();
						 return;
					 }
					 else if(arg2 > mFootViewPosition)
					 {
						 return;
					 }
					 
					 HashMap<String, Object> map =myList.get(arg2);
					 if(map == null)
					 {
						 return;
					 }
					 
					 
//					 int item_account_id =Integer.valueOf(((HashMap)myList.get(arg2)).get("account_id").toString());
					 int item_account_id = 0;
					 String account_id = map.get("account_id") == null ? null : map.get("account_id").toString();
					 if(account_id != null)
					 {
						 item_account_id =Integer.valueOf(account_id);
					 }
					 
//					 long item_event_id =Long.valueOf(((HashMap)myList.get(arg2)).get("event_id").toString());
					 long item_event_id =0;
					 String event_id = map.get("event_id") == null ? null : map.get("event_id").toString();
					 if(event_id != null)
					 {
						 item_event_id =Long.valueOf(event_id);
					 }
					 
//					 Integer item_sns_id =Integer.valueOf(((HashMap)myList.get(arg2)).get("sns_id").toString());
					 Integer item_sns_id =0;
					 String sns_id = map.get("sns_id") == null ? null : map.get("sns_id").toString();
					 if(sns_id != null)
					 {
						 item_sns_id = Integer.valueOf(sns_id);
					 }
					 
//					 Integer item_event_type =Integer.valueOf(((HashMap)myList.get(arg2)).get("event_type").toString());
					 Integer item_event_type = 0;
					 String event_type = map.get("event_type") == null ? null : map.get("event_type").toString();
					 if(event_type != null)
					 {
						 item_event_type = Integer.valueOf(event_type);
					 }

					 
					 DataManager.setContentResolver(mContext.getContentResolver());
					 
					 SnsClientAPI client=new SnsClientAPI(mActivity);
					 
					 //ALPS127006
					 //2010-10-13
					boolean isNetworkAvailable = false;
					try{
						isNetworkAvailable = Util.IsNetWorkAvailable(mContext);
					}
					catch(Exception e)
					{
						Log.i("EventListManager", "Error: scan network available ...");
					}
					if(!isNetworkAvailable)
					{
						Log.i("EventListManager", "network is not available ...");
						Toast.makeText(mContext,mContext.getResources().getString(R.string.Toast_Reason_no_valid_network), Toast.LENGTH_SHORT).show();
						return;
					}
						
					Log.i("EventListManager", "ResponseService:responseToEventClick  there's some available network to use");
					 boolean isLogin = false;
					 try
					 {
						 isLogin = client.checkClientLogin(item_sns_id);
					 }
					 catch(Exception e)
					 {
						 Log.i("EventListManager","client.checkClientLogin has Exception");
					 }
					 
					 if(!isLogin)
					 {
						 Log.i("EventListManager","client.checkClientLogin return false");
						 String ret = mContext.getResources().getString(R.string.Toast_Client_not_installed);
						 Toast.makeText(mContext,ret, Toast.LENGTH_SHORT).show();
						 return ;
					 }
					 
					 Intent it = null;
					 try
					 {
						 it=client.getClientPageIntent(item_account_id, item_event_id, item_event_type, item_sns_id);
					 }
					 catch(Exception e)
					 {
						 String ret = mContext.getResources().getString(R.string.Toast_Get_clientintent_exception);
						 Toast.makeText(mContext,ret, Toast.LENGTH_SHORT).show();
						 return;
					 }
					
					 if(it == null)
					 {
						 Log.i("EventListManager","it is null");
						 String ret = mContext.getResources().getString(R.string.Toast_Client_intent_null_pointer);
						 Toast.makeText(mContext,ret, Toast.LENGTH_SHORT).show();
					 }
					 else 
					 {
						 it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						 try
						 {
							 mActivity.startActivity(it);
						 }
						 catch(Exception e)
						 {
							 if(e instanceof ActivityNotFoundException)
							 {
								 String ret = mContext.getResources().getString(R.string.Toast_Client_not_installed);
								 Toast.makeText(mContext,ret, Toast.LENGTH_SHORT).show();
							 }
						 }
					 }	 
				  }	
			 });
			 this.mCurrentPage = 0;
			 this.mLimitNum = limit;
			 this.resource = itemresourceId;
		 }
		 
		 public void setLimit(int limit)
		 {
			 mLimitNum = limit;
		 }
		 public int getLimit()
		 {
			 return mLimitNum;
		 }

		 public void setParam(Integer[] accountid,String[] userId)
		 {
			 this.accountid = accountid;
			 this.userId = userId;
		 }
		 public int getSnsEvent(Integer[] accountid,String[] userId,boolean first)
		 {
			 if(mContext == null || accountid == null || accountid.length <= 0 || userId== null || userId.length <=0  || accountid.length!= userId.length)
			 {
				 return ERROR_PARAM_INVALID;
			 }
			 int ret = 0;
			 Integer ifirst = 0;
	    		
			 if(first)
			 {
				 setParam(accountid,userId);
			 }
			 else
			 {
				 ifirst = mCurrentPage;
			 }			 
			 WidgetEvent[] event = null;
	    		
			 try
			 {
				 event = DataManager.getWidgetEvents(accountid, userId,ifirst, mLimitNum);
			 }
			 catch(Exception e)
			 {
				 e.printStackTrace();
			 }
			 mCurrentPage += mLimitNum;
			 if(event == null)
			 {
				 if(first)
				 {
					 ret =  ERROR_NO_EVENT;
				 }
				 else
				 {
					 ret = ERROR_END_EVENT;
				 }
			 }
			 else
			 {
				 if(first)
				 {	 
					 if(myList == null)
					 {
						 myList = new ArrayList<HashMap<String, Object>>();
					 }
					 SnsEventUtils.parseEventData(mContext,event,false,myList);
				 }
				 else
				 {
					 if(tmpList == null)
					 {
						 tmpList = new ArrayList<HashMap<String, Object>>();
					 }
					 SnsEventUtils.parseEventData(mContext,event,false,tmpList);
				 }
				 ret = SUCCESS;
			 }
			 return ret;	
		 }
	    	
		 public int show(boolean first)
		 {	    		
			 if(myList == null)
			 {
			 }
			 else
			 {
				 if(mContext == null || resource == null || mSnsContactEventList == null)
				 {
					 return ERROR_PARAM_INVALID;
				 }
				 
				 if(first)
				 {
					 String from[] = SnsEventDataBase.adapterFrom;
					 int to[] =  SnsEventDataBase.adapterTo;
					 int res[] = {R.layout.itemwithoutpic,R.layout.itemwithpic};
					 mAdapter = new MySimpleAdapter(mContext,myList,res,from,to);
					 if(mAdapter == null)
					 {
						 return ERROR;
					 }
					 
					 View view = LayoutInflater.from(mContext).inflate(R.layout.sns_contact_event_foot_view, null);
					 if(view != null)
					 {
						 mSnsContactEventList.addFooterView(view);
					 }
					 mSnsContactEventList.setAdapter(mAdapter);
				 }
				 else
				 {
					 myList.addAll(tmpList);
					 mAdapter.notifyDataSetChanged();
				 }
				 mFootViewPosition = mAdapter.getCount();
			 }
			 return SUCCESS;
		 }
		 
		 
		 
		 class MySimpleAdapter extends MultiLayoutAdapter {
			 	private ImageLoader imageLoader;
			 	private List<? extends Map<String, ?>> data;
			 	private Context context;
			 	
			 	class MyViewBinder implements ViewBinder {
			 		
			 		public HashMap<Integer, Bitmap> webLogCache = new HashMap<Integer, Bitmap>();
			 		
			 		public boolean setViewValue(View view, final Object data, String textRepresentation) {
			 			if (view.getId()==R.id.EventFriendHeadPortrait || view.getId() == R.id.EventAttachPhoto) {
			 				final ImageView iv = (ImageView) view;
			 				Bitmap portrait=imageLoader.get((String)data,view.getId());
			 				iv.setImageBitmap(portrait);
			 				return true;
			 			}
//			 			else if(view.getId() == R.id.EventTitleTV)
//			 			{
//			 				CharSequence cs = (CharSequence)data;
//			 				((TextView)view).setText(cs);
//			 				return true;
//			 			}
			 			else if(view.getId() == R.id.EventWebLog)
			 			{
			 				int srcId = (Integer)data;
			 				Bitmap logBitmap = null;
			 				if(webLogCache.containsKey(srcId))
			 				{
			 					logBitmap = webLogCache.get(srcId);
			 				}
			 				else
			 				{
			 					logBitmap = BitmapFactory.decodeResource(context.getResources(), srcId);
			 					webLogCache.put(srcId, logBitmap);
			 					if(logBitmap==null)
			 						Log.d("","logo can't be read!");
			 				}
			 				((ImageView)view).setImageBitmap(logBitmap);
			 				return true;
			 			}
			 			else if(view.getId() == R.id.EventAttachPhoto)
			 			{
			 				final ImageView iv = (ImageView) view;
			 				Bitmap portrait=imageLoader.get((String)data);
			 				iv.setImageBitmap(portrait);	
			 			}
			 				
			 			
			 			return false;
			 		}
			 	}

			 	public MySimpleAdapter(Context context,
			 			List<? extends Map<String, ?>> data, int resource[],
			 			String[] from, int[] to) {
			 		super(context, data, resource, from, to);
			 		this.context = context;
			 		this.data=data;
			 		this.setViewBinder(new MyViewBinder());
			 		Bitmap defaultPortrait=null;
			 		try
			 		{
			 		defaultPortrait=BitmapFactory.decodeResource(context.getResources(),R.drawable.default_portrait);	
			 		} 
			 		catch (Exception e) 
			 		{
			 			e.printStackTrace();
			 		}
			 		imageLoader=new ImageLoader(this, defaultPortrait) {

			 			@Override
			 			public void loadImage(Map<String, Object> dataMap) {
			 				final String fileName=(String) dataMap.get("HeadPortrait");
			 				final String eventPhotoPath = (String)dataMap.get("EventPhoto");
			 				if(null != fileName && !fileName.equals("default")) {
			     					if (!isImageLoaded(fileName)) {
			     						Bitmap portrait=BitmapFactory.decodeFile(fileName);
			     						setImage(fileName,portrait);
			     					}
				     			}
				     			if(null != eventPhotoPath && eventPhotoPath.length() >= 1) {
				     				if (!isImageLoaded(eventPhotoPath)) {
				     					Bitmap portrait=BitmapFactory.decodeFile(eventPhotoPath);
				     					setImage(eventPhotoPath,portrait);
				     				}
				     			}
			 			}
			 		};
			 		imageLoader.setDefaultPortrait(R.id.EventFriendHeadPortrait, defaultPortrait);
			 		Bitmap defaultEventImage=null;
			 		try
			 		{
			 			defaultEventImage=BitmapFactory.decodeResource(context.getResources(),R.drawable.sns_evnt_default_image);	
			 		} 
			 		catch (Exception e) 
			 		{
			 			e.printStackTrace();
			 		}
			 		imageLoader.setDefaultPortrait(R.id.EventAttachPhoto, defaultEventImage);
			 	}

			 	@Override
			 	public View getView(int position, View convertView, ViewGroup parent) {
			 		View ret=super.getView(position, convertView, parent);
			 		imageLoader.put(position);
			 		return ret;
			 	}
			 }
	 }
