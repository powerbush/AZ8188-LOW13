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
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE") * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
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
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
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

import com.android.contacts.TextHighlightingAnimation.TextWithHighlighting;
import com.android.contacts.model.ContactsSource;
import com.android.contacts.model.Sources;
import com.android.contacts.ui.ContactsPreferences;
import com.android.contacts.ui.ContactsPreferencesActivity;
import com.android.contacts.ui.EditContactActivity;
import com.android.contacts.ui.EditSimContactActivity;
import com.android.contacts.ui.QuickContactWindow;
import com.android.contacts.ui.ContactsPreferencesActivity.Prefs;
import com.android.contacts.ui.widget.ContactEditorView;

import android.widget.BladeView;
import com.android.contacts.util.AccountSelectionUtil;
import com.android.contacts.util.Constants;
import com.android.contacts.util.AccountSelectionUtil.AccountSelectedListener;
import com.android.contacts.util.Constants.SimInfo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IContentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Contacts.ContactMethods;
import android.provider.Contacts.People;
import android.provider.Contacts.PeopleColumns;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract.ContactCounts;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.Intents;
import android.provider.ContactsContract.ProviderStatus;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.SearchSnippetColumns;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts.AggregationSuggestions;
import android.provider.ContactsContract.Intents.Insert;
import android.provider.ContactsContract.Intents.UI;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView.OnItemClickListener;

import android.app.ProgressDialog;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.util.Log;
import android.os.Message;

import com.android.internal.telephony.ITelephony;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.mediatek.featureoption.FeatureOption;
import com.mediatek.client.DataManager;
import com.mediatek.client.DataManager.SNSAccount;
import com.mediatek.client.DataManager.SNSAccountInfo;
import com.mediatek.client.DataManager.WidgetEvent;
import com.mediatek.client.DataManager.SnsUser;
import com.android.contacts.mtk.*;
import com.mediatek.wsp.util.EmotionParser;
import android.os.Looper;
import com.mediatek.wsp.util.SNSContact;
import java.io.InputStream;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.ContextMenu;
import java.util.concurrent.ConcurrentHashMap;
import com.mediatek.banyan.widget.MTKSNSEmotionParser;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import android.provider.Telephony.SIMInfo;
import android.app.StatusBarManager;
import android.content.IntentFilter;
//import android.content.BroadcastReceiver;
import com.mediatek.CellConnService.CellConnMgr;
//import com.android.contacts.ContactsMarkListActivity;
import android.os.HandlerThread;
import java.util.Locale;

import com.android.internal.telephony.gemini.GeminiPhone;

/**
 * Displays a list of contacts. Usually is embedded into the ContactsActivity.
 */
@SuppressWarnings("deprecation")
public class ContactsListActivity extends ListActivity implements View.OnCreateContextMenuListener,
        View.OnClickListener, View.OnKeyListener, TextWatcher, TextView.OnEditorActionListener,
        OnFocusChangeListener, OnTouchListener, ContactsUtils.CellConnMgrClient {

    public static class JoinContactActivity extends ContactsListActivity {

    }

    public static class ContactsSearchActivity extends ContactsListActivity {

    }

    public static boolean s_deletingContacts = false;  ////
    //public static boolean s_deletingContacts2 = false;  ////
    public static boolean s_importingSimContacts1 = false;  ////
    public static boolean s_importingSimContacts2 = false;  ////

    private static final String TAG = "ContactsListActivity";
	private static final int SINGLE_SIM_ID = -1;
    private static final boolean ENABLE_ACTION_ICON_OVERLAYS = true;

    private static final String LIST_STATE_KEY = "liststate";
    private static final String SHORTCUT_ACTION_KEY = "shortcutAction";
    public static final String FILTER_SIM_CONTACT_KEY = "filter_sim_contact";
    public static final String BROADCAST_RESULT = "broadcast_result";

    public static final String SELECT_GROUP_KEY = "group";
    public static final String ALL_CONTACTS_TITLE = "All Contacts";
    public static final String SIM_CONTACTS_TITLE = "SIM";
    
    // MTK to filter the contact or data ID, 0 means do not do this filter
    private static final int QUERY_UNKNOWN_TABLE = 0;
    private static final int QUERY_DATA_TABLE = 1;
    private static final int QUERY_CONTACT_TABLE = 2;

    static final int MENU_ITEM_VIEW_CONTACT = 1;
    static final int MENU_ITEM_CALL = 2;
    static final int MENU_ITEM_EDIT_BEFORE_CALL = 3;
    static final int MENU_ITEM_SEND_SMS = 4;
    static final int MENU_ITEM_SEND_IM = 5;
    static final int MENU_ITEM_EDIT = 6;
    static final int MENU_ITEM_DELETE = 7;
    static final int MENU_ITEM_TOGGLE_STAR = 8;
    
    static final int MENU_OWNER_EDIT = 9;
    static final int MENU_OWNER_DELETE = 10;
    static final int MENU_ITEM_SHARE = 11;//share single contact

    static final int MENU_ITEM_GROUPS = 12;
    static final int MENU_ITEM_SHARE_CONTACTS = 13;
	// mtk80909 for Speed Dial
	static final int MENU_ITEM_SPEED_DIAL = 97;
    private static final int SUBACTIVITY_NEW_CONTACT = 1;
    private static final int SUBACTIVITY_VIEW_CONTACT = 2;
    private static final int SUBACTIVITY_DISPLAY_GROUP = 3;
    private static final int SUBACTIVITY_SEARCH = 4;
    private static final int SUBACTIVITY_FILTER = 5;
    
    // modified by Ivan @ 2010-08-22 17:40
    private static final int SUBACTIVITY_VIEW_OWNER = 6; 
    private static final int SUBACTIVITY_NEW_OWNER_INSERT = 7;
    private static final int SUBACTIVITY_SELECT_ACCOUNT = 8;
    public static final String NEW_OWNER_INFO = "new_owner_info";
	private ArrayList<Integer> resultAccount = null;

    private static final int TEXT_HIGHLIGHTING_ANIMATION_DURATION = 350;

    /**
     * The action for the join contact activity.
     * <p>
     * Input: extra field {@link #EXTRA_AGGREGATE_ID} is the aggregate ID.
     *
     * TODO: move to {@link ContactsContract}.
     */
    public static final String JOIN_AGGREGATE =
            "com.android.contacts.action.JOIN_AGGREGATE";

    /**
     * Used with {@link #JOIN_AGGREGATE} to give it the target for aggregation.
     * <p>
     * Type: LONG
     */
    public static final String EXTRA_AGGREGATE_ID =
            "com.android.contacts.action.AGGREGATE_ID";

    /**
     * Used with {@link #JOIN_AGGREGATE} to give it the name of the aggregation target.
     * <p>
     * Type: STRING
     */
    @Deprecated
    public static final String EXTRA_AGGREGATE_NAME =
            "com.android.contacts.action.AGGREGATE_NAME";

    public static final String AUTHORITIES_FILTER_KEY = "authorities";

    private static final Uri CONTACTS_CONTENT_URI_WITH_LETTER_COUNTS =
            buildSectionIndexerUri(Contacts.CONTENT_URI);

    /** Mask for picker mode */
    static final int MODE_MASK_PICKER = 0x80000000;
    /** Mask for no presence mode */
    static final int MODE_MASK_NO_PRESENCE = 0x40000000;
    /** Mask for enabling list filtering */
    static final int MODE_MASK_NO_FILTER = 0x20000000;
    /** Mask for having a "create new contact" header in the list */
    static final int MODE_MASK_CREATE_NEW = 0x10000000;
    /** Mask for showing photos in the list */
    static final int MODE_MASK_SHOW_PHOTOS = 0x08000000;
    /** Mask for hiding additional information e.g. primary phone number in the list */
    static final int MODE_MASK_NO_DATA = 0x04000000;
    /** Mask for showing a call button in the list */
    static final int MODE_MASK_SHOW_CALL_BUTTON = 0x02000000;
    /** Mask to disable quickcontact (images will show as normal images) */
    static final int MODE_MASK_DISABLE_QUIKCCONTACT = 0x01000000;
    /** Mask to show the total number of contacts at the top */
    static final int MODE_MASK_SHOW_NUMBER_OF_CONTACTS = 0x00800000;

    /** Unknown mode */
    static final int MODE_UNKNOWN = 0;
    /** Default mode */
    static final int MODE_DEFAULT = 4 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;
    /** Custom mode */
    static final int MODE_CUSTOM = 8;
    /** Show all starred contacts */
    static final int MODE_STARRED = 20 | MODE_MASK_SHOW_PHOTOS;
    /** Show frequently contacted contacts */
    static final int MODE_FREQUENT = 30 | MODE_MASK_SHOW_PHOTOS;
    /** Show starred and the frequent */
    static final int MODE_STREQUENT = 35 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_SHOW_CALL_BUTTON;
    /** Show all contacts and pick them when clicking */
    static final int MODE_PICK_CONTACT = 40 | MODE_MASK_PICKER | MODE_MASK_SHOW_PHOTOS
            | MODE_MASK_DISABLE_QUIKCCONTACT;
    /** Show all contacts as well as the option to create a new one */
    static final int MODE_PICK_OR_CREATE_CONTACT = 42 | MODE_MASK_PICKER | MODE_MASK_CREATE_NEW
            | MODE_MASK_SHOW_PHOTOS | MODE_MASK_DISABLE_QUIKCCONTACT;
    /** Show all people through the legacy provider and pick them when clicking */
    static final int MODE_LEGACY_PICK_PERSON = 43 | MODE_MASK_PICKER
            | MODE_MASK_DISABLE_QUIKCCONTACT;
    /** Show all people through the legacy provider as well as the option to create a new one */
    static final int MODE_LEGACY_PICK_OR_CREATE_PERSON = 44 | MODE_MASK_PICKER
            | MODE_MASK_CREATE_NEW | MODE_MASK_DISABLE_QUIKCCONTACT;
    /** Show all contacts and pick them when clicking, and allow creating a new contact */
    static final int MODE_INSERT_OR_EDIT_CONTACT = 45 | MODE_MASK_PICKER | MODE_MASK_CREATE_NEW
            | MODE_MASK_SHOW_PHOTOS | MODE_MASK_DISABLE_QUIKCCONTACT;
    /** Show all phone numbers and pick them when clicking */
    static final int MODE_PICK_PHONE = 50 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE;
    /** Show all phone numbers through the legacy provider and pick them when clicking */
    static final int MODE_LEGACY_PICK_PHONE =
            51 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE | MODE_MASK_NO_FILTER;
    /** Show all postal addresses and pick them when clicking */
    static final int MODE_PICK_POSTAL =
            55 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE | MODE_MASK_NO_FILTER;
    /** Show all postal addresses and pick them when clicking */
    static final int MODE_LEGACY_PICK_POSTAL =
            56 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE | MODE_MASK_NO_FILTER;
    static final int MODE_GROUP = 57 | MODE_MASK_SHOW_PHOTOS;
    /** Run a search query */
    static final int MODE_QUERY = 60 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_NO_FILTER
            | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;
    /** Run a search query in PICK mode, but that still launches to VIEW */
    static final int MODE_QUERY_PICK_TO_VIEW = 65 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_PICKER
            | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;

    /** Show join suggestions followed by an A-Z list */
    static final int MODE_JOIN_CONTACT = 70 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE
            | MODE_MASK_NO_DATA | MODE_MASK_SHOW_PHOTOS | MODE_MASK_DISABLE_QUIKCCONTACT;

    /** Run a search query in a PICK mode */
    static final int MODE_QUERY_PICK = 75 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_NO_FILTER
            | MODE_MASK_PICKER | MODE_MASK_DISABLE_QUIKCCONTACT | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;

    /** Run a search query in a PICK_PHONE mode */
    static final int MODE_QUERY_PICK_PHONE = 80 | MODE_MASK_NO_FILTER | MODE_MASK_PICKER
            | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;

    /** Run a search query in PICK mode, but that still launches to EDIT */
    static final int MODE_QUERY_PICK_TO_EDIT = 85 | MODE_MASK_NO_FILTER | MODE_MASK_SHOW_PHOTOS
            | MODE_MASK_PICKER | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;

    //MTK
    static final int MODE_PICK_PHONE_EMAIL = 53 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE;
    static final int MODE_QUERY_PICK_PHONE_EMAIL = 82 | MODE_MASK_NO_FILTER | MODE_MASK_PICKER
    | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;
    
    public static final Uri PICK_PHONE_EMAIL_URI = Uri.parse("content://com.android.contacts/data/phone_email");
    public static final Uri PICK_PHONE_EMAIL_FILTER_URI = Uri.parse("content://com.android.contacts/data/phone_email/filter");
    /**
     * An action used to do perform search while in a contact picker.  It is initiated
     * by the ContactListActivity itself.
     */
    private static final String ACTION_SEARCH_INTERNAL = "com.android.contacts.INTERNAL_SEARCH";

    /** Maximum number of suggestions shown for joining aggregates */
    static final int MAX_SUGGESTIONS = 4;

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
    static final String[] CONTACTS_SUMMARY_PROJECTION_FROM_EMAIL = new String[] {
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
        Contacts.INDICATE_PHONE_SIM,		// 10
        // email lookup doesn't included HAS_PHONE_NUMBER in projection
    };

    static final String[] CONTACTS_SUMMARY_FILTER_PROJECTION = new String[] {
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
        SearchSnippetColumns.SNIPPET_MIMETYPE, // 11
        SearchSnippetColumns.SNIPPET_DATA1,     // 12
        SearchSnippetColumns.SNIPPET_DATA4,     // 13
        Contacts.INDICATE_PHONE_SIM,		// 14
    };

    static final String[] LEGACY_PEOPLE_PROJECTION = new String[] {
        People._ID,                         // 0
        People.DISPLAY_NAME,                // 1
        People.DISPLAY_NAME,                // 2
        People.DISPLAY_NAME,                // 3
        People.STARRED,                     // 4
        PeopleColumns.TIMES_CONTACTED,      // 5
        People.PRESENCE_STATUS,             // 6
    };
    static final int SUMMARY_ID_COLUMN_INDEX = 0;
    static final int SUMMARY_DISPLAY_NAME_PRIMARY_COLUMN_INDEX = 1;
    static final int SUMMARY_DISPLAY_NAME_ALTERNATIVE_COLUMN_INDEX = 2;
    static final int SUMMARY_SORT_KEY_PRIMARY_COLUMN_INDEX = 3;
    static final int SUMMARY_STARRED_COLUMN_INDEX = 4;
    static final int SUMMARY_TIMES_CONTACTED_COLUMN_INDEX = 5;
    static final int SUMMARY_PRESENCE_STATUS_COLUMN_INDEX = 6;
    static final int SUMMARY_PHOTO_ID_COLUMN_INDEX = 7;
    static final int SUMMARY_LOOKUP_KEY_COLUMN_INDEX = 8;
    static final int SUMMARY_PHONETIC_NAME_COLUMN_INDEX = 9;
    static final int SUMMARY_HAS_PHONE_COLUMN_INDEX = 10;
    static final int SUMMARY_SNIPPET_MIMETYPE_COLUMN_INDEX = 11;
    static final int SUMMARY_SNIPPET_DATA1_COLUMN_INDEX = 12;
    static final int SUMMARY_SNIPPET_DATA4_COLUMN_INDEX = 13;

    static final String[] PHONES_PROJECTION = new String[] {
        Phone._ID, //0
        Phone.TYPE, //1
        Phone.LABEL, //2
        Phone.NUMBER, //3
        Phone.DISPLAY_NAME, // 4
        Phone.CONTACT_ID, // 5
		Contacts.INDICATE_PHONE_SIM, // 6 // mtk80909 for Speed Dial
    };
    static final String[] LEGACY_PHONES_PROJECTION = new String[] {
        Phones._ID, //0
        Phones.TYPE, //1
        Phones.LABEL, //2
        Phones.NUMBER, //3
        People.DISPLAY_NAME, // 4
    };
    static final int PHONE_ID_COLUMN_INDEX = 0;
    static final int PHONE_TYPE_COLUMN_INDEX = 1;
    static final int PHONE_LABEL_COLUMN_INDEX = 2;
    static final int PHONE_NUMBER_COLUMN_INDEX = 3;
    static final int PHONE_DISPLAY_NAME_COLUMN_INDEX = 4;
    static final int PHONE_CONTACT_ID_COLUMN_INDEX = 5;
	static final int PHONE_INDICATE_PHONE_SIM_INDEX = 6; // mtk80909 for Speed Dial

    static final String[] POSTALS_PROJECTION = new String[] {
        StructuredPostal._ID, //0
        StructuredPostal.TYPE, //1
        StructuredPostal.LABEL, //2
        StructuredPostal.DATA, //3
        StructuredPostal.DISPLAY_NAME, // 4
    };
    static final String[] LEGACY_POSTALS_PROJECTION = new String[] {
        ContactMethods._ID, //0
        ContactMethods.TYPE, //1
        ContactMethods.LABEL, //2
        ContactMethods.DATA, //3
        People.DISPLAY_NAME, // 4
    };
    static final String[] RAW_CONTACTS_PROJECTION = new String[] {
        RawContacts._ID, //0
        RawContacts.CONTACT_ID, //1
        RawContacts.ACCOUNT_TYPE, //2
    };
    
    // modified by Ivan 2010-08-10
    public static final String[] RAW_CONTACTS_PROJECTION2 = new String[] {
        RawContacts._ID, //0
        RawContacts.CONTACT_ID, //1
        RawContacts.ACCOUNT_TYPE, //2
        RawContacts.ACCOUNT_NAME, //3
        RawContacts.SYNC3, //4
        RawContacts.SYNC4, //5
    };

    static final int POSTAL_ID_COLUMN_INDEX = 0;
    static final int POSTAL_TYPE_COLUMN_INDEX = 1;
    static final int POSTAL_LABEL_COLUMN_INDEX = 2;
    static final int POSTAL_ADDRESS_COLUMN_INDEX = 3;
    static final int POSTAL_DISPLAY_NAME_COLUMN_INDEX = 4;

    private static final int QUERY_TOKEN = 42;
    private static final int DELETE_TOKEN = 43;
    static final String KEY_PICKER_MODE = "picker_mode";

    private ContactItemListAdapter mAdapter;

    int mMode = MODE_DEFAULT;

    private QueryHandler mQueryHandler;
    private boolean mJustCreated;
    Uri mSelectedContactUri;

    private static final Uri mIccUri = Uri.parse("content://icc/adn/");//80794
	private static final Uri mIccUri1 = Uri.parse("content://icc/adn1/");
	private static final Uri mIccUri2 = Uri.parse("content://icc/adn2/");

//    private boolean mDisplayAll;
    private boolean mDisplayOnlyPhones;
    private boolean mdisplayPhoneContacts;
	private boolean mdisplaySimsContacts;
//	private boolean mdisplaySim1Contacts;
//	private boolean mdisplaySim2Contacts;
//
//	private boolean mdisplayUSimContacts;//80794 for USIM
//	private boolean mdisplayUSim1Contacts;
//	private boolean mdisplayUSim2Contacts;
	private ContactsUtils.SIMInfoWrapper mSimInfoWrapper;

    private Uri mGroupUri;

    private long mQueryAggregateId;

    private ArrayList<Long> mWritableRawContactIds = new ArrayList<Long>();
    private int  mWritableSourcesCnt;
    private int  mReadOnlySourcesCnt;

    /**
     * Used to keep track of the scroll state of the list.
     */
    private Parcelable mListState = null;

    private String mShortcutAction;

    /**
     * Internal query type when in mode {@link #MODE_QUERY_PICK_TO_VIEW}.
     */
    private int mQueryMode = QUERY_MODE_NONE;

    private static final int QUERY_MODE_NONE = -1;
    private static final int QUERY_MODE_MAILTO = 1;
    private static final int QUERY_MODE_TEL = 2;

    private int mProviderStatus = ProviderStatus.STATUS_NORMAL;

    private boolean mSearchMode;
    private boolean mSearchResultsMode;
    private boolean mShowNumberOfContacts;

    private boolean mShowSearchSnippets;
    private boolean mSearchInitiated;

    private String mInitialFilter;

    //MTK when add shortcut and set contact icon when do not want to new a sim contact
    private boolean mFilterSIMContact = false;
    private boolean mBroadcastResult = false;

    private static final String CLAUSE_ONLY_VISIBLE = Contacts.IN_VISIBLE_GROUP + "=1";
    private static final String CLAUSE_ONLY_PHONES = Contacts.HAS_PHONE_NUMBER + "=1";
    private static final String CLAUSE_ONLY_PHONE_CONTACT = RawContacts.INDICATE_PHONE_SIM
			+ "=" + RawContacts.INDICATE_PHONE;

	private static final String CLAUSE_ONLY_SIMs_CONTACT = RawContacts.INDICATE_PHONE_SIM
			+ ">" + RawContacts.INDICATE_PHONE;

	private int mSlot = -1;
	CellConnMgr mCellConnMgr;

    /**
     * In the {@link #MODE_JOIN_CONTACT} determines whether we display a list item with the label
     * "Show all contacts" or actually show all contacts
     */
    private boolean mJoinModeShowAllContacts;

    /**
     * The ID of the special item described above.
     */
    private static final long JOIN_MODE_SHOW_ALL_CONTACTS_ID = -2;

    public static final String CONTACTS_IN_GROUP_SELECT =
        " IN "
                + "(SELECT " + RawContacts.CONTACT_ID
                + " FROM " + "raw_contacts"
                + " WHERE " + "raw_contacts._id" + " IN "
                        + "(SELECT " + "data."+Data.RAW_CONTACT_ID
                        + " FROM " + "data "
                        + "JOIN mimetypes ON (data.mimetype_id = mimetypes._id)"
                        + " WHERE " + Data.MIMETYPE + "='" + GroupMembership.CONTENT_ITEM_TYPE
                                + "' AND " + GroupMembership.GROUP_ROW_ID + "="
                                + "(SELECT " + "groups" + "." + Groups._ID
                                + " FROM " + "groups"
                                + " WHERE " + Groups.DELETED + "=0 AND " + Groups.TITLE + "=?))" 
                 + " AND "+ RawContacts.DELETED +"=0)";

    // Uri matcher for contact id
    private static final int CONTACTS_ID = 1001;
    private static final UriMatcher sContactsIdMatcher;

    private ContactPhotoLoader mPhotoLoader;
    private ContactSnsLoader mSnsLoader;
    private ContactOwnerLoader mOwnerLoader;
    private int mPosition;

    private BladeView mBladeView;
    final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));//80794
	boolean mSim1RadioOn = true;
	boolean mSim2RadioOn = true;
	boolean mSimRadioOn = true;
	
	private static final int LISTEN_PHONE_STATES = 1;//80794
    private static final int LISTEN_PHONE_NONE_STATES = 2;

    private StatusBarManager mStatusBarMgr;
    
    //Add by mtk80908. USED to control setEmptyText.
    private boolean mIsFirstLaunch = false;
    
    //HashMap for batch delete contacts. Add by mtk80908.
    //The default initial capacity of hashMap is 16, and it is too small.
    private HashMap<Long, Integer> mAllContacts = new HashMap<Long, Integer>(256);
    private HashMap<Long, Integer> mToBeDeletedContacts = new HashMap<Long, Integer>(128);
    private boolean mSelectAllToDel = false;
    private String mSelectAllText = null;
    private String mUnSelectText = null;
    private String mDeleteText = null;
    
    private SimAssociationQueryHandler mSimAssociationQueryHandler;

    final String[] sLookupProjection = new String[] {
            Contacts.LOOKUP_KEY
    };

    static {
        sContactsIdMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sContactsIdMatcher.addURI(ContactsContract.AUTHORITY, "contacts/#", CONTACTS_ID);
    }

	// new data fields for multi-deletion, by mtk80909
	private boolean mDeleteState = false;
    
	private boolean mAddContactState = false;
	private boolean mProviderStatusChange = false;
	private boolean mBeingDeleted = false;
        private boolean mDeletedFailed = false;
	private boolean mAllOrNone = false;
	private BatchDeleteClickListener mBatchDeleteClickListener;
	private boolean mToBeDeleted[];
	private Set<Integer> mToBeDeletedSet = new HashSet<Integer>();
	private ProgressDialog mMarkAllProgDialog = null;
	private static PhoneDisambigDialog mPhoneDisambigDialog = null;
	int mSelected = 0;
	// list item layout related indices, by mtk80909
	// relative layout, by mtk80909
	private Button mSelectAllButton;
	private CheckBox mSelectAllBox;
	private Button mDeleteButton;
	private LinearLayout mTopView;
	private LinearLayout mBottomView;

	private Button mGroupBtn;
	private ImageButton mSearchButton;
	private ImageButton mAddNewContactBtn;
	private ImageButton mListGalleryBtnButton;
	private ViewGroup groupView;
	private TextView favTitle;
//	private Button btnEmptyCopySim;

    private static AlertDialog mImportAlertDialog = null;
    private static AlertDialog mSdCardNtFdAlertDialog = null;
    private static AlertDialog mHideConfmAlertDialog = null;
    private static AlertDialog mDelConfm1AlertDialog = null;
    private static AlertDialog mDelConfm2AlertDialog = null;
    private static AlertDialog mDelConfm3AlertDialog = null;
    private static AlertDialog mDelConfm4AlertDialog = null;
    private static AlertDialog mDelConfm5AlertDialog = null; 
    private static AlertDialog mShowGroupsDialog = null; 
    private static AlertDialog mSelectSIMDialog = null;
    
    private static final int DISPLAYNAME_LENGTH = 18;	
    //for ALPS00129932
    private boolean mAdapterReady= false;
    
    private static final boolean COPY_CONTACTS_FEATURE_ENABLED = true;
    
    //
    private boolean queryAfterGroupDialogDissmiss = false;
    
    private static boolean mShouldClickPlusButton = true;
    private ProgressDialog mQueryDialog = null;
   // private final BroadcastReceiver mReceiver = new SimIndicatorBroadcastReceiver();

    
    /**
     * -1 -- for single SIM
     * 0  -- for gemini SIM 1
     * 1  -- for gemini SIM 2
     */
    private boolean isReadyForDelete(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
		if(null == iTel) return false;
        try {
	        if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {	// Gemini
	           	return iTel.hasIccCardGemini(slotId)
	            && iTel.isRadioOnGemini(slotId)
	            && !iTel.isFDNEnabledGemini(slotId)
	            && TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(slotId);
	        } else {	// Single SIM
	          	return iTel.hasIccCard()
	            && iTel.isRadioOn()
	            && !iTel.isFDNEnabled()
	            && TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimState();
	        } 
        } catch (RemoteException e) {
        	Log.w(TAG, "RemoteException!");
        	return false;
        }
    }
    
//    private int getSimIdFromIndicator(int indicator) {
//    	switch (indicator) {
//    	case RawContacts.INDICATE_SIM1: 
//    	case RawContacts.INDICATE_USIM1: //80794 for USIM
//    		return com.android.internal.telephony.Phone.GEMINI_SIM_1;
//    	case RawContacts.INDICATE_SIM2: 
//    	case RawContacts.INDICATE_USIM2: 
//    		return com.android.internal.telephony.Phone.GEMINI_SIM_2;
//    	default: return SINGLE_SIM_ID;
//    	}
//    }
    
