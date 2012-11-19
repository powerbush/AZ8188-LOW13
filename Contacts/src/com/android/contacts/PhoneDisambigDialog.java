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
 * Copyright (C) 2009 The Android Open Source Project
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

import com.android.contacts.Collapser.Collapsible;
import com.android.contacts.model.ContactsSource;
import com.android.contacts.model.Sources;
import com.android.contacts.model.ContactsSource.DataKind;
import com.android.contacts.model.ContactsSource.StringInflater;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.featureoption.FeatureOption;
import android.util.Log;
import android.view.KeyEvent;
import com.mediatek.CellConnService.CellConnMgr;

/**
 * Class used for displaying a dialog with a list of phone numbers of which
 * one will be chosen to make a call or initiate an sms message.
 */
public class PhoneDisambigDialog implements DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener, DialogInterface.OnKeyListener, CompoundButton.OnCheckedChangeListener{

    private boolean mMakePrimary = false;
    private Context mContext;
    private AlertDialog mDialog;
    private boolean mSendSms;
    private int mStickyTab;
    private Cursor mPhonesCursor;
    private ListAdapter mPhonesAdapter;
    private ArrayList<PhoneItem> mPhoneItemList;
    private static final String TAG = "PhoneDisambigDialog";
    CellConnMgr mCellConnMgr;

    public PhoneDisambigDialog(Context context, Cursor phonesCursor, boolean sendSms,
            int stickyTab) {
        mContext = context;
        mSendSms = sendSms;
        mPhonesCursor = phonesCursor;
        mStickyTab = stickyTab;

        mPhoneItemList = makePhoneItemsList(phonesCursor);
        Collapser.collapseList(mPhoneItemList);

        mPhonesAdapter = new PhonesAdapter(mContext, mPhoneItemList, mSendSms);

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View setPrimaryView = inflater.
                inflate(R.layout.set_primary_checkbox, null);
        ((CheckBox) setPrimaryView.findViewById(R.id.setPrimary)).
                setOnCheckedChangeListener(this);

        // Need to show disambig dialogue.
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext).
                setAdapter(mPhonesAdapter, this).
                        setTitle(sendSms ?
                                R.string.sms_disambig_title : R.string.call_disambig_title).
                        setView(setPrimaryView).setOnKeyListener(this);

