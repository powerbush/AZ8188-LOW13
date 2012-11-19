package com.az.TimingUpGps;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.az.Location.CellInfoManager;
import com.az.Location.CellLocationManager;
import com.az.Location.WifiInfoManager;
import com.az.Location.WifiPowerManager;
import com.az.SmsGetLocation.NetInterface;
import com.az.Main.R;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AlarmService extends Service{
	
	static boolean doLocationFlish = true;
	//WifiPowerManager wifiPower = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void onDestroy() {
		//stopSelf();
		super.onDestroy();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		onLocation();
		super.onStart(intent, startId);
	}

	private void onLocation() {
			//if(doLocationFlish){
				//doLocationFlish= false; 	
				
				//wifiPower = new WifiPowerManager(this);
				//wifiPower.acquire(); //��wifi��Դ
				
				CellInfoManager cellManager = new CellInfoManager(this);
				WifiInfoManager wifiManager = new WifiInfoManager(this);
				CellLocationManager locationManager = new CellLocationManager(this, cellManager, wifiManager) {
				@Override
				public void onLocationChanged() {
					  
					  String Longitude=String.valueOf(this.longitude());//����
					  String Latitude=String.valueOf(this.latitude());//γ��
					  String addr=this.address();//γ��
					  this.stop();
					  
					  //wifiPower.release();//�ص�wifi��Դ
					  
					  String LoginURIString = getString(R.string.PersonLocation);
					  TelephonyManager telmgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
					  String imei = "IMEI:" + telmgr.getDeviceId();	              
					         
					  List <NameValuePair> InfoParamss = new ArrayList <NameValuePair>(); //Post�������ͱ���������NameValuePair[]���鴢��
					  InfoParamss.add(new BasicNameValuePair("longitude", Longitude)); 
					  InfoParamss.add(new BasicNameValuePair("latitude", Latitude));
					  InfoParamss.add(new BasicNameValuePair("imei_key", imei));
					  InfoParamss.add(new BasicNameValuePair("Address", addr));
					  
					  new NetInterface().SendInfoToNet(LoginURIString,InfoParamss);
					  
					  //stopSelf();
					  
					  //doLocationFlish= true; 
				}
			};
			locationManager.start();
		//}
	}
	
}
