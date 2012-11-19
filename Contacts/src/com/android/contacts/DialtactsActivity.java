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
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Intents.UI;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.Window;
import android.widget.TabHost;
import android.os.SystemProperties;
import android.widget.TabWidget;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Typeface;

import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import com.mediatek.featureoption.FeatureOption;
import android.app.StatusBarManager;
import android.content.Context;
import android.provider.Settings;


/**
 * The dialer activity that has one tab with the virtual 12key
 * dialer, a tab with recent calls in it, a tab with the contacts and
 * a tab with the favorite. This is the container and the tabs are
 * embedded using intents.
 * The dialer tab's title is 'phone', a more common name (see strings.xml).
 */
public class DialtactsActivity extends TabActivity implements TabHost.OnTabChangeListener {
    private static final String TAG = "Dailtacts";
    private static final String FAVORITES_ENTRY_COMPONENT =
            "com.android.contacts.DialtactsFavoritesEntryActivity";

    private static final int TAB_INDEX_UNKNOWN = -1;
    private static final int TAB_INDEX_DIALER = 0;
    private static final int TAB_INDEX_CALL_LOG = 1;
    private static final int TAB_INDEX_CONTACTS = 2;
    private static final int TAB_INDEX_FAVORITES = 3;

    private static final String KEY_TAB_INDEX = "current_tab";
    private static final String KEY_LANGUAGE = "language";

    static final String EXTRA_IGNORE_STATE = "ignore-state";
    private final BroadcastReceiver mReceiver = new SimIndicatorBroadcastReceiver();

    /** If true, when handling the contacts intent the favorites tab will be shown instead */
    private static final String PREF_FAVORITES_AS_CONTACTS = "favorites_as_contacts";
    private static final boolean PREF_FAVORITES_AS_CONTACTS_DEFAULT = false;

    private TabHost mTabHost;
    private String mFilterText;
    private Uri mDialUri;
    private LayoutInflater inflater;
    private TextView[] tabLabel = new TextView[4];
    private StatusBarManager mStatusBarMgr;
    boolean mShowSimIndicator = false;    

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.w(TAG, "-------------------------------------onCreate()");
        final Intent intent = getIntent();
        String optr = SystemProperties.get("ro.operator.optr");
        if (null != optr && optr.equals("OP02"))
        {
            fixIntent(intent);		
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(R.layout.dialer_activity);

        mTabHost = getTabHost();
        mTabHost.setOnTabChangedListener(this);

        TabWidget tabWidget = mTabHost.getTabWidget();
        
        tabWidget.setBackgroundColor(0xFF202020);

        inflater = LayoutInflater.from(this);
        // Setup the tabs
        setupDialerTab();
        setupCallLogTab();
        setupContactsTab();
        setupFavoritesTab();

        int tabIndex = checkForLanguageChange(icicle);
        if(tabIndex != TAB_INDEX_UNKNOWN)
            mTabHost.setCurrentTab(tabIndex);
        else
            setCurrentTab(intent);

        if (intent.getAction().equals(UI.FILTER_CONTACTS_ACTION)
                && icicle == null) {
            setupFilterText(intent);
        }
        mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            IntentFilter intentFilter =
                new IntentFilter(Intent.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
            registerReceiver(mReceiver, intentFilter);   
        }        
    }

