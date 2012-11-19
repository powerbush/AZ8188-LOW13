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

package com.android.contacts.ui;

import com.android.contacts.ContactsListActivity;
import com.android.contacts.ContactsSearchManager;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
//import com.android.contacts.SIMInfo;
import android.provider.Telephony.SIMInfo;//gemini enhancement
import com.android.contacts.ViewContactActivity;
import com.android.contacts.model.ContactsSource;
import com.android.contacts.model.Editor;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityModifier;
import com.android.contacts.model.EntitySet;
import com.android.contacts.model.GoogleSource;
import com.android.contacts.model.Sources;
import com.android.contacts.model.ContactsSource.EditType;
import com.android.contacts.model.Editor.EditorListener;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.android.contacts.ui.widget.BaseContactEditorView;
import com.android.contacts.ui.widget.ContactEditorView;
import com.android.contacts.ui.widget.PhotoEditorView;
import com.android.contacts.util.EmptyService;
import com.android.contacts.util.WeakAsyncTask;
import com.google.android.collect.Lists;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.ContentProviderOperation.Builder;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.ContactsContract.AggregationExceptions;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts.Data;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import android.provider.Settings;
import com.mediatek.featureoption.FeatureOption;
import com.android.contacts.PhoneOwner;
import com.android.contacts.mtk.*;
import com.android.internal.telephony.ITelephony;
import com.mediatek.CellConnService.CellConnMgr;

/**
 * Activity for editing or inserting a contact.
 */
public final class EditContactActivity extends Activity
        implements View.OnClickListener, Comparator<EntityDelta> {

    private static final String TAG = "EditContactActivity";

    /** The launch code when picking a photo and the raw data is returned */
    private static final int PHOTO_PICKED_WITH_DATA = 3021;

    /** The launch code when a contact to join with is returned */
    private static final int REQUEST_JOIN_CONTACT = 3022;

    /** The launch code when taking a picture */
    private static final int CAMERA_WITH_DATA = 3023;

    private static final String KEY_EDIT_STATE = "state";
    private static final String KEY_RAW_CONTACT_ID_REQUESTING_PHOTO = "photorequester";
    private static final String KEY_VIEW_ID_GENERATOR = "viewidgenerator";
    private static final String KEY_CURRENT_PHOTO_FILE = "currentphotofile";
    private static final String KEY_QUERY_SELECTION = "queryselection";
    private static final String KEY_CONTACT_ID_FOR_JOIN = "contactidforjoin";

    public static final String EXTRA_CONTACT_ID = "contact_id";
    public static final String EXTRA_CONTACT_NAME = "display_name";
    
    /** The result code when view activity should close after edit returns */
    public static final int RESULT_CLOSE_VIEW_ACTIVITY = 777;

    public static final int SAVE_MODE_DEFAULT = 0;
    public static final int SAVE_MODE_SPLIT = 1;
    public static final int SAVE_MODE_JOIN = 2;
    public static final int DELETE_MODE_DEFAULT = 3;

    private long mRawContactIdRequestingPhoto = -1;
    private Bitmap mTemBitmap = null;
    private Bitmap mTemBitmap2 = null;

    private static final int DIALOG_CONFIRM_DELETE = 1;
    private static final int DIALOG_CONFIRM_READONLY_DELETE = 2;
    private static final int DIALOG_CONFIRM_MULTIPLE_DELETE = 3;
    private static final int DIALOG_CONFIRM_READONLY_HIDE = 4;
    private static Dialog mSimSelectionDialog;

    private static final int ICON_SIZE = 250;

    private static final File PHOTO_DIR = new File(
            Environment.getExternalStorageDirectory() + "/DCIM/Camera");

    private File mCurrentPhotoFile;

    String mQuerySelection;

    private long mContactIdForJoin;

    private static final int STATUS_LOADING = 0;
    private static final int STATUS_EDITING = 1;
    private static final int STATUS_SAVING = 2;

	private static final int REQUEST_TYPE = 304;
	private int mSlot;
	private long mSimId;

    private int mStatus;
    private boolean mActivityActive;  // true after onCreate/onResume, false at onPause

    EntitySet mState;
    boolean mSafeForClickIcon = true;

    /** The linear layout holding the ContactEditorViews */
    LinearLayout mContent;

    private ArrayList<Dialog> mManagedDialogs = Lists.newArrayList();

    private ViewIdGenerator mViewIdGenerator;
    
    //Add by MTK80908
    //Used to save the state(edit mode or insert mode). Default INSERT mode;
    private static final int INSERT_MODE = 0;
    private static final int EDIT_MODE = 1;
    private int mMode = INSERT_MODE;
    //MTK80908 end
    
    private static long mContactId = (long)-1; // add by Ivan 2010-09-20

    private boolean mAddNewContact = false;
    public static final String NEW_OWNER_INFO = "new_owner_info";
    boolean mIsNewOwner = false;

    private ContactEditorView mFirstEditor = null;
    private ProgressDialog mLoadingDialog = null;
    private Boolean mLoading = false;
    private static final int DISMISS_LOADING_DIALOG = 1;
    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == DISMISS_LOADING_DIALOG) {
                if (mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                }
            }
        }
        
    };
    
    ContentValues mValuesForCreateContact = null;
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mCellMgr.register(this);
        if (true == FeatureOption.MTK_SNS_SUPPORT) {
            if (null == ContactsManager.SNS_TYPE_LIST)
                ContactsManager.readSnsTypeList(this.getResources());
        }
        final Intent intent = getIntent();
        ContactsUtils.setIntent(intent);//gemini enhancement
        ContactEditorView.setTarget(this);
        final String action = intent.getAction();
        Log.i(TAG,"action is " + action);
        mIsNewOwner = intent.getBooleanExtra(NEW_OWNER_INFO, false);
        Log.i(TAG,"mIsNewOwner is " + mIsNewOwner);

        setContentView(R.layout.act_edit);

        // Build editor and listen for photo requests
        mContent = (LinearLayout) findViewById(R.id.editors);
        
//        Spinner sp1 = (Spinner)findViewById(R.id.sp1);
//        sp1.setBackgroundColor(Color.TRANSPARENT);
        
        findViewById(R.id.btn_done).setOnClickListener(this);
        findViewById(R.id.btn_discard).setOnClickListener(this);
        
        
        // [mtk80909] We tried to let btn_done acquire focus first, and then btn_discard,
        // following QA's suggestion.
        Button buttonDone = (Button)findViewById(R.id.btn_done);
        //buttonDone.setHighFocusPriority(true);
        // Handle initial actions only when existing state missing
        final boolean hasIncomingState = icicle != null && icicle.containsKey(KEY_EDIT_STATE);

        mActivityActive = true;

        if (Intent.ACTION_EDIT.equals(action) && !hasIncomingState) {
            setTitle(R.string.editContact_title_edit);
            mStatus = STATUS_LOADING;
            ContactsUtils.setEditMode(true);
            ContactsUtils.setMoreButtonStatus(false);
            mMode = EDIT_MODE;
            //if(null != sp1) sp1.setVisibility(View.GONE);
            // Read initial state from database
            new QueryEntitiesTask(this).execute(intent);
        } else if (Intent.ACTION_INSERT.equals(action) && !hasIncomingState) {
            setTitle(R.string.editContact_title_insert);
            mStatus = STATUS_EDITING;
            ContactsUtils.setEditMode(false);
            // Trigger dialog to pick account type
            doAddAction();
        }

        if (icicle == null) {
            // If icicle is non-null, onRestoreInstanceState() will restore the generator.
            mViewIdGenerator = new ViewIdGenerator();
        }
        
        if (Intent.ACTION_EDIT.equals(action)) {
        	Log.i(TAG,"ACTION_EDIT again ");
        	ContactsUtils.setEditMode(true);
        	ContactsUtils.setMoreButtonStatus(false);
//        	if(null != sp1) sp1.setVisibility(View.GONE);
            final Sources sources = Sources.getInstance(EditContactActivity.this);
            final ContactsSource source = sources.getInflatedSource("",
                    ContactsSource.LEVEL_CONSTRAINTS);
            final LayoutInflater inflater = (LayoutInflater) getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            mFirstEditor = (ContactEditorView) inflater.inflate(R.layout.item_contact_editor,
                    mContent, false);
            mFirstEditor.inflateChildren(source);
            mMode = EDIT_MODE;
            return;
        }
