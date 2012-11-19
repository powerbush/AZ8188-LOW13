package com.android.contacts.ui.widget;


import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.util.Constants.SimInfo;
import com.android.internal.telephony.Phone;
import com.mediatek.telephony.TelephonyManagerEx;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class SimIconView extends FrameLayout {

	private static final int DISPLAY_NONE = 0;
	private static final int DISPLAY_FIRST_FOUR = 1;
	private static final int DISPLAY_LAST_FOUR = 2;

	private static final boolean mSupport3G = true;
	private static final boolean mNeedStatus = true;

	public SimIconView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.sim_icon_view, this, true);

	}

	public void updateSimIcon(SimInfo info) {
		// Set the first 4 digits as the sim icon name
		TextView textNum = (TextView) findViewById(R.id.simNum);
		String mSimNum = info.number;
		if (mSimNum == null)
			mSimNum = "";
		int mNumDisplayFormat = info.displayNumberFormat;
		if ((textNum != null) && (mSimNum != null)) {

			switch (mNumDisplayFormat) {
			case DISPLAY_NONE: {
				textNum.setVisibility(View.GONE);
				break;
			}
			case DISPLAY_FIRST_FOUR: {

				if (mSimNum.length() >= 4) {
					textNum.setText(mSimNum.substring(0, 4));
				} else {
					textNum.setText(mSimNum);
				}
				break;
			}
			case DISPLAY_LAST_FOUR: {

				if (mSimNum.length() >= 4) {
					textNum.setText(mSimNum.substring(mSimNum.length() - 4));
				} else {
					textNum.setText(mSimNum);
				}
				break;
			}
			}
		}

		int mSlotIndex = info.slot;
		TextView text3G = (TextView) findViewById(R.id.sim3g);
		if(ContactsUtils.getOptrProperties().equals("OP02")){
			if (text3G != null) {
				if ((mSupport3G == false) || (mSlotIndex != Phone.GEMINI_SIM_1)) {
					text3G.setVisibility(View.GONE);
				}else{
					text3G.setVisibility(View.VISIBLE);
				}
			}
		}else{
			text3G.setVisibility(View.GONE);
		}
		

		TelephonyManagerEx mTelephonyManager = TelephonyManagerEx.getDefault();
		int mStatus = mTelephonyManager
				.getSimIndicatorStateGemini(info.slot);
		ImageView imageStatus = (ImageView) findViewById(R.id.simStatus);
		imageStatus.setVisibility(View.GONE);
//		if (imageStatus != null) {
//			if (mNeedStatus == true) {
//				int res = getStatusResource(mStatus);
//
//				if (res == -1) {
//					imageStatus.setVisibility(View.GONE);
//				} else {
//					imageStatus.setImageResource(res);
//				}
//			} else {
//				imageStatus.setVisibility(View.GONE);
//			}
//
//		}
		
		int resId = android.provider.Telephony.SIMBackgroundRes[info.color];
		this.setBackgroundResource(resId);

	}

	private int getStatusResource(int state) {

		Log.i("Utils gemini", "!!!!!!!!!!!!!state is " + state);
		switch (state) {
		case Phone.SIM_INDICATOR_RADIOOFF:
			return com.mediatek.internal.R.drawable.sim_radio_off;
		case Phone.SIM_INDICATOR_LOCKED:
			return com.mediatek.internal.R.drawable.sim_locked;
		case Phone.SIM_INDICATOR_INVALID:
			return com.mediatek.internal.R.drawable.sim_invalid;
		case Phone.SIM_INDICATOR_SEARCHING:
			return com.mediatek.internal.R.drawable.sim_searching;
		case Phone.SIM_INDICATOR_ROAMING:
			return com.mediatek.internal.R.drawable.sim_roaming;
		case Phone.SIM_INDICATOR_CONNECTED:
			return com.mediatek.internal.R.drawable.sim_connected;
		case Phone.SIM_INDICATOR_ROAMINGCONNECTED:
			return com.mediatek.internal.R.drawable.sim_roaming_connected;
		default:
			return -1;
		}
	}

}
