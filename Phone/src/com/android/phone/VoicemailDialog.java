package com.android.phone;

import android.app.Activity;
import android.view.Window;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;
import com.android.internal.telephony.Phone;
import android.provider.Telephony.SIMInfo;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.Spannable;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundImageSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;
import android.text.TextUtils;

public class VoicemailDialog extends Activity implements View.OnClickListener {

    private static final String TAG = "VoicemailDialog";
    private Intent mIntent = null;
    
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        //setTitle(R.string.notification_voicemail);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.voicemail_dialog);

        ImageView imageIcon= (ImageView) findViewById(R.id.icon);
        imageIcon.setImageResource(R.drawable.voicemail_dialog_title);
        TextView title = (TextView) findViewById(R.id.message);
        title.setText(R.string.notification_voicemail);
        Window window = getWindow();
        TextView mMessageView = (TextView) window.findViewById(R.id.dialog_message);
        Button okButton = (Button) findViewById(R.id.button_ok);
        Button cancelButton = (Button) findViewById(R.id.button_cancel);

        mIntent = getIntent();
        int slotId = mIntent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1);
        Log.d(TAG, "==============================================get slotId = " + slotId);
        //String simDisplayName = getSimDisplayName(slotId);

//		        		SpannableStringBuilder style = new SpannableStringBuilder(callerName);
//						int mHighlitePosNum = mNameMatchedOffsets.length();
//						log("[bindView] count: " + mHighlitePosNum);						
//						for(int i=0; i < mHighlitePosNum; i+=DS_MATCHED_DATA_DIVIDER){
//							log("[bindView] count: " + (i+1) + " || " + (int)mNameMatchedOffsets.charAt(i)
//							      + " - " + (int)mNameMatchedOffsets.charAt(i+1) + " - " + (int)mNameMatchedOffsets.charAt(i+2));
//							style.setSpan(new BackgroundColorSpan(bgSpanColor), 
//									(int)mNameMatchedOffsets.charAt(i), (int)mNameMatchedOffsets.charAt(i+1)+1, //Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//							style.setSpan(new ForegroundColorSpan(fgSpanColor), 
//									(int)mNameMatchedOffsets.charAt(i), (int)mNameMatchedOffsets.charAt(i+1)+1, Spannable.SPAN_MARK_MARK);
       

	//simDisplayName = "<font color= ff0000>" + simDisplayName + "</font>";
        /*String dialogText = String.format(getBaseContext().getString(R.string.voice_mail_dialog_text), simDisplayName);
	SpannableStringBuilder style = new SpannableStringBuilder(dialogText);
	int bgBeginPos = dialogText.indexOf(simDisplayName);
	int bgEndPos = bgBeginPos + simDisplayName.length();
//Color.parseColor("#0xFFFF00")
	style.setSpan(new BackgroundColorSpan(Color.YELLOW), bgBeginPos, bgEndPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);*/
	//String dialogText = String.format(getBaseContext().getString(R.string.voice_mail_dialog_text), simDisplayName);
        long simId = -1;
        CharSequence simName = null;
        if (slotId > -1) {
            simId = getSimId(slotId);
            Log.d(TAG, "==============================================get simId = " + simId);
            simName = getSimInfo(PhoneApp.getInstance().getApplicationContext(), simId);
            Log.d(TAG, "==============================================get simName = " + simName);
        }

        mMessageView.setText(simName);


        okButton.setOnClickListener(this);
        //okButton.setHighFocusPriority(true);
        cancelButton.setOnClickListener(this);
    }

/*    private String getSimDisplayName(int slot) {
        SIMInfo info = SIMInfo.getSIMInfoBySlot(PhoneApp.getInstance().getApplicationContext(), slot);
        if (info != null) {
            return info.mDisplayName;
        }
        return null;
    } */

    private long getSimId(int slot) {
        SIMInfo info = SIMInfo.getSIMInfoBySlot(PhoneApp.getInstance().getApplicationContext(), slot);
        if (info != null) {
            return info.mSimId;
        }
        return -1;
    }

    public CharSequence getSimInfo(Context context, long simId){

        Log.d(TAG, "getSimInfo simId = " + simId);
	//get sim info
        SIMInfo simInfo = SIMInfo.getSIMInfoById(context, simId);
	if(null != simInfo){
            String displayName = simInfo.mDisplayName;

	    Log.d(TAG, "=================================== SIMInfo simId=" + simInfo.mSimId + " mDisplayName=" + displayName);
            String dialogText = String.format(getBaseContext().getString(R.string.voice_mail_dialog_text), displayName);
	    if(null == displayName) {
	        return dialogText;
	    }

	    SpannableStringBuilder dialogTextBuf = new SpannableStringBuilder(dialogText);
//	    buf.append(" ");
//	    if(displayName.length() <= 6){
//		buf.append(displayName);
//	    }else{
//	        buf.append(displayName.substring(0,3) + ".." + displayName.charAt(displayName.length() - 1));
//	    }
//          buf.append(displayName);
//	    buf.append(" ");

	    int bgBeginPos = dialogText.indexOf(displayName);
	    int bgEndPos = bgBeginPos + displayName.length();
	    //set background image
	    int colorRes = simInfo.mSimBackgroundRes;
	    Drawable drawable = context.getResources().getDrawable(simInfo.mSimBackgroundRes);
    	    dialogTextBuf.setSpan(new BackgroundImageSpan(simInfo.mSimBackgroundRes,drawable), bgBeginPos, bgEndPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //set simInfo color
            //int color = context.getResources().getColor(R.color.siminfo_color);//#ffffffff
            dialogTextBuf.setSpan(new ForegroundColorSpan(Color.parseColor("#ffffffff")), bgBeginPos, bgEndPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            dialogTextBuf.setSpan(new StyleSpan(Typeface.BOLD),bgBeginPos,bgEndPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    return dialogTextBuf;
	}
        String dialogTextNoSimInfo = String.format(getBaseContext().getString(R.string.voice_mail_dialog_text), "");
        return dialogTextNoSimInfo;
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button_ok:
            Log.d(TAG, "onClick() intent" + mIntent);
            String number = mIntent.getStringExtra("voicemail_number");
            Log.d(TAG, "onClick() number" + number);
            //Uri numberUri = Uri.parse("voicemail:x");
            Uri numberUri = Uri.fromParts("voicemail", number, null);
            Intent IntentToDialer = new Intent(Intent.ACTION_DIAL, numberUri);
            startActivity(IntentToDialer);
    		try {
    			Thread.sleep(1500);
    		} catch (Exception e) {
    			//
    		}
            if(TextUtils.isEmpty(number)) {
                String unkonwnVoicemail = getResources().getString(R.string.notification_voicemail_no_vm_number);
                Toast.makeText(PhoneApp.getInstance(), unkonwnVoicemail, Toast.LENGTH_LONG).show();
            }
            finish();
            break;
        case R.id.button_cancel:
            finish();
            break;
        }
    }



}