//    private Uri getIccUri (int indicate) {// 80794
//    	if (indicate == RawContacts.INDICATE_SIM1) {
//    		return mIccUri1;
//    	} else if (indicate == RawContacts.INDICATE_SIM2) {
//    		return mIccUri2;
//    	} else {
//    		return mIccUri;
//    	}
//    }

    private class DeleteClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
           	Cursor cursor = (Cursor) getListAdapter().getItem(mPosition);
            Log.d(TAG, "DeleteClickListener, mPosition= "+ mPosition);
            if (1== mPosition){  //owner contact
                if (mSelectedContactUri != null) {
                                if(true == FeatureOption.MTK_SNS_SUPPORT)
                                {
                                    if(null != PhoneOwner.getInstance() && ContentUris.parseId(mSelectedContactUri) == PhoneOwner.getInstance().getOwnerID()){
                                if(null != ownerView) {
                                    PhoneOwner owner = PhoneOwner.getInstance();
                                                QuickContactBadge quickContact = ((ContactListItemView)ownerView).getQuickContact();
                                            if(null != quickContact) {
                                                quickContact.assignContactUri(Contacts.getLookupUri(owner == null ? (long)-1 : owner.getOwnerID(), 
                                                    owner == null ? null : owner.getOwnerLookupKey()));
                                                quickContact.setClickable(false);
                                            }
                                        ImageView ownerPhoto = quickContact;
                                        if(null != ownerPhoto) ownerPhoto.setImageResource(R.drawable.ic_contact_list_picture);
                                        TextView info = (TextView) ((ContactListItemView)ownerView).getNameTextView();
                                        if(null != info) {
                                        info.setText(ContactsListActivity.this.getResources().getString(R.string.owner_none));
                                            info.setVisibility(View.VISIBLE);
                                    }
                                
                                        ImageView snsLogo = (ImageView) ((ContactListItemView)ownerView).getSnsLogo();
                                    if(null != snsLogo) snsLogo.setVisibility(View.GONE);
                                
                                        TextView snsStatu = (TextView) ((ContactListItemView)ownerView).getSnsStatus();
                                    if(null != snsStatu) snsStatu.setVisibility(View.GONE);
                                }
                                    
                                SharedPreferences setting = getSharedPreferences(PREFS_NAME, 
                                    Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
                                    SharedPreferences.Editor editor = setting.edit();
                                    editor.clear();
                                    editor.commit();
                                    PhoneOwner.setOwner(null);
                                    }
                                }
                                getContentResolver().delete(mSelectedContactUri, null, null);
                            }
                return;

            }
            if (null == cursor) {
                return;
            }
            if ((0 == cursor.getCount()) || cursor.isAfterLast()) {
                return;
            }
            Uri uri = null;
    
            //if (mSelectedContactUri != null) {
        	 final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
		     if (iTel == null) return;
        	int simPhoneIndicate = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.INDICATE_PHONE_SIM));
        	mSlot = SIMInfo.getSlotById(ContactsListActivity.this, (long)simPhoneIndicate);
        	long rawContactId = -1;
        	String name = null;
        	String number = null;
        	Log.d(TAG, "DeleteClickListener simPhoneIndicate " + simPhoneIndicate + ", mPosition " + mPosition);
        	Log.i(TAG,"DeleteClickListener mSlot is " + mSlot);
            if (simPhoneIndicate > RawContacts.INDICATE_PHONE) {
                	uri = ContactsUtils.getUri(mSlot);
                if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {                	
                    try {
                        if (null != iTel && !iTel.isRadioOnGemini(mSlot)) {
                            Toast.makeText(ContactsListActivity.this,
                                    R.string.AirPlane_mode_on_delete,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                            if (null != iTel && iTel.isFDNEnabledGemini(mSlot)) {

                                Toast.makeText(ContactsListActivity.this,
                                		R.string.FDNEnabled_delete,
                                        Toast.LENGTH_SHORT).show();

                                return;
                            }
                            if (!(TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(mSlot))) {
                                Toast.makeText(ContactsListActivity.this,
                                        R.string.sim_invalid_delete,
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                } else {
                    try {
                        if (null != iTel && !iTel.isRadioOn()) {
                            Toast.makeText(ContactsListActivity.this,
                                    R.string.AirPlane_mode_on_delete,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    if (null != iTel && iTel.isFDNEnabled()) {

                        Toast.makeText(ContactsListActivity.this,
                        		R.string.FDNEnabled_delete,
                                Toast.LENGTH_SHORT).show();

                        return;
                }
                    if (!(TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimState())) {
                        Toast.makeText(ContactsListActivity.this,
                                R.string.sim_invalid_delete,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        	}
					
            }
        	if (simPhoneIndicate > RawContacts.INDICATE_PHONE) {
				if (mSelectedContactUri != null) {
					int contact_id = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts._ID));
					Log.i(TAG, "contact_id is " + contact_id);
					ContentResolver resolver = getContentResolver();
					rawContactId = ContactsUtils.queryForRawContactId(resolver, contact_id);
					Log.i(TAG, "rawContactId is " + rawContactId);
					String where = ContactsUtils.deleteSimContact(rawContactId, resolver);		
					Log.i(TAG,"mQueryHandler is "+mQueryHandler);
					Log.d(TAG, "uri " + uri);
					mQueryHandler = new QueryHandler(ContactsListActivity.this);
					Log.i(TAG,"where is " + where);
					mQueryHandler.startDelete(DELETE_TOKEN, null, uri, where, null);
					Log.d(TAG, "uri  again ******** " + uri);
					return;
				}
			} else if (mSelectedContactUri != null) {
                /*if(true == FeatureOption.MTK_SNS_SUPPORT)
                {
                	if(null != PhoneOwner.getInstance() && ContentUris.parseId(mSelectedContactUri) == PhoneOwner.getInstance().getOwnerID()){
				if(null != ownerView) {
					PhoneOwner owner = PhoneOwner.getInstance();
                    			QuickContactBadge quickContact = ((ContactListItemView)ownerView).getQuickContact();
                            if(null != quickContact) {
                                quickContact.assignContactUri(Contacts.getLookupUri(owner == null ? (long)-1 : owner.getOwnerID(), 
						            owner == null ? null : owner.getOwnerLookupKey()));
						        quickContact.setClickable(false);
                            }
		        		ImageView ownerPhoto = quickContact;
		        		if(null != ownerPhoto) ownerPhoto.setImageResource(R.drawable.ic_contact_list_picture);
		        		TextView info = (TextView) ((ContactListItemView)ownerView).getNameTextView();
		        		if(null != info) {
						info.setText(ContactsListActivity.this.getResources().getString(R.string.owner_none));
		        			info.setVisibility(View.VISIBLE);
					}
		        
		        		ImageView snsLogo = (ImageView) ((ContactListItemView)ownerView).getSnsLogo();
					if(null != snsLogo) snsLogo.setVisibility(View.GONE);
		        
		        		TextView snsStatu = (TextView) ((ContactListItemView)ownerView).getSnsStatus();
					if(null != snsStatu) snsStatu.setVisibility(View.GONE);
				}
		        	
				SharedPreferences setting = getSharedPreferences(PREFS_NAME, 
					Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
		        	SharedPreferences.Editor editor = setting.edit();
		        	editor.clear();
		        	editor.commit();
		        	PhoneOwner.setOwner(null);
                	}
                }*/
                getContentResolver().delete(mSelectedContactUri, null, null);
            }
        }
    }

	// batch contact deletion confirmation and operation, mtk80909
	private class BatchDeleteClickListener implements
			DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			mProgress = 0;
		    if (mProgressDialog != null && mProgressDialog.isShowing()) return;
			mProgressDialog = new DeleteProgressDialog(ContactsListActivity.this);
			if(null != mProgressDialog) {
				mProgressDialog.setTitle(R.string.batchContactDeleteProgress);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog.setMax(mSelected);
				mProgressDialog.setProgress(0);
				mThread = new BackgroundThread();
				mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
						ContactsListActivity.this
								.getString(android.R.string.cancel), mThread);
				mProgressDialog.show();
				mDeleteHandler = new DeleteHandler();
			mSelectAllButton.setEnabled(false);
			mSelectAllBox.setEnabled(false);
			mDeleteButton.setEnabled(false);
			
				mThread.start();
			}
		}

		private class DeleteHandler extends Handler {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
                if (mThread.mCanceled) { // when cancel thread, also dismiss mProgressDialog
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                                mProgressDialog = null;
                    }     
                    return;
                }
				if (msg.what == R.id.batch_contact_delete_message) {
					if (mProgress >= mProgressDialog.getMax()) {
						if(null != mProgressDialog) 
							mProgressDialog.dismiss();
						mSelectedContactUri = null;
						// quitDeleteState();
					} else {
						++mProgress;
						if(null != mProgressDialog) 
							mProgressDialog.incrementProgressBy(1);
					}
				} else if (msg.what == R.id.batch_contact_delete_fail_message) {
                    Toast.makeText(ContactsListActivity.this,
                                                        R.string.batch_delete_error,
                                                        Toast.LENGTH_SHORT).show();
				}
			}
		};

		public void cancelFromExternal() {
			if (mThread != null && mThread.isAlive()) {
				mThread.mCanceled = true;
                Log.d(TAG, "cancelFromExternal(), cancel thread ");
			}
            
		}

		private class BackgroundThread extends Thread implements
				DialogInterface.OnClickListener {
			@Override
			public void run() {
				mCanceled = false;
                mDeletedFailed = false;
				mBeingDeleted = true;
				Log.i(TAG, "run() calls unregister");
				boolean bSnsOwner = (FeatureOption.MTK_SNS_SUPPORT && !mSearchMode && mMode == MODE_DEFAULT);
				if (/*mToBeDeleted == null 
						||*/ mProgressDialog == null 
						|| mProgressDialog.getMax() != mSelected
						|| !mAdapter.isUpdateRegistered()) {
					if (mProgressDialog != null && mProgressDialog.isShowing()) {
						mProgressDialog.dismiss();
						mCanceled = true;
					}
				}
				if (!mAdapter.isUpdateRegistered()) return;
				mAdapter.unregisterUpdate();
				List<Uri> uriList = new ArrayList<Uri>();
				// where clause for SIM contact deleting
				List<String> whereList = new ArrayList<String>();
				ContentResolver contentResolver = getContentResolver();
				long rawContactId = -1;
				int simPhoneIndicator = -2;
				String name = null;				
				Uri uri = null;
				
				//Check delete mode: delete all or delete part
				if (mSelectAllToDel) {
					Set<Long> contactsIdSet = mAllContacts.keySet();
					for (Iterator<Long> it = contactsIdSet.iterator(); it.hasNext() && !mCanceled;) {
						int simDelResult = 1;
						Long contact_id = it.next();
						simPhoneIndicator = mAllContacts.get(contact_id);
//						String where = ContactsUtils.deleteSimContact(rawContactId, contentResolver);
						mSlot = SIMInfo.getSlotById(ContactsListActivity.this, (long)simPhoneIndicator);
						Log.i(TAG,"BackgroundThread simPhoneIndicator is " + simPhoneIndicator);
						Log.i(TAG,"BackgroundThread mSlot is " + mSlot);
						uri = ContactsUtils.getUri(mSlot);
						if (simPhoneIndicator > RawContacts.INDICATE_PHONE) {
							rawContactId = ContactsUtils.queryForRawContactId(contentResolver,
									contact_id.longValue());
							String where = ContactsUtils.deleteSimContact(rawContactId, contentResolver);
							if (isReadyForDelete(mSlot)) {
								simDelResult = getContentResolver().delete(uri, where, null);
							} else {
								simDelResult = -1;
	                            mDeletedFailed = true;
							}
						}
						if (simDelResult > 0) {
							mSelectedContactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contact_id);
	                       if (mSelectedContactUri != null) {
	                           getContentResolver().delete(mSelectedContactUri, null, null);
	                       }
	                    }
	                    //wait for database delete sync. if not sleep, the UI will not friend.
	                    try {
	                    Log.d(TAG,"Thread.sleep(150);");                
	                        if(it.hasNext()) {
	                            Thread.sleep(150);
	                            } else {
	                            Thread.sleep(500);
	                            }                       
	                    } catch (InterruptedException e) {
	                    }
						mDeleteHandler.sendEmptyMessage(R.id.batch_contact_delete_message);
					}
				} else {
					Set<Long> contactsIdSet = mToBeDeletedContacts.keySet();
					for (Iterator<Long> it = contactsIdSet.iterator(); it.hasNext() && !mCanceled;) {
						int simDelResult = 1;
						Long contact_id = it.next();
						simPhoneIndicator = mToBeDeletedContacts.get(contact_id);
//						String where = ContactsUtils.deleteSimContact(rawContactId, contentResolver);
						mSlot = SIMInfo.getSlotById(ContactsListActivity.this, (long)simPhoneIndicator);
						Log.i(TAG,"BackgroundThread simPhoneIndicator is " + simPhoneIndicator);
						Log.i(TAG,"BackgroundThread mSlot is " + mSlot);
						uri = ContactsUtils.getUri(mSlot);
						if (simPhoneIndicator > RawContacts.INDICATE_PHONE) {
							rawContactId = ContactsUtils.queryForRawContactId(contentResolver,
									contact_id.longValue());
							String where = ContactsUtils.deleteSimContact(rawContactId, contentResolver);
							if (isReadyForDelete(mSlot)) {
								simDelResult = getContentResolver().delete(uri, where, null);
							} else {
								simDelResult = -1;
	                            mDeletedFailed = true;
							}
						}
						if (simDelResult > 0) {
							mSelectedContactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contact_id);
	                       if (mSelectedContactUri != null) {
	                           getContentResolver().delete(mSelectedContactUri, null, null);
	                       }
	                    }
	                    //wait for database delete sync. if not sleep, the UI will not friend.
	                    try {
	                    Log.d(TAG,"Thread.sleep(150);");                
	                        if(it.hasNext()) {
	                            Thread.sleep(150);
	                            } else {
	                            Thread.sleep(500);
	                            }                       
	                    } catch (InterruptedException e) {
	                    }
						mDeleteHandler.sendEmptyMessage(R.id.batch_contact_delete_message);
					}
				}
				
				
//				for (Iterator<Integer> it = mToBeDeletedSet.iterator(); it.hasNext() && !mCanceled; /**/) {
//					int simDelResult = 1;
//					int position = it.next();
//					Cursor cursor = (Cursor) getListAdapter().getItem(position);
//					int simPhoneIndicator = cursor
//							.getInt(cursor.getColumnIndexOrThrow(Contacts.INDICATE_PHONE_SIM));
//					mSlot = SIMInfo.getSlotById(ContactsListActivity.this, (long)simPhoneIndicator);
//
//					Log.i(TAG,"BackgroundThread simPhoneIndicator is " + simPhoneIndicator);
//					Log.i(TAG,"BackgroundThread mSlot is " + mSlot);
//					uri = ContactsUtils.getUri(mSlot);
//					if (simPhoneIndicator > RawContacts.INDICATE_PHONE) {
//						int contact_id = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts._ID));
//						//ContentResolver resolver = getContentResolver();
//						rawContactId = ContactsUtils.queryForRawContactId(contentResolver,
//							contact_id);
//						String where = ContactsUtils.deleteSimContact(rawContactId, contentResolver);
//						if (isReadyForDelete(mSlot)) {
//							simDelResult = getContentResolver().delete(uri, where, null);
//						} else {
//							simDelResult = -1;
//                            mDeletedFailed = true;
//						}
//					}
//					if (simDelResult > 0) {
//						mSelectedContactUri = getContactUriFromRealPosition(position);
//                       if (mSelectedContactUri != null) {
//                           getContentResolver().delete(mSelectedContactUri, null, null);
//                       }
//                    }     
//                    
//                    //wait for database delete sync. if not sleep, the UI will not friend.
//                    try {
//                    Log.d(TAG,"Thread.sleep(150);");                
//                        if(it.hasNext()) {
//                            Thread.sleep(150);
//                            } else {
//                            Thread.sleep(500);
//                            }                       
//                    } catch (InterruptedException e) {
//                    }
//					mDeleteHandler.sendEmptyMessage(R.id.batch_contact_delete_message);
//					
//				}
				
				mBeingDeleted = false;
				if (!mCanceled) {
					mDeleteHandler
							.sendEmptyMessage(R.id.batch_contact_delete_message);
				}
                if (mDeletedFailed) {
					mDeleteHandler
							.sendEmptyMessage(R.id.batch_contact_delete_fail_message);
                }
				Log.i(TAG, "run() calls register");
				mAdapter.registerUpdate();
			}

			public boolean mCanceled;

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (which == DialogInterface.BUTTON_NEGATIVE) {
					mCanceled = true;
					// mProgressDialog.dismiss();
				}
			}
		}

		private class DeleteProgressDialog extends ProgressDialog {
			public DeleteProgressDialog(Context context) {
				super(context);
				// TODO Auto-generated constructor stub
			}

			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				switch (keyCode) {
				case KeyEvent.KEYCODE_BACK:
					mThread.mCanceled = true;
					dismiss();
					return true;
				}
				return super.onKeyDown(keyCode, event);
			}

			@Override
			protected void onStop() {
				// TODO Auto-generated method stub
				try {
					mThread.join();
				} catch (InterruptedException e) {
					Log.w(TAG, "Deletion interrupted...");
				}
				if (mAdapter != null && !mAdapter.isUpdateRegistered()) {
					mAdapter.registerUpdate();
				}
				quitDeleteState();
				super.onStop();
			}

		};

		private Uri getContactUriFromRealPosition(int position) {
			// Log.i(TAG, "getContactUriFromRealPosition: position = " +
			// position);
			final Cursor cursor = (Cursor) mAdapter.getItem(position);

			final long contactId = null == cursor ? null : cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
			final String lookupKey = null == cursor ? null : cursor
					.getString(SUMMARY_LOOKUP_KEY_COLUMN_INDEX);

			if (lookupKey == null) {
				return Contacts.getLookupUri(contactId, lookupKey);
			} else {
				Uri lookupUri = Contacts.getLookupUri(contactId, lookupKey);
				return Contacts.lookupContact(getContentResolver(), lookupUri);
			}
		}

		private int mProgress;
		private BackgroundThread mThread;
		private Handler mDeleteHandler;
		private ProgressDialog mProgressDialog;
    }

    // modified by Ivan 2010-08-11
    private View ownerView;
    public static final String PREFS_NAME = "owner_info";
    //public static Long ownerID = null;
    //public static String ownerLookupKey = null;
    //public static String ownerName = null;
    public static final String PREFS_OWNER_ID = "owner_id";
    public static final String PREFS_OWNER_LOOKUPKEY = "owner_lookupkey";
    public static final String PREFS_OWNER_NAME = "owner_name";
    private int mIndexChange = 0;
    
    private Context mContaxt;
    
    private boolean bIsOwnerShow = false;
    
    /**
     * A {@link TextHighlightingAnimation} that redraws just the contact display name in a
     * list item.
     */
    private static class NameHighlightingAnimation extends TextHighlightingAnimation {
        private final ListView mListView;

        private NameHighlightingAnimation(ListView listView, int duration) {
            super(duration);
            this.mListView = listView;
        }

        /**
         * Redraws all visible items of the list corresponding to contacts
         */
        @Override
        protected void invalidate() {
            int childCount = mListView.getChildCount();
	    View itemView;
	    ContactListItemView view;
            for (int i = 0; i < childCount; i++) {
                itemView = mListView.getChildAt(i);
                if (itemView instanceof ContactListItemView) {
                    view = (ContactListItemView)itemView;
                    view.getNameTextView().invalidate();
                }
            }
        }

        @Override
        protected void onAnimationStarted() {
            mListView.setScrollingCacheEnabled(false);
        }

        @Override
        protected void onAnimationEnded() {
            mListView.setScrollingCacheEnabled(true);
        }
    }

    // The size of a home screen shortcut icon.
    private int mIconSize;
    private ContactsPreferences mContactsPrefs;
    private int mDisplayOrder;
    private int mSortOrder;
    private boolean mHighlightWhenScrolling;
    private TextHighlightingAnimation mHighlightingAnimation;
    private SearchEditText mSearchEditText;

    private static final int KAIXIN_TYPE = 1;
    private static final int RENREN_TYPE = 2;
    private static final int TWITTER_TYPE = 3;
    private static final int FLICKR_TYPE = 4;
    private static final int FACEBOOK_TYPE = 5;

    boolean mForeground = true;
    boolean mForceQuery = false;
    boolean mShowSimIndicator = false;
    /**
     * An approximation of the background color of the pinned header. This color
     * is used when the pinned header is being pushed up.  At that point the header
     * "fades away".  Rather than computing a faded bitmap based on the 9-patch
     * normally used for the background, we will use a solid color, which will
     * provide better performance and reduced complexity.
     */
    private int mPinnedHeaderBackgroundColor;
    private CheckedTextView mSelectAll;

    private ContentObserver mProviderStatusObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            checkProviderState(true);
        }
    };
	private ProgressDialog mChangeLocaleDialog;
    private Handler handler = new Handler(Looper.getMainLooper());
    private class LoadUserPreferenceHandler extends Handler {
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		if(true == FeatureOption.MTK_SNS_SUPPORT) {
			if(mMode == MODE_DEFAULT && !mSearchMode)
				setOwnerView();
		}
	}
    };

    private LoadUserPreferenceHandler loadUserPreferenceHandler = new LoadUserPreferenceHandler();

    private ContentObserver observer = new ContentObserver(handler) {
    	public void onChange(boolean selfChange){
		ListView list = getListView();
		if(list == null)
			return;
		if(selfChange){
      			final LayoutInflater inflater = getLayoutInflater();
        		if(ownerView == null)
				ownerView = (View) inflater.inflate(R.layout.contacts_list_item_owner, list, false);
        		PhoneOwner owner = PhoneOwner.getInstance();
        		QuickContactBadge quickContact = ((ContactListItemView)ownerView).getQuickContact();
                if(null != quickContact) {
                    quickContact.assignContactUri(Contacts.getLookupUri(owner == null ? (long)-1 : owner.getOwnerID(), 
				        owner == null ? null : owner.getOwnerLookupKey()));
				    if(null == owner)
				        quickContact.setClickable(false);
				    else
				        quickContact.setClickable(true);
				}
                if (mOwnerLoader != null)
                	mOwnerLoader.loadOwner(ownerView, owner);
		}
    	}
    };

    private Handler initCompleteHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		super.handleMessage(msg);
    		if (mAdapter != null) mAdapter.notifyDataSetChanged();
    	}
    };
    
    class SnsHandler extends Handler{
    	public SnsHandler() {
    		
    	}
    	
    	public SnsHandler(Looper looper) {
    		super(looper);
    	}
    	
    	public void handleMessage(Message msg) {
    		//Do SNS post-processing after contacts launching
    		//The sleeping time is better to be set as contacts launching time,
    		//but now set a much larger value for test.
    		//This values will be changed in future.
            Log.d(TAG, "SnsHandler sleep ");
            try {
                Thread.currentThread().sleep(3000);
            } catch (Exception e) {
            	
            }
            Log.d(TAG, "SnsHandler ------------------------------------------------------------------------ wake up now!!!!!!");
    	    bIsOwnerShow = false;
    		
    	    ContactsListActivity.this.runOnUiThread(
    	        new Runnable() {
    		    public void run() {
                        Log.d(TAG, "SnsHandler ui thread begin");
                        mSnsLoader = new ContactSnsLoader(ContactsListActivity.this);
    			mOwnerLoader = new ContactOwnerLoader(ContactsListActivity.this, R.drawable.ic_contact_list_picture, 
    			ContactsListActivity.this.getResources().getString(R.string.owner_none));
                        Log.d(TAG, "SnsHandler ui thread end");
    		    }
    		}
    	    );
    	    InitLoaderThread initLoaderThread = new InitLoaderThread(ContactsListActivity.this);
    	    initLoaderThread.setPriority(Thread.MIN_PRIORITY);
    	    initLoaderThread.start();
    		
    	    InitPhoneOwnerThread initPhoneOwnerThread = new InitPhoneOwnerThread(ContactsListActivity.this);
    	    initPhoneOwnerThread.start();
            Log.d(TAG, "SnsHandler Completed");
            
            mSimInfoWrapper = ContactsUtils.SIMInfoWrapper.getDefault(ContactsListActivity.this,false);
    	}
    }
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.i(TAG, "onCreate() ----------------------------------------------------------------- begin");
                // for modem switch
        // Batch delete should be interrupted to avoid weird behaviors
        if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
//        	if (FeatureOption.MTK_GEMINI_SUPPORT && mMode == MODE_DEFAULT && !mSearchMode) {
        		IntentFilter modemSwitchIntentFilter = new IntentFilter(GeminiPhone.EVENT_PRE_3G_SWITCH);
        		registerReceiver(mModemSwitchReceiver, modemSwitchIntentFilter);
        		Log.i(TAG,"onCreate() mModemSwitchReceiver is " + mModemSwitchReceiver);
//        	}
        }
        mAdapterReady= false;
        /*if (FeatureOption.MTK_GEMINI_SUPPORT) {
            IntentFilter intentFilter =
                new IntentFilter(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
            registerReceiver(mReceiver, intentFilter);   
        }*/
        mContaxt = this;
