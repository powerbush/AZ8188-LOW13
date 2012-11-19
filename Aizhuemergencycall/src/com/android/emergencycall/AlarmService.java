package com.android.emergencycall;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.az.Location.CellInfoManager;
import com.az.Location.CellLocationManager;
import com.az.Location.WifiInfoManager;
import com.az.SmsGetLocation.NetInterface;

public class AlarmService extends Service {

	// static boolean doLocationFlish = true;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		onLocation();
		return START_STICKY;
	}

	// @Override
	// public void onStart(Intent intent, int startId) {
	// onLocation();
	// super.onStart(intent, startId);
	// }

	private void onLocation() {
		// if(doLocationFlish){
		// doLocationFlish= false;

		// wifiPower = new WifiPowerManager(this);
		// wifiPower.acquire(); //��wifi��Դ
		CellInfoManager cellManager = new CellInfoManager(this);
		WifiInfoManager wifiManager = new WifiInfoManager(this);
		CellLocationManager locationManager = new CellLocationManager(this,
				cellManager, wifiManager) {
			@Override
			public void onLocationChanged() {

				String Longitude = String.valueOf(this.longitude());// ����
				String Latitude = String.valueOf(this.latitude());// γ��
				String addr = this.address();// γ��
				this.stop();

				// wifiPower.release();//�ص�wifi��Դ

				String LoginURIString = "http://210.51.7.193/io/PersonLocation.aspx";
				TelephonyManager telmgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				String imei = "IMEI:" + telmgr.getDeviceId();

				List<NameValuePair> InfoParamss = new ArrayList<NameValuePair>(); // Post�������ͱ���������NameValuePair[]���鴢��
				InfoParamss.add(new BasicNameValuePair("longitude", Longitude));
				InfoParamss.add(new BasicNameValuePair("latitude", Latitude));
				InfoParamss.add(new BasicNameValuePair("imei_key", imei));
				InfoParamss.add(new BasicNameValuePair("Address", addr));
				InfoParamss.add(new BasicNameValuePair("IsEmergency", "1"));

				new NetInterface().SendInfoToNet(LoginURIString, InfoParamss);

				// stopSelf();

				// doLocationFlish= true;
			}
		};

		locationManager.start();
		// }
	}
}
