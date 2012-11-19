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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.android.contacts.ui.ContactsPreferences;
import com.android.contacts.ui.EditSimContactActivity;
import com.android.internal.telephony.ITelephony;
import com.android.phone.CallLogAsync;
import com.android.phone.HapticFeedback;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.Settings;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.provider.Contacts.PhonesColumns;
import android.provider.Contacts.Intents.Insert;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony.SIMInfo;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/* Fion add start */
import android.provider.Settings;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.mediatek.featureoption.FeatureOption;
/* Fion add end */

import android.app.AlertDialog;
import android.app.Dialog;
import android.view.WindowManager;

import android.telephony.ServiceState;

/* Dialer search begin - xiaodong wang*/
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.DialerSearch;
import android.provider.ContactsContract.RawContacts;
//import android.provider.ContactsContract.CommonDataKinds.Phone;//80794
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.QuickContactBadge;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout.LayoutParams;
//import android.widget.Gallery.LayoutParams;

import com.android.internal.telephony.CallerInfo;
import com.android.internal.util.HanziToPinyin;
import com.android.internal.util.HanziToPinyin.Token;
import com.android.phone.CallLogAsync;
import android.text.InputFilter;
import android.text.Spanned;
import android.database.ContentObserver;

import com.android.contacts.model.ContactsSource;
import com.android.contacts.model.Sources;
import android.content.SharedPreferences;
import java.util.LinkedList;
import java.util.Queue;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.FrameLayout;
/* Dialer search end - xiaodong wang*/
import android.widget.Button;
import android.provider.Telephony;
import com.mediatek.telephony.PhoneNumberFormatUtilEx;
import com.mediatek.telephony.PhoneNumberFormattingTextWatcherEx;

import com.mediatek.CellConnService.CellConnMgr;
//import android.content.IntentFilter;
//import android.content.BroadcastReceiver;

/**
 * Dialer activity that displays the typical twelve key interface.
 */
@SuppressWarnings("deprecation")
public class TwelveKeyDialer extends Activity implements View.OnClickListener, View.OnTouchListener,
        View.OnLongClickListener, View.OnKeyListener, DialogInterface.OnClickListener,
        DialogInterface.OnShowListener, View.OnCreateContextMenuListener,
        AdapterView.OnItemClickListener, TextWatcher  ,InputFilter, ContactsUtils.OnDialListener{
//        DialogInterface.OnShowListener,
//        AdapterView.OnItemClickListener, TextWatcher {
    private static final String EMPTY_NUMBER = "";
    private static final String TAG = "TwelveKeyDialer";

    /** The length of DTMF tones in milliseconds */
    private static final int TONE_LENGTH_MS = 85;

    /** The DTMF tone volume relative to other sounds in the stream */
    private static final int TONE_RELATIVE_VOLUME = 240;

    /** Stream type used to play the DTMF tones off call, and mapped to the volume control keys */
    private static final int DIAL_TONE_STREAM_TYPE = AudioManager.STREAM_MUSIC;

    private EditText mDigits;
    private View mDelete;
    //xingping.zheng add
    private View mAddContact;
//    private MenuItem mAddToContactMenuItem;
    private ToneGenerator mToneGenerator;
    private Object mToneGeneratorLock = new Object();
    private float mDialerSearchTextScale = 1.0f;
    private int mDigitsViewWidth;

    /*Added by lianyu.zhang start*/
    private MenuItem mDialVTCall;
//    private static final int MENU_VT = 4;
    private boolean mIsVTCall = false;
    /*Added by lianyu.zhang start*/

/* Fion add start */
//    private Drawable mDigitsBackground;
//    private Drawable mDigitsEmptyBackground;
/* Fion add end */
    private View mDialpad;
    private LinearLayout mDialpadLayout;
    private View mVoicemailDialAndDeleteRow;
    private View mVoicemailButton;
    private View mDialButton;
/* Fion add start */
    private View mDialEccrow;
    private View mDialButton1;
    private View mDialButton2;
    /*Added by lianyu.zhang start*/
    private View mDialButtonvt;
    /*Added by lianyu.zhang end*/
    private View mDialButtonECC;
    private View mDialButtonMsg;
//    private View mDialButtonSip;
    private boolean radio1_on ;	
    private boolean radio2_on;		
/* Fion add end */

/* Dialer search begin - xiaodong wang */
    private Paint mDigitsPaint;
    private float maxDigitsSize;
    private float minDigitsSize;
    private float currDigitsSize;
    private boolean mPressDelete;
    private String mDigitString;
    private View mDialSim1Button;
    private View mDialSim2Button;
    private View mDialNumButton;
    private View mDialpadButton;
    private View mHideDialpadButton;
    private View mShowDialpad;
    private TextView mShowDialEditText;
    private View mShowDialpadButton;
    private View mAddToContactOnDigitButton;
    private View mAddToContactButton;
    private View mDeleteOnDigitButton;
    private Context mContext;
    private ContactsPreferences mContactsPrefs;
    private int mDisplayOrder;
    private int mSortOrder;
    private InputMethodManager imeMgr; //Used to check whether IME keyboard is turned on
/* Dialer search end - xiaodong wang */

    // xingping.zheng add CR:127118
    private boolean mUseParentIntent = false;
    public  boolean mIsForeground = false;
    // xingping.zheng add CR:127330
    private Dialog mCanDismissDialog;
    private ListView mDialpadChooser;
    private DialpadChooserAdapter mDialpadChooserAdapter;
    //Member variables for dialpad options
    private MenuItem m2SecPauseMenuItem;
    private MenuItem mWaitMenuItem;
    private static final int MENU_ADD_CONTACTS = 1;
    private static final int MENU_2S_PAUSE = 2;
    private static final int MENU_WAIT = 3;
    private static final int MENU_SPEED_DIAL = 4; // mtk80909 for Speed Dial
    private static final int MENU_SEND_MESSAGE =5;

    private static final int MSG_UPDATE_SIM_ASSOCIATION = 0;
    private static final int MSG_EXIT = 1;
    private static final int UPDATE_SIM_ASSOCIATION_DELAY = 500;

/* Fion add start */
    public static final int DEFAULT_SIM = 2; /* 0: SIM1, 1: SIM2 */
/* Fion add end */

    // Last number dialed, retrieved asynchronously from the call DB
    // in onCreate. This number is displayed when the user hits the
    // send key and cleared in onPause.
    CallLogAsync mCallLog = new CallLogAsync();
    private String mLastNumberDialed = EMPTY_NUMBER;
    private String mInput;

/* Fion add start */
    public static final int MODE_SIM1_ONLY = 1;
    public static final int MODE_SIM2_ONLY = 2;    
    public static final int MODE_DUAL_SIM = 3;
/* Fion add end */

    static final int SUMMARY_ID_COLUMN_INDEX = 0;//80794
    static final int SUMMARY_HAS_PHONE_COLUMN_INDEX = 1;//80794
    private static final int DELETE_TOKEN = 1;//80794
    
	private static final Uri mIccUri = Uri.parse("content://icc/adn/");// 80794
	private static final Uri mIccUri1 = Uri.parse("content://icc/adn1/");
	private static final Uri mIccUri2 = Uri.parse("content://icc/adn2/");
    
    public static final int MMI_DIALOG               = 1;
    public static final int IMEI_DIALOG              = 2;
    public static final int ADN_DIALOG               = 3;
    public static final int VOICE_MAIL_DIALOG        = 4;
    public static final int MISSING_VOICEMAIL_DIALOG = 5; 
    public static final int ERROR_POWER_OFF_DIALOG   = 6;
    public static final int ADD_TO_CONTACT_DIALOG	 = 10;
    protected Dialog mIMEIDialog;
    protected Dialog mMMIDialog;
    protected Dialog mADNDialog;
    protected Dialog mVoiceMailDialog;
    protected Dialog mMissingVoicemailDialog;
    protected Dialog mRadioOffAlertDialog;
    protected Bundle mDialogBundle;
    protected boolean mIsFromADN = false;
    // determines if we want to playback local DTMF tones.
    private boolean mDTMFToneEnabled;
    /* added by xingping.zheng start */
    protected HandlerThread mSimAssociationWorkerThread;
    protected SimAssociationHandler mSimAssociationHandler;
    protected HashMap<String, ArrayList> mSimAssociationMaps = new HashMap<String, ArrayList>();
    /* added by xingping.zheng end */
    // Vibration (haptic feedback) for dialer key presses.
    private HapticFeedback mHaptic = new HapticFeedback();

    /** Identifier for the "Add Call" intent extra. */
    static final String ADD_CALL_MODE_KEY = "add_call_mode";

    /*Added by lianyu.zhang start*/
    /** Identifier for the "VT call" intent extra. 
     * if the current call is VT, the value of extra is true
     * else is false
     * */
    static final String IS_VT_CALL = "is_vt_call";
    /*Added by lianyu.zhang end*/

    private static final int BAD_EMERGENCY_NUMBER_DIALOG = 0;
    
    private static final int New_TONE_GENERATOR = 101;
    private static final int LISTEN_PHONE_STATES = 102;
    private static final int LISTEN_PHONE_NONE_STATES = 103;
    private static final int RELEASE_TONE_GENERATOR = 104;
    
    private String mLastNumber; // last number we tried to dial. Used to restore error dialog.
    private int mPreviousCallState1 = TelephonyManager.CALL_STATE_IDLE;
    private int mPreviousCallState2 = TelephonyManager.CALL_STATE_IDLE;    
    /**
     * Identifier for intent extra for sending an empty Flash message for
     * CDMA networks. This message is used by the network to simulate a
     * press/depress of the "hookswitch" of a landline phone. Aka "empty flash".
     *
     * TODO: Using an intent extra to tell the phone to send this flash is a
     * temporary measure. To be replaced with an ITelephony call in the future.
     * TODO: Keep in sync with the string defined in OutgoingCallBroadcaster.java
     * in Phone app until this is replaced with the ITelephony API.
     */
    static final String EXTRA_SEND_EMPTY_FLASH
            = "com.android.phone.extra.SEND_EMPTY_FLASH";

    /** Indicates if we are opening this dialer to add a call from the InCallScreen. */
    private boolean mIsAddCallMode;

    //Indicates which sim missing voicemail number
    private int mSimNo = -1;

    /* added by xingping.zheng start */
    //private static final int MAX_SIM_ID = 99;
    //private int[] mSlotsMap = new int[MAX_SIM_ID+1];
//    private SimAssociationQueryHandler mSimAssociationQueryHandler;
    private Dialog mCallSelectionDialog;
    private Dialog mCallSelectionDialogOther;
    private Dialog mAddContactDialog;
    private StatusBarManager mStatusBarMgr;
    private Dialog mTurnOnSipDialog;
    private Dialog mTurnOn3GServiceDialog;
    private CellConnMgr mCellConnMgr;
    private int mSlot;
    private String mNumber;
    /* added by xingping.zheng end   */

    // mtk80909 for Speed Dial
    private MenuItem mSpeedDialManagement;
    
    private final char[] CHARACTERS = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '#', '*','+','-','(',')','/',',',';',' ',
        };

/* Dialer search begin - xiaodong wang */
    // Call log data column index
    static final int ID_COLUMN_INDEX 					= 0;
    static final int NUMBER_COLUMN_INDEX 				= 1;
    static final int DATE_COLUMN_INDEX 					= 2;
    static final int DURATION_COLUMN_INDEX 				= 3;
    static final int CALL_TYPE_COLUMN_INDEX 			= 4;
    static final int CALLER_NAME_COLUMN_INDEX 			= 5;
    static final int CALLER_NUMBERTYPE_COLUMN_INDEX 	= 6;
    static final int CALLER_NUMBERLABEL_COLUMN_INDEX 	= 7;
    static final int CALLER_SIMID_INDEX 				= 8;
    // Contacts data column index
    static final int CONTACTS_ID_COLUMN_INDEX 			= 0;
    static final int CONTACTS_DISPLAY_NAME 				= 1;
    static final int CONTACTS_PHOTO_ID_COLUMN_INDEX 	= 2;
    static final int CONTACTS_HAS_PHONE_COLUMN_INDEX 	= 3;
    // Phone data column index
    public static final int PHONE_CONTACT_ID_INDEX 	= 1;
    public static final int TYPE_INDEX       		= 2;
    public static final int NUMBER_INDEX     		= 3;
    public static final int LABEL_INDEX      		= 4;
    public static final int PHONE_NAME_INDEX       	= 5;
    public static final int PHOTO_INDEX      		= 6;
    // Dialer search result column index
    public static final int NAME_LOOKUP_ID_INDEX 		= 0;
    public static final int CONTACT_ID_INDEX 			= 1;
	public static final int CALL_LOG_DATE_INDEX			= 2;
	public static final int CALL_LOG_ID_INDEX 			= 3;
	public static final int CALL_TYPE_INDEX       		= 4;
	public static final int SIM_ID_INDEX				= 5;
	public static final int VTCALL						= 6;
	public static final int INDICATE_PHONE_SIM_INDEX	= 7;
	public static final int CONTACT_STARRED_INDEX		= 8;
	public static final int PHOTO_ID_INDEX      		= 9;
	public static final int SEARCH_PHONE_TYPE_INDEX 	= 10;
	public static final int NAME_INDEX       			= 11;
	public static final int SEARCH_PHONE_NUMBER_INDEX 	= 12;
	public static final int CONTACT_NAME_LOOKUP_INDEX	= 13;
	public static final int DS_MATCHED_DATA_OFFSETS 	= 14;
    public static final int DS_MATCHED_NAME_OFFSETS 	= 15;

    // Dialer search result divider
    public static final int DS_MATCHED_DATA_DIVIDER		= 3;
    // Menu item index
    //For contacts
    static final int MENU_ITEM_CONTACTS_CALLLOG_VIEW_CONTACT = 1;
    static final int MENU_ITEM_CONTACTS_EDIT_CONTACT = 2;
	static final int MENU_ITEM_CONTACTS_SHARE_CONTACT = 3;
    static final int MENU_ITEM_CONTACTS_TOGGLE_STAR = 4;
    static final int MENU_ITEM_CONTACTS_DELETE_CONTACT = 5;    
    //For call log
    static final int MENU_ITEM_CALLLOG_VOICE_CALL = 6;
    static final int MENU_ITEM_CALLLOG_VT_CALL = 7;
	static final int MENU_ITEM_CALLLOG_SIP_CALL = 8;
    static final int MENU_ITEM_CALLLOG_SEND_SMS = 9;
    static final int MENU_ITEM_CALLLOG_EDIT_BEFORE_CALL = 10;
    static final int MENU_ITEM_CALLLOG_ADD_TO_CONTACTS = 11;
    static final int MENU_ITEM_CALLLOG_DELETE_CALL_LOG = 12;

	static final int MENU_ITEM_CONTACTS_CALLLOG_ADD_SPEED_DIAL = 13;
    static final int MENU_ITEM_OWNER_EDIT = 14;
    static final int MENU_ITEM_OWNER_DELETE = 15;
    

    private static final int SUBACTIVITY_NEW_OWNER_INSERT = 7;
    public static final String NEW_OWNER_INFO = "new_owner_info";
    // search mode
   	public static final int DIALER_SEARCH_MODE_ALL = 0;
   	public static final int DIALER_SEARCH_MODE_NUMBER = 1;
   	private static final int MAX_DIGITS_DISPLAY_LENGTH = 20;	// Won't format number and resize digits any more if longer than 20 digits

    private QueryHandler mQueryHandler;
    private DialerSearchListAdapter mAdapter;
    private static final int QUERY_TOKEN = 30;
    private ListView mList;
//    private TextView mUserTips;
    private TextView mMatchedResultsCount;
    private String mResultCountsStr;
    private View mAddToContacts;
    private boolean mBottomIsVisiable = false;
    private FrameLayout mListLayout;
    private FrameLayout.LayoutParams dsListParam;
    private CharSequence[] mLabelArray;
    private Drawable mDrawableIncoming;
    private Drawable mDrawableOutgoing;
    private Drawable mDrawableMissed;
    private ContactPhotoLoader mPhotoLoader;
    private LayoutInflater mInflater;
    private ListView mDialNumberList;
    private Cursor mDialerSearchCursor;
    private int mDialerSearchCursorCount;
    private static final boolean DEBUG = true;
    private Uri mSelectedContactUri;
    private String addToContactNumber;
    private boolean mPressQuickContactBadge = false;
    private boolean mDialed = false;
    private Dialog mDialNumberListDlg;
    private Dialog mAddToContactDlg;
    private Dialog mDeleteContactDlg;
    private int mPosition;
    private String mEditNumber;
    private String mVoiceMailNumber;
    private String mVoiceMailNumber2;
    private boolean mSearchNumberOnly = false;
    private boolean mChangeInMiddle = false;
    private ContentObserver mContentObserver;
    private AlertDialog mDialSimListDlg;
    private int mPrevQueryDigCnt;
    private boolean mQueryComplete = false;
    private Queue<Integer> mSearchNumCntQ;
    private int noResultDigCnt;
    private boolean noMoreResult;
    private ContactsUtils.SIMInfoWrapper mSimInfoWrapper;
    boolean mShowSimIndicator = false;
    
    private boolean mFirstLaunchCompleted = false;
    
    //mtk80908 add begin.
    private static int delCount=0;//definition for deleting contacts. Used for update UI in UI thread.
    private static final int DS_MSG_CONTACTS_DELETE_CHANGED = 1000;
    private static final int DS_MSG_DELAY_TIME = 1000;
    private final Handler mDBHandlerForDelContacts = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case DS_MSG_CONTACTS_DELETE_CHANGED: {
            	if (delCount > 0) {
	            	if (mDigits != null) {
			            mDigits.getText().clear();
			            if (mShowDialEditText != null) 
			            	mShowDialEditText.setText(null);
			            if (mBottomIsVisiable) {
		    		    	mList.removeFooterView(mAddToContacts);
		    		    	mBottomIsVisiable = false;
		    		    }
			            log("mPhoneStateListener startQuery");
				    	startQuery(null, DIALER_SEARCH_MODE_ALL);
	            	 }
	            	delCount = 0;
	            }
            	return;
            }
            	default:
            		return;
            }
        }
    };
    //mtk80908 end
    
    private static final int TOKEN_DIAL = 0;
    private static final int TOKEN_SPEEDDIAL = 1;
    //private final BroadcastReceiver mSimIndicatorReceiver = new SimIndicatorBroadcastReceiver();
	
    static final String[] CONTACTS_PROJECTION = new String[] {
        Contacts._ID,                       // 0
        Contacts.DISPLAY_NAME,      		// 1
        Contacts.PHOTO_ID,                  // 2
        Contacts.HAS_PHONE_NUMBER,          // 3
    };

    private static final String[] PROJECTION_PHONE = {
    	CommonDataKinds.Phone._ID,              // 0
    	CommonDataKinds.Phone.CONTACT_ID,       // 1
    	CommonDataKinds.Phone.TYPE,             // 2
    	CommonDataKinds.Phone.NUMBER,           // 3
    	CommonDataKinds.Phone.LABEL,            // 4
    	CommonDataKinds.Phone.DISPLAY_NAME,     // 5
    	CommonDataKinds.Phone.PHOTO_ID			// 6
    };

     private static final String[] DIALER_SEARCH_PROJECTION = {
    	 DialerSearch.NAME_LOOKUP_ID,
         DialerSearch.CONTACT_ID ,
    	 DialerSearch.CALL_DATE,
    	 DialerSearch.CALL_LOG_ID,
         DialerSearch.CALL_TYPE,
         DialerSearch.SIM_ID,
         DialerSearch.INDICATE_PHONE_SIM,
         DialerSearch.CONTACT_STARRED,
         DialerSearch.PHOTO_ID,
         DialerSearch.SEARCH_PHONE_TYPE,
         DialerSearch.NAME, 
       	 DialerSearch.SEARCH_PHONE_NUMBER,
       	 DialerSearch.CONTACT_NAME_LOOKUP,
       	 DialerSearch.MATCHED_DATA_OFFSETS,
    	 DialerSearch.MATCHED_NAME_OFFSETS
    };

    static final String[] CALL_LOG_PROJECTION = new String[] {
        Calls._ID,
        Calls.NUMBER,
        Calls.DATE,
        Calls.DURATION,
        Calls.TYPE,
        Calls.CACHED_NAME,
        Calls.CACHED_NUMBER_TYPE,
        Calls.CACHED_NUMBER_LABEL,
        Calls.SIM_ID,
    };
    public static final class DialerSearchResultItemViews {
    	QuickContactBadge iconView;		// photo image, call type, sim card
        TextView nameView;
        TextView labelView;
        TextView numberView;
        ImageView callTypeView;
//        TextView dateView;
        ImageView expandTypeView;
        TextView operatorNameView;
        TextView dateView;
    }
    
    public static final class DialerSearchExpandInfo {
    	boolean mHasCallLog;
    	int		mContactId;
    	long	mCallDate;
    	int 	mSimId;
    	String	mCallNumber;
    	int 	mIsVtCall;
    	long    mPhotoId;
    	int		mSimIndicator;
    }
    private static final HashMap<String, String[]> dialerKeyMap; 
    static {
    	dialerKeyMap = new HashMap<String, String[]>();
    	String zeroList[] = {"0"};
    	String oneList[] = {"1"};
    	String twoList[] = {"2", "a", "b", "c"};
	    String threeList[] = {"3", "d", "e", "f"};
	    String fourList[] = {"4", "g", "h", "i"};
	    String fiveList[] = {"5", "j", "k", "l"};
	    String sixList[] = {"6", "m", "n", "o"};
	    String sevenList[] = {"7", "p", "q", "r", "s"};
	    String eightList[] = {"8", "t", "u", "v"};
	    String nineList[] = {"9", "w", "x", "y", "z"};    	
	    dialerKeyMap.put("*", null);
	    dialerKeyMap.put("#", null);
	    dialerKeyMap.put("0", zeroList);
	    dialerKeyMap.put("1", oneList);
	    dialerKeyMap.put("2", twoList);
	    dialerKeyMap.put("3", threeList);
	    dialerKeyMap.put("4", fourList);
	    dialerKeyMap.put("5", fiveList);
	    dialerKeyMap.put("6", sixList);
	    dialerKeyMap.put("7", sevenList);
	    dialerKeyMap.put("8", eightList);
	    dialerKeyMap.put("9", nineList);    	
    }
    /* added by xingping.zheng start */
    public class SimAssociationHandler extends Handler {

        private static final String TAG = "SimAssociationHandler";

        private boolean mUpdating = false;

        public SimAssociationHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            //super.handleMessage(msg);
            String number = null;
            int id = -1;
            ArrayList associateSims = null;
            boolean exist = false;
            switch(msg.what) {
                case MSG_UPDATE_SIM_ASSOCIATION:
                    log("MSG_UPDATE_SIM_ASSOCIATION, mUpdating = "+mUpdating);
                    if(mUpdating)
                        break;
                    mUpdating = true;
                    mSimAssociationMaps.clear();
                    Cursor cursor = getContentResolver().query(
                            Data.CONTENT_URI,
                            new String[] {
                                    Data.DATA1, Data.SIM_ID
                            },
                            Data.MIMETYPE + "='" + CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                                    + "' AND (" + Data.SIM_ID + ">0)", null, null);
                    if(cursor != null && cursor.moveToFirst()) {
                        try {
                            do {
                                number = cursor.getString(0);
                                id = cursor.getInt(1);
                                log("number = "+" id = "+id);
                                associateSims = mSimAssociationMaps.get(number);
                                if(associateSims == null) {
                                    associateSims = new ArrayList();
                                    mSimAssociationMaps.put(number, associateSims);
                                }
                                exist = false;
                                for(int i=0; i<associateSims.size(); i++) {
                                    if(((Integer)associateSims.get(i)).intValue() == id) {
                                        exist = true;
                                        break;
                                    }
                                }
                                if(!exist)
                                    associateSims.add(Integer.valueOf(id));
                                log("associateSims = "+associateSims);
                            } while(cursor.moveToNext());
                        } catch(Exception e) {
                            log("exception");
                        } 
                    }
                    if(cursor != null)
                        cursor.close();
                    mUpdating = false;
                    log("MSG_UPDATE_SIM_ASSOCIATION END");
                    break;
                case MSG_EXIT:
                    log("MSG_EXIT");
                    getLooper().quit();
                    break;
            }
        }

        void log(String msg) {
            Log.d(TAG, msg);
        }
    }
    /* added by xingping.zheng end */

/* Dialer search end - xiaodong wang */
/* Dialer search begin - xiaodong wang */
    private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id)
        {
    		log("twelvekeydialer onItemClick : parent " + parent + ", view " + v + ",position" + position + "id:" + id);
        	final DialerSearchResultItemViews views = (DialerSearchResultItemViews) v.getTag();
        	log("twelvekeydialer onItemClick : views " + views);
//        	CharSequence numberChars = views.numberView.getTag();
    		String number = (String)views.numberView.getTag();
    		log("twelvekeydialer onItemClick : number " + number);
    		
            if (!TextUtils.isEmpty(number)) {
        		//Number check
        		if (number.equals(CallerInfo.UNKNOWN_NUMBER) || 
        				number.equals(CallerInfo.PRIVATE_NUMBER) ||
        				number.equals(CallerInfo.PAYPHONE_NUMBER)) {
        			//Do not deal with special number.
        			return;
        		}
                if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
            	DialerSearchExpandInfo expandInfo = (DialerSearchExpandInfo)views.expandTypeView.getTag();
            	if (expandInfo.mHasCallLog && expandInfo != null) {
        			log("[onItemClick][CallLogNumber]Voice Call. originalSim:" + expandInfo.mSimId);
//        			ContactsUtils.dial(TwelveKeyDialer.this, number, ContactsUtils.DIAL_TYPE_AUTO, (long)expandInfo.mSimId, null);
        			onDialButtonClickIntWithSimId(number, expandInfo.mSimId);
            	} else {
            		log("[onItemClick][ContactsNumber]Voice Call.");
//            		ContactsUtils.dial(TwelveKeyDialer.this, number, ContactsUtils.DIAL_TYPE_AUTO, null);
            		onDialButtonClickInt(number);
            	}

                } else {
                    dialButtonPressedextInt(number, DEFAULT_SIM);
                }
            }
        }
    };

	private final class DialerSearchListAdapter extends CursorAdapter
		implements View.OnClickListener, OnScrollListener {
        Context mAdapterContext;
        Cursor mDialNumberCursor; 
        AlertDialog mDialNumberDialog;
        private static final boolean DBG = true; 
//        View mProcessView;	// prevent bindview invoked repeatedly
        
    	DialerSearchListAdapter(Context context) {
//    		super(context, R.layout.dialer_search_list_item_layout, null);
    		super(context, null, false);
    		log("twelvekeydialer: construct dialer search list adapter");
            /**
             * Reusable char array buffers.
             */
    		mDrawableIncoming = getResources().getDrawable(
                    R.drawable.ic_call_log_list_incoming_call);
            mDrawableOutgoing = getResources().getDrawable(
                    R.drawable.ic_call_log_list_outgoing_call);
            mDrawableMissed = getResources().getDrawable(
                    R.drawable.ic_call_log_list_missed_call);
            mLabelArray = getResources().getTextArray(com.android.internal.R.array.phoneTypes);  
            mAdapterContext = context;
    	}
    	
        public void onClick(View view) {
        	log("DialerSearchListAdapter onClick : view " + view);
        	switch (view.getId()) {
	            case R.id.image_call_type_sim_card_icon:{
	            	addToContactNumber = (String)view.getTag();
	            	if (addToContactNumber != null) {
	            		onAddContacButtonClickInt(addToContactNumber);
	            	}
	            	return;
	            }
	            case R.id.expand_icon: {
	            	if (mBottomIsVisiable) {
	    		    	mList.removeFooterView(mAddToContacts);
	    		    	mBottomIsVisiable = false;
	    		    }
		            DialerSearchExpandInfo expandInfo = (DialerSearchExpandInfo)view.getTag();

		            if (expandInfo.mHasCallLog) {

		    			Intent intent = new Intent(TwelveKeyDialer.this,CallDetailActivity.class);
						Bundle bundle = new Bundle();
						bundle.putLong("com.android.contacts.date_start", expandInfo.mCallDate);
						bundle.putLong("com.android.contacts.date_end", expandInfo.mCallDate);
						bundle.putString("com.android.contacts.number", expandInfo.mCallNumber);
						bundle.putLong("com.android.contacts.simid", expandInfo.mSimId);
						bundle.putLong("com.android.contacts.photoid", expandInfo.mPhotoId);
						bundle.putInt("com.android.contacts.issimind", expandInfo.mSimIndicator);
						intent.putExtras(bundle);
						startActivity(intent);
		            } else {
			    		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, expandInfo.mContactId);
		            	Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
		            	intent.putExtra(RawContacts.INDICATE_PHONE_SIM, expandInfo.mSimId);
//		            	intent.putExtra("owner_id", PhoneOwner.getInstance() == null ? 
//								(long)-1 : PhoneOwner.getInstance().getOwnerID());
//					    intent.putExtra("current_contact_id", id);
						startActivity(intent);		            	
		            }
	            	return;
	            }
	            default:
	            	return;
        	}
        }

        private String getModifiedDisplayName(String inputNumber, int simId) {
        	String callerName = null;
        	String number = tripNonDigit(inputNumber);
        	log("mVoiceMailNumber: " + mVoiceMailNumber + "mVoiceMailNumber2: " + mVoiceMailNumber2);
			if (number == null) {
				return null;
			}
            if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
            	callerName = getString(R.string.unknown);
            } else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
            	callerName = getString(R.string.private_num);
            } else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
            	callerName = getString(R.string.payphone);
            } else if (number.equals(mVoiceMailNumber) || number.equals(mVoiceMailNumber2)) {
               	if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
            		if (simId == Phone.GEMINI_SIM_1 && number.equals(mVoiceMailNumber)) {
            			callerName = getString(R.string.voicemail);
            		} else if (simId == Phone.GEMINI_SIM_2 && number.equals(mVoiceMailNumber2)) {
            			callerName = getString(R.string.voicemail);
            		} else {
            			callerName = getString(R.string.unknown);
            		}
            	} else {
            		number = getString(R.string.voicemail);
            	}
            } else {
            	callerName = getString(R.string.unknown);
            }
            log("changed callerName: " + callerName);
            return callerName;
        }
        
        @Override
        public void bindView(View view, Context context, Cursor c) {
        	boolean isCalllog = false;
        	boolean isContact = false;
        	boolean isContactCalllog = false;
        	boolean isVoicemailNumber = false;
        	boolean isSpecialNumber = false;
        	if (c == null)
        		return;
        	int cursorCount = c.getCount();
        	
        	int bgSpanColor = context.getResources().getThemeColor("dialer_search_result_higlight_background");
	        if (bgSpanColor == 0){
	        	bgSpanColor = Color.parseColor("#FF6D9222");
        	}
	        
	        int fgSpanColor = context.getResources().getThemeColor("dialer_search_result_text_foreground");
	        if (fgSpanColor == 0){
	        	fgSpanColor = Color.WHITE;
        	}
             
        	log("[bindView] mDialerSearchCursorCount: " + mDialerSearchCursorCount);
        	log("[bindView] cursor count: " + cursorCount);
        	DialerSearchResultItemViews views = (DialerSearchResultItemViews)view.getTag();
        	final String callerName = c.getString(NAME_INDEX);
        	final int contactId = c.getInt(CONTACT_ID_INDEX);
        	final int callLogId = c.getInt(CALL_LOG_ID_INDEX);
        	// if search contact name, the table will connect the first saved number & type for the search item, so get by first_xxx_index
        	// otherwise, search number, could get info by search_xxx_index
        	// TO Check 
    		final String number = c.getString(SEARCH_PHONE_NUMBER_INDEX);
    		String formatNumber = null;//PhoneNumberFormatUtilEx,formated number
    		final int phoneType = c.getInt(SEARCH_PHONE_TYPE_INDEX);
        	int digitLen = mDigits.getText().length();
        	String mNumberMatchedOffsets = null;
        	String mNameMatchedOffsets = null;
        	int mDsMatchedDataOffsetIdx = c.getColumnIndex(DialerSearch.MATCHED_DATA_OFFSETS);
        	log("[bindView]mDsMatchedDataOffsetIdx:"+mDsMatchedDataOffsetIdx);
        	if(mDsMatchedDataOffsetIdx != -1){
	        	mNumberMatchedOffsets = c.getString(mDsMatchedDataOffsetIdx);
	       		log("[bindView]number offset size: " + (mNumberMatchedOffsets == null?-1:mNumberMatchedOffsets.length()));
        	}

        	mDsMatchedDataOffsetIdx = c.getColumnIndex(DialerSearch.MATCHED_NAME_OFFSETS);
       		if (mDsMatchedDataOffsetIdx != -1) {
	        	mNameMatchedOffsets = c.getString(mDsMatchedDataOffsetIdx);
	        	log("[bindView]name offset size: " + (mNameMatchedOffsets == null?-1:mNameMatchedOffsets.length()));
       		}
        	if (contactId > 0 && callLogId == 0) {
        		isContact = true;
        	} else if (contactId > 0 && callLogId > 0) {
        		// must be put before next check since the number may be pointed to call log number
        		isContactCalllog = true;
        	} else if (callLogId > 0) {
        		isCalllog = true;
        	} else {
        		throw new IllegalArgumentException("Wrong database information for contact id or call log id: "+contactId+", "+callLogId);
        	}
        	
        	log("number: "+number+" callerName: "+callerName+" type:"+phoneType);
        	log("mVoiceMailNumber:"  + mVoiceMailNumber + " || mVoiceMailNumber2:" + mVoiceMailNumber2);
    		//Number check//
    		if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
    			formatNumber = getString(R.string.unknown);
    			isSpecialNumber = true;
    		} else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
    			formatNumber = getString(R.string.private_num);
    			isSpecialNumber = true;
    		} else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
    			formatNumber = getString(R.string.payphone);
    			isSpecialNumber = true;
    		} else if (number.equals(mVoiceMailNumber)
    				|| number.equals(mVoiceMailNumber2)) {
    			formatNumber = getString(R.string.voicemail);
    			isVoicemailNumber = true;
    		} else {
    			formatNumber = PhoneNumberFormatUtilEx.formatNumber(number);
    		} 
        	/* Initialize view state */
        	if(views.expandTypeView != null)
        		views.expandTypeView.setVisibility(View.VISIBLE);
        	views.nameView.setVisibility(View.VISIBLE);
        	views.labelView.setVisibility(View.VISIBLE);
    		
    		if (isContact || isContactCalllog) {
            	CharSequence labelText = CommonDataKinds.Phone.getTypeLabel(context.getResources(), phoneType, null);
            	if (!TextUtils.isEmpty(labelText) && formatNumber != null) {	// Search Chinese name, number is null
            		views.labelView.setText(labelText);
            	}
        	} else {
        		views.labelView.setText(null);
        	}

        	/* Set sim card information */
            int simId = c.getInt(SIM_ID_INDEX);
            // Store away the number so we can call it directly if you click on the call icon
            views.numberView.setTag(number);
            //mtk just use the id R.id.call_icon to store the sim id
