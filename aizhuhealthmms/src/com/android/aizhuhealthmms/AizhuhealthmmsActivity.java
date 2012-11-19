package com.android.aizhuhealthmms;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.aizhuhealthmms.ListViewEntry.sms;

public class AizhuhealthmmsActivity extends Activity {
	private static String TAG = "mms cathon";
	private Button text_phone_ok;
	private EditText text_input_phone;
	private TextView text_phone;
	private TextView smscontentboday;
	private ListView fileList;
	private ListView listview;
	private int textviewsizedefault = 24;
	private ListViewEntry entry;
	private ArrayList<sms> smslist;
	// private ContcatAdapter adapter;
	private AZHMmsApplication application;
	private DataReceiver dataReceiver;// BroadcastReceiver对象
	private TextView tv;// TextView对象应用
	public static final int CMD_STOP_SERVICE = 0;
	private AdapterContextMenuInfo info;
	private ContcatAdapter contcatAdapter;
	private static final String HEALTH_SMS_NUM = "1252015817456544";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.titlebar);
		//liaobz title
		TextView textView = (TextView) findViewById(R.id.title);
		try {
			Context azContext = createPackageContext("com.az.Main",
					Context.CONTEXT_IGNORE_SECURITY);
			SharedPreferences prs = azContext.getSharedPreferences(
					"com.az.PersonInfo_preferences",
					Context.MODE_WORLD_READABLE);
			String name = prs.getString("name_key", "");
			String call = prs.getString("selectsex_key", "");
			String titleText = getString(R.string.title_template)
					.replace("{name}", name)
					.replace("{call}", getString(R.string.male).equals(call) ? getString(R.string.call_male) : 
						getString(R.string.famale).equals(call) ? getString(R.string.call_female) : "");
			textView.setText("" + titleText + "");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		setupView();
	}

	@Override
	protected void onResume() {
		SharedPreferences prs = PreferenceManager
				.getDefaultSharedPreferences(AizhuhealthmmsActivity.this);
		String text = prs.getString("text_input_phone_pr", "");
		// text_phone.setText(text);
		// contcatAdapter.notifyDataSetChanged();
		listview.setAdapter(contcatAdapter);
		Log.i(TAG, "cathon onResume " + text);

		// 重写onStart方法
		Log.i(TAG, "cathon onresume ");
		dataReceiver = new DataReceiver();
		IntentFilter filter = new IntentFilter();// 创建IntentFilter对象
		filter.addAction(AZHMmsApplication.ACTION_RECV_NEW_MSG);
		filter.addAction(AZHMmsApplication.ACTION_RECV_NEW_MSG_FOR_DIALOG);
		registerReceiver(dataReceiver, filter);// 注册Broadcast Receiver
		super.onResume();
	}

	@Override
	protected void onPause() {
		// 取消注册Broadcast Receiver
		unregisterReceiver(dataReceiver);
		super.onPause();
	}

	public void setupView() {
		application = (AZHMmsApplication) getApplication();
		contcatAdapter = application.getAdapter();

		// cathon change the phb num filter now is 15899774980 10086
		SharedPreferences prs = PreferenceManager
				.getDefaultSharedPreferences(AizhuhealthmmsActivity.this);
		Editor pr = prs.edit();
		pr.putString("text_input_phone_pr", HEALTH_SMS_NUM);
		pr.commit();

		listview = (ListView) findViewById(R.id.listview_contact_sms);
		entry = new ListViewEntry(this);
		smslist = entry.getSms();
		contcatAdapter = new ContcatAdapter(this, smslist);
		listview.setAdapter(contcatAdapter);

		listview.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu contextmenu, View view,
					ContextMenuInfo contextmenuinfo) {
				contextmenu.setHeaderTitle(R.string.smscontexttitle);
				contextmenu.add(1, 1, 1, R.string.smssendto);
				contextmenu.add(1, 2, 2, R.string.smsdelete);
				contextmenu.add(1, 3, 3, R.string.smsdeleteall);
			}
		});

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Log.i(TAG, "cathon contextoptionmenu case 1 ");
			info = (AdapterContextMenuInfo) item.getMenuInfo();

			String body = ((sms) contcatAdapter.getItem(info.position))
					.getBody();

			Uri uri = Uri.parse("smsto:");
			Intent it = new Intent(Intent.ACTION_SENDTO, uri);
			it.putExtra("sms_body", body);
			startActivity(it);

			break;

		case 2:
			// 删除数据
			info = (AdapterContextMenuInfo) item.getMenuInfo();
			int position = info.position;

			// 数据库里面删除
			int dbId = (int) contcatAdapter.getItemId(position);
			if (entry.deltesms(dbId)) {
				// 界面上删除
				smslist.remove(position);
				contcatAdapter.updateView(smslist);
				Toast.makeText(this, this.getString(R.string.smsdelete),
						Toast.LENGTH_LONG).show();
			}
			break;

		case 3:
			Log.i(TAG, "cathon contextoptionmenu case 3 ");

			smslist = new ArrayList<ListViewEntry.sms>();
			if (entry.delteallsms()) {
				contcatAdapter.updateView(smslist);
				Toast.makeText(this, this.getString(R.string.smsdeleteall),
						Toast.LENGTH_LONG).show();
			}
			break;

		}
		return super.onContextItemSelected(item);
	}

	// 继承自BroadcastReceiver的子类
	private class DataReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 重写onReceive方法
			String action = intent.getAction();
			if (AZHMmsApplication.ACTION_RECV_NEW_MSG.equals(action)) {
				smslist = entry.getSms();
				contcatAdapter.updateView(smslist);
			} else if (AZHMmsApplication.ACTION_RECV_NEW_MSG_FOR_DIALOG
					.equals(action)) {
				abortBroadcast();
			}

			Log.i(TAG, "cathon onReceive get broadcast ");
		}
	}

}
