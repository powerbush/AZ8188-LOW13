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

package com.android.contacts.ui;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import com.android.contacts.ContactsListActivity;
import com.android.contacts.ContactsUtils; //import com.android.contacts.ITelephony;
//import com.android.contacts.SIMInfo;
import android.provider.Telephony.SIMInfo;
import com.android.contacts.model.ContactsSource.EditType;
import com.android.contacts.R;
import android.os.ServiceManager;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.gemini.GeminiPhone;
import android.app.Activity;
import android.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.OperationApplicationException;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Intents.Insert;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.DialerKeyListener;
import android.util.Log;
import android.widget.ListAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;
import android.widget.AdapterView.OnItemSelectedListener;
import android.text.Editable;
import android.widget.Spinner;
import com.mediatek.featureoption.FeatureOption;
import android.telephony.PhoneNumberUtils;

import com.mediatek.telephony.PhoneNumberFormatUtilEx;
import com.mediatek.telephony.PhoneNumberFormattingTextWatcherEx;

public class EditSimContactActivity extends Activity implements
View.OnClickListener {
	private static final String TAG = "EditSimContactActivity";
	private static final String SIM_NUM_PATTERN = "[+]?[[0-9][*#]]+[[0-9][*#,]]*";
	private static final String USIM_EMAIL_PATTERN = "[[0-9][a-z][A-Z][_]][[0-9][a-z][A-Z][-_.]]*@[[0-9][a-z][A-Z][-_.]]+";
	//	public static final Pattern EMAIL_ADDRESS = Pattern
	//	.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
	//			+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
	//			+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");
	private InsertSimContactThread mThread;
	private ImageView mHeaderIcon;

	private static final int MAX_PIN_LENGTH_NAME = 60;
	private static final int MAX_PIN_LENGTH_NUMBER = 40;
	private static final int MAX_FIX_NUMBER_LENGTH = 20;    
	private static final int MAX_PIN_LENGTH_EMAIL = 36;
	TextView tv;
	TextView tv1;
	TextView tv2;
	TextView tv3;
	EditText ev1;
	EditText ev2;
	EditText ev3;
	EditText ev4;

	TextView tv4;
	protected Spinner sp;
	Button edit_btn;
	Button btn1;
	Button btn2;
	long indicate;
	static final int MODE_DEFAULT = 0;
	static final int MODE_INSERT = 1;
	static final int MODE_EDIT = 2;
	int mMode = MODE_DEFAULT;
	long raw_contactId = -1;
	int contact_id = 0;

	protected static final int NAME_COLUMN = 0;
	protected static final int NUMBER_COLUMN = 1;
	protected static final int EMAIL_COLUMN = 2;
	protected static final int ADDITIONAL_NUMBER_COLUMN = 3;
	protected static final int GROUP_COLUMN = 4;

	private static final String[] ADDRESS_BOOK_COLUMN_NAMES = new String[] {
		"name",
		"number",
		"emails",
		"additionalNumber",
		"groupIds"
	};

	private String name = "";
	private String updateName = "";
	private String phone = "";
	private String updatephone = "";

	private String email = "";// for USIM
	private String updatemail = "";

	private String additional_number = "";
	private String update_additional_number = "";

	private int mSlotId = 0;
	private String mSimType = "SIM";
	final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
			.getService(Context.TELEPHONY_SERVICE));

	private ProgressDialog mSaveDialog;
	private static final int DIALOG_WAITING = 800;
	private boolean mAirPlaneModeOn = false;
	private boolean mAirPlaneModeOnNotEdit = false;
	private boolean mFDNEnabled = false;
	private boolean mSIMInvalid = false;
	private boolean mNumberIsNull = false;
	private boolean mNumberInvalid = false;
	private boolean mFixNumberInvalid = false;	
	private boolean mNumberLong = false;
	private boolean mNameLong = false;
	private boolean mFixNumberLong = false;	
	private boolean mStorageFull = false;
	private boolean mGeneralFailure = false;
	private int mSaveFailToastStrId = -1;
	private boolean mOnBackGoing = false;
	private String mPhoneTypeSuffix = null; // mtk80909 for ALPS00023212
	private boolean mEmailInvalid = false;
	private boolean mEmail2GInvalid = false;
	ArrayAdapter<CharSequence> mAdapter;

	private static final int LISTEN_PHONE_STATES = 1;//80794
	private static final int LISTEN_PHONE_NONE_STATES = 2;

	protected EditType mType;
	// Used only when a user tries to use custom label.
	private EditType mPendingType;
	protected static final int RES_LABEL_ITEM = android.R.layout.simple_list_item_1;
	
	
	//mtk80229
	
	private Handler  saveContactHandler = null;
	private Handler getsaveContactHandler()
	{
		if(null == saveContactHandler)
		{
			HandlerThread controllerThread = new HandlerThread("saveContacts");		
			controllerThread.start();		
			saveContactHandler = new Handler(controllerThread.getLooper());
		}
		return saveContactHandler;
	}
	

	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// for modem switch
		if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(GeminiPhone.EVENT_PRE_3G_SWITCH);
			registerReceiver(mModemSwitchListener, intentFilter);
		}

		final Intent intent = getIntent();
		Bundle args = intent.getExtras();
		final String action = args.getString("action");
		indicate = args.getLong(RawContacts.INDICATE_PHONE_SIM,
				RawContacts.INDICATE_PHONE);
		mSlotId = args.getInt("slotId", -1);
        if(mSlotId == -1){
        	SIMInfo info = SIMInfo.getSIMInfoById(this, indicate);
        	if(info != null)mSlotId = info.mSlot;
        }
		
		Log.i(TAG, "onCreate indicate is " + indicate);
		Log.i(TAG,"onCreate mSlotId is " + mSlotId);
		try {
			if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
					if (iTel != null && iTel.getIccCardTypeGemini(mSlotId).equals("USIM")) {
				mSimType = "USIM";
				}
			} else {
            	if (iTel.getIccCardType().equals("USIM")) {
    				mSimType = "USIM";
            	}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		    
		    Log.i(TAG,"mSimType is " + mSimType);
			if (mSimType.equals("USIM")) {
				setContentView(R.layout.act_usim_edit);// for usim
				tv = (TextView) findViewById(R.id.location_sim);
		tv2 = (TextView) findViewById(R.id.phone_numbers);
				tv3 = (TextView) findViewById(R.id.email);
				tv3 = (TextView) findViewById(R.id.additional_number);
				ev3 = (EditText) findViewById(R.id.edit_email);
				ev4 = (EditText) findViewById(R.id.edit_additional_numbers);
				if (ev3 != null) {
			        ev3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_PIN_LENGTH_EMAIL)});
					}
				if (ev4 != null) {
			        ev4.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_FIX_NUMBER_LENGTH)});
			        ev4.setKeyListener(SIMKeyListener.getInstance());

			        ev4.addTextChangedListener(new PhoneNumberFormattingTextWatcherEx());
					}
			tv.setText(EditSimContactActivity.this.getResources().getString(
					R.string.location_usim));
			} else {
				setContentView(R.layout.act_sim_edit);// for sim
				tv = (TextView) findViewById(R.id.location_sim);
			tv.setText(EditSimContactActivity.this.getResources().getString(
					R.string.location_sim));
				tv2 = (TextView) findViewById(R.id.numbers);
		}
		Log.i(TAG, "mSimType is " + mSimType);
		tv1 = (TextView) findViewById(R.id.contact_name);
		ev1 = (EditText) findViewById(R.id.edit_contact_name);
		ev2 = (EditText) findViewById(R.id.edit_phone_numbers);
		
		btn1 = (Button) findViewById(R.id.btn_sim1_done);
		btn2 = (Button) findViewById(R.id.btn_revert);		
		if (btn1 != null) {
		btn1.setOnClickListener(this);
		//btn1.setHighFocusPriority(true);
		}
		if (btn2 != null) {
		btn2.setOnClickListener(this);
		}
		if (ev2 != null) {
			ev2.addTextChangedListener(new PhoneNumberFormattingTextWatcherEx());
			ev2.setKeyListener(SIMKeyListener.getInstance());
		}
		
		mHeaderIcon = (ImageView) findViewById(R.id.header_icon);
		mMode = MODE_INSERT;
		setTitle(R.string.editContact_title_insert);
		if (Intent.ACTION_EDIT.equals(action)) {
			setTitle(R.string.editContact_title_edit);
			mMode = MODE_EDIT;
			fixIntent();
		}
	    mSaveDialog = new ProgressDialog(this);
		mSaveDialog.setMessage(this.getString(R.string.savingContact));
		
		String num = args.getString(Insert.PHONE);
		Log.i(TAG, "initial phone number " + num);
		if (!TextUtils.isEmpty(num)) {
			num = ((String) num).replaceAll("-", "");
			num = ((String) num).replaceAll("\\(", "");
			num = ((String) num).replaceAll("\\)", "");
//			num = ((String) num).replaceAll("\\+", "");
			num = ((String) num).replaceAll(" ", "");
	    	if(!Pattern.matches(SIM_NUM_PATTERN, num)){
				mNumberInvalid = true;
				}
			if (setSaveFailToastText()) {
				finish();
				return;
			}
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < num.length(); ++i) {
				char[] acceptedChars = SIMKeyListener.getInstance().getAcceptedChars();
				boolean containFlag = false;
				for (int j = 0; j < acceptedChars.length; ++j) {
					if (acceptedChars[j] == num.charAt(i)) {
						containFlag = true;
						break;
					}
				}
				if (containFlag) sb.append(num.charAt(i));
			}
			if (ev2 != null) {
				Log.i(TAG,"CharSequence num = args.getCharSequence(Insert.PHONE);");
		    ev2.setText(PhoneNumberFormatUtilEx.formatNumber(sb.toString()));
		}
            
		}	
        
		String emailNum = args.getString(Insert.EMAIL);
		Log.i(TAG, "initial email number " + emailNum);
		if (!TextUtils.isEmpty(emailNum)) {
            Log.i(TAG,"PhoneNumberUtils.isUriNumber(emailNum) IS " + PhoneNumberUtils.isUriNumber(emailNum));
            if (!PhoneNumberUtils.isUriNumber(emailNum)) {    //phone number      
                mEmailInvalid = true;
    			if (setSaveFailToastText()) {
    				finish();
    				return;
    			}
            } else {  //email number
                if (ev3 != null) {
    			    ev3.setText(emailNum);
                } else {
                    mEmail2GInvalid = true;
                    if (setSaveFailToastText()) {
                        finish();
                        return;
                    }
                }
            }
		}	
		if (ev2 != null) {
        ev2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_PIN_LENGTH_NUMBER)});
		}
		if (ev1 != null) {
        ev1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_PIN_LENGTH_NAME)});
		}
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        mHandler.sendEmptyMessage(LISTEN_PHONE_NONE_STATES);//80794
	}
	
	 @Override
	    protected void onResume() {
	        super.onResume();
	        mHandler.sendEmptyMessage(LISTEN_PHONE_STATES);//80794
	}

	@Override
	protected void onDestroy() {
		// for modem switch
		if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
			unregisterReceiver(mModemSwitchListener);
		}
		super.onDestroy();
	}

	public void fixIntent() {
		Intent intent = getIntent();
		ContentResolver resolver = this.getContentResolver();
		Uri uri = intent.getData();
		Log.i(TAG, "uri is " + uri);
		final String authority = uri.getAuthority();
		final String mimeType = intent.resolveType(resolver);
//		long raw_contactId = -1;
		if (ContactsContract.AUTHORITY.equals(authority)) {
			if (Contacts.CONTENT_ITEM_TYPE.equals(mimeType)) {
				// Handle selected aggregate
				final long contactId = ContentUris.parseId(uri);
				raw_contactId = ContactsUtils.queryForRawContactId(resolver,
						contactId);
			} else if (RawContacts.CONTENT_ITEM_TYPE.equals(mimeType)) {
				final long rawContactId = ContentUris.parseId(uri);
			}
		}
		Log.i(TAG, "raw_contactId IS " + raw_contactId);

		Uri dataUri = Uri.withAppendedPath(ContentUris.withAppendedId(
				RawContacts.CONTENT_URI, raw_contactId),
				RawContacts.Data.CONTENT_DIRECTORY);
		Log.i(TAG, "dataUri IS " + dataUri);
		Cursor cursor = resolver.query(dataUri, null, null, null, null);
		mPhoneTypeSuffix = null;
		String temphone = null;
		int i = 0;
		if (cursor != null) {
			while (cursor.moveToNext()) {
				
				String mt = cursor.getString(cursor
						.getColumnIndexOrThrow(Data.MIMETYPE));
				Log.i(TAG,"mt is "+mt);
				String isAdditionalNumber = cursor.getString(cursor
						.getColumnIndexOrThrow(Data.IS_ADDITIONAL_NUMBER));

				if ((i==0 && i<cursor.getCount())  && Phone.CONTENT_ITEM_TYPE.equals(mt)) {
					phone = cursor.getString(cursor
							.getColumnIndexOrThrow(Phone.NUMBER));
					mPhoneTypeSuffix = cursor.getString(cursor.getColumnIndexOrThrow(Data.DATA15));
						temphone = phone;
						Log.i(TAG, "*********** temphone " + temphone);
				}
				if (StructuredName.CONTENT_ITEM_TYPE.endsWith(mt)) {
					name = cursor.getString(cursor.getColumnIndexOrThrow(StructuredName.DISPLAY_NAME));
				}
				if (mSimType.equals("USIM")) {
//					Log.i(TAG,"mSimType is " + mSimType);
					if (Email.CONTENT_ITEM_TYPE.equals(mt)) {
						email = cursor.getString(cursor.getColumnIndexOrThrow(Email.DATA));
						Log.i(TAG,"email is " + email);
					}
					if ((i != 0 && i<cursor.getCount()) && Phone.CONTENT_ITEM_TYPE.equals(mt)) {
						additional_number = cursor.getString(cursor.getColumnIndexOrThrow(Phone.NUMBER));
					}
				}			
				i++;
			}
			cursor.close();
		}
		Log.i(TAG, "phone " + phone + ", name " + name);
		Log.i(TAG, "email " + email + ", additional_number " + additional_number);
		Log.i(TAG, "temphone " + temphone);

		if (ev1 != null) {
		ev1.setText(name);
		}
		if (ev2 != null) {
			Log.i(TAG,"In fixIntent() ");
			phone = PhoneNumberFormatUtilEx.formatNumber(phone);
		ev2.setText(phone);
        ev2.setSelection(0);
        ev2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					EditText tmp = (EditText)v;
                    if (tmp != null) {
                        tmp.setSelection(tmp.getEditableText().length());
                    }
				}						
			}
        	
        });
		}
        if (mSimType.equals("USIM")) {
        	if (ev3 != null) {
    			ev3.setText(email);
            }
    		if (ev4 != null) {
    			Log.i(TAG,"In fixIntent() additional_number");
    			additional_number = PhoneNumberFormatUtilEx.formatNumber(additional_number);
    			ev4.setText(additional_number);
                ev4.setSelection(0);
                ev4.setOnFocusChangeListener(new View.OnFocusChangeListener() {
    				public void onFocusChange(View v, boolean hasFocus) {
    					if (hasFocus) {
    						EditText tmp = (EditText)v;
                            if (tmp != null) {
                                tmp.setSelection(tmp.getEditableText().length());
                            }
    					}						
    				}                	
                	
                });                
    		}
		}
	}

	@Override
    protected Dialog onCreateDialog(int id) {
    	  switch (id) {
            case DIALOG_WAITING:
			mSaveDialog.setIndeterminate(true);
			mSaveDialog.setCancelable(false);
			return mSaveDialog;
            default:
               return null;
        }
    }

	public void onClick(View view) {
		Log.i(TAG,"In onCLick save begin");
		switch (view.getId()) {
		case R.id.btn_sim1_done: {
			btn1.setClickable(false);
			doSaveAction(mMode);
			break;
		}
		case R.id.btn_revert:
			doRevertAction();
			break;
		}
	}

	public void onBackPressed() {
		if (!mOnBackGoing) {
			mOnBackGoing = true;
    		doSaveAction(mMode);
		}		
	}

	private boolean doRevertAction() {
		finish();
		return true;
	}

	private void doSaveAction(int mode) {
		Log.i(TAG, "In doSaveAction ");
		showDialog(DIALOG_WAITING);
		if (mode == MODE_INSERT) 
		{
			Log.i("huibin","doSaveAction mode == MODE_INSERT");
			Log.i(TAG,"mode == MODE_INSERT");
			
			Handler handler = getsaveContactHandler();
			if(handler != null)
			{
				handler.post(new InsertSimContactThread(MODE_INSERT));
			}
		} else if (mode == MODE_EDIT) {
			Log.i("huibin","doSaveAction mode == MODE_EDIT");
			Handler handler = getsaveContactHandler();
			if(handler != null)
			{
				handler.post(new InsertSimContactThread(MODE_EDIT));
			}
		}

	}
	
	private static class SIMKeyListener extends DialerKeyListener {
        private static SIMKeyListener keyListener;
        /**
         * The characters that are used.
         * 
         * @see KeyEvent#getMatch
         * @see #getAcceptedChars
         */
        public static final char[] CHARACTERS = new char[] { '0', '1', '2',
                '3', '4', '5', '6', '7', '8', '9', '+', '*', '#', ',', '-', ' '};

        @Override
        protected char[] getAcceptedChars() {
            return CHARACTERS;
        }

        public static SIMKeyListener getInstance() {
            if (keyListener == null) {
                keyListener = new SIMKeyListener();
            }
            return keyListener;
		}

	}	

	private class InsertSimContactThread extends Thread {
		public boolean mCanceled = false;
		int mode = 0;

		public InsertSimContactThread(int md) {
			super("InsertSimContactThread");
			mode = md;
			Log.i(TAG, "InsertSimContactThread");
		}

		@Override
		public void run() {
			Uri checkUri = null;
			int result = 0;
			final ContentResolver resolver = getContentResolver();
			updateName = ev1.getText().toString();
			updatephone = ev2.getText().toString();

			Log.i(TAG,"before replace - updatephone is " + updatephone);
			updatephone = updatephone.replaceAll("-", "");
			updatephone = updatephone.replaceAll(" ", "");
			Log.i(TAG,"after replace - updatephone is " + updatephone);
			ContentValues values = new ContentValues();
			values.put("tag", TextUtils.isEmpty(updateName) ? "" : updateName);
			values.put("number", TextUtils.isEmpty(updatephone) ? "" : updatephone);

            if (mSimType.equals("USIM")) {//for USIM
        	updatemail = ev3.getText().toString();
			update_additional_number = ev4.getText().toString();		
			Log.i(TAG,"before replace - update_additional_number is " + update_additional_number);
			update_additional_number = update_additional_number.replaceAll("-", "");
			update_additional_number = update_additional_number.replaceAll(" ", "");
			Log.i(TAG,"after replace - update_additional_number is " + update_additional_number);
        	values.put("anr", TextUtils.isEmpty(update_additional_number) ? "" : update_additional_number);
        	values.put("emails", TextUtils.isEmpty(updatemail) ? "" : updatemail);
			}
			if (mode == MODE_INSERT) {
				if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
						try {
						if (iTel != null && !iTel.isRadioOnGemini(mSlotId)) {
								mAirPlaneModeOn = true;
							}
						if (iTel != null && iTel.isFDNEnabledGemini(mSlotId)) {
								mFDNEnabled = true;
							}
						if (!(TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(mSlotId))) {
								mSIMInvalid = true;
							}

						} catch (RemoteException ex) {
							ex.printStackTrace();
					}
				} else {
					try {
						if (iTel != null && !iTel.isRadioOn()) {
							mAirPlaneModeOn = true;
						}
						if (iTel != null && iTel.isFDNEnabled()) {
							mFDNEnabled = true;
						}
						if (!(TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimState())) {
							mSIMInvalid = true;
						}
					} catch (RemoteException ex) {
						ex.printStackTrace();
					}
					}
				if (mSimType.equals("USIM")) {
					if (TextUtils.isEmpty(updateName) && TextUtils.isEmpty(updatephone) && TextUtils.isEmpty(updatemail) && TextUtils.isEmpty(update_additional_number)) {
						if (mSaveDialog != null && mSaveDialog.isShowing()) {
							mSaveDialog.dismiss();
			            }
						finish();
						return;
					} else if (TextUtils.isEmpty(updatephone) /*&& TextUtils.isEmpty(updatemail) && TextUtils.isEmpty(update_additional_number)*/) {
						mNumberIsNull = true;
					} else if (!TextUtils.isEmpty(updatephone)) {
						if(!Pattern.matches(SIM_NUM_PATTERN, updatephone)){
							mNumberInvalid = true;
							}
					} 
					if (!TextUtils.isEmpty(update_additional_number)) {
						if(!Pattern.matches(SIM_NUM_PATTERN, update_additional_number)){
							mFixNumberInvalid = true;
							}
					} 
					 if (!TextUtils.isEmpty(updatemail)) {
						if (!Pattern.matches(USIM_EMAIL_PATTERN, updatemail)) {
							mEmailInvalid = true;
						}
					}
					
				} else {
					if (TextUtils.isEmpty(updatephone) && TextUtils.isEmpty(updateName)) {
						if (mSaveDialog != null && mSaveDialog.isShowing()) {
							mSaveDialog.dismiss();
			            }
						finish();
						return;
					} else if (TextUtils.isEmpty(updatephone)) {
						mNumberIsNull = true;
						} else if(!Pattern.matches(SIM_NUM_PATTERN, updatephone)){
						mNumberInvalid = true;
								}
				}							
					if (setSaveFailToastText()) {
						mOnBackGoing = false; return;													
                    }
					Log.i(TAG,"********BEGIN insert to SIM card ");
				checkUri = resolver.insert(ContactsUtils.getUri(mSlotId), values);
				Log.i(TAG,"********END insert to SIM card ");
				Log.i(TAG,"values is " + values);
				Log.i(TAG, "checkUri is " + checkUri);
				if (setSaveFailToastText2(checkUri)) {
					mOnBackGoing = false; return;		
							}

					Log.i(TAG, "insert to db");
					ContactsUtils.insertToDB(updateName, updatephone, updatemail, update_additional_number
							, resolver, indicate, mSimType);
				EditSimContactActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(EditSimContactActivity.this,
								R.string.contactSavedToast, Toast.LENGTH_SHORT)
								.show();
					}
				});
				if (mSaveDialog != null && mSaveDialog.isShowing()) {
					mSaveDialog.dismiss();
	            }
					finish();
					return;
			} else if (mode == MODE_EDIT) {
				ContentValues updatevalues = new ContentValues();
			// mtk80909 for ALPS00023212
				if (!TextUtils.isEmpty(mPhoneTypeSuffix)) {
					name = (TextUtils.isEmpty(name)) ? ("/" + mPhoneTypeSuffix): (name + "/" + mPhoneTypeSuffix);
					updateName = (TextUtils.isEmpty(updateName)) ? ("/" + mPhoneTypeSuffix): (updateName + "/" + mPhoneTypeSuffix);
				}
				phone = phone.replaceAll("-", "");
				phone = phone.replaceAll(" ", "");
				updatephone = updatephone.replaceAll("-", "");
				updatephone = updatephone.replaceAll(" ", "");
				additional_number = additional_number.replaceAll("-", "");
				additional_number = additional_number.replaceAll(" ", "");
				update_additional_number = update_additional_number.replaceAll("-", "");
				update_additional_number = update_additional_number.replaceAll(" ", "");
					updatevalues.put("tag", TextUtils.isEmpty(name) ? "" : name);
				updatevalues.put("number", TextUtils.isEmpty(phone) ? "" : phone);
				updatevalues.put("newTag", TextUtils.isEmpty(updateName) ? "" : updateName);
				updatevalues.put("newNumber", TextUtils.isEmpty(updatephone) ? "" : updatephone);
				
				updatevalues.put("anr", TextUtils.isEmpty(additional_number) ? "" : additional_number);
				updatevalues.put("newAnr", TextUtils.isEmpty(update_additional_number) ? "" : update_additional_number);
				updatevalues.put("emails", TextUtils.isEmpty(email) ? "" : email);
				updatevalues.put("newEmails", TextUtils.isEmpty(updatemail) ? "" : updatemail);
				
				Log.i(TAG, "updatevalues IS " + updatevalues);
				Log.i(TAG, "mode IS " + mode);
				Cursor cursor = null;
					Log.i(TAG, "indicate  is " + indicate);
					if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
					
							try {
						if (iTel != null && !iTel.isRadioOnGemini(mSlotId)) {
								mAirPlaneModeOn = true;
								}
						if (iTel != null && iTel.isFDNEnabledGemini(mSlotId)) {
								mFDNEnabled = true;
								}
						if (!(TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(mSlotId))) {
								mSIMInvalid = true;
								}
							} catch (RemoteException ex) {
								ex.printStackTrace();
							}
				} else {
							try {
						if (iTel != null && !iTel.isRadioOn()) {
							mAirPlaneModeOn = true;
						}
						if (iTel != null && iTel.isFDNEnabled()) {
								mFDNEnabled = true;
						}
						if (!(TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimState())) {
							mSIMInvalid = true;
								}
							} catch (RemoteException ex) {
								ex.printStackTrace();
							}
								}
					Cursor c = getContentResolver().query(
							RawContacts.CONTENT_URI,
						new String[] { RawContacts.CONTACT_ID },
						RawContacts._ID + "=" + raw_contactId, null, null);
				if (c != null && c.moveToFirst()) {
					contact_id = c.getInt(0);
					Log.i(TAG,"contact_id is "+contact_id);
					c.close();
				}
				
				if (TextUtils.isEmpty(updateName) && TextUtils.isEmpty(updatephone)) {
					String where;
					if (TextUtils.isEmpty(name)) {
						Log.i(TAG,"name is empty");
					where = "number = '" + phone + "'";
					} else if (TextUtils.isEmpty(phone)) {
						Log.i(TAG, "phone is empty");
					where = "tag = '" + name + "'";
					} else {
						Log.i(TAG,"name is not empty");
					where = "tag = '" + name + "' AND number = '" + phone + "'";
					}
					Uri iccUri = ContactsUtils.getUri(mSlotId);
								Log.d(TAG, "where " + where);		
									Log.d(TAG, "iccUri ******** " + iccUri);
					int deleteDone = getContentResolver().delete(
							iccUri, where, null);
									Log.i(TAG, "deleteDone is " + deleteDone);
									 if (deleteDone == 1) {
									int deleteDB = getContentResolver().delete(
											Contacts.CONTENT_URI,
											Contacts._ID + "=" + contact_id, null);
										 Log.i(TAG,"deleteDB is "+deleteDB);
									 }
									finish();
								return;
							}
				if (mSimType == "USIM") {
					if (TextUtils.isEmpty(updatephone) /*&& TextUtils.isEmpty(updatemail) && TextUtils.isEmpty(update_additional_number)*/) {
						mNumberIsNull = true;
					} else if (!TextUtils.isEmpty(updatephone)) {
						if(!Pattern.matches(SIM_NUM_PATTERN, updatephone)){
							mNumberInvalid = true;
							}
					} 
					if (!TextUtils.isEmpty(update_additional_number)) {
						if(!Pattern.matches(SIM_NUM_PATTERN, update_additional_number)){
							mFixNumberInvalid = true;
							}
					} 
					Log.i(TAG,"mFixNumberInvalid is " + mFixNumberInvalid);
					if (!TextUtils.isEmpty(updatemail)) {
						if (!Pattern.matches(USIM_EMAIL_PATTERN, updatemail)) {
							mEmailInvalid = true;
						}
					}
				} else if (mSimType == "SIM") {
					if (updatephone.length() == 0) {
								mNumberIsNull = true;
							} else if (!Pattern.matches(SIM_NUM_PATTERN,
									updatephone)) {
								mNumberInvalid = true;
							}
						}

						if (setSaveFailToastText()) {
							mOnBackGoing = false;
							return;
						}
					cursor = resolver.query(ContactsUtils.getUri(mSlotId),// query phonebook to load
								// contacts to cache for
								// update
								ADDRESS_BOOK_COLUMN_NAMES, null, null, null);
							if (cursor != null) {
						  result = resolver.update(ContactsUtils.getUri(mSlotId), updatevalues, null,
									null);
							Log.i(TAG, "updatevalues IS " + updatevalues);
							Log.i(TAG, "result IS " + result);
							if (updateFailToastText(result)) {
								mOnBackGoing = false;
									return;
								}
							cursor.close();
							}
				Log.i(TAG, "update to db");			
                    // mtk80909 for ALPS00023212
				final ContactsUtils.NamePhoneTypePair namePhoneTypePair = new ContactsUtils.NamePhoneTypePair(updateName);
				updateName = namePhoneTypePair.name;
			        final int phoneType = namePhoneTypePair.phoneType;
			        final String phoneTypeSuffix = namePhoneTypePair.phoneTypeSuffix;
			        phone = PhoneNumberFormatUtilEx.formatNumber(phone);
			        updatephone = PhoneNumberFormatUtilEx.formatNumber(updatephone);
			        additional_number = PhoneNumberFormatUtilEx.formatNumber(additional_number);
			        update_additional_number = PhoneNumberFormatUtilEx.formatNumber(update_additional_number);
					
		        //update name
		        ContentValues namevalues = new ContentValues();
		        String wherename = Data.RAW_CONTACT_ID + " = \'"
				+ raw_contactId + "\'" + " AND " +  Data.MIMETYPE + "='" +StructuredName.CONTENT_ITEM_TYPE + "'";
					Log.i(TAG,"wherename is "+wherename);
		        if (!TextUtils.isEmpty(updateName) && !TextUtils.isEmpty(name)) {
		        	namevalues.put(StructuredName.GIVEN_NAME, updateName);
		        	int upname = resolver.update(Data.CONTENT_URI, namevalues, wherename, null);
				Log.i(TAG, "upname is " + upname);
		        } else if (TextUtils.isEmpty(updateName) && !TextUtils.isEmpty(name)) {//update name is null, delete name row		        				    
		        	int deleteName = resolver.delete(Data.CONTENT_URI, wherename, null);
		        	Log.i(TAG, "deleteName is " + deleteName);
		        } else if (TextUtils.isEmpty(name) && !TextUtils.isEmpty(updateName)) {//original name is empty, insert name row
		        	namevalues.put(StructuredName.RAW_CONTACT_ID, raw_contactId);
					namevalues.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
					namevalues.put(StructuredName.GIVEN_NAME, updateName);
					Uri upNameUri = resolver.insert(Data.CONTENT_URI, namevalues);
					Log.i(TAG, "upNameUri is " + upNameUri);
		        }
				
				
				//update number
				ContentValues phonevalues = new ContentValues();
			    String wherephone = Data.RAW_CONTACT_ID + " = \'"
				+ raw_contactId + "\'" + " AND " +  Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'" +
				" AND " + Data.IS_ADDITIONAL_NUMBER + "=0";
		        Log.i(TAG," wherephone is "+wherephone);
		        if (!TextUtils.isEmpty(updatephone) && !TextUtils.isEmpty(phone)) {
		        	phonevalues.put(Phone.NUMBER, updatephone);
		        	int upnumber = resolver.update(Data.CONTENT_URI, phonevalues, wherephone, null);
					Log.i(TAG, "upnumber is " + upnumber);
		        } else if (TextUtils.isEmpty(updatephone) && !TextUtils.isEmpty(phone)) {//update phone is null, delete number row		        				    
		        	int deletePhone = resolver.delete(Data.CONTENT_URI, wherephone, null);
		        	Log.i(TAG, "deletePhone is " + deletePhone);
		        } else if (TextUtils.isEmpty(phone) && !TextUtils.isEmpty(updatephone)) {//original phone is empty, insert number row
		        	phonevalues.put(Phone.RAW_CONTACT_ID, raw_contactId);
				phonevalues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
		        	phonevalues.put(Data.IS_ADDITIONAL_NUMBER, 0);
//			    phonevalues.put(Phone.TYPE, phoneType);
			    phonevalues.put(Data.DATA2, 2);
		        	phonevalues.put(Phone.NUMBER, updatephone);
		        	// mtk80909 for ALPS00023212
			    if (!TextUtils.isEmpty(phoneTypeSuffix)) {
					phonevalues.put(Data.DATA15, phoneTypeSuffix);
				} else {
				    phonevalues.putNull(Data.DATA15);
				}
					Uri upNumberUri = resolver.insert(Data.CONTENT_URI, phonevalues);
					Log.i(TAG, "upNumberUri is " + upNumberUri);
				}
						
				
				//if USIM
				if (mSimType.equals("USIM")) {
					//update emails
					ContentValues emailvalues = new ContentValues();		
					
					emailvalues.put(Email.TYPE, Email.TYPE_MOBILE);
					
					String wheremail = Data.RAW_CONTACT_ID + " = \'"
					+ raw_contactId + "\'" + " AND " +  Data.MIMETYPE + "='" +Email.CONTENT_ITEM_TYPE + "'";
			        Log.i(TAG,"wheremail is "+wheremail);
			        if (!TextUtils.isEmpty(updatemail) && !TextUtils.isEmpty(email)) {
			        	emailvalues.put(Email.DATA, updatemail);
			        	int upemail = resolver.update(Data.CONTENT_URI, emailvalues, wheremail, null);
						Log.i(TAG, "upemail is " + upemail);
			        } else if (TextUtils.isEmpty(updatemail) && !TextUtils.isEmpty(email)) {//update email is null, delete email row		        				    
			        	int deleteEmail = resolver.delete(Data.CONTENT_URI, wheremail, null);
			        	Log.i(TAG, "deleteEmail is " + deleteEmail);
			        } else if (TextUtils.isEmpty(email) && !TextUtils.isEmpty(updatemail)) {//original email is empty, insert email row
			        	emailvalues.put(Email.RAW_CONTACT_ID, raw_contactId);
						emailvalues.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);	
						emailvalues.put(Email.DATA, updatemail);
						Uri upEmailUri = resolver.insert(Data.CONTENT_URI, emailvalues);
						Log.i(TAG, "upEmailUri is " + upEmailUri);
			        }
																		
					
					
					//update additional number
					ContentValues additionalvalues = new ContentValues();
					String whereadditional = Data.RAW_CONTACT_ID + " = \'"
					+ raw_contactId + "\'" + " AND " +  Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'"
					+ " AND " + Data.IS_ADDITIONAL_NUMBER + " =1";
			        Log.i(TAG,"whereadditional is "+whereadditional);
			        if (!TextUtils.isEmpty(update_additional_number) && !TextUtils.isEmpty(additional_number)) {
			        	additionalvalues.put(Phone.NUMBER, update_additional_number);
			        	int upadditional = resolver.update(Data.CONTENT_URI, additionalvalues, whereadditional, null);
						Log.i(TAG, "upadditional is " + upadditional);
			        } else if (TextUtils.isEmpty(update_additional_number) && !TextUtils.isEmpty(additional_number)) {//update additional number is null, delete additional number row		        				    
			        	int deleteAdditional = resolver.delete(Data.CONTENT_URI, whereadditional, null);
			        	Log.i(TAG, "deleteAdditional is " + deleteAdditional);
			        } else if (TextUtils.isEmpty(additional_number) && !TextUtils.isEmpty(update_additional_number)) {//original additional number is empty, insert additional number row
			        	additionalvalues.put(Phone.RAW_CONTACT_ID, raw_contactId);
						additionalvalues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
						additionalvalues.put(Phone.NUMBER, update_additional_number);
						additionalvalues.put(Data.IS_ADDITIONAL_NUMBER, 1);
						additionalvalues.put(Data.DATA2, 7);
						Uri upAdditionalUri = resolver.insert(Data.CONTENT_URI, additionalvalues);
						Log.i(TAG, "upAdditionalUri is " + upAdditionalUri);
			        }
									
				}
				
				EditSimContactActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(EditSimContactActivity.this,
								R.string.contactSavedToast, Toast.LENGTH_SHORT)
								.show();
					}
				});
				if (mSaveDialog != null && mSaveDialog.isShowing()) {
					mSaveDialog.dismiss();
				}
				finish();
				return;
	}
	}
}
	private boolean setSaveFailToastText() {
		mSaveFailToastStrId = -1;
		if (mAirPlaneModeOn) {
			mSaveFailToastStrId = R.string.AirPlane_mode_on;
			mAirPlaneModeOn = false;
		} else if (mFDNEnabled) {
			mSaveFailToastStrId = R.string.FDNEnabled;
			mFDNEnabled = false;
		} else if (mSIMInvalid) {
			mSaveFailToastStrId = R.string.sim_invalid;
			mSIMInvalid = false;
		} else if (mNumberIsNull) {
			mSaveFailToastStrId = R.string.cannot_insert_null_number;
			mNumberIsNull = false;
		} else if (mNumberInvalid) {
			mSaveFailToastStrId = R.string.sim_invalid_number;
			mNumberInvalid = false;
		} else if (mEmailInvalid) {
			mSaveFailToastStrId = R.string.email_invalid;
			mEmailInvalid = false;
		} else if (mEmail2GInvalid) {
			mSaveFailToastStrId = R.string.email_2g_invalid;
			mEmail2GInvalid = false;
		} else if (mFixNumberInvalid) {
			mSaveFailToastStrId = R.string.sim_invalid_fix_number;
			mFixNumberInvalid = false;
		} else if (mAirPlaneModeOnNotEdit) {
			mSaveFailToastStrId = R.string.AirPlane_mode_on_edit;
			mAirPlaneModeOnNotEdit = false;
		}

		Log.i(TAG, "mSaveFailToastStrId IS " + mSaveFailToastStrId);
		if (mSaveFailToastStrId >= 0) {
			EditSimContactActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					btn1.setClickable(true);
					Toast.makeText(EditSimContactActivity.this,
							mSaveFailToastStrId, Toast.LENGTH_SHORT).show();
				}
			});
			if (mSaveDialog != null && mSaveDialog.isShowing()) {
				mSaveDialog.dismiss();
			}
			return true;
		}
		return false;
	}

	private boolean setSaveFailToastText2(Uri checkUri) {
		if (checkUri != null
				&& "error".equals(checkUri.getPathSegments().get(0))) {
			mSaveFailToastStrId = -1;
			if ("-1".equals(checkUri.getPathSegments().get(1))) {
				mNumberLong = true;
				mSaveFailToastStrId = R.string.number_too_long;
				mNumberLong = false;
			} else if ("-2".equals(checkUri.getPathSegments().get(1))) {
				mNameLong = true;
				mSaveFailToastStrId = R.string.name_too_long;
				mNameLong = false;
			} else if ("-3".equals(checkUri.getPathSegments().get(1))) {
				mStorageFull = true;
				mSaveFailToastStrId = R.string.storage_full;
				mStorageFull = false;
			} else if ("-6".equals(checkUri.getPathSegments().get(1))) {
				mFixNumberLong = true;
				mSaveFailToastStrId = R.string.fix_number_too_long;
				mFixNumberLong = false;
			} else if ("-10".equals(checkUri.getPathSegments().get(1))) {
				mGeneralFailure = true;
				mSaveFailToastStrId = R.string.generic_failure;
				mGeneralFailure = false;
			}
			Log.i(TAG, "setSaveFailToastText2 mSaveFailToastStrId IS "
					+ mSaveFailToastStrId);
			if (mSaveFailToastStrId >= 0) {
				EditSimContactActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						btn1.setClickable(true);
						Toast.makeText(EditSimContactActivity.this,
								mSaveFailToastStrId, Toast.LENGTH_SHORT).show();
					}
				});
				if (mSaveDialog != null && mSaveDialog.isShowing()) {
					mSaveDialog.dismiss();
				}
				return true;
			}
			return false;
		} else if (checkUri != null) {
			return false;
		} else {
			if (mSaveDialog != null && mSaveDialog.isShowing()) {
				mSaveDialog.dismiss();
			}
			return true;
		}
	}

	private boolean updateFailToastText(int result) {
		mSaveFailToastStrId = -1;
		if (result == -1) {
			mSaveFailToastStrId = R.string.number_too_long;
		} else if (result == -2) {
			mSaveFailToastStrId = R.string.name_too_long;
		} else if (result == -3) {
			mSaveFailToastStrId = R.string.storage_full;
		} else if (result == -6) {
			mSaveFailToastStrId = R.string.fix_number_too_long;
		} else if (result == -10) {
			mSaveFailToastStrId = R.string.generic_failure;
		}
		if (mSaveFailToastStrId >= 0) {
			EditSimContactActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					btn1.setClickable(true);
					Toast.makeText(EditSimContactActivity.this,
							mSaveFailToastStrId, Toast.LENGTH_SHORT).show();
				}
			});
			if (mSaveDialog != null && mSaveDialog.isShowing()) {
				mSaveDialog.dismiss();
			}
			return true;
		}
		return false;
	}
	
		
	    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
	        /**
	         * Listen for phone state changes so that we can take down the
	         * "dialpad chooser" if the phone becomes idle while the
	         * chooser UI is visible.
	         */
	        @Override
	        public void onCallStateChanged(int state, String incomingNumber) {
	        }
	
	    @Override
	    public void onServiceStateChanged(ServiceState serviceState) {
	      Log.i(TAG, "IN onServiceStateChanged ");
	      EditSimContactActivity.this.closeContextMenu();
	
	      final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
	          .getService(Context.TELEPHONY_SERVICE));// 80794
	
	      boolean sim1RadioOn = true;
	      boolean simRadioOn = true;
	      boolean hasSim1Card = true;
	      boolean hasSimCard = true;
	
	      boolean simReady = false;
	      boolean sim1Ready = false;
	      if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
	        sim1Ready = ContactsUtils
	            .simStateReady(com.android.internal.telephony.Phone.GEMINI_SIM_1);
	
	        Log.i(TAG, "sim1Ready IS " + sim1Ready);
	        Log.i(TAG, "before sim1RadioOn is " + sim1RadioOn);
	        Log.i(TAG, "before hasSim1Card is " + hasSim1Card);
	
	        try {
	                if (null != iTel && !iTel.isRadioOnGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1)) {
	            sim1RadioOn = false;
	          }
	                if (null != iTel && !iTel.hasIccCardGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1)) {
	            hasSim1Card = false;
	          }
	        } catch (RemoteException ex) {
	          ex.printStackTrace();
	        }
	
	        Log.i(TAG, "after sim1RadioOn is " + sim1RadioOn);
	        Log.i(TAG, "after hasSim1Card is " + hasSim1Card);
	
	        Log.i(TAG,
	            "PhoneStateListener.onServiceStateChanged: serviceState="
	                + serviceState);
	        /*
	         * if ((sim1PINReq || sim1PUKReq ||!sim1RadioOn || !sim1Ready)
	         * && hasSim1Card) {
	         */
	        if (!sim1RadioOn/* || !sim1Ready*/) {
	          Log
	              .i(
	                  TAG,
	                  "TelephonyManager.getDefault().getSimStateGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1) is "
	                      + TelephonyManager
	                          .getDefault()
	                          .getSimStateGemini(
	                              com.android.internal.telephony.Phone.GEMINI_SIM_1));
	          sim1RadioOn = true;
	          if (hasSim1Card&&mSlotId == com.android.internal.telephony.Phone.GEMINI_SIM_1) {
		          mAirPlaneModeOnNotEdit = true;
		          if (setSaveFailToastText()) {
				          finish();												
	                }
	          }
	        }
	        /*
	         * if ((sim2PINReq || sim2PUKReq ||!sim2RadioOn || !sim1Ready)
	         * && hasSim2Card) {
	         */
	      } else {
	        /*
	         * simPUKReq = ContactsUtils.pukRequest(); simPINReq =
	         * ContactsUtils.pinRequest();
	         */
	        simReady = ContactsUtils.simStateReady();
	        Log.i(TAG, "simReady IS " + simReady);
	        try {
	          if (null != iTel && !iTel.isRadioOn()) {
	            simRadioOn = false;
	          }
	          if (null != iTel && !iTel.hasIccCard()) {
	            hasSimCard = false;
	          }
	        } catch (RemoteException ex) {
	          ex.printStackTrace();
	        }
	        if ((!simRadioOn /*|| !simReady*/)
	        /* && hasSimCard */) {
	          Log.i(TAG, "simRadioOn is " + simRadioOn);
	          Log.i(TAG,
	              "TelephonyManager.getDefault().getSimState() is "
	                  + TelephonyManager.getDefault().getSimState());
	          Log.i(TAG, "hasSimCard is " + hasSimCard);
	          simRadioOn = true;
	          if (hasSimCard&&mSlotId == com.android.internal.telephony.Phone.GEMINI_SIM_1) {
		          mAirPlaneModeOnNotEdit = true;
					if (setSaveFailToastText()) {
				          finish();												
	                }

	          }
	        }
	      }
	    }
	  };
	  
	  PhoneStateListener mPhoneStateListener2 = new PhoneStateListener() {
	      /**
	       * Listen for phone state changes so that we can take down the
	       * "dialpad chooser" if the phone becomes idle while the
	       * chooser UI is visible.
	       */
	      @Override
	      public void onCallStateChanged(int state, String incomingNumber) {
	      }
	
	  @Override
	  public void onServiceStateChanged(ServiceState serviceState) {
	    Log.i(TAG, "IN onServiceStateChanged ");
	    EditSimContactActivity.this.closeContextMenu();
	
	    final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
	        .getService(Context.TELEPHONY_SERVICE));// 80794
	
	    boolean sim2RadioOn = true;
	    boolean hasSim2Card = true;
	    boolean sim2Ready = false;
	    if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
	      sim2Ready = ContactsUtils
	          .simStateReady(com.android.internal.telephony.Phone.GEMINI_SIM_2);
	
	      Log.i(TAG, "sim2Ready IS " + sim2Ready);
	      Log.i(TAG, "before sim2RadioOn is " + sim2RadioOn);
	      Log.i(TAG, "before hasSim2Card is " + hasSim2Card);
	
	      try {
	              if (null != iTel && !iTel.isRadioOnGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2)) {
	          sim2RadioOn = false;
	        }
	              if (null != iTel && !iTel.hasIccCardGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2)) {
	          hasSim2Card = false;
	        }
	      } catch (RemoteException ex) {
	        ex.printStackTrace();
	      }
	
	      Log.i(TAG, "after sim2RadioOn is " + sim2RadioOn);
	      Log.i(TAG, "after hasSim2Card is " + hasSim2Card);
	
	      Log.i(TAG,
	          "PhoneStateListener.onServiceStateChanged: serviceState="
	              + serviceState);
	
	      if (!sim2RadioOn/* || !sim2Ready*/) {
	        Log
	            .i(
	                TAG,
	                "TelephonyManager.getDefault().getSimStateGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2) is "
	                    + TelephonyManager
	                        .getDefault()
	                        .getSimStateGemini(
	                            com.android.internal.telephony.Phone.GEMINI_SIM_2));
	        sim2RadioOn = true;
	        if (hasSim2Card&&mSlotId == com.android.internal.telephony.Phone.GEMINI_SIM_2) {
		        mAirPlaneModeOnNotEdit = true;
				if (setSaveFailToastText()) {
			        finish();												
                }

	        }
	      }
	    }
	  }
	};
	
    private void listenPhoneStates() {
        // While we're in the foreground, listen for phone state changes,
        // purely so that we can take down the "dialpad chooser" if the
        // phone becomes idle while the chooser UI is visible.
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
                telephonyManager.listenGemini(mPhoneStateListener2, PhoneStateListener.LISTEN_SERVICE_STATE, com.android.internal.telephony.Phone.GEMINI_SIM_2);
                telephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE, com.android.internal.telephony.Phone.GEMINI_SIM_1);
        }
        else
        {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        }
    }

    private void stopListenPhoneStates() {
    	// Stop listening for phone state changes.
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
            telephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_NONE, com.android.internal.telephony.Phone.GEMINI_SIM_1);
            telephonyManager.listenGemini(mPhoneStateListener2, PhoneStateListener.LISTEN_NONE, com.android.internal.telephony.Phone.GEMINI_SIM_2);
        }
        else
        {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
    
    private Handler mHandler = new Handler() {
    	public void handleMessage(Message msg){
    		switch (msg.what) {
    			case LISTEN_PHONE_STATES:
    				listenPhoneStates();
    				break;   
    			case LISTEN_PHONE_NONE_STATES:
    				 stopListenPhoneStates();  				
    		}    		
    	}
    };
    
	private BroadcastReceiver mModemSwitchListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {        
			if (intent.getAction().equals(GeminiPhone.EVENT_PRE_3G_SWITCH)){
				Log.i(TAG,"Before modem switch .....");
				finish();
			}
		}
	};

}
