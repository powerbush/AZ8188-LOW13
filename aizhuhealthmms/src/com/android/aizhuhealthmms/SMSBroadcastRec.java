package com.android.aizhuhealthmms;

import java.io.IOException;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;


public class SMSBroadcastRec extends BroadcastReceiver{
	private static String TAG = "mms cathon";
	private SmsMessage msg;
	private String phoneAddress;
	private String phonesmscontent=null;
	MyThread m;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//abortBroadcast(); //拦截短信不让Broadcast往下发
		Log.i(TAG, "cathon SMSBroadcastRec get begin ");
		if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
			
			Log.i(TAG, "cathon SMSBroadcastRec get smsreceviemsg ");
			
			Bundle bundle=intent.getExtras();
			Object[] objs=(Object[]) bundle.get("pdus");
			for(Object ob:objs){
				byte[] pdu=(byte[])ob;
				msg=SmsMessage.createFromPdu(pdu);
				phoneAddress=msg.getDisplayOriginatingAddress();
				phonesmscontent=msg.getDisplayMessageBody();
				
				Log.i(TAG, "cathon SMSBroadcastRec handle get num " + phoneAddress);
				Log.i(TAG, "cathon SMSBroadcastRec handle msg " + msg.getDisplayMessageBody());				

//				if(msg.getDisplayMessageBody().contains("gps")){
//					Intent SmsIntent = new Intent(context,SMSService.class);
//					SmsIntent.putExtra("phoneAddress", phoneAddress);
//					context.startService(SmsIntent);
//				}

				SharedPreferences prs= PreferenceManager.getDefaultSharedPreferences(context);
				String textphone=prs.getString("text_input_phone_pr", "");
				
				Log.i(TAG, "cathon broadcast " + textphone + "  " +phoneAddress);
				
				//textphone cathon define thephonenumber 15899774980
				if(phoneAddress.equals(textphone)||phoneAddress.equals("+86"+textphone)){
					
					abortBroadcast(); //拦截短信不让Broadcast往下发
					
					m = new MyThread(context);
					m.start();
					
					
					Intent SmsIntent = new Intent(context,SMSService.class);
					SmsIntent.putExtra("phoneAddress", phoneAddress);
					SmsIntent.putExtra("phonesmscontent", phonesmscontent);
					context.startService(SmsIntent);					
					
				}				
				//if(phoneAddress=="+8615899774980"){

				//}
				
			}
		}
		
	}
	

	class MyThread extends Thread{
		MediaPlayer mMediaPlayer;
		Context context;

		public MyThread(Context mContext) {
		mMediaPlayer = new MediaPlayer();
		this.context = mContext;
		}

		@Override
		public void run() {
			Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			try {
				mMediaPlayer.setDataSource(context, alert);
				final AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
				if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mMediaPlayer.setLooping(false);
				mMediaPlayer.prepare();
				}
			} catch (IllegalStateException e) {
			e.printStackTrace();
			} catch (IOException e) {
			e.printStackTrace();
			}
			mMediaPlayer.start();
		}
		
		private void StopAlarmRing() {
		mMediaPlayer.stop();
		}

	}

	
}	
	
