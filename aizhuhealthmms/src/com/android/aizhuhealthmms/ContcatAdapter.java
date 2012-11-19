package com.android.aizhuhealthmms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Inflater;

import com.android.aizhuhealthmms.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContcatAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<ListViewEntry.sms> smslist;
	private LayoutInflater inflater;

	public ContcatAdapter(Context context, ArrayList<ListViewEntry.sms> smslist) {
		this.context = context;
		this.smslist = smslist;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return smslist.size();
	}

	@Override
	public Object getItem(int i) {
		return smslist.get(i);
	}

	@Override
	public long getItemId(int i) {
		return ((ListViewEntry.sms) getItem(i)).getId();
	}

	@Override
	public View getView(int i, View view, ViewGroup viewgroup) {
		view = inflater.inflate(R.layout.listviewitem, null);
		TextView tvPhone = (TextView) view.findViewById(R.id.phome_contact_sms);
		TextView tvBody = (TextView) view.findViewById(R.id.body_content_sms);
		TextView tvRecvdate = (TextView) view.findViewById(R.id.recvdate);
		TextView tvRecvtime = (TextView) view.findViewById(R.id.recvtime);
		ListViewEntry.sms sml = smslist.get(i);
		// cathon xiong set smscontent size
		tvPhone.setText(sml.getPhone());
		tvBody.setText(sml.getBody());
		long recvdatetime = sml.getRecvtime();
		tvRecvdate.setText(new SimpleDateFormat("yyyy/MM/dd").format(new Date(
				recvdatetime)));
		tvRecvtime.setText(new SimpleDateFormat("HH:mm").format(new Date(
				recvdatetime)));

		return view;
	}

	public void updateView(ArrayList<ListViewEntry.sms> smslist) {
		this.smslist = smslist;
		this.notifyDataSetChanged();
	}

}