//        initSpinner();
        
    }

    @Override
    protected void onResume() {
	mSafeForClickIcon = true;
        super.onResume();
        mActivityActive = true;
    }

    @Override
    protected void onPause() {
        super.onResume();
        mActivityActive = false;
        Log.i(TAG,"In onPause ");
        ContactsUtils.dispatchActivityOnPause();
    }

    private static class QueryEntitiesTask extends
            WeakAsyncTask<Intent, Void, EntitySet, EditContactActivity> {

        private String mSelection;
        private WeakReference<ProgressDialog> mProgress;
        public QueryEntitiesTask(EditContactActivity target) {
            super(target);
        }

        @Override
        protected EntitySet doInBackground(EditContactActivity target, Intent... params) {
            final Intent intent = params[0];

            final ContentResolver resolver = target.getContentResolver();

            // Handle both legacy and new authorities
            final Uri data = intent.getData();
            final String authority = data.getAuthority();
            final String mimeType = intent.resolveType(resolver);

            mSelection = "0";
            if (ContactsContract.AUTHORITY.equals(authority)) {
                if (Contacts.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    // Handle selected aggregate
                    final long contactId = ContentUris.parseId(data);
                    if (true == FeatureOption.MTK_SNS_SUPPORT) {
                        mContactId = contactId;
                    }

                    mSelection = RawContacts.CONTACT_ID + "=" + contactId;
                    ContactsUtils.setContactId(contactId);
                } else if (RawContacts.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    final long rawContactId = ContentUris.parseId(data);
                    final long contactId = ContactsUtils.queryForContactId(resolver, rawContactId);
                    ContactsUtils.setRawContactId(rawContactId);
                    if (true == FeatureOption.MTK_SNS_SUPPORT) {
                        mContactId = contactId;
                    }
                    mSelection = RawContacts.CONTACT_ID + "=" + contactId;
                }
            } else if (android.provider.Contacts.AUTHORITY.equals(authority)) {
                final long rawContactId = ContentUris.parseId(data);
                mSelection = Data.RAW_CONTACT_ID + "=" + rawContactId;
                ContactsUtils.setRawContactId(rawContactId);
            }
            

            return EntitySet.fromQuery(target.getContentResolver(), mSelection, null, null);
        }

        @Override
        protected void onPostExecute(EditContactActivity target, EntitySet entitySet) {
            target.mQuerySelection = mSelection;
//            final ProgressDialog progress = mProgress.get();
            // Load edit details in background
            final Context context = target;
            final Sources sources = Sources.getInstance(context);

            // Handle any incoming values that should be inserted
            final Bundle extras = target.getIntent().getExtras();
            final boolean hasExtras = extras != null && extras.size() > 0;
            final boolean hasState = entitySet.size() > 0;
            if (hasExtras && hasState) {
                // Find source defining the first RawContact found
                final EntityDelta state = entitySet.get(0);
                final String accountType = state.getValues().getAsString(RawContacts.ACCOUNT_TYPE);
                final ContactsSource source = sources.getInflatedSource(accountType,
                        ContactsSource.LEVEL_CONSTRAINTS);
                EntityModifier.parseExtras(context, source, state, extras);
            }

            target.mState = entitySet;
            ContactsUtils.setState(target.mState);

            // Bind UI to new background state
            target.bindEditors();
            
//            dismissDialog(progress);
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (hasValidState()) {
            // Store entities with modifications
            outState.putParcelable(KEY_EDIT_STATE, mState);
        }

        outState.putLong(KEY_RAW_CONTACT_ID_REQUESTING_PHOTO, mRawContactIdRequestingPhoto);
        outState.putParcelable(KEY_VIEW_ID_GENERATOR, mViewIdGenerator);
        if (mCurrentPhotoFile != null) {
            outState.putString(KEY_CURRENT_PHOTO_FILE, mCurrentPhotoFile.toString());
        }
        outState.putString(KEY_QUERY_SELECTION, mQuerySelection);
        outState.putLong(KEY_CONTACT_ID_FOR_JOIN, mContactIdForJoin);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Read modifications from instance
        mState = savedInstanceState.<EntitySet> getParcelable(KEY_EDIT_STATE);
        mRawContactIdRequestingPhoto = savedInstanceState.getLong(
                KEY_RAW_CONTACT_ID_REQUESTING_PHOTO);
        mViewIdGenerator = savedInstanceState.getParcelable(KEY_VIEW_ID_GENERATOR);
        String fileName = savedInstanceState.getString(KEY_CURRENT_PHOTO_FILE);
        if (fileName != null) {
            mCurrentPhotoFile = new File(fileName);
        }
        mQuerySelection = savedInstanceState.getString(KEY_QUERY_SELECTION);
        mContactIdForJoin = savedInstanceState.getLong(KEY_CONTACT_ID_FOR_JOIN);

        bindEditors();

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
    	mCellMgr.unregister();
        super.onDestroy();

        if(mTemBitmap2!=null){
            mTemBitmap2.recycle();
            mTemBitmap2 = null;
        }
        if(mTemBitmap!=null){
            mTemBitmap.recycle();
            mTemBitmap = null;
        }

        for (Dialog dialog : mManagedDialogs) {
            dismissDialog(dialog);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
            case DIALOG_CONFIRM_DELETE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.deleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DeleteClickListener())
                        .create();
            case DIALOG_CONFIRM_READONLY_DELETE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.readOnlyContactDeleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DeleteClickListener())
                        .create();
            case DIALOG_CONFIRM_MULTIPLE_DELETE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.multipleContactDeleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DeleteClickListener())
                        .create();
            case DIALOG_CONFIRM_READONLY_HIDE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.readOnlyContactWarning)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DeleteClickListener())
                        .create();
        }
        return null;
    }

    /**
     * Start managing this {@link Dialog} along with the {@link Activity}.
     */
    private void startManagingDialog(Dialog dialog) {
        synchronized (mManagedDialogs) {
            mManagedDialogs.add(dialog);
        }
    }

    /**
     * Show this {@link Dialog} and manage with the {@link Activity}.
     */
    void showAndManageDialog(Dialog dialog) {
        startManagingDialog(dialog);
        dialog.show();
    }

    /**
     * Dismiss the given {@link Dialog}.
     */
    static void dismissDialog(Dialog dialog) {
        try {
            // Only dismiss when valid reference and still showing
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            Log.w(TAG, "Ignoring exception while dismissing dialog: " + e.toString());
        }
    }

    /**
     * Check if our internal {@link #mState} is valid, usually checked before
     * performing user actions.
     */
    protected boolean hasValidState() {
        return mStatus == STATUS_EDITING && mState != null && mState.size() > 0;
    }

    /**
     * Rebuild the editors to match our underlying {@link #mState} object, usually
     * called once we've parsed {@link Entity} data or have inserted a new
     * {@link RawContacts}.
     */
    protected void bindEditors() {
        if (mState == null) {
            return;
        }
        mLoading = true;

        Collections.sort(mState, this);
        final Context ctx = EditContactActivity.this;
        final int size = mState.size();
        if (size > 1) {
            if (mLoadingDialog == null) {
                mLoadingDialog = new ProgressDialog(this);
                mLoadingDialog.setMessage(getString(R.string.load_wait));
                mLoadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    public void onDismiss(DialogInterface dialog) {
                        if (mLoading) {
                            Log.e(TAG, "onDismiss before finishing bind editor ");
                            finish();
                        }
                        
                    }
                    
                });
            }
            mLoadingDialog.show();
        }
        new Thread(new Runnable() {
            public void run() {
        final LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
                final Sources sources = Sources.getInstance(ctx);
                // Remove any existing editors and rebuild any visible

        for (int i = 0; i < size; i++) {
            // TODO ensure proper ordering of entities in the list
                    final EntityDelta entity = mState.get(i);
            final ValuesDelta values = entity.getValues();
                    if (!values.isVisible()) {
                        if (i == size - 1) {
                            mLoading = false;
                            if (mLoadingDialog != null) {
                                mHandler.sendEmptyMessage(DISMISS_LOADING_DIALOG);
                            }
                            mContent.setVisibility(View.VISIBLE);
                            mStatus = STATUS_EDITING;
                        }
                        continue;
                    }

            final String accountType = values.getAsString(RawContacts.ACCOUNT_TYPE);
            final ContactsSource source = sources.getInflatedSource(accountType,
                    ContactsSource.LEVEL_CONSTRAINTS);
            final long rawContactId = values.getAsLong(RawContacts._ID);

                    final BaseContactEditorView editor;
            if (!source.readOnly) {
                        if (i == 0 && mFirstEditor != null) {
                            editor = mFirstEditor;
                        } else {
                editor = (BaseContactEditorView) inflater.inflate(R.layout.item_contact_editor,
                        mContent, false);
                        }
            } else {
                editor = (BaseContactEditorView) inflater.inflate(
                        R.layout.item_read_only_contact_editor, mContent, false);
            }
                    final PhotoEditorView photoEditor = editor.getPhotoEditor();
            photoEditor.setEditorListener(new PhotoListener(rawContactId, source.readOnly,
                    photoEditor));
                    final int index = i;
                    EditContactActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            if (index == 0) {
                                mContent.removeAllViews();
                            }
            mContent.addView(editor);
            editor.setState(entity, source, mViewIdGenerator);
                            //MTK
							if (rawContactId == mRawContactIdRequestingPhoto && mTemBitmap != null) {
                                Log.w(TAG, "Some wrong when binding photo, rebind it.");
                                photoEditor.setPhotoBitmap(mTemBitmap);
                                mRawContactIdRequestingPhoto = -1;
                                mTemBitmap = null;
        }
                            if (index == size - 1) {
                                mLoading = false;
                                if (mLoadingDialog != null) {
                                    mLoadingDialog.dismiss();
                                }
                                mContent.setVisibility(View.VISIBLE);
                                mStatus = STATUS_EDITING;
                            }
                        }
                    });
                    
                }
            }
        }).start();
