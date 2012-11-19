package com.android.aizhuhealthmms;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ListViewEntry {
	private static String TAG = "mms cathon";
	
	private Context context;

	private int smscnt=0;
	public ListViewEntry(Context context){
		this.context=context;
	}
	
	public ArrayList<sms> getSms(){
		SQLiteDatabase db=context.openOrCreateDatabase("urgency_contact.db", Context.MODE_WORLD_READABLE+Context.MODE_WORLD_WRITEABLE, null);
		db.execSQL("create table if not exists contact(" +
				"_id integer primary key autoincrement,"+
				"smscnt text not null,"+
				"phone text not null,"+
					"body text not null"+")");
		Cursor c=db.query("contact", null, null, null, null, null, null);
		
		Log.i(TAG, "cathon get sqldb  "+ c);
		
		if(c!=null){
			ArrayList<sms> smsList=new ArrayList<ListViewEntry.sms>();
			while(c.moveToNext()){
				sms s=new sms();
				String phone=c.getString(c.getColumnIndex("phone"));
				Log.i(TAG, "cathon get sqldb  "+ phone);
				s.setPhone(phone);
				String body =c.getString(c.getColumnIndex("body"));
				Log.i(TAG, "cathon get sqldb  "+ body);
				s.setBody(body);
				smsList.add(s);
			}
			c.close();
			db.close();
			return smsList;
			
		}else{
			ArrayList<sms> smsl=new ArrayList<ListViewEntry.sms>();
			return smsl ;
		}
	}
	
	public ArrayList<sms> deltesms(int indexdelte){
		SQLiteDatabase db=context.openOrCreateDatabase("urgency_contact.db", Context.MODE_WORLD_READABLE+Context.MODE_WORLD_WRITEABLE, null);
		db.execSQL("delete from contact where _id = " + indexdelte);
		//db.execSQL("delete from  database01 where name='"+con.getName()+"'");
		Cursor c=db.query("contact", null, null, null, null, null, null);
		
		Log.i(TAG, "cathon get sqldb  "+ c);
		
		if(c!=null){
			ArrayList<sms> smsList=new ArrayList<ListViewEntry.sms>();
			while(c.moveToNext()){
				sms s=new sms();
				
				smscnt=c.getInt(c.getColumnIndex("_id"));
				Log.i(TAG, "cathon delete sms " + smscnt);
				
				String phone=c.getString(c.getColumnIndex("phone"));
				Log.i(TAG, "cathon get sqldb  "+ phone);
				s.setPhone(phone);
				String body =c.getString(c.getColumnIndex("body"));
				Log.i(TAG, "cathon get sqldb  "+ body);
				s.setBody(body);
				smsList.add(s);
			}
			c.close();
			db.close();
			return smsList;
			
		}else{
			ArrayList<sms> smsl=new ArrayList<ListViewEntry.sms>();
			return smsl ;
		}
	}

	public ArrayList<sms> delteallsms(){
		SQLiteDatabase db=context.openOrCreateDatabase("urgency_contact.db", Context.MODE_WORLD_READABLE+Context.MODE_WORLD_WRITEABLE, null);
		db.execSQL("delete from contact");
		Cursor c=db.query("contact", null, null, null, null, null, null);
		
		Log.i(TAG, "cathon get sqldb  "+ c);
		
		if(c!=null){
			ArrayList<sms> smsList=new ArrayList<ListViewEntry.sms>();
			while(c.moveToNext()){
				sms s=new sms();
				
				smscnt=c.getInt(c.getColumnIndex("smscnt"));
				Log.i(TAG, "cathon delete sms " + smscnt);
				
				String phone=c.getString(c.getColumnIndex("phone"));
				Log.i(TAG, "cathon get sqldb  "+ phone);
				s.setPhone(phone);
				String body =c.getString(c.getColumnIndex("body"));
				Log.i(TAG, "cathon get sqldb  "+ body);
				s.setBody(body);
				smsList.add(s);
			}
			c.close();
			db.close();
			return smsList;
			
		}else{
			ArrayList<sms> smsl=new ArrayList<ListViewEntry.sms>();
			return smsl ;
		}
	}
	
	public class sms{
		String phone;
		String body;
		public String getPhone() {
			return phone;
		}
		public void setPhone(String phone) {
			this.phone = phone;
		}
		public String getBody() {
			return body;
		}
		public void setBody(String body) {
			this.body = body;
		}
	}
}
