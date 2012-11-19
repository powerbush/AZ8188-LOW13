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

import com.android.contacts.Collapser.Collapsible;
import com.android.contacts.ContactsUtils.OnDialCompleteListener;
import com.android.contacts.model.ContactsSource;
import com.android.contacts.model.Sources;
import com.android.contacts.model.ContactsSource.DataKind;
import com.android.contacts.ui.EditContactActivity;
import com.android.contacts.util.Constants;
import com.android.contacts.util.DataStatus;
import com.android.contacts.util.NotifyingAsyncQueryHandler;
import com.android.internal.telephony.ITelephony;
import com.android.internal.widget.ContactHeaderWidget;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.EntityIterator;
import android.content.Intent;
import android.content.Entity.NamedContentValues;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ParseException;
import android.net.Uri;
import android.net.WebAddress;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.AggregationExceptions;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.DisplayNameSources;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.provider.ContactsContract.StatusUpdates;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
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
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mediatek.client.DataManager;
import com.mediatek.client.DataManager.WidgetEvent;
import com.mediatek.client.DataManager.SnsUser;
import com.mediatek.wsp.util.SnsType;
import com.android.contacts.mtk.*;
import android.graphics.BitmapFactory;
import android.view.animation.AnimationUtils;

import android.widget.AdapterView.OnItemClickListener;
import android.provider.Settings;
import android.widget.*;
import com.mediatek.client.SnsClientAPI;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import com.mediatek.wsp.util.EmotionParser;
import android.graphics.Color;
import com.mediatek.featureoption.FeatureOption;
import android.content.AsyncQueryHandler;

import com.mediatek.CellConnService.CellConnMgr;
import com.android.contacts.ui.widget.DontPressWithParentImageView;
import android.content.IntentFilter;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.os.SystemProperties;
import com.android.contacts.util.Constants.SimInfo;
import android.content.ContentProviderOperation;//sim Assocation
import android.content.ContentProviderResult;//sim Assocation
import android.content.OperationApplicationException;//sim Assocation
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import com.mediatek.telephony.PhoneNumberFormatUtilEx;
import android.provider.Telephony.SIMInfo;//gemini enhancement
import android.provider.Telephony;

/**
 * Displays the details of a specific contact.
 */
