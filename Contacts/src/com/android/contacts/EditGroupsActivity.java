package com.android.contacts;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.android.contacts.ContactsUtils.ArrayData;
import com.android.contacts.model.ContactsSource;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.Sources;
import com.android.contacts.ui.EditContactActivity;
import com.android.contacts.ui.EditSimContactActivity;
import com.android.contacts.util.AccountSelectionUtil;
import com.android.contacts.util.Constants;
import com.android.contacts.util.WeakAsyncTask;
import com.android.internal.telephony.ITelephony;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Contacts.People;
import android.provider.ContactsContract.ContactCounts;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Telephony.MmsSms;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.QuickContactBadge;
import android.widget.ScrollView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import com.mediatek.featureoption.FeatureOption;

public class EditGroupsActivity extends Activity implements Comparator<Entry<Long, ContactsUtils.ArrayData>>{
    private static final String TAG = "EditGroupsActivity";

    public static final int REQUEST_CONTACTS = 1;
    public static final int REQUEST_FINISH = 2;
    
    
    private GroupsListAdapter mAdapter;
    private QueryHandler mQueryHandler;
    
    private static final int MENU_ADD_MEM = 0;
    private static final int MENU_REMOVE_MEM = 1;
    private static final int MENU_MOV_MEM = 2;
    private static final int MENU_EDIT_MENU = 3;
    private static final int MENU_GROUPS_SMS = 4;
    private static final int MENU_GROUPS_EMAIL = 5;
    
    private static final int MENU_OWNER_EDIT = 6;
    private static final int MENU_OWNER_DELETE = 7;
    private static final int MENU_ITEM_VIEW_CONTACT = 8;
    private static final int MENU_ITEM_CALL = 9;
    private static final int MENU_ITEM_SEND_SMS = 10;
    private static final int MENU_ITEM_TOGGLE_STAR = 11;
    private static final int MENU_ITEM_EDIT = 12;
    private static final int MENU_ITEM_DELETE = 13;
    private static final int MENU_ITEM_SHARE = 14;
    private static final int MENU_ITEM_SPEED_DIAL = 15;
    
    public static final String Groups_MODE_KEY = "mode";
    public static final int MODE_VIEW_GROUPS = 1;
    public static final int MODE_NEW_GROUPS = 2;
    public static final int MODE_EDIT_GROUPS = 3;
    private int mMode;
    
    private TextView mTitle;
    private TextView mCount;
    
    private long mGroupId;
    private String mTitleText;
    
    private ContactPhotoLoader mPhotoLoader;
    private Cursor mCursor;
    
    private ArrayList<Entry<Long, ContactsUtils.ArrayData>> mArray = new ArrayList<Entry<Long, ContactsUtils.ArrayData>>();
    private ArrayList<Entry<Long, ContactsUtils.ArrayData>> mDelArray = new ArrayList<Entry<Long, ContactsUtils.ArrayData>>();
    private ArrayList<Entry<Long, ContactsUtils.ArrayData>> mAddArray = new ArrayList<Entry<Long, ContactsUtils.ArrayData>>();
    
  //refresh
    private boolean mForeground = true;
    private boolean mForceQuery = false;
    
    private boolean mEditContact = false;
    
    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
        Contacts._ID,                       // 0
        Contacts.DISPLAY_NAME_PRIMARY,      // 1
        Contacts.DISPLAY_NAME_ALTERNATIVE,  // 2
        Contacts.SORT_KEY_PRIMARY,          // 3
        Contacts.STARRED,                   // 4
        Contacts.TIMES_CONTACTED,           // 5
        Contacts.CONTACT_PRESENCE,          // 6
        Contacts.PHOTO_ID,                  // 7
        Contacts.LOOKUP_KEY,                // 8
        Contacts.PHONETIC_NAME,             // 9
        Contacts.HAS_PHONE_NUMBER,          // 10
        Contacts.INDICATE_PHONE_SIM,        // 11 
    };
    
    static final String[] RAW_CONTACTS_PROJECTION = new String[] {
        RawContacts._ID, //0
        RawContacts.CONTACT_ID, //1
        RawContacts.ACCOUNT_TYPE, //2
    };

    final String[] sLookupProjection = new String[] {
            Contacts.LOOKUP_KEY
    };
    
    private int mPosition;
    private Uri mSelectedContactUri;
    private TextView empty;   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_group);
        Intent intent = getIntent();
        
        mMode = intent.getIntExtra(Groups_MODE_KEY, MODE_NEW_GROUPS);
        mGroupId = intent.getLongExtra(ContactsGroupsActivity.GROUP_ID_KEY, -1);
        mTitleText = intent.getStringExtra(ContactsGroupsActivity.GROUP_TITLE_KEY);
        Log.i(TAG, "mMode : mGroupsId : mTitle = " + mMode + ":" + mGroupId + ":" + mTitleText);
        ImageView icon = (ImageView)findViewById(R.id.icon);
        
        if (mMode == MODE_VIEW_GROUPS) {
            mTitle = (TextView)findViewById(R.id.tv_title);
            mCount = (TextView)findViewById(R.id.count);
            icon.setImageResource(ContactsUtils.getGroupsIcon(this, mTitleText));
            ViewGroup groupTitleView = (ViewGroup)findViewById(R.id.group_title_view);
            groupTitleView.setVisibility(View.VISIBLE);
        } else {
            mTitle = (EditText)findViewById(R.id.et_title);
            icon.setVisibility(View.GONE);
            findViewById(R.id.edit_view).setVisibility(View.VISIBLE);
            mTitle.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(mTitleText)) {
            mTitle.setText(ContactsUtils.getGroupsName(this, mTitleText));
        }
        
        Button add_mem_btn = (Button)findViewById(R.id.add_group_mem);
        add_mem_btn.setOnClickListener(mViewClickListener);
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture);
        ListView list = (ListView)findViewById(R.id.contact);
        ScrollView emptyView = (ScrollView)findViewById(android.R.id.empty);
        empty = (TextView)findViewById(R.id.emptyText);
