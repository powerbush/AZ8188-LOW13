package com.android.mms.block;

import java.util.ArrayList;
import java.util.zip.Inflater;

import com.android.mms.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BlockListAdapter  extends BaseAdapter{
private Context context;
private ArrayList<BlockListData.sms> smslist;
private LayoutInflater inflater;

public BlockListAdapter(Context context,ArrayList<BlockListData.sms> smslist){
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
		view=inflater.inflate(R.layout.block_list_view_item, null);
		//TextView phonetx=(TextView) view.findViewById(R.id.phome_contact_sms);
		TextView bodytx=(TextView) view.findViewById(R.id.body_content_sms);
		BlockListData.sms sml=smslist.get(i);
		//phonetx.setText(sml.getPhone());
		bodytx.setText(sml.getBody());
		return view;
	}

}