//        final LayoutInflater inflater = (LayoutInflater) getSystemService(
//                Context.LAYOUT_INFLATER_SERVICE);
//        final Sources sources = Sources.getInstance(this);
//
//        // Sort the editors
//        Collections.sort(mState, this);
//
//        // Remove any existing editors and rebuild any visible
//        
//        for (int i = 0; i < size; i++) {
//            // TODO ensure proper ordering of entities in the list
//            EntityDelta entity = mState.get(i);
//            final ValuesDelta values = entity.getValues();
//            if (!values.isVisible()) continue;
//
//            final String accountType = values.getAsString(RawContacts.ACCOUNT_TYPE);
//            final ContactsSource source = sources.getInflatedSource(accountType,
//                    ContactsSource.LEVEL_CONSTRAINTS);
//            final long rawContactId = values.getAsLong(RawContacts._ID);
//
//            BaseContactEditorView editor;
//            if (!source.readOnly) {
//                if (i == 0 && mFirstEditor != null) {
//                    editor = mFirstEditor;
//                } else {
//                    editor = (BaseContactEditorView) inflater.inflate(R.layout.item_contact_editor,
//                            mContent, false);
//                }
//            } else {
//                editor = (BaseContactEditorView) inflater.inflate(
//                        R.layout.item_read_only_contact_editor, mContent, false);
//            }
//            PhotoEditorView photoEditor = editor.getPhotoEditor();
//            photoEditor.setEditorListener(new PhotoListener(rawContactId, source.readOnly,
//                    photoEditor));
//
//            mContent.addView(editor);
//            editor.setState(entity, source, mViewIdGenerator);
//            if (size > 5) {
//                try {
//                    Thread.sleep(50);
//                } catch (Exception e) {
//                    
//                }
//            }
//            
//        }

        // Show editor now that we've loaded state