/*	if(true == FeatureOption.MTK_SNS_SUPPORT)
	{
		// modified by Ivan 2010-08-31
		bIsOwnerShow = false;
		mSnsLoader = new ContactSnsLoader(this);
		mOwnerLoader = new ContactOwnerLoader(this, R.drawable.ic_contact_list_picture, 
			this.getResources().getString(R.string.owner_none));
		InitLoaderThread initLoaderThread = new InitLoaderThread(this);
		initLoaderThread.setPriority(Thread.MIN_PRIORITY);
		initLoaderThread.start();
	}*/
        Log.d(TAG, "send message to SnsHandler begin");
        if (FeatureOption.MTK_SNS_SUPPORT) {
            HandlerThread handlerThread = new HandlerThread("snsThread");
            handlerThread.start();
            SnsHandler snsHandler = new SnsHandler(handlerThread.getLooper());
            Message msg = snsHandler.obtainMessage();
            msg.sendToTarget();
        }
        Log.d(TAG, "send message to SnsHandler end");

        mIndexChange = 0;
        mIconSize = getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
        mContactsPrefs = new ContactsPreferences(this);
		/*mPhotoLoader = new ContactPhotoLoader(this,
				R.drawable.ic_contact_list_picture);*/
        //New Photo Loader and Pause it when launching contacts.
		mPhotoLoader = new ContactPhotoLoader(this, R.drawable.contacts_unknown_image);
		mPhotoLoader.pause();
        // Resolve the intent
        final Intent intent = getIntent();

        // Allow the title to be set to a custom String using an extra on the intent
        String title = intent.getStringExtra(UI.TITLE_EXTRA_KEY);
        if (title != null) {
            setTitle(title);
        }

        String action = intent.getAction();
        String component = intent.getComponent().getClassName();

        // When we get a FILTER_CONTACTS_ACTION, it represents search in the context
        // of some other action. Let's retrieve the original action to provide proper
        // context for the search queries.
        if (UI.FILTER_CONTACTS_ACTION.equals(action)) {
            mSearchMode = true;
            mShowSearchSnippets = true;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mInitialFilter = extras.getString(UI.FILTER_TEXT_EXTRA_KEY);
                String originalAction =
                        extras.getString(ContactsSearchManager.ORIGINAL_ACTION_EXTRA_KEY);
                if (originalAction != null) {
                    action = originalAction;
                }
                String originalComponent =
                        extras.getString(ContactsSearchManager.ORIGINAL_COMPONENT_EXTRA_KEY);
                if (originalComponent != null) {
                    component = originalComponent;
                }
                Uri data = extras.getParcelable(ContactsSearchManager.ORIGINAL_DATA_EXTRA_KEY);
                Log.i(TAG, "original data " + data);
                if (data != null) {
                    intent.setData(data);
                }
                String type = extras.getString(ContactsSearchManager.ORIGINAL_TYPE_EXTRA_KEY);
                Log.i(TAG, "original type " + type);
                if (type != null) {
                    intent.setType(type);
                }
            } else {
                mInitialFilter = null;
            }
        }

        Log.i(TAG, "Called with action: " + action);
        mMode = MODE_UNKNOWN;
        if (UI.LIST_DEFAULT.equals(action) || UI.FILTER_CONTACTS_ACTION.equals(action)) {
            mMode = MODE_DEFAULT;
            // When mDefaultMode is true the mode is set in onResume(), since the preferneces
            // activity may change it whenever this activity isn't running
        } else if (UI.LIST_GROUP_ACTION.equals(action)) {
            mMode = MODE_GROUP;
            String groupName = intent.getStringExtra(UI.GROUP_NAME_EXTRA_KEY);
            if (TextUtils.isEmpty(groupName)) {
                finish();
                return;
            }
            buildUserGroupUri(groupName);
        } else if (UI.LIST_ALL_CONTACTS_ACTION.equals(action)) {
            mMode = MODE_CUSTOM;
            mDisplayOnlyPhones = false;
        } else if (UI.LIST_STARRED_ACTION.equals(action)) {
            mMode = mSearchMode ? MODE_DEFAULT : MODE_STARRED;
        } else if (UI.LIST_FREQUENT_ACTION.equals(action)) {
            mMode = mSearchMode ? MODE_DEFAULT : MODE_FREQUENT;
        } else if (UI.LIST_STREQUENT_ACTION.equals(action)) {
            mMode = mSearchMode ? MODE_DEFAULT : MODE_STREQUENT;
        } else if (UI.LIST_CONTACTS_WITH_PHONES_ACTION.equals(action)) {
            mMode = MODE_CUSTOM;
            mDisplayOnlyPhones = true;
        } else if (Intent.ACTION_PICK.equals(action)) {
            // XXX These should be showing the data from the URI given in
            // the Intent.
            final String type = intent.resolveType(this);
            if (Contacts.CONTENT_TYPE.equals(type)) {
                mMode = MODE_PICK_CONTACT;
            } else if (People.CONTENT_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_PERSON;
            } else if (Phone.CONTENT_TYPE.equals(type)) {
                mMode = MODE_PICK_PHONE;
            } else if (Phones.CONTENT_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_PHONE;
            } else if (StructuredPostal.CONTENT_TYPE.equals(type)) {
                mMode = MODE_PICK_POSTAL;
            } else if (ContactMethods.CONTENT_POSTAL_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_POSTAL;
            }
        } else if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
            if (component.equals("alias.DialShortcut")) {
                mMode = MODE_PICK_PHONE;
                mShortcutAction = Intent.ACTION_CALL;
                mShowSearchSnippets = false;
                setTitle(R.string.callShortcutActivityTitle);
            } else if (component.equals("alias.MessageShortcut")) {
                mMode = MODE_PICK_PHONE;
                mShortcutAction = Intent.ACTION_SENDTO;
                mShowSearchSnippets = false;
                setTitle(R.string.messageShortcutActivityTitle);
            } else if (mSearchMode) {
                mMode = MODE_PICK_CONTACT;
                mShortcutAction = Intent.ACTION_VIEW;
                mFilterSIMContact = true;
                setTitle(R.string.shortcutActivityTitle);
            } else {
                mMode = MODE_PICK_OR_CREATE_CONTACT;
                mFilterSIMContact = true;
                mShortcutAction = Intent.ACTION_VIEW;
                setTitle(R.string.shortcutActivityTitle);
            }
        } else if (Intent.ACTION_GET_CONTENT.equals(action)) {
            final String type = intent.resolveType(this);
            if (Contacts.CONTENT_ITEM_TYPE.equals(type)) {
               mFilterSIMContact = getIntent().getBooleanExtra(FILTER_SIM_CONTACT_KEY, false);
               mBroadcastResult = getIntent().getBooleanExtra(BROADCAST_RESULT, false);
                if (mSearchMode) {
                    mMode = MODE_PICK_CONTACT;
                } else {
                    mMode = MODE_PICK_OR_CREATE_CONTACT;
                }
            } else if (Phone.CONTENT_ITEM_TYPE.equals(type)) {
                mMode = MODE_PICK_PHONE;
                boolean requestEmail = getIntent().getBooleanExtra("request_email", false);
                Log.i(TAG, "request email " + requestEmail);
                if (requestEmail) {
                    mMode = MODE_PICK_PHONE_EMAIL;
                }
            } else if (Phones.CONTENT_ITEM_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_PHONE;
            } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(type)) {
                mMode = MODE_PICK_POSTAL;
            } else if (ContactMethods.CONTENT_POSTAL_ITEM_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_POSTAL;
            }  else if (People.CONTENT_ITEM_TYPE.equals(type)) {
                if (mSearchMode) {
                    mMode = MODE_LEGACY_PICK_PERSON;
                } else {
                    mMode = MODE_LEGACY_PICK_OR_CREATE_PERSON;
                }
            }

        } else if (Intent.ACTION_INSERT_OR_EDIT.equals(action)) {
            mMode = MODE_INSERT_OR_EDIT_CONTACT;
            mAddContactState = true;
            mSearchMode = true;
            mShowSearchSnippets = true;
            mInitialFilter = "";
        } else if (Intent.ACTION_SEARCH.equals(action)) {
            // See if the suggestion was clicked with a search action key (call button)
            if ("call".equals(intent.getStringExtra(SearchManager.ACTION_MSG))) {
                String query = intent.getStringExtra(SearchManager.QUERY);
                if (!TextUtils.isEmpty(query)) {
                    Intent newIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                            Uri.fromParts("tel", query, null));
                    startActivity(newIntent);
                }
                finish();
                return;
            }

            // See if search request has extras to specify query
            if (intent.hasExtra(Insert.EMAIL)) {
                mMode = MODE_QUERY_PICK_TO_VIEW;
                mQueryMode = QUERY_MODE_MAILTO;
                mInitialFilter = intent.getStringExtra(Insert.EMAIL);
            } else if (intent.hasExtra(Insert.PHONE)) {
                mMode = MODE_QUERY_PICK_TO_VIEW;
                mQueryMode = QUERY_MODE_TEL;
                mInitialFilter = intent.getStringExtra(Insert.PHONE);
            } else {
                // Otherwise handle the more normal search case
                mMode = MODE_QUERY;
                mShowSearchSnippets = true;
                mInitialFilter = getIntent().getStringExtra(SearchManager.QUERY);
            }
            mSearchResultsMode = true;
            mSearchMode = false;
        } else if (ACTION_SEARCH_INTERNAL.equals(action)) {
            String originalAction = null;
            String type = null;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                originalAction = extras.getString(ContactsSearchManager.ORIGINAL_ACTION_EXTRA_KEY);
                type = extras.getString(ContactsSearchManager.ORIGINAL_TYPE_EXTRA_KEY);
            }
            mShortcutAction = intent.getStringExtra(SHORTCUT_ACTION_KEY);
            mFilterSIMContact = intent.getBooleanExtra(FILTER_SIM_CONTACT_KEY, false);
            mBroadcastResult = intent.getBooleanExtra(BROADCAST_RESULT, false);
            if (Intent.ACTION_INSERT_OR_EDIT.equals(originalAction)) {
                mMode = MODE_QUERY_PICK_TO_EDIT;
                mShowSearchSnippets = true;
                mInitialFilter = getIntent().getStringExtra(SearchManager.QUERY);
            } else if (mShortcutAction != null && intent.hasExtra(Insert.PHONE)) {
                mMode = MODE_QUERY_PICK_PHONE;
                mQueryMode = QUERY_MODE_TEL;
                mInitialFilter = intent.getStringExtra(Insert.PHONE);
            } else if (type != null && Phone.CONTENT_ITEM_TYPE.equals(type)) {
                mMode = MODE_QUERY_PICK_PHONE;
                if (intent.getBooleanExtra("request_email", false)) {
                    mMode = MODE_QUERY_PICK_PHONE_EMAIL;
                }
                mQueryMode = QUERY_MODE_NONE;
                mShowSearchSnippets = true;
                mInitialFilter = getIntent().getStringExtra(SearchManager.QUERY);
            } else {
                mMode = MODE_QUERY_PICK;
                mQueryMode = QUERY_MODE_NONE;
                mShowSearchSnippets = true;
                mInitialFilter = getIntent().getStringExtra(SearchManager.QUERY);
            }
            mSearchResultsMode = true;
        // Since this is the filter activity it receives all intents
        // dispatched from the SearchManager for security reasons
        // so we need to re-dispatch from here to the intended target.
        } else if (Intents.SEARCH_SUGGESTION_CLICKED.equals(action)) {
            Uri data = intent.getData();
            Uri telUri = null;
            if (sContactsIdMatcher.match(data) == CONTACTS_ID) {
                long contactId = Long.valueOf(data.getLastPathSegment());
                final Cursor cursor = queryPhoneNumbers(contactId);
                if (cursor != null) {
                    if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                        int phoneNumberIndex = cursor.getColumnIndex(Phone.NUMBER);
                        String phoneNumber = cursor.getString(phoneNumberIndex);
                        telUri = Uri.parse("tel:" + phoneNumber);
                    }
                    cursor.close();
                }
            }
            // See if the suggestion was clicked with a search action key (call button)
            Intent newIntent;
            if ("call".equals(intent.getStringExtra(SearchManager.ACTION_MSG)) && telUri != null) {
                newIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED, telUri);
            } else {
                newIntent = new Intent(Intent.ACTION_VIEW, data);
            }
            startActivity(newIntent);
            finish();
            return;
        } else if (Intents.SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED.equals(action)) {
            //Intent newIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED, intent.getData());
            //startActivity(newIntent);
            //finish();
             Uri srcuri = intent.getData();	     
		     String number =  srcuri.getSchemeSpecificPart();
             Uri phoneUri = Uri.fromParts("tel", number, null);
             Intent EnterDialerIntent = new Intent(Intent.ACTION_DIAL, phoneUri);
             startActivity(EnterDialerIntent);
             finish();
            //return;
        } else if (Intents.SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED.equals(action)) {
            // TODO actually support this in EditContactActivity.
            String number = intent.getData().getSchemeSpecificPart();
            Intent newIntent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
            newIntent.putExtra(Intents.Insert.PHONE, number);
            startActivity(newIntent);
            finish();
            return;
        }

        if (JOIN_AGGREGATE.equals(action)) {
            if (mSearchMode) {
                mMode = MODE_PICK_CONTACT;
            } else {
                mMode = MODE_JOIN_CONTACT;
                mQueryAggregateId = intent.getLongExtra(EXTRA_AGGREGATE_ID, -1);
                if (mQueryAggregateId == -1) {
                    Log.e(TAG, "Intent " + action + " is missing required extra: "
                            + EXTRA_AGGREGATE_ID);
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }

        if (mMode == MODE_UNKNOWN) {
            mMode = MODE_DEFAULT;
        }
        // MTK : For Group mode we do not show the number of contact in a textview.
        if (((mMode & MODE_MASK_SHOW_NUMBER_OF_CONTACTS) != 0 || mSearchMode)
                && !mSearchResultsMode) {
            mShowNumberOfContacts = true;
        }

        if (mMode == MODE_JOIN_CONTACT) {
            setContentView(R.layout.contacts_list_content_join);
            TextView blurbView = (TextView)findViewById(R.id.join_contact_blurb);
            String blurb;
            String strDisplayName = getContactDisplayName(mQueryAggregateId);
            if (strDisplayName.length()>DISPLAYNAME_LENGTH){
                String strTemp = strDisplayName.subSequence(0,DISPLAYNAME_LENGTH).toString();
                strTemp = strTemp + "...";
               blurb= getString(R.string.blurbJoinContactDataWith,strTemp);         
            }
            else{   
                blurb = getString(R.string.blurbJoinContactDataWith,strDisplayName);         
            }
            blurbView.setText(blurb);
            mJoinModeShowAllContacts = true;
        } else if (mSearchMode) {
            setContentView(R.layout.contacts_search_content);
        } else if (mSearchResultsMode) {
            setContentView(R.layout.contacts_list_search_results);
            TextView titleText = (TextView)findViewById(R.id.search_results_for);
            titleText.setText(Html.fromHtml(getString(R.string.search_results_for,
                    "<b>" + mInitialFilter + "</b>")));
        } else {
            setContentView(R.layout.contacts_list_content);
			mSelectAllButton = (Button) findViewById(R.id.all_button);
			mSelectAllBox = (CheckBox) findViewById(R.id.all_check);
			mDeleteButton = (Button) findViewById(R.id.delete_button);
			mTopView = (LinearLayout) findViewById(R.id.all_layout);
			mBottomView = (LinearLayout) findViewById(R.id.delete_layout);
            groupView = (ViewGroup)findViewById(R.id.top_view);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this
            .getListView().getLayoutParams();
            params.height = 0;
            if (mMode == MODE_STREQUENT) {
                if (groupView != null) {
                    groupView.setVisibility(View.GONE);
                }
                favTitle = (TextView)findViewById(R.id.fav_title);
                favTitle.setVisibility(View.VISIBLE);
            }
            else{
                params.addRule(RelativeLayout.BELOW, groupView
	            .getId());
            }
            getListView().setLayoutParams(params);
            
            mGroupBtn = (Button) findViewById(R.id.group_button);
            mSearchButton = (ImageButton) findViewById(R.id.search_contact);
            mAddNewContactBtn = (ImageButton) findViewById(R.id.add_new_contact);
		mListGalleryBtnButton=(ImageButton) findViewById(R.id.contact_list_gallery_but);
            if (mGroupBtn != null) {
                mGroupBtn.setOnClickListener(this);
            }
 		mListGalleryBtnButton.setOnClickListener(this);
            mSearchButton.setOnClickListener(this);
            
            if (mAddNewContactBtn != null) {
            mAddNewContactBtn.setOnClickListener(this);
            }

//            Button btnEmptyNewContact = (Button)findViewById(R.id.btn_new_contact);
//            btnEmptyNewContact.setOnClickListener(this);
            
//            btnEmptyCopySim = (Button)findViewById(R.id.btn_copyfromsimcard);
//            btnEmptyCopySim.setOnClickListener(this);

			if(null != mTopView) mTopView.setVisibility(View.GONE);
			if(null != mBottomView) mBottomView.setVisibility(View.GONE);
			if(null != mSelectAllButton) mSelectAllButton.setOnClickListener(this);
			if(null != mSelectAllBox) mSelectAllBox.setOnClickListener(this);
			if(null != mDeleteButton) mDeleteButton.setOnClickListener(this);
			mIsFirstLaunch = true;
        }
        getListView().setCacheColorHint(Color.WHITE);

/*	if(true == FeatureOption.MTK_SNS_SUPPORT)
	{
		InitPhoneOwnerThread initPhoneOwnerThread = new InitPhoneOwnerThread(this);
		initPhoneOwnerThread.start();
	}*/
        

        setupListView();
        if (mSearchMode) {
            setupSearchView();
        }

        mQueryHandler = new QueryHandler(this);
        mJustCreated = true;
      
        mForeground = true;
        mForceQuery = false;
        
        mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        QuickContactWindow.setTarget(this);
        if (mMode == MODE_INSERT_OR_EDIT_CONTACT) {
            setEmptyText();
            Log.d(TAG,"onSearchTextChanged");
            Filter filter = mAdapter.getFilter();
            filter.filter(getTextFilter());
        }
        mCellConnMgr = new CellConnMgr();
		mCellConnMgr.register(getApplicationContext());
		
		mSimAssociationQueryHandler = new SimAssociationQueryHandler(this, getContentResolver());
		//Move it to message handler.
		//mSimInfoWrapper = ContactsUtils.SIMInfoWrapper.getDefault(this,false);
        Log.i(TAG, "onCreate() ----------------------------------------------------------------- end");
    }
    
    // for modem switch
    private BroadcastReceiver mModemSwitchReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// try to stop the batch delete
			Log.i(TAG, "mBatchDeleteClickListener = " + mBatchDeleteClickListener);
			
			
	        if (mBatchDeleteClickListener != null) {
	        	mBatchDeleteClickListener.cancelFromExternal();
	        }
		}
    	
    };

    void setSimIndicatorVisibility(boolean visible) {
        if(visible)
            mStatusBarMgr.showSIMIndicator(getComponentName(), Settings.System.VOICE_CALL_SIM_SETTING);
        else
            mStatusBarMgr.hideSIMIndicator(getComponentName());
    }

    private class InitLoaderThread extends Thread {
	
	private Context mContext = null;

	public InitLoaderThread(Context context){
		mContext = context;
	}

	public void run() {
		if(true == FeatureOption.MTK_SNS_SUPPORT)
		{
			// modified by Ivan 2010-08-31
			Looper.prepare();
			if(null == ContactsManager.SNS_TYPE_LIST)
				ContactsManager.readSnsTypeList(mContext.getResources());
			EmotionParser.init(0, mContext);
			EmotionParser emoIns;
			emoIns = EmotionParser.getInstance();
			if(null != emoIns) MTKSNSEmotionParser.create(emoIns.getEmotionNum(0),emoIns.getEmotionType(), emoIns.getEmotionTexts(0), emoIns.getEmotionData());
			initCompleteHandler.sendEmptyMessage(0);
		}
	}
    }

    private class InitPhoneOwnerThread extends Thread {
	
	private Context mContext = null;

	public InitPhoneOwnerThread(Context context){
		mContext = context;
	}

	public void run() {
		if(true == FeatureOption.MTK_SNS_SUPPORT && !mSearchMode)
		{
			// modified by Ivan 2010-08-31
			PhoneOwner.setOwner(null);
			SharedPreferences setting = mContext.getSharedPreferences(PREFS_NAME,
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
			long ownerid = setting.getLong(PREFS_OWNER_ID, (long)-1);
			String ownerLookupKey = setting.getString(PREFS_OWNER_LOOKUPKEY, null);
			String ownerName = setting.getString(PREFS_OWNER_NAME, null);
			PhoneOwner.initPhoneOwner(ownerid, ownerLookupKey);
		
			if(PhoneOwner.getInstance() != null){
				if(null == ownerName || ownerName.length() == 0)
					ownerName = mContext.getText(android.R.string.unknownName) + "";
				PhoneOwner.getInstance().setName(ownerName);
			}
			mContext.getContentResolver().registerContentObserver(SNSContact.Account.CONTENT_URI, false, observer);
			loadUserPreferenceHandler.sendEmptyMessage(0);
		}
	}
    }

    /**
     * setOwnerView method is used to set Owner List Item View
     * but this will is always exists, even if there is no such contact,
     * if there is no owner contact, this view print "Contact of myself"
     * if there is owner contact, this view print owner name and his status.
     * 
     * @param list: is the list parent list of ownerView
     */
    private void setOwnerView(){
    	if(false == FeatureOption.MTK_SNS_SUPPORT)
    	{
    		return;
    	}
    	final LayoutInflater inflater = getLayoutInflater();
        if(ownerView == null)
		ownerView = new ContactListItemView(this, null); //(View) inflater.inflate(R.layout.contacts_list_item_owner, list, false);

	PhoneOwner owner = PhoneOwner.getInstance();
        QuickContactBadge quickContact = ((ContactListItemView)ownerView).getQuickContact();
	    if(null != quickContact) {
	        quickContact.assignContactUri(Contacts.getLookupUri(owner == null ? (long)-1 : owner.getOwnerID(), 
		        owner == null ? null : owner.getOwnerLookupKey()));
		    if(null == owner)
		        quickContact.setClickable(false);
		    else
                quickContact.setClickable(true);
		}
	ImageView ownerPhoto = quickContact;
	if(null != ownerPhoto) ownerPhoto.setImageResource(R.drawable.ic_contact_list_picture);
	
	TextView info = (TextView) ((ContactListItemView)ownerView).getNameTextView();
        if(null != info) {
		info.setText(this.getResources().getString(R.string.owner_none));
        	info.setVisibility(View.VISIBLE);
	}

	ImageView snsLogo = (ImageView) ((ContactListItemView)ownerView).getSnsLogo();
        if(null != snsLogo) snsLogo.setVisibility(View.GONE);
        
        TextView snsStatu = (TextView) ((ContactListItemView)ownerView).getSnsStatus();
        if(null != snsStatu) snsStatu.setVisibility(View.GONE);
	if (mOwnerLoader != null)
		mOwnerLoader.loadOwner(ownerView, owner);

	bIsOwnerShow = true;
    }
    
    /**
     * Register an observer for provider status changes - we will need to
     * reflect them in the UI.
     */
    private void registerProviderStatusObserver() {
        getContentResolver().registerContentObserver(ProviderStatus.CONTENT_URI,
                false, mProviderStatusObserver);
    }

    /**
     * Register an observer for provider status changes - we will need to
     * reflect them in the UI.
     */
    private void unregisterProviderStatusObserver() {
        getContentResolver().unregisterContentObserver(mProviderStatusObserver);
    }

    private void setupListView() {
        final ListView list = getListView();
        final LayoutInflater inflater = getLayoutInflater();

        mHighlightingAnimation =
                new NameHighlightingAnimation(list, TEXT_HIGHLIGHTING_ANIMATION_DURATION);

        // Tell list view to not show dividers. We'll do it ourself so that we can *not* show
        // them when an A-Z headers is visible.
        list.setDividerHeight(0);
        list.setFocusable(true);
        list.setOnCreateContextMenuListener(this);

        mAdapter = new ContactItemListAdapter(this);
        setListAdapter(mAdapter);

        if (list instanceof PinnedHeaderListView && mAdapter.getDisplaySectionHeadersEnabled()) {
            mPinnedHeaderBackgroundColor =
                    getResources().getColor(R.color.pinned_header_background);
            PinnedHeaderListView pinnedHeaderList = (PinnedHeaderListView)list;
            View pinnedHeader = inflater.inflate(R.layout.list_section, list, false);
            pinnedHeaderList.setPinnedHeaderView(pinnedHeader);
            mBladeView = (BladeView)this.findViewById(R.id.category);
			if (mBladeView != null) {
				mBladeView.setEnableSectionColor(Color.BLACK);
				mBladeView.setDisableSectionColor(Color.argb(0xFF, 0x6C, 0x6C, 0x6C));
			}
			
        } 
        
        // If the entry point of ContactsListActivity isn't the default one, 
        // we don't use the BladeView and instead use the old approach of fast scrolling.
        // If the screen width is larger than the height, 
        // we treat it as a landscape situation and do not use the BladeView.
        // -- mtk80909
        if (mMode != MODE_DEFAULT || isWidthLargerThanHeight()) {
        	mBladeView = null;
        	list.setFastScrollEnabled(true);
        }

        list.setOnScrollListener(mAdapter);
        list.setOnKeyListener(this);
        list.setOnFocusChangeListener(this);
        list.setOnTouchListener(this);

        // We manually save/restore the listview state
        list.setSaveEnabled(false);
    }

    /**
     * Configures search UI.
     */
    private void setupSearchView() {
        mSearchEditText = (SearchEditText)findViewById(R.id.search_src_text);
        mSearchEditText.addTextChangedListener(this);
        mSearchEditText.setOnEditorActionListener(this);
        mSearchEditText.setText(mInitialFilter);
    }

    private String getContactDisplayName(long contactId) {
        String contactName = null;
        Cursor c = getContentResolver().query(
                ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId),
                new String[] {Contacts.DISPLAY_NAME}, null, null, null);
        try {
            if (c != null && c.moveToFirst()) {
                contactName = c.getString(0);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        if (contactName == null) {
            contactName = "";
        }

        return contactName;
    }

    private int getSummaryDisplayNameColumnIndex() {
        if (mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
            return SUMMARY_DISPLAY_NAME_PRIMARY_COLUMN_INDEX;
        } else {
            return SUMMARY_DISPLAY_NAME_ALTERNATIVE_COLUMN_INDEX;
        }
    }

    /** {@inheritDoc} */
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            // TODO a better way of identifying the button
            case android.R.id.button1: {
                final int position = (Integer)v.getTag();
                Cursor c = mAdapter.getCursor();
                if (c != null) {
                    c.moveToPosition(position);
                    callContact(c);
                }
                break;
            }
            case R.id.group_button: {
                showGroupsDialog();
                break;
            }
            case R.id.search_contact: {
                onSearchRequested();
                break;
            }
		case R.id.contact_list_gallery_but: {
            	Intent intentGallery=new Intent			 			(this,GalleryContactPhoneActivity.class);
            	startActivity(intentGallery);
            	break;
            }
            case R.id.add_new_contact: 
//            case R.id.btn_new_contact:
            	{
            	Log.i(TAG,"%%%%%%%%% add_new_contact: mShouldClickPlusButton is " + mShouldClickPlusButton);
                final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                if (mShouldClickPlusButton) {
                	mShouldClickPlusButton = false;
                startActivity(intent);
                }
                break;
            }

//            case R.id.btn_copyfromsimcard:
//            {
//    			List<SIMInfo> simInfoList = SIMInfo.getInsertedSIMList(this);
//    			if (null != simInfoList && simInfoList.isEmpty()) {
//    				new AlertDialog.Builder(this).setMessage(
//    						R.string.no_simcard_message).setTitle(
//    						R.string.no_simcard_title).setIcon(
//    						android.R.drawable.ic_dialog_alert).setPositiveButton(
//    						android.R.string.ok,
//    						new DialogInterface.OnClickListener() {
//
//    							public void onClick(DialogInterface dialog,
//    									int which) {
//    								// TODO Auto-generated method stub
//    								return;
//    							}
//    						}).show();
//    				break;
//    			}
//    			
//            	final Intent intent = new Intent(this, ImportExportBridgeActivity.class);
//            	intent.putExtra("bridge_type", 1);
//            	startActivity(intent);
//            	break;
//            }
        }

		// mtk80909, 2010-9-17
		if ((mSelectAllButton != null && v.getId() == mSelectAllButton.getId())
				|| (mSelectAllBox != null && v.getId() == mSelectAllBox.getId())) {
			if (mMarkAllProgDialog != null) {
				return;
			}
			// dealing with NullPointerException
			if (mDeleteButton == null || mSelectAllBox == null || mSelectAllButton == null
					|| getListView() == null /*|| mToBeDeleted == null || mToBeDeletedSet == null*/) {
				Log.i(TAG, "onClick() null");
//				Log.i(TAG, "mToBeDeleted == " + mToBeDeleted);
				return;
			}

			mDeleteButton.setEnabled(false);
			mSelectAllBox.setEnabled(false);
			mSelectAllButton.setEnabled(false);
			getListView().setEnabled(false);

			mMarkAllProgDialog = new ProgressDialog(this);
			if(null != mMarkAllProgDialog) {
				mMarkAllProgDialog.setCancelable(false);
				mMarkAllProgDialog.setMessage(getResources().getString(R.string.please_wait));
				mMarkAllProgDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mMarkAllProgDialog.show();
			}
			
			//Update buttons status and toggle startQuery to update check box state 
			if (mSelectAllToDel) {
				mSelectAllBox.setChecked(false);
				mSelected = 0;
				mDeleteButton.setText(mDeleteText + "(" + mSelected + ")");
				mSelectAllButton.setText(mSelectAllText);
				if (!mToBeDeletedContacts.isEmpty())
					mToBeDeletedContacts.clear();
				mSelectAllToDel = false;
			} else {
				mSelectAllBox.setChecked(true);
				mSelected = mAllContacts.size();
				mDeleteButton.setText(mDeleteText + "(" + mSelected + ")");
				mSelectAllButton.setText(mUnSelectText);
				mSelectAllToDel = true;
			}
				
//			boolean bSnsOwner = (FeatureOption.MTK_SNS_SUPPORT && !mSearchMode && mMode == MODE_DEFAULT);
//			int startIndex = bSnsOwner ? 1 : 0;
//			int length = bSnsOwner ? mToBeDeleted.length - 1 : mToBeDeleted.length;
//			if (mSelected == length) {
//				mAllOrNone = true;
//				mSelectAllBox.setChecked(false);
//				for (int k = startIndex; k < mToBeDeleted.length; ++k) {
//					mToBeDeleted[k] = false;
//					mToBeDeletedSet.remove(k);
//				}
//				
//				mSelected = 0;
//				mDeleteButton.setText(getResources().getString(
//						R.string.delete_contacts_basic)
//						+ "(" + mSelected + ")");
//				mSelectAllButton.setText(getResources().getString(
//						R.string.select_all));
//			} else {
//				mAllOrNone = true;
//				mSelected = length;
//				mSelectAllBox.setChecked(true);
//				for (int k = startIndex; k < mToBeDeleted.length; ++k) {
//					mToBeDeleted[k] = true;
//					mToBeDeletedSet.add(k);
//				}
//				
//				mDeleteButton.setText(getResources().getString(
//						R.string.delete_contacts_basic)
//						+ "(" + mSelected + ")");
//				mSelectAllButton.setText(getResources().getString(
//						R.string.unselect_all));
//			}
			Log.i(TAG, "##################calls startQuery() in onClick() selectAll");
			this.startQuery();
		}
		if (mDeleteButton != null && v.getId() == mDeleteButton.getId()) {
			//Do no use mSelect count to do judgment
			if (mToBeDeletedContacts.isEmpty() && !mSelectAllToDel) {
				quitDeleteState();
				return;
			} else {
				showDialog(R.id.dialog_batch_contact_delete_confirmation);
			}
		}
    }

    private void showGroupsDialog() {
        final Cursor cursor = getContentResolver().query(Groups.CONTENT_URI, 
                new String[]{Groups._ID, Groups.TITLE}, Groups.DELETED + "=0 AND " + Groups.ACCOUNT_TYPE + "='DeviceOnly'", null, Groups.SYSTEM_ID + " DESC, " + Groups.TITLE);
        if (cursor != null) {
        Log.i(TAG, "Group count " + cursor.getCount());
        }
        final GroupAdapter adapter = new GroupAdapter(this, cursor);
        final DialogInterface.OnClickListener clickListener =
            new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            	ContactsUtils.mSimList = Constants.getInsertedSimList(ContactsListActivity.this);
                String title = (String) adapter.getItem(which);
                if (!SIM_CONTACTS_TITLE.equals(title)) {
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(ContactsListActivity.this).edit();
                    editor.putString("group", title);
                    editor.putString("simIds", "");
                    editor.apply();
                    Log.i(TAG, "select title " + title);
                    startQuery();
                    mGroupBtn.setText(ContactsUtils.getGroupsName(mContaxt, title));
                } else if(ContactsUtils.mSimList.size()<=1){
                	ContactsUtils.mSelectedSimList = Constants.getInsertedSimList(ContactsListActivity.this);
                	String simIds = "";
                    if(ContactsUtils.mSimList!=null){
                    	for(SimInfo i:ContactsUtils.mSimList){
                    		simIds =simIds + i.simId + ",";
                    	}
                    }
                    
    				SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(ContactsListActivity.this).edit();
                    editor.putString("group", SIM_CONTACTS_TITLE);
                    editor.putString("simIds", simIds);
                    editor.apply();
                    
    				startQuery();
    				ContactsUtils.mSelectedSimList.clear();
    				mGroupBtn.setText(ContactsUtils.getGroupsName(mContaxt, SIM_CONTACTS_TITLE));
                    
                } else{
                	showSimDialog();
                }
//                
                dialog.dismiss();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(adapter, -1, clickListener);
        builder.setTitle(R.string.select_group_or_sim);
        if(mShowGroupsDialog == null){
        	mShowGroupsDialog = builder.create();
        	mShowGroupsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                
                public void onDismiss(DialogInterface dialog) {
                    if (cursor != null) {
                        cursor.close();
        				mShowGroupsDialog = null;
        				if(queryAfterGroupDialogDissmiss == true){
        					queryAfterGroupDialogDissmiss = false;
        					startQuery();
        				}
                    }
                    
                }
            });
            mShowGroupsDialog.show();
        }
