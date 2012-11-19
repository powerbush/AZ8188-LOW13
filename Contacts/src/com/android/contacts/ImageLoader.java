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

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.BaseAdapter;



abstract public class ImageLoader {
	private Map<Object,Bitmap> portraitMap=new HashMap<Object,Bitmap>();
	private Bitmap defaultPortrait;
	private Timer timer=new Timer();
	private InterruptableTimerTask tt;
	public static final int num=8;
	private BaseAdapter hostAdapter;
	private boolean dataChanged=false;
	private Handler mainHandler=new Handler(Looper.getMainLooper());
	private Runnable notify=new Runnable() {
		public void run() {
			hostAdapter.notifyDataSetChanged();
		}
	};
	public ImageLoader(BaseAdapter hostAdapter,Bitmap defaultPortrait) {
			this.defaultPortrait=defaultPortrait;
			this.hostAdapter=hostAdapter;
	}
	
	abstract public void loadImage(Map<String,Object> dataMap);
	class InterruptableTimerTask extends TimerTask {
		private boolean interrupted=false;
		private int position;
		public InterruptableTimerTask(int position) {
			super();
			this.position=position;
		}
		synchronized public void interrupt() {
			this.interrupted=true;
		}
		synchronized public boolean interrupted() {
			boolean ret=this.interrupted;
			this.interrupted=false;
			return ret;
		}
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			this.interrupted();	// clear interrupted flag
			int start=position;
			int end1=(start-num)<0?0:(start-num);
			int end2=start+num>hostAdapter.getCount()?hostAdapter.getCount():start+num;
			for (int i=end1;i<end2;++i) {
				if (this.interrupted()) {
					Log.v("ImageLoader","TimerTask:interrupted");
					return;
				}
				try {
					Map<String,Object> dataMap=((Map<String,Object>)hostAdapter.getItem(i));
					loadImage(dataMap);
				} catch (Exception e) {
					return;
				}
				if (dataChanged) {
					dataChanged=false;
					mainHandler.post(notify);
				}
			}
		}
	}
	protected boolean isImageLoaded(Object key) {
		return portraitMap.containsKey(key);
	}
	protected void setImage(Object key,Bitmap value) {
		dataChanged=true;
		portraitMap.put(key, value);
	}
	public Bitmap get(Object id) 
	{
		Bitmap ret=portraitMap.get(id);
		if (ret==null) {
			ret=defaultPortrait;
		}
		return ret;
	}
	
	public Bitmap get(Object id, Object tag) 
	{
		Bitmap ret=portraitMap.get(id);
		if (ret == null) 
		{
			ret = defaultProtraitMap.get(tag);
		}
		return ret;
	}
	
	public void put(final int position) {
		if (tt!=null) {
			tt.cancel();
			tt.interrupt();
		}
		tt=new InterruptableTimerTask(position);
		timer.schedule(tt, 200);
	}
	
	public void setDefaultPortrait(Object tag, Bitmap bitmap)
	{
		if(null != tag || null != bitmap)
		{
			defaultProtraitMap.put(tag, bitmap);
		}
		else
		{
			Log.i("ImageLoader","setDefaultPortrait() invalid argument:  tag=" + tag + "    bitmap" + bitmap);
		}
	}
	
	private Map<Object, Bitmap> defaultProtraitMap = new HashMap<Object, Bitmap>();
}
