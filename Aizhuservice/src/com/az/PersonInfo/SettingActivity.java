package com.az.PersonInfo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.telephony.TelephonyManager;
import android.util.Log;

import android.view.Window;
import android.preference.PreferenceScreen;
import android.preference.Preference;

import android.content.SharedPreferences;

import android.widget.Toast;
import android.view.View;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.view.KeyEvent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;

/*必需引用apache.http相关类别来建立HTTP联机*/
import org.apache.http.HttpResponse; 
import org.apache.http.NameValuePair; 
import org.apache.http.client.ClientProtocolException; 
import org.apache.http.client.entity.UrlEncodedFormEntity; 
import org.apache.http.client.methods.HttpPost; 
import org.apache.http.impl.client.DefaultHttpClient; 
import org.apache.http.message.BasicNameValuePair; 
import org.apache.http.protocol.HTTP; 
import org.apache.http.util.EntityUtils; 

import com.az.Main.MainActivity;
import com.az.Main.R;


public class SettingActivity extends PreferenceActivity 
	implements Preference.OnPreferenceChangeListener,View.OnClickListener{
	
	private SharedPreferences mPerferences;
	private DatePreference mInsuranceEndPreference;
	private DatePreference mInsuranceStartPreference;
	private EditTextPreference mNamePreference;
	private EditTextPreference mPhoneNumPreference;
	private EditTextPreference mCertificateNumPreference;
	private EditTextPreference mCityPreference;
	private EditTextPreference mAddressPreference;
	private EditTextPreference mEmergencyPersonPreference;
	private EditTextPreference mEmergencyContactPreference;
	private EditTextPreference mImeiPreference;
	private EditTextPreference mRemarkPreference;
	private ListPreference mSelectsexPreference;
	private ListPreference mCertificateTypePreference;
	private ListPreference mProvincePreference;
	private ListPreference mDataTypPreference;
	private CheckBoxPreference mInsurance01Preference;
	private CheckBoxPreference mInsurance02Preference;
	private CheckBoxPreference mInsurance03Preference;
	private CheckBoxPreference mInsurance04Preference;
	private CheckBoxPreference mInsurance05Preference;
	private CheckBoxPreference mInsurance06Preference;
	private CheckBoxPreference mInsurance07Preference;
	private CheckBoxPreference mInsurance08Preference;
	private CheckBoxPreference mInsurance99Preference;
	
	//新添加
	
	private EditTextPreference age_key;//年龄
	private EditTextPreference height_key;//身高
	private  ListPreference constitution_key;//体质状况
	private EditTextPreference weight_key;//体重
	private  CheckBoxPreference disease_01;//身体状况
	private  CheckBoxPreference disease_02;
	private  CheckBoxPreference disease_03;
	private  CheckBoxPreference disease_04;
	private  CheckBoxPreference disease_05;
	private  CheckBoxPreference disease_06;
	private  CheckBoxPreference disease_07;
	private  CheckBoxPreference disease_08;
	private  CheckBoxPreference disease_09;
	private  CheckBoxPreference disease_10;
	private  CheckBoxPreference disease_11;
	private  CheckBoxPreference disease_12;
	
	public  String imei;
	
	String InsuranceTpye="";
	String diseaseTpye="";
	boolean do_sendInfoFlag = false;
	private AlertDialog dialog = null;
	//private ConnectivityManager mCM; 
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
				/*Exit();*/
			Intent intent=new Intent(this,MainActivity.class);
			startActivity(intent);
			finish();
				return true;
			}
		
			return super.onKeyDown(keyCode, event);	 
	}
	
	public void UpOk(){
		
		new AlertDialog.Builder(this).setTitle(getString(R.string.AzInformationNotice)).setMessage(getString(R.string.AzInfoUpOK)).setPositiveButton(getString(R.string.azconfirm), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialoginterface, int i) {
				Intent intent=new Intent(SettingActivity.this,MainActivity.class);
				startActivity(intent);
				finish();
				
			}
		}).setNegativeButton(getString(R.string.azcancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialoginterface, int i) {
				dialoginterface.dismiss();
				
			}
		} ).show();
		
	}
	private void UpFail(){
		new AlertDialog.Builder(this).setTitle(getString(R.string.AzInformationNotice)).setMessage(getString(R.string.AzInfoUpErr)).setNegativeButton(getString(R.string.azcancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialoginterface, int i) {
				// TODO Auto-generated method stub
				dialoginterface.dismiss();
			}
		}).show();
	}
	
	private void Exit(){
		   new AlertDialog.Builder(this)
	    	.setTitle(getString(R.string.AzInformationNotice))
	    	.setMessage(getString(R.string.AzInfoExitNotice))
	    	.setPositiveButton(getString(R.string.azconfirm), new DialogInterface.OnClickListener()
	    	{ 
	    		public void onClick(DialogInterface dialog, int whichButton) 
	    		{   	
	    			finish();
	    			//System.exit(0);
	    		} 
	    	}	  	    	
	    	)
	    	.setNegativeButton(getString(R.string.azcancel), new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int whichButton) {                
	    		}
	    	}).show();
	}	   
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
	//	mSelectsexPreference.setSummary(mSelectsexPreference.getEntry());
		super.onResume();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 注意顺序    
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);	
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.title);  // 注意顺序   
        TelephonyManager telmgr = (TelephonyManager)this.getSystemService(Service.TELEPHONY_SERVICE);
    	imei = "IMEI:" + telmgr.getDeviceId();
        findViewById(R.id.flish_button).setOnClickListener(this);
        //mCM = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);   

        mPerferences = getSharedPreferences("com.az.PersonInfo_preferences",Context.MODE_WORLD_READABLE);
        
        mInsuranceEndPreference = (DatePreference) findPreference("insuranceEnd_key");
        mInsuranceEndPreference.setOnPreferenceChangeListener(this);
        
        mInsuranceStartPreference = (DatePreference) findPreference("insuranceStart_key");
        mInsuranceStartPreference.setOnPreferenceChangeListener(this);
        
        mNamePreference = (EditTextPreference) findPreference("name_key");
        mNamePreference.setOnPreferenceChangeListener(this);
        
        mPhoneNumPreference = (EditTextPreference) findPreference("phoneNum_key");
        mPhoneNumPreference.setOnPreferenceChangeListener(this);
        
        mCertificateNumPreference = (EditTextPreference) findPreference("certificateNum_key");
        mCertificateNumPreference.setOnPreferenceChangeListener(this);
        
        mCityPreference = (EditTextPreference) findPreference("city_key");
        mCityPreference.setOnPreferenceChangeListener(this);
        
        mAddressPreference = (EditTextPreference) findPreference("address_key");
        mAddressPreference.setOnPreferenceChangeListener(this);
        
        mEmergencyPersonPreference = (EditTextPreference) findPreference("emergencyPerson_key");
        mEmergencyPersonPreference.setOnPreferenceChangeListener(this);
        
        mEmergencyContactPreference = (EditTextPreference) findPreference("emergencyContact_key");
        mEmergencyContactPreference.setOnPreferenceChangeListener(this);
        
        mImeiPreference = (EditTextPreference) findPreference("imei_key");
        mImeiPreference.setOnPreferenceChangeListener(this);
        mImeiPreference.setSummary(imei);
        mImeiPreference.setEnabled(false);
        mRemarkPreference = (EditTextPreference) findPreference("remark_key");
        mRemarkPreference.setOnPreferenceChangeListener(this);
        
        mSelectsexPreference = (ListPreference) findPreference("selectsex_key");
        mSelectsexPreference.setOnPreferenceChangeListener(this);
        mCertificateTypePreference = (ListPreference) findPreference("certificateType_key");
        mCertificateTypePreference.setOnPreferenceChangeListener(this);
    	mProvincePreference = (ListPreference) findPreference("province_key");
    	mProvincePreference.setOnPreferenceChangeListener(this);
    	mDataTypPreference = (ListPreference) findPreference("dataTyp_key");
    	mDataTypPreference.setOnPreferenceChangeListener(this);
    	
        mInsurance01Preference = (CheckBoxPreference) findPreference("insurance_01");
        mInsurance02Preference = (CheckBoxPreference) findPreference("insurance_02");
        mInsurance03Preference = (CheckBoxPreference) findPreference("insurance_03");
        mInsurance04Preference = (CheckBoxPreference) findPreference("insurance_04");
        mInsurance05Preference = (CheckBoxPreference) findPreference("insurance_05");
        mInsurance06Preference = (CheckBoxPreference) findPreference("insurance_06");
        mInsurance07Preference = (CheckBoxPreference) findPreference("insurance_07");
        mInsurance08Preference = (CheckBoxPreference) findPreference("insurance_08");
        mInsurance99Preference = (CheckBoxPreference) findPreference("insurance_99");
       

       //新加部分 
        age_key=(EditTextPreference) findPreference("age_key");
        age_key.setOnPreferenceChangeListener(this);

        height_key=(EditTextPreference) findPreference("height_key");
        height_key.setOnPreferenceChangeListener(this);
        constitution_key=(ListPreference) findPreference("constitution_key");
        constitution_key.setOnPreferenceChangeListener(this);
        weight_key=(EditTextPreference) findPreference("weight_key");
        weight_key.setOnPreferenceChangeListener(this);
        disease_01=(CheckBoxPreference) findPreference("disease_01");
        disease_01.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object obj) {
				
				if(!disease_01.isChecked()){
					disease_11.setChecked(false);
				
				}
				
				return true;
			}
		});
        disease_02=(CheckBoxPreference) findPreference("disease_02");
        disease_02.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object obj) {
				if(!disease_02.isChecked()){
					disease_11.setChecked(false);
				
				}
				return true;
			}
		});
        disease_03=(CheckBoxPreference) findPreference("disease_03");
        disease_03.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object obj) {
				if(!disease_03.isChecked()){
					disease_11.setChecked(false);
				
				}
				return true;
			}
		});
        disease_04=(CheckBoxPreference) findPreference("disease_04");
        disease_04.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object obj) {
				if(!disease_04.isChecked()){
					disease_11.setChecked(false);
				
				}
				
				return true;
			}
		});
        disease_05=(CheckBoxPreference) findPreference("disease_05");
        disease_05.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object obj) {
				if(!disease_05.isChecked()){
					disease_11.setChecked(false);
				
				}
				return true;
			}
		});
        
        disease_06=(CheckBoxPreference) findPreference("disease_06");
        disease_06.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object obj) {
				if(!disease_06.isChecked()){
					disease_11.setChecked(false);
				
				}
				return true;
			}
		});
        
        disease_07=(CheckBoxPreference) findPreference("disease_07");
        
        disease_07.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object obj) {
				if(!disease_07.isChecked()){
					disease_11.setChecked(false);
				
				}
				return true;
			}
		});
        disease_08=(CheckBoxPreference) findPreference("disease_08");
        disease_08.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object obj) {
				if(!disease_08.isChecked()){
					disease_11.setChecked(false);
				
				}
				return true;
			}
		});
        
        disease_09=(CheckBoxPreference) findPreference("disease_09");
        disease_09.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object obj) {
				
				if(!disease_09.isChecked()){
					disease_11.setChecked(false);
				
				}
				
				return true;
			}
		});
        
        disease_10=(CheckBoxPreference) findPreference("disease_10");
        
        disease_10.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object obj) {
				if(!disease_10.isChecked()){
					disease_11.setChecked(false);
				
				}
				return true;
			}
		});
        
        disease_11=(CheckBoxPreference) findPreference("disease_11");
        
        disease_12=(CheckBoxPreference) findPreference("disease_12");
       disease_11.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object obj) {
			// TODO Auto-generated method stub
			 if(!disease_11.isChecked()){
					disease_12.setChecked(false);
					disease_11.setChecked(false);
					disease_10.setChecked(false);
					disease_09.setChecked(false);
					disease_08.setChecked(false);
					disease_07.setChecked(false);
					disease_06.setChecked(false);
					disease_05.setChecked(false);
					disease_04.setChecked(false);
					disease_03.setChecked(false);
					disease_02.setChecked(false);
					disease_01.setChecked(false);
					
				}
			return true;
		}
	});
        
       
        disease_12.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object obj) {
				
				if(!disease_12.isChecked()){
					disease_11.setChecked(false);
				
				}
				return true;
			}
		});

        mNamePreference.setSummary(mPerferences.getString("name_key", ""));
        mSelectsexPreference.setSummary(mPerferences.getString("selectsex_key",""));  
        age_key.setSummary(mPerferences.getString("age_key",""));  
        height_key.setSummary(mPerferences.getString("height_key",""));  
        weight_key.setSummary(mPerferences.getString("weight_key",""));  
        constitution_key.setSummary(mPerferences.getString("constitution_key",""));  
        mPhoneNumPreference.setSummary(mPerferences.getString("phoneNum_key", ""));
        mCertificateTypePreference.setSummary(mPerferences.getString("certificateType_key",""));        
        mCertificateNumPreference.setSummary(mPerferences.getString("certificateNum_key", ""));
        mProvincePreference.setSummary(mPerferences.getString("province_key",""));     
        mCityPreference.setSummary(mPerferences.getString("city_key", ""));
        mAddressPreference.setSummary(mPerferences.getString("address_key", ""));
        mEmergencyPersonPreference.setSummary(mPerferences.getString("emergencyPerson_key", ""));
        mEmergencyContactPreference.setSummary(mPerferences.getString("emergencyContact_key", ""));
        //mImeiPreference.setSummary(mPerferences.getString("imei_key", ""));
        mInsuranceStartPreference.setSummary(mPerferences.getString("insuranceStart_key", ""));
        mInsuranceEndPreference.setSummary(mPerferences.getString("insuranceEnd_key", ""));
        mDataTypPreference.setSummary(mPerferences.getString("dataTyp_key",""));  
        mRemarkPreference.setSummary(mPerferences.getString("remark_key", ""));
        
        dialog = new ProgressDialog(this);
        
		dialog.setTitle(getString(R.string.AzWaiting));
		dialog.setMessage(getString(R.string.AzUpdataIng));
	}
	

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.flish_button:
				
				if(mInsurance99Preference.isChecked()){
		        	InsuranceTpye = "01,02,03,04,05,06,07,08,";
		        }
		        else{
		        	InsuranceTpye = (mInsurance01Preference.isChecked()? "01,":"")+(mInsurance02Preference.isChecked()? "02,":"")+
		        					(mInsurance03Preference.isChecked()? "03,":"")+(mInsurance04Preference.isChecked()? "04,":"")+
		        					(mInsurance05Preference.isChecked()? "05,":"")+(mInsurance06Preference.isChecked()? "06,":"")+
		        					(mInsurance07Preference.isChecked()? "07,":"")+(mInsurance08Preference.isChecked()? "08,":"");
		        }
				if(disease_11.isChecked()){
					diseaseTpye = "11,";
		        }
		        else{
		        	diseaseTpye = (disease_01.isChecked()? "01,":"")+(disease_02.isChecked()? "02,":"")+
		        					(disease_03.isChecked()? "03,":"")+(disease_04.isChecked()? "04,":"")+
		        					(disease_05.isChecked()? "05,":"")+(disease_06.isChecked()? "06,":"")+
		        					(disease_07.isChecked()? "07,":"")+(disease_08.isChecked()? "08,":"")+
		        					(disease_09.isChecked()? "09,":"")+(disease_10.isChecked()? "10,":"")+
		        					(disease_12.isChecked()? "12,":"")
		        					;
		        }
				
				
				if(mNamePreference.getSummary().toString().equals("")){
					Toast.makeText(SettingActivity.this, getString(R.string.AzNameNotice), Toast.LENGTH_LONG).show();
				}
				else if(mCertificateTypePreference.getValue().equals("")){
					Toast.makeText(SettingActivity.this, getString(R.string.AzCertificateTypeNotice), Toast.LENGTH_LONG).show();
				}
				else if(mCertificateNumPreference.getSummary().toString().equals("")){
					Toast.makeText(SettingActivity.this, getString(R.string.AzCertificateNumNotice), Toast.LENGTH_LONG).show();
				}
				/*else if(mImeiPreference.getSummary().toString()==""){
					Toast.makeText(SettingActivity.this, "卡号，不能为空", Toast.LENGTH_LONG).show();
				}*/
				else if(InsuranceTpye==""){
					Toast.makeText(SettingActivity.this, getString(R.string.AzInsuranceTpyeNotice), Toast.LENGTH_LONG).show();
				}
				else if(mDataTypPreference.getValue().equals("")){
					Toast.makeText(SettingActivity.this, getString(R.string.AzDataTypNotice), Toast.LENGTH_LONG).show();
				}else if(diseaseTpye==""){
					Toast.makeText(SettingActivity.this, getString(R.string.AzDiseaseTpyeNotice), Toast.LENGTH_LONG).show();
				}else if(constitution_key.getSummary().equals("")){
					Toast.makeText(SettingActivity.this, getString(R.string.AzConstitutionNotice), Toast.LENGTH_LONG).show();
				}
				else{
					SharedPrefCommit();
				}
		        break;
			default:
				break;
		}
	}

	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		String key = preference.getKey();
		
		if ("insuranceEnd_key".equals(key)) {
			
			return true;
		}
		if ("saveKeyProfile".equals(key)) {

			return true;
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		//String key = preference.getKey();
		preference.setSummary(String.valueOf(newValue)); 
		
		return true;
	}

	private void do_SendInfo()
    {	
		/*if(!ConnectState(this)){
			
			Log.i("life", "网络关闭");
			openAPN();
		}*/
		
		/*有root权限可以使用此OpenGprs方法打开数据连接*/
		/*OpenGprs();*/
		
		/*try {
			OpenData(this);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		/*try {
			setMobileDataEnabled(this, true);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
    	 String LoginURIString = getString(R.string.PersonInfo);//"http://61.143.124.173:8080/io/PersonInfo.aspx";
         /*建立HTTP Post联机*/
         HttpPost httpRequest = new HttpPost(LoginURIString); 
         //Post运作传送变量必须用NameValuePair[]数组储存
         List <NameValuePair> params = new ArrayList <NameValuePair>(); 
         params.add(new BasicNameValuePair("updateCmd", "TSCMD4"));
         
         params.add(new BasicNameValuePair("name_key",mNamePreference.getSummary().toString())); 
        String selectsex= mSelectsexPreference.getValue();
        if(mSelectsexPreference.getValue().equals(getString(R.string.Boy))){
        	selectsex="01";
        }else if(mSelectsexPreference.getValue().equals(getString(R.string.Girl))){
        	selectsex="02";
        }else{
        	selectsex="";
        }
         params.add(new BasicNameValuePair("selectsex_key",selectsex)); 
         params.add(new BasicNameValuePair("phoneNum_key",mPhoneNumPreference.getSummary().toString()));    
         
         String certificate =mCertificateTypePreference.getValue();
         if(mCertificateTypePreference.getValue().equals(getString(R.string.IdCard))){
        	 certificate="01";
         }else if(mCertificateTypePreference.getValue().equals(getString(R.string.Passport))){
        	 certificate="02";
         }else if(mCertificateTypePreference.getValue().equals(getString(R.string.Armyman))){
        	 certificate="03";
         }else if(mCertificateTypePreference.getValue().equals(getString(R.string.Driver))){
        	 certificate="04";
         }else if(mCertificateTypePreference.getValue().equals(getString(R.string.Other))){
        	 certificate="05";
         }else {
        	 certificate="";
         }
         params.add(new BasicNameValuePair("certificateType_key",certificate));  
         params.add(new BasicNameValuePair("certificateNum_key",mCertificateNumPreference.getSummary().toString())); 
         params.add(new BasicNameValuePair("province_key",mProvincePreference.getValue()));
         params.add(new BasicNameValuePair("city_key",mCityPreference.getSummary().toString()));  
         params.add(new BasicNameValuePair("address_key",mAddressPreference.getSummary().toString()));  
         params.add(new BasicNameValuePair("emergencyPerson_key",mEmergencyPersonPreference.getSummary().toString()));  
         params.add(new BasicNameValuePair("emergencyContact_key",mEmergencyContactPreference.getSummary().toString()));  
         params.add(new BasicNameValuePair("imei_key",imei));  
         params.add(new BasicNameValuePair("insuranceStart_key",mInsuranceStartPreference.getSummary().toString())); 
         params.add(new BasicNameValuePair("insuranceEnd_key",mInsuranceEndPreference.getSummary().toString()));  
         params.add(new BasicNameValuePair("insurance",InsuranceTpye));  
         
         String datatype=mDataTypPreference.getValue();
         if(mDataTypPreference.getValue().equals(getString(R.string.NewInfo))){
        	 datatype="01";
         }else if(mDataTypPreference.getValue().equals(getString(R.string.UpData))){
        	 datatype="02";
         }else if(mDataTypPreference.getValue().equals(getString(R.string.Add))){
        	 datatype="03";
         }else if(mDataTypPreference.getValue().equals(getString(R.string.Decrease))){
        	 datatype="04";
         }else {
        	 datatype="";
         }
         
         params.add(new BasicNameValuePair("dataTyp_key",datatype));  
         params.add(new BasicNameValuePair("remark_key",mRemarkPreference.getSummary().toString())); 
         params.add(new BasicNameValuePair("diseaseTpye", diseaseTpye));//身体状况
         params.add(new BasicNameValuePair("age_key", age_key.getSummary().toString()));//年龄
         
         params.add(new BasicNameValuePair("weight_key", weight_key.getSummary().toString()));//体重
         
         params.add(new BasicNameValuePair("height_key", height_key.getSummary().toString()));//身高 
         String constitution =constitution_key.getValue();
         if(constitution_key.getValue().equals(getString(R.string.AType))){
        	 constitution="01";
         }else if(constitution_key.getValue().equals(getString(R.string.BType))){
        	 constitution="02";
         }else if(constitution_key.getValue().equals(getString(R.string.CType))){
        	 constitution="03";
         }else if(constitution_key.getValue().equals(getString(R.string.DType))){
        	 constitution="04";
         }else if(constitution_key.getValue().equals(getString(R.string.EType))){
        	 constitution="05";
         }else if(constitution_key.getValue().equals(getString(R.string.FType))){
        	 constitution="06";
         }else if(constitution_key.getValue().equals(getString(R.string.GType))){
        	 constitution="07";
         }else if(constitution_key.getValue().equals(getString(R.string.HType))){
        	 constitution="08";
         }else if(constitution_key.getValue().equals(getString(R.string.IType))){
        	 constitution="09";
         }
         else {
        	 constitution="";
         }
         params.add(new BasicNameValuePair("constitution_key", constitution));
         try 
         { 
        	 /*发出HTTP request*/
        	 httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8)); 
        	 /*取得HTTP response*/
        	 HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest); 
        	 /*若状态码为200 ok*/
        	 if(httpResponse.getStatusLine().getStatusCode() == 200)  
        	 { 
        		 /*取出响应字符串*/
        		 String strResult = EntityUtils.toString(httpResponse.getEntity()); 
        		 Pattern p = Pattern.compile("true"); 
        		 Matcher m = p.matcher(strResult); 
        		 while(m.find())
        		 { 
        			  do_sendInfoFlag = true;
        		 }
        	 } 
        	 else 
        	 { 
        		  do_sendInfoFlag = false;
        	 } 
         } 
         catch (ClientProtocolException e) 
         {  
        	 do_sendInfoFlag = false;
        	 e.printStackTrace(); 
         } 
         catch (IOException e) 
         {  
        	 do_sendInfoFlag = false;
        	 e.printStackTrace(); 
         } 
         catch (Exception e) 
         {  
        	 do_sendInfoFlag = false;
        	 e.printStackTrace();  
         }    
    }
	public static boolean  ConnectState(Context context){
		 ConnectivityManager manager=( ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if(manager.getActiveNetworkInfo()!=null ){
			boolean bs=manager.getActiveNetworkInfo().isAvailable();
			return bs;
		}else{
			return false;
		}
	}

	private void SharedPrefCommit() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {				
				try {	
					/*
					mSelectsexPreference.setSummary(mSelectsexPreference.getEntry());
			        mInsuranceEndPreference.setSummary(mPerferences.getString("insuranceEnd_key", ""));
			        mInsuranceStartPreference.setSummary(mPerferences.getString("insuranceStart_key", ""));
			        mNamePreference.setSummary(mPerferences.getString("name_key", ""));
			        mPhoneNumPreference.setSummary(mPerferences.getString("phoneNum_key", ""));
			        mCertificateNumPreference.setSummary(mPerferences.getString("certificateNum_key", ""));
			        mCityPreference.setSummary(mPerferences.getString("city_key", ""));
			        mAddressPreference.setSummary(mPerferences.getString("address_key", ""));
			        mEmergencyPersonPreference.setSummary(mPerferences.getString("emergencyPerson_key", ""));
			        mEmergencyContactPreference.setSummary(mPerferences.getString("emergencyContact_key", ""));
			        mImeiPreference.setSummary(mPerferences.getString("imei_key", ""));
			        mRemarkPreference.setSummary(mPerferences.getString("remark_key", ""));
			        */
					
			        SharedPreferences.Editor mEditor = mPerferences.edit();		                
			        mEditor.putString("updateCmd","TSCMD4");  //命令类型4，上传客户信息内容
			        mEditor.putString("name_key",mNamePreference.getSummary().toString());  
			        mEditor.putString("selectsex_key",mSelectsexPreference.getValue());  
			        mEditor.putString("age_key", age_key.getSummary().toString());//年龄
			        mEditor.putString("height_key", height_key.getSummary().toString());//身高 
			        mEditor.putString("weight_key", weight_key.getSummary().toString());//体重
			        mEditor.putString("constitution_key", constitution_key.getValue());
			        mEditor.putString("phoneNum_key",mPhoneNumPreference.getSummary().toString());
			        mEditor.putString("certificateType_key",mCertificateTypePreference.getValue());  
			        mEditor.putString("certificateNum_key",mCertificateNumPreference.getSummary().toString()); 
			        mEditor.putString("province_key",mProvincePreference.getValue());
			        mEditor.putString("city_key",mCityPreference.getSummary().toString());  
			        mEditor.putString("address_key",mAddressPreference.getSummary().toString());  
			        mEditor.putString("emergencyPerson_key",mEmergencyPersonPreference.getSummary().toString());  
			        mEditor.putString("emergencyContact_key",mEmergencyContactPreference.getSummary().toString());  
			        //mEditor.putString("imei_key",mImeiPreference.getSummary().toString());  
			        mEditor.putString("insuranceStart_key",mInsuranceStartPreference.getSummary().toString()); 
			        mEditor.putString("insuranceEnd_key",mInsuranceEndPreference.getSummary().toString());  
			        mEditor.putString("dataTyp_key",mDataTypPreference.getValue());  
			        mEditor.putString("remark_key",mRemarkPreference.getSummary().toString()); 
			        mEditor.putString("diseaseTpye", diseaseTpye);//身体状况	
			        mEditor.putString("insurance",InsuranceTpye);  
			       
			        mEditor.commit();
			        do_SendInfo();
			        return getString(R.string.Succe);
				} catch (Exception e) {
					e.printStackTrace();
				}				
				return getString(R.string.False);
			}
			@Override
			protected void onPreExecute() {
				dialog.show();
				super.onPreExecute();
			}
			@Override
			protected void onPostExecute(String result) {
				dialog.dismiss();
				if(result == getString(R.string.Succe)){
					if(do_sendInfoFlag){
						UpOk();
						//Toast.makeText(SettingActivity.this, "上传数据成功", Toast.LENGTH_LONG).show();
					}
					else{
						UpFail();
						//Toast.makeText(SettingActivity.this, "上传数据失败", Toast.LENGTH_LONG).show();
					}
				}else {
					UpFail();
					//Toast.makeText(SettingActivity.this, "上传数据失败", Toast.LENGTH_LONG).show();
				}
				super.onPostExecute(result);
			}	
		}.execute();
	}
	
	/*
	public  void openAPN(){ 
		Uri uri = Uri.parse("content://telephony/carriers"); 
		List<APN> list = getAPNList(); 
		for (APN apn : list) { 
		ContentValues cv = new ContentValues(); 
		APNMatchTools match=new APNMatchTools();
		cv.put("apn", match.matchAPN(apn.apn)); 
		cv.put("type", match.matchAPN(apn.type)); 
		getContentResolver().update(uri, cv, "_id=?", new String[]{apn.id}); 
		} 
		} 
	
	public final class APNMatchTools { 
		public  class APNNet{ 

		public String CMWAP = "cmwap"; 

		public String CMNET = "cmnet"; 
		//中国联通3GWAP设置        中国联通3G因特网设置        中国联通WAP设置        中国联通因特网设置 
		//3gwap                 3gnet                uniwap            uninet 

		public  String GWAP_3 = "3gwap"; 

		public  String GNET_3="3gnet"; 

		public  String UNIWAP="uniwap"; 

		public String UNINET="uninet"; 
		} 
		public String matchAPN(String currentName) { 
		if("".equals(currentName) || null==currentName){ 
		return ""; 
		} 
		currentName = currentName.toLowerCase(); 
		if(currentName.startsWith("cmnet")) 
		return "cmnet"; 
		else if(currentName.startsWith("cmwap")) 
		return "cmwap"; 
		else if(currentName.startsWith("3gnet")) 
		return "3gnet"; 
		else if(currentName.startsWith("3gwap")) 
		return "3gwap"; 
		else if(currentName.startsWith("uninet")) 
		return "uninet"; 
		else if(currentName.startsWith("uninet")) 
		return "uninet"; 
		else if(currentName.startsWith("default")) 
		return "default"; 
		else return ""; 
		// return currentName.substring(0, currentName.length() - SUFFIX.length()); 
		} 
		} 


	
	
	private List<APN> getAPNList(){ 
		Uri uri = Uri.parse("content://telephony/carriers"); 
		String tag = "Main.getAPNList()"; 
		//current不为空表示可以使用的APN 
		String  projection[] = {"_id,apn,type,current"}; 
		Cursor cr = this.getContentResolver().query(uri, projection, null, null, null); 
		List<APN> list = new ArrayList<APN>(); 
		while(cr!=null && cr.moveToNext()){ 
		Log.i("life", cr.getString(cr.getColumnIndex("_id")) + "  " + cr.getString(cr.getColumnIndex("apn")) + "  " + cr.getString(cr.getColumnIndex("type"))+ "  " + cr.getString(cr.getColumnIndex("current"))); 
		APN a = new APN(); 
		a.id = cr.getString(cr.getColumnIndex("_id")); 
		a.apn = cr.getString(cr.getColumnIndex("apn")); 
		a.type = cr.getString(cr.getColumnIndex("type")); 
		list.add(a); 
		} 
		if(cr!=null) 
		cr.close(); 
		return list; 
		} 

	
	public static class APN{ 
		String id; 
		String apn; 
		String type; 

		}
	public  void OpenGprs(){
		gprsEnabled(false);


	}
	
    private boolean gprsEnabled(boolean bEnable) 
    { 
        Object[] argObjects = null; 
                 
        boolean isOpen = gprsIsOpenMethod("getMobileDataEnabled"); 
        //if(isOpen == !bEnable) 
        //{ 
            setGprsEnabled("setMobileDataEnabled", true); 
       //} 
         
        return isOpen;   
    } 

	
    //检测GPRS是否打开 
    private boolean gprsIsOpenMethod(String methodName) 
    { 
        Class cmClass       = mCM.getClass(); 
        Class[] argClasses  = null; 
        Object[] argObject  = null; 
         
        Boolean isOpen = false; 
        try 
        { 
            Method method = cmClass.getMethod(methodName, argClasses); 
 
            isOpen = (Boolean) method.invoke(mCM, argObject); 
        } catch (Exception e) 
        { 
            e.printStackTrace(); 
        } 
 
        return isOpen; 
    } 

	
	 //开启/关闭GPRS 
    public void setGprsEnabled(String methodName, boolean isEnable) 
    { 
        Class cmClass       = mCM.getClass(); 
        Class[] argClasses  = new Class[1]; 
        argClasses[0]       = boolean.class; 
         
        try 
        { 
            Method method = cmClass.getMethod(methodName, argClasses); 
            method.invoke(mCM, isEnable); 
        } catch (Exception e) 
        { 
            e.printStackTrace(); 
        } 
    } 

    public void OpenData(Context context) throws Exception{
    	Method dataConnSwitchmethod;
    	Class telephonyManagerClass;
    	Object ITelephonyStub;
    	Class ITelephonyClass;
    	boolean isEnabled;

    	    TelephonyManager telephonyManager = (TelephonyManager) context
    	            .getSystemService(Context.TELEPHONY_SERVICE);

    	    if(telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED){
    	        isEnabled = true;
    	    }else{
    	        isEnabled = false;  
    	    }   

    	    telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
    	    Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
    	    getITelephonyMethod.setAccessible(true);
    	    ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
    	    ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

    	    if (isEnabled) {
    	        dataConnSwitchmethod = ITelephonyClass
    	                .getDeclaredMethod("disableDataConnectivity");
    	    } else {
    	        dataConnSwitchmethod = ITelephonyClass
    	                .getDeclaredMethod("enableDataConnectivity");   
    	    }
    	    dataConnSwitchmethod.setAccessible(true);
    	    dataConnSwitchmethod.invoke(ITelephonyStub);
    }
    private void setMobileDataEnabled(Context context, boolean enabled) throws Exception {
        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
        iConnectivityManagerField.setAccessible(true);
        final Object iConnectivityManager = iConnectivityManagerField.get(conman);
        final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
        final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        setMobileDataEnabledMethod.setAccessible(true);

        setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
    }
    */
}

