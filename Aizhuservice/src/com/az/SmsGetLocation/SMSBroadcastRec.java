package com.az.SmsGetLocation;

import java.util.ArrayList;

import com.az.TimingUpGps.AlarmService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsMessage;


public class SMSBroadcastRec extends BroadcastReceiver{

	private SmsMessage msg;
	private String phoneAddress;
	private Context context;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		this.context=context;
		if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
			Bundle bundle=intent.getExtras();
			Object[] objs=(Object[]) bundle.get("pdus");
			for(Object ob:objs){
				byte[] pdu=(byte[])ob;
				msg=SmsMessage.createFromPdu(pdu);
				phoneAddress=msg.getDisplayOriginatingAddress();
				if(msg.getDisplayMessageBody().contains("gps")){
					if(CheckPhoneNumvAlidity()==true){
						abortBroadcast(); //���ض��Ų���Broadcast���·�
						Intent SmsIntent = new Intent(this.context,SMSService.class);
						SmsIntent.putExtra("phoneAddress", phoneAddress);
						this.context.startService(SmsIntent);
					}
				}
			}
		}
	}
	
	private boolean CheckPhoneNumvAlidity()//ע��˴�����̫��ʱ
	{
		SQLiteDatabase sqldb=this.context.openOrCreateDatabase("paddy_database",1, null);
		Cursor cur = sqldb.query("database01",new String[]{"nameId","name"},"nameId='phoneId'",null,null,null,null);	 
		ArrayList<String> ar=new ArrayList<String>();
		//�Ƿ��ǽ�����ϵ�����ݿⱣ��ĵ绰
		 boolean availability = false;
		 //���ݿ��е�
		while(cur.moveToNext()){
			ar.add(cur.getString(cur.getColumnIndex("name")));
			//Log.i("life",cur.getString(cur.getColumnIndex("name")));
			String s=cur.getString(cur.getColumnIndex("name"));
			if(s.contains(phoneAddress)||("+86"+s).contains(phoneAddress)){
				availability=true;
			}
		}
		cur.close();
		sqldb.close();
		return availability;
	}
	
}	
	