//            views.numberView.setTag(R.id.call_icon, simId);
            //MTK
            views.numberView.setEnabled(true);

            views.nameView.setGravity(Gravity.CENTER_VERTICAL);
            
            if (isVoicemailNumber || isSpecialNumber) {
            	views.nameView.setText(formatNumber);
        		views.numberView.setText(null);
        		views.labelView.setText(null);
            } else if (digitLen == 0) {
        		if (callerName == null) {
        			views.nameView.setText(formatNumber);
            		views.numberView.setText(null);
            		views.labelView.setText(null);
        		} else {
        			views.nameView.setText(callerName);
            		views.numberView.setText(formatNumber);
        		}
        	} else {
	        	/* Set highlight for displayed name */
	        	if (callerName != null) {	// && !isNumberHilite && (name_type == 11 || name_type == 12)) {
					if(mNameMatchedOffsets !=null){
		        		SpannableStringBuilder style = new SpannableStringBuilder(callerName);
						int mHighlitePosNum = mNameMatchedOffsets.length();
						log("[bindView] count: " + mHighlitePosNum);						
						for(int i=0; i < mHighlitePosNum; i+=DS_MATCHED_DATA_DIVIDER){
							log("[bindView] count: " + (i+1) + " || " + (int)mNameMatchedOffsets.charAt(i)
							      + " - " + (int)mNameMatchedOffsets.charAt(i+1) + " - " + (int)mNameMatchedOffsets.charAt(i+2));
							style.setSpan(new BackgroundColorSpan(bgSpanColor), 
									(int)mNameMatchedOffsets.charAt(i), (int)mNameMatchedOffsets.charAt(i+1)+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							style.setSpan(new ForegroundColorSpan(fgSpanColor), 
									(int)mNameMatchedOffsets.charAt(i), (int)mNameMatchedOffsets.charAt(i+1)+1, Spannable.SPAN_MARK_MARK);
						}
			 			views.nameView.setText(style);
					}else{
						views.nameView.setText(callerName);
					}
		            /* Set highlight for displayed number */
		        	if (formatNumber != null ) {
		        		if(mNumberMatchedOffsets !=null){
							SpannableStringBuilder style = new SpannableStringBuilder(formatNumber);
							ArrayList<Integer> numberHighliteOffset = adjustHighlitePosForHyphen(formatNumber,mNumberMatchedOffsets);
							if(numberHighliteOffset != null){
								style.setSpan(new BackgroundColorSpan(bgSpanColor), 
										numberHighliteOffset.get(0), numberHighliteOffset.get(1)+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
								style.setSpan(new ForegroundColorSpan(fgSpanColor), 
										numberHighliteOffset.get(0), numberHighliteOffset.get(1)+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
								views.numberView.setText(style);
							}
		        		}else{
		        			views.numberView.setText(formatNumber);
		        		}
		        		
		        	} else {
		        		log("[bindView]caller number is null");
		        		views.numberView.setText(null);
		        		views.labelView.setText(null);
		        	}
	        	} else {
	        		log("[bindView]callName is null");
	        		views.numberView.setText(null);
	        		views.labelView.setText(null);
		        	if (formatNumber != null ) {
		        		if(mNumberMatchedOffsets !=null){
							SpannableStringBuilder style = new SpannableStringBuilder(formatNumber);
							ArrayList<Integer> numberHighliteOffset = adjustHighlitePosForHyphen(formatNumber,mNumberMatchedOffsets);
							if(numberHighliteOffset != null){
								style.setSpan(new BackgroundColorSpan(bgSpanColor), 
										numberHighliteOffset.get(0), numberHighliteOffset.get(1)+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
								style.setSpan(new ForegroundColorSpan(fgSpanColor), 
										numberHighliteOffset.get(0), numberHighliteOffset.get(1)+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
								views.nameView.setText(style);
							}
		        		}else{
		        			views.nameView.setText(formatNumber);
		        		}
		        		
		        	} else {
		        		views.nameView.setText(null);
		        	}
                }
        	}

            if (isCalllog) {
            	//Now we always have a number NO matter whether callLog or not.TO check here.
                if (!isSpecialNumber) {
                    views.iconView.setTag(number);
                    views.iconView.setOnClickListener(this);
                }
            } else {
            	views.iconView.setOnClickListener(new OnClickListener(){
                    public void onClick(View v) {
                    	((QuickContactBadge)v).onClick(v);
                    	mPressQuickContactBadge = true;	// Set flag to prevent initialize search result when invoking onPause()
                    }
            	});
            	views.iconView.setTag(null);
                final String lookupKey = c.getString(CONTACT_NAME_LOOKUP_INDEX);
                views.iconView.setExcludeMimes(new String[] { Contacts.CONTENT_ITEM_TYPE });
                views.iconView.assignContactUri(Contacts.getLookupUri(contactId, lookupKey));
            }
            
            
            long photoId = 0;
            if (!c.isNull(PHOTO_ID_INDEX)) {
                photoId = c.getLong(PHOTO_ID_INDEX);
            }
            QuickContactBadge iconViewToUse = views.iconView;
            int indicate = c.getInt(INDICATE_PHONE_SIM_INDEX);
            int simSlotId = -1;
            if(indicate > 0)
            	simSlotId = mSimInfoWrapper.getSimSlotById(indicate);
			// have to init photoid event it's 0, other wise the picture will mess up - ALPS00137770
           	mPhotoLoader.loadPhoto(iconViewToUse, photoId,simSlotId);

			ImageView callTypeImage = views.callTypeView;
			DialerSearchExpandInfo expandInfo = new DialerSearchExpandInfo();
			callTypeImage.setVisibility(View.VISIBLE);
            if (views.dateView != null)
				views.dateView.setVisibility(View.VISIBLE);
            if (views.operatorNameView != null)
            	views.operatorNameView.setVisibility(View.VISIBLE);			
            // the search result number is call number, should display call type, 
            //otherwise, it's the database join result for contact name or other number of the same contact
			if(formatNumber != null)
				log("[bindView]tripNonDialableDigit(number)" + tripNonDialableDigit(formatNumber));
			if(number !=null)
				log("[bindView]number" + number);
			
			//In view_dialer_search,if it can find number in data table, then use it SEARCH_PHONE_NUMBER;
			//if not, use callLog number instead.
			//So DONOT need to check the number and formatNumber. 
			if(isCalllog || isContactCalllog) {
	        	/* Set icon image by different call type and sim card */
	            int callType = c.getInt(CALL_TYPE_INDEX);
	            int isVTCall = c.getInt(VTCALL);
	            long callDate = c.getLong(CALL_LOG_DATE_INDEX);
//	            simId
	            log("[bindView]callType: " + callType );
	            if (views.dateView != null) {
		            java.text.DateFormat df = DateFormat.getTimeFormat(TwelveKeyDialer.this);
		            String dateString = df.format(callDate);
		            views.dateView.setText(dateString);
	            }
	            if (simId == ContactsUtils.CALL_TYPE_SIP && views.operatorNameView != null) {
	            	views.operatorNameView.setBackgroundResource(com.mediatek.internal.R.drawable.sim_background_sip);
	            	views.operatorNameView.setPadding(3, 0, 3, 0);
	            	views.operatorNameView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
	            	views.operatorNameView.setText(R.string.sipcall);
	            	views.operatorNameView.setVisibility(View.VISIBLE);
	            } else if (views.operatorNameView != null) {
		            String operatorName = mSimInfoWrapper.getSimDisplayNameById(simId);
		            if (operatorName != null) {
			            int colorType = mSimInfoWrapper.getInsertedSimColorById(simId);
			            if (colorType != -1) {
			            	views.operatorNameView.setBackgroundResource(Telephony.SIMBackgroundRes[colorType]);
			            } else {
			            	views.operatorNameView.setBackgroundResource(com.mediatek.internal.R.drawable.sim_background_locked);
			            }
			            views.operatorNameView.setPadding(3, 0, 3, 0);
			            views.operatorNameView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
			            views.operatorNameView.setText(operatorName);
		            } else {
		            	views.operatorNameView.setVisibility(View.GONE);
		            }
	            }
	            expandInfo.mHasCallLog = true;
	            expandInfo.mCallDate = callDate;
	            expandInfo.mCallNumber = number;
	            expandInfo.mSimId = simId;
	            expandInfo.mIsVtCall = isVTCall;
	            expandInfo.mSimIndicator = indicate;
	            expandInfo.mPhotoId = photoId;

	            // Set the icon
	            switch (callType) {
	                case Calls.INCOMING_TYPE:
	                	log("dialer search bind set INCOMING_TYPE");
//	                    if (!FeatureOption.MTK_GEMINI_SUPPORT) {
//	                    	callTypeImage.setImageDrawable(getResources().getDrawable(R.drawable.contact_calllog_dialerseach_incoming_call));
//	                        break;
//	                    }
	                	if (isVTCall == 1)
	                		callTypeImage.setImageDrawable(getResources().getDrawable(R.drawable.contact_calllog_dialerseach_video_incoming_call));
	                	else
	                		callTypeImage.setImageDrawable(getResources().getDrawable(R.drawable.contact_calllog_dialerseach_incoming_call));
	                    break;
		            case Calls.OUTGOING_TYPE:
//		                if (!FeatureOption.MTK_GEMINI_SUPPORT) {
//		                	callTypeImage.setImageDrawable(getResources().getDrawable(R.drawable.contact_calllog_dialerseach_outing_call));
//		                    break;
//		                }	                	
		            	if (isVTCall == 1)
	                		callTypeImage.setImageDrawable(getResources().getDrawable(R.drawable.contact_calllog_dialerseach_video_outing_call));
	                	else
	                		callTypeImage.setImageDrawable(getResources().getDrawable(R.drawable.contact_calllog_dialerseach_outing_call));
		                break;
		            case Calls.MISSED_TYPE:
//		                if (!FeatureOption.MTK_GEMINI_SUPPORT) {
//		                	callTypeImage.setImageDrawable(getResources().getDrawable(R.drawable.contact_calllog_dialerseach_missing_call));
//		                    break;
//		                }
		            	if (isVTCall == 1)
	                		callTypeImage.setImageDrawable(getResources().getDrawable(R.drawable.contact_calllog_dialerseach_video_missing_call));
	                	else
	                		callTypeImage.setImageDrawable(getResources().getDrawable(R.drawable.contact_calllog_dialerseach_missing_call));
		                break;
		            default:
		            	break;
		        }
        	} else {
        		callTypeImage.setVisibility(View.GONE);
        		if (views.operatorNameView != null)
        			views.operatorNameView.setVisibility(View.GONE);
        		if (views.dateView != null)
        			views.dateView.setVisibility(View.GONE);
	            expandInfo.mHasCallLog = false;
	            expandInfo.mContactId = contactId;
	            expandInfo.mSimId = indicate;
        	}
			if (views.expandTypeView != null)
				views.expandTypeView.setTag(expandInfo);
        }

		@Override
		public void notifyDataSetChanged() {
			log("twelvekeydialer: notifyDataSetChanged");
			super.notifyDataSetChanged();
		}
        
		/*
		 * For dialer search
		 * The caller check number and mNumberMatchedOffsets are not null.
		 */
        private ArrayList<Integer> adjustHighlitePosForHyphen(String number, String mNumberMatchedOffsets){
        	ArrayList<Integer> res = new ArrayList<Integer>();
        	try{
	        	int mHighliteBegin = (int)mNumberMatchedOffsets.charAt(0);
	        	int mHighliteEnd = (int)mNumberMatchedOffsets.charAt(1);
	        	if(mHighliteBegin > mHighliteEnd || mHighliteEnd > number.length())
	        		return null;
	        	for(int i=0;i<=mHighliteBegin;i++){
	        		char c = number.charAt(i);
	        		if( c== '-' || c == ' '){
	        			mHighliteBegin++;
	        			mHighliteEnd++;
	        		}
	        	}
	        	for(int i=mHighliteBegin+1;i<=mHighliteEnd;i++){
	        		char c = number.charAt(i);
	        		if( c== '-' || c == ' '){
	        			mHighliteEnd++;
	        		}
	        	}
	        	log("[Number highlite]mHighliteBegin:" + mHighliteBegin + " | mHighliteEnd:" + mHighliteEnd);
	        	res.add(mHighliteBegin);
	        	res.add(mHighliteEnd);
        	}catch(Exception e){
        		e.printStackTrace();
        		return null;
        	}
        	return res;
        }
        
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
//            LayoutInflater inflater = 
//            	(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            DialerSearchResultItemViews views = new DialerSearchResultItemViews();
            View view;
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
	            view = mInflater.inflate(R.layout.dialer_search_list_item_layout, parent, false);
	            log("twelvekeydialer Gemini: newView, parent: "+parent+"\nview: "+view);
	            
	            views.iconView = (QuickContactBadge) view.findViewById(R.id.image_call_type_sim_card_icon);
	            views.nameView = (TextView) view.findViewById(R.id.caller_name);
	            views.labelView = (TextView) view.findViewById(R.id.label);
	            views.numberView = (TextView) view.findViewById(R.id.number);
	            views.callTypeView = (ImageView) view.findViewById(R.id.call_type_icon);
	            views.expandTypeView = (ImageView) view.findViewById(R.id.expand_icon);
	            views.operatorNameView = (TextView) view.findViewById(R.id.operator_name);
	            views.dateView = (TextView) view.findViewById(R.id.date);
	            ImageView callTypeIconSingle = (ImageView) view.findViewById(R.id.call_type_icon_single);
	            if(callTypeIconSingle != null)
	            	callTypeIconSingle.setVisibility(View.GONE);
	            TextView dateViewSingle = (TextView) view.findViewById(R.id.date_single);
	            if(dateViewSingle != null)
	            	dateViewSingle.setVisibility(View.GONE);
	            views.expandTypeView.setOnClickListener(this);
            } else {
	            view = mInflater.inflate(R.layout.dialer_search_list_item_layout, parent, false);
	            log("twelvekeydialer: newView, parent: "+parent+"\nview: "+view);
	            
	            views.iconView = (QuickContactBadge) view.findViewById(R.id.image_call_type_sim_card_icon);
	            views.nameView = (TextView) view.findViewById(R.id.caller_name);
	            views.labelView = (TextView) view.findViewById(R.id.label);
	            views.numberView = (TextView) view.findViewById(R.id.number);
	            views.callTypeView = (ImageView) view.findViewById(R.id.call_type_icon_single);
	            views.callTypeView.setVisibility(View.VISIBLE);
	            views.dateView = (TextView) view.findViewById(R.id.date_single);
	            views.dateView.setVisibility(View.VISIBLE);
	            views.expandTypeView = (ImageView) view.findViewById(R.id.expand_icon);
	            
	            ImageView callTypeIcon = (ImageView) view.findViewById(R.id.call_type_icon);
	            if(callTypeIcon != null)
	            	callTypeIcon.setVisibility(View.GONE);
	            TextView operatorName = (TextView) view.findViewById(R.id.operator_name);
	            if(operatorName != null)
	            	operatorName.setVisibility(View.GONE);
	            TextView dateView = (TextView) view.findViewById(R.id.date);
	            if(dateView != null)
	            	dateView.setVisibility(View.GONE);
	            
	            views.expandTypeView.setOnClickListener(this);
            }
            view.setTag(views);
            return view;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v;
			log("getView position: "+position+" convertView: "+convertView+" parent:"+parent);
			if (!mDialerSearchCursor.moveToPosition(position)) {
				throw new IllegalStateException("couldn't move cursor to position "+position);
			}
			if (convertView == null) {
				v = newView(mAdapterContext, mDialerSearchCursor, parent);
//				mProcessView = v;
			} else {
				v = convertView;
//				return mProcessView;
			}
			bindView(v, mAdapterContext, mDialerSearchCursor);
			return v;
		}
		
		public void setResultCursor(Cursor cursor) {
			if (mDialerSearchCursor != null) {
				mDialerSearchCursor.close();
			}
			mDialerSearchCursor = cursor;
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
            if (scrollState != OnScrollListener.SCROLL_STATE_IDLE) {
            	mDialpad.setVisibility(View.GONE);
            	mShowDialpad.setVisibility(View.VISIBLE);
            	if (mShowDialEditText != null)
            		mShowDialEditText.setText(mDigits.getText());            	
    		    if (!mBottomIsVisiable && mDigits.getText().length()>0) {
    		    	mList.addFooterView(mAddToContacts);
    		    	mBottomIsVisiable = true;
    		    }
//            	dsListParam.height = 394 - mShowDialpadButton.getHeight();
//		    	mListLayout.setLayoutParams(dsListParam);
            }
        }
    }
    
    private class QueryHandler extends AsyncQueryHandler {//80794
    	private final WeakReference<TwelveKeyDialer> mActivity;
    	
    	QueryHandler (Context context) {
    		super(context.getContentResolver());
    		log("Construct QueryHandler, content resolver: "+context.getContentResolver());
            mActivity = new WeakReference<TwelveKeyDialer>((TwelveKeyDialer) context);
    	}
    	
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			Integer cnt = mSearchNumCntQ.poll();
			if (cnt != null) {
				mPrevQueryDigCnt = cnt.intValue();
			}
			log("twelvekeydialer: onQueryComplete"+cursor);
			
			final TwelveKeyDialer activity = mActivity.get(); 
			
			if (activity != null && !activity.isFinishing()) {
                final DialerSearchListAdapter dialerSearchAdapter = activity.mAdapter;
                // Whenever we get a suggestions cursor, we need to immediately kick off
                // another query for the complete list of contacts
                if (cursor!= null) {
                	mDialerSearchCursorCount = cursor.getCount();
                	log("cursor count: "+mDialerSearchCursorCount);
	                    String tempStr = mDigits.getText().toString();
	                	if (tempStr != null && mDialerSearchCursorCount > 0) {
	                		mQueryComplete = true;
	                		noResultDigCnt = 0;
	                   		// notify UI to update view only if the search digit count is equal to current input search digits in text view
	                		// since user may input/delete quickly, the list view will be update continuously and take a lot of time 
	                		if (tripHyphen(tempStr).length() == mPrevQueryDigCnt) {
	                			// Don't need to close cursor every time after query complete.
	    	                    if (mMatchedResultsCount != null) {
	    	                    	if (mDigits.getText().length() > 0) {
	    	                    		mMatchedResultsCount.setText(mDialerSearchCursorCount + mResultCountsStr);
	    	                    	} else {
	    	                    		mMatchedResultsCount.setText("0" + mResultCountsStr);
	    	                    	}
	    	                    }
	                			activity.mAdapter.setResultCursor(cursor);
	                			dialerSearchAdapter.changeCursor(cursor);
	                		} else {
	                			cursor.close();
	                		}
	                	} else {
	                	    if (mMatchedResultsCount != null) {
    	                    	if (mDigits.getText().length() > 0) {
    	                    		mMatchedResultsCount.setText(mDialerSearchCursorCount + mResultCountsStr);
    	                    	} else {
    	                    		mMatchedResultsCount.setText("0" + mResultCountsStr);
    	                    	}
    	                    }
	                		noResultDigCnt = mDigits.getText().length();
	                		cursor.close();
	                		activity.mAdapter.setResultCursor(null);
	                	}
                }
            } else {
            	if (cursor != null)
            		cursor.close();
            }
		}
		protected void onDeleteComplete(int token, Object cookie, int result) {//80794
			log("result is "+result);
			if (result < 0) {
				Toast.makeText(TwelveKeyDialer.this,
						R.string.delete_error, Toast.LENGTH_SHORT).show();
			} else {
//				 getContentResolver().delete(mSelectedContactUri, null, null);
		            if (mSelectedContactUri != null) {
		            	log("Before delete db");
		                int deleteCount = getContentResolver().delete(mSelectedContactUri, null, null);
		                //mDigits.getText().clear();
		                log("onDeleteComplete startQuery");
		                //TwelveKeyDialer.this.startQuery(null, DIALER_SEARCH_MODE_ALL);
                        if (deleteCount > 0 ) {
                        	TwelveKeyDialer.delCount = deleteCount;
                            TwelveKeyDialer.this.mDBHandlerForDelContacts.sendEmptyMessage(DS_MSG_CONTACTS_DELETE_CHANGED);
                        }
		            }
			}

		}
    
    }
    
    public void setSearchNumberOnly() {
    	mSearchNumberOnly = true;
    }
    
    public void startQuery(String searchContent, int mode) {
		if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
			//Now Do not query if IME keyboard is turned on. 
			//TBD: It Should bed changed if supports IME search.
			if (imeMgr != null && imeMgr.isInputEnabled() && searchContent != null) {
				log("[startQuery] In IME input mode. Donot search.");
				return;
			}
			log("twelvekeydialer: startQuery searchContent: "+searchContent+" mode: "+mode);
	        searchContent = tripHyphen(searchContent);
        	noMoreResult = (noResultDigCnt > 0 && mDigits.getText().length() > noResultDigCnt) ? true : false;
        	log("noResultDigCnt: " + noResultDigCnt + " || mDigits.getText(): " + mDigits.getText());
        	mQueryComplete = false;
        	if (searchContent == null) {
		        mQueryHandler.startQuery(QUERY_TOKEN, null, 
				        Uri.parse("content://com.android.contacts/dialer_search/filter/init#" + mDisplayOrder+"#"+mSortOrder), 
				        DIALER_SEARCH_PROJECTION, null, null, null);
		        mSearchNumCntQ.offer(Integer.valueOf(0));
	        } else if (mode == DIALER_SEARCH_MODE_ALL) {
	        	if (!noMoreResult) {
		        	mQueryHandler.startQuery(QUERY_TOKEN, null, 
	    		        Uri.parse("content://com.android.contacts/dialer_search/filter/"+searchContent), 
	    		        DIALER_SEARCH_PROJECTION, null, null, null);
		        	mSearchNumCntQ.offer(Integer.valueOf(searchContent.length()));
	        	}
            } else if (mode == DIALER_SEARCH_MODE_NUMBER) {
        		// won't check noMoreResult for search number mode, since if edit in middle will invoke no search result!
    	        mQueryHandler.startQuery(QUERY_TOKEN, null, 
    		        Uri.parse("content://com.android.contacts/dialer_search_number/filter/"+searchContent), 
    		        DIALER_SEARCH_PROJECTION, null, null, null);
    	        mSearchNumCntQ.offer(Integer.valueOf(searchContent.length()));
			}
    	}
    }

    private class DeleteClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
        	Cursor cursor = (Cursor) mAdapter.getItem(mPosition);
            Log.d(TAG, "DeleteClickListener, mPosition= "+ mPosition);
            if (null == cursor) {
                return;
            }
            if ((0 == cursor.getCount()) || cursor.isAfterLast()) {
                return;
            }
            Uri uri = null;
    
            final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
		    if (iTel == null) return;
		    int simPhoneIndicate = cursor.getInt(INDICATE_PHONE_SIM_INDEX);
        	int slotId = SIMInfo.getSlotById(TwelveKeyDialer.this, (long)simPhoneIndicate);
        	long rawContactId = -1;
        	String name = null;
        	String number = null;
        	Log.d(TAG, "DeleteClickListener simPhoneIndicate " + simPhoneIndicate + ", mPosition " + mPosition);
        	Log.i(TAG,"DeleteClickListener slotId is " + slotId);
            if (simPhoneIndicate > RawContacts.INDICATE_PHONE) {
                uri = ContactsUtils.getUri(slotId);
				if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
					try {
						if (null != iTel && !iTel.isRadioOnGemini(slotId)) {
							Toast.makeText(TwelveKeyDialer.this,
									R.string.AirPlane_mode_on_delete,
									Toast.LENGTH_SHORT).show();
							return;
						}
						if (null != iTel && iTel.isFDNEnabledGemini(slotId)) {

							Toast.makeText(TwelveKeyDialer.this,
									R.string.FDNEnabled_delete,
									Toast.LENGTH_SHORT).show();

							return;
						}
						if (!(TelephonyManager.SIM_STATE_READY == TelephonyManager
								.getDefault().getSimStateGemini(slotId))) {
							Toast.makeText(TwelveKeyDialer.this,
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
							Toast.makeText(TwelveKeyDialer.this,
									R.string.AirPlane_mode_on_delete,
									Toast.LENGTH_SHORT).show();
							return;
						}
						if (null != iTel && iTel.isFDNEnabled()) {

							Toast.makeText(TwelveKeyDialer.this,
									R.string.FDNEnabled_delete,
									Toast.LENGTH_SHORT).show();

							return;
						}
						if (!(TelephonyManager.SIM_STATE_READY == TelephonyManager
								.getDefault().getSimState())) {
							Toast.makeText(TwelveKeyDialer.this,
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
					long contact_id = cursor.getLong(CONTACT_ID_INDEX);
					Log.d(TAG, "contact_id is " + contact_id);
					ContentResolver resolver = getContentResolver();
					rawContactId = ContactsUtils.queryForRawContactId(resolver, contact_id);
					Log.d(TAG, "rawContactId is " + rawContactId);
					String where = ContactsUtils.deleteSimContact(rawContactId, resolver);		
					Log.d(TAG,"mQueryHandler is "+mQueryHandler);
					Log.d(TAG, "uri " + uri);
					mQueryHandler = new QueryHandler(TwelveKeyDialer.this);
					Log.d(TAG,"where is " + where);
					mQueryHandler.startDelete(DELETE_TOKEN, null, uri, where, null);
					Log.d(TAG, "uri  again ******** " + uri);
					return;
				}
			} else if (mSelectedContactUri != null) {
				int deleteCount = getContentResolver().delete(
						mSelectedContactUri, null, null);
				log("DeleteClickListener onClick");
				if (deleteCount > 0) {
					TwelveKeyDialer.delCount = deleteCount;
					TwelveKeyDialer.this.mDBHandlerForDelContacts
							.sendEmptyMessage(DS_MSG_CONTACTS_DELETE_CHANGED);
				}
            }
        }
    }
    
    private class AddContactClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
//        	final String number = mDigits.getText().toString();
        	addToContact(addToContactNumber);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo;
        try {
        	menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }
        log("[onContextItemSelected]menuInfo.position:" + menuInfo.position);
        mPosition = menuInfo.position;
    	Cursor cursor = null;
    	try {
    		cursor = (Cursor) mAdapter.getItem(menuInfo.position);
    	} catch (Exception e) {
    		if (cursor != null)
    			cursor.close();
    		cursor = null;
    	}
    	if (cursor == null)
    		return false;
    	
        final long contactId = cursor.getLong(CONTACT_ID_INDEX);
        final int indicate = cursor.getInt(INDICATE_PHONE_SIM_INDEX);
        final String lookupKey = cursor.getString(CONTACT_NAME_LOOKUP_INDEX);
        final int starState = cursor.getInt(CONTACT_STARRED_INDEX);
        final String  number = cursor.getString(SEARCH_PHONE_NUMBER_INDEX);
		final long callLogId = cursor.getLong(CALL_LOG_ID_INDEX);
		final int simIdUsed = cursor.getInt(SIM_ID_INDEX);
        
        switch (item.getItemId()) {
        case MENU_ITEM_CONTACTS_CALLLOG_VIEW_CONTACT: {
    		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        	Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
        	intent.putExtra(RawContacts.INDICATE_PHONE_SIM, indicate);
        	//Check whether it needs send owner_id.
        	//intent.putExtra("owner_id", PhoneOwner.getInstance() == null ? 
			//		(long)-1 : PhoneOwner.getInstance().getOwnerID());
		    //intent.putExtra("current_contact_id", id);
			startActivity(intent);	
        	 return true;
        }
        case MENU_ITEM_CONTACTS_EDIT_CONTACT: {
			if (indicate >= RawContacts.INDICATE_SIM) {
				final Uri uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
				log("uri is "+uri);
				final Intent intent = new Intent(this, EditSimContactActivity.class);
				intent.setData(uri);
				intent.putExtra("action", Intent.ACTION_EDIT);
				intent.putExtra(RawContacts.INDICATE_PHONE_SIM, indicate);
	            int simSlot = -1;
	            if(indicate > 0)
	            	simSlot = mSimInfoWrapper.getSimSlotById(indicate);
	            intent.putExtra("slotId", simSlot);
				startActivity(intent);
			} else {
				long rawContactId = ContactsUtils.queryForRawContactId(getContentResolver(), contactId);
				Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
				final Intent intent = new Intent(Intent.ACTION_EDIT, rawContactUri);
				startActivity(intent);
		    }
			return true;
        }
        case MENU_ITEM_CONTACTS_SHARE_CONTACT: {
        	Uri uri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, Uri.encode(lookupKey));
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(Contacts.CONTENT_VCARD_TYPE);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
    	    intent.putExtra("contactId", (int)contactId);
            startActivity(intent);
            return true;
        }
        case MENU_ITEM_CONTACTS_TOGGLE_STAR: {
            ContentValues values = new ContentValues(1);
            values.put(Contacts.STARRED, starState == 0 ? 1 : 0);
            final Uri selectedUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
            int updateCount = getContentResolver().update(selectedUri, values, null, null);
            if (updateCount > 0 ) {
            	TwelveKeyDialer.delCount = updateCount;
                mDBHandlerForDelContacts.sendEmptyMessage(DS_MSG_CONTACTS_DELETE_CHANGED);
            }
            //mDigits.getText().clear();
            return true;
        }
        case MENU_ITEM_CONTACTS_DELETE_CONTACT: {
            mSelectedContactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
            showDialog(R.id.dialog_delete_contact_confirmation);
            return true;
        }
        case MENU_ITEM_CALLLOG_VOICE_CALL: {
        	log("[onContextItemSelected]MENU_ITEM_CALLLOG_VOICE_CALL: number-" + number + " || simIdUsed-" + simIdUsed);
        	if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
        	    onDialButtonClickIntWithSimId(number,simIdUsed);
        	} else {
        	    dialButtonPressedextInt(number, DEFAULT_SIM);
        	}
            return true;
        }
        case MENU_ITEM_CALLLOG_VT_CALL: {
        	onVideoCallButtonClickInt(number);
        	log("[onContextItemSelected]MENU_ITEM_CALLLOG_VT_CALL: number-" + number);
//        	ContactsUtils.dial(TwelveKeyDialer.this, number, ContactsUtils.DIAL_TYPE_VIDEO, null);
        	return true;
        }
        case MENU_ITEM_CALLLOG_SIP_CALL: {
        	log("[onContextItemSelected]MENU_ITEM_CALLLOG_SIP_CALL: number-" + number);
        	if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
        	    onDialButtonClickIntWithSimId(number,simIdUsed);
        	} else {
        	    dialButtonPressedextInt(number, DEFAULT_SIM);
        	}
        	return true;
        }
        //The follow 2 cases send intent to target immediately.
        //case MENU_ITEM_CALLLOG_SEND_SMS:
        //case MENU_ITEM_CALLLOG_EDIT_BEFORE_CALL:
        case MENU_ITEM_CALLLOG_ADD_TO_CONTACTS: {
        	onAddContacButtonClickInt(number);
        	return true;
        }
        case MENU_ITEM_CALLLOG_DELETE_CALL_LOG: {
            int deleteCount = getContentResolver().delete(Calls.CONTENT_URI, Calls._ID + " = " + callLogId, null);
//            if (mDigits.length() == 0) {
//            	startQuery(null, DIALER_SEARCH_MODE_ALL);
//            }
//            if (mDigits.length() > 0)
//            	mDigits.getText().clear();
            log("onContextItemSelected MENU_ITEM_CALLLOG_DELETE_CALL_LOG");
//            startQuery(null, DIALER_SEARCH_MODE_ALL);
            if (deleteCount > 0 ) {
            	TwelveKeyDialer.delCount = deleteCount;
                mDBHandlerForDelContacts.sendEmptyMessage(DS_MSG_CONTACTS_DELETE_CHANGED);
            }
            return true;
        }
        case MENU_ITEM_CONTACTS_CALLLOG_ADD_SPEED_DIAL: {
            final Intent intent = new Intent(this, AddSpeedDialActivity.class);
            Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
            intent.setData(contactUri);
            startActivity(intent);
            return true;
        }
        // modified by Qilong.sun 2010-12-14. Code for owner.
        case MENU_ITEM_OWNER_EDIT:{
        	if (false == FeatureOption.MTK_SNS_SUPPORT) {
        		return true;
        	}
        	if (PhoneOwner.getInstance() == null) {
    			this.startActivityForResult(
    					new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI)
    					.putExtra(NEW_OWNER_INFO, true), SUBACTIVITY_NEW_OWNER_INSERT);
        	} else {
        		long rawContactId = ContactsUtils.queryForRawContactId(getContentResolver(), 
        				PhoneOwner.getInstance().getOwnerID());
            			Uri rawContactUri = ContentUris.withAppendedId(
            					RawContacts.CONTENT_URI, rawContactId);
                	startActivityForResult(new Intent(Intent.ACTION_EDIT, rawContactUri),
                		SUBACTIVITY_NEW_OWNER_INSERT);
        	}
        	return true;
        }
        case MENU_ITEM_OWNER_DELETE: {
        	if (false == FeatureOption.MTK_SNS_SUPPORT) {
        		return true;
        	}
        	mSelectedContactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
            showDialog(R.id.dialog_delete_contact_confirmation);
            return true;
        }
        	default:
        		break;
        }
        return super.onContextItemSelected(item);
    }

    //Code just used for phone owner. 
    //TBD: Check if needed.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            	
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
            		if(contactId != -1){
            			PhoneOwner.initPhoneOwner(contactId, lookupKey);
            			PhoneOwner owner = PhoneOwner.getInstance();
            			if(null == contactName || contactName.length() == 0)
            				contactName = this.getText(android.R.string.unknownName) + "";
            			owner.setName(contactName);
            		}
            	}
            	break;
        }
    }
    
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }
		log("[onCreateContextMenu] info.position:" + info.position);
        Cursor cursor = (Cursor) mAdapter.getItem(info.position);
        if (cursor == null)
        	return;
    	int contactId = cursor.getInt(CONTACT_ID_INDEX);
    	int callLogId = cursor.getInt(CALL_LOG_ID_INDEX);
    	int nameType = cursor.getInt(SEARCH_PHONE_TYPE_INDEX);
    	String number = cursor.getString(SEARCH_PHONE_NUMBER_INDEX);
        String name  = cursor.getString(NAME_INDEX);
        int starState = cursor.getInt(CONTACT_STARRED_INDEX);
        int simId = cursor.getInt(INDICATE_PHONE_SIM_INDEX);
        
        String formatNumber = null;
        
        boolean isVoicemailNumber = false;
        boolean isSpecialNumber = false;
    	boolean isNoNameCallLog = false;
    	boolean isContactCalllog = false;
    	boolean isOnlyContact = false;
    	//Definition for VT
		boolean isVTIdle = true; 
		//Phone state definition for Gemini Dual SIM
        boolean sim1Radio = true;
        boolean sim1Idle = true;
        boolean sim2Idle = true;
        boolean sim1Ready = true;
        //Phone state definition for single SIM
        boolean simRadio = true;
        boolean simIdle = true;
        boolean simReady = true;

		//Number check//
		if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
			formatNumber = getString(R.string.unknown);
			isSpecialNumber = true;
		} else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
			formatNumber = getString(R.string.private_num);
			isSpecialNumber = true;
		} else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
			formatNumber = getString(R.string.payphone);
			isSpecialNumber = true;
		} else if (number.equals(mVoiceMailNumber)
				|| number.equals(mVoiceMailNumber2)) {
			formatNumber = getString(R.string.voicemail);
//			numberUri = Uri.parse("voicemail:x");
			isVoicemailNumber = true;
		} else {
			formatNumber = PhoneNumberFormatUtilEx.formatNumber(number);
		} 
			
        if (null != name) {
        	menu.setHeaderTitle(name);
        } else {
        	menu.setHeaderTitle(formatNumber);
        }
    	if (contactId > 0 && callLogId == 0) {
    		isOnlyContact = true;
    	} else if (contactId > 0 && callLogId > 0) {
    		isContactCalllog = true;
    	} else if (contactId == 0 && callLogId > 0){
    		isNoNameCallLog = true;
    	} else {
    		return;
    	}
    	
    	if (FeatureOption.MTK_VT3G324M_SUPPORT) {
            try {
                ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                		.getService(Context.TELEPHONY_SERVICE));
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    simRadio = iTel.isRadioOnGemini(Phone.GEMINI_SIM_1);
                    simIdle = iTel.isIdleGemini(Phone.GEMINI_SIM_1);
                    sim2Idle = iTel.isIdleGemini(Phone.GEMINI_SIM_2);
    				isVTIdle = iTel.isVTIdle();
    				simReady = (TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault()
    	          	        .getSimStateGemini(Phone.GEMINI_SIM_1));
                } else {
                	simRadio = iTel.isRadioOn();
                    simIdle = iTel.isIdle();
    				isVTIdle = iTel.isVTIdle();
    				simReady = (TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault()
    	          	        .getSimState());
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
    	}
    	
    	//Code for Phone Owner. 
    	// Added by Qilong.Sun 2010-12-14
        if(FeatureOption.MTK_SNS_SUPPORT)
        {
			SharedPreferences setting = getSharedPreferences(
					ContactsListActivity.PREFS_NAME, Context.MODE_WORLD_READABLE
							| Context.MODE_WORLD_WRITEABLE);
			long ownerid = setting.getLong(ContactsListActivity.PREFS_OWNER_ID, (long) -1);
			String ownerLookupKey = setting.getString(
					ContactsListActivity.PREFS_OWNER_LOOKUPKEY, null);
			String ownerName = setting.getString(ContactsListActivity.PREFS_OWNER_NAME, null);
			if(ownerid != -1){
				PhoneOwner.initPhoneOwner(ownerid, ownerLookupKey);
				if (null == ownerName || ownerName.length() == 0)
					ownerName = mContext.getText(android.R.string.unknownName) + "";
				PhoneOwner.getInstance().setName(ownerName);
			}
        	Log.v(TAG, "ownerid:" + ownerid);
        	Log.v(TAG, "ownerLookupKey:" + ownerLookupKey);
        	Log.v(TAG, "ownerName:" + ownerName);

        	PhoneOwner owner = PhoneOwner.getInstance();
        	Log.v(TAG, "owner:" + owner);
        	if(owner !=null)
               	Log.v(TAG, "owner ID:"+ owner.getOwnerID());
        	
        	if(null != owner && contactId == owner.getOwnerID()){
        		long id = info.id;
        		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, owner.getOwnerID());
        		long rawContactId = ContactsUtils.queryForRawContactId(
    										getContentResolver(), owner.getOwnerID());
        		Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
        		menu.setHeaderTitle(owner.getName());
        		menu.add(0, MENU_ITEM_OWNER_EDIT, 0, R.string.menu_editContact).setIntent(
        				new Intent(Intent.ACTION_EDIT, rawContactUri));
        		menu.add(0, MENU_ITEM_OWNER_DELETE, 0, R.string.menu_deleteContact);
        		return;
        	}
        }
    	// End Qilong.Sun
    		
    	//It is in call log including NO NAME CALLLOG OR CONTACT CALLLOG
    	if (isContactCalllog || isNoNameCallLog) {	
    		boolean isSipNumber = false;
    		Uri numberUri = null;
    		//comment out by mtk80908. Do not support voicemailNumber URI now.
    		//Maybe add support in future.
//    		if (isVoicemailNumber) {
//    			numberUri = Uri.parse("voicemail:x");
//    		} 
    		if (PhoneNumberUtils.isUriNumber(number)) {
    			numberUri = Uri.fromParts("sip", number, null);
    			isSipNumber = true;
    		} else {
    			numberUri = Uri.fromParts("tel", number, null);
    		}
    		//For Common menu item
    		if (isSpecialNumber) {
    			//do nothing
    		} else if (isVoicemailNumber) {
    			int insertedSimCount = mSimInfoWrapper.getInsertedSimCount();
    			//Only Add call items when inserted SIM count is above zero.
    			if (insertedSimCount > 0) {
        			//For common call log
				    String menuTxt = null;
                    menuTxt = getResources().getString(R.string.call_detail_call_via, formatNumber);
                    menu.add(0, MENU_ITEM_CALLLOG_VOICE_CALL, 0, menuTxt);
    			}
               	//For VT call log item
    			if (FeatureOption.MTK_VT3G324M_SUPPORT) {
            		menu.add(0, MENU_ITEM_CALLLOG_VT_CALL, 0,
            				getResources().getString(R.string.recentCalls_vt_callNumber,formatNumber))
    						.setEnabled(isVTIdle && simRadio && simReady && simIdle && sim2Idle);	
    			}

    		} else if (isSipNumber) {
    			//For SIP Call Log
            	menu.add(0, MENU_ITEM_CALLLOG_SIP_CALL, 0, 
            			getResources().getString(R.string.call_detail_call_via, number));
    		} else {
    			int insertedSimCount = mSimInfoWrapper.getInsertedSimCount();
    			//Only Add call items when inserted SIM count is above zero.
    			if (insertedSimCount > 0) {
        			//For common call log
					// Dual SIM Inserted - call number via...
				    String menuTxt = null;
                    if(PhoneNumberUtils.isEmergencyNumber(number)) {
                        menuTxt = getResources().getString(R.string.recentCalls_callNumber, number);
                    } else {
                        menuTxt = getResources().getString(R.string.call_detail_call_via, formatNumber);
                    }
                    menu.add(0, MENU_ITEM_CALLLOG_VOICE_CALL, 0, menuTxt);
    			}
    			
               	//For VT call log item
    			if (FeatureOption.MTK_VT3G324M_SUPPORT) {
            		menu.add(0, MENU_ITEM_CALLLOG_VT_CALL, 0,
            				getResources().getString(R.string.recentCalls_vt_callNumber,formatNumber))
    						.setEnabled(isVTIdle && simRadio && simReady && simIdle && sim2Idle);	
    			}
    			
	            menu.add(0, MENU_ITEM_CALLLOG_SEND_SMS, 0, R.string.menu_sendTextMessage)
            	.setIntent(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", number, null)));
	            menu.add(0, MENU_ITEM_CALLLOG_EDIT_BEFORE_CALL, 0, R.string.recentCalls_editNumberBeforeCall)
            	.setIntent(new Intent(Intent.ACTION_DIAL, numberUri));
        		//For No Name Call Log
    			if (isNoNameCallLog) {
    				menu.add(0, MENU_ITEM_CALLLOG_ADD_TO_CONTACTS, 0, R.string.recentCalls_addToContact);
    			}
        		//For Name Contacts Call Log
    			if (isContactCalllog) {
    				menu.add(0, MENU_ITEM_CONTACTS_CALLLOG_VIEW_CONTACT, 0, R.string.menu_viewContact);
    				menu.add(0, MENU_ITEM_CONTACTS_CALLLOG_ADD_SPEED_DIAL, 0, R.string.speed_dial_view);
    			}
    		}
    		menu.add(0, MENU_ITEM_CALLLOG_DELETE_CALL_LOG, 0, R.string.recentCalls_removeFromRecentList);
    	} //End in call log number 
    	
    	//It is simple contacts.
    	if (isOnlyContact) {
    		//For Common menu item
    		menu.add(0, MENU_ITEM_CONTACTS_CALLLOG_VIEW_CONTACT, 0, R.string.menu_viewContact);
        	menu.add(0, MENU_ITEM_CONTACTS_EDIT_CONTACT, 0, R.string.menu_editContact);
        	menu.add(0, MENU_ITEM_CONTACTS_SHARE_CONTACT, 0, R.string.share_contacts);
    		//Toggle Add to favorites only for Phone Contacts 
	        if (simId == RawContacts.INDICATE_PHONE) {
	            if (starState == 0) {
	                menu.add(0, MENU_ITEM_CONTACTS_TOGGLE_STAR, 0, R.string.menu_addStar);
	            } else {
	                menu.add(0, MENU_ITEM_CONTACTS_TOGGLE_STAR, 0, R.string.menu_removeStar);
	            }
	        }
        	menu.add(0, MENU_ITEM_CONTACTS_CALLLOG_ADD_SPEED_DIAL, 0, R.string.speed_dial_view);
        	menu.add(0, MENU_ITEM_CONTACTS_DELETE_CONTACT, 0, R.string.menu_deleteContact);
        	
    	} //End simple contacts number
    	
    	super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	//end mtk80908. Adjust context menu definition and behavior. 2011-4-30
/* Dialer search end - xiaodong wang */


    private Handler mHandler = new Handler() {
    	public void handleMessage(Message msg){
    		switch (msg.what) {
    			case New_TONE_GENERATOR : 
    				newToneGenerator();
    				break;
    			case LISTEN_PHONE_STATES:
    				listenPhoneStates();
    				break;   
    			case LISTEN_PHONE_NONE_STATES:
    				 stopListenPhoneStates();
                     break; 
    			case RELEASE_TONE_GENERATOR:
    				 releaseToneGenerator(); 
                     break;   				
    		}    		
    	}
    };
    
    private void newToneGenerator() {
        // if the mToneGenerator creation fails, just continue without it.  It is
        // a local audio signal, and is not as important as the dtmf tone itself.
        synchronized(mToneGeneratorLock) {
            if (mToneGenerator == null) {
                try {
                    // we want the user to be able to control the volume of the dial tones
                    // outside of a call, so we use the stream type that is also mapped to the
                    // volume control keys for this activity
                    mToneGenerator = new ToneGenerator(DIAL_TONE_STREAM_TYPE, TONE_RELATIVE_VOLUME);
                    setVolumeControlStream(DIAL_TONE_STREAM_TYPE);
                } catch (RuntimeException e) {
                    Log.w(TAG, "Exception caught while creating local tone generator: " + e);
                    mToneGenerator = null;
                }
            }
        }
    }
    
    private void listenPhoneStates() {
        // While we're in the foreground, listen for phone state changes,
        // purely so that we can take down the "dialpad chooser" if the
        // phone becomes idle while the chooser UI is visible.
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
                telephonyManager.listenGemini(mPhoneStateListener2, PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_SERVICE_STATE, Phone.GEMINI_SIM_2);
                telephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_SERVICE_STATE, Phone.GEMINI_SIM_1);
        }
        else
        {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private void stopListenPhoneStates() {
    	// Stop listening for phone state changes.
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
            telephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_NONE, Phone.GEMINI_SIM_1);
            telephonyManager.listenGemini(mPhoneStateListener2, PhoneStateListener.LISTEN_NONE, Phone.GEMINI_SIM_2);
        }
        else
        {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
    
    private void releaseToneGenerator() {
        synchronized(mToneGeneratorLock) {
            if (mToneGenerator != null) {
                mToneGenerator.release();
                mToneGenerator = null;
            }
        }
    }

    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
            /**
             * Listen for phone state changes so that we can take down the
             * "dialpad chooser" if the phone becomes idle while the
             * chooser UI is visible.
             */
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                 Log.i(TAG, "PhoneStateListener.onCallStateChanged: "
                       + state + ", '" + incomingNumber + "'");

                if (mPreviousCallState1 == TelephonyManager.CALL_STATE_OFFHOOK && (state == TelephonyManager.CALL_STATE_IDLE) && dialpadChooserVisible()) {
                    Log.i(TAG, "Call ended with dialpad chooser visible!  Taking it down...");
                    // Note there's a race condition in the UI here: the
                    // dialpad chooser could conceivably disappear (on its
                    // own) at the exact moment the user was trying to select
                    // one of the choices, which would be confusing.  (But at
                    // least that's better than leaving the dialpad chooser
                    // onscreen, but useless...)
                    showDialpadChooser(false);
                    updateDialAndDeleteButtonEnabledState();
                }
                else if ((state == TelephonyManager.CALL_STATE_IDLE))
                {
                    updateDialAndDeleteButtonEnabledState();
                }
                mPreviousCallState1 = state;
            }

            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                 Log.i(TAG, "PhoneStateListener.onServiceStateChanged: serviceState="+serviceState);
                 updateDialAndDeleteButtonEnabledState();
                 removeSim1ContactsInAirplaneMode();
