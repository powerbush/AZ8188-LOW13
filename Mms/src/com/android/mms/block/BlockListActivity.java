package com.android.mms.block;

import java.util.ArrayList;

import com.android.mms.block.BlockListData;
import com.android.mms.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.content.pm.ActivityInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Gravity; 
import android.view.Window;
import android.content.DialogInterface;

public class BlockListActivity extends Activity{
		
	private EditText contact_phone;
	private SharedPreferences prs;
	
	private Button bloclBtnSetting;
	@Override
		protected void onCreate(Bundle savedInstanceState) {
			requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 注意顺序    
			super.onCreate(savedInstanceState);
			setContentView(R.layout.block_list_view);
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.block_title);  // 注意顺序  
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			setupView();
		}
		@Override
		protected void onResume() {
			super.onResume();
		}
		public void setupView(){

			bloclBtnSetting=(Button) findViewById(R.id.flish_button);
			
			bloclBtnSetting.setOnClickListener(new OnClickListener() {	
				@Override
				public void onClick(View view) {

					AlertDialog.Builder dialog=new AlertDialog.Builder(BlockListActivity.this);
				        LayoutInflater factory = LayoutInflater.from(BlockListActivity.this);  
				        View v=factory.inflate(R.layout.block_dialog, null);
					contact_phone=(EditText) v.findViewById(R.id.block_contact_phone);
					prs= PreferenceManager.getDefaultSharedPreferences(BlockListActivity.this);	
					contact_phone.setText(prs.getString("text_input_phone_pr", ""));
					
					dialog.setView(v)
					.setPositiveButton(getString(R.string.blockconfirm),new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialoginterface, int i) {//将数据保存到数据库里
							Editor pr=prs.edit();
							pr.putString("text_input_phone_pr", contact_phone.getText().toString());
							pr.commit();
						}
					})
					.setNegativeButton(getString(R.string.blockcancel), new DialogInterface.OnClickListener() {				
						@Override
						public void onClick(DialogInterface dialoginterface, int i) {
							dialoginterface.dismiss();
					}});
					/*将对话框显示在屏的上部*/
					AlertDialog Dialog=dialog.create();
					Window window = Dialog.getWindow();     
					window.setGravity(Gravity.TOP);   
					Dialog.show();    
				}
			});
			BlockListData entry=new BlockListData(this);
			
		        ArrayList<BlockListData.sms>	smslist=entry.getSms();
			ListView listview =(ListView) findViewById(R.id.listview_contact_sms);
		
		
			BlockListAdapter adapter= new BlockListAdapter(this, smslist);
			listview.setAdapter(adapter);
		}
}