//        AlertDialog dialog = builder.create();
//        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            
//            public void onDismiss(DialogInterface dialog) {
//                if (cursor != null) {
//                    cursor.close();
//                }
//                
//            }
//        });
//        dialog.show();
    }
    
    private void showSimDialog(){
    	ContactsUtils.mSimList = Constants.getInsertedSimList(this);
		AlertDialog.Builder builder = ContactsUtils.buildSimListDialogMultChoice(
				this, null);
		android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener(){

			public void onClick(DialogInterface dialog, int which) {
				String simIds = "";
                if(ContactsUtils.mSelectedSimList!=null){
                	for(SimInfo i:ContactsUtils.mSelectedSimList){
                		simIds =simIds + i.simId + ",";
                	}
                }
                
				SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(ContactsListActivity.this).edit();
                editor.putString("group", SIM_CONTACTS_TITLE);
                editor.putString("simIds", simIds);
                editor.apply();
                
				startQuery();
				ContactsUtils.mSelectedSimList.clear();
				mGroupBtn.setText(ContactsUtils.getGroupsName(mContaxt, SIM_CONTACTS_TITLE));
			}
		};
		builder.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok, listener);
		mSelectSIMDialog = builder.create();
		mSelectSIMDialog.getListView().setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.checked);
				mCheckBox.toggle();
				SimInfo info = ContactsUtils.mSimList.get(position);
				if(mCheckBox.isChecked()){
					if(!ContactsUtils.mSelectedSimList.contains(info))ContactsUtils.mSelectedSimList.add(info);
				}else{
					if(ContactsUtils.mSelectedSimList.contains(info))ContactsUtils.mSelectedSimList.remove(info);
				}
				Button button = mSelectSIMDialog.getButton(Dialog.BUTTON_POSITIVE);
				if(ContactsUtils.mSelectedSimList.size() > 0){
					button.setEnabled(true);
				}else {
					button.setEnabled(false);
				}
			}
		});
		
		mSelectSIMDialog.show();
		Button button = mSelectSIMDialog.getButton(Dialog.BUTTON_POSITIVE);
		if(ContactsUtils.mSelectedSimList.size() > 0){
			button.setEnabled(true);
		}else {
			button.setEnabled(false);
		}
    }
    
    private class GroupAdapter extends CursorAdapter {
        
        private LayoutInflater gInflater;
        public GroupAdapter (Context context, Cursor cursor) {
            super(context, cursor);
            final Context dialogContext = new ContextThemeWrapper(context, android.R.style.Theme_Light);
            gInflater = (LayoutInflater)dialogContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int count = getCount();
            Log.i(TAG, "count " + count);
            if (position == 0 || position == count - 1) {
                View view = gInflater.inflate(R.layout.simple_list_item_1, parent, false);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                if (position == 0) {
                    text.setText(R.string.showAllGroups);
                } else if (position == count - 1) {
                    text.setText(R.string.sim);
                }
                return view;
            }
            return super.getView(position - 1, convertView, parent);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView text = (TextView) view.findViewById(android.R.id.text1);
            String title = cursor.getString(cursor.getColumnIndexOrThrow(Groups.TITLE));
            String groupName = ContactsUtils.getGroupsName(mContext, title);
            //TODO Need change the layout
            text.setText(groupName);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return gInflater.inflate(R.layout.simple_list_item_1, parent, false);
        }

        @Override
        public int getCount() {
            //will show the SIM in last one
            return super.getCount() + 2;
        }

        @Override
        public Object getItem(int position) {
            int count = getCount();
            if (position == 0) {
                return ALL_CONTACTS_TITLE;
            }
            if (position == count - 1) {
                return SIM_CONTACTS_TITLE;
            }
            this.mCursor.moveToPosition(position - 1);
            return this.mCursor.getString(this.mCursor.getColumnIndexOrThrow(Groups.TITLE));
        }
        
    }

    //Add by mtk80908.
    //USED for first launching Optimization.
    //Do post-processing about setEmptyText after startQuery, since startQuery would
    //delay launching time.
    private void postSetEmptyText() {
//    	TextView empty = (TextView) findViewById(R.id.emptyText);
//    	CharSequence emptyStr = empty.getText();
//    	if (TextUtils.isEmpty(emptyStr)) {
    		setEmptyText();
//    	}
    }
    private void setEmptyText() {
        if (mMode == MODE_JOIN_CONTACT || mSearchMode) {
            return;
        }

        TextView empty = (TextView) findViewById(R.id.emptyText);
		if(null != empty) {
        if (mDisplayOnlyPhones) {
            empty.setText(getText(R.string.noContactsWithPhoneNumbers));
        } else if (mMode == MODE_STREQUENT || mMode == MODE_STARRED) {
            empty.setText(getText(R.string.noFavoritesHelpText));
        } else if (mMode == MODE_QUERY || mMode == MODE_QUERY_PICK
                || mMode == MODE_QUERY_PICK_PHONE || mMode == MODE_QUERY_PICK_TO_VIEW
                || mMode == MODE_QUERY_PICK_TO_EDIT) {
            empty.setText(getText(R.string.noMatchingContacts));
			} else if (mMode == MODE_PICK_PHONE || mMode == MODE_PICK_PHONE_EMAIL) {
			    empty.setText(getText(R.string.noContactsHelpTextForCreateShortcut));
        } else {
            boolean hasSim = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
                    .hasIccCard();
            boolean createShortcut = Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction());
            if (isSyncActive()) {
                if (createShortcut) {
                    // Help text is the same no matter whether there is SIM or not.
                    empty.setText(getText(R.string.noContactsHelpTextWithSyncForCreateShortcut));
                } else if (hasSim) {
                    empty.setText(getText(R.string.noContactsHelpTextWithSync));
                } else {
                    empty.setText(getText(R.string.noContactsNoSimHelpTextWithSync));
                }
            } else {
                if (createShortcut) {
                    // Help text is the same no matter whether there is SIM or not.
                    empty.setText(getText(R.string.noContactsHelpTextForCreateShortcut));
                } else if (hasSim) {
                    empty.setText(getText(R.string.noContactsHelpText));
                } else {
                    empty.setText(getText(R.string.noContactsNoSimHelpText));
                }
            }
        }
    }
    }

    private boolean isSyncActive() {
        Account[] accounts = AccountManager.get(this).getAccounts();
        if (accounts != null && accounts.length > 0) {
            IContentService contentService = ContentResolver.getContentService();
            for (Account account : accounts) {
                try {
                    if (contentService.isSyncActive(account, ContactsContract.AUTHORITY)) {
                        return true;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Could not get the sync status");
                }
            }
        }
        return false;
    }

    private void buildUserGroupUri(String group) {
        mGroupUri = Uri.withAppendedPath(Contacts.CONTENT_GROUP_URI, group);
    }

    /**
     * Sets the mode when the request is for "default"
     */
    private void setDefaultMode() {
        // Load the preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mDisplayOnlyPhones = prefs.getBoolean(Prefs.DISPLAY_ONLY_PHONES,
                Prefs.DISPLAY_ONLY_PHONES_DEFAULT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy() ----------------------------------------------------------------- begin");
        mPhotoLoader.stop();
        Log.i(TAG,"mCellConnMgr IS " + mCellConnMgr);
        if (mCellConnMgr != null) mCellConnMgr.unregister();
        if (mBatchDeleteClickListener != null) {
        	mBatchDeleteClickListener.cancelFromExternal();
        }
        
        // for modem switch
		if (FeatureOption.MTK_GEMINI_3G_SWITCH) {
			Log.i(TAG,"onDestroy() mModemSwitchReceiver is " + mModemSwitchReceiver);
        	unregisterReceiver(mModemSwitchReceiver);
        }
        
        //add by huibin 2010-08-31
        if(true == FeatureOption.MTK_SNS_SUPPORT)
	{
        	if (mSnsLoader != null)
        		mSnsLoader.stop();
        	if (mOwnerLoader != null)
        		mOwnerLoader.stop();
	}   
        /*if (FeatureOption.MTK_GEMINI_SUPPORT) {
          unregisterReceiver(mReceiver);
        }*/
        
        if (mSimInfoWrapper != null)
        	mSimInfoWrapper.release();

        Log.i(TAG, "onDestroy() ----------------------------------------------------------------- end");
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart() ----------------------------------------------------------------- begin");
        super.onStart();

        mContactsPrefs.registerChangeListener(mPreferencesChangeListener);
        Log.i(TAG, "onStart() ----------------------------------------------------------------- end");
    }


/*    private class SimIndicatorBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                Log.d(TAG, "SimIndicatorBroadcastReceiver, onReceive() ");
                if (action.equals(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED)) {
                    Log.d(TAG, "SimIndicatorBroadcastReceiver, onReceive(), ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED received ");
                    Log.d(TAG, "SimIndicatorBroadcastReceiver, onReceive(), mShowSimIndicator= " + mShowSimIndicator);
                    if (true == mShowSimIndicator) {
                        setSimIndicatorVisibility(true);
                    }
                }
            }
        }
    }*/

    private void dismissAlertDialog(AlertDialog alertDialog){
		// mtk80909 for ALPS00033230
		try {
			if (alertDialog != null && alertDialog.isShowing()) {
				alertDialog.dismiss();
			}
		} catch (IllegalArgumentException e) {
			Log.w(TAG, "Trying to dismiss a dialog not connected to the current UI");
		}

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause() ----------------------------------------------------------------- begin");
        closeContextMenu();

        ContactsUtils.dispatchActivityOnPause();
        dismissAlertDialog(mImportAlertDialog);
        dismissAlertDialog(mSdCardNtFdAlertDialog);
        dismissAlertDialog(mHideConfmAlertDialog);
        dismissAlertDialog(mDelConfm1AlertDialog);
        dismissAlertDialog(mDelConfm2AlertDialog);
        dismissAlertDialog(mDelConfm3AlertDialog);
        dismissAlertDialog(mDelConfm4AlertDialog);
        dismissAlertDialog(mDelConfm5AlertDialog);
        dismissAlertDialog(mChangeLocaleDialog);
        dismissAlertDialog(mSelectSIMDialog);
        
        mForeground = false;

	    if (mQueryDialog != null && mQueryDialog.isShowing()) {
			mQueryDialog.dismiss();
			mQueryDialog = null;
		}
    
        if (mPhoneDisambigDialog != null && mPhoneDisambigDialog.isShowing()) {
        	Log.i(TAG, "here? ");
        	mPhoneDisambigDialog.dismiss();
        }
		AlertDialog callDialog = ContactsUtils.getCallDialog();
		if (callDialog != null && callDialog.isShowing()) {
            try{
	              callDialog.dismiss();  
                }catch(IllegalArgumentException e){
		       Log.w(TAG, "Ignoring exception while dismissing callDialog");
                }		
        }     
	   	    
       	if (mDeleteState && !mBeingDeleted) {
        	quitDeleteState();
        	Log.i(TAG, "quitDeleteState on Pause()");
       	}
        if (mMode != MODE_INSERT_OR_EDIT_CONTACT && mAddContactState) {
            mAddContactState = false;
            Log.i(TAG, "exit mAddContactState on Pause()");
        }
        unregisterProviderStatusObserver();
        if (mMode != MODE_JOIN_CONTACT) {       	
            mHandler.sendEmptyMessage(LISTEN_PHONE_NONE_STATES);//80794
        }    
        if(!ContactsListActivity.this.isFinishing() && mShowGroupsDialog != null && mShowGroupsDialog.isShowing()){
        	mShowGroupsDialog.dismiss();        	
        }
        /*if (FeatureOption.MTK_GEMINI_SUPPORT) {
            Log.i(TAG, "onPause(), mShowSimIndicator = false ");
            
            setSimIndicatorVisibility(false);
            mShowSimIndicator = false;
        }*/
        Log.i(TAG, "onPause() ----------------------------------------------------------------- end");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume() ----------------------------------------------------------------- begin");
		
		/*Log.i(TAG,"In onResume ContactsMarkListActivity.mShouldFinish is " + ContactsMarkListActivity.mShouldFinish);		
		if (ContactsMarkListActivity.mShouldFinish) {
			ContactsMarkListActivity.mShouldFinish = false;
		}*/
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            if ((mMode == MODE_DEFAULT && !mSearchMode && !mSearchResultsMode) || mMode == MODE_STREQUENT) {
              Log.i(TAG, "onResume(), mShowSimIndicator = true ");
              setSimIndicatorVisibility(true);
              mShowSimIndicator = true;
            }
        }

        registerProviderStatusObserver();
//		mPhotoLoader.clear();
	if(true == FeatureOption.MTK_SNS_SUPPORT)
	{
		if(null != mSnsLoader)
			mSnsLoader.clear();
	}
//        mPhotoLoader.resume();
	
	//add by huibin 2010-08-31
        if(true == FeatureOption.MTK_SNS_SUPPORT)
	{
		if(null != mSnsLoader) mSnsLoader.resume();
		if(null != mOwnerLoader) mOwnerLoader.resume();
		if(bIsOwnerShow && null != ownerView && null != mOwnerLoader){	
			mOwnerLoader.loadOwner(ownerView, PhoneOwner.getInstance());
		}
	}

//        Activity parent = getParent();

        // Do this before setting the filter. The filter thread relies
        // on some state that is initialized in setDefaultMode
        if (mMode == MODE_DEFAULT) {
            // If we're in default mode we need to possibly reset the mode due to a change
            // in the preferences activity while we weren't running
            setDefaultMode();
        }

        // See if we were invoked with a filter
        if (mSearchMode) {
            mSearchEditText.requestFocus();
        }

        if (!mSearchMode && !checkProviderState(mJustCreated)) {
            return;
        }
        Log.i(TAG, "onResume() ##################before startQuery()");
        Log.d(TAG, "onResume()  mJustCreated="+ mJustCreated+";mForceQuery=" +mForceQuery+ "; mBeingDeleted="+mBeingDeleted);
        if ((mJustCreated || mForceQuery) && !mBeingDeleted && !mAddContactState) {// mtk80909
            // We need to start a query here the first time the activity is launched, as long
            // as we aren't doing a filter.
            //not startQuery any more when mAddContactState is true, other will white screen when scroll, ALPS00054409

            Log.i(TAG, "onResume() ##################will startQuery()");
            if (mMode == MODE_DEFAULT && !mSearchMode) {
            mQueryDialog = new ProgressDialog(this);
			mQueryDialog.setCancelable(false);
					mQueryDialog.setMessage(getString(
					R.string.please_wait));
			mQueryDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mQueryDialog.show();
            }
            startQuery();
        }
        mForeground = true;
        mForceQuery = false;
        mJustCreated = false;
        mSearchInitiated = false;
        mShouldClickPlusButton = true;
        if (mMode != MODE_JOIN_CONTACT) {
        mHandler.sendEmptyMessage(LISTEN_PHONE_STATES);//80794
        }
        
        //Do resume photo loader at the end of OnResume.
        mPhotoLoader.clear();
        mPhotoLoader.resume();
        Log.i(TAG, "onResume() ----------------------------------------------------------------- end");
    }

 // create by Ivan 2010-08-08
    /**
     * this method do update job.
     * read sns infomation from static object
     * this static object will be modify by other activity
     */
    private void doUpdateSNSInfo(){
    	if(false == FeatureOption.MTK_SNS_SUPPORT)
    	{
    		return;
    	}
    	
	if(ownerView == null)
	{
		return;
	}
    	
        PhoneOwner owner = PhoneOwner.getInstance();
        QuickContactBadge quickContact = ((ContactListItemView)ownerView).getQuickContact();
		if(null != quickContact) {
	        quickContact.assignContactUri(Contacts.getLookupUri(owner == null ? (long)-1 : owner.getOwnerID(), 
		        owner == null ? null : owner.getOwnerLookupKey()));
		    if(null == owner)
		        quickContact.setClickable(false);
		    else
                quickContact.setClickable(true);
		}
	ImageView ownerPhoto = quickContact;
	
	if(null != ownerPhoto)
        	ownerPhoto.setImageResource(R.drawable.ic_contact_list_picture);
        TextView info = (TextView) ((ContactListItemView)ownerView).getNameTextView();
	if(null != info){
        	info.setText(this.getResources().getString(R.string.owner_none));
        	info.setVisibility(View.VISIBLE);
	}
        
        ImageView snsLogo = (ImageView) ((ContactListItemView)ownerView).getSnsLogo();
        if(null != snsLogo) snsLogo.setVisibility(View.GONE);
        
        TextView snsStatu = (TextView) ((ContactListItemView)ownerView).getSnsStatus();
        if(null != snsStatu) snsStatu.setVisibility(View.GONE);
	ContentResolver cr = getContentResolver();
	if(null == cr || null == owner)
		return;
	Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,  
        		Contacts._ID + " = '" + owner.getOwnerID() + "'", 
        		null, null);

        if(null != cursor){
        	Bitmap ownerIcon = null;
        	if (cursor.moveToFirst()) {
        		String contactId = cursor.getString(
        				cursor.getColumnIndex(ContactsContract.Contacts._ID));
        		Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,  
        			Long.parseLong(contactId));  
        		InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);  
        		ownerIcon = BitmapFactory.decodeStream(input);
        	}
        	if(cursor != null) cursor.close();
        	SnsUser curContact = ContactsManager.readStatusFromWSP(owner.getOwnerID(), 
        			mContaxt, RAW_CONTACTS_PROJECTION2);
        	
        	if(curContact != null){
			owner.setStatus(curContact.status);
        		owner.setSnsLogo(curContact.snsUrl);
        	}
        	
        	if(ownerIcon == null){
        		if(null != ownerPhoto) ownerPhoto.setImageResource(R.drawable.ic_contact_list_picture);
        	}
        	else{
        		if(null != ownerPhoto) ownerPhoto.setImageBitmap(ownerIcon);
        	}
		if(null != info){
			info.setText(owner.getName());
			info.setVisibility(View.VISIBLE);
		}

        	if(owner.getStatus() == null || owner.getSnsLogo() == null){
        		if(null != snsLogo) snsLogo.setVisibility(View.GONE);
        		if(null != snsStatu) snsStatu.setVisibility(View.GONE);
        	}
        	else{
			if(null != curContact){
				if(KAIXIN_TYPE == curContact.sns_id){
					if(null != snsLogo) snsLogo.setImageResource(R.drawable.logo_kaixin);
				} else if (RENREN_TYPE == curContact.sns_id) {
					if(null != snsLogo) snsLogo.setImageResource(R.drawable.logo_renren);
				} else if (TWITTER_TYPE == curContact.sns_id) {
					if(null != snsLogo) snsLogo.setImageResource(R.drawable.logo_twitter);
				} else if (FLICKR_TYPE == curContact.sns_id) {
					if(null != snsLogo) snsLogo.setImageResource(R.drawable.logo_flickr);
				} else if (FACEBOOK_TYPE == curContact.sns_id) {
					if(null != snsLogo) snsLogo.setImageResource(R.drawable.logo_facebook);
				} else {
					// for other sns!
				}
			}
			if(null != snsLogo) snsLogo.setVisibility(View.VISIBLE);

        		if(null != snsStatu && null != curContact) {
				snsStatu.setText(ContactsManager.parserEmotion(owner.getStatus(),curContact.sns_id));
        			snsStatu.setVisibility(View.VISIBLE);
			}
        	}
        }
	if(cursor != null) cursor.close();
    }
    
    /**
     * Obtains the contacts provider status and configures the UI accordingly.
     *
     * @param loadData true if the method needs to start a query when the
     *            provider is in the normal state
     * @return true if the provider status is normal
     */
    private boolean checkProviderState(boolean loadData) {
        View importFailureView = findViewById(R.id.import_failure);
        if (importFailureView == null) {
            return true;
        }

        TextView messageView = (TextView) findViewById(R.id.emptyText);

        // This query can be performed on the UI thread because
        // the API explicitly allows such use.
        Cursor cursor = getContentResolver().query(ProviderStatus.CONTENT_URI, new String[] {
                ProviderStatus.STATUS, ProviderStatus.DATA1
        }, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(0);
                if (status != mProviderStatus) {
                    mProviderStatus = status;
                    switch (status) {
                        case ProviderStatus.STATUS_NORMAL:
                            mAdapter.notifyDataSetInvalidated();
                            if (loadData) {
                                Log.d(TAG, "checkProviderState(), ##################before startQuery() ");
                                startQuery();
                            }
                            break;

                        case ProviderStatus.STATUS_CHANGING_LOCALE:
                        	mProviderStatusChange = true;
                            messageView.setText(R.string.locale_change_in_progress);
                            if (mChangeLocaleDialog == null) {
                                mChangeLocaleDialog = new ProgressDialog(ContactsListActivity.this);
                                mChangeLocaleDialog.setMessage(ContactsListActivity.this.getString(R.string.load_wait));
                            }
                            Log.d(TAG, "%%%%%%show progress dialog for change language");
                            mChangeLocaleDialog.show();
                            mAdapter.changeCursor(null);
                            mAdapter.notifyDataSetInvalidated();
                            break;

                        case ProviderStatus.STATUS_UPGRADING:
                            messageView.setText(R.string.upgrade_in_progress);
                            mAdapter.changeCursor(null);
                            mAdapter.notifyDataSetInvalidated();
                            break;

                        case ProviderStatus.STATUS_UPGRADE_OUT_OF_MEMORY:
                            long size = cursor.getLong(1);
                            String message = getResources().getString(
                                    R.string.upgrade_out_of_memory, new Object[] {size});
                            messageView.setText(message);
                            configureImportFailureView(importFailureView);
                            mAdapter.changeCursor(null);
                            mAdapter.notifyDataSetInvalidated();
                            break;
                    }
                    if (mProviderStatus != ProviderStatus.STATUS_CHANGING_LOCALE) {
                        dismissAlertDialog(mChangeLocaleDialog);
                        mProviderStatusChange = false;
                    }
                }
            }
        } finally {
            cursor.close();
        }

        importFailureView.setVisibility(
                mProviderStatus == ProviderStatus.STATUS_UPGRADE_OUT_OF_MEMORY
                        ? View.VISIBLE
                        : View.GONE);
        return mProviderStatus == ProviderStatus.STATUS_NORMAL;
    }

    private void configureImportFailureView(View importFailureView) {

        OnClickListener listener = new OnClickListener(){

            public void onClick(View v) {
                switch(v.getId()) {
                    case R.id.import_failure_uninstall_apps: {
                        startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
                        break;
                    }
                    case R.id.import_failure_retry_upgrade: {
                        // Send a provider status update, which will trigger a retry
                        ContentValues values = new ContentValues();
                        values.put(ProviderStatus.STATUS, ProviderStatus.STATUS_UPGRADING);
                        getContentResolver().update(ProviderStatus.CONTENT_URI, values, null, null);
                        break;
                    }
                }
            }};

        Button uninstallApps = (Button) findViewById(R.id.import_failure_uninstall_apps);
        uninstallApps.setOnClickListener(listener);

        Button retryUpgrade = (Button) findViewById(R.id.import_failure_retry_upgrade);
        retryUpgrade.setOnClickListener(listener);
    }

    private String getTextFilter() {
        if (mSearchEditText != null) {
            if(TextUtils.isEmpty(mSearchEditText.getText().toString())&&mMode == MODE_INSERT_OR_EDIT_CONTACT)
                {
                    return "*";
                } else {            
                    return mSearchEditText.getText().toString();
                }
        }
        return null;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (!checkProviderState(false)) {
            return;
        }

        // The cursor was killed off in onStop(), so we need to get a new one here
        // We do not perform the query if a filter is set on the list because the
        // filter will cause the query to happen anyway
 		if (!mBeingDeleted) {
        if (TextUtils.isEmpty(getTextFilter())) {
				if (true == FeatureOption.MTK_SNS_SUPPORT) {
		    			doUpdateSNSInfo();
		    		}
				//MTK to avoid the requery in normal mode when back to this view
				//mForcequery will handle the case when any data changed
				//startQuery();
        } else {
            // Run the filtered query on the adapter
            mAdapter.onContentChanged();
    	    }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        // Save list state in the bundle so we can restore it after the QueryHandler has run
        if (mList != null) {
            icicle.putParcelable(LIST_STATE_KEY, mList.onSaveInstanceState());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle icicle) {
        super.onRestoreInstanceState(icicle);
        // Retrieve list state. This will be applied after the QueryHandler has run
        mListState = icicle.getParcelable(LIST_STATE_KEY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop() ----------------------------------------------------------------- begin");
        mContactsPrefs.unregisterChangeListener();

        //MTK to avoid the requery in normal mode when back to this view
        //mForcequery will handle the case when any data changed
	if (!mDeleteState && !TextUtils.isEmpty(getTextFilter())) {
            mAdapter.setSuggestionsCursor(null);
            mAdapter.changeCursor(null);
	}
        if (mMode == MODE_QUERY) {
            // Make sure the search box is closed
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchManager.stopSearch();
        }
        
        // modified by Ivan 2010-08-16  
        if(true == FeatureOption.MTK_SNS_SUPPORT)
        {
            new Thread(new Runnable() {
                public void run() {
                    SharedPreferences setting = getSharedPreferences(PREFS_NAME, 
				Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
                    SharedPreferences.Editor editor = setting.edit();
                    PhoneOwner owner = PhoneOwner.getInstance();
                    if (owner != null) {
                        editor.putLong(PREFS_OWNER_ID, owner.getOwnerID());
                        editor.putString(PREFS_OWNER_LOOKUPKEY, owner.getOwnerLookupKey());
                        editor.putString(PREFS_OWNER_NAME, owner.getName());
                    } else {
                        editor.clear();
                    }
                    editor.commit();
                }
            }).start();
    		
    		// modified end
        }
        Log.i(TAG, "onStop() ----------------------------------------------------------------- end");
    }

    @Override
    public void onBackPressed() {
    	Log.e(TAG, "------------------------------------begin onBackPressed(): getParent():" + getParent()); 
    	if (null != getParent()) {
            getParent().moveTaskToBack(true);
    	} else {
            super.onBackPressed();
    	}
        Log.e(TAG, "------------------------------------end onBackPressed()"); 
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // If Contacts was invoked by another Activity simply as a way of
        // picking a contact, don't show the options menu
        if (((mMode & MODE_MASK_PICKER) == MODE_MASK_PICKER) && !mAddContactState ) {
            Log.d(TAG, " onCreateOptionsMenu(), MODE_MASK_PICKER, and not mAddContactState return");
            return false;
        }
		// If Contacts is now under delete state, no menu will be displayed.
		if (mDeleteState || mProviderStatusChange) {
			return false;
		}
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list, menu);
            menu.add(0, MENU_ITEM_SHARE_CONTACTS, 0, R.string.share_visible_contacts).setIcon(R.drawable.contact_group_sharecontacts_menu);
        //menu.add(0, MENU_ITEM_GROUPS, 0, R.string.groups).setIcon(R.drawable.contact_groupmanage_menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		if (mDeleteState)
			return false;
        final boolean defaultMode = (mMode == MODE_DEFAULT);

		// mtk80909 for Speed Dial
		final boolean strequentMode = (mMode == MODE_STREQUENT);
		menu.findItem(R.id.groups).setVisible(!mAddContactState);
				if(mAdapter.getCount()==0)
			{
                menu.findItem(R.id.menu_search).setVisible(false);
				menu.findItem(R.id.menu_delete).setVisible(false);
//				menu.findItem(R.id.groups).setVisible(false);
				menu.findItem(R.id.menu_speed_dial).setVisible(false);
			}
		else
			{
			//menu.findItem(R.id.menu_search).setVisible(true);
            menu.findItem(R.id.menu_search).setVisible(!mSearchMode && !mSearchResultsMode && !mAddContactState);
//			menu.findItem(R.id.groups).setVisible(true);
			menu.findItem(R.id.menu_speed_dial).setVisible(!mAddContactState);
        menu.findItem(R.id.menu_display_groups).setVisible(defaultMode && !mAddContactState);
			menu.findItem(R.id.menu_delete).setVisible(
				defaultMode && mAdapter.getRealCount() > 0 && !mSearchMode && !mAddContactState);
			 if (!mAdapterReady && defaultMode && !mSearchMode){
				menu.findItem(R.id.menu_import_export).setVisible(false);
			}else{
            menu.findItem(R.id.menu_import_export).setVisible(!mAddContactState);
			 }
			}
        if(true == FeatureOption.MTK_SNS_SUPPORT) {
            SNSAccount accounts[] = DataManager.getAccounts();
        	boolean hasAccountsExceptTwitter = false;
        	for(int i = 0; null != accounts && !hasAccountsExceptTwitter && i < accounts.length; i++){
        		if(!accounts[i].account_name.toLowerCase().equals("mtk_twitter")){
        			hasAccountsExceptTwitter = true;
        		}
        	}
        	menu.findItem(R.id.menu_import_sns).setVisible(hasAccountsExceptTwitter && !mSearchMode && defaultMode && !strequentMode && !mAddContactState);
        }
        else {
            menu.findItem(R.id.menu_import_sns).setVisible(false);
        }
		// mtk80909 for Speed Dial
		menu.findItem(R.id.menu_speed_dial).setVisible((defaultMode || strequentMode) && ContactsUtils.SPEED_DIAL);
        menu.findItem(MENU_ITEM_SHARE_CONTACTS).setVisible(!mAddContactState);
        menu.findItem(R.id.menu_accounts).setVisible(!mAddContactState);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int tmpIndex = 0;
		int tmpSize = 0;
		SNSAccountInfo[] accounts = null;
		SNSAccountInfo[] accountsWithoutTwitter = null;
		SNSAccountInfo tmpAccount = null;
        switch (item.getItemId()) {
            case R.id.menu_display_groups: {
                final Intent intent = new Intent(this, ContactsPreferencesActivity.class);
                startActivityForResult(intent, SUBACTIVITY_DISPLAY_GROUP);
                return true;
            }
            case R.id.menu_search: {
                onSearchRequested();
                return true;
            }
            case R.id.menu_add: {
                if (mAddContactState) {
                    mAddContactState = false;
                    Intent intent;
                    intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                    intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    Bundle extras = getIntent().getExtras();
                    if (extras != null) {
                        intent.putExtras(extras);
                    }
                    //intent.putExtra(KEY_PICKER_MODE, (mMode & MODE_MASK_PICKER) == MODE_MASK_PICKER);
                    
                    startActivity(intent);
                    finish();
                }else {
                   final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                   startActivity(intent);
                }
                
                return true;
            }
            case R.id.menu_import_export: {
                //displayImportExportDialog();
                Intent intentImport =  new Intent(this, ImportExportBridgeActivity.class);
                intentImport.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intentImport);
                return true;
            }
            case R.id.menu_accounts: {
                final Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                intent.putExtra(AUTHORITIES_FILTER_KEY, new String[] {
                    ContactsContract.AUTHORITY
                });
                startActivity(intent);
                return true;
            }
			// mtk80909 2010-9-17
			case R.id.menu_delete: {
				invokeDeleteState();
				return true;
			}
			
			// mtk80909 for Speed Dial
			case R.id.menu_speed_dial: {
				final Intent intent = new Intent(this, SpeedDialManageActivity.class);
				startActivity(intent);
				return true;
			}
            case R.id.groups: {
                final Intent intent = new Intent(this, ContactsGroupsActivity.class);
				startActivity(intent);
				return true;
			}
			case MENU_ITEM_SHARE_CONTACTS: {
				//doShareVisibleContacts();
				doShareVisibleContacts("Multi_Contact", null);
				return true;		
				}
	    case R.id.menu_import_sns: {
                if(false == FeatureOption.MTK_SNS_SUPPORT){
                    return false;
                }
                tmpIndex = 0;
                tmpSize = 0;
                accounts = DataManager.getAllSnsAccountInfo();
                accountsWithoutTwitter = null;
                tmpAccount = null;
                for(int i = 0; accounts != null && i < accounts.length; i++) {
                    tmpAccount = accounts[i];
                    if(tmpAccount.sns_id == TWITTER_TYPE) continue;
                    tmpSize++;
                }
                accountsWithoutTwitter = new SNSAccountInfo[tmpSize];
                for(int i = 0; accounts != null && i < accounts.length; i++) {
                    tmpAccount = accounts[i];
                    if(tmpAccount.sns_id == TWITTER_TYPE) continue;
                    accountsWithoutTwitter[tmpIndex] = tmpAccount;
                    tmpIndex++;
                }
                switch(accountsWithoutTwitter.length) {
                case 0:
                    Log.i(TAG, "Error: Account error, only have twitter account ...");
                    break;
                case 1:
                    startImportSNSActivity(accountsWithoutTwitter[0].account_id, 12);
                    break;
                default:
                    startSingleAccountSelect();
                    break;
                }
		return true;
	    }
        }
        return false;
    }

	// mtk80909
	private void invokeDeleteState() {
		mDeleteState = true;
		
		//Add by mtk80908. Initialize variables and flags used in delete state.
		mSelectAllToDel = false;
		mAllContacts.clear();
		mToBeDeletedContacts.clear();
		mTopView.setVisibility(View.VISIBLE);
		groupView.setVisibility(View.GONE);
		mBottomView.setVisibility(View.VISIBLE);
		mSelected = 0;
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getListView().getLayoutParams();
		params.height = 0;
		params.addRule(RelativeLayout.ABOVE, mBottomView.getId());
		params.addRule(RelativeLayout.BELOW, mTopView.getId());
		getListView().setLayoutParams(params);
		
		final Resources r = getResources();
	    mSelectAllText = r.getString(R.string.select_all);
	    mUnSelectText = r.getString(R.string.unselect_all);
	    mDeleteText = r.getString(R.string.delete_contacts_basic);
		
		mDeleteButton.setText( mDeleteText + "(" + mSelected + ")");
		// mtk80909 start
		mSelectAllBox.setChecked(false);
		mSelectAllButton.setText(mSelectAllText);
		mSelectAllBox.setEnabled(true);
		mSelectAllButton.setEnabled(true);
		mDeleteButton.setEnabled(true);
		// mtk80909 end
		
		Log.i(TAG, "calls startQuery() ##################in invokeDeleteState()");
		startQuery();
	}

	// mtk80909
	private void quitDeleteState() {
		mDeleteState = false;
		
		//Add by mtk80908. Reset variables and flags used in delete state.
		mAllContacts.clear();
		mToBeDeletedContacts.clear();
		
//		mAllOrNone = false; // mtk80909
		mSelectAllBox.setChecked(false);
//		mToBeDeleted = null;
//		mToBeDeletedSet.clear();
        dismissAlertDialog(mDelConfm5AlertDialog);

		Log.i(TAG, "calls startQuery() ##################in quitDeleteState() selectAll");
		startQuery();
	}


    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData,
            boolean globalSearch) {
		Log.i(TAG, "#######################startSearch() begin");
        if (mProviderStatus != ProviderStatus.STATUS_NORMAL) {
            return;
        }

        if (globalSearch) {
            super.startSearch(initialQuery, selectInitialQuery, appSearchData, globalSearch);
        } else {
            if (!mSearchMode && (mMode & MODE_MASK_NO_FILTER) == 0) {
                if ((mMode & MODE_MASK_PICKER) != 0) {
                    ContactsSearchManager.startSearchForResult(this, initialQuery,
                            SUBACTIVITY_FILTER);
                } else {
                    ContactsSearchManager.startSearch(this, initialQuery);
                }
            }
        }
    }

    /**
     * Performs filtering of the list based on the search query entered in the
     * search text edit.
     */
    protected void onSearchTextChanged() {
        // Set the proper empty string
        setEmptyText();

        Filter filter = mAdapter.getFilter();
        filter.filter(getTextFilter());
    }

    /**
     * Starts a new activity that will run a search query and display search results.
     */
    private void doSearch() {
        Log.d(TAG,"#####################doSearch");
        String query = getTextFilter();
        if (TextUtils.isEmpty(query)) {
            return;
        }

        Intent intent = new Intent(this, SearchResultsActivity.class);
        Intent originalIntent = getIntent();
        Bundle originalExtras = originalIntent.getExtras();
        if (originalExtras != null) {
            intent.putExtras(originalExtras);
        }

        intent.putExtra(SearchManager.QUERY, query);
        if ((mMode & MODE_MASK_PICKER) != 0) {
            intent.setAction(ACTION_SEARCH_INTERNAL);
            intent.putExtra(SHORTCUT_ACTION_KEY, mShortcutAction);
            intent.putExtra(FILTER_SIM_CONTACT_KEY, mFilterSIMContact);
            intent.putExtra("request_email", originalIntent.getBooleanExtra("request_email", false));
            intent.putExtra(BROADCAST_RESULT, originalIntent.getBooleanExtra(BROADCAST_RESULT, false));
            if (mShortcutAction != null) {
                if (Intent.ACTION_CALL.equals(mShortcutAction)
                        || Intent.ACTION_SENDTO.equals(mShortcutAction)) {
                    intent.putExtra(Insert.PHONE, query);
                }
            } else {
                switch (mQueryMode) {
                    case QUERY_MODE_MAILTO:
                        intent.putExtra(Insert.EMAIL, query);
                        break;
                    case QUERY_MODE_TEL:
                        intent.putExtra(Insert.PHONE, query);
                        break;
                }
            }
            startActivityForResult(intent, SUBACTIVITY_SEARCH);
        } else {
            intent.setAction(Intent.ACTION_SEARCH);
            startActivity(intent);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
            case R.string.import_from_sim:
            case R.string.import_from_sim1:
            case R.string.import_from_sim2:
            case R.string.import_from_sdcard: {
                return AccountSelectionUtil.getSelectAccountDialog(this, id);
            }
            case R.id.dialog_sdcard_not_found: {
                mSdCardNtFdAlertDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.no_sdcard_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.no_sdcard_message)
                        .setPositiveButton(android.R.string.ok, null).create();
                return mSdCardNtFdAlertDialog;
            }
            case R.id.dialog_delete_contact_confirmation: {
                mDelConfm1AlertDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.deleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                new DeleteClickListener()).create();
                return mDelConfm1AlertDialog;
            }
            case R.id.dialog_readonly_contact_hide_confirmation: {
                mHideConfmAlertDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.readOnlyContactWarning)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                new DeleteClickListener()).create();
                return mHideConfmAlertDialog;
            }
            case R.id.dialog_readonly_contact_delete_confirmation: {
                mDelConfm2AlertDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.readOnlyContactDeleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                new DeleteClickListener()).create();
                return mDelConfm2AlertDialog;
            }
            case R.id.dialog_multiple_contact_delete_confirmation: {
                mDelConfm3AlertDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.multipleContactDeleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                new DeleteClickListener()).create();
                return mDelConfm3AlertDialog;
            }
            case R.id.dialog_sim_contact_delete_confirmation: {//80794
			        mDelConfm4AlertDialog = new AlertDialog.Builder(this).setTitle(
					R.string.deleteConfirmation_title).setIcon(
					android.R.drawable.ic_dialog_alert).setMessage(
					R.string.deleteConfirmation).setNegativeButton(
					android.R.string.cancel, null).setPositiveButton(
					android.R.string.ok, new DeleteClickListener()).create();
                    return mDelConfm4AlertDialog;
			}
			case R.id.dialog_batch_contact_delete_confirmation: {// mtk80909
				mBatchDeleteClickListener = new BatchDeleteClickListener();
				mDelConfm5AlertDialog = new AlertDialog.Builder(this).setTitle(
						R.string.deleteConfirmation_title).setIcon(
						android.R.drawable.ic_dialog_alert).setMessage(
						R.string.batchContactDeleteConfirmation).setNegativeButton(
						android.R.string.cancel, null).setPositiveButton(
						android.R.string.ok, mBatchDeleteClickListener).create();
		        return mDelConfm5AlertDialog;
            }
        }
        return super.onCreateDialog(id, bundle);
    }

    /**
     * Create a {@link Dialog} that allows the user to pick from a bulk import
     * or bulk export task across all contacts.
     */