//                 mDBHandlerForDelContacts.sendEmptyMessageDelayed(DS_MSG_CONTACTS_DELETE_CHANGED, DS_MSG_DELAY_TIME);
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
                 Log.i(TAG, "PhoneStateListener2222.onCallStateChanged2 "
                       + state + ", '" + incomingNumber + "'");
                if (mPreviousCallState2 == TelephonyManager.CALL_STATE_OFFHOOK && (state == TelephonyManager.CALL_STATE_IDLE) && dialpadChooserVisible()) {

                    Log.i(TAG, "Call ended with dialpad chooser visible!  Taking it down...");
                    // Note there's a race condition in the UI here: the
                    // dialpad chooser could conceivably disappear (on its
                    // own) at the exact moment the user was trying to select
                    // one of the choices, which would be confusing.  (But at
                    // least that's better than leaving the dialpad chooser
                    // onscreen, but useless...)
                    showDialpadChooser(false);
                    updateDialAndDeleteButtonEnabledState();					
                }
                else if ((state == TelephonyManager.CALL_STATE_IDLE) )
                {
                    updateDialAndDeleteButtonEnabledState();
                }
                mPreviousCallState2 = state;				
            }

            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                 Log.i(TAG, "PhoneStateListener2.onServiceStateChanged: serviceState="+serviceState);
                 updateDialAndDeleteButtonEnabledState();
                 removeSim2ContactsInAirplaneMode();
//                 mDBHandlerForDelContacts.sendEmptyMessageDelayed(DS_MSG_CONTACTS_DELETE_CHANGED, DS_MSG_DELAY_TIME);
            }            
        };

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Do nothing
    }

    public void onTextChanged(CharSequence input, int start, int before, int changeCount) {
        // Do nothing
        // DTMF Tones do not need to be played here any longer -
        // the DTMF dialer handles that functionality now.
    }

    public void afterTextChanged(Editable input) {
        Log.d(TAG, "afterTextChanged:" + input);
    	if(mIsFromADN)
        {
            Log.d(TAG, "ignore input from adn select dialog");
            mIsFromADN = false;
        }
        else
        {
            if (SpecialCharSequenceMgr.handleChars(this, input.toString(), mDigits)) {
            // A special sequence was entered, clear the digits
            mDigits.getText().clear();
            }
        }

/* Fion add start */
//        final boolean notEmpty = mDigits.length() != 0;
//        if (notEmpty) {
//            mDigits.setBackgroundDrawable(mDigitsBackground);
//        } else {
//            mDigits.setCursorVisible(false);
//            mDigits.setBackgroundDrawable(mDigitsEmptyBackground);
//        }
/* Fion add end */
        if (mDigits.getText().length() <= MAX_DIGITS_DISPLAY_LENGTH) {
        	updateDialAndDeleteButtonEnabledState();
        }
    }

    public void showSpecialSequenceDialog(int dialog, Bundle bundle)
    {
        mDialogBundle = bundle;
    	showDialog(dialog, mDialogBundle);
    }
    
    public void hideSpecialSequenceDialog()
    {
    	if(mIMEIDialog != null && mIMEIDialog.isShowing())
        {
            mIMEIDialog.dismiss();
            //mIMEIDialog = null;
        }
    	
        if(mMMIDialog != null && mMMIDialog.isShowing())
        {	
            mMMIDialog.dismiss();
            //mMMIDialog = null;
        }
        
        if(mADNDialog != null && mADNDialog.isShowing())
        {
            mADNDialog.dismiss();
            //mADNDialog = null;
        }
        
        if(mVoiceMailDialog != null && mVoiceMailDialog.isShowing())
        {
            mVoiceMailDialog.dismiss();
            //mVoiceMailDialog = null;
        }
        
        if(mMissingVoicemailDialog != null && mMissingVoicemailDialog.isShowing())
        {
            mMissingVoicemailDialog.dismiss();
            //mMissingVoicemailDialog = null;
        }
        
        if(mRadioOffAlertDialog != null && mRadioOffAlertDialog.isShowing())
            mRadioOffAlertDialog.dismiss();
/****************used in 48mpdev****************/
        if (mDialNumberListDlg != null && mDialNumberListDlg.isShowing())
        	mDialNumberListDlg.dismiss();
        
        if (mAddToContactDlg != null && mAddToContactDlg.isShowing())
        	mAddToContactDlg.dismiss();
        
        if (mDialSimListDlg != null && mDialSimListDlg.isShowing())
        	mDialSimListDlg.dismiss();
        
        if (mDeleteContactDlg != null && mDeleteContactDlg.isShowing())
        	mDeleteContactDlg.dismiss();
/****************used in 48mpdev****************/
    }
    
    private class SearchResultChangeObserver extends ContentObserver {
        public SearchResultChangeObserver() {
            super(new Handler());
        }
        @Override
        public void onChange(boolean selfChange) {
        	Log.e(TAG, "onChange length: "+ mDigits.length());
        	if (mDigits != null) {
	            mDigits.getText().clear();
	            if (mShowDialEditText != null) 
	            	mShowDialEditText.setText(null);
	            if (mBottomIsVisiable) {
    		    	mList.removeFooterView(mAddToContacts);
    		    	mBottomIsVisiable = false;
    		    }
		    	startQuery(null, DIALER_SEARCH_MODE_ALL);
        	}
    	}
    }
    
    private View inflateView;
    
    class InflateHandler extends Handler{
    	public InflateHandler() {
    		
    	}
    	
    	public InflateHandler(Looper looper) {
    		super(looper);
    	}
    	
    	public void handleMessage(Message msg) {
            Log.e(TAG, "InflateHandler inflate");
            Log.e(TAG, "InflateHandler InflateHandler thread id:" + Thread.currentThread().getId());
            LayoutInflater mDialerInflater = (LayoutInflater) TwelveKeyDialer.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflateView =  mDialerInflater.inflate(getContentViewResource(), null, false);
            TwelveKeyDialer.this.runOnUiThread(new Runnable() {
            	public void run() {
            		Log.e(TAG, "InflateHandler UI thread id:" + Thread.currentThread().getId());
            		ViewGroup.LayoutParams layoutParam = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                	mDialpadLayout.addView(inflateView, layoutParam);
                	
                	
                    Resources r = getResources();
                    Log.e(TAG, "in InflateHandler, original onCreate logic begin");
                    /* Dialer search begin - xiaodong wang */
                	if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
                        mContext = TwelveKeyDialer.this;
                        mQueryHandler = new QueryHandler(TwelveKeyDialer.this);
                        mAdapter = new DialerSearchListAdapter(TwelveKeyDialer.this);
                        //Just for Sinlge. Should be changed later
                        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
                            mMatchedResultsCount = (TextView)findViewById(R.id.ds_result_count);
                        } else {
                        	mMatchedResultsCount = (TextView)findViewById(R.id.ds_result_count);
                        }
                        mResultCountsStr = getString(R.string.ds_matched_results);
                        mList = (ListView) findViewById(R.id.ContactsList);
                        Log.e(TAG, "onCreate() mList: "+mList);
//                        mUserTips = (TextView) findViewById(R.id.user_tips);
                        //head textView to results count 
                        if (mMatchedResultsCount != null)
                        	mMatchedResultsCount.setText("0" + mResultCountsStr);
                        //foot button used to add contacts if dial pad hides
                        mList.setHeaderDividersEnabled(true);
                        mList.setFooterDividersEnabled(false);
                        //set foot divider mentally, since list view cannot control footer divider separately
                		View mSeparatorLine = new TextView(TwelveKeyDialer.this);
                		mSeparatorLine.setBackgroundColor(Color.parseColor("#FFC0C2C3"));
                		((TextView)mSeparatorLine).setHeight(1);
                		mList.addFooterView(mSeparatorLine, null, false);
                        
                        mAddToContacts = new LinearLayout(TwelveKeyDialer.this);
                        mAddToContacts.setPadding(20, 16, 20, 0);
                		Button mAddToContactsBtn = new Button(TwelveKeyDialer.this);
                		mAddToContactsBtn.setText(R.string.ds_add_to_contacts);
                		mAddToContactsBtn.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
                		mAddToContactsBtn.setOnClickListener(new View.OnClickListener() {
                			public void onClick(View v) {
                            	addToContactNumber = mDigits.getText().toString();
                            	if ( addToContactNumber != null && addToContactNumber.length()>0)
//                            		showDialog(ADD_TO_CONTACT_DIALOG);
                                    onAddContacButtonClick();
                			}
                		});
                        
                		((LinearLayout)mAddToContacts).addView(mAddToContactsBtn);
                        //In order to wipe adapter in ListView, we have to add and remove the button. 
                        mList.addFooterView(mAddToContacts);
                        mList.setAdapter(mAdapter);
                        mList.removeFooterView(mAddToContacts);
                        mBottomIsVisiable = false;
                        
                        mList.setOnItemClickListener(mOnClickListener);
                        mList.setOnCreateContextMenuListener(TwelveKeyDialer.this);
                        mList.setOnScrollListener(mAdapter);

                        mListLayout = (FrameLayout) findViewById(R.id.List);
//                        dsListParam = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
                        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        mPhotoLoader = new ContactPhotoLoader(TwelveKeyDialer.this, R.drawable.ic_contact_list_picture);
//                        mContentObserver = new SearchResultChangeObserver();
//                    	mContext.getContentResolver().registerContentObserver(
//                    			Uri.parse("content://com.android.contacts/dialer_search/filter/"), 
//                    			true, mContentObserver);
                    	mSearchNumCntQ = new LinkedList<Integer>();
                	} else {
                		Log.e(TAG, "Dialer search not support");
                	}
                /* Dialer search end - xiaodong wang */


                /* Fion add start */
                        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
                        {
                			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
                				mDigits = (EditText) findViewById(R.id.digits);
//                				mDigits.setBackgroundColor(Color.argb(100, 0, 100, 0));
                			} else {
                            	mDigits = (EditText) findViewById(R.id.digitsGemini);
                        	}
                        }
                        else
                        {
                            mDigits = (EditText) findViewById(R.id.digits);
                        }
                        mDigits.setOnClickListener(TwelveKeyDialer.this);
                        mDigits.setOnKeyListener(TwelveKeyDialer.this);

                //comment out by mtk80908, remove the text input limit. If no side effect, remove the code later.        
//                        InputFilter[] nf = {this};
//                        mDigits.setFilters(nf);
                // mth80908 end        
                        
//                		mDigitsPaint = new Paint();
//                		mDigitsPaint.set(mDigits.getPaint());
//                		maxDigitsSize = mDigits.getTextSize() * mDialerSearchTextScale;
//                		minDigitsSize = maxDigitsSize - 3*2;	// reduce 2 dip each time in resizeDigitSize()
//                		currDigitsSize = maxDigitsSize;
                        maybeAddNumberFormatting();

                        // Check for the presence of the keypad
                        View view = findViewById(R.id.one);
                        if (view != null) {
                            Log.e(TAG, "twelvekeydialer : one. view  ");
                            setupKeypad();
                        }

                /* Fion add start */
                        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
                        {
                            Log.e(TAG, "twelvekeydialer : view  gemini");
                			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
                				mAddToContactOnDigitButton = findViewById(R.id.addToContactOnDigitButton);
                				mAddToContactOnDigitButton.setOnClickListener(TwelveKeyDialer.this);
                			    mDialButton1 = findViewById(R.id.dialsim1Button);
                			    if (FeatureOption.MTK_VT3G324M_SUPPORT) {
                			    	mDialButtonvt = findViewById(R.id.dialButtonVtAndMsg);
                			    } else {
                			    	mDialButtonMsg = findViewById(R.id.dialButtonVtAndMsg);
                			    }
                			} else {
                				if( FeatureOption.MTK_VT3G324M_SUPPORT)
                	            {
                	            	mVoicemailDialAndDeleteRow = findViewById(R.id.call1call2vt);
                	            	if (mVoicemailDialAndDeleteRow != null)
                	            		mDialButtonvt = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButtonVt);
                	            }
                	            else
                	            {
                	            	mVoicemailDialAndDeleteRow = findViewById(R.id.call1call2);
                	            }
                		        // Check whether we should show the onscreen "Dial" button.
                		        mDialButton1 = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButton1);
                				view = findViewById(R.id.deleteButtonGemini);
                		        view.setOnClickListener(TwelveKeyDialer.this);
                		        view.setOnLongClickListener(TwelveKeyDialer.this);
                		        mDelete = view;
                			}
                            //initVoicemailButton();

                            mDialButton = null;
                			
                            // Check whether we should show the onscreen "Dial" button.
//                            mDialButton1 = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButton1);
//                            mDialButton2 = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButton2);
                            
                            /*Added by lianyu.zhang start*/
//                            if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
//                            {
//                            	if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
//                            		mDialButtonvt = findViewById(R.id.dialButtonVtAndMsg);
//                            	}else{
//                            		mDialButtonvt = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButtonVt);
//                            	}
//                            }
                            /*Added by lianyu.zhang end*/

//                            mDialButtonECC = mDialEccrow.findViewById(R.id.dialButtonEcc);			
//                            mDialButtonSip = mDialEccrow.findViewById(R.id.dialButtonSip);
                			
//                            if (r.getBoolean(R.bool.config_show_onscreen_dial_button)) {
                                mDialButton1.setOnClickListener(TwelveKeyDialer.this);
//                                mDialButton2.setOnClickListener(this);		
//                                mDialButtonECC.setOnClickListener(this);	
//                                mDialButtonSip.setOnClickListener(this);
                                /*Added by lianyu.zhang start*/
                                if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
                                {   
                                	if (mDialButtonvt != null)
                                		mDialButtonvt.setOnClickListener(TwelveKeyDialer.this);
                                } else {
                        			if (mDialButtonMsg != null) {
                        				((ImageView)mDialButtonMsg).setImageResource(R.drawable.phone_dial_message_button);
                        				mDialButtonMsg.setOnClickListener(TwelveKeyDialer.this);
                        			}
                                }
                                /*Added by lianyu.zhang end*/
//                            } else {
//                                mDialButton1.setVisibility(View.GONE); // It's VISIBLE by default
//                                mDialButton1 = null;

//                                mDialButton2.setVisibility(View.GONE); // It's VISIBLE by default
//                                mDialButton2 = null;

//                                mDialButtonECC.setVisibility(View.GONE); // It's VISIBLE by default
//                                mDialButtonECC = null;
//                                mDialButtonSip.setVisibility(View.GONE); // It's VISIBLE by default
//                                mDialButtonSip = null;
                                
                                /*Added by lianyu.zhang start*/
//                                if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
//                                {
//                                	mDialButtonvt.setVisibility(View.GONE);
//                                	mDialButtonvt = null;
//                                }
                                /*Added by lianyu.zhang end*/
//                            }

                            Log.e(TAG, "twelvekeydialer : Gemini call1, call 2 view");
                	
//                            view = findViewById(R.id.deleteButtonGemini);
//                            view.setOnClickListener(this);
//                            view.setOnLongClickListener(this);
//                            mDelete = view;

                            Log.e(TAG, "twelvekeydialer : Gemini delete button gemini view");
                				
                        }
                        else
                        {		
                            Log.e(TAG, "twelvekeydialer : view  not gemini");
                			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
                		        mDialButton = findViewById(R.id.dialsim1Button);
                		        if (mDialButton != null)
                		        	mDialButton.setOnClickListener(TwelveKeyDialer.this);
                		        mAddToContactButton = findViewById(R.id.addToContactOnDigitButton);
                		        if (mAddToContactButton != null) 
                		        	mAddToContactButton.setOnClickListener(TwelveKeyDialer.this);
                        		if (FeatureOption.MTK_VT3G324M_SUPPORT) {
                        			mDialButtonvt = findViewById(R.id.dialButtonVtAndMsg);
                        			if (mDialButtonvt != null)
                        				mDialButtonvt.setOnClickListener(TwelveKeyDialer.this);
                        		} else {
                        			mDialButtonMsg = findViewById(R.id.dialButtonVtAndMsg);
                        			if (mDialButtonMsg != null) {
                        				((ImageView)mDialButtonMsg).setImageResource(R.drawable.phone_dial_message_button);
                        				mDialButtonMsg.setOnClickListener(TwelveKeyDialer.this);
                        			}
                        		}
//                		        mDialButtonvt = findViewById(R.id.dialButtonVtAndMsg);
//                                if( FeatureOption.MTK_VT3G324M_SUPPORT == true && mDialButtonvt != null)
//                                {
//                                	mDialButtonvt.setOnClickListener(this);
//                                } else if (mDialButtonvt != null) {
//                                	mDialButtonvt.setEnabled(false);
//                                }
                			} else {
                            	mVoicemailDialAndDeleteRow = findViewById(R.id.voicemailAndDialAndDelete);
                			}
                            initVoicemailButton();

                            Log.e(TAG, "twelvekeydialer : view not gemini after bu1/bu2");
                	
                			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == false) {	
                                // Check whether we should show the onscreen "Dial" button.
                                mDialButton = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButton);
                			}

