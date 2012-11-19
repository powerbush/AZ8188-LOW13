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
 * Copyright (C) 2006 The Android Open Source Project
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

import com.android.internal.telephony.ITelephony;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Telephony.SIMInfo;
import android.provider.Telephony.Intents;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.mediatek.featureoption.FeatureOption;

/**
 * Helper class to listen for some magic character sequences
 * that are handled specially by the dialer.
 *
 * TODO: there's lots of duplicated code between this class and the
 * corresponding class under apps/Phone.  Let's figure out a way to
 * unify these two classes (in the framework? in a common shared library?)
 */
public class SpecialCharSequenceMgr {
    private static final String TAG = "SpecialCharSequenceMgr";
    private static final String MMI_IMEI_DISPLAY = "*#06#";

    /** This class is never instantiated. */
    private SpecialCharSequenceMgr() {
    }

    static boolean handleChars(Context context, String input, EditText textField) {
        return handleChars(context, input, false, textField);
    }

    static boolean handleChars(Context context, String input) {
        return handleChars(context, input, false, null);
    }

    static boolean handleChars(Context context, String input, boolean useSystemWindow,
            EditText textField) {

        Log.d(TAG, "handleChars() dialString:" + input);
        //get rid of the separators so that the string gets parsed correctly
        String dialString = PhoneNumberUtils.stripSeparators(input);

        if (handleIMEIDisplay(context, dialString, useSystemWindow)
                || handlePinEntry(context, dialString)
                || handleAdnEntry(context, dialString, textField)
                || handleSecretCode(context, dialString)) {
            return true;
        }

        return false;
    }

    /**
     * Handles secret codes to launch arbitrary activities in the form of *#*#<code>#*#*.
     * If a secret code is encountered an Intent is started with the android_secret_code://<code>
     * URI.
     *
     * @param context the context to use
     * @param input the text to check for a secret code in
     * @return true if a secret code was encountered
     */
    static boolean handleSecretCode(Context context, String input) {
        // Secret codes are in the form *#*#<code>#*#*
        int len = input.length();
        if (len > 8 && input.startsWith("*#*#") && input.endsWith("#*#*")) {
            Intent intent = new Intent(Intents.SECRET_CODE_ACTION,
                    Uri.parse("android_secret_code://" + input.substring(4, len - 4)));
            context.sendBroadcast(intent);
            return true;
        }

        return false;
    }

