package com.android.phone;

import java.io.IOException;
import java.util.ArrayList;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.gemini.GeminiPhone;

import java.util.*;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.util.AttributeSet;
import android.util.Log;
import android.provider.Telephony.SIMInfo;
import android.provider.Telephony.SimInfo;
import android.provider.Telephony;
import android.widget.RelativeLayout;
import com.mediatek.CellConnService.CellConnMgr;
import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.featureoption.FeatureOption;

class PreCheckForRunning {
    private CellConnMgr mCellConnMgr;
    private ServiceComplete mServiceComplete;
    private Context context;
    private static final String TAG = "PreCheckForRunning";
    public boolean byPass = false;
    
    public PreCheckForRunning(Context ctx)
    {
        context = ctx;
        mServiceComplete = new ServiceComplete();
        mCellConnMgr = new CellConnMgr(mServiceComplete);
        mCellConnMgr.register(context.getApplicationContext());
    }
    
    class ServiceComplete implements Runnable {
        public void run() {
            int result = mCellConnMgr.getResult();
            Log.d(TAG, "ServiceComplete with the result = " + CellConnMgr.resultToString(result));
            if (CellConnMgr.RESULT_OK == result || CellConnMgr.RESULT_STATE_NORMAL == result) {
                context.startActivity(intent);
            }
        }
    }
    
    public void checkToRun(Intent intent, int slotId, int req)
    {
        if (byPass) {
            context.startActivity(intent);
            return ;
        }
        setIntent(intent);
        int r = mCellConnMgr.handleCellConn(slotId, req);
        Log.d(TAG, "The result of handleCellConn = " + CellConnMgr.resultToString(r));
        /*if (r == CellConnMgr.RESULT_OK) {
            context.startActivity(intent);
            Log.d(TAG, "System is ready, Do our job bypass CellConnMgr.");
        }else {
            Log.d(TAG, "System not ready, CellConnMgr will notify us after.");
            setIntent(intent);
        }*/
    }
    
    public void setIntent(Intent it)
    {
        intent = it;
    }
    
    public void deRegister()
    {
        mCellConnMgr.unregister();
    }
    private Intent intent;
}
public class MultipleSimActivity extends PreferenceActivity implements Preference.onViewChangedListener, Preference.OnPreferenceChangeListener
{
    //Give the sub items type(ListPreference, CheckBoxPreference, Preference)
    private String mItemType = "PreferenceScreen";
    static public String  intentKey = "ITEM_TYPE";
    static public String SUB_TITLE_NAME = "sub_title_name";
    
    //Used for PreferenceScreen to start a activity
    static public String  targetClassKey = "TARGET_CLASS";
    
    //Used for ListPreference to initialize the list
    static public String initArray = "INIT_ARRAY";
    static public String initArrayValue = "INIT_ARRAY_VALUE";
    
    //Most time, we get the sim number by Framework's api, but we firstly check how many sim support a special feature
    //For example, although two sim inserted, but there's only one support the VT
    static public String initSimNumber = "INIT_SIM_NUMBER";
    static public String initFeatureName = "INIT_FEATURE_NAME";
    static public String initTitleName = "INIT_TITLE_NAME";
    static public String initSimId = "INIT_SIM_ID";
    static public String initBaseKey = "INIT_BASE_KEY";
    
    //VT private:
    private static final String SELECT_DEFAULT_PICTURE    = "0";
    private static final String SELECT_MY_PICTURE         = "2";
    
    private int mSimNumbers = 0;
    private String mTargetClass = null;
    private String mFeatureName;
    private String mTitleName;
    private String mListTitle;
    //for the key of checkbox and listpreference: mBaseKey + cardSlot || simId
    private String mBaseKey;
    private Phone mPhone = null;
    private List<SIMInfo> simList;
    private long[] simIds = null;
    private HashMap<Object, Integer> pref2CardSlot = new HashMap<Object, Integer>();
    private static String TAG = "MultipleSimActivity";
    private static final boolean DBG = true; // (PhoneApp.DBG_LEVEL >= 2);
    
