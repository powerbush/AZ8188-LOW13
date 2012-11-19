package com.android.aizhuhealthmms;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AlertDialogActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alert_dialog);
	}

	public void openActivity(View view) {
		Intent intent = new Intent(this, AizhuhealthmmsActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		startActivity(intent);
		finish();
	}

	public void dismissDialog(View view) {
		finish();
	}

}
