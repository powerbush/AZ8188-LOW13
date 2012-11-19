package com.android.aizhuhealthmms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlertDialogReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		intent.setClass(context,AlertDialogActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

}