public class ContactOwnerActivity extends Activity
        implements View.OnCreateContextMenuListener, DialogInterface.OnClickListener,
        AdapterView.OnItemClickListener, NotifyingAsyncQueryHandler.AsyncQueryListener, 
        View.OnClickListener, View.OnTouchListener, GestureDetector.OnGestureListener {
    private static final String TAG = "ContactOwner";

    public static final String AUTHORITIES_FILTER_KEY = "authorities";

    private static final boolean SHOW_SEPARATORS = false;

    private static final int DIALOG_CONFIRM_DELETE = 1;
    private static final int DIALOG_CONFIRM_READONLY_DELETE = 2;
    private static final int DIALOG_CONFIRM_MULTIPLE_DELETE = 3;
    private static final int DIALOG_CONFIRM_READONLY_HIDE = 4;

    private static final int REQUEST_JOIN_CONTACT = 1;
    private static final int REQUEST_EDIT_CONTACT = 2;
    private static final int REQUEST_OWNER_CONTACT = 3;
    private static final int REQUEST_OWNER_ACCOUNT = 4;

    public static final int MENU_ITEM_MAKE_DEFAULT = 3; 
    public static final int MENU_ITEM_CALL = 4;
    public static final int MENU_ITEM_EMAIL = 5;
    public static final int MENU_ITEM_POSTAL = 6;
    public static final int MENU_ITEM_VTCALL = 7;//VT call
    public static final int MENU_ITEM_ASSOCIATION = 8;//sim association
    public static final int MENU_ITEM_REMOVE_NUMBER = 9;//gemini enhancement
    public static final int MENU_ITEM_JOIN_CONTACT = 10;//gemini enhancement
    public static final int MENU_ITEM_REMOVE_ASSOCIATION = 11;//gemini enhancement

    protected Uri mLookupUri;
    private ContentResolver mResolver;
    private ViewAdapter mAdapter;
    private int mNumPhoneNumbers = 0;

    /**
     * A list of distinct contact IDs included in the current contact.
     */
    private ArrayList<Long> mRawContactIds = new ArrayList<Long>();

    /* package */ ArrayList<ViewEntry> mPhoneEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mSmsEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mEmailEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mPostalEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mImEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mNicknameEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mOrganizationEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mGroupEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ViewEntry> mOtherEntries = new ArrayList<ViewEntry>();
    /* package */ ArrayList<ArrayList<ViewEntry>> mSections = new ArrayList<ArrayList<ViewEntry>>();

    private Cursor mCursor;

    protected ContactHeaderWidget mContactHeaderWidget;
    private NotifyingAsyncQueryHandler mHandler;

    protected LayoutInflater mInflater;

    protected int mReadOnlySourcesCnt;
    protected int mWritableSourcesCnt;
    protected boolean mAllRestricted;

    protected Uri mPrimaryPhoneUri = null;

    protected ArrayList<Long> mWritableRawContactIds = new ArrayList<Long>();

    private static final int TOKEN_ENTITIES = 0;
    private static final int TOKEN_STATUSES = 1;
    private static final int TOKEN_NORMAL = 2;

    private boolean mHasEntities = false;
    private boolean mHasStatuses = false;

    private long mNameRawContactId = -1;
    private int mDisplayNameSource = DisplayNameSources.UNDEFINED;

    private ArrayList<Entity> mEntities = Lists.newArrayList();
    private HashMap<Long, DataStatus> mStatuses = Maps.newHashMap();

    // modified by Ivan 2010-07-29
    /*
     * mStatuEditor is the EditBox of status, can not edit, but can click
     * mSnsPhoto is the upload photo from camera, ImageButton
     * mSnsUpdate is upload photo from pic of local, ImageButton
     * mOwnerSns is sns information list of the owner, we just use it empty item
     * mOwnerContact is contact information list of the owner
     * mBtnSnsAccountManager is the button linked with sunway's account mananger
     * mBtnContactAccountManager is the button linked with EditContactActivity
     */
    private TextView mStatuEditor;
    //private ImageView mSnsFace;
    //private ImageView mSnsSetting;
    //private Button mBtnUpdate;
    private ImageView mSnsPhoto;
    private ImageButton mSnsUpdate;
    private LinearLayout mOwnerSns;
	private LinearLayout mOwnerSns2;
    private LinearLayout mOwnerContact;
    private Button mBtnSnsAccountManager;
    private Button mBtnSnsAccountManager2;
    private Button mBtnContactAccountManager;
    //private LinearLayout mll;

    // modified by Ivan 2010-08-01
    private SnsUser mOwner = null; // current owner contact

    protected Uri mAssociationPhoneUri = null;
	private SimAssociationQueryHandler mSimAssociationQueryHandler;
    
    /**
     * The view shown if the detail list is empty.
     * We set this to the list view when first bind the adapter, so that it won't be shown while
     * we're loading data.
     */
    private View mEmptyView;

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            if (mCursor != null && !mCursor.isClosed()) {
                startEntityQuery();
            }
        }
    };

    static final String[] RAW_CONTACTS_PROJECTION2 = new String[] {
        RawContacts._ID, //0
        RawContacts.CONTACT_ID, //1
        RawContacts.ACCOUNT_TYPE, //2
        RawContacts.ACCOUNT_NAME, //3
        RawContacts.SYNC3, //4
        RawContacts.SYNC4, //5
    };
    
    public void onClick(DialogInterface dialog, int which) {
        closeCursor();
	if (null == mLookupUri) {
		Toast.makeText(this, R.string.invalidContactMessage, Toast.LENGTH_SHORT).show();
	} else {
	        getContentResolver().delete(mLookupUri, null, null);
       		PhoneOwner.setOwner(null); // modified by Ivan 2010-08-16
        	finish();
	}
    }

    private ListView mListView;
	private ListView mListEvent = null;
    private boolean mShowSmsLinksForAllPhones;
    private View mNoContactInfo;
    private static final int KAIXIN_TYPE = 1;
    private static final int RENREN_TYPE = 2;
    private static final int TWITTER_TYPE = 3;
    private static final int FLICKR_TYPE = 4;
    private static final int FACEBOOK_TYPE = 5;
    
    private int mCurrentSelect = 0;

    private long mContactId;
    private Context mContext;
    private Button mOwnerAccountBtn = null;
	
    private TabHost tabHost;
    private static final String TAB_SNS_EVENT_ID = "TAB_EVENT";
    private static final String TAB_PROFILE_ID = "TAB_PROFILE";
	
    private ContactOwnerLoader mOwnerLoader;
    private Handler mUpdateLayoutHandler = null;
    private static final int MESSAGE_UPDATE_LAYOUT = 1;
    public static final String PREFS_NAME = "owner_info";
    public static final String PREFS_OWNER_ID = "owner_id";
    public static final String PREFS_OWNER_LOOKUPKEY = "owner_lookupkey";
    public static final String PREFS_OWNER_NAME = "owner_name";

    private ContactEventLoader mEventLoader = null;
    private static final int MESSAGE_ANIM_START = 1;
    private static final int MESSAGE_ANIM_STOP = 2;
    private static final int MESSAGE_RELOAD = 3;
    private Handler mAnimHandler = null;
    private EventListManager event = null;
    private static final String ORIGINAL_CONTENT_ID = "original_owner_info_id";
    private static final String ORIGINAL_CONTENT_LOOKUPKEY = "original_owner_info_lookupkey";
    private static final String ORIGINAL_CONTENT_NAME = "original_owner_info_name";

    private class SimAssociationQueryHandler extends AsyncQueryHandler {

        protected Context mContext;
        
        public SimAssociationQueryHandler(Context context, ContentResolver cr) {
            super(cr);
            mContext = context;
            // TODO Auto-generated constructor stub
        }
        
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Log.i(TAG,"onQueryComplete, sim association");
            
//            ArrayList associateSims = new ArrayList();
            
            final String number = (String)cookie;
            
//            try {
//                if(cursor != null && cursor.moveToFirst()) {
//                    do {
//                        if(cursor.getInt(0) > 0)
//                            associateSims.add(Integer.valueOf(cursor.getInt(0)));
//                    } while (cursor.moveToNext());
//                }
//            } catch(Exception e) {
//            	Log.i(TAG,"onQueryComplete, cursor exception");
//            } finally {
//                if(cursor != null)
//                    cursor.close();
//            }
            
            ContactsUtils.dial(mContext, number, ContactsUtils.DIAL_TYPE_VOICE, new ContactsUtils.OnDialCompleteListener(){
            	 public void onDialComplete(boolean dialed) {
                     // TODO Auto-generated method stub
                     if(dialed)
                         ContactOwnerActivity.this.finish();
                 }
            });
        }
	}
	
    private final BroadcastReceiver mReceiver = new SimIndicatorBroadcastReceiver();
    protected long indicate;
	CellConnMgr mCellConnMgr;
	private StatusBarManager mStatusBarMgr;

    private class SimIndicatorBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                Log.d(TAG, "SimIndicatorBroadcastReceiver, onReceive() ");
                if (action.equals(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED)) {
                    Log.d(TAG, "SimIndicatorBroadcastReceiver, onReceive(), ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED received ");
                    if (true == mShowSimIndicator) {
                        setSimIndicatorVisibility(true);
                    } else {
                        setSimIndicatorVisibility(false);
                    }
    }
            }
        }
    }

    boolean mShowSimIndicator = false;

    void setSimIndicatorVisibility(boolean visible) {
        if(visible)
            mStatusBarMgr.showSIMIndicator(getComponentName(), Settings.System.VOICE_CALL_SIM_SETTING);
        else
            mStatusBarMgr.hideSIMIndicator(getComponentName());
	}
	
	private static String mDeleteNumber = null;
	private long mDeleteDataId ; //Add by mtk80908. Used to delete phone number. 
	private static final String[] ADDRESS_BOOK_COLUMN_NAMES = new String[] {
        "name",
        "number",
        "emails",
        "additionalNumber",
        "groupIds"
    };
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
			if (FeatureOption.MTK_GEMINI_SUPPORT) {
          IntentFilter intentFilter =
              new IntentFilter(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
          registerReceiver(mReceiver, intentFilter);   
      }
	
	if(icicle != null){
		try{
			long id = Long.parseLong(icicle.getString(ORIGINAL_CONTENT_ID));
			String lookupKey = icicle.getString(ORIGINAL_CONTENT_LOOKUPKEY);
			String name = icicle.getString(ORIGINAL_CONTENT_NAME);
			PhoneOwner.initPhoneOwner(id, lookupKey);
			if(PhoneOwner.getInstance() != null){
				if(null == name || name.length() == 0)
					name = this.getText(android.R.string.unknownName) + "";
        			PhoneOwner.getInstance().setName(name);
        		}
		} catch (Exception e) {
			Log.i(TAG, "Init PhoneOwner exception : " + e.getMessage());
		}
	}
        //EmotionParser.init(0, this);
        if(null == ContactsManager.SNS_TYPE_LIST)
		ContactsManager.readSnsTypeList(this.getResources());
        mContext = this;
        final Intent intent = getIntent();
        mContactId = intent.getLongExtra("owner_id", (long)-1);
        Uri data = intent.getData();
      indicate = intent.getIntExtra(RawContacts.INDICATE_PHONE_SIM, RawContacts.INDICATE_PHONE);
      mContactId = intent.getIntExtra(Contacts._ID, -1);//gemini enhancement        
        if(data != null){
        	String authority = data.getAuthority();
	        if (ContactsContract.AUTHORITY.equals(authority)) {
	            mLookupUri = data;
	        } else if (android.provider.Contacts.AUTHORITY.equals(authority)) {
	            final long rawContactId = ContentUris.parseId(data);
	            mLookupUri = RawContacts.getContactLookupUri(getContentResolver(),
	                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId));
	
	        }
        }
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.owner_card_layout);

        mContactHeaderWidget = (ContactHeaderWidget) findViewById(R.id.contact_header_widget);

        mHandler = new NotifyingAsyncQueryHandler(this, this);
        mListView = (ListView) findViewById(R.id.owner_contact);
        if(null != mListView) {
		mListView.setOnCreateContextMenuListener(this);
		mListView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
		mListView.setOnItemClickListener(this);
	}

        mEmptyView = findViewById(R.id.none_contact_view);
	mNoContactInfo = findViewById(R.id.none_contact_info_view);
	if(null != mEmptyView) mEmptyView.setVisibility(View.GONE);
	if(null != mNoContactInfo) mNoContactInfo.setVisibility(View.GONE);

        mResolver = getContentResolver();

        // Build the list of sections. The order they're added to mSections dictates the
        // order they are displayed in the list.
        mSections.add(mPhoneEntries);
        mSections.add(mSmsEntries);
        mSections.add(mEmailEntries);
        mSections.add(mImEntries);
        mSections.add(mPostalEntries);
	mSections.add(mNicknameEntries);
        mSections.add(mOrganizationEntries);
        mSections.add(mGroupEntries);
        mSections.add(mOtherEntries);
        //TODO Read this value from a preference
        mShowSmsLinksForAllPhones = true;
        
        // modified by Ivan 2010-07-22
        /**
         * Initalize the controller
         */
        mStatuEditor = (TextView)this.findViewById(R.id.sns_status);
        mSnsPhoto = (ImageView)this.findViewById(R.id.sns_photo);
        mSnsUpdate = (ImageButton)this.findViewById(R.id.sns_update);
        mOwnerSns = (LinearLayout)this.findViewById(R.id.edit_sns_account_panel);
	mOwnerSns2 = (LinearLayout)this.findViewById(R.id.edit_sns_account_panel_2);
	mListEvent = (ListView) findViewById(R.id.sns_event_data);
	if(null != mListEvent) mListEvent.setEmptyView((ScrollView)findViewById(R.id.sns_event_empty));
	mEventLoader = new ContactEventLoader(this, mListEvent);

        mBtnSnsAccountManager = (Button)this.findViewById(R.id.sns_account_manager);
	mBtnSnsAccountManager2 = (Button)this.findViewById(R.id.sns_account_manager_2);
        mBtnContactAccountManager = (Button)this.findViewById(R.id.contact_account_manager);
        if(null != mStatuEditor) mStatuEditor.setFocusable(true);
        if(null != mStatuEditor) mStatuEditor.setOnClickListener(this);
        if(null != mBtnSnsAccountManager) mBtnSnsAccountManager.setOnClickListener(this);
        if(null != mBtnSnsAccountManager2) mBtnSnsAccountManager2.setOnClickListener(this);
        if(null != mBtnContactAccountManager) mBtnContactAccountManager.setOnClickListener(this);
        Bitmap tempPhoto = BitmapFactory.decodeResource(
        		getResources(), com.android.internal.R.drawable.ic_contact_picture);
	if(null != mContactHeaderWidget) {
		mContactHeaderWidget.setPhoto(tempPhoto);
		mContactHeaderWidget.setDisplayName(this.getResources().getString(R.string.owner_none), "");
		mContactHeaderWidget.setSocialSnippet(null);
	}
        /**
         * set unvisiable to the controller,
         * only when there is sns information, they can be seen
         */
        if(null != mStatuEditor) mStatuEditor.setVisibility(View.GONE);
        if(null != mSnsPhoto) mSnsPhoto.setVisibility(View.GONE);
        if(null != mSnsUpdate) mSnsUpdate.setVisibility(View.GONE);
        
        if(null != mSnsPhoto) mSnsPhoto.setOnClickListener(this);
        if(null != mSnsUpdate) mSnsUpdate.setOnClickListener(this);

	mOwnerLoader = new ContactOwnerLoader(this, R.drawable.ic_contact_list_picture, 
		this.getResources().getString(R.string.owner_none));
	mAnimHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MESSAGE_ANIM_START:
				startUpdateAnim();
				break;

			case MESSAGE_ANIM_STOP:
				stopUpdateAnim();
				updateResult();
				break;
			case MESSAGE_RELOAD:
				onStart();
				break;
			}
		}
	};

	mUpdateLayoutHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MESSAGE_UPDATE_LAYOUT:
				if(null == PhoneOwner.getInstance()) {
					mContactId = (long)-1;
					mLookupUri = null;
				} else {
				
					mContactId = PhoneOwner.getInstance().getOwnerID();
					Uri lookupUri = Contacts.getLookupUri(mContactId, 
						PhoneOwner.getInstance().getOwnerLookupKey());
	    				mLookupUri = Contacts.lookupContact(getContentResolver(), lookupUri);
				}
				updateLayout();
				startEntityQuery();

				SharedPreferences setting = getSharedPreferences(PREFS_NAME, 
					Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
    				SharedPreferences.Editor editor = setting.edit();
    				PhoneOwner owner = PhoneOwner.getInstance();
    				if(owner != null){
    					editor.putLong(PREFS_OWNER_ID, owner.getOwnerID());
    					editor.putString(PREFS_OWNER_LOOKUPKEY, owner.getOwnerLookupKey());
    					editor.putString(PREFS_OWNER_NAME, owner.getName());
    				}
    				else{
    					editor.clear();
    				}
    				editor.commit();

				break;
			}
		}
	};
	if(null != mContactHeaderWidget && null != mContactHeaderWidget.getPhotoView()) {
		mContactHeaderWidget.getPhotoView().setOnClickListener(new OnClickListener(){
			public void onClick(View v) {}
		});
	}
	tabHost = (TabHost) this.findViewById(R.id.tab_root);
	if(null == tabHost) {
		finish();
		return;
	}
	tabHost.setup();

	tabHost.addTab(tabHost.newTabSpec(TAB_PROFILE_ID).setContent(
			R.id.tab_profile).setIndicator(
			this.getString(R.string.Contact_Info_Tab_Indicator),
			this.getResources().getDrawable(R.drawable.tab_contact_icon)));
	tabHost.addTab(tabHost.newTabSpec(TAB_SNS_EVENT_ID).setContent(
			R.id.tab_event).setIndicator(
			this.getString(R.string.Contact_Event_Tab_Indicator),
			this.getResources().getDrawable(R.drawable.tab_sns_event_icon)));
	tabHost.setCurrentTab(0);

	tabHost.setOnTabChangedListener(new OnTabChangeListener() {
		public void onTabChanged(String tabId) {
			if (tabId.equals(TAB_PROFILE_ID)) {

			} else if (tabId.equals(TAB_SNS_EVENT_ID)) {
				
				} else {

			}
		}
	});
			mCellConnMgr = new CellConnMgr();
			mCellConnMgr.register(getApplicationContext());
			
			mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
			mSimAssociationQueryHandler = new SimAssociationQueryHandler(this, getContentResolver());
    }

    public void onConfigurationChanged(Configuration newConfig)
    { 
	super.onConfigurationChanged(newConfig);
    }

    private void startUpdateAnim() {
	
    }

    private void stopUpdateAnim() {
	
    }
    //@Override
    // modified by Ivan 2010-08-09
    public void onStart(){
    	super.onStart();
    }
    
    public void updateResult(){
	int resource[] = new int[]{R.layout.itemwithoutpic,R.layout.itemwithpic};
	long owner_id = null == PhoneOwner.getInstance() ? (long)-1 : PhoneOwner.getInstance().getOwnerID();
	Cursor rawc = getContentResolver().query(RawContacts.CONTENT_URI,
		RAW_CONTACTS_PROJECTION2,
		RawContacts.CONTACT_ID + "=" + owner_id, null, null);

	if (rawc != null) {
		ArrayList<String[]> auIDs = new ArrayList<String[]>();
		String[] item = null;
		String accountType;
		long rawContactId;
		while (rawc.moveToNext()) {
			accountType = rawc.getString(2);
			rawContactId = rawc.getLong(0);
			if (null != accountType
				&& ContactsManager.SNS_TYPE_LIST.contains(accountType.toLowerCase())) {
				item = new String[] { "", "" };
				item[0] = rawc.getString(4); // accountID
				item[1] = rawc.getString(5); // userID
				auIDs.add(item);
			}
		}
		if (null != rawc)
			rawc.close();
		if (auIDs.size() > 0) {
			Integer[] accountid = new Integer[auIDs.size()];
			// accountid[0] =1;
			String[] userId = new String[auIDs.size()];
			String[] temp;
			for (int i = 0; i < auIDs.size(); i++) {
				temp = auIDs.get(i);
				accountid[i] = Integer.parseInt(temp[0]);
				userId[i] = temp[1];
			}
				
			if(event == null)
			{
				event = new EventListManager(this,
						ContactOwnerActivity.this, R.id.sns_event_data, resource, 20);
				event.getSnsEvent(accountid, userId, true);
				event.show(true);
			}
		}
	}
    }

    public void updateLayout(){
	updateResult();
	long owner_id = null == PhoneOwner.getInstance() ? (long)-1 : PhoneOwner.getInstance().getOwnerID();
    	mOwner = ContactsManager.readStatusFromWSP(owner_id, mContext, RAW_CONTACTS_PROJECTION2);
    	
    	if(null != mOwner){
		if(null != mOwnerSns2) mOwnerSns2.setVisibility(View.GONE);
		if (mListEvent != null)
			mListEvent.setVisibility(View.VISIBLE);
		if (mListEvent != null && mListEvent.getEmptyView() != null){
			if(mListEvent.getCount() <= 0)
				mListEvent.getEmptyView().setVisibility(View.VISIBLE);
			else
				mListEvent.getEmptyView().setVisibility(View.GONE);
		}
	    	if(null != mContactHeaderWidget) mContactHeaderWidget.setSnsPadVisibility(View.VISIBLE);
	    	// TODO: set the sns info in the view panel
		if(null == mOwner.status || mOwner.status.length() == 0 || mOwner.status.equals("") || null == mOwner.snsUrl){
			if(null != mContactHeaderWidget) mContactHeaderWidget.setSnsPadVisibility(View.GONE);
		}
		else{
			if(null != mContactHeaderWidget) 
				mContactHeaderWidget.setStatusText(
						ContactsManager.parserEmotion(mOwner.status, mOwner.sns_id));
					
			Bitmap bitmap = null;
			if(KAIXIN_TYPE == mOwner.sns_id){
				bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo_kaixin);
			} else if (RENREN_TYPE == mOwner.sns_id) {
				bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo_renren);
			} else if (TWITTER_TYPE == mOwner.sns_id) {
				bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo_twitter);
			} else if (FLICKR_TYPE == mOwner.sns_id) {
				bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo_flickr);
			} else if (FACEBOOK_TYPE == mOwner.sns_id) {
				bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo_facebook);
			} else {
				// for other sns!
			}
			if(null != mContactHeaderWidget) mContactHeaderWidget.setSnsLog(bitmap);
		}
		if(null != mOwnerSns) mOwnerSns.setVisibility(View.GONE);
		if(null != mStatuEditor) mStatuEditor.setVisibility(View.VISIBLE);
            	if(null != mSnsPhoto) mSnsPhoto.setVisibility(View.VISIBLE);
            	if(null != mSnsUpdate) mSnsUpdate.setVisibility(View.VISIBLE);
    	}
    	else{
		if(null != mListEvent) mListEvent.setVisibility(View.GONE);
		if(null != mListEvent) mListEvent.getEmptyView().setVisibility(View.GONE);
    		if(null != mContactHeaderWidget) mContactHeaderWidget.setSnsPadVisibility(View.GONE);
    		if(null != mOwnerSns) mOwnerSns.setVisibility(View.VISIBLE);
    		if(null != mStatuEditor) mStatuEditor.setVisibility(View.GONE);
            	if(null != mSnsPhoto) mSnsPhoto.setVisibility(View.GONE);
            	if(null != mSnsUpdate) mSnsUpdate.setVisibility(View.GONE);
    	}
	if(null == PhoneOwner.getInstance() || -1 == PhoneOwner.getInstance().getOwnerID()){
		if(null != tabHost) tabHost.setVisibility(View.GONE);
		if(null != mEmptyView) mEmptyView.setVisibility(View.VISIBLE);
		if(null != mListView)
			mListView.setVisibility(View.GONE);
		if(null != mListView.getEmptyView())
			mListView.getEmptyView().setVisibility(View.GONE);
	}
	else{
		if(null != tabHost) tabHost.setVisibility(View.VISIBLE);
		if(null != mOwnerSns) mOwnerSns.setVisibility(View.GONE);
		if(null != mContactHeaderWidget) 
			mContactHeaderWidget.setDisplayName(PhoneOwner.getInstance().getName(), "");
		if(null != mEmptyView) mEmptyView.setVisibility(View.GONE);
		if(null != mListView) mListView.setVisibility(View.VISIBLE);
	}
    }

    // create by Ivan 2010-08-09
    /**
     * this method is click method to response to the click event
     */
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.sns_status:
			Log.i(TAG, "onClick, sns_status ...");
			startEditStatus();
			break;
		case R.id.sns_account_manager:
		case R.id.sns_account_manager_2:
			Log.i(TAG, "sns_account_manager ...");
			startAccountManager();
			break;
		case R.id.contact_account_manager:
			Log.i(TAG, "contact_account_manager ...");
            
			final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
			intent.putExtra("is_owner", true);
			intent.putExtra("new_owner_info", true);
            		startActivityForResult(intent, REQUEST_OWNER_CONTACT);
			break;

		case R.id.sns_photo:
			Log.i(TAG, "Upload photo click ...");
			uploadSnsFormCamera();
			break;
			
		case R.id.sns_update:
			Log.i(TAG, "Sns update image click ...");
			uploadSnsFormGallery();
			break;
			
		default:{
			//mStatuEditor.setFocusable(false);
		}
		
		}
	}
	
	// modified by Ivan & Huibin 2010-08-06
	public void uploadSnsFormGallery(){
		Log.i(TAG, "uploadSnsFromGallery ...");
		if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) 
		{
			Toast.makeText(this, R.string.no_sdcard_title, Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent();
		intent.setAction("com.mediatek.client.UPLOAD");
		intent.addCategory("android.intent.category.GALLERY");
		
		startActivity(intent);
		Log.i(TAG, "End uploadSnsFromGallery ...");
	}
	
	public void uploadSnsFormCamera(){
		Log.i(TAG, "uploadSnsFromCamera ...");
		if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) 
		{
			Toast.makeText(this, R.string.no_sdcard_title, Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent();
		intent.setAction("com.mediatek.client.UPLOAD");
		intent.addCategory("android.intent.category.CAMERA");
		
		startActivity(intent);
		Log.i(TAG, "End uploadSnsFromCamera ...");
	}
	
	// modified this one ...
	public void startEditStatus(){
		final Intent intent = new Intent("StatusInputActivity");
        startActivity(intent);
	}
	
	public void snsStatusUpdate(){
		
	}
	
	public void startInsertFaceDlg(){
		
	}
	
	public void startSettingDlg(){
		
	}
	
	public void doUpdateSnsStatus(){
		
	}
	// end modified
	
	// modified by Ivan 2010-08-16
	/**
	 * start Account manager, app from sunwei.
	 * start an other activity
	 */
	public void startAccountManager(){
		Log.i(TAG, "startAccountManager ...");
                final Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                intent.putExtra(AUTHORITIES_FILTER_KEY, new String[] {
                    ContactsContract.AUTHORITY
                });
                startActivityForResult(intent, REQUEST_OWNER_ACCOUNT);
        	Log.i(TAG, "End startAccountManager ...");
	}
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
	PhoneOwner owner = PhoneOwner.getInstance();
	long id = null == owner ? (long)-1 : owner.getOwnerID();
	String lookupkey = null == owner ? null : owner.getOwnerLookupKey();
	String name = null == owner ? null : owner.getName();

	outState.putString(ORIGINAL_CONTENT_ID, id + "");
	outState.putString(ORIGINAL_CONTENT_LOOKUPKEY, lookupkey);
	outState.putString(ORIGINAL_CONTENT_NAME, name);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startEntityQuery();
	updateLayout();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ContactsUtils.dispatchActivityOnPause();
        closeCursor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCursor();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CONFIRM_DELETE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.deleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, this)
                        .create();
            case DIALOG_CONFIRM_READONLY_DELETE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.readOnlyContactDeleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, this)
                        .create();
            case DIALOG_CONFIRM_MULTIPLE_DELETE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.multipleContactDeleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, this)
                        .create();
            case DIALOG_CONFIRM_READONLY_HIDE: {
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.readOnlyContactWarning)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, this)
                        .create();
            }

        }
        return null;
    }

    /** {@inheritDoc} */
    public void onQueryComplete(int token, Object cookie, final Cursor cursor) {
		final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
        if (token == TOKEN_STATUSES) {
            try {
                // Read available social rows and consider binding
                readStatuses(cursor);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            considerBindData();
            return;
        } else if (token == TOKEN_NORMAL) {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    indicate = cursor.getInt(0);
                    if (indicate >= RawContacts.INDICATE_SIM) {
                    	String result = SystemProperties.get("gsm.baseband.capability"); 
                        String result2 = SystemProperties.get("gsm.baseband.capability2");
                        Log.i(TAG,"result is " + result);
                        Log.i(TAG,"result2 is " + result2);
						if (null != mContactHeaderWidget)
							mContactHeaderWidget.showStar(false);
						int slotId = ContactsUtils.getSlotId(
								ContactOwnerActivity.this, (int)indicate);
						if (slotId >= 0) {
							try {
								if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
									if (slotId == com.android.internal.telephony.Phone.GEMINI_SIM_1) {
										if (!TextUtils.isEmpty(result) && Integer.valueOf(result) > 3) {
										if (null != iTel.getIccCardTypeGemini(slotId) && iTel.getIccCardTypeGemini(slotId)
												.equals("USIM")) {
											if (null != mContactHeaderWidget)
												mContactHeaderWidget
														.setPhoto(BitmapFactory
																.decodeResource(
																		this
																				.getResources(),
																		R.drawable.contact_icon_usim));
										} else {
											if (null != mContactHeaderWidget)
												mContactHeaderWidget
														.setPhoto(BitmapFactory
																.decodeResource(
																		this
																				.getResources(),
																		R.drawable.contact_icon_sim));
                    }
								} else {
											if (null != mContactHeaderWidget)
												mContactHeaderWidget
														.setPhoto(BitmapFactory
																.decodeResource(
																		this
																				.getResources(),
																		R.drawable.contact_icon_sim));
										}
									} else {
										if (!TextUtils.isEmpty(result2) && Integer.valueOf(result2) > 3) {
											if (null != iTel.getIccCardTypeGemini(slotId) && iTel.getIccCardTypeGemini(slotId)
													.equals("USIM")) {
										if (null != mContactHeaderWidget)
											mContactHeaderWidget
													.setPhoto(BitmapFactory
															.decodeResource(
																	this
																			.getResources(),
																	R.drawable.contact_icon_usim));
											} else {
												if (null != mContactHeaderWidget)
													mContactHeaderWidget
															.setPhoto(BitmapFactory
																	.decodeResource(
																			this
																					.getResources(),
																			R.drawable.contact_icon_sim));
	                    }
										} else {
											if (null != mContactHeaderWidget)
												mContactHeaderWidget
														.setPhoto(BitmapFactory
																.decodeResource(
																		this
																				.getResources(),
																		R.drawable.contact_icon_sim));
										}
									}

								} else {
									if (!TextUtils.isEmpty(result) && Integer.valueOf(result) > 3) {
										if (null != iTel.getIccCardTypeGemini(slotId) && iTel.getIccCardTypeGemini(slotId)
												.equals("USIM")) {
											if (null != mContactHeaderWidget)
												mContactHeaderWidget
														.setPhoto(BitmapFactory
																.decodeResource(
																		this
																				.getResources(),
																		R.drawable.contact_icon_usim));
										} else {
											if (null != mContactHeaderWidget)
												mContactHeaderWidget
														.setPhoto(BitmapFactory
																.decodeResource(
																		this
																				.getResources(),
																		R.drawable.contact_icon_sim));
                    }
									} else {
										if (null != mContactHeaderWidget)
											mContactHeaderWidget
													.setPhoto(BitmapFactory
															.decodeResource(
																	this
																			.getResources(),
																	R.drawable.contact_icon_sim));
									}
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
                }
                cursor.close();
            }

            return;
        }

        // One would think we could just iterate over the Cursor
        // directly here, as the result set should be small, and we've
        // already run the query in an AsyncTask, but a lot of ANRs
        // were being reported in this code nonetheless.  See bug
        // 2539603 for details.  The real bug which makes this result
        // set huge and CPU-heavy may be elsewhere.
        // TODO: if we keep this async, perhaps the entity iteration
        // should also be original AsyncTask, rather than ping-ponging
        // between threads like this.
        final ArrayList<Entity> oldEntities = mEntities;
        (new AsyncTask<Void, Void, ArrayList<Entity>>() {
            @Override
            protected ArrayList<Entity> doInBackground(Void... params) {
                ArrayList<Entity> newEntities = new ArrayList<Entity>(cursor.getCount());
                EntityIterator iterator = RawContacts.newEntityIterator(cursor);
                try {
                    while (iterator.hasNext()) {
                        Entity entity = iterator.next();
                        newEntities.add(entity);
                    }
                } finally {
                    iterator.close();
                }
                return newEntities;
            }

            @Override
            protected void onPostExecute(ArrayList<Entity> newEntities) {
                if (newEntities == null) {
                    // There was an error loading.
                    return;
                }
                synchronized (ContactOwnerActivity.this) {
                    if (mEntities != oldEntities) {
                        // Multiple async tasks were in flight and we
                        // lost the race.
                        return;
                    }
                    mEntities = newEntities;
                    mHasEntities = true;
                }
                considerBindData();
            }
        }).execute();
    }

    private long getRefreshedContactId() {
	
	if(mLookupUri == null)
	{return -1;}
        Uri freshContactUri = Contacts.lookupContact(getContentResolver(), mLookupUri);
        if (freshContactUri != null) {
            return ContentUris.parseId(freshContactUri);
        }
        return -1;
    }

    /**
     * Read from the given {@link Cursor} and build a set of {@link DataStatus}
     * objects to match any valid statuses found.
     */
    private synchronized void readStatuses(Cursor cursor) {
        mStatuses.clear();

        // Walk found statuses, creating internal row for each
        while (cursor.moveToNext()) {
            final DataStatus status = new DataStatus(cursor);
            final long dataId = cursor.getLong(StatusQuery._ID);
            mStatuses.put(dataId, status);
        }

        mHasStatuses = true;
    }

    private static Cursor setupContactCursor(ContentResolver resolver, Uri lookupUri) {
        if (lookupUri == null) {
            return null;
        }
        final List<String> segments = lookupUri.getPathSegments();
        if (segments.size() != 4) {
            return null;
        }

        // Contains an Id.
        final long uriContactId = Long.parseLong(segments.get(3));
        final String uriLookupKey = Uri.encode(segments.get(2));
        final Uri dataUri = Uri.withAppendedPath(
                ContentUris.withAppendedId(Contacts.CONTENT_URI, uriContactId),
                Contacts.Data.CONTENT_DIRECTORY);

        // This cursor has several purposes:
        // - Fetch NAME_RAW_CONTACT_ID and DISPLAY_NAME_SOURCE
        // - Fetch the lookup-key to ensure we are looking at the right record
        // - Watcher for change events
        Cursor cursor = resolver.query(dataUri,
                new String[] {
                    Contacts.NAME_RAW_CONTACT_ID,
                    Contacts.DISPLAY_NAME_SOURCE,
                    Contacts.LOOKUP_KEY
                }, null, null, null);

        if (null != cursor && cursor.moveToFirst()) {
            String lookupKey =
                    cursor.getString(cursor.getColumnIndex(Contacts.LOOKUP_KEY));
            if (!lookupKey.equals(uriLookupKey)) {
                // ID and lookup key do not match
                cursor.close();
                return null;
            }
            return cursor;
        } else {
            if(null != cursor) cursor.close();
            return null;
        }
    }

    private synchronized void startEntityQuery() {
        closeCursor();
        Uri uri = null;
        String oldPath = "";
        if(mLookupUri != null){
        	oldPath = mLookupUri.getPath();
	        // Interprete mLookupUri
	        mCursor = setupContactCursor(mResolver, mLookupUri);
	        // If mCursor is null now we did not succeed in using the Uri's Id (or it didn't contain
	        // a Uri). Instead we now have to use the lookup key to find the record
	        if (mCursor == null) {
	            mLookupUri = Contacts.getLookupUri(getContentResolver(), mLookupUri);
	            mCursor = setupContactCursor(mResolver, mLookupUri);
	        }
	
	        // If mCursor is still null, we were unsuccessful in finding the record
	        if (mCursor == null) {
	            mNameRawContactId = -1;
	            mDisplayNameSource = DisplayNameSources.UNDEFINED;
	            // TODO either figure out a way to prevent a flash of black background or
	            // use some other UI than a toast
	            //Toast.makeText(this, R.string.invalidContactMessage, Toast.LENGTH_SHORT).show();
	            Log.e(TAG, "invalid contact uri: " + mLookupUri);
	            finish();
	            return;
	        }
        if (mLookupUri != null) {
            uri = Contacts.lookupContact(getContentResolver(), mLookupUri);
        }
	        final long contactId = ContentUris.parseId(mLookupUri);
        boolean uriIsWrong = false;
        String newPath = "";
        if (mLookupUri != null) {
        	newPath = mLookupUri.getPath();
        	if (oldPath.contains("lookup") 
        			&& newPath.contains("lookup")
        			&& !newPath.equals(oldPath)) {
        		uriIsWrong = true;
        	}
        }

        if (mContactId != -1 && mContactId != contactId && uriIsWrong) {
        	Toast.makeText(this, R.string.invalidContactMessage, Toast.LENGTH_SHORT).show();
        	if (mCursor != null) mCursor.registerContentObserver(mObserver);  // mtk80909, CR: ALPS00134978
			finish();
			return;
		}
		mContactId = contactId;
	        mNameRawContactId =
	                mCursor.getLong(mCursor.getColumnIndex(Contacts.NAME_RAW_CONTACT_ID));
	        mDisplayNameSource =
	                mCursor.getInt(mCursor.getColumnIndex(Contacts.DISPLAY_NAME_SOURCE));
	
	        mCursor.registerContentObserver(mObserver);
	
	        // Clear flags and start queries to data and status
	        mHasEntities = false;
	        mHasStatuses = false;

        mHandler.startQuery(TOKEN_NORMAL, null, uri,
				new String[] { RawContacts.INDICATE_PHONE_SIM }, null, null,
				null);

	        mHandler.startQuery(TOKEN_ENTITIES, null, RawContactsEntity.CONTENT_URI, null,
	                RawContacts.CONTACT_ID + "=?", new String[] {
	                    String.valueOf(contactId)
	                }, null);
	        final Uri dataUri = Uri.withAppendedPath(
	                ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId),
	                Contacts.Data.CONTENT_DIRECTORY);
	        mHandler.startQuery(TOKEN_STATUSES, null, dataUri, StatusQuery.PROJECTION,
	                        StatusUpdates.PRESENCE + " IS NOT NULL OR " + StatusUpdates.STATUS
	                                + " IS NOT NULL", null, null);
	
	        mContactHeaderWidget.bindFromContactLookupUri(mLookupUri);
        }
    }

    private void closeCursor() {
        if (mCursor != null) {
            mCursor.unregisterContentObserver(mObserver);
            mCursor.close();
            mCursor = null;
        }
    }

    /**
     * Consider binding views after any of several background queries has
     * completed. We check internal flags and only bind when all data has
     * arrived.
     */
    private void considerBindData() {
        if (mHasEntities && mHasStatuses) {
            bindData();
        }
    }

    private void bindData() {

        // Build up the contact entries
        buildEntries();

        // Collapse similar data items in select sections.
        Collapser.collapseList(mPhoneEntries);
        Collapser.collapseList(mSmsEntries);
        Collapser.collapseList(mEmailEntries);
        Collapser.collapseList(mPostalEntries);
        Collapser.collapseList(mImEntries);

        if (mAdapter == null) {
            mAdapter = new ViewAdapter(this, mSections);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.setSections(mSections, SHOW_SEPARATORS);
        }
	mListView.setEmptyView(mNoContactInfo);
/*
        mListView.setEmptyView(mEmptyView);
	if(PhoneOwner.getInstance() != null && PhoneOwner.getInstance().getOwnerID() != -1){
		mListView.setEmptyView(mNoContactInfo);
	}
*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
	if(null == PhoneOwner.getInstance())
		return false;
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_owner, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
	if(null == PhoneOwner.getInstance())
		return true;
        // Only allow edit when we have at least one raw_contact id
        final boolean hasRawContact = (null == mRawContactIds ? false : mRawContactIds.size() > 0);
        menu.findItem(R.id.menu_edit).setEnabled(hasRawContact);

        // Only allow share when unrestricted contacts available
        menu.findItem(R.id.menu_share).setEnabled(!mAllRestricted && hasRawContact);
	
	// Only allow share when unrestricted contacts available
        menu.findItem(R.id.menu_delete).setEnabled(!mAllRestricted && hasRawContact);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
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

        ViewEntry entry = ContactEntryAdapter.getEntry(mSections, info.position, SHOW_SEPARATORS);
	if(null == entry) return;
        menu.setHeaderTitle(R.string.contactOptionsTitle);
        boolean isVTIdle = true;
        boolean simIdle = true;
        boolean sim1Idle = true;
        boolean sim2Idle = true;
        ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        if (null == iTel) {
            return;
        }
        if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {  // for VT IT, last code
        	try {
            isVTIdle = iTel.isVTIdle();
          } catch (RemoteException ex) {
                    ex.printStackTrace();
          }
        }

        if (entry.mimetype.equals(CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {

            if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {  // for VT IT, last code
                menu.add(0, MENU_ITEM_CALL, 0, R.string.menu_call).setEnabled(isVTIdle);
            } else {
                menu.add(0, MENU_ITEM_CALL, 0, R.string.menu_call);
            }

            menu.add(0, 0, 0, R.string.menu_sendSMS).setIntent(entry.secondaryIntent);
            if (!entry.isPrimary) {
                menu.add(0, MENU_ITEM_MAKE_DEFAULT, 0, R.string.menu_makeDefaultNumber);
            }
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                try {
                    sim1Idle = iTel.isIdleGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1);
                    sim2Idle = iTel.isIdleGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2);
                    Log.d(TAG, "sim1 sim1Idle on " + sim1Idle);
                    Log.d(TAG, "sim2 sim2Idle on " + sim2Idle);
    
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }  
                if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {  // for VT IT, last code
                    menu.add(0, MENU_ITEM_VTCALL, 0, getResources().getString(R.string.call_via_vt))
                        .setEnabled(isVTIdle && sim1Idle && sim2Idle); 
                }
            } else {
                try {
                    simIdle = iTel.isIdle();
                    Log.d(TAG, "sim1 simIdle on " + simIdle);
    
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }  
                if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {  // for VT IT, last code
                    menu.add(0, MENU_ITEM_VTCALL, 0, getResources().getString(R.string.call_via_vt))
                        .setEnabled(isVTIdle && simIdle); 
                }
                
            }
            List<SimInfo> simList = Constants.getInsertedSimList(this);
//            if(simList != null && simList.size() > 0){
            	if(entry.isAssociation)
            		menu.add(0, MENU_ITEM_REMOVE_ASSOCIATION, 0, R.string.menu_remove_association);            	
            	else {
            		MenuItem item = menu.add(0, MENU_ITEM_ASSOCIATION, 0, R.string.menu_association);
					if(simList != null && simList.size() > 0)
            			item.setEnabled(true);
					else item.setEnabled(false);
            	}
//            }
            menu.add(0, MENU_ITEM_REMOVE_NUMBER, 0, R.string.menu_remove_number);//gemini enhancement
        } else if (entry.mimetype.equals(CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
            menu.add(0, MENU_ITEM_EMAIL, 0, R.string.menu_sendEmail);
            if (!entry.isPrimary) {
                menu.add(0, MENU_ITEM_MAKE_DEFAULT, 0, R.string.menu_makeDefaultEmail);
            }
        } else if (entry.mimetype.equals(CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)) {
            menu.add(0, MENU_ITEM_POSTAL, 0, R.string.menu_viewAddress);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit: {
                Long rawContactIdToEdit = null;

                if (mRawContactIds.size() > 0) {
                    rawContactIdToEdit = mRawContactIds.get(0);
                } else {
                    // There is no rawContact to edit.
                    break;
                }
                Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
                        rawContactIdToEdit);
                startActivityForResult(new Intent(Intent.ACTION_EDIT, rawContactUri),
                        REQUEST_OWNER_CONTACT);
                break;
            }
	    case R.id.menu_delete: {
                // Get confirmation
                if (mReadOnlySourcesCnt > 0 & mWritableSourcesCnt > 0) {
                    showDialog(DIALOG_CONFIRM_READONLY_DELETE);
                } else if (mReadOnlySourcesCnt > 0 && mWritableSourcesCnt == 0) {
                    showDialog(DIALOG_CONFIRM_READONLY_HIDE);
                } else if (mReadOnlySourcesCnt == 0 && mWritableSourcesCnt > 1) {
                    showDialog(DIALOG_CONFIRM_MULTIPLE_DELETE);
                } else {
                    showDialog(DIALOG_CONFIRM_DELETE);
                }
                return true;
            }
            case R.id.menu_join: {
                showJoinAggregateActivity();
                return true;
            }
            case R.id.menu_options: {
                showOptionsActivity();
                return true;
            }
            case R.id.menu_share: {
                if (mAllRestricted) return false;

		if(mLookupUri == null)
		{
			return true;
		}

                // TODO: Keep around actual LOOKUP_KEY, or formalize method of extracting
                final String lookupKey = Uri.encode(mLookupUri.getPathSegments().get(2));
                final Uri shareUri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, lookupKey);

                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(Contacts.CONTENT_VCARD_TYPE);
                intent.putExtra(Intent.EXTRA_STREAM, shareUri);

                // Launch chooser to share contact via
                final CharSequence chooseTitle = getText(R.string.share_via);
                final Intent chooseIntent = Intent.createChooser(intent, chooseTitle);

                try {
                    startActivity(chooseIntent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(this, R.string.share_error, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_MAKE_DEFAULT: {
                if (makeItemDefault(item)) {
                    return true;
                }
                break;
            }
            case MENU_ITEM_CALL: {
                StickyTabs.saveTab(this, getIntent());
                ViewEntry entry = getViewEntryForMenuItem(item);
                if (entry == null) {
                    return false;
                }
			Uri uri = Uri.withAppendedPath(
					CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri
							.encode(entry.data));
			mSimAssociationQueryHandler.startQuery(0, entry.data, uri,
					new String[] { "sim_id" }, null, null, null);
                break;
            }
            case MENU_ITEM_VTCALL: {
                StickyTabs.saveTab(this, getIntent());
                ViewEntry entry = getViewEntryForMenuItem(item);
                if (entry == null) {
                    return false;
                }
                Intent intent = entry.intent;
                if (intent != null) {
                    try {
                        if ((true == FeatureOption.MTK_VT3G324M_SUPPORT) && intent.getAction().equals(Intent.ACTION_CALL_PRIVILEGED)) {
                            //ContactsUtils.initiateCallWithSim(this, entry.data);
                            intent.putExtra("is_vt_call", true);
                            //ContactsUtils.enterDialer(this, entry.data);
                            startActivity(intent);
                           
                        } else {
                           startActivity(intent);
                        }
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "No activity found for intent: " + intent);
                        // Added by delong.liu@archermind.com
                        Toast.makeText(this, R.string.activity_not_found_error, Toast.LENGTH_SHORT).show();
                        // Added by delong.liu@archermind.com end
                        signalError();
                    }
                } else {
                    signalError();
                }
                break;
            }

            case MENU_ITEM_ASSOCIATION:
            	if (indicate >= RawContacts.INDICATE_SIM){
    				ViewEntry v = mPhoneEntries.get(0);
            		String title="";
            		Uri u = Uri.withAppendedPath(RawContacts.CONTENT_URI, String.valueOf(v.contactId));
    				Cursor c2 = getContentResolver().query(u, new String[]{"display_name"}, null, null, null);
    				if(c2 != null&& c2.moveToNext()){
    					title = c2.getString(0);
    					c2.close();
    				}
            		new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setIcon(android.R.drawable.ic_menu_more)
                    .setMessage(R.string.warning_detail)
                    .setPositiveButton(android.R.string.ok, mImportListener)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(true)
                    .create()
                    .show();
            	}else{
            		ViewEntry entry2 = getViewEntryForMenuItem(item);
            		if(entry2 == null)return false;
            		Intent i = new Intent(this,AssociationSimActivity.class);
            		i.setData(mLookupUri);
            		i.putExtra("data_id", entry2.id);
            		startActivity(i);
            	}
            	break;

            case MENU_ITEM_REMOVE_ASSOCIATION:
		final ViewEntry entry2 = getViewEntryForMenuItem(item);
            	new AlertDialog.Builder(this)
                .setTitle(R.string.remove_number_title)
                .setIcon(android.R.drawable.ic_menu_more)
                .setMessage(R.string.remove_association_message)
                .setPositiveButton(R.string.remove_number_title, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
            	ContentValues values = new ContentValues();
        		values.put(Data.SIM_ID, -1);
        		mHandler.startUpdate(0, null, Data.CONTENT_URI, values,
        				RawContacts.Data._ID + "=? ", new String[] { String
        						.valueOf(entry2.id) });
					}
                	
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)               
                .create()
                .show();            	
        		break;
        		
            case MENU_ITEM_REMOVE_NUMBER: {//gemini enhancement
            	ViewEntry entry = getViewEntryForMenuItem(item);
            	if (entry == null) {
            		return false;
              }
            	mDeleteNumber = entry.data;
            	mDeleteDataId = entry.id;
            	Log.i(TAG,"mDeleteNumber IS " + mDeleteNumber);
            	new AlertDialog.Builder(this)
                .setTitle(R.string.remove_number_title)
                .setIcon(android.R.drawable.ic_menu_more)
                .setMessage(ContactOwnerActivity.this.getString(R.string.confirm_remove_number_dialog, mDeleteNumber))
                .setPositiveButton(android.R.string.ok, mRemoveNumberListener)
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(true)               
                .create()
                .show();
            	break;
            }
                 
            case MENU_ITEM_EMAIL:
            case MENU_ITEM_POSTAL: {
                ViewEntry entry = getViewEntryForMenuItem(item);
                if (entry == null) {
                    return false;
                }
                Intent intent = entry.intent;
                if (intent != null) {
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "No activity found for intent: " + intent);
                        if (item.getItemId() == MENU_ITEM_EMAIL) {
                            Toast.makeText(this, R.string.email_error, Toast.LENGTH_SHORT).show();
                        } else if (item.getItemId() == MENU_ITEM_POSTAL) {
                            Toast.makeText(this, R.string.map_error, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.activity_not_found_error, Toast.LENGTH_SHORT).show();
                        }
                        
                        signalError();
                    }
                } else {
                    signalError();
                }
                break;
            }
        }
        return super.onContextItemSelected(item);
    }

    private boolean makeItemDefault(MenuItem item) {
        ViewEntry entry = getViewEntryForMenuItem(item);
        if (entry == null) {
            return false;
        }

        // Update the primary values in the data record.
        ContentValues values = new ContentValues(1);
        values.put(Data.IS_SUPER_PRIMARY, 1);
        getContentResolver().update(ContentUris.withAppendedId(Data.CONTENT_URI, entry.id),
                values, null, null);
        startEntityQuery();
        return true;
    }

    /**
     * Shows a list of aggregates that can be joined into the currently viewed aggregate.
     */
    public void showJoinAggregateActivity() {
        long freshId = getRefreshedContactId();
        if (freshId > 0) {
            String displayName = null;
            if (mCursor.moveToFirst()) {
                displayName = mCursor.getString(0);
            }
            Intent intent = new Intent(ContactsListActivity.JOIN_AGGREGATE);
            intent.putExtra(ContactsListActivity.EXTRA_AGGREGATE_ID, freshId);
            if (displayName != null) {
                intent.putExtra(ContactsListActivity.EXTRA_AGGREGATE_NAME, displayName);
            }
            startActivityForResult(intent, REQUEST_JOIN_CONTACT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_JOIN_CONTACT) {
            if (resultCode == RESULT_OK && intent != null) {
                final long contactId = ContentUris.parseId(intent.getData());
                joinAggregate(contactId);
            }
        } else if (requestCode == REQUEST_EDIT_CONTACT) {
            if (resultCode == EditContactActivity.RESULT_CLOSE_VIEW_ACTIVITY) {
                finish();
            } else if (resultCode == Activity.RESULT_OK) {
                mLookupUri = intent.getData();
                if (mLookupUri == null) {
                    finish();
                }
            }
        } else if (requestCode == REQUEST_OWNER_CONTACT) { // modified by Ivan 2010-07-23
        	if (resultCode == EditContactActivity.RESULT_CLOSE_VIEW_ACTIVITY) {
                finish();
            } else if (resultCode == Activity.RESULT_OK) {
            	long contactId = ContentUris.parseId(intent.getData());
            	String contactName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
                mLookupUri = intent.getData();
                //ContactsListActivity.ownerID = contactId;
                String lookupKey = mLookupUri.getPathSegments().get(2);
                //ContactsListActivity.ownerLookupKey = lookupKey;
                PhoneOwner.initPhoneOwner(contactId, lookupKey);
                PhoneOwner owner = PhoneOwner.getInstance();
		if(null == contactName || contactName.length() == 0)
			contactName = this.getText(android.R.string.unknownName) + "";
                if(null != owner) owner.setName(contactName);
                if (mLookupUri == null) {
                    finish();
                }
            }
        } else if (requestCode == REQUEST_OWNER_ACCOUNT) {
		mOwnerLoader.loadOwnerHeader(mContactHeaderWidget, mOwnerSns, 
			mSnsUpdate, mSnsPhoto, mStatuEditor, PhoneOwner.getInstance(),
			mUpdateLayoutHandler);
	}
    }

    private void joinAggregate(final long contactId) {
        Cursor c = mResolver.query(RawContacts.CONTENT_URI, new String[] {RawContacts._ID},
                RawContacts.CONTACT_ID + "=" + contactId, null, null);

        try {
	    if(null != c) {
		    while(c.moveToNext()) {
		        long rawContactId = c.getLong(0);
		        setAggregationException(rawContactId, AggregationExceptions.TYPE_KEEP_TOGETHER);
		    }
	    }
        } finally {
            if(null != c) c.close();
        }

        Toast.makeText(this, R.string.contactsJoinedMessage, Toast.LENGTH_LONG).show();
        startEntityQuery();
    }

    /**
     * Given a contact ID sets an aggregation exception to either join the contact with the
     * current aggregate or split off.
     */
    protected void setAggregationException(long rawContactId, int exceptionType) {
        ContentValues values = new ContentValues(3);
        for (long aRawContactId : mRawContactIds) {
            if (aRawContactId != rawContactId) {
                values.put(AggregationExceptions.RAW_CONTACT_ID1, aRawContactId);
                values.put(AggregationExceptions.RAW_CONTACT_ID2, rawContactId);
                values.put(AggregationExceptions.TYPE, exceptionType);
                mResolver.update(AggregationExceptions.CONTENT_URI, values, null, null);
            }
        }
    }

    private void showOptionsActivity() {
        final Intent intent = new Intent(this, ContactOptionsActivity.class);
	if(mLookupUri == null)
	{	
	    return;
	}
        intent.setData(mLookupUri);
        startActivity(intent);
    }

    private ViewEntry getViewEntryForMenuItem(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return null;
        }

        return ContactEntryAdapter.getEntry(mSections, info.position, SHOW_SEPARATORS);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                try {
                    ITelephony phone = ITelephony.Stub.asInterface(
                            ServiceManager.checkService("phone"));
                    if (phone != null && !phone.isIdle()) {
                        // Skip out and let the key be handled at a higher level
                        break;
                    }
                } catch (RemoteException re) {
                    // Fall through and try to call the contact
                }

                int index = mListView.getSelectedItemPosition();
                if (index != -1) {
                    ViewEntry entry = ViewAdapter.getEntry(mSections, index, SHOW_SEPARATORS);
                    if (entry != null && entry.intent != null &&
                            entry.intent.getAction() == Intent.ACTION_CALL_PRIVILEGED) {
                        if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        		Uri uri = Uri.withAppendedPath(
                    					CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(entry.data));                              
                                mSimAssociationQueryHandler.startQuery(0, entry.data, uri,
                    					new String[] { "sim_id" }, null, null, null);
                        } else {
                        startActivity(entry.intent);
						}
                        StickyTabs.saveTab(this, getIntent());
                        return true;
                    }
                } else if (mPrimaryPhoneUri != null) {
                    // There isn't anything selected, call the default number
                    final Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                            mPrimaryPhoneUri);
                    startActivity(intent);
                    StickyTabs.saveTab(this, getIntent());
                    return true;
                }
                return false;
            }

            case KeyEvent.KEYCODE_DEL: {
                if (mReadOnlySourcesCnt > 0 & mWritableSourcesCnt > 0) {
                    showDialog(DIALOG_CONFIRM_READONLY_DELETE);
                } else if (mReadOnlySourcesCnt > 0 && mWritableSourcesCnt == 0) {
                    showDialog(DIALOG_CONFIRM_READONLY_HIDE);
                } else if (mReadOnlySourcesCnt == 0 && mWritableSourcesCnt > 1) {
                    showDialog(DIALOG_CONFIRM_MULTIPLE_DELETE);
                } else {
                    showDialog(DIALOG_CONFIRM_DELETE);
                }
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onItemClick(AdapterView parent, View v, int position, long id) {
        ViewEntry entry = ViewAdapter.getEntry(mSections, position, SHOW_SEPARATORS);
        if (entry != null) {
            Intent intent = entry.intent;
            if (intent != null) {
                boolean isDialCall = false;
                if (Intent.ACTION_CALL_PRIVILEGED.equals(intent.getAction())) {
                    isDialCall = true;
                    StickyTabs.saveTab(this, getIntent());
                    
                }
                Log.d(TAG, "onItemClick, isDialCall= "+isDialCall);
                try {
                    boolean isSipCall = intent.getBooleanExtra("sipcall", false);
                    Log.d(TAG, "onClick, isSipCall= "+isSipCall);
                   if (isSipCall && isDialCall) {
                     startActivity(intent);
                     return;
                    }

                    //mtk80736 support dual sim
                    if (FeatureOption.MTK_GEMINI_SUPPORT && isDialCall) {
        		Uri uri = Uri.withAppendedPath(CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(entry.data));
        		mSimAssociationQueryHandler.startQuery(0, entry.data, uri, new String[]{"sim_id"}, null, null, null);
                    } else {
                        startActivity(intent);
                    }
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "No activity found for intent: " + intent);
                    signalError();
                }
            } else {
                signalError();
            }
        } else {
            signalError();
        }
    }

    /**
     * Signal an error to the user via a beep, or some other method.
     */
    private void signalError() {
        //TODO: implement this when we have the sonification APIs
    }

    /**
     * Build up the entries to display on the screen.
     *
     * @param personCursor the URI for the contact being displayed
     */
    private final void buildEntries() {
        // Clear out the old entries
        final int numSections = mSections.size();
        int tempSimIdForAssociation = -1;
        for (int i = 0; i < numSections; i++) {
            mSections.get(i).clear();
        }

        mRawContactIds.clear();

        mReadOnlySourcesCnt = 0;
        mWritableSourcesCnt = 0;
        mAllRestricted = true;
        mPrimaryPhoneUri = null;

        mAssociationPhoneUri = null;//gemini enhancement

        mWritableRawContactIds.clear();

        final Context context = this;
        final Sources sources = Sources.getInstance(context);
	if(null == sources) return;

        // Build up method entries
        if (mLookupUri != null) {
            for (Entity entity: mEntities) {
                final ContentValues entValues = entity.getEntityValues();
		if(null == entValues) continue;
                final String accountType = entValues.getAsString(RawContacts.ACCOUNT_TYPE);
                final long rawContactId = entValues.getAsLong(RawContacts._ID);

                // Mark when this contact has any unrestricted components
                final boolean isRestricted = entValues.getAsInteger(RawContacts.IS_RESTRICTED) != 0;
                if (!isRestricted) mAllRestricted = false;

                if (!mRawContactIds.contains(rawContactId)) {
                    mRawContactIds.add(rawContactId);
                }
                ContactsSource contactsSource = sources.getInflatedSource(accountType,
                        ContactsSource.LEVEL_SUMMARY);
                if (contactsSource != null && contactsSource.readOnly) {
                    mReadOnlySourcesCnt += 1;
                } else {
                    mWritableSourcesCnt += 1;
                    mWritableRawContactIds.add(rawContactId);
                }


                for (NamedContentValues subValue : entity.getSubValues()) {
                    final ContentValues entryValues = subValue.values;
		    if(null == entryValues) continue;
                    entryValues.put(Data.RAW_CONTACT_ID, rawContactId);

                    final long dataId = entryValues.getAsLong(Data._ID);
                    final String mimeType = entryValues.getAsString(Data.MIMETYPE);
                    if (mimeType == null) continue;

                    final DataKind kind = sources.getKindOrFallback(accountType, mimeType, this,
                            ContactsSource.LEVEL_MIMETYPES);
                    if (kind == null) continue;

                    final ViewEntry entry = ViewEntry.fromValues(context, mimeType, kind,
                            rawContactId, dataId, entryValues);
		    if(null == entry) continue;
                    final boolean hasData = !TextUtils.isEmpty(entry.data);
                    final boolean isSuperPrimary = entryValues.getAsInteger(Data.IS_SUPER_PRIMARY) != 0;
                  //gemini enhancement
                    Log.i(TAG,"entryValues is " + entryValues);
                    tempSimIdForAssociation = entryValues.getAsInteger(Data.SIM_ID);
                    
                    final boolean isAssociation = tempSimIdForAssociation > 0;
                    
                    Log.i(TAG,"tempSimIdForAssociation is " + tempSimIdForAssociation);
                    Log.i(TAG,"isAssociation is " + isAssociation);           

                    if (Phone.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build phone entries
                        mNumPhoneNumbers++;

                        entry.intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                                Uri.fromParts(Constants.SCHEME_TEL, entry.data, null));
                        entry.secondaryIntent = new Intent(Intent.ACTION_SENDTO,
                                Uri.fromParts(Constants.SCHEME_SMSTO, entry.data, null));

                        // Remember super-primary phone
                        if (isSuperPrimary) mPrimaryPhoneUri = entry.uri;
                        Log.i(TAG,"isSuperPrimary entry.uri is " + entry.uri);

                        entry.isPrimary = isSuperPrimary;

                        entry.simId = tempSimIdForAssociation;
                        entry.isAssociation = isAssociation;//gemini enhancement
                        mPhoneEntries.add(entry);

                        if (entry.type == CommonDataKinds.Phone.TYPE_MOBILE
                                || mShowSmsLinksForAllPhones) {
                            // Add an SMS entry
                            if (kind.iconAltRes > 0) {
                                entry.secondaryActionIcon = kind.iconAltRes;
                            }
                        }
                    } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build email entries
                        entry.intent = new Intent(Intent.ACTION_SENDTO,
                                Uri.fromParts(Constants.SCHEME_MAILTO, entry.data, null));
                        entry.isPrimary = isSuperPrimary;
                        mEmailEntries.add(entry);

                        // When Email rows have status, create additional Im row
                        final DataStatus status = mStatuses.get(entry.id);
                        if (status != null) {
                            final String imMime = Im.CONTENT_ITEM_TYPE;
                            final DataKind imKind = sources.getKindOrFallback(accountType,
                                    imMime, this, ContactsSource.LEVEL_MIMETYPES);
                            final ViewEntry imEntry = ViewEntry.fromValues(context,
                                    imMime, imKind, rawContactId, dataId, entryValues);
                            imEntry.intent = ContactsUtils.buildImIntent(entryValues);
                            imEntry.applyStatus(status, false);
                            mImEntries.add(imEntry);
                        }
                    } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build postal entries
                        entry.maxLines = 4;
                        entry.intent = new Intent(Intent.ACTION_VIEW, entry.uri);
                        mPostalEntries.add(entry);
                    } else if (Im.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build IM entries
                        entry.intent = ContactsUtils.buildImIntent(entryValues);
                        if (TextUtils.isEmpty(entry.label)) {
                            entry.label = getString(R.string.chat).toLowerCase();
                        }

                        // Apply presence and status details when available
                        final DataStatus status = mStatuses.get(entry.id);
                        if (status != null) {
                            entry.applyStatus(status, false);
                        }
                        mImEntries.add(entry);
                    } else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType) &&
                            (hasData || !TextUtils.isEmpty(entry.label))) {
                        // Build organization entries
                        final boolean isNameRawContact = (mNameRawContactId == rawContactId);

                        final boolean duplicatesTitle =
                            isNameRawContact
                            && mDisplayNameSource == DisplayNameSources.ORGANIZATION
                            && (!hasData || TextUtils.isEmpty(entry.label));

                        if (!duplicatesTitle) {
                            entry.uri = null;

                            if (TextUtils.isEmpty(entry.label)) {
                                entry.label = entry.data;
                                entry.data = "";
                            }

                            mOrganizationEntries.add(entry);
                        }
                    } else if (Nickname.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build nickname entries
                        final boolean isNameRawContact = (mNameRawContactId == rawContactId);

                        final boolean duplicatesTitle =
                            isNameRawContact
                            && mDisplayNameSource == DisplayNameSources.NICKNAME;

                        if (!duplicatesTitle) {
                            entry.uri = null;
                            mNicknameEntries.add(entry);
                        }
                    } else if (Note.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build note entries
                        entry.uri = null;
                        entry.maxLines = 100;
                        mOtherEntries.add(entry);
                    } else if (Website.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build note entries
                        entry.uri = null;
                        entry.maxLines = 10;
                        try {
                            WebAddress webAddress = new WebAddress(entry.data);
                            entry.intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(webAddress.toString()));
                        } catch (ParseException e) {
                            Log.e(TAG, "Couldn't parse website: " + entry.data);
                        }
                        mOtherEntries.add(entry);
                    } else if (SipAddress.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        // Build SipAddress entries
                        entry.uri = null;
                        entry.maxLines = 1;
                        entry.intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                                Uri.fromParts(Constants.SCHEME_SIP, entry.data, null));
                        //MTK added for SIP call
                        entry.intent.putExtra("sipcall", true);
                        mOtherEntries.add(entry);
                        // TODO: Consider moving the SipAddress into its own
                        // section (rather than lumping it in with mOtherEntries)
                        // so that we can reposition it right under the phone number.
                        // (Then, we'd also update FallbackSource.java to set
                        // secondary=false for this field, and tweak the weight
                        // of its DataKind.)
                    } else if (CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        int count = mGroupEntries.size();
                        if (count == 0) {
                            entry.uri = null;
                            entry.maxLines = 1;
                            entry.intent = null;
                            entry.data = ContactsUtils.getGroupsName(this, entry.data);
                            mGroupEntries.add(entry);
                            continue;
                        }
                        ViewEntry old = mGroupEntries.get(0);
                        if (old.data != null) {
                            StringBuilder newData = new StringBuilder(ContactsUtils.getGroupsName(this, old.data));
                            newData.append(",");
                            newData.append(ContactsUtils.getGroupsName(this, entry.data));
                            old.data = newData.toString();
                        }
                    } else {
                        // Handle showing custom rows
                        if (true == FeatureOption.MTK_SNS_SUPPORT) {
	                    		if(!entry.mimetype.toLowerCase().equals("vnd.android.cursor.item/sns.flickr")) {
	                    			entry.intent = new Intent(Intent.ACTION_VIEW, entry.uri);
	                    		} else {
	                    			entry.intent = null;
	                    		}
                    		} else {
                    			entry.intent = new Intent(Intent.ACTION_VIEW, entry.uri);
                    		}

                        // Use social summary when requested by external source
                        final DataStatus status = mStatuses.get(entry.id);
                        final boolean hasSocial = kind.actionBodySocial && status != null;
                        if (hasSocial) {
                            entry.applyStatus(status, true);
                        }

                        if (hasSocial || hasData) {
                            mOtherEntries.add(entry);
                        }
                    }
                }
            }
        }
    }

    static String buildActionString(DataKind kind, ContentValues values, boolean lowerCase,
            Context context) {
        if (kind.actionHeader == null) {
            return null;
        }
        CharSequence actionHeader = kind.actionHeader.inflateUsing(context, values);
        if (actionHeader == null) {
            return null;
        }
        return lowerCase ? actionHeader.toString().toLowerCase() : actionHeader.toString();
    }

    static String buildDataString(DataKind kind, ContentValues values, Context context) {
        if (kind.actionBody == null) {
            return null;
        }
        CharSequence actionBody = kind.actionBody.inflateUsing(context, values);
        return actionBody == null ? null : actionBody.toString();
    }

    /**
     * A basic structure with the data for a contact entry in the list.
     */
    static class ViewEntry extends ContactEntryAdapter.Entry implements Collapsible<ViewEntry> {
        public Context context = null;
        public String resPackageName = null;
        public int actionIcon = -1;
        public boolean isPrimary = false;

        String[] associationIcon = new String[20];//gemini enhancement
        String[] numberForAssociation = new String[20];
        public int secondaryActionIcon = -1;
        public Intent intent;
        public Intent secondaryIntent = null;
        public int maxLabelLines = 1;
        public ArrayList<Long> ids = new ArrayList<Long>();
        public int collapseCount = 0;

        public int presence = -1;

        public CharSequence footerLine = null;

        public int simId;
        
        private ViewEntry() {
        }

        /**
         * Build new {@link ViewEntry} and populate from the given values.
         */
        public static ViewEntry fromValues(Context context, String mimeType, DataKind kind,
                long rawContactId, long dataId, ContentValues values) {
            final ViewEntry entry = new ViewEntry();
            entry.context = context;
            entry.contactId = rawContactId;
            entry.id = dataId;
            entry.uri = ContentUris.withAppendedId(Data.CONTENT_URI, entry.id);
            entry.mimetype = mimeType;
            entry.label = buildActionString(kind, values, false, context);
            entry.data = buildDataString(kind, values, context);

            if (kind != null && kind.typeColumn != null && values != null && values.containsKey(kind.typeColumn)) {
                entry.type = values.getAsInteger(kind.typeColumn);
            }
            if (kind.iconRes > 0) {
                entry.resPackageName = kind.resPackageName;
                entry.actionIcon = kind.iconRes;
            }

            return entry;
        }

        /**
         * Apply given {@link DataStatus} values over this {@link ViewEntry}
         *
         * @param fillData When true, the given status replaces {@link #data}
         *            and {@link #footerLine}. Otherwise only {@link #presence}
         *            is updated.
         */
        public ViewEntry applyStatus(DataStatus status, boolean fillData) {
            presence = status.getPresence();
            if (fillData && status.isValid()) {
                this.data = status.getStatus().toString();
                this.footerLine = status.getTimestampLabel(context);
            }

            return this;
        }

        public boolean collapseWith(ViewEntry entry) {
            // assert equal collapse keys
            if (!shouldCollapseWith(entry)) {
                return false;
            }

            // Choose the label associated with the highest type precedence.
            if (TypePrecedence.getTypePrecedence(mimetype, type)
                    > TypePrecedence.getTypePrecedence(entry.mimetype, entry.type)) {
                type = entry.type;
                label = entry.label;
            }

            // Choose the max of the maxLines and maxLabelLines values.
            maxLines = Math.max(maxLines, entry.maxLines);
            maxLabelLines = Math.max(maxLabelLines, entry.maxLabelLines);

            // Choose the presence with the highest precedence.
            if (StatusUpdates.getPresencePrecedence(presence)
                    < StatusUpdates.getPresencePrecedence(entry.presence)) {
                presence = entry.presence;
            }

            // If any of the collapsed entries are primary make the whole thing primary.
            isPrimary = entry.isPrimary ? true : isPrimary;

            // uri, and contactdId, shouldn't make a difference. Just keep the original.

            // Keep track of all the ids that have been collapsed with this one.
            ids.add(entry.id);
            collapseCount++;
            return true;
        }

        public boolean shouldCollapseWith(ViewEntry entry) {
            if (entry == null) {
                return false;
            }

            if (!ContactsUtils.shouldCollapse(context, mimetype, data, entry.mimetype,
                    entry.data)) {
                return false;
            }

            if (!TextUtils.equals(mimetype, entry.mimetype)
                    || !ContactsUtils.areIntentActionEqual(intent, entry.intent)
                    || !ContactsUtils.areIntentActionEqual(secondaryIntent, entry.secondaryIntent)
                    || actionIcon != entry.actionIcon) {
                return false;
            }

            return true;
        }
    }

    /** Cache of the children views of a row */
    static class ViewCache {
        public TextView label;
        public TextView data;
        public TextView dataForAssociationSim;
        public TextView footer;
        public ImageView actionIcon;
        public ImageView presenceIcon;
        public ImageView primaryIcon;
        public ImageView associationIcon;//gemini enhancement
        public DontPressWithParentImageView secondaryActionButton;
        public View secondaryActionDivider;

        // Need to keep track of this too
        ViewEntry entry;
    }

    private final class ViewAdapter extends ContactEntryAdapter<ViewEntry>
            implements View.OnClickListener {


        ViewAdapter(Context context, ArrayList<ArrayList<ViewEntry>> sections) {
            super(context, sections, SHOW_SEPARATORS);
        }

        public void onClick(View v) {
            Intent intent = (Intent) v.getTag();
            startActivity(intent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewEntry entry = getEntry(mSections, position, false);
            View v;

            ViewCache views;

            // Check to see if we can reuse convertView
            if (convertView != null) {
                v = convertView;
                views = (ViewCache) v.getTag();
            } else {
                // Create a new view if needed
                v = mInflater.inflate(R.layout.list_item_text_icons, parent, false);

                // Cache the children
                views = new ViewCache();
                views.label = (TextView) v.findViewById(android.R.id.text1);
                views.data = (TextView) v.findViewById(android.R.id.text2);
                views.dataForAssociationSim = (TextView) v.findViewById(R.id.text3);
                views.associationIcon = (ImageView) v.findViewById(R.id.association_icon);
                views.footer = (TextView) v.findViewById(R.id.footer);
                views.actionIcon = (ImageView) v.findViewById(R.id.action_icon);
                views.primaryIcon = (ImageView) v.findViewById(R.id.primary_icon);
                views.presenceIcon = (ImageView) v.findViewById(R.id.presence_icon);
                views.secondaryActionButton = (DontPressWithParentImageView) v.findViewById(
                        R.id.secondary_action_button);
                if(null != views.secondaryActionButton)
			views.secondaryActionButton.setOnClickListener(this);
                views.secondaryActionDivider = v.findViewById(R.id.divider);
                v.setTag(views);
            }

            // Update the entry in the view cache
            views.entry = entry;

            // Bind the data to the view
            bindView(v, entry);
            return v;
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            // getView() handles this
            throw new UnsupportedOperationException();
        }

        @Override
        protected void bindView(View view, ViewEntry entry) {
            final Resources resources = mContext.getResources();
            ViewCache views = (ViewCache) view.getTag();

            // Set the label
            TextView label = views.label;
            setMaxLines(label, entry.maxLabelLines);
            label.setText(entry.label);

          //set Association image
            ImageView associationAction = views.associationIcon;
            // Set the data
            TextView data = views.data;
            Log.i(TAG,"views.data IS " +views.data);
            if (data != null) {
                if (entry.mimetype.equals(Phone.CONTENT_ITEM_TYPE)
                        || entry.mimetype.equals(Constants.MIME_SMS_ADDRESS)) {
                        data.setText(PhoneNumberFormatUtilEx.formatNumber(entry.data));
                } else {
                    data.setText(entry.data);
                }
                setMaxLines(data, entry.maxLines);
            }

            if (true == FeatureOption.MTK_SNS_SUPPORT) {
           	    if(entry.mimetype.toLowerCase().equals("vnd.android.cursor.item/sns.flickr")) {
            		label.setEnabled(false);
            		data.setEnabled(false);
            	}
            	else {
            		label.setEnabled(true);
            		data.setEnabled(true);
            	}
            }
            
            // Set the footer
            if (!TextUtils.isEmpty(entry.footerLine)) {
                views.footer.setText(entry.footerLine);
                views.footer.setVisibility(View.VISIBLE);
            } else {
                views.footer.setVisibility(View.GONE);
            }

            // Set the primary icon
            views.primaryIcon.setVisibility(entry.isPrimary ? View.VISIBLE : View.GONE);
            views.associationIcon.setVisibility(entry.isAssociation? View.VISIBLE: View.GONE);
            views.dataForAssociationSim.setVisibility(entry.isAssociation? View.VISIBLE: View.GONE);
            
            if (entry.isAssociation) {
                	data.setMaxWidth(128);
            	SIMInfo simInfo = SIMInfo.getSIMInfoById(ContactOwnerActivity.this, entry.simId);
            	if (simInfo != null) {
            		Log.i(TAG,"simInfo.mDisplayName is " + simInfo.mDisplayName);
                	Log.i(TAG,"simInfo.mColor is " + simInfo.mColor);
                	Log.i(TAG,"views.dataForAssociationSim is " + views.dataForAssociationSim);
                	views.dataForAssociationSim.setText(simInfo.mDisplayName);
                    int slotId = SIMInfo.getSlotById(ContactOwnerActivity.this, entry.simId);
                    Log.d(TAG,"slotId = "+slotId);
                    if(slotId>=0){
                        Log.d(TAG,"slotId >=0 ");
                        views.dataForAssociationSim.setBackgroundResource(Telephony.SIMBackgroundRes[simInfo.mColor]);
                        } else {
                            Log.d(TAG,"slotId <0 ");
                            views.dataForAssociationSim.setBackgroundResource(com.mediatek.internal.R.drawable.sim_background_locked);	
                            }
            	} else {
            		Log.i(TAG,"NULL View");
            	}            	
            	
            }          

            // Set the action icon
            ImageView action = views.actionIcon;
            if (entry.actionIcon != -1) {
                Drawable actionIcon;
                if (entry.resPackageName != null) {
                    // Load external resources through PackageManager
                    actionIcon = mContext.getPackageManager().getDrawable(entry.resPackageName,
                            entry.actionIcon, null);
                } else {
                    actionIcon = resources.getDrawable(entry.actionIcon);
                }
                action.setImageDrawable(actionIcon);
                action.setVisibility(View.VISIBLE);
            } else {
                // Things should still line up as if there was an icon, so make it invisible
                action.setVisibility(View.INVISIBLE);
            }

            // Set the presence icon
            Drawable presenceIcon = ContactPresenceIconUtil.getPresenceIcon(
                    mContext, entry.presence);
            ImageView presenceIconView = views.presenceIcon;
            if (presenceIcon != null) {
                presenceIconView.setImageDrawable(presenceIcon);
                presenceIconView.setVisibility(View.VISIBLE);
            } else {
                presenceIconView.setVisibility(View.GONE);
            }

            // Set the secondary action button
            DontPressWithParentImageView secondaryActionView = views.secondaryActionButton;
            Drawable secondaryActionIcon = null;
            if (entry.secondaryActionIcon != -1) {
                secondaryActionIcon = resources.getDrawable(entry.secondaryActionIcon);
            }
            if (entry.secondaryIntent != null && secondaryActionIcon != null) {
                secondaryActionView.setImageDrawable(secondaryActionIcon);
                secondaryActionView.setTag(entry.secondaryIntent);
                secondaryActionView.setVisibility(View.VISIBLE);
                views.secondaryActionDivider.setVisibility(View.VISIBLE);
            } else {
                secondaryActionView.setVisibility(View.GONE);
                views.secondaryActionDivider.setVisibility(View.GONE);
            }
        }

        private void setMaxLines(TextView textView, int maxLines) {
            if (maxLines == 1) {
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            } else {
                textView.setSingleLine(false);
                textView.setMaxLines(maxLines);
                textView.setEllipsize(null);
            }
        }
    }

    private interface StatusQuery {
        final String[] PROJECTION = new String[] {
                Data._ID,
                Data.STATUS,
                Data.STATUS_RES_PACKAGE,
                Data.STATUS_ICON,
                Data.STATUS_LABEL,
                Data.STATUS_TIMESTAMP,
                Data.PRESENCE,
        };

        final int _ID = 0;
    }

    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData,
            boolean globalSearch) {
        if (globalSearch) {
            super.startSearch(initialQuery, selectInitialQuery, appSearchData, globalSearch);
        } else {
            ContactsSearchManager.startSearch(this, initialQuery);
        }
    }

	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private  void importOneSimContact(String name, int phoneType, String phoneNumber) {
//		NamePhoneTypePair namePhoneTypePair = new NamePhoneTypePair(
//				cursor.getString(NAME_COLUMN));
		ContentValues sEmptyContentValues = new ContentValues();
		
//		String emailAddresses = "";//cursor.getString(EMAILS_COLUMN);

		final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(RawContacts.CONTENT_URI);
		builder.withValues(sEmptyContentValues);
		operationList.add(builder.build());

		builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
		builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
		builder.withValue(Phone.TYPE, phoneType);
		if (!TextUtils.isEmpty(phoneNumber)) {
			builder.withValue(Phone.NUMBER, phoneNumber);
			builder.withValue(Data.IS_PRIMARY, 1);
		} else {
			builder.withValue(Phone.NUMBER, null);
			builder.withValue(Data.IS_PRIMARY, 1);
		}
		operationList.add(builder.build());

		builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
		builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		builder.withValue(StructuredName.DISPLAY_NAME, name);
		operationList.add(builder.build());

		try {
			ContentProviderResult[] r = getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
			ContentProviderResult r1 = r[0];
			ContentProviderResult r2 = r[1];
			String raw_contact_id = r1.uri.getPath();
			raw_contact_id = raw_contact_id.substring(raw_contact_id.lastIndexOf("/")+1);
			String data_id = r2.uri.getPath();
			data_id = data_id.substring(data_id.lastIndexOf("/")+1);
			Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, Long.parseLong(raw_contact_id));
			
			Intent i = new Intent(this,AssociationSimActivity.class);
    		i.setData(contactUri);
    		i.putExtra("data_id", Long.parseLong(data_id));
    		startActivity(i);
//			for(int i = 0; i < r.length; i ++){
//				ContentProviderResult a = r[i];
//			}
			//			return r;
		} catch (RemoteException e) {
			Log.e(TAG, String
					.format("%s: %s", e.toString(), e.getMessage()));
		} catch (OperationApplicationException e) {
			Log.e(TAG, String
					.format("%s: %s", e.toString(), e.getMessage()));
		}
}
	
	private  void importOneUSimContact(String name, int phoneType, String phoneNumber,String email, String additional_number ) {
//		NamePhoneTypePair namePhoneTypePair = new NamePhoneTypePair(
//				cursor.getString(NAME_COLUMN));
		ContentValues sEmptyContentValues = new ContentValues();
		
//		String emailAddresses = "";//cursor.getString(EMAILS_COLUMN);

		final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(RawContacts.CONTENT_URI);
		builder.withValues(sEmptyContentValues);
		operationList.add(builder.build());

		builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
		builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
		builder.withValue(Phone.TYPE, phoneType);
		if (!TextUtils.isEmpty(phoneNumber)) {
			builder.withValue(Phone.NUMBER, phoneNumber);
			builder.withValue(Data.IS_PRIMARY, 1);
		} else {
			builder.withValue(Phone.NUMBER, null);
			builder.withValue(Data.IS_PRIMARY, 1);
		}
		operationList.add(builder.build());

		builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
		builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		builder.withValue(StructuredName.DISPLAY_NAME, name);
		operationList.add(builder.build());
		
    	if (!TextUtils.isEmpty(email)) {       	
//          for (String emailAddress : emailAddressArray) {
              builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
              builder.withValueBackReference(Email.RAW_CONTACT_ID, 0);
              builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
              builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
              builder.withValue(Email.DATA, email);
              operationList.add(builder.build());
//          }
          }    	       	
      	if (!TextUtils.isEmpty(additional_number)) {
          Log.i(TAG,"additionalNumber is " + additional_number);
      	builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
      	builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
	        builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
	        builder.withValue(Phone.TYPE, phoneType);			        	
	        builder.withValue(Phone.NUMBER, additional_number);
	        builder.withValue(Data.IS_ADDITIONAL_NUMBER, 1);
	        operationList.add(builder.build());
	        }    

		try {
			ContentProviderResult[] r = getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
			ContentProviderResult r1 = r[0];
			ContentProviderResult r2 = r[1];
			String raw_contact_id = r1.uri.getPath();
			raw_contact_id = raw_contact_id.substring(raw_contact_id.lastIndexOf("/")+1);
			String data_id = r2.uri.getPath();
			data_id = data_id.substring(data_id.lastIndexOf("/")+1);
			Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, Long.parseLong(raw_contact_id));
			
			Intent i = new Intent(this,AssociationSimActivity.class);
    		i.setData(contactUri);
    		i.putExtra("data_id", Long.parseLong(data_id));
    		startActivity(i);
//			for(int i = 0; i < r.length; i ++){
//				ContentProviderResult a = r[i];
//			}
			//			return r;
		} catch (RemoteException e) {
			Log.e(TAG, String
					.format("%s: %s", e.toString(), e.getMessage()));
		} catch (OperationApplicationException e) {
			Log.e(TAG, String
					.format("%s: %s", e.toString(), e.getMessage()));
		}
}
    public void showJoinAggregateActivity(Uri contactLookupUri) {
        if (contactLookupUri == null) {
            Toast.makeText(this, R.string.join_null_contact, Toast.LENGTH_SHORT).show();
            //MTK since the mState was changed after trim, rebuild it.
            //mState = null;
            //doAddAction();
            return;
        }

        long contactIdForJoin = ContentUris.parseId(contactLookupUri);
        Intent intent = new Intent(ContactsListActivity.JOIN_AGGREGATE);
        intent.putExtra(ContactsListActivity.EXTRA_AGGREGATE_ID, contactIdForJoin);
        startActivityForResult(intent, REQUEST_JOIN_CONTACT);
    }
    
	public CellConnMgr getCellConnMgr() {
        // TODO Auto-generated method stub
        return mCellConnMgr;
    }

    private DialogInterface.OnClickListener mImportListener = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			if(mPhoneEntries.size()>0){
				ViewEntry v = mPhoneEntries.get(0);
				String name ="";
				int type=0;
				String number = "";
				Cursor c1 = getContentResolver().query(v.uri, new String[]{"data1","data2"}, null, null, null);
				if(c1 != null && c1.moveToNext()){
					number = c1.getString(0);
					type = Integer.parseInt(c1.getString(1));
					c1.close();
				}
				Uri u = Uri.withAppendedPath(RawContacts.CONTENT_URI, String.valueOf(v.contactId));
				Cursor c2 = getContentResolver().query(u, new String[]{"display_name"}, null, null, null);
				if(c2 != null&& c2.moveToNext()){
					name = c2.getString(0);
					c2.close();
				}

				final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
						.getService(Context.TELEPHONY_SERVICE));
				if (iTel == null) {
					return;
				}
				boolean isUSIM = false;
				int slotId = ContactsUtils.getSlotId(ContactOwnerActivity.this,
						(int)indicate);
				try {
					if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
						if (slotId == com.android.internal.telephony.Phone.GEMINI_SIM_1) {
							if (null != iTel.getIccCardTypeGemini(slotId) && iTel.getIccCardTypeGemini(slotId).equals("USIM")) {
								isUSIM = true;
							}
						} else {
							if (slotId == com.android.internal.telephony.Phone.GEMINI_SIM_2) {
								if (null != iTel.getIccCardTypeGemini(slotId) && iTel.getIccCardTypeGemini(slotId)
										.equals("USIM")) {
									isUSIM = true;
								}
							}
						}
					} else {
						if (iTel.getIccCardType().equals("USIM")) {
							isUSIM = true;
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if(isUSIM){
				    //importOneUSimContact(name,type,number);
					ContentResolver contentResolver = getContentResolver(); 	
								long rawContactId = -1;

								//String name = null;
								Log.i(TAG, "IsUSIM = true, ImportListener onClick indicate is " + indicate);
								long freshId = getRefreshedContactId();
								Log.i(TAG, "freshId is " + freshId);
								if (freshId > 0) {
									if (mCursor.moveToFirst()) {
										Log.i(TAG, "mCursor is " + mCursor);
										rawContactId = mCursor.getInt(0);
									}
								}

					Uri dataUri = Uri.withAppendedPath(ContentUris
							.withAppendedId(RawContacts.CONTENT_URI,
									rawContactId),
							RawContacts.Data.CONTENT_DIRECTORY);
					Log.i(TAG, "USIM dataUri IS " + dataUri);
					Cursor cursor = contentResolver.query(dataUri, null, null, null, null);
					String phoneTypeSuffix = null;
					String temphone = null;
					String phone = null;
					String email = null;
					String additional_number = null;
					int i = 0;

					if (cursor != null) {
						while (cursor.moveToNext()) {
							
							String mt = cursor.getString(cursor
									.getColumnIndexOrThrow(Data.MIMETYPE));
							Log.i(TAG," USIM mt is "+mt);

							if ((i == 0 && i < cursor.getCount())
									&& Phone.CONTENT_ITEM_TYPE.equals(mt)) {
								phone = cursor.getString(cursor
										.getColumnIndexOrThrow(Phone.NUMBER));
								phoneTypeSuffix = cursor.getString(cursor.getColumnIndexOrThrow(Data.DATA15));
									temphone = phone;
									Log.i(TAG, " USIM *********** temphone " + temphone);
							}
							if (StructuredName.CONTENT_ITEM_TYPE.endsWith(mt)) {
								name = cursor.getString(cursor.getColumnIndexOrThrow(StructuredName.DISPLAY_NAME));
							}
//								Log.i(TAG,"mSimType is " + mSimType);
								if (Email.CONTENT_ITEM_TYPE.equals(mt)) {
									email = cursor.getString(cursor.getColumnIndexOrThrow(Email.DATA));
									Log.i(TAG,"USIM email is " + email);
							}
							if ((i >0 && i < cursor.getCount())
									&& Phone.CONTENT_ITEM_TYPE.equals(mt)) {
								additional_number = cursor.getString(cursor
										.getColumnIndexOrThrow(Phone.NUMBER));
									Log.i(TAG,"USIM additional_number is " + additional_number);								
							}	
							i++;
						}
						cursor.close();
					}
				  // ContactsUtils.insertToDB(name, phone, email, additional_number, mResolver, -1, "USIM" );
				   importOneUSimContact(name,type,number,email,additional_number);

				}
				else{
					Log.i(TAG, "import  one sim card " );
					importOneSimContact(name,type,number);
				}
					
			}
			
		}
	};

	//remove number //gemini enhancement
    private DialogInterface.OnClickListener mRemoveNumberListener = new DialogInterface.OnClickListener() {
    	
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
//			deleteContact();
			ContentResolver contentResolver = getContentResolver();		
			long rawContactId = -1;
			final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
					.getService(Context.TELEPHONY_SERVICE));
			if (iTel == null) {
				return;
			}
			String name = null;
			Log.i(TAG, "In mRemoveNumberListener onClick indicate is " + indicate);
			long freshId = getRefreshedContactId();
			Log.i(TAG, "freshId is " + freshId);
			if (freshId > 0) {
				if (mCursor.moveToFirst()) {
					Log.i(TAG, "mCursor is " + mCursor);
					rawContactId = mCursor.getInt(0);
				}
			}
			Log.i(TAG,"********rawContactId is " + rawContactId);
			Log.i(TAG,"********mDeleteDataId is " + mDeleteDataId);
			boolean isUSIM = false;
			int slotId = ContactsUtils.getSlotId(ContactOwnerActivity.this,
					(int)indicate);
			try {
				if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
					if (slotId == com.android.internal.telephony.Phone.GEMINI_SIM_1) {
						if (null != iTel.getIccCardTypeGemini(slotId) && iTel.getIccCardTypeGemini(slotId).equals("USIM")) {
							isUSIM = true;
						}
					} else {
						if (slotId == com.android.internal.telephony.Phone.GEMINI_SIM_2) {
							if (null != iTel.getIccCardTypeGemini(slotId) && iTel.getIccCardTypeGemini(slotId)
									.equals("USIM")) {
								isUSIM = true;
							}
						}
					}
				} else {
					if (iTel.getIccCardType().equals("USIM")) {
						isUSIM = true;
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (indicate > RawContacts.INDICATE_PHONE) {
				String number = null;
				if (!isUSIM) {
					
					Cursor c = contentResolver.query(Data.CONTENT_URI,new String[] { Data.DATA1 },
							Data.RAW_CONTACT_ID + "=" + rawContactId + " AND "
									+ Data.MIMETYPE + "=?", new String[]{StructuredName.CONTENT_ITEM_TYPE}, null);
					Log.i(TAG,"After query name");
					if (null != c && c.moveToFirst()) {
						name = c.getString(0);
					}
					Log.i(TAG,"After query name is " + name);
						
					Log.i(TAG, "rawContactId is " + rawContactId);
					String phoneTypeSuffix = null;
					Log.i(TAG, "rawContactId is " + rawContactId);				
					Cursor mc = contentResolver.query(Data.CONTENT_URI, new String[] { Data.DATA1, Data.DATA15 },
							Data.RAW_CONTACT_ID + "=" + rawContactId + " AND " + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'"
							/*+ Data.IS_ADDITIONAL_NUMBER + "=0"*/, null, null);
					if (null != mc && mc.moveToFirst()) {
						number = mc.getString(0); // DATA1
						number = number.replaceAll("-", "");
						phoneTypeSuffix = mc.getString(1); // DATA15
					}
					Log.i(TAG,"number is " + number);
					
						String where;
					where = "tag = '" + name + "' AND number = '" + number
							+ "'";

					Uri iccUri = ContactsUtils.getUri(slotId);
									Log.d(TAG, "where " + where);		
										Log.d(TAG, "iccUri ******** " + iccUri);
					int deleteDone = -1;
					if(null != iccUri)
						deleteDone = getContentResolver().delete(iccUri, where, null);
										Log.i(TAG, "deleteDone is " + deleteDone);
										 if (deleteDone == 1) {
										int deleteDB = getContentResolver().delete(
												Contacts.CONTENT_URI,
												Contacts._ID + "=" + freshId, null);
											 Log.i(TAG,"deleteDB is "+deleteDB);
										 }
										finish();
									return;
					
				} else if (isUSIM) {
					Uri dataUri = Uri.withAppendedPath(ContentUris
							.withAppendedId(RawContacts.CONTENT_URI,
									rawContactId),
							RawContacts.Data.CONTENT_DIRECTORY);
					Log.i(TAG, "USIM dataUri IS " + dataUri);
					Cursor cursor = contentResolver.query(dataUri, null, null, null, null);
					String phoneTypeSuffix = null;
					String temphone = null;
					String phone = null;
					String email = null;
					String additional_number = null;
					int i = 0;
					if (cursor != null) {
						while (cursor.moveToNext()) {
							
							String mt = cursor.getString(cursor
									.getColumnIndexOrThrow(Data.MIMETYPE));
							Log.i(TAG," USIM mt is "+mt);
							if ((i == 0 && i < cursor.getCount())
									&& Phone.CONTENT_ITEM_TYPE.equals(mt)) {
								phone = cursor.getString(cursor
										.getColumnIndexOrThrow(Phone.NUMBER));
								phoneTypeSuffix = cursor.getString(cursor.getColumnIndexOrThrow(Data.DATA15));
									temphone = phone;
									Log.i(TAG, " USIM *********** temphone " + temphone);
							}
							if (StructuredName.CONTENT_ITEM_TYPE.endsWith(mt)) {
								name = cursor.getString(cursor.getColumnIndexOrThrow(StructuredName.DISPLAY_NAME));
							}
								if (Email.CONTENT_ITEM_TYPE.equals(mt)) {
									email = cursor.getString(cursor.getColumnIndexOrThrow(Email.DATA));
									Log.i(TAG,"USIM email is " + email);
							}
							if ((i > 0 && i < cursor.getCount())
									&& Phone.CONTENT_ITEM_TYPE.equals(mt)) {
								additional_number = cursor.getString(cursor
										.getColumnIndexOrThrow(Phone.NUMBER));
							}	
							i++;
						}
						cursor.close();
					}
					Log.i(TAG,"USIM phone is " + phone);
					Log.i(TAG,"USIM additional_number is " + additional_number);
					Log.i(TAG,"USIM mDeleteNumber is " + mDeleteNumber);					
					if (mDeleteNumber.equals(phone)) {
						phone = phone.replaceAll("-", "");
						if (TextUtils.isEmpty(name)) {
							String where;
							where = "number = '" + phone + "'";
							Uri iccUri = ContactsUtils.getUri(slotId);
										Log.d(TAG, "USIM where " + where);		
											Log.d(TAG, "USIM iccUri ******** " + iccUri);
								int deleteDone = -1;
								if(null != iccUri)
									deleteDone = getContentResolver().delete(iccUri, where, null);
											Log.i(TAG, "USIM deleteDone is " + deleteDone);
											 if (deleteDone == 1) {
											int deleteDB = getContentResolver().delete(
													Contacts.CONTENT_URI,
													Contacts._ID + "=" + freshId, null);
												 Log.i(TAG,"USIM deleteDB is "+deleteDB);
											 }
//											finish();
//										return;
						} else {
							String where;
							where = "tag = '" + name + "' AND number = '" + phone + "'";
							Uri iccUri = ContactsUtils.getUri(slotId);
							Log.d(TAG, "USIM where " + where);		
							Log.d(TAG, "USIM iccUri ******** " + iccUri);
					        int deleteDone = -1;
					        if(null != iccUri)
					        	deleteDone = getContentResolver().delete(iccUri, where, null);
							Log.i(TAG, "USIM deleteDone is " + deleteDone);
							if (deleteDone == 1) {
							int deleteDB = getContentResolver().delete(Contacts.CONTENT_URI, Contacts._ID + "=" + freshId, null);
						    Log.i(TAG,"USIM deleteDB is "+deleteDB);
							}
//							finish();
//							return;
					}

				} else if (mDeleteNumber.equals(additional_number)){										
					if (!TextUtils.isEmpty(phone)) {
						int result = -1;
							ContentValues updatevalues = new ContentValues();
						updatevalues.put("tag", TextUtils.isEmpty(name) ? "" : name);
						updatevalues.put("number", TextUtils.isEmpty(phone) ? "" : phone.replaceAll("-", ""));
						updatevalues.put("newTag", TextUtils.isEmpty(name) ? "" : name);
						updatevalues.put("newNumber", TextUtils.isEmpty(phone) ? "" : phone.replaceAll("-", ""));						
						updatevalues.put("anr", TextUtils.isEmpty(additional_number) ? "": additional_number.replaceAll("-", ""));
					    updatevalues.put("newAnr", "");
						updatevalues.put("emails", TextUtils.isEmpty(email) ? "" : email);
						updatevalues.put("newEmails", TextUtils.isEmpty(email) ? "" : email);
						Uri iccUriTemp = ContactsUtils.getUri(slotId);
						cursor = null;
						if(null != iccUriTemp)
							cursor = contentResolver.query(iccUriTemp,
										ADDRESS_BOOK_COLUMN_NAMES, null, null, null);
								if (cursor != null) {
									result = -1;
							if(null != iccUriTemp)
								result = contentResolver.update(iccUriTemp,
										updatevalues, null, null);
							Log.i(TAG,"ContactsUtils.getUri(slotId) is " + ContactsUtils.getUri(slotId));
							Log.i(TAG, "mDeleteNumber == additional_number updatevalues IS " + updatevalues);
							Log.i(TAG, "mDeleteNumber == additional_number result IS " + result);	
							cursor.close();
						}
						if (result > 0) {
							String whereadditional = Data.RAW_CONTACT_ID + " = \'"
							+ rawContactId + "\'" + " AND " +  Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'"
							+ " AND " + Data.IS_ADDITIONAL_NUMBER + " =1";
							Log.i(TAG,"whereadditional is " + whereadditional);
							int deleteDBAdditional = contentResolver.delete(Data.CONTENT_URI, whereadditional, null);
							Log.i(TAG,"deleteDBAdditional is " + deleteDBAdditional);
					}

					}

				}
				
				}
				
			} else {
				String wherephone = Data._ID + " = " + mDeleteDataId;
//				+ rawContactId + " AND " +  Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'" +
//				" AND " + Data.DATA1 + "='" + mDeleteNumber + "'";
				Log.i(TAG, "phone contact wherephone is " + wherephone);
				int deletePhone = contentResolver.delete(Data.CONTENT_URI, wherephone, null);
	        	Log.i(TAG, "phone contact deletePhone is " + deletePhone);
//			    finish();
	    	}			
		}
	};
	
}