    public static final String VT_FEATURE_NAME = "VT";
    public static final String NETWORK_MODE_NAME = "NETWORK_MODE";
    public static final String LIST_TITLE = "LIST_TITLE_NAME";
    
    private PreCheckForRunning preCfr = null;
    //TelephonyManagerEx tMex = null;
    private final MultipleSimReceiver mReceiver = new MultipleSimReceiver();
    ProgressDialog pd = null;
    
    private static void log(String msg) {
            Log.d(TAG, msg);
     }
    
    @Override
    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        mPhone = PhoneFactory.getDefaultPhone();
        simIds = getIntent().getLongArrayExtra(initSimId);
        //tMex = new TelephonyManagerEx(this);
        this.addPreferencesFromResource(R.xml.multiple_sim);
        
        String itemType = getIntent().getStringExtra(intentKey);
        if (itemType != null)
        {
            mItemType = itemType;
        }
        
        preCfr = new PreCheckForRunning(this);
        
        mTargetClass = getIntent().getStringExtra(targetClassKey);
        mFeatureName = getIntent().getStringExtra(initFeatureName);
        mTitleName = getIntent().getStringExtra(initTitleName);
        mBaseKey = getIntent().getStringExtra(initBaseKey);
        mListTitle = getIntent().getStringExtra(LIST_TITLE);
            
        if (mBaseKey == null)
        {
            mBaseKey = new String("");
        }
        //If upper level tells us the exact sim numbers, then we use it firstly, else we get it
        //by framework's api
        if (simIds != null)
        {
            mSimNumbers = simIds.length;
            simList = new ArrayList<SIMInfo>();
            for (int i = 0; i < mSimNumbers; ++i)
            {
                simList.add(SIMInfo.getSIMInfoById(this,simIds[i]));
            }
        }else
        {
            simList = SIMInfo.getInsertedSIMList(this);
            mSimNumbers = simList.size();
        }
        
