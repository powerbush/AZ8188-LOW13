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
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.contacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.android.contacts.ui.widget.SimIconView;
import com.android.contacts.util.Constants;
import com.android.contacts.util.NotifyingAsyncQueryHandler;
import com.android.contacts.util.Constants.NumberInfo;
import com.android.contacts.util.Constants.SimInfo;
import com.android.internal.widget.ContactHeaderWidget;

/**
 * Association the contact number to a sim card.
 */
public class AssociationSimActivity extends Activity implements
		NotifyingAsyncQueryHandler.AsyncQueryListener, OnClickListener {
	private static final String TAG = "AssociationSimActivity";

	private AlertDialog mDetailListDialog;
	private ContactHeaderWidget mHeaderWidget;
	private ListView mListView;
	private Button mDone;
	private Button mDiscard;

	private List<Map<String, Object>> mData;
	private List<SimInfo> mSimList;
	private List<NumberInfo> mNumberList;
	private AssociationInfo mAssociationInfo;

	private Uri mLookupUri;
	private Uri mContactsUri;
	private NotifyingAsyncQueryHandler mHandler;

	private String displayName;

	private static final int TOKEN_ENTITIES = 0;
	private long dataId;

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.association_sim);

		Intent intent = getIntent();

		Uri data = intent.getData();
		dataId = intent.getLongExtra("data_id", 0);
		mLookupUri = data;
		long contactId = ContentUris.parseId(mLookupUri);

		mContactsUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
				contactId);

		mHeaderWidget = (ContactHeaderWidget) findViewById(R.id.association_header_widget);
		if (null != mHeaderWidget) {
			mHeaderWidget.showStar(false);
			mHeaderWidget.bindFromContactLookupUri(mLookupUri);
			mHeaderWidget.setSnsPadVisibility(View.GONE);
			mHeaderWidget.setBtnUpdateVisibility(View.GONE);
			mHeaderWidget.setBtnStartVisibility(View.GONE);
		}

		mHandler = new NotifyingAsyncQueryHandler(this, this);

		mListView = (ListView) findViewById(R.id.association_list);
		mDone = (Button) findViewById(R.id.btn_done);
		mDiscard = (Button) findViewById(R.id.btn_discard);

		mDone.setOnClickListener(this);
		mDiscard.setOnClickListener(this);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initialData();

		long contactId = ContentUris.parseId(mLookupUri);
		String projecttion = RawContacts.CONTACT_ID + "=? AND "
				+ RawContacts.Data.MIMETYPE + "=? ";
		String args[] = new String[] { String.valueOf(contactId),
				CommonDataKinds.Phone.CONTENT_ITEM_TYPE };
