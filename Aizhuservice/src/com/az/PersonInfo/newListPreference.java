package com.az.PersonInfo;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;

public class newListPreference extends ListPreference {

	private static final String LOGTAG="newListPreference";
	private CharSequence[] entries;
	private CharSequence[] entryValues;
	private int selectedId;
	private Context cxt;
	private int indexOfValue;

	public newListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		cxt=context;
	}

	//��д�������������ͬ��Summary
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		// TODO Auto-generated method stub
		super.onSetInitialValue(restoreValue, defaultValue);
		entries = getEntries();
		entryValues = getEntryValues();
		String value= getValue();//�������ɾ����ֻ������debug
		indexOfValue=this.findIndexOfValue(getSharedPreferences().getString(this.getKey(), ""));
		Log.e(LOGTAG, "index:"+indexOfValue+",value:"+value);
		if(indexOfValue>=0){
			String key=String.valueOf(entries[indexOfValue]);
			Log.e(LOGTAG, "key:"+key+",value:"+value);
			if(null!=key){
				setSummary(key);
			}
		}
	}

	//��д������������һ��OK��ť
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		//super.onPrepareDialogBuilder(builder);//���ܵ��ø��������������������б����رնԻ���
		builder.setSingleChoiceItems(entries, indexOfValue, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.v(LOGTAG, String.valueOf(which));
				selectedId=which;
			}
		});
		//builder.setPositiveButton(null, null);//ListPreferenceԴ��������������д�ģ�����������Ҫ��д
		builder.setPositiveButton(getPositiveButtonText()==null?"OK":getPositiveButtonText(), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface paramDialogInterface, int paramInt) {
				if(selectedId>=0){
					setSummary(entries[selectedId]);
					paramDialogInterface.dismiss();
					newListPreference.this.persistString(entryValues[selectedId].toString());
					newListPreference.this.callChangeListener(entryValues[selectedId]);
				}
			}
		});
	}
}


