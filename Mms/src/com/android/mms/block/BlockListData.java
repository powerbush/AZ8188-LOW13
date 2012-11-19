package com.android.mms.block;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

 
public class BlockListData {
	
	private Context context;
	public BlockListData(Context context){	
		this.context=context;
	}
	
	public void MoveSMSInfo(String phoneAddress, String smsBody)
	{
		SQLiteDatabase db=this.context.openOrCreateDatabase("urgency_contact.db", Context.MODE_WORLD_READABLE+Context.MODE_WORLD_WRITEABLE, null);

		db.execSQL("create table if not exists contact(" +
							"_id integer primary key autoincrement,"+
							"phone text not null,"+
								"body text not null"+")");

		ContentValues values=new ContentValues();
		values.put("phone", ""+phoneAddress);
		values.put("body", ""+smsBody);
		db.insert("contact", null, values);
		db.close();
	}
		
	public ArrayList<sms> getSms(){
		SQLiteDatabase db=context.openOrCreateDatabase("urgency_contact.db", Context.MODE_WORLD_READABLE+Context.MODE_WORLD_WRITEABLE, null);
		db.execSQL("create table if not exists contact(" +
				"_id integer primary key autoincrement,"+
				"phone text not null,"+
					"body text not null"+")");
		Cursor c=db.query("contact", null, null, null, null, null, null);
		
		if(c!=null){
			ArrayList<sms> smsList=new ArrayList<BlockListData.sms>();
			while(c.moveToNext()){
				sms s=new sms();
				//String phone=c.getString(c.getColumnIndex("phone"));
				//s.setPhone(phone);
				String body =c.getString(c.getColumnIndex("body"));
				s.setBody(body);
				smsList.add(s);
				
			}
			c.close();
			db.close();
			return smsList;
			
		}else{
			ArrayList<sms> smsl=new ArrayList<BlockListData.sms>();
			return smsl ;
		}
	}
	
	
	public class sms{
		//String phone;
		String body;
		//public String getPhone() {
			//return phone;
		//}
		//public void setPhone(String phone) {
			//this.phone = phone;
		//}
		public String getBody() {
			return body;
		}
		public void setBody(String body) {
			this.body = body;
		}
	}
}