//        mContent.setVisibility(View.VISIBLE);
//        mStatus = STATUS_EDITING;
    }


    /**
     * Class that listens to requests coming from photo editors
     */
    private class PhotoListener implements EditorListener, DialogInterface.OnClickListener {
        private long mRawContactId;
        private boolean mReadOnly;
        private PhotoEditorView mEditor;

        public PhotoListener(long rawContactId, boolean readOnly, PhotoEditorView editor) {
            mRawContactId = rawContactId;
            mReadOnly = readOnly;
            mEditor = editor;
        }

        public void onDeleted(Editor editor) {
            // Do nothing
        }

        public void onRequest(int request) {
            if (!hasValidState()) return;

            if (request == EditorListener.REQUEST_PICK_PHOTO) {
                if (mEditor.hasSetPhoto()) {
                    // There is an existing photo, offer to remove, replace, or promoto to primary
                    createPhotoDialog().show();
                } else if (!mReadOnly) {
                    // No photo set and not read-only, try to set the photo
                    doPickPhotoAction(mRawContactId);
                }
            }
        }

        /**
         * Prepare dialog for picking a new {@link EditType} or entering a
         * custom label. This dialog is limited to the valid types as determined
         * by {@link EntityModifier}.
         */
        public Dialog createPhotoDialog() {
            Context context = EditContactActivity.this;

            // Wrap our context to inflate list items using correct theme
            final Context dialogContext = new ContextThemeWrapper(context,
                    android.R.style.Theme_Light);

            String[] choices;
            if (mReadOnly) {
                choices = new String[1];
                choices[0] = getString(R.string.use_photo_as_primary);
            } else {
                choices = new String[3];
                choices[0] = getString(R.string.use_photo_as_primary);
                choices[1] = getString(R.string.removePicture);
                choices[2] = getString(R.string.changePicture);
            }
            final ListAdapter adapter = new ArrayAdapter<String>(dialogContext,
                    android.R.layout.simple_list_item_1, choices);

            final AlertDialog.Builder builder = new AlertDialog.Builder(dialogContext);
            builder.setTitle(R.string.attachToContact);
            builder.setSingleChoiceItems(adapter, -1, this);
            return builder.create();
        }

        /**
         * Called when something in the dialog is clicked
         */
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();

            switch (which) {
                case 0:
                    // Set the photo as super primary
                    mEditor.setSuperPrimary(true);
                    Toast.makeText(EditContactActivity.this, R.string.set_success,
    						Toast.LENGTH_SHORT).show();

                    // And set all other photos as not super primary
                    int count = mContent.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View childView = mContent.getChildAt(i);
                        if (childView instanceof BaseContactEditorView) {
                            BaseContactEditorView editor = (BaseContactEditorView) childView;
                            PhotoEditorView photoEditor = editor.getPhotoEditor();
                            if (!photoEditor.equals(mEditor)) {
                                photoEditor.setSuperPrimary(false);
                            }
                        }
                    }
                    break;

                case 1:
                    // Remove the photo
                    mEditor.setPhotoBitmap(null);
                    Toast.makeText(EditContactActivity.this, R.string.remove_success,
    						Toast.LENGTH_SHORT).show();
                    break;

                case 2:
                    // Pick a new photo for the contact
                    doPickPhotoAction(mRawContactId);
                    break;
            }
        }
    }

    /** {@inheritDoc} */
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_done:
                doSaveAction(SAVE_MODE_DEFAULT);
                break;
            case R.id.btn_discard:
                doRevertAction();
                break;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onBackPressed() {
        if (mLoading) {
            mHandler.sendEmptyMessage(DISMISS_LOADING_DIALOG);
        }
        doSaveAction(SAVE_MODE_DEFAULT);

        
    }

    /** {@inheritDoc} */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if (requestCode == REQUEST_JOIN_CONTACT && mContactIdForJoin <= 0) {
    		finish();
        	return;
    	}
    	
    	// mtk80909 start (for unsuccessful joining operation)
        if (resultCode != RESULT_OK && requestCode == REQUEST_JOIN_CONTACT) {
            Intent intent = getIntent();
            intent.replaceExtras(new Bundle());
            setIntent(intent);
        	joinAggregate(-1); // < 0 means no contact should be joined
        }
        // mtk80909 end
    	
        // Ignore failed requests
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case PHOTO_PICKED_WITH_DATA: {
                BaseContactEditorView requestingEditor = null;
                for (int i = 0; i < mContent.getChildCount(); i++) {
                    View childView = mContent.getChildAt(i);
                    if (childView instanceof BaseContactEditorView) {
                        BaseContactEditorView editor = (BaseContactEditorView) childView;
                        if (editor.getRawContactId() == mRawContactIdRequestingPhoto) {
                            requestingEditor = editor;
                            break;
                        }
                    }
                }

                if (requestingEditor != null) {
                    
                    if(mTemBitmap2!=null){
                        mTemBitmap2.recycle();
                        mTemBitmap2 = null;
                    }
       
                     mTemBitmap2 = data.getParcelableExtra("data");
                    requestingEditor.setPhotoBitmap(mTemBitmap2);
                    //photo.recycle();
                    mRawContactIdRequestingPhoto = -1;
                } else {
                    if(mTemBitmap!=null){
                        mTemBitmap.recycle();
                        mTemBitmap = null;
                    }
                	mTemBitmap = data.getParcelableExtra("data");
                    Log.w(TAG, "The contact that requested the photo is no longer present.");
                    // The contact that requested the photo is no longer present.
                    // TODO: Show error message
                }

                break;
            }

            case CAMERA_WITH_DATA: {
                doCropPhoto(mCurrentPhotoFile);
                break;
            }

            case REQUEST_JOIN_CONTACT: {
                if (resultCode == RESULT_OK && data != null) {
                    final long contactId = ContentUris.parseId(data.getData());
                    joinAggregate(contactId);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit, menu);
        if(FeatureOption.MTK_DIALER_SEARCH_SUPPORT)
        	menu.removeItem(R.id.menu_add);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mLoading) {
            Log.i(TAG, "binding editor");
            return true;
        }
        if(null != menu.findItem(R.id.menu_split)) 
        menu.findItem(R.id.menu_split).setVisible(mState != null && mState.size() > 1);
	if(true == FeatureOption.MTK_SNS_SUPPORT){
		if(PhoneOwner.getInstance() != null && PhoneOwner.getInstance().getOwnerID() == mContactId){
			 menu.removeItem(R.id.menu_split);
			 menu.removeItem(R.id.menu_join);
		}
	}
		//add by MTK80908
		if(FeatureOption.MTK_DIALER_SEARCH_SUPPORT && mMode == INSERT_MODE){
			menu.removeItem(R.id.menu_join);
		}
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_done:
                return doSaveAction(SAVE_MODE_DEFAULT);
            case R.id.menu_discard:
                return doRevertAction();
            case R.id.menu_add:
                mAddNewContact = true;
                return doAddAction();
            case R.id.menu_delete:
                return doDeleteAction();
            case R.id.menu_split:
                return doSplitContactAction();
            case R.id.menu_join:
                return doJoinContactAction();
        }
        return false;
    }

    /**
     * Background task for persisting edited contact data, using the changes
     * defined by a set of {@link EntityDelta}. This task starts
     * {@link EmptyService} to make sure the background thread can finish
     * persisting in cases where the system wants to reclaim our process.
     */
    public static class PersistTask extends
            WeakAsyncTask<EntitySet, Void, Integer, EditContactActivity> {
        private static final int PERSIST_TRIES = 3;

        private static final int RESULT_UNCHANGED = 0;
        private static final int RESULT_SUCCESS = 1;
        private static final int RESULT_FAILURE = 2;

        private WeakReference<ProgressDialog> mProgress;

        private int mSaveMode;
        private Uri mContactLookupUri = null;

        public PersistTask(EditContactActivity target, int saveMode) {
            super(target);
            mSaveMode = saveMode;
        }

        /** {@inheritDoc} */
        @Override
        protected void onPreExecute(EditContactActivity target) {
            if (mSaveMode != DELETE_MODE_DEFAULT) {
            mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(target, null,
                    target.getText(R.string.savingContact)));
            }

            // Before starting this task, start an empty service to protect our
            // process from being reclaimed by the system.
            final Context context = target;
            context.startService(new Intent(context, EmptyService.class));
        }

        /** {@inheritDoc} */
        @Override
        protected Integer doInBackground(EditContactActivity target, EntitySet... params) {
            final Context context = target;
            final ContentResolver resolver = context.getContentResolver();

            EntitySet state = params[0];

            // Trim any empty fields, and RawContacts, before persisting
            final Sources sources = Sources.getInstance(context);
            EntityModifier.trimEmpty(state, sources);

            // Attempt to persist changes
            int tries = 0;
            Integer result = RESULT_FAILURE;
            while (tries++ < PERSIST_TRIES) {
                try {
                    // Build operations and try applying
                    final ArrayList<ContentProviderOperation> diff = state.buildDiff();
                    ContentProviderResult[] results = null;
                    if (!diff.isEmpty()) {
                         results = resolver.applyBatch(ContactsContract.AUTHORITY, diff);
                    }

                    final long rawContactId = getRawContactId(state, diff, results);
                    if (rawContactId != -1) {
                        final Uri rawContactUri = ContentUris.withAppendedId(
                                RawContacts.CONTENT_URI, rawContactId);

                        // convert the raw contact URI to a contact URI
                        mContactLookupUri = RawContacts.getContactLookupUri(resolver,
                                rawContactUri);
                    }
                    result = (diff.size() > 0) ? RESULT_SUCCESS : RESULT_UNCHANGED;
                    break;

                } catch (RemoteException e) {
                    // Something went wrong, bail without success
                    Log.e(TAG, "Problem persisting user edits", e);
                    break;
                }
                catch(ArrayIndexOutOfBoundsException ex)
                {
                    Log.e(TAG,"array index out of bounds... ", ex);
                    break;
                } catch (OperationApplicationException e) {
                    // Version consistency failed, re-parent change and try again
                    Log.w(TAG, "Version consistency failed, re-parenting: " + e.toString());
                    final EntitySet newState = EntitySet.fromQuery(resolver,
                            target.mQuerySelection, null, null);
                    state = EntitySet.mergeAfter(newState, state);
                }
            }

            return result;
        }

        private long getRawContactId(EntitySet state,
                final ArrayList<ContentProviderOperation> diff,
                final ContentProviderResult[] results) {
            long rawContactId = state.findRawContactId();
            if (rawContactId != -1) {
                return rawContactId;
            }

            // we gotta do some searching for the id
            final int diffSize = diff.size();
            for (int i = 0; i < diffSize; i++) {
                ContentProviderOperation operation = diff.get(i);
                if (operation.getType() == ContentProviderOperation.TYPE_INSERT
                        && operation.getUri().getEncodedPath().contains(
                                RawContacts.CONTENT_URI.getEncodedPath())) {
                    return ContentUris.parseId(results[i].uri);
                }
            }
            return -1;
        }

        /** {@inheritDoc} */
        @Override
        protected void onPostExecute(EditContactActivity target, Integer result) {
            final Context context = target;


            if (result == RESULT_SUCCESS && mSaveMode != SAVE_MODE_JOIN && mSaveMode != DELETE_MODE_DEFAULT) {
                Toast.makeText(context, R.string.contactSavedToast, Toast.LENGTH_SHORT).show();
            } else if (result == RESULT_FAILURE) {
                Toast.makeText(context, R.string.contactSavedErrorToast, Toast.LENGTH_LONG).show();
            }
            if (mSaveMode != DELETE_MODE_DEFAULT) {
                final ProgressDialog progress = mProgress.get();
                dismissDialog(progress);
            }


            // Stop the service that was protecting us
            context.stopService(new Intent(context, EmptyService.class));

            target.onSaveCompleted(result != RESULT_FAILURE, mSaveMode, mContactLookupUri);
        }
    }

    /**
     * Saves or creates the contact based on the mode, and if successful
     * finishes the activity.
     */
    boolean doSaveAction(int saveMode) {
        if (!hasValidState()) {
            return false;
        }

        mStatus = STATUS_SAVING;
        final PersistTask task = new PersistTask(this, saveMode);
        task.execute(mState);

        return true;
    }

    private class DeleteClickListener implements DialogInterface.OnClickListener {

        public void onClick(DialogInterface dialog, int which) {
            Sources sources = Sources.getInstance(EditContactActivity.this);
            // Mark all raw contacts for deletion
            for (EntityDelta delta : mState) {
                delta.markDeleted();
            }
            // Save the deletes
            doSaveAction(DELETE_MODE_DEFAULT);
            finish();
        }
    }

    private void onSaveCompleted(boolean success, int saveMode, Uri contactLookupUri) {
        switch (saveMode) {
            case SAVE_MODE_DEFAULT:
            case DELETE_MODE_DEFAULT:
                if (success && contactLookupUri != null) {
                    final Intent resultIntent = new Intent();

                    final Uri requestData = getIntent().getData();
                    final String requestAuthority = requestData == null ? null : requestData
                            .getAuthority();

                    if (android.provider.Contacts.AUTHORITY.equals(requestAuthority)) {
                        // Build legacy Uri when requested by caller
                        final long contactId = ContentUris.parseId(Contacts.lookupContact(
                                getContentResolver(), contactLookupUri));
                        final Uri legacyUri = ContentUris.withAppendedId(
                                android.provider.Contacts.People.CONTENT_URI, contactId);
                        resultIntent.setData(legacyUri);
                    } else {
                        // Otherwise pass back a lookup-style Uri
			String contactName = null;
			Cursor cursor = getContentResolver().query(contactLookupUri, new String[] {Contacts.DISPLAY_NAME}, 
				null, null, null);
			try {
			    if (cursor != null && cursor.moveToFirst()) {
				contactName = cursor.getString(0);
			    }
			} finally {
			    if (cursor != null) {
				cursor.close();
			    }
			} 
			resultIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, contactName);
                        final long contactId = ContentUris.parseId(Contacts.lookupContact(
                                getContentResolver(), contactLookupUri));
			resultIntent.putExtra(EditContactActivity.EXTRA_CONTACT_ID, contactId);
                        resultIntent.setData(contactLookupUri);
                    }

                    setResult(RESULT_OK, resultIntent);
                } else {
                    setResult(RESULT_CANCELED, null);
                }
                finish();
                break;

            case SAVE_MODE_SPLIT:
                if (success) {
                    Intent intent = new Intent();
                    intent.setData(contactLookupUri);
                    setResult(RESULT_CLOSE_VIEW_ACTIVITY, intent);
                }
                finish();
                break;

            case SAVE_MODE_JOIN:
                mStatus = STATUS_EDITING;
                if (success) {
                    showJoinAggregateActivity(contactLookupUri);
                }
                break;
        }
    }

    /**
     * Shows a list of aggregates that can be joined into the currently viewed aggregate.
     *
     * @param contactLookupUri the fresh URI for the currently edited contact (after saving it)
     */
    public void showJoinAggregateActivity(Uri contactLookupUri) {
        if (contactLookupUri == null) {
            Toast.makeText(this, R.string.join_null_contact, Toast.LENGTH_SHORT).show();
            //MTK since the mState was changed after trim, rebuild it.
            //mState = null;
            //doAddAction();
            return;
        }

        mContactIdForJoin = ContentUris.parseId(contactLookupUri);
        Intent intent = new Intent(ContactsListActivity.JOIN_AGGREGATE);
        intent.putExtra(ContactsListActivity.EXTRA_AGGREGATE_ID, mContactIdForJoin);
        startActivityForResult(intent, REQUEST_JOIN_CONTACT);
    }

    private interface JoinContactQuery {
        String[] PROJECTION = {
                RawContacts._ID,
                RawContacts.CONTACT_ID,
                RawContacts.NAME_VERIFIED,
        };

        String SELECTION = RawContacts.CONTACT_ID + "=? OR " + RawContacts.CONTACT_ID + "=?";

        int _ID = 0;
        int CONTACT_ID = 1;
        int NAME_VERIFIED = 2;
    }

    /**
     * Performs aggregation with the contact selected by the user from suggestions or A-Z list.
     */
    private void joinAggregate(final long contactId) {
        ContentResolver resolver = getContentResolver();

        // Load raw contact IDs for all raw contacts involved - currently edited and selected
        // in the join UIs
        Cursor c = resolver.query(RawContacts.CONTENT_URI,
                JoinContactQuery.PROJECTION,
                JoinContactQuery.SELECTION,
                new String[]{String.valueOf(contactId), String.valueOf(mContactIdForJoin)}, null);

        long rawContactIds[];
        long verifiedNameRawContactId = -1;
        try {
            rawContactIds = new long[c.getCount()];
            for (int i = 0; i < rawContactIds.length; i++) {
                c.moveToNext();
                long rawContactId = c.getLong(JoinContactQuery._ID);
                rawContactIds[i] = rawContactId;
                if (c.getLong(JoinContactQuery.CONTACT_ID) == mContactIdForJoin) {
                    if (verifiedNameRawContactId == -1
                            || c.getInt(JoinContactQuery.NAME_VERIFIED) != 0) {
                        verifiedNameRawContactId = rawContactId;
                    }
                }
            }
        } finally {
            c.close();
        }

        // For each pair of raw contacts, insert an aggregation exception
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        for (int i = 0; i < rawContactIds.length; i++) {
            for (int j = 0; j < rawContactIds.length; j++) {
                if (i != j) {
                    buildJoinContactDiff(operations, rawContactIds[i], rawContactIds[j]);
                }
            }
        }

        // Mark the original contact as "name verified" to make sure that the contact
        // display name does not change as a result of the join
        Builder builder = ContentProviderOperation.newUpdate(
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, verifiedNameRawContactId));
        builder.withValue(RawContacts.NAME_VERIFIED, 1);
        operations.add(builder.build());

        // Apply all aggregation exceptions as one batch
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);

            // We can use any of the constituent raw contacts to refresh the UI - why not the first
            Intent intent = new Intent();
            intent.setData(ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactIds[0]));

            // Reload the new state from database
            new QueryEntitiesTask(this).execute(intent);

            // mtk80909 add a condition
            if (contactId >= 0) Toast.makeText(this, R.string.contactsJoinedMessage, Toast.LENGTH_LONG).show();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to apply aggregation exception batch", e);
            Toast.makeText(this, R.string.contactSavedErrorToast, Toast.LENGTH_LONG).show();
        } catch (OperationApplicationException e) {
            Log.e(TAG, "Failed to apply aggregation exception batch", e);
            Toast.makeText(this, R.string.contactSavedErrorToast, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Construct a {@link AggregationExceptions#TYPE_KEEP_TOGETHER} ContentProviderOperation.
     */
    private void buildJoinContactDiff(ArrayList<ContentProviderOperation> operations,
            long rawContactId1, long rawContactId2) {
        Builder builder =
                ContentProviderOperation.newUpdate(AggregationExceptions.CONTENT_URI);
        builder.withValue(AggregationExceptions.TYPE, AggregationExceptions.TYPE_KEEP_TOGETHER);
        builder.withValue(AggregationExceptions.RAW_CONTACT_ID1, rawContactId1);
        builder.withValue(AggregationExceptions.RAW_CONTACT_ID2, rawContactId2);
        operations.add(builder.build());
    }

    /**
     * Revert any changes the user has made, and finish the activity.
     */
    private boolean doRevertAction() {
        finish();
        return true;
    }

    /**
     * Create a new {@link RawContacts} which will exist as another
     * {@link EntityDelta} under the currently edited {@link Contacts}.
     */
    private boolean doAddAction() {
        if (mStatus != STATUS_EDITING) {
            if (mAddNewContact) {
                mAddNewContact = false;
            }
            return false;
        }

        // Adding is okay when missing state
        new AddContactTask(this).execute();
        return true;
    }

    /**
     * Delete the entire contact currently being edited, which usually asks for
     * user confirmation before continuing.
     */
    private boolean doDeleteAction() {
        if (!hasValidState())
            return false;
        int readOnlySourcesCnt = 0;
        int writableSourcesCnt = 0;
        Sources sources = Sources.getInstance(EditContactActivity.this);
        for (EntityDelta delta : mState) {
            final String accountType = delta.getValues().getAsString(RawContacts.ACCOUNT_TYPE);
            final ContactsSource contactsSource = sources.getInflatedSource(accountType,
                    ContactsSource.LEVEL_CONSTRAINTS);
            if (contactsSource != null && contactsSource.readOnly) {
                readOnlySourcesCnt += 1;
            } else {
                writableSourcesCnt += 1;
            }
        }

        if (readOnlySourcesCnt > 0 && writableSourcesCnt > 0) {
            showDialog(DIALOG_CONFIRM_READONLY_DELETE);
        } else if (readOnlySourcesCnt > 0 && writableSourcesCnt == 0) {
            showDialog(DIALOG_CONFIRM_READONLY_HIDE);
        } else if (readOnlySourcesCnt == 0 && writableSourcesCnt > 1) {
            showDialog(DIALOG_CONFIRM_MULTIPLE_DELETE);
        } else {
            showDialog(DIALOG_CONFIRM_DELETE);
        }
        return true;
    }

    /**
     * Pick a specific photo to be added under the currently selected tab.
     */
    boolean doPickPhotoAction(long rawContactId) {
        if (!hasValidState()) return false;

        if (!EditContactActivity.this.mSafeForClickIcon) {
	    return false;
	}

	EditContactActivity.this.mSafeForClickIcon = false;
        mRawContactIdRequestingPhoto = rawContactId;

        showAndManageDialog(createPickPhotoDialog());

        return true;
    }

    /**
     * Creates a dialog offering two options: take a photo or pick a photo from the gallery.
     */
    private Dialog createPickPhotoDialog() {
        Context context = EditContactActivity.this;

        // Wrap our context to inflate list items using correct theme
        final Context dialogContext = new ContextThemeWrapper(context,
                android.R.style.Theme_Light);

        String[] choices;
        choices = new String[2];
        choices[0] = getString(R.string.take_photo);
        choices[1] = getString(R.string.pick_photo);
        final ListAdapter adapter = new ArrayAdapter<String>(dialogContext,
                android.R.layout.simple_list_item_1, choices);

        final DialogInterface.OnCancelListener cancelListener =
            new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                EditContactActivity.this.mSafeForClickIcon = true;
            }
        };
        final AlertDialog.Builder builder = new AlertDialog.Builder(dialogContext);
        builder.setTitle(R.string.attachToContact);
        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch(which) {
                    case 0:
                        doTakePhoto();
                        break;
                    case 1:
                        doPickPhotoFromGallery();
                        break;
                }
            }
        });
        builder.setOnCancelListener(cancelListener);
        return builder.create();
    }

    /**
     * Create a file name for the icon photo using current time.
     */
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    /**
     * Launches Camera to take a picture and store it in a file.
     */
    protected void doTakePhoto() {
        try {
            // Launch camera to take photo for selected contact
            PHOTO_DIR.mkdirs();
            mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());
            final Intent intent = getTakePickIntent(mCurrentPhotoFile);
            startActivityForResult(intent, CAMERA_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Constructs an intent for capturing a photo and storing it in a temporary file.
     */
    public static Intent getTakePickIntent(File f) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }

    /**
     * Sends a newly acquired photo to Gallery for cropping
     */
    protected void doCropPhoto(File f) {
        try {

            // Add the image to the media store
            MediaScannerConnection.scanFile(
                    this,
                    new String[] { f.getAbsolutePath() },
                    new String[] { null },
                    null);

            // Launch gallery to crop the photo
            final Intent intent = getCropImageIntent(Uri.fromFile(f));
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (Exception e) {
            Log.e(TAG, "Cannot crop image", e);
            Toast.makeText(this, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Constructs an intent for image cropping.
     */
    public static Intent getCropImageIntent(Uri photoUri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", ICON_SIZE);
        intent.putExtra("outputY", ICON_SIZE);
        intent.putExtra("return-data", true);
        return intent;
    }

    /**
     * Launches Gallery to pick a photo.
     */
    protected void doPickPhotoFromGallery() {
        try {
            // Launch picker to choose photo for selected contact
            final Intent intent = getPhotoPickIntent();
            startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Constructs an intent for picking a photo from Gallery, cropping it and returning the bitmap.
     */
    public static Intent getPhotoPickIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", ICON_SIZE);
        intent.putExtra("outputY", ICON_SIZE);
        intent.putExtra("return-data", true);
        return intent;
    }

    /** {@inheritDoc} */
    public void onDeleted(Editor editor) {
        // Ignore any editor deletes
    }

    private boolean doSplitContactAction() {
        if (!hasValidState()) return false;

        showAndManageDialog(createSplitDialog());
        return true;
    }

    private Dialog createSplitDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.splitConfirmation_title);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setMessage(R.string.splitConfirmation);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Split the contacts
                mState.splitRawContacts();
                doSaveAction(SAVE_MODE_SPLIT);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setCancelable(false);
        return builder.create();
    }

    private boolean doJoinContactAction() {
        return doSaveAction(SAVE_MODE_JOIN);
    }

    /**
     * Build dialog that handles adding a new {@link RawContacts} after the user
     * picks a specific {@link ContactsSource}.
     */
    private static class AddContactTask extends
            WeakAsyncTask<Void, Void, ArrayList<Account>, EditContactActivity> {

        public AddContactTask(EditContactActivity target) {
            super(target);
        }

        @Override
        protected ArrayList<Account> doInBackground(final EditContactActivity target,
                Void... params) {
            return Sources.getInstance(target).getAccounts(true);
        }

        @Override
        protected void onPostExecute(final EditContactActivity target, ArrayList<Account> accounts) {
            if (!target.mActivityActive) {
                // A monkey or very fast user.
                return;
            }
            target.selectAccountAndCreateContact(accounts);
            if (target.mAddNewContact) {
                target.mAddNewContact = false;
            }
        }
    }

    public void selectAccountAndCreateContact(ArrayList<Account> accounts) {
        // No Accounts available.  Create a phone-local contact.
        
        final Context context = EditContactActivity.this;
        //Add by Huibin by 2010-08-31
        if(true == FeatureOption.MTK_SNS_SUPPORT)
        {
			if(null == accounts) return;
			Account account = null;
			for(int i = 0; i < accounts.size(); i++){
				account = (Account) accounts.get(i);
				if(ContactsManager.SNS_TYPE_LIST.contains(account.type.toLowerCase())){
					accounts.remove(i);
				}
			}
			
	    }
        if (mIsNewOwner) {
        	createContact(null, null);
			ContactsUtils.setSpinnerName(context.getString(R.string.phone));
			ContactsUtils.setEditMode(true);
			return;
			}
		final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));

		if (iTel == null) {
			Log.i(TAG,"selectAccountAndCreateContact iTel == null");
			return;
		}
		if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
		try {
				Log.i(TAG,"iTel.isSimInsert(com.android.internal.telephony.Phone.GEMINI_SIM_1) is " + iTel.isSimInsert(com.android.internal.telephony.Phone.GEMINI_SIM_1));
				Log.i(TAG,"iTel.isSimInsert(com.android.internal.telephony.Phone.GEMINI_SIM_2) is " + iTel.isSimInsert(com.android.internal.telephony.Phone.GEMINI_SIM_2));
		if (accounts.isEmpty() && !iTel.isSimInsert(com.android.internal.telephony.Phone.GEMINI_SIM_1)
				&& !iTel.isSimInsert(com.android.internal.telephony.Phone.GEMINI_SIM_2)) {
          createContact(null, null);//gemini enhancement
          return;  // Don't show a dialog.
        }
		} catch (RemoteException e) {
			Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
		}         
      

	       mSimSelectionDialog = ContactsUtils.createSimSelectionDialog(context, 
                   context.getResources().getString(R.string.store_location_title), 
                   Settings.System.DEFAULT_SIM_NOT_SET, 
                   ContactsUtils.createItemHolder(context, context.getString(R.string.phone), false, accounts), 
                   new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
   				// TODO Auto-generated method stub
   					Log.i(TAG,"IN onClick ");
                dialog.dismiss();
   					AlertDialog alertDialog = (AlertDialog) dialog;
                    Object object = (alertDialog.getListView().getAdapter().getItem(which));
                    if (object instanceof String) {
                    	Log.i(TAG,"object instanceof String ");
                    	createContact(null, null);
            			ContactsUtils.setSpinnerName(context.getString(R.string.phone));
            			return;
                    } else if(object instanceof Account) {
                    	Log.i(TAG,"object instanceof Account ");
                    	Log.i(TAG,"((Account) object).name is " + ((Account) object).name);
                    	Log.i(TAG,"((Account) object).type is " + ((Account) object).type);
                    	createContact(((Account) object).name, ((Account) object).type);
	      				ContactsUtils.setSpinnerName(((Account) object).name);
                    } else if(object instanceof Integer) {
                    	mSlot = ((Integer)alertDialog.getListView().getAdapter().getItem(which)).intValue();
                        int nRet = mCellMgr.handleCellConn(mSlot, REQUEST_TYPE);
                        Log.d(TAG, "Phone -> SIM handleCellConn result value = " + CellConnMgr.resultToString(nRet));
                        Log.i(TAG,"mSimSelectionDialog mSlot IS " + mSlot);
		            	  }
		              }              

   				});
           mSimSelectionDialog.show();
        final DialogInterface.OnCancelListener cancelListener =
            new DialogInterface.OnCancelListener() {
        public void onCancel(DialogInterface dialog) {
            // If nothing remains, close activity
                finish();
        }
    };
           mSimSelectionDialog.setOnCancelListener(cancelListener);
		
		} else {
			try {
				Log.i(TAG,"iTel.hasIccCard() is " + iTel.hasIccCard());
				if (accounts.isEmpty() && !iTel.hasIccCard()) {
		          createContact(null, null);//gemini enhancement
		          return;  // Don't show a dialog.
		        }
				} catch (RemoteException e) {
					Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
				}   							
		    	final String[] accountName = new String[10];
		    	final String[] accountType = new String[10];
		        final Sources sources = Sources.getInstance(context);
		  	final Context dialogContext = new ContextThemeWrapper(this,
						android.R.style.Theme_Light);
				final Resources res = dialogContext.getResources();
				final LayoutInflater dialogInflater = (LayoutInflater) dialogContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//
		//
//				// Adapter that shows a list of string resources
				final ArrayAdapter<CharSequence> accountAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_list_item_1)
						{
		            @Override
		            public View getView(int position, View convertView, ViewGroup parent) {
						Log.i(TAG,"position is " + position);
		                if (convertView == null) {
							convertView = dialogInflater.inflate(
									android.R.layout.simple_list_item_1, parent, false);
		                }
						final CharSequence resId = this.getItem(position);
		                ((TextView)convertView).setText(resId);
		                return convertView;
		            }
		        };
				accountAdapter.add(context.getString(R.string.phone)/*R.string.usim1*/);
				Log.i(TAG,"context.getString(R.string.phone) is " +context.getString(R.string.phone));	

		//  
				if (accounts != null) {
					if (!accounts.isEmpty()) {
					  	Log.i(TAG,"accounts.size() is " + accounts.size());
						for (int j=0; j<accounts.size(); j++) {			
							accountName[j] = accounts.get(j).name;		
							accountType[j] = accounts.get(j).type;			
							Log.i(TAG,"accountName( " + j + "is " + accountName[j]);
							accountAdapter.add(accountName[j]);
						}
					}
				}
				try {
					Log.i(TAG,"iTel.hasIccCard() is " + iTel.hasIccCard());
					if (iTel.hasIccCard()) {
						accountAdapter.add(EditContactActivity.this.getString(R.string.sim));
			        }
					} catch (RemoteException e) {
						Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
					}
				
		       DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		                dialog.dismiss();
		      		Log.i(TAG,"which is " + which);
		      		CharSequence resId = accountAdapter.getItem(which);      		
		      		Intent intent = new Intent(EditContactActivity.this, EditSimContactActivity.class);
		      		Bundle extras = getIntent().getExtras();
		              if (extras != null && extras.size() > 0) {
		                  intent.putExtras(extras);
		              }
		              intent.putExtra("action", Intent.ACTION_INSERT);
		              if (resId.equals(context.getString(R.string.phone))) {
		        			createContact(null, null);
		        			ContactsUtils.setSpinnerName((String)resId);
		        			return;
		        		} else {           
				      		  for (int i=0; i<accountAdapter.getCount(); i++) {
				      			  if (resId.equals(accountName[i])) {
				      				createContact((String)resId, accountType[i]);
				      				ContactsUtils.setSpinnerName(accountName[i]);
				      				Log.i(TAG,"resId is " + resId);
				      				Log.i(TAG,"accountType" + i + "is " + accountType[i]);
				      				return;
				      			  }
				      		  }
                                    mSlot = com.android.internal.telephony.Phone.GEMINI_SIM_1;
	                                int nRet = mCellMgr.handleCellConn(mSlot, REQUEST_TYPE);
	                                Log.d(TAG, "Single version Phone -> SIM handleCellConn result value = " + CellConnMgr.resultToString(nRet));
		        		}
		      		}
		        };
		        
		        final DialogInterface.OnCancelListener cancelListener =
		            new DialogInterface.OnCancelListener() {
		        public void onCancel(DialogInterface dialog) {
		            // If nothing remains, close activity
		                finish();
		        }
		    };
		        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		      	builder.setTitle(R.string.store_location_title);
				builder.setIcon(android.R.drawable.ic_menu_more);
				builder.setAdapter(accountAdapter, clickListener);
				builder.setOnCancelListener(cancelListener);
		        showAndManageDialog(builder.create());
				
			
		}

    }
    
	private Runnable serviceComplete = new Runnable() {
		public void run() {
			Log.d(TAG, "serviceComplete run");			
			int nRet = mCellMgr.getResult();
			Log.d(TAG, "serviceComplete result = " + CellConnMgr.resultToString(nRet));
			if (mCellMgr.RESULT_ABORT == nRet) {
				finish();
				return;
			} else {
				SIMInfo siminfo = SIMInfo.getSIMInfoBySlot(EditContactActivity.this, mSlot); 
                if (siminfo != null) {
                	mSimId = siminfo.mSimId;
                }
                Log.i(TAG,"mSimSelectionDialog mSimId is " + mSimId);
		Intent intent = new Intent(EditContactActivity.this, EditSimContactActivity.class);
		Bundle extras = getIntent().getExtras();
      if (extras != null && extras.size() > 0) {
          intent.putExtras(extras);
      }
      intent.putExtra("action", Intent.ACTION_INSERT);
        	      			intent.putExtra(RawContacts.INDICATE_PHONE_SIM, mSimId);
        	      			intent.putExtra("slotId", mSlot);
            	                startActivity(intent);
            	                finish();
			}
		}
	};

    /**
     * @param account may be null to signal a device-local contact should
     *     be created.
     */
    private void createContact(String account, String type) {//gemini enhancement
        final Sources sources = Sources.getInstance(this);
        	mValuesForCreateContact = new ContentValues();
        
        if (account != null) {
        	mValuesForCreateContact.put(RawContacts.ACCOUNT_NAME, /*account.name*/account);
        	mValuesForCreateContact.put(RawContacts.ACCOUNT_TYPE, type);
        } else {
        	mValuesForCreateContact.putNull(RawContacts.ACCOUNT_NAME);
        	mValuesForCreateContact.putNull(RawContacts.ACCOUNT_TYPE);
        }
        

        // Parse any values from incoming intent
        EntityDelta insert = new EntityDelta(ValuesDelta.fromAfter(mValuesForCreateContact));
        Log.i(TAG,"insert is " + insert);
        final ContactsSource source = sources.getInflatedSource(account != null ? account : null,
            ContactsSource.LEVEL_CONSTRAINTS);
        final Bundle extras = getIntent().getExtras();
        if (!mAddNewContact) {
        EntityModifier.parseExtras(this, source, insert, extras);
        }

        // Ensure we have some default fields
        EntityModifier.ensureKindExists(insert, source, Phone.CONTENT_ITEM_TYPE);
//        EntityModifier.ensureKindExists(insert, source, Email.CONTENT_ITEM_TYPE);//remove email editext field

        // Create "My Contacts" membership for Google contacts
        // TODO: move this off into "templates" for each given source
        if (GoogleSource.ACCOUNT_TYPE.equals(source.accountType)) {
            GoogleSource.attemptMyContactsMembership(insert, this);
        }
        ContactsUtils.setState(mState);
        ContactsUtils.setValues(mValuesForCreateContact);

        if (mState == null) {
            // Create state if none exists yet
            mState = EntitySet.fromSingle(insert);
        } else {
            // Add contact onto end of existing state
            mState.add(insert);
        }
        Log.i(TAG,"mState is " + mState);

        bindEditors();
    }

    /**
     * Compare EntityDeltas for sorting the stack of editors.
     */
    public int compare(EntityDelta one, EntityDelta two) {
        // Check direct equality
        if (one.equals(two)) {
            return 0;
        }

        final Sources sources = Sources.getInstance(this);
        String accountType = one.getValues().getAsString(RawContacts.ACCOUNT_TYPE);
        final ContactsSource oneSource = sources.getInflatedSource(accountType,
                ContactsSource.LEVEL_SUMMARY);
        accountType = two.getValues().getAsString(RawContacts.ACCOUNT_TYPE);
        final ContactsSource twoSource = sources.getInflatedSource(accountType,
                ContactsSource.LEVEL_SUMMARY);

        // Check read-only
        if (oneSource.readOnly && !twoSource.readOnly) {
            return 1;
        } else if (twoSource.readOnly && !oneSource.readOnly) {
            return -1;
        }

        // Check account type
        boolean skipAccountTypeCheck = false;
        boolean oneIsGoogle = oneSource instanceof GoogleSource;
        boolean twoIsGoogle = twoSource instanceof GoogleSource;
        if (oneIsGoogle && !twoIsGoogle) {
            return -1;
        } else if (twoIsGoogle && !oneIsGoogle) {
            return 1;
        } else if (oneIsGoogle && twoIsGoogle){
            skipAccountTypeCheck = true;
        }

        int value = 0;
        if (!skipAccountTypeCheck) {
        	/*if (oneSource.accountType == null) {	// Null pointer protection for phone contacts
        		return -1;
        	}
          */
            if (oneSource.accountType != null && twoSource.accountType != null) {
                value = oneSource.accountType.compareTo(twoSource.accountType);
            } else {
                return -1;
            }
            if (value != 0) {
                return value;
            }
        }

        // Check account name
        ValuesDelta oneValues = one.getValues();
        String oneAccount = oneValues.getAsString(RawContacts.ACCOUNT_NAME);
        if (oneAccount == null) oneAccount = "";
        ValuesDelta twoValues = two.getValues();
        String twoAccount = twoValues.getAsString(RawContacts.ACCOUNT_NAME);
        if (twoAccount == null) twoAccount = "";
        value = oneAccount.compareTo(twoAccount);
        if (value != 0) {
            return value;
        }

        // Both are in the same account, fall back to contact ID
        Long oneId = oneValues.getAsLong(RawContacts._ID);
        Long twoId = twoValues.getAsLong(RawContacts._ID);
        if (oneId == null) {
            return -1;
        } else if (twoId == null) {
            return 1;
        }

        return (int)(oneId - twoId);
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
    private CellConnMgr mCellMgr = new CellConnMgr(serviceComplete);
}