//                            if (r.getBoolean(R.bool.config_show_onscreen_dial_button)) {
                			if (mDialButton != null)    
                				mDialButton.setOnClickListener(TwelveKeyDialer.this);
//                            } else {
//                                mDialButton.setVisibility(View.GONE); // It's VISIBLE by default
//                                mDialButton = null;
//                            }

                            Log.e(TAG, "twelvekeydialer : find deletebutton view");			
                	
                			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == false) {
                                // xingping.zheng modify
                                if(!isQVGAPlusQwerty())
                                {
                                    view = mVoicemailDialAndDeleteRow.findViewById(R.id.deleteButton);
                                    Log.d(TAG, "findViewById : deleteButton view = "+view);
                                }
                                else
                                {	
                                    view = findViewById(R.id.textfieldDeleteButton);
                                    Log.d(TAG, "findViewById : textfieldDeleteButton view = "+view);
                                }
                                
                                if(view != null)
                                {
                                    view.setOnClickListener(TwelveKeyDialer.this);
                                    view.setOnLongClickListener(TwelveKeyDialer.this);
                                    mDelete = view;
                                }
                            }
                        }

                        //xingping.zheng add
                        if(isQVGAPlusQwerty())
                        {
                            view = findViewById(R.id.addContactButton);
                            view.setOnClickListener(TwelveKeyDialer.this);
                            mAddContact = view;
                        }
                		if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
//                			mHideDialpadButton = findViewById(R.id.dialpadButton);
//                			mHideDialpadButton.setOnClickListener(this);
                		
                			/* show dialpad button */
                        	mShowDialpad = findViewById(R.id.showDialpad);
                			mShowDialpadButton = findViewById(R.id.showDialpadButton);
                			mShowDialEditText = (TextView)findViewById(R.id.digitsHide);
                        	if (mShowDialpad != null) {
                        		mShowDialpad.setVisibility(View.GONE);
                        		mShowDialpad.setOnClickListener(TwelveKeyDialer.this);        		
                        	}
//                        	if (mShowDialEditText != null) {
//                        		mShowDialEditText.setInputType(android.text.InputType.TYPE_NULL);
//                        	}
//                		    mShowDialpadButton.setVisibility(View.GONE);

                		    if(mShowDialpadButton !=null)
                		    	mShowDialpadButton.setOnClickListener(TwelveKeyDialer.this);
                		    if (mBottomIsVisiable) {
                		    	mList.removeFooterView(mAddToContacts);
                		    	mBottomIsVisiable = false;
                		    }
                		    mDeleteOnDigitButton = findViewById(R.id.deleteOnDigitButton);
                		    if(mDeleteOnDigitButton != null){
                			    mDeleteOnDigitButton.setOnClickListener(TwelveKeyDialer.this);
                			    mDeleteOnDigitButton.setOnLongClickListener(TwelveKeyDialer.this);
                		    }
                			mDialpad = findViewById(R.id.dial_search_pad);
                			
                			mSimInfoWrapper = ContactsUtils.SIMInfoWrapper.getDefault(TwelveKeyDialer.this);
                			Log.i(TAG,"[TwelveKeyDialer][updateSimInfo][mAdapter] : " + mAdapter);
                			mSimInfoWrapper.setListNotifyDataChanged(mAdapter);
//                			mContactsPrefs = new ContactsPreferences(this);
//                	        mDisplayOrder = mContactsPrefs.getDisplayOrder();
//                	        mSortOrder = mContactsPrefs.getSortOrder();
//                			ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
//                			mDialpad.setBackgroundDrawable(dw);
//                			mDialpad.setBackgroundColor(Color.argb(255, 0, 0, 0));
                		} else {
                        	mDialpad = findViewById(R.id.dialpad);  // This is null in landscape mode.
                		}
                        // In landscape we put the keyboard in phone mode.
                        // In portrait we prevent the soft keyboard to show since the
                        // dialpad acts as one already.
                        if (null == mDialpad) {
                            Log.e(TAG, "twelvekeydialer : mDialpad = null");			
                            mDigits.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
                        } else {
                            Log.e(TAG, "twelvekeydialer : mDialpad != null");			        
                            mDigits.setInputType(android.text.InputType.TYPE_NULL);
                            mDigits.setCursorVisibleNoCheck(true);
                        }

                /* Fion add end */

                        Log.e(TAG, "twelvekeydialer : after view");

                        // Set up the "dialpad chooser" UI; see showDialpadChooser().
                        mDialpadChooser = (ListView) findViewById(R.id.dialpadChooser);
                        mDialpadChooser.setOnItemClickListener(TwelveKeyDialer.this);
                       
                        try {
                            mHaptic.init(TwelveKeyDialer.this, r.getBoolean(R.bool.config_enable_dialer_key_vibration));
                        } catch (Resources.NotFoundException nfe) {
                             Log.e(TAG, "Vibrate control bool missing.", nfe);
                        } 

						if (FeatureOption.MTK_GEMINI_SUPPORT) {
							mVoiceMailNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
									.getVoiceMailNumberGemini(Phone.GEMINI_SIM_1);
							mVoiceMailNumber2 = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
									.getVoiceMailNumberGemini(Phone.GEMINI_SIM_2);
						} else {
							mVoiceMailNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
									.getVoiceMailNumber();
						}
//		Adjust font size. Should be used in future. 
                        if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT) {
			                DisplayMetrics dm = new DisplayMetrics();
			                WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
			                wm.getDefaultDisplay().getMetrics(dm);
			                switch(dm.densityDpi){
			                    case DisplayMetrics.DENSITY_HIGH:
			                        mDialerSearchTextScale = 2.0f/3;
			                    break;
			                    case DisplayMetrics.DENSITY_LOW:
			                        mDialerSearchTextScale = 4.0f/3;
			                    break;
			                    case DisplayMetrics.DENSITY_MEDIUM:
			                        mDialerSearchTextScale = 1.0f;
			                    // CR 138409, onResume is before onMeasure, so can't get width of digits in setFormattedDigits
			                    // in mdpi the text width is fixed, but for other density, need to discuss with text view owner
			                        if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
			        	                mDigitsViewWidth = 202; 
			                        } else {
			        	                mDigitsViewWidth = 255;
			                        }
			                    break;
			               }
			               mDigitsPaint = new Paint();
			               mDigitsPaint.set(mDigits.getPaint());
			               maxDigitsSize = mDigits.getTextSize() * mDialerSearchTextScale;
			               minDigitsSize = maxDigitsSize - 2*8;	// reduce 2 dip each time in resizeDigitSize()
			               currDigitsSize = maxDigitsSize;
                        }
                        /* added by xingping.zheng start */
//                        mSimAssociationQueryHandler = new SimAssociationQueryHandler(this, getContentResolver());
                        mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
                        mCellConnMgr = new CellConnMgr(mServiceComplete);
                        mCellConnMgr.register(getApplicationContext());
                        /* added by xingping.zheng end   */
                       Log.d(TAG, "in InflateHandler, original onCreate end");
                       
                       
                       mDigits.addTextChangedListener(TwelveKeyDialer.this);
                       
                       
                       
                       Log.e(TAG, "in InflateHandler, original onResume() begin");

                       // Query the last dialed number. Do it first because hitting
                       // the DB is 'slow'. This call is asynchronous.
                       queryLastOutgoingCall();

                       // retrieve the DTMF tone play back setting.
                       mDTMFToneEnabled = Settings.System.getInt(getContentResolver(),
                               Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
                       
                       //get IME service, used to check whether IME keyboard is turned on
                       imeMgr = (InputMethodManager)TwelveKeyDialer.this.getSystemService(TwelveKeyDialer.this.INPUT_METHOD_SERVICE);
                       
                       // Retrieve the haptic feedback setting.
                       mHaptic.checkSystemSetting();
                       
                       mHandler.sendEmptyMessage(LISTEN_PHONE_STATES);
                       
                       Activity parent = getParent();
                       // See if we were invoked with a DIAL intent. If we were, fill in the appropriate
                       // digits in the dialer field.
               /*        if (parent != null && parent instanceof DialtactsActivity) {
                           Uri dialUri = ((DialtactsActivity) parent).getAndClearDialUri();
                           if (dialUri != null) {
                               resolveIntent();
                           }
                       } */
//                       if (!TextUtils.isEmpty(mInput)) {
//                           mDigits.setText(mInput);
//                       }
                       // xiaodong wang: for dialer search, change screen will reset state of the screen.
                       if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == false) {
                           if (!TextUtils.isEmpty(mInput)) {
                               mDigits.setTextKeepState(mInput);
                           }
                       } else {
                       	if (mDigits.length() ==0) {
               	            if (mMatchedResultsCount != null) {
               	            	mMatchedResultsCount.setText("0" + mResultCountsStr);
               	            }
               		    	startQuery(null, DIALER_SEARCH_MODE_ALL);
               			}
                       }
                       resolveIntent();
                       mHandler.sendEmptyMessage(New_TONE_GENERATOR);

                       // Potentially show hint text in the mDigits field when the user
                       // hasn't typed any digits yet.  (If there's already an active call,
                       // this hint text will remind the user that he's about to add a new
                       // call.)
                       //
                       // TODO: consider adding better UI for the case where *both* lines
                       // are currently in use.  (Right now we let the user try to add
                       // another call, but that call is guaranteed to fail.  Perhaps the
                       // entire dialer UI should be disabled instead.)
                       if (phoneIsInUse()) {
                           Log.e(TAG, "phone is inuse  ");
//                         mDigits.setHint(R.string.dialerDialpadHintText);
                           mDigits.setHint(null);
                       } else {
                           Log.e(TAG, "phone is not inuse  ");        
                           // Common case; no hint necessary.
                           mDigits.setHint(null);

                           // Also, a sanity-check: the "dialpad chooser" UI should NEVER
                           // be visible if the phone is idle!
                           showDialpadChooser(false);
                       }

                       //mDigits.requestFocus();

                       Log.e(TAG, "before updatedialanddeletebuttonstate");			

                       if(mDelete == null)
                       {
                           View viewr = null;
                           if (FeatureOption.MTK_GEMINI_SUPPORT == true)
                           {
                               viewr = findViewById(R.id.deleteButtonGemini);
                               Log.d(TAG, "retry get view = "+viewr);
                               if(viewr != null)
                               {
                                   viewr.setOnClickListener(TwelveKeyDialer.this);
                                   viewr.setOnLongClickListener(TwelveKeyDialer.this);
                                   mDelete = viewr;
                               }
                           }
                           else
                           {
                           	if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == false) {
                                   if(!isQVGAPlusQwerty())
                                   {
                                       viewr = mVoicemailDialAndDeleteRow.findViewById(R.id.deleteButton);
                                       Log.d(TAG, "retry get view = "+viewr);
                                   }
                                   else
                                   { 	
                                       viewr = findViewById(R.id.textfieldDeleteButton);
                                       Log.d(TAG, "retry get view (QVGA-LAND) view = "+viewr);
                                   }
                                   if(viewr != null)
                                   {
                                       viewr.setOnClickListener(TwelveKeyDialer.this);
                                       viewr.setOnLongClickListener(TwelveKeyDialer.this);
                                       mDelete = viewr;
                                   }
                               }
                           }
                       }
               		if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
               			// Put before updateDialAndDeleteButtonEnabledState where will control dialer pad chooser visibility
//               		    if (mShowDialpadButton.getVisibility() == View.VISIBLE && !dialpadChooserVisible()) {
//               		    	mDialpad.setVisibility(View.GONE);        	
//               		    }
               			mContactsPrefs = new ContactsPreferences(TwelveKeyDialer.this);
               	        mDisplayOrder = mContactsPrefs.getDisplayOrder();
               	        mSortOrder = mContactsPrefs.getSortOrder();
//               		    if (mDigits.length() ==0) {
//               		    	startQuery(null, DIALER_SEARCH_MODE_ALL);
//               			}
               		    mPhotoLoader.clear();
               		    mPhotoLoader.resume();
               		    mPressQuickContactBadge = false;
                       	noResultDigCnt = 0;
               		    if (!dialpadChooserVisible()) {
               	        	if(mShowDialpad != null)
               	        		mShowDialpad.setVisibility(View.GONE);		    	
               			    if (mBottomIsVisiable) {
               			    	mList.removeFooterView(mAddToContacts);
               			    	mBottomIsVisiable = false;
               			    }
               	        	mDialpad.setVisibility(View.VISIBLE);
//               	        	dsListParam.height =  mListLayout.getHeight() - mDialpad.getHeight() + mDigits.getHeight();
//               	        	log("Calc layout height:"+mListLayout.getHeight()+", "+ mDialpad.getHeight()+", "+ mDigits.getHeight());
//               	        	mListLayout.setLayoutParams(dsListParam);
               		    }
               		}

//Comment out by MTK80908, Since design has changed from GE and the receiver is useless, 
//comment out this code
//                    if (FeatureOption.MTK_GEMINI_SUPPORT == true)
//                    {
//        	            registerMMIReceiver();
//                    }

                       updateDialAndDeleteButtonEnabledState();
                       mIsForeground = true;

                       /* added by xingping.zheng start */
                       if (FeatureOption.MTK_GEMINI_SUPPORT) {
                       setSimIndicatorVisibility(true);
                       mShowSimIndicator = true;
                       }
                       /* added by xingping.zheng end   */

                       if(!mDigits.hasFocus())
                       	mDigits.requestFocus();
                       
                       mDigits.setCursorVisibleNoCheck(true);
                       Log.e(TAG, "in InflateHandler, original onResume() end");
                       
                       setFirstLaunchCompleted(true);
                       
            	}
            });
            //mHandler.sendEmptyMessage(SHOW_WHOLE_SCREEN);
            if(mSimAssociationHandler != null)
                mSimAssociationHandler.sendEmptyMessageDelayed(MSG_UPDATE_SIM_ASSOCIATION, UPDATE_SIM_ASSOCIATION_DELAY);
    	}
    }
    
    private void setFirstLaunchCompleted(boolean isCompleted) {
    	mFirstLaunchCompleted = isCompleted;
    }
    
    private boolean getNotFirstLaunch() {
    	return mFirstLaunchCompleted;
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        /*if (FeatureOption.MTK_GEMINI_SUPPORT) {
            IntentFilter intentFilter =
                new IntentFilter(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
            registerReceiver(mSimIndicatorReceiver, intentFilter);   
        }*/
        setFirstLaunchCompleted(false);
        Log.e(TAG, "------------------------------------------------begin onCreate()  ");

        Resources r = getResources();
        // Do not show title in the case the device is in carmode.
/*      if ((r.getConfiguration().uiMode & Configuration.UI_MODE_TYPE_MASK) ==
                Configuration.UI_MODE_TYPE_CAR) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        } */
        // Set the content view
        //setContentView(getContentViewResource());

        // inflate call screen when language is changed.
        // if (mLocaleChanged == true){
        // inflate again;
        // }
/*        if (null != CacheViewService.CachedDialerView) {
    	    ViewGroup viewParent = (ViewGroup)CacheViewService.CachedDialerView.getParent();
            if (viewParent != null){
                viewParent.removeView(CacheViewService.CachedDialerView);
            }
            Log.e(TAG, "use cached view as ContentView ");
            setContentView(CacheViewService.CachedDialerView);
        } else {
        	Log.e(TAG, "inflate layout");
        	setContentView(getContentViewResource());
        }*/
        setContentView(R.layout.twelve_key_dialer_frame);
        mDialpadLayout = (LinearLayout)findViewById(R.id.twelve_key_dialer_frame);
        

        Log.e(TAG, "main thread id:" + Thread.currentThread().getId());
        HandlerThread handlerThread = new HandlerThread("inflate");
        handlerThread.start();
        InflateHandler inflateHandler = new InflateHandler(handlerThread.getLooper());
        Message msg = inflateHandler.obtainMessage();
        msg.sendToTarget();
        
        // xingping.zheng add CR:127118
        //mUseParentIntent = true;
        Log.e(TAG, "------------------------------------------------onCreate() onRestore begin");
        boolean ignoreState;
        //if (isChild()&& mUseParentIntent) {
        if (isChild()) {
            Intent intent = getParent().getIntent();
            ignoreState = intent.getBooleanExtra(DialtactsActivity.EXTRA_IGNORE_STATE, false);
        } else {
        	ignoreState = false;
        }
        if (!ignoreState && icicle != null) {
            super.onRestoreInstanceState(icicle);
        }
        /* added by xingping.zheng start */
        mSimAssociationWorkerThread = new HandlerThread("SimAssociationWorkerThread");
        mSimAssociationWorkerThread.start();
        if(mSimAssociationWorkerThread.getLooper() != null)
            mSimAssociationHandler = new SimAssociationHandler(mSimAssociationWorkerThread.getLooper());
        /* added by xingping.zheng end */
        //mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        //mCellConnMgr = new CellConnMgr(mServiceComplete);
        //mCellConnMgr.register(getApplicationContext());
        
/*        try {
            mHaptic.init(this, r.getBoolean(R.bool.config_enable_dialer_key_vibration));
        } catch (Resources.NotFoundException nfe) {
             Log.e(TAG, "Vibrate control bool missing.", nfe);
        }*/
        Log.e(TAG, "------------------------------------------------onCreate() onRestore end ");
//		Adjust font size. Should be used in future.       
//        DisplayMetrics dm = new DisplayMetrics();
//        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
//        wm.getDefaultDisplay().getMetrics(dm);
//        switch(dm.densityDpi){
//            case DisplayMetrics.DENSITY_HIGH:
//                mDialerSearchTextScale = 0.6f;
//                break;
//            case DisplayMetrics.DENSITY_LOW:
//                mDialerSearchTextScale = 1.3f;
//                break;
//            case DisplayMetrics.DENSITY_MEDIUM:
//                mDialerSearchTextScale = 1.0f;
//             // CR 138409, onResume is before onMeasure, so can't get width of digits in setFormattedDigits
//             // in mdpi the text width is fixed, but for other density, need to discuss with text view owner
//                if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
//                	mDigitsViewWidth = 202; 
//                } else {
//                	mDigitsViewWidth = 255;
//                }
//                break;
//        }

        
/* Dialer search begin - xiaodong wang 
	if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
        mContext = this;
        mQueryHandler = new QueryHandler(this);
        mAdapter = new DialerSearchListAdapter(this);
        //Just for Sinlge. Should be changed later
        if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
            mMatchedResultsCount = (TextView)findViewById(R.id.ds_result_count);
        } else {
        	mMatchedResultsCount = (TextView)findViewById(R.id.ds_result_count);
        }
        mResultCountsStr = getString(R.string.ds_matched_results);
        mList = (ListView) findViewById(R.id.ContactsList);
        Log.e(TAG, "onCreate() mList: "+mList);
//        mUserTips = (TextView) findViewById(R.id.user_tips);
        //head textView to results count 
        if (mMatchedResultsCount != null)
        	mMatchedResultsCount.setText("0" + mResultCountsStr);
        //foot button used to add contacts if dial pad hides
        mList.setHeaderDividersEnabled(true);
        mList.setFooterDividersEnabled(false);
        //set foot divider mentally, since list view cannot control footer divider separately
		View mSeparatorLine = new TextView(this);
		mSeparatorLine.setBackgroundColor(Color.parseColor("#FFC0C2C3"));
		((TextView)mSeparatorLine).setHeight(1);
		mList.addFooterView(mSeparatorLine, null, false);
        
        mAddToContacts = new LinearLayout(this);
        mAddToContacts.setPadding(20, 16, 20, 0);
		Button mAddToContactsBtn = new Button(this);
		mAddToContactsBtn.setText(R.string.ds_add_to_contacts);
		mAddToContactsBtn.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
		mAddToContactsBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
            	addToContactNumber = mDigits.getText().toString();
            	if ( addToContactNumber != null && addToContactNumber.length()>0)
//            		showDialog(ADD_TO_CONTACT_DIALOG);
                    onAddContacButtonClick();
			}
		});
        
		((LinearLayout)mAddToContacts).addView(mAddToContactsBtn);
        //In order to wipe adapter in ListView, we have to add and remove the button. 
        mList.addFooterView(mAddToContacts);
        mList.setAdapter(mAdapter);
        mList.removeFooterView(mAddToContacts);
        mBottomIsVisiable = false;
        
        mList.setOnItemClickListener(mOnClickListener);
        mList.setOnCreateContextMenuListener(this);
        mList.setOnScrollListener(mAdapter);

        mListLayout = (FrameLayout) findViewById(R.id.List);
//        dsListParam = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture);
//        mContentObserver = new SearchResultChangeObserver();
//    	mContext.getContentResolver().registerContentObserver(
//    			Uri.parse("content://com.android.contacts/dialer_search/filter/"), 
//    			true, mContentObserver);
    	mSearchNumCntQ = new LinkedList<Integer>();
	} else {
		Log.e(TAG, "Dialer search not support");
	}
 //Dialer search end - xiaodong wang 


 //Fion add start 
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
				mDigits = (EditText) findViewById(R.id.digits);
//				mDigits.setBackgroundColor(Color.argb(100, 0, 100, 0));
			} else {
            	mDigits = (EditText) findViewById(R.id.digitsGemini);
        	}
        }
        else
        {
            mDigits = (EditText) findViewById(R.id.digits);
        }
        mDigits.setOnClickListener(this);
        mDigits.setOnKeyListener(this);

//comment out by mtk80908, remove the text input limit. If no side effect, remove the code later.        
//        InputFilter[] nf = {this};
//        mDigits.setFilters(nf);
// mth80908 end        
        
//		mDigitsPaint = new Paint();
//		mDigitsPaint.set(mDigits.getPaint());
//		maxDigitsSize = mDigits.getTextSize() * mDialerSearchTextScale;
//		minDigitsSize = maxDigitsSize - 3*2;	// reduce 2 dip each time in resizeDigitSize()
//		currDigitsSize = maxDigitsSize;
        maybeAddNumberFormatting();

        // Check for the presence of the keypad
        View view = findViewById(R.id.one);
        if (view != null) {
            Log.e(TAG, "twelvekeydialer : one. view  ");
            setupKeypad();
        }

 //Fion add start 
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
            Log.e(TAG, "twelvekeydialer : view  gemini");
			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
				mAddToContactOnDigitButton = findViewById(R.id.addToContactOnDigitButton);
				mAddToContactOnDigitButton.setOnClickListener(this);
			    mDialButton1 = findViewById(R.id.dialsim1Button);
			    if (FeatureOption.MTK_VT3G324M_SUPPORT) {
			    	mDialButtonvt = findViewById(R.id.dialButtonVtAndMsg);
			    } else {
			    	mDialButtonMsg = findViewById(R.id.dialButtonVtAndMsg);
			    }
			} else {
				if( FeatureOption.MTK_VT3G324M_SUPPORT)
	            {
	            	mVoicemailDialAndDeleteRow = findViewById(R.id.call1call2vt);
	            	if (mVoicemailDialAndDeleteRow != null)
	            		mDialButtonvt = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButtonVt);
	            }
	            else
	            {
	            	mVoicemailDialAndDeleteRow = findViewById(R.id.call1call2);
	            }
		        // Check whether we should show the onscreen "Dial" button.
		        mDialButton1 = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButton1);
				view = findViewById(R.id.deleteButtonGemini);
		        view.setOnClickListener(this);
		        view.setOnLongClickListener(this);
		        mDelete = view;
			}
            //initVoicemailButton();

            mDialButton = null;
			
            // Check whether we should show the onscreen "Dial" button.
//            mDialButton1 = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButton1);
//            mDialButton2 = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButton2);
            
//            Added by lianyu.zhang start
//            if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
//            {
//            	if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
//            		mDialButtonvt = findViewById(R.id.dialButtonVtAndMsg);
//            	}else{
//            		mDialButtonvt = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButtonVt);
//            	}
//            }
//            Added by lianyu.zhang end

//            mDialButtonECC = mDialEccrow.findViewById(R.id.dialButtonEcc);			
//            mDialButtonSip = mDialEccrow.findViewById(R.id.dialButtonSip);
			
//            if (r.getBoolean(R.bool.config_show_onscreen_dial_button)) {
                mDialButton1.setOnClickListener(this);
//                mDialButton2.setOnClickListener(this);		
//                mDialButtonECC.setOnClickListener(this);	
//                mDialButtonSip.setOnClickListener(this);
//                Added by lianyu.zhang start
                if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
                {   
                	if (mDialButtonvt != null)
                		mDialButtonvt.setOnClickListener(this);
                } else {
        			if (mDialButtonMsg != null) {
        				((ImageView)mDialButtonMsg).setImageResource(R.drawable.phone_dial_message_button);
        				mDialButtonMsg.setOnClickListener(this);
        			}
                }
//                Added by lianyu.zhang end
//            } else {
//                mDialButton1.setVisibility(View.GONE); // It's VISIBLE by default
//                mDialButton1 = null;

//                mDialButton2.setVisibility(View.GONE); // It's VISIBLE by default
//                mDialButton2 = null;

//                mDialButtonECC.setVisibility(View.GONE); // It's VISIBLE by default
//                mDialButtonECC = null;
//                mDialButtonSip.setVisibility(View.GONE); // It's VISIBLE by default
//                mDialButtonSip = null;
                
//                Added by lianyu.zhang start
//                if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
//                {
//                	mDialButtonvt.setVisibility(View.GONE);
//                	mDialButtonvt = null;
//                }
//                Added by lianyu.zhang end
//            }

            Log.e(TAG, "twelvekeydialer : Gemini call1, call 2 view");
	
//            view = findViewById(R.id.deleteButtonGemini);
//            view.setOnClickListener(this);
//            view.setOnLongClickListener(this);
//            mDelete = view;

            Log.e(TAG, "twelvekeydialer : Gemini delete button gemini view");
				
        }
        else
        {		
            Log.e(TAG, "twelvekeydialer : view  not gemini");
			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
		        mDialButton = findViewById(R.id.dialsim1Button);
		        if (mDialButton != null)
		        	mDialButton.setOnClickListener(this);
		        mAddToContactButton = findViewById(R.id.addToContactOnDigitButton);
		        if (mAddToContactButton != null) 
		        	mAddToContactButton.setOnClickListener(this);
        		if (FeatureOption.MTK_VT3G324M_SUPPORT) {
        			mDialButtonvt = findViewById(R.id.dialButtonVtAndMsg);
        			if (mDialButtonvt != null)
        				mDialButtonvt.setOnClickListener(this);
        		} else {
        			mDialButtonMsg = findViewById(R.id.dialButtonVtAndMsg);
        			if (mDialButtonMsg != null) {
        				((ImageView)mDialButtonMsg).setImageResource(R.drawable.phone_dial_message_button);
        				mDialButtonMsg.setOnClickListener(this);
        			}
        		}
//		        mDialButtonvt = findViewById(R.id.dialButtonVtAndMsg);
//                if( FeatureOption.MTK_VT3G324M_SUPPORT == true && mDialButtonvt != null)
//                {
//                	mDialButtonvt.setOnClickListener(this);
//                } else if (mDialButtonvt != null) {
//                	mDialButtonvt.setEnabled(false);
//                }
			} else {
            	mVoicemailDialAndDeleteRow = findViewById(R.id.voicemailAndDialAndDelete);
			}
            initVoicemailButton();

            Log.e(TAG, "twelvekeydialer : view not gemini after bu1/bu2");
	
			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == false) {	
                // Check whether we should show the onscreen "Dial" button.
                mDialButton = mVoicemailDialAndDeleteRow.findViewById(R.id.dialButton);
			}

//            if (r.getBoolean(R.bool.config_show_onscreen_dial_button)) {
			if (mDialButton != null)    
				mDialButton.setOnClickListener(this);
//            } else {
//                mDialButton.setVisibility(View.GONE); // It's VISIBLE by default
//                mDialButton = null;
//            }

            Log.e(TAG, "twelvekeydialer : find deletebutton view");			
	
			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == false) {
                // xingping.zheng modify
                if(!isQVGAPlusQwerty())
                {
                    view = mVoicemailDialAndDeleteRow.findViewById(R.id.deleteButton);
                    Log.d(TAG, "findViewById : deleteButton view = "+view);
                }
                else
                {	
                    view = findViewById(R.id.textfieldDeleteButton);
                    Log.d(TAG, "findViewById : textfieldDeleteButton view = "+view);
                }
                
                if(view != null)
                {
                    view.setOnClickListener(this);
                    view.setOnLongClickListener(this);
                    mDelete = view;
                }
            }
        }

        //xingping.zheng add
        if(isQVGAPlusQwerty())
        {
            view = findViewById(R.id.addContactButton);
            view.setOnClickListener(this);
            mAddContact = view;
        }
		if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
//			mHideDialpadButton = findViewById(R.id.dialpadButton);
//			mHideDialpadButton.setOnClickListener(this);
		
//			 show dialpad button 
        	mShowDialpad = findViewById(R.id.showDialpad);
			mShowDialpadButton = findViewById(R.id.showDialpadButton);
			mShowDialEditText = (TextView)findViewById(R.id.digitsHide);
        	if (mShowDialpad != null) {
        		mShowDialpad.setVisibility(View.GONE);
        		mShowDialpad.setOnClickListener(this);        		
        	}
//        	if (mShowDialEditText != null) {
//        		mShowDialEditText.setInputType(android.text.InputType.TYPE_NULL);
//        	}
//		    mShowDialpadButton.setVisibility(View.GONE);

		    if(mShowDialpadButton !=null)
		    	mShowDialpadButton.setOnClickListener(this);
		    if (mBottomIsVisiable) {
		    	mList.removeFooterView(mAddToContacts);
		    	mBottomIsVisiable = false;
		    }
		    mDeleteOnDigitButton = findViewById(R.id.deleteOnDigitButton);
		    if(mDeleteOnDigitButton != null){
			    mDeleteOnDigitButton.setOnClickListener(this);
			    mDeleteOnDigitButton.setOnLongClickListener(this);
		    }
			mDialpad = findViewById(R.id.dial_search_pad);
			
			mSimInfoWrapper = ContactsUtils.SIMInfoWrapper.getDefault(this);
//			mContactsPrefs = new ContactsPreferences(this);
//	        mDisplayOrder = mContactsPrefs.getDisplayOrder();
//	        mSortOrder = mContactsPrefs.getSortOrder();
//			ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
//			mDialpad.setBackgroundDrawable(dw);
//			mDialpad.setBackgroundColor(Color.argb(255, 0, 0, 0));
		} else {
        	mDialpad = findViewById(R.id.dialpad);  // This is null in landscape mode.
		}
        // In landscape we put the keyboard in phone mode.
        // In portrait we prevent the soft keyboard to show since the
        // dialpad acts as one already.
        if (null == mDialpad) {
            Log.e(TAG, "twelvekeydialer : mDialpad = null");			
            mDigits.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        } else {
            Log.e(TAG, "twelvekeydialer : mDialpad != null");			        
            mDigits.setInputType(android.text.InputType.TYPE_NULL);
            mDigits.setCursorVisibleNoCheck(true);
        }

// Fion add end 

        Log.e(TAG, "twelvekeydialer : after view");

        // Set up the "dialpad chooser" UI; see showDialpadChooser().
        mDialpadChooser = (ListView) findViewById(R.id.dialpadChooser);
        mDialpadChooser.setOnItemClickListener(this);

        // xingping.zheng add CR:127118
        //mUseParentIntent = true;
        boolean ignoreState;
        //if (isChild()&& mUseParentIntent) {
        if (isChild()) {
            Intent intent = getParent().getIntent();
            ignoreState = intent.getBooleanExtra(DialtactsActivity.EXTRA_IGNORE_STATE, false);
        } else {
        	ignoreState = false;
        }
        if (!ignoreState && icicle != null) {
            super.onRestoreInstanceState(icicle);
        }

        try {
            mHaptic.init(this, r.getBoolean(R.bool.config_enable_dialer_key_vibration));
        } catch (Resources.NotFoundException nfe) {
             Log.e(TAG, "Vibrate control bool missing.", nfe);
        }

		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			mVoiceMailNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
					.getVoiceMailNumberGemini(Phone.GEMINI_SIM_1);
			mVoiceMailNumber2 = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
					.getVoiceMailNumberGemini(Phone.GEMINI_SIM_2);
		} else {
			mVoiceMailNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
					.getVoiceMailNumber();
		}
//		Adjust font size. Should be used in future. 
        if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT) {
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(dm);
			switch(dm.densityDpi){
			    case DisplayMetrics.DENSITY_HIGH:
			        mDialerSearchTextScale = 2.0f/3;
			        break;
			    case DisplayMetrics.DENSITY_LOW:
			        mDialerSearchTextScale = 4.0f/3;
			        break;
			    case DisplayMetrics.DENSITY_MEDIUM:
			        mDialerSearchTextScale = 1.0f;
			     // CR 138409, onResume is before onMeasure, so can't get width of digits in setFormattedDigits
			 // in mdpi the text width is fixed, but for other density, need to discuss with text view owner
			        if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
			        	mDigitsViewWidth = 202; 
			        } else {
			        	mDigitsViewWidth = 255;
			        }
			        break;
			}
			mDigitsPaint = new Paint();
			mDigitsPaint.set(mDigits.getPaint());
			maxDigitsSize = mDigits.getTextSize() * mDialerSearchTextScale;
			minDigitsSize = maxDigitsSize - 2*8;	// reduce 2 dip each time in resizeDigitSize()
			currDigitsSize = maxDigitsSize;
        }
//         added by xingping.zheng start
//        mSimAssociationQueryHandler = new SimAssociationQueryHandler(this, getContentResolver());
        mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        mCellConnMgr = new CellConnMgr(mServiceComplete);
        mCellConnMgr.register(getApplicationContext());
//         added by xingping.zheng end   
       Log.d(TAG, "-----------------------------------------end onCreate");*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "-----------------------------------------onDestroy()");
        if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
        	if (mPhotoLoader != null)
        		mPhotoLoader.stop();
        	if (mSimInfoWrapper != null)
        		mSimInfoWrapper.release();
        }
        if (mCellConnMgr != null)
        	mCellConnMgr.unregister();
        /*if (FeatureOption.MTK_GEMINI_SUPPORT) {
          unregisterReceiver(mSimIndicatorReceiver);
        }    */    
        setFirstLaunchCompleted(false);
        /* added by xingping.zheng start */
        if(mSimAssociationHandler != null)
            mSimAssociationHandler.sendEmptyMessage(MSG_EXIT);
        /* added by xingping.zheng end */
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle icicle) {
        // Do nothing, state is restored in onCreate() if needed
    }