//    private void displayImportExportDialog() {
//        // Wrap our context to inflate list items using correct theme
//        final Context dialogContext = new ContextThemeWrapper(this, android.R.style.Theme_Light);
//        final Resources res = dialogContext.getResources();
//        final LayoutInflater dialogInflater = (LayoutInflater)dialogContext
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
//        if (iTel == null) {
//        	return;
//        }
//        // Adapter that shows a list of string resources
//        final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
//                android.R.layout.simple_list_item_1) {
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                if (convertView == null) {
//                    convertView = dialogInflater.inflate(android.R.layout.simple_list_item_1,
//                            parent, false);
//                }
//
//                final int resId = this.getItem(position);
//                ((TextView)convertView).setText(resId);
//                return convertView;
//            }
//        };
//		if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
//        	int sim1Id = com.android.internal.telephony.Phone.GEMINI_SIM_1;
//        	int sim2Id = com.android.internal.telephony.Phone.GEMINI_SIM_2;
//
//        	boolean sim1Ready = false, sim2Ready = false;
//            try {
//            	mSim1Type = iTel.getIccCardTypeGemini(sim1Id);
//                if (null != iTel && iTel.hasIccCardGemini(sim1Id)
//                        && iTel.isRadioOnGemini(sim1Id)
//                        && TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(sim1Id)
//                        && !iTel.isFDNEnabledGemini(sim1Id)) {
//                	if (mSim1Type.equals("SIM")) {
//                    adapter.add(R.string.import_from_sim1);
//                        adapter.add(R.string.copy_phone_to_sim1); // ContactsCopy
//                	} else if (mSim1Type.equals("USIM")) {
//                		adapter.add(R.string.import_from_usim1);
//                        adapter.add(R.string.copy_phone_to_usim1); // ContactsCopy
//                	}
//                    sim1Ready = true; // ContactsCopy
//                }
//            } catch (RemoteException ex) {
//                ex.printStackTrace();
//            }
//            try {
//            	mSim2Type = iTel.getIccCardTypeGemini(sim2Id);
//                if (null != iTel && iTel.hasIccCardGemini(sim2Id)
//                        && iTel.isRadioOnGemini(sim2Id)
//                        && TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(sim2Id)
//                        && !iTel.isFDNEnabledGemini(sim2Id)) {
//                	if (mSim2Type.equals("SIM")) {
//                    adapter.add(R.string.import_from_sim2);
//                         adapter.add(R.string.copy_phone_to_sim2); // ContactsCopy
//                	} else if (mSim2Type.equals("USIM")) {
//                		 adapter.add(R.string.import_from_usim2);
//                         adapter.add(R.string.copy_phone_to_usim2); // ContactsCopy
//                	}
//                    sim2Ready = true; // ContactsCopy
//                }
//            } catch (RemoteException ex) {
//                ex.printStackTrace();
//            }
//            // ContactsCopy starts
//            if (sim1Ready && sim2Ready) {
//            	if (mSim1Type.equals("SIM")) {
//            	adapter.add(R.string.copy_sim1_to_sim2);
//            	adapter.add(R.string.copy_sim2_to_sim1);
//            	} else if (mSim1Type.equals("USIM")) {
//            		adapter.add(R.string.copy_usim1_to_sim2);
//                	adapter.add(R.string.copy_sim2_to_usim1);
//            	} 
//            	if (mSim2Type.equals("USIM")) {
//            		adapter.add(R.string.copy_sim1_to_usim2);
//                	adapter.add(R.string.copy_usim2_to_sim1);
//            	} 
//            	if (mSim1Type.equals("USIM") && mSim2Type.equals("USIM")) {
//            		adapter.add(R.string.copy_usim1_to_usim2);
//                	adapter.add(R.string.copy_usim2_to_usim1);
//            	}
//            }
//            // ContactsCopy ends
//        } else {
//            try {
//            	mSimType = iTel.getIccCardType();
//                if (null != iTel && iTel.hasIccCard() && iTel.isRadioOn() 
//                		&& TelephonyManager.getDefault().getSimState() == TelephonyManager.SIM_STATE_READY
//                		&& !iTel.isFDNEnabled()) {
//                	if (mSimType.equals("SIM")) {
//            adapter.add(R.string.import_from_sim);
//                        adapter.add(R.string.copy_phone_to_sim);
//                	} else if (mSimType.equals("USIM")) {
//                		adapter.add(R.string.import_from_usim);
//                        adapter.add(R.string.copy_phone_to_usim);
//                	}
//        }
//            } catch (RemoteException ex) {
//                ex.printStackTrace();
//            }
//        }
//
//        if (res.getBoolean(R.bool.config_allow_import_from_sdcard)) {
//            adapter.add(R.string.import_from_sdcard);
//        }
//        if (res.getBoolean(R.bool.config_allow_export_to_sdcard)) {
//            adapter.add(R.string.export_to_sdcard);
//        }
//        if(true == FeatureOption.MTK_SNS_SUPPORT) {
//        	SNSAccount accounts[] = DataManager.getAccounts();
//        	boolean hasAccountsExceptTwitter = false;
//        	for(int i = 0; null != accounts && !hasAccountsExceptTwitter && i < accounts.length; i++){
//        		if(!accounts[i].account_name.toLowerCase().equals("mtk_twitter")){
//        			hasAccountsExceptTwitter = true;
//        		}
//        	}
//        	if(hasAccountsExceptTwitter) {
//        		adapter.add(R.string.import_from_sns);
//        	}
//        }
//        if (res.getBoolean(R.bool.config_allow_share_visible_contacts)) {
//            adapter.add(R.string.share_visible_contacts);
//        }
//
//        final DialogInterface.OnClickListener clickListener =
//                new DialogInterface.OnClickListener() {
//
//        	private int tmpIndex = 0;
//			private int tmpSize = 0;
//			private SNSAccountInfo[] accounts = null;
//			private SNSAccountInfo[] accountsWithoutTwitter = null;
//			private SNSAccountInfo tmpAccount = null;
//			
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                if (!adapter.isEnabled(which)) {
//                	Log.w(TAG, "item " + which + " is disabled.");
//                	return;
//                } else {
//                	Log.w(TAG, "item " + which + " is enabled.");
//                }
//                final int resId = adapter.getItem(which);
//
//
//                Context myContext = ContactsListActivity.this; 
//                Intent copyIntent = new Intent(myContext, ContactsMarkListActivity.class);
//                switch (resId) {
//                    case R.string.import_from_sim1:
//                    case R.string.import_from_sim2:
//                    case R.string.import_from_sim:
//                    case R.string.import_from_usim1:
//                    case R.string.import_from_usim2:
//                    case R.string.import_from_usim:
//                    case R.string.import_from_sdcard: {
//                        handleImportRequest(resId);
//                        break;
//                    }
//                    case R.string.copy_phone_to_sim1:{
//                    	copyIntent.putExtra("src", RawContacts.INDICATE_PHONE);
//                        copyIntent.putExtra("dst", RawContacts.INDICATE_SIM1);
//                        myContext.startActivity(copyIntent);
//                        break;
//                    }
//       
//                    case R.string.copy_phone_to_sim2: {
//                    	copyIntent.putExtra("src", RawContacts.INDICATE_PHONE);
//                        copyIntent.putExtra("dst", RawContacts.INDICATE_SIM2);
//                        myContext.startActivity(copyIntent);
//                        break;
//                    }
//                    
//                    case R.string.copy_phone_to_sim: {
//                    	copyIntent.putExtra("src", RawContacts.INDICATE_PHONE);
//                        copyIntent.putExtra("dst", RawContacts.INDICATE_SIM);
//                        myContext.startActivity(copyIntent);
//                        break;
//                    }
//                    
//                    case R.string.copy_phone_to_usim1:{//for USIM
//                    	copyIntent.putExtra("src", RawContacts.INDICATE_PHONE);
//                        copyIntent.putExtra("dst", RawContacts.INDICATE_USIM1);
//                        myContext.startActivity(copyIntent);
//                        break;
//                    }
//       
//                    case R.string.copy_phone_to_usim2: {
//                    	copyIntent.putExtra("src", RawContacts.INDICATE_PHONE);
//                        copyIntent.putExtra("dst", RawContacts.INDICATE_USIM2);
//                        myContext.startActivity(copyIntent);
//                        break;
//                    }
//                    
//                    case R.string.copy_phone_to_usim: {
//                    	copyIntent.putExtra("src", RawContacts.INDICATE_PHONE);
//                        copyIntent.putExtra("dst", RawContacts.INDICATE_USIM);
//                        myContext.startActivity(copyIntent);
//                        break;
//                    }
//                    
//                    case R.string.copy_sim1_to_sim2: {
//                    	copyIntent.putExtra("src", RawContacts.INDICATE_SIM1);
//                        copyIntent.putExtra("dst", RawContacts.INDICATE_SIM2);
//                        myContext.startActivity(copyIntent);
//                        break;
//                    }
//                    
//                    case R.string.copy_sim1_to_usim2: {
//                    	copyIntent.putExtra("src", RawContacts.INDICATE_SIM1);
//                        copyIntent.putExtra("dst", RawContacts.INDICATE_USIM2);
//                        myContext.startActivity(copyIntent);
//                        break;
//                    }
//              
//                    case R.string.copy_sim2_to_sim1: {
//                    	copyIntent.putExtra("src", RawContacts.INDICATE_SIM2);
//                        copyIntent.putExtra("dst", RawContacts.INDICATE_SIM1);
//                        myContext.startActivity(copyIntent);
//                        break;
//                    }
//                    
//                    case R.string.copy_sim2_to_usim1: {
//                    	copyIntent.putExtra("src", RawContacts.INDICATE_SIM2);
//                        copyIntent.putExtra("dst", RawContacts.INDICATE_USIM1);
//                        myContext.startActivity(copyIntent);
//                        break;
//                    }                                    
//                    
//                    case R.string.copy_usim1_to_usim2: {
//                    	copyIntent.putExtra("src", RawContacts.INDICATE_USIM1);
//                        copyIntent.putExtra("dst", RawContacts.INDICATE_USIM2);
//                        myContext.startActivity(copyIntent);
//                        break;
//                    }
//                    
//                    case R.string.copy_usim1_to_sim2: {
//                    	copyIntent.putExtra("src", RawContacts.INDICATE_USIM1);
//                        copyIntent.putExtra("dst", RawContacts.INDICATE_SIM2);
//                        myContext.startActivity(copyIntent);
//                        break;
//                    }
//                    
//                    case R.string.copy_usim2_to_sim1: {
//                    	copyIntent.putExtra("src", RawContacts.INDICATE_USIM2);
//                        copyIntent.putExtra("dst", RawContacts.INDICATE_SIM1);
//                        myContext.startActivity(copyIntent);
//                        break;
//                    }
//                    
//                    case R.string.copy_usim2_to_usim1: {
//                    	copyIntent.putExtra("src", RawContacts.INDICATE_USIM2);
//                        copyIntent.putExtra("dst", RawContacts.INDICATE_USIM1);
//                        myContext.startActivity(copyIntent);
//                        break;
//                    }
//                    
//                    case R.string.export_to_sdcard: {
//                        Context context = ContactsListActivity.this;
//                        Intent exportIntent = new Intent(context, ExportVCardActivity.class);
//                        context.startActivity(exportIntent);
//                        break;
//                    }
//                    case R.string.import_from_sns: {
//                    	if(false == FeatureOption.MTK_SNS_SUPPORT){
//                    		return;
//                    	}
//                    	tmpIndex = 0;
//            			tmpSize = 0;
//            			accounts = DataManager.getAllSnsAccountInfo();
//            			accountsWithoutTwitter = null;
//            			tmpAccount = null;
//            			for(int i = 0; accounts != null && i < accounts.length; i++) {
//            				tmpAccount = accounts[i];
//            				if(tmpAccount.sns_id == TWITTER_TYPE) continue;
//            				tmpSize++;
//            			}
//            			accountsWithoutTwitter = new SNSAccountInfo[tmpSize];
//            			for(int i = 0; accounts != null && i < accounts.length; i++) {
//            				tmpAccount = accounts[i];
//            				if(tmpAccount.sns_id == TWITTER_TYPE) continue;
//            				accountsWithoutTwitter[tmpIndex] = tmpAccount;
//            				tmpIndex++;
//            			}
//            			switch(accountsWithoutTwitter.length) {
//            			case 0:
//            				Log.i(TAG, "Error: Account error, only have twitter account ...");
//            				break;
//            			case 1:
//            				startImportSNSActivity(accountsWithoutTwitter[0].account_id, 12);
//            				break;
//            			default:
//                            startSingleAccountSelect();
//            				break;
//            			}
//                    	break;
//                    }
//                    case R.string.share_visible_contacts: {
//                        doShareVisibleContacts("Multi_Contact", null);//add param for share single contact
//                        break;
//                    }
//                    default: {
//                        Log.e(TAG, "Unexpected resource: " +
//                                getResources().getResourceEntryName(resId));
//                    }
//                }
//            }
//        };
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle(R.string.dialog_import_export);
//            builder.setNegativeButton(android.R.string.cancel, null);
//            builder.setSingleChoiceItems(adapter, -1, clickListener);
//            mImportAlertDialog = builder.create();
//            mImportAlertDialog.show();
//    }

    private void startSingleAccountSelect() {
    	if(false == FeatureOption.MTK_SNS_SUPPORT){
    		return;
    	}
		try {
        	Intent intent = new Intent();
    		intent.setAction("com.mediatek.client.Account");
    		intent.addCategory("android.intent.category.AccountSingle");
    		intent.putExtra("UndisplayedSnsId", "3#");
    		startActivityForResult(intent, SUBACTIVITY_SELECT_ACCOUNT);
    	}
    	catch(Exception ex) {
    		ex.printStackTrace();
    	}
	}
    
    private void startImportSNSActivity(int account, int limitNumber) {
    	if(false == FeatureOption.MTK_SNS_SUPPORT) {
    		return;
    	}
		Intent intent = new Intent();
		intent.setAction("com.mediatek.contacts.intent.importsnsfriends");
		intent.putExtra("AccountId", account);
		try {
			startActivity(intent);
		}
		catch(Exception ex) {
			ex.printStackTrace();
    	}
	}
    
    private void doShareVisibleContacts(String type, Uri uri) {//add param for share single contact            
        //MTK
    	Cursor cursor = null;
    	int contactId = -1;
    	Log.i(TAG,"uri is " + uri);
    	Log.i(TAG,"type is " + type);
        try {
            	if (type == "Single_Contact") {
            		Log.i(TAG,"In single contact");            		
            		cursor = getContentResolver().query(uri,
                sLookupProjection, appendContactSelection(getContactSelection(),QUERY_CONTACT_TABLE), null, null);

            		Log.i(TAG,"cursor is " + cursor);
            		if (cursor != null && cursor.moveToNext()) {
            			Cursor c = (Cursor) mAdapter.getItem(mPosition);
            			if (c != null) {
            				contactId = c.getInt(c.getColumnIndexOrThrow(Contacts._ID));
//            				c.close();
            			}            			
                		Log.i(TAG,"Single_Contact  cursor.getCount() is "+cursor.getCount());
                		Log.i(TAG,"original contactId is " + contactId);
            			uri = Uri.withAppendedPath(
                                Contacts.CONTENT_VCARD_URI,
                                cursor.getString(0));
                		Log.i(TAG,"Single_Contact  uri is "+uri+" \ncursor.getString(0) is "+cursor.getString(0));
            		}
            	} else if (type == "Multi_Contact") {
            		cursor = getContentResolver().query(Contacts.CONTENT_URI,
                            sLookupProjection, appendContactSelection(getContactSelection(),QUERY_CONTACT_TABLE), null, null);
            		if (cursor != null) {
            		Log.i(TAG,"Multi_Contact  cursor.getCount() is "+cursor.getCount());
            		}
            if (!cursor.moveToFirst()) {
                Toast.makeText(this, R.string.share_error, Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder uriListBuilder = new StringBuilder();
            int index = 0;
            for (;!cursor.isAfterLast(); cursor.moveToNext()) {
                if (index != 0)
                    uriListBuilder.append(':');
                uriListBuilder.append(cursor.getString(0));
                index++;
            }
                    uri = Uri.withAppendedPath(
                    Contacts.CONTENT_MULTI_VCARD_URI,
                    Uri.encode(uriListBuilder.toString()));
                    Log.i(TAG,"Multi_Contact  uri is "+uri);
            	}
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(Contacts.CONTENT_VCARD_TYPE);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            if (contactId != -1) intent.putExtra("contactId", contactId);
            startActivity(intent);
            } catch (Exception e) {
            	Log.e(TAG, "bad menuInfo", e);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

//    private void handleImportRequest(int resId) {
//        // There's three possibilities:
//        // - more than one accounts -> ask the user
//        // - just one account -> use the account without asking the user
//        // - no account -> use phone-local storage without asking the user
//        final Sources sources = Sources.getInstance(this);
//        final List<Account> accountList = sources.getAccounts(true);
//        final int size = accountList.size();
//        if (size > 1) {
//            showDialog(resId);
//            return;
//        }
//
//        AccountSelectionUtil.doImport(this, resId, (size == 1 ? accountList.get(0) : null));
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SUBACTIVITY_NEW_CONTACT:
                if (resultCode == RESULT_OK) {
                    returnPickerResult(null, data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
                            data.getData(), (mMode & MODE_MASK_PICKER) != 0
                            ? Intent.FLAG_GRANT_READ_URI_PERMISSION : 0);
                }
                break;

            case SUBACTIVITY_VIEW_CONTACT:
                if (resultCode == RESULT_OK) {
                    mAdapter.notifyDataSetChanged();
                }
                break;

            case SUBACTIVITY_DISPLAY_GROUP:
                // Mark as just created so we re-run the view query
                mJustCreated = true;
                break;

            case SUBACTIVITY_FILTER:
            case SUBACTIVITY_SEARCH:
                // Pass through results of filter or search UI
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
            
             // modified by Ivan 2010-07-25
            case SUBACTIVITY_VIEW_OWNER:
            	if(false == FeatureOption.MTK_SNS_SUPPORT)
            	{
            		return;
            	}
            	break;
            	
            case SUBACTIVITY_NEW_OWNER_INSERT:
            	if(false == FeatureOption.MTK_SNS_SUPPORT)
            	{
            		return;
            	}
            	if (resultCode == RESULT_OK) {
            		final long contactId = data.getLongExtra(
            				"contact_id", -1);
            		String lookupKey = data.getData().getPathSegments().get(2);
            		String contactName = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
            		PhoneOwner.initPhoneOwner(contactId, lookupKey);
            		PhoneOwner owner = PhoneOwner.getInstance();
			if(null == contactName || contactName.length() == 0)
				contactName = this.getText(android.R.string.unknownName) + "";
            		if(null != owner) owner.setName(contactName);
            	}
            	break;
            	
            case SUBACTIVITY_SELECT_ACCOUNT:
            	if(false == FeatureOption.MTK_SNS_SUPPORT)
            	{
            		return;
            	}
            	if (resultCode == RESULT_OK) {
            		if(data != null) {
            			Bundle bun = data.getExtras();
            			if(bun != null) {
            				if(resultAccount != null) {
            					resultAccount.clear();
            				}        				
            				resultAccount = bun.getIntegerArrayList("data");        				
            				if(resultAccount != null && resultAccount.size() >= 0) {
            					startImportSNSActivity(resultAccount.get(0), 12);
            				}
            			}
            		}
            	}
            	break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        // If Contacts was invoked by another Activity simply as a way of
        // picking a contact, don't show the context menu
        if ((mMode & MODE_MASK_PICKER) == MODE_MASK_PICKER) {
            return;
        }
		// mtk80909, If Contacts is under delete state, no menu will be
		// displayed.
		if (mDeleteState)
			return;
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }
	        
	        //add by huibin 2010-08-31
	        if(true == FeatureOption.MTK_SNS_SUPPORT)
	        {
	        	// modified by Ivan 2010-08-24 19:03
			PhoneOwner owner = PhoneOwner.getInstance();
			if(bIsOwnerShow && ((info.position == 1 && info.id == 0) || (null != owner && info.id == owner.getOwnerID()))){
			    long id = info.id;

	        	    if(owner == null){
	        		menu.setHeaderTitle(this.getResources().getString(R.string.owner_none));
	        		menu.add(0, MENU_OWNER_EDIT, 0, R.string.menu_editContact);
        			menu.add(0, MENU_OWNER_DELETE, 0, R.string.menu_deleteContact)
        				.setVisible(false);
	        	    }
	        	    else{
				Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, owner.getOwnerID());
	            		long rawContactId = ContactsUtils.queryForRawContactId(getContentResolver(), owner.getOwnerID());
	            		Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
	        		menu.setHeaderTitle(owner.getName());
	        		menu.add(0, MENU_OWNER_EDIT, 0, R.string.menu_editContact).setIntent(
					new Intent(Intent.ACTION_EDIT, rawContactUri));
        			menu.add(0, MENU_OWNER_DELETE, 0, R.string.menu_deleteContact);
	        	    }
	        	    return;
	        	}
	        	// end modified
	        }
   
	Cursor cursor = null;
	cursor = (Cursor) getListAdapter().getItem(info.position);
	if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }
        long id = info.id;
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
        long rawContactId = ContactsUtils.queryForRawContactId(getContentResolver(), id);
        Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);

        // Setup the menu header
        String header=cursor.getString(getSummaryDisplayNameColumnIndex());
        if (TextUtils.isEmpty(header)){
            menu.setHeaderTitle(R.string.unknown);

        }else{
            menu.setHeaderTitle(header);
        }

        // View contact details
        final Intent viewContactIntent = new Intent(Intent.ACTION_VIEW, contactUri);
        StickyTabs.setTab(viewContactIntent, getIntent());
		// empty uri protection
		if (contactUri != null && !TextUtils.isEmpty(contactUri.toString())) {
			menu.add(0, MENU_ITEM_VIEW_CONTACT, 0, R.string.menu_viewContact)
				.setIntent(viewContactIntent);
		} else {
			menu.add(0, MENU_ITEM_VIEW_CONTACT, 0, R.string.menu_viewContact);
		}

        if (cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX) != 0) {
            // Calling contact
//            menu.add(0, MENU_ITEM_CALL, 0, getString(R.string.menu_call));
            // Send SMS item
//            menu.add(0, MENU_ITEM_SEND_SMS, 0, getString(R.string.menu_sendSMS));
        }

        // Contact editing
		// mtk80909 modify start
		int simPhoneIndicate = cursor.getInt(cursor
				.getColumnIndexOrThrow(Contacts.INDICATE_PHONE_SIM));
		mSlot = SIMInfo.getSlotById(ContactsListActivity.this, (long)simPhoneIndicate);
		Log.i(TAG,"onCreateContextMenu simPhoneIndicate is " + simPhoneIndicate);
		Log.i(TAG,"onCreateContextMenu mSlot is " + mSlot);
        // Star toggling
        int starState = cursor.getInt(SUMMARY_STARRED_COLUMN_INDEX);
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
			if (true == com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
				if (mSlot == com.android.internal.telephony.Phone.GEMINI_SIM_1 && null != iTel
						&& (!iTel
								.hasIccCardGemini(mSlot) || !iTel
								.isRadioOnGemini(mSlot) || !sim1Ready
								|| iTel.isFDNEnabledGemini(mSlot))) {
					addEditAndDelete = false;
				} else if (mSlot == com.android.internal.telephony.Phone.GEMINI_SIM_2 && null != iTel
						&& (!iTel
								.hasIccCardGemini(mSlot) || !iTel
								.isRadioOnGemini(mSlot) || !sim2Ready
								|| iTel.isFDNEnabledGemini(mSlot))) {
					addEditAndDelete = false;
				}
			} else {
				if (mSlot == 0 && null != iTel
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
		// mtk80909 end

		// mtk80909 for Speed Dial
		considerAddSpeedDialMenuItem(menu, cursor, contactUri);

		// mtk80909 end
		  final Context dialogContext = new ContextThemeWrapper(this, android.R.style.Theme_Light);//add share single contact
	      final Resources res = dialogContext.getResources();
		if (res.getBoolean(R.bool.config_allow_share_visible_contacts)) {
			menu.add(0, MENU_ITEM_SHARE, 0, R.string.share_contacts);
//            adapter.add(R.string.share_visible_contacts);
        }
	}
	
	// mtk80909 for Speed Dial
	private void considerAddSpeedDialMenuItem(ContextMenu menu, 
			Cursor cursor, Uri contactUri) {
		boolean hasPhone = (cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX) != 0);
		if (!hasPhone) return;
		final Intent intent = new Intent(this, AddSpeedDialActivity.class);
		intent.setData(contactUri);
		StickyTabs.setTab(intent, getIntent());
		menu.add(0, MENU_ITEM_SPEED_DIAL, 0, R.string.speed_dial_view)
				.setIntent(intent);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }
		Cursor cursor = null;
		try{
		    	cursor = (Cursor) getListAdapter().getItem(info.position);
		} catch(Exception e){
			if(null != cursor) cursor.close();
			cursor = null;
                        return false;
		}
		    mPosition = info.position;
		long indicate = -1;
		if(null != cursor) {
			indicate = cursor.getInt(cursor
				.getColumnIndexOrThrow(RawContacts.INDICATE_PHONE_SIM));
		}
		Log.i(TAG,"onContextItemSelected indicate is " + indicate);
		mSlot = SIMInfo.getSlotById(ContactsListActivity.this, (long)indicate);
        switch (item.getItemId()) {
            case MENU_ITEM_TOGGLE_STAR: {
                // Toggle the star
                ContentValues values = new ContentValues(1);
                values.put(Contacts.STARRED, cursor.getInt(SUMMARY_STARRED_COLUMN_INDEX) == 0 ? 1 : 0);
                final Uri selectedUri = this.getContactUri(info.position);
				if(null != selectedUri)
                getContentResolver().update(selectedUri, values, null, null);
                return true;
            }

            case MENU_ITEM_CALL: {
                callContact(cursor);
                return true;
            }

            case MENU_ITEM_SEND_SMS: {
                smsContact(cursor);
                return true;
            }

            case MENU_ITEM_DELETE: {
                if (indicate > RawContacts.INDICATE_PHONE) {
            		mSelectedContactUri = getContactUri(mPosition);
            		Log.i(TAG,"mSelectedContactUri IS "+mSelectedContactUri);
            		showDialog(R.id.dialog_sim_contact_delete_confirmation);
                return true;
            	} else {
            		doContactDelete(getContactUri(mPosition));
                    return true;
            	}
            }    
            case MENU_ITEM_EDIT: {
			if (indicate > RawContacts.INDICATE_PHONE) {
					final Uri uri = getSelectedUri(mPosition);
					Log.i(TAG,"uri is "+uri);
					final Intent intent = new Intent(this,
							EditSimContactActivity.class);
					intent.setData(uri);
					intent.putExtra("action", Intent.ACTION_EDIT);
					intent.putExtra(RawContacts.INDICATE_PHONE_SIM, indicate);
					intent.putExtra("slotId", mSlot);
					Log.i(TAG,"MENU_ITEM_EDIT mSlot is " + mSlot);
					startActivity(intent);
					return true;
			} else {
				long id = info.id;
				Uri contactUri = ContentUris.withAppendedId(
						Contacts.CONTENT_URI, id);
              
//				long rawContactId = ContactsUtils.queryForRawContactId(
//						getContentResolver(), id);
//				Uri rawContactUri = ContentUris.withAppendedId(
//						RawContacts.CONTENT_URI, rawContactId);
				final Intent intent = new Intent(Intent.ACTION_EDIT,
						contactUri);
				startActivity(intent);
				return true;
			    }
            }
            
            // modified by Ivan 2010-08-03
            case MENU_OWNER_EDIT:{
            	if(false == FeatureOption.MTK_SNS_SUPPORT)
            	{
            		return true;
            	}
            	if(PhoneOwner.getInstance() == null){
        			this.startActivityForResult(
        					new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI)
        					.putExtra(NEW_OWNER_INFO, true),
        					SUBACTIVITY_NEW_OWNER_INSERT);
            	}
            	else{
			long rawContactId = ContactsUtils.queryForRawContactId(getContentResolver(), 
				PhoneOwner.getInstance().getOwnerID());
	            	Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);

                    	startActivityForResult(new Intent(Intent.ACTION_EDIT, rawContactUri),
                    		SUBACTIVITY_NEW_OWNER_INSERT);
            	}
            	return true;
            }
            	
            case MENU_OWNER_DELETE: {
            	if(false == FeatureOption.MTK_SNS_SUPPORT)
            	{
            		return true;
            	}
            	doOwnerDelete();
            	return true;
            }

            case MENU_ITEM_SHARE: {
//            	final Uri selectedUri = this.getContactUri(info.position);
            	final Uri uri = getSelectedUri(mPosition);
            	if (uri != null) doShareVisibleContacts("Single_Contact", uri);//share single contact
            	return true;
            }
            
        }

        return super.onContextItemSelected(item);
    }

    // mtk80909 modified for ALPS00023212
    String[] getPhoneNumber(Cursor cursor) {
//		String phone = null;
		String[] returnValue = new String[]{"", null};
//		if (cursor != null) {
			boolean hasPhone = cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX) != 0;
			if (!hasPhone) {
				// There is no phone number.
				signalError();
				return returnValue;
			}

			Cursor phonesCursor = null;
			phonesCursor = queryPhoneNumbers(cursor
					.getLong(SUMMARY_ID_COLUMN_INDEX));
			if (phonesCursor == null || phonesCursor.getCount() == 0) {
				// No valid number
				signalError();
				if (phonesCursor != null) phonesCursor.close();
				return returnValue;
			} else if (phonesCursor.getCount() == 1) {
				returnValue[0] = phonesCursor.getString(phonesCursor
						.getColumnIndex(Phone.NUMBER));
			    returnValue[1] = phonesCursor.getString(phonesCursor
			            .getColumnIndex(Data.DATA15));
			    phonesCursor.close();
				return returnValue;
			} else {
				// phonesCursor.moveToPosition(-1);
				if (phonesCursor != null) {
					while (phonesCursor.moveToNext()) {
						if (phonesCursor.getInt(phonesCursor
								.getColumnIndex(Phone.IS_SUPER_PRIMARY)) != 0) {
							returnValue[0] = phonesCursor.getString(phonesCursor
									.getColumnIndex(Phone.NUMBER));
			                returnValue[1] = phonesCursor.getString(phonesCursor
			                        .getColumnIndex(Data.DATA15));
			                phonesCursor.close();
							return returnValue;
						}
					}
					phonesCursor.close();
				}
			}
