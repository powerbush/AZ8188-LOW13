package com.android.aizhuhealthmms;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import java.lang.Thread;
import android.util.Log;
import android.content.BroadcastReceiver;

public class SMSService extends Service{
	private static String TAG = "mms cathon";
	private String GpsAddress;
	private String Longitude;//����
	private String Latitude;//γ��
	private String phoneAddress;
	private String phonesmscontent=null;
	public static boolean SmsEvenFish = false;
	public SQLiteDatabase db;
	private int smscnt=0;
	public static final String ACTION_SMS_UPDATE = "com.android.emergencysms.update";
	CommandReceiver cmdReceiver;
	private boolean flag;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

    @Override
    public void onCreate() {//��дonCreate����
            flag = true;
            //cmdReceiver = new CommandReceiver();
            super.onCreate();
    }
    
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		phoneAddress = intent.getStringExtra("phoneAddress");
		phonesmscontent=intent.getStringExtra("phonesmscontent");;
		Log.i(TAG, "cathon SMSService onstart");
//		if(CheckPhoneNumvAlidity()){
//			//GetLocation();
//		}

		MoveSMSInfo();

//        IntentFilter filter = new IntentFilter();//����IntentFilter����
//        filter.addAction("com.android.aizhuhealthmms");
//        registerReceiver(cmdReceiver, filter);//ע��Broadcast Receiver
        
        //doJob();//���÷��������߳�
        
		super.onStart(intent, startId);
	}

    public void doJob(){
        new Thread(){
                public void run(){
                        while(flag){
                                try{//˯��һ��ʱ��
                                        Thread.sleep(1000);
                                }
                                catch(Exception e){
                                        e.printStackTrace();
                                }
                                Intent intent = new Intent();//����Intent����
                                intent.setAction("com.android.aizhuhealthmms");
                                intent.putExtra("data", Math.random());
                                sendBroadcast(intent);//���͹㲥
                        }
                }
               
        }.start();
    }           
    
	private void MoveSMSInfo()
	{
		//���������ĺ��뽫ʱ���������ݴ�����ʾ������ط�  --������13528750390 ʵ���÷��������ṩ
		SharedPreferences prs= PreferenceManager.getDefaultSharedPreferences(this);
		String textphone=prs.getString("text_input_phone_pr", "");
		
		Log.i(TAG, "cathon sms compare " + textphone + "  " +phoneAddress);
		
		//textphone cathon define thephonenumber 15899774980
		if(phoneAddress.equals(textphone)||phoneAddress.equals("+86"+textphone)){
			SQLiteDatabase db=this.openOrCreateDatabase("urgency_contact.db", MODE_PRIVATE, null);

			db.execSQL("create table if not exists contact(" +
								"_id integer primary key autoincrement,"+
								"smscnt text not null,"+
								"phone text not null,"+
									"body text not null"+")");
			smscnt=smscnt+1;
			ContentValues values=new ContentValues();
			Log.i(TAG, "cathon save sms to dbase " + smscnt);			
			values.put("smscnt", ""+ smscnt);
			
			values.put("phone", ""+	phoneAddress);
			values.put("body", ""+	phonesmscontent);
			db.insert("contact", null, values);
			db.close();
		}
		
		//broadcastupdatesms();
		
	}

//	private void broadcastupdatesms(){
//		//int appWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, INVALID);
//
//        final Runnable sendbro = new Runnable() {
//            public void run() {
//                // delete all cache files
//                    Intent broadcast = new Intent(ACTION_SMS_UPDATE);
//            		sendBroadcast(broadcast);
//                    //finish();
//            }
//        };
//
//	    new Thread(sendbro).start();
//        Log.i(TAG, "broadcast weatherwidget send");
//	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {//��дonStartCommand����
            IntentFilter filter = new IntentFilter();//����IntentFilter����
            filter.addAction("com.android.aizhuhealthmms");
            registerReceiver(cmdReceiver, filter);//ע��Broadcast Receiver
            //doJob();//���÷��������߳�
            Log.i(TAG, "cathon service onStartCommand ");
            return super.onStartCommand(intent, flags, startId);
    }
    
    private class CommandReceiver extends BroadcastReceiver{//�̳���BroadcastReceiver������
        @Override
        public void onReceive(Context context, Intent intent) {//��дonReceive����
                int cmd = intent.getIntExtra("cmd", -1);//��ȡExtra��Ϣ
                
                Log.i(TAG, "cathon service CommandReceiver " + cmd);
                
                if(cmd == AizhuhealthmmsActivity.CMD_STOP_SERVICE){//�����������Ϣ��ֹͣ����                               
                        flag = false;//ֹͣ�߳�
                        stopSelf();//ֹͣ����
                }
        }
    }
    
  
    
}