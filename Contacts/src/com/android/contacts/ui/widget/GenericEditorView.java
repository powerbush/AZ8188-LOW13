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

package com.android.contacts.ui.widget;

import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.model.ContactsSource;
import com.android.contacts.model.Editor;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.EntityModifier;
import com.android.contacts.model.ContactsSource.DataKind;
import com.android.contacts.model.ContactsSource.EditField;
import com.android.contacts.model.ContactsSource.EditType;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.android.contacts.ui.ViewIdGenerator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.telephony.PhoneNumberFormattingTextWatcher;
import com.mediatek.telephony.PhoneNumberFormattingTextWatcherEx;
import android.telephony.PhoneNumberUtils;
import com.mediatek.telephony.PhoneNumberFormatUtilEx;

import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * Simple editor that handles labels and any {@link EditField} defined for
 * the entry. Uses {@link ValuesDelta} to read any existing
 * {@link Entity} values, and to correctly write any changes values.
 */
public class GenericEditorView extends RelativeLayout implements Editor, View.OnClickListener {
    private static final String TAG = "GenericEditorView";
    protected static final int RES_FIELD = R.layout.item_editor_field;
    protected static final int RES_LABEL_ITEM = android.R.layout.simple_list_item_1;
    protected static final int RES_SPINNER = R.layout.item_spinner_field;

    protected LayoutInflater mInflater;

    protected static final int INPUT_TYPE_CUSTOM = EditorInfo.TYPE_CLASS_TEXT
            | EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS;

    protected TextView mLabel;
    protected ViewGroup mFields;
    protected View mDelete;
    protected View mMore;
    protected View mLess;
    protected Button mSpinnerButton;

    protected DataKind mKind;
    protected ValuesDelta mEntry;
    protected EntityDelta mState;
    protected boolean mReadOnly;

    protected boolean mHideOptional = true;

    protected EditType mType;
    // Used only when a user tries to use custom label.
    private EditType mPendingType;

    private ViewIdGenerator mViewIdGenerator;

    //MTK for the structured name
    private EditText mPrefixName;
    private EditText mGivenName;
    private EditText mMiddleName;
    private EditText mFamilyName;
    private EditText mSuffixName;
    private EditText mPhoneticGivenName;
    private EditText mPhoneticMiddleName;
    private EditText mPhoneticFamilyName;
    
    private Dialog mGroupDialog;
    
    private HashMap<Long, String> mMap = new HashMap<Long, String>();

    public GenericEditorView(Context context) {
        super(context);
    }

    public GenericEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** {@inheritDoc} */
    @Override
    protected void onFinishInflate() {
        mInflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        mLabel = (TextView)findViewById(R.id.edit_label);
        mLabel.setOnClickListener(this);

        mFields = (ViewGroup)findViewById(R.id.edit_fields);

        mDelete = findViewById(R.id.edit_delete);
        mDelete.setOnClickListener(this);

        mMore = findViewById(R.id.edit_more);
        mMore.setOnClickListener(this);

        mLess = findViewById(R.id.edit_less);
        mLess.setOnClickListener(this);
    }

    protected EditorListener mListener;

    public void setEditorListener(EditorListener listener) {
        mListener = listener;
    }

