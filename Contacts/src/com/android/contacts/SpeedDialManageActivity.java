package com.android.contacts;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.ITelephony;
import android.provider.Telephony.SIMInfo;//gemini enhancement

import java.lang.ref.WeakReference;
import com.mediatek.telephony.PhoneNumberFormatUtilEx;
import com.mediatek.telephony.PhoneNumberFormattingTextWatcherEx;

public class SpeedDialManageActivity extends ListActivity
		implements View.OnClickListener {
	
	private SimpleCursorAdapter mAdapter;
	private QueryHandler mQueryHandler;
	private ListView mListView;
	
	/*
	 * SharedPreferences for speed dial, 
	 * storing <index, number> and <index, simId> pairs.
	 */
	private SharedPreferences mPref;
	
	/*
	 * Records the query times of the current cursor.
	 */
	private int mQueryTimes;
	
	/*
	 * The cursor used to bind view.
	 */
	private MatrixCursor mMatrixCursor;
	
	/*
	 * Position on the list view for adding a speed dial number .
	 */
	private int mAddPosition = -1;

	/*
	 * Position on the list view for removing a speed dial number.
	 */
	private int mRemovePosition = -1;
	
	/*
	 * State variable to record whether the preferences have been loaded.
	 */
	private boolean mHasGotPref;
	
	
	private AlertDialog mRemoveConfirmDialog = null;
	
	private static final String TAG = "SpeedDialManageActivity";
	
	/*
	 * Projection used to query the contacts db.
	 */
	static final String[] QUERY_PROJECTION = {
		PhoneLookup._ID,				// 0
        PhoneLookup.DISPLAY_NAME,		// 1
        PhoneLookup.TYPE,				// 2
        PhoneLookup.NUMBER,				// 3
        PhoneLookup.INDICATE_PHONE_SIM, // 4
        PhoneLookup.PHOTO_ID,			// 5
        PhoneLookup.LABEL,				// 6
	};
	
	/*
	 * Projection indices used to query the contacts db.
	 */
	static final int QUERY_ID_INDEX = 0; 		
	static final int QUERY_DISPLAY_NAME_INDEX = 1;
	static final int QUERY_LABEL_INDEX = 2;
	static final int QUERY_NUMBER_INDEX = 3;
	static final int QUERY_INDICATE_PHONE_SIM_INDEX = 4;
	static final int QUERY_PHOTO_ID_INDEX = 5;
	static final int QUERY_CUSTOM_LABEL_INDEX = 6;

	/*
	 * Projection and indices used to display (used by the MatrixCursor).
	 */
	static final String[] BIND_PROJECTION = {
		PhoneLookup._ID,				// 0
		PhoneLookup.DISPLAY_NAME,		// 1
		PhoneLookup.TYPE,				// 2
		PhoneLookup.NUMBER,				// 3
		PhoneLookup.PHOTO_ID,			// 4
		PhoneLookup.INDICATE_PHONE_SIM, // 5
	};
	static final int BIND_ID_INDEX = 0;
	static final int BIND_DISPLAY_NAME_INDEX = 1;
	static final int BIND_LABEL_INDEX = 2;
	static final int BIND_NUMBER_INDEX = 3;
	static final int BIND_PHOTO_ID_INDEX = 4;
	static final int BIND_INDICATE_PHONE_SIM_INDEX = 5;
	
	
	/*
	 * The name of the preferences file.
	 */
	static final String PREF_NAME = "speed_dial";
	
	/*
	 * ID's of views to bind.
	 */
	private static final int[] ADAPTER_TO = {
		R.id.sd_index,
		R.id.sd_name,
		R.id.sd_label,
		R.id.sd_number,
		R.id.sd_photo,
		R.id.sd_remove,
	};
	
	/*
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
	
	/*
	 * Current values in the SharedPreferences -- Phone/SIM indicators.
	 * -1 	-- Phone
	 * 0	-- Single SIM
	 * 1	-- SIM1
	 * 2	-- SIM2
	 */
	private int[] mPrefMarkState = {
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1
	};
	
	/*
	 * 2, 3, ..., 9 are current available numbers to assign speed dials.
	 */
	static final int SPEED_DIAL_MIN = 2;
	static final int SPEED_DIAL_MAX = 9;
	
	private static final int LIST_CAPACITY = 9;
	
	private static final int REQUEST_CODE_PICK_CONTACT = 1;
	
	private static final int QUERY_TOKEN = 47;

	private static final int MENU_REMOVE = 1;
	
	private ITelephony mITel;
	private ContactPhotoLoader mPhotoLoader;
	private boolean mHasNumberByKey = false;

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.i(TAG, "onListItemClick");
		
		if (position == 0) return;
		
    	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    	intent.setType(Phone.CONTENT_ITEM_TYPE);
    	//intent.putExtra("request_email", false);
    	mAddPosition = position;
    	startActivityForResult(intent, REQUEST_CODE_PICK_CONTACT);
	}

	/**
	 * This method runs after selecting a contact phone number to add to speed dial.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (REQUEST_CODE_PICK_CONTACT != requestCode || RESULT_OK != resultCode || data == null) {
			mAddPosition = -1;
			return;
		}
		
		String number = data.getStringExtra("number");
		int simId = data.getIntExtra("simId", -1);
		getPrefStatus();
		mHasGotPref = true;
        int tempKey = findKeyByNumber(number);
        Log.d(TAG, "onActivityResult(),  after findKeyByNumber(), tempKey="+tempKey);
        if ( SpeedDialManageActivity.SPEED_DIAL_MIN > tempKey) {
		mPrefNumState[mAddPosition + 1] = number;
		mPrefMarkState[mAddPosition + 1] = simId;
            mHasNumberByKey = false;
        } else {
            mHasNumberByKey = true;
        }
		Log.i(TAG, "onActivityResult: number = " + number + ", simId = " + simId);
		for (int i = 0; i < mPrefNumState.length; ++i) {
			Log.i(TAG, "mPrefNumState[" + i + "] = " + mPrefNumState[i] + ", mPrefMarkState[" + i + "]" + mPrefMarkState[i]);
		}
	}

	private int findKeyByNumber(String number) {
        if (TextUtils.isEmpty(number)) {
            return -1;

        }
		for (int i = SpeedDialManageActivity.SPEED_DIAL_MIN; i < SpeedDialManageActivity.SPEED_DIAL_MAX + 1; ++i) {
            if (ContactsUtils.shouldCollapse(SpeedDialManageActivity.this, 
					Phone.CONTENT_ITEM_TYPE, number, Phone.CONTENT_ITEM_TYPE, mPrefNumState[i])){
				return i;
            }
        }
        return -1;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate() , begin");
		mListView = getListView();
		mListView.setOnCreateContextMenuListener(this);
		mAdapter = new SimpleCursorAdapter(this, R.layout.speed_dial_list_item, 
				null, BIND_PROJECTION, ADAPTER_TO);
		mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			// TODO use other adapter types.
			/**
			 * what is called in bindView().
			 */
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			    Log.d(TAG, "setViewValue() begin");
				// TODO Auto-generated method stub
				int viewId = view.getId();
				boolean isNumberEmpty = TextUtils.isEmpty(cursor.getString(BIND_NUMBER_INDEX));
				if (viewId == R.id.sd_number || viewId == R.id.sd_label || viewId == R.id.sd_remove) {
					view.setVisibility(isNumberEmpty ? View.GONE : View.VISIBLE);
					if (!isNumberEmpty && viewId == R.id.sd_remove) {
						view.setOnClickListener(SpeedDialManageActivity.this);
					}
					if (viewId == R.id.sd_remove) return true;
				} else if (viewId == R.id.sd_name) {
					view.setEnabled(!isNumberEmpty || TextUtils.equals(cursor.getString(BIND_ID_INDEX), "1"));
				} else if (viewId == R.id.sd_photo) {
					view.setClickable(false);
					view.setVisibility(isNumberEmpty ? View.GONE : View.VISIBLE);
					if (!isNumberEmpty) {
						Log.i(TAG, "phone/sim indicator = " + cursor.getString(BIND_INDICATE_PHONE_SIM_INDEX));
						Log.i(TAG, "int value = " 
								+ Integer.valueOf(cursor.getString(BIND_INDICATE_PHONE_SIM_INDEX)).intValue());
						view.setBackgroundDrawable(null);
						int simId = Integer.valueOf(cursor.getString(BIND_INDICATE_PHONE_SIM_INDEX)).intValue();
						
						mPhotoLoader.loadPhoto((ImageView)view, 
									Long.valueOf(cursor.getString(BIND_PHOTO_ID_INDEX)).longValue(), 
									SIMInfo.getSlotById(SpeedDialManageActivity.this, simId));
					}
					
					//Log.d(TAG, "setViewValue() , end, return true");
					return true;
				}
				//Log.d(TAG, "setViewValue() , end, return false");
				return false; // so that text can be set to the view
			}
		});
		
		setListAdapter(mAdapter);
		mQueryHandler = new QueryHandler(this);
		mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture);
		
		Log.d(TAG, "onCreate() , end");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        Log.d(TAG, "onResume begin");
		mITel = ITelephony.Stub.asInterface(ServiceManager
					.getService(Context.TELEPHONY_SERVICE));
		if (!mHasGotPref) getPrefStatus();
		mHasGotPref = false;
		mPhotoLoader.clear();
		mPhotoLoader.resume();
		startQuery();
        Log.d(TAG, "onResume end");
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
        Log.d(TAG, "onDestroy");
		mPhotoLoader.stop();
        if (mMatrixCursor != null) mMatrixCursor.close();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    Log.i(TAG, "context menu created");
    //Log.i(TAG, "current view is " + v);
    
    AdapterView.AdapterContextMenuInfo info;
    try {
         info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    } catch (ClassCastException e) {
        Log.e(TAG, "bad menuInfo", e);
        return;
    }
    
    // This can be null sometimes, don't crash...
    if (info == null) {
        Log.e(TAG, "bad menuInfo");
        return;
    }
    Log.i(TAG, "onCreateContextMenu(), info.position=" + info.position);
	Cursor cursor = null;
	cursor = (Cursor) getListAdapter().getItem(info.position);
	if (cursor == null) {
        // For some reason the requested item isn't available, do nothing
        return;
    }
    String name = cursor.getString(BIND_DISPLAY_NAME_INDEX);
    String number = cursor.getString(BIND_NUMBER_INDEX);
    
   // if (v instanceof ListView) {
      //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
      //View itemView = ((ListView)v).getChildAt(info.position);  
      //String number= ((TextView)itemView.findViewById(R.id.sd_number)).getText().toString();
      //String name= ((TextView)itemView.findViewById(R.id.sd_name)).getText().toString();
      if (!TextUtils.isEmpty(number)) {
        Log.i(TAG, "What about really creating?");
        menu.add(0, MENU_REMOVE, 0, R.string.remove_speed_dial);
        if (!TextUtils.isEmpty(name)) {
            menu.setHeaderTitle(name); // For header part of the menu list to display.
        } else {
            menu.setHeaderTitle(number);
        }
      }
    //}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)(item.getMenuInfo());
		confirmRemovePosition(info.position);
		return true;
	}

	/**
	 * Pops up a dialog for the user to confirm to remove a speed dial number.
	 * @param position is the absolute position in the ListView.
	 */
	void confirmRemovePosition(int position) {
		Log.d(TAG, "confirmRemovePosition(), position= " +position);
	
		Cursor c = (Cursor)mAdapter.getItem(position);
		if(c == null)return;
		String name = c.getString(BIND_DISPLAY_NAME_INDEX);
		Log.d(TAG, "confirmRemovePosition(), name= "+ name);
		String label = c.getString(BIND_LABEL_INDEX);
		//String message1 = getResources().getString(R.string.remove_sd_confirm_1);
		//String message2 = getResources().getString(R.string.remove_sd_confirm_2);
		//String message = message1 + " " + name + " (" + label + ") " + message2 + " " + (position + 1) + "?";
		
		String message = getString(R.string.remove_sd_confirm_1, name, label, String.valueOf(position + 1));
		Log.d(TAG, "confirmRemovePosition(), message= "+ message);
		mRemovePosition = position;
        if (mRemoveConfirmDialog == null) {
		mRemoveConfirmDialog = new AlertDialog
				.Builder(this)
				.setCancelable(true)
				.setOnCancelListener(new DialogInterface.OnCancelListener() 
				{
					public void onCancel(DialogInterface arg0) {
						// TODO Auto-generated method stub
						mRemovePosition = -1;
						mRemoveConfirmDialog = null;
					}
				})
				.setTitle(R.string.remove_speed_dial)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(message)
				.setPositiveButton(R.string.remove_speed_dial, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						actuallyRemove();
						//startQuery();
						//mListView.requestLayout();
						mRemovePosition = -1;
							mRemoveConfirmDialog = null;
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mRemovePosition = -1;
							mRemoveConfirmDialog = null;
					}
				})
				.create();
        }
		mRemoveConfirmDialog.show();
	}
	
	/**
	 * Removes the speed dial after click 'Remove' on the confirm dialog
	 * and updates the preferences.
	 */
	void actuallyRemove() {
		/*
		// Set the TextViews
		View v = mListView.getChildAt(mRemovePosition - mListView.getFirstVisiblePosition());
		TextView nameView = (TextView)(v.findViewById(R.id.sd_name));
		nameView.setText(R.string.add_speed_dial);
		nameView.setEnabled(false);
		TextView labelView = (TextView)(v.findViewById(R.id.sd_label));
		labelView.setText("");
		labelView.setVisibility(View.GONE);
		TextView numberView = (TextView)(v.findViewById(R.id.sd_number));
		numberView.setText("");
		numberView.setVisibility(View.GONE);
		ImageView removeView = (ImageView)(v.findViewById(R.id.sd_remove));
		removeView.setVisibility(View.GONE);
		ImageView photoView = (ImageView)(v.findViewById(R.id.sd_photo));
		photoView.setVisibility(View.GONE);
		// Pref
		*/
		mPrefNumState[mRemovePosition + 1] = "";
		mPrefMarkState[mRemovePosition + 1] = -1;
		SharedPreferences.Editor editor = mPref.edit();
		editor.putString(String.valueOf(mRemovePosition + 1), mPrefNumState[mRemovePosition + 1]);
		editor.putInt(String.valueOf(offset(mRemovePosition + 1)), mPrefMarkState[mRemovePosition + 1]);
		editor.apply();
        startQuery();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		if (mRemoveConfirmDialog != null && mRemoveConfirmDialog.isShowing()) {
			mRemoveConfirmDialog.dismiss();
			mRemoveConfirmDialog = null;
		}
	}

	/**
	 * Read the preferences file and put the preferences into the arrays
	 * mPrefNumState[] and mPrefMarkState[]
	 */
	private void getPrefStatus() {
		Log.i(TAG, "getPrefStatus()");
		mPref = getSharedPreferences(PREF_NAME, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		mHasGotPref = true;
		for (int i = SPEED_DIAL_MIN; i < SPEED_DIAL_MAX + 1; ++i) {
			mPrefNumState[i] = mPref.getString(String.valueOf(i), "");
			mPrefMarkState[i] = mPref.getInt(String.valueOf(offset(i)), -1);
		}
	}
	
	/**
	 * When the preference of a speed dial is <N, Phone number>,
	 * the SIM indicator is stored as <N + 100, Phone/SIM/SIM1/SIM2>. 
	 * @param i is the number to assign a speed dial to.
	 * @return i + 100
	 */
	static int offset(int i) {
		return i + 100;
	}
	
	private void startQuery() {
		initMatrixCursor();
		Log.i(TAG, "startQuery(), query init");
		goOnQuery2();
	}	
	
	/**
	 * Initiates the cursor, puts the 'Voicemail' tag into the first row of the cursor and resets mQueryTimes.
	 */
	private void initMatrixCursor() {
//		if (mMatrixCursor != null) mMatrixCursor.close();
		mMatrixCursor = new MatrixCursor(BIND_PROJECTION, LIST_CAPACITY);
		mMatrixCursor.addRow(new String[] {"1", getResources().getString(R.string.voicemail), "", "", "", ""});
		mQueryTimes = SPEED_DIAL_MIN;
	}
	
	/**
	 * Searches the preferences, populates empty rows of the MatrixCursor, and starts real query for non-empty rows. 
	 */
	private void goOnQuery() {
		int end;
		for (end = mQueryTimes; end < SPEED_DIAL_MAX + 1 && TextUtils.isEmpty(mPrefNumState[end]); ++end) {
			// empty loop body
		}
		populateMatrixCursorEmpty(this, mMatrixCursor, mQueryTimes - 1, end - 1);
		Log.i(TAG, "goOnQuery(), mQueryTimes = " + mQueryTimes + ", end = " + end);
		if (end > SPEED_DIAL_MAX) {
			Log.i(TAG, "goOnQuery(), queryComplete in goOnQuery()");
			mAdapter.changeCursor(mMatrixCursor);
			updatePreferences();
			showToastIfNecessary();
		} else {
			mQueryTimes = end;
			Log.i(TAG, "goOnQuery(), startQuery at mQueryTimes = " + mQueryTimes);
			Log.i(TAG, "goOnQuery(), number = " + mPrefNumState[mQueryTimes]);
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, 
					Uri.encode(mPrefNumState[mQueryTimes]));
			Log.i(TAG, "goOnQuery(), uri = " + uri);
			mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
					QUERY_PROJECTION, null, null, null);
			//Cursor testCursor = getContentResolver().query(uri, QUERY_PROJECTION, null, null, null);
		}
	}
	
	private void goOnQuery2() {
		for(mQueryTimes = SPEED_DIAL_MIN; mQueryTimes<SPEED_DIAL_MAX+1; mQueryTimes++){
			if(TextUtils.isEmpty(mPrefNumState[mQueryTimes])){
				populateMatrixCursorEmpty(this, mMatrixCursor, mQueryTimes-1, mQueryTimes);
				mPrefNumState[mQueryTimes] = mPref.getString(String.valueOf(mQueryTimes), "");
				mPrefMarkState[mQueryTimes] = mPref.getInt(String.valueOf(offset(mQueryTimes)), -1);
				continue;
			}
			populateMatrixCursorEmpty(this, mMatrixCursor, mQueryTimes - 1, mQueryTimes - 1);
			Log.i(TAG, "goOnQuery(), startQuery at mQueryTimes = " + mQueryTimes);
			Log.i(TAG, "goOnQuery(), number = " + mPrefNumState[mQueryTimes]);
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, 
					Uri.encode(mPrefNumState[mQueryTimes]));
			Log.i(TAG, "goOnQuery(), uri = " + uri);
			Cursor cursor = getContentResolver().query(uri, QUERY_PROJECTION, null, null, null);
			if(cursor != null && cursor.getCount() > 0){
				populateMatrixCursorRow(mQueryTimes - 1, cursor);
			}else{
				Log.d(TAG, "query(), get nothing after query ");
				populateMatrixCursorEmpty(SpeedDialManageActivity.this, mMatrixCursor, mQueryTimes - 1, mQueryTimes);
				clearPrefStateIfNecessary(mQueryTimes);				
			}
			if(cursor != null)cursor.close();
		}
		updatePreferences();
		showToastIfNecessary();
		mAdapter.changeCursor(mMatrixCursor);
	}
	
	/**
	 * Populates the MatrixCursor with empty rows using 'Tap to add speed dial' tag.
	 * Rows ranged in [start, end) will be filled.
	 * @param start is the first row to populate.
	 * @param end is one past the last row to populate.
	 */
	static void populateMatrixCursorEmpty(Context context, MatrixCursor matrixCursor, int start, int end) {
		for (int i = start; i < end; ++i) {
			matrixCursor.addRow(new String[] {String.valueOf(i + 1), 
					context.getResources().getString(R.string.add_speed_dial), "", "", "-1", "-1"});
		}
	}

	/**
	 * Populates the indicated row of the MatrixCursor with the data in cursor.
	 * @param row is the indicated row index of the MatrixCursor to populate
	 * @param cursor is the data source
	 */
	private void populateMatrixCursorRow(int row, Cursor cursor) {
		cursor.moveToFirst();
		String name = cursor.getString(QUERY_DISPLAY_NAME_INDEX);
		int type = cursor.getInt(QUERY_LABEL_INDEX);
		String label = "";
		if (type == 0) {
			label = cursor.getString(QUERY_CUSTOM_LABEL_INDEX);
		} else { 
			label = (String)CommonDataKinds.Phone.getTypeLabel(getResources(), 
				type, null);
		}
		String number = cursor.getString(QUERY_NUMBER_INDEX);
		long photoId = cursor.getLong(QUERY_PHOTO_ID_INDEX);
		int simId = -1;
		if (!cursor.isNull(QUERY_INDICATE_PHONE_SIM_INDEX)) {
			simId = cursor.getInt(QUERY_INDICATE_PHONE_SIM_INDEX);
		}
		Log.i(TAG, "populateMatrixCursorRow(), name = " + name + ", label = " + label + ", number = " + number);
		if (TextUtils.isEmpty(number)) {			
			populateMatrixCursorEmpty(this, mMatrixCursor, row, row + 1);
			mPrefNumState[row] = mPref.getString(String.valueOf(row), "");
			mPrefMarkState[row] = mPref.getInt(String.valueOf(offset(row)), -1);
			return;
		}
		mMatrixCursor.addRow(new String[]{String.valueOf(row + 1), 
				name, label, PhoneNumberFormatUtilEx.formatNumber(number)
				, String.valueOf(photoId), String.valueOf(simId)});
	}
	
	/**
	 * Updates the SharedPreferences.
	 */
	private void updatePreferences() {
		SharedPreferences.Editor editor = mPref.edit();
		for (int i = SPEED_DIAL_MIN; i < SPEED_DIAL_MAX + 1; ++i) {
            
            int simId = mPrefMarkState[i];
            boolean simReady = simId == -1 || isSimReady(simId);
            Log.d (TAG, "updatePreferences(), isSimReady("+i+") = "+ simReady);
            if (simReady) { 
			editor.putString(String.valueOf(i), mPrefNumState[i]);
			editor.putInt(String.valueOf(offset(i)), mPrefMarkState[i]);
		}
		}
		editor.apply();
	}
	
	/**
	 * If this activity has just been returned to from adding a new speed dial
	 * and the adding action is successful, a toast of the newly added speed dial 
	 * will show.
	 */
	private void showToastIfNecessary() {
	    Log.d(TAG, "showToastIfNecessary(),  mAddPosition= "+ mAddPosition);
	    Log.d(TAG, "showToastIfNecessary(),  mHasNumberByKey= "+ mHasNumberByKey);
        
		if (mAddPosition == -1) {
			return;
		}
        if (mHasNumberByKey) {
            mAddPosition = -1;
            mMatrixCursor.moveToPosition(-1);
            Toast.makeText(this, getString(R.string.reselect_number), Toast.LENGTH_LONG).show();
            return;
        }
		Log.i(TAG, "showToastIfNecessary(), mMatrixCursor's present position: " + mMatrixCursor.getPosition()
				+ ", mMatrixCursor's count: " + mMatrixCursor.getCount()
				+ ", mPosition + 1: " + (mAddPosition + 1));
		mMatrixCursor.moveToPosition(mAddPosition);
		CharSequence name = mMatrixCursor.getString(BIND_DISPLAY_NAME_INDEX);
		CharSequence label = mMatrixCursor.getString(BIND_LABEL_INDEX);
		int index = mAddPosition + 1;
		CharSequence fullInfo = getString(R.string.speed_dial_added, name, label, String.valueOf(index));
		//CharSequence fullInfo = name + " (" + label + ") " + basicText + " " + index;
		Toast.makeText(this, fullInfo, Toast.LENGTH_LONG).show();
		mAddPosition = -1;
		mMatrixCursor.moveToPosition(-1);
	}
	
	private class QueryHandler extends AsyncQueryHandler {
		protected final WeakReference<SpeedDialManageActivity> mActivity;
		
		public QueryHandler(Context context) {
			super(context.getContentResolver());
			// TODO Auto-generated constructor stub
			mActivity = new WeakReference<SpeedDialManageActivity>((SpeedDialManageActivity)context);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			Log.i(TAG, "onQueryComplete(), cursor = " + cursor);
			if (cursor != null && cursor.getCount() > 0) {
				populateMatrixCursorRow(mQueryTimes - 1, cursor);
			} else {
			Log.d(TAG, "onQueryComplete(), get nothing after query ");
				populateMatrixCursorEmpty(SpeedDialManageActivity.this, mMatrixCursor, mQueryTimes - 1, mQueryTimes);
				clearPrefStateIfNecessary(mQueryTimes);
			}
			if (cursor != null) cursor.close();
			++mQueryTimes;
			Log.i(TAG, "mQueryTimes = " + mQueryTimes);
			if (mQueryTimes <= SPEED_DIAL_MAX) {
				goOnQuery();
			} else {
				Log.i(TAG, "onQueryComplete(), query stop in onQueryComplete");
				updatePreferences();
				showToastIfNecessary();
				mAdapter.changeCursor(mMatrixCursor);
			}
		}	
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
				int slotId = SIMInfo.getSlotById(SpeedDialManageActivity.this, simId);
				Log.d(TAG, "isSimReady(), slotId=  "+slotId);                
                if (-1 == slotId)
                    return true;
                boolean meLock = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(slotId);

                Log.d(TAG, "isSimReady(), mITel.isSimInsert(slotId)=  "+mITel.isSimInsert(slotId));
                Log.d(TAG, "isSimReady(), mITel.isRadioOnGemini(slotId)=  "+mITel.isRadioOnGemini(slotId));
                Log.d(TAG, "isSimReady(), mITel.isFDNEnabledGemini(slotId)=  "+mITel.isFDNEnabledGemini(slotId));
                Log.d(TAG, "isSimReady(), meLock=  "+meLock);
                if (mITel.isRadioOnGemini(slotId)) {
                    return !mITel.hasIccCardGemini(slotId)
    				|| (mITel.isRadioOnGemini(slotId)
    				&& !mITel.isFDNEnabledGemini(slotId)
                                    && meLock
    				&& !ContactsUtils.isServiceRunning[slotId]);

                } else {
                return !mITel.isSimInsert(slotId)
				|| (mITel.isRadioOnGemini(slotId)
				&& !mITel.isFDNEnabledGemini(slotId)
                                && meLock
				&& !ContactsUtils.isServiceRunning[slotId]);
                }
			} else {
			if (mITel.isRadioOn()) {
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
	 * Responses to the minus button: R.id.sd_remove in speed_dial_list_item.xml 
	 */
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.sd_remove) {
			int position = -1;
			for (int i = 0; i < mListView.getCount(); ++i) {
				if (mListView.getChildAt(i) == v.getParent()) {
					position = i;
					break;
				}
			}
			Log.d(TAG, "onClick(),  before confirmRemovePosition(), position= " +position);
			confirmRemovePosition(position + mListView.getFirstVisiblePosition());
		}
	}

}