/* Dialer search begin - xiaodong wang */
    private String tripNonDigit(String number) {
		StringBuilder sb = new StringBuilder();
		if (number == null) 
			return null;
		int len = number.length();
		
		for (int i = 0; i < len; i++) {
			char c = number.charAt(i);
//			if (PhoneNumberUtils.isISODigit(c)) {
			if (PhoneNumberUtils.isNonSeparator(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
    }
    
    private String tripHyphen(String number) {
		StringBuilder sb = new StringBuilder();
		if (number == null) 
			return null;
		int len = number.length();
		
		for (int i = 0; i < len; i++) {
			char c = number.charAt(i);
			if (c != '-' && c != ' ') {
				sb.append(c);
			}
		}
		return sb.toString();
    }
    
    private String tripNonDialableDigit(String number) {
		StringBuilder sb = new StringBuilder();
		if (number == null) 
			return null;
		int len = number.length();
		
		for (int i = 0; i < len; i++) {
			char c = number.charAt(i);
			if (PhoneNumberUtils.isDialable(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
    }
    
	
    private void resizeDigitSize() {
    	if (!FeatureOption.MTK_DIALER_SEARCH_SUPPORT)
    		return;
    	int mDigitWidth = (mDigits.getWidth() == 0) ? mDigitsViewWidth : mDigits.getWidth();
		int digitWidth = mDigitWidth - mDigits.getPaddingLeft() - mDigits.getPaddingRight();
		float realDigitWidth = (float)digitWidth * mDialerSearchTextScale;
		if (realDigitWidth <= 0)
			return;
		mDigitsPaint.setTextSize(currDigitsSize);
		String inputString = mDigits.getText().toString();
		float oneDigitWidth = 0;
		if (inputString.length() > 0) {
			oneDigitWidth = mDigitsPaint.measureText(inputString.substring(0, 1));
		}else{
			currDigitsSize = maxDigitsSize;
		}
		if (!mPressDelete) {	// need to cover copy/paste/cast cases
			// reduce text size
			while ((currDigitsSize > minDigitsSize) && 
					(mDigitsPaint.measureText(inputString) > realDigitWidth)) {
				currDigitsSize -= 2;
				if (currDigitsSize < minDigitsSize) {
					currDigitsSize = minDigitsSize;
					break;
				}
				mDigitsPaint.setTextSize(currDigitsSize);
			}
		} else {	// need to cover copy/paste/cast cases
			// enlarge text size
			while ((currDigitsSize < maxDigitsSize) && 
					(mDigitsPaint.measureText(inputString) < realDigitWidth  - 1.5*oneDigitWidth)) {
					// ALPS00138496: after enlarge may invoke the string out of the boundary of text view
				currDigitsSize += 2;
				if (currDigitsSize > maxDigitsSize){
					currDigitsSize = maxDigitsSize;
					break;
				}
				mDigitsPaint.setTextSize(currDigitsSize);
			}
		}
		
		mDigits.setTextSize(currDigitsSize);
//		mDigits.getHeight() -
//		mDigits.setPadding(left, top, right, bottom)
    }
    
    private boolean hasDigitString(String number) {
    	boolean hasDigit = false;
        for (int i = 0, count = number.length(); i < count; i++) {
//            if (PhoneNumberUtils.isISODigit(number.charAt(i))) {
        	if (PhoneNumberUtils.isNonSeparator(number.charAt(i))) {
            	hasDigit = true;
            }
        }
        return hasDigit;
    }
    
    class DsPhoneNumberFormatTextWatcher extends PhoneNumberFormattingTextWatcherEx {
    	private boolean mFormatting;
    	private int searchMode; 
    	
		public synchronized void afterTextChanged(Editable arg0) {
			// Make sure to ignore calls to afterTextChanged caused by the work done below, 
			// since text.delete, text.replace will invoke afterTextChanged too and make stack overflow!
			Log.e(TAG, "afterTextChanged"+arg0.toString());
			if(mDialed)
				mDialed = false;
			if (!mFormatting) {
				Log.e(TAG, "formatting");
				mFormatting = true;
				mDigitString = mDigits.getText().toString();
//				if (mDigitString.length() > MAX_DIGITS_DISPLAY_LENGTH && noMoreResult) {
//					// To accelerate, won't execute below action if disgits extend text view
//					// and there's no search result any more
//					mFormatting = false;
//					return;
//				}
//				log("text length: "+mDigits.getText().length()+"string length"+mDigitString.length());
				if(mDigitString.length() < MAX_DIGITS_DISPLAY_LENGTH)
					super.afterTextChanged(arg0);
				resizeDigitSize();
//				log("after format: "+arg0.toString());
				mDigitString = tripNonDigit(mDigitString);
				if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
					if (arg0.length() > 0) {
//						if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
//							((ImageView)mAddToContactOnDigitButton).setImageDrawable(getResources().getDrawable(R.drawable.add_to_contact_button_double));
//						} else {
//							((ImageView)mAddToContactButton).setImageDrawable(getResources().getDrawable(R.drawable.add_to_contact_icon_single));
//						}
						String digis = arg0.toString();
						startQuery(digis, searchMode);
					}  else if (arg0.length() == 0) {
//						if (FeatureOption.MTK_GEMINI_SUPPORT == true) {
//							((ImageView)mAddToContactOnDigitButton).setImageDrawable(getResources().getDrawable(R.drawable.add_to_contact_button_double_dis));
//						} else {
//							((ImageView)mAddToContactButton).setImageDrawable(getResources().getDrawable(R.drawable.add_to_contact_single_dis));
//						}
						mSearchNumberOnly = false;
						startQuery(null, DIALER_SEARCH_MODE_ALL);
					}
				}
				mFormatting = false;
			}
            mDigits.setCursorVisibleNoCheck(true);
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// TODO Auto-generated method stub
//			if (mDigits.getText().length() > MAX_DIGITS_DISPLAY_LENGTH && noMoreResult) {
//				return;
//			}
			int selIdex = Selection.getSelectionStart(s);
//			log("beforeTextChanged"+s.toString()+"start: "+start+"count: "+count+"after: "+after+"length: "+s.length()+"selection idx: "+selIdex);
			super.beforeTextChanged(s, start, count, after);
			//if (start + 1 < s.length()) {// add or delete number in the middler of digis number
			if (selIdex < s.length()) {
				mChangeInMiddle = true;
			}
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
//			if (mDigits.getText().length() > MAX_DIGITS_DISPLAY_LENGTH && noMoreResult) {
//				return;
//			}
//			log("onTextChanged"+s.toString()+"start: "+start+"before: "+before+"length: "+s.length());
			String digis = s.toString();
			super.onTextChanged(s, start, before, count);
			if (!mFormatting && digis.length() > 0) {
				Log.e(TAG, "Query");
				if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
					if (mSearchNumberOnly || count > 1 || mChangeInMiddle) {
						setSearchNumberOnly(); // pase action should also set flag
						searchMode = DIALER_SEARCH_MODE_NUMBER;
					} else {
						searchMode = DIALER_SEARCH_MODE_ALL;
					}
				}
			}
			mChangeInMiddle = false;
		}
    }
    
    
/* Dialer search end - xiaodong wang */

    protected void maybeAddNumberFormatting() {
    	mDigits.addTextChangedListener(new DsPhoneNumberFormatTextWatcher());
    }

    /**
     * Overridden by subclasses to control the resource used by the content view.
     */
    protected int getContentViewResource() {

/* Fion add start */
        if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        {
            Log.e(TAG, "getContentViewResource : gemini  ");
			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
				return R.layout.twelve_key_dialer_with_search_gemini;
			}            
			/*Added by lianyu.zhang start*/
            else if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
            {
            	return R.layout.twelve_key_dialer_gemini_vt;
            }
            else
            {
            	return R.layout.twelve_key_dialer_gemini;
            }
            /*Added by lianyu.zhang end*/
        }
        else
        {
            Log.e(TAG, "getContentViewResource : single sim ");
			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
				return R.layout.twelve_key_dialer_with_search_gemini;
			} else {            
            	return R.layout.twelve_key_dialer;
        	}
        }
/* Fion add end */
    }

    private void resolveIntent() {
        //boolean ignoreState = false;

        // Find the proper intent
        final Intent intent;
        //if (isChild() && mUseParentIntent) {
        if (isChild()) {
        	// xingping.zheng modify CR:127118
            intent = getParent().getIntent();
            //ignoreState = intent.getBooleanExtra(DialtactsActivity.EXTRA_IGNORE_STATE, false);
        } else {
            intent = getIntent();
        }
         Log.i(TAG, "cathon ==> resolveIntent(): intent: " + intent);

        // by default we are not adding a call.
        mIsAddCallMode = false;

        // By default we don't show the "dialpad chooser" UI.
        boolean needToShowDialpadChooser = false;

        // Resolve the intent
        final String action = intent.getAction();
        if (Intent.ACTION_DIAL.equals(action) || Intent.ACTION_VIEW.equals(action)) {
            // see if we are "adding a call" from the InCallScreen; false by default.
            mIsAddCallMode = intent.getBooleanExtra(ADD_CALL_MODE_KEY, false);

            Uri uri = intent.getData();
            if (uri != null) {
                if ("tel".equals(uri.getScheme())) {
                    // Put the requested number into the input area
                    String data = uri.getSchemeSpecificPart();
//                    setFormattedDigits(data);
                    if((intent.getFlags()&Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)==0 || false == FeatureOption.MTK_DIALER_SEARCH_SUPPORT)
                    	setFormattedDigits(data);
                    intent.setData(null);
                } else if ("voicemail".equals(uri.getScheme())) {
                    Log.d(TAG, "resolveIntent(): scheme == voicemail");
                    String data = uri.getSchemeSpecificPart();
                    Log.d(TAG, "resolveIntent(): data:" + data);
                    setFormattedDigits(data);
                } else {
                    String type = intent.getType();
                    if (People.CONTENT_ITEM_TYPE.equals(type)
                            || Phones.CONTENT_ITEM_TYPE.equals(type)) {
                        // Query the phone number
                        Cursor c = getContentResolver().query(intent.getData(),
                                new String[] {PhonesColumns.NUMBER}, null, null, null);
                        if (c != null) {
                            if (c.moveToFirst()) {
                                // Put the number into the input area
//                                setFormattedDigits(c.getString(0));
                            	if((intent.getFlags()&Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)==0 || false == FeatureOption.MTK_DIALER_SEARCH_SUPPORT)
                            		setFormattedDigits(c.getString(0));
                            }
                            c.close();
                        }
                    }
                    // xingping.zheng modify CR:225106
                    else if(CallLog.Calls.CONTENT_TYPE.equals(type))
                    {
                    	if (phoneIsInUse()) {
                            Log.i(TAG, "resolveIntent(): intent type "+type+" phone is in use; showing dialpad chooser!");
                            needToShowDialpadChooser = true;
                        }
                    }
                }
            } else {
                // ACTION_DIAL or ACTION_VIEW with no data.
                // This behaves basically like ACTION_MAIN: If there's
                // already an active call, bring up an intermediate UI to
                // make the user confirm what they really want to do.
                // Be sure *not* to show the dialpad chooser if this is an
                // explicit "Add call" action, though.
                if (!mIsAddCallMode && phoneIsInUse()) {
                    needToShowDialpadChooser = true;
                }
            }
        } else if (Intent.ACTION_MAIN.equals(action)) {
            // The MAIN action means we're bringing up a blank dialer
            // (e.g. by selecting the Home shortcut, or tabbing over from
            // Contacts or Call log.)
            //
            // At this point, IF there's already an active call, there's a
            // good chance that the user got here accidentally (but really
            // wanted the in-call dialpad instead).  So we bring up an
            // intermediate UI to make the user confirm what they really
            // want to do.
        	Log.i(TAG, "cathon ==> resolveIntent(): intent: main enter ");
			
            if (phoneIsInUse()) {
                 Log.i(TAG, "resolveIntent(): phone is in use; showing dialpad chooser!");
                needToShowDialpadChooser = true;
            }
        }

        // Bring up the "dialpad chooser" IFF we need to make the user
        // confirm which dialpad they really want.
        showDialpadChooser(needToShowDialpadChooser);

        //return ignoreState;
    }

    protected void setFormattedDigits(String data) {
        // strip the non-dialable numbers out of the data string.
    	String dialString = data;
    	if(!dialString.contains(",") && !dialString.contains(";")){
    		dialString = PhoneNumberUtils.extractNetworkPortionAlt(data);
    	}
//    	dialString = PhoneNumberUtils.formatNumber(dialString);
    	dialString = PhoneNumberFormatUtilEx.formatNumber(dialString);
        if (!TextUtils.isEmpty(dialString)) {
            Editable digits = mDigits.getText();
            setSearchNumberOnly();
            digits.replace(0, digits.length(), dialString);
            // for some reason this isn't getting called in the digits.replace call above..
            // but in any case, this will make sure the background drawable looks right
            afterTextChanged(digits);
            mEditNumber = tripNonDigit(digits.toString());
            resizeDigitSize();
        }
        mDigits.requestFocus();
        mDigits.setCursorVisibleNoCheck(true);
//        if (!isDigitsEmpty()) {
//            mDigits.setCursorVisible(true);
//        }
        mDigits.setSelection(0);
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
    	Log.d(TAG, "onNewIntent:" + newIntent);
        setIntent(newIntent);
        //mUseParentIntent = false;
        //resolveIntent();
        //mUseParentIntent = true;
    }

/*    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onPostCreate");
        super.onPostCreate(savedInstanceState);

        // This can't be done in onCreate(), since the auto-restoring of the digits
        // will play DTMF tones for all the old digits if it is when onRestoreSavedInstanceState()
        // is called. This method will be called every time the activity is created, and
        // will always happen after onRestoreSavedInstanceState().
//        mDigits.addTextChangedListener(this);
    }*/

/*    private void setupKeypad() {
        // Setup the listeners for the buttons
        View view = findViewById(R.id.one);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        findViewById(R.id.two).setOnClickListener(this);
        findViewById(R.id.three).setOnClickListener(this);
        findViewById(R.id.four).setOnClickListener(this);
        findViewById(R.id.five).setOnClickListener(this);
        findViewById(R.id.six).setOnClickListener(this);
        findViewById(R.id.seven).setOnClickListener(this);
        findViewById(R.id.eight).setOnClickListener(this);
        findViewById(R.id.nine).setOnClickListener(this);
        findViewById(R.id.star).setOnClickListener(this);

        view = findViewById(R.id.zero);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        findViewById(R.id.pound).setOnClickListener(this);
    }*/

    private void setupKeypad() {
        // Setup the listeners for the buttons
        View view = findViewById(R.id.one);
        view.setOnTouchListener(this);
        view.setOnKeyListener(this);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        findViewById(R.id.two).setOnTouchListener(this);
        findViewById(R.id.three).setOnTouchListener(this);
        findViewById(R.id.four).setOnTouchListener(this);
        findViewById(R.id.five).setOnTouchListener(this);
        findViewById(R.id.six).setOnTouchListener(this);
        findViewById(R.id.seven).setOnTouchListener(this);
        findViewById(R.id.eight).setOnTouchListener(this);
        findViewById(R.id.nine).setOnTouchListener(this);
        findViewById(R.id.star).setOnTouchListener(this);
        
        findViewById(R.id.two).setOnKeyListener(this);
        findViewById(R.id.three).setOnKeyListener(this);
        findViewById(R.id.four).setOnKeyListener(this);
        findViewById(R.id.five).setOnKeyListener(this);
        findViewById(R.id.six).setOnKeyListener(this);
        findViewById(R.id.seven).setOnKeyListener(this);
        findViewById(R.id.eight).setOnKeyListener(this);
        findViewById(R.id.nine).setOnKeyListener(this);
        findViewById(R.id.star).setOnKeyListener(this);

        view = findViewById(R.id.zero);
        view.setOnTouchListener(this);
        view.setOnKeyListener(this);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        // mtk80909 for Speed Dial
        findViewById(R.id.two).setOnLongClickListener(this);
        findViewById(R.id.three).setOnLongClickListener(this);
        findViewById(R.id.four).setOnLongClickListener(this);
        findViewById(R.id.five).setOnLongClickListener(this);
        findViewById(R.id.six).setOnLongClickListener(this);
        findViewById(R.id.seven).setOnLongClickListener(this);
        findViewById(R.id.eight).setOnLongClickListener(this);
        findViewById(R.id.nine).setOnLongClickListener(this);
        findViewById(R.id.two).setOnClickListener(this);
        findViewById(R.id.three).setOnClickListener(this);
        findViewById(R.id.four).setOnClickListener(this);
        findViewById(R.id.five).setOnClickListener(this);
        findViewById(R.id.six).setOnClickListener(this);
        findViewById(R.id.seven).setOnClickListener(this);
        findViewById(R.id.eight).setOnClickListener(this);
        findViewById(R.id.nine).setOnClickListener(this);
        findViewById(R.id.pound).setOnClickListener(this);
        findViewById(R.id.star).setOnClickListener(this);

        findViewById(R.id.pound).setOnTouchListener(this);
        findViewById(R.id.pound).setOnKeyListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "--------------------------------------------onResume()");
        if(!getNotFirstLaunch()) return;
        Log.e(TAG, "--------------------------------------------begin onResume()");

        // Query the last dialed number. Do it first because hitting
        // the DB is 'slow'. This call is asynchronous.
        queryLastOutgoingCall();
        /* added by xingping.zheng start */
        if(mSimAssociationHandler != null)
            mSimAssociationHandler.sendEmptyMessageDelayed(MSG_UPDATE_SIM_ASSOCIATION, UPDATE_SIM_ASSOCIATION_DELAY);
        /* added by xingping.zheng end */
        // retrieve the DTMF tone play back setting.
        mDTMFToneEnabled = Settings.System.getInt(getContentResolver(),
                Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
        
        //get IME service, used to check whether IME keyboard is turned on
        imeMgr = (InputMethodManager)this.getSystemService(this.INPUT_METHOD_SERVICE);
        
        // Retrieve the haptic feedback setting.
        mHaptic.checkSystemSetting();
        
        mHandler.sendEmptyMessage(LISTEN_PHONE_STATES);
        
        Activity parent = getParent();
        // See if we were invoked with a DIAL intent. If we were, fill in the appropriate
        // digits in the dialer field.
/*        if (parent != null && parent instanceof DialtactsActivity) {
            Uri dialUri = ((DialtactsActivity) parent).getAndClearDialUri();
            if (dialUri != null) {
                resolveIntent();
            }
        } */
//        if (!TextUtils.isEmpty(mInput)) {
//            mDigits.setText(mInput);
//        }
        // xiaodong wang: for dialer search, change screen will reset state of the screen.
        if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == false) {
            if (!TextUtils.isEmpty(mInput)) {
                mDigits.setTextKeepState(mInput);
            }
        } else {
        	if (mDigits.length() ==0) {
	            if (mMatchedResultsCount != null) {
	            	mMatchedResultsCount.setText("0" + mResultCountsStr);
	            }
		    	startQuery(null, DIALER_SEARCH_MODE_ALL);
			}
        }
        resolveIntent();
        mHandler.sendEmptyMessage(New_TONE_GENERATOR);

        // Potentially show hint text in the mDigits field when the user
        // hasn't typed any digits yet.  (If there's already an active call,
        // this hint text will remind the user that he's about to add a new
        // call.)
        //
        // TODO: consider adding better UI for the case where *both* lines
        // are currently in use.  (Right now we let the user try to add
        // another call, but that call is guaranteed to fail.  Perhaps the
        // entire dialer UI should be disabled instead.)
        if (phoneIsInUse()) {
            Log.e(TAG, "onresume :phone is inuse  ");
//          mDigits.setHint(R.string.dialerDialpadHintText);
            mDigits.setHint(null);
        } else {
            Log.e(TAG, "onresume :phone is not inuse  ");        
            // Common case; no hint necessary.
            mDigits.setHint(null);

            // Also, a sanity-check: the "dialpad chooser" UI should NEVER
            // be visible if the phone is idle!
            showDialpadChooser(false);
        }

        //mDigits.requestFocus();

        Log.e(TAG, "twelvekeydialer : onResume() before updatedialanddeletebuttonstate");			

        if(mDelete == null)
        {
            View view = null;
            if (FeatureOption.MTK_GEMINI_SUPPORT == true)
            {
                view = findViewById(R.id.deleteButtonGemini);
                Log.d(TAG, "retry get view = "+view);
                if(view != null)
                {
                    view.setOnClickListener(this);
                    view.setOnLongClickListener(this);
                    mDelete = view;
                }
            }
            else
            {
            	if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == false) {
                    if(!isQVGAPlusQwerty())
                    {
                        view = mVoicemailDialAndDeleteRow.findViewById(R.id.deleteButton);
                        Log.d(TAG, "retry get view = "+view);
                    }
                    else
                    { 	
                        view = findViewById(R.id.textfieldDeleteButton);
                        Log.d(TAG, "retry get view (QVGA-LAND) view = "+view);
                    }
                    if(view != null)
                    {
                        view.setOnClickListener(this);
                        view.setOnLongClickListener(this);
                        mDelete = view;
                    }
                }
            }
        }
		if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
			// Put before updateDialAndDeleteButtonEnabledState where will control dialer pad chooser visibility
//		    if (mShowDialpadButton.getVisibility() == View.VISIBLE && !dialpadChooserVisible()) {
//		    	mDialpad.setVisibility(View.GONE);        	
//		    }
			mContactsPrefs = new ContactsPreferences(this);
	        mDisplayOrder = mContactsPrefs.getDisplayOrder();
	        mSortOrder = mContactsPrefs.getSortOrder();
//		    if (mDigits.length() ==0) {
//		    	startQuery(null, DIALER_SEARCH_MODE_ALL);
//			}
		    mPhotoLoader.update();
		    mPhotoLoader.resume();
		    mPressQuickContactBadge = false;
        	noResultDigCnt = 0;
		    if (!dialpadChooserVisible()) {
	        	if(mShowDialpad != null)
	        		mShowDialpad.setVisibility(View.GONE);		    	
			    if (mBottomIsVisiable) {
			    	mList.removeFooterView(mAddToContacts);
			    	mBottomIsVisiable = false;
			    }
	        	mDialpad.setVisibility(View.VISIBLE);
//	        	dsListParam.height =  mListLayout.getHeight() - mDialpad.getHeight() + mDigits.getHeight();
//	        	log("Calc layout height:"+mListLayout.getHeight()+", "+ mDialpad.getHeight()+", "+ mDigits.getHeight());
//	        	mListLayout.setLayoutParams(dsListParam);
		    }
		}

		//Comment out by MTK80908, Since design has changed from GE and the receiver is useless, 
		//comment out this code		
        //if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        //{
        //	registerMMIReceiver();
        //}
		
        updateDialAndDeleteButtonEnabledState();
        mIsForeground = true;

//         added by xingping.zheng start 
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
        setSimIndicatorVisibility(true);
        mShowSimIndicator = true;
        }
//         added by xingping.zheng end   

        if(!mDigits.hasFocus())
        	mDigits.requestFocus();
        
        mDigits.setCursorVisibleNoCheck(true);
        
        //Add by mtk80908. Query to get or update voice mail number.
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			mVoiceMailNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
					.getVoiceMailNumberGemini(Phone.GEMINI_SIM_1);
			mVoiceMailNumber2 = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
					.getVoiceMailNumberGemini(Phone.GEMINI_SIM_2);
		} else {
			mVoiceMailNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
					.getVoiceMailNumber();
		}
        Log.e(TAG, "-----------------------------------------end onResume()");    
    }

/*private class SimIndicatorBroadcastReceiver extends BroadcastReceiver {
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
    
/*
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            // Hide soft keyboard, if visible (it's fugly over button dialer).
            // The only known case where this will be true is when launching the dialer with
            // ACTION_DIAL via a soft keyboard.  we dismiss it here because we don't
            // have a window token yet in onCreate / onNewIntent
            InputMethodManager inputMethodManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mDigits.getWindowToken(), 0);
        }
    } */
    @Override
    public void onBackPressed() {
    	Log.e(TAG, "------------------------------------begin onBackPressed(): getParent():" + getParent());
    	//Modified by mtk80908. 
    	//TBD: Need to check the change.
        getParent().moveTaskToBack(true);
        Log.e(TAG, "------------------------------------end onBackPressed()"); 
    }

    @Override
    protected void onPause() {
        mIsForeground = false;
        Log.e(TAG, "begin onPause()");
        super.onPause();
        mHandler.sendEmptyMessage(RELEASE_TONE_GENERATOR);
        mHandler.sendEmptyMessage(LISTEN_PHONE_NONE_STATES);
        // TODO: I wonder if we should not check if the AsyncTask that
        // lookup the last dialed number has completed.
        if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == false) {
        	if (mDigits != null)
        		mInput = mDigits.getText().toString();
        }
        mLastNumberDialed = EMPTY_NUMBER;  // Since we are going to query again, free stale number.
    
        // xingping.zheng add CR:127330
        dismissDialogs();
        
        //Comment out by MTK80908, Since design has changed from GE and the receiver is useless, 
        //comment out this code
        //if (FeatureOption.MTK_GEMINI_SUPPORT == true)
        //{
        //	unRegisterMMIReceiver();
        //}
        
        hideSpecialSequenceDialog();

		if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
		    // Reset dialer search info when exit the screen
			log("[onPause]mDialed " + mDialed);
			if (mPressQuickContactBadge==false && !mDialed) {
				if (mDigits != null)
					mDigits.getText().clear();
			    if (mShowDialEditText != null)
			    	mShowDialEditText.setText(null);
	            if (mBottomIsVisiable) {
    		    	mList.removeFooterView(mAddToContacts);
    		    	mBottomIsVisiable = false;
    		    }
	            if (mMatchedResultsCount != null) {
	            	mMatchedResultsCount.setText("0" + mResultCountsStr);
	            }
			    noResultDigCnt = 0;
			    //Do check null here to avoid Null Pointer exception.
			    //TBD: Maybe the code structure should be changed in future.
			    if (mSearchNumCntQ != null && mDigits != null 
			    		&& mMatchedResultsCount != null 
			    		&& mAdapter != null && mQueryHandler != null)
			    	startQuery(null, DIALER_SEARCH_MODE_ALL);
			}
			if(mDialed)
                mDialed = false;
			closeContextMenu();
