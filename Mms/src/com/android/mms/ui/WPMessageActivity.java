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
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

package com.android.mms.ui;

import static com.android.mms.transaction.ProgressCallbackEntity.PROGRESS_ABORT;
import static com.android.mms.transaction.ProgressCallbackEntity.PROGRESS_COMPLETE;
import static com.android.mms.transaction.ProgressCallbackEntity.PROGRESS_START;
import static com.android.mms.transaction.ProgressCallbackEntity.PROGRESS_STATUS_ACTION;
import static com.android.mms.ui.WPMessageListAdapter.COLUMN_ID;
import static com.android.mms.ui.WPMessageListAdapter.COLUMN_WPMS_TYPE;
import static com.android.mms.ui.WPMessageListAdapter.WP_PROJECTION;
import static com.android.mms.ui.WPMessageListAdapter.COLUMN_WPMS_LOCKED;
import com.android.internal.telephony.ITelephony;
import com.android.mms.transaction.SmsReceiverService;
import com.android.mms.transaction.WapPushMessagingNotification;
import com.android.mms.util.ThreadCountManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SqliteWrapper;
import android.drm.mobile1.DrmException;
import android.drm.mobile1.DrmRawContent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.CamcorderProfile;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.ContactsContract;
import android.provider.DrmStore;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Telephony;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.Telephony.WapPush;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Config;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.android.internal.widget.ContactHeaderWidget;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.android.mms.transaction.SendTransaction;

import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.mms.LogTag;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.WorkingMessage;
import com.android.mms.data.WorkingMessage.MessageStatusListener;
import com.google.android.mms.ContentType;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.SendReq;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.ConversationList.BaseProgressQueryHandler;
import com.android.mms.ui.MessageUtils.ResizeImageResultCallback;
import com.android.mms.ui.RecipientsEditor.RecipientContextMenuInfo;
import com.android.mms.util.SendingProgressTokenManager;
import com.android.mms.util.SmileyParser;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;

