package com.android.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.mediatek.featureoption.FeatureOption;

public class SimCardUtils {
    public static final String TAG = "SimCardUtils";

    public static interface SimSlot {
        public static final int SLOT_NONE = -1;
        public static final int SLOT_SINGLE = 0;
        public static final int SLOT_ID1 = com.android.internal.telephony.Phone.GEMINI_SIM_1;
        public static final int SLOT_ID2 = com.android.internal.telephony.Phone.GEMINI_SIM_2;
    }

    public static interface SimType {
        public static final String SIM_TYPE_USIM_TAG = "USIM";

        public static final int SIM_TYPE_SIM = 0;
        public static final int SIM_TYPE_USIM = 1;
    }
    
    public static class SimUri {
        public static final Uri mIccUri = Uri.parse("content://icc/adn/");   
        public static final Uri mIccUri1 = Uri.parse("content://icc/adn1/");
        public static final Uri mIccUri2 = Uri.parse("content://icc/adn2/");
        
        public static final Uri mIccUsimUri = Uri.parse("content://icc/pbr");
        public static final Uri mIccUsim1Uri = Uri.parse("content://icc/pbr1/");
        public static final Uri mIccUsim2Uri = Uri.parse("content://icc/pbr2/");
        
        public static Uri getSimUri(int slotId) {
            boolean isUsim = isSimUsimType(slotId);
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (slotId == 0) {
                    return isUsim ? mIccUsim1Uri : mIccUri1;
                } else {
                    return isUsim ? mIccUsim2Uri : mIccUri2;
                }
            } else {
                return isUsim ? mIccUsimUri : mIccUri;
            }
        }
    }
    
    public static boolean isSimPukRequest(int slotId) {
        boolean isPukRequest = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            isPukRequest = (TelephonyManager.SIM_STATE_PUK_REQUIRED == TelephonyManager
                    .getDefault().getSimStateGemini(slotId));
        } else {
            isPukRequest = (TelephonyManager.SIM_STATE_PUK_REQUIRED == TelephonyManager
                    .getDefault().getSimState());
        }
        return isPukRequest;
    }

    public static boolean isSimPinRequest(int slotId) {
        boolean isPinRequest = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            isPinRequest = (TelephonyManager.SIM_STATE_PIN_REQUIRED == TelephonyManager
                    .getDefault().getSimStateGemini(slotId));
        } else {
            isPinRequest = (TelephonyManager.SIM_STATE_PIN_REQUIRED == TelephonyManager
                    .getDefault().getSimState());
        }
        return isPinRequest;
    }

    public static boolean isSimStateReady(int slotId) {
        boolean isSimStateReady = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            isSimStateReady = (TelephonyManager.SIM_STATE_READY == TelephonyManager
                    .getDefault().getSimStateGemini(slotId));
        } else {
            isSimStateReady = (TelephonyManager.SIM_STATE_READY == TelephonyManager
                    .getDefault().getSimState());
        }
        return isSimStateReady;
    }
    
    public static boolean isSimInserted(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isSimInsert = false;
        try {
            if (iTel != null) {
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    isSimInsert = iTel.isSimInsert(slotId);
                } else {
                    isSimInsert = iTel.isSimInsert(0);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            isSimInsert = false;
        }
        return isSimInsert;
    }
    
    public static boolean isFdnEnabed(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isFdnEnabled = false;
        try {
            if (iTel != null) {
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    isFdnEnabled = iTel.isFDNEnabledGemini(slotId);
                } else {
                    isFdnEnabled = iTel.isFDNEnabled();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            isFdnEnabled = false;
        }
        return isFdnEnabled;
    }
    
    public static boolean isSetRadioOn(ContentResolver resolver, int slotId) {
        boolean isRadioOn = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            int dualSimSet = Settings.System.getInt(resolver,
                    Settings.System.DUAL_SIM_MODE_SETTING, 3);
            isRadioOn = (Settings.System.getInt(resolver,
                    Settings.System.AIRPLANE_MODE_ON, 0) == 0)
                    && ((slotId + 1 == dualSimSet) || (3 == dualSimSet));
        } else {
            isRadioOn = Settings.System.getInt(resolver,
                    Settings.System.AIRPLANE_MODE_ON, 0) == 0;
        }
        return isRadioOn;
    }
    
    /**
     * check PhoneBook State is ready if ready, then return true.
     * 
     * @param slotId
     * @return
     */
    public static boolean isPhoneBookReady(int slotId) {
        final ITelephony iPhb = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        if (null == iPhb) {
            Log.d(TAG, "checkPhoneBookState, iPhb == null");
            return false;
        }
        boolean isPbReady = false;
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                isPbReady = iPhb.isPhbReadyGemini(slotId);
                Log.d(TAG, "isPbReady:" + isPbReady + "||slotId:" + slotId);

            } else {
                isPbReady = iPhb.isPhbReady();
                Log.d(TAG, "isPbReady:" + isPbReady + "||slotId:" + slotId);
            }
        } catch (Exception e) {
            Log.w(TAG, "e.getMessage is " + e.getMessage());
        }
        return isPbReady;
    }
    
    public static int getSimTypeBySlot(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        int simType = SimType.SIM_TYPE_SIM;
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardTypeGemini(slotId)))
                    simType = SimType.SIM_TYPE_USIM;
            } else {
                if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardType()))
                    simType = SimType.SIM_TYPE_USIM;
            }
        } catch (Exception e) {
            Log.d(TAG, "catched exception.");
            e.printStackTrace();
        }
        return simType;
    }
    
    public static boolean isSimUsimType(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isUsim = false;
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardTypeGemini(slotId)))
                    isUsim = true;
            } else {
                if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardType()))
                    isUsim = true;
            }
        } catch (Exception e) {
            Log.d(TAG, "catched exception.");
            e.printStackTrace();
        }
        return isUsim;
    }
    
}