//			if (mQueryHandler != null)
//				mQueryHandler.cancelOperation(QUERY_TOKEN);

		}
        /* added by xingping.zheng start */
        /*if (FeatureOption.MTK_GEMINI_SUPPORT) {
            setSimIndicatorVisibility(false);
            mShowSimIndicator = false;
        }*/
        ContactsUtils.dispatchActivityOnPause();
        /* added by xingping.zheng end   */
        Log.e(TAG, "end onPause()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        mAddToContactMenuItem = menu.add(0, MENU_ADD_CONTACTS, 0, R.string.recentCalls_addToContact)
//                .setIcon(android.R.drawable.ic_menu_add);
        m2SecPauseMenuItem = menu.add(0, MENU_2S_PAUSE, 0, R.string.add_2sec_pause)
                .setIcon(R.drawable.ic_menu_2sec_pause);
        mWaitMenuItem = menu.add(0, MENU_WAIT, 0, R.string.add_wait)
                .setIcon(R.drawable.ic_menu_wait);
        /*Added by lianyu.zhang start*/
//        if( FeatureOption.MTK_VT3G324M_SUPPORT == true && 
//        		FeatureOption.MTK_GEMINI_SUPPORT == false)
//        {
//        	mDialVTCall = menu.add(0,MENU_VT,0,"Dial Video Call");
//        }
        /*Added by lianyu.zhang end*/


        // mtk80909 for Speed Dial
        mSpeedDialManagement = menu.add(0, MENU_SPEED_DIAL, 0, R.string.menu_speed_dial)
        		.setIcon(R.drawable.contact_icon_speed_dial);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	// add by mtk80908. Avoid null point exception
    	// This is caused by layout inflate post-processing.
    	// TBD: We should review that codes later.
    	if (mDialpadChooser == null)
    		return false;
        // We never show a menu if the "choose dialpad" UI is up.
        if (dialpadChooserVisible()) {
            return false;
        }

        // mtk80909 for Speed Dial
        mSpeedDialManagement.setVisible(true);

        if (isDigitsEmpty()) {
//            mAddToContactMenuItem.setVisible(false);
            m2SecPauseMenuItem.setVisible(false);
            mWaitMenuItem.setVisible(false);
            /*Added by lianyu.zhang start*/
//            if( FeatureOption.MTK_VT3G324M_SUPPORT == true && 
//            		FeatureOption.MTK_GEMINI_SUPPORT == false)
//            {
//            	mDialVTCall.setVisible(false);
//            }
            /*Added by lianyu.zhang end*/
        } else {
        	
        	/*Added by lianyu.zhang start*/
//            if( FeatureOption.MTK_VT3G324M_SUPPORT == true && 
//            		FeatureOption.MTK_GEMINI_SUPPORT == false)
//            {
//            	mDialVTCall.setVisible(true);
//            }
            /*Added by lianyu.zhang end*/
        	
            CharSequence digits = mDigits.getText();

            // Put the current digits string into an intent
            Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
            intent.putExtra(Insert.PHONE, digits);
            intent.setType(People.CONTENT_ITEM_TYPE);
//            mAddToContactMenuItem.setIntent(intent);
//            mAddToContactMenuItem.setVisible(true);

            // Check out whether to show Pause & Wait option menu items
            int selectionStart;
            int selectionEnd;
            String strDigits = digits.toString();

            selectionStart = mDigits.getSelectionStart();
            selectionEnd = mDigits.getSelectionEnd();

            if (selectionStart != -1) {
                if (selectionStart > selectionEnd) {
                    // swap it as we want start to be less then end
                    int tmp = selectionStart;
                    selectionStart = selectionEnd;
                    selectionEnd = tmp;
                }

                if (selectionStart != 0) {
                    // Pause can be visible if cursor is not in the begining
                    m2SecPauseMenuItem.setVisible(true);

                    // For Wait to be visible set of condition to meet
                    mWaitMenuItem.setVisible(showWait(selectionStart,
                                                      selectionEnd, strDigits));
                } else {
                    // cursor in the beginning both pause and wait to be invisible
                    m2SecPauseMenuItem.setVisible(false);
                    mWaitMenuItem.setVisible(false);
                }
            } else {
                // cursor is not selected so assume new digit is added to the end
                int strLength = strDigits.length();
                mWaitMenuItem.setVisible(showWait(strLength,
                                                      strLength, strDigits));
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                long callPressDiff = SystemClock.uptimeMillis() - event.getDownTime();
                if (callPressDiff >= ViewConfiguration.getLongPressTimeout()) {
                    // Launch voice dialer
                    Intent intent = new Intent(Intent.ACTION_VOICE_COMMAND);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                    }
                }
                return true;
            }
            case KeyEvent.KEYCODE_1: {
//                long timeDiff = SystemClock.uptimeMillis() - event.getDownTime();
//                if (timeDiff >= ViewConfiguration.getLongPressTimeout()) {
//                    // Long press detected, call voice mail
//                    callVoicemail();
//                    return true;
//                }
                break;
            }
            //Add by mtk80908. Just for CRs alps00038613.
            //To do: check it and change it in a better way in future if it has.
            case KeyEvent.KEYCODE_MENU:{
            	//Disable menu button if dialpad is hide.
            	if (mShowDialpad !=null && mShowDialpad.getVisibility() == View.VISIBLE)
            		return true;
            	
            	if(mDigits != null) {
                	mDigits.requestFocus();
                	mDigits.setCursorVisibleNoCheck(true);
            	}
            	
            	break;
            }
        }
        	return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                // TODO: In dialButtonPressed we do some of these
                // tests again. We should try to consolidate them in
                // one place.
                if (!phoneIsCdma() && mIsAddCallMode && isDigitsEmpty()) {
                    // For CDMA phones, we always call
                    // dialButtonPressed() because we may need to send
                    // an empty flash command to the network.
                    // Otherwise, if we are adding a call from the
                    // InCallScreen and the phone number entered is
                    // empty, we just close the dialer to expose the
                    // InCallScreen under it.
                    finish();
                }

                // If we're CDMA, regardless of where we are adding a call from (either
                // InCallScreen or Dialtacts), the user may need to send an empty
                // flash command to the network. So let's call dialButtonPressed() regardless
                // and dialButtonPressed will handle this functionality for us.
                // otherwise, we place the call.
                dialButtonPressed();
                return true;
            }
        }
        	return super.onKeyUp(keyCode, event);
    }

    private void keyPressed(int keyCode) {
//        mHaptic.vibrate(); When press button in the dialer screen, the tone and vibration is not match
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        mDigits.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        Log.d(TAG, "key code : "+event.getKeyCode());
        if(event.getAction() == KeyEvent.ACTION_MULTIPLE && event.getKeyCode() == 0)
        {
            return true;
        }
    	
        return super.dispatchKeyEvent(event);
    }
    
    public boolean onKey(View view, int keyCode, KeyEvent event) {
    	
    	Log.d(TAG, "key code : " +event.getKeyCode() + ", keyCode:" + keyCode + ", event action:" + event.getAction() + ", view id:" + view.getId() + ",key repeat count:" + event.getRepeatCount());
        switch (view.getId()) {
            case R.id.digits:
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
//                    dialButtonPressed();
//            		final String number = mDigits.getText().toString();
//            		log("twelvekeydialer onKey : number " + number);
//                    if (!TextUtils.isEmpty(number)) {
//                        if (FeatureOption.MTK_GEMINI_SUPPORT) {
//                        	onDialButtonClickInt(number);
//                        } else {
//                        	dialButtonPressed();
//                        }
//                    }
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(mDigits.getWindowToken(), 0);
                    return true;
                }
                break;
        }
        
        if (KeyEvent.ACTION_DOWN == event.getAction() && KeyEvent.KEYCODE_DPAD_CENTER == keyCode && 0 == event.getRepeatCount()) {
        	switch (view.getId()) {
        	case R.id.one:
                 playTone(ToneGenerator.TONE_DTMF_1);
                 mHaptic.vibrate();
                 break;
        	case R.id.two:
                 playTone(ToneGenerator.TONE_DTMF_2);
                 mHaptic.vibrate();
                 break;
        	case R.id.three:
                 playTone(ToneGenerator.TONE_DTMF_3);
                 mHaptic.vibrate();
                 break;        		 
        	case R.id.four:
                 playTone(ToneGenerator.TONE_DTMF_4);
                 mHaptic.vibrate();
                 break;        		
        	case R.id.five:
                 playTone(ToneGenerator.TONE_DTMF_5);
                 mHaptic.vibrate();
                 break;        		
        	case R.id.six:
                 playTone(ToneGenerator.TONE_DTMF_6);
                 mHaptic.vibrate();
                 break;        		        	
        	case R.id.seven:
                 playTone(ToneGenerator.TONE_DTMF_7);
                 mHaptic.vibrate();
                 break;        		
        	case R.id.eight:
                 playTone(ToneGenerator.TONE_DTMF_8);
                 mHaptic.vibrate();
                 break;         	
        	case R.id.nine:
                 playTone(ToneGenerator.TONE_DTMF_9);
                 mHaptic.vibrate();
                 break;
        	case R.id.zero:
                 playTone(ToneGenerator.TONE_DTMF_0);
                 mHaptic.vibrate();
                 break;        		
        	case R.id.star:
                 playTone(ToneGenerator.TONE_DTMF_S);
                 mHaptic.vibrate();
                 break;         		
        	case R.id.pound:
                 playTone(ToneGenerator.TONE_DTMF_P);
                 mHaptic.vibrate();
                 break; 
        	}
        } else if (KeyEvent.ACTION_UP == event.getAction() && KeyEvent.KEYCODE_DPAD_CENTER == keyCode) {
        	switch (view.getId()) {
        	case R.id.two:
        		 keyPressed(KeyEvent.KEYCODE_2);
        		 break;
        	case R.id.three:
       		     keyPressed(KeyEvent.KEYCODE_3);
    		     break;
        	case R.id.four:
      		     keyPressed(KeyEvent.KEYCODE_4);
    		     break;        		
        	case R.id.five:
      		     keyPressed(KeyEvent.KEYCODE_5);
    		     break;
        	case R.id.six:
      		     keyPressed(KeyEvent.KEYCODE_6);
    		     break;
        	case R.id.seven:
      		     keyPressed(KeyEvent.KEYCODE_7);
    		     break;
        	case R.id.eight:
     		     keyPressed(KeyEvent.KEYCODE_8);
    		     break;
        	case R.id.nine:
     		     keyPressed(KeyEvent.KEYCODE_9);
    		     break;
        	case R.id.star:
        		 keyPressed(KeyEvent.KEYCODE_STAR);
        		 break;
        	case R.id.pound:
        		 keyPressed(KeyEvent.KEYCODE_POUND);
        		 break;
        	}        	
        }
/*
        if(mDigits.hasFocus())
        {
        	if(event.isDown())
        		return mTwelvedialerKeyListener.onKeyDown(event);
        	else
        		return mTwelvedialerKeyListener.onKeyUp(event);
        }
        else*/
        	return false;
    }

    private CharSequence createErrorMessage(String number) {
        if (!TextUtils.isEmpty(number)) {
            return getString(R.string.dial_emergency_error, mLastNumber);
        } else {
            return getText(R.string.dial_emergency_empty_error).toString();
        }
    }

    // xingping.zheng CR:127330
    protected void dismissDialogs()
    {
        if(null != mCanDismissDialog)
        {
            mCanDismissDialog.dismiss();
            mCanDismissDialog = null;
        }
        
        if(mCallSelectionDialog != null && mCallSelectionDialog.isShowing()) {
            mCallSelectionDialog.dismiss();
            mCallSelectionDialog = null;
        }
        
        if(mCallSelectionDialogOther != null && mCallSelectionDialogOther.isShowing()) {
            mCallSelectionDialogOther.dismiss();
            mCallSelectionDialogOther = null;
        }
        
        if(mAddContactDialog != null && mAddContactDialog.isShowing()) {
            mAddContactDialog.dismiss();
            mAddContactDialog = null;
        }
        
        if(mTurnOnSipDialog != null && mTurnOnSipDialog.isShowing()) {
            mTurnOnSipDialog.dismiss();
            mTurnOnSipDialog = null;
        }

        if(mTurnOn3GServiceDialog != null && mTurnOn3GServiceDialog.isShowing()) {
            mTurnOn3GServiceDialog.dismiss();
            mTurnOn3GServiceDialog = null;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog dialog = null;
        CharSequence[] dialogItems = new CharSequence[2]; 
        switch(id)
        {
        case BAD_EMERGENCY_NUMBER_DIALOG:
            {
            // construct dialog
            dialog = new AlertDialog.Builder(this)
                    .setTitle(getText(R.string.emergency_enable_radio_dialog_title))
                    .setMessage(createErrorMessage(mLastNumber))
                    .setPositiveButton(R.string.ok, null)
                    .setCancelable(true).create();

            // blur stuff behind the dialog
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            mCanDismissDialog = dialog;
        }
            break;
        case ERROR_POWER_OFF_DIALOG:
            {
            	// construct dialog
                dialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.error_power_off_message)
                        .setPositiveButton(R.string.ok, null)
                        .setCancelable(true).create();

                // blur stuff behind the dialog
                dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                mRadioOffAlertDialog = dialog;
            }
            break;
        case ADN_DIALOG:
            {	
            	CharSequence adn1, adn2;
            	if(mDialogBundle != null)
            	{
            	    StringBuilder builder1 = new StringBuilder();
            	    StringBuilder builder2 = new StringBuilder();
            	    adn1 = mDialogBundle.getCharSequence("ADN1");
            	    adn2 = mDialogBundle.getCharSequence("ADN2");
            	    builder1.append("SIM1: ");
            		
            	    if(adn1 != null)
            		builder1.append(adn1);
            	    else
            	        builder1.append(getResources().getString(R.string.imei_invalid_message));
            		
            	    builder2.append("SIM2: ");
            	    if(adn2 != null)
            		builder2.append(adn2);
            	    else
            	        builder2.append(getResources().getString(R.string.imei_invalid_message));
            		
            	    dialogItems[0] = builder1.toString();
            	    dialogItems[1] = builder2.toString();
            	}
            	dialog = new AlertDialog.Builder(this)
	                                    .setTitle(R.string.adn_dialog_title)
	                                    .setItems(dialogItems, this)
	                                    .setCancelable(true)
	                                    .create();
            	mADNDialog = dialog;
            	dialog.setOnShowListener(this);
            }
            break;
        case VOICE_MAIL_DIALOG:
            {
            	CharSequence voicemail1, voicemail2;
            	if(mDialogBundle != null)
            	{
                    StringBuilder builder1 = new StringBuilder();
            	    StringBuilder builder2 = new StringBuilder();
            	    voicemail1 = mDialogBundle.getCharSequence("voicemail0");
            	    voicemail2 = mDialogBundle.getCharSequence("voicemail1");
            	    builder1.append("SIM1: ");
            		
            	    if(voicemail1 != null && voicemail1.length() > 0)
            		builder1.append(voicemail1);
            	    else
            		builder1.append(getResources().getString(R.string.imei_invalid_message));
            		
            	    builder2.append("SIM2: ");
            	    if(voicemail2 != null && voicemail2.length()>0)
            	        builder2.append(voicemail2);
            	    else
            	        builder2.append(getResources().getString(R.string.imei_invalid_message));
            		
            	    dialogItems[0] = builder1.toString();
            	    dialogItems[1] = builder2.toString();
            	}
            	dialog = new AlertDialog.Builder(this)
	                                    .setTitle(R.string.voice_mail_dialog_title)
	                                    .setItems(dialogItems, this)
	                                    .setCancelable(true)
	                                    .create();
            	mVoiceMailDialog = dialog;
            	dialog.setOnShowListener(this);   
            }
        	break;
        case MMI_DIALOG:
            {
                SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(this, Phone.GEMINI_SIM_1);
                if(simInfo != null)
                    dialogItems[0] = simInfo.mDisplayName;
                simInfo = SIMInfo.getSIMInfoBySlot(this, Phone.GEMINI_SIM_2);
                if(simInfo != null)
                    dialogItems[1] = simInfo.mDisplayName;
                dialog = new AlertDialog.Builder(this)
                                        .setTitle(R.string.pin_dialog_title)
                                        .setItems(dialogItems, this)
                                        .setCancelable(true)
                                        .create();
                mMMIDialog = dialog;
                mMMIDialog.setOnShowListener(this);
            }
            break;
        case IMEI_DIALOG:
            {
            	CharSequence imei1, imei2;
            	if(mDialogBundle != null)
            	{
            	    StringBuilder builder1 = new StringBuilder();
            	    StringBuilder builder2 = new StringBuilder();
            	    imei1 = mDialogBundle.getCharSequence("IMEI1");
            	    imei2 = mDialogBundle.getCharSequence("IMEI2");
            		
            	    builder1.append("IMEI1: ");
            	    if(imei1 != null)
            	        builder1.append(imei1);
            	    else
            	        builder1.append(getResources().getString(R.string.imei_invalid_message));
            		
            	    builder2.append("IMEI2: ");
            	    if(imei2 != null)
            	        builder2.append(imei2);
            	    else
            		builder2.append(getResources().getString(R.string.imei_invalid_message));
            		
            	    dialogItems[0] = builder1.toString();
            	    dialogItems[1] = builder2.toString();
            	}
            	
            	dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.imei_dialog_title)
                .setItems(dialogItems, null)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(true)
                .create();
                dialog.getListView().setEnabled(false);
                mIMEIDialog = dialog;
                mIMEIDialog.setOnShowListener(this);
                //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
            }
        	break;
            case MISSING_VOICEMAIL_DIALOG:
            {
            	dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.no_vm_number)
                .setMessage(R.string.no_vm_number_msg)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //if (VDBG) log("Missing voicemail AlertDialog: POSITIVE click...");
                            //msg.sendToTarget();  // see dontAddVoiceMailNumber()
                            //PhoneApp.getInstance().pokeUserActivity();
                        }})
                .setNegativeButton(R.string.add_vm_number_str,
                                   new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //if (VDBG) log("Missing voicemail AlertDialog: NEGATIVE click...");
                            //msg2.sendToTarget();  // see addVoiceMailNumber()
                            //PhoneApp.getInstance().pokeUserActivity();
                        	Intent intent = new Intent("com.android.phone.CallFeaturesSetting.ADD_VOICEMAIL");
        	        		intent.setClassName("com.android.phone", "com.android.phone.VoiceMailSetting");
        	        		intent.putExtra(Phone.GEMINI_SIM_ID_KEY, mSimNo);
        	        		startActivity(intent);
        	        		mSimNo = -1;
                        }})
                .setOnCancelListener(new OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            //if (VDBG) log("Missing voicemail AlertDialog: CANCEL handler...");
                            //msg.sendToTarget();  // see dontAddVoiceMailNumber()
                            //PhoneApp.getInstance().pokeUserActivity();
                        }})
                .create();
            	mMissingVoicemailDialog = dialog;
            	//mtk71029
            	if(FeatureOption.MTK_GEMINI_SUPPORT){
            		updateMissingVoiceMailDialog(mSimNo);
            	}
            }
            break;
            case R.id.dialog_delete_contact_confirmation: {
            	dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.deleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                new DeleteClickListener()).create();
            	mDeleteContactDlg = dialog;
                break;
            }
            case ADD_TO_CONTACT_DIALOG: {
            	String fnumber = "\u202D" + addToContactNumber + "\u202C";
                final CharSequence message = getResources().getString(
                        R.string.add_contact_dlg_message_fmt, fnumber);
                dialog = new AlertDialog.Builder(this)
            		.setTitle(R.string.recentCalls_addToContact)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setMessage(message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok,
                            new AddContactClickListener()).create();
                mAddToContactDlg = dialog;
                break;
            }
        }
        return dialog;
    }

    // xingping.zheng add for CR:127337
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args)
    {
    	switch (id) {
	    	case BAD_EMERGENCY_NUMBER_DIALOG:{
    	    mCanDismissDialog = dialog;
    	    ((AlertDialog)dialog).setMessage(createErrorMessage(mLastNumber));
	    	    break;
	    	}
		    case ADD_TO_CONTACT_DIALOG: {
            	String fnumber = "\u202D" + addToContactNumber + "\u202C";
                final CharSequence message = getResources().getString(
                        R.string.add_contact_dlg_message_fmt, fnumber);
		        ((AlertDialog)dialog).setMessage(message);
		        break;
		    }
    	}
    }

    public void onClick(View view) {
        //Log.d(TAG, "onClick() view:" + view.getId());
    	mPressDelete = false;
        switch (view.getId()) {
            case R.id.one: {
                keyPressed(KeyEvent.KEYCODE_1);
                Log.d(TAG, "onClick() 'one'");
                return;
            }
            case R.id.zero: {
                keyPressed(KeyEvent.KEYCODE_0);
                Log.d(TAG, "onClick() 'zero'");
                return;
            }

            // mtk80909 for Speed Dial
            case R.id.two: {
                keyPressed(KeyEvent.KEYCODE_2);
                Log.d(TAG, "onClick() 'two'");
                return;
            }
            case R.id.three: {
                keyPressed(KeyEvent.KEYCODE_3);
                Log.d(TAG, "onClick() 'three'");
                return;
            }
            case R.id.four: {
                keyPressed(KeyEvent.KEYCODE_4);
                Log.d(TAG, "onClick() 'four'");
                return;
            }
            case R.id.five: {
                keyPressed(KeyEvent.KEYCODE_5);
                Log.d(TAG, "onClick() 'five'");
                return;
            }
            case R.id.six: {
                keyPressed(KeyEvent.KEYCODE_6);
                Log.d(TAG, "onClick() 'six'");
                return;
            }
            case R.id.seven: {
                keyPressed(KeyEvent.KEYCODE_7);
                Log.d(TAG, "onClick() 'seven'");
                return;
            }
            case R.id.eight: {
                keyPressed(KeyEvent.KEYCODE_8);
                Log.d(TAG, "onClick() 'eight'");
                return;
            }
            case R.id.nine: {
                keyPressed(KeyEvent.KEYCODE_9);
                Log.d(TAG, "onClick() 'nine'");
                return;
            }
            case R.id.pound: {
                keyPressed(KeyEvent.KEYCODE_POUND);
                Log.d(TAG, "onClick() 'pound'");
                return;
            }
            case R.id.star: {
                keyPressed(KeyEvent.KEYCODE_STAR);
                Log.d(TAG, "onClick() 'star'");
                return;
            }
            
            
            case R.id.deleteButton: {
            	mPressDelete = true;
                keyPressed(KeyEvent.KEYCODE_DEL);
                mHaptic.vibrate();
                Log.d(TAG, "onClick() 'deleteButton'");
                return;
            }
            // xingping.zheng add
            case R.id.textfieldDeleteButton: {
                mPressDelete = true;
                keyPressed(KeyEvent.KEYCODE_DEL);
                mHaptic.vibrate();
                Log.d(TAG, "onClick() 'textfieldDeleteButton'");
                return;
            }
            case R.id.dialButton: {
                dialButtonPressed();
                mHaptic.vibrate();  // Vibrate here too, just like we do for the regular keys
                Log.d(TAG, "onClick() 'dialButton'");
                return;
            }
            case R.id.voicemailButton: {
                callVoicemail();
                mHaptic.vibrate();
                Log.d(TAG, "onClick() 'voicemailButton'");
                return;
            }
            case R.id.dialButton1: {
                dialButtonPressedext(Phone.GEMINI_SIM_1);
                mHaptic.vibrate();  // Vibrate here too, just like we do for the regular keys	
                Log.d(TAG, "onClick() 'dialButton1'"); 
                return;
            }
            case R.id.dialButton2: {
                dialButtonPressedext(Phone.GEMINI_SIM_2);	
                mHaptic.vibrate();  // Vibrate here too, just like we do for the regular keys 
                Log.d(TAG, "onClick() 'dialButton2'");
                return;
            }
            
            /*Added by lianyu.zhang start*/
            case R.id.dialButtonVtAndMsg: {
            	if (FeatureOption.MTK_VT3G324M_SUPPORT) {
            		onVideoCallButtonClick();
            	} else {
            		// send message. TBD.function
            		String number = mDigits.getText().toString();
            		Uri msgUri = Uri.fromParts("sms", number, null);
            		Intent intent = new Intent(Intent.ACTION_SENDTO, msgUri);
            		startActivity(intent);
            	}
            	mHaptic.vibrate();
            	return;
            }
            /*Added by lianyu.zhang end*/
            
            case R.id.dialButtonEcc: {
                Log.d(TAG, "onClick() 'dialButtonEcc'");
                mHaptic.vibrate();  // Vibrate here too, just like we do for the regular keys

            Log.w(TAG, "dialButtonEcc check if emergency number, ");                

            mLastNumber = mDigits.getText().toString();
            if (mLastNumber!= null && mLastNumber.length() > 0 && !PhoneNumberUtils.isEmergencyNumber(mLastNumber)) {

                mDigits.getText().delete(0, mDigits.getText().length());
                showDialog(BAD_EMERGENCY_NUMBER_DIALOG);
				
                return ;

            }
				
                if (radio1_on) 
                {
                    dialButtonPressedext(Phone.GEMINI_SIM_1);
                }
                else if (radio2_on) 
                {
                    dialButtonPressedext(Phone.GEMINI_SIM_2);
                }
                else 
                {
                    int dualSimModeSetting = Settings.System.getInt(
                            getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING, MODE_DUAL_SIM);

                    if (dualSimModeSetting == MODE_SIM2_ONLY)
                    {
                        dialButtonPressedext(Phone.GEMINI_SIM_2);                    
                    }
                    else
                    {
                        dialButtonPressedext(Phone.GEMINI_SIM_1);                    
                    }
                }                
                Log.w(TAG, "dialButtonEcc , radio1_on :"+radio1_on+"radio2_on:"+radio2_on);                
                return;
            }
			
            case R.id.deleteButtonGemini: {
                Log.d(TAG, "onClick() 'deleteButtonGemini'");
            	mPressDelete = true;
                keyPressed(KeyEvent.KEYCODE_DEL);
                mHaptic.vibrate();
                return;
            }		
            //xingping.zheng add
            case R.id.addContactButton:{
                Log.d(TAG, "onClick() 'addContactButton'");
                final String number = mDigits.getText().toString();
                Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                intent.setType(Contacts.CONTENT_ITEM_TYPE);
                intent.putExtra(Insert.PHONE, number);
                startActivity(intent);
                return;
            }
/* Fion add end */	
/* Dialer search begin - xiaodong wang */
            /* For dual sim */
            case R.id.dialsim1Button: {
                Log.d(TAG, "onClick() 'dialsim1Button'");
                mHaptic.vibrate();  // Vibrate here too, just like we do for the regular keys
                //dialButtonPressedext(Phone.GEMINI_SIM_1);
                if(FeatureOption.MTK_GEMINI_SUPPORT)
                    onDialButtonClick();
                else
                    dialButtonPressed();
                return;
            }
            case R.id.dialsim2Button: {
                Log.d(TAG, "onClick() 'dialsim2Button'");
            	mHaptic.vibrate();
            	dialButtonPressedext(Phone.GEMINI_SIM_2);
            	return;
            }
            /* For single sim */
            case R.id.dialpadButton: {
                Log.d(TAG, "onClick() 'dialpadButton'");
            	Log.e(TAG, "onClick: dialpadButton");
            	mHaptic.vibrate();
            	mDialpad.setVisibility(View.GONE);
            	if (mShowDialpad != null)
            		mShowDialpad.setVisibility(View.VISIBLE);
            	if(mShowDialEditText != null)
            		mShowDialEditText.setText(mDigits.getText());
//            	mShowDialpadButton.setVisibility(View.VISIBLE);
//            	log(" :"+mListLayout.getHeight()+", "+ mShowDialpadButton.getHeight());		    	
//            	dsListParam.height = 394 - mShowDialpadButton.getHeight();
//		    	mListLayout.setLayoutParams(dsListParam);
            	return;
            }
            case R.id.showDialpadButton:
            case R.id.showDialpad: {
                Log.d(TAG, "onClick() 'showDialpadButton'");
            	mHaptic.vibrate();
            	if (mShowDialpad != null)
            		mShowDialpad.setVisibility(View.GONE);            	
//            	mShowDialpadButton.setVisibility(View.GONE);
            	mDialpad.setVisibility(View.VISIBLE);
//            	mList.removeFooterView(mAddToContacts);
    		    if (mBottomIsVisiable) {
    		    	mList.removeFooterView(mAddToContacts);
    		    	mBottomIsVisiable = false;
    		    }
            	mDigits.requestFocus();
//            	dsListParam.height = 195;
//            	mListLayout.setLayoutParams(dsListParam);
            	return;
            }
            case R.id.deleteOnDigitButton: {
                Log.d(TAG, "onClick() 'deleteOnDigitButton'");
//              keyPressed(KeyEvent.KEYCODE_DEL);
            	mPressDelete = true;
                KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
                mDigits.onKeyDown(KeyEvent.KEYCODE_DEL, event);
                return;
            }
            case R.id.addToContactOnDigitButton:
            case R.id.addToContactButton: {
                Log.d(TAG, "onClick() 'addToContactButton' or 'addToContactOnDigitButton'");
                /*
                addToContactNumber = mDigits.getText().toString();
                showDialog(ADD_TO_CONTACT_DIALOG);
                */
                onAddContacButtonClick();
                return;
            }
/* Dialer search end - xiaodong wang */
            case R.id.digits: {
                Log.d(TAG, "onClick() 'digits'");
//                if (!isDigitsEmpty()) {
//                    mDigits.setCursorVisible(true);
//                }
//                mDigits.setCursorVisible(true);
                if (mDigits != null) {
                	mDigits.requestFocus();
                	mDigits.setCursorVisibleNoCheck(true);
                }
                return;
            }
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
    	mPressDelete = false;
    	if (event.getAction() == MotionEvent.ACTION_DOWN) {
        	//Log.d(TAG, "begin onTouch() view:" + view.getId() + "event.getAction()" + "MotionEvent.ACTION_DOWN");
    		switch (view.getId()) {
            case R.id.one: {
            	Log.d(TAG, "on Touch 'one'");
                playTone(ToneGenerator.TONE_DTMF_1);
                //mHaptic.vibrate();
                break;
            }
            case R.id.two: {
            	Log.d(TAG, "on Touch 'two'");
                playTone(ToneGenerator.TONE_DTMF_2);
                //mHaptic.vibrate();
                break;
            }
            case R.id.three: {
            	Log.d(TAG, "on Touch 'three'");
                playTone(ToneGenerator.TONE_DTMF_3);
                //mHaptic.vibrate();
                break;
            }
            case R.id.four: {
            	Log.d(TAG, "on Touch 'four'");
                playTone(ToneGenerator.TONE_DTMF_4);
                //mHaptic.vibrate();
                break;
            }
            case R.id.five: {
            	Log.d(TAG, "on Touch 'five'");
                playTone(ToneGenerator.TONE_DTMF_5);
                //mHaptic.vibrate();
                break;
            }
            case R.id.six: {
            	Log.d(TAG, "on Touch 'six'");
                playTone(ToneGenerator.TONE_DTMF_6);
                //mHaptic.vibrate();
                break;
            }
            case R.id.seven: {
            	Log.d(TAG, "on Touch 'seven'");
                playTone(ToneGenerator.TONE_DTMF_7);
                //mHaptic.vibrate();
                break;
            }
            case R.id.eight: {
            	Log.d(TAG, "on Touch 'eight'");
                playTone(ToneGenerator.TONE_DTMF_8);
                //mHaptic.vibrate();
                break;
            }
            case R.id.nine: {
            	Log.d(TAG, "on Touch 'nine'");
                playTone(ToneGenerator.TONE_DTMF_9);
                //mHaptic.vibrate();
                break;
            }
            case R.id.zero: {
            	Log.d(TAG, "on Touch 'zero'");
                playTone(ToneGenerator.TONE_DTMF_0);
                //mHaptic.vibrate();
                break;
            }
            case R.id.pound: {
            	Log.d(TAG, "on Touch 'pound'");
                playTone(ToneGenerator.TONE_DTMF_P);
                //mHaptic.vibrate();
                break;
            }
            case R.id.star: {
            	Log.d(TAG, "on Touch 'star'");
                playTone(ToneGenerator.TONE_DTMF_S);
                //mHaptic.vibrate();
                break;
            }
    	  }
    	} /*else if (event.getAction() == (MotionEvent.ACTION_UP)) {
    		Log.d(TAG, "begin onTouch() view:" + view.getId() + "event.getAction()" + "MotionEvent.ACTION_UP");
    		 switch (view.getId()) {
             case R.id.two: {
                 keyPressed(KeyEvent.KEYCODE_2);
                 break;
             }
             case R.id.three: {
                 keyPressed(KeyEvent.KEYCODE_3);
                 break;
             }
             case R.id.four: {
                 keyPressed(KeyEvent.KEYCODE_4);
                 break;
             }
             case R.id.five: {
                 keyPressed(KeyEvent.KEYCODE_5);
                 break;
             }
             case R.id.six: {
                 keyPressed(KeyEvent.KEYCODE_6);
                 break;
             }
             case R.id.seven: {
                 keyPressed(KeyEvent.KEYCODE_7);
                 break;
             }
             case R.id.eight: {
                 keyPressed(KeyEvent.KEYCODE_8);
                 break;
             }
             case R.id.nine: {
                 keyPressed(KeyEvent.KEYCODE_9);
                 break;
             }
             case R.id.pound: {
                 keyPressed(KeyEvent.KEYCODE_POUND);
                 break;
             }
             case R.id.star: {
                 keyPressed(KeyEvent.KEYCODE_STAR);
                 break;
             }
    	  }
       }*/
    	return super.onTouchEvent(event);
    }

    private void addToContact(String number) {
    	if (number == null)
    		return;
    	Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
    	intent.setType(Contacts.CONTENT_ITEM_TYPE);
    	intent.putExtra(Insert.PHONE, number);
    	startActivity(intent);
    }
    
    public boolean onLongClick(View view) {
    		Log.d(TAG, "onLongClick view:" + view.getId());
        final Editable digits = mDigits.getText();
        int id = view.getId();
        mPressDelete = false;

        // mtk80909 for Speed Dial
        SharedPreferences pref = null;
        String numberPressed = "";
        if (ContactsUtils.SPEED_DIAL) {
        	pref = getSharedPreferences(SpeedDialManageActivity.PREF_NAME, 
        			Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
        	Log.i(TAG, "pref obtained");
        }
        
        switch (id) {
			case R.id.textfieldDeleteButton:
			case R.id.deleteButtonGemini:
            case R.id.deleteButton: {
            	mPressDelete = true;
            	digits.clear();
//                currDigitsSize = maxDigitsSize;
//                mDigits.setTextSize(maxDigitsSize);
//                digits.clear();
                // TODO: The framework forgets to clear the pressed
                // status of disabled button. Until this is fixed,
                // clear manually the pressed status. b/2133127
                mDelete.setPressed(false);
                return true;
            }
            case R.id.deleteOnDigitButton: {
            	mPressDelete = true;
            	digits.clear();
            	if (mDeleteOnDigitButton != null)
            		mDeleteOnDigitButton.setPressed(false);
//            	currDigitsSize = maxDigitsSize;
//            	mDigits.setTextSize(maxDigitsSize);
                return true;
            }
            case R.id.one: {
                if (isDigitsEmpty()) {
                    if(FeatureOption.MTK_GEMINI_SUPPORT == true)
                        callVoicemailGemini();
                    else
                        callVoicemail();
                    return true;
                }
                return false;
            }

            // mtk80909 for Speed Dial start
            case R.id.two:
            	if (TextUtils.isEmpty(numberPressed)) numberPressed = "2";
            case R.id.three:
            	if (TextUtils.isEmpty(numberPressed)) numberPressed = "3";
            case R.id.four:
            	if (TextUtils.isEmpty(numberPressed)) numberPressed = "4";
            case R.id.five:
            	if (TextUtils.isEmpty(numberPressed)) numberPressed = "5";
            case R.id.six:
            	if (TextUtils.isEmpty(numberPressed)) numberPressed = "6";
            case R.id.seven:
            	if (TextUtils.isEmpty(numberPressed)) numberPressed = "7";
            case R.id.eight:
            	if (TextUtils.isEmpty(numberPressed)) numberPressed = "8";
            case R.id.nine:
            	if (TextUtils.isEmpty(numberPressed)) numberPressed = "9";
            	
            	
            	if (!ContactsUtils.SPEED_DIAL) return false;
                
                if (!isDigitsEmpty()) {
                    return false;
                }
            	String numberToCall = pref.getString(numberPressed, "");
            	if (TextUtils.isEmpty(numberToCall)) return false;
            	Cursor phoneCursor = getContentResolver().query(
                        Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numberToCall)), 
                        new String[]{PhoneLookup._ID}, null, null, null);
            	if (phoneCursor == null || phoneCursor.getCount() == 0) {
            		Log.i(TAG, "clear preferences");
            		int numOffset = SpeedDialManageActivity.offset(Integer.valueOf(numberPressed).intValue());
            		int simId = pref.getInt(String.valueOf(numOffset), -1);
            		if (simId == -1 || isSimReady(simId)) {
	            		
	            		SharedPreferences.Editor editor = pref.edit();
	            		editor.putString(numberPressed, "");
	            		
	            		editor.putInt(String.valueOf(numOffset), -1);
	            		editor.apply();
	            	}
            		if (phoneCursor != null) phoneCursor.close();
            		return false;
            	}
            	if (phoneCursor != null) phoneCursor.close();
            	
            	if (FeatureOption.MTK_GEMINI_SUPPORT) {
            		Log.i(TAG, "genimi");
					// start sim association query
					Log.i(TAG, "start sim association with number for speed dial = "+numberToCall);
//					Uri uri = Uri.withAppendedPath(CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(numberToCall));
//					mSimAssociationQueryHandler.startQuery(TOKEN_SPEEDDIAL,						  // token
//														   numberToCall,					 // cookie
//														   Data.CONTENT_URI,		 // uri
//														   new String[]{Data.SIM_ID},// projection
//														   Data.MIMETYPE + "='" + CommonDataKinds.Phone.CONTENT_ITEM_TYPE 
//														   + "' AND (" + Data.DATA1 + "=" + numberToCall + ") AND (" + Data.SIM_ID + " IS NOT null)", // selection
//														   null,					 // selectionArgs
//														   null);
					onDialButtonClickInt(numberToCall);
            		//ContactsUtils.initiateCallWithSim(this, numberToCall);
            	} else {
            		Log.i(TAG, "single SIM");
            		ContactsUtils.initiateCall(this, numberToCall);
            	}
            	mDigits.getText().clear();
            	return true;
            // mtk80909 for Speed Dial end
            
            case R.id.zero: {
                keyPressed(KeyEvent.KEYCODE_PLUS);
                mHaptic.vibrate();
                return true;
            }
        }
        return false;
    }

    void callVoicemail() {
        StickyTabs.saveTab(this, getIntent());
        boolean isSimReady = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimState();
        if(!isSimReady){
            mLastNumber = "";
            showDialog(BAD_EMERGENCY_NUMBER_DIALOG);
            mDigits.getText().clear();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                Uri.fromParts("voicemail", EMPTY_NUMBER, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        mDigits.getText().clear();
        finish();
    }
    
    /**
     * for callVoicemailGemini to update dialog with simSlot.
     */
    private void updateMissingVoiceMailDialog(final int simSlot){
    	final SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(this, simSlot);
        if(this.mMissingVoicemailDialog != null && simInfo != null){
        	AlertDialog dialog = (AlertDialog)mMissingVoicemailDialog;
        	String oldMessage = this.getResources().getString(R.string.no_vm_number_msg);
        	dialog.setMessage(compressString(simInfo.mDisplayName,20)+" : "+oldMessage);
        }
    }
    
    /**
     * mtk71029 update for CR: ALPS00041535
     * The GeminiEnhance Dial Spec page8: Some special input number should only show the info of the current default 
     * Voice SIM card, such as PIN MMI code, N# contacts, Voice Mail. But the IMEI query will show all device's IMEI.
     * 
     */
    /*package*/void callVoicemailGemini(){
        boolean sim1Ready;
        boolean sim2Ready;
        boolean radio1On;
        boolean radio2On;
        String sim1vmn;
        String sim2vmn;
        String defaultVmn;
        boolean defaultReady;
        boolean defaultRadioOn;
        
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));  
        
        final long defaultSim = Settings.System.getLong(this.getContentResolver(), 
                Settings.System.VOICE_CALL_SIM_SETTING,
                Settings.System.DEFAULT_SIM_NOT_SET);
        final int defaultSlot = SIMInfo.getSlotById(this, defaultSim);
        
        try {
            sim1vmn = telephonyManager.getVoiceMailNumberGemini(Phone.GEMINI_SIM_1);
            sim2vmn = telephonyManager.getVoiceMailNumberGemini(Phone.GEMINI_SIM_2);
            sim1Ready = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(Phone.GEMINI_SIM_1);
            sim2Ready = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(Phone.GEMINI_SIM_2);
            radio1On = phone.isRadioOnGemini(Phone.GEMINI_SIM_1);
            radio2On = phone.isRadioOnGemini(Phone.GEMINI_SIM_2);
        }catch(Exception e){
            Log.d(TAG, "Get Radio State Failed");
            sim1vmn = null;
            sim2vmn = null;
            sim1Ready = false;
            sim2Ready = false;
            radio1On = false;
            radio2On = false;
        }
        if (!radio1On && !radio2On){
            showDialog(ERROR_POWER_OFF_DIALOG);
            return ;
        }
        if(defaultSlot == Phone.GEMINI_SIM_1 || defaultSlot == Phone.GEMINI_SIM_2){
        	defaultVmn = defaultSlot == Phone.GEMINI_SIM_1?sim1vmn:sim2vmn;
        	defaultReady = defaultSlot == Phone.GEMINI_SIM_1?sim1Ready:sim2Ready;
        	defaultRadioOn = defaultSlot == Phone.GEMINI_SIM_1?radio1On:radio2On;
        	if(defaultReady){
                if(defaultVmn != null && defaultVmn.length() > 0) {
                    /*
                    Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
                    intent.putExtra(Phone.GEMINI_SIM_ID_KEY, defaultSlot);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.fromParts("tel", defaultVmn, null));
                    startActivity(intent);
                    */
                    Intent intent = ContactsUtils.generateDialIntent(false, defaultSlot, defaultVmn);
                    sendBroadcast(intent);
                }else{
                    mSimNo = defaultSlot;
                    updateMissingVoiceMailDialog(defaultSlot);
                    showDialog(MISSING_VOICEMAIL_DIALOG);
                }
        	}
        }else{
        	if (sim1Ready && sim2Ready){
                Bundle bundle = new Bundle();
                bundle.putCharSequence("voicemail0", sim1vmn);
                bundle.putCharSequence("voicemail1", sim2vmn);
                showSpecialSequenceDialog(VOICE_MAIL_DIALOG, bundle);
            }else if (sim1Ready) {
                if(sim1vmn != null && sim1vmn.length() > 0){
                    /*
                    Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
                    intent.putExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.fromParts("tel", sim1vmn, null));
                    startActivity(intent);
                    */
                    Intent intent = ContactsUtils.generateDialIntent(false, Phone.GEMINI_SIM_1, sim1vmn);
                    sendBroadcast(intent);
                }else{
                     mSimNo = Phone.GEMINI_SIM_1;
                     updateMissingVoiceMailDialog(Phone.GEMINI_SIM_1);
                     showDialog(MISSING_VOICEMAIL_DIALOG);                        
                }
            }else if (sim2Ready){
               if(sim2vmn != null && sim2vmn.length() > 0){
                   /*  
                   Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
                   intent.putExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_2);
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   intent.setData(Uri.fromParts("tel", sim2vmn, null));
                   startActivity(intent);
                   */
                   Intent intent = ContactsUtils.generateDialIntent(false, Phone.GEMINI_SIM_2, sim2vmn);
                   sendBroadcast(intent);
               }else{
                     mSimNo = Phone.GEMINI_SIM_2;
                     updateMissingVoiceMailDialog(Phone.GEMINI_SIM_2);
                     showDialog(MISSING_VOICEMAIL_DIALOG);
               }
            }else{
               //No SIM is in ready state
               Log.d(TAG, "No sim is ready!");
            }
        }
    }

    /*void callVoicemailGemini()
    {
    	TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
    	ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));    
    	String sim1vmn;
    	String sim2vmn;
    	boolean sim1Inserted;
    	boolean sim2Inserted;
    	boolean radio1On;
    	boolean radio2On;
    	try
    	{
    	    sim1vmn = telephonyManager.getVoiceMailNumberGemini(Phone.GEMINI_SIM_1);
    	    sim2vmn = telephonyManager.getVoiceMailNumberGemini(Phone.GEMINI_SIM_2);
    	
    	    sim1Inserted = phone.isSimInsert(Phone.GEMINI_SIM_1);
            sim2Inserted = phone.isSimInsert(Phone.GEMINI_SIM_2);
            radio1On     = phone.isRadioOnGemini(Phone.GEMINI_SIM_1);
            radio2On     = phone.isRadioOnGemini(Phone.GEMINI_SIM_2);
    	}
    	catch(Exception e)
    	{
    	    Log.d(TAG, "Get Radio State Failed");
    	    sim1vmn      = null;
    	    sim2vmn      = null;
    	    sim1Inserted = false;
    	    sim2Inserted = false;
    	    radio1On     = false;
    	    radio2On     = false;
    	}
    	
        if(sim1Inserted && sim2Inserted)
	{
	    if(radio1On && radio2On)
	    {
	        Bundle bundle = new Bundle();
	        bundle.putCharSequence("voicemail0", sim1vmn);
                bundle.putCharSequence("voicemail1", sim2vmn);
	        showSpecialSequenceDialog(VOICE_MAIL_DIALOG, bundle);
	    }
            else if(radio1On)
            {
                if(sim1vmn != null && sim1vmn.length() > 0)
                {
		    Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
                    intent.putExtra(Phone.GEMINI_SIM_ID_KEY, 0);
    	            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.fromParts("tel", sim1vmn, null));
                    startActivity(intent);
	        }
                else
                {
                    showDialog(MISSING_VOICEMAIL_DIALOG);
                }
            }
            else if(radio2On)
            {
                if(sim2vmn != null && sim2vmn.length() > 0)
                {
		    Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
                    intent.putExtra(Phone.GEMINI_SIM_ID_KEY, 1);
    	            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.fromParts("tel", sim2vmn, null));
                    startActivity(intent);
	        }
                else
                {
                    showDialog(MISSING_VOICEMAIL_DIALOG);
                }
            }
            else
            {
                showDialog(ERROR_POWER_OFF_DIALOG);
            }
        }
	else if(sim1Inserted && radio1On)
        {
            if(sim1vmn != null && sim1vmn.length() > 0)
	    {
	        Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
    		intent.putExtra(Phone.GEMINI_SIM_ID_KEY, 0);
	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		intent.setData(Uri.fromParts("tel", sim1vmn, null));
    		startActivity(intent);
	    }
	    else
	    {
                showDialog(MISSING_VOICEMAIL_DIALOG);
	    }
        }
	else if(sim2Inserted && radio2On)
	{
            if(sim2vmn != null && sim2vmn.length() > 0)
	    {
	        Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
    		intent.putExtra(Phone.GEMINI_SIM_ID_KEY, 1);
	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		intent.setData(Uri.fromParts("tel", sim2vmn, null));
    		startActivity(intent);
	    }
	    else
	    {
	        showDialog(MISSING_VOICEMAIL_DIALOG);
	    }
        }
        else
        {
            showDialog(ERROR_POWER_OFF_DIALOG);
        }
    }*/