//        group.addView(emptyView);
        empty.setText(R.string.no_members_in_group);
        empty.setVisibility(View.GONE);
        list.setEmptyView(emptyView);
        mAdapter = new GroupsListAdapter(this, R.layout.groups_add_item, 0, mArray);
        list.setAdapter(mAdapter);
        if (mMode == MODE_VIEW_GROUPS || mMode == MODE_EDIT_GROUPS) {
            startQuery();
        }
        list.setOnItemClickListener(mOnClickListener);
        list.setOnCreateContextMenuListener(this);
        ViewGroup savePanel = (ViewGroup)findViewById(R.id.save_panel);
        if (mMode == MODE_VIEW_GROUPS) {
            savePanel.setVisibility(View.GONE);
        } else {
            Button done = (Button) findViewById(R.id.save);
            Button cancel = (Button) findViewById(R.id.cancel);
            done.setOnClickListener(mViewClickListener);
            cancel.setOnClickListener(mViewClickListener);
        }
    }

    @Override
    protected void onPause() {
        mForeground = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        mPhotoLoader.clear();
        mPhotoLoader.resume();
        if ((!mForeground && mForceQuery) || mEditContact) {
            startQuery();
            if(mEditContact == true)mEditContact = false;
        }
        mForeground = true;
        mForceQuery = false;
        super.onResume();
    }
    
    @Override
    protected void onDestroy() {
        mPhotoLoader.stop();
        if (mCursor != null && mMode != MODE_EDIT_GROUPS) {
            mCursor.unregisterContentObserver(mObserver);
        }
        mCursor = null;
        super.onDestroy();
    }
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
            case R.id.dialog_delete_contact_confirmation: {
                AlertDialog mDelConfm1AlertDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.deleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                new DeleteClickListener()).create();
                return mDelConfm1AlertDialog;
            }
        }
        return super.onCreateDialog(id, bundle);
    }
    
    private class DeleteClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
			 if (mSelectedContactUri != null) {
                getContentResolver().delete(mSelectedContactUri, null, null);
                startQuery();
            }
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case MENU_ADD_MEM: {
            	if(true == ContactsUtils.mSavingGroup){
            		Toast.makeText(this, R.string.group_is_saving, Toast.LENGTH_LONG).show();
            		return true;
            	}
                Intent intent = new Intent(EditGroupsActivity.this, ContactsGroupsMultiOpt.class);
                intent.putExtra(Groups_MODE_KEY, ContactsGroupsMultiOpt.MODE_ADD_CONTACT);
                if (mTitleText != null) {
                    intent.putExtra(ContactsGroupsActivity.GROUP_TITLE_KEY, mTitleText);
                }
                startActivityForResult(intent, REQUEST_CONTACTS);
                break;
            }
            case MENU_REMOVE_MEM: {
                Intent intent = new Intent(this, ContactsGroupsMultiOpt.class);
                intent.putExtra(Groups_MODE_KEY, ContactsGroupsMultiOpt.MODE_DEL_CONTACT);
                if (mTitleText != null) {
                    intent.putExtra(ContactsGroupsActivity.GROUP_TITLE_KEY, mTitleText);
                    intent.putExtra(ContactsGroupsActivity.GROUP_ID_KEY, mGroupId);
                }
                mForeground = false;
                mForceQuery = true;
                startActivity(intent);
                break;
            }
            case MENU_MOV_MEM: {
                Intent intent = new Intent(this, ContactsGroupsMultiOpt.class);
                intent.putExtra(Groups_MODE_KEY, ContactsGroupsMultiOpt.MODE_MOV_CONTACT);
                if (mTitleText != null) {
                    intent.putExtra(ContactsGroupsActivity.GROUP_TITLE_KEY, mTitleText);
                    intent.putExtra(ContactsGroupsActivity.GROUP_ID_KEY, mGroupId);
                }
                startActivityForResult(intent,REQUEST_FINISH);
                break;
            }
            case MENU_EDIT_MENU: {
                Intent intent = new Intent(this, EditGroupsActivity.class);
                intent.putExtra(Groups_MODE_KEY, MODE_EDIT_GROUPS);
                intent.putExtra(ContactsGroupsActivity.GROUP_TITLE_KEY, mTitleText);
                intent.putExtra(ContactsGroupsActivity.GROUP_ID_KEY, mGroupId);
                startActivityForResult(intent,REQUEST_FINISH);
                break;
            }
            case MENU_GROUPS_SMS: {
                new SendGroupSmsTask(this).execute(mTitleText);
                break;
            }
            case MENU_GROUPS_EMAIL: {
                new SendGroupEmailTask(this).execute(mTitleText);
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (mMode != MODE_VIEW_GROUPS) {
            return true;
        }
        menu.add(0, MENU_ADD_MEM, 0, R.string.add_group_members).setIcon(R.drawable.contact_group_addcontact);
        if (mArray.size() > 0) {
            menu.add(0, MENU_REMOVE_MEM, 0, R.string.remove_group_members).setIcon(R.drawable.contact_group_removemember_menu);
            menu.add(0, MENU_MOV_MEM, 0, R.string.move_group_members).setIcon(R.drawable.contact_group_movemember_menu);
            menu.add(0, MENU_GROUPS_SMS, 0, R.string.send_group_sms).setIcon(R.drawable.contact_group_groupsms_menu);
            menu.add(0, MENU_GROUPS_EMAIL, 0, R.string.send_group_email).setIcon(R.drawable.contact_group_email_menu);
        }
        if (mGroupId > ContactsGroupsActivity.DEFAULT_GROUPS_COUNT) {
            menu.add(0, MENU_EDIT_MENU, 0, R.string.edit_group).setIcon(android.R.drawable.ic_menu_edit);
        }
        return true;
    }
    
    private Uri getContactUri(int position) {
        if (position == ListView.INVALID_POSITION) {
            throw new IllegalArgumentException("Position not in list bounds");
        }

        Entry<Long, ContactsUtils.ArrayData> item = mAdapter.getItem(position);
        ContactsUtils.ArrayData data = item.getValue();
        long contactId = item.getKey();
        
        if (data == null) {
            return null;
        }
        
        final String lookupKey = data.mLookupKey;
        if (lookupKey == null) {
        	return Contacts.getLookupUri(contactId, lookupKey);
        } else {
			Uri lookupUri = Contacts.getLookupUri(contactId, lookupKey);
			return Contacts.lookupContact(getContentResolver(), lookupUri);
        }
    }
    private boolean callContact(ContactsUtils.ArrayData data) {
		return callOrSmsContact(data, false /* call */);
    }

    private boolean smsContact(ContactsUtils.ArrayData data) {
		return callOrSmsContact(data, true /* sms */);
    }
    
    private boolean callOrSmsContact(ContactsUtils.ArrayData data, boolean sendSms) {
        if (data == null) {
            return false;
        }
        if(mMode != MODE_VIEW_GROUPS)return false;
        boolean hasPhone = data.hasPhoneNumber != 0;
        if (!hasPhone) {
            // There is no phone number.
            return false;
        }

        String phone = null;
        Cursor phonesCursor = null;
        phonesCursor = queryPhoneNumbers(data.mId);
        if (phonesCursor == null || phonesCursor.getCount() == 0) {
                    // No valid number
            if (phonesCursor != null) {
            	phonesCursor.close();
          	}
         	return false;
    	} else if (phonesCursor.getCount() == 1) {
    		// only one number, call it.
           	phone = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.NUMBER));
      	} else {
           	phonesCursor.moveToPosition(-1);
            while (phonesCursor.moveToNext()) {
            	if (phonesCursor.getInt(phonesCursor.
                	getColumnIndex(Phone.IS_SUPER_PRIMARY)) != 0) {
            		// Found super primary, call it.
            		phone = phonesCursor.
            		getString(phonesCursor.getColumnIndex(Phone.NUMBER));
            		break;
            	}
            }
      	}

      	if (phone == null) {
        	// Display dialog to choose a number to call.
           	PhoneDisambigDialog phoneDialog = new PhoneDisambigDialog(
            	this, phonesCursor, sendSms, StickyTabs.getTab(getIntent()));
         	phoneDialog.show();
//          	mPhoneDisambigDialog = phoneDialog;
      	} else {
         	if (sendSms) {
         		ContactsUtils.initiateSms(this, phone);
            } else {
            	if (FeatureOption.MTK_GEMINI_SUPPORT) {                         
            		if (TextUtils.isEmpty(phone)){
            			return false;
		            } else {
                        //ContactsUtils.initiateCallWithSim(this, phone);
                        StickyTabs.saveTab(this, getIntent());
                        ContactsUtils.enterDialer(this, phone);                          
		            } 
		        }else {
		        	if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
		        		StickyTabs.saveTab(this, getIntent());
                        ContactsUtils.enterDialer(this, phone);                     
					} else {
                    	StickyTabs.saveTab(this, getIntent());
                        ContactsUtils.initiateCall(this, phone);
					}
                }
            }
        }
        return true;
    }
    
    private Cursor queryPhoneNumbers(long contactId) {
        Uri baseUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        Uri dataUri = Uri.withAppendedPath(baseUri, Contacts.Data.CONTENT_DIRECTORY);

        Cursor c = getContentResolver().query(dataUri,
                new String[] {Phone._ID, Phone.NUMBER, Phone.IS_SUPER_PRIMARY,
                        RawContacts.ACCOUNT_TYPE, Phone.TYPE, Phone.LABEL, Data.DATA15},    
                Data.MIMETYPE + "=?", new String[] {Phone.CONTENT_ITEM_TYPE}, null);
        if (c != null && c.moveToFirst()) {
                return c;
            }
            if(c != null)c.close();
        return null;
    }
    protected void doContactDelete(Uri contactUri) {
        int mReadOnlySourcesCnt = 0;
        int mWritableSourcesCnt = 0;
        ArrayList<Long> mWritableRawContactIds = new ArrayList<Long>();

        Sources sources = Sources.getInstance(this);
        Long contactId = ContentUris.parseId(contactUri);
        if(contactId == null)contactId = (long)0;
        
        Cursor c = getContentResolver().query(RawContacts.CONTENT_URI, RAW_CONTACTS_PROJECTION,
                RawContacts.CONTACT_ID + "=" + contactId, null,
                null);
        if (c != null) {
        	try {
        		String accountType;
        		long rawContactId;
        		ContactsSource contactsSource;
        		while (c.moveToNext()) {
                    	accountType = c.getString(2);
                    	rawContactId = c.getLong(0);
                    	contactsSource = sources.getInflatedSource(accountType,
                            ContactsSource.LEVEL_SUMMARY);
                    	if (null != contactsSource && contactsSource.readOnly) {
                    		mReadOnlySourcesCnt += 1;
                    	} else {
                    		mWritableSourcesCnt += 1;
                    		mWritableRawContactIds.add(rawContactId);
                    	}
        		}
        	} finally {
        		c.close();
        	}
        }

        mSelectedContactUri = contactUri;
        if (mReadOnlySourcesCnt > 0 && mWritableSourcesCnt > 0) {
            showDialog(R.id.dialog_readonly_contact_delete_confirmation);
        } else if (mReadOnlySourcesCnt > 0 && mWritableSourcesCnt == 0) {
            showDialog(R.id.dialog_readonly_contact_hide_confirmation);
        } else if (mReadOnlySourcesCnt == 0 && mWritableSourcesCnt > 1) {
            showDialog(R.id.dialog_multiple_contact_delete_confirmation);
        } else {
            showDialog(R.id.dialog_delete_contact_confirmation);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return true;
        }
        mPosition = info.position;
        Entry<Long, ContactsUtils.ArrayData> entry = mAdapter.getItem(info.position);
        ContactsUtils.ArrayData data = entry.getValue();
        long contactId = entry.getKey();
        
    	if (data == null) {
            return false;
        }
    	
		int mPosition = info.position;
		int indicate = data.mSimId;
        switch (item.getItemId()) {
            case MENU_ITEM_TOGGLE_STAR: {
                // Toggle the star
                ContentValues values = new ContentValues(1);
                values.put(Contacts.STARRED, data.starState == 0 ? 1 : 0);
                final Uri selectedUri = this.getContactUri(info.position);
				if(null != selectedUri)
                getContentResolver().update(selectedUri, values, null, null);
				startQuery();
                return true;
            }

            case MENU_ITEM_CALL: {
                callContact(data);
                return true;
            }

            case MENU_ITEM_SEND_SMS: {
                smsContact(data);
                return true;
            }

            case MENU_ITEM_DELETE: {
                if (indicate >= RawContacts.INDICATE_SIM) {
            		mSelectedContactUri = getContactUri(info.position);
            		Log.i(TAG,"mSelectedContactUri IS "+mSelectedContactUri);
            		showDialog(R.id.dialog_sim_contact_delete_confirmation);
            		return true;
            	} else {
            		if(getContactUri(info.position)!=null)
            			doContactDelete(getContactUri(info.position));
                    return true;
            	}
            }    
            case MENU_ITEM_EDIT: {
            	mEditContact = true;
				Uri contactUri = ContentUris.withAppendedId(
						Contacts.CONTENT_URI, contactId);
				long rawContactId = ContactsUtils.queryForRawContactId(
						getContentResolver(), contactId);
				Uri rawContactUri = ContentUris.withAppendedId(
						RawContacts.CONTENT_URI, rawContactId);
				final Intent intent = new Intent(Intent.ACTION_EDIT,
						rawContactUri);
				startActivity(intent);
				return true;
			}

            case MENU_ITEM_SHARE: {
            	final Uri uri = getContactUri(info.position);
            	doShareVisibleContacts("Single_Contact", uri, contactId);//share single contact
            	return true;
            }
            
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if(mMode != MODE_VIEW_GROUPS) {
            return;
        }
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }
        Entry<Long, ContactsUtils.ArrayData> item = mAdapter.getItem(info.position);
        ContactsUtils.ArrayData data = item.getValue();
        long contactId = item.getKey();
        
    	if (data == null) {
                return;
        }
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        long rawContactId = ContactsUtils.queryForRawContactId(getContentResolver(), contactId);
        Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);

        // Setup the menu header
        String header= (String) data.mTitle;
        
        if (TextUtils.isEmpty(header)){
            menu.setHeaderTitle(R.string.unknown);

        }else{
            menu.setHeaderTitle(header);
        }

        // View contact details
        final Intent viewContactIntent = new Intent(Intent.ACTION_VIEW, contactUri);
        StickyTabs.setTab(viewContactIntent, getIntent());
        menu.add(0, MENU_ITEM_VIEW_CONTACT, 0, R.string.menu_viewContact)
            .setIntent(viewContactIntent);

        // Contact editing
        int simPhoneIndicate = data.mSimId;
        // Star toggling
        int starState = data.starState;//cursor.getInt(SUMMARY_STARRED_COLUMN_INDEX);
        if (simPhoneIndicate == RawContacts.INDICATE_PHONE) {
        	if (starState == 0) {
        		menu.add(0, MENU_ITEM_TOGGLE_STAR, 0, R.string.menu_addStar);
        	} else {
        		menu.add(0, MENU_ITEM_TOGGLE_STAR, 0, R.string.menu_removeStar);
        	}
        }
        boolean addEditAndDelete = true;
    	final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
    		.getService(Context.TELEPHONY_SERVICE));

    	boolean simReady = (TelephonyManager.SIM_STATE_READY 
            	== TelephonyManager.getDefault()
            		.getSimState());
        boolean sim1Ready = (TelephonyManager.SIM_STATE_READY 
            		== TelephonyManager.getDefault()
            		.getSimStateGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1));
        boolean sim2Ready = (TelephonyManager.SIM_STATE_READY 
            		== TelephonyManager.getDefault()
            		.getSimStateGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2));
    	try {
    		if (true == FeatureOption.MTK_GEMINI_SUPPORT) {
    			if (simPhoneIndicate == 1 && null != iTel
    						&& (!iTel
    								.hasIccCardGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1) || !iTel
    								.isRadioOnGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1) || !sim1Ready
    								|| iTel.isFDNEnabledGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1))) {
    				addEditAndDelete = false;
    			} else if (simPhoneIndicate == 2 && null != iTel
    						&& (!iTel
    								.hasIccCardGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2) || !iTel
    								.isRadioOnGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2) || !sim2Ready
    								|| iTel.isFDNEnabledGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2))) {
    				addEditAndDelete = false;
    			}
    		} else {
    			if (simPhoneIndicate == 0 && null != iTel
    						&& (!iTel.hasIccCard() || !iTel.isRadioOn() || !simReady || iTel.isFDNEnabled())) {
    				addEditAndDelete = false;
    			}
    		}
    	} catch (RemoteException e) {
    		addEditAndDelete = false;
    	}
    	if (addEditAndDelete) {
    		menu.add(0, MENU_ITEM_EDIT, 0, R.string.menu_editContact);
    		menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_deleteContact);
        }
    	
    	considerAddSpeedDialMenuItem(menu, data, contactUri);

		// mtk80909 end
		  final Context dialogContext = new ContextThemeWrapper(this, android.R.style.Theme_Light);//add share single contact
	      final Resources res = dialogContext.getResources();
		if (res.getBoolean(R.bool.config_allow_share_visible_contacts)) {
			menu.add(0, MENU_ITEM_SHARE, 0, R.string.share_contacts);
        }
	}
	
	// mtk80909 for Speed Dial
	private void considerAddSpeedDialMenuItem(ContextMenu menu, 
			ContactsUtils.ArrayData data, Uri contactUri) {
		boolean hasPhone = (data.hasPhoneNumber != 0);
		if (!hasPhone) return;
		final Intent intent = new Intent(this, AddSpeedDialActivity.class);
		intent.setData(contactUri);
		StickyTabs.setTab(intent, getIntent());
		menu.add(0, MENU_ITEM_SPEED_DIAL, 0, R.string.speed_dial_view)
				.setIntent(intent);
    }


    
	private void doShareVisibleContacts(String type, Uri uri, long contactId) {
		Cursor cursor = null;
		Log.i(TAG, "uri is " + uri);
		try {
			if (type == "Single_Contact") {
				String selectionStr = Contacts.IN_VISIBLE_GROUP + "=1"
					+ " AND( " + RawContacts.INDICATE_PHONE_SIM + ">" + RawContacts.INDICATE_PHONE
					+ " OR " + RawContacts.INDICATE_PHONE_SIM
					+ "=" + RawContacts.INDICATE_PHONE
					+")";
				cursor = getContentResolver().query(
						uri,
						sLookupProjection,
						selectionStr,
						null, null);
				if (cursor != null && cursor.moveToNext()) {
					uri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, Uri
							.encode(cursor.getString(0)));
					Log.i(TAG, "Single_Contact  uri is " + uri
							+ " \ncursor.getString(0) is "
							+ cursor.getString(0));
				}
			} 
			final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(Contacts.CONTENT_VCARD_TYPE);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            if (contactId != -1) {
            	intent.putExtra("contactId", (int)contactId);
            }
            startActivity(intent);
		} catch (Exception e) {
			Log.e(TAG, "bad menuInfo", e);
		} finally {
			if(cursor != null)cursor.close();
		}
	}
    
    private void startQuery() {
        if (mQueryHandler == null) {
            mQueryHandler = new QueryHandler(this);
        }
        Uri groupUri = Uri.withAppendedPath(Contacts.CONTENT_GROUP_URI, mTitleText);
        Log.i(TAG, "Group Contact Uri " + groupUri);
        mQueryHandler.startQuery(0, null, groupUri, CONTACTS_SUMMARY_PROJECTION, null, null, Contacts.SORT_KEY_PRIMARY);
    }
    
    private class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(Context context) {
            super(context.getContentResolver());
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
            mCursor = cursor;
            if (cursor == null) {
                Log.e(TAG, "error happen onQueryComplete");
                if(empty != null)empty.setVisibility(View.VISIBLE);
                return;
            }
            int count = mCursor.getCount();
            if(count == 0 && empty != null)empty.setVisibility(View.VISIBLE);
            if (mCount != null) {
                mCount.setText("(" + count+ ")");
            }
            mCursor.setNotificationUri(getContentResolver(), Contacts.CONTENT_GROUP_URI);
            if(mMode != MODE_EDIT_GROUPS)mCursor.registerContentObserver(mObserver);
            Log.i(TAG, "QueryComplete begin count " + count);
            mArray.clear();
            HashMap<Long, ArrayData> contactMap = new HashMap<Long, ArrayData>();
            int i = 0;
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                long photoId = cursor.getLong(2);
                ContactsUtils.ArrayData data = new ContactsUtils.ArrayData().fromCursor(cursor);
                if (TextUtils.isEmpty(data.mSortkey))
                	data.mSortkey = "0" + i;
                else {
					char c = data.mSortkey.charAt(0);
					if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
						data.mSortkey = Character.toUpperCase(c) + "" + i;
					} else {
						data.mSortkey = "0" + i;
					}
                }
                i++;
                contactMap.put(id, data);
            }
            mArray = new ArrayList(contactMap.entrySet());
            Collections.sort(mArray, EditGroupsActivity.this);
            contactMap.clear();
            mAdapter.updateIndexer();
            mAdapter.notifyDataSetChanged();
            Log.i(TAG, "QueryComplete end.");
            StringBuilder builder = new StringBuilder();
            for (Entry<Long, ContactsUtils.ArrayData> entry : mArray) {
                builder.append(entry.getKey());
                builder.append(",");
            }
            cursor.close();
            Log.i(TAG, "initial array " + builder.toString());
        }
        
    }
    
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            Log.i(TAG, "ContentObserver");
            if (!mForeground) {
                mForceQuery = true;
                Log.i(TAG, "content changed, will requery on resume");
                return;
            }
            startQuery();
        }
    };

    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mMode != MODE_VIEW_GROUPS) {
            return;
        }
        Entry<Long, ContactsUtils.ArrayData> item = mAdapter.getItem(position);
        ContactsUtils.ArrayData data = item.getValue();
        long contactId = item.getKey();
