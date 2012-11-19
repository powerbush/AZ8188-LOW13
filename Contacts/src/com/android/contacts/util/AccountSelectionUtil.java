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

package com.android.contacts.util;

import com.android.contacts.ImportVCardActivity;
import com.android.contacts.R;
import com.android.contacts.model.ContactsSource;
import com.android.contacts.model.GoogleSource;
import com.android.contacts.model.Sources;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.net.Uri;
import android.provider.ContactsContract.RawContacts;

import com.android.internal.telephony.Phone;

import java.util.List;

/**
 * Utility class for selectiong an Account for importing contact(s)
 */
public class AccountSelectionUtil {
    // TODO: maybe useful for EditContactActivity.java...
    private static final String LOG_TAG = "AccountSelectionUtil";

    public static boolean mVCardShare = false;

    public static Uri mPath;

    public static class AccountSelectedListener
            implements DialogInterface.OnClickListener {

        final private Context mContext;
        final private int mResId;

        final protected List<Account> mAccountList;

        public AccountSelectedListener(Context context, List<Account> accountList, int resId) {
            if (accountList == null || accountList.size() == 0) {
                Log.e(LOG_TAG, "The size of Account list is 0.");
            }
            mContext = context;
            mAccountList = accountList;
            mResId = resId;
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
//            doImport(mContext, mResId, mAccountList.get(which));
        }
    }

    public static Dialog getSelectAccountDialog(Context context, int resId) {
        return getSelectAccountDialog(context, resId, null, null);
    }

    public static Dialog getSelectAccountDialog(Context context, int resId,
            DialogInterface.OnClickListener onClickListener) {
        return getSelectAccountDialog(context, resId, onClickListener, null);
    }

    /**
     * When OnClickListener or OnCancelListener is null, uses a default listener.
     * The default OnCancelListener just closes itself with {@link Dialog#dismiss()}.
     */
    public static Dialog getSelectAccountDialog(Context context, int resId,
            DialogInterface.OnClickListener onClickListener,
            DialogInterface.OnCancelListener onCancelListener) {
        final Sources sources = Sources.getInstance(context);
        final List<Account> writableAccountList = sources.getAccounts(true);

        // Assume accountList.size() > 1

        // Wrap our context to inflate list items using correct theme
        final Context dialogContext = new ContextThemeWrapper(
                context, android.R.style.Theme_Light);
        final LayoutInflater dialogInflater = (LayoutInflater)dialogContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ArrayAdapter<Account> accountAdapter =
            new ArrayAdapter<Account>(context, android.R.layout.simple_list_item_2,
                    writableAccountList) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = dialogInflater.inflate(
                            android.R.layout.simple_list_item_2,
                            parent, false);
                }

                // TODO: show icon along with title
                final TextView text1 =
                        (TextView)convertView.findViewById(android.R.id.text1);
                final TextView text2 =
                        (TextView)convertView.findViewById(android.R.id.text2);

                final Account account = this.getItem(position);
                final ContactsSource source =
                    sources.getInflatedSource(account.type,
                            ContactsSource.LEVEL_SUMMARY);
                final Context context = getContext();

                text1.setText(account.name);
                text2.setText(source.getDisplayLabel(context));

                return convertView;
            }
        };

        if (onClickListener == null) {
            AccountSelectedListener accountSelectedListener =
                new AccountSelectedListener(context, writableAccountList, resId);
            onClickListener = accountSelectedListener;
        }
        if (onCancelListener == null) {
            onCancelListener = new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                }
            };
        }
        return new AlertDialog.Builder(context)
            .setTitle(R.string.dialog_new_contact_account)
            .setSingleChoiceItems(accountAdapter, 0, onClickListener)
            .setOnCancelListener(onCancelListener)
            .create();
    }

//    public static void doImport(Context context, int resId, Account account) {
//        Log.i(LOG_TAG, "doImport " + context.getString(resId));
//        switch (resId) {
//            case R.string.import_from_sim: {
//                doImportFromSim(context, account, RawContacts.INDICATE_SIM);
//                break;
//            }
//            case R.string.import_from_sim1: {
//                doImportFromSim(context, account, RawContacts.INDICATE_SIM1);
//                break;
//            }
//            case R.string.import_from_sim2: {
//                doImportFromSim(context, account, RawContacts.INDICATE_SIM2);
//                break;
//            } 
//            case R.string.import_from_usim: {
//                doImportFromSim(context, account, RawContacts.INDICATE_USIM);
//                break;
//            } 
//            case R.string.import_from_usim1: {
//                doImportFromSim(context, account, RawContacts.INDICATE_USIM1);
//                break;
//            } 
//            case R.string.import_from_usim2: {
//                doImportFromSim(context, account, RawContacts.INDICATE_USIM2);
//                break;
//            } 
//            case R.string.import_from_sdcard: {
//                doImportFromSdCard(context, account);
//                break;
//            }
//        }
//    }

//    public static void doImportFromSim(Context context, Account account, int simId) {
//        if (account != null) {
//            GoogleSource.createMyContactsIfNotExist(account, context);
//        }
//
//        Intent importIntent = new Intent(Intent.ACTION_VIEW);
//        importIntent.putExtra(Phone.GEMINI_SIM_ID_KEY, simId);
//        importIntent.putExtra(RawContacts.INDICATE_PHONE_SIM, simId); 
//        importIntent.setType("vnd.android.cursor.item/sim-contact");
//        if (account != null) {
//            importIntent.putExtra("account_name", account.name);
//            importIntent.putExtra("account_type", account.type);
//        }
//        importIntent.setClassName("com.android.phone", "com.android.phone.SimContacts");
//        context.startActivity(importIntent);
//    }
    
//    public static void doImportFromSim(Context context, Account account) {
//        if (account != null) {
//            GoogleSource.createMyContactsIfNotExist(account, context);
//        }
//
//        Intent importIntent = new Intent(Intent.ACTION_VIEW);
//        importIntent.setType("vnd.android.cursor.item/sim-contact");
//        if (account != null) {
//            importIntent.putExtra("account_name", account.name);
//            importIntent.putExtra("account_type", account.type);
//        }
//        importIntent.setClassName("com.android.phone", "com.android.phone.SimContacts");
//        context.startActivity(importIntent);
//    }

//    public static void doImportFromSdCard(Context context, Account account) {
//        if (account != null) {
//            GoogleSource.createMyContactsIfNotExist(account, context);
//        }
//
//        Intent importIntent = new Intent(context, ImportVCardActivity.class);
//        if (account != null) {
//            importIntent.putExtra("account_name", account.name);
//            importIntent.putExtra("account_type", account.type);
//        }
//
//        if (mVCardShare) {
//            importIntent.setAction(Intent.ACTION_VIEW);
//            importIntent.setData(mPath);
//        }
//        mVCardShare = false;
//        mPath = null;
//        context.startActivity(importIntent);
//    }
    }