/* Fion add start */
    void dialButtonPressed() {
            dialButtonPressedext(DEFAULT_SIM);
    }

    void dialButtonPressedext(int simId) {
        final String number = mDigits.getText().toString();
        dialButtonPressedextInt(number, simId);
    }
    
    //Modified by mtk80908. Used for dialing given number. 
    void dialButtonPressedextInt(String number, int simId) {
        boolean sendEmptyFlash = false;
        Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);

        if (/*isDigitsEmpty()*/TextUtils.isEmpty(number)) { // There is no number entered.
            if (phoneIsCdma() && phoneIsOffhook()) {
                // On CDMA phones, if we're already on a call, pressing
                // the Dial button without entering any digits means "send
                // an empty flash."
                intent.setData(Uri.fromParts("tel", EMPTY_NUMBER, null));
                intent.putExtra(EXTRA_SEND_EMPTY_FLASH, true);
                sendEmptyFlash = true;
            } else if (!TextUtils.isEmpty(mLastNumberDialed)) {
                // Otherwise, pressing the Dial button without entering
                // any digits means "recall the last number dialed".
               // mDigits.setText(mLastNumberDialed);
                return;
            } else {
                // Rare case: there's no "last number dialed".  There's
                // nothing useful for the Dial button to do in this case.
                //playTone(ToneGenerator.TONE_PROP_NACK);
                return;
            }
        } else {  // There is a number.
            boolean isSimReady = true;
            if(simId == DEFAULT_SIM){
                //isSimReady = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimState();
            }
            else{
                isSimReady = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(simId);
            }
            Log.d(TAG, "dialButtonPressedext simId = "+simId+" isSimReady = "+isSimReady);
            if(!isSimReady){
                // only allow to dial ecc number
            	// Modified by mtk80908
                //mLastNumber = mDigits.getText().toString();
            	mLastNumber = number;
                if (mLastNumber!= null && mLastNumber.length() > 0 && !PhoneNumberUtils.isEmergencyNumber(mLastNumber)) {
                    if (mDigits != null && mDigits.length() > 0)
                    	mDigits.getText().clear();
                    showDialog(BAD_EMERGENCY_NUMBER_DIALOG);
                    return;
                }
            }
            intent.setData(Uri.fromParts("tel", number, null));
        }

        StickyTabs.saveTab(this, getIntent());
        intent.putExtra(Phone.GEMINI_SIM_ID_KEY, simId);

        /*Added by lianyu.zhang start*/
        if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
        {
        	if(mIsVTCall){
        		mIsVTCall = false;
        		Log.d(TAG, "dialButtonPressedext: IS_VT_CALL:true");
        		intent.putExtra(IS_VT_CALL, true);
        	}
        }
        /*Added by lianyu.zhang end*/

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        mDigits.getText().clear();

        // Don't finish TwelveKeyDialer yet if we're sending a blank flash for CDMA. CDMA
        // networks use Flash messages when special processing needs to be done, mainly for
        // 3-way or call waiting scenarios. Presumably, here we're in a special 3-way scenario
        // where the network needs a blank flash before being able to add the new participant.
        // (This is not the case with all 3-way calls, just certain CDMA infrastructures.)
        if (!sendEmptyFlash) {
            //finish();
        }
    }
/* Fion add end */


    /**
     * Plays the specified tone for TONE_LENGTH_MS milliseconds.
     *
     * The tone is played locally, using the audio stream for phone calls.
     * Tones are played only if the "Audible touch tones" user preference
     * is checked, and are NOT played if the device is in silent mode.
     *
     * @param tone a tone code from {@link ToneGenerator}
     */
    void playTone(int tone) {
        // if local tone playback is disabled, just return.
        if (!mDTMFToneEnabled) {
            return;
        }

        // Also do nothing if the phone is in silent mode.
        // We need to re-check the ringer mode for *every* playTone()
        // call, rather than keeping a local flag that's updated in
        // onResume(), since it's possible to toggle silent mode without
        // leaving the current activity (via the ENDCALL-longpress menu.)
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
            || (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
            return;
        }

        synchronized(mToneGeneratorLock) {
            if (mToneGenerator == null) {
                Log.w(TAG, "playTone: mToneGenerator == null, tone: "+tone);
                return;
            }

            // Start the new tone (will stop any playing tone)
            mToneGenerator.startTone(tone, TONE_LENGTH_MS);
        }
    }

    /**
     * Brings up the "dialpad chooser" UI in place of the usual Dialer
     * elements (the textfield/button and the dialpad underneath).
     *
     * We show this UI if the user brings up the Dialer while a call is
     * already in progress, since there's a good chance we got here
     * accidentally (and the user really wanted the in-call dialpad instead).
     * So in this situation we display an intermediate UI that lets the user
     * explicitly choose between the in-call dialpad ("Use touch tone
     * keypad") and the regular Dialer ("Add call").  (Or, the option "Return
     * to call in progress" just goes back to the in-call UI with no dialpad
     * at all.)
     *
     * @param enabled If true, show the "dialpad chooser" instead
     *                of the regular Dialer UI
     */
    private void showDialpadChooser(boolean enabled) {
        if (enabled) {
            Log.i(TAG, "Showing dialpad chooser!");
            mDigits.setVisibility(View.GONE);
            //xingping.zheng add
            if(isQVGAPlusQwerty())
                mAddContact.setVisibility(View.GONE);
            if (mDialpad != null) mDialpad.setVisibility(View.GONE);
            if (mShowDialpad != null)
            	mShowDialpad.setVisibility(View.GONE);
//            if(mShowDialpadButton != null)mShowDialpadButton.setVisibility(View.GONE);
//            mList.removeFooterView(mAddToContacts);
//		    if (mBottomIsVisiable) {
//		    	mList.removeFooterView(mAddToContacts);
//		    	mBottomIsVisiable = false;
//		    }
			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
				mList.setVisibility(View.GONE);
			} else {
				mDigits.setVisibility(View.GONE);
            	mVoicemailDialAndDeleteRow.setVisibility(View.GONE);
			}
            

            if (FeatureOption.MTK_GEMINI_SUPPORT == true || isQVGAPlusQwerty())
            {
				if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == false) {
                	mDelete.setVisibility(View.GONE);
            	}
            }

            // Instantiate the DialpadChooserAdapter and hook it up to the
            // ListView.  We do this only once.
            if (mDialpadChooserAdapter == null) {
                mDialpadChooserAdapter = new DialpadChooserAdapter(this);
                mDialpadChooser.setAdapter(mDialpadChooserAdapter);
            }
        } else {
            Log.i(TAG, "Displaying normal Dialer UI.");
            mDigits.setVisibility(View.VISIBLE);
//            //xingping.zheng add
//            if(isQVGAPlusQwerty())
//                mAddContact.setVisibility(View.VISIBLE);
//            if (mDialpad != null) mDialpad.setVisibility(View.VISIBLE);
//            mVoicemailDialAndDeleteRow.setVisibility(View.VISIBLE);
            if (mDialpad != null) {
            	mDialpad.setVisibility(View.VISIBLE);
            	mDigits.requestFocus();
                mDigits.setCursorVisibleNoCheck(true);
            }
			if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
				mList.setVisibility(View.GONE);
			} else {
                //xingping.zheng add
                if(isQVGAPlusQwerty())
                mAddContact.setVisibility(View.VISIBLE);
				mDigits.setVisibility(View.VISIBLE);
            	mVoicemailDialAndDeleteRow.setVisibility(View.VISIBLE);
			}
            mDialpadChooser.setVisibility(View.GONE);

            if (FeatureOption.MTK_GEMINI_SUPPORT == true || isQVGAPlusQwerty())
            {
				if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == false) {
                	mDelete.setVisibility(View.VISIBLE);
            	}
            }
            updateDialAndDeleteButtonEnabledState();				
        }
    }

    /**
     * @return true if we're currently showing the "dialpad chooser" UI.
     */
    private boolean dialpadChooserVisible() {
        return mDialpadChooser.getVisibility() == View.VISIBLE;
    }

    /**
     * Simple list adapter, binding to an icon + text label
     * for each item in the "dialpad chooser" list.
     */
    private static class DialpadChooserAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        // Simple struct for a single "choice" item.
        static class ChoiceItem {
            String text;
            Bitmap icon;
            int id;

            public ChoiceItem(String s, Bitmap b, int i) {
                text = s;
                icon = b;
                id = i;
            }
        }

        // IDs for the possible "choices":
        static final int DIALPAD_CHOICE_USE_DTMF_DIALPAD = 101;
        static final int DIALPAD_CHOICE_RETURN_TO_CALL = 102;
        static final int DIALPAD_CHOICE_ADD_NEW_CALL = 103;

        private static final int NUM_ITEMS = 3;
        private ChoiceItem mChoiceItems[] = new ChoiceItem[NUM_ITEMS];

        public DialpadChooserAdapter(Context context) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);

            // Initialize the possible choices.
            // TODO: could this be specified entirely in XML?

            // - "Use touch tone keypad"
            mChoiceItems[0] = new ChoiceItem(
                    context.getString(R.string.dialer_useDtmfDialpad),
                    BitmapFactory.decodeResource(context.getResources(),
                                                 R.drawable.ic_dialer_fork_tt_keypad),
                    DIALPAD_CHOICE_USE_DTMF_DIALPAD);

            // - "Return to call in progress"
            mChoiceItems[1] = new ChoiceItem(
                    context.getString(R.string.dialer_returnToInCallScreen),
                    BitmapFactory.decodeResource(context.getResources(),
                                                 R.drawable.ic_dialer_fork_current_call),
                    DIALPAD_CHOICE_RETURN_TO_CALL);

            // - "Add call"
            mChoiceItems[2] = new ChoiceItem(
                    context.getString(R.string.dialer_addAnotherCall),
                    BitmapFactory.decodeResource(context.getResources(),
                                                 R.drawable.ic_dialer_fork_add_call),
                    DIALPAD_CHOICE_ADD_NEW_CALL);
        }

        public int getCount() {
            return NUM_ITEMS;
        }

        /**
         * Return the ChoiceItem for a given position.
         */
        public Object getItem(int position) {
            return mChoiceItems[position];
        }

        /**
         * Return a unique ID for each possible choice.
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a view for each row.
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            // When convertView is non-null, we can reuse it (there's no need
            // to reinflate it.)
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.dialpad_chooser_list_item, null);
            }

            TextView text = (TextView) convertView.findViewById(R.id.text);
            text.setText(mChoiceItems[position].text);

            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            icon.setImageBitmap(mChoiceItems[position].icon);

            return convertView;
        }
    }

    /**
     * Handle clicks from the dialpad chooser.
     */
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        DialpadChooserAdapter.ChoiceItem item =
                (DialpadChooserAdapter.ChoiceItem) parent.getItemAtPosition(position);
        int itemId = item.id;
        switch (itemId) {
            case DialpadChooserAdapter.DIALPAD_CHOICE_USE_DTMF_DIALPAD:
                 Log.i(TAG, "DIALPAD_CHOICE_USE_DTMF_DIALPAD");
                // Fire off an intent to go back to the in-call UI
                // with the dialpad visible.
                returnToInCallScreen(true);
                break;

            case DialpadChooserAdapter.DIALPAD_CHOICE_RETURN_TO_CALL:
                 Log.i(TAG, "DIALPAD_CHOICE_RETURN_TO_CALL");
                // Fire off an intent to go back to the in-call UI
                // (with the dialpad hidden).
                returnToInCallScreen(false);
                break;

            case DialpadChooserAdapter.DIALPAD_CHOICE_ADD_NEW_CALL:
                 Log.i(TAG, "DIALPAD_CHOICE_ADD_NEW_CALL");
                // Ok, guess the user really did want to be here (in the
                // regular Dialer) after all.  Bring back the normal Dialer UI.
                showDialpadChooser(false);
                break;

            default:
                Log.w(TAG, "onItemClick: unexpected itemId: " + itemId);
                break;
        }
    }

    /**
     * Returns to the in-call UI (where there's presumably a call in
     * progress) in response to the user selecting "use touch tone keypad"
     * or "return to call" from the dialpad chooser.
     */
    private void returnToInCallScreen(boolean showDialpad) {
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) phone.showCallScreenWithDialpad(showDialpad);
        } catch (RemoteException e) {
            Log.w(TAG, "phone.showCallScreenWithDialpad() failed", e);
        }

        // Finally, finish() ourselves so that we don't stay on the
        // activity stack.
        // Note that we do this whether or not the showCallScreenWithDialpad()
        // call above had any effect or not!  (That call is a no-op if the
        // phone is idle, which can happen if the current call ends while
        // the dialpad chooser is up.  In this case we can't show the
        // InCallScreen, and there's no point staying here in the Dialer,
        // so we just take the user back where he came from...)
        finish();
    }

    /**
     * @return true if the phone is "in use", meaning that at least one line
     *              is active (ie. off hook or ringing or dialing).
     */
    private boolean phoneIsInUse() {
        boolean phoneInUse = false;
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) 
            {
                if (FeatureOption.MTK_GEMINI_SUPPORT == true)            
                {
                    boolean sim1=false, sim2=false;
                    sim1 = phone.isIdleGemini(Phone.GEMINI_SIM_1) ;
                    sim2 = phone.isIdleGemini(Phone.GEMINI_SIM_2) ;
                    if (!sim1 || !sim2)
                    {
                        phoneInUse=true;
                    }else
                    {
                    	//this is a back door for sip call in gemini
                    	phoneInUse = !phone.isIdle();
                    }
                }
                else
                {
                    phoneInUse = !phone.isIdle();
                }
            }
        } catch (RemoteException e) {
            Log.w(TAG, "phone.isIdle() failed", e);
        }
        return phoneInUse;
    }

    /**
     * @return true if the phone is a CDMA phone type
     */
    private boolean phoneIsCdma() {
        boolean isCdma = false;
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) {
                isCdma = (phone.getActivePhoneType() == TelephonyManager.PHONE_TYPE_CDMA);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "phone.getActivePhoneType() failed", e);
        }
        return isCdma;
    }

    /**
     * @return true if the phone state is OFFHOOK
     */
    private boolean phoneIsOffhook() {
        boolean phoneOffhook = false;
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) phoneOffhook = phone.isOffhook();
        } catch (RemoteException e) {
            Log.w(TAG, "phone.isOffhook() failed", e);
        }
        return phoneOffhook;
    }


    /**
     * Returns true whenever any one of the options from the menu is selected.
     * Code changes to support dialpad options
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_2S_PAUSE:
                updateDialString(",");
                return true;
            case MENU_WAIT:
                updateDialString(";");
                return true;
            /*Added by lianyu.zhang start*/
