package com.android.contacts;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.android.contacts.util.WeakAsyncTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.ContactCounts;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.Contacts.Data;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.ScrollView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

public class ContactsGroupsMultiOpt extends Activity implements TextWatcher{
    private static final String TAG="ContactsGroupsMultiOpt";
    
    private static final int MENU_MARK_ALL = 1;
    private static final int MENU_UNMARK_ALL = 2;
    
    public static final int MODE_PICK_CONTACT = 0;
    public static final int MODE_ADD_CONTACT = 1;
    public static final int MODE_DEL_CONTACT = 2;
    public static final int MODE_MOV_CONTACT = 3;
    
    public static final int BATCH_DELETE_COUNT = 100;
    
    private SearchEditText mSearchEditText;
    private int mMode;
    private long mGroupId;
    private String mGroupTitle;
    private ListView mList;
    private ContactsAdapter mAdapter;
    private String mSelection = null;
    
    private long mMoveToGroupId = -1;
    private ContactPhotoLoader mPhotoLoader;
    private Button mDoneBtn;
    private CheckedTextView mSelectAll;
    
    private List<Long> filterList = new ArrayList<Long>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.contacts_groups_opt);
        
        mSearchEditText = (SearchEditText)findViewById(R.id.search_src_text);
        mSearchEditText.addTextChangedListener(this);
        mSearchEditText.setLoading(false);
        mList = (ListView)findViewById(android.R.id.list);
        mList.setOnItemClickListener(mOnItemClickListener);
        ScrollView empty = (ScrollView)findViewById(android.R.id.empty);
        TextView emptyView = (TextView)findViewById(R.id.emptyText);
        emptyView.setText(R.string.no_match_contact);
        mList.setEmptyView(empty);
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture);
        mDoneBtn = (Button)findViewById(R.id.save);
        mDoneBtn.setOnClickListener(mViewClickListener);
        
        mSelectAll = (CheckedTextView)findViewById(R.id.select_all);
        mSelectAll.setOnClickListener(mViewClickListener);
        
        Button cancel = (Button)findViewById(R.id.cancel);
        cancel.setOnClickListener(mViewClickListener);
        Intent intent = getIntent();
        mMode = intent.getIntExtra("mode", MODE_PICK_CONTACT);
        mGroupId = intent.getLongExtra("groups_id", -1);
        mGroupTitle = intent.getStringExtra(ContactsGroupsActivity.GROUP_TITLE_KEY);
        Log.i(TAG,"mGroupTitle is " + mGroupTitle);
        if (mGroupTitle != null) mGroupTitle = mGroupTitle.replaceAll("\\'", "\\''");//escape character in java is "\", but in sql is "'"
        Log.i(TAG,"after replace mGroupTitle is " + mGroupTitle);
        Log.i(TAG, "mMode : mGroupId = " + mMode + " : " + mGroupId);
        Uri uri = Contacts.CONTENT_URI;
        uri = uri.buildUpon()
        .appendQueryParameter(ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, "true").build();
        switch (mMode) {
            case MODE_ADD_CONTACT:
                if (mGroupTitle != null) {
                    mSelection = Contacts._ID + " NOT " + ContactsListActivity.CONTACTS_IN_GROUP_SELECT.replace("?", "'" + mGroupTitle + "'")
                    	+ " AND "+ RawContacts.INDICATE_PHONE_SIM + "=" + RawContacts.INDICATE_PHONE;
                }else{
                    mSelection = Contacts._ID + " NOT " + ContactsListActivity.CONTACTS_IN_GROUP_SELECT.replace("?", "''")
                      	+ " AND "+ RawContacts.INDICATE_PHONE_SIM + "=" + RawContacts.INDICATE_PHONE;                	
                }
                break;
            case MODE_DEL_CONTACT: {
                if (mGroupTitle != null) {
                    mSelection = Contacts._ID + ContactsListActivity.CONTACTS_IN_GROUP_SELECT.replace("?", "'" + mGroupTitle + "'");
                }
            }
                
            case MODE_MOV_CONTACT: {
                if (mGroupTitle != null) {
                    mSelection = Contacts._ID + ContactsListActivity.CONTACTS_IN_GROUP_SELECT.replace("?", "'" + mGroupTitle + "'");
                }
                break;
            }
            case MODE_PICK_CONTACT: {
                //TODO  URI
                break;
            }
        }
        
        Cursor cursor = getContentResolver().query(uri, null, mSelection, null, Contacts.SORT_KEY_PRIMARY);
        mAdapter = new ContactsAdapter(this, cursor);
        mList.setAdapter(mAdapter);
        setBtnText();
        if(mAdapter.getCount()>0)mSelectAll.setVisibility(View.VISIBLE);
        else mSelectAll.setVisibility(View.GONE);
        // FIXME : do we should clear the map here?
        ContactsUtils.mContactMap.clear();
    }

    @Override
    protected void onResume() {
        mPhotoLoader.clear();
        mPhotoLoader.resume();
        super.onResume();
    }
    
    
    @Override
    protected void onDestroy() {
        mPhotoLoader.stop();
        Cursor cursor = mAdapter.getCursor();
        if (cursor != null) {
            cursor.close();
        }
        mAdapter.changeCursor(null);
        Log.e(TAG, "onDestroy()" );
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
    	Log.e(TAG, "------------------------------------begin onBackPressed()" );
        ContactsUtils.mContactMap.clear();
        super.onBackPressed();
        Log.e(TAG, "------------------------------------end onBackPressed()"); 
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (id) {
            case MODE_DEL_CONTACT: {
                builder.setMessage(R.string.confirm_remove_members);
                builder.setTitle(R.string.remove_group_members);
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    
                    public void onClick(DialogInterface dialog, int which) {
                        int count = 0;
                        Log.i(TAG, "Delete group item " + count); 
                        new DelGroupTask(ContactsGroupsMultiOpt.this).execute();
                    }
                });
                break;
            }
                
            case MODE_MOV_CONTACT: {
                builder.setTitle(R.string.move_contacts_to);
                builder.setIcon(android.R.drawable.ic_dialog_info);
                final Cursor cursor = getContentResolver().query(Groups.CONTENT_URI, 
                        new String[]{Groups._ID, Groups.TITLE},  Groups.DELETED + "=0 AND " + Groups.ACCOUNT_TYPE + "='DeviceOnly'" + " AND " + Groups._ID + " !=" + mGroupId , null, null);
                final List<Long> idList = new ArrayList<Long>();
                final List<String> titleList = new ArrayList<String>();
                if(cursor!=null){
                	while(cursor.moveToNext()){
                		idList.add(cursor.getLong(0));
                        String title = cursor.getString(1);
                        titleList.add(ContactsUtils.getGroupsName(this, title));
                	}
                }
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    
                    public void onClick(DialogInterface dialog, int which) {
                    	new MoveGroupTask(ContactsGroupsMultiOpt.this).execute(mMoveToGroupId);
                    }
                });
                
                CharSequence[] list = new CharSequence[titleList.size()];
                for(int i = 0; i < titleList.size(); i ++){
                	list[i] = titleList.get(i);
                }
                builder.setSingleChoiceItems(list, -1, new DialogInterface.OnClickListener() {
                    
                    public void onClick(DialogInterface dialog, int which) {
                    	
                        long id = idList.get(which);
                        String title = titleList.get(which);
                        Log.i(TAG, "Move to " + title);
                        mMoveToGroupId = id;
                        
                    }
                });
                break;
            }
        }
        builder.setNegativeButton(android.R.string.no, null);
        return builder.create();
    }

    private void setBtnText() {
        int count = ContactsUtils.mContactMap.size();
        int id = R.string.menu_done;
        switch (mMode) {
            case MODE_DEL_CONTACT: 
                id = R.string.deleteConfirmation_title;
                break;
            case MODE_MOV_CONTACT: 
                id = R.string.move;
                break;
        }
        
        if(getTextFilter() == null || getTextFilter().length() == 0){
        	mDoneBtn.setText(getString(id) + " (" + count +")");
        	mDoneBtn.setEnabled(count > 0);
        	
            if(count == this.mList.getCount()){
            	mSelectAll.setChecked(true);
            }else{
            	mSelectAll.setChecked(false);
            }
        }else{
        	
        	int count2 = 0;
        	for(Long l:filterList){
        		if(ContactsUtils.mContactMap.containsKey(l))count2 ++;
        	}
        	mDoneBtn.setText(getString(id) + " (" + count2 +")");
            mDoneBtn.setEnabled(count2 > 0);
            
            if(count2 == filterList.size() && count2 != 0){
            	mSelectAll.setChecked(true);
            }else{
            	mSelectAll.setChecked(false);
            }
        }
    }
    
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.i(TAG, "id = " + id);
        Log.i(TAG, "position " + position);
        CheckedTextView text = (CheckedTextView)v.findViewById(R.id.text);
        text.toggle();
        boolean isChecked = text.isChecked();
        Cursor cursor = mAdapter.getCursor();
        if (!ContactsUtils.mContactMap.containsKey(id)) {
            if (isChecked) {
                cursor.moveToPosition(position);
                ContactsUtils.ArrayData data = new ContactsUtils.ArrayData().fromCursor(cursor);
                ContactsUtils.mContactMap.put(id, data);
            }
        } else {
            if (!isChecked) {
                ContactsUtils.mContactMap.remove(id);
            }
        }
        
        setBtnText();
        Iterator<Entry<Long, ContactsUtils.ArrayData>> iter = ContactsUtils.mContactMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Long, ContactsUtils.ArrayData> entry = iter.next();
            Log.i(TAG, "id: " + entry.getKey() + ", value: " + entry.getValue().toString());
        }
    }
    
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
        {
            onListItemClick((ListView)parent, v, position, id);
        }
    };
    
    private View.OnClickListener mViewClickListener = new View.OnClickListener() {
        
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.save:
                    int count = ContactsUtils.mContactMap.size();
                    if (count == 0) {
                        Toast.makeText(ContactsGroupsMultiOpt.this, R.string.no_select_alert, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(getTextFilter() != null && getTextFilter().length() > 0 && filterList != null){
                		Long[] keySets = ContactsUtils.mContactMap.keySet().toArray(new Long[0]);
                        Log.i(TAG, "keySets: " + keySets.toString());
                        Log.i(TAG, "filterList: " + filterList.toString());
                		for(Long key:keySets){
                			if(!filterList.contains(key))ContactsUtils.mContactMap.remove(key);
                		}
                	}
                    switch (mMode) {
                        case MODE_ADD_CONTACT: {
                            setResult(Activity.RESULT_OK);
                            finish();
                            break;
                        }
                        case MODE_DEL_CONTACT: {
                            showDialog(MODE_DEL_CONTACT);
                            break;
                        }
                        case MODE_MOV_CONTACT: {
                            showDialog(MODE_MOV_CONTACT);
                            break;
                        }
                        case MODE_PICK_CONTACT: {
                            Intent intent = new Intent();
                            Long[] id_array = ContactsUtils.mContactMap.keySet().toArray(new Long[0]);
                            intent.putExtra("id_array", id_array);
                            setResult(Activity.RESULT_OK, intent);
                            ContactsUtils.mContactMap.clear();
                            finish();
                            break;
                        }
                    }
                    break;
                case R.id.cancel:
                    setResult(Activity.RESULT_CANCELED);
                    ContactsUtils.mContactMap.clear();
                    
                    Log.i(TAG, "id.cancel ,before finish");
                    finish();
                    break;
                    
                case R.id.select_all:                	
                	boolean isCheck = !mSelectAll.isChecked();
                	mSelectAll.setChecked(isCheck);
                	
                	if(isCheck){
                		Cursor cursor = mAdapter.getCursor();
                        if (cursor != null) {
                            cursor.moveToPosition(-1);
                            
                            while (cursor.moveToNext()) {
                                long contactId = cursor.getLong(cursor.getColumnIndexOrThrow(Contacts._ID));
                                if (!ContactsUtils.mContactMap.containsKey(contactId)) {
                                    ContactsUtils.mContactMap.put(contactId, new ContactsUtils.ArrayData().fromCursor(cursor));
                                }
                            }
                        }
                	}else{
                		if(getTextFilter() == null || getTextFilter().length() == 0){
                            ContactsUtils.mContactMap.clear();
                		}else{
                			if(filterList != null){
                    		Long[] keySets = ContactsUtils.mContactMap.keySet().toArray(new Long[0]);
                            Log.i(TAG, "keySets: " + keySets.toString());
                            Log.i(TAG, "filterList: " + filterList.toString());
                    		for(Long key:keySets){
                    			if(filterList.contains(key))ContactsUtils.mContactMap.remove(key);
                    		}
                    	}
                			
                		}
                	}
                    mAdapter.notifyDataSetChanged();
                    setBtnText();
                    break;
                
            }
            
        }
    };
    
    private String getTextFilter() {
        if (mSearchEditText != null) {
            return mSearchEditText.getText().toString();
        }
        return null;
    }
    
    public void afterTextChanged(Editable s) {
        Filter filter = mAdapter.getFilter();
        filter.filter(getTextFilter());
        filterList = new ArrayList<Long>();
        
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // TODO Auto-generated method stub
        
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub
        
    }
    
    private class MoveGroupTask extends
    WeakAsyncTask<Long, Void, Boolean, ContactsGroupsMultiOpt> {

    	private WeakReference<ProgressDialog> mProgress;
    	public MoveGroupTask(ContactsGroupsMultiOpt target) {
    		super(target);
    	}

    	@Override
    	protected void onPreExecute(ContactsGroupsMultiOpt target) {
    		mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(target, null,
            target.getText(R.string.saving_group),false,true));
    	}

    	@Override
    	protected Boolean doInBackground(final ContactsGroupsMultiOpt target,
    			Long... params) {
    		Long groupId = params[0];
    		 doMove(groupId);
    		 return false;
    	}

        @Override
        protected void onPostExecute(final ContactsGroupsMultiOpt target, Boolean error) {
            target.setResult(Activity.RESULT_OK);
            
            int toast;
            final ProgressDialog progress = mProgress.get();
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }
            if (error) {
                toast = R.string.save_group_fail;
            } else {
                toast = R.string.save_group_success;
            }
            Toast.makeText(ContactsGroupsMultiOpt.this, toast, Toast.LENGTH_LONG).show();
            ContactsUtils.mContactMap.clear();
            Log.i(TAG, "MoveGroupTask, onPostExecute(),before finish");
            
            target.finish();
        }
    }
    private void  doMove(long groupId) {
        ContentResolver resolver = getContentResolver();
        
        StringBuilder selection = new StringBuilder();
        selection.append(ContactsContract.Data.MIMETYPE + "='" + CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'");
        selection.append(" AND ");
        selection.append(CommonDataKinds.GroupMembership.GROUP_ROW_ID + "='" + groupId + "'");
        
        Cursor c = resolver.query(ContactsContract.Data.CONTENT_URI, 
        		new String[]{RawContacts.CONTACT_ID,ContactsContract.Data._ID}, 
        		selection.toString(), 
        		null, 
        		null);
        Map map = new HashMap<Long, Long>();
        while(c!=null && c.moveToNext()){
        	long contactId = c.getLong(0);
        	long dataId = c.getLong(1);
        	map.put(contactId, dataId);
        }
        if(c!= null)c.close();
        StringBuilder whereDel = new StringBuilder();
        StringBuilder idBuilderDel = new StringBuilder();
        
        ContentValues values = new ContentValues();
        StringBuilder where = new StringBuilder();
        StringBuilder idBuilder = new StringBuilder();
        where.append(RawContacts.CONTACT_ID + " IN(");
        Iterator<Entry<Long, ContactsUtils.ArrayData>> iter = ContactsUtils.mContactMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Long, ContactsUtils.ArrayData> entry = iter.next();
            long id = entry.getKey();
            if(map.containsKey(id)){
            	if (idBuilderDel.length() > 0) {
            		idBuilderDel.append(",");
            	}
            	idBuilderDel.append(map.get(id));            	
            }
            
            if (idBuilder.length() > 0) {
               	idBuilder.append(",");
            }
            idBuilder.append(id);            
        }
        where.append(idBuilder);
        where.append(")");
        where.append(" AND ");
        where.append(ContactsContract.Data.MIMETYPE + "='" + CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'");
        where.append(" AND ");
        where.append(CommonDataKinds.GroupMembership.GROUP_ROW_ID + "='" + this.mGroupId + "'");
        values.put(CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupId);
        int count = resolver.update(ContactsContract.Data.CONTENT_URI, values, where.toString(), null);
        Log.i(TAG, "move contact " + where.toString());
        Log.i(TAG, "move data count " + count);
        
        whereDel.append(ContactsContract.Data._ID + " IN(");
        whereDel.append(idBuilderDel);
        whereDel.append(")");
        count = resolver.delete(ContactsContract.Data.CONTENT_URI, whereDel.toString(), null);
        Log.i(TAG, "delete repeat contact " + whereDel.toString());
        Log.i(TAG, "delete repeat data count " + count);
    }

    private class DelGroupTask extends
    WeakAsyncTask<Long, Void, Boolean, ContactsGroupsMultiOpt> {

    	private WeakReference<ProgressDialog> mProgress;
    	public DelGroupTask(ContactsGroupsMultiOpt target) {
    		super(target);
    	}

    	@Override
    	protected void onPreExecute(ContactsGroupsMultiOpt target) {
    		mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(target, null,
            target.getText(R.string.removing_group_member),false,true));
    	}

    	@Override
    	protected Boolean doInBackground(final ContactsGroupsMultiOpt target,
    			Long... params) {
    		doDelete();
    		return false;
    	}

        @Override
        protected void onPostExecute(final ContactsGroupsMultiOpt target, Boolean error) {
            target.setResult(Activity.RESULT_OK);
            int toast;
            final ProgressDialog progress = mProgress.get();
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
            }
            if (error) {
                toast = R.string.removing_group_members_fail;
            } else {
                toast = R.string.removing_group_members_sucess;
            }
            Toast.makeText(ContactsGroupsMultiOpt.this, toast, Toast.LENGTH_LONG).show();
            ContactsUtils.mContactMap.clear();
            Log.i(TAG, "DelGroupTask, onPostExecute(),before finish");
            target.finish();
        }
    }
    
   /* private void doDelete() {
        Log.i(TAG, "do delete");
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        StringBuilder where = new StringBuilder();
        StringBuilder idBuilder = new StringBuilder();
        where.append(RawContacts.CONTACT_ID + " IN(");
        Iterator<Entry<Long, ContactsUtils.ArrayData>> iter = ContactsUtils.mContactMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Long, ContactsUtils.ArrayData> entry = iter.next();
            long id = entry.getKey();
            if (idBuilder.length() > 0) {
                idBuilder.append(",");
            }
            idBuilder.append(id);
        }
        where.append(idBuilder);
        where.append(")");
        where.append(" AND ");
        where.append(ContactsContract.Data.MIMETYPE + "='" + CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'");
        where.append(" AND ");
        where.append(ContactsContract.Data.DATA1 + "='" + mGroupId + "'");
        int count = resolver.delete(ContactsContract.Data.CONTENT_URI, where.toString(), null);
        Log.i(TAG, "delete contact " + where.toString());
        Log.i(TAG, "delete data count " + count);
        ContactsUtils.mContactMap.clear();
        //finish();
    }*/
    
    private void doDelete() {
        Log.i(TAG, "do delete");
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        Iterator<Entry<Long, ContactsUtils.ArrayData>> iter = ContactsUtils.mContactMap.entrySet().iterator();
        int totalDelete = ContactsUtils.mContactMap.keySet().size();
        Log.d(TAG, " do delete, totalDelete = "+ totalDelete);
        int deleteCount = totalDelete/BATCH_DELETE_COUNT;
        Log.d(TAG, " do delete, deleteCount = "+ deleteCount);
        int deleteCountMod = totalDelete%BATCH_DELETE_COUNT;
        Log.d(TAG, " do delete, deleteCountMod = "+ deleteCountMod);
        while (iter.hasNext()) {
            while (deleteCount != 0) {
                StringBuilder where = new StringBuilder();
                StringBuilder idBuilder = new StringBuilder();
                where.append(RawContacts.CONTACT_ID + " IN(");
                for (int i = 0; i< BATCH_DELETE_COUNT; i++){
                   Entry<Long, ContactsUtils.ArrayData> entry = iter.next();
                   long id = entry.getKey();
                   if (idBuilder.length() > 0) {
                       idBuilder.append(",");
                   }
                   idBuilder.append(id);
                }
                where.append(idBuilder);
                where.append(")");
                where.append(" AND ");
                where.append(ContactsContract.Data.MIMETYPE + "='" + CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'");
                where.append(" AND ");
                where.append(ContactsContract.Data.DATA1 + "='" + mGroupId + "'");
                Log.d(TAG, " do delete, before delete");
                int count = resolver.delete(ContactsContract.Data.CONTENT_URI, where.toString(), null);
                Log.i(TAG, "delete contact " + where.toString());
                Log.i(TAG, "delete data count " + count);
                try {
                    Log.d(TAG,"Thread.sleep(150);");                
                    if(iter.hasNext()) {
                        Thread.sleep(150);
                    }                      
                } catch (InterruptedException e) {
                }
                deleteCount--;
            }
            if (deleteCountMod != 0 ) {
                StringBuilder where = new StringBuilder();
                StringBuilder idBuilder = new StringBuilder();
                where.append(RawContacts.CONTACT_ID + " IN(");
                for (int i = 0; i< deleteCountMod; i++){
                   Entry<Long, ContactsUtils.ArrayData> entry = iter.next();
                   long id = entry.getKey();
                   if (idBuilder.length() > 0) {
                       idBuilder.append(",");
                   }
                   idBuilder.append(id);
                }
                where.append(idBuilder);
                where.append(")");
                where.append(" AND ");
                where.append(ContactsContract.Data.MIMETYPE + "='" + CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'");
                where.append(" AND ");
                where.append(ContactsContract.Data.DATA1 + "='" + mGroupId + "'");
                Log.d(TAG, " do delete, before delete");
                int count = resolver.delete(ContactsContract.Data.CONTENT_URI, where.toString(), null);
                Log.i(TAG, "delete contact " + where.toString());
                Log.i(TAG, "delete data count " + count);
            }
        }
        ContactsUtils.mContactMap.clear();
       // finish();
    }
    
    private class ContactsAdapter extends CursorAdapter 
        implements OnScrollListener, SectionIndexer{
        LayoutInflater mLayoutInflater;
        private SectionIndexer mIndexer;
        public ContactsAdapter (Context ctx, Cursor cursor) {
            super(ctx, cursor);
            mLayoutInflater = LayoutInflater.from(ctx);
            updateIndexer(cursor);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME));
            Log.i(TAG, "display name " + name);
            CheckedTextView text = (CheckedTextView)view.findViewById(R.id.text);
            text.setText(name);
            QuickContactBadge icon = (QuickContactBadge)view.findViewById(R.id.icon);
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(Contacts._ID));
            text.setChecked(ContactsUtils.mContactMap.containsKey(id));
            long photoId = cursor.getLong(cursor.getColumnIndexOrThrow(Contacts.PHOTO_ID));
            int sim = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.INDICATE_PHONE_SIM));
            mPhotoLoader.loadPhoto(icon, photoId, sim);
            int position = cursor.getPosition();
            TextView divider = (TextView)view.findViewById(R.id.divider);
            final int section = getSectionForPosition(position);
            if (getPositionForSection(section) == position) {
                String title = (String)mIndexer.getSections()[section];
                divider.setText(title);
                divider.setVisibility(View.VISIBLE);
            } else {
                divider.setVisibility(View.GONE);
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mLayoutInflater.inflate(R.layout.groups_opt_list_item, parent, false);
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            Log.i(TAG, "runQueryOnBackgroundThread : " + constraint);
            ContactsGroupsMultiOpt.this.runOnUiThread(new Runnable() {
                public void run() {
                    if (mSearchEditText != null) {
                        mSearchEditText.setLoading(true);
                    }
                }
            });
            Uri baseUri;
            if (!TextUtils.isEmpty(constraint)) {
                baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI, Uri.encode(constraint.toString()));
            } else {
                baseUri = Contacts.CONTENT_URI;
            }
            baseUri = baseUri.buildUpon()
            .appendQueryParameter(ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, "true").build();
            Cursor cursor = getContentResolver().query(baseUri, null, mSelection, null, Contacts.SORT_KEY_PRIMARY);
            ContactsGroupsMultiOpt.this.runOnUiThread(new Runnable() {
                public void run() {
                    if (mSearchEditText != null) {
                        mSearchEditText.setLoading(false);
                    }
                }
            });
            String now = getTextFilter();
            Log.i(TAG, "runQueryOnBackgroundThread filter " + now);
            if ((now == null || now.length() == 0)
                    && (constraint == null || constraint.length() == 0)) {
                return cursor;
            }
            if ((now == null || now.length() == 0)
                    && !(constraint == null || constraint.length() == 0)) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            if (now != null && !now.equals(constraint)) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            
            filterList = new ArrayList<Long>();
            Log.i(TAG, "aaaaa ContactsUtils.mContactMap: " + ContactsUtils.mContactMap.toString());
            while(cursor != null && cursor.moveToNext()){
            	String name = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.DISPLAY_NAME));
            	long id = cursor.getLong(cursor.getColumnIndexOrThrow(Contacts._ID));
            	filterList.add(id);
            }
            cursor.moveToFirst();
            
            return cursor;
        }

        @Override
        public void changeCursor(Cursor cursor) {
            updateIndexer(cursor);
            super.changeCursor(cursor);
            setBtnText();
        }

        private void updateIndexer(Cursor cursor) {
            if (cursor == null) {
                mIndexer = null;
                return;
            }

            Bundle bundle = cursor.getExtras();
            if (bundle.containsKey(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES)) {
                String sections[] =
                    bundle.getStringArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES);
                int counts[] = bundle.getIntArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS);
                mIndexer = new ContactsSectionIndexer(sections, counts);
            } else {
                mIndexer = null;
            }
        }
        
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            
        }

        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                mPhotoLoader.pause();

            } else {
                mPhotoLoader.resume();
            }
        }

        public int getPositionForSection(int section) {
            if (mIndexer == null) {
                return -1;
            }
            int retVal = mIndexer.getPositionForSection(section);   
            return retVal;
        }

        public int getSectionForPosition(int position) {
            if (mIndexer == null) {
                return -1;
            }
            int retVal = mIndexer.getSectionForPosition(position);
            return retVal;
        }

        public Object[] getSections() {
            if (mIndexer == null) {
                return new String[] { " " };
            } else {
                return mIndexer.getSections();
            }
        }
        
        
    }

}
