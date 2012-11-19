package com.android.contacts;

import java.util.ArrayList;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Collapser.Collapsible;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.BitmapFactory;

import com.android.contacts.util.NotifyingAsyncQueryHandler;
import com.android.internal.widget.ContactHeaderWidget;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.contacts.SpeedDialManageActivity;
import android.provider.Telephony.SIMInfo;//gemini enhancement

import com.android.internal.telephony.ITelephony;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.telephony.PhoneNumberFormatUtilEx;
import com.mediatek.telephony.PhoneNumberFormattingTextWatcherEx;


/**
 * AddSpeedDialActivity
 * @author mtk80909
 * UI to add the current contact's phone number to certain keys as speed dials.
 */
public class AddSpeedDialActivity extends Activity implements
	DialogInterface.OnClickListener,
	AdapterView.OnItemClickListener,
	NotifyingAsyncQueryHandler.AsyncQueryListener, View.OnClickListener {

	private static final String TAG = "AddSpeedDialActivity";
	
	/**
	 * Query token for contact's phone numbers
	 */
	private static final int NUMBER_QUERY_TOKEN = 47;
	
	/**
	 * Query token for confirming that phone numbers in speed dial preferences
	 * are in Contacts database.
	 */
	private static final int SPEED_DIAL_QUERY_TOKEN = 48;
	
	private SharedPreferences mPref;
	private ListView mListView;
	private ContactHeaderWidget mHeaderWidget;
	private Cursor mContactCursor;
	private Cursor mPhoneCursor;
	private ITelephony mITel;
	private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            if (mContactCursor != null && !mContactCursor.isClosed()) {
                startEntityQuery();
            }
        }
	};
	
	private NotifyingAsyncQueryHandler mHandler;
	private Uri mLookupUri;
	private ContentResolver mResolver;
	
	private static final int ASSIGNED_KEY_NOT_MODIFIED = 0;
	private static final int ASSIGNED_KEY_MODIFIED = 2;

    private int mTempIndex = 0;  // used for save index of onclick of dialog
    //private int mTempKey = 0;    // used for save assigned key which need to save
    private int mTempPrefMarkState = -1;
    private String mTempPrefNumState = "";
    private boolean mIsFirstEnterAddSpeedDial = true;
    private boolean mIsClickSpeedDialDialog = false;
    private int mModified = ASSIGNED_KEY_NOT_MODIFIED;
    
	
	/**
	 * Current values in the SharedPreferences -- Phone numbers.
	 */
	private String[] mPrefNumState = {
		"", // 0
		"", // 1
		"", // 2
		"", // 3
		"", // 4
		"", // 5
		"", // 6
		"", // 7
		"", // 8
		"", // 9
	};
	
	/**
	 * Current values in the SharedPreferences -- Phone/SIM indicators.
	 * -1 	-- Phone
	 * 0	-- Single SIM
	 * 1	-- SIM1
	 * 2	-- SIM2
	 */
	private int[] mPrefMarkState = {
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1
	};
	
	
	
	/**
	 * Projection and indices used to display (used by the MatrixCursor).
	 */
	static final String[] BIND_PROJECTION = {
		PhoneLookup._ID,				// 0
		PhoneLookup.DISPLAY_NAME,		// 1
		PhoneLookup.TYPE,				// 2
		PhoneLookup.NUMBER,				// 3
	};
	static final int BIND_ID_INDEX = 0;
	static final int BIND_DISPLAY_NAME_INDEX = 1;
	static final int BIND_LABEL_INDEX = 2;
	static final int BIND_NUMBER_INDEX = 3;
	
	/*
	 * ID's of views to bind.
	 */
	private static final int[] ADAPTER_TO = {
		R.id.sd_index,
		R.id.sd_name,
		R.id.sd_label,
		R.id.sd_number,
	};
	
	
	private AddSpeedDialAdapter mAdapter;
	private ArrayList<NumberEntry> mNumberEntries;
	private int mPhoneSimIndicator;

	private AlertDialog mNumberDialog;
	private AlertDialog mKeyDialog;
	private String mDisplayName;
	private int mQueryTimes;
	private MatrixCursor mMatrixCursor;
	private SimpleCursorAdapter mKeyDialogAdapter;
	
	/**
	 * Implementation follows ViewContactActivity.startEntityQuery().
	 */
	private synchronized void startEntityQuery() {
		closeCursor();
		mContactCursor = ViewContactActivity.setupContactCursor(mResolver, mLookupUri);
		if (mContactCursor == null) {
            mLookupUri = Contacts.getLookupUri(getContentResolver(), mLookupUri);
            mContactCursor = ViewContactActivity.setupContactCursor(mResolver, mLookupUri);
		}
		if (mContactCursor == null) {
			finish();
			return;
		}
		mContactCursor.registerContentObserver(mObserver);
		final long contactId = ContentUris.parseId(mLookupUri);
		Uri uri = Contacts.lookupContact(mResolver, mLookupUri);
		mHeaderWidget.bindFromContactLookupUri(mLookupUri);
		if(uri == null)return ;
		Cursor tmpCursor = mResolver.query(uri, 
				new String[] { RawContacts.INDICATE_PHONE_SIM, RawContacts.DISPLAY_NAME_PRIMARY }, null, null,
				null);
		if(tmpCursor != null){
			tmpCursor.moveToFirst();
			mPhoneSimIndicator = tmpCursor.getInt(0);
			mDisplayName = tmpCursor.getString(1);
			tmpCursor.close();
		}
		
		// query the contact entity in the background
		// TODO: reduce the number of columns returned
        mHandler.startQuery(NUMBER_QUERY_TOKEN, null, RawContactsEntity.CONTENT_URI, null,
                RawContacts.CONTACT_ID + "=? AND " + Phone.MIMETYPE + "=?", 
                new String[] {String.valueOf(contactId),  Phone.CONTENT_ITEM_TYPE}, 
                null);
	}
	
	/**
	 * If the current contact is a SIM contact, 
	 * set SIM pictures as their photos
	 */
	private void setSimContactPhoto() {
		final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
	    if (null == iTel) {
            Log.d(TAG, "setSimContactPhoto(), call ITelephony failed!! ");
            return;
        }
		Resources res = getResources();
        /*switch (mPhoneSimIndicator) {
        case RawContacts.INDICATE_SIM :
            mHeaderWidget.setPhoto(BitmapFactory.decodeResource(
                  res, R.drawable.contact_icon_sim));
            break;
        case RawContacts.INDICATE_SIM1 :
            mHeaderWidget.setPhoto(BitmapFactory.decodeResource(
                  res, R.drawable.contact_icon_sim1));
            break;
        case RawContacts.INDICATE_SIM2 :
            mHeaderWidget.setPhoto(BitmapFactory.decodeResource(
                  res, R.drawable.contact_icon_sim2));
            break;
        default: break;
        }*/
     if (mPhoneSimIndicator >= RawContacts.INDICATE_SIM) {
		int slotId = SIMInfo.getSlotById(AddSpeedDialActivity.this, mPhoneSimIndicator);
        Log.d(TAG, "setSimContactPhoto(), slotId= "+ slotId +" ,mPhoneSimIndicator= "+ mPhoneSimIndicator);
	    if (slotId >= 0) {
			try {
				if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
					if (iTel.getIccCardTypeGemini(slotId).equals("USIM")) {
						if (null != mHeaderWidget)
								mHeaderWidget.setPhoto(BitmapFactory.decodeResource(
													res, R.drawable.contact_icon_usim));
					} else {
						if (null != mHeaderWidget)
								mHeaderWidget.setPhoto(BitmapFactory.decodeResource(
													res, R.drawable.contact_icon_sim));
                    }
				} else {
				   if (iTel.getIccCardType().equals("USIM")) {
						if (null != mHeaderWidget)
								mHeaderWidget.setPhoto(BitmapFactory.decodeResource(
													res, R.drawable.contact_icon_usim));
				   } else {
						if (null != mHeaderWidget)
								mHeaderWidget.setPhoto(BitmapFactory.decodeResource(
													res, R.drawable.contact_icon_sim));
				   }
        }
			} catch (Exception ex) {
								ex.printStackTrace();
			}
		}
	}
       
	}
	
    private void closeCursor() {
        if (mContactCursor != null) {
            mContactCursor.unregisterContentObserver(mObserver);
            mContactCursor.close();
            mContactCursor = null;
        }
    }
	
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		if (position == 0) { // phone number view
			showPhoneNumbers();
		} else if (position == 1) { // assigned key view
			showAssignedKeys();
		} else {
			throw new RuntimeException("This is not possible");
		}
	}
	
	/**
	 * Show the dialog of all phone numbers of the current contact.
	 */
	private void showPhoneNumbers() {
		if (mNumberEntries == null || mNumberEntries.isEmpty()) {
			Log.e(TAG, "Number entries empty!!");
			return;
		} 
		if (mNumberEntries.size() == 1) {
			Log.w(TAG, "Only one phone number here. We don't need a disambig dialog.");
			return;
		}
		// now, mNumberEntries must have at least 2 items.
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		ArrayAdapter<NumberEntry> adapter = new ArrayAdapter<NumberEntry>(this, 
				R.layout.phone_disambig_item, android.R.id.text2, mNumberEntries) {
			
	        @Override
	        public View getView(int position, View convertView, ViewGroup parent) {
	            View view = super.getView(position, convertView, parent);

	            NumberEntry item = getItem(position);
	            TextView typeView = (TextView)view.findViewById(android.R.id.text1);
	            TextView numberView = (TextView)view.findViewById(android.R.id.text2);
                typeView.setText(item.label);
                numberView.setText(item.number);
	            return view;
	        }
		};
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (which < 0 || which >= mNumberEntries.size()) {
					dialog.dismiss();
					return;
				}
				mAdapter.setNumberIndex(which);
				mAdapter.notifyDataSetChanged();
			}
		};
		mNumberDialog = builder.setTitle(mDisplayName).setAdapter(adapter, listener).create();
		mNumberDialog.show();
	}
	
	/**
	 * show the dialog 
	 */
	private void showAssignedKeys() {
		initMatrixCursor();
		goOnQuery();
	}
	
	/**
	 * Searches the preferences, populates empty rows of the MatrixCursor, and starts real query for non-empty rows. 
	 */
	private void goOnQuery() {
		int end;
		for (end = mQueryTimes; end < SpeedDialManageActivity.SPEED_DIAL_MAX + 1 && TextUtils.isEmpty(mPrefNumState[end]); ++end) {
			// empty loop body
		}
		SpeedDialManageActivity.populateMatrixCursorEmpty(this, mMatrixCursor, mQueryTimes - 1, end - 1);
		Log.i(TAG, "mQueryTimes = " + mQueryTimes + ", end = " + end);
		if (end > SpeedDialManageActivity.SPEED_DIAL_MAX) {
			Log.i(TAG, "queryComplete in goOnQuery()");
			mKeyDialogAdapter.changeCursor(mMatrixCursor);
			showKeyDialog();
		} else {
			mQueryTimes = end;
			Log.i(TAG, "startQuery at mQueryTimes = " + mQueryTimes);
			Log.i(TAG, "number = " + mPrefNumState[mQueryTimes]);
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, 
					Uri.encode(mPrefNumState[mQueryTimes]));
			Log.i(TAG, "uri = " + uri);
			mHandler.startQuery(SPEED_DIAL_QUERY_TOKEN, null, uri,
					SpeedDialManageActivity.QUERY_PROJECTION, null, null, null);
			//Cursor testCursor = getContentResolver().query(uri, QUERY_PROJECTION, null, null, null);
		}
	}
	
	private void initMatrixCursor() {
		if (mMatrixCursor != null) mMatrixCursor.close();
		final int listCapacity = SpeedDialManageActivity.SPEED_DIAL_MAX - SpeedDialManageActivity.SPEED_DIAL_MIN + 1;
		mMatrixCursor = new MatrixCursor(SpeedDialManageActivity.BIND_PROJECTION, listCapacity);
		mQueryTimes = SpeedDialManageActivity.SPEED_DIAL_MIN;
	}

	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		// TODO Auto-generated method stub
		
		if (token == NUMBER_QUERY_TOKEN) {
			Log.i(TAG, "query complete");
			Log.i(TAG, "cursor = " + cursor);
			Log.i(TAG, "cursor.getCount() = " + ((cursor == null) ? 0 : cursor.getCount()));
			String[] columnNames = cursor.getColumnNames();
			String columnNamesToString = "[";
			for (int i = 0; i < columnNames.length; ++i) {
				columnNamesToString += columnNames[i] + ", ";
			}
			Log.i(TAG, columnNamesToString + "]");
			setSimContactPhoto();
			bindData(cursor);
			if (mPhoneCursor != null && !mPhoneCursor.isClosed()) {
				mPhoneCursor.close();
			}
			mPhoneCursor = cursor;
		} else if (token == SPEED_DIAL_QUERY_TOKEN) {
			if (cursor != null && cursor.getCount() > 0) {
				populateMatrixCursorRow(mQueryTimes - 1, cursor);
			} else {
				SpeedDialManageActivity.populateMatrixCursorEmpty(this, mMatrixCursor, mQueryTimes - 1, mQueryTimes);
				clearPrefStateIfNecessary(mQueryTimes);
			}
			if (cursor != null) cursor.close();
			++mQueryTimes;
			Log.i(TAG, "mQueryTimes = " + mQueryTimes);
			if (mQueryTimes <= SpeedDialManageActivity.SPEED_DIAL_MAX) {
				goOnQuery();
			} else {
				Log.i(TAG, "query stop in onQueryComplete");
				mKeyDialogAdapter.changeCursor(mMatrixCursor);
				showKeyDialog();
			}
		} else {
			Log.w(TAG, "onQueryComplete(): Should not reach here.");
		}
	}

	/**
	 * Populates the indicated row of the MatrixCursor with the data in cursor.
	 * @param row is the indicated row index of the MatrixCursor to populate
	 * @param cursor is the data source
	 */
	private void populateMatrixCursorRow(int row, Cursor cursor) {
		cursor.moveToFirst();
		String name = cursor.getString(SpeedDialManageActivity.QUERY_DISPLAY_NAME_INDEX);
		int type = cursor.getInt(SpeedDialManageActivity.QUERY_LABEL_INDEX);;
	
		String label = "";
		if (type == 0) {
			label = cursor.getString(SpeedDialManageActivity.QUERY_CUSTOM_LABEL_INDEX);
		} else {
			label = (String)CommonDataKinds.Phone.
					getTypeLabel(getResources(), type, null);
		}
		String number = cursor.getString(SpeedDialManageActivity.QUERY_NUMBER_INDEX);
		long photoId = cursor.getLong(SpeedDialManageActivity.QUERY_PHOTO_ID_INDEX);
		int simId = -1;
		if (!cursor.isNull(SpeedDialManageActivity.QUERY_INDICATE_PHONE_SIM_INDEX)) {
			simId = cursor.getInt(SpeedDialManageActivity.QUERY_INDICATE_PHONE_SIM_INDEX);
		}
		Log.i(TAG, "name = " + name + ", label = " + label + ", number = " + number);
		if (TextUtils.isEmpty(number)) {			
			SpeedDialManageActivity.populateMatrixCursorEmpty(this, mMatrixCursor, row, row + 1);
			mPrefNumState[row] = mPref.getString(String.valueOf(row), "");
			mPrefMarkState[row] = mPref.getInt(String.valueOf(SpeedDialManageActivity.offset(row)), -1);
			return;
		}
		mMatrixCursor.addRow(new String[]{String.valueOf(row + 1), 
				name, label, PhoneNumberFormatUtilEx.formatNumber(number)
				, String.valueOf(photoId), String.valueOf(simId)});
	}
	
	/**
	 * If the preference state stores a number 
	 * and the SIM card corresponding to its SIM indicator is not ready,
	 * the cursor is populated with the empty value,
	 * but the preference is not deleted.
	 * @param queryTimes
	 */
	void clearPrefStateIfNecessary(int queryTimes) {
		int simId = mPrefMarkState[queryTimes];
		// SIM state is ready
		if (simId == -1 || isSimReady(simId)) {
			mPrefMarkState[queryTimes] = -1;
			mPrefNumState[queryTimes] = "";
		}
	}
	
	/**
	 * Gets the present SIM state
	 * @param simId is the SIM ID. Legal inputs include {0, 1, 2};
	 * @return true if SIM is ready.
	 */
	private boolean isSimReady(final int simId) {
		if (null == mITel)
			return false;
            Log.d(TAG, "isSimReady(), simId=  "+simId);
		try {
			if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    int slotId = SIMInfo.getSlotById(AddSpeedDialActivity.this, simId);
                    Log.d(TAG, "isSimReady(), slotId=  "+slotId);
                if (-1 == slotId) {
                    return true;
                }
                if (mITel.isRadioOnGemini(slotId)) {
    				return !mITel.hasIccCardGemini(slotId)
    				|| (mITel.isRadioOnGemini(slotId)
    				&& !mITel.isFDNEnabledGemini(slotId)
    				&& TelephonyManager.SIM_STATE_READY == TelephonyManager
    						.getDefault().getSimStateGemini(slotId)
    				&& !ContactsUtils.isServiceRunning[slotId]);
                } else {
				return !mITel.isSimInsert(slotId)
				|| (mITel.isRadioOnGemini(slotId)
				&& !mITel.isFDNEnabledGemini(slotId)
				&& TelephonyManager.SIM_STATE_READY == TelephonyManager
						.getDefault().getSimStateGemini(slotId)
				&& !ContactsUtils.isServiceRunning[slotId]);
                }
			} else {
			    if(mITel.isRadioOn()) {
    				return !mITel.hasIccCard()
    				|| (mITel.isRadioOn()
    				&& !mITel.isFDNEnabled()
    				&& TelephonyManager.SIM_STATE_READY == TelephonyManager
    						.getDefault().getSimState()
    				&& !ContactsUtils.isServiceRunning[0]);
			} else {
				return !mITel.isSimInsert(0)
				|| (mITel.isRadioOn()
				&& !mITel.isFDNEnabled()
				&& TelephonyManager.SIM_STATE_READY == TelephonyManager
						.getDefault().getSimState()
				&& !ContactsUtils.isServiceRunning[0]);
			}
			}
		} catch (RemoteException e) {
			Log.w(TAG, "RemoteException!");
			return false;
		}
	}
	
	/**
	 * Bind the cursor to mAdapter so that the ListView will be updated
	 * @param cursor
	 */
	private void bindData(Cursor cursor) {
		
		mNumberEntries.clear();
		if (cursor == null || cursor.getCount() == 0) {
			return;
		}
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			NumberEntry numberEntry = new NumberEntry();
			int type = cursor.getInt(cursor.getColumnIndex(RawContactsEntity.DATA2));
			if (type == 0) {
				numberEntry.label = cursor.getString(cursor.getColumnIndex(RawContactsEntity.DATA3));
			} else {
			numberEntry.label = (String)CommonDataKinds.Phone.getTypeLabel(getResources(), 
					cursor.getInt(cursor.getColumnIndex(RawContactsEntity.DATA2)), null);
			}
			numberEntry.number = cursor.getString(cursor.getColumnIndex(RawContactsEntity.DATA1));
			mNumberEntries.add(numberEntry);
		}
		Collapser.collapseList(mNumberEntries);
		Log.i(TAG, "mNumberEntries in bindData: " + mNumberEntries);
		if (mAdapter == null) {
			mAdapter = new AddSpeedDialAdapter(this, mNumberEntries);
			mListView.setAdapter(mAdapter);
		} else {
			mAdapter.updateWith(mNumberEntries);
		}
	}
	
	
	/**
	 * Responses 'ADD' and 'CANCEL' button
	 */
	public void onClick(View v) {
		// TODO Auto-generated method stub
		final int viewId = v.getId();
		switch (viewId) {
		case R.id.add_button:
			saveChanges();
			Log.i(TAG, "changes saved through 'add' button");
			finish();
			break;
		case R.id.cancel_button:
			Log.i(TAG, "canceled");
			finish();
			break;
		default:
			break;
		}
	}
	
	
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		saveChanges();
		Log.i(TAG, "changes saved through 'BACK' button");
		super.onBackPressed();
	}

	private void saveChanges() {
        Log.d(TAG, " saveChanges(), mModified= "+mModified);
        if (ASSIGNED_KEY_MODIFIED == mModified) { // user assign one  key
            int index;
    		for (int i = SpeedDialManageActivity.SPEED_DIAL_MIN; i < SpeedDialManageActivity.SPEED_DIAL_MAX + 1; ++i) {
                index = findKeyByNumber(mTempPrefNumState);
                Log.d(TAG, "findKeyByNumber is  " + index);
                if (SpeedDialManageActivity.SPEED_DIAL_MIN <= index) {
                    mPrefNumState[index] = "";
                    mPrefMarkState[index] = -1;
                }
            }
            Log.d(TAG, " saveChanges(), mTempPrefNumState="+mTempPrefNumState );
            Log.d(TAG, " saveChanges(), mTempIndex="+mTempIndex );
            
            mPrefNumState[mTempIndex] = mTempPrefNumState;
            mPrefMarkState[mTempIndex] = mTempPrefMarkState;
        }
		updatePreferences();
	}
	
	private int findKeyByNumber(String number) {
        if (TextUtils.isEmpty(number)) {
            return -1;

        }
		for (int i = SpeedDialManageActivity.SPEED_DIAL_MIN; i < SpeedDialManageActivity.SPEED_DIAL_MAX + 1; ++i) {
            if (ContactsUtils.shouldCollapse(AddSpeedDialActivity.this, 
					Phone.CONTENT_ITEM_TYPE, number, Phone.CONTENT_ITEM_TYPE, mPrefNumState[i])){
				return i;
            }
        }
        return -1;
	}
	
	private void updatePreferences() {
		SharedPreferences.Editor editor = mPref.edit();
		for (int i = SpeedDialManageActivity.SPEED_DIAL_MIN; i < SpeedDialManageActivity.SPEED_DIAL_MAX + 1; ++i) {
			editor.putString(String.valueOf(i), mPrefNumState[i]);
			editor.putInt(String.valueOf(SpeedDialManageActivity.offset(i)), mPrefMarkState[i]);
		}
		editor.apply();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_speed_dial_layout);
		
		findViewById(R.id.add_button).setOnClickListener(this);
		findViewById(R.id.cancel_button).setOnClickListener(this);
		
		resolveIntent();
		setHeaderWidget();
		setupQueryHandler();
		setupListView();
		mResolver = getContentResolver();
		mNumberEntries = new ArrayList<NumberEntry>();
		Log.i(TAG, "mNumberEntries in onCreate(): " + mNumberEntries);
		mKeyDialogAdapter = new SimpleCursorAdapter(this, R.layout.speed_dial_simple_list_item, null, BIND_PROJECTION, ADAPTER_TO);
		mKeyDialogAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			// TODO use other adapter types.
			/**
			 * what is called in bindView().
			 */
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				// TODO Auto-generated method stub
				int viewId = view.getId();
				boolean isNumberEmpty = TextUtils.isEmpty(cursor.getString(BIND_NUMBER_INDEX));
				view.setEnabled(!isNumberEmpty);
				if (viewId == R.id.sd_index) {
					((TextView)view).setText(cursor.getString(columnIndex) + ": ");
				} else if (viewId == R.id.sd_name) {
					if (isNumberEmpty) {
						((TextView)view).setText(AddSpeedDialActivity.
								this.getResources().getString(R.string.available));
					} else {
						((TextView)view).setText(cursor.getString(columnIndex));
					}
				} else if (viewId == R.id.sd_label) {
					view.setVisibility(isNumberEmpty ? View.GONE : View.VISIBLE);
					((TextView)view).setText("(" + cursor.getString(columnIndex) + ")");
				} else if (viewId == R.id.sd_number) {
					view.setVisibility(isNumberEmpty ? View.GONE : View.VISIBLE);
					((TextView)view).setText(cursor.getString(columnIndex));
				}
				return true;
			}
		});
	}
	
	/**
	 * Ensures that the URI obtained from the intent is not null.
	 */
	private void resolveIntent() {
		final Intent intent = getIntent();
		mLookupUri = intent.getData();
		if (mLookupUri == null) {
			finish();
		}
	}
	
	private void setHeaderWidget() {
		mHeaderWidget = (ContactHeaderWidget) findViewById(R.id.contact_header_widget);
		mHeaderWidget.showStar(false);
		mHeaderWidget.setSelectedContactsAppTabIndex(StickyTabs.getTab(getIntent()));
	}
	
	private void setupListView() {
		mListView = (ListView) findViewById(R.id.contact_data);
        mListView.setOnItemClickListener(this);
	}
	
	private void setupQueryHandler() {
		mHandler = new NotifyingAsyncQueryHandler(this, this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		closeCursor();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		closeCursor();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getPrefStatus();
		if (mNumberEntries == null) {
			mNumberEntries = new ArrayList<NumberEntry>();
		}
		mITel = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
		startEntityQuery();
	}
	
	private void getPrefStatus() {
		Log.i(TAG, "getPrefStatus()");
		mPref = getSharedPreferences(SpeedDialManageActivity.PREF_NAME
				, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		for (int i = SpeedDialManageActivity.SPEED_DIAL_MIN; 
				i < SpeedDialManageActivity.SPEED_DIAL_MAX + 1; ++i) {
			mPrefNumState[i] = mPref.getString(String.valueOf(i), "");
			mPrefMarkState[i] = mPref.getInt(String.valueOf(SpeedDialManageActivity.offset(i)), -1);
		}
	}

	/**
	 * Adapter class intended for the display of a phone number and then the assigned key
	 * of this phone number
	 * @author mtk80909
	 *
	 */
	private final class AddSpeedDialAdapter extends BaseAdapter {
		ArrayList<NumberEntry> mNumberEntries;
		Context mContext;
		LayoutInflater mInflater;
		private int mNumberIndex;
		
		public AddSpeedDialAdapter(Context context, ArrayList<NumberEntry> numberEntries) {
			mContext = context;
			mNumberEntries = numberEntries;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mNumberIndex = 0;
		}
		
		public void updateWith(ArrayList<NumberEntry> numberEntries) {
			mNumberEntries = numberEntries;
			notifyDataSetChanged();
		}
		
		public int getCount() {
			// TODO Auto-generated method stub
			return 2;
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
			
			Log.i(TAG, "getView(): position = " + position);
			
			if (mNumberIndex >= mNumberEntries.size()) {
				mNumberIndex = 0;
				return null;
			}
			
			Log.i(TAG, "mNumberIndex = " + mNumberIndex);
			
			NumberEntry numberEntry = mNumberEntries.get(mNumberIndex);
			View v;
            if (convertView != null) {
                v = convertView;
            } else {
                v = mInflater.inflate(R.layout.add_speed_dial_item, parent, false);
            }
            bindView(position, v, numberEntry);
			return v;
		}
		
		protected void bindView(int position, View v, NumberEntry numberEntry) {
			Log.i(TAG, "bindView(): position = " + position);
			TextView labelView = (TextView)v.findViewById(R.id.sd_label);
			TextView numberView = (TextView)v.findViewById(R.id.sd_number);

            View moreButton = v.findViewById(R.id.sd_more);
            
			if (position == 0) {// display the selected phone number
				labelView.setText(numberEntry.label);
				numberView.setText(numberEntry.number);
                moreButton.setVisibility( mNumberEntries.size() <= 1 ? View.GONE : View.VISIBLE );
			} else if (position == 1) {// display the assigned key
				labelView.setText(AddSpeedDialActivity
						.this.getResources().getString(R.string.assigned_key));
                Log.d(TAG, "bindView(), AddSpeedDialActivity.this.mIsFirstEnterAddSpeedDial= "+ AddSpeedDialActivity.this.mIsFirstEnterAddSpeedDial);
                if ( !AddSpeedDialActivity.this.mIsFirstEnterAddSpeedDial) {  // update by showKeyDialog
                    numberView.setText(AddSpeedDialActivity.this.getAssignedKeyExt((String)numberEntry.number));
                } else {    // first enter add speed dial
				numberView.setText(AddSpeedDialActivity.this.getAssignedKey((String)numberEntry.number));
                }
                mTempPrefNumState = numberEntry.number.toString();
                mTempPrefMarkState = mPhoneSimIndicator;
                if (mIsClickSpeedDialDialog) {
                    mIsClickSpeedDialDialog = false;
                }
				
			} else {
				throw new RuntimeException("This is not possible");
			}
            
		}
		
		public void setNumberIndex(int numberIndex) {
			mNumberIndex = numberIndex;
		}
		
		public int getNumberIndex() {
			return mNumberIndex;
		}
	}
	
	/**
	 * Search the mPrefXXXState to find to which key number is assigned.
	 * @param number is the input phone number.
	 * @return
	 */
	private String getAssignedKey(String number) {
		for (int i = SpeedDialManageActivity.SPEED_DIAL_MIN; i <= 
				SpeedDialManageActivity.SPEED_DIAL_MAX; ++i) {
			if (ContactsUtils.shouldCollapse(this, Phone.CONTENT_ITEM_TYPE, 
					number, Phone.CONTENT_ITEM_TYPE, mPrefNumState[i])) {
				return String.valueOf(i);
			}
		}
		return getResources().getString(R.string.unassigned);
	}
	
	/**
	 * Search the mPrefXXXState to find to which key number is assigned,if exist and want to
	 * replace, then return new key, if not replace , return old key, if number not exist in mPrefXXXState.
	 * return new key if assigned key or return unassigned if unassigned key
	 * @param number is the input phone number.
	 * @return
	 */
	private String getAssignedKeyExt(String number) {
        boolean hasNum = hasNumberByKey(mTempIndex);
		for (int i = SpeedDialManageActivity.SPEED_DIAL_MIN; i <= 
				SpeedDialManageActivity.SPEED_DIAL_MAX; ++i) {
			if (ContactsUtils.shouldCollapse(this, Phone.CONTENT_ITEM_TYPE, 
					number, Phone.CONTENT_ITEM_TYPE, mPrefNumState[i])) {
				//modify already assigned key to another key	
				Log.d(TAG, "getAssignedKeyExt(), Find Same Number, mIsClickSpeedDialDialog= "+ mIsClickSpeedDialDialog);
				Log.d(TAG, "getAssignedKeyExt(), Find Same Number, mTempIndex= "+ mTempIndex);
				if (mIsClickSpeedDialDialog && (0 !=  mTempIndex) && (i != mTempIndex) && !hasNum) { 
                    mModified = ASSIGNED_KEY_MODIFIED;
                    return String.valueOf(mTempIndex);
                }
                
				if (mIsClickSpeedDialDialog && (0 !=  mTempIndex) && hasNum) { 
                    Toast.makeText(AddSpeedDialActivity.this, getString(R.string.reselect_key), Toast.LENGTH_LONG).show();
                }
				mModified = ASSIGNED_KEY_NOT_MODIFIED;
				return String.valueOf(i);
			}
		}
        
        if (0 !=  mTempIndex && !hasNum) {
            Log.d(TAG, "getAssignedKeyExt(), NOT Find Same Number and other number, mTempIndex= "+ mTempIndex);
            mModified = ASSIGNED_KEY_MODIFIED;
		    return String.valueOf(mTempIndex);
        }

        if (mIsClickSpeedDialDialog && (0 !=  mTempIndex) && hasNum) { 
            Toast.makeText(AddSpeedDialActivity.this, getString(R.string.reselect_key), Toast.LENGTH_LONG).show();
        }

        mModified = ASSIGNED_KEY_NOT_MODIFIED;
		return getResources().getString(R.string.unassigned);
	}    

	private boolean hasNumberByKey(int key) {
        Log.d(TAG, "hasNumberByKey(), mPrefNumState["+key+"]= " + mPrefNumState[key]);
        if (!TextUtils.isEmpty(mPrefNumState[key])){
            Log.d(TAG, "hasNumberByKey(), find number by key!!!, key= "+ key);
			return true;
        }
        Log.d(TAG, "hasNumberByKey(), not find number by key!!! , key"+ key);
        return false;
    }

    
	/**
	 * A simple collapsible class for the phone number entry.
	 * @author mtk80909
	 *
	 */
	private final class NumberEntry implements Collapser.Collapsible<NumberEntry> {
		CharSequence label;
		CharSequence number;
		
		@Override
		public boolean collapseWith(NumberEntry t) {
			// TODO Auto-generated method stub
			if (!shouldCollapseWith(t)) {
				return false;
			}
			label = t.label;
			number = t.number;
			return true;
		}

		@Override
		public boolean shouldCollapseWith(NumberEntry t) {
			// TODO Auto-generated method stub
			if (t == null) return false;
			return ContactsUtils.shouldCollapse(AddSpeedDialActivity.this, 
					Phone.CONTENT_ITEM_TYPE, number, Phone.CONTENT_ITEM_TYPE, t.number);
		}
	}
	
	/**
	 * After query, initializes and shows the dialog of assigned keys.
	 * The dialog is binded with mKeyDialogAdapter.
	 */
	private void showKeyDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.speed_dial_view));
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				which += 2;
				//mPrefNumState[which] = (String)mNumberEntries.get(mAdapter.mNumberIndex).number;
				//mPrefMarkState[which] = mPhoneSimIndicator;
                mTempIndex = which;
                Log.d(TAG, "showKeyDialog(), mTempIndex= "+mTempIndex);
				//mTempPrefNumState = (String)mNumberEntries.get(mAdapter.mNumberIndex).number;
				//mTempPrefMarkState = mPhoneSimIndicator;
                mIsFirstEnterAddSpeedDial = false;
                mIsClickSpeedDialDialog = true;
				mAdapter.notifyDataSetChanged();
			}
		};
		builder.setAdapter(mKeyDialogAdapter, listener);
		mKeyDialog = builder.create();
		mKeyDialog.show();
	}
}