//			cursor.close();
//		}
		return returnValue;
	}
    
    protected void doOwnerDelete() {
    	if(false == FeatureOption.MTK_SNS_SUPPORT)
        {
		return;
        }
    	Uri ownerContactUri = null;
    	if ( PhoneOwner.getInstance() == null ){
    		return;
    	}
    	String lookupKey = PhoneOwner.getInstance().getOwnerLookupKey();
    	if(lookupKey == null){
    		mSelectedContactUri = Contacts.getLookupUri(PhoneOwner.getInstance().getOwnerID(), 
            		lookupKey);
        }else{
            Uri lookupUri = Contacts.getLookupUri(PhoneOwner.getInstance().getOwnerID(), 
            		PhoneOwner.getInstance().getOwnerLookupKey());
            mSelectedContactUri = Contacts.lookupContact(getContentResolver(), lookupUri);
        }
    	
        mReadOnlySourcesCnt = 0;
        mWritableSourcesCnt = 0;
        mWritableRawContactIds.clear();

        if (mSelectedContactUri != null) {
            Cursor c = getContentResolver().query(RawContacts.CONTENT_URI, RAW_CONTACTS_PROJECTION,
                    RawContacts.CONTACT_ID + "=" + ContentUris.parseId(mSelectedContactUri), null,
                    null);
            if(null == c) return;
            if (c != null) {
		Sources sources = Sources.getInstance(ContactsListActivity.this);
		String accountType;
		long rawContactId;
		ContactsSource contactsSource;
                while (c.moveToNext()) {
                    accountType = c.getString(2);
                    rawContactId = c.getLong(0);
                    contactsSource = sources.getInflatedSource(accountType,
                            ContactsSource.LEVEL_SUMMARY);
                    if (contactsSource != null && contactsSource.readOnly) {
                        mReadOnlySourcesCnt += 1;
                    } else {
                        mWritableSourcesCnt += 1;
                        mWritableRawContactIds.add(rawContactId);
                    }
                }
            	c.close();
            }
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
	else {
		PhoneOwner.setOwner(null);
	}
    }

    /**
     * Event handler for the use case where the user starts typing without
     * bringing up the search UI first.
     */
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (!mSearchMode && (mMode & MODE_MASK_NO_FILTER) == 0 && !mSearchInitiated) {
            int unicodeChar = event.getUnicodeChar();
            if (unicodeChar != 0) {
                mSearchInitiated = true;
                startSearch(new String(new int[]{unicodeChar}, 0, 1), false, null, false);
                return true;
            }
        }
        return false;
    }

    /**
     * Event handler for search UI.
     */
    public void afterTextChanged(Editable s) {
        onSearchTextChanged();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    /**
     * Event handler for search UI.
     */
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            hideSoftKeyboard();
            if (TextUtils.isEmpty(getTextFilter())) {
                finish();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            /*case KeyEvent.KEYCODE_CALL: {
                if (callSelection()) {
                    return true;
                }
                break;
            }*/

            case KeyEvent.KEYCODE_DEL: {
                if (deleteSelection()) {
                    return true;
                }
                break;
            }
			case KeyEvent.KEYCODE_BACK: {
				if (mDeleteState) {
					quitDeleteState();
					return true;
				}
                if (mAddContactState) {
                    mAddContactState = false;
                    //return true;
                }
			}
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                if (callSelection()) {
                    return true;
                }
                break;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean deleteSelection() {
        if ((mMode & MODE_MASK_PICKER) != 0) {
            return false;
        }

        final int position = getListView().getSelectedItemPosition();
        if (position != ListView.INVALID_POSITION) {
            Uri contactUri = getContactUri(position);
            if (contactUri != null) {
                doContactDelete(contactUri);
                return true;
            }
        }
        return false;
    }

    /**
     * Prompt the user before deleting the given {@link Contacts} entry.
     */
    protected void doContactDelete(Uri contactUri) {
        mReadOnlySourcesCnt = 0;
        mWritableSourcesCnt = 0;
        mWritableRawContactIds.clear();

        Sources sources = Sources.getInstance(ContactsListActivity.this);
        Cursor c = getContentResolver().query(RawContacts.CONTENT_URI, RAW_CONTACTS_PROJECTION,
                RawContacts.CONTACT_ID + "=" + ContentUris.parseId(contactUri), null,
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

    /**
     * Dismisses the soft keyboard when the list takes focus.
     */
    public void onFocusChange(View view, boolean hasFocus) {
        if (view == getListView() && hasFocus) {
            hideSoftKeyboard();
        }
    }

    /**
     * Dismisses the soft keyboard when the list takes focus.
     */
    public boolean onTouch(View view, MotionEvent event) {
        if (view == getListView()) {
            hideSoftKeyboard();
        }
        return false;
    }

    /**
     * Dismisses the search UI along with the keyboard if the filter text is empty.
     */
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (mSearchMode && keyCode == KeyEvent.KEYCODE_BACK && TextUtils.isEmpty(getTextFilter())) {
            hideSoftKeyboard();
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        hideSoftKeyboard();
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        int indicate = -1;
        long contactId = -1;//add for gemini enhancement
        boolean isHeader = false;
        
        if(true == FeatureOption.MTK_SNS_SUPPORT)
      	{
      		if(position == 1 && bIsOwnerShow)
    			isHeader = true;
      	}
    	// End
    	if((isHeader && id == 0) || (null != PhoneOwner.getInstance() && id == PhoneOwner.getInstance().getOwnerID())){
		if(false == FeatureOption.MTK_SNS_SUPPORT)
		{
			return;
		}
    		// modified by Ivan
    		Uri lookupUri = null;
    		Uri ownerUri = null;
    		PhoneOwner owner = PhoneOwner.getInstance();
    		if(owner != null){
    			lookupUri = Contacts.getLookupUri(owner.getOwnerID(), owner.getOwnerLookupKey());
    			ownerUri = Contacts.lookupContact(getContentResolver(), lookupUri);
    		}
    		Intent ownerIntent = new Intent();
    		ownerIntent.putExtra("owner_id", PhoneOwner.getInstance() == null ? (long)-1 : PhoneOwner.getInstance().getOwnerID());
    		ownerIntent.setData(ownerUri);
    		ownerIntent.setClassName("com.android.contacts", 
    				"com.android.contacts.ContactOwnerActivity");
    		startActivityForResult(ownerIntent, SUBACTIVITY_VIEW_OWNER);
		}
	else {
        if (mSearchMode && mAdapter.isSearchAllContactsItemPosition(position)) {
            doSearch();
        } else if (mMode == MODE_INSERT_OR_EDIT_CONTACT || mMode == MODE_QUERY_PICK_TO_EDIT) {
            Intent intent;
            if (position == 0 && !mSearchMode && mMode != MODE_QUERY_PICK_TO_EDIT) {
                intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
            } else {
                intent = new Intent(Intent.ACTION_EDIT, getSelectedUri(position));
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                intent.putExtras(extras);
            }
            intent.putExtra(KEY_PICKER_MODE, (mMode & MODE_MASK_PICKER) == MODE_MASK_PICKER);

            if (cursor != null) {
                indicate = cursor.getInt(cursor
                        .getColumnIndexOrThrow(RawContacts.INDICATE_PHONE_SIM));
                mSlot = SIMInfo.getSlotById(ContactsListActivity.this, (long)indicate);
                
                contactId = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts._ID));//add for gemini enhancement
            }
            intent.putExtra(RawContacts.INDICATE_PHONE_SIM, indicate);
            intent.putExtra("slotId", mSlot);
            intent.putExtra(Contacts._ID, contactId);//add for gemini enhancement
		    if(true == FeatureOption.MTK_SNS_SUPPORT)
		    {
			intent.putExtra("owner_id", PhoneOwner.getInstance() == null ? 
				(long)-1 : PhoneOwner.getInstance().getOwnerID());
			intent.putExtra("current_contact_id", id);
		    }
            startActivity(intent);
            finish();
        } else if ((mMode & MODE_MASK_CREATE_NEW) == MODE_MASK_CREATE_NEW
                && position == 0) {
            Intent newContact = new Intent(Intents.Insert.ACTION, Contacts.CONTENT_URI);
	            if (mFilterSIMContact) {
	                newContact.putExtra(FILTER_SIM_CONTACT_KEY, mFilterSIMContact);
                }
            startActivityForResult(newContact, SUBACTIVITY_NEW_CONTACT);
        } else if (mMode == MODE_JOIN_CONTACT && id == JOIN_MODE_SHOW_ALL_CONTACTS_ID) {
            mJoinModeShowAllContacts = false;
            startQuery();
        } else if (id > 0) {
            final Uri uri = getSelectedUri(position);
	            if (mMode == MODE_DEFAULT && mDeleteState) {
					// Log.i(TAG, "position = " + position);
					try {
					int firstVisiblePosition = getListView()
							.getFirstVisiblePosition();
							
					// Will perhaps be modified for SNS strategy changes.		
					int tmpPos = position; //(true == FeatureOption.MTK_SNS_SUPPORT && bIsOwnerShow) ? position	: position;
					
					ContactListItemView rootLayout = (ContactListItemView) (getListView()
							.getChildAt(tmpPos - firstVisiblePosition));
					CheckBox cb = rootLayout.getCheckBox();
					
	                contactId = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts._ID));
					//Get Previous check box state
					boolean cbPrevState = cb.isChecked();
					if (cbPrevState) {
						//Remove OP
						cb.setChecked(false);
						//Previous OP has select All Contacts
						if (mSelectAllToDel) {
							if (!mToBeDeletedContacts.isEmpty())
								mToBeDeletedContacts.clear();
							mToBeDeletedContacts.putAll(mAllContacts);
							mToBeDeletedContacts.remove(contactId);
							mSelectAllToDel = false;
							mSelectAllBox.setChecked(mSelectAllToDel);
							mSelectAllButton.setText(mSelectAllText);
						} else {
							mToBeDeletedContacts.remove(contactId);
						}
						mSelected = mToBeDeletedContacts.size();
					} else {
						//Add OP
						cb.setChecked(true);
		                indicate = cursor.getInt(cursor
		                        .getColumnIndexOrThrow(RawContacts.INDICATE_PHONE_SIM));
						mToBeDeletedContacts.put(contactId, indicate);
						mSelected = mToBeDeletedContacts.size();
						if (mSelected == mAllContacts.size()) {
							mSelectAllToDel = true;
							mSelectAllBox.setChecked(mSelectAllToDel);
							mSelectAllButton.setText(mUnSelectText);
						}
					}
					
					// Set button status
					
					mDeleteButton.setText(mDeleteText + "(" + mSelected + ")");
					
//					cb.setChecked(!cb.isChecked());
//					int realPos = mAdapter.getRealPosition(position);
//					mToBeDeleted[realPos] = cb.isChecked();
//					if (cb.isChecked()) mToBeDeletedSet.add(realPos);
//					else mToBeDeletedSet.remove(realPos);
//					mSelected = mToBeDeleted[realPos] ? mSelected + 1
//							: (mSelected < 1 ? 0 : mSelected - 1);
//					// [mtk80909]
//					boolean bSnsOwner = (FeatureOption.MTK_SNS_SUPPORT && !mSearchMode && mMode == MODE_DEFAULT);
//					int length = bSnsOwner ? mToBeDeleted.length - 1 : mToBeDeleted.length;
//					if (bSnsOwner) mToBeDeleted[0] = false;
//					mSelectAllBox.setChecked(mSelected == length);
//					Log.i(TAG, "CheckBox clicked, realPos = " + realPos);
//					mDeleteButton.setText(getResources().getString(
//							R.string.delete_contacts_basic)
//							+ "(" + mSelected + ")");
//					mSelectAllButton
//							.setText(getResources()
//									.getString(
//											mSelected == length ? R.string.unselect_all
//													: R.string.select_all));
					} catch (NullPointerException e) {
	            		Log.w(TAG, "null pointer exception");
	            		e.printStackTrace();
	            	} catch (Exception e) {
	            		Log.w(TAG, "other exception");
	            	}
            } else if ((mMode & MODE_MASK_PICKER) == 0) {
				if (cursor != null) {
			        indicate = cursor.getInt(cursor
			                .getColumnIndexOrThrow(RawContacts.INDICATE_PHONE_SIM));
			        mSlot = SIMInfo.getSlotById(ContactsListActivity.this, (long)indicate);
			        Log.i(TAG,"(mMode & MODE_MASK_PICKER) == 0 indicate is " + indicate);
			        Log.i(TAG,"(mMode & MODE_MASK_PICKER) == 0 mSlot is " + mSlot);
			    }
                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.putExtra(RawContacts.INDICATE_PHONE_SIM, indicate);
                intent.putExtra("slotId", mSlot);
                StickyTabs.setTab(intent, getIntent());
	                if(true == FeatureOption.MTK_SNS_SUPPORT)
			{
				intent.putExtra("owner_id", PhoneOwner.getInstance() == null ? 
					(long)-1 : PhoneOwner.getInstance().getOwnerID());
		              	intent.putExtra("current_contact_id", id);
			}
                // empty uri protection
					if (uri != null && !TextUtils.isEmpty(uri.toString())) {
						startActivityForResult(intent, SUBACTIVITY_VIEW_CONTACT);
					}
            } else if (mMode == MODE_JOIN_CONTACT) {
                returnPickerResult(null, null, uri, 0);
            } else if (mMode == MODE_QUERY_PICK_TO_VIEW) {
                // Started with query that should launch to view contact
                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	                if(true == FeatureOption.MTK_SNS_SUPPORT)
			{
				intent.putExtra("owner_id", PhoneOwner.getInstance() == null ? 
					(long)-1 : PhoneOwner.getInstance().getOwnerID());
              			intent.putExtra("current_contact_id", id);
			}
                startActivity(intent);
                finish();
	            } else if (mMode == MODE_PICK_PHONE || mMode == MODE_QUERY_PICK_PHONE
	                    || mMode == MODE_PICK_PHONE_EMAIL || mMode == MODE_QUERY_PICK_PHONE_EMAIL) {
                Cursor c = (Cursor) mAdapter.getItem(position);
                returnPickerResult(c, c.getString(PHONE_DISPLAY_NAME_COLUMN_INDEX), uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else if ((mMode & MODE_MASK_PICKER) != 0) {
                Cursor c = (Cursor) mAdapter.getItem(position);
                returnPickerResult(c, c.getString(getSummaryDisplayNameColumnIndex()), uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else if (mMode == MODE_PICK_POSTAL
                    || mMode == MODE_LEGACY_PICK_POSTAL
                    || mMode == MODE_LEGACY_PICK_PHONE) {
                returnPickerResult(null, null, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } else {
            signalError();
	        }
        }
    }

    private void hideSoftKeyboard() {
        // Hide soft keyboard, if visible
        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mList.getWindowToken(), 0);
    }

    /**
     * @param selectedUri In most cases, this should be a lookup {@link Uri}, possibly
     *            generated through {@link Contacts#getLookupUri(long, String)}.
     */
    private void returnPickerResult(Cursor c, String name, Uri selectedUri, int uriPerms) {
        final Intent intent = new Intent();

        if (mShortcutAction != null) {
            Intent shortcutIntent;
            if (Intent.ACTION_VIEW.equals(mShortcutAction)) {
                // This is a simple shortcut to view a contact.
                shortcutIntent = new Intent(ContactsContract.QuickContact.ACTION_QUICK_CONTACT);
                shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                shortcutIntent.setData(selectedUri);
                shortcutIntent.putExtra(ContactsContract.QuickContact.EXTRA_MODE,
                        ContactsContract.QuickContact.MODE_LARGE);
                shortcutIntent.putExtra(ContactsContract.QuickContact.EXTRA_EXCLUDE_MIMES,
                        (String[]) null);

                final Bitmap icon = framePhoto(loadContactPhoto(selectedUri, null));
                if (icon != null) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, scaleToAppIconSize(icon));
                } else {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource.fromContext(this,
                                    R.drawable.ic_launcher_shortcut_contact));
                }
            } else {
                // This is a direct dial or sms shortcut.
                String number = c.getString(PHONE_NUMBER_COLUMN_INDEX);
                int type = c.getInt(PHONE_TYPE_COLUMN_INDEX);
                String scheme;
                int resid;
                if (Intent.ACTION_CALL.equals(mShortcutAction)) {
                    //MTK
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        mShortcutAction = Intent.ACTION_DIAL;
                    }
                    scheme = Constants.SCHEME_TEL;
                    resid = R.drawable.badge_action_call;
                } else {
                    scheme = Constants.SCHEME_SMSTO;
                    resid = R.drawable.badge_action_sms;
                }

                // Make the URI a direct tel: URI so that it will always continue to work
                Uri phoneUri = Uri.fromParts(scheme, number, null);
                shortcutIntent = new Intent(mShortcutAction, phoneUri);

                intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                        generatePhoneNumberIcon(selectedUri, type, resid));
            }
            shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            setResult(RESULT_OK, intent);
        } else {
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            intent.addFlags(uriPerms);

			// mtk80909 for Speed Dial
			if (mMode == MODE_PICK_PHONE) {
				String number = c.getString(PHONE_NUMBER_COLUMN_INDEX);
				int simId = c.getInt(PHONE_INDICATE_PHONE_SIM_INDEX);
				int slotId = SIMInfo.getSlotById(ContactsListActivity.this, simId);
				Log.i(TAG, "Speed Dial: number = " + number + ", simId = " + simId);
				Log.i(TAG,"Speed Dial: returnPickerResult slotId is " + slotId);
				intent.putExtra("number", number);
				intent.putExtra("simId", simId);
				intent.putExtra("slotId", slotId);
				/* TODO return more information if needed, 
				 * or using URI only and ask the receiver to query.
				 */
			}
			
			
            setResult(RESULT_OK, intent.setData(selectedUri));
            
            //MTK add for MmsWidget
            Log.d(TAG, "returnPickerResult(), mBroadcastResult="+ mBroadcastResult);
            if (mBroadcastResult) {
                Log.d(TAG, "returnPickerResult(), mBroadcastResult=true");
                
                Intent widgetIntent = new Intent(intent);
                widgetIntent.setAction(ContactsContract.Intents.UPDATE_FILTER_CONTACT);
                ContactsListActivity.this.sendBroadcast(widgetIntent);
            }
        }
        finish();
    }

    private Bitmap framePhoto(Bitmap photo) {
        final Resources r = getResources();
        final Drawable frame = r.getDrawable(com.android.internal.R.drawable.quickcontact_badge);

        final int width = r.getDimensionPixelSize(R.dimen.contact_shortcut_frame_width);
        final int height = r.getDimensionPixelSize(R.dimen.contact_shortcut_frame_height);

        frame.setBounds(0, 0, width, height);

        final Rect padding = new Rect();
        frame.getPadding(padding);

        final Rect source = new Rect(0, 0, photo.getWidth(), photo.getHeight());
        final Rect destination = new Rect(padding.left, padding.top,
                width - padding.right, height - padding.bottom);

        final int d = Math.max(width, height);
        final Bitmap b = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
        final Canvas c = new Canvas(b);

        c.translate((d - width) / 2.0f, (d - height) / 2.0f);
        frame.draw(c);
        c.drawBitmap(photo, source, destination, new Paint(Paint.FILTER_BITMAP_FLAG));

        return b;
    }

    /**
     * Generates a phone number shortcut icon. Adds an overlay describing the type of the phone
     * number, and if there is a photo also adds the call action icon.
     *
     * @param lookupUri The person the phone number belongs to
     * @param type The type of the phone number
     * @param actionResId The ID for the action resource
     * @return The bitmap for the icon
     */
    private Bitmap generatePhoneNumberIcon(Uri lookupUri, int type, int actionResId) {
        final Resources r = getResources();
        boolean drawPhoneOverlay = true;
        final float scaleDensity = getResources().getDisplayMetrics().scaledDensity;

        Bitmap photo = loadContactPhoto(lookupUri, null);
        if (photo == null) {
            // If there isn't a photo use the generic phone action icon instead
            Bitmap phoneIcon = getPhoneActionIcon(r, actionResId);
            if (phoneIcon != null) {
                photo = phoneIcon;
                drawPhoneOverlay = false;
            } else {
                return null;
            }
        }

        // Setup the drawing classes
        Bitmap icon = createShortcutBitmap();
        Canvas canvas = new Canvas(icon);

        // Copy in the photo
        Paint photoPaint = new Paint();
        photoPaint.setDither(true);
        photoPaint.setFilterBitmap(true);
        Rect src = new Rect(0,0, photo.getWidth(),photo.getHeight());
        Rect dst = new Rect(0,0, mIconSize, mIconSize);
        canvas.drawBitmap(photo, src, dst, photoPaint);

        // Create an overlay for the phone number type
        String overlay = null;
        switch (type) {
            case Phone.TYPE_HOME:
                overlay = getString(R.string.type_short_home);
                break;

            case Phone.TYPE_MOBILE:
                overlay = getString(R.string.type_short_mobile);
                break;

            case Phone.TYPE_WORK:
                overlay = getString(R.string.type_short_work);
                break;

            case Phone.TYPE_PAGER:
                overlay = getString(R.string.type_short_pager);
                break;

            case Phone.TYPE_OTHER:
                overlay = getString(R.string.type_short_other);
                break;
        }
        if (overlay != null) {
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
            textPaint.setTextSize(20.0f * scaleDensity);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            textPaint.setColor(r.getColor(R.color.textColorIconOverlay));
            textPaint.setShadowLayer(3f, 1, 1, r.getColor(R.color.textColorIconOverlayShadow));
            canvas.drawText(overlay, 2 * scaleDensity, 16 * scaleDensity, textPaint);
        }

        // Draw the phone action icon as an overlay
        if (ENABLE_ACTION_ICON_OVERLAYS && drawPhoneOverlay) {
            Bitmap phoneIcon = getPhoneActionIcon(r, actionResId);
            if (phoneIcon != null) {
                src.set(0, 0, phoneIcon.getWidth(), phoneIcon.getHeight());
                int iconWidth = icon.getWidth();
                dst.set(iconWidth - ((int) (20 * scaleDensity)), -1,
                        iconWidth, ((int) (19 * scaleDensity)));
                canvas.drawBitmap(phoneIcon, src, dst, photoPaint);
            }
        }

        return icon;
    }

    private Bitmap scaleToAppIconSize(Bitmap photo) {
        // Setup the drawing classes
        Bitmap icon = createShortcutBitmap();
        Canvas canvas = new Canvas(icon);

        // Copy in the photo
        Paint photoPaint = new Paint();
        photoPaint.setDither(true);
        photoPaint.setFilterBitmap(true);
        Rect src = new Rect(0,0, photo.getWidth(),photo.getHeight());
        Rect dst = new Rect(0,0, mIconSize, mIconSize);
        canvas.drawBitmap(photo, src, dst, photoPaint);

        return icon;
    }

    private Bitmap createShortcutBitmap() {
        return Bitmap.createBitmap(mIconSize, mIconSize, Bitmap.Config.ARGB_8888);
    }

    /**
     * Returns the icon for the phone call action.
     *
     * @param r The resources to load the icon from
     * @param resId The resource ID to load
     * @return the icon for the phone call action
     */
    private Bitmap getPhoneActionIcon(Resources r, int resId) {
        Drawable phoneIcon = r.getDrawable(resId);
        if (phoneIcon instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) phoneIcon;
            return bd.getBitmap();
        } else {
            return null;
        }
    }

    private Uri getUriToQuery() {
        switch(mMode) {
            case MODE_JOIN_CONTACT:
                return getJoinSuggestionsUri(null);
            case MODE_FREQUENT:
            case MODE_STARRED:
                return Contacts.CONTENT_URI;

            case MODE_DEFAULT:
            case MODE_CUSTOM:
            case MODE_INSERT_OR_EDIT_CONTACT:
            case MODE_PICK_CONTACT:
            case MODE_PICK_OR_CREATE_CONTACT:{
                return CONTACTS_CONTENT_URI_WITH_LETTER_COUNTS;
            }
            case MODE_STREQUENT: {
                return Contacts.CONTENT_STREQUENT_URI;
            }
            case MODE_LEGACY_PICK_PERSON:
            case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                return People.CONTENT_URI;
            }
            case MODE_PICK_PHONE: {
                return buildSectionIndexerUri(Phone.CONTENT_URI);
            }
            case MODE_PICK_PHONE_EMAIL: {
                return buildSectionIndexerUri(PICK_PHONE_EMAIL_URI);
            }

            case MODE_LEGACY_PICK_PHONE: {
                return Phones.CONTENT_URI;
            }
            case MODE_PICK_POSTAL: {
                return buildSectionIndexerUri(StructuredPostal.CONTENT_URI);
            }
            case MODE_LEGACY_PICK_POSTAL: {
                return ContactMethods.CONTENT_URI;
            }
            case MODE_QUERY_PICK_TO_VIEW: {
                if (mQueryMode == QUERY_MODE_MAILTO) {
                    return Uri.withAppendedPath(Email.CONTENT_FILTER_URI,
                            Uri.encode(mInitialFilter));
                } else if (mQueryMode == QUERY_MODE_TEL) {
                    return Uri.withAppendedPath(Phone.CONTENT_FILTER_URI,
                            Uri.encode(mInitialFilter));
                }
                return CONTACTS_CONTENT_URI_WITH_LETTER_COUNTS;
            }
            case MODE_QUERY:
            case MODE_QUERY_PICK:
            case MODE_QUERY_PICK_TO_EDIT: {
                return getContactFilterUri(mInitialFilter);
            }
            case MODE_QUERY_PICK_PHONE: {
                return Uri.withAppendedPath(Phone.CONTENT_FILTER_URI,
                        Uri.encode(mInitialFilter));
            }

            case MODE_QUERY_PICK_PHONE_EMAIL: {
                return Uri.withAppendedPath(PICK_PHONE_EMAIL_FILTER_URI, 
                        Uri.encode(mInitialFilter));
            }
            case MODE_GROUP: {
                return mGroupUri;
            }
            default: {
                throw new IllegalStateException("Can't generate URI: Unsupported Mode.");
            }
        }
    }

    /**
     * Build the {@link Contacts#CONTENT_LOOKUP_URI} for the given
     * {@link ListView} position, using {@link #mAdapter}.
     */
    private Uri getContactUri(int position) {
        if (position == ListView.INVALID_POSITION) {
            throw new IllegalArgumentException("Position not in list bounds");
        }

        final Cursor cursor = (Cursor)mAdapter.getItem(position);
        if (cursor == null) {
            return null;
        }

        switch(mMode) {
            case MODE_LEGACY_PICK_PERSON:
            case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                final long personId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
                return ContentUris.withAppendedId(People.CONTENT_URI, personId);
            }

            default: {
                // Build and return soft, lookup reference
                final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
                final String lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY_COLUMN_INDEX);
                //for CR4567 6101
                if (lookupKey == null) {
                return Contacts.getLookupUri(contactId, lookupKey);
                } else {
					Uri lookupUri = Contacts.getLookupUri(contactId, lookupKey);
					return Contacts.lookupContact(getContentResolver(), lookupUri);
                }
            }
        }
    }

    /**
     * Build the {@link Uri} for the given {@link ListView} position, which can
     * be used as result when in {@link #MODE_MASK_PICKER} mode.
     */
    private Uri getSelectedUri(int position) {
        if (position == ListView.INVALID_POSITION) {
            throw new IllegalArgumentException("Position not in list bounds");
        }

        final long id = mAdapter.getItemId(position);
        switch(mMode) {
            case MODE_LEGACY_PICK_PERSON:
            case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                return ContentUris.withAppendedId(People.CONTENT_URI, id);
            }
            case MODE_PICK_PHONE:
            case MODE_QUERY_PICK_PHONE:
            case MODE_PICK_PHONE_EMAIL:
            case MODE_QUERY_PICK_PHONE_EMAIL: {
                return ContentUris.withAppendedId(Data.CONTENT_URI, id);
            }
            case MODE_LEGACY_PICK_PHONE: {
                return ContentUris.withAppendedId(Phones.CONTENT_URI, id);
            }
            case MODE_PICK_POSTAL: {
                return ContentUris.withAppendedId(Data.CONTENT_URI, id);
            }
            case MODE_LEGACY_PICK_POSTAL: {
                return ContentUris.withAppendedId(ContactMethods.CONTENT_URI, id);
            }
            default: {
                return getContactUri(position);
            }
        }
    }

    String[] getProjectionForQuery() {
        switch(mMode) {
            case MODE_JOIN_CONTACT:
            case MODE_STREQUENT:
            case MODE_FREQUENT:
            case MODE_STARRED:
            case MODE_DEFAULT:
            case MODE_CUSTOM:
            case MODE_INSERT_OR_EDIT_CONTACT:
            case MODE_GROUP:
            case MODE_PICK_CONTACT:
            case MODE_PICK_OR_CREATE_CONTACT: {
                return mSearchMode
                        ? CONTACTS_SUMMARY_FILTER_PROJECTION
                        : CONTACTS_SUMMARY_PROJECTION;
            }
            case MODE_QUERY:
            case MODE_QUERY_PICK:
            case MODE_QUERY_PICK_TO_EDIT: {
                return CONTACTS_SUMMARY_FILTER_PROJECTION;
            }
            case MODE_LEGACY_PICK_PERSON:
            case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                return LEGACY_PEOPLE_PROJECTION ;
            }
            case MODE_QUERY_PICK_PHONE:
            case MODE_PICK_PHONE: 
            case MODE_PICK_PHONE_EMAIL:
            case MODE_QUERY_PICK_PHONE_EMAIL: {
                return PHONES_PROJECTION;
            }
            case MODE_LEGACY_PICK_PHONE: {
                return LEGACY_PHONES_PROJECTION;
            }
            case MODE_PICK_POSTAL: {
                return POSTALS_PROJECTION;
            }
            case MODE_LEGACY_PICK_POSTAL: {
                return LEGACY_POSTALS_PROJECTION;
            }
            case MODE_QUERY_PICK_TO_VIEW: {
                if (mQueryMode == QUERY_MODE_MAILTO) {
                    return CONTACTS_SUMMARY_PROJECTION_FROM_EMAIL;
                } else if (mQueryMode == QUERY_MODE_TEL) {
                    return PHONES_PROJECTION;
                }
                break;
            }
        }

        // Default to normal aggregate projection
        return CONTACTS_SUMMARY_PROJECTION;
    }

    private Bitmap loadContactPhoto(Uri selectedUri, BitmapFactory.Options options) {
        Uri contactUri = null;
        if (Contacts.CONTENT_ITEM_TYPE.equals(getContentResolver().getType(selectedUri))) {
            // TODO we should have a "photo" directory under the lookup URI itself
            contactUri = Contacts.lookupContact(getContentResolver(), selectedUri);
        } else {

            Cursor cursor = getContentResolver().query(selectedUri,
                    new String[] { Data.CONTACT_ID }, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    final long contactId = cursor.getLong(0);
                    contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        Cursor cursor = null;
        Bitmap bm = null;

        try {
            Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
            cursor = getContentResolver().query(photoUri, new String[] {Photo.PHOTO},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                bm = ContactsUtils.loadContactPhoto(cursor, 0, options);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (bm == null) {
            final int[] fallbacks = {
                R.drawable.ic_contact_picture,
                R.drawable.ic_contact_picture_2,
                R.drawable.ic_contact_picture_3
            };
            bm = BitmapFactory.decodeResource(getResources(),
                    fallbacks[new Random().nextInt(fallbacks.length)]);
        }

        return bm;
    }

    /**
     * Return the selection arguments for a default query based on the
     * {@link #mDisplayOnlyPhones} flag.
     */
    private String getContactSelection() {
        if (mDisplayOnlyPhones) {
            return CLAUSE_ONLY_VISIBLE + " AND " + CLAUSE_ONLY_PHONES;
        } else {
            return CLAUSE_ONLY_VISIBLE;
        }
    }

    private String appendContactSelection(String selection, int dataOrContact) {//80794
		Log.i(TAG, "In appendContactSelection selection is " + selection);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		mdisplayPhoneContacts = prefs.getBoolean(Prefs.DISPLAY_PHONE_CONTACTS,
				Prefs.DISPLAY_PHONE_CONTACTS_DEFAULT);
		mdisplaySimsContacts = prefs.getBoolean(Prefs.DISPLAY_SIMs_CONTACTS,
				Prefs.DISPLAY_SIMs_CONTACTS_DEFAULT);

       String group = prefs.getString(SELECT_GROUP_KEY, ALL_CONTACTS_TITLE);
        if (mGroupBtn != null) {
            mGroupBtn.setText(ContactsUtils.getGroupsName(this, group));
        }

		StringBuilder builder = new StringBuilder();
		if (selection != null && selection.length() > 0) {
			builder.append(selection);
		}
        if (mMode != MODE_INSERT_OR_EDIT_CONTACT) {

		if (!ALL_CONTACTS_TITLE.equals(group) && !SIM_CONTACTS_TITLE.equals(group) && dataOrContact != QUERY_UNKNOWN_TABLE) {
            if (builder != null && builder.length() > 0) {
                builder.append(" AND ");
            }
            builder.append("(");
            Log.i(TAG,"Before replaceAll group is " + group);
            if (group != null) group = group.replaceAll("\\'", "\\''");//escape character in java is "\", but in sql is "'"
            Log.i(TAG,"After replaceAll group is " + group);
            Cursor cursor = getContentResolver().query(Groups.CONTENT_URI, null, 
            		Groups.TITLE + "='" + group + "' AND " + Groups.DELETED + "=0", null, null);
            Log.i(TAG, "cursor.getCount() = " + cursor.getCount()); 
            if (cursor.getCount() == 0) {
            	SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(ContactsListActivity.this).edit();
                 editor.putString("group", ALL_CONTACTS_TITLE);        
            	editor.apply();
            	group = PreferenceManager
                .getDefaultSharedPreferences(ContactsListActivity.this).getString(SELECT_GROUP_KEY, ALL_CONTACTS_TITLE);
                Log.i(TAG,"5332 group is " + group);
                builder.append(" 1 ");
            } else if (dataOrContact == QUERY_DATA_TABLE) {
                builder.append(Data.CONTACT_ID + CONTACTS_IN_GROUP_SELECT.replace("?", "'" + group + "'"));
            } else {
                builder.append(Contacts._ID + CONTACTS_IN_GROUP_SELECT.replace("?", "'" + group + "'"));
            }
            builder.append(")");
        }else if(SIM_CONTACTS_TITLE.equals(group)){
        	if (builder != null && builder.length() > 0) {
                builder.append(" AND ");
            }
            builder.append("(");
            String mSimFilters = "-2";
            if(ContactsUtils.mSelectedSimList == null || ContactsUtils.mSelectedSimList.size() == 0){
            	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ContactsListActivity.this);
                String simIds = pref.getString("simIds", "");
                String[] simIdList = null;
                if(simIds != null && simIds.length() > 0){
                	simIdList = simIds.split(",");
                }
                if(simIdList != null){
                	for(String s:simIdList)mSimFilters = mSimFilters + "," + s;
                }
                
            }else
            if(ContactsUtils.mSelectedSimList != null && ContactsUtils.mSelectedSimList.size() > 0){
            	for(SimInfo info:ContactsUtils.mSelectedSimList){
            		mSimFilters = mSimFilters + "," + info.simId;
            		
            	}
            }
            builder.append(RawContacts.INDICATE_PHONE_SIM + " IN (" + mSimFilters + ")");
            builder.append(")");
        }
        }
       
		//Add by mtk80908. Do not allow to add a phone number to SIM contacts 
		//since the number of SIM contacts has limit.    
		if (mMode == MODE_INSERT_OR_EDIT_CONTACT) {
            if (builder != null && builder.length() > 0) {
                builder.append(" AND ");
            }
            builder.append(CLAUSE_ONLY_PHONE_CONTACT);
        } else {            
			if (mdisplaySimsContacts && mdisplayPhoneContacts) {
				if (builder != null && builder.length() > 0) {
				    builder.append(" AND ");
				}
                builder.append("(");
                builder.append(CLAUSE_ONLY_SIMs_CONTACT 
                        + " OR " + CLAUSE_ONLY_PHONE_CONTACT);
                builder.append(")");
		    } else if (mdisplayPhoneContacts) {
                if (builder != null && builder.length() > 0) {
                    builder.append(" AND ");
                }
                builder.append(CLAUSE_ONLY_PHONE_CONTACT);
		    } else if (mdisplaySimsContacts) {
                if (builder != null && builder.length() > 0) {
                    builder.append(" AND ");
                }
                builder.append(CLAUSE_ONLY_SIMs_CONTACT);
		    } else {
                if (builder != null && builder.length() > 0) {
                    builder.append(" AND ");
                }
                builder.append("(");
                builder.append(RawContacts.INDICATE_PHONE_SIM + "<" + RawContacts.INDICATE_PHONE
                        + " AND " + RawContacts.INDICATE_PHONE_SIM + ">" + "1000");
                builder.append(")");
		    }
        }
		return builder.toString();
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
        Log.i(TAG, "IN onServiceStateChanged, ServiceState= "+ serviceState.getState());
      if((serviceState.getState() == ServiceState.STATE_POWER_OFF) 
        || (serviceState.getState() == ServiceState.STATE_IN_SERVICE)) {
      ContactsListActivity.this.closeContextMenu();
        }
      if(serviceState.getState() != ServiceState.STATE_POWER_OFF) return;  ////
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
        if (!sim1RadioOn || !sim1Ready) {
          Log
              .i(
                  TAG,
                  "TelephonyManager.getDefault().getSimStateGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1) is "
                      + TelephonyManager
                          .getDefault()
                          .getSimStateGemini(
                              com.android.internal.telephony.Phone.GEMINI_SIM_1));
          
          ContactsListActivity.s_deletingContacts = true;  ////
          waitImportingSimThread();  ////
          new Thread(new Runnable() {
            public void run() {
                long simId1 = 0;
            	 SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(ContactsListActivity.this, com.android.internal.telephony.Phone.GEMINI_SIM_1);
         	    if (simInfo1 != null) {
         	    	simId1 = simInfo1.mSimId;
         	    	Log.i(TAG,"simId1 is " + simId1);
         	        }
                             getContentResolver().
                                 delete(RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("sim", "true").build(), 
                                         RawContacts.INDICATE_PHONE_SIM + " = " + simId1, null);
              Log.i(TAG, "After delete sim1");
              ContactsListActivity.s_deletingContacts = false;  ////
            }

          }).start();
          ContactsUtils.setSim1Ready(0);
          ContactsUtils.setSim1Start(0);
          Log.i(TAG, "ContactsUtils.getSim1Ready(0) circle1 is "
              + ContactsUtils.getSim1Ready());
          Log.i(TAG, "ContactsUtils.getSim1Start sSim1Started is "
              + ContactsUtils.getSim1Start());
          sim1RadioOn = true;
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
          if (!iTel.hasIccCard()) {
            hasSimCard = false;
          }
        } catch (RemoteException ex) {
          ex.printStackTrace();
        }
        if ((!simRadioOn || !simReady)
        /* && hasSimCard */) {
          Log.i(TAG, "simRadioOn is " + simRadioOn);
          Log.i(TAG,
              "TelephonyManager.getDefault().getSimState() is "
                  + TelephonyManager.getDefault().getSimState());
          Log.i(TAG, "hasSimCard is " + hasSimCard);
          ContactsListActivity.s_deletingContacts = true;  ////
          waitImportingSimThread();  ////
          new Thread(new Runnable() {
            public void run() {
            	long simId1 = 0;
            	 SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(ContactsListActivity.this, com.android.internal.telephony.Phone.GEMINI_SIM_1);
          	    if (simInfo1 != null) {
          	    	simId1 = simInfo1.mSimId;
          	    	Log.i(TAG,"single sim simId1 is " + simId1);
          	        }
                        getContentResolver().
                            delete(RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("sim", "true").build(), 
                                    RawContacts.INDICATE_PHONE_SIM + "=" + simId1, null);
              Log.i(TAG, "After delete sim");
              ContactsListActivity.s_deletingContacts = false;  ////
            }

          }).start();
          ContactsUtils.setSimReady(0);
          ContactsUtils.setSimStart(0);
          Log.i(TAG, "ContactsUtils.getSimReady(0) circle is "
              + ContactsUtils.getSimReady());
          Log.i(TAG, "ContactsUtils.getSimStart sSimStarted is "
              + ContactsUtils.getSimStart());
          simRadioOn = true;
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
    Log.i(TAG, "IN onServiceStateChanged2, ServiceState= "+ serviceState.getState());
    if((serviceState.getState() == ServiceState.STATE_POWER_OFF) 
      || (serviceState.getState() == ServiceState.STATE_IN_SERVICE)) {
    ContactsListActivity.this.closeContextMenu();
      }

    if(serviceState.getState() != ServiceState.STATE_POWER_OFF) return;  ////
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

        if (!sim2RadioOn || !sim2Ready) {
          Log
              .i(
                  TAG,
                  "TelephonyManager.getDefault().getSimStateGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2) is "
                      + TelephonyManager
                          .getDefault()
                          .getSimStateGemini(
                              com.android.internal.telephony.Phone.GEMINI_SIM_2));
          ContactsListActivity.s_deletingContacts = true;  ////
          waitImportingSimThread();  ////
          new Thread(new Runnable() {
            public void run() {
                long simId2 = 0;
            	   SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(ContactsListActivity.this, com.android.internal.telephony.Phone.GEMINI_SIM_2);
           	    if (simInfo2 != null) {
           	    	simId2 = simInfo2.mSimId;
           	    	Log.i(TAG,"simId2 is " + simId2);
           	        }
                             getContentResolver().
                                 delete(RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("sim", "true").build(), 
                                         RawContacts.INDICATE_PHONE_SIM + "=" + simId2, null);
              Log.i(TAG, "After delete sim2");
              ContactsListActivity.s_deletingContacts = false;  ////
            }
          }).start();
          ContactsUtils.setSim2Ready(0);
          ContactsUtils.setSim2Start(0);
          Log.i(TAG, "ContactsUtils.getSim2Ready(0) circle2 is "
              + ContactsUtils.getSim2Ready());
          Log.i(TAG, "ContactsUtils.getSim2Start sSim2Started is "
              + ContactsUtils.getSim2Start());
          sim2RadioOn = true;
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

    private Uri getContactFilterUri(String filter) {
        Uri baseUri;
        if (!TextUtils.isEmpty(filter)) {
            baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI, Uri.encode(filter));
        } else {
            baseUri = Contacts.CONTENT_URI;
        }

        if (mAdapter.getDisplaySectionHeadersEnabled()) {
            return buildSectionIndexerUri(baseUri);
        } else {
            return baseUri;
        }
    }

    private Uri getPeopleFilterUri(String filter) {
        if (!TextUtils.isEmpty(filter)) {
            return Uri.withAppendedPath(People.CONTENT_FILTER_URI, Uri.encode(filter));
        } else {
            return People.CONTENT_URI;
        }
    }

    private static Uri buildSectionIndexerUri(Uri uri) {
        return uri.buildUpon()
                .appendQueryParameter(ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, "true").build();
    }

    private Uri getJoinSuggestionsUri(String filter) {
        Builder builder = Contacts.CONTENT_URI.buildUpon();
        builder.appendEncodedPath(String.valueOf(mQueryAggregateId));
        builder.appendEncodedPath(AggregationSuggestions.CONTENT_DIRECTORY);
        if (!TextUtils.isEmpty(filter)) {
            builder.appendEncodedPath(Uri.encode(filter));
        }
        builder.appendQueryParameter("limit", String.valueOf(MAX_SUGGESTIONS));
	if(true == FeatureOption.MTK_SNS_SUPPORT) {
		long owner_id = PhoneOwner.getInstance() == null ? (long)-1 : 
			PhoneOwner.getInstance().getOwnerID();
		builder.appendQueryParameter("owner_id", String.valueOf(owner_id));
	}
	else {
		builder.appendQueryParameter("owner_id", String.valueOf(-1));
	}
        return builder.build();
    }

    private String getSortOrder(String[] projectionType) {
        if (mSortOrder == ContactsContract.Preferences.SORT_ORDER_PRIMARY) {
            return Contacts.SORT_KEY_PRIMARY;
        } else {
            return Contacts.SORT_KEY_ALTERNATIVE;
        }
    }

    void startQuery() {
        // Set the proper empty string
    	if (!mIsFirstLaunch) {
    		setEmptyText();
    	}

        if (mSearchResultsMode) {
            TextView foundContactsText = (TextView)findViewById(R.id.search_results_found);
            foundContactsText.setText(R.string.search_results_searching);
        }

        mAdapter.setLoading(true);

        // Cancel any pending queries
        mQueryHandler.cancelOperation(QUERY_TOKEN);
        mQueryHandler.setLoadingJoinSuggestions(false);

        mSortOrder = mContactsPrefs.getSortOrder();
        mDisplayOrder = mContactsPrefs.getDisplayOrder();

        // When sort order and display order contradict each other, we want to
        // highlight the part of the name used for sorting.
        mHighlightWhenScrolling = false;
        if (mSortOrder == ContactsContract.Preferences.SORT_ORDER_PRIMARY &&
                mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_ALTERNATIVE) {
            mHighlightWhenScrolling = true;
        } else if (mSortOrder == ContactsContract.Preferences.SORT_ORDER_ALTERNATIVE &&
                mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
            mHighlightWhenScrolling = true;
        }

        String[] projection = getProjectionForQuery();
        if ((mMode != MODE_INSERT_OR_EDIT_CONTACT) && mSearchMode && TextUtils.isEmpty(getTextFilter())) {
            Log.d(TAG, "startQuery() ####################, before changeCursor() ");
            mAdapter.changeCursor(new MatrixCursor(projection));
            return;
        }

        String callingPackage = getCallingPackage();
        Uri uri = getUriToQuery();
        Log.d(TAG, " startQuery(), query URI= "+ uri);
        if (!TextUtils.isEmpty(callingPackage)) {
            uri = uri.buildUpon()
                    .appendQueryParameter(ContactsContract.REQUESTING_PACKAGE_PARAM_KEY,
                            callingPackage)
                    .build();
        }

        // Kick off the new query
        String action = getIntent().getAction();
        String shortcutSelection = "";
        String shortcutSelection2 = null;
        Log.i(TAG, "mShortcutAction  = " + mShortcutAction );
        if (mFilterSIMContact) {
            shortcutSelection = " AND " + RawContacts.INDICATE_PHONE_SIM + "=" + RawContacts.INDICATE_PHONE;
            shortcutSelection2 = RawContacts.INDICATE_PHONE_SIM + "=" + RawContacts.INDICATE_PHONE;
        }
        //MTK for the Sms widget. maybe we need a new argument
        String filterSelection = "";
        if (mBroadcastResult) {
            filterSelection = " AND " + Contacts.FILTER + "=" + Contacts.FILTER_NONE;
        }
        switch (mMode) {
            case MODE_GROUP:
            case MODE_DEFAULT:
            case MODE_CUSTOM:
            case MODE_PICK_CONTACT:
            case MODE_PICK_OR_CREATE_CONTACT:
            case MODE_INSERT_OR_EDIT_CONTACT:
            	if(true == FeatureOption.MTK_SNS_SUPPORT)
            	{
			long owner_id = PhoneOwner.getInstance() == null ? (long)-1 : PhoneOwner.getInstance().getOwnerID();
            		/*mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, 
				getContactSelection() + " AND " + Contacts._ID + " != " + owner_id, 
				null, getSortOrder(projection));*/
                    mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, //80794
                        		"( " + appendContactSelection(getContactSelection(), QUERY_CONTACT_TABLE) + filterSelection + " ) AND " + Contacts._ID + " != " + owner_id 
                        		+ shortcutSelection,
                                null, getSortOrder(projection));
            	}
            	else
            	{
            		//mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, getContactSelection(),null, getSortOrder(projection));
                    mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection,//80794
					appendContactSelection(getContactSelection(), QUERY_CONTACT_TABLE) + shortcutSelection + filterSelection, null,
					getSortOrder(projection));
            	}
                
                break;

            case MODE_LEGACY_PICK_PERSON:
            case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, null, null,
                        People.DISPLAY_NAME);
                break;
            }
            case MODE_PICK_POSTAL:
            case MODE_QUERY:
            case MODE_QUERY_PICK:
            case MODE_QUERY_PICK_PHONE:
            case MODE_QUERY_PICK_TO_VIEW:
            case MODE_QUERY_PICK_TO_EDIT: {
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, 
                        appendContactSelection(shortcutSelection2, QUERY_UNKNOWN_TABLE) + filterSelection, null,
                        getSortOrder(projection));
                break;
            }

            case MODE_STARRED:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                        projection, Contacts.STARRED + "=1", null,
                        getSortOrder(projection));
                break;

            case MODE_FREQUENT:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                        projection,
                        Contacts.TIMES_CONTACTED + " > 0", null,
                        Contacts.TIMES_CONTACTED + " DESC, "
                        + getSortOrder(projection));
                break;

            case MODE_STREQUENT:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, null, null, null);
                break;

            case MODE_PICK_PHONE:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                        projection, appendContactSelection(CLAUSE_ONLY_VISIBLE, QUERY_DATA_TABLE), null, getSortOrder(projection));
                break;
                
            case MODE_QUERY_PICK_PHONE_EMAIL:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                        projection, appendContactSelection(CLAUSE_ONLY_VISIBLE, QUERY_UNKNOWN_TABLE), null, getSortOrder(projection));
                break;
                
            case MODE_PICK_PHONE_EMAIL:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                        projection, appendContactSelection(CLAUSE_ONLY_VISIBLE, QUERY_DATA_TABLE), null, getSortOrder(projection));
                break;

            case MODE_LEGACY_PICK_PHONE:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                        projection, null, null, Phones.DISPLAY_NAME);
                break;

            case MODE_LEGACY_PICK_POSTAL:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                        projection,
                        ContactMethods.KIND + "=" + android.provider.Contacts.KIND_POSTAL, null,
                        ContactMethods.DISPLAY_NAME);
                break;

            case MODE_JOIN_CONTACT:
                String where = RawContacts.INDICATE_PHONE_SIM + "=" + RawContacts.INDICATE_PHONE;
                mQueryHandler.setLoadingJoinSuggestions(true);
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection,
                        where, null, null);
                break;
        }
    }

    /**
     * Called from a background thread to do the filter and return the resulting cursor.
     *
     * @param filter the text that was entered to filter on
     * @return a cursor with the results of the filter
     */
    Cursor doFilter(String filter) {
    
        Log.i(TAG, "######################doFilter() begin");
        String[] projection = getProjectionForQuery();
        if(TextUtils.isEmpty(filter)&&mMode == MODE_INSERT_OR_EDIT_CONTACT){
              filter = "*";
        }
        if (mSearchMode && TextUtils.isEmpty(getTextFilter())) {
            return new MatrixCursor(projection);
        }

        String action = getIntent().getAction();
        String shortcutSelection = "";
        String shortcutSelection2 = null;
        if (mFilterSIMContact) {
            shortcutSelection = " AND " + RawContacts.INDICATE_PHONE_SIM + "=" + RawContacts.INDICATE_PHONE;
            shortcutSelection2 = RawContacts.INDICATE_PHONE_SIM + "=" + RawContacts.INDICATE_PHONE;
        }
        //MTK for the Sms widget. maybe we need a new argument
        String filterSelection = "";
        if (mBroadcastResult) {
            filterSelection = " AND " + Contacts.FILTER + "=" + Contacts.FILTER_NONE;
        }
        final ContentResolver resolver = getContentResolver();
        switch (mMode) {
            case MODE_DEFAULT:
            case MODE_CUSTOM:
            case MODE_PICK_CONTACT:
            case MODE_PICK_OR_CREATE_CONTACT:
            case MODE_INSERT_OR_EDIT_CONTACT: {
                return resolver.query(getContactFilterUri(filter), projection,
                        appendContactSelection(getContactSelection() + shortcutSelection + filterSelection, QUERY_CONTACT_TABLE), null, getSortOrder(projection));
            }

            case MODE_LEGACY_PICK_PERSON:
            case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                return resolver.query(getPeopleFilterUri(filter), projection, null, null,
                        People.DISPLAY_NAME);
            }

            case MODE_STARRED: {
                return resolver.query(getContactFilterUri(filter), projection,
                        Contacts.STARRED + "=1", null,
                        getSortOrder(projection));
            }

            case MODE_FREQUENT: {
                return resolver.query(getContactFilterUri(filter), projection,
                        Contacts.TIMES_CONTACTED + " > 0", null,
                        Contacts.TIMES_CONTACTED + " DESC, "
                        + getSortOrder(projection));
            }

            case MODE_STREQUENT: {
                Uri uri;
                if (!TextUtils.isEmpty(filter)) {
                    uri = Uri.withAppendedPath(Contacts.CONTENT_STREQUENT_FILTER_URI,
                            Uri.encode(filter));
                } else {
                    uri = Contacts.CONTENT_STREQUENT_URI;
                }
                return resolver.query(uri, projection, null, null, null);
            }

            case MODE_PICK_PHONE: {
                Uri uri = getUriToQuery();
                if (!TextUtils.isEmpty(filter)) {
                    uri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(filter));
                }
                return resolver.query(uri, projection, appendContactSelection(CLAUSE_ONLY_VISIBLE, QUERY_DATA_TABLE), null,
                        getSortOrder(projection));
            }
            
            case MODE_PICK_PHONE_EMAIL: {
                Uri uri = getUriToQuery();
                if (!TextUtils.isEmpty(filter)) {
                    uri = Uri.withAppendedPath(PICK_PHONE_EMAIL_FILTER_URI, Uri.encode(filter));
                }
                return resolver.query(uri, projection, appendContactSelection(CLAUSE_ONLY_VISIBLE, QUERY_DATA_TABLE), null,
                        getSortOrder(projection));
            }

            case MODE_LEGACY_PICK_PHONE: {
                //TODO: Support filtering here (bug 2092503)
                break;
            }

            case MODE_JOIN_CONTACT: {

                // We are on a background thread. Run queries one after the other synchronously
                Cursor cursor = resolver.query(getJoinSuggestionsUri(filter), projection, null,
                        null, null);
                mAdapter.setSuggestionsCursor(cursor);
                mJoinModeShowAllContacts = false;
                return resolver.query(getContactFilterUri(filter), projection,//80794
					Contacts._ID + " != " + mQueryAggregateId + " AND "
							+ CLAUSE_ONLY_VISIBLE + " AND "
							+ CLAUSE_ONLY_PHONE_CONTACT, null,
					getSortOrder(projection));
            }
        }
        throw new UnsupportedOperationException("filtering not allowed in mode " + mMode);
    }

    private Cursor getShowAllContactsLabelCursor(String[] projection) {
        MatrixCursor matrixCursor = new MatrixCursor(projection);
        Object[] row = new Object[projection.length];
        // The only columns we care about is the id
        row[SUMMARY_ID_COLUMN_INDEX] = JOIN_MODE_SHOW_ALL_CONTACTS_ID;
        matrixCursor.addRow(row);
        return matrixCursor;
    }

    /**
     * Calls the currently selected list item.
     * @return true if the call was initiated, false otherwise
     */
    boolean callSelection() {
        ListView list = getListView();
        if (list.hasFocus()) {
            Cursor cursor = (Cursor) list.getSelectedItem();
			return callOrSmsContact(cursor, false);
        }
        return false;
    }

    boolean callContact(Cursor cursor) {
		return callOrSmsContact(cursor, false /* call */);
    }

    boolean smsContact(Cursor cursor) {
		return callOrSmsContact(cursor, true /* sms */);
    }

	/**
	 * Calls the contact which the cursor is point to.
	 * 
	 * @return true if the call was initiated, false otherwise
	 */
	boolean callOrSmsContact(Cursor cursor, boolean sendSms) {
        if (cursor == null) {
            return false;

        }

        switch (mMode) {
            case MODE_PICK_PHONE:
            case MODE_LEGACY_PICK_PHONE:
            case MODE_QUERY_PICK_PHONE: {
                String phone = cursor.getString(PHONE_NUMBER_COLUMN_INDEX);
                if (sendSms) {
                    ContactsUtils.initiateSms(this, phone);
                } else {
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        if (TextUtils.isEmpty(phone)){
                            return false;
                        }else{
                           // ContactsUtils.initiateCallWithSim(this, phone);
                            /*Uri phoneUri = Uri.fromParts("tel", phone, null);
                            Intent CallKeyIntent = new Intent(Intent.ACTION_DIAL, phoneUri);
                            startActivity(CallKeyIntent);  */
//                               ContactsUtils.enterDialer(this, phone);
/*                        		 Uri uri = Uri.withAppendedPath(
                        				 android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(phone));                              
                                    mSimAssociationQueryHandler.startQuery(0, phone, uri,
                        					new String[] { "sim_id" }, null, null, null);  	
                                            */
                               
                          makeCall(phone, ContactsUtils.DIAL_TYPE_VOICE);
                       }    
                    } else {
//                        if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
////                            ContactsUtils.enterDialer(this, phone);
//                        	intent.putExtra("is_vt_call", true);
//                            startActivity(intent);
//				} else {
                        //ContactsUtils.initiateCall(this, phone);
                        makeCall(phone, ContactsUtils.DIAL_TYPE_VOICE);
//                    }
                }
                }
                return true;
            }

            case MODE_PICK_POSTAL:
            case MODE_LEGACY_PICK_POSTAL: {
                return false;
            }

            default: {

                boolean hasPhone = cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX) != 0;
                if (!hasPhone) {
                    // There is no phone number.
                    signalError();
                    return false;
                }

                String phone = null;
                Cursor phonesCursor = null;
                phonesCursor = queryPhoneNumbers(cursor.getLong(SUMMARY_ID_COLUMN_INDEX));
                if (phonesCursor == null || phonesCursor.getCount() == 0) {
                    // No valid number
                    signalError();
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
                	mPhoneDisambigDialog = phoneDialog;
                } else {
                	//Changed by mtk80908 for CR ALPS00039280.
                	//Close phonesCursor since it is not used any more. 
                	//If not, may cause DatabaseObjectNotCloseException. 
                	if(phonesCursor !=null)
                		phonesCursor.close();
                    if (sendSms) {
                        ContactsUtils.initiateSms(this, phone);
                    } else {
						if (FeatureOption.MTK_GEMINI_SUPPORT) {                         
		                	if (TextUtils.isEmpty(phone)){
		                        return false;
		                    } else {
                            //ContactsUtils.initiateCallWithSim(this, phone);
                            StickyTabs.saveTab(this, getIntent());
/*                        		 Uri uri = Uri.withAppendedPath(
                        				 android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(phone));                              
                                    mSimAssociationQueryHandler.startQuery(0, phone, uri,
                        					new String[] { "sim_id" }, null, null, null);
                        					*/
                            makeCall(phone, ContactsUtils.DIAL_TYPE_VOICE);
		                    } 
                            
		                }else {
//							if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
//									StickyTabs.saveTab(this, getIntent());
////                                   ContactsUtils.enterDialer(this, phone);   
//									intent.putExtra("is_vt_call", true);
//	                                startActivity(intent);
//							} else {
                        			StickyTabs.saveTab(this, getIntent());
                        			//ContactsUtils.initiateCall(this, phone);
                               makeCall(phone, ContactsUtils.DIAL_TYPE_VOICE);                        			
//							}
                    	}
                }
            }
        }
        }
        return true;
    }

    private Cursor queryPhoneNumbers(long contactId) {
        Uri baseUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        Uri dataUri = Uri.withAppendedPath(baseUri, Contacts.Data.CONTENT_DIRECTORY);

        // mtk80909 modified for ALPS00023212
        Cursor c = getContentResolver().query(dataUri,
                new String[] {Phone._ID, Phone.NUMBER, Phone.IS_SUPER_PRIMARY,
                        RawContacts.ACCOUNT_TYPE, Phone.TYPE, Phone.LABEL, Data.DATA15},    
                Data.MIMETYPE + "=?", new String[] {Phone.CONTENT_ITEM_TYPE}, null);
        if (c != null && c.moveToFirst()) {
                return c;
            }
            c.close();
        return null;
    }

    // TODO: fix PluralRules to handle zero correctly and use Resources.getQuantityText directly
    protected String getQuantityText(int count, int zeroResourceId, int pluralResourceId) {
        if (count == 0) {
            return getString(zeroResourceId);
        } else {
            String format = getResources().getQuantityText(pluralResourceId, count).toString();
            return String.format(format, count);
        }
    }

    /**
     * Signal an error to the user.
     */
    void signalError() {
        //TODO play an error beep or something...
    }

    Cursor getItemForView(View view) {
        ListView listView = getListView();
        int index = listView.getPositionForView(view);
        if (index < 0) {
            return null;
        }
        return (Cursor) listView.getAdapter().getItem(index);
    }

    private class QueryHandler extends AsyncQueryHandler {
        protected final WeakReference<ContactsListActivity> mActivity;
        protected boolean mLoadingJoinSuggestions = false;

        public QueryHandler(Context context) {
            super(context.getContentResolver());
            mActivity = new WeakReference<ContactsListActivity>((ContactsListActivity) context);
        }

        public void setLoadingJoinSuggestions(boolean flag) {
            mLoadingJoinSuggestions = flag;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
           Log.d(TAG, "#################onQueryComplete() begin ");
        	if (mIsFirstLaunch) {
        		postSetEmptyText();
        		mIsFirstLaunch = false;
        	}
            mAdapterReady= true;
            final ContactsListActivity activity = mActivity.get();
            //if (activity != null) activity.closeOptionsMenu();
    		if (mQueryDialog != null && mQueryDialog.isShowing()) {
				mQueryDialog.dismiss();
				mQueryDialog = null;
			}
            if (activity != null) activity.closeContextMenu();
            if (activity != null && !activity.isFinishing()) {
				// mtk80909 start
				if (activity.mDeleteButton != null)
					activity.mDeleteButton.setEnabled(true);
				if (activity.mSelectAllBox != null)
					activity.mSelectAllBox.setEnabled(true);
				if (activity.mSelectAllButton != null)
					activity.mSelectAllButton.setEnabled(true);
				if (activity.getListView() != null)
					activity.getListView().setEnabled(true);
				// mtk80909 end
                // Whenever we get a suggestions cursor, we need to immediately kick off
                // another query for the complete list of contacts
                if (cursor != null && mLoadingJoinSuggestions) {
                    mLoadingJoinSuggestions = false;
                    if (cursor.getCount() > 0) {
                        activity.mAdapter.setSuggestionsCursor(cursor);
                    } else {
                        cursor.close();
                        activity.mAdapter.setSuggestionsCursor(null);
                    }

                    if (activity.mAdapter.mSuggestionsCursorCount == 0
                            || !activity.mJoinModeShowAllContacts) {
			if(true == FeatureOption.MTK_SNS_SUPPORT) {
				long owner_id = PhoneOwner.getInstance() == null ? (long)-1 : 
					PhoneOwner.getInstance().getOwnerID();
				startQuery(QUERY_TOKEN, null, activity.getContactFilterUri(
                                        	activity.getTextFilter()),
                                	CONTACTS_SUMMARY_PROJECTION,
                                	Contacts._ID + " != " + activity.mQueryAggregateId
                                        	+ " AND " + CLAUSE_ONLY_VISIBLE + " AND " 
						+ Contacts._ID + " != " + owner_id + " AND " 
						+ Contacts.INDICATE_PHONE_SIM + "=" + RawContacts.INDICATE_PHONE, null,
                                	activity.getSortOrder(CONTACTS_SUMMARY_PROJECTION));
			} else {
		                startQuery(QUERY_TOKEN, null, activity.getContactFilterUri(
		                                activity.getTextFilter()),
		                        CONTACTS_SUMMARY_PROJECTION,
		                        Contacts._ID + " != " + activity.mQueryAggregateId
		                                + " AND " + CLAUSE_ONLY_VISIBLE + " AND "
		                                + Contacts.INDICATE_PHONE_SIM + "=" + RawContacts.INDICATE_PHONE, null,
		                        activity.getSortOrder(CONTACTS_SUMMARY_PROJECTION));
			}
                        return;
                    }

                    cursor = activity.getShowAllContactsLabelCursor(CONTACTS_SUMMARY_PROJECTION);
                }

                // mtk80909 2010-12-9 start (Illegal state JE at changeCursor)
                if (mBeingDeleted) {
                    cursor.close();
                	if (mMarkAllProgDialog != null && mMarkAllProgDialog.isShowing()) {
				        mMarkAllProgDialog.dismiss();
				        mMarkAllProgDialog = null;
			        }
                    return;
                }
                // mtk80909 2010-12-9 end
                Log.d(TAG, "onQueryComplete(),  ##########################before changeCursor() ");
                activity.mAdapter.changeCursor(cursor);
                Log.d(TAG, "onQueryComplete(),  #############################after changeCursor() ");
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
                String groupTitle = pref.getString(SELECT_GROUP_KEY, ALL_CONTACTS_TITLE);
                if (groupTitle != null) {
                    if (mGroupBtn != null) {
                        mGroupBtn.setText(ContactsUtils.getGroupsName(activity, groupTitle)/* + "(" + cursor.getCount()+ ")"*/);
                    }
                }
                ensureBladeView();

                // Now that the cursor is populated again, it's possible to restore the list state
                if (activity.mListState != null) {
                    activity.mList.onRestoreInstanceState(activity.mListState);
                    activity.mListState = null;
                }

				// mtk80909 start
				if (activity.mDeleteState /*&& !activity.mAllOrNone*/) {
//					activity.mTopView.setVisibility(View.VISIBLE);
//					activity.groupView.setVisibility(View.GONE);
//					activity.mBottomView.setVisibility(View.VISIBLE);
//					activity.mSelected = 0;
//					activity.mToBeDeleted = new boolean[activity.mAdapter
//					        							.getRealCount()];
//					activity.mToBeDeletedSet = new HashSet<Integer>();
//					activity.mSelectAllButton.setText(getResources()
//							.getString(R.string.select_all));
//					activity.mSelectAllBox.setChecked(false);
//					activity.mDeleteButton.setText(activity.getResources()
//							.getString(R.string.delete_contacts_basic)
//							+ "(" + activity.mSelected + ")");
//					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) activity
//							.getListView().getLayoutParams();
//					params.height = 0;
//					params.addRule(RelativeLayout.ABOVE, activity.mBottomView
//							.getId());
//					params.addRule(RelativeLayout.BELOW, activity.mTopView
//							.getId());
//					activity.getListView().setLayoutParams(params);
					
					if (activity.mAdapter.getRealCount() <= 0) 
						quitDeleteState();
					//Need to traverse the cursor to store all contact id and indicator
					Cursor tmpCursor = cursor;
					//Move the first position of cursor to make sure to traverse the cursor
					//From the first to the last.
					int tmpSelected = tmpCursor.getCount();
					//TBD: Need to check the performance here.
					//If not OK, maybe comparing current cursor count with previous 
					//all contacts count to decide whether to update mAllContacts or not
					// will be better. 
					//But this method also has a problem that it can not update
					// mAllcontacts if the background operation does update contacts or
					// does delete contacts and insert contacts in one transation.
					//if (tmpSelected != mAllContacts.size()) {
						Log.i("KKK","[onQueryComplete] before copy to all hashmap");
						int tmpPos = tmpCursor.getPosition();
						if (tmpCursor.moveToFirst()) {
							do {
				            	//Add contacts to mAllContacts
				            	long currentID = tmpCursor.getLong(SUMMARY_ID_COLUMN_INDEX);
				                int indicate = tmpCursor.getInt(tmpCursor
										.getColumnIndexOrThrow(RawContacts.INDICATE_PHONE_SIM));
				                if (!mAllContacts.containsKey(currentID))
				                	mAllContacts.put(currentID, indicate);
							} while(tmpCursor.moveToNext());
						}
						tmpCursor.moveToPosition(tmpPos);
						Log.i("KKK","[onQueryComplete] end copy to all hashmap");						
					//}
					//Reset delete number on Delete Button if in delete all mode
					if (mSelectAllToDel) {
						activity.mSelected = mAllContacts.size();						
						activity.mDeleteButton.setText(mDeleteText + "(" + activity.mSelected + ")");
					}
					
				} else if (!activity.mDeleteState) {
					if (activity.mTopView != null){
						activity.mTopView.setVisibility(View.GONE);
						if (mMode == MODE_DEFAULT) {
						activity.groupView.setVisibility(View.VISIBLE);
						}
			            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) activity
						.getListView().getLayoutParams();
						params.height = 0;
						params.addRule(RelativeLayout.BELOW, groupView
								.getId());
						activity.getListView().setLayoutParams(params);
					}
					if (activity.mBottomView != null)
						activity.mBottomView.setVisibility(View.GONE);
					activity.mSelected = 0;
//					activity.mToBeDeleted = null;
//					activity.mToBeDeletedSet.clear();
					if (activity.mDeleteButton != null)
						activity.mDeleteButton.setText(activity.getResources()
								.getString(R.string.delete_contacts));
					LayoutParams params = activity.getListView()
							.getLayoutParams();
					params.height = LayoutParams.MATCH_PARENT;
					activity.getListView().setLayoutParams(params);
				} /*else if (activity.mAllOrNone) {
					activity.mAllOrNone = false;
				}*/
//				if (activity.mDeleteState
//						&& !activity.mBeingDeleted
//						/*&& (activity.mToBeDeleted == null || activity.mToBeDeleted.length != activity.mAdapter
//								.getRealCount())*/) {
////					activity.mToBeDeleted = new boolean[activity.mAdapter
////							.getRealCount()];
////					activity.mToBeDeletedSet = new HashSet<Integer>();
//					activity.mSelected = 0;
//					activity.mSelectAllBox.setChecked(false);
//					activity.mDeleteButton.setText(getResources().getString(
//							R.string.delete_contacts_basic)
//							+ "(" + mSelected + ")");
//					activity.mSelectAllButton.setText(getResources()
//							.getString(R.string.select_all));
//					if (activity.mAdapter.getRealCount() <= 0) 
//						quitDeleteState();
//				}
				// mtk 80909 end
            } else {
                if (cursor != null) {
                    cursor.close();
                }
            }
            // mtk80909
			if (mMarkAllProgDialog != null && mMarkAllProgDialog.isShowing()) {
				mMarkAllProgDialog.dismiss();
				mMarkAllProgDialog = null;
			}
        }

        protected void onDeleteComplete(int token, Object cookie, int result) {//80794
			Log.i(TAG,"result is "+result);
			if (result < 0) {
				Toast.makeText(ContactsListActivity.this,
						R.string.delete_error, Toast.LENGTH_SHORT).show();
			} else {
				 getContentResolver().delete(mSelectedContactUri, null, null);
			}

        }

        /**
         * This method works now only in default mode.
         */
        void ensureBladeView() {
        	if (mMode != MODE_DEFAULT || mBladeView == null) return;
            if (mAdapter == null || !mAdapter.isEmpty()) {
                mBladeView.setList(mList);
                mList.setFastScrollEnabled(false);
                mBladeView.setVisibility(View.VISIBLE);
            } else {
            	mBladeView.setVisibility(View.GONE);
            	mList.setFastScrollEnabled(true);
            } 
        }
    }

    final static class ContactListItemCache {
        public CharArrayBuffer nameBuffer = new CharArrayBuffer(128);
        public CharArrayBuffer dataBuffer = new CharArrayBuffer(128);
        public CharArrayBuffer highlightedTextBuffer = new CharArrayBuffer(128);
        public TextWithHighlighting textWithHighlighting;
        public CharArrayBuffer phoneticNameBuffer = new CharArrayBuffer(128);
    }

    final static class PinnedHeaderCache {
        public TextView titleView;
        public ColorStateList textColor;
        public Drawable background;
    }

    private final class ContactItemListAdapter extends CursorAdapter
            implements SectionIndexer, OnScrollListener, PinnedHeaderListView.PinnedHeaderAdapter {
        private SectionIndexer mIndexer;
        private boolean mLoading = true;
        private CharSequence mUnknownNameText;
        private boolean mDisplayPhotos = false;
        private boolean mDisplayCallButton = false;
        private boolean mDisplayAdditionalData = true;
        private int mFrequentSeparatorPos = ListView.INVALID_POSITION;
        private boolean mDisplaySectionHeaders = true;
        private Cursor mSuggestionsCursor;
        private int mSuggestionsCursorCount;

	private int mStartIndex = 0;
	private int mItemCount = 0;
	private int mScrollState;
	private ConcurrentHashMap<Long, ContactListItemView> mScrollViews = new ConcurrentHashMap<Long, ContactListItemView>();
	private ConcurrentHashMap<Long, Long> mScrollIds = new ConcurrentHashMap<Long, Long>();

        public ContactItemListAdapter(Context context) {
            super(context, null, false);

            mUnknownNameText = context.getText(android.R.string.unknownName);
            switch (mMode) {
                case MODE_LEGACY_PICK_POSTAL:
                case MODE_PICK_POSTAL:
                case MODE_LEGACY_PICK_PHONE:
                case MODE_PICK_PHONE:
                case MODE_PICK_PHONE_EMAIL:
                case MODE_STREQUENT:
                case MODE_FREQUENT:
                    mDisplaySectionHeaders = false;
                    break;
            }

            if (mSearchMode) {
                mDisplaySectionHeaders = false;
            }

            // Do not display the second line of text if in a specific SEARCH query mode, usually for
            // matching a specific E-mail or phone number. Any contact details
            // shown would be identical, and columns might not even be present
            // in the returned cursor.
            if (mMode != MODE_QUERY_PICK_PHONE && mQueryMode != QUERY_MODE_NONE) {
                mDisplayAdditionalData = false;
            }

            if ((mMode & MODE_MASK_NO_DATA) == MODE_MASK_NO_DATA) {
                mDisplayAdditionalData = false;
            }

            if ((mMode & MODE_MASK_SHOW_CALL_BUTTON) == MODE_MASK_SHOW_CALL_BUTTON) {
                mDisplayCallButton = true;
            }

            if ((mMode & MODE_MASK_SHOW_PHOTOS) == MODE_MASK_SHOW_PHOTOS) {
                mDisplayPhotos = true;
            }
        }

        public boolean getDisplaySectionHeadersEnabled() {
            return mDisplaySectionHeaders;
        }

        public void setSuggestionsCursor(Cursor cursor) {
            if (mSuggestionsCursor != null) {
                mSuggestionsCursor.close();
            }
            mSuggestionsCursor = cursor;
            mSuggestionsCursorCount = cursor == null ? 0 : cursor.getCount();
        }

        /**
         * Callback on the UI thread when the content observer on the backing cursor fires.
         * Instead of calling requery we need to do an async query so that the requery doesn't
         * block the UI thread for a long time.
         */
        @Override
        protected void onContentChanged() {
            CharSequence constraint = getTextFilter();
            if (!TextUtils.isEmpty(constraint)) {
                // Reset the filter state then start an async filter operation
                Filter filter = getFilter();
                filter.filter(constraint);
            } else {
                if (!mForeground) {
                    mForceQuery = true;
                    Log.i(TAG, "content changed, will requery on resume");
                    return;
                }
                Log.i(TAG, "onContentChanged(), content changed, fake before startQuery() ");
                
                // Start an async query
                if(mShowGroupsDialog == null){
                Log.i(TAG, "###############onContentChanged(), mShowGroupsDialog == null, content changed, before startQuery() ");

                	startQuery();
                }else{
                	queryAfterGroupDialogDissmiss = true;
                }
            }
        }

        public void setLoading(boolean loading) {
            mLoading = loading;
        }

        @Override
        public boolean isEmpty() {
            if (mProviderStatus != ProviderStatus.STATUS_NORMAL) {
                return true;
            }

            if (mSearchMode) {
                return TextUtils.isEmpty(getTextFilter());
            } else if ((mMode & MODE_MASK_CREATE_NEW) == MODE_MASK_CREATE_NEW) {
                // This mode mask adds a header and we always want it to show up, even
                // if the list is empty, so always claim the list is not empty.
                return false;
            } else {
                if (mCursor == null || mLoading) {
                    // We don't want the empty state to show when loading.
                    return false;
                } else {
                    return super.isEmpty();
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 && (mShowNumberOfContacts || (mMode & MODE_MASK_CREATE_NEW) != 0)) {
                return IGNORE_ITEM_VIEW_TYPE;
            }

            if (position == 1 && true == FeatureOption.MTK_SNS_SUPPORT &&
            		mMode == MODE_DEFAULT && !mSearchMode) {
            	return IGNORE_ITEM_VIEW_TYPE; // SNS Owner
            }
            
            if (isShowAllContactsItemPosition(position - mIndexChange)) {
                return IGNORE_ITEM_VIEW_TYPE;
            }

            if (isSearchAllContactsItemPosition(position - mIndexChange)) {
                return IGNORE_ITEM_VIEW_TYPE;
            }

            if (getSeparatorId(position - mIndexChange) != 0) {
                // We don't want the separator view to be recycled.
                return IGNORE_ITEM_VIEW_TYPE;
            }

            return super.getItemViewType(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (!mDataValid) {
                throw new IllegalStateException(
                        "this should only be called when the cursor is valid");
            }

            // handle the total contacts item
            if (position == 0 && mShowNumberOfContacts) {
                return getTotalContactCountView(parent);
            }

            if (position == 0 && (mMode & MODE_MASK_CREATE_NEW) != 0) {
                // Add the header for creating a new contact
                return getLayoutInflater().inflate(R.layout.create_new_contact, parent, false);
            }

	    if(position == 1 && FeatureOption.MTK_SNS_SUPPORT) {

			if(mMode == MODE_DEFAULT && !mSearchMode) {
				mIndexChange = 1;
				bIsOwnerShow = true;
				return getOwnerView(parent);
			}
			else {
				bIsOwnerShow = false;
			}
		    
	    }

            if (isShowAllContactsItemPosition(position - mIndexChange)) {
                return getLayoutInflater().
                        inflate(R.layout.contacts_list_show_all_item, parent, false);
            }
            boolean isSearchAllContacts = isSearchAllContactsItemPosition(position - mIndexChange);

            if ( isSearchAllContacts && !mAddContactState) {
                return getLayoutInflater().
                        inflate(R.layout.contacts_list_search_all_item, parent, false);
            }
            if (isSearchAllContacts && mAddContactState) {
                TextView t= (TextView) getLayoutInflater().
                        inflate(R.layout.empty_textview, parent, false);
                return t;
            }
            // Handle the separator specially
            int separatorId = getSeparatorId(position - mIndexChange);
            Log.i(TAG,"********position is " + position);
            Log.i(TAG,"********mIndexChange is " + mIndexChange);
            Log.i(TAG,"********mSuggestionsCursorCount is " + mSuggestionsCursorCount);
            if (separatorId != 0) {
                TextView view = (TextView) getLayoutInflater().
                        inflate(R.layout.list_separator, parent, false);
                view.setText(separatorId);
                return view;
            }

            boolean showingSuggestion;
            Cursor cursor;
            if (mSuggestionsCursorCount != 0 && position < mSuggestionsCursorCount + 2) {
                showingSuggestion = true;
                cursor = mSuggestionsCursor;
            } else {
                showingSuggestion = false;
                cursor = mCursor;
            }

            int realPosition = getRealPosition(position - mIndexChange);
            if (!cursor.moveToPosition(realPosition)) {
                //MTK80736 avoid JE when the list refreshed frequently
                Log.e(TAG, "couldn't move cursor to position " + position + " in " + cursor.getCount());
                //throw new IllegalStateException("couldn't move cursor to position " + position);
                return getLayoutInflater().inflate(R.layout.total_contacts, parent, false);
            }
            
            if (cursor != null) {
            	Log.i(TAG,"*********cursor.getCount() is " + cursor.getCount());
            }
            if (mCursor != null) {
            	Log.i(TAG,"*********mCursor.getCount() is " + mCursor.getCount());
            }

            View v;
            if (convertView == null || convertView.getTag() == null) {
                v = newView(mContext, cursor, parent);
            } else {
                v = convertView;
            }
		long currentID = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
		mScrollViews.put(Long.parseLong(position+""), (ContactListItemView)v);
		mScrollIds.put(Long.parseLong(position+""), currentID);
            bindView(v, mContext, cursor);
            bindSectionHeader(v, realPosition, mDisplaySectionHeaders && !showingSuggestion);
            return v;
        }

        private View getNoneView(ViewGroup parent) {
        	if(false == FeatureOption.MTK_SNS_SUPPORT)
        	{
        		return null;
        	}
        	final LayoutInflater inflater = getLayoutInflater();
            View noneView = (View) inflater.inflate(R.layout.contacts_list_item_none,
                    parent, false);
            return noneView;
        }
        
	private View getOwnerView(ViewGroup list){
	    	if(false == FeatureOption.MTK_SNS_SUPPORT)
	    	{
	    		return getNoneView(list);
	    	}
	    	//Make sure the returned ownerView is not null
	    	if (ownerView == null)
	    		setOwnerView();
		return ownerView;
	}

        private View getTotalContactCountView(ViewGroup parent) {
            final LayoutInflater inflater = getLayoutInflater();
            View view;
            if ((mBladeView != null) && Locale.getDefault().getLanguage().toLowerCase().equals("ar")) {
                view = inflater.inflate(R.layout.total_contacts_ar, parent, false);
            } else {
                view = inflater.inflate(R.layout.total_contacts, parent, false);
            }

            TextView totalContacts = (TextView) view.findViewById(R.id.totalContactsText);

            String text;
            int count = getRealCount();
	    if(true == FeatureOption.MTK_SNS_SUPPORT){
		if(mMode == MODE_DEFAULT && !mSearchMode && count > 0) {
			count = count - 1;
		}
	    }

            if (mSearchMode && !TextUtils.isEmpty(getTextFilter())) {
                text = getQuantityText(count, R.string.listFoundAllContactsZero,
                        R.plurals.searchFoundContacts);
            } else {
                if (mDisplayOnlyPhones) {
                    text = getQuantityText(count, R.string.listTotalPhoneContactsZero,
                            R.plurals.listTotalPhoneContacts);
                } else {
                    text = getQuantityText(count, R.string.listTotalAllContactsZero,
                            R.plurals.listTotalAllContacts);
                }
            }
            totalContacts.setText(text);
            return view;
        }

        private boolean isShowAllContactsItemPosition(int position) {
            return mMode == MODE_JOIN_CONTACT && mJoinModeShowAllContacts
                    && mSuggestionsCursorCount != 0 && position == mSuggestionsCursorCount + 2;
        }

        private boolean isSearchAllContactsItemPosition(int position) {
            return mSearchMode && (position == getCount() - 1);
        }

        private int getSeparatorId(int position) {
            int separatorId = 0;
            if (position == mFrequentSeparatorPos) {
                separatorId = R.string.favoritesFrquentSeparator;
            }
            if (mSuggestionsCursorCount != 0) {
                if (position == 0) {
                    separatorId = R.string.separatorJoinAggregateSuggestions;
                } else if (position == mSuggestionsCursorCount + 1) {
                    separatorId = R.string.separatorJoinAggregateAll;
                }
            }
            return separatorId;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final ContactListItemView view = new ContactListItemView(context, null);
            view.setOnCallButtonClickListener(ContactsListActivity.this);
			ContactListItemCache cache = new ContactListItemCache();
			view.setTag(cache);
            return view;
        }

        @Override
        public void bindView(View itemView, Context context, Cursor cursor) {
            final ContactListItemView view = (ContactListItemView)itemView;
            final ContactListItemCache cache = (ContactListItemCache) view.getTag();
            final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
					.getService(Context.TELEPHONY_SERVICE));
            int typeColumnIndex;
            int dataColumnIndex;
            int labelColumnIndex;
            int nameColumnIndex;
            int phoneticNameColumnIndex;
			int simIndicateIndex; // mtk80909 for Speed Dial
            boolean displayAdditionalData = mDisplayAdditionalData;
            boolean highlightingEnabled = false;
            switch(mMode) {
                case MODE_PICK_PHONE:
                case MODE_LEGACY_PICK_PHONE:
                case MODE_QUERY_PICK_PHONE:
                case MODE_PICK_PHONE_EMAIL:
                case MODE_QUERY_PICK_PHONE_EMAIL:{
                    nameColumnIndex = PHONE_DISPLAY_NAME_COLUMN_INDEX;
                    phoneticNameColumnIndex = -1;
                    dataColumnIndex = PHONE_NUMBER_COLUMN_INDEX;
                    typeColumnIndex = PHONE_TYPE_COLUMN_INDEX;
                    labelColumnIndex = PHONE_LABEL_COLUMN_INDEX;
					simIndicateIndex = PHONE_INDICATE_PHONE_SIM_INDEX; // mtk80909 for Speed Dial
                    break;
                }
                case MODE_PICK_POSTAL:
                case MODE_LEGACY_PICK_POSTAL: {
                    nameColumnIndex = POSTAL_DISPLAY_NAME_COLUMN_INDEX;
                    phoneticNameColumnIndex = -1;
                    dataColumnIndex = POSTAL_ADDRESS_COLUMN_INDEX;
                    typeColumnIndex = POSTAL_TYPE_COLUMN_INDEX;
                    labelColumnIndex = POSTAL_LABEL_COLUMN_INDEX;
					simIndicateIndex = -1; // mtk80909 for Speed Dial
                    break;
                }
                default: {
                    nameColumnIndex = getSummaryDisplayNameColumnIndex();
                    if (mMode == MODE_LEGACY_PICK_PERSON
                            || mMode == MODE_LEGACY_PICK_OR_CREATE_PERSON) {
                        phoneticNameColumnIndex = -1;
                    } else {
                        phoneticNameColumnIndex = SUMMARY_PHONETIC_NAME_COLUMN_INDEX;
                    }
                    dataColumnIndex = -1;
                    typeColumnIndex = -1;
                    labelColumnIndex = -1;
				simIndicateIndex = -1; // mtk80909 for Speed Dial
                    displayAdditionalData = false;
                    highlightingEnabled = mHighlightWhenScrolling && mMode != MODE_STREQUENT;
                }
			}

			// mtk80909 for Speed Dial (test)
			if (simIndicateIndex > -1) {
				int simIndicate = cursor.getInt(simIndicateIndex);
				Log.i("special", "simIndicate = " + simIndicate);
            }

            // Set the name
            cursor.copyStringToBuffer(nameColumnIndex, cache.nameBuffer);
            TextView nameView = view.getNameTextView();
            int size = cache.nameBuffer.sizeCopied;
            if (size != 0) {
                if (highlightingEnabled) {
                    if (cache.textWithHighlighting == null) {
                        cache.textWithHighlighting =
                                mHighlightingAnimation.createTextWithHighlighting();
                    }
                    buildDisplayNameWithHighlighting(nameView, cursor, cache.nameBuffer,
                            cache.highlightedTextBuffer, cache.textWithHighlighting);
                } else {
                    nameView.setText(cache.nameBuffer.data, 0, size);
                }
            } else {
                nameView.setText(mUnknownNameText);
            }

            boolean hasPhone = cursor.getColumnCount() > SUMMARY_HAS_PHONE_COLUMN_INDEX
                    && cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX) != 0;

            // Make the call button visible if requested.
            if (mDisplayCallButton && hasPhone) {
                int pos = cursor.getPosition();
                view.showCallButton(android.R.id.button1, pos);
            } else {
                view.hideCallButton();
            }
	    SnsUser curContact = null;
            // Set the photo, if requested
            if (mDisplayPhotos) {
                boolean useQuickContact = (mMode & MODE_MASK_DISABLE_QUIKCCONTACT) == 0;
                int indicate = cursor.getInt(cursor
						.getColumnIndexOrThrow(RawContacts.INDICATE_PHONE_SIM));
                long photoId = 0;
                if (!cursor.isNull(SUMMARY_PHOTO_ID_COLUMN_INDEX)) {
                    photoId = cursor.getLong(SUMMARY_PHOTO_ID_COLUMN_INDEX);
                }

                ImageView viewToUse;
                if (useQuickContact) {
                    // Build soft lookup reference
                    final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
                    final String lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY_COLUMN_INDEX);
                    QuickContactBadge quickContact = view.getQuickContact();
                    quickContact.assignContactUri(Contacts.getLookupUri(contactId, lookupKey));
                    quickContact.setSelectedContactsAppTabIndex(StickyTabs.getTab(getIntent()));
                    viewToUse = quickContact;
                } else {
                    viewToUse = view.getPhotoView();
                }
				if (mMode == MODE_DEFAULT)
					viewToUse.setClickable(!mDeleteState);
                final int position = cursor.getPosition();
                //Query to get mSimInfoWrapper. Add by mtk80908.
                if (mSimInfoWrapper == null)
                	mSimInfoWrapper = ContactsUtils.SIMInfoWrapper.getDefault(ContactsListActivity.this,false);
                mSlot = mSimInfoWrapper.getSimSlotById(indicate);
                mPhotoLoader.loadPhoto(viewToUse, photoId, mSlot);
                Log.i(TAG,"set photo indicate is "+indicate);
                Log.i(TAG,"set photo mSlot is "+mSlot);
                			// support gemini
//				if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {//80794 for USIM
//					Log.i(TAG,"MTK_GEMINI_SUPPORT");
//					if (photoId == 0 && indicate == RawContacts.INDICATE_SIM1) {
//						viewToUse
//								.setImageResource(R.drawable.contact_icon_sim1);
//					} else if (photoId == 0
//							&& indicate == RawContacts.INDICATE_SIM2) {
//						viewToUse
//								.setImageResource(R.drawable.contact_icon_sim2);
//					} else if (photoId == 0 && indicate == RawContacts.INDICATE_USIM1) {
//						viewToUse
//						.setImageResource(R.drawable.contact_icon_usim1);
//					} else if (photoId == 0
//							&& indicate == RawContacts.INDICATE_USIM2) {
//						viewToUse
//								.setImageResource(R.drawable.contact_icon_usim2);
//					}
//					else if (photoId == 0) {
//						viewToUse
//								.setImageResource(R.drawable.ic_contact_list_picture);
//					}
//				} else {
//					Log.i(TAG,"not MTK_GEMINI_SUPPORT");
//					if (photoId == 0 && indicate == RawContacts.INDICATE_SIM) {
//						viewToUse.setImageResource(R.drawable.contact_icon_sim);
//					} else if (photoId == 0 && indicate == RawContacts.INDICATE_USIM) {
//						viewToUse.setImageResource(R.drawable.contact_icon_usim);
//					} else if (photoId == 0) {
//						viewToUse.setImageResource(R.drawable.ic_contact_list_picture);
//					}
//
//				}
               
               if(true == FeatureOption.MTK_SNS_SUPPORT)
               {
		    if(null != view) {
               		//view.setSnsLogo(null);
			//view.setSnsStatus(null);
			long currentID = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
			ImageView logo = view.getSnsLogo();
			TextView text = view.getSnsStatus();
			if(null != logo && null != text && mSnsLoader != null){
				mSnsLoader.loadSns(logo, text, currentID);
			}
		    }
               }

            }

            if ((mMode & MODE_MASK_NO_PRESENCE) == 0) {
                // Set the proper icon (star or presence or nothing)
                int serverStatus;
                if (!cursor.isNull(SUMMARY_PRESENCE_STATUS_COLUMN_INDEX)) {
                    serverStatus = cursor.getInt(SUMMARY_PRESENCE_STATUS_COLUMN_INDEX);
                    Drawable icon = ContactPresenceIconUtil.getPresenceIcon(mContext, serverStatus);
                    if (icon != null) {
                        view.setPresence(icon);
                    } else {
                        view.setPresence(null);
                    }
                } else {
                    view.setPresence(null);
                }
            } else {
                view.setPresence(null);
            }

            if (mShowSearchSnippets && cursor.getColumnCount() > SUMMARY_SNIPPET_MIMETYPE_COLUMN_INDEX) {
                boolean showSnippet = false;
                String snippetMimeType = cursor.getString(SUMMARY_SNIPPET_MIMETYPE_COLUMN_INDEX);
                if (Email.CONTENT_ITEM_TYPE.equals(snippetMimeType)) {
                    String email = cursor.getString(SUMMARY_SNIPPET_DATA1_COLUMN_INDEX);
                    if (!TextUtils.isEmpty(email)) {
                        view.setSnippet(email);
                        showSnippet = true;
                    }
                } else if (Organization.CONTENT_ITEM_TYPE.equals(snippetMimeType)) {
                    String company = cursor.getString(SUMMARY_SNIPPET_DATA1_COLUMN_INDEX);
                    String title = cursor.getString(SUMMARY_SNIPPET_DATA4_COLUMN_INDEX);
                    if (!TextUtils.isEmpty(company)) {
                        if (!TextUtils.isEmpty(title)) {
                            view.setSnippet(company + " / " + title);
                        } else {
                            view.setSnippet(company);
                        }
                        showSnippet = true;
                    } else if (!TextUtils.isEmpty(title)) {
                        view.setSnippet(title);
                        showSnippet = true;
                    }
                } else if (Nickname.CONTENT_ITEM_TYPE.equals(snippetMimeType)) {
                    String nickname = cursor.getString(SUMMARY_SNIPPET_DATA1_COLUMN_INDEX);
                    if (!TextUtils.isEmpty(nickname)) {
                        view.setSnippet(nickname);
                        showSnippet = true;
                    }
                }

                if (!showSnippet) {
                    view.setSnippet(null);
                }
            }
            
            if(true == FeatureOption.MTK_SNS_SUPPORT)
            {
            	if(curContact != null && curContact.snsUrl != null && curContact.status != null && curContact.status.length() >= 0){
		    	return;
		    }
            	}
            //Modefied by mtk80908
            if (mMode == MODE_DEFAULT && mDeleteState) { // [mtk80909]
            	//Set CheckBox state According to previous selection.
            	long currentID = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
                CheckBox cb = view.getCheckBox();
                if (cb != null) {
                	cb.setVisibility(View.VISIBLE);
                	cb.setTag(new Integer(cursor.getPosition()));
//	                boolean bSnsOwner = (FeatureOption.MTK_SNS_SUPPORT && !mSearchMode && mMode == MODE_DEFAULT);
//	                cb.setChecked(ContactsListActivity.this.mToBeDeleted[bSnsOwner ? cursor
//	                        .getPosition() + 1 : cursor.getPosition()]);
                	if (mSelectAllToDel) {
                		cb.setChecked(true);
                	} else if (mToBeDeletedContacts != null &&
                			mToBeDeletedContacts.containsKey(currentID)) {
                		cb.setChecked(true);
                	} else {
                		cb.setChecked(false);
                	}
                } else {
                    Log.e(TAG, "cb should not be null");
                }
            } else {
                view.setCheckable(false);
            }
            
            if (!displayAdditionalData) {
                if (phoneticNameColumnIndex != -1) {

                    // Set the name
                    cursor.copyStringToBuffer(phoneticNameColumnIndex, cache.phoneticNameBuffer);
                    int phoneticNameSize = cache.phoneticNameBuffer.sizeCopied;
                    if (phoneticNameSize != 0) {
                        view.setLabel(cache.phoneticNameBuffer.data, phoneticNameSize);
                    } else {
                        view.setLabel(null);
                    }
                } else {
                    view.setLabel(null);
                }
                
                return;
            }

            // Set the data.
            cursor.copyStringToBuffer(dataColumnIndex, cache.dataBuffer);
			// mtk80909, 2010-9-17
			final int NUMBER_MAX_LENGTH = 20;
			final int LABEL_MAX_LENGTH = 8;

            size = cache.dataBuffer.sizeCopied;
            // mtk80909, 2010-9-17
			if (size > NUMBER_MAX_LENGTH) {
				size = NUMBER_MAX_LENGTH;
				for (int i = 1; i < 4; ++i) {
					cache.dataBuffer.data[NUMBER_MAX_LENGTH - i] = '.';
				}
			}
			
            view.setData(cache.dataBuffer.data, size);

            // Set the label.
            if (!cursor.isNull(typeColumnIndex)) {
                final int type = cursor.getInt(typeColumnIndex);
                final String label = cursor.getString(labelColumnIndex);

                if (mMode == MODE_LEGACY_PICK_POSTAL || mMode == MODE_PICK_POSTAL) {
                    // TODO cache
                    view.setLabel(StructuredPostal.getTypeLabel(context.getResources(), type,
                            label));
                } else {
                    // TODO cache
                    view.setLabel(Phone.getTypeLabel(context.getResources(), type, label));
                }
                // mtk80909, 2010-9-17
				if (mMode == MODE_PICK_PHONE && view.getLabelView().getText().length() > LABEL_MAX_LENGTH) {
					CharSequence cs = view.getLabelView().getText().subSequence(0, LABEL_MAX_LENGTH) + "...";
					view.setLabel(cs);
                }
            } else {
                view.setLabel(null);
            }
        }

        /**
         * Computes the span of the display name that has highlighted parts and configures
         * the display name text view accordingly.
         */
        private void buildDisplayNameWithHighlighting(TextView textView, Cursor cursor,
                CharArrayBuffer buffer1, CharArrayBuffer buffer2,
                TextWithHighlighting textWithHighlighting) {
            int oppositeDisplayOrderColumnIndex;
            if (mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
                oppositeDisplayOrderColumnIndex = SUMMARY_DISPLAY_NAME_ALTERNATIVE_COLUMN_INDEX;
            } else {
                oppositeDisplayOrderColumnIndex = SUMMARY_DISPLAY_NAME_PRIMARY_COLUMN_INDEX;
            }
            cursor.copyStringToBuffer(oppositeDisplayOrderColumnIndex, buffer2);

            textWithHighlighting.setText(buffer1, buffer2);
            textView.setText(textWithHighlighting);
        }

        private void bindSectionHeader(View itemView, int position, boolean displaySectionHeaders) {
            final ContactListItemView view = (ContactListItemView)itemView;
            final ContactListItemCache cache = (ContactListItemCache) view.getTag();
            if (!displaySectionHeaders) {
                view.setSectionHeader(null);
                view.setDividerVisible(true);
            } else {
                final int section = getSectionForRealPosition(position);
                if (getRealPositionForSection(section) == position) {
                    String title = (String)mIndexer.getSections()[section];
                    view.setSectionHeader(title);
                } else {
                    view.setDividerVisible(false);
                    view.setSectionHeader(null);
                }

                // move the divider for the last item in a section
                if (getRealPositionForSection(section + 1) - 1 == position) {
                    view.setDividerVisible(false);
                } else {
                    view.setDividerVisible(true);
                }
            }
        }

        @Override
        public void changeCursor(Cursor cursor) {
            if (cursor != null) {
                setLoading(false);
            }
            Log.i(TAG, "################changeCursor() begin ");
            if (cursor != null ) {  // for ALPS00054320
               Bundle bundle = cursor.getExtras();
               int countsLen = 0;


              if (bundle.containsKey(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES)) {
                String sections[] =
                    bundle.getStringArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES);
                Log.i(TAG, "######################changeCursor()  sections: " + sections);
                if(sections != null) {
                	Log.i(TAG, "####################changeCursor()  sections length: " + sections.length);
              	}
                int counts[] = bundle.getIntArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS);
                   if (counts != null) {
                countsLen = counts.length;
                Log.i(TAG, "###################changeCursor()  counts length: " + countsLen);
                if ((cursor.getCount() != 0) && (countsLen == 0)) { //have one contact but no section get
                
                Log.i(TAG, "################changeCursor() return########### ");
                    return;
                }
                }
            }

            }

            // Get the split between starred and frequent items, if the mode is strequent
            mFrequentSeparatorPos = ListView.INVALID_POSITION;
            int cursorCount = 0;
            if (cursor != null && (cursorCount = cursor.getCount()) > 0
                    && mMode == MODE_STREQUENT) {
                cursor.move(-1);
                int starred;
                for (int i = 0; cursor.moveToNext(); i++) {
                    starred = cursor.getInt(SUMMARY_STARRED_COLUMN_INDEX);
                    if (starred == 0) {
                        if (i >= 0) {
                            // Only add the separator when there are starred items present
                            mFrequentSeparatorPos = i;
                        }
                        break;
                    }
                }
            }

            if (cursor != null && mSearchResultsMode) {
                TextView foundContactsText = (TextView)findViewById(R.id.search_results_found);
                String text = getQuantityText(cursor.getCount(), R.string.listFoundAllContactsZero,
                        R.plurals.listFoundAllContacts);
                foundContactsText.setText(text);
            }

            super.changeCursor(cursor);
            // Update the indexer for the fast scroll widget
            updateIndexer(cursor);
        }

        private void updateIndexer(Cursor cursor) {
            if (cursor == null) {
                mIndexer = null;
                return;
            }

            Bundle bundle = cursor.getExtras();
            if (bundle.containsKey(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES)) {
                String sections[] =
                    bundle.getStringArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES);
                int counts[] = bundle.getIntArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS);
                mIndexer = new ContactsSectionIndexer(sections, counts);
            } else {
                mIndexer = null;
            }
        }

        /**
         * Run the query on a helper thread. Beware that this code does not run
         * on the main UI thread!
         */
        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            Log.i(TAG, "runQueryOnBackgroundThread " + constraint);
            ContactsListActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    if (mSearchEditText != null) {
                        mSearchEditText.setLoading(true);
                    }
                }
            });
            
            Cursor cursor = doFilter(constraint.toString());
            ContactsListActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    if (mSearchEditText != null) {
                        mSearchEditText.setLoading(false);
                    }
                }
            });
            String now = getTextFilter();
            Log.i(TAG, "runQueryOnBackgroundThread filter " + now);
            if ((now == null || now.length() == 0)
                    && (constraint == null || constraint.length() == 0)) {
                return cursor;
            }
            if ((now == null || now.length() == 0)
                    && !(constraint == null || constraint.length() == 0)) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            if (now != null && !now.equals(constraint)) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
                
            return cursor;
        }

        public Object [] getSections() {
            if (mIndexer == null) {
                return new String[] { " " };
            } else {
                return mIndexer.getSections();
            }
        }

        public int getPositionForSection(int sectionIndex) {
            if (mIndexer == null) {
                return -1;
            }
            int retVal = mIndexer.getPositionForSection(sectionIndex);            
            int headerOffset = mIndexChange; // SNS owner view
            if (mShowNumberOfContacts) {
            	headerOffset += 1;	// contact count view
            }
            retVal += headerOffset;
            return retVal;
        }

        public int getRealPositionForSection(int sectionIndex) {
            if (mIndexer == null) {
                return -1;
            }            
            int retVal = mIndexer.getPositionForSection(sectionIndex);
            return retVal;
        }

        public int getSectionForPosition(int position) {
            if (mIndexer == null) {
                return -1;
            }
            int headerOffset = mIndexChange; // SNS owner view
            if (mShowNumberOfContacts) {
            	headerOffset += 1;	// contact count view
        }
            int retVal = mIndexer.getSectionForPosition(position - headerOffset);
            return retVal < 0 ? 0 : retVal;
        }        

        public int getSectionForRealPosition(int position) {
            if (mIndexer == null) {
                return -1;
        	}
            int retVal = mIndexer.getSectionForPosition(position);
            return retVal;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return mMode != MODE_STARRED
                && !mShowNumberOfContacts
                && mSuggestionsCursorCount == 0;
        }

        @Override
        public boolean isEnabled(int position) {
            if (mShowNumberOfContacts) {
                if (position == 0) {
                    return false;
                }
                position--;
            }

            if (mSuggestionsCursorCount > 0) {
                return position != 0 && position != mSuggestionsCursorCount + 1;
            }
            return position != mFrequentSeparatorPos;
        }

        @Override
        public int getCount() {
            if (!mDataValid) {
                return 0;
            }
            int superCount = super.getCount();

            if (mShowNumberOfContacts && (mSearchMode || superCount > 0)) {
                // We don't want to count this header if it's the only thing visible, so that
                // the empty text will display.
                superCount++;
            }

            if (mSearchMode) {
                // Last element in the list is the "Find
                superCount++;
            }

            // We do not show the "Create New" button in Search mode
            if ((mMode & MODE_MASK_CREATE_NEW) != 0 && !mSearchMode) {
                // Count the "Create new contact" line
                superCount++;
            }

            if (mSuggestionsCursorCount != 0) {
                // When showing suggestions, we have 2 additional list items: the "Suggestions"
                // and "All contacts" headers.
                return mSuggestionsCursorCount + superCount + 2;
            }
            else if (mFrequentSeparatorPos != ListView.INVALID_POSITION) {
                // When showing strequent list, we have an additional list item - the separator.
                return superCount + 1;
            } else {
		if(true == FeatureOption.MTK_SNS_SUPPORT) {
			if(mMode == MODE_DEFAULT && !mSearchMode) {
				if(0 == superCount)
					return superCount;
				else 
					return superCount + 1;
			}
	    	}
                return superCount;
            }
        }

        /**
         * Gets the actual count of contacts and excludes all the headers.
         */
        public int getRealCount() {
	    if(true == FeatureOption.MTK_SNS_SUPPORT) {
		int count = super.getCount();
		if(mMode == MODE_DEFAULT && !mSearchMode) {
			if(0 == count)
				return count;
			else
				return count + 1;
		}
	    }
            return super.getCount();
        }

        private int getRealPosition(int pos) {
            if (mShowNumberOfContacts) {
                pos--;
            }

            if ((mMode & MODE_MASK_CREATE_NEW) != 0 && !mSearchMode) {
                return pos - 1;
            } else if (mSuggestionsCursorCount != 0) {
                // When showing suggestions, we have 2 additional list items: the "Suggestions"
                // and "All contacts" separators.
                if (pos < mSuggestionsCursorCount + 2) {
                    // We are in the upper partition (Suggestions). Adjusting for the "Suggestions"
                    // separator.
                    return pos - 1;
                } else {
                    // We are in the lower partition (All contacts). Adjusting for the size
                    // of the upper partition plus the two separators.
                    return pos - mSuggestionsCursorCount - 2;
                }
            } else if (mFrequentSeparatorPos == ListView.INVALID_POSITION) {
                // No separator, identity map
                return pos;
            } else if (pos <= mFrequentSeparatorPos) {
                // Before or at the separator, identity map
                return pos;
            } else {
                // After the separator, remove 1 from the pos to get the real underlying pos
                return pos - 1;
            }
        }

        @Override
        public Object getItem(int pos) {
	    pos = pos - mIndexChange;
            if (mSuggestionsCursorCount != 0 && pos <= mSuggestionsCursorCount) {
				mSuggestionsCursor.moveToPosition(mBeingDeleted ? pos
						: getRealPosition(pos));
                return mSuggestionsCursor;
            } else if (isSearchAllContactsItemPosition(pos)){
                return null;
            } else {
                int realPosition = getRealPosition(pos);
				if (realPosition < 0 && !mBeingDeleted) {
                    return null;
                }
				return super.getItem(mBeingDeleted ? pos : realPosition);
            }
        }

        @Override
        public long getItemId(int pos) {
	    pos = pos - mIndexChange;
            if (mSuggestionsCursorCount != 0 && pos < mSuggestionsCursorCount + 2) {
                if (mSuggestionsCursor.moveToPosition(pos - 1)) {
                    return mSuggestionsCursor.getLong(mRowIDColumn);
                } else {
                    return 0;
                }
            } else if (isSearchAllContactsItemPosition(pos)) {
                return 0;
            }
            int realPosition = getRealPosition(pos);
            if (realPosition < 0) {
                return 0;
            }
            return super.getItemId(realPosition);
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
                Log.d(TAG, "onScroll() begin########################## ");
            if (view instanceof PinnedHeaderListView) {
                ((PinnedHeaderListView)view).configureHeaderView(firstVisibleItem);
                if (mIndexer != null) {
                	int section = getSectionForPosition(firstVisibleItem);
                    Log.d(TAG, "onScroll() ##########################section= "+ section);
                    if (mBladeView != null)
                        mBladeView.setCurrentSection(section);
                }
            }
	    if(true == FeatureOption.MTK_SNS_SUPPORT) {
		if(mDisplayPhotos) {
			mStartIndex = firstVisibleItem;
			mItemCount = visibleItemCount;
		}
	    }
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrollState = scrollState;
            if (mHighlightWhenScrolling) {
                if (scrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                    mHighlightingAnimation.startHighlighting();
                } else {
                    mHighlightingAnimation.stopHighlighting();
                }
            }
	    if(true == FeatureOption.MTK_SNS_SUPPORT)
            {
	    	if (mSnsLoader != null)
	    		mSnsLoader.stop();
	    }
            if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                mPhotoLoader.pause();
                if(true == FeatureOption.MTK_SNS_SUPPORT)
                {
                	if (mSnsLoader != null)
                		mSnsLoader.pause();
                }
            } else if (mDisplayPhotos) {
                mPhotoLoader.resume();
            }

	    if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
		if(true == FeatureOption.MTK_SNS_SUPPORT)
                {
                	if(mDisplayPhotos && mSnsLoader != null) 
                		mSnsLoader.resume();
                }
	    }

	    if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
		if(true == FeatureOption.MTK_SNS_SUPPORT)
                {
			if(mDisplayPhotos && mSnsLoader != null) {
				mSnsLoader.clear();
		    mSnsLoader.resume();
		    long currentID;
		    ContactListItemView itemview;
				for(int i = mStartIndex; i < mStartIndex + mItemCount; i++){
					itemview = (ContactListItemView) mScrollViews.get(Long.parseLong(i+""));
					if(itemview == null) continue;
					currentID = mScrollIds.get(Long.parseLong(i+"")) == null ? 0 : (long) mScrollIds.get(Long.parseLong(i+""));
					//itemview.setSnsLogo(null);
					//itemview.setSnsStatus(null);
					ImageView logo = itemview.getSnsLogo();
					TextView text = itemview.getSnsStatus();
					if(null != logo && null != text)
						mSnsLoader.loadSns(logo, text, currentID);
				}
				mScrollViews.clear();
				mScrollIds.clear();
			}
                }
	    }
        }

        /**
         * Computes the state of the pinned header.  It can be invisible, fully
         * visible or partially pushed up out of the view.
         */
        public int getPinnedHeaderState(int position) {
	    position = position - mIndexChange;
            if (mIndexer == null || mCursor == null || mCursor.getCount() == 0) {
                return PINNED_HEADER_GONE;
            }

            int realPosition = getRealPosition(position);
            if (realPosition < 0) {
                return PINNED_HEADER_GONE;
            }

            // The header should get pushed up if the top item shown
            // is the last item in a section for a particular letter.
            int section = getSectionForRealPosition(realPosition);
			int nextSectionPosition = getRealPositionForSection(section + 1);
            if (nextSectionPosition != -1 && realPosition == nextSectionPosition - 1) {
                return PINNED_HEADER_PUSHED_UP;
            }

            return PINNED_HEADER_VISIBLE;
        }

        /**
         * Configures the pinned header by setting the appropriate text label
         * and also adjusting color if necessary.  The color needs to be
         * adjusted when the pinned header is being pushed up from the view.
         */
        public void configurePinnedHeader(View header, int position, int alpha) {
	    position = position - mIndexChange;
            PinnedHeaderCache cache = (PinnedHeaderCache)header.getTag();
            if (cache == null) {
                cache = new PinnedHeaderCache();
                cache.titleView = (TextView)header.findViewById(R.id.header_text);
                cache.textColor = cache.titleView.getTextColors();
                cache.background = header.getBackground();
                header.setTag(cache);
            }

            int realPosition = getRealPosition(position);
            int section = getSectionForRealPosition(realPosition);

            String title = (String)mIndexer.getSections()[section];
            cache.titleView.setText(title);

            if (alpha == 255) {
                // Opaque: use the default background, and the original text color
                header.setBackgroundDrawable(cache.background);
                cache.titleView.setTextColor(cache.textColor);
            } else {
                // Faded: use a solid color approximation of the background, and
                // a translucent text color
                header.setBackgroundColor(Color.rgb(
                        Color.red(mPinnedHeaderBackgroundColor) * alpha / 255,
                        Color.green(mPinnedHeaderBackgroundColor) * alpha / 255,
                        Color.blue(mPinnedHeaderBackgroundColor) * alpha / 255));

                int textColor = cache.textColor.getDefaultColor();
                cache.titleView.setTextColor(Color.argb(alpha,
                        Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
            }
        }

		public void registerUpdate() {
			Log.i(TAG, "registerUpdate() start");
			mCursor.registerContentObserver(mChangeObserver);
			mCursor.registerDataSetObserver(mDataSetObserver);
			mUpdateRegistered = true;
			Log.i(TAG, "registerUpdate() end");
		}

		public void unregisterUpdate() {
			Log.i(TAG, "unregisterUpdate() start");
			mCursor.unregisterContentObserver(mChangeObserver);
			mCursor.unregisterDataSetObserver(mDataSetObserver);
			mUpdateRegistered = false;
			Log.i(TAG, "unregisterUpdate() end");
		}

		private boolean mUpdateRegistered = true;

		public boolean isUpdateRegistered() {
			return mUpdateRegistered;
		}
		// mtk80909 end
    }

    private boolean isWidthLargerThanHeight()
    {
    	boolean retVal = false;
    	DisplayMetrics dm = new DisplayMetrics();
    	WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
    	wm.getDefaultDisplay().getMetrics(dm);
    	if(dm.widthPixels > dm.heightPixels)
    		retVal = true;
        return retVal;
    }

	BladeView getBladeView() {
		return mBladeView;
	}
	
	private ContactsPreferences.ChangeListener mPreferencesChangeListener = new ContactsPreferences.ChangeListener() {
        //@Override
        public void onChange() {
            // When returning from DisplayOptions, onActivityResult ensures that we reload the list,
            // so we do not have to do anything here. However, ContactsPreferences requires a change
            // listener, otherwise it would not reload its settings.
        }
    };
    
	public CellConnMgr getCellConnMgr() {
        // TODO Auto-generated method stub
        return mCellConnMgr;
    }
	
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
            
            ArrayList associateSims = new ArrayList();
            
            final String number = (String)cookie;
            
            try {
                if(cursor != null && cursor.moveToFirst()) {
                    do {
                        if(cursor.getInt(0) > 0)
                            associateSims.add(Integer.valueOf(cursor.getInt(0)));
                    } while (cursor.moveToNext());
                }
            } catch(Exception e) {
            	Log.i(TAG,"onQueryComplete, cursor exception");
            } finally {
                if(cursor != null)
                    cursor.close();
            }
//            ContactsUtils.dial(mContext, number, associateSims, null);
            ContactsUtils.dial(mContext, number, associateSims, new ContactsUtils.OnDialCompleteListener() {
                
                public void onDialComplete(boolean dialed) {
                    // TODO Auto-generated method stub
                    if(dialed)
                        ContactsListActivity.this.finish();
                }
            });
        }
	}

    private void makeCall(String number, int type) {
        ContactsUtils.dial(this, number, type, new ContactsUtils.OnDialCompleteListener() {
            public void onDialComplete(boolean dialed) {
                // TODO Auto-generated method stub
                if(dialed)
                    ContactsListActivity.this.finish();
            }
        });

    }    
    
    static public void waitImportingSimThread()   ////
    {
    	while(ContactsSearchActivity.s_importingSimContacts1 || ContactsSearchActivity.s_importingSimContacts2) {try{Thread.sleep(1);}catch(Exception e){}}
    }
    
   
}
