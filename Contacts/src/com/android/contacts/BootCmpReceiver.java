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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.provider.Settings;
import android.os.ServiceManager;
import com.android.internal.telephony.ITelephony;
import com.mediatek.featureoption.FeatureOption;

public class BootCmpReceiver extends BroadcastReceiver {
	private static final String TAG = "BootCmpReceiver";
	private static Context mContext = null;
	private static int circle;
	private static int circle1;
	private static int circle2;
        private static boolean misPhbReady=false;
        private static boolean misAirplaneRec=false;
        private static boolean misDualSimModeRec=false;

  public void onReceive(Context context, Intent intent) {
    mContext = context;
    Log.i(TAG, "In onReceive ");
    final String action = intent.getAction();
    Log.i(TAG, "action is " + action);
    circle = ContactsUtils.getSimReady();
    Log.i(TAG, "circle is " + circle);
    circle1 = ContactsUtils.getSim1Ready();
    Log.i(TAG, "circle1 is " + circle1);
    circle2 = ContactsUtils.getSim2Ready();
    Log.i(TAG, "circle2 is " + circle2);
    Log.i(TAG, "onReceive, ***misPhbReady= " + misPhbReady
        + "; misAirplaneRec=" + misAirplaneRec + "; misDualSimModeRec="
        + misDualSimModeRec);
    if (action.equals(com.android.internal.telephony.TelephonyIntents.ACTION_PHB_STATE_CHANGED)) {
      Log.i(TAG, "iN action.equals(Intent.ACTION_PHB_STATE_CHANGED) ");
      misPhbReady = true;
      
    } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
      misAirplaneRec = true;
      Log.i(TAG, "iN action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED) ");

    } else if (action.equals(Intent.ACTION_DUAL_SIM_MODE_CHANGED)) {
      misDualSimModeRec = true;
      Log.i(TAG, "iN action.equals(Intent.ACTION_DUAL_SIM_MODE_CHANGED) ");
      
    }
    
