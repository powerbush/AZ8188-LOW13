package com.android.contacts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GalleryEmergencyEntry {
	private Context context;
	public GalleryEmergencyEntry(Context context) {
		this.context = context;
	}

	public ArrayList<GalleryContactEntry> getContactPhones() {
		
		//String databaseFilename = "/data/data/com.az.Main/databases/paddy_database";
		String databaseFilename = "/data/data/com.az.Main/databases/emergencyphb.db";
		
		/*
	    File dir = new File("/data/data/com.az.Main/databases");
	    // �ж��ļ����Ƿ���ڣ������ھ��½�һ��
	    if (!dir.exists())    
	         dir.mkdir();
	    //�ж����ݿ��ļ��Ƿ���ڣ�����������ִ�е��룬����ֱ�Ӵ����ݿ�
	    if (!(new File(databaseFilename)).exists()) {    
	    	
	    	Log.i("gancuirong", "will new file======================");
	    	
	    	// ��÷�װpaddy_database.db�ļ���InputStream����
		     InputStream is = context.getResources().openRawResource(R.raw.paddy_database);    
		     Log.i("gancuirong", "InputStream is="+is);
		     
		     // �õ����ݿ��ļ���д���� 
		     FileOutputStream fos = new FileOutputStream(databaseFilename);    
		     Log.i("gancuirong", "FileOutputStream fos"+fos);
		    
		     byte[] buffer = new byte[8192];
		     int count = 0;
		     // ��ʼ����paddy_database.db�ļ�
	         while ((count = is.read(buffer)) > 0) {
	              fos.write(buffer, 0, count);
	         }
	         fos.close();
	         is.close();
	    }
	    */
		
	    ArrayList<GalleryContactEntry> phones = new ArrayList<GalleryContactEntry>();
	    try{
		
			SQLiteDatabase db = context.openOrCreateDatabase(databaseFilename,Context.MODE_WORLD_WRITEABLE + Context.MODE_WORLD_READABLE,null);
			Log.i("gancuirong", "db======databaseFilename"+db);
			
			//Cursor cursor = db.query("database01",new String[]{"nameId","nameFlag","name"},"nameId='phoneId'",null,null,null,null);
			Cursor cursor=db.query("emerphb", new String[]{"_id","name","phonenum","photo"}, null, null, null, null, null);
			Log.i("gancuirong", cursor+"cursor=============");
			if(cursor!=null){
				while(cursor.moveToNext()){
					GalleryContactEntry phoneContactEntry = new GalleryContactEntry();
					// phoneContactEntry.setContactPhone(cursor.getString(cursor.getColumnIndex("nameId")));//��ϵ�˵ĵ绰����
					// phoneContactEntry.setImageId(Integer.valueOf(phoneContactEntry.getContactPhone().substring(2)));//Ψһ��ʶ 
					// phoneContactEntry.setContactName(cursor.getString(cursor.getColumnIndex("nameFlag")));//��ϵ�˵�����
					
					phoneContactEntry.setImageId(cursor.getInt(cursor.getColumnIndex("_id")));
					phoneContactEntry.setContactPhone(cursor.getString(cursor.getColumnIndex("phonenum")));
					phoneContactEntry.setContactName(cursor.getString(cursor.getColumnIndex("name")));
					
					phones.add(phoneContactEntry);
				}
				cursor.close();
			}		
			db.close();
		}
		catch(Exception e){}
		return phones;
	}


}