        skipUsIfNeeded();
        createSubItems();
    }
    
    @Override
    public void onResume()
    {
        super.onResume();        
        PreferenceScreen prefSet = getPreferenceScreen();
        
        //For single sim or only one sim inserted, we couldn't go here
        GeminiPhone dualPhone = null;
        if (mPhone instanceof GeminiPhone)
        {
            dualPhone = (GeminiPhone)mPhone;
        }
        for (int i = 0; i < prefSet.getPreferenceCount(); ++i)
        {
            Preference p = prefSet.getPreference(i);
            if (dualPhone != null) {
                p.setEnabled(dualPhone.isRadioOnGemini(pref2CardSlot.get(p)));
            }
        }
    }
    
    //We will consider the feature and the number inserted sim then 
    //skip us and enter the setting directly
    private void skipUsIfNeeded()
    {
        //For VT and Network mode we will pay special handler, so we don't skip
        if (VT_FEATURE_NAME.equals(mFeatureName) || NETWORK_MODE_NAME.equals(mFeatureName))
        {
            return;
        }
        
        if (this.mSimNumbers == 1 && mTargetClass != null)
        {
            Intent intent = new Intent();
            int position = mTargetClass.lastIndexOf('.');
            String pkName = mTargetClass.substring(0, position);
            intent.setClassName(pkName, mTargetClass);
            intent.setAction(Intent.ACTION_MAIN);
            intent.putExtra(Phone.GEMINI_SIM_ID_KEY, simList.get(0).mSlot);
            startActivity(intent);
            this.finish();
        }
    }
    
    private void createSubItems()
    {
        PreferenceScreen prefSet = getPreferenceScreen();
        ArrayList<String> keys = new ArrayList<String>();
        for (int i = 0; i < prefSet.getPreferenceCount(); ++i)
        {
            String c = prefSet.getPreference(i).getClass().getName();
            if (!c.endsWith(mItemType))
            {
                keys.add(prefSet.getPreference(i).getKey());
            }
        }
        
        for (String s : keys)
        {
            prefSet.removePreference(prefSet.findPreference(s));
        }
        
        int prefCount = prefSet.getPreferenceCount();
        
        for (int i = prefCount - 1; i > mSimNumbers - 1; --i)
        {
            prefSet.removePreference(prefSet.getPreference(i));
        }
        
        if (mItemType.equals("PreferenceScreen"))
        {
            initPreferenceScreen();
        }else
        {
            if (mItemType.equals("CheckBoxPreference"))
            {
                initCheckBoxPreference();
            }else
            {
                if (mItemType.equals("ListPreference"))
                {
                    initListPreference();
                }
            }
        }
        
        if (mTitleName != null)
        {
            this.setTitle(mTitleName);
        }
    }
    
    void initPreferenceScreen()
    {
        PreferenceScreen prefSet = getPreferenceScreen();
        ListView lv = this.getListView();
        Object o1 = lv.getItemIdAtPosition(0);
        Object o2 = lv.getItemIdAtPosition(1);
        if (mSimNumbers == simList.size())
        {
            for (int i = 0; i < mSimNumbers; ++i)
            {
                Preference p = prefSet.getPreference(i);
                p.setTitle(simList.get(i).mDisplayName);
                //this can used save the simids which will then used for get sim color
                p.setSummary("18601258794");
                p.setViewChangeListener(this);
                pref2CardSlot.put(prefSet.getPreference(i), Integer.valueOf(simList.get(i).mSlot));
            }
        }else
        {
            //For the special case, we will handle by the initFeatureName to decide which slot support this
        }
    }
    
    void initCheckBoxPreference()
    {
        PreferenceScreen prefSet = getPreferenceScreen();
        for (int i = 0; i < mSimNumbers; ++i)
        {
            String key = null;
            Preference p = prefSet.getPreference(i);
            p.setTitle(simList.get(i).mDisplayName);
            p.setSummary("18601258794");
            p.setViewChangeListener(this);
            //pref2CardSlot.put(prefSet.getPreference(i), Integer.valueOf(SIMInfo.getSlotById(this, simList.get(i).mSimId)));
            pref2CardSlot.put(prefSet.getPreference(i), Integer.valueOf(simList.get(i).mSlot));
            
            //Now we use @ as the flag to indicate if need to modify the key
            //Use this must make sure there's only one cotrol use, else will cause issue
            if (mBaseKey != null && mBaseKey.endsWith("@"))
            {
                mBaseKey = mBaseKey.substring(0, mBaseKey.length()-1);
                key = mBaseKey;
                p.setKey(mBaseKey);
            }else {
                //If this used the key + simId more proper?
                key = mBaseKey + "_" + simList.get(i).mSlot;
                p.setKey(key);
            }
            
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            CheckBoxPreference cp = (CheckBoxPreference)p;
            cp.setChecked(sp.getBoolean(key, true));
            p.setOnPreferenceChangeListener(this);
        }
    }
    
    void initListPreference()
    {
        PreferenceScreen prefSet = getPreferenceScreen();
        CharSequence[] array = (String[])getIntent().getCharSequenceArrayExtra(initArray);
        CharSequence[] arrayValue = getIntent().getCharSequenceArrayExtra(initArrayValue);
        
        if (NETWORK_MODE_NAME.equals(mFeatureName)) {
            IntentFilter intentFilter = new IntentFilter(PhoneApp.NETWORK_MODE_CHANGE_RESPONSE);
            registerReceiver(mReceiver, intentFilter);
        }
        
        for (int i = 0; i < mSimNumbers; ++i)
        {
            String key = null;
            ListPreference listPref = (ListPreference)prefSet.getPreference(i);
            listPref.setTitle(simList.get(i).mDisplayName);
            listPref.setViewChangeListener(this);
            //Now we use @ as the flag to indicate if need to modify the key
            //Use this must make sure there's only one cotrol use, else will cause issue
            if (mBaseKey != null && mBaseKey.endsWith("@"))
            {
                mBaseKey = mBaseKey.substring(0, mBaseKey.length()-1);
                key = mBaseKey;
                listPref.setKey(mBaseKey);
            }else {
                //If this used the key + simId more proper?
                key = mBaseKey + "_" + simList.get(i).mSlot;
                listPref.setKey(key);
            }
            
            if (mListTitle != null) {
                listPref.setDialogTitle(mListTitle);
            }
            
            listPref.setEntries(array);
            listPref.setEntryValues(arrayValue);
            pref2CardSlot.put(prefSet.getPreference(i), Integer.valueOf(simList.get(i).mSlot));
            //listPref.setKey(mBaseKey + (simList.get(i).mSlot));
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            listPref.setValue(sp.getString(key, null));
            listPref.setOnPreferenceChangeListener(this);
        }
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
    {
        PreferenceScreen prefSet = getPreferenceScreen();

        GeminiPhone dualPhone = null;
        if (mPhone instanceof GeminiPhone)
        {
            dualPhone = (GeminiPhone)mPhone;
        }
         
        for (int i = 0; i < prefSet.getPreferenceCount(); i++)
        {
            if ((preference == prefSet.getPreference(i)) && (mTargetClass != null) && (dualPhone != null))
            {
                int slotId = pref2CardSlot.get(preference);
                preference.setEnabled(dualPhone.isRadioOnGemini(slotId));
                if(dualPhone.isRadioOnGemini(slotId)){
                    Intent intent = new Intent();
                    int position = mTargetClass.lastIndexOf('.');
                    String pkName = mTargetClass.substring(0, position);
                    intent.setClassName(pkName, mTargetClass);
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.putExtra(Phone.GEMINI_SIM_ID_KEY, slotId);
                    intent.putExtra(SUB_TITLE_NAME, SIMInfo.getSIMInfoBySlot(this, slotId).mDisplayName);
                    if (mFeatureName != null && mFeatureName.equals("VT"))
                    {
                        intent.putExtra("ISVT", true);
                    }
                    preCfr.checkToRun(intent, slotId, 302);
                } 
                /*mServiceComplete.setIntent(intent);
                int r = mCellConnMgr.handleCellConn(slotId, 302);
                if (r == CellConnMgr.RESULT_OK) {
                    startActivity(intent);
                    Log.d(TAG, "System is ready, Do our job bypass CellConnMgr.");
                }else {
                    Log.d(TAG, "System not ready, CellConnMgr will notify us after.");
                }*/
            }
        }
        return false;
}
    
    public void checkAllowedRun(Intent intent, Preference preference) {
        int slot = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, 0);
        GeminiPhone dualPhone = null;
        if (mPhone instanceof GeminiPhone)
        {
            dualPhone = (GeminiPhone)mPhone;
        }
        
        //The sim status isn't ready, we post this to common module to handle this
        if (TelephonyManager.SIM_STATE_READY != TelephonyManager.getDefault().getSimStateGemini(slot)) {
            
        }
    }

    private String getProperOperatorNumber(SIMInfo info)
    {
        String res = null;
        int charCount = 4;
        if (info == null) return res;
        res = info.mNumber;
        switch (info.mDispalyNumberFormat)
        {
            case SimInfo.DISPALY_NUMBER_NONE:
                res = "";
                break;
                
            case SimInfo.DISPLAY_NUMBER_FIRST:
                if (res != null && res.length() > 4)
                {
                    res = res.substring(0, charCount);
                }
                break;
            
            case SimInfo.DISPLAY_NUMBER_LAST:
                if (res != null && res.length() > 4)
                {
                    res = res.substring(res.length() - charCount, res.length());
                }
                break;
                
            default:
                res = "";
        }
        
        return res;
    }
    
    public void giveViewInformation(View currentView, Preference p) {
        // TODO Auto-generated method stub
        Log.d(TAG, "Preference information = " + p.getClass().toString());
        int cardSlot = pref2CardSlot.get(p);
        SIMInfo info = SIMInfo.getSIMInfoBySlot(this, cardSlot);
        
        if (info == null) {
            return ;
        }
        
        if (!Settings.isSupport3G(cardSlot) || !("OP02".equals(PhoneUtils.getOptrProperties()))) {
            TextView text3G = (TextView) currentView.findViewById(R.id.sim3g);
            if (text3G != null)
            {
                text3G.setVisibility(View.GONE);
            }
        }
        
        /*ImageView imageStatus = (ImageView) currentView.findViewById(R.id.simStatus);
        if (imageStatus != null) {
            int res = getSimStatusImge(tMex.getSimIndicatorStateGemini(info.mSlot));
            if(res == -1) {
                imageStatus.setVisibility(View.GONE);
            } else {
                imageStatus.setImageResource(res);              
            }
        }*/
        
        int simColor = info.mColor;
        RelativeLayout viewSim = (RelativeLayout)currentView.findViewById(R.id.simIcon);
        if (viewSim != null) {
            if((simColor>=0)&&(simColor<=7)) {
                viewSim.setBackgroundResource(Telephony.SIMBackgroundRes[simColor]);
            } else {
                viewSim.setBackgroundDrawable(null);
            }
        }   
        
        TextView operatorName = (TextView)currentView.findViewById(R.id.OperatorName);
        if (operatorName != null)
        {
            operatorName.setText(info != null ? info.mDisplayName : p.getTitle());
        }
        
        TextView simNumber = (TextView)currentView.findViewById(R.id.showNumber);
        if (simNumber != null)
        {
            //String numberName = null;
            //if (info != null) numberName = getProperOperatorNumber(info);
            /*if (numberName == null || numberName.length() == 0)
            {
                numberName = "Unknown";
            }*/
            if (info.mNumber == null || info.mNumber.equals("")) {
                simNumber.setVisibility(View.GONE);
            }
            simNumber.setText(info.mNumber);
        }
        
        TextView simIconNumber = (TextView)currentView.findViewById(R.id.simNum);
        if (simIconNumber != null) {
            simIconNumber.setText(getProperOperatorNumber(info));
        }
        
    }
    
    public int getNetworkMode(int buttonNetworkMode)
    {
        int modemNetworkMode = 0;
        int settingsNetworkMode = android.provider.Settings.Secure.getInt(
                mPhone.getContext().getContentResolver(),
                android.provider.Settings.Secure.PREFERRED_NETWORK_MODE, 0);
        if (buttonNetworkMode != settingsNetworkMode) {
            switch(buttonNetworkMode) {
                case Phone.NT_MODE_GLOBAL:
                    modemNetworkMode = Phone.NT_MODE_GLOBAL;
                    break;
                case Phone.NT_MODE_EVDO_NO_CDMA:
                    modemNetworkMode = Phone.NT_MODE_EVDO_NO_CDMA;
                    break;
                case Phone.NT_MODE_CDMA_NO_EVDO:
                    modemNetworkMode = Phone.NT_MODE_CDMA_NO_EVDO;
                    break;
                case Phone.NT_MODE_CDMA:
                    modemNetworkMode = Phone.NT_MODE_CDMA;
                    break;
                case Phone.NT_MODE_GSM_UMTS:
                    modemNetworkMode = Phone.NT_MODE_GSM_UMTS;
                    break;
                case Phone.NT_MODE_WCDMA_ONLY:
                    modemNetworkMode = Phone.NT_MODE_WCDMA_ONLY;
                    break;
                case Phone.NT_MODE_GSM_ONLY:
                    modemNetworkMode = Phone.NT_MODE_GSM_ONLY;
                    break;
                case Phone.NT_MODE_WCDMA_PREF:
                    modemNetworkMode = Phone.NT_MODE_WCDMA_PREF;
                    break;
                default:
                    modemNetworkMode = Phone.PREFERRED_NT_MODE;
            }
        }
        android.provider.Settings.Secure.putInt(mPhone.getContext().getContentResolver(),
                android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                modemNetworkMode );
        return modemNetworkMode;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        if (mFeatureName != null && mFeatureName.equals(VT_FEATURE_NAME))
        {
            VTCallUtils.checkVTFile();
            if (preference.getKey().equals("button_vt_replace_expand_key") && newValue.toString().equals(SELECT_DEFAULT_PICTURE))
            {
                showDialogDefaultPic();
            }else if (preference.getKey().equals("button_vt_replace_expand_key") && newValue.toString().equals(SELECT_MY_PICTURE)) {
                showDialogMyPic();
            }
        }else if (mFeatureName != null && mFeatureName.equals(NETWORK_MODE_NAME))
        {
            int settingsNetworkMode = android.provider.Settings.Secure.getInt(
                    mPhone.getContext().getContentResolver(),
                    android.provider.Settings.Secure.PREFERRED_NETWORK_MODE, 0);
            Log.d(TAG, "Current network mode = " + settingsNetworkMode);
            int networkMode = getNetworkMode(Integer.valueOf((String) newValue).intValue());
            Log.d(TAG, "new network mode = " + networkMode);
            if (settingsNetworkMode != networkMode) {
                Intent intent = new Intent(
                        PhoneApp.NETWORK_MODE_CHANGE,
                        null);
                intent.putExtra(PhoneApp.OLD_NETWORK_MODE, settingsNetworkMode);
                intent.putExtra(PhoneApp.NETWORK_MODE_CHANGE, networkMode);
                intent.putExtra(Phone.GEMINI_SIM_ID_KEY, pref2CardSlot.get(preference));
                showProgressDialog();
                sendBroadcast(intent);
            }
        }
        
        return true;
    }
    
    private void showProgressDialog() {
        // TODO Auto-generated method stub
        pd = new ProgressDialog(this);
        pd.setMessage(getText(R.string.updating_settings));
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.show();
    }

    public void showDialogMyPic()
    {
        ImageView mImg2 = new ImageView(this);
        final Bitmap mBitmap2 = BitmapFactory.decodeFile(VTAdvancedSetting.getPicPathUserselect());
        mImg2.setImageBitmap(mBitmap2);
        
        AlertDialog mDialog2 = new AlertDialog.Builder(this)
        .setPositiveButton(R.string.vt_change_my_pic,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog,
                            int which) {
                        // TODO Auto-generated method stub

                        try {
                            Intent intent = new Intent(
                                    Intent.ACTION_GET_CONTENT,
                                    null);

                            intent.setType("image/*");
                            intent.putExtra("crop", "true");
                            intent.putExtra("aspectX", 1);
                            intent.putExtra("aspectY", 1);
                            intent.putExtra("outputX",
                                    getResources().getInteger(
                                            R.integer.qcif_x));
                            intent.putExtra("outputY",
                                    getResources().getInteger(
                                            R.integer.qcif_y));
                            intent
                                    .putExtra("return-data",
                                            true);

                            startActivityForResult(intent,
                                    VTAdvancedSetting.REQUESTCODE_PICTRUE_PICKED_WITH_DATA);

                        } catch (ActivityNotFoundException e) {
                            if (DBG)
                                log("Pictrue not found , Gallery ActivityNotFoundException !");
                        }
                        
                        mBitmap2.recycle();
                        if (DBG) log(" - Bitmap.isRecycled() : " + mBitmap2.isRecycled() );
                        
                        return;
                    }
                }).setNegativeButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog,
                            int which) {
                        mBitmap2.recycle();
                        if (DBG) log(" - Bitmap.isRecycled() : " + mBitmap2.isRecycled() );
                        return;
                    }
                }).create();
        
        mDialog2.setView(mImg2);
        mDialog2.setTitle(getResources().getString(R.string.vt_pic_replace_local_mypic));                
        mDialog2.show();
    }
    
    public void showDialogDefaultPic()
    {
        ImageView mImg = new ImageView(this);
        final Bitmap mBitmap = BitmapFactory.decodeFile(VTAdvancedSetting.getPicPathDefault());
        mImg.setImageBitmap(mBitmap);
        
        AlertDialog mDialog = new AlertDialog.Builder(this)
        .setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog,
                            int which) {
                        mBitmap.recycle();
                        if (DBG) log(" - Bitmap.isRecycled() : " + mBitmap.isRecycled() );
                        return;
                    }
                }).create();
        mDialog.setView(mImg);
        mDialog.setTitle(getResources().getString(R.string.vt_pic_replace_local_default));                
        mDialog.show(); 
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        if (DBG) log("onActivityResult: requestCode = " + requestCode + ", resultCode = " +resultCode);
        
        if (resultCode != RESULT_OK) return;
        
        switch (requestCode){
        
        case VTAdvancedSettingEx.REQUESTCODE_PICTRUE_PICKED_WITH_DATA:
            try {
                final Bitmap bitmap = data.getParcelableExtra("data");
                VTCallUtils.saveMyBitmap(getPicPathUserselect(), bitmap);
                bitmap.recycle();
                if (DBG) log(" - Bitmap.isRecycled() : " + bitmap.isRecycled() );
            } catch (IOException e) {
                e.printStackTrace();
            }
            showDialogMyPic();
            break;
            
        }
    }
    
    static public String getPicPathUserselect()
    {
        return "/data/data/com.android.phone/" + VTAdvancedSettingEx.NAME_PIC_TO_REPLACE_LOCAL_VIDEO_USERSELECT + ".vt";
    }
    
    protected void onDestroy() {
        super.onDestroy();
        if (preCfr != null) {
            preCfr.deRegister();
        }
    }
    
    static private int getSimStatusImge(int state) {
        switch (state) {
            case Phone.SIM_INDICATOR_RADIOOFF:
                return com.mediatek.internal.R.drawable.sim_radio_off;
            case Phone.SIM_INDICATOR_LOCKED:
                return com.mediatek.internal.R.drawable.sim_locked;
            case Phone.SIM_INDICATOR_INVALID:
                return com.mediatek.internal.R.drawable.sim_invalid;
            case Phone.SIM_INDICATOR_SEARCHING:
                return com.mediatek.internal.R.drawable.sim_searching;
            case Phone.SIM_INDICATOR_ROAMING:
                return com.mediatek.internal.R.drawable.sim_roaming;
            case Phone.SIM_INDICATOR_CONNECTED:
                return com.mediatek.internal.R.drawable.sim_connected;
            case Phone.SIM_INDICATOR_ROAMINGCONNECTED:
                return com.mediatek.internal.R.drawable.sim_roaming_connected;
            default:
                return -1;
        }
    }
    
    private class MultipleSimReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(PhoneApp.NETWORK_MODE_CHANGE_RESPONSE)) {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                    pd = null;
                }
                if (!intent.getBooleanExtra(PhoneApp.NETWORK_MODE_CHANGE_RESPONSE, true)) {
                    Log.d(TAG, "BroadcastReceiver: network mode change failed! restore the old value.");
                    android.provider.Settings.Secure.putInt(mPhone.getContext().getContentResolver(),
                            android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                            intent.getIntExtra(PhoneApp.OLD_NETWORK_MODE, 0));
                    Log.d(TAG, "BroadcastReceiver  = " + intent.getIntExtra(PhoneApp.OLD_NETWORK_MODE, 0));
                }else {
                    Log.d(TAG, "BroadcastReceiver: network mode change success! set to the new value.");
                    android.provider.Settings.Secure.putInt(mPhone.getContext().getContentResolver(),
                            android.provider.Settings.Secure.PREFERRED_NETWORK_MODE,
                            intent.getIntExtra("NEW_NETWORK_MODE", 0));
                    Log.d(TAG, "BroadcastReceiver  = " + intent.getIntExtra("NEW_NETWORK_MODE", 0));
                }
            }
        }
    }
}