    if (!misPhbReady) {
    	
		final ITelephony iPhb = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
		if (null == iPhb) {
            return;
		}
        boolean isPhoneBookReady = false;
        boolean isPhoneBookReady1 = false;
        boolean isPhoneBookReady2 = false;
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                isPhoneBookReady1 = iPhb.isPhbReadyGemini(0);
                isPhoneBookReady2 = iPhb.isPhbReadyGemini(1);
				Log.d(TAG, "onReceive,  isPhoneBookReady1= " + isPhoneBookReady1
						+ "; isPhoneBookReady2=" + isPhoneBookReady2);
				
            } else {
                isPhoneBookReady = iPhb.isPhbReady();
				Log.d(TAG, "onReceive, isPhoneBookReady=" + isPhoneBookReady );
            }        
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (isPhoneBookReady1 || isPhoneBookReady2) {
                    misPhbReady = true;
                }
        
            } else {
                if (isPhoneBookReady) {
                    misPhbReady = true;
                }
            }
        } catch(Exception e) {
            Log.w(TAG, "com.android.internal.telephony.IIccPhoneBook e.getMessage is "+ e.getMessage());
        }

    }

    Log.i(TAG, "onReceive, after receive signal, misPhbReady= " + misPhbReady
			+ "; misAirplaneRec=" + misAirplaneRec + "; misDualSimModeRec="
			+ misDualSimModeRec);

    if (misPhbReady && !misAirplaneRec && !misDualSimModeRec) {
      Log.i(TAG, "***Got phb ready, not airplane and dual Sim mode ");
      boolean ready = intent.getBooleanExtra("ready", false);
      int simId = intent.getIntExtra("simId", -10);
      if (ready) {
        if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
          if (simId == 0 && circle1 == 0) {
            Log.i(TAG, "simId is " + simId);
            sim1RadioOn();
          }
          if (simId == 1 && circle2 == 0) {
            Log.i(TAG, "simId is " + simId + " another");
            sim2RadioOn();
          }
        } else {
          if (simId == 0 && circle == 0) {
            Log.i(TAG, "simId is " + simId + "single sim mode");
            sim1RadioOn();
          }
        }
      }
    }

    else if (misPhbReady && misAirplaneRec) {

      Log.i(TAG, "***Got phb ready and airplane mode ");
      misAirplaneRec = false;
      if (Settings.System.getInt(context.getContentResolver(),
          Settings.System.AIRPLANE_MODE_ON, -1) == 0) {// airplanemode
        // off
        Log.i(TAG, "AIRPLANE_MODE_ON is 0");
        if (true == com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
          int i = Settings.System.getInt(
              context.getContentResolver(),
              Settings.System.DUAL_SIM_MODE_SETTING, -1);
          Log.i(TAG, "i is " + i);
          if (1 == Settings.System.getInt(context
              .getContentResolver(),
              Settings.System.DUAL_SIM_MODE_SETTING, -1)
              && circle1 == 0) {
            Log.i(TAG, "ACTION_AIRPLANE_MODE_CHANGED sim1 radio on");

            sim1RadioOn();

          } else if (2 == Settings.System.getInt(context
              .getContentResolver(),
              Settings.System.DUAL_SIM_MODE_SETTING, -1)
              && circle2 == 0) {
            Log.i(TAG, "ACTION_AIRPLANE_MODE_CHANGED sim2 radio on");
            sim2RadioOn();

          } else /*
               * if (3 ==
               * Settings.System.getInt(context.getContentResolver
               * (),Settings.System.DUAL_SIM_MODE_SETTING, -1))
               */{
            Log.i(TAG, "ACTION_AIRPLANE_MODE_CHANGED dual sim radio on");
            if (circle1 == 0) {
              sim1RadioOn();
            }
            if (circle2 == 0) {
              sim2RadioOn();
            }
          }
        } else if (circle == 0) {
          sim1RadioOn();
        }
      } else
        return;// airplanemode on

    } else if (misPhbReady && misDualSimModeRec) {
      Log.i(TAG, "****Got phb ready and dual sim mode");

      misDualSimModeRec = false;
      if (1 == Settings.System.getInt(context.getContentResolver(),
          Settings.System.DUAL_SIM_MODE_SETTING, -1)
          && circle1 == 0) {

        Log.i(TAG,"ACTION_DUAL_SIM_MODE_CHANGED sim1 radio on circle1 is " + circle1);
        sim1RadioOn();

      } else if (2 == Settings.System.getInt(
          context.getContentResolver(),
          Settings.System.DUAL_SIM_MODE_SETTING, -1)
          && circle2 == 0) {
        Log.i(TAG, "ACTION_DUAL_SIM_MODE_CHANGED sim2 radio on circle2 is " + circle2);
        sim2RadioOn();
      } else if (3 == Settings.System.getInt(
          context.getContentResolver(),
          Settings.System.DUAL_SIM_MODE_SETTING, -1)) {
        Log.i(TAG, "ACTION_DUAL_SIM_MODE_CHANGED dual sim radio on circle1 is " + circle1 + " circle2 is " + circle2);
        if (circle1 == 0) {
          sim1RadioOn();
        }
        if (circle2 == 0) {
          sim2RadioOn();
        }
      }

    }
    // else if
    // (action.equals("com.android.contacts.ACTION_SIM_STATE_CHANGED")) {
    // boolean pinRequested = intent.getBooleanExtra("pin_requested",
    // false);
    // boolean pukRequested = intent.getBooleanExtra("puk_requested",
    // false);
    // boolean networkLocked = intent.getBooleanExtra("network_locked",
    // false);
    // int simId = intent.getIntExtra("simId", -10);
    // Log.i(TAG,"pinRequested is "+pinRequested+" pukRequested is "+pukRequested+" networkLocked is "+networkLocked);
    // Log.i(TAG,"simId is "+simId+" circle1 is "+circle1+" circle2 is "+circle2+" circle is "+circle);
    // if (pinRequested || pukRequested || networkLocked) {
    // if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
    // if (simId == 0 && circle1 == 0) {
    // Log.i(TAG, "simId is " + simId);
    // sim1RadioOn();
    // }
    // if (simId == 1 && circle2 == 0) {
    // Log.i(TAG, "simId is " + simId + " another");
    // sim2RadioOn();
    // }
    // } else {
    // if (simId == 0 && circle == 0) {
    // Log.i(TAG, "simId is " + simId + "single sim mode");
    // sim1RadioOn();
    // }
    // }
    // }
    // }

  }

	public void sim1RadioOn() {
		Intent sim1radioonintent = new Intent(mContext, StartSIMService.class);
		sim1radioonintent.putExtra("which_sim", 0);
		mContext.startService(sim1radioonintent);

	}

	public void sim2RadioOn() {
		Intent sim2radioonintent = new Intent(mContext, StartSIMService2.class);
		sim2radioonintent.putExtra("which_sim", 1);
		mContext.startService(sim2radioonintent);

	}

}
