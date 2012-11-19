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

package com.android.contacts.ui.widget;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.android.contacts.ContactsListActivity;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R; //import com.android.contacts.ContactsListActivity.DeleteClickListener;
import com.android.contacts.model.ContactsSource;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityModifier;
import com.android.contacts.model.EntitySet;
import com.android.contacts.model.GoogleSource;
import com.android.contacts.model.Sources;
import com.android.contacts.model.ContactsSource.DataKind;
import com.android.contacts.model.ContactsSource.EditType;
import com.android.contacts.model.Editor.EditorListener;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.android.contacts.ui.EditContactActivity;
import com.android.contacts.ui.EditSimContactActivity;
import com.android.internal.telephony.ITelephony;
import com.android.contacts.ui.ViewIdGenerator;
//import com.android.contacts.ui.EditContactActivity.PhotoListener;
import android.provider.Telephony.SIMInfo;//gemini enhancement

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import com.android.contacts.model.GoogleSource;//gemini enhancement
import java.util.Iterator;
import android.widget.LinearLayout;

/**
 * Custom view that provides all the editor interaction for a specific
 * {@link Contacts} represented through an {@link EntityDelta}. Callers can
 * reuse this view and quickly rebuild its contents through
 * {@link #setState(EntityDelta, ContactsSource)}.
 * <p>
 * Internal updates are performed against {@link ValuesDelta} so that the
 * source {@link Entity} can be swapped out. Any state-based changes, such as
 * adding {@link Data} rows or changing {@link EditType}, are performed through
 * {@link EntityModifier} to ensure that {@link ContactsSource} are enforced.
 */
public class ContactEditorView extends BaseContactEditorView implements OnClickListener {
    private static final String TAG = "ContactEditorView";
    private TextView mReadOnly;
    private TextView mReadOnlyName;

    private View mPhotoStub;
    private GenericEditorView mName;

    private boolean mIsSourceReadOnly;
    private ViewGroup mGeneral;
    private ViewGroup mSecondary;
    private boolean mSecondaryVisible;

    private TextView mSecondaryHeader;

    private Drawable mSecondaryOpen;
    private Drawable mSecondaryClosed;

    private View mHeaderColorBar;
    private View mSideBar;
    private ImageView mHeaderIcon;
    private TextView mHeaderAccountType;
    private TextView mHeaderAccountName;

    private long mRawContactId = -1;

    private EntityDelta mState;
	EntitySet mStateFromEditContact;
    private ContactsSource mSource;
    private ViewIdGenerator mVig;

    //MTK
    KindSectionView mPhoneKindView = null;
    KindSectionView mEmailKindView = null;
    KindSectionView mPostalKindView = null;
    KindSectionView mOrganizationView = null;
    KindSectionView mNicknameView = null;
    KindSectionView mNoteView = null;
    KindSectionView mImView = null;
    KindSectionView mWebsiteView = null;
	ArrayAdapter<CharSequence> mAdapter;
	private int indicate;
	protected Context mContext = null;
	private TextView mStoreLocationInfo;
	protected Spinner sp1;
	protected Intent mIntent;
	public static WeakReference<EditContactActivity> mTarget = null;
	CharSequence text = null;
	private static AlertDialog mChangeStoreInfoAlertDialog = null;
	ArrayList<Account> mAccounts = null;
	final String[] accountName = new String[10];
	final String[] accountType = new String[10];
	String nameForSpinner = null;
	private int tempSeletion = 0;
	private boolean isSetSelectionFromNewContact = false;
	private String mTempName = null;
	private boolean isEditMode = false;
	private String[] displayName;
	private int[] slotId;
	private long[] simId;
	LinearLayout mStoreLocation;
	
