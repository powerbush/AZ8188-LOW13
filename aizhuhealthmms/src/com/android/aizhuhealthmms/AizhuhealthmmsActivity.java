package com.android.aizhuhealthmms;

import java.util.ArrayList;

import com.android.aizhuhealthmms.R;
import com.android.aizhuhealthmms.ListViewEntry.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AizhuhealthmmsActivity extends Activity{
	private static String TAG = "mms cathon";	
	private Button text_phone_ok;
	private EditText text_input_phone;
	private TextView text_phone;
	private TextView smscontentboday;
	private ListView fileList;  
	private ListView listview;
	private int textviewsizedefault=24;
	private ListViewEntry entry;
	private ArrayList<sms>	smslist;
	private ContcatAdapter adapter;
	private DataReceiver dataReceiver;//BroadcastReceiver����
	private TextView tv;//TextView����Ӧ��
	public static final int CMD_STOP_SERVICE = 0;
	private AdapterContextMenuInfo info;
	
	@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			Log.i(TAG, "cathon AizhuhealthmmsActivity oncreate");
			//smscontentboday=(TextView)findViewById(R.id.body_content_sms);
			//ItemOnLongClick2(); 
			setContentView(R.layout.main);
			setupView();
		}
	
		@Override
		protected void onResume() {
			SharedPreferences prs= PreferenceManager.getDefaultSharedPreferences(AizhuhealthmmsActivity.this);
			String text=prs.getString("text_input_phone_pr", "");
			//text_phone.setText(text);
			Log.i(TAG, "cathon onResume " + text);
			super.onResume();
		}
		
		
		public void setupView(){
//			text_phone_ok=(Button) findViewById(R.id.text_phone_ok);
//			text_input_phone=(EditText) findViewById(R.id.text_input_phone);
//			text_phone=(TextView) findViewById(R.id.text_phone);

			//text_phone_ok.setOnClickListener(new OnClickListener() {

//				@Override
//				public void onClick(View view) {
//					text_phone.setText(text_input_phone.getText().toString());
//					Log.i(TAG, "cathon getphone numb oncreate " + text_input_phone.getText().toString());
//					SharedPreferences prs= PreferenceManager.getDefaultSharedPreferences(AizhuhealthmmsActivity.this);
//					Editor pr=prs.edit();
//					pr.putString("text_input_phone_pr", text_input_phone.getText().toString());
//					pr.commit();
//				}
			//});

			SharedPreferences prs= PreferenceManager.getDefaultSharedPreferences(AizhuhealthmmsActivity.this);
			Editor pr=prs.edit();
			
			//cathon change the phb num filter now is 15899774980  10086
			pr.putString("text_input_phone_pr", "15899774980");
			pr.commit();
			
			entry=new ListViewEntry(this);
			
			smslist=entry.getSms();
			listview =(ListView) findViewById(R.id.listview_contact_sms);

			adapter= new ContcatAdapter(this, smslist);
			listview.setAdapter(adapter);
			
	    	listview.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				
				@Override
				public void onCreateContextMenu(ContextMenu contextmenu, View view,
						ContextMenuInfo contextmenuinfo) {
					contextmenu.setHeaderTitle("������ѯ");
					contextmenu.add(1,1,1,"ת��");
					contextmenu.add(1,2,2,"ɾ��");
					contextmenu.add(1,3,3,"ɾ��ȫ��");
				}
			});

		}

	    @Override
	    public boolean onContextItemSelected(MenuItem item) {
	    	switch(item.getItemId()){
	    	case 1:
				//ɾ������
				Log.i(TAG, "cathon contextoptionmenu case 1 ");
				
	    		break;

	    	case 2:

	    		info=(AdapterContextMenuInfo) item.getMenuInfo();
				int smssingledeleteindex=info.position;
				smssingledeleteindex=smssingledeleteindex+1;
				
	    		Log.i(TAG, "cathon contextoptionmenu case 2 " + smssingledeleteindex);

				entry=new ListViewEntry(this);
				smslist=entry.deltesms(smssingledeleteindex);
				listview =(ListView) findViewById(R.id.listview_contact_sms);
				adapter= new ContcatAdapter(this, smslist);
				listview.setAdapter(adapter);
	
				Toast.makeText(this, this.getString(R.string.smsdelete), Toast.LENGTH_LONG).show();
	        
	    		break;

	    	case 3:
	    		Log.i(TAG, "cathon contextoptionmenu case 3 ");
	    		
	        	entry=new ListViewEntry(this);	
	        	smslist=entry.delteallsms();
				listview =(ListView) findViewById(R.id.listview_contact_sms);
				adapter= new ContcatAdapter(this, smslist);
				listview.setAdapter(adapter);
		        	
				Toast.makeText(this, AizhuhealthmmsActivity.this.getString(R.string.smsdeleteall), Toast.LENGTH_LONG).show();
	    		break;
	    		
	    	}
	    	return super.onContextItemSelected(item);
	    }
	    
