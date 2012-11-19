package com.android.contacts;

import android.app.ListActivity;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.android.contacts.ContactsListActivity.ContactListItemCache; //import com.android.internal.telephony.ITelephony;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.app.AlertDialog;
import android.provider.ContactsContract.ContactCounts;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.text.TextUtils;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ContactsMultiChoiceActivity extends ListActivity implements
		View.OnClickListener, DialogInterface.OnClickListener {

	private static final String TAG = "ContactsMultiChoiceActivity";
	private static final String intentExtraName = "com.android.contacts.pickphoneandemail";
	private static final String itExtra_contacts = "com.android.contacts.pickcontacts";
	private static final String itExtra_email = "com.android.contacts.email";
        private static final String itExtra_fetch_contacts = "com.android.contacts.fetchcontacts";

	private QueryHandler mQueryHandler;
	private ContactItemListAdapter mAdapter;
	private ProgressDialog mSelectAllDialog = null;
	private ProgressDialog mQueryDialog = null;
	private Set<Integer> mSelectedPositionsSet = new HashSet<Integer>();
	boolean[] mSelectedPositions;
	int mSelectedCount;
	private AlertDialog mCompConfirmDialog;
	private TextView mEmptyText;
	private static final int QUERY_TOKEN = 1;

	private Button mDoneButton;
	private TextView mStatusText;
	private LinearLayout mBottomView;
	private LinearLayout mTopView;
	private String mDoneText;
	
	/* SelectAll's Req begin */
    private LinearLayout mSelectAllView;
    private TextView mSelectAllText;
    private CheckBox mSelectAllBox;
    /* SelectAll's Req end */
	
	private boolean mFirstEntry = true;

	public static final Uri PICK_PHONE_EMAIL_URI = Uri
			.parse("content://com.android.contacts/data/phone_email");

	private static final Uri PICK_CONTACTS_URI = buildSectionIndexerUri(Contacts.CONTENT_URI);

	private static final Uri PICK_EMAIL_URI = buildSectionIndexerUri(Email.CONTENT_URI);

	static final String[] PHONE_EMAIL_PROJECTION = new String[] { Phone._ID, // 0
			Phone.TYPE, // 1
			Phone.LABEL, // 2
			Phone.NUMBER, // 3
			Phone.DISPLAY_NAME, // 4
			Phone.CONTACT_ID, // 5
			Phone.MIMETYPE, // 6
	};

	static final String[] CONTACTS_PROJECTION = new String[] { Contacts._ID, // 0
			Contacts.DISPLAY_NAME_PRIMARY, // 1
			Contacts.DISPLAY_NAME_ALTERNATIVE, // 2
			Contacts.SORT_KEY_PRIMARY, // 3
			Contacts.DISPLAY_NAME, // 4
	};

        final String[] sLookupProjection = new String[] {
                        Contacts.LOOKUP_KEY
        };    

//	static final String[] EMAIL_PROJECTION = new String[] { Phone._ID, // 0
//			Phone.TYPE, // 1
//			Phone.LABEL, // 2
//			Phone.NUMBER, // 3
//			Phone.DISPLAY_NAME, // 4
//			Phone.CONTACT_ID, // 5
//	};

	static final int ID_COLUMN_INDEX = 0;
	static final int PHONE_TYPE_COLUMN_INDEX = 1;
	static final int PHONE_LABEL_COLUMN_INDEX = 2;
	static final int PHONE_NUMBER_COLUMN_INDEX = 3;
	static final int DISPLAY_NAME_COLUMN_INDEX = 4;
	static final int PHONE_CONTACT_ID_COLUMN_INDEX = 5;
	static final int PHONE_CONTACT_MIMETYPE_COLUMN_INDEX = 6;

	private static final String CLAUSE_ONLY_VISIBLE = Contacts.IN_VISIBLE_GROUP
			+ "=1";

	private static final int ID_MARK_ALL = 1;
	private static final int ID_UNMARK_ALL = 2;
	private static final int ID_DONE = 3;
	private static final int ID_CANCEL = 4;

	private static final int REQ_TYPE_PHONE_EMAIL = 0;
	private static final int REQ_TYPE_CONTACTS = 1;
	private static final int REQ_TYPE_EMAIL = 2;
        private static final int REQ_TYPE_FETCH_CONTACTS = 3;

	private int mReqType = REQ_TYPE_PHONE_EMAIL;

	private int mCountRestict = 0;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.d(TAG, "onCreate");

		final Intent intent = getIntent();
		mCountRestict = intent.getIntExtra("pick_count", 0);
		if (0 == mCountRestict) {
			setResult(RESULT_CANCELED, null);
			finish();
			return;
		}

		mReqType = intent.getIntExtra("request_type", 0);

		//mReqType = REQ_TYPE_CONTACTS;
		//mReqType = REQ_TYPE_PHONE_EMAIL;
		//mReqType = REQ_TYPE_EMAIL;

		Log.d(TAG, "OnCreate req type = " + mReqType);

		// layout
		setContentView(R.layout.contacts_list_multichoice);

		// top view
		mTopView = (LinearLayout) findViewById(R.id.topview_layout);
		if (null == mTopView)
			return;

		mTopView.setVisibility(View.GONE);

		mStatusText = (TextView) findViewById(R.id.statusText);
		if (null == mStatusText)
			return;
		if (0 < mCountRestict) {
			// mStatusText.setText(mCountRestict +
			// getResources().getString(R.string.status));
			mStatusText.setText(getResources().getString(R.string.status,
					mCountRestict));
		} else if (-1 == mCountRestict) {
			mStatusText.setText(getResources().getString(R.string.status_default));
		}
		
		/* SelectAll's Req begin */
        mSelectAllView = (LinearLayout) findViewById(R.id.selectAll_layout);
        mSelectAllText = (TextView) findViewById(R.id.selectAll_text);
        mSelectAllBox = (CheckBox) findViewById(R.id.selectAll_box);
        if (mSelectAllView != null) mSelectAllView.setOnClickListener(this);
        /* SelectAll's Req end */

		// bottom biew
		mBottomView = (LinearLayout) findViewById(R.id.done_layout);
		if (null == mBottomView)
			return;

		mBottomView.setVisibility(View.GONE);

		mDoneButton = (Button) findViewById(R.id.done_button);
		if (null == mDoneButton)
			return;

		mDoneText = getResources().getString(R.string.done);
		if (null == mDoneText)
			return;

		mDoneButton.setText(mDoneText + "(" + 0 + ")");

		mDoneButton.setOnClickListener(this);

        mSelectedPositionsSet.clear();
		mSelectedPositions = null;

		setupListView();
		setEmptyText();

		mSelectedPositionsSet = new HashSet<Integer>();
		mQueryHandler = new QueryHandler(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mSelectAllDialog != null && mSelectAllDialog.isShowing()) {
			mSelectAllDialog.dismiss();
			mSelectAllDialog = null;
		}
		if (mCompConfirmDialog != null && mCompConfirmDialog.isShowing()) {
			mCompConfirmDialog.dismiss();
			mCompConfirmDialog = null;
		}
		if (mQueryDialog != null && mQueryDialog.isShowing()) {
			mQueryDialog.dismiss();
			mQueryDialog = null;
		}
		if (mEmptyText != null)
			mEmptyText.setVisibility(View.GONE);
		if (!mAdapter.isUpdateRegistered())
			mAdapter.registerUpdate();
		
		this.mAdapter.clearContentChangeFlag();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");

		if (!mAdapter.isUpdateRegistered())
			mAdapter.registerUpdate();

		if (this.mFirstEntry) {
			mQueryDialog = new ProgressDialog(this);
			mQueryDialog.setCancelable(false);
			mQueryDialog.setMessage(getResources().getString(
					R.string.please_wait));
			mQueryDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mQueryDialog.show();

			if (mReqType == REQ_TYPE_PHONE_EMAIL) {
				mQueryHandler.startQuery(QUERY_TOKEN, null,
						buildSectionIndexerUri(PICK_PHONE_EMAIL_URI),
						PHONE_EMAIL_PROJECTION, CLAUSE_ONLY_VISIBLE, null,
						Contacts.SORT_KEY_PRIMARY);
			}

			if ((mReqType == this.REQ_TYPE_CONTACTS) || (mReqType == this.REQ_TYPE_FETCH_CONTACTS)) {
				mQueryHandler.startQuery(QUERY_TOKEN, null,
						buildSectionIndexerUri(PICK_CONTACTS_URI),
						CONTACTS_PROJECTION, CLAUSE_ONLY_VISIBLE, null,
						Contacts.SORT_KEY_PRIMARY);
			}

			if (mReqType == REQ_TYPE_EMAIL) {
				mQueryHandler.startQuery(QUERY_TOKEN, null,
						buildSectionIndexerUri(PICK_EMAIL_URI),
						PHONE_EMAIL_PROJECTION, this.CLAUSE_ONLY_VISIBLE, null,
						Contacts.SORT_KEY_PRIMARY);
			}


			if (null != mDoneButton) {
				this.mDoneButton.setText(mDoneText + "(" + 0 + ")");
			}

			mFirstEntry = false;
		} else {
			if (null != mDoneButton) {
				mDoneButton.setText(mDoneText + "("
						+ mSelectedPositionsSet.size() + ")");
			}
			//restoreCheckState();
		}

/*		if (mAdapter.isContentChanged()) {
			mQueryDialog = new ProgressDialog(this);
			mQueryDialog.setCancelable(false);
			mQueryDialog.setMessage(getResources().getString(
					R.string.please_wait));
			mQueryDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mQueryDialog.show();
			mQueryHandler.startQuery(QUERY_TOKEN, null,
					buildSectionIndexerUri(PICK_PHONE_EMAIL_URI),
					PHONES_PROJECTION, CLAUSE_ONLY_VISIBLE, null,
					Contacts.SORT_KEY_PRIMARY);
			if (null != mDoneButton) {
				mDoneButton.setText(mDoneText + "(" + 0 + ")");
			}
			mSelectedPositionsSet.clear();
			mSelectedPositions = null;
		} else {
			if (null != mDoneButton) {
				mDoneButton.setText(mDoneText + "("
						+ mSelectedPositionsSet.size() + ")");
			}
			restoreCheckState();
		}*/
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
        mSelectedPositionsSet.clear();
		mSelectedPositions = null;
        Cursor cursor = mAdapter.getCursor();
        if (cursor != null) {
            Log.d(TAG, "onDestroy, close cursor");
            cursor.close();
        }

	}

	private static Uri buildSectionIndexerUri(Uri uri) {
		return uri.buildUpon().appendQueryParameter(
				ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, "true").build();
	}

	private void setupListView() {

		final ListView list = getListView();

		list.setFocusable(true);
		mAdapter = new ContactItemListAdapter(this);
		setListAdapter(mAdapter);
	}

	private void setEmptyText() {
		getListView().setEmptyView(findViewById(android.R.id.empty));
		mEmptyText = (TextView) findViewById(R.id.emptyText);
		if (null == mEmptyText) {
			this.finish();
			return;
		}
		mEmptyText.setText(R.string.noContacts);
		mEmptyText.setVisibility(View.GONE);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		if ((mCountRestict == mSelectedCount)
				&& (!mSelectedPositions[position])) {
			// Toast.makeText(this, R.string.sms_tips,
			// Toast.LENGTH_SHORT).show();
			new AlertDialog.Builder(this).setMessage(
					getResources().getString(R.string.sms_tips)).setTitle(
					android.R.string.dialog_alert_title).setIcon(
					android.R.drawable.ic_dialog_alert).setPositiveButton(
					android.R.string.yes, this)
			// .setNegativeButton(android.R.string.no, this)
					.show();
			// .setOnDismissListener(this);
			return;
		}

		if (mSelectedPositions[position]) {
			mSelectedPositions[position] = false;
			mSelectedPositionsSet.remove(position);
			mSelectedCount = (mSelectedCount < 1) ? 0 : (mSelectedCount - 1);
		} else {
			mSelectedPositions[position] = true;
			mSelectedPositionsSet.add(position);
			mSelectedCount = (mSelectedCount >= mSelectedPositions.length) ? mSelectedPositions.length
					: (mSelectedCount + 1);
		}
		int firstVisiblePosition = l.getFirstVisiblePosition();
		ContactListItemView itemView = (ContactListItemView) (l
				.getChildAt(position - firstVisiblePosition));
		itemView.getCheckBox().toggle();
		mDoneButton.setText(mDoneText + "(" + mSelectedCount + ")");
		
		/* SelectAll's Req begin */
        mSelectAllBox.setChecked(mSelectedCount == mSelectedPositions.length);
        mSelectAllText.setText(mSelectedCount == mSelectedPositions.length ? R.string.deselectall
                : R.string.selectall);
        /* SelectAll's Req begin */
	}

	// This is a method implemented for DialogInterface.OnClickListener.
	// It is only a warning, no action with this method.
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON1) {
			return;
		}
	}

	/** {@inheritDoc} */
	public void onClick(View v) {
	    
	    /* SelectAll's Req begin */
	    if (v.getId() == R.id.selectAll_layout) {
            if (mSelectAllDialog == null) {
                mSelectAllDialog = new ProgressDialog(this);
                mSelectAllDialog.setCancelable(false);
                mSelectAllDialog.setMessage(getString(
                        R.string.please_wait));
                mSelectAllDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                }
                mSelectAllDialog.show();
                mSelectAllBox.toggle();
                boolean isChecked = mSelectAllBox.isChecked();
                mSelectAllText.setText(isChecked ? R.string.deselectall : R.string.selectall);
                new Thread() {
                    @Override
                    public void run() {
                        for (int k = 0; k < mSelectedPositions.length; ++k) {
                            mSelectedPositions[k] = (mSelectAllBox.isChecked()) ? true : false;
                            if (mSelectAllBox.isChecked()) mSelectedPositionsSet.add(k);
                            else mSelectedPositionsSet.remove(k);
                        }
                        if (mSelectAllDialog != null && mSelectAllDialog.isShowing()) {
                        mSelectAllDialog.dismiss();
                        }

                    }
                }.start();
                updateCheckBoxes(isChecked);
                mSelectedCount = (isChecked) ? mSelectedPositions.length : 0;
                mDoneButton.setText(mDoneText + "(" + mSelectedCount + ")");
                return;
	    }
	    /* SelectAll's Req end */
	    
		// TODO Handle the action of choice done

		if (0 == mSelectedCount)
			finish();

		final Intent retIntent = new Intent();
		if (null == retIntent) {
			setResult(RESULT_CANCELED, null);
			finish();
			return;
		}
		Cursor cursor = null;
		String phoneID = null;

		int curArray = 0;
		long[] idArray = new long[mSelectedCount];
		if (null == idArray) {
			setResult(RESULT_CANCELED, null);
			finish();
			return;
		}
		for (Iterator<Integer> it = mSelectedPositionsSet.iterator(); it
				.hasNext()
				&& curArray < mSelectedCount; /* No increment */) {
			int position = it.next();
			cursor = (Cursor) getListAdapter().getItem(position);
			if (null == cursor) {
				Log.i(TAG, "Done cursor is null");
				// setResult(RESULT_CANCELED, null);
				// finish();
				continue;
			}
			phoneID = cursor.getString(ID_COLUMN_INDEX);
			if (null == phoneID) {
				phoneID = "";
				Log.i(TAG, "Done strID is null");
				continue;
			}
			if (TextUtils.isEmpty(phoneID)) {
				Log.i(TAG, "Done strID is empty");
				continue;
			}
			Log.i(TAG, "Done single item is" + phoneID);
			idArray[curArray++] = Long.parseLong(phoneID);
			cursor = null;
			phoneID = null;
		}

		// TODO delete Test code begin
		curArray = 0;
		for (; curArray < mSelectedCount; curArray++) {
			Log.i(TAG, "Done item " + curArray + " is " + idArray[curArray]);
		}
		// TODO delete Test code end

		if (REQ_TYPE_PHONE_EMAIL == mReqType) {
			retIntent.putExtra(intentExtraName, idArray);
		} else if (REQ_TYPE_CONTACTS == mReqType) {
			retIntent.putExtra(itExtra_contacts, idArray);
		} else if (REQ_TYPE_EMAIL == mReqType) {
			retIntent.putExtra(itExtra_email, idArray);
        	} else if (REQ_TYPE_FETCH_CONTACTS== mReqType) {
		        Uri uri = null;
		        if (mSelectedCount == 1) {
                            uri = getLookupUriForEmail("Single_Contact", idArray);
                            //retIntent.putExtra("singleContact", 1);   //single Contact
                        } else {
                            uri = getLookupUriForEmail("Multi_Contact", idArray);
                            //retIntent.putExtra("singleContact", 0);  //multi Contacts
                        }
                        retIntent.putExtra(itExtra_fetch_contacts, uri);
		}

		setResult(RESULT_OK, retIntent);
		finish();
	}


	private Uri getLookupUriForEmail(String type, long[] contactsIds) {
		// MTK
		Cursor cursor = null;
		Uri uri = null;
		Log.i(TAG, "type is " + type);
		if (type == "Single_Contact") {
			Log.i(TAG, "In single contact");
			uri = Uri.withAppendedPath(Contacts.CONTENT_URI, Long
					.toString(contactsIds[0]));
			cursor = getContentResolver().query(uri, sLookupProjection,
					CLAUSE_ONLY_VISIBLE, null, null);

			Log.i(TAG, "cursor is " + cursor);
			if (cursor != null && cursor.moveToNext()) {

				Log.i(TAG, "Single_Contact  cursor.getCount() is "
						+ cursor.getCount());

				uri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, cursor
						.getString(0));
				Log.i(TAG, "Single_Contact  uri is " + uri
						+ " \ncursor.getString(0) is " + cursor.getString(0));
			}
		} else if (type == "Multi_Contact") {
			StringBuilder sb = new StringBuilder("");
			for (long contactId : contactsIds) {
				if (contactId == contactsIds[contactsIds.length - 1]) {
					sb.append(contactId);
				} else {
					sb.append(contactId + ",");
				}
			}
			String selection = Contacts._ID + " in (" + sb.toString() + ")";
			Log.d(TAG, "Multi_Contact, selection=" + selection);
			cursor = getContentResolver().query(Contacts.CONTENT_URI,
					sLookupProjection, selection, null, null);
			if (cursor != null) {
				Log.i(TAG, "Multi_Contact  cursor.getCount() is "
						+ cursor.getCount());
			}
			if (!cursor.moveToFirst()) {
				// Toast.makeText(this, R.string.share_error,
				// Toast.LENGTH_SHORT).show();
				return null;
			}

			StringBuilder uriListBuilder = new StringBuilder();
			int index = 0;
			for (; !cursor.isAfterLast(); cursor.moveToNext()) {
				if (index != 0)
					uriListBuilder.append(':');
				uriListBuilder.append(cursor.getString(0));
				index++;
			}
			uri = Uri.withAppendedPath(Contacts.CONTENT_MULTI_VCARD_URI, Uri
					.encode(uriListBuilder.toString()));
			Log.i(TAG, "Multi_Contact  uri is " + uri);
		}
		if (cursor != null)
			cursor.close();

		return uri;

	}
        
       

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {
	 * super.onCreateOptionsMenu(menu); //menu.add(0, ID_MARK_ALL, 0,
	 * R.string.select_all); //menu.add(0, ID_UNMARK_ALL, 0,
	 * R.string.unselect_all); menu.add(0, ID_CANCEL, 0, R.string.cancel);
	 * menu.add(0, ID_DONE, 0, R.string.done); return true; }
	 * 
	 * @Override public boolean onPrepareOptionsMenu(Menu menu) { if (mAdapter
	 * == null || mAdapter.getCount() <= 0) return false;
	 * 
	 * //menu.findItem(ID_MARK_ALL).setVisible( // mAdapter.getCount() !=
	 * mSelectedCount); //menu.findItem(ID_UNMARK_ALL).setVisible( //
	 * mAdapter.getCount() == mSelectedCount); Log.i(TAG, "mSelectedCount = " +
	 * mSelectedCount); menu.findItem(ID_DONE).setEnabled(mSelectedCount > 0);
	 * 
	 * return true; }
	 */

	private void updateCheckBoxes(boolean markAll) {
		ListView lv = getListView();
		if (lv == null)
			return;
		int firstVisiblePosition = lv.getFirstVisiblePosition();
		int lastVisiblePosition = lv.getLastVisiblePosition();
		for (int k = firstVisiblePosition; k <= lastVisiblePosition; ++k) {
			ContactListItemView itemView = (ContactListItemView) (getListView()
					.getChildAt(k - firstVisiblePosition));
			itemView.getCheckBox().setChecked(markAll);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mSelectAllDialog != null) { // selecting all
			return false;
		}
		final int itemId = item.getItemId();
		switch (itemId) {
		case ID_MARK_ALL:
		case ID_UNMARK_ALL: {
			mSelectAllDialog = new ProgressDialog(this);
			mSelectAllDialog.setCancelable(false);
			mSelectAllDialog.setMessage(getResources().getString(
					R.string.please_wait));
			mSelectAllDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mSelectAllDialog.show();
			new Thread() {
				@Override
				public void run() {
					for (int k = 0; k < mSelectedPositions.length; ++k) {
						mSelectedPositions[k] = (itemId == ID_MARK_ALL) ? true
								: false;
						if (itemId == ID_MARK_ALL)
							mSelectedPositionsSet.add(k);
						else
							mSelectedPositionsSet.remove(k);
					}
					mSelectAllDialog.dismiss();
					mSelectAllDialog = null;
				}
			}.start();
			updateCheckBoxes(itemId == ID_MARK_ALL);
			mSelectedCount = (itemId == ID_MARK_ALL) ? mSelectedPositions.length
					: 0;
			return true;
		}
		case ID_DONE: {
			// TODO Handle the action of choice done
			final Intent retIntent = new Intent();
			if (null == retIntent) {
				setResult(RESULT_CANCELED, null);
				finish();
				return false;
			}
			Cursor cursor = null;
			String idItem = null;

			int curArray = 0;
			long[] idArray = new long[mSelectedCount];
			if (null == idArray) {
				setResult(RESULT_CANCELED, null);
				finish();
				return false;
			}
			for (Iterator<Integer> it = mSelectedPositionsSet.iterator(); it
					.hasNext()
					&& curArray < mSelectedCount; /* No increment */) {
				int position = it.next();
				cursor = (Cursor) getListAdapter().getItem(position);
				if (null == cursor) {
					Log.i(TAG, "Done cursor is null");
					// setResult(RESULT_CANCELED, null);
					// finish();
					continue;
				}
				idItem = cursor.getString(ID_COLUMN_INDEX);
				if (null == idItem) {
					idItem = "";
					Log.i(TAG, "Done strID is null");
					continue;
				}
				if (TextUtils.isEmpty(idItem)) {
					Log.i(TAG, "Done strID is empty");
					continue;
				}
				Log.i(TAG, "Done single item is" + idItem);
				idArray[curArray++] = Long.parseLong(idItem);
				cursor = null;
				idItem = null;
			}

			// TODO delete Test code begin
			curArray = 0;
			for (; curArray < mSelectedCount; curArray++) {
				Log
						.i(TAG, "Done item " + curArray + " is "
								+ idArray[curArray]);
			}
			// TODO delete Test code end

			retIntent.putExtra(intentExtraName, idArray);
			setResult(RESULT_OK, retIntent);
			finish();
			return true;
		}
		case ID_CANCEL: {
			// TODO Handle the action of cancel
			finish();
			return true;
		}
		}
		return false;
	}

	private class QueryHandler extends AsyncQueryHandler {
		protected final WeakReference<ContactsMultiChoiceActivity> mActivity;

		public QueryHandler(Context context) {
			super(context.getContentResolver());
			mActivity = new WeakReference<ContactsMultiChoiceActivity>(
					(ContactsMultiChoiceActivity) context);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			Log.i(TAG, "cursor is " + cursor);
			Log.i(TAG, "cursor.getcount is " + cursor.getCount());
			// final ITelephony iTel =
			// ITelephony.Stub.asInterface(ServiceManager
			// .getService(Context.TELEPHONY_SERVICE));
			final ContactsMultiChoiceActivity activity = mActivity.get();

			if (mQueryDialog != null && mQueryDialog.isShowing()) {
                Handler h = new Handler();
                h.postDelayed(new Runnable(){
                    public void run(){
                        mQueryDialog.dismiss();                 
			        	mQueryDialog = null;
                    }
                    },150);
				
			}
            if (activity != null && !activity.isFinishing()) {

			if (!activity.mAdapter.isUpdateRegistered()) {
				if (cursor != null)
					cursor.close();
				return;
			}
			activity.mAdapter.changeCursor(cursor);
			activity.mSelectedCount = 0;
			activity.mSelectedPositions = new boolean[activity.mAdapter
					.getCount()];
			activity.mEmptyText.setVisibility(View.VISIBLE);
			activity.mAdapter.unregisterUpdate();

			// update layout
			if (0 == activity.mAdapter.getCount()) {
				activity.mTopView.setVisibility(View.GONE);
				activity.mBottomView.setVisibility(View.GONE);
				/* SelectAll's Req begin */
				activity.mSelectAllView.setVisibility(View.GONE);
				/* SelectAll's Req end */
			} else {
				activity.mTopView.setVisibility(View.VISIBLE);
				activity.mBottomView.setVisibility(View.VISIBLE);
				/* SelectAll's Req begin */
                if (mReqType == REQ_TYPE_PHONE_EMAIL) {
                    activity.mSelectAllView.setVisibility(View.VISIBLE);
                    activity.mSelectAllBox.setChecked(false);
                    activity.mSelectAllText.setText(R.string.selectall);
                }
				/* SelectAll's Req end */
			}

			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) activity
					.getListView().getLayoutParams();
			params.height = 0;
			params.addRule(RelativeLayout.ABOVE, activity.mBottomView.getId());
			/* SelectAll's Req begin */
			params.addRule(RelativeLayout.BELOW, activity.mSelectAllView.getId());
			//params.addRule(RelativeLayout.BELOW, activity.mTopView.getId());
			/* SelectAll's Req end */
			activity.getListView().setLayoutParams(params);
            } else {
                Log.d(TAG, "onQueryComplete(), activity finish, close cursor ");
                if (cursor != null) {
                    cursor.close();
                }
            }
		}
	}

	private final class ContactItemListAdapter extends CursorAdapter implements
			SectionIndexer, OnScrollListener,
			PinnedHeaderListView.PinnedHeaderAdapter {
		CharSequence mUnknownNameText;
		private boolean mUpdateRegistered = true;
		private boolean mContentChanged = true;

		public ContactItemListAdapter(Context context) {
			super(context, null, false);
			mUnknownNameText = context.getText(android.R.string.unknownName);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			final ContactListItemView itemview = (ContactListItemView) view;
			final ContactListItemCache cache = (ContactListItemCache) view
					.getTag();
			String name = cursor.getString(DISPLAY_NAME_COLUMN_INDEX);
			final String label = cursor.getString(PHONE_LABEL_COLUMN_INDEX);
			final int type = cursor.getInt(PHONE_TYPE_COLUMN_INDEX);
			// Log.i(TAG, "name is " + name + " label is " + label +
			// " number is "
			// + number + " type is " + type);
			TextView nameView = itemview.getNameTextView();

            nameView.setEllipsize(TextUtils.TruncateAt.END);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int selectBoxWidth = itemview.getCheckBox().getMeasuredWidth();
            int paddingRight = context.getResources().getDimensionPixelOffset(
                    R.dimen.list_item_padding_right) + 5; //adjust the margin to available for portrait 
            if (0 == selectBoxWidth) {
                selectBoxWidth = 58; //If we could not retrive the check box size, we set the default size
            }

            nameView.setWidth(dm.widthPixels - paddingRight - selectBoxWidth);
            nameView.setText(TextUtils.isEmpty(name) ? mUnknownNameText : name);
            
            // xuxin_beg_68772_2011_08_24
			if (REQ_TYPE_PHONE_EMAIL == mReqType || REQ_TYPE_EMAIL == mReqType) 
			{
				String sMiniType = cursor.getString(PHONE_CONTACT_MIMETYPE_COLUMN_INDEX);
				if(sMiniType.equals(Email.CONTENT_ITEM_TYPE))
				{
					itemview.setLabel(Email.getTypeLabel(context.getResources(), type, label));
				}
				else
				{ 
				    itemview.setLabel(Phone.getTypeLabel(context.getResources(), type, label));
				}				
			}
			// xuxin_end
			cursor.copyStringToBuffer(PHONE_NUMBER_COLUMN_INDEX,
					cache.dataBuffer);
			int size = cache.dataBuffer.sizeCopied;
			Log.i(TAG, "cache.dataBuffer is " + cache.dataBuffer + " size is "
					+ size);
			if (REQ_TYPE_CONTACTS != mReqType) {
				itemview.setData(cache.dataBuffer.data, size);
			}
			CheckBox cb = itemview.getCheckBox();
			cb.setChecked(mSelectedPositions[cursor.getPosition()]);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			final ContactListItemView view = new ContactListItemView(context,
					null);
			// view.setOnCallButtonClickListener(ContactsMultiChoiceActivity.this);
			ContactListItemCache cache = new ContactListItemCache();
			// view.setCheckBox();
			view.setTag(cache);
			return view;
		}

		public int getPositionForSection(int section) {
			// TODO Auto-generated method stub
			return 0;
		}

		public int getSectionForPosition(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		public Object[] getSections() {
			// TODO Auto-generated method stub
			return null;
		}

		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub

		}

		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub

		}

		public void configurePinnedHeader(View header, int position, int alpha) {
			// TODO Auto-generated method stub

		}

		public int getPinnedHeaderState(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		public void registerUpdate() {
			Log.i(TAG, "registerUpdate");
			mCursor.registerContentObserver(mChangeObserver);
			mCursor.registerDataSetObserver(mDataSetObserver);
			mUpdateRegistered = true;
		}

		public void unregisterUpdate() {
			Log.i(TAG, "unregisterUpdate");
			mCursor.unregisterContentObserver(mChangeObserver);
			mCursor.unregisterDataSetObserver(mDataSetObserver);
			mUpdateRegistered = false;
		}

		public boolean isUpdateRegistered() {
			return mUpdateRegistered;
		}
		
		boolean isContentChanged() {
			return mContentChanged;
		}
		
		void clearContentChangeFlag() {
			this.mContentChanged = false;
		}
		
        /**
         * Callback on the UI thread when the content observer on the backing cursor fires.
         * Instead of calling requery we need to do an async query so that the requery doesn't
         * block the UI thread for a long time.
         */
        @Override
        protected void onContentChanged() {
        	Log.d(TAG, "onContentChanged");
        	mContentChanged = true;
        }

        public Cursor getCursor(){
            return mCursor;
        }        
	}
	
	private void restoreCheckState() {
		for (Integer i : mSelectedPositionsSet) {
			Log.d(TAG, "restoreCheckState " + i);
/*			ContactListItemView itemView = (ContactListItemView) (this
					.getListView().getChildAt(i));
			itemView.getCheckBox().toggle();*/
			//int firstVisiblePosition = this
			//.getListView().getFirstVisiblePosition();
			ContactListItemView itemView = (ContactListItemView) (this
					.getListView()
					.getChildAt(i));
			itemView.getCheckBox().setChecked(true);
		}
	}

}