	final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
			.getService(Context.TELEPHONY_SERVICE));

	public static void setTarget(EditContactActivity activity) {
		mTarget = new WeakReference<EditContactActivity>(activity);
	}

    public ContactEditorView(Context context) {
        super(context);
		mContext = context;
    }

    public ContactEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
		mContext = context;
    }

    /** {@inheritDoc} */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mInflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        mPhoto = (PhotoEditorView)findViewById(R.id.edit_photo);
        mPhotoStub = findViewById(R.id.stub_photo);

        final int photoSize = getResources().getDimensionPixelSize(R.dimen.edit_photo_size);

        mReadOnly = (TextView)findViewById(R.id.edit_read_only);

        mName = (GenericEditorView)findViewById(R.id.edit_name);
        mName.setMinimumHeight(photoSize);
        mName.setDeletable(false);

        mReadOnlyName = (TextView) findViewById(R.id.read_only_name);

        mGeneral = (ViewGroup)findViewById(R.id.sect_general);
        mSecondary = (ViewGroup)findViewById(R.id.sect_secondary);

        mHeaderColorBar = findViewById(R.id.header_color_bar);
        mSideBar = findViewById(R.id.color_bar);
        mHeaderIcon = (ImageView) findViewById(R.id.header_icon);
        mHeaderAccountType = (TextView) findViewById(R.id.header_account_type);
        mHeaderAccountName = (TextView) findViewById(R.id.header_account_name);

        mSecondaryHeader = (TextView)findViewById(R.id.head_secondary);
        mSecondaryHeader.setOnClickListener(this);
        mSecondaryHeader.setBackgroundResource(android.R.drawable.list_selector_background);
        final Resources res = getResources();
        mSecondaryOpen = res.getDrawable(R.drawable.ic_btn_less_up);
        mSecondaryClosed = res.getDrawable(R.drawable.ic_btn_more_up);

		mIntent = ContactsUtils.getIntent();
		Log.i(TAG, "mIntent is " + mIntent);
		final String action = mIntent.getAction();
		indicate = mIntent.getIntExtra(RawContacts.INDICATE_PHONE_SIM,
				RawContacts.INDICATE_PHONE);
		Log.i(TAG, "indicate " + indicate);
		mStoreLocationInfo = (TextView) findViewById(R.id.select_store_location_title);
		mStoreLocation = (LinearLayout) findViewById(R.id.kind_editors);

		sp1 = (Spinner) findViewById(R.id.sp1);
		isEditMode = ContactsUtils.getEditMode();
		if(null != sp1) sp1.setEnabled(!isEditMode);
		initSpinner();
		Log.i(TAG,"***** onFinishInflate before setSecondaryVisible ContactsUtils.getMoreButtonStatus() is " + ContactsUtils.getMoreButtonStatus());
        if (!ContactsUtils.getMoreButtonStatus()) this.setSecondaryVisible(false);

    }

    /** {@inheritDoc} */
    public void onClick(View v) {
        // Toggle visibility of secondary kinds
        
        final boolean makeVisible = mSecondary.getVisibility() != View.VISIBLE;
        
        if(makeVisible && mSecondary.getChildCount() == 0) {
            if (mState != null) {
                for (DataKind kind : mSource.getSortedDataKinds()) {
                    // Skip kind of not editable
                    if (!kind.editable) continue;
                    if (!kind.secondary) continue;
                    final String mimeType = kind.mimeType;
                    if (!mIsSourceReadOnly) {
                        // Otherwise use generic section-based editors
                        if (kind.fieldList == null) continue;
                        final ViewGroup parent = mSecondary;
                        final KindSectionView section = (KindSectionView)mInflater.inflate(
                                R.layout.item_kind_section, parent, false);
                        section.setState(kind, mState, mIsSourceReadOnly, mVig);
                        parent.addView(section);
                    }
                }
            }
        }
        
        this.setSecondaryVisible(makeVisible);

	}

	private void initSpinner() {
		final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
//		mAccounts = ContactsUtils.getAccount();
		mAccounts = Sources.getInstance(mContext).getAccounts(true);
		nameForSpinner = ContactsUtils.getSpinnerName();
		Log.i(TAG, "nameForSpinner is " + nameForSpinner);
		Log.i(TAG, "mAccounts is " + mAccounts);
		Log.i(TAG, "isEditMode is " + isEditMode);
		long Contact_Id = ContactsUtils.getContactId();
		Log.i(TAG, "initSpinner Contact_Id is " + Contact_Id);
		if (isEditMode) {
			if(null != mStoreLocation) mStoreLocation.setVisibility(View.GONE);
			Cursor cursor = mContext.getContentResolver().query(RawContacts.CONTENT_URI, new String[] {RawContacts.ACCOUNT_NAME}, RawContacts.CONTACT_ID + "=" + Contact_Id, null, null);
			if (cursor != null && cursor.moveToNext()) {     
				nameForSpinner = cursor.getString(0);
				Log.i(TAG,"initSpinner nameForSpinner IS " + nameForSpinner);
				cursor.close();
			}
			if (nameForSpinner != null) ContactsUtils.setSpinnerName(nameForSpinner);
		}
		mStateFromEditContact = ContactsUtils.getState();
		Log.i(TAG,"In initSpinner mStateFromEditContact is " + mStateFromEditContact);
		mAdapter = new ArrayAdapter<CharSequence>(mContext, android.R.layout.simple_spinner_item);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		final Sources sources = Sources.getInstance(mContext);
		final List<Account> writableAccountList = sources.getAccounts(true);
		final Context dialogContext = new ContextThemeWrapper(mContext,
				android.R.style.Theme_Light);
		final Resources res = dialogContext.getResources();
//		final LayoutInflater dialogInflater = (LayoutInflater) dialogContext
//				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		


				mAdapter.add(mContext.getString(R.string.phone)/* R.string.usim1 */);

		if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {			 
		List<SIMInfo> simInfos = SIMInfo.getInsertedSIMList(mContext);
		int i = 0;
		slotId = new int[10];
		simId = new long[100];
		displayName = new String[10];
		if (!simInfos.isEmpty()) {
        	for (Iterator<SIMInfo> it = simInfos.iterator(); it.hasNext();) {
            	SIMInfo simInfo = it.next();
            	Log.i(TAG, "simInfo is " + simInfo);
            	slotId[i] = simInfo.mSlot;
				simId[i] = simInfo.mSimId;
            	displayName[i] = simInfo.mDisplayName;
            	Log.i(TAG, "displayName " + i + " is " + displayName[i]);
            	Log.i(TAG, "slotId " + i + " is " + slotId[i]);
				Log.i(TAG, "simId " + i + " is " + simId[i]);
            	mAdapter.add(displayName[i]);
            	i++;
			}
		} else {
			Log.i(TAG, "simInfos is null! ");
		}
		} else {
			try {
				if (iTel != null && iTel.hasIccCard()) {
				Log.i(TAG,"iTel.hasIccCard() is " + iTel.hasIccCard());
					mAdapter.add(mContext.getString(R.string.sim));
		        }
				} catch (RemoteException e) {
					Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
				}
			
		}


		if (mAccounts != null) {
			if (!mAccounts.isEmpty()) {
				Log.i(TAG, "accounts.size() is " + mAccounts.size());
				for (int j = 0; j < mAccounts.size(); j++) {
					// Log.i(TAG,"accounts.get(" + i + ") is " +
					// writableAccountList.get(i));
					accountName[j] = mAccounts.get(j).name;
					accountType[j] = mAccounts.get(j).type;
					// accountName[i] = accounts[i].name;
					Log.i(TAG, "accountName( " + j + "is " + accountName[j]);
					mAdapter.add(accountName[j]);
				}
			}
		}

		Log.i(TAG, "mAdapter.getCount is  " + mAdapter.getCount());
		boolean newOwner = mIntent.getBooleanExtra(ContactsListActivity.NEW_OWNER_INFO, false);
		if (mAdapter == null /* || newOwner *//*|| mAdapter.getCount() <= 1*/) {
			if (null != sp1)sp1.setVisibility(View.GONE);
		} else {
			Log.i(TAG, "mAdapter != null");
			if (null != sp1)
				sp1.setAdapter(mAdapter);
			if (null != sp1) {
				Log.i(TAG, "null != sp1");
				if (mAccounts != null) {
					Log.i(TAG,"mAccounts != null nameForSpinner IS " + nameForSpinner);
					if (TextUtils.isEmpty(nameForSpinner)) {
						long rawContactId = ContactsUtils.getRawContactId();
						Cursor c = mContext.getContentResolver().query(RawContacts.CONTENT_URI,
							new String[] { RawContacts.ACCOUNT_NAME, RawContacts.INDICATE_PHONE_SIM},
								RawContacts._ID + "=" + rawContactId, null, null);
						Log.i(TAG,"c is " + c);
						if (c != null && c.moveToFirst()) {
							nameForSpinner = c.getString(0);
							Log.i(TAG,"after query nameForSpinner is "+nameForSpinner);
							c.close();
						}
						for (int j = 0; j < mAdapter.getCount(); j++) {
							Log.i(TAG, "mAdapter.getCount() is "+ mAdapter.getCount());
							Log.i(TAG, "mAdapter.getItem(" + j + ") is "+ mAdapter.getItem(j));
							if (!TextUtils.isEmpty(nameForSpinner)) {
								if (nameForSpinner.equals(mAdapter.getItem(j))) {
								Log.i(TAG, "equals ");
									sp1.setSelection(j);
									tempSeletion = j;
								mTempName = nameForSpinner;
								
							}
						}
						}
						
						} else {
							for (int j = 0; j < mAdapter.getCount(); j++) {
							if (nameForSpinner.equals(mAdapter.getItem(j))) {
								Log.i(TAG, "equals ");
									sp1.setSelection(j);
									tempSeletion = j;
								mTempName = nameForSpinner;
								
							}
							}
						}
						Log.i(TAG, "nameForSpinner again is "+ nameForSpinner);
					
				} else {
					sp1.setSelection(0);
					mTempName = mContext.getString(R.string.phone);
				}
				isSetSelectionFromNewContact = true;
				
			}

		}
		

		if (null != sp1 && View.GONE != sp1.getVisibility()) {
			sp1.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					Log.i(TAG,"In sp1.setOnItemSelectedListener ");
					final Bundle extras = mIntent.getExtras();
					text = mAdapter.getItem(position);
					((TextView)view).setTextColor(0xff5a5c5c);
//					mTempName = (String)(sp1.getContentDescription());
//					Log.i(TAG,"sp1.getContentDescription() is " + sp1.getContentDescription());
					if (isSetSelectionFromNewContact) {
						isSetSelectionFromNewContact = false;
						return;
					}
					Log.i(TAG, "before displayConfirmDialog ");
					displayConfirmDialog();
				}

				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
				}

			});
		}

	}


	public void displayConfirmDialog() {
		Log.i(TAG, "In displayConfirmDialog ");
		Log.i(TAG,"In displayConfirmDialog mTempName is " + mTempName);
		Log.i(TAG,"In displayConfirmDialog text is " + text);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setIcon(android.R.drawable.ic_menu_more);
		builder.setTitle(mContext.getString(R.string.dialog_change_store_info, text));
		builder.setMessage(mContext.getString(R.string.changeStoreInfoConfirmation, mTempName, text));
		builder.setNegativeButton(android.R.string.cancel, new RevertStoreInfoClickListener()).create();
		builder.setPositiveButton(android.R.string.ok, new ChangeStoreInfoClickListener()).create();
		mChangeStoreInfoAlertDialog = builder.create();
		mChangeStoreInfoAlertDialog.show();
	}

	private void dismissAlertDialog(AlertDialog alertDialog) {
		if (alertDialog != null && alertDialog.isShowing()) {
			alertDialog.dismiss();
		}
	}

	private class ChangeStoreInfoClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
			final Bundle extras = mIntent.getExtras();
			Log.i(TAG, "save contact in " + text);
			if (iTel == null) {
				Log.i(TAG,"iTel == null");
				return;
			}
			Intent intent = new Intent(mContext, EditSimContactActivity.class);
			if (extras != null && extras.size() > 0) {
				intent.putExtras(extras);
			}
			intent.putExtra("action", Intent.ACTION_INSERT);					
			if (text.equals(mContext.getString(R.string.phone))) {
				mTempName = (String)text;
				return;
			} else {
				for (int i = 0; i < mAccounts.size(); i++) {
					if (text.equals(accountName[i])) {
						// EditContactActivity.createContact((String)text,
						// accountType[i]);
						ContactsUtils.setSpinnerName(accountName[i]);
						mTempName = accountName[i];
						Log.i(TAG, "text is " + text);
						Log.i(TAG, "accountType " + i + "is " + accountType[i]);
						final Sources sources = Sources.getInstance(mContext);
						ContentValues values = new ContentValues();
						values = ContactsUtils.getValues();
						Log.i(TAG,"Before rebuild values is " +values );
						if (accountName[i] != null) {
							Log.i(TAG,"accountName[i] != null accountName[i] is " + accountName[i]);
							values.put(RawContacts.ACCOUNT_NAME, /* account.name */accountName[i]);
							values.put(RawContacts.ACCOUNT_TYPE, accountType[i]);
						} else {
							values.putNull(RawContacts.ACCOUNT_NAME);
							values.putNull(RawContacts.ACCOUNT_TYPE);
						}

//						ContactsUtils.setValues(values);
						Log.i(TAG,"values is " + values);
						EntityDelta insert = new EntityDelta(ValuesDelta.fromAfter(values));
						Log.i(TAG,"insert is " + insert);
						 final ContactsSource source = sources.getInflatedSource(accountName[i] != null ? accountName[i] : null,
						            ContactsSource.LEVEL_CONSTRAINTS);
						 Log.i(TAG,"source is " + source);
						EntityModifier.ensureKindExists(insert, source, Phone.CONTENT_ITEM_TYPE);
						if (GoogleSource.ACCOUNT_TYPE.equals(source.accountType)) {
							GoogleSource.attemptMyContactsMembership(insert, mContext);
						}
						mStateFromEditContact = ContactsUtils.getState();
						Log.i(TAG,"mStateFromEditContact is " + mStateFromEditContact);
						if (mStateFromEditContact == null) {//add for edit contact
				            // Create state if none exists yet
							
							mStateFromEditContact = EntitySet.fromSingle(insert);
				        } else {
				            // Add contact onto end of existing state
				        	mStateFromEditContact.add(insert);
				        }
						bindEditors();//add for edit contact
						return;
					}
				}
        		  try {
					if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
						for (int i = 0; i< displayName.length; i++) {
			            	  if (text.equals(displayName[i])) {
						Log.i(TAG, "iTel.getIccCardTypeGemini(slotId["
								+ i + "]) is "
								+ iTel.getIccCardTypeGemini(slotId[i]));
//								if (null != iTel && iTel.getIccCardTypeGemini(slotId[i]).equals("USIM")) {
							intent.putExtra(RawContacts.INDICATE_PHONE_SIM, simId[i]);
							intent.putExtra("slotId", slotId[i]);
		                    mContext.startActivity(intent);
							if (mTarget != null && mTarget.get() != null) {
			                mTarget.get().finish();
		                    }
			            	  }
						}
					} 
					else {
							long simId = -1;
						    SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(mContext, com.android.internal.telephony.Phone.GEMINI_SIM_1);
						    if (simInfo != null) {
						    	simId = simInfo.mSimId;
						    	Log.i(TAG,"Single version simId is " + simId);
						        }
							intent.putExtra(RawContacts.INDICATE_PHONE_SIM, simId);
							intent.putExtra("slotId", com.android.internal.telephony.Phone.GEMINI_SIM_1);
							mContext.startActivity(intent);
							if (mTarget != null
									&& mTarget.get() != null) {
								mTarget.get().finish();
							}
						}
        		  } catch (RemoteException ex) {
        				ex.printStackTrace();
				}
			}
			dismissAlertDialog(mChangeStoreInfoAlertDialog);

		}
	}

	private class RevertStoreInfoClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			Log.i(TAG, "RevertStoreInfoClickListener");			 
			 for (int i = 0; i < mAdapter.getCount(); i++) {
				 if (mTempName.equals(mAdapter.getItem(i))) {
					 tempSeletion = i;
//					 break;
				 }
			 }
			 if(null != sp1) sp1.setSelection(tempSeletion);
			 isSetSelectionFromNewContact = true;
		}
	}
	
	protected void bindEditors() {
    if (mState == null) {
        return;
    }

//    Collections.sort(mStateFromEditContact, mContext);
    final int size = mStateFromEditContact.size();
    Log.i(TAG,"size is " + size);
//    if (size > 1) {
//        if (mLoadingDialog == null) {
//            mLoadingDialog = new ProgressDialog(mContext);
//            mLoadingDialog.setMessage(getString(R.string.load_wait));
//            mLoadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//                public void onDismiss(DialogInterface dialog) {
//                    if (mLoading) {
//                        Log.e(TAG, "onDismiss before finishing bind editor ");
//                        finish();
//                    }
//                    
//                }
//                
//            });
//        }
//        mLoadingDialog.show();
//    }
    new Thread(new Runnable() {
        public void run() {
//    final LayoutInflater inflater = (LayoutInflater) getSystemService(
//            Context.LAYOUT_INFLATER_SERVICE);
            final Sources sources = Sources.getInstance(mContext);
            // Remove any existing editors and rebuild any visible

    for (int i = 0; i < size; i++) {
        // TODO ensure proper ordering of entities in the list
                final EntityDelta entity = mStateFromEditContact.get(i);
                Log.i(TAG,"In bindEditors entity is " +entity);
        final ValuesDelta values = entity.getValues();
        Log.i(TAG,"In bindEditors values is " +values);
//                if (!values.isVisible()) {
//                    if (i == size - 1) {
//                        mLoading = false;
//                        if (mLoadingDialog != null) {
//                            mHandler.sendEmptyMessage(DISMISS_LOADING_DIALOG);
//                        }
//                        mContent.setVisibility(View.VISIBLE);
//                        mStatus = STATUS_EDITING;
//                    }
//                    continue;
//                }

        final String accountType = values.getAsString(RawContacts.ACCOUNT_TYPE);
        final String accountName = values.getAsString(RawContacts.ACCOUNT_NAME);
        Log.i(TAG,"In bindEditors accountType is " +accountType);
        Log.i(TAG,"In bindEditors accountName is " +accountName);
        final ContactsSource source = sources.getInflatedSource(accountType,
                ContactsSource.LEVEL_CONSTRAINTS);
        final long rawContactId = values.getAsLong(RawContacts._ID);
        Log.i(TAG,"In bindEditors rawContactId is " +rawContactId);

                final BaseContactEditorView editor;
//        if (!source.readOnly) {
//                    if (i == 0 && mFirstEditor != null) {
//                        editor = mFirstEditor;
//                    } else {
//            editor = (BaseContactEditorView) inflater.inflate(R.layout.item_contact_editor,
//                    mContent, false);
//                    }
//        } else {
//            editor = (BaseContactEditorView) inflater.inflate(
//                    R.layout.item_read_only_contact_editor, mContent, false);
//        }
//        editor.findViewById(R.id.header).setVisibility(View.INVISIBLE);//when account is null, not show phone only 
//                final PhotoEditorView photoEditor = editor.getPhotoEditor();
//        photoEditor.setEditorListener(new PhotoListener(rawContactId, source.readOnly, photoEditor));
//                final int index = i;
//                mContext.runOnUiThread(new Runnable() {
//                    public void run() {
//                        if (index == 0) {
//                            mContent.removeAllViews();
//                        }
//        mContent.addView(editor);
//        editor.setState(entity, source, mViewIdGenerator);
//                        //MTK
//						if (rawContactId == mRawContactIdRequestingPhoto && mTemBitmap != null) {
//                            Log.w(TAG, "Some wrong when binding photo, rebind it.");
//                            photoEditor.setPhotoBitmap(mTemBitmap);
//                            mRawContactIdRequestingPhoto = -1;
//                            mTemBitmap = null;
//    }
//                        if (index == size - 1) {
//                            mLoading = false;
//                            if (mLoadingDialog != null) {
//                                mLoadingDialog.dismiss();
//                            }
//                            mContent.setVisibility(View.VISIBLE);
//                            mStatus = STATUS_EDITING;
//                        }
//                    }
//                });
                
            }
        }
    }).start();
    }

    /**
     * Set the visibility of secondary sections, along with header icon.
     *
     * <p>If the source is read-only and there's no secondary fields, the entire secondary section
     * will be hidden.
     */
    private void setSecondaryVisible(boolean makeVisible) {
        mSecondaryVisible = makeVisible;

        if (!mIsSourceReadOnly) {
            mSecondaryHeader.setVisibility(View.VISIBLE);
            mSecondaryHeader.setCompoundDrawablesWithIntrinsicBounds(
                    makeVisible ? mSecondaryOpen : mSecondaryClosed, null, null, null);
            mSecondary.setVisibility(makeVisible ? View.VISIBLE : View.GONE);
        } else {
            mSecondaryHeader.setVisibility(View.GONE);
            mSecondary.setVisibility(View.GONE);
        }
    }
    public boolean mInflateChildren = false;
    //MTK80736
    public void inflateChildren(ContactsSource source) {
        for (DataKind kind : source.getSortedDataKinds()) {
            // Skip kind of not editable
            if (!kind.editable) continue;

            final String mimeType = kind.mimeType;
            if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                mName.inflateStructuredNameView(source);
            } else if (Photo.CONTENT_ITEM_TYPE.equals(mimeType)) {
                // do nothing
            } else {
                // Otherwise use generic section-based editors
                final ViewGroup parent = kind.secondary ? mSecondary : mGeneral;
                if (kind.secondary) continue;
                final KindSectionView section = (KindSectionView)mInflater.inflate(
                        R.layout.item_kind_section, parent, false);
                if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    mPhoneKindView = section;
                } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    mEmailKindView = section;
                } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    mPostalKindView = section;
                } else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    mOrganizationView = section;
                } else if (Nickname.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    mNicknameView = section;
                } else if (Im.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    mImView = section;
                } else if (Note.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    mNoteView = section;
                } else if (Website.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    mWebsiteView = section;
                }
            }
        }
    }

    /**
     * Set the internal state for this view, given a current
     * {@link EntityDelta} state and the {@link ContactsSource} that
     * apply to that state.
     */
    @Override
    public void setState(EntityDelta state, ContactsSource source, ViewIdGenerator vig) {
        // Remove any existing sections
        mGeneral.removeAllViews();
        mSecondary.removeAllViews();

        // Bail if invalid state or source
        if (state == null || source == null) return;
        mState = state;
        mSource = source;
        mVig = vig;

        setId(vig.getId(state, null, null, ViewIdGenerator.NO_VIEW_INDEX));

        mIsSourceReadOnly = source.readOnly;

        // Make sure we have StructuredName
        EntityModifier.ensureKindExists(state, source, StructuredName.CONTENT_ITEM_TYPE);

        // Fill in the header info
        ValuesDelta values = state.getValues();
        String accountName = values.getAsString(RawContacts.ACCOUNT_NAME);
        CharSequence accountType = source.getDisplayLabel(mContext);
        if (TextUtils.isEmpty(accountType)) {
            accountType = mContext.getString(R.string.account_phone);
        }
		// if (!TextUtils.isEmpty(accountName)) {
		// mHeaderAccountName.setText(
		// mContext.getString(R.string.from_account_format, accountName));
		// }
		// mHeaderAccountType.setText(mContext.getString(R.string.account_type_format,
		// accountType));
		// mHeaderIcon.setImageDrawable(source.getDisplayIcon(mContext));

        mRawContactId = values.getAsLong(RawContacts._ID);

        // Show photo editor when supported
        EntityModifier.ensureKindExists(state, source, Photo.CONTENT_ITEM_TYPE);
        mHasPhotoEditor = (source.getKindForMimetype(Photo.CONTENT_ITEM_TYPE) != null);
        mPhoto.setVisibility(mHasPhotoEditor ? View.VISIBLE : View.GONE);
        mPhoto.setEnabled(!mIsSourceReadOnly);
        mName.setEnabled(!mIsSourceReadOnly);

        // Show and hide the appropriate views
        if (mIsSourceReadOnly) {
            mGeneral.setVisibility(View.GONE);
            mName.setVisibility(View.GONE);
            mReadOnly.setVisibility(View.VISIBLE);
            mReadOnly.setText(mContext.getString(R.string.contact_read_only, accountType));
            mReadOnlyName.setVisibility(View.VISIBLE);
        } else {
            mGeneral.setVisibility(View.VISIBLE);
            mName.setVisibility(View.VISIBLE);
            mReadOnly.setVisibility(View.GONE);
            mReadOnlyName.setVisibility(View.GONE);
        }

        boolean anySecondaryFieldFilled = false;
        // Create editor sections for each possible data kind
        for (DataKind kind : source.getSortedDataKinds()) {
            // Skip kind of not editable
            if (!kind.editable) continue;

            final String mimeType = kind.mimeType;
            if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                // Handle special case editor for structured name
                final ValuesDelta primary = state.getPrimaryEntry(mimeType);
                if (!mIsSourceReadOnly) {
                    mName.setValues(kind, primary, state, mIsSourceReadOnly, vig);
                } else {
                    String displayName = primary.getAsString(StructuredName.DISPLAY_NAME);
                    mReadOnlyName.setText(displayName);
                }
            } else if (Photo.CONTENT_ITEM_TYPE.equals(mimeType)) {
                // Handle special case editor for photos
                final ValuesDelta primary = state.getPrimaryEntry(mimeType);
                mPhoto.setValues(kind, primary, state, mIsSourceReadOnly, vig);
                if (mIsSourceReadOnly && !mPhoto.hasSetPhoto()) {
                    mPhotoStub.setVisibility(View.GONE);
                } else {
                    mPhotoStub.setVisibility(View.VISIBLE);
                }
            } else if (!mIsSourceReadOnly) {
                // Otherwise use generic section-based editors
                if (kind.fieldList == null && !GroupMembership.CONTENT_ITEM_TYPE.equals(kind.mimeType)) continue;
                if (kind.secondary) continue;
                final ViewGroup parent = kind.secondary ? mSecondary : mGeneral;
                KindSectionView section = null;
                if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    section = mPhoneKindView;
                } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    section = mEmailKindView;
                } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    section = mPostalKindView;
                } else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    section = mOrganizationView;
                } else if (Nickname.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    section = mNicknameView;
                } else if (Im.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    section = mImView;
                } else if (Note.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    section = mNoteView;
                } else if (Website.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    section = mWebsiteView;
                }
                Log.i("ContactEditorView", "mimeType " + mimeType + " section " + section);
                if (section == null) {
                    section = (KindSectionView)mInflater.inflate(R.layout.item_kind_section, parent, false);
                }
                section.setState(kind, state, mIsSourceReadOnly, vig);
                if (kind.secondary && section.isAnyEditorFilledOut()) {
                    anySecondaryFieldFilled = true;
                }
                if (ContactsUtils.getMoreButtonStatus()) anySecondaryFieldFilled = true;
                parent.addView(section);
            }
        }
        Log.i(TAG,"******setState mSecondary.getChildCount() is " + mSecondary.getChildCount());
        if(ContactsUtils.getMoreButtonStatus() && mSecondary.getChildCount() == 0) {
        	Log.i(TAG,"mState is " + mState);
            if (mState != null) {
                for (DataKind kind : mSource.getSortedDataKinds()) {
                    // Skip kind of not editable
                    if (!kind.editable) continue;
                    if (!kind.secondary) continue;
                    final String mimeType = kind.mimeType;
                    if (!mIsSourceReadOnly) {
                        // Otherwise use generic section-based editors
                        if (kind.fieldList == null) continue;
                        final ViewGroup parent = mSecondary;
                        final KindSectionView section = (KindSectionView)mInflater.inflate(
                                R.layout.item_kind_section, parent, false);
                        section.setState(kind, mState, mIsSourceReadOnly, mVig);
                        if (section != null) {
                        	section.requestFocus();
                        }                      
                        parent.addView(section);
                    }
                }
            }
        }

        Log.i(TAG,"******setState before setSecondaryVisible anySecondaryFieldFilled is " + anySecondaryFieldFilled);
        setSecondaryVisible(anySecondaryFieldFilled);
    }

    /**
     * Sets the {@link EditorListener} on the name field
     */
    @Override
    public void setNameEditorListener(EditorListener listener) {
        mName.setEditorListener(listener);
    }

    @Override
    public long getRawContactId() {
        return mRawContactId;
    }

    private static class SavedState extends BaseSavedState {
        public boolean mSecondaryVisible;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mSecondaryVisible = (in.readInt() == 0 ? false : true);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mSecondaryVisible ? 1 : 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    /**
     * Saves the visibility of the secondary field.
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.mSecondaryVisible = mSecondaryVisible;
        return ss;
    }

    /**
     * Restores the visibility of the secondary field.
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        setSecondaryVisible(ss.mSecondaryVisible);
    }
}