    /**
     * Handle ADN requests by filling in the SIM contact number into the requested
     * EditText.
     *
     * This code works alongside the Asynchronous query handler {@link QueryHandler}
     * and query cancel handler implemented in {@link SimContactQueryCookie}.
     */
    static boolean handleAdnEntry(Context context, String input, EditText textField) {
        // xingping.zheng add for CR:127941
    	ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
    	
        /* ADN entries are of the form "N(N)(N)#" */

        // if the phone is keyguard-restricted, then just ignore this
        // input.  We want to make sure that sim card contacts are NOT
        // exposed unless the phone is unlocked, and this code can be
        // accessed from the emergency dialer.
        KeyguardManager keyguardManager =
                (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.inKeyguardRestrictedInputMode()) {
            return false;
        }

        int len = input.length();
        if ((len > 1) && (len < 5) && (input.endsWith("#"))) {
            try {
                // get the ordinal number of the sim contact
                int index = Integer.parseInt(input.substring(0, len-1));
                if(index <= 0)
                    return false;

                // The original code that navigated to a SIM Contacts list view did not
                // highlight the requested contact correctly, a requirement for PTCRB
                // certification.  This behaviour is consistent with the UI paradigm
                // for touch-enabled lists, so it does not make sense to try to work
                // around it.  Instead we fill in the the requested phone number into
                // the dialer text field.

                // create the async query handler
                QueryHandler handler = new QueryHandler (context.getContentResolver());

                // create the cookie object
                SimContactQueryCookie sc = new SimContactQueryCookie(index - 1, handler,
                        ADN_QUERY_TOKEN);

                // setup the cookie fields
                sc.contactNum = index - 1;
                sc.setTextField(textField);
                if (null != textField)
                {
                    sc.text = textField.getText().toString();
                }
                else
                {
                    sc.text = null;
                }
                // create the progress dialog
                Log.d(TAG, "handleAdnEntry() sc.progressDialog:" + sc.progressDialog);
                if(null == sc.progressDialog) {
                    sc.progressDialog = new ProgressDialog(context);
                }
                sc.progressDialog.setTitle(R.string.simContacts_title);
                sc.progressDialog.setMessage(context.getText(R.string.simContacts_emptyLoading));
                sc.progressDialog.setIndeterminate(true);
                sc.progressDialog.setCancelable(true);
                sc.progressDialog.setOnCancelListener(sc);
                sc.progressDialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

                sc.mDoubleQuery = false;
                sc.context = context;
                if(FeatureOption.MTK_GEMINI_SUPPORT){
                    final long defaultSim = Settings.System.getLong(context.getContentResolver(), Settings.System.VOICE_CALL_SIM_SETTING, -3);

                    log("handleAdnEntry, defaultSim = "+defaultSim);

                    if(defaultSim == Settings.System.VOICE_CALL_SIM_SETTING_INTERNET ||
                       defaultSim == Settings.System.DEFAULT_SIM_NOT_SET) {
                        log("handlePinEntry, bial out...");
                        return false;
                    }
                    
                    if(defaultSim == Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
                        // show sim selection dialog
                        boolean sim1Ready = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(Phone.GEMINI_SIM_1);
                        boolean sim2Ready = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(Phone.GEMINI_SIM_2);
                        boolean radio1On     = phone.isRadioOnGemini(Phone.GEMINI_SIM_1);
                        boolean radio2On     = phone.isRadioOnGemini(Phone.GEMINI_SIM_2);

                        if(!radio1On && !radio2On)
                            return false;
                        
                        if(sim1Ready && sim2Ready){
                            sc.mDoubleQuery = true;
                            try {
                            	if (iTel != null && iTel.getIccCardTypeGemini(Phone.GEMINI_SIM_1).equals("USIM")) {
                            		handler.startQuery(ADN_QUERY_TOKEN_SIM1, sc, Uri.parse("content://icc/pbr1"),
                                            new String[]{ADN_PHONE_NUMBER_COLUMN_NAME}, null, null, null);
                            	} else {
                            handler.startQuery(ADN_QUERY_TOKEN_SIM1, sc, Uri.parse("content://icc/adn1"),
                                               new String[]{ADN_PHONE_NUMBER_COLUMN_NAME}, null, null, null);
                        }
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                        }
                        else if(sim1Ready){
                            try {
                            	if (iTel != null && iTel.getIccCardTypeGemini(Phone.GEMINI_SIM_1).equals("USIM")) {
                            		handler.startQuery(ADN_QUERY_TOKEN_SIM1, sc, Uri.parse("content://icc/pbr1"),
                                            new String[]{ADN_PHONE_NUMBER_COLUMN_NAME}, null, null, null);
                            	} else {
                            handler.startQuery(ADN_QUERY_TOKEN_SIM1, sc, Uri.parse("content://icc/adn1"),
                                               new String[]{ADN_PHONE_NUMBER_COLUMN_NAME}, null, null, null);
                        }
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                        }
                        else if(sim2Ready){
                            try {
                            	if (iTel != null && iTel.getIccCardTypeGemini(Phone.GEMINI_SIM_2).equals("USIM")) {
                            		handler.startQuery(ADN_QUERY_TOKEN_SIM1, sc, Uri.parse("content://icc/pbr2"),
                                               new String[]{ADN_PHONE_NUMBER_COLUMN_NAME}, null, null, null);
                            	} else {
                                    handler.startQuery(ADN_QUERY_TOKEN_SIM1, sc, Uri.parse("content://icc/adn2"),
                                            new String[]{ADN_PHONE_NUMBER_COLUMN_NAME}, null, null, null);                            		
                            	}
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                        }
                        else
                            return false;
                    } else {
                        int defaultSimSlot = SIMInfo.getSlotById(context, defaultSim);
                        
                        sc.mDoubleQuery = false;
                        
                        log("handleAdnEntry, defaultSimSlot = "+defaultSimSlot);
                        
                        boolean isSimReady = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(defaultSimSlot);

                       if(!isSimReady)
                           return false;
                       
                       int token = ADN_QUERY_TOKEN_SIM1;
                       StringBuilder builder = new StringBuilder();
                       builder.append("content://icc/");
                       try {
                       if(defaultSimSlot == Phone.GEMINI_SIM_1) {
                    	   if (iTel != null && iTel.getIccCardTypeGemini(Phone.GEMINI_SIM_1).equals("USIM")) {
                    		   builder.append("pbr1");
                          	} else {
                           builder.append("adn1");
                          	}
                       } else {
                    	   if (iTel != null && iTel.getIccCardTypeGemini(Phone.GEMINI_SIM_2).equals("USIM")) {
                    		   builder.append("pbr2");
                          	} else {
                           builder.append("adn2");
                          	}
                           token = ADN_QUERY_TOKEN_SIM2;
                       }
                       } catch (RemoteException ex) {
                           ex.printStackTrace();
                       }

                       handler.startQuery(token, sc, Uri.parse(builder.toString()), new String[]{ADN_PHONE_NUMBER_COLUMN_NAME}, null, null, null);
                    }
                }
                else
                {
                    boolean isSimReady = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimState();
                    if(!isSimReady)
                        return false;
                    try {
                    	if (iTel != null && iTel.getIccCardType().equals("USIM")) {     
                    		handler.startQuery(ADN_QUERY_TOKEN, sc, Uri.parse("content://icc/pbr"),
                                    new String[]{ADN_PHONE_NUMBER_COLUMN_NAME}, null, null, null);
                    	} else {
                    handler.startQuery(ADN_QUERY_TOKEN, sc, Uri.parse("content://icc/adn"),
                        new String[]{ADN_PHONE_NUMBER_COLUMN_NAME}, null, null, null);
                    	}
                    } catch (Exception ex) {
            			ex.printStackTrace();
            		}
                }
                // display the progress dialog
                if (null != sc.progressDialog && !sc.progressDialog.isShowing()) {
                    Log.d(TAG, "handleAdnEntry() sc.progressDialog.show()");       
                    sc.progressDialog.show();
                }
                return true;
            } catch (NumberFormatException ex) {
                // Ignore
            } catch (RemoteException ex) {
                // Ignore
            }
        }
        return false;
    }

    static boolean handlePinEntry(Context context, String input) {
        if ((input.startsWith("**04") || input.startsWith("**05")) && input.endsWith("#")) {
            try {
                if (FeatureOption.MTK_GEMINI_SUPPORT) 
                {
                    ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                    if (phone == null) {
                    	Log.i(TAG,"phone is null !");
                    	return false;
                    }
                    final long defaultSim = Settings.System.getLong(context.getContentResolver(), Settings.System.VOICE_CALL_SIM_SETTING, -3);
                    
                    log("handlePinEntry, defaultSim = "+defaultSim);
                    
                    if(defaultSim == Settings.System.VOICE_CALL_SIM_SETTING_INTERNET ||
                       defaultSim == Settings.System.DEFAULT_SIM_NOT_SET) {
                        log("handlePinEntry, bial out...");
                        return false;
                    }
                    
                    if(defaultSim == Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
                        // show sim selection dialog
                        boolean sim1Ready = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(Phone.GEMINI_SIM_1);
                        boolean sim2Ready = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(Phone.GEMINI_SIM_2);
                        boolean radio1On  = phone.isRadioOnGemini(Phone.GEMINI_SIM_1);
                        boolean radio2On  = phone.isRadioOnGemini(Phone.GEMINI_SIM_2);
                        Log.d(TAG, "handlePinEntry sim1Ready : "+sim1Ready+
                                                 " sim2Ready : "+sim2Ready+
                                     " radio1On  : "+radio1On+
                                     " radio2On  : "+radio2On);
                        if(!radio1On && !radio2On)
                            return false;
                        if(sim1Ready && sim2Ready){        
                            Log.d(TAG, "handlePinEntry popup dialog");
                            Bundle bundle = new Bundle();
                        bundle.putCharSequence("MMI Code", input);
                            TwelveKeyDialer dialer = (TwelveKeyDialer)context;
                            dialer.showSpecialSequenceDialog(dialer.MMI_DIALOG, bundle);
                            return true;
                        }
                        else if(sim1Ready){
                            Log.d(TAG, "handlePinEntry send mmi to sim1");
                            return phone.handlePinMmiGemini(input, Phone.GEMINI_SIM_1);
                        }
                        else if(sim2Ready){
                            Log.d(TAG, "handlePinEntry send mmi to sim2");
                            return phone.handlePinMmiGemini(input, Phone.GEMINI_SIM_2);
                        }
                        else{
                            Log.d(TAG, "handlePinEntry nothing to do");
                            return false;
                        }
                    } else {
                        int defaultSimSlot = SIMInfo.getSlotById(context, defaultSim);
                        log("handlePinEntry, defaultSimSlot = "+defaultSimSlot);
                        
                        boolean isSimReady = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimState();
                        
                        if(!isSimReady)
                            return false;
                        
                        return phone.handlePinMmiGemini(input, defaultSimSlot);
                    }
                }
                else
                {
                    boolean isSimReady = TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimState();
                    if(!isSimReady)
                        return false;
                    return ITelephony.Stub.asInterface(ServiceManager.getService("phone"))
                            .handlePinMmi(input);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to handlePinMmi due to remote exception");
                return false;
            }
        }
        return false;
    }

    static boolean handleIMEIDisplay(Context context, String input, boolean useSystemWindow) {
        if (input.equals(MMI_IMEI_DISPLAY)) {
            int phoneType = ((TelephonyManager)context.getSystemService(
                    Context.TELEPHONY_SERVICE)).getPhoneType();

            if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                showIMEIPanel(context, useSystemWindow);
                return true;
            } else if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                showMEIDPanel(context, useSystemWindow);
                return true;
            }
        }

        return false;
    }

    static void showIMEIPanel(Context context, boolean useSystemWindow) {
        String imeiStr, imeiStr2;
        StringBuilder buf = new StringBuilder();
		
        AlertDialog alert = null;
        if (FeatureOption.MTK_GEMINI_SUPPORT) 
        {
            imeiStr = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
                    .getDeviceIdGemini(Phone.GEMINI_SIM_1);

            imeiStr2 = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
                    .getDeviceIdGemini(Phone.GEMINI_SIM_2);

            Bundle bundle = new Bundle();
            bundle.putCharSequence("IMEI1", imeiStr);
            bundle.putCharSequence("IMEI2", imeiStr2);
            
            TwelveKeyDialer dialer = (TwelveKeyDialer)context;
            dialer.showSpecialSequenceDialog(dialer.IMEI_DIALOG, bundle);
        }
        else
        {
            imeiStr = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
                .getDeviceId();

            //buf.append(context.getText(R.string.imei));
            buf.append("IMEI: ");
            buf.append(imeiStr);						
		
            alert = new AlertDialog.Builder(context)
            .setTitle(R.string.imei_dialog_title)
            .setMessage(buf.toString())
            .setPositiveButton(android.R.string.ok, null)
            .setCancelable(false)
            .show();
        }
    }

    static void showMEIDPanel(Context context, boolean useSystemWindow) {
        String meidStr = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE))
                .getDeviceId();
        TextView view=new TextView(context);
        view.setMinimumWidth(200);        
        AlertDialog alert = new AlertDialog.Builder(context)
                .setTitle(R.string.meid)
                .setMessage(meidStr)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .setView(view)
                .show();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
    }