//		if (dataId > 0) {
//			projecttion = projecttion + " AND " + RawContactsEntity.DATA_ID
//					+ "=? ";
//			args = new String[] { String.valueOf(contactId),
//					CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
//					String.valueOf(dataId) };
//		}
		Cursor cursor = getContentResolver().query(RawContacts.CONTENT_URI,
				new String[] { "display_name" }, "_id=? ",
				new String[] { String.valueOf(contactId) }, null);
		if (cursor != null && cursor.moveToNext()) {
			displayName = (String) cursor.getString(0);
			cursor.close();
		}
		mHandler.startQuery(TOKEN_ENTITIES, null,
				RawContactsEntity.CONTENT_URI, null, projecttion, args, null);

		mData = getData();
		MyAdapter adapter = new MyAdapter(this);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(mListener);
	}

	private OnItemClickListener mListener = new AdapterView.OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View v, int positon,
				long arg3) {

			if (positon == 0) {
//				if (dataId > 0 || mNumberList.size() <= 1) {
//					v.setEnabled(false);
//					return;
//				}
				String titleStr = displayName;
				final NumberAdapter adapter = new NumberAdapter(
						AssociationSimActivity.this);
				final DialogInterface.OnClickListener mNumberListListener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mAssociationInfo.numberLabel = mNumberList.get(which).name;
						mAssociationInfo.number = mNumberList.get(which).number;
						mAssociationInfo.dataId = mNumberList.get(which).dataId;
						mAssociationInfo.simId = mNumberList.get(which).simId;
						mData = getData();
						MyAdapter adapter = new MyAdapter(
								AssociationSimActivity.this);
						mListView.setAdapter(adapter);
						mDetailListDialog.dismiss();
						mDetailListDialog = null;
					}
				};
				AlertDialog.Builder builder = new AlertDialog.Builder(
						AssociationSimActivity.this);
				builder.setSingleChoiceItems(adapter, -1, mNumberListListener)
						.setTitle(titleStr).setIcon(
								android.R.drawable.ic_menu_more);
				mDetailListDialog = builder.create();
				mDetailListDialog.show();
				return;
			} else {
				final DialogInterface.OnClickListener mSimListListener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mAssociationInfo.simId = mSimList.get(which).simId;
						mData = getData();
						MyAdapter adapter = new MyAdapter(
								AssociationSimActivity.this);
						mListView.setAdapter(adapter);
						mDetailListDialog.dismiss();
						mDetailListDialog = null;
					}
				};
				mDetailListDialog = ContactsUtils.buildSimListDialog(
						AssociationSimActivity.this, mSimListListener);
				mDetailListDialog.show();
			}
		}

	};

	private void initialData() {
		mNumberList = new ArrayList<NumberInfo>();
		mSimList = Constants.getInsertedSimList(this);
		mAssociationInfo = new AssociationInfo();
	}

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", mAssociationInfo.numberLabel);
		map.put("info", mAssociationInfo.number);
		list.add(map);

		int simId = mAssociationInfo.simId;
		SimInfo info = Constants.getSimInfoById(this, simId);
		String simDeatil = this.getResources().getString(R.string.unassociated);
		if (info != null)
			simDeatil = info.label + "  " + info.number;

		map = new HashMap<String, Object>();
		String associatiated_SIM = this.getResources().getString(R.string.associatiated_SIM);
		map.put("title", associatiated_SIM);
		map.put("info", simDeatil);
		list.add(map);

		return list;
	}

	private final class NumberHolder {
		public TextView title;
		public TextView info;
	}

	private class NumberAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		NumberAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return mNumberList.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mNumberList.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			NumberHolder holder = null;
			if (convertView == null) {
				holder = new NumberHolder();
				convertView = mInflater
						.inflate(R.layout.number_list_item, null);
				holder.title = (TextView) convertView.findViewById(R.id.text1);
				holder.info = (TextView) convertView.findViewById(R.id.text2);
				convertView.setTag(holder);
			} else {
				holder = (NumberHolder) convertView.getTag();
			}
			holder.title.setText((String) mNumberList.get(position).name);
			holder.info.setText((String) mNumberList.get(position).number);
			return convertView;
		}

	}

	private final class SimHolder {
		public TextView title;
		public TextView info;
		// public ImageView img;
		public SimIconView simIcon;
	}

	private class SimAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		SimAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return mSimList.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			SimHolder holder = null;
			if (convertView == null) {
				holder = new SimHolder();
				convertView = mInflater.inflate(R.layout.sim_list_item, null);
				holder.title = (TextView) convertView.findViewById(R.id.text1);
				holder.info = (TextView) convertView.findViewById(R.id.text2);
				holder.simIcon = (SimIconView) convertView
						.findViewById(R.id.sim_icon);
				convertView.setTag(holder);
			} else {
				holder = (SimHolder) convertView.getTag();
			}
			holder.simIcon.updateSimIcon(mSimList.get(position));
			holder.title.setText((String) mSimList.get(position).label);
			holder.info.setText((String) mSimList.get(position).number);
			return convertView;
		}

	}

	private class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return mData.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.association_sim_item,
						null);
				holder.title = (TextView) convertView.findViewById(R.id.text1);
				holder.info = (TextView) convertView.findViewById(R.id.text2);
				holder.img = (ImageView) convertView
						.findViewById(R.id.more_icon);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.title.setText((String) mData.get(position).get("title"));
			holder.info.setText((String) mData.get(position).get("info"));
			return convertView;
		}

	}

	private final class ViewHolder {
		public TextView title;
		public TextView info;
		public ImageView img;
	}

	private final class AssociationInfo {
		public String numberLabel;
		public String number;
		// public String simName;
		// public String simNumber;
		public int dataId;
		public int simId;

		AssociationInfo() {
		};

		AssociationInfo(String numberLabel, String number, int simId, int dataId) {
			this.numberLabel = numberLabel;
			this.number = number;
			this.simId = simId;
			this.dataId = dataId;
		}

	}

	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		// TODO Auto-generated method stub
		if (cursor == null)
			return;
		while (cursor.moveToNext()) {
			// displayName =
			// (String)cursor.getString(cursor.getColumnIndex("display_name"));
			int id = (Integer) cursor.getInt(cursor
					.getColumnIndex(RawContactsEntity.DATA_ID));
			String number = (String) cursor.getString(cursor
					.getColumnIndex(Data.DATA1));
			String type = (String) cursor.getString(cursor
					.getColumnIndex(Data.DATA2));
			String label = (String) CommonDataKinds.Phone.getTypeLabel(
					getResources(), Integer.parseInt(type), null);
			int simId = (int) cursor.getInt(cursor.getColumnIndex(Data.SIM_ID));
			NumberInfo t = new NumberInfo(label, number, simId);
			t.dataId = id;
			mNumberList.add(t);
		}
		int p = 0;
		for(int i = 0; i < mNumberList.size(); i++){
			if(mNumberList.get(i).dataId == dataId)p = i;
		}
		if (mNumberList.size() > 0) {
			int simId = mNumberList.get(p).simId;
			int dataId = mNumberList.get(p).dataId;
			SimInfo info = Constants.getSimInfoById(this, simId);
			String numberLabel = mNumberList.get(p).name;
			String number = mNumberList.get(p).number;
			mAssociationInfo = new AssociationInfo(numberLabel, number, simId,
					dataId);
		}
		mData = getData();
		MyAdapter adapter = new MyAdapter(AssociationSimActivity.this);
		mListView.setAdapter(adapter);
		if (cursor != null)
			cursor.close();
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_done:
			save();
			this.finish();
			break;
		case R.id.btn_discard:
			this.finish();
			break;
		}

	}

	private void save() {
		ContentValues values = new ContentValues();
		values.put(Data.SIM_ID, mAssociationInfo.simId);
		mHandler.startUpdate(0, null, Data.CONTENT_URI, values,
				RawContacts.Data._ID + "=? ", new String[] { String
						.valueOf(mAssociationInfo.dataId) });
	}
}
