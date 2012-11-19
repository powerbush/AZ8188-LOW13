package com.android.aizhuhealthmms;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ListViewEntry {
	private static String TAG = "mms cathon";

	private Context context;

	private int smscnt = 0;

	public ListViewEntry(Context context) {
		this.context = context;
	}

	public ArrayList<sms> getSms() {
		SQLiteDatabase db = context.openOrCreateDatabase("urgency_contact.db",
				Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE,
				null);
		db.execSQL("create table if not exists health_msg("
				+ "_id integer primary key autoincrement,"
				+ "smscnt text not null," + "recvtime long not null,"
				+ "phone text not null," + "body text not null" + ")");

		// 查询数据库,构建实体类list
		Cursor c = db.query("health_msg", null, null, null, null, null,
				" recvtime desc ");
		if (c != null) {
			ArrayList<sms> smsList = new ArrayList<ListViewEntry.sms>();
			while (c.moveToNext()) {
				sms s = new sms();
				s.setId(c.getInt(c.getColumnIndex("_id")));
				s.setBody(c.getString(c.getColumnIndex("body")));
				s.setRevtime(c.getLong(c.getColumnIndex("recvtime")));
				s.setPhone(c.getString(c.getColumnIndex("phone")));
				smsList.add(s);
			}
			c.close();
			db.close();
			return smsList;
		} else {
			ArrayList<sms> smsl = new ArrayList<ListViewEntry.sms>();
			return smsl;
		}
	}

	public boolean deltesms(int indexdelte) {
		SQLiteDatabase db = context.openOrCreateDatabase("urgency_contact.db",
				Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE,
				null);
		// 删除指定行
		try {
			db.execSQL("delete from health_msg where _id = " + indexdelte);
		} catch (Exception e) {
			Log.i(TAG + "(liaobz)", e.getMessage());
			return false;
		}
		return true;
	}

	public boolean delteallsms() {
		SQLiteDatabase db = context.openOrCreateDatabase("urgency_contact.db",
				Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE,
				null);
		// 删除所有行
		try {
			db.execSQL("delete from health_msg");
		} catch (Exception e) {
			Log.i(TAG + "(liaobz)", e.getMessage());
			return false;
		}
		return true;
	}

	public class sms {
		int id;
		long recvtime;
		String phone;
		String body;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public long getRecvtime() {
			return recvtime;
		}

		public void setRevtime(long recvtime) {
			this.recvtime = recvtime;
		}

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