    /*******
     * This code is used to handle SIM Contact queries
     *******/
    private static final String ADN_PHONE_NUMBER_COLUMN_NAME = "number";
    private static final String ADN_NAME_COLUMN_NAME = "name";
    private static final int ADN_QUERY_TOKEN = -1;
    private static final int ADN_QUERY_TOKEN_SIM1 = 0;
    private static final int ADN_QUERY_TOKEN_SIM2 = 1;

    /**
     * Cookie object that contains everything we need to communicate to the
     * handler's onQuery Complete, as well as what we need in order to cancel
     * the query (if requested).
     *
     * Note, access to the textField field is going to be synchronized, because
     * the user can request a cancel at any time through the UI.
     */
    private static class SimContactQueryCookie implements DialogInterface.OnCancelListener{
        public ProgressDialog progressDialog;
        public int contactNum;

        // Used to identify the query request.
        private int mToken;
        private QueryHandler mHandler;

        // The text field we're going to update
        private EditText textField;
        public String text;

        public Context context;
        public boolean mDoubleQuery = false;
        public String mSim1Number;
        public String mSim1Name;
        public String mSim2Number;
        public String mSim2Name; 
        
        public SimContactQueryCookie(int number, QueryHandler handler, int token) {
            contactNum = number;
            mHandler = handler;
            mToken = token;
        }

