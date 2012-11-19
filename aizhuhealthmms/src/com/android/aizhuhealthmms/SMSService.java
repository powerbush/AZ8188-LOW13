package com.android.aizhuhealthmms;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Application;
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

public class SMSService extends Service {
	private static String TAG = "mms cathon";
	private String GpsAddress;
	private String Longitude;// 经度
	private String Latitude;// 纬度
	private String phoneAddress;
	private String phonesmscontent = null;
	public static boolean SmsEvenFish = false;
	public SQLiteDatabase db;
	private int smscnt = 0;
	public static final String ACTION_SMS_UPDATE = "com.android.emergencysms.update";
	CommandReceiver cmdReceiver;
	private boolean flag;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {// 重写onCreate方法
		flag = true;
		// cmdReceiver = new CommandReceiver();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		phoneAddress = intent.getStringExtra("phoneAddress");
		phonesmscontent = intent.getStringExtra("phonesmscontent");
		Log.i(TAG, "cathon SMSService onstart");
		// if(CheckPhoneNumvAlidity()){
		// //GetLocation();
		// }

		MoveSMSInfo();

		// IntentFilter filter = new IntentFilter();//创建IntentFilter对象
		// filter.addAction("com.android.aizhuhealthmms");
		// registerReceiver(cmdReceiver, filter);//注册Broadcast Receiver

		// doJob();//调用方法启动线程

		// 发送信息已更新广播
		Intent intentForDialog = new Intent(
				AZHMmsApplication.ACTION_RECV_NEW_MSG_FOR_DIALOG);
		// sendBroadcast(intentForDialog);
		sendOrderedBroadcast(intentForDialog, null);

		super.onStart(intent, startId);
	}

	public void doJob() {
		new Thread() {
			public void run() {
				while (flag) {
					try {// 睡眠一段时间
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
					Intent intent = new Intent();// 创建Intent对象
					intent.setAction("com.android.aizhuhealthmms");
					intent.putExtra("data", Math.random());
					sendBroadcast(intent);// 发送广播
				}
			}

		}.start();
	}

	private void MoveSMSInfo() {
		// 服务器给的号码将时将短信内容储存显示在另外地方 --测试用12520.. 实际用服务器端提供
		SharedPreferences prs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String textphone = prs.getString("text_input_phone_pr", "");

		Log.i(TAG, "cathon sms compare " + textphone + "  " + phoneAddress);

		// textphone cathon define thephonenumber 15899774980
		if (phoneAddress.equals(textphone)
				|| phoneAddress.equals("+86" + textphone)) {
			SQLiteDatabase db = this.openOrCreateDatabase("urgency_contact.db",
					Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE,
					null);

			db.execSQL("create table if not exists health_msg("
					+ "_id integer primary key autoincrement,"
					+ "smscnt text not null," + "recvtime long not null,"
					+ "phone text not null," + "body text not null" + ")");
			smscnt = smscnt + 1;
			ContentValues values = new ContentValues();
			Log.i(TAG, "cathon save sms to dbase " + smscnt);
			values.put("smscnt", "" + smscnt);

			values.put("recvtime", System.currentTimeMillis());
			values.put("phone", "" + phoneAddress);
			values.put("body", "" + phonesmscontent);
			db.insert("health_msg", null, values);
			db.close();
		}
		// 发送信息已更新广播
		Intent intent = new Intent(AZHMmsApplication.ACTION_RECV_NEW_MSG);
		sendBroadcast(intent);

	}

	// private void broadcastupdatesms(){
	// //int appWidgetId =
	// getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, INVALID);
	//
	// final Runnable sendbro = new Runnable() {
	// public void run() {
	// // delete all cache files
	// Intent broadcast = new Intent(ACTION_SMS_UPDATE);
	// sendBroadcast(broadcast);
	// //finish();
	// }
	// };
	//
	// new Thread(sendbro).start();
	// Log.i(TAG, "broadcast weatherwidget send");
	// }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {// 重写onStartCommand方法
		IntentFilter filter = new IntentFilter();// 创建IntentFilter对象
		filter.addAction("com.android.aizhuhealthmms");
		registerReceiver(cmdReceiver, filter);// 注册Broadcast Receiver
		// doJob();//调用方法启动线程
		Log.i(TAG, "cathon service onStartCommand ");
		return super.onStartCommand(intent, flags, startId);
	}

	// 继承自BroadcastReceiver的子类
	private class CommandReceiver extends BroadcastReceiver {
		@Override
		// 重写onReceive方法
		public void onReceive(Context context, Intent intent) {
			// 获取Extra信息
			int cmd = intent.getIntExtra("cmd", -1);

			Log.i(TAG, "cathon service CommandReceiver " + cmd);
			// 如果发来的消息是停止服务
			if (cmd == AizhuhealthmmsActivity.CMD_STOP_SERVICE) {
				flag = false;// 停止线程
				stopSelf();// 停止服务
			}
		}
	}

}