        mDialog = dialogBuilder.create();
    }

    /**
     * Show the dialog.
     */
    public void show() {
        mCellConnMgr = new CellConnMgr();
		mCellConnMgr.register(mContext);    
        if (mPhoneItemList.size() == 1) {
            // If there is only one after collapse, just select it, and close;
            onClick(mDialog, 0);
            return;
        }
        mDialog.show();
    }

    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL:

                //MTK , for OFN: only focus tab
                if (!mDialog.getListView().isFocused()) {
                        Log.d(TAG, "onKeyUp, not focus");
                    //getListView().requestFocus();
                    break;
                }
                int position;
                position = mDialog.getListView().getSelectedItemPosition();
                    Log.d(TAG, "onKeyUp, position=" + position);
                //if nothing focus, position should be -1
                if (position >= 0){
                    PhoneItem phoneItem = mPhoneItemList.get(position);
                    String phone = phoneItem.phoneNumber;
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        //ContactsUtils.initiateCallWithSim(mContext, phone);
                        ContactsUtils.enterDialer(mContext,phone);
                    } else {
                        ContactsUtils.initiateCall(mContext, phone);
                    }
                    
                    if (mDialog != null && mDialog.isShowing()) {
                         mDialog.dismiss();
                     }
                return true;
                
                }
        }

        return false;
    }

    public void onClick(DialogInterface dialog, int which) {
        if (mPhoneItemList.size() > which && which >= 0) {
            PhoneItem phoneItem = mPhoneItemList.get(which);
            long id = phoneItem.id;
            String phone = phoneItem.phoneNumber;

            if (mMakePrimary) {
                ContentValues values = new ContentValues(1);
                values.put(Data.IS_SUPER_PRIMARY, 1);
                mContext.getContentResolver().update(ContentUris.
                        withAppendedId(Data.CONTENT_URI, id), values, null, null);
            }

            if (mSendSms) {
                ContactsUtils.initiateSms(mContext, phone);
            } else {
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    //ContactsUtils.initiateCallWithSim(mContext, phone);
                    //ContactsUtils.enterDialer(mContext, phone);
                    makeCall(phone, ContactsUtils.DIAL_TYPE_VOICE);
                } else {
                    if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
                        //ContactsUtils.enterDialer(mContext, phone);
                        makeCall(phone, ContactsUtils.DIAL_TYPE_VOICE);
				}else{
                	StickyTabs.saveTab(mContext, mStickyTab);
                	//ContactsUtils.initiateCall(mContext, phone);
                	makeCall(phone, ContactsUtils.DIAL_TYPE_VOICE);
				}
            }
            }
        } else {
            dialog.dismiss();
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mMakePrimary = isChecked;
    }

    public void onDismiss(DialogInterface dialog) {
        mPhonesCursor.close();
    }

    private static class PhonesAdapter extends ArrayAdapter<PhoneItem> {
        private final boolean sendSms;
        private final Sources mSources;

        public PhonesAdapter(Context context, List<PhoneItem> objects, boolean sendSms) {
            super(context, R.layout.phone_disambig_item,
                    android.R.id.text2, objects);
            this.sendSms = sendSms;
            mSources = Sources.getInstance(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            PhoneItem item = getItem(position);
            ContactsSource source = mSources.getInflatedSource(item.accountType,
                    ContactsSource.LEVEL_SUMMARY);

            // Obtain a string representation of the phone type specific to the
            // ContactSource associated with that phone number
            TextView typeView = (TextView)view.findViewById(android.R.id.text1);
            DataKind kind = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
            if (kind != null) {
                ContentValues values = new ContentValues();
                values.put(Phone.TYPE, item.type);
                values.put(Phone.LABEL, item.label);
                StringInflater header = sendSms ? kind.actionAltHeader : kind.actionHeader;
                typeView.setText(header.inflateUsing(getContext(), values));
            } else {
                typeView.setText(R.string.call_other);
            }
            return view;
        }
    }

    private class PhoneItem implements Collapsible<PhoneItem> {

        final long id;
        final String phoneNumber;
        final String accountType;
        final long type;
        final String label;

        public PhoneItem(long id, String phoneNumber, String accountType, int type, String label) {
            this.id = id;
            this.phoneNumber = (phoneNumber != null ? phoneNumber : "");
            this.accountType = accountType;
            this.type = type;
            this.label = label;
        }

        public boolean collapseWith(PhoneItem phoneItem) {
            if (!shouldCollapseWith(phoneItem)) {
                return false;
            }
            // Just keep the number and id we already have.
            return true;
        }

        public boolean shouldCollapseWith(PhoneItem phoneItem) {
            if (PhoneNumberUtils.compare(PhoneDisambigDialog.this.mContext,
                    phoneNumber, phoneItem.phoneNumber)) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return phoneNumber;
        }
    }

    private ArrayList<PhoneItem> makePhoneItemsList(Cursor phonesCursor) {
        ArrayList<PhoneItem> phoneList = new ArrayList<PhoneItem>();

        phonesCursor.moveToPosition(-1);
        while (phonesCursor.moveToNext()) {
            long id = phonesCursor.getLong(phonesCursor.getColumnIndex(Data._ID));
            String phone = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.NUMBER));
            String accountType =
                    phonesCursor.getString(phonesCursor.getColumnIndex(RawContacts.ACCOUNT_TYPE));
            int type = phonesCursor.getInt(phonesCursor.getColumnIndex(Phone.TYPE));
            String label = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.LABEL));

            phoneList.add(new PhoneItem(id, phone, accountType, type, label));
        }

        return phoneList;
    }
    
    // mtk80909 start
    public boolean isShowing() {
    	return (mDialog != null && mDialog.isShowing());
}
    
    public void dismiss() {
        ContactsUtils.dispatchActivityOnPause();
        Log.i(TAG,"mCellConnMgr IS " + mCellConnMgr);
        if (mCellConnMgr != null) mCellConnMgr.unregister();
        
    	if (mDialog != null && mDialog.isShowing()) {
    		mDialog.dismiss();
    	}
    }
    // mtk80909 end

	public CellConnMgr getCellConnMgr() {
        // TODO Auto-generated method stub
        return mCellConnMgr;
    }    

    
    private void makeCall(String number, int type) {
        ContactsUtils.dial(mContext, number, type, new ContactsUtils.OnDialCompleteListener() {
            
            public void onDialComplete(boolean dialed) {
                // TODO Auto-generated method stub
                //if(dialed)
                //    ViewContactActivity.this.finish();
            }
        });
    }
    
}
