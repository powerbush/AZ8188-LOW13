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

package com.android.contacts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import android.accounts.Account;
import android.app.Service;
import android.content.AsyncQueryHandler;
//import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
//import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
//import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
//import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
//import android.provider.Contacts.People;
import android.provider.ContactsContract.Data;
//import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
//import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;//for usim
//import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.telephony.TelephonyManager;
import android.provider.Settings;

//import com.android.internal.telephony.TelephonyIntents;
import android.text.TextUtils;
import android.util.Log;
import android.os.ServiceManager;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.IccCard;
//import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.TelephonyIntents;
import android.content.BroadcastReceiver;
import com.android.contacts.ContactsUtils;
import android.provider.Telephony.SIMInfo;//gemini enhancement
import com.mediatek.telephony.PhoneNumberFormatUtilEx;
import com.mediatek.telephony.PhoneNumberFormattingTextWatcherEx;


public abstract class AbstractStartSIMService extends Service{
		
		private static final String TAG = "AbstractStartSIMService";
		private ImportAllSimContactsThread mThread1;
		private ImportAllSimContactsThread mThread2;
		protected QueryHandler mQueryHandler;
		protected Cursor mCursor = null;
		protected Cursor mCursor1 = null;
		protected Cursor mCursor2 = null;
		protected static final int QUERY_SIM_TOKEN = 0;
		protected static final int QUERY_SIM1_TOKEN = 1;
		protected static final int QUERY_SIM2_TOKEN = 2;
		
		protected static final int NAME_COLUMN = 0;
	    protected static final int NUMBER_COLUMN = 1;
	    protected static final int EMAIL_COLUMN = 2;
	    protected static final int ADDITIONAL_NUMBER_COLUMN = 3;
	    protected static final int GROUP_COLUMN = 4;
	    
	    private Handler mHandler;
	    private static int sSimStarted;
	    private static int sSim1Started;
	    private static int sSim2Started;
	    private static long mSimId = -1;
		public  static final String ACTION_PHB_LOAD_FINISHED = "com.android.contacts.ACTION_PHB_LOAD_FINISHED";
		public static boolean phb1_load_finished = false;
		public static boolean phb2_load_finished = false;

		// mtk80909 for speed dial
		private int mCurrentSimId;

	    private static final String[] COLUMN_NAMES = new String[] {
	        "name",
	        "number",
	        "emails",
	        "additionalNumber",	        
	        "groupIds"
	    };

		private static final boolean VERBOSE_LOGGING = true;
		
		// In order to prevent locking DB too long, 
		// set the max operation count 90 in a batch.
		private static final int MAX_OP_COUNT_IN_ONE_BATCH = 90;

		public void onCreate(Bundle icicle) {
			Log.i(TAG, "onCreate()");
		}
		