//add for gemini
import com.android.internal.telephony.IccCard;
import android.telephony.TelephonyManager;
import android.os.RemoteException;
import android.provider.Telephony.Mms;
import android.provider.Browser;
import com.mediatek.wappush.SiExpiredCheck;
import android.widget.EditText;
import android.view.inputmethod.InputMethodManager;
/**
* This is the main UI for:
* 1. Viewing wappush message history of a conversation.
*/
public class WPMessageActivity extends Activity
       implements Contact.UpdateListener,View.OnClickListener {
   public static final int REQUEST_CODE_ADD_CONTACT      = 18;

   private static final String WP_TAG = "wappush";

   private static final boolean DEBUG = false;
   private static final boolean TRACE = false;
   private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;

   // Menu ID
   private static final int MENU_DELETE_THREAD         = 1;
   private static final int MENU_CONVERSATION_LIST     = 6;

   // Context menu ID
   private static final int MENU_VIEW_CONTACT          = 12;
   private static final int MENU_ADD_TO_CONTACTS       = 13;
   private static final int MENU_VIEW_MESSAGE_DETAILS  = 17;
   private static final int MENU_DELETE_MESSAGE        = 18;
   private static final int MENU_FORWARD_MESSAGE       = 21;
   private static final int MENU_COPY_MESSAGE_TEXT     = 24;
   private static final int MENU_ADD_ADDRESS_TO_CONTACTS = 27;
   private static final int MENU_LOCK_MESSAGE          = 28;
   private static final int MENU_UNLOCK_MESSAGE        = 29;
   private static final int MENU_ADD_TO_BOOKMARK       = 30;
   private static final int MENU_GOTO_BROWSER          = 31;

   private static final int MESSAGE_LIST_QUERY_TOKEN = 9527;
   private static final int DELETE_MESSAGE_TOKEN  = 9700;

   private ContentResolver mContentResolver;
   private BackgroundQueryHandler mBackgroundQueryHandler;
   private Conversation mConversation;     // Conversation we are working in
   private WPMessageListView mMsgListView;        // ListView for messages in this conversation
   public WPMessageListAdapter mMsgListAdapter;  // and its corresponding ListAdapter
   private ContactHeaderWidget mContactHeader;
   private ImageView mContactLauncher;

   private AlertDialog mDetailDialog;
   private static final String STR_RN = "\\r\\n"; // for "\r\n"
   private static final String STR_CN = "\n"; // the char value of '\n'
   public static boolean mDestroy = false;
   private Long mThreadId = -1l;
   private Intent mAddContactIntent;   // Intent used to add a new contact   
   //private boolean isDiscard = false;  //related with add to Contact
   
   private boolean mPossiblePendingNotification;   // If the message list has changed, we may have
   //SiExpired Check
   private SiExpiredCheck siExpiredCheck;
   
   //add for multi-delete
   private View mDeletePanel;              // View containing the delete and cancel buttons
   private View mSelectPanel;              // View containing the select all check box
   private Button mDeleteButton;
   private Button mCancelButton;
   private CheckBox mSelectedAll;
   
   //
   private static final int SI_ACTION_NONE = 0;
   private static final int SI_ACTION_LOW = 1;
   private static final int SI_ACTION_MEDIUM = 2;
   private static final int SI_ACTION_HIGH = 3;
   private static final int SI_ACTION_DELETE = 4;
   
   private static final int SL_ACTION_LOW = 1;
   private static final int SL_ACTION_HIGH = 2;
   private static final int SL_ACTION_CACHE = 3;
   

   @SuppressWarnings("unused")
   private static void log(String logMsg) {
       Thread current = Thread.currentThread();
       long tid = current.getId();
       StackTraceElement[] stack = current.getStackTrace();
       String methodName = stack[3].getMethodName();
       // Prepend current thread ID and name of calling method to the message.
       logMsg = "[" + tid + "] [" + methodName + "] " + logMsg;
       Log.d(WP_TAG, "WPMessageActivity: " + logMsg);
   }

   //==========================================================
   // Inner classes
   //==========================================================
   private final Handler mMessageListItemHandler = new Handler() {
       @Override
       public void handleMessage(Message msg) {
    	   String type;
    	   switch (msg.what) {             
    	   case MessageListItem.ITEM_CLICK: {
    		   //add for multi-delete               		
    		   mMsgListAdapter.changeSelectedState(msg.arg1);
    		   if (mMsgListAdapter.getSelectedNumber() > 0) {
    			   mDeleteButton.setEnabled(true);
    			   if (mMsgListAdapter.getSelectedNumber() == mMsgListAdapter.getCount()) {
    				   mSelectedAll.setChecked(true);
    				   return;
    			   }
    		   } else {
    			   mDeleteButton.setEnabled(false);
    		   }
    		   mSelectedAll.setChecked(false);
    		   break;
    	   }

    	   default:                
    		   return;
    	   }
       }
   };

   /**
    * Return the messageItem associated with the type ("si" or "sl") and message id.
    * @param type Type of the message: "si" or "sl"
    * @param msgId Message id of the message. This is the _id of the wappush msg or pdu row and is
    * stored in the WPMessageItem
    * @param createFromCursorIfNotInCache true if the item is not found in the WPMessageListAdapter's
    * cache and the code can create a new WPMessageItem based on the position of the current cursor.
    * If false, the function returns null if the WPMessageItem isn't in the cache.
    * @return WPMessageItem or null if not found and createFromCursorIfNotInCache is false
    */
   private WPMessageItem getMessageItem(int type, long msgId, boolean createFromCursorIfNotInCache) {
       return mMsgListAdapter.getCachedMessageItem(type, msgId,
               createFromCursorIfNotInCache ? mMsgListAdapter.getCursor() : null);
   }

   private boolean isCursorValid() {
       // Check whether the cursor is valid or not.
       Cursor cursor = mMsgListAdapter.getCursor();
       if (cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
           Log.e(WP_TAG, "WPMessageActivity: " + "Bad cursor.", new RuntimeException());
           return false;
       }
       return true;
   }

   @Override
   public void startActivityForResult(Intent intent, int requestCode)
   {
       if (null != intent && null != intent.getData()
               && intent.getData().getScheme().equals("mailto")) {
           try {
               super.startActivityForResult(intent, requestCode);
           } catch (ActivityNotFoundException e) {
               Log.w(WP_TAG, "Failed to startActivityForResult: " + intent);
               Intent i = new Intent().setClassName("com.android.email", "com.android.email.activity.setup.AccountSetupBasics");
               this.startActivity(i);
               finish();
           } catch (Exception e) {
               Log.e(WP_TAG, "Failed to startActivityForResult: " + intent);
               Toast.makeText(this,getString(R.string.message_open_email_fail),
                     Toast.LENGTH_SHORT).show();
         }
       } else {
       super.startActivityForResult(intent, requestCode);
       }
   }
   
   //add for multi-delete
   private void markCheckedState(boolean checkedState) {
   	mMsgListAdapter.setItemsValue(checkedState, null);
   	mDeleteButton.setEnabled(checkedState);
       int count = mMsgListView.getChildCount();     
       ViewGroup layout = null;
       int childCount = 0;
       View view = null;
       for (int i = 0; i < count; i++) {
    	   layout = (ViewGroup)mMsgListView.getChildAt(i);
    	   childCount = layout.getChildCount();
           
           for (int j = 0; j < childCount; j++) {
        	   view = layout.getChildAt(j);
               if (view instanceof CheckBox) {
               	if (!mMsgListAdapter.mIsDeleteMode) {
               		((CheckBox)view).setChecked(false);
               		((CheckBox)view).setVisibility(View.GONE);
               	} else {
               		((CheckBox)view).setVisibility(View.VISIBLE);
                       ((CheckBox)view).setChecked(checkedState);
               	}               	
                   break;
               }
           }
       }
   }
   
   private class DeleteMessageListener implements OnClickListener {
       private final Uri mDeleteUri;
       private final boolean mDeleteLocked;

       public DeleteMessageListener(Uri uri, boolean deleteLocked) {
           mDeleteUri = uri;
           mDeleteLocked = deleteLocked;
       }

       public DeleteMessageListener(long msgId, int type, boolean deleteLocked) {
           mDeleteUri = ContentUris.withAppendedId(WapPush.CONTENT_URI, msgId);
           mDeleteLocked = deleteLocked;
       }

       public void onClick(DialogInterface dialog, int whichButton) {
           mBackgroundQueryHandler.startDelete(DELETE_MESSAGE_TOKEN,
                   null, mDeleteUri, mDeleteLocked ? null : "locked=0", null);
       }
   }

   //add for multi-delete
   private void changeDeleteMode() {
	   mMsgListAdapter.mIsDeleteMode = !mMsgListAdapter.mIsDeleteMode;
	   markCheckedState(false);
	   if (mMsgListAdapter.mIsDeleteMode) {
		   mSelectPanel.setVisibility(View.VISIBLE);
		   mDeletePanel.setVisibility(View.VISIBLE);
		   mSelectedAll.setChecked(false);
	   } else {
		   mSelectPanel.setVisibility(View.GONE);
		   mDeletePanel.setVisibility(View.GONE);
	   }
	   if (!mMsgListAdapter.mIsDeleteMode) {
       	   mMsgListAdapter.clearList();
       }
   }
   
   private boolean canAddToContacts(Contact contact) {
       // There are some kind of automated messages, like STK messages, that we don't want
       // to add to contacts. These names begin with special characters, like, "*Info".
       final String name = contact.getName();
       if (!TextUtils.isEmpty(contact.getNumber())) {
           char c = contact.getNumber().charAt(0);
           if (isSpecialChar(c)) {
               return false;
           }
       }
       if (!TextUtils.isEmpty(name)) {
           char c = name.charAt(0);
           if (isSpecialChar(c)) {
               return false;
           }
       }
       if (!(Mms.isEmailAddress(name) || Mms.isPhoneNumber(name) ||
               MessageUtils.isLocalNumber(contact.getNumber()))) {     // Handle "Me"
           return false;
       }
       return true;
   }

   private boolean isSpecialChar(char c) {
       return c == '*' || c == '%' || c == '$';
   }

   private final void addCallAndContactMenuItems(
           ContextMenu menu, MsgListMenuClickListener l, WPMessageItem msgItem) {
       // Add all possible links in the address & message
       StringBuilder textToSpannify = new StringBuilder();
       textToSpannify.append(msgItem.mAddress + ": ");
       textToSpannify.append(msgItem.mBody);

       SpannableString msg = new SpannableString(textToSpannify.toString());
       Linkify.addLinks(msg, Linkify.ALL);
       ArrayList<String> uris =
           MessageUtils.extractUris(msg.getSpans(0, msg.length(), URLSpan.class));
       
       Log.i(WP_TAG, "WPMessageActivity: " + "uris is : " + uris);

       while (uris.size() > 0) {
           String uriString = uris.remove(0);
           // Remove any dupes so they don't get added to the menu multiple times
           while (uris.contains(uriString)) {
               uris.remove(uriString);
           }

           int sep = uriString.indexOf(":");
           String prefix = null;
           if (sep >= 0) {
               prefix = uriString.substring(0, sep);
               uriString = uriString.substring(sep + 1);
           }
           boolean addToContacts = false;
           if ("mailto".equalsIgnoreCase(prefix))  {
        	   //wappush does not need mail context menu
               addToContacts = !haveEmailContact(uriString);
           } else if ("tel".equalsIgnoreCase(prefix)) {
        	   //wappush does not need call context menu
               addToContacts = !isNumberInContacts(uriString);
           }
           if (addToContacts) {               
               //isDiscard = false;
               
              // Intent intent = ConversationList.createAddContactIntent(uriString);
                Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, uriString);
               String addContactString = getString(
                       R.string.menu_add_address_to_contacts).replace("%s", uriString);
               menu.add(0, MENU_ADD_ADDRESS_TO_CONTACTS, 0, addContactString)
                   .setOnMenuItemClickListener(l)
                   .setIntent(intent);
           }
       }
   }

   private boolean haveEmailContact(String emailAddress) {
       Cursor cursor = SqliteWrapper.query(this, getContentResolver(),
               Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(emailAddress)),
               new String[] { Contacts.DISPLAY_NAME }, null, null, null);

       if (cursor != null) {
           try {
        	   String name = null;
               while (cursor.moveToNext()) {
            	   name = cursor.getString(0);
                   if (!TextUtils.isEmpty(name)) {
                       return true;
                   }
               }
           } finally {
               cursor.close();
           }
       }
       return false;
   }

   private boolean isNumberInContacts(String phoneNumber) {
       return Contact.get(phoneNumber, false).existsInDatabase();
   }

   private final OnCreateContextMenuListener mMsgListMenuCreateListener =
       new OnCreateContextMenuListener() {
       public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
           Cursor cursor = mMsgListAdapter.getCursor();
           int type = cursor.getInt(COLUMN_WPMS_TYPE);
           long msgId = cursor.getLong(COLUMN_ID);

           WPMessageItem msgItem = mMsgListAdapter.getCachedMessageItem(type, msgId, cursor);
           if (msgItem == null) {
               Log.e(WP_TAG, "WPMessageActivity: " + "Cannot load message item for type = " + type
                       + ", msgId = " + msgId);
               return;
           }
           
           menu.setHeaderTitle(R.string.message_options);

           MsgListMenuClickListener l = new MsgListMenuClickListener();

           if (msgItem.mLocked) {
               menu.add(0, MENU_UNLOCK_MESSAGE, 0, R.string.menu_unlock)
                   .setOnMenuItemClickListener(l);
           } else {
               menu.add(0, MENU_LOCK_MESSAGE, 0, R.string.menu_lock)
                   .setOnMenuItemClickListener(l);
           }

           addCallAndContactMenuItems(menu, l, msgItem);
           
           if(null != msgItem.mURL){
        	   menu.add(0, MENU_GOTO_BROWSER, 0, R.string.menu_goto_browser)
               .setOnMenuItemClickListener(l);
        	   
        	   menu.add(0, MENU_ADD_TO_BOOKMARK, 0, R.string.menu_add_to_bookmark)
               .setOnMenuItemClickListener(l);
           }
           
           menu.add(0, MENU_FORWARD_MESSAGE, 0, R.string.menu_forward)
                   .setOnMenuItemClickListener(l);

           menu.add(0, MENU_COPY_MESSAGE_TEXT, 0, R.string.copy_message_text)
                   .setOnMenuItemClickListener(l);

           menu.add(0, MENU_VIEW_MESSAGE_DETAILS, 0, R.string.view_message_details)
                   .setOnMenuItemClickListener(l);
           menu.add(0, MENU_DELETE_MESSAGE, 0, R.string.delete_message)
                   .setOnMenuItemClickListener(l);
           }
   };

   private void copyToClipboard(String str) {
       ClipboardManager clip =
           (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
       clip.setText(str);
   }

   private void forwardMessage(WPMessageItem msgItem) {
       Intent intent = createIntent(this, 0);

       intent.putExtra("exit_on_sent", true);
       intent.putExtra("forwarded_message", true);

       Log.i(WP_TAG, "WPMessageActivity: " + "forwardMessage msgItem.mBody is : " + msgItem.mBody);
       intent.putExtra("sms_body", msgItem.mBody);
       // ForwardMessageActivity is simply an alias in the manifest for ComposeMessageActivity.
       // We have to make an alias because ComposeMessageActivity launch flags specify
       // singleTop. When we forward a message, we want to start a separate ComposeMessageActivity.
       // The only way to do that is to override the singleTop flag, which is impossible to do
       // in code. By creating an alias to the activity, without the singleTop flag, we can
       // launch a separate ComposeMessageActivity to edit the forward message.
       intent.setClassName(this, "com.android.mms.ui.ForwardMessageActivity");
       startActivity(intent);
   }

   /**
    * Context menu handlers for the message list view.
    */
   private final class MsgListMenuClickListener implements MenuItem.OnMenuItemClickListener {
       public boolean onMenuItemClick(MenuItem item) {
           if (!isCursorValid()) {
               return false;
           }
           Cursor cursor = mMsgListAdapter.getCursor();
           int type = cursor.getInt(COLUMN_WPMS_TYPE);
           long msgId = cursor.getLong(COLUMN_ID);
           WPMessageItem msgItem = getMessageItem(type, msgId, true);

           if (msgItem == null) {
               return false;
           }

           switch (item.getItemId()) {
               case MENU_COPY_MESSAGE_TEXT:
                   String copyBody = msgItem.mBody.replaceAll(STR_RN, STR_CN);
                   copyToClipboard(copyBody);
                   return true;

               case MENU_FORWARD_MESSAGE:
                   forwardMessage(msgItem);
                   return true;
                   
               case MENU_GOTO_BROWSER:
            	   if(null == msgItem.mURL){
            		   Log.i(WP_TAG, "WPMessageActivity: " + "MENU_GOTO_BROWSER msgItem.mURL is null.");
            		   return true;
            	   }
            	   Uri uri = Uri.parse(MessageUtils.CheckAndModifyUrl(msgItem.mURL));
                   Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                   intent.putExtra(Browser.EXTRA_APPLICATION_ID, WPMessageActivity.this.getPackageName());
                   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                   try{
                   		startActivity(intent);
                   }catch(ActivityNotFoundException ex){
                	   Toast.makeText(WPMessageActivity.this, R.string.error_unsupported_scheme, Toast.LENGTH_LONG).show();
                   	   Log.e(WP_TAG,"Scheme " + uri.getScheme() + "is not supported!" );
                   }
                   
                   return true;
                   
               case MENU_ADD_TO_BOOKMARK:
            	   if(null == msgItem.mURL){
            		   Log.i(WP_TAG, "WPMessageActivity: " + "MENU_GOTO_BROWSER msgItem.mURL is null.");
            		   return true;
            	   }
            	   Intent i = new Intent();
                   i.setAction("android.intent.action.INSERT");
                   i.setType("vnd.android.cursor.dir/bookmark");
                   i.putExtra("url", MessageUtils.CheckAndModifyUrl(msgItem.mURL));
                   startActivity(i);
                   return true;

               case MENU_VIEW_MESSAGE_DETAILS: {
                   String messageDetails = getWPMessageDetails(WPMessageActivity.this, cursor);
                   mDetailDialog = new AlertDialog.Builder(WPMessageActivity.this)
                           .setTitle(R.string.message_details_title)
                           .setMessage(messageDetails)
                           .setPositiveButton(android.R.string.ok, null)
                           .setCancelable(true)
                           .show();
                   return true;
               }
               case MENU_DELETE_MESSAGE: {
                   DeleteMessageListener l = new DeleteMessageListener(
                		   msgId, type, msgItem.mLocked);
                   String where = Telephony.WapPush._ID + "=" + msgItem.mMsgId;
                   String[] projection = new String[] { WapPush.THREAD_ID };
                   Log.d(WP_TAG, "WPMessageActivity: " + "where:" + where);
                   Cursor queryCursor = getContentResolver().query(WapPush.CONTENT_URI,
                           projection, where, null, null);
                   if (queryCursor.moveToFirst()) {
                       mThreadId = queryCursor.getLong(0);
                   }
                   confirmDeleteDialog(l, msgItem.mLocked);
                   return true;
               }
               case MENU_LOCK_MESSAGE: {
                   lockMessage(msgItem, true);
                   return true;
               }

               case MENU_UNLOCK_MESSAGE: {
                   lockMessage(msgItem, false);
                   return true;
               }
               
               case MENU_ADD_ADDRESS_TO_CONTACTS:{
            	   mAddContactIntent = item.getIntent();
		   String number = "";
		   number = mAddContactIntent.getStringExtra(ContactsContract.Intents.Insert.PHONE);
            	   //startActivityForResult(mAddContactIntent, REQUEST_CODE_ADD_CONTACT);
		   onAddContactButtonClickInt(number, true);
            	   return true;
               }               

               default:
                   return false;
           }
       }
   }
   
   private static String getWPMessageDetails(Context context, Cursor cursor) {
       StringBuilder details = new StringBuilder();
       Resources res = context.getResources();

       // Message Type: WP message.
       details.append(res.getString(R.string.message_type_label));
       details.append(res.getString(R.string.wp_msg_type));
       
       int type = cursor.getInt(WPMessageListAdapter.COLUMN_WPMS_TYPE);
       Log.i(WP_TAG, "WPMessageActivity: " + "type is : " + type);
       
       //Priority: Low, Medium, High
       //SI: None, Low, Medium, High, Delete
       //Priority: SL: High, Low, Cache
       details.append('\n');
       details.append(res.getString(R.string.wp_msg_priority_label));
       int priority = cursor.getInt(WPMessageListAdapter.COLUMN_WPMS_ACTION);
       if(WapPush.TYPE_SI == type){
    	   switch(priority){
    	   case SI_ACTION_NONE:
    		   Log.i(WP_TAG, "WPMessageActivity: " + "action error, none");
    		   break;
    	   case SI_ACTION_LOW:
    		   details.append(res.getString(R.string.wp_msg_priority_low));
    		   break;
    	   case SI_ACTION_MEDIUM:
    		   details.append(res.getString(R.string.wp_msg_priority_medium));
    		   break;
    	   case SI_ACTION_HIGH:
    		   details.append(res.getString(R.string.wp_msg_priority_high));
    		   break;
    	   case SI_ACTION_DELETE:
    		   Log.i(WP_TAG, "WPMessageActivity: " + "action error, delete");
    		   break;
    	   default:
    		   Log.i(WP_TAG, "WPMessageActivity: " + "getWPMessageDetails si priority error.");
    	   }
       }else if(WapPush.TYPE_SL == type){
    	   switch(priority){
    	   case SL_ACTION_LOW:
    		   details.append(res.getString(R.string.wp_msg_priority_low));
    		   break;
    	   case SL_ACTION_HIGH:
    		   details.append(res.getString(R.string.wp_msg_priority_high));
    		   break;
    	   case SL_ACTION_CACHE:
    		   details.append(res.getString(R.string.wp_msg_priority_low));
    		   break;
    	   default:
    		   Log.i(WP_TAG, "WPMessageActivity: " + "getWPMessageDetails sl priority error.");
    	   }
       }else{
    	   Log.i(WP_TAG, "WPMessageActivity: " + "getWPMessageDetails type error.");
       }
       
//       //Created time: ***
//       if(WapPush.TYPE_SI == type){
//    	   details.append('\n');
//           details.append(res.getString(R.string.wp_msg_created_label));
//           long createdDate = cursor.getLong(WPMessageListAdapter.COLUMN_WPMS_CREATE) * 1000;
//           if(0 == createdDate){
//        	   details.append(res.getString(R.string.wp_msg_created_unknown));
//           }else{
//        	   details.append(MessageUtils.formatTimeStampString(context, createdDate, true));
//           }           
//       }       

       // Address: ***
       details.append('\n');
       details.append(res.getString(R.string.from_label));       
       details.append(cursor.getString(WPMessageListAdapter.COLUMN_WPMS_ADDR));

       // Date: ***
       details.append('\n');
       details.append(res.getString(R.string.received_label));
       long date = cursor.getLong(WPMessageListAdapter.COLUMN_WPMS_DATE);
       details.append(MessageUtils.formatTimeStampString(context, date, true));
       
       //Expired time
       long expiredDate = cursor.getLong(WPMessageListAdapter.COLUMN_WPMS_EXPIRATION)*1000;
       if(expiredDate != 0){
           details.append('\n');
           details.append(String.format(context.getString(R.string.wp_msg_expiration_label), MessageUtils.formatTimeStampString(context, expiredDate,true)));
       }

       return details.toString();
   }

   private void lockMessage(WPMessageItem msgItem, boolean locked) {
       Uri uri;
       uri = WapPush.CONTENT_URI;
       final Uri lockUri = ContentUris.withAppendedId(uri, msgItem.mMsgId);

       final ContentValues values = new ContentValues(1);
       values.put("locked", locked ? 1 : 0);

       new Thread(new Runnable() {
           public void run() {
               getContentResolver().update(lockUri,
                       values, null, null);
           }
       }).start();
   }
   

   private ContactList getRecipients() {
       return mConversation.getRecipients();
   }

   private void updateTitle(ContactList list) {
       String s;
       switch (list.size()) {
           case 1: {
               s = list.get(0).getNameAndNumber();
               break;
           }
           default: {
               // Handle multiple recipients
               s = list.formatNames(", ");
               break;
           }
       }
       getWindow().setTitle(s);
   }

   //==========================================================
   // Activity methods
   //==========================================================
   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       
       Log.i(WP_TAG, "WPMessageActivity: " + "Enter onCreate function.");

       setContentView(R.layout.wp_message_activity);

       //avoid open application more than one times
       if(savedInstanceState != null) {
           hasOpenApplication = savedInstanceState.getBoolean("hasOpenApplication");
       }
       openApplication();
       
       // Initialize members for UI elements.
       mMsgListView = (WPMessageListView) findViewById(R.id.wp_history);
       mMsgListView.setDivider(null);      // no divider so we look like IM conversation.
       
       mContactHeader = (ContactHeaderWidget) findViewById(R.id.association_header_widget);
       mContactLauncher = (ImageView) findViewById(R.id.launcher_contacts);

       //add for multi-delete
       mDeletePanel = findViewById(R.id.delete_panel);
       mSelectPanel = findViewById(R.id.select_panel);
       mSelectPanel.setOnClickListener(this);
       mSelectedAll = (CheckBox)findViewById(R.id.select_all_checked);
       mCancelButton = (Button)findViewById(R.id.cancel);
       mCancelButton.setOnClickListener(this);
       mDeleteButton = (Button)findViewById(R.id.delete);
       mDeleteButton.setOnClickListener(this);
       mDeleteButton.setEnabled(false);
       
       
       mContentResolver = getContentResolver();
       mBackgroundQueryHandler = new BackgroundQueryHandler(mContentResolver);

       initialize(savedInstanceState);
       mDestroy = false;
       
       siExpiredCheck = new SiExpiredCheck(this);
       siExpiredCheck.startSiExpiredCheckThread();
   }

   // if then intent is send from notification, we will open the latest push message's url.
   boolean hasOpenApplication = false;
   public void openApplication(){
       if (hasOpenApplication == true) {
              return;
       }
       //avoid open application more than one time
	   hasOpenApplication = true;
	   Intent initIntent = getIntent();

	   int threadCount = initIntent.getIntExtra("THREAD_COUNT",0);
	   if(threadCount>0){
		   goToConversationList();
	   }
	   
       String url = initIntent.getStringExtra("URL");
       if(url!=null){
    	   Uri uri = Uri.parse(MessageUtils.CheckAndModifyUrl(url));
           Intent intent = new Intent(Intent.ACTION_VIEW, uri);
           intent.putExtra(Browser.EXTRA_APPLICATION_ID, WPMessageActivity.this.getPackageName());
           intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
           try{
           		startActivity(intent);
           }catch(ActivityNotFoundException ex){
        	   Toast.makeText(WPMessageActivity.this, R.string.error_unsupported_scheme, Toast.LENGTH_LONG).show();
           	   Log.e(WP_TAG,"Scheme " + uri.getScheme() + "is not supported!" );
           } 
       }
   }

   public void initialize(Bundle savedInstanceState) {
       Intent intent = getIntent();

       // mainly init the mConversation variable
       initActivityState(savedInstanceState, intent);

       log("initialize: savedInstanceState = " + savedInstanceState +
               " intent = " + intent +
               " mConversation = " + mConversation);

       // Set up the message history ListAdapter
       initMessageList();

       if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
           log("initialize: update title, mConversation=" + mConversation.toString());
       }

       //updateTitle(mConversation.getRecipients());
       bindToContactHeaderWidget(mConversation.getRecipients());
   }
   
   private void bindToContactHeaderWidget(ContactList list) {
       mContactHeader.wipeClean();
       mContactHeader.invalidate();
       
       /*
       mContactHeader.showStar(false);
       mContactHeader.bindFromContactLookupUri(mLookupUri);
       mContactHeader.setSnsPadVisibility(View.GONE);
       mContactHeader.setBtnUpdateVisibility(View.GONE);
       mContactHeader.setBtnStartVisibility(View.GONE);
       */
		
       switch (list.size()) {
           case 0:
               String recipient = "";
               mContactHeader.showStar(true);
   	       mContactHeader.setStarInvisible(true);
               mContactHeader.setDisplayName(recipient, null);
               break;
           case 1:
               final Contact contact = list.get(0);
               final String addr = contact.getNumber();
               final String name = list.formatNames("");
               final Uri contactUri = contact.getUri();
               Intent intent;
               
               if (contact.existsInDatabase()) {
                  mContactHeader.showStar(true);
   	              mContactHeader.setStarInvisible(true);
                  mContactHeader.setDisplayName(name,addr);
                  mContactHeader.setContactUri(contactUri,true);
                  mContactLauncher.setBackgroundResource(R.drawable.add_contact_selector_exist);
                  intent = new Intent(Intent.ACTION_VIEW, contactUri);

               } else {
                  //mContactHeader.setDisplayName(addr,null);
                  mContactHeader.showStar(true);
   	              mContactHeader.setStarInvisible(true);
                  mContactHeader.bindFromPhoneNumber(addr);
                  mContactLauncher.setBackgroundResource(R.drawable.add_contact_selector);
                  intent = ConversationList.createAddContactIntent(contact.getNumber());
               }

               //mContactHeader.setContactUri(contactUri,true);
               //set photo
               BitmapDrawable photo = (BitmapDrawable) contact.getAvatar(this,null);
               if (null != photo) {
                  mContactHeader.setPhoto(photo.getBitmap());
               }
               
               final Intent contactIntent = intent;
               mContactLauncher.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v) {
                        if (contactIntent.getAction() == Intent.ACTION_VIEW) {
                            contactIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            startActivity(contactIntent);
                        } else {
                            onAddContactButtonClickInt(contact.getNumber(), false);
                        }
                    }
                });
               break;
           default:
               // Handle multiple recipients
               mContactHeader.showStar(true);
   	           mContactHeader.setStarInvisible(true);
               mContactHeader.setDisplayName(list.formatNames(", "), null);
               mContactHeader.setContactUri(null);
               /*
               mContactHeader.setPhoto(((BitmapDrawable)getResources()
                       .getDrawable(R.drawable.ic_groupchat))
                       .getBitmap());
               */
               mContactHeader.setPresence(0);  // clear the presence, too
               break;
       }
   }
   
