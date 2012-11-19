package com.android.contacts;

import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Groups;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class ContactsGroupsDelActivity extends ListActivity 
        implements View.OnClickListener{
    private static final String TAG = "ContactsGroupsDelActivity";

    private static final int MENU_MARK_ALL = 1;
    private static final int MENU_UNMARK_ALL = 2;
    
    private GroupsAdapter mAdapter;
    private ArrayList<Long> mIds = new ArrayList<Long>();
    private Button mSaveBtn;
    private CheckedTextView mSelectAll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_groups_del);
        
        this.setTitle(R.string.del_group);
        
     
        mSelectAll = (CheckedTextView)findViewById(R.id.select_all);
		mSelectAll.setOnClickListener(this);
		
        Cursor cursor = getContentResolver().query(Groups.CONTENT_URI, null, Groups.SYSTEM_ID + "=0 AND " + Groups.ACCOUNT_TYPE + "='DeviceOnly'" + " AND " + Groups.DELETED + "=0", null, null);
        mAdapter = new GroupsAdapter(this, cursor);
//        if(mAdapter.getCount()>0){
//        	LayoutInflater mLayoutInflater = LayoutInflater.from(this);
//        	LinearLayout l = new LinearLayout(this);
//        	View a = mLayoutInflater.inflate(R.layout.contacts_groups_select_all_header, l, true);
//        	CheckedTextView mSelectAll2 = (CheckedTextView)a.findViewById(R.id.select_all2);
//        	CheckedTextView mSelectAll2 = new CheckedTextView(this);
//        	mSelectAll2.setId(123);
//        	mSelectAll2.setHeight(android.R.attr.listPreferredItemHeight);
//        	mSelectAll.setTextAppearance(this, android.R.attr.textAppearanceLarge);
//        	mSelectAll.setCheckMarkDrawable(android.R.attr.listChoiceIndicatorMultiple);
//        	mSelectAll2.setText(R.string.select_all);
//        	mSelectAll.setPadding(6, 0, 6, 0);
//        	mSelectAll.setGravity(Gravity.CENTER_VERTICAL);
//        	mSelectAll2.setOnClickListener(this);
//        	this.getListView().addHeaderView(mSelectAll2);
//        }
        setListAdapter(mAdapter);
        
        mSaveBtn = (Button)findViewById(R.id.save);
        Button cancel = (Button)findViewById(R.id.cancel);
        mSaveBtn.setOnClickListener(this);
        cancel.setOnClickListener(this);
        setBtnText();
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        switch (id) {
//            case MENU_MARK_ALL: {
//                Cursor cursor = mAdapter.getCursor();
//                if (cursor != null) {
//                    cursor.moveToPosition(-1);
//                    while (cursor.moveToNext()) {
//                        long groupId = cursor.getLong(cursor.getColumnIndexOrThrow(Groups._ID));
//                        if (!mIds.contains(groupId)) {
//                            mIds.add(groupId);
//                        }
//                    }
//                }
//                break;
//            }
//            case MENU_UNMARK_ALL: {
//                mIds.clear();
//                
//                break;
//            }
//        }
//        mAdapter.notifyDataSetChanged();
//        setBtnText();
//        return true;
//    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        menu.clear();
//        menu.add(0, MENU_MARK_ALL, 0, R.string.select_all).setIcon(R.drawable.quickcontact_disambig_checkbox_on);
//        menu.add(0, MENU_UNMARK_ALL, 0, R.string.unselect_all).setIcon(R.drawable.quickcontact_disambig_checkbox_off);
//        return true;
//    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    
    
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.save) {
            if (mIds.size() == 0) {
                Toast.makeText(this, "No Groups is selected", Toast.LENGTH_SHORT).show();
                return;
            }
            showDialog(id);
        }else if(id == R.id.select_all ){
        	boolean isChecked = !mSelectAll.isChecked();
        	mSelectAll.setChecked(isChecked);
        	if(isChecked){
        		Cursor cursor = mAdapter.getCursor();
                if (cursor != null) {
                    cursor.moveToPosition(-1);
                    while (cursor.moveToNext()) {
                        long groupId = cursor.getLong(cursor.getColumnIndexOrThrow(Groups._ID));
                        if (!mIds.contains(groupId)) {
                            mIds.add(groupId);
                        }
                    }
                }
        	}
        	else {
        		mIds.clear();
        	}
        	mAdapter.notifyDataSetChanged();
            setBtnText();
        }else if (id == R.id.cancel){
    		mIds.clear();
        	finish();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (id == R.id.save) {
            builder.setMessage(R.string.del_groups_alert);
            builder.setTitle(R.string.del_group);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    int count = 0;
                    for (long ids : mIds) {
                        count += getContentResolver().delete(ContentUris.withAppendedId(Groups.CONTENT_URI, ids), null, null);
                    }
                    Log.i(TAG, "Delete group item " + count); 
                    finish();
                }
            });
            builder.setNegativeButton(android.R.string.no, null);
        }
        return builder.create();
    }

    private void setBtnText() {
        int count = mIds.size();
//        int id = R.string.menu_done;
        int id = R.string.deleteConfirmation_title;
        mSaveBtn.setText(getString(id) + " (" + count +")");
        mSaveBtn.setEnabled(count > 0);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        CheckedTextView text = (CheckedTextView)v.findViewById(R.id.text);
        text.toggle();
        boolean isChecked = text.isChecked();
        if (isChecked) {
            mIds.add(id);
        } else {
            mIds.remove(id);
        }
        if(mIds.size() == this.mAdapter.getCount()){
        	mSelectAll.setChecked(true);
        }else{
        	mSelectAll.setChecked(false);
        }
        setBtnText();
    }
    
    private class GroupsAdapter extends CursorAdapter {
        private LayoutInflater mLayoutInflater;
        public GroupsAdapter (Context context, Cursor cursor) {
            super(context, cursor);
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(Groups.TITLE));
            CheckedTextView text = (CheckedTextView)view.findViewById(R.id.text);
            text.setText(name);
            long groupId = cursor.getLong(cursor.getColumnIndexOrThrow(Groups._ID));
            text.setChecked(mIds.contains(groupId));
            ImageView icon = (ImageView)view.findViewById(R.id.icon);
            icon.setImageResource(R.drawable.contact_group_custom);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mLayoutInflater.inflate(R.layout.groups_del_list_item, parent, false);
        }
        
    }
}
