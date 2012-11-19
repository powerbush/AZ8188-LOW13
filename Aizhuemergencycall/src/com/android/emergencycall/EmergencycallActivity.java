package com.android.emergencycall;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;

public class EmergencycallActivity extends Activity {
	private String mobile = null;
	private String databaseFilename = "/data/data/com.az.Main/databases/emergencyphb.db";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.main);
		mobile = "10086";
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
				+ mobile));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		startActivity(intent);

		// ������������
		int count = 0;
		SmsManager smsManager = SmsManager.getDefault();
		ArrayList<String> phones = new ArrayList<String>();
		try {
			SQLiteDatabase db = openOrCreateDatabase(databaseFilename,
					Context.MODE_WORLD_WRITEABLE + Context.MODE_WORLD_READABLE,
					null);

			Cursor cursor = db.query("emerphb", new String[] { "phonenum" },
					null, null, null, null, null);
			if (cursor != null) {
				while (cursor.moveToNext() && count++ < 3) {
					smsManager
							.sendTextMessage(cursor.getString(cursor
									.getColumnIndex("phonenum")), null, "��"
									+ count + "����������", null, null);
				}
				cursor.close();
			}
			db.close();
		} catch (Exception e) {
		}

		// ����������λ����,�Զ��ϴ�������
		startService(new Intent(this, AlarmService.class));
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();

	}
}