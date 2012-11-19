package com.android.emergencycall;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class EmergencycallActivity extends Activity {
	private String mobile=null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        mobile="10086";
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+ mobile));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
      	startActivity(intent);
    }
    
   @Override
   protected void onPause() {
	// TODO Auto-generated method stub
	super.onPause();
	finish();
	
   }
}