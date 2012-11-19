package com.android.contacts;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

// import android.provider.Telephony.SIMInfo;

public class ContactsGroupsActivity extends ListActivity {
    private static final String TAG = "ContactsGroupsActivity";
    private static final boolean DEBUG = true;

    public static final String GROUP_TITLE_KEY = "title";
    public static final String GROUP_ID_KEY = "groups_id";
    
    private static final int MENU_ITEM_ADD = 1;
    private static final int MENU_ITEM_DEL = 2;
    
    private static final int MENU_EDIT_GROUP = 1;
    private static final int MENU_DEL_GROUP = 2;
    
    public static final int DEFAULT_GROUPS_COUNT = 5;
    private GroupsAdapter mAdapter;
    private QueryHandler mQueryHandler;
    
    //refresh
    private boolean mForeground = true;
    private boolean mForceQuery = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.contacts_groups);
        Cursor cursor = this.getContentResolver().query(Groups.CONTENT_SUMMARY_URI, null, 
                Groups.DELETED + "=0 AND " + Groups.ACCOUNT_TYPE + "='DeviceOnly'", null, Groups.SYSTEM_ID + " DESC, " + Groups.TITLE);
        mAdapter = new GroupsAdapter(this, R.layout.groups_list_item, cursor);
        ListView list = getListView();
        list.setAdapter(mAdapter);
        list.setOnCreateContextMenuListener(this);
        Button btn = (Button)findViewById(R.id.add_group);
        btn.setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                Intent intent = new Intent(ContactsGroupsActivity.this, EditGroupsActivity.class);
                intent.putExtra("mode", EditGroupsActivity.MODE_NEW_GROUPS);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        mForeground = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
        	Cursor cursor = mAdapter.getCursor();
        	if (cursor != null)
        		cursor.close();
        }
    }

    @Override
    protected void onResume() {
        if (!mForeground && mForceQuery) {
            startQuery();
        }
        mForeground = true;
        mForceQuery = false;
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menu = item.getItemId();
        Intent intent = null;
        switch (menu) {
            case MENU_ITEM_ADD:
                intent = new Intent(this, EditGroupsActivity.class);
                intent.putExtra(EditGroupsActivity.Groups_MODE_KEY, EditGroupsActivity.MODE_NEW_GROUPS);
                break;
            case MENU_ITEM_DEL:
                intent = new Intent(this, ContactsGroupsDelActivity.class);
                break;
        }
        if(intent != null)startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, MENU_ITEM_ADD, 0, R.string.add_group).setIcon(android.R.drawable.ic_menu_add);
        Cursor cursor = mAdapter.getCursor();
        if (cursor != null && cursor.getCount() > DEFAULT_GROUPS_COUNT) {
            menu.add(0, MENU_ITEM_DEL, 0, R.string.del_group).setIcon(android.R.drawable.ic_menu_delete);
        }
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);
        long groupId = cursor.getLong(cursor.getColumnIndexOrThrow(Groups._ID));
        switch (id) {
            case MENU_EDIT_GROUP: {
                Intent intent = new Intent(this, EditGroupsActivity.class);
                intent.putExtra(EditGroupsActivity.Groups_MODE_KEY, EditGroupsActivity.MODE_EDIT_GROUPS);
                String title = cursor.getString(cursor.getColumnIndexOrThrow(Groups.TITLE));
                Log.i(TAG, "onListItemClick position " + position + ",cursor " + cursor.getPosition() + ", title " + title);
                intent.putExtra(GROUP_TITLE_KEY, title);
                intent.putExtra(GROUP_ID_KEY, groupId);
                startActivity(intent);
                break;
            }
            case MENU_DEL_GROUP: {
                confirmDelGroup(groupId);
                break;
            }
        }
        
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }
        
        int position = info.position;
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position );
        String title = cursor.getString(cursor.getColumnIndexOrThrow(Groups.TITLE));
        menu.setHeaderTitle(ContactsUtils.getGroupsName(this, title));
        if (position >= DEFAULT_GROUPS_COUNT) {
            menu.add(0, MENU_EDIT_GROUP, 0, R.string.edit_group);
            menu.add(0, MENU_DEL_GROUP, 0, R.string.del_group);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, EditGroupsActivity.class);
        intent.putExtra(EditGroupsActivity.Groups_MODE_KEY, EditGroupsActivity.MODE_VIEW_GROUPS);
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position );
        String title = cursor.getString(cursor.getColumnIndexOrThrow(Groups.TITLE));
        Log.i(TAG, "onListItemClick position " + position + ",cursor " + cursor.getPosition() + ", title " + title);
        intent.putExtra(GROUP_TITLE_KEY, title);
        intent.putExtra(GROUP_ID_KEY, cursor.getLong(cursor.getColumnIndexOrThrow(Groups._ID)));
        startActivity(intent);
    }

    private void confirmDelGroup(final long group_id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.del_group);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setMessage(R.string.del_group_alert);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                int count = getContentResolver().delete(ContentUris.withAppendedId(Groups.CONTENT_URI, group_id), null, null);
                Log.i(TAG, "delete group " + group_id + ", count : " + count);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }
    
    public void startQuery() {
        Log.i(TAG, "startQuery");
        if (mQueryHandler == null) {
            mQueryHandler = new QueryHandler(this);
        }
        mQueryHandler.startQuery(0, null, Groups.CONTENT_SUMMARY_URI, 
                null, Groups.DELETED + "=0 AND " + Groups.ACCOUNT_TYPE + "='DeviceOnly'", null, Groups.SYSTEM_ID + " DESC, " + Groups.TITLE);
    }

    private class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(Context context) {
            super(context.getContentResolver());
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            mAdapter.changeCursor(cursor);
        }
        
    }
    public class GroupsAdapter extends ResourceCursorAdapter {
    	private boolean showLabel = false;
    	private int lableTextId = 0;
        public GroupsAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c);
        }
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
        	TextView labelView = (TextView)view.findViewById(R.id.label);
        	if(showLabel){
        		labelView.setVisibility(View.VISIBLE);
        		if(lableTextId != 0)labelView.setText(lableTextId);
        	}
        	else labelView.setVisibility(View.GONE);
        	
            TextView countView = (TextView)view.findViewById(R.id.count);
            TextView titleView = (TextView)view.findViewById(R.id.title);
            ImageView iconView = (ImageView)view.findViewById(R.id.group_icon);
            String title = cursor.getString(cursor.getColumnIndexOrThrow(Groups.TITLE));
            long count = cursor.getLong(cursor.getColumnIndexOrThrow(Groups.SUMMARY_COUNT));
            titleView.setText(ContactsUtils.getGroupsName(context, title));
            countView.setText("(" + count + ")");
            iconView.setImageResource(ContactsUtils.getGroupsIcon(context, title));
        }
        @Override
        public int getCount() {
            return mCursor.getCount();
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == 0 || position == DEFAULT_GROUPS_COUNT) {            	
            	showLabel = true;
            	if(position == 0 )lableTextId= R.string.default_groups;
            	else lableTextId= R.string.custom_groups;
            } else showLabel = false;
            return super.getView(position, convertView, parent);
        }
        
        @Override
        protected void onContentChanged() {
            if (!mForeground) {
                mForceQuery = true;
                Log.i(TAG, "content changed, will requery on resume");
                return;
            }
            // Start an async query
            startQuery();
        }
        
    }
}