    int checkForLanguageChange(Bundle icicle) {
        Log.d(TAG, "checkForLanguageChange");
        int retval = TAB_INDEX_UNKNOWN;
        if(icicle != null) {
            int tabIndex = icicle.getInt(KEY_TAB_INDEX);
            String oldLanguage = icicle.getString(KEY_LANGUAGE);
            String newLanguage = getResources().getConfiguration().locale.getLanguage();
            Log.d(TAG, "tabIndex = "+tabIndex+" oldLanguage = "+oldLanguage+", new language = "+newLanguage);
            if(oldLanguage != null && !oldLanguage.equals(newLanguage))
                retval = tabIndex;
        }
        return retval;
    }

    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        String language = getResources().getConfiguration().locale.getLanguage();
        Log.d(TAG, "language = "+language);
        outState.putString(KEY_LANGUAGE, language);
        outState.putInt(KEY_TAB_INDEX, mTabHost.getCurrentTab());
    }

  //add layout wgao 0503
    public void setContentView(int viewID){
//	   super.setContentView(R.layout.dialer_activity);
	   TabHost host = new TabHost(this){
		   @Override
		    public boolean dispatchKeyEvent(KeyEvent event) {
		        final boolean handled = super.dispatchKeyEvent(event);
		        View currentView = getFocusedChild();
		        TabWidget tabWidget = getTabWidget();
		        // unhandled key ups change focus to tab indicator for embedded activities
		        // when there is nothing that will take focus from default focus searching
		        if (!handled
		                && (event.getAction() == KeyEvent.ACTION_DOWN)
		                && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN)) {
		        	if (currentView.findFocus().focusSearch(View.FOCUS_DOWN) == null && tabWidget.getChildTabViewAt(mCurrentTab) != null){
		        	    tabWidget.getChildTabViewAt(mCurrentTab).requestFocus();
		                playSoundEffect(SoundEffectConstants.NAVIGATION_UP);
		                return true;
		        	}
		        }
		        return handled;
		    }
	   };
	   host.setId(com.android.internal.R.id.tabhost);
	   inflater = LayoutInflater.from(this);
	   host.addView(inflater.inflate(R.layout.dialer_activity, null));
	   setContentView(host);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w(TAG, "-------------------------------------onPause()");
        final int currentTabIndex = mTabHost.getCurrentTab();
        final SharedPreferences.Editor editor =
                getSharedPreferences(StickyTabs.PREFERENCES_NAME, MODE_PRIVATE).edit();
        if (currentTabIndex == TAB_INDEX_CONTACTS || currentTabIndex == TAB_INDEX_FAVORITES) {
            editor.putBoolean(PREF_FAVORITES_AS_CONTACTS, currentTabIndex == TAB_INDEX_FAVORITES);
        }

        try {
        editor.apply();
        } catch (OutOfMemoryError e) {
        	Log.w(TAG, "onPause(): We are out of memory, the preference editor won't commit.");
    }
        
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            Log.i(TAG, "onPause(), mShowSimIndicator = false ");
            
            setSimIndicatorVisibility(false);
            mShowSimIndicator = false;
        }
    }

    private void fixIntent(Intent intent) {
        // This should be cleaned up: the call key used to send an Intent
        // that just said to go to the recent calls list.  It now sends this
        // abstract action, but this class hasn't been rewritten to deal with it.
        if (Intent.ACTION_CALL_BUTTON.equals(intent.getAction())) {
            intent.setDataAndType(Calls.CONTENT_URI, Calls.CONTENT_TYPE);
            intent.putExtra("call_key", true);
            setIntent(intent);
        }
    }

    private void setupCallLogTab() {
        // Force the class since overriding tab entries doesn't work
        Intent intent = new Intent("com.android.phone.action.RECENT_CALLS");
        intent.setClass(this, RecentCallsListActivity.class);
        StickyTabs.setTab(intent, TAB_INDEX_CALL_LOG);
        View view = inflater.inflate(R.layout.tab_short, null);
        ImageView tabImage = (ImageView)view.findViewById(R.id.tab_icon);
        tabImage.setImageResource(R.drawable.ic_tab_recent);
        tabLabel[1] = (TextView)view.findViewById(R.id.tab_label);
        tabLabel[1].setText(R.string.recentCallsIconLabel);
        mTabHost.addTab(mTabHost.newTabSpec("call_log")
//              .setIndicator(getString(R.string.recentCallsIconLabel),
//                        getResources().getDrawable(R.drawable.ic_tab_recent))
                .setIndicator(view)
                .setContent(intent), false);
    }

    private void setupDialerTab() {
        Intent intent = new Intent("com.android.phone.action.TOUCH_DIALER");
        intent.setClass(this, TwelveKeyDialer.class);
        StickyTabs.setTab(intent, TAB_INDEX_DIALER);
        View view = inflater.inflate(R.layout.tab_short, null);
        ImageView tabImage = (ImageView)view.findViewById(R.id.tab_icon);
        tabImage.setImageResource(R.drawable.ic_tab_dialer);
        tabLabel[0] = (TextView)view.findViewById(R.id.tab_label);
        tabLabel[0].setText(R.string.dialerIconLabel);
        mTabHost.addTab(mTabHost.newTabSpec("dialer")
//                .setIndicator(getString(R.string.dialerIconLabel),
//                        getResources().getDrawable(R.drawable.ic_tab_dialer))
                .setIndicator(view)
                .setContent(intent), false);
    }

    private void setupContactsTab() {
        Intent intent = new Intent(UI.LIST_DEFAULT);
        intent.setClass(this, ContactsListActivity.class);
        StickyTabs.setTab(intent, TAB_INDEX_CONTACTS);
        View view = inflater.inflate(R.layout.tab_short, null);
        ImageView tabImage = (ImageView)view.findViewById(R.id.tab_icon);
        tabImage.setImageResource(R.drawable.ic_tab_contacts);
        tabLabel[2] = (TextView)view.findViewById(R.id.tab_label);
        tabLabel[2].setText(R.string.contactsIconLabel);
        mTabHost.addTab(mTabHost.newTabSpec("contacts")
//              .setIndicator(getString(R.string.contactsIconLabel)),
//              getResources().getDrawable(R.drawable.ic_tab_contacts))
                .setIndicator(view)
                .setContent(intent), false);
    }
    
    private void setupFavoritesTab() {
        //Intent intent = new Intent(UI.LIST_STREQUENT_ACTION);
        //intent.setClass(this, ContactsListActivity.class);
    	
    	//Intent intent=new Intent(DialtactsActivity.this, GalleryContactPhoneActivity.class);
    	Intent intent=new Intent(DialtactsActivity.this, GalleryEmergencyPhoneActivity.class);
        
    	StickyTabs.setTab(intent, TAB_INDEX_FAVORITES);
        View view = inflater.inflate(R.layout.tab_short, null);
        ImageView tabImage =(ImageView)view.findViewById(R.id.tab_icon);
        tabImage.setImageResource(R.drawable.ic_tab_starred);
        tabLabel[3] = (TextView)view.findViewById(R.id.tab_label);
        tabLabel[3].setText(R.string.contactsFavoritesLabel);
        mTabHost.addTab(mTabHost.newTabSpec("favorites")
//              .setIndicator(getString(R.string.contactsFavoritesLabel),
//              getResources().getDrawable(R.drawable.starred))
                .setIndicator(view)
                .setContent(intent), false);
    }

    /**
     * Sets the current tab based on the intent's request type
     *
     * @param intent Intent that contains information about which tab should be selected
     */
    private void setCurrentTab(Intent intent) {
        // Dismiss menu provided by any children activities
        Activity activity = getLocalActivityManager().
                getActivity(mTabHost.getCurrentTabTag());
        if (activity != null) {
            activity.closeOptionsMenu();
        }

        // Tell the children activities that they should ignore any possible saved
        // state and instead reload their state from the parent's intent
        intent.putExtra(EXTRA_IGNORE_STATE, true);

        // Choose the tab based on the inbound intent
        String componentName = intent.getComponent().getClassName();
        if ("com.android.contacts.DialtactsCallLogEntryActivity".equals(componentName)) {
            mTabHost.setCurrentTab(TAB_INDEX_CALL_LOG);
        } else if (getClass().getName().equals(componentName)) {
            if (phoneIsInUse()) {
                // We are in a call, show the dialer tab (which allows going back to the call)
            	// but if user wants to show calllog, we show calllog tab
            	if( Calls.CONTENT_TYPE.equals(intent.getType()))
            		mTabHost.setCurrentTab(TAB_INDEX_CALL_LOG);
            	else
            		mTabHost.setCurrentTab(TAB_INDEX_DIALER);
            } else if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
                // launched from history (long-press home) --> nothing to change
            } else if (isDialIntent(intent)) {
                // The dialer was explicitly requested
                mTabHost.setCurrentTab(TAB_INDEX_DIALER);
            } else if (Calls.CONTENT_TYPE.equals(intent.getType())) {
                // After a call, show the call log
                Log.d(TAG, "setCurrentTab() goto call log tap");
                mTabHost.setCurrentTab(TAB_INDEX_CALL_LOG);
            } else {
                // Load the last tab used to make a phone call. default to the dialer in
                // first launch
                mTabHost.setCurrentTab(StickyTabs.loadTab(this, TAB_INDEX_DIALER));
            }
        } else if (FAVORITES_ENTRY_COMPONENT.equals(componentName)) {
            mTabHost.setCurrentTab(TAB_INDEX_FAVORITES);
        } else {
            // Launched as "Contacts" --> Go either to favorites or contacts, whichever is more
            // recent
            final SharedPreferences prefs = getSharedPreferences(StickyTabs.PREFERENCES_NAME,
                    MODE_PRIVATE);
            final boolean favoritesAsContacts = prefs.getBoolean(PREF_FAVORITES_AS_CONTACTS,
                    PREF_FAVORITES_AS_CONTACTS_DEFAULT);
            if (favoritesAsContacts) {
                mTabHost.setCurrentTab(TAB_INDEX_FAVORITES);
            } else {
                mTabHost.setCurrentTab(TAB_INDEX_CONTACTS);
            }
        }

        // Tell the children activities that they should honor their saved states
        // instead of the state from the parent's intent
        intent.putExtra(EXTRA_IGNORE_STATE, false);
    }

    @Override
    public void onNewIntent(Intent newIntent) {
        setIntent(newIntent);
        String optr = SystemProperties.get("ro.operator.optr");
        if (null != optr && optr.equals("OP02"))
        {
            fixIntent(newIntent);
        }
        setCurrentTab(newIntent);
        final String action = newIntent.getAction();
        if (action.equals(UI.FILTER_CONTACTS_ACTION)) {
            setupFilterText(newIntent);
        } else if (isDialIntent(newIntent)) {
            setupDialUri(newIntent);
        }
        
        //Comment out by mtk80908. Avoid to set SIM indicatorVisibility twice.
        //if (FeatureOption.MTK_GEMINI_SUPPORT) {
        //    Log.i(TAG, "onNewIntent(), mShowSimIndicator = true ");
        //   setSimIndicatorVisibility(true);
        //   mShowSimIndicator = true;
        //}        
        
    }

    /** Returns true if the given intent contains a phone number to populate the dialer with */
    private boolean isDialIntent(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_DIAL.equals(action)) {
            return true;
        }
        if (Intent.ACTION_VIEW.equals(action)) {
            final Uri data = intent.getData();
            if (data != null && "tel".equals(data.getScheme())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the filter text stored in {@link #setupFilterText(Intent)}.
     * This text originally came from a FILTER_CONTACTS_ACTION intent received
     * by this activity. The stored text will then be cleared after after this
     * method returns.
     *
     * @return The stored filter text
     */
    public String getAndClearFilterText() {
        String filterText = mFilterText;
        mFilterText = null;
        return filterText;
    }

    /**
     * Stores the filter text associated with a FILTER_CONTACTS_ACTION intent.
     * This is so child activities can check if they are supposed to display a filter.
     *
     * @param intent The intent received in {@link #onNewIntent(Intent)}
     */
    private void setupFilterText(Intent intent) {
        // If the intent was relaunched from history, don't apply the filter text.
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            return;
        }
        String filter = intent.getStringExtra(UI.FILTER_TEXT_EXTRA_KEY);
        if (filter != null && filter.length() > 0) {
            mFilterText = filter;
        }
    }

    /**
     * Retrieves the uri stored in {@link #setupDialUri(Intent)}. This uri
     * originally came from a dial intent received by this activity. The stored
     * uri will then be cleared after after this method returns.
     *
     * @return The stored uri
     */
    public Uri getAndClearDialUri() {
        Uri dialUri = mDialUri;
        mDialUri = null;
        return dialUri;
    }

    /**
     * Stores the uri associated with a dial intent. This is so child activities can
     * check if they are supposed to display new dial info.
     *
     * @param intent The intent received in {@link #onNewIntent(Intent)}
     */
    private void setupDialUri(Intent intent) {
        // If the intent was relaunched from history, don't reapply the intent.
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            return;
        }
        mDialUri = intent.getData();
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            // Instead of stopping, simply push this to the back of the stack.
            // This is only done when running at the top of the stack;
            // otherwise, we have been launched by someone else so need to
            // allow the user to go back to the caller.
            moveTaskToBack(false);
        } else {
            super.onBackPressed();
        }
    }

    /** {@inheritDoc} */
    //@Override
    public void onTabChanged(String tabId) {
        // Because we're using Activities as our tab children, we trigger
        // onWindowFocusChanged() to let them know when they're active.  This may
        // seem to duplicate the purpose of onResume(), but it's needed because
        // onResume() can't reliably check if a keyguard is active.
        Activity activity = getLocalActivityManager().getActivity(tabId);
        if (activity != null) {
            activity.onWindowFocusChanged(true);
        }
        setTabsLabelColor(tabId);
    }
    
    private void setTabsLabelColor(String tabId) {
        if ("dialer".equals(tabId)) {
            for (int i = 0; i < 4; i++) {
                if (i == 0) {
                    tabLabel[i].setTextColor(getResources().getColor(R.color.tab_label_select));
                    tabLabel[i].setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    tabLabel[i].setTextColor(getResources().getColor(R.color.tab_label_unselect));
                    tabLabel[i].setTypeface(Typeface.DEFAULT);
                }
            }

        } else if ("call_log".equals(tabId)) {
            for (int i = 0; i < 4; i++) {
                if (i == 1) {
                    tabLabel[i].setTextColor(getResources().getColor(R.color.tab_label_select));
                    tabLabel[i].setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    tabLabel[i].setTextColor(getResources().getColor(R.color.tab_label_unselect));
                    tabLabel[i].setTypeface(Typeface.DEFAULT);
                }
            }

        } else if ("contacts".equals(tabId)) {
            for (int i = 0; i < 4; i++) {
                if (i == 2) {
                    tabLabel[i].setTextColor(getResources().getColor(R.color.tab_label_select));
                    tabLabel[i].setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    tabLabel[i].setTextColor(getResources().getColor(R.color.tab_label_unselect));
                    tabLabel[i].setTypeface(Typeface.DEFAULT);
                }
            }
        } else if ("favorites".equals(tabId)) {
            for (int i = 0; i < 4; i++) {
                if (i == 3) {
                    tabLabel[i].setTextColor(getResources().getColor(R.color.tab_label_select));
                    tabLabel[i].setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    tabLabel[i].setTextColor(getResources().getColor(R.color.tab_label_unselect));
                    tabLabel[i].setTypeface(Typeface.DEFAULT);
                }
            }
        }
    }

    /**
     * @return true if the phone is "in use", meaning that at least one line
     *              is active (ie. off hook or ringing or dialing).
     */
    private boolean phoneIsInUse() {
        boolean phoneInUse = false;
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) phoneInUse = !phone.isIdle();
        } catch (RemoteException e) {
            Log.w(TAG, "phone.isIdle() failed", e);
        }
        return phoneInUse;
    }

    void setSimIndicatorVisibility(boolean visible) {
        if(visible)
            mStatusBarMgr.showSIMIndicator(getComponentName(), Settings.System.VOICE_CALL_SIM_SETTING);
        else
            mStatusBarMgr.hideSIMIndicator(getComponentName());
    }    

    private class SimIndicatorBroadcastReceiver extends BroadcastReceiver {
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
    }    

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy() ");
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
          unregisterReceiver(mReceiver);
        }
    }    
}