        /**
         * Synchronized getter for the EditText.
         */
        public synchronized EditText getTextField() {
            return textField;
        }

        public synchronized QueryHandler getQueryHandler(){
            return mHandler;
        }
        
        /**
         * Synchronized setter for the EditText.
         */
        public synchronized void setTextField(EditText text) {
            textField = text;
        }

        /**
         * Cancel the ADN query by stopping the operation and signaling
         * the cookie that a cancel request is made.
         */
        public synchronized void onCancel(DialogInterface dialog) {
            // close the progress dialog
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            // setting the textfield to null ensures that the UI does NOT get
            // updated.
            textField = null;

            // Cancel the operation if possible.
            mHandler.cancelOperation(mToken);
        }
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }
    
    /**
     * Asynchronous query handler that services requests to look up ADNs
     *
     * Queries originate from {@link handleAdnEntry}.
     */
    private static class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        /**
         * Override basic onQueryComplete to fill in the textfield when
         * we're handed the ADN cursor.
         */
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor c) {
            Log.d(TAG, "onQueryComplete token = "+token);
            SimContactQueryCookie sc = (SimContactQueryCookie) cookie;
            Context context = sc.progressDialog.getContext();
            EditText text = sc.getTextField();
          	final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
            String name = null;
            String number = null;

            if ((c != null) && (text != null) && (c.moveToPosition(sc.contactNum)))
    	    {
    	    	name = c.getString(c.getColumnIndexOrThrow(ADN_NAME_COLUMN_NAME));
    	    	number = c.getString(c.getColumnIndexOrThrow(ADN_PHONE_NUMBER_COLUMN_NAME));
    	    }

            switch(token)
            {
            case ADN_QUERY_TOKEN_SIM1:
                {
                    Log.d(TAG, "ADN_QUERY_TOKEN_SIM1");
            	    // start to query sim2 adn
        	    if(sc.mDoubleQuery)
        	    {
        	        sc.mSim1Name = name;
        	        sc.mSim1Number = number;
        	        try {
        	        	if (iTel != null && iTel.getIccCardTypeGemini(Phone.GEMINI_SIM_2).equals("USIM")) {
        	        		sc.getQueryHandler().startQuery(ADN_QUERY_TOKEN_SIM2, sc, Uri.parse("content://icc/pbr2"),
                                    new String[]{ADN_PHONE_NUMBER_COLUMN_NAME}, null, null, null);
        	        	} else {
        	        sc.getQueryHandler().startQuery(ADN_QUERY_TOKEN_SIM2, sc, Uri.parse("content://icc/adn2"),
                                                        new String[]{ADN_PHONE_NUMBER_COLUMN_NAME}, null, null, null);
        	        	}       
        	        } catch (Exception ex) {
        				ex.printStackTrace();
        			}
        	    }
        	    break;
                }
            case ADN_QUERY_TOKEN:
                {
                    Log.d(TAG, "ADN_QUERY_TOKEN_SIM2");
                }
            	break;
            case ADN_QUERY_TOKEN_SIM2:
                {
                    sc.mSim2Name = name;
                    sc.mSim2Number = number;
                }
            	break;
            }

            if(token == ADN_QUERY_TOKEN || !sc.mDoubleQuery)
            {
            	// close the progress dialog.
                if (null != sc.progressDialog && sc.progressDialog.isShowing()) {
                    Log.d(TAG, "dismiss sc.progressDialog:" + sc.progressDialog);                      
                    sc.progressDialog.dismiss();
                }
                // if the textview is valid, and the cursor is valid and postionable
                // on the Nth number, then we update the text field and display a
                // toast indicating the caller name.
                if ((c != null) && (text != null) && (c.moveToPosition(sc.contactNum))) 
                {
                    int len = 0;
                    if (null != number)
                    {
                        len = number.length();
                    }						
                    if (sc.text.equals(number))
                    {
                        Toast.makeText(context, context.getString(R.string.ghostData_phone)+"\n"+number, Toast.LENGTH_LONG)
                        .show();
                    }
                    else if ((len > 1) && (len < 5) && (number.endsWith("#"))) 
                    {
                        Toast.makeText(context, context.getString(R.string.ghostData_phone)+"\n"+number, Toast.LENGTH_LONG)
                        .show();
                    } 
                    else 
                    {
                        // fill the text in.
                        text.getText().replace(0, 0, number);

                        // display the name as a toast
                        name = context.getString(R.string.menu_callNumber, name);
                        Toast.makeText(context, name, Toast.LENGTH_SHORT)
                        .show();
                    }
                }
            }
            else if(token == ADN_QUERY_TOKEN_SIM2)
            {
            	if (null != sc.progressDialog && sc.progressDialog.isShowing()) {
                    Log.d(TAG, "dismiss sc.progressDialog:" + sc.progressDialog);                      
                    sc.progressDialog.dismiss();
                }
            	// display a dialog and let user to choose adn number
            	Bundle bundle = new Bundle();
            	bundle.putCharSequence("ADN1", sc.mSim1Number);
            	bundle.putCharSequence("ADN2", sc.mSim2Number);
                bundle.putCharSequence("Name1", sc.mSim1Name);
                bundle.putCharSequence("Name2", sc.mSim2Name);
            	TwelveKeyDialer dialer = (TwelveKeyDialer)sc.context;
                if(dialer.mIsForeground)
            	dialer.showSpecialSequenceDialog(dialer.ADN_DIALOG, bundle);
            }
        }
    }
}