//            case MENU_VT:
//            	isVTCall = true;
//            	if( FeatureOption.MTK_GEMINI_SUPPORT == false )
//            	{
//            		isVTCall = true;
//            		dialButtonPressed();
//            	}
//            	return true;
            /*Added by lianyu.zhang end*/


            // mtk80909 for Speed Dial
            case MENU_SPEED_DIAL:
				final Intent intent = new Intent(this, SpeedDialManageActivity.class);
				startActivity(intent);
				return true;
        }
        return false;
    }

    /**
     * Updates the dial string (mDigits) after inserting a Pause character (,)
     * or Wait character (;).
     */
    private void updateDialString(String newDigits) {
        int selectionStart;
        int selectionEnd;

        // SpannableStringBuilder editable_text = new SpannableStringBuilder(mDigits.getText());
        int anchor = mDigits.getSelectionStart();
        int point = mDigits.getSelectionEnd();

        selectionStart = Math.min(anchor, point);
        selectionEnd = Math.max(anchor, point);

        Editable digits = mDigits.getText();
        if (selectionStart != -1 ) {
            if (selectionStart == selectionEnd) {
                // then there is no selection. So insert the pause at this
                // position and update the mDigits.
                digits.replace(selectionStart, selectionStart, newDigits);
            } else {
                digits.replace(selectionStart, selectionEnd, newDigits);
                // Unselect: back to a regular cursor, just pass the character inserted.
                mDigits.setSelection(selectionStart + 1);
            }
        } else {
            int len = mDigits.length();
            digits.replace(len, len, newDigits);
        }
    }

    /**
     * Update the enabledness of the "Dial" and "Backspace" buttons if applicable.
     * New function to update dialpad  button state 
     */
    private void updateDialAndDeleteButtonEnabledState(){
    	
    }
    /**
     * Update the enabledness of the "Dial" and "Backspace" buttons if applicable.
     */
    private void updateDialAndDeleteButtonEnabledState_old() {
        final boolean digitsNotEmpty = !isDigitsEmpty();

    /* Fion add start */
        // If we're already on a CDMA call, then we want to enable the Call button
        if (phoneIsCdma() && phoneIsOffhook()) {
            if (mDialButton != null) {
                mDialButton.setEnabled(true);
            }
        } else {
            // CR:129628
            if(isQVGAPlusQwerty())
            {
            	mAddContact.setEnabled(digitsNotEmpty);
            }
            if (mDialButton != null) {
                mDialButton.setEnabled(digitsNotEmpty);
            }
        /* Fion add start */  
        else if ((FeatureOption.MTK_GEMINI_SUPPORT == true) && (mDialButton1 != null) && (mDialButton2 != null) && (mDialButtonECC!=null))
            {
                try {
					
                    Log.e(TAG, "updateDialAndDeleteButtonStateEnabledAttr");
                    ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                    if (phone != null) 
                    {
						if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
							if (mAddToContactOnDigitButton != null) {
							    mAddToContactOnDigitButton.setEnabled(digitsNotEmpty);
							    mAddToContactOnDigitButton.setFocusable(digitsNotEmpty);
							}
						}
                        //private ImageButton DialImageButton1;
                        //private ImageButton DialImageButton2;	

                        radio1_on = phone.isRadioOnGemini(Phone.GEMINI_SIM_1);
                        radio2_on = phone.isRadioOnGemini(Phone.GEMINI_SIM_2);					
                        Log.e(TAG, "updateDialAndDeleteButtonStateEnabledAttr, radio1"+radio1_on+", radio2:"+radio2_on);


                        if ((!radio1_on && !radio2_on) ||
                              (!(phone.isSimInsert(Phone.GEMINI_SIM_1) ) && !(phone.isSimInsert(Phone.GEMINI_SIM_2) )))
                        {
//                             mDialButtonECC.setEnabled(true);  
//							 
//                             mVoicemailDialAndDeleteRow.setVisibility(View.GONE);                        
//                             
//                             if (dialpadChooserVisible()) {
//                                 mDialEccrow.setVisibility(View.GONE);
//                                 showDialpadChooser(true);
//                             }
//                             else
//                             {
//                                 mDialEccrow.setVisibility(View.VISIBLE);                        							 
//                             }
//                         	mDialButtonECC.setEnabled(true);  
							if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
//								mDialButtonLayout.setVisibility(View.GONE);
								mDialButton1.setVisibility(View.GONE);
//								mDialButton2.setVisibility(View.GONE);
//								mDialButtonECC.setVisibility(View.VISIBLE);
//								mDialButtonSip.setVisibility(View.VISIBLE);
							} else {
                                mVoicemailDialAndDeleteRow.setVisibility(View.GONE);
                                if (dialpadChooserVisible()) {
//                                    mDialEccrow.setVisibility(View.GONE);
                                    showDialpadChooser(true);
                                }
                                else {
//                                    mDialEccrow.setVisibility(View.VISIBLE);                        							 
	  	             	        }
							}
						}
                        else
                        {
                             boolean sim1_in_idle = true;
                             boolean sim2_in_idle = true;						
							 
//                             mDialButtonECC.setEnabled(false);  
							if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
//		                         mDialButtonLayout.setVisibility(View.VISIBLE);
		                         mDialButton1.setVisibility(View.VISIBLE);
//		                         mDialButton2.setVisibility(View.VISIBLE);
//		                         mDialButtonECC.setVisibility(View.GONE);
//		                         mDialButtonSip.setVisibility(View.GONE);
							} else {
                            	mVoicemailDialAndDeleteRow.setVisibility(View.VISIBLE);
//	                            mDialEccrow.setVisibility(View.GONE);                        							 
							}
                              try {
                                  ITelephony my_phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                                  if (my_phone != null) 
                                  {
                                      sim1_in_idle = my_phone.isIdleGemini(Phone.GEMINI_SIM_1) && (0 == my_phone.getPendingMmiCodesGemini(Phone.GEMINI_SIM_1));
                                      sim2_in_idle = my_phone.isIdleGemini(Phone.GEMINI_SIM_2) && (0 == my_phone.getPendingMmiCodesGemini(Phone.GEMINI_SIM_2));									  
                                  }
                              } catch (RemoteException e) {
                                  Log.w(TAG, "phone.isIdle() failed", e);
                              }
							
    		              if (radio1_on && sim2_in_idle)
                              {
                                mDialButton1.setEnabled(true);                        
                                mDialButton1.setFocusable(true);                         
                                ((ImageView)mDialButton1).setImageResource(R.drawable.ic_call_button_sim1);
                                /*Added by lianyu.zhang start*/
                                if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
                                {
                                	mDialButtonvt.setEnabled(true);
                                	mDialButtonvt.setFocusable(true); 
                                	((ImageView)mDialButtonvt).setImageResource(R.drawable.ic_call_button_vt);
                                }
                                /*Added by lianyu.zhang end*/
                              }
                              else
                              {
                                mDialButton1.setEnabled(false);                        
                                mDialButton1.setFocusable(false);                        
                                ((ImageView)mDialButton1).setImageResource(R.drawable.ic_call_button_no_sim1);
                                /*Added by lianyu.zhang start*/
                                if( FeatureOption.MTK_VT3G324M_SUPPORT == true )
                                {
                                	mDialButtonvt.setEnabled(false);
                                	mDialButtonvt.setFocusable(false); 
                                	((ImageView)mDialButtonvt).setImageResource(R.drawable.ic_call_button_no_vt);
                                }
                                /*Added by lianyu.zhang end*/
                              }
    						
                              if (radio2_on && sim1_in_idle)
                              {
//                                mDialButton2.setEnabled(true);                                                                   
//                                mDialButton2.setFocusable(true);
//                                ((ImageView)mDialButton2).setImageResource(R.drawable.ic_call_button_sim2);
                              }
                              else
                              {
//                                 mDialButton2.setEnabled(false);                                                                                               
//                                 mDialButton2.setFocusable(false);                                                                                              
//                                ((ImageView)mDialButton2).setImageResource(R.drawable.ic_call_button_no_sim2);
                              }

                              if (dialpadChooserVisible()) {
                                  showDialpadChooser(true);
                              }
                        }
                    }
                } catch (RemoteException ex) {
                    // ignore it
                }
            }				
        /* Fion add end */
        }
		if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
			if (mDeleteOnDigitButton != null)
				mDeleteOnDigitButton.setEnabled(digitsNotEmpty);
		} else {
        	mDelete.setEnabled(digitsNotEmpty);
    	}
    }


    /**
     * Check if voicemail is enabled/accessible.
     */
    private void initVoicemailButton() {
	if (FeatureOption.MTK_DIALER_SEARCH_SUPPORT == true) {
		return;
	}
        boolean hasVoicemail = false;
        try {
            hasVoicemail = TelephonyManager.getDefault().getVoiceMailNumber() != null;
        } catch (SecurityException se) {
            // Possibly no READ_PHONE_STATE privilege.
        }
        if(mVoicemailDialAndDeleteRow != null)
        	mVoicemailButton = mVoicemailDialAndDeleteRow.findViewById(R.id.voicemailButton);
//        if (hasVoicemail) {
        if(mVoicemailButton != null)
            mVoicemailButton.setOnClickListener(this);
//        } else {
//            mVoicemailButton.setEnabled(false);
//        }
    }

    /**
     * This function return true if Wait menu item can be shown
     * otherwise returns false. Assumes the passed string is non-empty
     * and the 0th index check is not required.
     */
    private boolean showWait(int start, int end, String digits) {
        if (start == end) {
            // visible false in this case
            if (start > digits.length()) return false;

            // preceding char is ';', so visible should be false
            if (digits.charAt(start-1) == ';') return false;

            // next char is ';', so visible should be false
            if ((digits.length() > start) && (digits.charAt(start) == ';')) return false;
        } else {
            // visible false in this case
            if (start > digits.length() || end > digits.length()) return false;

            // In this case we need to just check for ';' preceding to start
            // or next to end
            if (digits.charAt(start-1) == ';') return false;
        }
        return true;
    }

    /**
     * @return true if the widget with the phone number digits is empty.
     */
    private boolean isDigitsEmpty() {
        return mDigits.length() == 0;
    }

    /**
     * Starts the asyn query to get the last dialed/outgoing
     * number. When the background query finishes, mLastNumberDialed
     * is set to the last dialed number or an empty string if none
     * exists yet.
     */
    private void queryLastOutgoingCall() {
        mLastNumberDialed = EMPTY_NUMBER;
        CallLogAsync.GetLastOutgoingCallArgs lastCallArgs =
                new CallLogAsync.GetLastOutgoingCallArgs(
                    this,
                    new CallLogAsync.OnLastOutgoingCallComplete() {
                        public void lastOutgoingCall(String number) {
                            // TODO: Filter out emergency numbers if
                            // the carrier does not want redial for
                            // these.
                            mLastNumberDialed = number;
                            updateDialAndDeleteButtonEnabledState();
                        }
                    });
        mCallLog.getLastOutgoingCall(lastCallArgs);
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
    
    public boolean isQVGAPlusQwerty()
    {
    	boolean retval = false;
    	DisplayMetrics dm = new DisplayMetrics();
    	WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
    	wm.getDefaultDisplay().getMetrics(dm);
    	if(dm.widthPixels == 320 && dm.heightPixels == 240)
    		retval = true;
        return retval;
    }

    public void onClick(DialogInterface arg0, int arg1) {
        // TODO Auto-generated method stub
        if(arg0 == mMMIDialog)
        {
            try
            {
                String mmiCode = (String)mDialogBundle.getCharSequence("MMI Code");
                ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                if(arg1 == 0)
                    phone.handlePinMmiGemini(mmiCode, Phone.GEMINI_SIM_1);
                else
                    phone.handlePinMmiGemini(mmiCode, Phone.GEMINI_SIM_2);
            }
            catch(RemoteException d)
            {
                // ignore
            }
        }
        else if(arg0 == mADNDialog)
        {
            CharSequence name = null;
            CharSequence number = null;
            if(arg1 == 0)
            {
                name = mDialogBundle.getCharSequence("Name1");
                number = mDialogBundle.getCharSequence("ADN1");
            }
            else if(arg1 == 1)
            {
            	name = mDialogBundle.getCharSequence("Name2");
                number = mDialogBundle.getCharSequence("ADN2");
            }
            
            if(number != null)
            {
                int length = number.length();
                if(length > 1 && length < 5 && ((String)number).endsWith("#"))
                {
                    mIsFromADN = true;
                    Toast.makeText(this, this.getString(R.string.ghostData_phone)+"\n"+number, Toast.LENGTH_LONG).show();
                }
                else
                {
            	    name = this.getString(R.string.menu_callNumber, name);
                    Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
                }
                setSearchNumberOnly();
                mDigits.getText().replace(0, 0, number);
            }
        }
        else if(arg0 == mVoiceMailDialog)
	{
            String voicemail1 = (String)mDialogBundle.getCharSequence("voicemail0");
            String voicemail2 = (String)mDialogBundle.getCharSequence("voicemail1");
            Intent intent;
	        
	    if(arg1 == 0)
	    {
	        if(voicemail1 != null && voicemail1.length() > 0)
	        {
	            /*
	            intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
	            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, arg1);
	            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            intent.setData(Uri.fromParts("tel", voicemail1, null));
	            */
	            intent = ContactsUtils.generateDialIntent(false, arg1, voicemail1);
	            sendBroadcast(intent);
	        		
	            // temp
	            finish();
	        }
	        else
	        {
	            intent = new Intent("com.android.phone.CallFeaturesSetting.ADD_VOICEMAIL");
	            intent.setClassName("com.android.phone", "com.android.phone.VoiceMailSetting");
	            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, arg1);
	            startActivity(intent);
	        }
	    }
	    else
	    {
	        if(voicemail2 != null && voicemail2.length() > 0)
            {
	            /*
                intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
                intent.setData(Uri.fromParts("tel", voicemail2, null));
                intent.putExtra(Phone.GEMINI_SIM_ID_KEY, arg1);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                */
	            intent = ContactsUtils.generateDialIntent(false, arg1, voicemail2);
                sendBroadcast(intent);
                // temp
	            finish();
	        }
	        else
	        {
	            intent = new Intent("com.android.phone.CallFeaturesSetting.ADD_VOICEMAIL");
	            intent.setClassName("com.android.phone", "com.android.phone.VoiceMailSetting");
	            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, arg1);
	            startActivity(intent);
	        }
	    }
	}
    }

    public static String compressString(CharSequence oldString, int newLength){
         StringBuilder sb = new StringBuilder(32);
         int length = oldString.length()> newLength? newLength : oldString.length();
         char c;
         for(int i=0; i< length; i++){
            c = oldString.charAt(i);
            if(c == '\n'){
                 sb.append(' ');
            }else{
                 sb.append(c);
            }
         }
         if(oldString.length() > newLength){
            sb.append("...");
         }
         return sb.toString();
    }

    public void onShow(DialogInterface arg0) {
        // TODO Auto-generated method stub
        if(arg0 == mADNDialog)
        {
	    // update adn number
	    CharSequence adn1, adn2;
            if(mDialogBundle != null)
            {
                StringBuilder builder1 = new StringBuilder();
        	StringBuilder builder2 = new StringBuilder();
        	adn1 = mDialogBundle.getCharSequence("ADN1");
        	adn2 = mDialogBundle.getCharSequence("ADN2");
        	SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(mContext, Phone.GEMINI_SIM_1);
		if (simInfo1 != null) {
			String sim1Name = null;
			if (simInfo1.mDisplayName != null & simInfo1.mDisplayName.length() > 10) {
				sim1Name = simInfo1.mDisplayName.subSequence(0, 5) + "..." + simInfo1.mDisplayName.subSequence(simInfo1.mDisplayName.length()-5, simInfo1.mDisplayName.length());
				builder1.append(sim1Name + ":\n");
			} else {
			builder1.append(simInfo1.mDisplayName + ":\n");
			}
		}
        		
        	if(adn1 != null && adn1.length() > 0)
                {
                    if(adn1.length() > 20)
                        builder1.append(adn1.subSequence(0, 20)+"...");
                    else
        	        builder1.append(adn1);
                }
        	else
        	    builder1.append(getResources().getString(R.string.imei_invalid_message));
        		
        	SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(mContext, Phone.GEMINI_SIM_2);
		if (simInfo2 != null) {
			String sim2Name = null;
			if (simInfo2.mDisplayName != null & simInfo2.mDisplayName.length() > 10) {
				sim2Name = simInfo2.mDisplayName.subSequence(0, 5) + "..." + simInfo2.mDisplayName.subSequence(simInfo2.mDisplayName.length()-5, simInfo2.mDisplayName.length());
				builder2.append(sim2Name + ":\n");
			} else {
			builder2.append(simInfo2.mDisplayName + ":\n");
			}
		}
                if(adn2 != null)
                {
                    if(adn2.length() > 20)
                        builder2.append(adn2.subSequence(0, 20)+"...");
                    else
                        builder2.append(adn2);
                }
        	else
        	    builder2.append(getResources().getString(R.string.imei_invalid_message));
        		
        	AlertDialog alert = (AlertDialog)arg0;
        	ListView listView = alert.getListView();
                if(listView != null)
                {
                    listView.requestFocus();
                    listView.setSelection(0);
                }
            
        	TextView textView0 = (TextView)listView.getChildAt(0);
        	TextView textView1 = (TextView)listView.getChildAt(1);
        	
                if(textView0 != null && textView1 != null)
                {
                    textView0.setText(builder1.toString());
                    textView1.setText(builder2.toString());	
                    if(adn1 == null)
                        textView0.setEnabled(false);
                    else
                        textView0.setEnabled(true);

                    if(adn2 == null)
                        textView1.setEnabled(false);
                    else
                        textView1.setEnabled(true);
                }
            }
        }
        else if(arg0 == mVoiceMailDialog){
	    // update adn number
	    CharSequence voicemail1, voicemail2;
            if(mDialogBundle != null)
            {
        	StringBuilder builder1 = new StringBuilder();
        	StringBuilder builder2 = new StringBuilder();
        	voicemail1 = mDialogBundle.getCharSequence("voicemail0");
        	voicemail2 = mDialogBundle.getCharSequence("voicemail1");
        	SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(mContext, Phone.GEMINI_SIM_1);
		if (simInfo1 != null) {
			builder1.append(compressString(simInfo1.mDisplayName,20) + ":\n");
		}
        		
        	if(voicemail1 != null && voicemail1.length() > 0){
                    builder1.append(compressString(voicemail1, 20));
                }
        	else{
        	    builder1.append(getResources().getString(R.string.imei_invalid_message));
                }
        		
        	SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(mContext,Phone.GEMINI_SIM_2);
		if (simInfo2 != null) {
			builder2.append(compressString(simInfo2.mDisplayName,20) + ":\n");
		}
        	if(voicemail2 != null && voicemail2.length() > 0){
                    builder2.append(compressString(voicemail2, 20));
                }
        	else{
        	    builder2.append(getResources().getString(R.string.imei_invalid_message));
                }
        		
        	AlertDialog alert = (AlertDialog)arg0;
        	ListView listView = alert.getListView();
                if(listView != null)
                {
                    listView.requestFocus();
                    listView.setSelection(0);
                }
            
        	TextView textView0 = (TextView)listView.getChildAt(0);
        	TextView textView1 = (TextView)listView.getChildAt(1);
        	
                if(textView0 != null && textView1 != null)
                {
                    textView0.setText(builder1.toString());
                    textView1.setText(builder2.toString());	
                    if(voicemail1 == null || voicemail1.length() == 0)
        	            textView0.setEnabled(false);
                    else
                        textView0.setEnabled(true);
        		
                    if(voicemail2 == null || voicemail2.length() == 0)
                        textView1.setEnabled(false);
                    else
                        textView1.setEnabled(true);
                }
            }
        }
        else if(arg0 == mMMIDialog || arg0 == mIMEIDialog)
        {
            boolean isSim1Valide = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(Phone.GEMINI_SIM_1);
            boolean isSim2Valide = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(Phone.GEMINI_SIM_2);
            AlertDialog alert = (AlertDialog)arg0;
            ListView listView = alert.getListView();
            if(listView != null)
            {
                listView.requestFocus();
                listView.setSelection(0);
            }
            TextView textView0 = (TextView)listView.getChildAt(0);
            TextView textView1 = (TextView)listView.getChildAt(1);
            
            if(arg0 == mMMIDialog) {
                SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(this, Phone.GEMINI_SIM_1);
                if(simInfo != null)
                    textView0.setText(simInfo.mDisplayName);
                simInfo = SIMInfo.getSIMInfoBySlot(this, Phone.GEMINI_SIM_2);
                if(simInfo != null)
                    textView1.setText(simInfo.mDisplayName);
                textView0.setEllipsize(TruncateAt.MIDDLE);
                textView1.setEllipsize(TruncateAt.MIDDLE);
            }
            
             if(textView0 != null)
             {
                  if(isSim1Valide)
                	  textView0.setEnabled(true);
                  else
                	  textView0.setEnabled(false);
             }
             if(textView1 != null)
             {
                  if(isSim2Valide)
                	  textView1.setEnabled(true);
                  else
                	  textView1.setEnabled(false);
             }
        }
    }
    
    class PhoneStateReceiver extends BroadcastReceiver
    {
    	public void onReceive(Context context, Intent intent) {
    		Log.d(TAG, "Intent content is: " + intent.getAction());
    		updateDialAndDeleteButtonEnabledState();
    	}
    }
    
    BroadcastReceiver mReceiver = new PhoneStateReceiver();
    
    void registerMMIReceiver()
    {
    	IntentFilter filter = new IntentFilter("com.android.phone.mmi");
    	registerReceiver(mReceiver, filter);
    }
    
    void unRegisterMMIReceiver()
    {
    	unregisterReceiver(mReceiver);
    }

    /* added by xingping.zheng start */
    protected void onAddContacButtonClick() {
        final String number = mDigits.getText().toString();
        onAddContacButtonClickInt(number);
    }
    
    protected void onAddContacButtonClickInt(final String number) {
        if(!TextUtils.isEmpty(number)) {
            String message = getResources().getString(R.string.add_contact_dialog_message, number);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                                         .setTitle(number)
                                                         .setMessage(message);
            
            AlertDialog dialog = builder.create();
            
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.add_contact_dialog_existing), new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    intent.setType(Contacts.CONTENT_ITEM_TYPE);
                    intent.putExtra(Insert.PHONE, number);
                    startActivity(intent);
                }
            });
            
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.add_contact_dialog_new), new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                    intent.putExtra(Insert.PHONE, number);
                    startActivity(intent);
                }
                
            });
            
            mAddContactDialog = dialog;
            dialog.show();
        }
    }
    
    protected void onOneButtonLongClick() {
        //final long defaultSim = Settings.System.getLong(getContentResolver(), Settings.System.VOICE_CALL_SIM_SETTING, -3);
        final long defaultSim = 1;
        final int defaultSlotId = SIMInfo.getSlotById(this, defaultSim);
        
        if(defaultSim == Settings.System.DEFAULT_SIM_NOT_SET ||
           defaultSim == Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
            log("onOneButtonLockClick, bail out...");
        }
        
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
        
        if(defaultSim == Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
            // it must have two sim cards...
            // popup sim select dialog to let user select a sim card
        } else {
            final boolean isSimReady = TelephonyManager.SIM_STATE_READY == telephonyManager.getSimStateGemini(defaultSlotId);
            try {
                final boolean isRadioOn  = phone.isRadioOnGemini(defaultSlotId);
                
                if(!isRadioOn) {
                    showDialog(ERROR_POWER_OFF_DIALOG);
                    return ;
                }
            } catch (RemoteException e) {
                log("onOneButtonLongClick, "+e.getMessage());
            }

            if(isSimReady) {
                final String voicemailNumber = telephonyManager.getVoiceMailNumberGemini(defaultSlotId);
                if(voicemailNumber != null && voicemailNumber.length() > 0) {
                    Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
                    intent.putExtra(Phone.GEMINI_SIM_ID_KEY, defaultSlotId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.fromParts("tel", voicemailNumber, null));
                    startActivity(intent);
                } else {
                    showDialog(MISSING_VOICEMAIL_DIALOG);
                }
            }
        }
    }
    
    protected void onVideoCallButtonClick() {
        final String number = mDigits.getText().toString();
        if(TextUtils.isEmpty(number)) {
            log("onVideoCallButtonClick number is empty");
            return;
        }
        onVideoCallButtonClickInt(number);
    }
    
    protected void onVideoCallButtonClickInt(final String number) {
        mDialed = true;
        int request = CellConnMgr.REQUEST_TYPE_SIMLOCK;
        if (!TextUtils.isEmpty(number)){
            mIsVTCall = true;
        }
        mNumber = number;
        // dial emergency number directly
		if (PhoneNumberUtils.isEmergencyNumber(mNumber)) {
            Intent intent = ContactsUtils.generateDialIntent(false, Phone.GEMINI_SIM_1, mNumber);
            if(FeatureOption.MTK_GEMINI_SUPPORT) {
                sendBroadcast(intent);
            } else {
                startActivity(intent);
            }
            StickyTabs.saveTab(TwelveKeyDialer.this, getIntent());
            mIsVTCall = false;
            mDialed = false;
            return;
        }
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            mSlot = ContactsUtils.get3GCapabilitySIM();
            log("onVideoCallButtonClick, mSlot = "+mSlot);
            if(mSlot == -1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.reminder)
                       .setMessage(R.string.turn_on_3g_service_message)
                       .setNegativeButton(android.R.string.no,(DialogInterface.OnClickListener) null)
                       .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                Intent intent = new Intent();
                                intent.setClassName("com.android.phone", "com.android.phone.Modem3GCapabilitySwitch");
                                startActivity(intent);
                            }
                        });
                mTurnOn3GServiceDialog = builder.create();
                mTurnOn3GServiceDialog.show();
                return;
            }
            request = CellConnMgr.REQUEST_TYPE_ROAMING | CellConnMgr.FLAG_REQUEST_NOPREFER;
        }
        int result = mCellConnMgr.handleCellConn(mSlot, request);
        log("onVideoCallButtonClick, result = "+result);
    }

    protected void onDialButtonClick(){
        final String number = mDigits.getText().toString();
        onDialButtonClickInt(number);
    } 
    
    protected void onDialButtonClickInt(final String number){
    	onDialButtonClickIntWithSimId(number,Settings.System.DEFAULT_SIM_NOT_SET);
    }
    protected void onDialButtonClickIntWithSimId(final String number,long originalSim)
    {
    	mDialed = true; 
        boolean sendEmptyFlash = false;
        Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED);
        log("onDialButtonClick");
        if (TextUtils.isEmpty(number)) { // There is no number entered.
            if (phoneIsCdma() && phoneIsOffhook()) {
                // On CDMA phones, if we're already on a call, pressing
                // the Dial button without entering any digits means "send
                // an empty flash."
                intent.setData(Uri.fromParts("tel", EMPTY_NUMBER, null));
                intent.putExtra(EXTRA_SEND_EMPTY_FLASH, true);
                sendEmptyFlash = true;
            } else if (!TextUtils.isEmpty(mLastNumberDialed)) {
                // Otherwise, pressing the Dial button without entering
                // any digits means "recall the last number dialed".
               // mDigits.setText(mLastNumberDialed);
                return;
            } else {
                // Rare case: there's no "last number dialed".  There's
                // nothing useful for the Dial button to do in this case.
                //playTone(ToneGenerator.TONE_PROP_NACK);
                return;
            }
        } else {  // There is a number.
            // start sim association query
            log("start sim association with number = "+number);
            //Uri uri = Uri.withAppendedPath(CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(number));
            /*
            mSimAssociationQueryHandler.startQuery(TOKEN_DIAL,                        // token
                                                   number,                   // cookie
                                                   Data.CONTENT_URI,         // uri
                                                   new String[]{Data.SIM_ID},// projection
                                                   Data.MIMETYPE + "='" + CommonDataKinds.Phone.CONTENT_ITEM_TYPE 
                                                   + "' AND (" + Data.DATA1 + "=" + number + ") AND (" + Data.SIM_ID + " IS NOT null)", // selection
                                                   null,                     // selectionArgs
                                                   null);
            */
            String stripNumber = number;
            if(!PhoneNumberUtils.isUriNumber(number))
                stripNumber = PhoneNumberUtils.stripSeparators(number);
            /*
            Cursor cursor = null;
            ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
            boolean sim1Insert = false;
            boolean sim2Insert = false;
            boolean sim1Contacts = AbstractStartSIMService.phb1_load_finished;//ContactsUtils.getSimContactsReady(this, 0);
            boolean sim2Contacts = AbstractStartSIMService.phb2_load_finished;//ContactsUtils.getSimContactsReady(this, 1);
            try {
                sim1Insert = telephony.isSimInsert(0);
                sim2Insert = telephony.isSimInsert(1);
            } catch(Exception e) {
                //
            }
            log("sim1Contacts = "+sim1Contacts+" sim2Contacts = "+sim2Contacts);
            log("sim1Insert = "+sim1Insert+" sim2Insert = "+sim2Insert);
            if((sim1Contacts || !sim1Insert) && 
               (sim2Contacts || !sim2Insert)) {
                cursor = getContentResolver().query(
                        Data.CONTENT_URI,
                        new String[] {
                            Data.SIM_ID
                        },
                        Data.MIMETYPE + "='" + CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' AND ("
                                + Data.DATA1 + "='" + stripNumber + "') AND (" + Data.SIM_ID
                                + ">0)", null, null);
            }
            querySimAssociationComplete(number, cursor,originalSim);
            */
            ArrayList associateSims = mSimAssociationMaps == null ? null : mSimAssociationMaps.get(stripNumber);
            if(associateSims == null)
                associateSims = new ArrayList();
            ContactsUtils.dial(mContext, number, originalSim,// Settings.System.DEFAULT_SIM_NOT_SET,
                    associateSims, new ContactsUtils.OnDialCompleteListener() {
                        public void onDialComplete(boolean dialed) {
                            StickyTabs.saveTab(TwelveKeyDialer.this, getIntent());
                            mDialed = !dialed;
                        }
                    }, TwelveKeyDialer.this);
            //intent.setData(Uri.fromParts("tel", number, null));
        }
        /*
        StickyTabs.saveTab(this, getIntent());
        intent.putExtra(Phone.GEMINI_SIM_ID_KEY, simId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        */
        //mDigits.getText().clear();

        // Don't finish TwelveKeyDialer yet if we're sending a blank flash for CDMA. CDMA
        // networks use Flash messages when special processing needs to be done, mainly for
        // 3-way or call waiting scenarios. Presumably, here we're in a special 3-way scenario
        // where the network needs a blank flash before being able to add the new participant.
        // (This is not the case with all 3-way calls, just certain CDMA infrastructures.)
        /*
        if (!sendEmptyFlash) {
            finish();
        }
        */
    }
    
    protected void querySimAssociationComplete(String number, Cursor cursor, long originalSim) {
        log("querySimAssociationComplete");
        
        ArrayList associateSims = new ArrayList();
        
        try {
            if(cursor != null && cursor.moveToFirst()) {
                do {
                    if(cursor.getInt(0) > 0) {
                        associateSims.add(Integer.valueOf(cursor.getInt(0)));
                    }
                } while (cursor.moveToNext());
            }
        } catch(Exception e) {
            log("querySimAssociationComplete, cursor exception");
        } finally {
            if(cursor != null)
                cursor.close();
        }
        
        log("querySimAssociationComplete, associateSims = "+associateSims);
        
        ContactsUtils.dial(mContext, 
                           number,
                           originalSim,//Settings.System.DEFAULT_SIM_NOT_SET,
                           associateSims, 
                           new ContactsUtils.OnDialCompleteListener() {
                               public void onDialComplete(boolean dialed) {
                                   StickyTabs.saveTab(TwelveKeyDialer.this, getIntent());
                                   mDialed = !dialed;
                              }
                           },
                           TwelveKeyDialer.this);
    }
    
//    private class SimAssociationQueryHandler extends AsyncQueryHandler {
//
//        protected Context mContext;
//        
//        public SimAssociationQueryHandler(Context context, ContentResolver cr) {
//            super(cr);
//            mContext = context;
//            // TODO Auto-generated constructor stub
//        }
//        
//        @Override
//        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
//            log("onQueryComplete, sim association");
//            
//            ArrayList associateSims = new ArrayList();
//            
//            final String number = (String)cookie;
//            
//            try {
//                if(cursor != null && cursor.moveToFirst()) {
//                    do {
//                        if(cursor.getInt(0) > 0)
//                            associateSims.add(Integer.valueOf(cursor.getInt(0)));
//                    } while (cursor.moveToNext());
//                }
//            } catch(Exception e) {
//                log("onQueryComplete, cursor exception");
//            } finally {
//                if(cursor != null)
//                    cursor.close();
//            }
//            
//            log("onQueryComplete, associateSims = "+associateSims);
//            
//            ContactsUtils.dial(mContext, 
//                               number, 
//                               Settings.System.DEFAULT_SIM_NOT_SET,
//                               associateSims, 
//                               new ContactsUtils.OnDialCompleteListener() {
//                                   public void onDialComplete(boolean dialed) {
//                                       StickyTabs.saveTab(TwelveKeyDialer.this, getIntent());
//                                  }
//                               },
//                               TwelveKeyDialer.this);
//        }
//    }
    
    void setSimIndicatorVisibility(boolean visible) {
        if(visible)
            mStatusBarMgr.showSIMIndicator(getComponentName(), Settings.System.VOICE_CALL_SIM_SETTING);
        else
            mStatusBarMgr.hideSIMIndicator(getComponentName());
    }
    
    Runnable mServiceComplete = new Runnable() {
        public void run() {
            int result = mCellConnMgr.getResult();
            mSlot = mCellConnMgr.getPreferSlot();
            log("mServiceComplete, result = "+result+" mSlot = "+mSlot);
            if(result == com.mediatek.CellConnService.CellConnMgr.RESULT_STATE_NORMAL) {
                if(mIsVTCall){
                    dialButtonPressedextInt(mNumber, mSlot);
                    log("mServiceComplete, VT Call mSlot " + mSlot +", number "+mNumber);
                }else if(!TextUtils.isEmpty(mNumber)){
                    Intent intent = ContactsUtils.generateDialIntent(false, mSlot, mNumber);
                    sendBroadcast(intent);
                    log("mServiceComplete, Voice Call mSlot " + mSlot +", number "+mNumber);
                }else{
                    log("mServiceComplete, number is empty.");
                }
                mDialed = false;
            } else {
                mIsVTCall = false;
            }
        }
    };
    /* added by xingping.zheng end */

	public CharSequence filter(CharSequence source, int start, int end,
			Spanned dest, int dstart, int dend) {
		// TODO Auto-generated method stub
		String result = "";
		for (int i = start; i < end; i++) {
			if (ok(source.charAt(i))) {
				result += source.charAt(i);
			}
		}

		return result;
	}
	
	private boolean ok(char c){
		for(int i = 0; i < CHARACTERS.length; i ++)
			if(c == CHARACTERS[i])return true;
		return false;
	}
	
	private void log(String msg) {
		if (DEBUG)
			Log.i(TAG, msg);
	}

    // mtk80909 for Speed Dial
	private boolean isSimReady(final int simIndicator) {
        Log.d(TAG, "isSimReady(), simIndicator= "+simIndicator);
    
		final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
		if (null == iTel)
			return false;
		try {
			if (FeatureOption.MTK_GEMINI_SUPPORT) {
                int simId;
                if (10 <= simIndicator ) {  //USIM
                    simId = simIndicator - 10 -1;
                } else {        //SIM
                    simId = simIndicator - 1;
                }
                Log.d(TAG, "isSimReady(), simId=  "+simId);
				return iTel.hasIccCardGemini(simId)
				&& iTel.isRadioOnGemini(simId)
				&& !iTel.isFDNEnabledGemini(simId)
				&& TelephonyManager.SIM_STATE_READY == TelephonyManager
						.getDefault().getSimStateGemini(simId)
				&& !ContactsUtils.isServiceRunning[simId];
			} else {
				return iTel.hasIccCard()
				&& iTel.isRadioOn()
				&& !iTel.isFDNEnabled()
				&& TelephonyManager.SIM_STATE_READY == TelephonyManager
						.getDefault().getSimState()
				&& !ContactsUtils.isServiceRunning[0];
			}
		} catch (RemoteException e) {
			Log.w(TAG, "RemoteException!");
			return false;
		}
	}

    public boolean onDial(Context context, int slot, String number) {
        // TODO Auto-generated method stub
        log("onDial");
        boolean retval = true;
        final Context _context = context;
        if(slot == (int)Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
            int enabled = Settings.System.getInt(context.getContentResolver(), Settings.System.ENABLE_INTERNET_CALL, 0);
            log("enabled = "+enabled);
            if(enabled == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.reminder)
                       .setMessage(R.string.enable_sip_dialog_message)
                       .setNegativeButton(android.R.string.no, (DialogInterface.OnClickListener)null)
                       .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                           
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            Intent intent = new Intent();
                            intent.setClassName("com.android.phone", "com.android.phone.SipCallSetting");
                            _context.startActivity(intent);
                        }
                    });
                mTurnOnSipDialog = builder.create();
                // hide sim selection dialog
                ContactsUtils.dispatchActivityOnPause();
                mTurnOnSipDialog.show();
                retval = false;
                mDialed = true;
            } else {
                log("dial sip call, number = "+number);
                Intent intent = ContactsUtils.generateDialIntent(true, slot, number);
                //context.startActivity(intent);
                context.sendBroadcast(intent);
                mDialed = false;
            }
        } else {
            mSlot = slot;
            mNumber = number;
            mIsVTCall = false;
            int result = mCellConnMgr.handleCellConn(slot, CellConnMgr.REQUEST_TYPE_ROAMING);
            log("result = "+result);
            retval = false;
            /*
            if(result == com.mediatek.CellConnService.CellConnMgr.RESULT_STATE_NORMAL) {
                Intent intent = ContactsUtils.generateDialIntent(false, slot, number);
                //context.startActivity(intent);
                context.sendBroadcast(intent);
            } else
                retval = false;
            */
        }
        StickyTabs.saveTab(this, getIntent());
        return retval;
    }
    
    /**
     * remove SIM contacts if Airpland mode is on
     * Just reuse code in ContactsListActivity. 
     * TO DO: Here should be optimized in future.
     */
    private void removeSim1ContactsInAirplaneMode(){

        Log.i(TAG, "IN removeSim1ContactsInAirplaneMode ");
        TwelveKeyDialer.this.closeContextMenu();

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
            new Thread(new Runnable() {
              public void run() {
                  long simId1 = 0;
              	 SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(TwelveKeyDialer.this, com.android.internal.telephony.Phone.GEMINI_SIM_1);
           	    if (simInfo1 != null) {
           	    	simId1 = simInfo1.mSimId;
           	    	Log.i(TAG,"simId1 is " + simId1);
           	        }
                    int deleteCount = getContentResolver().
                                   delete(RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("sim", "true").build(), 
                                           RawContacts.INDICATE_PHONE_SIM + " = " + simId1, null);
                    if (deleteCount > 0 ) {
                    	TwelveKeyDialer.delCount = deleteCount;
                        TwelveKeyDialer.this.mDBHandlerForDelContacts.sendEmptyMessage(DS_MSG_CONTACTS_DELETE_CHANGED);
                    }
                Log.i(TAG, "After delete sim1");
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
            if (null != iTel && !iTel.hasIccCard()) {
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
            new Thread(new Runnable() {
              public void run() {
              	long simId1 = 0;
              	 SIMInfo simInfo1 = SIMInfo.getSIMInfoBySlot(TwelveKeyDialer.this, com.android.internal.telephony.Phone.GEMINI_SIM_1);
            	    if (simInfo1 != null) {
            	    	simId1 = simInfo1.mSimId;
            	    	Log.i(TAG,"single sim simId1 is " + simId1);
            	        }
            	    	int deleteCount =  getContentResolver().
                              delete(RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("sim", "true").build(), 
                                      RawContacts.INDICATE_PHONE_SIM + "=" + simId1, null);
                        if (deleteCount > 0 ) {
                        	TwelveKeyDialer.delCount = deleteCount;
                            TwelveKeyDialer.this.mDBHandlerForDelContacts.sendEmptyMessage(DS_MSG_CONTACTS_DELETE_CHANGED);
                        }
                Log.i(TAG, "After delete sim");
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
    
    private void removeSim2ContactsInAirplaneMode(){

        Log.i(TAG, "IN removeSim1ContactsInAirplaneMode ");
        TwelveKeyDialer.this.closeContextMenu();
        
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

          if (!sim2RadioOn || !sim2Ready) {
            Log
                .i(
                    TAG,
                    "TelephonyManager.getDefault().getSimStateGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2) is "
                        + TelephonyManager
                            .getDefault()
                            .getSimStateGemini(
                                com.android.internal.telephony.Phone.GEMINI_SIM_2));
            new Thread(new Runnable() {
              public void run() {
                  long simId2 = 0;
              	   SIMInfo simInfo2 = SIMInfo.getSIMInfoBySlot(TwelveKeyDialer.this, com.android.internal.telephony.Phone.GEMINI_SIM_2);
             	    if (simInfo2 != null) {
             	    	simId2 = simInfo2.mSimId;
             	    	Log.i(TAG,"simId2 is " + simId2);
             	        }
             	    	int deleteCount = getContentResolver().
                                   delete(RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("sim", "true").build(), 
                                           RawContacts.INDICATE_PHONE_SIM + "=" + simId2, null);
                        if (deleteCount > 0 ) {
                        	TwelveKeyDialer.delCount = deleteCount;
                            TwelveKeyDialer.this.mDBHandlerForDelContacts.sendEmptyMessage(DS_MSG_CONTACTS_DELETE_CHANGED);
                        }
                Log.i(TAG, "After delete sim2");
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
  }//end file
      