		public void onStart(Intent intent, int startId) {
			final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));

		if (VERBOSE_LOGGING) {
			Log.i(TAG,"In onStart " + intent + ", startId " + startId);
			}
	        if (intent == null || iTel == null) {
	        	Log.i(TAG,"iTel is " + iTel);
	            return; 
	        }

			Log.i(TAG, "this == " + this.toString());
			mHandler = new DeleteHandler();
		
			int slotId = intent.getIntExtra("which_sim", 0);
            ContactsUtils.setSimContactsReady(this, slotId, false);
			Log.i(TAG,"IN onStart slotId is "+slotId);
	        mQueryHandler = new QueryHandler(getContentResolver());
			// mtk80909 for Speed Dial
			mCurrentSimId = slotId;
			ContactsUtils.isServiceRunning[mCurrentSimId] = true;
			deleteSimContact(slotId);	//for sim contact
	        
	        
		}
		
		public void onDestroy() {
			super.onDestroy();
			Log.i(TAG,"onDestroy()");

			// mtk80909 for Speed Dial
			ContactsUtils.isServiceRunning[mCurrentSimId] = false;

		}
		
	    public IBinder onBind(Intent intent) {
	        return null;
	    }
		
  public void deleteSimContact(int slotId) {
	  List <SIMInfo> simInfoList = SIMInfo.getAllSIMList(AbstractStartSIMService.this);
	  Log.i(TAG,"deleteSimContact simInfoList is " + simInfoList);
	  final StringBuilder builderForSlot1 = new StringBuilder();
	  final StringBuilder builderForSlot2 = new StringBuilder();
	  if (simInfoList != null) {
			builderForSlot1.append("(-100");
			builderForSlot2.append("(-100");
			for (SIMInfo simInfo:simInfoList) {
				Log.i(TAG,"simInfo.mSimId is " + simInfo.mSimId);
				Log.i(TAG,"simInfo.mSlot is " + simInfo.mSlot);
					if (simInfo.mSlot == com.android.internal.telephony.Phone.GEMINI_SIM_1 || simInfo.mSlot == -1) {
						builderForSlot1.append(",");
						builderForSlot1.append(simInfo.mSimId);
					} 
					if (simInfo.mSlot == com.android.internal.telephony.Phone.GEMINI_SIM_2 || simInfo.mSlot == -1) {
						builderForSlot2.append(",");
						builderForSlot2.append(simInfo.mSimId);
					}
				}
			builderForSlot1.append(")");
			builderForSlot2.append(")");
			Log.i(TAG,"builderForSlot1 is " + builderForSlot1);
			Log.i(TAG,"builderForSlot2 is " + builderForSlot2);
	  }
    if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
      Log
          .i(TAG,
              "com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT");
      if (slotId == com.android.internal.telephony.Phone.GEMINI_SIM_1) {
        Log
            .i(TAG,
                "slotId == com.android.internal.telephony.Phone.GEMINI_SIM_1");
        new Thread(new Runnable() {

          public void run() {
        	  ContactsListActivity.s_deletingContacts = true;  ////
              ContactsListActivity.waitImportingSimThread();  ////  
            getContentResolver().delete(
                RawContacts.CONTENT_URI.buildUpon()
                    .appendQueryParameter("sim", "true")
                    .build(),
                RawContacts.INDICATE_PHONE_SIM + " IN " + builderForSlot1, null);
            Log.i(TAG, "After delete sim1");
            ContactsListActivity.s_deletingContacts = false;  ////
            ContactsUtils.setSim1Ready(0);  ////
            ContactsUtils.setSim1Start(0);  ////
            mHandler.sendEmptyMessage(com.android.internal.telephony.Phone.GEMINI_SIM_1);
          }

        }).start();

      } else {
        Log
            .i(TAG,
                "slotId == com.android.internal.telephony.Phone.GEMINI_SIM_2");
        new Thread(new Runnable() {

          public void run() {
        	  ContactsListActivity.s_deletingContacts = true;  ////
              ContactsListActivity.waitImportingSimThread();  ////  
            getContentResolver().delete(
                RawContacts.CONTENT_URI.buildUpon()
                    .appendQueryParameter("sim", "true")
                    .build(),
                RawContacts.INDICATE_PHONE_SIM + " IN " + builderForSlot2, null);
            Log.i(TAG, "After delete sim2");
            ContactsListActivity.s_deletingContacts = false;  ////
            ContactsUtils.setSim2Ready(0);  ////
            ContactsUtils.setSim2Start(0);  ////
            mHandler.sendEmptyMessage(com.android.internal.telephony.Phone.GEMINI_SIM_2);
          }

        }).start();

      }
    } else {
      Log.i(TAG, "slotId == single sim ");
      new Thread(new Runnable() {
        public void run() {
        	ContactsListActivity.s_deletingContacts = true;  ////
            ContactsListActivity.waitImportingSimThread();  ////    
          getContentResolver().delete(
              RawContacts.CONTENT_URI.buildUpon()
                  .appendQueryParameter("sim", "true")
                  .build(),
              RawContacts.INDICATE_PHONE_SIM + ">" + RawContacts.INDICATE_PHONE, null);
          ContactsListActivity.s_deletingContacts = false;  ////
          ContactsUtils.setSimReady(0);  ////
          ContactsUtils.setSimStart(0);  ////
          mHandler.sendEmptyMessage(com.android.internal.telephony.Phone.GEMINI_SIM_1);
        }

      }).start();

    }
  }

  private void query(int slotId) {
    final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
        .getService(Context.TELEPHONY_SERVICE));
    Uri uri1 = null;
    Uri uri2 = null;
    Uri uri = null;

    boolean simPUKReq = ContactsUtils.pukRequest();
    boolean sim1PUKReq = ContactsUtils.pukRequest(slotId);
    boolean sim2PUKReq = ContactsUtils.pukRequest(slotId);

    boolean simPINReq = ContactsUtils.pinRequest();
    boolean sim1PINReq = ContactsUtils.pinRequest(slotId);
    boolean sim2PINReq = ContactsUtils.pinRequest(slotId);

    boolean simReady = ContactsUtils.simStateReady();
    boolean sim1Ready = ContactsUtils.simStateReady(slotId);
    boolean sim2Ready = ContactsUtils.simStateReady(slotId);
    int DualSimSet = Settings.System.getInt(getContentResolver(),
        Settings.System.DUAL_SIM_MODE_SETTING, 3);
    boolean isRadioOn1 = (Settings.System.getInt(getContentResolver(),
        Settings.System.AIRPLANE_MODE_ON, 0) == 0)
        && ((1 == DualSimSet) || (3 == DualSimSet));
    boolean isRadioOn2 = (Settings.System.getInt(getContentResolver(),
        Settings.System.AIRPLANE_MODE_ON, 0) == 0)
        && ((2 == DualSimSet) || (3 == DualSimSet));
    Log.i(TAG, "slotId = " + slotId + " in query()");
    Log.i(TAG, "sim1PUKReq is " + sim1PUKReq + "  sim2PUKReq is "
        + sim2PUKReq + "   simPUKReq is " + simPUKReq);
    Log.i(TAG, "sim1Ready IS " + sim1Ready);
    Log.i(TAG, "sim2Ready IS " + sim2Ready);
    Log.i(TAG, "sim1PINReq is " + sim1PINReq + " sim2PINReq is "
        + sim2PINReq);
    Log.i(TAG, "isRadioOn1 is " + isRadioOn1 + " isRadioOn2 is "
        + isRadioOn2);
     if (iTel == null) {
	        	return;
	        }
    if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
      if (slotId == com.android.internal.telephony.Phone.GEMINI_SIM_1) {
							try {
								Log.i(TAG,"iTel.isRadioOnGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1) IS "+iTel.isRadioOnGemini(slotId));
								Log.i(TAG,"iTel.isFDNEnabledGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1) is "+iTel.isFDNEnabledGemini(slotId));
						
						if (sim1PUKReq || !isRadioOn1
								|| iTel.isFDNEnabledGemini(slotId)
								|| sim1PINReq /*|| !sim1Ready*/) {
              return;
            }
						if (iTel.getIccCardTypeGemini(slotId).equals("USIM")) {
							Log.i(TAG, "resolveUsim1Intent");
							uri1 = ContactsUtils.mIccUsim1Uri;
						} else {
        Log.i(TAG, "resolveIntent1");
						uri1 = ContactsUtils.mIccUri1;
      }
            } catch (Exception e) {
								Log.w(TAG, "com.android.internal.telephony.Phone.GEMINI_SIM_1 e.getMessage is "+ e.getMessage());
          }
        }
					if (slotId == com.android.internal.telephony.Phone.GEMINI_SIM_2) {
							try {
								Log.i(TAG,"iTel.isRadioOnGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2) IS "+iTel.isRadioOnGemini(slotId));
								Log.i(TAG,"iTel.isFDNEnabledGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2) is "+iTel.isFDNEnabledGemini(slotId));
							
	            		if (sim2PUKReq || !isRadioOn2
	            				|| iTel.isFDNEnabledGemini(slotId)
	            				|| sim2PINReq /*|| !sim2Ready*/) {
							return;
						}
	            		if (iTel.getIccCardTypeGemini(slotId).equals("USIM")) {
							Log.i(TAG, "resolveUsim2Intent");
							uri2 = ContactsUtils.mIccUsim2Uri;
						} else {
        Log.i(TAG, "resolveIntent2");
	            		uri2 = ContactsUtils.mIccUri2;
						}	
							}catch (Exception e)	 {
								Log.w(TAG, "com.android.internal.telephony.Phone.GEMINI_SIM_2 e.getMessage is "+ e.getMessage());
							}
	            		
      }
      if (VERBOSE_LOGGING) {
        Log.i(TAG, "query: starting an async query12");
      }
      if (uri1 != null) {
        Log.i(TAG, "query: starting an async query sim1");
        mQueryHandler.startQuery(QUERY_SIM1_TOKEN, null, uri1,
            COLUMN_NAMES, null, null, null);
      }
      if (uri2 != null) {
        Log.i(TAG, "query: starting an async query sim2");
	                	mQueryHandler.startQuery(QUERY_SIM2_TOKEN, null, uri2, COLUMN_NAMES, null, null, null);
	                }
			} else {
				try {
				if (simPUKReq || !iTel.isRadioOn() || iTel.isFDNEnabled() || simPINReq /*|| !simReady*/) {
            return;
				}
				
				if (iTel.getIccCardType().equals("USIM")) {
					Log.i(TAG, "resolveUsimIntent");
                	uri = ContactsUtils.mIccUsimUri;
				} else {
	        uri = ContactsUtils.mIccUri;
          }
        } catch (RemoteException ex) {
          ex.printStackTrace();
        }

      if (VERBOSE_LOGGING) {
        Log.i(TAG, "query: starting an async query");
      }
      if (uri != null) {
        mQueryHandler.startQuery(QUERY_SIM_TOKEN, null, uri,
            COLUMN_NAMES, null, null, null);
      }
    }

  }

  private class QueryHandler extends AsyncQueryHandler {
    public QueryHandler(ContentResolver cr) {
      super(cr);
    }
    protected void onQueryComplete(int token, Object cookie, Cursor c) {
      Log.i(TAG, "token = " + token + " in onQueryComplete");
      if (c != null) {
        if (VERBOSE_LOGGING) {
          Log.i(TAG, "onQueryComplete: cursor.count=" + c.getCount());
        }
        sSimStarted = ContactsUtils.getSimStart();
        Log.i(TAG, "sSimStarted is " + sSimStarted);
        sSim1Started = ContactsUtils.getSim1Start();
        Log.i(TAG, "sSim1Started is " + sSim1Started);
        sSim2Started = ContactsUtils.getSim2Start();
        Log.i(TAG, "sSim2Started is " + sSim2Started);

        if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {

          if (token == QUERY_SIM1_TOKEN && sSim1Started == 0) {
            Log.i(TAG, "QUERY_SIM1_TOKEN");
            mCursor1 = c;
            ContactsListActivity.s_importingSimContacts1 = true;   ////
            ContactsUtils.setSim1Start(1);
            mThread1 = new ImportAllSimContactsThread(
                com.android.internal.telephony.Phone.GEMINI_SIM_1);
            Log.i(TAG, "start sim1 importing thread");
            mThread1.start();
          } else if (token == QUERY_SIM2_TOKEN && sSim2Started == 0) {
            Log.i(TAG, "QUERY_SIM2_TOKEN");
            mCursor2 = c;
            ContactsListActivity.s_importingSimContacts2 = true;  ////
            ContactsUtils.setSim2Start(1);
            mThread2 = new ImportAllSimContactsThread(
                com.android.internal.telephony.Phone.GEMINI_SIM_2);
            Log.i(TAG, "start sim2 importing thread");
            mThread2.bIsSim1 = false;  ////
            mThread2.start();
          }
        } else if (sSimStarted == 0) {
          Log.i(TAG, "QUERY_SIM_TOKEN");
          mCursor = c;
          ContactsListActivity.s_importingSimContacts1 = true;  ////
          ContactsUtils.setSimStart(1);
          mThread1 = new ImportAllSimContactsThread(com.android.internal.telephony.Phone.GEMINI_SIM_1);
          mThread1.start();
        }

      } else {
        Log.i(TAG, "onQueryComplete if cursor null stopSelf");
		if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
		
				 if (token == QUERY_SIM1_TOKEN)	{
                    Intent  intent = new Intent(ACTION_PHB_LOAD_FINISHED);//
                    intent.putExtra("simId", 0);
                    AbstractStartSIMService.this.sendBroadcast(intent);
			        Log.d(TAG,"sendBroadcast sim 1 loading failed.");
				 	} else {
                    Intent  intent = new Intent(ACTION_PHB_LOAD_FINISHED);//
                    intent.putExtra("simId", 1);
                    AbstractStartSIMService.this.sendBroadcast(intent);
			        Log.d(TAG,"sendBroadcast sim 2 loading failed.");				 	
                    }
           }
        stopSelf();

      }

    }
  }

  private class ImportAllSimContactsThread extends Thread implements
      OnCancelListener, OnClickListener {
    public boolean mCanceled = false;
    int mSlot = 0;
    public boolean bIsSim1 = true;  ////
    
    public ImportAllSimContactsThread(int sim) {

      super("ImportAllSimContactsThread");

      mSlot = sim;
    }

    @Override
    public void run() {
      final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
          .getService(Context.TELEPHONY_SERVICE));
      final ContentValues emptyContentValues = new ContentValues();
      final ContentResolver resolver = getContentResolver();
      Log.i(TAG, "importing thread for SIM " + mSlot);
		    if (iTel == null) {
		    	return;
		    }
	    SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(AbstractStartSIMService.this, mSlot);
	    if (simInfo != null) {
	    	mSimId = simInfo.mSimId;
	    	Log.i(TAG,"*******SIMInfo.getSIMInfoBySlot mSlot is " + mSlot);
	    	Log.i(TAG,"********SIMInfo.getSIMInfoBySlot mSimId is " + mSimId);
	        }
		    
      if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
        if (mSlot == com.android.internal.telephony.Phone.GEMINI_SIM_1) {
          boolean sim1Ready = ContactsUtils.simStateReady(mSlot);
          if (mCursor1 != null) {
            Log.i(TAG, "begin insert sim1 contact");
            Log.i(TAG, "begin insert one sim1 contact "
                + mCursor1.getPosition());
            synchronized(ImportAllSimContactsThread.class)  ////
            {
            actuallyImportOneSimContact(mCursor1, resolver, mSlot, mSimId, this.bIsSim1);  ////
            }
				            try {
								Log.i(TAG,"In run iTel.isRadioOnGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1) IS "+iTel.isRadioOnGemini(mSlot));
								Log.i(TAG,"In run iTel.isFDNEnabledGemini(com.android.internal.telephony.Phone.GEMINI_SIM_1) is "+iTel.isFDNEnabledGemini(mSlot));
								if (iTel.isFDNEnabledGemini(mSlot) 
                    || !sim1Ready) {
                  ContactsUtils.setSim1Start(0);
                  ContactsListActivity.s_importingSimContacts1 = false;  ////
                  return;
                }
              } catch (Exception e) {
								Log.w(TAG, "mSim == 1 e.getMessage is "+ e.getMessage());
            }
            ContactsUtils.setSim1Ready(1);
            ContactsListActivity.s_importingSimContacts1 = false;  ////
		    Intent  intent = new Intent(ACTION_PHB_LOAD_FINISHED);//
            intent.putExtra("simId", 0);
            phb1_load_finished = true;
            ContactsUtils.setSimContactsReady(AbstractStartSIMService.this, 0, true);
            AbstractStartSIMService.this.sendBroadcast(intent);
			Log.d(TAG,"sendBroadcast sim 1 loading finished.");
            mCursor1 = null;
            Log.i(TAG, "end insert sim1 contact");
          }
        } else if (mSlot == com.android.internal.telephony.Phone.GEMINI_SIM_2) {
          boolean sim2Ready = ContactsUtils.simStateReady(mSlot);
          if (mCursor2 != null) {
            Log.i(TAG, "begin insert sim2 contact");
            Log.i(TAG, "begin insert one sim2 contact "
                + mCursor2.getPosition());
            synchronized(ImportAllSimContactsThread.class)  ////
            {
            actuallyImportOneSimContact(mCursor2, resolver, mSlot, mSimId, this.bIsSim1);  ////
            }
				            try {
								Log.i(TAG,"In run iTel.isRadioOnGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2) IS "+iTel.isRadioOnGemini(mSlot));
								Log.i(TAG,"In run iTel.isFDNEnabledGemini(com.android.internal.telephony.Phone.GEMINI_SIM_2) is "+iTel.isFDNEnabledGemini(mSlot));
								if (iTel.isFDNEnabledGemini(mSlot)
                    || !sim2Ready) {
                  ContactsUtils.setSim2Start(0);
                  ContactsListActivity.s_importingSimContacts2 = false;  ////
                  return;
                }
              } catch (Exception e) {
								Log.w(TAG, "mSim == 2 e.getMessage is "+ e.getMessage());
            }
            ContactsUtils.setSim2Ready(1);
            ContactsListActivity.s_importingSimContacts2 = false;  ////
		    Intent  intent = new Intent(ACTION_PHB_LOAD_FINISHED);//
            intent.putExtra("simId", 1);
            phb2_load_finished = true;
            ContactsUtils.setSimContactsReady(AbstractStartSIMService.this, 1, true);
            AbstractStartSIMService.this.sendBroadcast(intent);
			Log.d(TAG,"sendBroadcast sim 2 loading finished.");
            mCursor2 = null;
            Log.i(TAG, "end insert sim2 contact");
          }
        }
      } else {
        if (mSlot == 0) {
          boolean simReady = ContactsUtils.simStateReady();
          if (mCursor != null) {
            Log.i(TAG, "begin insert sim contact");
            Log.i(TAG, "begin insert one sim contact "
                + mCursor.getPosition());
            synchronized(ImportAllSimContactsThread.class)  ////
            {
            actuallyImportOneSimContact(mCursor, resolver, mSlot, mSimId, this.bIsSim1);  ////
            }
			            try {
							Log.i(TAG,"In run iTel.isRadioOn() IS "+iTel.isRadioOn());
							Log.i(TAG,"In run iTel.isFDNEnabled() is "+iTel.isFDNEnabled());
                if (iTel.isFDNEnabled() || !simReady) {
                  ContactsUtils.setSimStart(0);
                  ContactsListActivity.s_importingSimContacts1 = false;  ////
                  return;
                }
              } catch (Exception e) {
							Log.w(TAG, "mSim == 0 e.getMessage is "+ e.getMessage());
            }
            ContactsUtils.setSimReady(1);
            phb1_load_finished = true;
            ContactsListActivity.s_importingSimContacts1 = false;  ////            
            mCursor = null;
            Log.i(TAG, "end insert sim contact");
          }
        }
      }
      // if (mCursor == null || mCursor1 == null || mCursor2 == null) {
      Log.i(TAG, "Before stopSelf mCursor is " + mCursor
          + "\nmCursor1 is " + mCursor1 + "\nmCursor2 is" + mCursor2);
      stopSelf();
      Log.i(TAG, "After stopSelf ");
      // }
    }

    public void onCancel(DialogInterface dialog) {
      mCanceled = true;
    }

    public void onClick(DialogInterface arg0, int arg1) {
      // TODO Auto-generated method stub

    }
  }

  private static final String ACCOUNT_TYPE_GOOGLE = "com.google";
  private static final String GOOGLE_MY_CONTACTS_GROUP = "System Group: My Contacts";

  private static void actuallyImportOneSimContact(final Cursor cursor,
      final ContentResolver resolver, int slot, long simId, boolean bIsSim1) { ////

    final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
    int i = 0;
	        String additionalNumber = null;
	        boolean isSim1Usim = false;
	        boolean isSim2Usim = false;
    if (cursor != null) {
      while (cursor.moveToNext()) {
    	 
              if(ContactsListActivity.s_deletingContacts) 
    	      {    	
    			  break;    			  
    	      }
    	 
    
    	
	    	final ContactsUtils.NamePhoneTypePair namePhoneTypePair = new ContactsUtils.NamePhoneTypePair(cursor.getString(NAME_COLUMN));
        final String name = namePhoneTypePair.name;
        final int phoneType = namePhoneTypePair.phoneType;
	        Log.i(TAG,"phoneType is " + phoneType);
            final String phoneTypeSuffix = namePhoneTypePair.phoneTypeSuffix; // mtk80909 for ALPS00023212
            String phoneNumber = cursor.getString(NUMBER_COLUMN);
	        additionalNumber = cursor.getString(ADDITIONAL_NUMBER_COLUMN);	        
	        final String emailAddresses = cursor.getString(EMAIL_COLUMN);
	        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
	        if (iTel == null) {
	        	Log.i(TAG,"iTel is " + iTel);
	        	return;
	        }
	        Log.i(TAG,"In actuallyImportOneSimContact emailAddresses is "+emailAddresses);
	        final String[] emailAddressArray;
	        if (!TextUtils.isEmpty(emailAddresses)) {
	            emailAddressArray = emailAddresses.split(",");
	        } else {
	            emailAddressArray = null;
	        }
	        Log.i(TAG,"In actuallyImportOneSimContact emailAddressArray is "+emailAddressArray);

        int j = 0;
        ContentProviderOperation.Builder builder = ContentProviderOperation
            .newInsert(RawContacts.CONTENT_URI);
        j++;
//        int simType = -1;
//        SIMInfo simInfo = iTel.getSIMInfoBySlot(this, sim);
//        if (!simInfo.isEmpty()) {
//        	simType = simInfo.mSimId;
//        }
	        try {
        if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
          if (slot == com.android.internal.telephony.Phone.GEMINI_SIM_2) {
            Log.i(TAG, "sim == 2");
            Log.i(TAG,"******** actuallyImportOneSimContact slot is " + slot);
            Log.i(TAG,"********* actuallyImportOneSimContact simId is " + simId);
	        	if (iTel.getIccCardTypeGemini(slot).equals("USIM")) {
	        		isSim2Usim = true;
	        	} 	        	
          } else if (slot == com.android.internal.telephony.Phone.GEMINI_SIM_1) {
            Log.i(TAG, "sim == 1");
            Log.i(TAG,"******** actuallyImportOneSimContact slot is " + slot);
            Log.i(TAG,"********* actuallyImportOneSimContact simId is " + simId);
	        	if (iTel.getIccCardTypeGemini(slot).equals("USIM")) {
	        		isSim1Usim = true;
	        	}
          }
        } else {
          if (slot == 0) {
            Log.i(TAG, "sim == 0");
            Log.i(TAG,"******** actuallyImportOneSimContact slot is " + slot);
            Log.i(TAG,"********* actuallyImportOneSimContact simId is " + simId);
	        	if (iTel.getIccCardType().equals("USIM")) {
	        		isSim1Usim = true;
	        	  }
		        }
	          }
	        } catch (Exception e) {
	        	Log.i(TAG,"In actuallyImportOneSimContact e.getMessage is " + e.getMessage());
        }
        ContentValues values = new ContentValues();
        values.put(RawContacts.INDICATE_PHONE_SIM, simId);
        values.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED);
        builder.withValues(values);

        operationList.add(builder.build());
        // mtk80908 begin. Do not insert null number into table data
        if (!TextUtils.isEmpty(phoneNumber)) {
          phoneNumber = PhoneNumberFormatUtilEx.formatNumber(phoneNumber);
          builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
          builder.withValueBackReference(Phone.RAW_CONTACT_ID, i);
          builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
//          builder.withValue(Phone.TYPE, phoneType);
          builder.withValue(Data.DATA2, 2);
          builder.withValue(Phone.NUMBER, phoneNumber);
          // mtk80909 for ALPS00023212
          if (!TextUtils.isEmpty(phoneTypeSuffix)) {
            builder.withValue(Data.DATA15, phoneTypeSuffix);
          }
          operationList.add(builder.build());
          j++;
        }
        // mtk80908 end

	        
        if (!TextUtils.isEmpty(name)) {
	        builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
	        builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, i);
	        builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);	        
          builder.withValue(StructuredName.GIVEN_NAME, name);
        operationList.add(builder.build());
        j++;
	        }
	        
	        
	        //if USIM
		        if (isSim1Usim || isSim2Usim) {
		        	Log.i(TAG,"isSim1Usim is " + isSim1Usim + "isSim2Usim is" + isSim2Usim);
//		        	if (!TextUtils.isEmpty(emailAddressArray)) {
//			        	Log.i(TAG,"In actuallyImportOneSimContact emailAddressArray is " + emailAddressArray);
////			            for (String emailAddress : emailAddressArray) {
//			                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
//			                builder.withValueBackReference(Email.RAW_CONTACT_ID, i);
//			                builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
//			                builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
//			                builder.withValue(Email.DATA, emailAddressArray);
//			                operationList.add(builder.build());
//			                j++;
////			            }
//			        }
		        	
		            if (!TextUtils.isEmpty(emailAddresses)) {
		                for (String emailAddress : emailAddressArray) {
		                	Log.i(TAG,"&&&&&&&&&&&&emailAddress IS " + emailAddress);
		                	if(emailAddress!= null)
			                	Log.i(TAG,"&&&&&&&&&&&&emailAddress.length IS " + emailAddress.length());
		                	if (!TextUtils.isEmpty(emailAddress) && !emailAddress.equals("null")){
		                		Log.i(TAG,"emailAddress is not null emailAddress is " + emailAddress);
			                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
			                builder.withValueBackReference(Email.RAW_CONTACT_ID, i);
			                builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
			                builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
		                    builder.withValue(Email.DATA, emailAddress);
			                operationList.add(builder.build());
			                j++;
		                	}
		                }
			        }
		        	
		        	
		        	
		        if (!TextUtils.isEmpty(additionalNumber)) {
		        	additionalNumber = PhoneNumberFormatUtilEx.formatNumber(additionalNumber);
		            Log.i(TAG,"additionalNumber is " + additionalNumber);
		        	builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
			        builder.withValueBackReference(Phone.RAW_CONTACT_ID, i);
			        builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
//			        builder.withValue(Phone.TYPE, phoneType);	
			        builder.withValue(Data.DATA2, 7);
			        builder.withValue(Phone.NUMBER, additionalNumber);
			        builder.withValue(Data.IS_ADDITIONAL_NUMBER, 1);
			        operationList.add(builder.build());
			        j++;
			        }
		        } 
        i = i + j;
        if (i > MAX_OP_COUNT_IN_ONE_BATCH) {
      try {
        Log.i(TAG, "Before applyBatch ");
        resolver.applyBatch(ContactsContract.AUTHORITY, operationList);
        Log.i(TAG, "After applyBatch ");
      } catch (RemoteException e) {
	                Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
      } catch (OperationApplicationException e) {
	                Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));

	            }
	            i = 0;
	            operationList.clear();
	        }
	        }
	        try {
	        	Log.i(TAG,"Before applyBatch ");
	            resolver.applyBatch(ContactsContract.AUTHORITY, operationList); 
	            Log.i(TAG,"After applyBatch ");
	        } catch (RemoteException e) {
	            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
	        } catch (OperationApplicationException e) {
	            Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));

      }
      cursor.close();
    }
  }

  private class DeleteHandler extends Handler {

    public DeleteHandler() {
      super();
    }

    @Override
    public void handleMessage(Message msg) {
      Log.i(TAG, "launching query msg.what = " + msg.what
          + " by DeleteHandler");
      switch (msg.what) {
      case com.android.internal.telephony.Phone.GEMINI_SIM_1:
        query(com.android.internal.telephony.Phone.GEMINI_SIM_1);
        break;
      case com.android.internal.telephony.Phone.GEMINI_SIM_2:
        query(com.android.internal.telephony.Phone.GEMINI_SIM_2);
        break;
      }
    }
  }
	   
}
