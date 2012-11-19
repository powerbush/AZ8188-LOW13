package com.android.contacts.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class TextView extends android.widget.TextView{

	public TextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TextView(Context context) {
		super(context);
	}
	
    @Override
    protected void onDraw(Canvas canvas) {
    	bringTextIntoView();
    	super.onDraw(canvas);
    }
}