//        Uri contactUri2 = Contacts.getLookupUri(id, data.mLookupKey);
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
        startActivity(intent);
    }
    
    
    private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
        {
            onListItemClick((ListView)parent, v, position, id);
        }
    };
    

    private View.OnClickListener mViewClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.save:
                    CharSequence title = mTitle.getText();
                    if (mMode != MODE_VIEW_GROUPS) {
                        if (!checkName(title)) {
                            return;
                        }
                    }
                    new SaveGroupTask(EditGroupsActivity.this).execute(title);
//                    doSave(title);
//                    finish();
                    break;
                case R.id.cancel:
                    finish();
                    break;
                case R.id.add_group_mem : {

                	if(true == ContactsUtils.mSavingGroup){
                		Toast.makeText(EditGroupsActivity.this, R.string.group_is_saving, Toast.LENGTH_LONG).show();
                		return;
                	}
                    ContactsUtils.mContactMap.clear();
                    Intent intent = new Intent(EditGroupsActivity.this, ContactsGroupsMultiOpt.class);
                    intent.putExtra(Groups_MODE_KEY, ContactsGroupsMultiOpt.MODE_ADD_CONTACT);
                    if (mTitleText != null) {
                        intent.putExtra(ContactsGroupsActivity.GROUP_TITLE_KEY, mTitleText);
                    }
                    startActivityForResult(intent, REQUEST_CONTACTS);
                    break;
                }
            }

        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            ContactsUtils.mContactMap.clear();
            return;
        }
        Log.i(TAG, "Map size " + ContactsUtils.mContactMap.size());
        int count = (ContactsUtils.mContactMap!= null ? ContactsUtils.mContactMap.size() : 0) 
        		+ (mCursor!=null ? mCursor.getCount() : 0);
        if (mCount != null) {
            mCount.setText("(" + count+ ")");
        }
        if (requestCode == REQUEST_CONTACTS) {
            if (mMode == MODE_VIEW_GROUPS) {
                new PersistGroupTask(this).execute();
            }
            Set<Entry<Long, ContactsUtils.ArrayData>> entrySet = ContactsUtils.mContactMap.entrySet();
            mArray.addAll(entrySet);
            if (mMode == MODE_EDIT_GROUPS) {
                mAddArray.addAll(entrySet);
            }
            if (mMode != MODE_VIEW_GROUPS) {
                ContactsUtils.mContactMap.clear();
            }
            
            Collections.sort(mArray, sortByString);
            mAdapter.updateIndexer();
            mAdapter.notifyDataSetChanged();
            Log.i(TAG, "mAdapter size " + mArray.size());
        }
        if(requestCode == REQUEST_FINISH){
        	this.finish();
        }
    }
    
    private boolean checkName(CharSequence name) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(EditGroupsActivity.this, R.string.name_needed, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!name.toString().equals(mTitleText)) { 
            Cursor cursor = getContentResolver().query(Groups.CONTENT_URI, 
                    new String[]{Groups._ID}, Groups.TITLE + "=? AND " + Groups.DELETED + "=0 AND " + Groups.ACCOUNT_TYPE + "='DeviceOnly'", new String[]{name.toString()}, null);
            if (cursor == null || cursor.getCount() == 0) {
                if (cursor != null) {
                    cursor.close();
                }
                return true;
            } else {
                cursor.close();
                Toast.makeText(EditGroupsActivity.this, R.string.group_name_exists, Toast.LENGTH_SHORT).show();
                return false;
            }
            
        }
        return true;
    }
    
    private class PersistGroupTask extends
        WeakAsyncTask<Void, Void, Boolean, EditGroupsActivity> {
        private WeakReference<ProgressDialog> mProgress;
        public PersistGroupTask(EditGroupsActivity target) {
            super(target);
        }
        @Override
        protected Boolean doInBackground(EditGroupsActivity target, Void... params) {
            boolean error = false;
            final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
            ContentProviderOperation.Builder builder;
            
//            if (operationList.size() > 0) {
//                try {
//                    Log.e(TAG, "getContentResolver().applyBatch begin: ");
//                    ContentProviderResult[] results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
//                    Log.e(TAG, "getContentResolver().applyBatch end!");
//                } catch (RemoteException e) {
//                    error = true;
//                    Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
//                } catch (OperationApplicationException e) {
//                    error = true;
//                    Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
//                }
//            }
            
            for (Entry<Long, ContactsUtils.ArrayData> entry : ContactsUtils.mContactMap.entrySet()) {
            	long contactId = entry.getKey();
            Cursor cursor = getContentResolver().query(RawContacts.CONTENT_URI, new String[]{RawContacts._ID}, 
                        RawContacts.CONTACT_ID + "=" + contactId + " AND " + RawContacts.DELETED + "==0", null, null);
                if (cursor == null) continue;
                Log.e(TAG, "performance: create data begin");
            while (cursor.moveToNext()) {
                long rawId = cursor.getLong(0);
                    builder= ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    builder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
                    builder.withValue(GroupMembership.GROUP_ROW_ID, mGroupId);
                    builder.withValue(Data.RAW_CONTACT_ID, rawId);
                    operationList.add(builder.build());
                    
                    if (operationList.size() == 100) {
                        try {
                            ContentProviderResult[] results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
                        } catch (RemoteException e) {
                            error = true;
                            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                        } catch (OperationApplicationException e) {
                            error = true;
                            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                        }
                        operationList.clear();
                    }
            }
            cursor.close();
            
            }
            Log.e(TAG, "performance: create data end");

            Log.e(TAG, "performance: update begin");
            if (operationList.size() > 0) {
                try {
                    ContentProviderResult[] results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
                } catch (RemoteException e) {
                    error = true;
                    Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                } catch (OperationApplicationException e) {
                    error = true;
                    Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                }
            }
            Log.e(TAG, "performance: create data end");
            return error;
        }

        @Override
        protected void onPostExecute(EditGroupsActivity target, Boolean error) {
            ContactsUtils.mContactMap.clear();
            ContactsUtils.mSavingGroup = false;
            final ProgressDialog progress = mProgress.get();
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }
            int toast;
            if (error) {
                toast = R.string.save_group_fail;
            } else {
                toast = R.string.save_group_success;
            }
            Toast.makeText(EditGroupsActivity.this, toast, Toast.LENGTH_SHORT).show();
//            target.finish();
            target.startQuery();
        }

        @Override
        protected void onPreExecute(EditGroupsActivity target) {
            mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(target, null,
                    target.getText(R.string.saving_group),false,true));
            ContactsUtils.mSavingGroup = true;
        }
        
    }
    
    private class SaveGroupTask extends
            WeakAsyncTask<CharSequence, Void, Boolean, EditGroupsActivity> {

        private WeakReference<ProgressDialog> mProgress;
        public SaveGroupTask(EditGroupsActivity target) {
            super(target);
        }

        @Override
        protected void onPreExecute(EditGroupsActivity target) {
            mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(target, null,
                    target.getText(R.string.saving_group),false,true));
        }
        
        @Override
        protected Boolean doInBackground(final EditGroupsActivity target,
                CharSequence... params) {
            CharSequence name = params[0];
            return doSave(name);
        }

        @Override
        protected void onPostExecute(final EditGroupsActivity target, Boolean error) {
            int toast;
            final ProgressDialog progress = mProgress.get();
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }
            if (error) {
                toast = R.string.save_group_fail;
            } else {
                toast = R.string.save_group_success;
            }
            Toast.makeText(EditGroupsActivity.this, toast, Toast.LENGTH_LONG).show();
            if(mMode == MODE_EDIT_GROUPS)setResult(Activity.RESULT_OK);
            finish();
        }
    }
    
    private boolean doSave(final CharSequence name) {
        if (mMode == MODE_VIEW_GROUPS) {
            return true;
        }
        StringBuilder builder3 = new StringBuilder();
        for (Entry<Long, ContactsUtils.ArrayData> entry : mArray) {
            builder3.append(entry.getKey());
            builder3.append(",");
        }
        Log.i(TAG, "changed array " + builder3.toString());
        StringBuilder builder1 = new StringBuilder();
        for (Entry<Long, ContactsUtils.ArrayData> entry : mAddArray) {
            builder1.append(entry.getKey());
            builder1.append(",");
        }
        Log.i(TAG, "added array " + builder1.toString());
        StringBuilder builder2 = new StringBuilder();
        for (Entry<Long, ContactsUtils.ArrayData> entry : mDelArray) {
            builder2.append(entry.getKey());
            builder2.append(",");
        }
        Log.i(TAG, "deleted array " + builder2.toString());
        boolean error = false;
        final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
        if (mMode == MODE_EDIT_GROUPS) {
            if (!mTitleText.equals(name)) {
                ContentValues values = new ContentValues(1);
                values.put(Groups.TITLE, name.toString());
                int count = getContentResolver().update(ContentUris.withAppendedId(Groups.CONTENT_URI, mGroupId), values, null, null);
                
                SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
                String groupName = prefs.getString("group", "");
                if(groupName.equals(mTitleText)){
    				SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(this).edit();
                    editor.putString("group", name.toString());
                    editor.apply();                	
                }
                
            }
            for (int i = 0; i < mAddArray.size(); i++) {
                Entry<Long, ContactsUtils.ArrayData> entry = mAddArray.get(i);
                long contactId = entry.getKey();
                Cursor cursor = getContentResolver().query(RawContacts.CONTENT_URI, new String[]{RawContacts._ID}, 
                        RawContacts.CONTACT_ID + "=" + contactId + " AND " + RawContacts.DELETED + "=0", null, null);
                if (cursor == null) continue;
                while (cursor.moveToNext()) {
                    long rawId = cursor.getLong(0);
                    ContentProviderOperation.Builder builder= ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    builder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
                    builder.withValue(GroupMembership.GROUP_ROW_ID, mGroupId);
                    builder.withValue(Data.RAW_CONTACT_ID, rawId);
                    operationList.add(builder.build());
                    
                    if (operationList.size() == 100) {
                        try {
                            ContentProviderResult[] results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
                        } catch (RemoteException e) {
                            error = true;
                            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                        } catch (OperationApplicationException e) {
                            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                            error = true;
                        }
                        operationList.clear();
                    }
                }
                cursor.close();
            }
            if (operationList.size() > 0) {
                try {
                    ContentProviderResult[] results = getContentResolver().applyBatch(
                            ContactsContract.AUTHORITY, operationList);
                    
                } catch (RemoteException e) {
                    error = true;
                    Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                } catch (OperationApplicationException e) {
                    Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    error = true;
                }
                operationList.clear();
            }
            int count = 0;
            for (int i = 0; i < mDelArray.size(); i++) {
                Entry<Long, ContactsUtils.ArrayData> entry = mDelArray.get(i);
                long contactId = entry.getKey();
                count += getContentResolver().delete(Data.CONTENT_URI, 
                        Data.MIMETYPE + "='" + GroupMembership.CONTENT_ITEM_TYPE + 
                        "' AND " + Data.CONTACT_ID + "=" + contactId + 
                        " AND " + ContactsContract.Data.DATA1 + "=" + mGroupId, null);
            }
            Log.i(TAG, "delete count " + count);
            return error;
        }
        ContentProviderOperation.Builder builder;
        ContentValues values = new ContentValues();
        values.put(Groups.TITLE, name.toString());
        values.put(Groups.GROUP_VISIBLE, 1);
        values.put(Groups.SYSTEM_ID, 0);
        Uri uri = getContentResolver().insert(Groups.CONTENT_URI, values);
        long groupId = (uri == null) ? 0:ContentUris.parseId(uri);
        Log.i(TAG, "insert group " + name + ", id " + groupId);
        int size = mArray.size();
        for (int i = 0; i < size; i++) {
            Entry<Long, ContactsUtils.ArrayData> entry = mArray.get(i);
            long contactId = entry.getKey();
            Cursor cursor = getContentResolver().query(RawContacts.CONTENT_URI, new String[]{RawContacts._ID}, 
                    RawContacts.CONTACT_ID + "=" + contactId + " AND " + RawContacts.DELETED + "=0", null, null);
            if (cursor == null) continue;
            while (cursor.moveToNext()) {
                long rawId = cursor.getLong(0);
                builder= ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
                builder.withValue(GroupMembership.GROUP_ROW_ID, groupId);
                builder.withValue(Data.RAW_CONTACT_ID, rawId);
                operationList.add(builder.build());
                
                if (operationList.size() == 100) {
                    try {
                        ContentProviderResult[] results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
                    } catch (RemoteException e) {
                        error = true;
                        Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                    } catch (OperationApplicationException e) {
                        Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
                        error = true;
                    }
                    operationList.clear();
                }
            }
            cursor.close();
        }
        try {
            ContentProviderResult[] results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
        } catch (RemoteException e) {
            error = true;
            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
        } catch (OperationApplicationException e) {
            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            error = true;
        }
        
        return error;
    }
    
    private class SendGroupSmsTask extends
            WeakAsyncTask<String, Void, String, EditGroupsActivity> {
        private WeakReference<ProgressDialog> mProgress;
        public SendGroupSmsTask(EditGroupsActivity target) {
            super(target);
        }
        @Override
        protected void onPreExecute(EditGroupsActivity target) {
            mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(target, null,
                    target.getText(R.string.please_wait),false,true));
        }
        @Override
        protected String doInBackground(final EditGroupsActivity target, String... group) {
        	return ContactsUtils.getSmsAddressFromGroup(target.getBaseContext(), group[0]);
        }

        @Override
        protected void onPostExecute(final EditGroupsActivity target, String address) {
            ProgressDialog progress = mProgress.get();
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }
            if(address == null || address.length() == 0){
            	Toast.makeText(target, R.string.no_valid_number_in_group, Toast.LENGTH_LONG).show();
            }else{
            	String[] list = address.split(";");
                if(list.length > 1){
                	Toast.makeText(target, list[1], Toast.LENGTH_LONG).show();
                }
                address = list[0];
                if(address == null || address.length() == 0)return;
            	Intent intent = new Intent(Intent.ACTION_SENDTO);
            	intent.setData(Uri.fromParts(Constants.SCHEME_SMSTO,address , null));
            	startActivity(intent);
            }
        }
    }
    
    private class SendGroupEmailTask extends
            WeakAsyncTask<String, Void, String, EditGroupsActivity> {
        private WeakReference<ProgressDialog> mProgress;

        public SendGroupEmailTask(EditGroupsActivity target) {
            super(target);
        }

        @Override
        protected void onPreExecute(EditGroupsActivity target) {
            mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(target, null, target
                    .getText(R.string.please_wait)));
        }

        @Override
        protected String doInBackground(final EditGroupsActivity target, String... group) {
            return ContactsUtils.getEmailAddressFromGroup(target.getBaseContext(), group[0]);
        }

        @Override
        protected void onPostExecute(final EditGroupsActivity target, String address) {
            ProgressDialog progress = mProgress.get();
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }
            try {
                // Intent intent = new Intent(Intent.ACTION_SENDTO,
                // Uri.fromParts(Constants.SCHEME_MAILTO, address, null));
//                String[] addrList = address.split(",");
//                
//                Intent intent = new Intent(Intent.ACTION_SEND);
//                intent.setType("*/*");
//                intent.putExtra(Intent.EXTRA_EMAIL, addrList);
            	Uri dataUri = null;
            	
            	if(address!= null && address.length()>0){
//            		address = address.replace(",", ";");
            		dataUri = Uri.parse("mailto:" + address);	
            	}
            	if(address == null || address.length() == 0){
            		Toast.makeText(EditGroupsActivity.this, R.string.no_valid_email_in_group, Toast.LENGTH_SHORT).show();
            	}else{
                Intent intent=new Intent(Intent.ACTION_SENDTO,dataUri);
                startActivity(intent);
            	}
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "No activity found for Eamil");
                Toast.makeText(EditGroupsActivity.this, R.string.email_error, Toast.LENGTH_SHORT).show();
            }catch (Exception e) {
                Log.e(TAG, "SendGroupEmail error", e);
            }
        }
    }
    
    private class GroupsListAdapter extends ArrayAdapter<Entry<Long, ContactsUtils.ArrayData>> 
        implements View.OnClickListener, OnScrollListener, SectionIndexer{
        private LayoutInflater mInflater;
        private SectionIndexer mIndexer;
        public GroupsListAdapter(Context context, int layout, int textViewResourceId, List<Entry<Long, ContactsUtils.ArrayData>> objects) {
            super(context, layout, textViewResourceId, objects);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            updateIndexer();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Entry<Long, ContactsUtils.ArrayData> item = getItem(position);
            ContactsUtils.ArrayData data = item.getValue();
            long id = item.getKey();
            Log.i(TAG, "getView position " + position + ", name " + data.mTitle);
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.groups_add_item, parent, false);
            } else {
                view = convertView;
            }
            TextView title = (TextView)view.findViewById(R.id.title);
            QuickContactBadge icon = (QuickContactBadge)view.findViewById(R.id.icon);
            title.setText(data.mTitle);
            Uri contactUri = Contacts.getLookupUri(id, data.mLookupKey);
            Log.i(TAG, "getView contact Uri " + contactUri);
            icon.assignContactUri(contactUri);
            mPhotoLoader.loadPhoto(icon, data.mPhotoId, data.mSimId);
            
            TextView divider = (TextView)view.findViewById(R.id.divider);
            final int section = getSectionForPosition(position);
            if (getPositionForSection(section) == position) {
                String index = (String)mIndexer.getSections()[section];
                divider.setText(index);
                divider.setVisibility(View.VISIBLE);
            } else {
                divider.setVisibility(View.GONE);
            }
            ImageView delete = (ImageView)view.findViewById(R.id.delete);
            if (mMode == MODE_VIEW_GROUPS) {
                delete.setVisibility(View.GONE);
                return view;
            }
            delete.setImageResource(R.drawable.contact_group_movemember_list);
            delete.setTag(position);
            delete.setOnClickListener(this);
            return view;
        }

        @Override
        public int getCount() {
            return mArray.size();
        }
        // TODO  Need set the Notify M
        @Override
        public Entry<Long, ContactsUtils.ArrayData> getItem(int position) {
            return mArray.get(position);
        }

        public void onClick(View v) {
            int pos = (Integer)v.getTag();
            Log.i(TAG, "remove pos " + pos);
            Entry<Long, ContactsUtils.ArrayData> entry = mArray.remove(pos);
            if (!mAddArray.remove(entry)) {
                mDelArray.add(entry);
            }
            mAdapter.updateIndexer();
            mAdapter.notifyDataSetChanged();
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                mPhotoLoader.pause();

            } else {
                mPhotoLoader.resume();
            }
        }
        
        private void updateIndexer() {
            if (mArray == null || mArray.size() == 0) {
                mIndexer = null;
                return;
            }
            
            ArrayList<String> sections = new ArrayList<String>();
            ArrayList<Integer> counts = new ArrayList<Integer>();
            int size = mArray.size();
            String current = null;
            int position = -1;
            for (int i = 0; i < size; i++) {
                Entry<Long, ContactsUtils.ArrayData> entry = mArray.get(i);
                String sortKey = entry.getValue().mSortkey;
                if (sortKey == null) {
                    if (sections.size() == 0) {
                        sections.add(0, null);
                    }
                    if (counts.size() > 0) {
//                        counts.add(0, counts.get(0) + 1);
                        counts.set(0, counts.get(0) + 1);
                    } else {
                        counts.add(0, 1);
                    }
                    position = 0;
                    continue;
                }
                String firstChar = sortKey.substring(0, 1);
                if (firstChar == null || !Character.isLetter(firstChar.charAt(0))){
                    if (sections.size() == 0) {
                        sections.add(0, null);
                    }
                    if (counts.size() > 0) {
                        counts.set(0, counts.get(0) + 1);
                    } else {
                        counts.add(0, 1);
                    }
                    position = 0;
                    continue;
                }
                firstChar = firstChar.toUpperCase();
                if (firstChar.equals(current)) {
                    counts.set(position, counts.get(position) + 1);
                } else {
                    position++;
                    sections.add(position, firstChar);
                    counts.add(position, 1);
                    current = firstChar;
                }
            }
            int count = counts.size();
            int[] countArray = new int[count];
            for (int i = 0; i < count; i++) {
                countArray[i] = counts.get(i);
            }
            mIndexer = new ContactsSectionIndexer(sections.toArray(new String[]{}), countArray);
        }
        
        public int getPositionForSection(int section) {
            if (mIndexer == null) {
                return -1;
            }
            int retVal = mIndexer.getPositionForSection(section);   
            return retVal;
        }

        public int getSectionForPosition(int position) {
            if (mIndexer == null) {
                return -1;
            }
            int retVal = mIndexer.getSectionForPosition(position);
            return retVal;
        }

        public Object[] getSections() {
            if (mIndexer == null) {
                return new String[] { " " };
            } else {
                return mIndexer.getSections();
            }
        }
    }
    public int compare(Entry<Long, ArrayData> one, Entry<Long, ArrayData> two) {
        if (one == null && two == null) {
            return 0;
        }
        if (one == null) {
            return -1;
        }
        if (two == null) {
            return 1;
        }
        
        ArrayData dataOne = one.getValue();
        ArrayData dataTwo = two.getValue();
        if ( dataTwo == null  || dataTwo.mSortkey == null || dataTwo.mSortkey.length() < 2 ) {
            return 1;
        }
        if (dataOne == null|| dataOne.mSortkey == null || dataOne.mSortkey.length() < 2) {
            return -1;
        }
        
        int a = Integer.valueOf(dataOne.mSortkey.substring(1));
        int b = Integer.valueOf(dataTwo.mSortkey.substring(1));
        
        return a < b ? -1 : (a > b ? 1 : 0);
    }
    
    private Comparator<Entry<Long, ContactsUtils.ArrayData>> sortByString = new Comparator<Entry<Long, ContactsUtils.ArrayData>>() {

	    public int compare(Entry<Long, ArrayData> one, Entry<Long, ArrayData> two) {
	        if (one == null && two == null) {
	            return 0;
	        }
	        if (one == null) {
	            return -1;
	        }
	        if (two == null) {
	            return 1;
	        }
	        
	        ArrayData dataOne = one.getValue();
	        ArrayData dataTwo = two.getValue();
        if ( dataTwo == null  || dataTwo.mSortkey == null) {
            return 1;
        }
        if (dataOne == null|| dataOne.mSortkey == null) {
            return -1;
        }
        
	        return dataOne.mSortkey.toString().compareToIgnoreCase(dataTwo.mSortkey.toString());
    }
    
	};
}
