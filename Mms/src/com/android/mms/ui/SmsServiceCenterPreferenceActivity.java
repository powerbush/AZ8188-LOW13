package com.android.mms.ui;

import static android.content.res.Configuration.KEYBOARDHIDDEN_NO;

import java.util.List;

import com.android.mms.R;
import com.android.mms.ui.AdvancedEditorPreference.GetSimInfo;

import android.R.color;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.Phone;
import android.provider.Telephony;
import android.provider.Telephony.SIMInfo;
import com.mediatek.telephony.TelephonyManagerEx;
import com.android.mms.ui.MessagingPreferenceActivity;

public class SmsServiceCenterPreferenceActivity extends PreferenceActivity implements GetSimInfo{
	private static final String TAG = "SmsServiceCenterPreferenceActivity";
    
	private AdvancedEditorPreference mSim1;
    private AdvancedEditorPreference mSim2;
    private AdvancedEditorPreference mSim3;
    private AdvancedEditorPreference mSim4;
    
    private String mSim1Number;
    private String mSim2Number;
    private String mSim3Number;
    private String mSim4Number;
    
    private int simCount;
	
	private int currentSim = -1;
	private TelephonyManagerEx mTelephonyManager;
	private EditText mNumberText;
	private AlertDialog mNumberTextDialog;
	private List<SIMInfo> listSimInfo;
	String intentPreference;
    private static Handler mSMSHandler = new Handler();
    private final int MAX_EDITABLE_LENGTH = 20;
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    	listSimInfo = SIMInfo.getInsertedSIMList(this);
        simCount = listSimInfo.size();
        