protected void onAddContactButtonClickInt(final String number ,final boolean startActivityForResult) {
		if (!TextUtils.isEmpty(number)) {
            String message = this.getResources().getString(R.string.add_contact_dialog_message, number);
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                                         .setTitle(number)
                                                         .setMessage(message);

			AlertDialog dialog = builder.create();

            dialog.setButton(AlertDialog.BUTTON_POSITIVE, this.getResources().getString(R.string.add_contact_dialog_existing), new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
                    Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
							intent.setType(Contacts.CONTENT_ITEM_TYPE);
                    intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
							if (startActivityForResult) {
								startActivityForResult(intent,
										REQUEST_CODE_ADD_CONTACT);
							} else {
								startActivity(intent);
							}
						}
					});

            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, this.getResources().getString(R.string.add_contact_dialog_new), new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
                    final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                    intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
							if (startActivityForResult) {
								startActivityForResult(intent,
										REQUEST_CODE_ADD_CONTACT);
							} else {
								startActivity(intent);
							}
						}

					});

			// AlertDialog mAddContactDialog = dialog;
			dialog.show();
		}
	}
   
   @Override
   protected void onNewIntent(Intent intent) {
	   super.onNewIntent(intent);
	   Log.i(WP_TAG,"onNewIntent");
	   setIntent(intent);

       //check if we need to open the latest message's url
       openApplication();
	   
       Conversation conversation = null;

       // If we have been passed a thread_id, use that to find our
       // conversation.
       long threadId = intent.getLongExtra("thread_id", 0);
       Uri intentUri = intent.getData();
       
       boolean sameThread = false;
       if (threadId > 0) {
           conversation = Conversation.get(this, threadId, false);
       } else {
           if (mConversation.getThreadId() == 0) {
               // We've got a draft. See if the new intent's recipient is the same as
               // the draft's recipient. First make sure the working recipients are synched
               // to the conversation.
               sameThread = mConversation.sameRecipient(intentUri);
           }
           if (!sameThread) {
               // Otherwise, try to get a conversation based on the
               // data URI passed to our intent.
               conversation = Conversation.get(this, intentUri, false);
           }
       }

       if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
           log("onNewIntent: data=" + intentUri + ", thread_id extra is " + threadId);
           log("     new conversation=" + conversation + ", mConversation=" + mConversation);
       }

       if (conversation != null) {
           // Don't let any markAsRead DB updates occur before we've loaded the messages for
           // the thread.
           //conversation.blockMarkAsRead(true);

           // this is probably paranoia to compare both thread_ids and recipient lists,
           // but we want to make double sure because this is a last minute fix for Froyo
           // and the previous code checked thread ids only.
           // (we cannot just compare thread ids because there is a case where mConversation
           // has a stale/obsolete thread id (=1) that could collide against the new thread_id(=1),
           // even though the recipient lists are different)
           sameThread = (conversation.getThreadId() == mConversation.getThreadId() &&
                   conversation.equals(mConversation));
       }

       if (sameThread) {
           log("onNewIntent: same conversation");
       } else {
           if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
               log("onNewIntent: different conversation, initialize...");
           }

           initialize(null);
           conversation.blockMarkAsRead(true);
           startMsgListQuery();
           //updateTitle(mConversation.getRecipients());
           bindToContactHeaderWidget(mConversation.getRecipients());
       }
   }
   
   @Override
   protected void onRestart() {
	   Log.i(WP_TAG, "WPMessageActivity: " + "Enter onRestart function.");
       super.onRestart();

// isDiscard isn't usefull now.No need to go back to conversation list after add an contact.
//       if(true == isDiscard){
//    	   isDiscard = false;
//    	   goToConversationList();
//       } 
   }

   @Override
   protected void onStart() {
       super.onStart();
       mConversation.blockMarkAsRead(true);
       
       /*
        * When this activity is stopped and the related contacts are changed, we should update the changes.
        */
       ContactList recipients = getRecipients();
       for(Contact c: recipients){
    	   c.reload();
       }
       
       startMsgListQuery();

       if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
           log("onStart: update title, mConversation=" + mConversation.toString());
       }

       //updateTitle(mConversation.getRecipients());
       bindToContactHeaderWidget(mConversation.getRecipients());
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
       super.onSaveInstanceState(outState);

       outState.putString("recipients", getRecipients().serialize());
       outState.putBoolean("hasOpenApplication", hasOpenApplication);
       
   }   

   @Override
   protected void onResume() {
       super.onResume();

       if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
           log("onResume: update title, mConversation=" + mConversation.toString());
       }

       addRecipientsListeners();
       // There seems to be a bug in the framework such that setting the title
       // here gets overwritten to the original title.  Do this delayed as a
       // workaround.
       mMessageListItemHandler.postDelayed(new Runnable() {
           public void run() {
               ContactList recipients = getRecipients();
               //updateTitle(recipients);
               bindToContactHeaderWidget(mConversation.getRecipients());
           }
       }, 100);
       
       Log.i("PUSH", "onResume function......");
       siExpiredCheck.startExpiredCheck();
   }

   @Override
   protected void onPause() {
       super.onPause();

       removeRecipientsListeners();
       // OLD: stop getting notified of presence updates to update the titlebar.
       // NEW: we are using ContactHeaderWidget which displays presence, but updating presence
       //      there is out of our control.
       //Contact.stopPresenceObserver();
       if (mDetailDialog != null){
       	mDetailDialog.dismiss();
       }
   }

   @Override
   protected void onStop() {
       super.onStop();
       // Allow any blocked calls to update the thread's read status.
       mConversation.blockMarkAsRead(false);

       if (mMsgListAdapter != null) {
           mMsgListAdapter.changeCursor(null);
       }

       Log.i(WP_TAG, "WPMessageActivity: " + "onStop stopExpiredCheck.");
       siExpiredCheck.stopExpiredCheck();
       
   }

   @Override
   protected void onDestroy() {
       if (TRACE) {
           android.os.Debug.stopMethodTracing();
       }
       mDestroy = true;
       //stop the si expired check thread
       siExpiredCheck.stopSiExpiredCheckThread();
       super.onDestroy();
   }

   @Override
   public void onConfigurationChanged(Configuration newConfig) {
       super.onConfigurationChanged(newConfig);
       if (LOCAL_LOGV) {
           Log.v(WP_TAG, "WPMessageActivity: " + "onConfigurationChanged: " + newConfig);
       }
   }

   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
       switch (keyCode) {
           case KeyEvent.KEYCODE_DEL:
               if ((mMsgListAdapter != null) && mMsgListView.isFocused()) {
                   Cursor cursor;
                   try {
                       cursor = (Cursor) mMsgListView.getSelectedItem();
                   } catch (ClassCastException e) {
                       Log.e(WP_TAG, "WPMessageActivity: " + "Unexpected ClassCastException.", e);
                       return super.onKeyDown(keyCode, event);
                   }

                   if (cursor != null) {
                       boolean locked = cursor.getInt(COLUMN_WPMS_LOCKED) != 0;
                       DeleteMessageListener l = new DeleteMessageListener(
                               cursor.getLong(COLUMN_ID),
                               cursor.getInt(COLUMN_WPMS_TYPE),
                               locked);
                       confirmDeleteDialog(l, locked);
                       return true;
                   }
               }
               break;
           case KeyEvent.KEYCODE_DPAD_CENTER:
               break;
           case KeyEvent.KEYCODE_ENTER:
               break;
           case KeyEvent.KEYCODE_BACK:
        	   //add for multi-delete
        	   if (mMsgListAdapter.mIsDeleteMode) {
        		   changeDeleteMode();
        	   } else {
        		   exitComposeMessageActivity(new Runnable() {
        			   public void run() {
        				   finish();
        			   }
        		   });
        	   }             
        	   return true;
       }

       return super.onKeyDown(keyCode, event);
   }

   private void exitComposeMessageActivity(final Runnable exit) {
       exit.run();
   }

   private void goToConversationList() {
       finish();
       startActivity(new Intent(this, ConversationList.class));
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
	   //add for multi-delete
	   if (mMsgListAdapter.mIsDeleteMode) {
		   return true;
	   }
	   menu.clear();

       // Only add the "View contact" menu item when there's a single recipient and that
       // recipient is someone in contacts.
       ContactList recipients = getRecipients();
       if (recipients.size() == 1 && recipients.get(0).existsInDatabase()) {
           menu.add(0, MENU_VIEW_CONTACT, 0, R.string.menu_view_contact).setIcon(
                   R.drawable.ic_menu_contact);
           //isDiscard = true;
       }

       if (mMsgListAdapter.getCount() > 0) {
           Cursor cursor = mMsgListAdapter.getCursor();
           if ((null != cursor) && (cursor.getCount() > 0)) {
               menu.add(0, MENU_DELETE_THREAD, 0, R.string.delete_message).setIcon(
                   android.R.drawable.ic_menu_delete);
           }
       }
       
       menu.add(0, MENU_CONVERSATION_LIST, 0, R.string.all_threads).setIcon(
               R.drawable.ic_menu_friendslist);

       buildAddAddressToContactMenuItem(menu);
       return true;
   }

   private void buildAddAddressToContactMenuItem(Menu menu) {
       // Look for the first recipient we don't have a contact for and create a menu item to
       // add the number to contacts.
       for (Contact c : getRecipients()) {
           if (!c.existsInDatabase() && canAddToContacts(c)) {
               //Intent intent = ConversationList.createAddContactIntent(c.getNumber());
                Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, c.getNumber());
               menu.add(0, MENU_ADD_ADDRESS_TO_CONTACTS, 0, R.string.menu_add_to_contacts)
                   .setIcon(android.R.drawable.ic_menu_add)
                   .setIntent(intent);
               
               //isDiscard = false;
               break;
           }
       }
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) { 			  			
           case MENU_DELETE_THREAD:
        	   //add for multi-delete
               changeDeleteMode();
               break;
           case MENU_CONVERSATION_LIST:
               exitComposeMessageActivity(new Runnable() {
                   public void run() {
                       goToConversationList();
                   }
               });
               break;
           case MENU_VIEW_CONTACT: {
               // View the contact for the first (and only) recipient.
               ContactList list = getRecipients();
               if (list.size() == 1 && list.get(0).existsInDatabase()) {
                   Uri contactUri = list.get(0).getUri();
                   Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
                   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                   startActivity(intent);
               }
               break;
           }
           case MENU_ADD_ADDRESS_TO_CONTACTS:
               mAddContactIntent = item.getIntent();
	        String number ="";
		number = mAddContactIntent.getStringExtra(ContactsContract.Intents.Insert.PHONE);
		onAddContactButtonClickInt(number, true);
               //startActivityForResult(mAddContactIntent, REQUEST_CODE_ADD_CONTACT);
               break;
       }

       return true;
   }
  
   /**
    * Check for locked messages in all threads or a specified thread.
    * @param handler An AsyncQueryHandler that will receive onQueryComplete
    *                upon completion of looking for locked messages
    * @param threadId   The threadId of the thread to search. -1 means all threads
    * @param token   The token that will be passed to onQueryComplete
    */
   public static void startQueryHaveLockedMessages(AsyncQueryHandler handler, long threadId,
           int token) {
       handler.cancelOperation(token);
       Uri uri = WapPush.CONTENT_URI_THREAD;
       if (threadId != -1) {
           uri = ContentUris.withAppendedId(uri, threadId);
       }       
       handler.startQuery(token, new Long(threadId), uri,
               WP_PROJECTION, "locked=1", null, null);
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (DEBUG) {
           log("onActivityResult: requestCode=" + requestCode
                   + ", resultCode=" + resultCode + ", data=" + data);
       }

       switch(requestCode) {
           case REQUEST_CODE_ADD_CONTACT:
               // The user just added a new contact. We saved the contact info in
               // mAddContactIntent. Get the contact and force our cached contact to
               // get reloaded with the new info (such as contact name). After the
               // contact is reloaded, the function onUpdate() in this file will get called
               // and it will update the title bar, etc.
               if (mAddContactIntent != null) {
                   String address =
                       mAddContactIntent.getStringExtra(ContactsContract.Intents.Insert.EMAIL);
                   if (address == null) {
                       address =
                           mAddContactIntent.getStringExtra(ContactsContract.Intents.Insert.PHONE);
                   }
                   if (address != null) {
                       Contact contact = Contact.get(address, false);
                       if (contact != null) {
                           contact.reload();
                       }
                   }
               }
               break;

           default:
               // TODO
               break;
       }
       
   }

   //==========================================================
   // Private methods
   //==========================================================
   private void confirmDeleteDialog(OnClickListener listener, boolean locked) {
       AlertDialog.Builder builder = new AlertDialog.Builder(this);
       builder.setTitle(locked ? R.string.confirm_dialog_locked_title :
           R.string.confirm_dialog_title);
       builder.setIcon(android.R.drawable.ic_dialog_alert);
       builder.setCancelable(true);
       builder.setMessage(locked ? R.string.confirm_delete_locked_message :
                   R.string.confirm_delete_message);
       builder.setPositiveButton(R.string.delete, listener);
       builder.setNegativeButton(R.string.no, null);
       builder.show();
   }


   private void startMsgListQuery() {
       Uri conversationUri = ContentUris.withAppendedId(WapPush.CONTENT_URI_THREAD, mConversation.getThreadId());

       if (conversationUri == null) {
           return;
       }

       if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
           log("startMsgListQuery for " + conversationUri);
       }

       // Cancel any pending queries
       mBackgroundQueryHandler.cancelOperation(MESSAGE_LIST_QUERY_TOKEN);
       try {
           // Kick off the new query
           mBackgroundQueryHandler.startQuery(
                   MESSAGE_LIST_QUERY_TOKEN, null, conversationUri,
                   WP_PROJECTION, null, null, null);
       } catch (SQLiteException e) {
           SqliteWrapper.checkSQLiteException(this, e);
       }
   }

   private void initMessageList() {
       if (mMsgListAdapter != null) {
           return;
       }

       String highlightString = getIntent().getStringExtra("highlight");
       Pattern highlight = highlightString == null
           ? null
           : Pattern.compile("\\b" + Pattern.quote(highlightString), Pattern.CASE_INSENSITIVE);

       // Initialize the list adapter with a null cursor.
       mMsgListAdapter = new WPMessageListAdapter(this, null, mMsgListView, true, highlight);
       mMsgListAdapter.setOnDataSetChangedListener(mDataSetChangedListener);
       mMsgListAdapter.setMsgListItemHandler(mMessageListItemHandler);
       mMsgListView.setAdapter(mMsgListAdapter);
       mMsgListView.setItemsCanFocus(false);
       mMsgListView.setVisibility(View.VISIBLE);
       mMsgListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	   if (view != null) {
        		   //add for multi-delete
        		   if (mMsgListAdapter.mIsDeleteMode) {
        			   ((WPMessageListItem) view).onMessageListItemClick();
        		   }       			         			   
        	   }
           }
       });
       
       mMsgListAdapter.setItemOnCreateContextMenuListener(mMsgListMenuCreateListener);
   }

   //mainly init the mConversation variable
   private void initActivityState(Bundle bundle, Intent intent) {
       if (bundle != null) {
           String recipients = bundle.getString("recipients");
           mConversation = Conversation.get(this,
                   ContactList.getByNumbers(recipients,
                           false /* don't block */, true /* replace number */), false);
           addRecipientsListeners();
           return;
       }

       // If we have been passed a thread_id, use that to find our
       // conversation.
       long threadId = intent.getLongExtra("thread_id", 0);
       if (threadId > 0) {
           mConversation = Conversation.get(this, threadId, false);
       } else {
           Uri intentData = intent.getData();

           if (intentData != null) {
               // try to get a conversation based on the data URI passed to our intent.
               mConversation = Conversation.get(this, intentData, false);
           } else {
               // special intent extra parameter to specify the address
               String address = intent.getStringExtra("address");
               if (!TextUtils.isEmpty(address)) {
                   mConversation = Conversation.get(this, ContactList.getByNumbers(address,
                           false /* don't block */, true /* replace number */), false);
               } else {
                   mConversation = Conversation.createNew(this);
               }
           }
       }
       addRecipientsListeners();
       mConversation.WPMarkAsRead();
   }
   
   @Override
   public void onUserInteraction() {
	   Log.w(WP_TAG,"onUserInteraction!");
	   checkPendingNotification();
   }

   @Override
   public void onWindowFocusChanged(boolean hasFocus) {
       if (hasFocus) {
    	   Log.w(WP_TAG,"onWindowFocusChanged!");
    	   checkPendingNotification();
       }
   }

   private final WPMessageListAdapter.OnDataSetChangedListener
                   mDataSetChangedListener = new WPMessageListAdapter.OnDataSetChangedListener() {
       public void onDataSetChanged(WPMessageListAdapter adapter) {
    	   mPossiblePendingNotification = true;
       }

       public void onContentChanged(WPMessageListAdapter adapter) {
           startMsgListQuery();
       }
   };
   
   private void checkPendingNotification() {
       if (mPossiblePendingNotification && hasWindowFocus()) {
           mConversation.WPMarkAsRead();
           mPossiblePendingNotification = false;
       }
   }

   private final class BackgroundQueryHandler extends BaseProgressQueryHandler {
       public BackgroundQueryHandler(ContentResolver contentResolver) {
           super(contentResolver);
       }

       @Override
       protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
           switch(token) {
               case MESSAGE_LIST_QUERY_TOKEN:
                   int newSelectionPos = -1;
                   long targetMsgId = getIntent().getLongExtra("select_id", -1);
                   if (targetMsgId != -1) {
                       cursor.moveToPosition(-1);
                       long msgId = 0L;
                       while (cursor.moveToNext()) {
                    	   msgId = cursor.getLong(COLUMN_ID);
                           if (msgId == targetMsgId) {
                               newSelectionPos = cursor.getPosition();
                               break;
                           }
                       }
                   }

                   //add for multi-delete                   
                   mMsgListAdapter.initListMap(cursor);           	
                  
                   mMsgListAdapter.changeCursor(cursor);
                   if (newSelectionPos != -1) {
                       mMsgListView.setSelection(newSelectionPos);
                   }

                   // Once we have completed the query for the message history, if
                   // there is nothing in the cursor and we are not composing a new
                   // message, we must be editing a draft in a new conversation (unless
                   // mSentMessage is true).
                   // Show the recipients editor to give the user a chance to add
                   // more people before the conversation begins.
                   if(cursor.getCount() == 0 && !mDestroy){
                   	mConversation.clearThreadId();
                   	mConversation.ensureThreadId();
                   	goToConversationList();
                   }
                                  
                   mConversation.blockMarkAsRead(false);
                   return;

               case ConversationList.HAVE_LOCKED_MESSAGES_TOKEN:
                   long threadId = (Long)cookie;
                   ConversationList.confirmDeleteThreadDialog(
                           new ConversationList.DeleteThreadListener(threadId,
                               mBackgroundQueryHandler, WPMessageActivity.this),
                           threadId == -1,
                           cursor != null && cursor.getCount() > 0,
                           WPMessageActivity.this);
                   break;
           }
       }

       @Override
       protected void onDeleteComplete(int token, Object cookie, int result) {
           switch(token) {
           case DELETE_MESSAGE_TOKEN:
           case ConversationList.DELETE_CONVERSATION_TOKEN:
               // Update the notification for new messages since they
               // may be deleted.
               WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(
                       WPMessageActivity.this, false);
               if (mMsgListAdapter.mIsDeleteMode) {
               	changeDeleteMode();
               }
               if (progress()) {
                   dismissProgressDialog();
               }
               break;
           }

           // If we're deleting the whole conversation, throw away
           // our current working message and bail.
           if (token == ConversationList.DELETE_CONVERSATION_TOKEN) {
               Conversation.init(WPMessageActivity.this);
               finish();
           }
       }
   }

   public void onUpdate(final Contact updated) {
       // Using an existing handler for the post, rather than conjuring up a new one.
       mMessageListItemHandler.post(new Runnable() {
           public void run() {
               ContactList recipients = getRecipients();
               if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                   log("[CMA] onUpdate contact updated: " + updated);
                   log("[CMA] onUpdate recipients: " + recipients);
               }
               //updateTitle(recipients);
               bindToContactHeaderWidget(mConversation.getRecipients());
               // The contact information for one (or more) of the recipients has changed.
               // Rebuild the message list so each MessageItem will get the last contact info.
               WPMessageActivity.this.mMsgListAdapter.notifyDataSetChanged();
           }
       });
   }

   public static Intent createIntent(Context context, long threadId) {
       Intent intent = new Intent(context, WPMessageActivity.class);

       if (threadId > 0) {
           intent.setData(ContentUris.withAppendedId(WapPush.CONTENT_URI_THREAD, threadId));
       }

       return intent;
  }
   
   private void addRecipientsListeners() {
       Contact.addListener(this);
   }

   private void removeRecipientsListeners() {
       Contact.removeListener(this);
   }
   
 //add for multi-delete
   public void onClick(View v) {
	   if (v == mDeleteButton) { 
		   if (mMsgListAdapter.getSelectedNumber() >= mMsgListAdapter.getCount()) {			  
			   ConversationList.confirmDeleteThreadDialog(
                       new ConversationList.DeleteThreadListener(mConversation.getThreadId(),
                           mBackgroundQueryHandler, WPMessageActivity.this),
                           false, false,  WPMessageActivity.this);	
		   } else {
			   confirmMultiDelete();
		   }			   
	   } else if (v == mCancelButton) {
		   changeDeleteMode();
	   } else if (v == mSelectPanel) {
		   boolean newStatus = !mSelectedAll.isChecked();
		   mSelectedAll.setChecked(newStatus);
		   markCheckedState(newStatus);  		
	   }
   }
   
   private void confirmMultiDelete() {		
	   AlertDialog.Builder builder = new AlertDialog.Builder(this);
       builder.setTitle(R.string.confirm_dialog_title);
       builder.setIcon(android.R.drawable.ic_dialog_alert);
       builder.setCancelable(true);
       builder.setMessage(R.string.confirm_delete_selected_messages);
       builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			    mBackgroundQueryHandler.setProgressDialog(DeleteProgressDialogUtil.getProgressDialog(
			            WPMessageActivity.this));
			    mBackgroundQueryHandler.showProgressDialog();
				new Thread(new Runnable() {
		            public void run() {
		                mBackgroundQueryHandler.setMax(mMsgListAdapter.getSelectedNumber());
		            	Iterator iter = mMsgListAdapter.getItemList().entrySet().iterator();
				    	while (iter.hasNext()) {
				    		@SuppressWarnings("unchecked")
				    		Map.Entry<Long, Boolean> entry = (Entry<Long, Boolean>) iter.next();
				    		if (entry.getValue()) {
				    			Uri deleteUri = ContentUris.withAppendedId(WapPush.CONTENT_URI, entry.getKey());
					    		mBackgroundQueryHandler.startDelete(DELETE_MESSAGE_TOKEN,
					    				null, deleteUri, null, null);
				    		}				    		
				    	}							
		            }
		        }).start();  	
			}
		});
       builder.setNegativeButton(R.string.no, null);
       builder.show();
	}
}

