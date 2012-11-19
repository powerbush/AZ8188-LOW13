package com.android.mms.ui;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.ListActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.content.pm.ActivityInfo;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import com.android.mms.R;
//import com.android.mms.block.BlockListActivity;
import com.android.mms.block.BlockSettingActivity;
import com.android.mms.data.Conversation;
import android.net.Uri;
import android.database.Cursor;
import android.graphics.Color; 


public class ConversationMainList extends ListActivity {
   
	private long threadId=0;
    private String phoneNum;
    
	//ListActivityһ�����б�ķ�ʽ��ʾ����Դ�������Activity
	//ListActivity Class Overview(������ժ�Թٷ��ĵ�˵�ķǳ������)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_main_list1);
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>();
        HashMap<String, Object> map1 = new HashMap<String, Object>();
        HashMap<String, Object> map2 = new HashMap<String, Object>();
        //HashMap<String, Object> map3 = new HashMap<String, Object>();
        
        //һ��map�����Ӧһ������
        map1.put("user_name", getString(R.string.user_newmms));
        map1.put("user_icon", R.drawable.newmmms);
        
        map2.put("user_name", getString(R.string.user_mms1));
        map2.put("user_icon", R.drawable.mms1);
        
        //map3.put("user_name", getString(R.string.user_mms2));
        //map3.put("user_icon", R.drawable.mms2);
        
        list.add(map1);
        list.add(map2);
        //list.add(map3);
        
        /*
         * ����һ Context
         * ������ �����ϱ��������Ǹ�ArrayList����
         * ������ �����������ָ�� ����һ������ ��key Ҳ����һ��map�����key ���½�Ͽ�һ�� ��Ϊ����һ������Ҳ����һ��
         * ��Ӧһ��map���� һ��map�������2������ �� user_name �� user_icon ���������������ָ����2��key ������ͨ��String����ķ�ʽ
         * ������  ���һ����֪���� ��˼�� user_name ���������� R.id.user_name ���TextView��ʾ  user_icon ���������� 
         * R.id.user_icon ��ʾ
         */
        
        SimpleAdapter listAdapter = new SimpleAdapter(this,list,
        		R.layout.conversation_main_list2, new String[] {"user_name","user_icon"},
        		new int[] {R.id.user_name,R.id.user_icon});
        		
        //����Adapter setListAdapter()�˷�������ListActivity
        setListAdapter(listAdapter);

	ListView lv = getListView();
	lv.setCacheColorHint(0);  //�����϶��б��ʱ���ֹ���ֺ�ɫ����  
       
    }
    
    
    //�����ǵ��һ������ ����˵һ��ʱ ������Click�¼�
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	Intent intent;
    	super.onListItemClick(l, v, position, id);
    	switch(position){
    		case 0:
                //liao
                intent=ComposeMessageActivity.createIntent(this, 0);
                intent.putExtra("is_forbid_slide",true); 
    			startActivity(intent);	
    			break;
    		case 1:
    			intent = new Intent(this, ConversationList.class);
    			startActivity(intent);
    			break;
    		case 2:
			//intent = new Intent(this, BlockListActivity.class);
			getThreadId();
			intent = new Intent(this, ComposeMessageActivity.class);
           		intent.setData(Conversation.getUri(threadId));
			intent.putExtra("is_forbid_slide",true); //liao
    			startActivity(intent);
    			break;
    	}
    }
    
    private void getThreadId(){
        /*liao*/
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
        String MmsPhoneNum = preferences.getString("phone_num", "");
        //Log.i("info_liao", MmsPhoneNum + "");
        /*liao*/
		Uri uri = Uri.parse("content://sms/inbox");         
		Cursor cur = this.managedQuery(uri,new String[]{"thread_id","address"}, null, null, null);         
		if(cur != null){
			if (cur.moveToFirst()) {         
				do{     
					//for(int j = 0; j < cur.getColumnCount(); j++){    
					threadId=cur.getLong(0); 
					phoneNum=cur.getString(1); 
					if(phoneNum.equals(MmsPhoneNum) || phoneNum.equals("+86" + MmsPhoneNum)){  //liao
						break;
					}
					//} 
				}while(cur.moveToNext());   
			}
		}
		cur.close();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        menu.add(1,1,1,R.string.menu_preferences).setIcon(android.R.drawable.ic_menu_preferences);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        Intent intent  = new Intent();
        switch (item.getItemId()) {
        case 1:
            intent.setClass(this, BlockSettingActivity.class);
            break;
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

}