//	    @Override
//	    public boolean onKeyDown(int keyCode, KeyEvent event) {
//	    	
//	    if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
//	    	Log.i(TAG, "cathon onKeyDown down ");
//	    	textviewsizedefault--;
//	    	smscontentboday.setTextSize(textviewsizedefault);
//	    	return true;
//	    }
//	    else if(keyCode==KeyEvent.KEYCODE_VOLUME_UP)
//	    {
//	    	Log.i(TAG, "cathon onKeyDown up ");
//	    	textviewsizedefault++;
//	    	smscontentboday.setTextSize(textviewsizedefault);
//	    	return true;
//	    }
//	    else
//	    {
//	    	return super.onKeyDown(keyCode, event);
//	    }
//
//	    }

		@Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        //����һ��add�������
	        // �˵�Ⱥ��Ⱥ��Item�ģɣģ��������ţ�������ʾ��������������ͼ�꣬�������һ��Ⱥ�������Լ���Ӱ�
	        //����һ�ֲ˵����ڣأͣ��ж��壬��ҿ����Լ��鿴�ĵ�
	        menu.add(Menu.NONE, 1, 1, AizhuhealthmmsActivity.this.getString(R.string.smssendto)).setIcon(android.R.drawable.ic_menu_send);
	        menu.add(Menu.NONE, 2, 2, AizhuhealthmmsActivity.this.getString(R.string.smsdelete)).setIcon(android.R.drawable.ic_menu_delete);
	        menu.add(Menu.NONE, 3, 3, AizhuhealthmmsActivity.this.getString(R.string.smsdeleteall)).setIcon(android.R.drawable.ic_menu_delete);
	        
	        // return true�Ż�������
	        return true;
	        }
	    
		@Override	
	    public boolean onOptionsItemSelected(MenuItem item){
	        switch (item.getItemId()) {
	        case 1:
	        Toast.makeText(this, AizhuhealthmmsActivity.this.getString(R.string.smssendto), Toast.LENGTH_LONG).show();
	        break;
	        
	        case 2:
//				entry=new ListViewEntry(this);
//				smslist=entry.deltesms();
//				listview =(ListView) findViewById(R.id.listview_contact_sms);
//				adapter= new ContcatAdapter(this, smslist);
//				listview.setAdapter(adapter);
//	
	        Toast.makeText(this, AizhuhealthmmsActivity.this.getString(R.string.smsdelete), Toast.LENGTH_LONG).show();
	        break;
	        
	        case 3:
	        	entry=new ListViewEntry(this);
	        	smslist=entry.delteallsms();
				listview =(ListView) findViewById(R.id.listview_contact_sms);
				adapter= new ContcatAdapter(this, smslist);
				listview.setAdapter(adapter);
		        	
	        Toast.makeText(this, AizhuhealthmmsActivity.this.getString(R.string.smsdeleteall), Toast.LENGTH_LONG).show();
	        break;

	        default:
	        break;
	        }
	        return false;
	        }

        private class DataReceiver extends BroadcastReceiver{//�̳���BroadcastReceiver������
            @Override
            public void onReceive(Context context, Intent intent) {//��дonReceive����
                //double data = intent.getDoubleExtra("data", 0);
                //tv.setText("Service������Ϊ:"+data);     

//                Intent myIntent = new Intent();//����Intent����
//                myIntent.setAction("com.android.aizhuhealthmms");
//                myIntent.putExtra("cmd", CMD_STOP_SERVICE);
//                sendBroadcast(myIntent);//���͹㲥

            	Log.i(TAG, "cathon onReceive get broadcast ");
            }
        }
        
        @Override
        protected void onStart() {//��дonStart����
        	Log.i(TAG, "cathon onStart ");
                dataReceiver = new DataReceiver();
                IntentFilter filter = new IntentFilter();//����IntentFilter����
                filter.addAction("com.android.aizhuhealthmms");
                registerReceiver(dataReceiver, filter);//ע��Broadcast Receiver
                super.onStart();
        }
        
        @Override
        protected void onStop() {//��дonStop����
        	Log.i(TAG, "cathon onStop ");
                unregisterReceiver(dataReceiver);//ȡ��ע��Broadcast Receiver
                super.onStop();
        }



}
