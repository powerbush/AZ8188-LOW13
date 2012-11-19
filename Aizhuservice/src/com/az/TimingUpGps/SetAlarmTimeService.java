package com.az.TimingUpGps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.az.SmsGetLocation.SMSBroadcastRec;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Proxy;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SetAlarmTimeService extends Service{
	
	public Context con;
	public SQLiteDatabase db;
	public AlarmManager an;
	private SMSBroadcastRec receiver;

	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}
	/*
	1. settings 中stop service
		onDestroy方法中，调用startService进行Service的重启。
	2.settings中force stop 应用
		捕捉系统进行广播（action为android.intent.action.PACKAGE_RESTARTED）
	3. 借助第三方应用kill掉running task
		提升service的优先级
	 */
    public int onStartCommand(Intent intent, int flags, int startId) {
            // TODO Auto-generated method stub
            //Log.v("TrafficService","startCommand");
            
            flags =  START_STICKY;//START_STICKY是service被kill掉后自动重写创建
            return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {   
        Intent localIntent = new Intent();
        localIntent.setClass(this, SetAlarmTimeService.class);  //销毁时重新启动Service
        this.startService(localIntent);
    }

	@Override
	public void onStart(Intent intent, int startId) {
		//Log.i("life", "Startservice启动了");
		setForeground(true);
		/*receiver=new SMSBroadCastReceiver();
		
		IntentFilter filter =new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(2147483647);
		
		registerReceiver(receiver, filter);*/
		
		TimeZone tz=new TimeZone();
		IntentFilter tzfilter=new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
		registerReceiver(tz, tzfilter);
		
		DataChange dc=new DataChange();
		IntentFilter dcfilter=new IntentFilter(Intent.ACTION_DATE_CHANGED);
		registerReceiver(dc, dcfilter);
		
		TimeMinChange tm=new TimeMinChange();
		IntentFilter tmfilter=new IntentFilter(Intent.ACTION_DATE_CHANGED);
		tmfilter.addAction(Intent.ACTION_TIME_CHANGED);
		registerReceiver(tm, tmfilter);
		
		
		//建立一个定时器
			Calendar c=Calendar.getInstance();
			Calendar calendar=Calendar.getInstance();
			c.set(1500, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),  calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)+2, calendar.get(Calendar.SECOND));
			Log.i("life", " "+calendar.get(Calendar.YEAR)+calendar.get(Calendar.MONTH)+calendar.get(Calendar.DAY_OF_MONTH)+calendar.get(Calendar.HOUR_OF_DAY)+calendar.get(Calendar.MINUTE)+2+calendar.get(Calendar.SECOND));
			Intent alertIntent=new Intent(this,AlarmBroadCast.class);
			PendingIntent pi=PendingIntent.getBroadcast(this, 0, alertIntent, 0);
			
			an=(AlarmManager) getSystemService(ALARM_SERVICE);
			an.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 600000,pi);//600000
			
			super.onStart(intent, startId);
	}
	//时区更改广播
	
	public class TimeZone extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())){
				//建立一个定时器
				Calendar c=Calendar.getInstance();
				Calendar calendar=Calendar.getInstance();
				c.set(1500, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),  calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)+2, calendar.get(Calendar.SECOND));
				Log.i("life", " "+calendar.get(Calendar.YEAR)+calendar.get(Calendar.MONTH)+calendar.get(Calendar.DAY_OF_MONTH)+calendar.get(Calendar.HOUR_OF_DAY)+calendar.get(Calendar.MINUTE)+2+calendar.get(Calendar.SECOND));
				Intent alertIntent=new Intent(SetAlarmTimeService.this,AlarmBroadCast.class);
				PendingIntent pi=PendingIntent.getBroadcast(SetAlarmTimeService.this, 0, alertIntent, 0);
				
					an=(AlarmManager) getSystemService(ALARM_SERVICE);
					an.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 600000,pi);
				
			}
			
		}
		
	}
	//日期更改
	public class DataChange extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(Intent.ACTION_DATE_CHANGED.equals(intent.getAction())){
				//建立一个定时器
				Calendar c=Calendar.getInstance();
				Calendar calendar=Calendar.getInstance();
				c.set(1500, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),  calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)+2, calendar.get(Calendar.SECOND));
				Log.i("life", " "+calendar.get(Calendar.YEAR)+calendar.get(Calendar.MONTH)+calendar.get(Calendar.DAY_OF_MONTH)+calendar.get(Calendar.HOUR_OF_DAY)+calendar.get(Calendar.MINUTE)+2+calendar.get(Calendar.SECOND));
				Intent alertIntent=new Intent(SetAlarmTimeService.this,AlarmBroadCast.class);
				PendingIntent pi=PendingIntent.getBroadcast(SetAlarmTimeService.this, 0, alertIntent, 0);
				
					an=(AlarmManager) getSystemService(ALARM_SERVICE);
					an.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 600000,pi);
			}
		}
		
	}
	//时间更改
	public class TimeMinChange extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Calendar c=Calendar.getInstance();
			Calendar calendar=Calendar.getInstance();
			c.set(1500, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),  calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)+2, calendar.get(Calendar.SECOND));
			Log.i("life", " "+calendar.get(Calendar.YEAR)+calendar.get(Calendar.MONTH)+calendar.get(Calendar.DAY_OF_MONTH)+calendar.get(Calendar.HOUR_OF_DAY)+calendar.get(Calendar.MINUTE)+2+calendar.get(Calendar.SECOND));
			Intent alertIntent=new Intent(SetAlarmTimeService.this,AlarmBroadCast.class);
			PendingIntent pi=PendingIntent.getBroadcast(SetAlarmTimeService.this, 0, alertIntent, 0);
			
				an=(AlarmManager) getSystemService(ALARM_SERVICE);
				an.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 600000,pi);
			
		}
		
	}
	
}
