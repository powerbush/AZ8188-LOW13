package com.az.PersonInfo;
import java.util.Date;

import com.az.Main.R;
import com.az.Main.R.id;
import com.az.Main.R.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;



public class DatePreference extends DialogPreference {
	
	private DatePicker mPicker;
	private String mValue;
	private int YearValue=2012;
	private int MonthValue=6;
	private int DayOfMonthvalue=20;
	private String DateValue;

	public DatePreference(Context context, AttributeSet attrs) {
		super(context, attrs);		
		setDialogLayoutResource(R.layout.date_preference);
	}
	
	public void setValue(String value) {
		final boolean wasBlocking = shouldDisableDependents();
		
		mValue = value;
		persistString(value);			
		
		final boolean isBlocking = shouldDisableDependents(); 
	    if (isBlocking != wasBlocking) {
	        notifyDependencyChange(isBlocking);
	    }
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);  
		mPicker = (DatePicker)view.findViewById(R.id.datePicker_preference);
		if(mPicker != null) {	
			mPicker.init(YearValue, MonthValue-1, DayOfMonthvalue, null);
		}
		
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if(positiveResult) {				
			YearValue = mPicker.getYear();
			MonthValue = mPicker.getMonth()+1;
			DayOfMonthvalue = mPicker.getDayOfMonth();
			DateValue =((new Integer(YearValue).toString()) + "-" + 
					   (new Integer(MonthValue).toString()) + "-" + 
					   (new Integer(DayOfMonthvalue).toString()));	
			if(callChangeListener(DateValue)) {
				setValue(DateValue);
			}
		}
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,	Object defaultValue) {
		String value;
		if(restorePersistedValue) value = getPersistedString("");
		else {
			value = defaultValue.toString();
		}
        setValue(value);
	}
}