    public void setDeletable(boolean deletable) {
        mDelete.setVisibility(deletable ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mLabel.setEnabled(enabled);
        final int count = mFields.getChildCount();
        for (int pos = 0; pos < count; pos++) {
            final View v = mFields.getChildAt(pos);
            v.setEnabled(enabled);
        }
        mMore.setEnabled(enabled);
        mLess.setEnabled(enabled);
    }

    /**
     * Build the current label state based on selected {@link EditType} and
     * possible custom label string.
     */
    private void rebuildLabel() {
        // Handle undetected types
        if (mType == null) {
            mLabel.setText(R.string.unknown);
            return;
        }

        if (mType.customColumn != null) {
            // Use custom label string when present
            final String customText = mEntry.getAsString(mType.customColumn);
            if (customText != null) {
                mLabel.setText(customText);
                return;
            }
        }

        // Otherwise fall back to using default label
        mLabel.setText(mType.labelRes);
    }

    /** {@inheritDoc} */
    public void onFieldChanged(String column, String value) {
        // Field changes are saved directly
        mEntry.put(column, value);
        if (mListener != null) {
            mListener.onRequest(EditorListener.FIELD_CHANGED);
        }
    }

    public boolean isAnyFieldFilledOut() {
        int childCount = mFields.getChildCount();
        for (int i = 0; i < childCount; i++) {
            EditText editorView = (EditText) mFields.getChildAt(i);
            if (!TextUtils.isEmpty(editorView.getText())) {
                return true;
            }
        }
        return false;
    }

    private void rebuildValues() {
        setValues(mKind, mEntry, mState, mReadOnly, mViewIdGenerator);
    }

    //MTK
    public void inflateStructuredNameView(ContactsSource source) {
        DataKind kind = source.getKindForMimetype(StructuredName.CONTENT_ITEM_TYPE);
        if (kind == null) {
            return;
        }

        for (EditField field : kind.fieldList) {
            // Inflate field from definition
            EditText fieldView = (EditText)mInflater.inflate(RES_FIELD, mFields, false);
            final String column = field.column;

            if (StructuredName.PREFIX.equals(column)) {
                mPrefixName = fieldView;
            } else if (StructuredName.GIVEN_NAME.equals(column)) {
                mGivenName = fieldView;
            } else if (StructuredName.MIDDLE_NAME.equals(column)) {
                mMiddleName = fieldView;
            } else if (StructuredName.FAMILY_NAME.equals(column)) {
                mFamilyName = fieldView;
            } else if (StructuredName.SUFFIX.equals(column)) {
                mSuffixName = fieldView;
            } else if (StructuredName.PHONETIC_GIVEN_NAME.equals(column)) {
                mPhoneticGivenName = fieldView;
            } else if (StructuredName.PHONETIC_MIDDLE_NAME.equals(column)) {
                mPhoneticMiddleName = fieldView;
            } else if (StructuredName.PHONETIC_FAMILY_NAME.equals(column)) {
                mPhoneticFamilyName  = fieldView;
            }

        }
    }
    
    /**
     * Prepare this editor using the given {@link DataKind} for defining
     * structure and {@link ValuesDelta} describing the content to edit.
     */
    public void setValues(DataKind kind, ValuesDelta entry, EntityDelta state, boolean readOnly,
            ViewIdGenerator vig) {
        mKind = kind;
        mEntry = entry;
        mState = state;
        mReadOnly = readOnly;
        mViewIdGenerator = vig;
        setId(vig.getId(state, kind, entry, ViewIdGenerator.NO_VIEW_INDEX));

        final boolean enabled = !readOnly;

        if (entry != null && !entry.isVisible()) {
            // Hide ourselves entirely if deleted
            setVisibility(View.GONE);
            return;
        } else {
            setVisibility(View.VISIBLE);
        }

        // Display label selector if multiple types available
        final boolean hasTypes = EntityModifier.hasEditTypes(kind);
        mLabel.setVisibility(hasTypes ? View.VISIBLE : View.GONE);
        mLabel.setEnabled(enabled);
        if (hasTypes) {
            mType = EntityModifier.getCurrentType(entry, kind);
            rebuildLabel();
        }

        // Build out set of fields
        mFields.removeAllViews();
        boolean hidePossible = false;
        int n = 0;
        for (int i = 0; kind.fieldList != null && i < kind.fieldList.size(); i++) {
            EditField field = kind.fieldList.get(i);
            // Inflate field from definition
            EditText fieldView = null;
         // Read current value from state
            final String column = field.column;
            if (StructuredName.PREFIX.equals(column)) {
                fieldView = mPrefixName;
            } else if (StructuredName.GIVEN_NAME.equals(column)) {
                fieldView = mGivenName;
            } else if (StructuredName.MIDDLE_NAME.equals(column)) {
                fieldView = mMiddleName;
            } else if (StructuredName.FAMILY_NAME.equals(column)) {
                fieldView = mFamilyName;
            } else if (StructuredName.SUFFIX.equals(column)) {
                fieldView = mSuffixName;
            } else if (StructuredName.PHONETIC_GIVEN_NAME.equals(column)) {
                fieldView = mPhoneticGivenName;
            } else if (StructuredName.PHONETIC_MIDDLE_NAME.equals(column)) {
                fieldView = mPhoneticMiddleName;
            } else if (StructuredName.PHONETIC_FAMILY_NAME.equals(column)) {
                fieldView = mPhoneticFamilyName;
            }
            
            if (fieldView == null) {
                fieldView = (EditText)mInflater.inflate(RES_FIELD, mFields, false);
            }
            fieldView.setId(vig.getId(state, kind, entry, n++));
            if (field.titleRes > 0) {
                fieldView.setHint(field.titleRes);
            }
            final int inputType = field.inputType;
            fieldView.setInputType(inputType);
            //MTK
            //if (inputType == InputType.TYPE_CLASS_PHONE) {
            //    fieldView.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
            //}
            fieldView.setMinLines(field.minLines);

            final String value = entry.getAsString(column);
            //fieldView.setText(value);
            if (inputType == InputType.TYPE_CLASS_PHONE) {
                if (!TextUtils.isEmpty(value)) {
                    fieldView.setText(PhoneNumberFormatUtilEx.formatNumber(value));
                } else {
                  fieldView.setText(value);
                 }
                fieldView.addTextChangedListener(new PhoneNumberFormattingTextWatcherEx());
            } else {
                fieldView.setText(value);

            }

            fieldView.setSelection(0);
            fieldView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        EditText tmp = (EditText)v;
                        if (tmp != null) {
                            tmp.setSelection(tmp.getEditableText().length());
                        }
                    }                       
                }
                
            });

            // Prepare listener for writing changes
            fieldView.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    if (inputType == InputType.TYPE_CLASS_PHONE ) {
                        // Trigger event for newly changed value
                        //onFieldChanged(column, PhoneNumberUtils.stripSeparators(s.toString()));
                        onFieldChanged(column, s.toString().replaceAll(" ", ""));
                    } else {
                    // Trigger event for newly changed value
                    onFieldChanged(column, s.toString());
                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });

            // Hide field when empty and optional value
            final boolean couldHide = (!ContactsUtils.isGraphic(value) && field.optional);
            final boolean willHide = (mHideOptional && couldHide);
            fieldView.setVisibility(willHide ? View.GONE : View.VISIBLE);
            fieldView.setEnabled(enabled);
            fieldView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.edit_text));
            if(isSpecialWidthAndHeight(240, 320)){
                fieldView.setPadding(10,0,10,5);
            }else{
                fieldView.setPadding(10, 10, 10, 20);
            }
            fieldView.setSingleLine();
            hidePossible = hidePossible || couldHide;

            mFields.addView(fieldView);
        }
        if (kind.hasSpinner) {
            mSpinnerButton = (Button)mInflater.inflate(RES_SPINNER, mFields, false);
            mSpinnerButton.setOnClickListener(this);
            mFields.addView(mSpinnerButton);
            if (GroupMembership.CONTENT_ITEM_TYPE.equals(mKind.mimeType)) {
                Log.i("GenericEditorView", "initgroups");
                new AsyncTask<Void, Void, String>(){

                    protected String doInBackground(Void... params) {
                        StringBuilder groups = new StringBuilder();
                        StringBuilder selection = new StringBuilder();
                        
                        ArrayList<ValuesDelta> entryList = mState.getMimeEntries(GroupMembership.CONTENT_ITEM_TYPE);
                        if (entryList == null) {
                            return mContext.getString(R.string.groups_none);
                        }
                        boolean hasSelection = false;
                        int count = entryList.size();
                        selection.append(Groups._ID + " IN(");
                        for (int i = 0; i < count; i++) {
                            ValuesDelta values = entryList.get(i);
                            // error
                            Long group_id = values.getAsLong(GroupMembership.GROUP_ROW_ID);
                            if (group_id == null)
                                continue;
                            if (i != 0) {
                                selection.append(",");
                            }
                            selection.append(group_id);
                            hasSelection = true;
                        }
                        selection.append(")");
                        Cursor cursor = mContext.getContentResolver().query(Groups.CONTENT_URI, 
                                new String[]{Groups._ID, Groups.TITLE}, hasSelection ? selection.toString() : null, null, null);
                        mMap.clear();
                        
                        if (cursor != null) {
                            while(cursor.moveToNext()) {
                                long id = cursor.getLong(0);
                                String title = cursor.getString(1);
                                if (cursor.getPosition() != 0) {
                                    groups.append(",");
                                }
                                if(title!=null)title = ContactsUtils.getGroupsName(getContext(), title);
                                groups.append(title);
                                mMap.put(id, title);
                            }
                            cursor.close();
                        }
                        return groups.toString();
                    }

                    protected void onPostExecute(String result) {
                        mSpinnerButton.setText(result);
                    }
                    
                }.execute();
//                ArrayList<ValuesDelta> entryList = mState.getMimeEntries(GroupMembership.CONTENT_ITEM_TYPE);
//                for (ValuesDelta values : entryList) {
//                    long group_id = values.getAsLong(GroupMembership.GROUP_ROW_ID);
//                }
            }
        }
        // When hiding fields, place expandable
        if (hidePossible) {
            mMore.setVisibility(mHideOptional ? View.VISIBLE : View.GONE);
            mLess.setVisibility(mHideOptional ? View.GONE : View.VISIBLE);
        } else {
            mMore.setVisibility(View.GONE);
            mLess.setVisibility(View.GONE);
        }
        mMore.setEnabled(enabled);
        mLess.setEnabled(enabled);
    }

    /**
     * Prepare dialog for entering a custom label. The input value is trimmed: white spaces before
     * and after the input text is removed.
     * <p>
     * If the final value is empty, this change request is ignored;
     * no empty text is allowed in any custom label.
     */
    private Dialog createCustomDialog() {
        final EditText customType = new EditText(mContext);
        customType.setInputType(INPUT_TYPE_CUSTOM);
        //MTK
        customType.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        customType.requestFocus();

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.customLabelPickerTitle);
        builder.setView(customType);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final String customText = customType.getText().toString().trim();
                if (ContactsUtils.isGraphic(customText)) {
                    // Now we're sure it's ok to actually change the type value.
                    mType = mPendingType;
                    mPendingType = null;
                    mEntry.put(mKind.typeColumn, mType.rawValue);
                    mEntry.put(mType.customColumn, customText);
                    rebuildLabel();
                    if (!mFields.hasFocus())
                        mFields.requestFocus();
                }
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }

    /**
     * Prepare dialog for picking a new {@link EditType} or entering a
     * custom label. This dialog is limited to the valid types as determined
     * by {@link EntityModifier}.
     */
    public Dialog createLabelDialog() {
        // Build list of valid types, including the current value
        final List<EditType> validTypes = EntityModifier.getValidTypes(mState, mKind, mType);

        // Wrap our context to inflate list items using correct theme
        final Context dialogContext = new ContextThemeWrapper(mContext,
                android.R.style.Theme_Light);
        final LayoutInflater dialogInflater = mInflater.cloneInContext(dialogContext);

        final ListAdapter typeAdapter = new ArrayAdapter<EditType>(mContext, RES_LABEL_ITEM,
                validTypes) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = dialogInflater.inflate(RES_LABEL_ITEM, parent, false);
                }

                final EditType type = this.getItem(position);
                final TextView textView = (TextView)convertView;
                textView.setText(type.labelRes);
                return textView;
            }
        };

        final DialogInterface.OnClickListener clickListener =
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                final EditType selected = validTypes.get(which);
                if (selected.customColumn != null) {
                    // Show custom label dialog if requested by type.
                    //
                    // Only when the custum value input in the next step is correct one.
                    // this method also set the type value to what the user requested here.
                    mPendingType = selected;
                    createCustomDialog().show();
                } else {
                    // User picked type, and we're sure it's ok to actually write the entry.
                    mType = selected;
                    mEntry.put(mKind.typeColumn, mType.rawValue);
                    rebuildLabel();
                    if (!mFields.hasFocus())
                        mFields.requestFocus();
                }
            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.selectLabel);
        builder.setSingleChoiceItems(typeAdapter, 0, clickListener);
        return builder.create();
    }

    /** {@inheritDoc} */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_label: {
                createLabelDialog().show();
                break;
            }
            case R.id.edit_delete: {
                // Keep around in model, but mark as deleted
                mEntry.markDeleted();

                // Remove editor from parent view
                final ViewGroup parent = (ViewGroup)getParent();
                if (parent != null) parent.removeView(this); // condition added by mtk80909 dealing with null-pointer

                if (mListener != null) {
                    // Notify listener when present
                    mListener.onDeleted(this);
                }
                break;
            }
            case R.id.edit_more:
            case R.id.edit_less: {
                mHideOptional = !mHideOptional;
                Log.i(TAG, "more or less click ");
                rebuildValues();
                if (!mFields.hasFocus()) {
                    Log.d(TAG, " onClick(), mFields.requestFocus()");
                    mFields.requestFocus();
                    }
                break;
            }
            case R.id.spinner_button: {
                String mimetype = mKind.mimeType;
                Log.i("GenericEditorView", "spinner button kind " + mimetype);
                if (CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE.equals(mimetype)) {
                    if(mGroupDialog == null)
                    	mGroupDialog = creatGroupsDialog();
                    if(!mGroupDialog.isShowing())mGroupDialog.show();
                    
        }
                break;
            }
        }
    }
    
    private Dialog creatGroupsDialog() {
        final HashMap<Long, String> newMap = new HashMap<Long, String>(mMap);
        final Context dialogContext = new ContextThemeWrapper(mContext,
                android.R.style.Theme_Light);
//        final LayoutInflater dialogInflater = mInflater.cloneInContext(dialogContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        
        final Cursor cursor = mContext.getContentResolver().query(Groups.CONTENT_URI,
                new String[]{Groups._ID, Groups.TITLE}, Groups.DELETED + "=0 AND " + Groups.ACCOUNT_TYPE + "='DeviceOnly'", null, Groups.SYSTEM_ID + " DESC, " + Groups.TITLE);
        CursorAdapter adapter = new CursorAdapter(mContext, cursor, false) {
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View view = mInflater.inflate(R.layout.simple_list_item_multiple_choice, parent, false);
                return view;
            }
            public void bindView(View view, Context context, Cursor cursor) {
                CheckedTextView tv =  (CheckedTextView)view;
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                tv.setText(ContactsUtils.getGroupsName(mContext, name));
                tv.setChecked(newMap.containsKey(id));
                
            }
        };
        
        ListView list = new ListView(mContext);
        list.setBackgroundResource(R.drawable.white_background);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                cursor.moveToPosition(position);
                String title = cursor.getString(1);
                Log.i(TAG, "onlist item click " + position);
                CheckedTextView tv =  (CheckedTextView)v;
                tv.toggle();
                boolean isChekced = tv.isChecked();
                if (isChekced) {
                    if (!newMap.containsKey(id)) {
                        newMap.put(id, title);
                    }
                } else {
                    if (newMap.containsKey(id)) {
                        newMap.remove(id);
                    }
                }
            }
        });
        builder.setView(list,0,0,0,0);
        builder.setTitle(R.string.groups_pick);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                StringBuilder builder = new StringBuilder();
                for (Entry<Long, String> entry: newMap.entrySet()) {
                    if (builder.length() > 0) {
                        builder.append(",");
                    }
                    String title = entry.getValue();
                    
                    builder.append(ContactsUtils.getGroupsName(getContext(), title));
                }
                
                if (builder.length() != 0) {
                    mSpinnerButton.setText(builder);
                } else {
                    mSpinnerButton.setText(R.string.groups_none);
                }

                ArrayList<ValuesDelta> entryList = mState.getMimeEntries(GroupMembership.CONTENT_ITEM_TYPE);
                ArrayList<Long> insert = new ArrayList<Long>(newMap.keySet());
                ArrayList<Long> delete = new ArrayList<Long>(mMap.keySet());
                int insertCount = insert.size();
                int deleteCount = delete.size();
                Log.i(TAG, "before set state insertCount " + insertCount + ",deleteCount " + deleteCount);
                for (int i = 0; i < insertCount; i++) {
                    long groupId = insert.get(i);
                    int index = delete.indexOf(groupId);
                    if (index >= 0) {
                        delete.remove(index);
                        continue;
                    }
                    ValuesDelta value = EntityModifier.insertChild(mState, mKind);
                    if(value != null)value.put(GroupMembership.GROUP_ROW_ID, (int)groupId);
                }
                deleteCount = delete.size();
                for (int i = 0; i < deleteCount; i++) {
                    long groupId = delete.get(i);
                    if (entryList == null) {
                        Log.e(TAG, "entry list should be not null");
                        break;
                    }
                    int count = entryList.size();
                    for (int j = 0; j < count; j++) {
                        ValuesDelta values = entryList.get(j);
                        // error
                        try{
                        	long group_id = -1;
                        	Long temp =  values.getAsLong(GroupMembership.GROUP_ROW_ID);
                        	if(temp != null)group_id = temp;
                        
                        if (groupId == group_id) {
                            values.markDeleted();
                            break;
                        }
                        }catch(Exception e){
                        	continue;
                        }
                    }
                }
                mMap = newMap;
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog =  builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            
            public void onDismiss(DialogInterface dialog) {
            	mGroupDialog = null;
                if (cursor != null) {
                    cursor.close();
                }
                
            }
        });
        return dialog;
    }

    private static class SavedState extends BaseSavedState {
        public boolean mHideOptional;
        public int[] mVisibilities;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mVisibilities = new int[in.readInt()];
            in.readIntArray(mVisibilities);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mVisibilities.length);
            out.writeIntArray(mVisibilities);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    /**
     * Saves the visibility of the child EditTexts, and mHideOptional.
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.mHideOptional = mHideOptional;

        final int numChildren = mFields.getChildCount();
        ss.mVisibilities = new int[numChildren];
        for (int i = 0; i < numChildren; i++) {
            ss.mVisibilities[i] = mFields.getChildAt(i).getVisibility();
        }

        return ss;
    }

    /**
     * Restores the visibility of the child EditTexts, and mHideOptional.
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mHideOptional = ss.mHideOptional;

        int numChildren = Math.min(mFields.getChildCount(), ss.mVisibilities.length);
        for (int i = 0; i < numChildren; i++) {
            mFields.getChildAt(i).setVisibility(ss.mVisibilities[i]);
        }
    }
    public boolean isSpecialWidthAndHeight(int width, int height) {
        Display mDisplay = ((Activity)getContext()).getWindowManager().getDefaultDisplay();
        final int mWidth = mDisplay.getWidth();
        final int mHeight = mDisplay.getHeight();

        return ((width == mWidth) && (height == mHeight))||((height == mWidth) && (width == mHeight));
    }
}
