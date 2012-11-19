package com.android.aizhuhealthmms;

import java.util.ArrayList;
import java.util.zip.Inflater;

import com.android.aizhuhealthmms.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContcatAdapter  extends BaseAdapter{
private Context context;
private ArrayList<ListViewEntry.sms> smslist;
private LayoutInflater inflater;

public ContcatAdapter(Context context,ArrayList<ListViewEntry.sms> smslist){
	this.context=context;
	this.smslist=smslist;
	inflater=LayoutInflater.from(context);
}
	
	
	@Override
	public int getCount() {
		return smslist.size();
	}

	@Override
	public Object getItem(int i) {
		// TODO Auto-generated method stub
		return smslist.get(i);
	}

	@Override
	public long getItemId(int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewgroup) {
		// TODO Auto-generated method stub
		view=inflater.inflate(R.layout.listviewitem, null);
		TextView phonetx=(TextView) view.findViewById(R.id.phome_contact_sms);
		TextView bodytx=(TextView) view.findViewById(R.id.body_content_sms);
		ListViewEntry.sms sml=smslist.get(i);
		//cathon xiong set smscontent size
		phonetx.setText(sml.getPhone());
		phonetx.setTextSize(28);
		bodytx.setText(sml.getBody());
		bodytx.setTextSize(54);
		return view;
	}

}