        AdvancedEditorPreference.init(this, simCount);
        addPreferencesFromResource(R.xml.multicardeditorpreference);    
        Intent intent = getIntent();
        intentPreference = intent.getStringExtra("preference");
        //translate key to SIM-related key;
        Log.i("SmsServiceCenterPreferenceActivity, getIntent:", intent.toString());
        Log.i("SmsServiceCenterPreferenceActivity, getpreference:", intentPreference);
        changeMultiCardKeyToSimRelated(intentPreference);
    }
    private void changeMultiCardKeyToSimRelated(String preference) {

        mSim1 = (AdvancedEditorPreference) findPreference("pref_key_sim1");
        mSim2 = (AdvancedEditorPreference) findPreference("pref_key_sim2");
        mSim3 = (AdvancedEditorPreference) findPreference("pref_key_sim3");
        mSim4 = (AdvancedEditorPreference) findPreference("pref_key_sim4");
        //get the stored value
    	SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
    	if (simCount == 1) {
    		getPreferenceScreen().removePreference(mSim2);
    		getPreferenceScreen().removePreference(mSim3);
    		getPreferenceScreen().removePreference(mSim4);   		
    	} else if (simCount == 2) {
    		getPreferenceScreen().removePreference(mSim3);
    		getPreferenceScreen().removePreference(mSim4);   
    	} else if (simCount == 3) {
    	    getPreferenceScreen().removePreference(mSim4);
    	}
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
    	if (preference == mSim1 && intentPreference.equals(MessagingPreferenceActivity.SMS_MANAGE_SIM_MESSAGES)) {
        	Intent it = new Intent();
        	int slotId = listSimInfo.get(0).mSlot;
        	 if (slotId == 0) {
        		 it.setClass(this, ManageSimMessages.class);
        	 } else if (slotId == 1) {
        		 it.setClass(this, ManageSim2Messages.class);
        	 }
        	it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	//it.putExtra("slotId", 0);
        	startActivity(it);
	    } else if (preference == mSim2 && intentPreference.equals(MessagingPreferenceActivity.SMS_MANAGE_SIM_MESSAGES)) {
        	Intent it = new Intent();
        	int slotId = listSimInfo.get(1).mSlot;
       	    if (slotId == 0) {
       		    it.setClass(this, ManageSimMessages.class);
       	    } else if (slotId == 1) {
       		    it.setClass(this, ManageSim2Messages.class);
       	    }
        	it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	//it.putExtra("slotId", 1);
        	startActivity(it);
	    } else if (preference == mSim1) {
        	//mSim1.setKey(Long.toString(listSimInfo.get(0).mSimId) + "_" + preference);
        	currentSim = listSimInfo.get(0).mSlot;
        	mNumberText = new EditText(this);
        	mNumberText.setHint(R.string.type_to_compose_text_enter_to_send);
        	mNumberText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_EDITABLE_LENGTH)});
        	//mNumberText.setKeyListener(new DigitsKeyListener(false, true));
        	mNumberText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_CLASS_PHONE);
        	mNumberText.computeScroll();
        	Log.i(TAG, "currentSlot is: " + currentSim);
        	String scNumber = getServiceCenter(currentSim);
        	Log.i(TAG, "getScNumber is: " + scNumber);
        	mNumberText.setText(scNumber);
        	mNumberTextDialog = new AlertDialog.Builder(this)
        	.setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.sms_service_center)
            .setView(mNumberText)
            .setPositiveButton(R.string.OK, new PositiveButtonListener())
            .setNegativeButton(R.string.Cancel, new NegativeButtonListener())
            .show();
        } else if (preference == mSim2) {
        	currentSim = listSimInfo.get(1).mSlot;
        	//mSim2.setKey(Long.toString(listSimInfo.get(1).mSimId) + "_" + preference);
        	mNumberText = new EditText(this);
        	mNumberText.setHint(R.string.type_to_compose_text_enter_to_send);
        	mNumberText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_EDITABLE_LENGTH)});
        	//mNumberText.setKeyListener(new DigitsKeyListener(false, true));
        	mNumberText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_CLASS_PHONE);
        	mNumberText.computeScroll();
        	Log.i(TAG, "currentSlot is: " + currentSim);
        	String scNumber = getServiceCenter(currentSim);
        	Log.i(TAG, "getScNumber is: " + scNumber);
        	mNumberText.setText(scNumber);
        	mNumberTextDialog = new AlertDialog.Builder(this)
        	.setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.sms_service_center)
            .setView(mNumberText)
            .setPositiveButton(R.string.OK, new PositiveButtonListener())
            .setNegativeButton(R.string.Cancel, new NegativeButtonListener())
            .show();
        } else if (preference == mSim3) {
        	currentSim = listSimInfo.get(2).mSlot;
        	//mSim3.setKey(Long.toString(listSimInfo.get(2).mSimId) + "_" + preference);
        	mNumberText = new EditText(this);
        	mNumberText.setHint(R.string.type_to_compose_text_enter_to_send);
        	mNumberText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_EDITABLE_LENGTH)});
        	//mNumberText.setKeyListener(new DigitsKeyListener(false, true));
        	mNumberText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_CLASS_PHONE);
        	mNumberText.computeScroll();
        	mNumberText.setText(getServiceCenter(currentSim));
        	mNumberTextDialog = new AlertDialog.Builder(this)
        	.setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.sms_service_center)
            .setView(mNumberText)
            .setPositiveButton(R.string.OK, new PositiveButtonListener())
            .setNegativeButton(R.string.Cancel, new NegativeButtonListener())
            .show();
        } else if (preference == mSim4) {
        	currentSim = listSimInfo.get(3).mSlot;
        	//mSim4.setKey(Long.toString(listSimInfo.get(3).mSimId) + "_" + preference);
        	mNumberText = new EditText(this);
        	mNumberText.setHint(R.string.type_to_compose_text_enter_to_send);
        	mNumberText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_EDITABLE_LENGTH)});
        	//mNumberText.setKeyListener(new DigitsKeyListener(false, true));
        	mNumberText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_CLASS_PHONE);
        	mNumberText.computeScroll();
        	mNumberText.setText(getServiceCenter(currentSim));
        	mNumberTextDialog = new AlertDialog.Builder(this)
        	.setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.sms_service_center)
            .setView(mNumberText)
            .setPositiveButton(R.string.OK, new PositiveButtonListener())
            .setNegativeButton(R.string.Cancel, new NegativeButtonListener())
            .show();
        }
        
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    public String getSimName(int id) {
    	return listSimInfo.get(id).mDisplayName;
    }

    
    public String getSimNumber(int id) {
    	return listSimInfo.get(id).mNumber;
    }
    
    public int getSimColor(int id) {
    	return listSimInfo.get(id).mSimBackgroundRes;
    }
    
    public int getNumberFormat(int id) {
    	return listSimInfo.get(id).mDispalyNumberFormat;
    }
    
    public int getSimStatus(int id) {
    	mTelephonyManager = TelephonyManagerEx.getDefault();
    	//int slotId = SIMInfo.getSlotById(this,listSimInfo.get(id).mSimId);
    	int slotId = listSimInfo.get(id).mSlot;
    	if (slotId != -1) {
    		return mTelephonyManager.getSimIndicatorStateGemini(slotId);
    	}
    	return -1;
    }
    
    public boolean is3G(int id)	{
    	mTelephonyManager = TelephonyManagerEx.getDefault();
    	//int slotId = SIMInfo.getSlotById(this, listSimInfo.get(id).mSimId);
    	int slotId = listSimInfo.get(id).mSlot;
    	Log.i(TAG, "SIMInfo.getSlotById id: " + id + " slotId: " + slotId);
    	if (slotId == 0) {
    		return true;
    	}
    	return false;
    }
    
    private String getServiceCenter(int id) {
    	mTelephonyManager = TelephonyManagerEx.getDefault();
    	return mTelephonyManager.getScAddress(id);	
    }
    
    private boolean setServiceCenter(String SCnumber, int id) {
    	mTelephonyManager = TelephonyManagerEx.getDefault();
    	Log.i(TAG, "setScAddress is: " + SCnumber);
    	return mTelephonyManager.setScAddress(SCnumber, id);
    }
    
    private void tostScOK() {
    	Toast.makeText(this, R.string.set_service_center_OK, 0);
    }
    private void tostScFail() {
    	Toast.makeText(this, R.string.set_service_center_fail, 0);
    }
    private class PositiveButtonListener implements OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			mTelephonyManager = TelephonyManagerEx.getDefault();
			String scNumber = mNumberText.getText().toString();
			Log.i(TAG, "setScNumber is: " + scNumber);
			Log.i(TAG, "currentSim is: " + currentSim);
			//setServiceCenter(scNumber, currentSim);
			new Thread(new Runnable() {
                public void run() {
                	mTelephonyManager.setScAddress(mNumberText.getText().toString(), currentSim);
                }
			}).start();
		}
    }
    
    private class NegativeButtonListener implements OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			// cancel
			dialog.dismiss();
		}
    }
}
