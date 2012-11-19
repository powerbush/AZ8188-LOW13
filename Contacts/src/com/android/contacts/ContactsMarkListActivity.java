/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.android.contacts;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Pattern;

import com.android.contacts.ContactsListActivity.ContactListItemCache;
import com.android.internal.telephony.ITelephony;
import com.android.contacts.ui.EditSimContactActivity;

import android.accounts.Account;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.app.AlertDialog;
import android.pim.vcard.VCardConfig;
import android.pim.vcard.VCardEntryCommitter;
import android.pim.vcard.VCardEntryConstructor;
import android.pim.vcard.VCardEntryCounter;
import android.pim.vcard.VCardInterpreter;
import android.pim.vcard.VCardInterpreterCollection;
import android.pim.vcard.VCardParser;
import android.pim.vcard.VCardParser_V21;
import android.pim.vcard.VCardParser_V30;
import android.pim.vcard.VCardSourceDetector;
import android.pim.vcard.exception.VCardException;
import android.pim.vcard.exception.VCardNestedException;
import android.pim.vcard.exception.VCardNotSupportedException;
import android.pim.vcard.exception.VCardVersionException;
import android.provider.ContactsContract.ContactCounts;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Email;//for USIM
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.text.TextUtils;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.provider.Telephony.SIMInfo;//gemini enhancement
import com.mediatek.telephony.PhoneNumberFormatUtilEx;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import com.mediatek.CellConnService.CellConnMgr;

public class ContactsMarkListActivity extends ListActivity
	implements View.OnClickListener /* mtk80909 enhancement */{

	private static final String TAG = "ContactsMarkList";
	private Handler mCopyHandler = new CopyHandler();
	private QueryHandler mQueryHandler;
	private ContactItemListAdapter mAdapter;
	int mCopySrc;
	int mCopyDst;
	long srcSimId = -1;
	long dstSimId = -1;
	boolean isSrcSimUSIM = false;

	boolean isDstSimUSIM = false;
	private ProgressDialog mCopyProgDialog = null;
	private ProgressDialog mSelectAllDialog = null;
	private ProgressDialog mQueryDialog = null;
	private boolean mBeingCopied = false;
	private Set<Integer> mSelectedPositionsSet = new HashSet<Integer>();
	boolean[] mSelectedPositions;
	int mSelectedCount;
	private CopyThread mCopyThread;
	private AlertDialog mCompConfirmDialog;
	private TextView mEmptyText;
	private static final int QUERY_TOKEN = 1;
	private boolean mNeedQuery = false;
	//public static boolean mShouldFinish = false;

    	// mtk80909 enhancement
	private Account mAccount;
	private static final String ACCOUNT_TYPE_GOOGLE = "com.google";
	private static final String GOOGLE_MY_CONTACTS_GROUP = "System Group: My Contacts";

	private static final Uri PICK_CONTACTS_URI = buildSectionIndexerUri(Contacts.CONTENT_URI);
	static final String[] CONTACTS_PROJECTION = new String[] { Contacts._ID, // 0
			Contacts.DISPLAY_NAME_PRIMARY, // 1
			Contacts.DISPLAY_NAME_ALTERNATIVE, // 2
			Contacts.SORT_KEY_PRIMARY, // 3
			Contacts.DISPLAY_NAME, // 4
	};
	
    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
        Contacts._ID,                       // 0
        Contacts.DISPLAY_NAME_PRIMARY,      // 1
        Contacts.DISPLAY_NAME_ALTERNATIVE,  // 2
        Contacts.SORT_KEY_PRIMARY,          // 3
        Contacts.STARRED,                   // 4
        Contacts.TIMES_CONTACTED,           // 5
        Contacts.CONTACT_PRESENCE,          // 6
        Contacts.PHOTO_ID,                  // 7
        Contacts.LOOKUP_KEY,                // 8
        Contacts.PHONETIC_NAME,             // 9
        Contacts.HAS_PHONE_NUMBER,          // 10
        Contacts.INDICATE_PHONE_SIM,        // 11 
    };

	static final String[] PHONES_PROJECTION = new String[] { Phone._ID, // 0
			Phone.TYPE, // 1
			Phone.LABEL, // 2
			Phone.NUMBER, // 3
			Phone.DISPLAY_NAME, // 4
			Phone.CONTACT_ID, // 5
            Phone.DISPLAY_NAME_SOURCE, // 6
			Contacts.INDICATE_PHONE_SIM, //7
	};
	static final int PHONE_ID_COLUMN_INDEX = 0;
	static final int PHONE_TYPE_COLUMN_INDEX = 1;
	static final int PHONE_LABEL_COLUMN_INDEX = 2;
	static final int PHONE_NUMBER_COLUMN_INDEX = 3;
	static final int PHONE_DISPLAY_NAME_COLUMN_INDEX = 4;
	static final int PHONE_CONTACT_ID_COLUMN_INDEX = 5;
	static final int PHONE_DISPLAY_NAME_SOURCE_INDEX = 6;

	private static final String[] EMAIL_PROJECTION = new String[] {
			Email.DISPLAY_NAME, // 0
			Email.CONTACT_PRESENCE, // 1
			Email.CONTACT_ID, // 2
			Phone.DISPLAY_NAME, //
	};
	private static final int EMAIL_NAME_COLUMN = 0;
	private static final int EMAIL_STATUS_COLUMN = 1;
	private static final int EMAIL_ID_COLUMN = 2;
	private static final int EMAIL_CONTACT_NAME_COLUMN = 3;

	private static final String CLAUSE_ONLY_VISIBLE = Contacts.IN_VISIBLE_GROUP
			+ "=1";

	private static final int ID_MARK_ALL = 1;
	private static final int ID_UNMARK_ALL = 2;
	private static final int ID_COPY = 3;

	private static final int ID_IN_COPY = 0;
	private static final int ID_END_COPY = 1;

	private int mSuccessCount = 0;
	private int mFailCount = 0;
	
	// Valid pattern for phone numbers of SIM contacts
	private static final String SIM_NUM_PATTERN = "[+]?[[0-9][*#]]+[[0-9][*#,]]*"; 
	private static String mErrorNotification = null;

	// mtk80909 enhancement
	private LinearLayout mButtonsLayout;
	private Button mCopyButton;
	private Button mBackButton;
	private static final String LOG_TAG = TAG;
	private static final boolean DO_PERFORMANCE_PROFILE = false;
	private final static int VCARD_VERSION_V21 = 1;
    private final static int VCARD_VERSION_V30 = 2;
    private final static int VCARD_VERSION_V40 = 3;
    private Handler mHandler = new Handler();
    private ProgressDialog mProgressDialogForScanVCard;
    private List<VCardFile> mAllVCardFileList;
    private VCardScanThread mVCardScanThread;
    private VCardReadThread mVCardReadThread;
    private ImportProgressDlg  mProgressDialogForReadVCard;
    private ImportMultiFileProgressDlg mImportMultiVCardProgressDlg;
    private String mErrorMessage;
    private boolean mNeedReview = false;
    private boolean mJustCreated = false;
    private boolean mImportVCard = false;
    private boolean mExportVCard = false;
    private MatrixCursor mMatrixCursor = null;
    private final static String[] MATRIX_CURSOR_PROJECTION = {
    	"_id",	// 0
    	"file_name", // 1
    	"last_modified", // 2
    };
    private final static int MATRIX_CURSOR_ID_INDEX = 0;
    private final static int MATRIX_CURSOR_FILE_NAME_INDEX = 1;
    private final static int MATRIX_CURSOR_LAST_MODIFIED_INDEX = 2;
    
    private TextView mSrcAndDst;
    private LinearLayout mSelectAllLayout;
    private TextView mSelectAllText;
    private CheckBox mSelectAllBox;
    private View mSplitLine1;
    private View mSplitLine2;
    static final int MESSAGE_TIME_OUT = 1;
    static final int TIME_OUT = 5000;//5000 ms time out.
   	private static final int REQUEST_TYPE = 304;
    public  static final String ACTION_SHOULD_FINISHED = "com.android.contacts.ContactsMarkList.ACTION_SHOULD_FINISHED";
	private FinishReceiver  mFinishReceiver = new FinishReceiver();   
    private class ClockHandler extends Handler {
    	public void handleMessage(Message msg){
    		switch(msg.what){
    		case MESSAGE_TIME_OUT:
    			Log.d("ClockHandler","Time out");   
				if (mNeedQuery){
					mNeedQuery = false;
			            startQuery();     
                    }
    			break;
    		}
    	}
    }
    // Runs on the UI thread.
    private class DialogDisplayer implements Runnable {
        private final int mResId;
        public DialogDisplayer(int resId) {
            mResId = resId;
        }
        public DialogDisplayer(String errorMessage) {
            mResId = R.id.dialog_error_with_message;
            mErrorMessage = errorMessage;
        }
        public void run() {
            showDialog(mResId);
        }
    }

    public class ImportProgressDlg extends ProgressDialog{
        public ImportProgressDlg(Context context) {
               super( context);
           }
        public void onBackPressed(){
            Log.d(LOG_TAG,"ImportProgressDlg:onBackPressed");
            if(ContactsMarkListActivity.this.mVCardReadThread!=null){
                  Log.d(LOG_TAG,"ContactsMarkListActivity.this.mVCardReadThread!=null");
            ContactsMarkListActivity.this.mVCardReadThread.cancel();
                }
            //super.onBackPressed();
            }            
        }
    
    public class ImportMultiFileProgressDlg extends CustomProgressDialog {
        public ImportMultiFileProgressDlg(Context context) {
            super(context);
        }

        public void onBackPressed() {
            Log.d(LOG_TAG, "ImportMultiFileProgressDlg onBackPressed");
            if (ContactsMarkListActivity.this.mVCardReadThread != null) {
                Log.d(LOG_TAG, "ImportMultiFileProgressDlg cancel mVCardReadThread");
                ContactsMarkListActivity.this.mVCardReadThread.cancel();
            }
            //super.onBackPressed();
        }
    }
    
    public class FinishReceiver extends BroadcastReceiver {
		@Override
		 public  void onReceive(Context context, Intent intent)
			{
				final String action = intent.getAction();
				Log.i("ContactsMarkListActivity:FinishReceiver", "action is " + action);
                                nofifyFinished();
				ContactsMarkListActivity.this.finish();
				//mShouldFinish = false;						
            }
    }
    public class PBKLoadReceiver extends BroadcastReceiver {
		@Override
		 public  void onReceive(Context context, Intent intent)
			{
				final String action = intent.getAction();
				Log.i("PBKLoadReceiver", "action is " + action);
				if (action.equals(AbstractStartSIMService.ACTION_PHB_LOAD_FINISHED)){
					if (mNeedQuery){
                            int simId = intent.getIntExtra("simId", -1);
                          Log.i("PBKLoadReceiver", "simId is " + simId);
                            if(ContactsMarkListActivity.this.mCopySrc== simId){
								mNeedQuery = false;
					            startQuery();
                                }
						}
					}
        }
    }

    private class MatrixCursorPopulator implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mMatrixCursor != null && !mMatrixCursor.isClosed()) {
				mMatrixCursor.close();
			}
			mMatrixCursor = new MatrixCursor(MATRIX_CURSOR_PROJECTION);
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			int size = mAllVCardFileList.size();
			for (int i = 0; i < mAllVCardFileList.size(); ++i) {
				VCardFile vcardFile = mAllVCardFileList.get(i);
				String id = String.valueOf(i);
				String fileName = vcardFile.getName();
				String lastModified = "(" + dateFormat.format(new Date(vcardFile.getLastModified())) + ")";
				mMatrixCursor.addRow(new String[] {id, fileName, lastModified});
			}
			if (mMatrixCursor == null || mMatrixCursor.getCount() == 0) {
				throw new RuntimeException("mMatrixCursor shouldn't be empty or null here");
			}
			mAdapter.changeCursor(mMatrixCursor);
			mButtonsLayout.setVisibility(View.VISIBLE);
			mCopyButton.setEnabled(false);
			mSelectedCount = 0;
			mSelectedPositions = new boolean[mAdapter.getCount()];
			mButtonsLayout.setVisibility(View.VISIBLE);
			mSelectAllLayout.setVisibility(View.VISIBLE);
			mSelectAllBox.setChecked(false);
			mSelectAllText.setText(R.string.selectall);
			mSrcAndDst.setVisibility(View.VISIBLE);
			mSplitLine1.setVisibility(View.VISIBLE);
			mSplitLine2.setVisibility(View.VISIBLE);
		}
    }

    private class CancelListener
        implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
        public void onClick(DialogInterface dialog, int which) {
        	setResult(RESULT_OK);
			nofifyFinished();
            finish();
        }

        public void onCancel(DialogInterface dialog) {
        	setResult(RESULT_OK);
 			nofifyFinished();
            finish();
        }
    }

    private CancelListener mCancelListener = new CancelListener();

    private class VCardReadThread extends Thread
            implements DialogInterface.OnCancelListener {
        private ContentResolver mResolver;
        private VCardParser mVCardParser;
        private boolean mCanceled;
        private PowerManager.WakeLock mWakeLock;
        private Uri mUri;
        private File mTempFile;

        private List<VCardFile> mSelectedVCardFileList;
        private List<String> mErrorFileNameList;

        public VCardReadThread(Uri uri) {
            mUri = uri;
            init();
        }

        public VCardReadThread(final List<VCardFile> selectedVCardFileList) {
            mSelectedVCardFileList = selectedVCardFileList;
            mErrorFileNameList = new ArrayList<String>();
            init();
        }

        private void init() {
            Context context = ContactsMarkListActivity.this;
            mResolver = context.getContentResolver();
            PowerManager powerManager = (PowerManager)context.getSystemService(
                    Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_DIM_WAKE_LOCK |
                    PowerManager.ON_AFTER_RELEASE, LOG_TAG);
        }

        @Override
        public void finalize() {
            if (mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }

        @Override
        public void run() {
            boolean shouldCallFinish = true;
            mWakeLock.acquire();
            Uri createdUri = null;
            mTempFile = null;
            boolean  zeroSizeFile = false;
            // Some malicious vCard data may make this thread broken
            // (e.g. OutOfMemoryError).
            // Even in such cases, some should be done.
            
            Log.i(LOG_TAG, "begin read vcard********* ");
            try {
                if (mUri != null) {  // Read one vCard expressed by mUri
                    final Uri targetUri = mUri;
                    mProgressDialogForReadVCard.setProgressNumberFormat("");
                    mProgressDialogForReadVCard.setProgress(0);

                    // Count the number of VCard entries
                    mProgressDialogForReadVCard.setIndeterminate(true);
                    long start;
                    if (DO_PERFORMANCE_PROFILE) {
                        start = System.currentTimeMillis();
                    }
                    final VCardEntryCounter counter = new VCardEntryCounter();
                    final VCardSourceDetector detector = new VCardSourceDetector();
                    final VCardInterpreterCollection builderCollection =
                            new VCardInterpreterCollection(Arrays.asList(counter, detector));
                    boolean result;
                    try {
                        // We don't know which type should be useld to parse the Uri.
                        // It is possble to misinterpret the vCard, but we expect the parser
                        // lets VCardSourceDetector detect the type before the misinterpretation.
                        result = readOneVCardFile(targetUri, VCardConfig.VCARD_TYPE_UNKNOWN,
                                builderCollection, true, null);
                        Log.i(LOG_TAG, "readOneVCardFile *** result="+result);
                        if (0 == counter.getCount()) {
                            Log.i(LOG_TAG, "counter.getCount()=0 ");
                            zeroSizeFile = true;
                        }
                    } catch (VCardNestedException e) {
                        try {
                            final int estimatedVCardType = detector.getEstimatedType();
                            final String estimatedCharset = detector.getEstimatedCharset();
                            // Assume that VCardSourceDetector was able to detect the source.
                            // Try again with the detector.
                            result = readOneVCardFile(targetUri, estimatedVCardType,
                                    counter, false, null);

                            Log.i(LOG_TAG, "readOneVCardFile VCardNestedException, result="+result);
                            if (0 == counter.getCount()) {
                                Log.i(LOG_TAG, "count=0*********");
                                zeroSizeFile = true;
                            }
                        } catch (VCardNestedException e2) {
                            result = false;
                            Log.e(LOG_TAG, "Must not reach here. " + e2);
                        }
                    }
                    if (DO_PERFORMANCE_PROFILE) {
                        long time = System.currentTimeMillis() - start;
                        Log.d(LOG_TAG, "time for counting the number of vCard entries: " +
                                time + " ms");
                    }
                    if (!result) {
                        shouldCallFinish = false;
                        return;
                    }

                    mProgressDialogForReadVCard.setProgressNumberFormat(
                            getString(R.string.reading_vcard_contacts));
                    mProgressDialogForReadVCard.setIndeterminate(false);
                    mProgressDialogForReadVCard.setMax(counter.getCount());
                    String charset = detector.getEstimatedCharset();
                    createdUri = doActuallyReadOneVCard(targetUri, mAccount, true, detector,
                            mErrorFileNameList);
                    //wait for database delete sync. if not sleep, the UI will not friend.
                    try {
                        Log.d(TAG,"Thread.sleep(130*"+counter.getCount()+");");
                            Thread.sleep(130*counter.getCount());
                        } catch (InterruptedException e) {
                      }
                  //add by mtk80908 for ALPS129583
                    if(mProgressDialogForReadVCard.getProgress()<mProgressDialogForReadVCard.getMax())
		            try {
		                   this.sleep(1000);
		            } catch (InterruptedException e) {
		                   e.printStackTrace();
		            } 
                } else {  // Read multiple files.
                    for (VCardFile vcardFile : mSelectedVCardFileList) {
                        if (mCanceled) {
                        	Log.d(LOG_TAG, "Read multiple files canceled");
                            if (null != mProgressDialogForReadVCard && mProgressDialogForReadVCard.isShowing() && !ContactsMarkListActivity.this.isFinishing()) {
                                Log.d(LOG_TAG, "null != mProgressDialogForReadVCard");
                                mProgressDialogForReadVCard.dismiss();
                                mProgressDialogForReadVCard = null;
                            }
                            if (null != mImportMultiVCardProgressDlg && mImportMultiVCardProgressDlg.isShowing() && !ContactsMarkListActivity.this.isFinishing()) {
                                Log.d(LOG_TAG, "null != mImportMultiVCardProgressDlg");
                                mImportMultiVCardProgressDlg.dismiss();
                                mImportMultiVCardProgressDlg = null;
                            }
                            return;
                        }
                        //mImportMultiVCardProgressDlg.setProgressNumberFormat("");
                        mImportMultiVCardProgressDlg.setIndeterminate(true);

                        // TODO: detect scheme!
                        final Uri targetUri = Uri.parse("file://" + vcardFile.getCanonicalPath());
                        
                        final VCardEntryCounter counter = new VCardEntryCounter();
                        final VCardSourceDetector detector = new VCardSourceDetector();
                        final VCardInterpreterCollection builderCollection =
                            new VCardInterpreterCollection(Arrays.asList(counter, detector));
                        try {
                            if (!readOneVCardFile(targetUri, VCardConfig.VCARD_TYPE_UNKNOWN,
                                    builderCollection, true, mErrorFileNameList)) {
                                continue;
                            }
                        } catch (VCardNestedException e) {
                            // Assume that VCardSourceDetector was able to detect the source.
                        }
                        // xuxin_beg_2011_08_11
                        if (!mCanceled)
                        {
                            mImportMultiVCardProgressDlg.setIndeterminate(false);
                            mImportMultiVCardProgressDlg.setProgressNumberFormat(
                                    getString(R.string.reading_vcard_contacts));
                            mImportMultiVCardProgressDlg.setProgress(0);
                            mImportMultiVCardProgressDlg.setMax(counter.getCount());
                            String charset = detector.getEstimatedCharset();
                            doActuallyReadOneVCard(targetUri, mAccount, false, detector, mErrorFileNameList, vcardFile);
                        }
                        // xuxin_end
/*                     //wait for database delete sync. if not sleep, the UI will not friend.
                        try {
                            Log.d(TAG, "Thread.sleep(130*" + counter.getCount() + ");");
                            Thread.sleep(130 * counter.getCount());
                        } catch (InterruptedException e) {
                        }*/
                    }
                }
            } finally {
            	Log.d(LOG_TAG, "Read multiple finally");
                mWakeLock.release();
                if (null != mProgressDialogForReadVCard && mProgressDialogForReadVCard.isShowing() && !ContactsMarkListActivity.this.isFinishing()) {
                    Log.d(LOG_TAG, "null != mProgressDialogForReadVCard");
                    mProgressDialogForReadVCard.dismiss();
                    mProgressDialogForReadVCard = null;
                }
                if (null != mImportMultiVCardProgressDlg && mImportMultiVCardProgressDlg.isShowing()&& !ContactsMarkListActivity.this.isFinishing()) {
                    Log.d(LOG_TAG, "null != mImportMultiVCardProgressDlg");
                    mImportMultiVCardProgressDlg.dismiss();
                    mImportMultiVCardProgressDlg = null;
                }
                if (mTempFile != null) {
                    if (!mTempFile.delete()) {
                        Log.w(LOG_TAG, "Failed to delete a cache file.");
                    }
                    mTempFile = null;
                }
                if (zeroSizeFile) {
                    Log.i(LOG_TAG, "finally, counter.getCount()=0 ");
                    runOnUIThread(new DialogDisplayer(
                    getString(R.string.fail_reason_invalid_vcard_file_error)));
                    return;

                }             
                // finish() is called via mCancelListener, which is used in DialogDisplayer.
                if (shouldCallFinish && !isFinishing()) {
                    if (mErrorFileNameList == null || mErrorFileNameList.isEmpty()) {
                        Log.v(LOG_TAG, "mErrorFileNameList == null, finish()");
                        setResult(RESULT_OK);
						nofifyFinished();
                        finish();
                        if (mNeedReview) {
                            mNeedReview = false;
                            Log.v(LOG_TAG, "Prepare to review the imported contact");

                            if (createdUri != null) {
                                // get contact_id of this raw_contact
                                final long rawContactId = ContentUris.parseId(createdUri);
                                Uri contactUri = RawContacts.getContactLookupUri(
                                        getContentResolver(), ContentUris.withAppendedId(
                                                RawContacts.CONTENT_URI, rawContactId));

                                Intent viewIntent = new Intent(Intent.ACTION_VIEW, contactUri);
                                startActivity(viewIntent);
                            }
                        }
                    } else {
                        StringBuilder builder = new StringBuilder();
                        boolean first = true;
                        for (String fileName : mErrorFileNameList) {
                            if (first) {
                                first = false;
                            } else {
                                builder.append(", ");
                            }
                            builder.append(fileName);
                        }

                        runOnUIThread(new DialogDisplayer(
                                getString(R.string.fail_reason_failed_to_read_files,
                                        builder.toString())));
                    }
                }
            }
        }

        private Uri doActuallyReadOneVCard(Uri uri, Account account,
                boolean showEntryParseProgress,
                VCardSourceDetector detector, List<String> errorFileNameList) {
            final Context context = ContactsMarkListActivity.this;
            int vcardType = detector.getEstimatedType();
            if (vcardType == VCardConfig.VCARD_TYPE_UNKNOWN) {
                vcardType = VCardConfig.getVCardTypeFromString(
                        context.getString(R.string.config_import_vcard_type));
            }
            final String estimatedCharset = detector.getEstimatedCharset();
            final String currentLanguage = Locale.getDefault().getLanguage();
            VCardEntryConstructor builder;
            builder = new VCardEntryConstructor(vcardType, mAccount, estimatedCharset);
            final VCardEntryCommitter committer = new VCardEntryCommitter(mResolver);
            builder.addEntryHandler(committer);
            if (showEntryParseProgress) {
                builder.addEntryHandler(new ProgressShower(mProgressDialogForReadVCard,
                        context.getString(R.string.reading_vcard_message),
                        ContactsMarkListActivity.this,
                        mHandler));
            }

            try {
                if (!readOneVCardFile(uri, vcardType, builder, false, null)) {
                    return null;
                }
            } catch (VCardNestedException e) {
                Log.e(LOG_TAG, "Never reach here.");
            }
            final ArrayList<Uri> createdUris = committer.getCreatedUris();
            return (createdUris == null || createdUris.size() != 1) ? null : createdUris.get(0);
        }

        private Uri doActuallyReadOneVCard(Uri uri, Account account,
                boolean showEntryParseProgress,
                VCardSourceDetector detector, List<String> errorFileNameList, VCardFile vcardFile) {
        	Log.d(LOG_TAG, "doActuallyReadOneVCard ++");
            final Context context = ContactsMarkListActivity.this;
            int vcardType = detector.getEstimatedType();
            if (vcardType == VCardConfig.VCARD_TYPE_UNKNOWN) {
                vcardType = VCardConfig.getVCardTypeFromString(
                        context.getString(R.string.config_import_vcard_type));
            }
            final String estimatedCharset = detector.getEstimatedCharset();
            final String currentLanguage = Locale.getDefault().getLanguage();
            VCardEntryConstructor builder;
            builder = new VCardEntryConstructor(vcardType, mAccount, estimatedCharset);
            final VCardEntryCommitter committer = new VCardEntryCommitter(mResolver);
            builder.addEntryHandler(committer);
            builder.addEntryHandler(new CustomProgressShower(mImportMultiVCardProgressDlg,
                    ContactsMarkListActivity.this,
                    mHandler, vcardFile.getName()));

            Log.d(LOG_TAG, "doActuallyReadOneVCard start readOneVCardFile");
            try {
                if (!readOneVCardFile(uri, vcardType, builder, false, null)) {
                    return null;
                }
            } catch (VCardNestedException e) {
                Log.e(LOG_TAG, "Never reach here.");
            }
            Log.d(LOG_TAG, "doActuallyReadOneVCard end readOneVCardFile");
            final ArrayList<Uri> createdUris = committer.getCreatedUris();
            return (createdUris == null || createdUris.size() != 1) ? null : createdUris.get(0);
        }
        
        /**
         * Charset should be handled by {@link VCardEntryConstructor}. 
         */
        private boolean readOneVCardFile(Uri uri, int vcardType,
                VCardInterpreter interpreter,
                boolean throwNestedException, List<String> errorFileNameList)
                throws VCardNestedException {
        	Log.d(LOG_TAG, "readOneVCardFile ++");
            InputStream is;
            try {
            	Log.d(LOG_TAG, "readOneVCardFile openInputStream");
                is = mResolver.openInputStream(uri);
                mVCardParser = new VCardParser_V21(vcardType);

                try {
                	Log.d(LOG_TAG, "readOneVCardFile start parse");
                    mVCardParser.parse(is, interpreter);
                    Log.d(LOG_TAG, "readOneVCardFile end parse");
                } catch (VCardVersionException e1) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                    if (interpreter instanceof VCardEntryConstructor) {
                        // Let the object clean up internal temporal objects,
                        ((VCardEntryConstructor)interpreter).clear();
                    } else if (interpreter instanceof VCardInterpreterCollection) {
                        for (VCardInterpreter elem :
                            ((VCardInterpreterCollection) interpreter).getCollection()) {
                            if (elem instanceof VCardEntryConstructor) {
                                ((VCardEntryConstructor)elem).clear();
                            }
                        }
                    }

                    is = mResolver.openInputStream(uri);

                    try {
                        mVCardParser = new VCardParser_V30(vcardType);
                        mVCardParser.parse(is, interpreter);
                    } catch (VCardVersionException e2) {
                        throw new VCardException("vCard with unspported version.");
                    }
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException was emitted: " + e.getMessage());

                if (null != mProgressDialogForReadVCard && mProgressDialogForReadVCard.isShowing()&& !ContactsMarkListActivity.this.isFinishing()) {
                    mProgressDialogForReadVCard.dismiss();
                    //mProgressDialogForReadVCard = null;
                }
                if (null != mImportMultiVCardProgressDlg && mImportMultiVCardProgressDlg.isShowing()&& !ContactsMarkListActivity.this.isFinishing()) {
                	mImportMultiVCardProgressDlg.dismiss();
                    //mImportMultiVCardProgressDlg = null;
                }

                if (errorFileNameList != null) {
                    errorFileNameList.add(uri.toString());
                } else {
                    runOnUIThread(new DialogDisplayer(
                            getString(R.string.fail_reason_io_error) +
                                    ": " + e.getLocalizedMessage()));
                }
                return false;
            } catch (VCardNotSupportedException e) {
                if ((e instanceof VCardNestedException) && throwNestedException) {
                    throw (VCardNestedException)e;
                }
                if (errorFileNameList != null) {
                    errorFileNameList.add(uri.toString());
                } else {
                    runOnUIThread(new DialogDisplayer(
                            getString(R.string.fail_reason_vcard_not_supported_error) +
                            " (" + e.getMessage() + ")"));
                }
                return false;
            } catch (VCardException e) {
                if (errorFileNameList != null) {
                    errorFileNameList.add(uri.toString());
                } else {
                    runOnUIThread(new DialogDisplayer(
                            getString(R.string.fail_reason_vcard_parse_error) +
                            " (" + e.getMessage() + ")"));
                }
                return false;
            }
            return true;
        }

        public void cancel() {
            mCanceled = true;
            if (mVCardParser != null) {
            	Log.d(LOG_TAG, "cancel VCard parse");
                mVCardParser.cancel();
            }
        }

        public void onCancel(DialogInterface dialog) {
            cancel();
        }
    }
    
    private void runOnUIThread(Runnable runnable) {
        if (mHandler == null) {
            Log.w(LOG_TAG, "Handler object is null. No dialog is shown.");
        } else {
            mHandler.post(runnable);
        }
    }
    
    private class VCardScanThread extends Thread implements OnCancelListener, OnClickListener {
        private boolean mCanceled;
        private boolean mGotIOException;
        private File mRootDirectory;

        // To avoid recursive link.
        private Set<String> mCheckedPaths;
        private PowerManager.WakeLock mWakeLock;

        private class CanceledException extends Exception {
        }

        public VCardScanThread(File sdcardDirectory) {
            mCanceled = false;
            mGotIOException = false;
            mRootDirectory = sdcardDirectory;
            mCheckedPaths = new HashSet<String>();
            PowerManager powerManager = (PowerManager)ContactsMarkListActivity.this.getSystemService(
                    Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_DIM_WAKE_LOCK |
                    PowerManager.ON_AFTER_RELEASE, LOG_TAG);
        }

        @Override
        public void run() {
            mAllVCardFileList = new Vector<VCardFile>();
            try {
                mWakeLock.acquire();
                getVCardFileRecursively(mRootDirectory);
            } catch (CanceledException e) {
                mCanceled = true;
            } catch (IOException e) {
                mGotIOException = true;
            } finally {
                mWakeLock.release();
            }

            if (mCanceled) {
                mAllVCardFileList = null;
            }

            if (mProgressDialogForScanVCard != null && mProgressDialogForScanVCard.isShowing()&& !ContactsMarkListActivity.this.isFinishing()) {
            mProgressDialogForScanVCard.dismiss();
            mProgressDialogForScanVCard = null;
            }

            if (mGotIOException) {
                runOnUIThread(new DialogDisplayer(R.id.dialog_io_exception));
            } else if (mCanceled) {
            	setResult(RESULT_OK);
				nofifyFinished();
                finish();
            } else {
                int size = mAllVCardFileList.size();
                final Context context = ContactsMarkListActivity.this;
                if (size == 0) {
                    runOnUIThread(new DialogDisplayer(R.id.dialog_vcard_not_found));
                } else {
                    // TODO: start selection and import.
                	
                	runOnUIThread(new MatrixCursorPopulator());
                }
            }
        }

        private void getVCardFileRecursively(File directory)
                throws CanceledException, IOException {
            if (mCanceled) {
                throw new CanceledException();
            }

            FileFilter ff = new FileFilter() {
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    } else if (f.getName().toLowerCase().endsWith(".vcf") && f.canRead()){
                        return true;
                    }
                    return false;
                }
            };

            final File[] files = directory.listFiles(ff);
            
            // e.g. secured directory may return null toward listFiles().
            if (files == null) {
                Log.w(LOG_TAG, "listFiles() returned null (directory: " + directory + ")");
                return;
            }
            for (File file : files) {
                if (mCanceled) {
                    throw new CanceledException();
                }
                String canonicalPath = file.getCanonicalPath();
                if (mCheckedPaths.contains(canonicalPath)) {
                    continue;
                }

                mCheckedPaths.add(canonicalPath);

                if (file.isDirectory()) {
                    getVCardFileRecursively(file);
                } else {
                    String fileName = file.getName();
                    VCardFile vcardFile = new VCardFile(
                            fileName, canonicalPath, file.lastModified());
                    mAllVCardFileList.add(vcardFile);
                }
            }
        }

        public void onCancel(DialogInterface dialog) {
            mCanceled = true;
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                mCanceled = true;
            }
        }
    }

	public static final Uri PICK_PHONE_EMAIL_URI = Uri.parse("content://com.android.contacts/data/phone_email");
	final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
    String mName = null;
    String mEmail = null;
    String mNumber = null;
    private boolean COPY_FROM_PHONE_USIM_TO_USIM = false;

	int tempCopySrc = 0;
	int tempCopyDst = 0;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
        mCellMgr.register(this);

		final Intent intent = getIntent();
		if (intent == null) {
			Log.i(TAG,"********** intent is null");
			return;
		}

		// layout
		// mtk80909 enhancement
		setContentView(R.layout.contacts_copy_layout);
		mButtonsLayout = (LinearLayout)findViewById(R.id.buttons_layout);
		mCopyButton = (Button)findViewById(R.id.copy_button);
		mBackButton = (Button)findViewById(R.id.back_button);
		if(mCopyButton != null) mCopyButton.setOnClickListener(this);
		if(mBackButton != null) mBackButton.setOnClickListener(this);



		mSelectAllLayout = (LinearLayout)findViewById(R.id.selectAll_layout);
		mSelectAllText = (TextView)findViewById(R.id.selectAll_text);
		mSelectAllBox = (CheckBox)findViewById(R.id.selectAll_box);
		mSplitLine1 = findViewById(R.id.split_line1);
		if (mSplitLine1 != null) mSplitLine1.setBackgroundResource(com.android.internal.R.drawable.divider_horizontal_dark_opaque);
		mSplitLine2 = findViewById(R.id.split_line2);
		if (mSplitLine2 != null) mSplitLine2.setBackgroundResource(com.android.internal.R.drawable.divider_horizontal_dark_opaque);
		if (mSelectAllLayout != null) mSelectAllLayout.setOnClickListener(this);
		mSrcAndDst = (TextView)findViewById(R.id.src_and_dst);

		setupListView();
		onNewIntent(intent);
		setEmptyText();
		
		mSelectedPositionsSet = new HashSet<Integer>();
		mQueryHandler = new QueryHandler(this);

		// mtk80909 enhancement
		mJustCreated = true;
        this.registerReceiver(mPBKBroadCastReceiver,new IntentFilter(AbstractStartSIMService.ACTION_PHB_LOAD_FINISHED));
		this.registerReceiver(mFinishReceiver,new IntentFilter(ContactsMarkListActivity.ACTION_SHOULD_FINISHED));
        mSelectedPositions = null;

	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		String srcText = "";
		String dstText = "";
		// mtk80909 enhancement
		mCopySrc = intent.getIntExtra("src", -1000);
		mCopyDst = intent.getIntExtra("dst", -1000);
        mImportVCard = (mCopySrc == -2 && mCopyDst == RawContacts.INDICATE_PHONE); 
        mExportVCard = (mCopySrc == RawContacts.INDICATE_PHONE && mCopyDst == -2);
		Log.i(TAG, "intent getExtra mCopySrc is " + mCopySrc + " mCopyDst is " + mCopyDst);
		if (mCopySrc >= 0) {
			SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(ContactsMarkListActivity.this, mCopySrc);
			if (simInfo != null) {
				srcSimId = simInfo.mSimId;
				Log.i(TAG,"onNewIntent srcSimId is " + srcSimId);
			}
			try {
			if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {			
					if (iTel != null && iTel.getIccCardTypeGemini(mCopySrc).equals("USIM")) {
						isSrcSimUSIM = true;
					}
			} else {
				if (iTel.getIccCardType().equals("USIM")) {
					isSrcSimUSIM = true;
            	}
			}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} 
		Log.i(TAG,"isSrcSimUSIM is" + isSrcSimUSIM);
		if (mCopyDst >= 0) {
			SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(ContactsMarkListActivity.this, mCopyDst);
			if (simInfo != null) {
				dstSimId = simInfo.mSimId;
				Log.i(TAG,"onNewIntent dstSimId is " + dstSimId);
			}
			try {
			if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
					if (iTel != null && iTel.getIccCardTypeGemini(mCopyDst).equals("USIM")) {
						isDstSimUSIM = true;
					}
			} else {
				if (iTel.getIccCardType().equals("USIM")) {
					isDstSimUSIM = true;
            	}
			}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		} 
		Log.i(TAG,"isDstSimUSIM is" + isDstSimUSIM);
	

//		if (mCopyDst > 0) {
//			SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(ContactsMarkListActivity.this, mCopyDst);
//			if (simInfo != null) {
//				dstSimId = simInfo.mSimId;
//			}
//		}

	
//		mCopySrc = changeToIndicateValue(tempCopySrc);
//		mCopyDst = changeToIndicateValue(tempCopyDst);
		Log.i(TAG, "intent getExtra srcSimId is " + srcSimId + " dstSimId is " + dstSimId);
		COPY_FROM_PHONE_USIM_TO_USIM = (mCopySrc == RawContacts.INDICATE_PHONE || isSrcSimUSIM) && isDstSimUSIM;

		// mtk80909 enhancement
		final String accountName = intent.getStringExtra("account_name");
		final String accountType = intent.getStringExtra("account_type");
        if (!TextUtils.isEmpty(accountName) && !TextUtils.isEmpty(accountType)) {
            mAccount = new Account(accountName, accountType);
        }


        if (mCopySrc == -1) {
        	srcText = getString(R.string.phone);
        } else if (mCopySrc == -2) {
        	srcText = getString(R.string.sdcard);
        } else {
        SIMInfo simInfos = SIMInfo.getSIMInfoBySlot(ContactsMarkListActivity.this, mCopySrc);
        if(simInfos == null){
            Log.i(TAG, "Src simInfo is NULL");
        	srcText = "";
        } else {
            Log.i(TAG, "Src simInfo is " + simInfos.mDisplayName);
        	srcText = simInfos.mDisplayName;
        }
        }
        if (mCopyDst == -1) {
        	dstText = getString(R.string.phone);
        } else if (mCopyDst == -2) {
        	dstText = getString(R.string.sdcard);
        } else {
        SIMInfo simInfos = SIMInfo.getSIMInfoBySlot(ContactsMarkListActivity.this, mCopyDst);
        if(simInfos == null){
            Log.i(TAG, "Dst simInfo is NULL");
            dstText = "";
        } else {
        	Log.i(TAG, "Dst simInfo is " + simInfos.mDisplayName);
        	dstText = simInfos.mDisplayName;     
        }

        }
        // TODO: Take use of the framework interfaces to turn the numbers into texts. 
//        srcText = String.valueOf(copySrcSimId);
//        dstText = String.valueOf(copyDstSimId);
        
        mSrcAndDst.setText(getString(R.string.src_and_dst, srcText, dstText));
	}

//	protected int changeToIndicateValue(int CopyId) {
//		int tempId = 0;
//		if (CopyId == -2) {
//			tempId = CopyId;
//		} else if (CopyId == -1) {
//			tempId = CopyId;
//		} else if (CopyId == 0 || CopyId == 1) {
//			try {
//			if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
//				if (null != iTel && iTel.getIccCardTypeGemini(CopyId).equals("USIM")) {
//					tempId = CopyId + RawContacts.INDICATE_USIM + 1;					
//				} else {
//					tempId = CopyId + 1;
//				}
//			} else {
//				if (null != iTel && iTel.getIccCardType().equals("USIM")) {
//					tempId = CopyId + RawContacts.INDICATE_USIM;
//				} else {
//					tempId = CopyId;
//				}
//				
//			}
//            } catch (RemoteException ex) {
//			ex.printStackTrace();
//			}
//			return tempId;
//		}	
//		return tempId;
//	}
	@Override
	protected void onPause() {
		super.onPause();
		if (mBeingCopied && mCopyThread != null) {
			mCopyThread.mCanceled = true;
		}
		
		// mtk80909 enhancement
        if (mVCardReadThread != null) {
            mVCardReadThread.cancel();
            mVCardReadThread = null;
		}
		if (mSelectAllDialog != null && mSelectAllDialog.isShowing()&& !ContactsMarkListActivity.this.isFinishing()) {
			mSelectAllDialog.dismiss();
			mSelectAllDialog = null;
		}
		if (mCompConfirmDialog != null && mCompConfirmDialog.isShowing()&& !ContactsMarkListActivity.this.isFinishing()) {
			mCompConfirmDialog.dismiss();
			mCompConfirmDialog = null;
		}
	    if (mQueryDialog != null && mQueryDialog.isShowing()&& !ContactsMarkListActivity.this.isFinishing()) {
			mQueryDialog.dismiss();
			mQueryDialog = null;
		}
		if (mEmptyText != null)
			mEmptyText.setVisibility(View.GONE);
		if (!mAdapter.isUpdateRegistered()) mAdapter.registerUpdate();
	}

	@Override
	protected void onResume() {
		super.onResume();
//	 	mSelectedPositionsSet.clear();
        Log.d(TAG,"onResume");
		// mtk80909 for CR ALPS00032731
		// When onResume() is called and SIM status is not legal,
		// the activity will be finished.
		final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
		if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
		try {
			if (iTel == null
						|| (mCopySrc >= RawContacts.INDICATE_SIM && !iTel
								.isRadioOnGemini(mCopySrc))) {
					setResult(RESULT_OK);
					nofifyFinished();
				finish();
				return;
			}
		} catch (RemoteException e) {
			Log.w(TAG, "RemoteException!");
				setResult(RESULT_OK);
				nofifyFinished();
			finish();
			return;
		}
		} else {
			try {
				if (iTel == null
						|| (mCopySrc >= RawContacts.INDICATE_SIM && !iTel
								.isRadioOn())) {
					setResult(RESULT_OK);
					nofifyFinished();
					finish();
					return;
				}
			} catch (RemoteException e) {
				Log.w(TAG, "RemoteException!");
				setResult(RESULT_OK);
				nofifyFinished();
				finish();
				return;
			}
		}
		
        /*if (mShouldFinish) {
			mShouldFinish = false;
            nofifyFinished();
			finish();
            return ;
		}	*/
        Log.d(TAG, "onResume(), mJustCreated= "+ mJustCreated+ "; mBeingCopied="+mBeingCopied);
        Log.d(TAG, " onResume(), mImportVCard=" + mImportVCard);
		// mtk80909 enhancement
		if (mJustCreated) {
			mButtonsLayout.setVisibility(View.GONE);
		}
		if (!mBeingCopied && mJustCreated) {
			if (!mAdapter.isUpdateRegistered()) mAdapter.registerUpdate();
			if (mImportVCard) {
				if (mJustCreated) {
					doScanExternalStorageAndImportVCard();
				}
			} else {
			Log.d(TAG, " onResume(), ProgressDialog show*****");
			mQueryDialog = new ProgressDialog(this);
			mQueryDialog.setCancelable(false);
					mQueryDialog.setMessage(getString(
					R.string.please_wait));
			mQueryDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mQueryDialog.show();
            boolean bPBKReady = true;
            Log.d(LOG_TAG,"mCopySrc = "+mCopySrc);
            Log.d(LOG_TAG,"sim1Ready = "+ContactsUtils.getSim1Ready()+" sim2Rready= "+ContactsUtils.getSim2Ready());
            if(mCopySrc==0)	{
                if(1!=ContactsUtils.getSim1Ready()){
                        bPBKReady = false;
                    } else {
                        bPBKReady = true;
                    }
            }
            if(mCopySrc==1)	{
                if(1!=ContactsUtils.getSim2Ready()){
                        bPBKReady = false;
                    } else {
                        bPBKReady = true;
                    }
                }	
            if(bPBKReady){
                    startQuery();
                } else {//launch an wait dialog, wait for sim ready.
                     this.mClockHandler.sendMessageDelayed(this.mClockHandler.obtainMessage(MESSAGE_TIME_OUT), 
							TIME_OUT);
                     mNeedQuery = true;
                     Log.d(TAG,"wait for sim ready");
                }
			}
		}

		// mtk80909 enhancement
		mJustCreated = false;
		if (mCompConfirmDialog == null) {
			Log.i(TAG, "mCompConfirmDialog is null. ");
		} else {
			boolean isShowing = mCompConfirmDialog.isShowing();
			Log.i(TAG, "mCompConfirmDialog is showing? " + isShowing);
			if (isShowing) {
				ViewGroup decorView = (ViewGroup) mCompConfirmDialog.getWindow().getDecorView();
				Log.i(TAG, "decorView getHeight: " + decorView.getHeight());
				Log.i(TAG, "decorView getWidth: " + decorView.getWidth());
				View parentPanel = decorView.getChildAt(0);
				Log.i(TAG, "parentPanel getHeight: " + parentPanel.getHeight());
				Log.i(TAG, "parentPanel getWidth: " + parentPanel.getWidth());
				decorView.requestLayout();
				
			} 
		}
	}
	
	// mtk80909 enhancement import VCard
    private void doScanExternalStorageAndImportVCard() {
        // TODO: should use getExternalStorageState().
        final File file = Environment.getExternalStorageDirectory();
        if (!file.exists() || !file.isDirectory() || !file.canRead()) {
            showDialog(R.id.dialog_sdcard_not_found);
        } else {
            mVCardScanThread = new VCardScanThread(file);
            showDialog(R.id.dialog_searching_vcard);
		}
	}

	private void startQuery() {
		Log.i(TAG,"In startQuery isDstSimUSIM is " + isDstSimUSIM + " ----srcSimId is " + srcSimId + " --dstSimId is " + dstSimId);
        if (!isSrcSimUSIM && dstSimId == RawContacts.INDICATE_PHONE) {//copy from sim to phone
			mQueryHandler.startQuery(QUERY_TOKEN, null, Contacts.CONTENT_URI, ContactsListActivity.CONTACTS_SUMMARY_PROJECTION, 
			RawContacts.INDICATE_PHONE_SIM + " = " + srcSimId + " AND " + CLAUSE_ONLY_VISIBLE, null, Contacts.SORT_KEY_PRIMARY);
		
		} else if (COPY_FROM_PHONE_USIM_TO_USIM || (!isSrcSimUSIM && isDstSimUSIM)) {//copy from phone or usim to usim
			Log.i(TAG,"startQuery COPY_FROM_PHONE_USIM_TO_USIM or copy from sim to usim");
				mQueryHandler.startQuery(QUERY_TOKEN, null,
						Contacts.CONTENT_URI,
						CONTACTS_SUMMARY_PROJECTION, RawContacts.INDICATE_PHONE_SIM + " = " + srcSimId, null, Contacts.SORT_KEY_PRIMARY);
		} else if (isSrcSimUSIM && dstSimId == RawContacts.INDICATE_PHONE) {//copy from usim to phone
			mQueryHandler.startQuery(QUERY_TOKEN, null, Contacts.CONTENT_URI,
			CONTACTS_SUMMARY_PROJECTION, RawContacts.INDICATE_PHONE_SIM + " = " + srcSimId, null, Contacts.SORT_KEY_PRIMARY);
		} else 	if (!isDstSimUSIM) {//copy to sim card
				 mQueryHandler.startQuery(QUERY_TOKEN, null, buildSectionIndexerUri(Phone.CONTENT_URI),
					PHONES_PROJECTION, RawContacts.INDICATE_PHONE_SIM + " = "
					 + srcSimId + " AND " + CLAUSE_ONLY_VISIBLE, null, null);
		}

		if (this.mExportVCard) {
			mQueryHandler.startQuery(QUERY_TOKEN, null,
					buildSectionIndexerUri(PICK_CONTACTS_URI),
					CONTACTS_PROJECTION, CLAUSE_ONLY_VISIBLE, null,
					Contacts.SORT_KEY_PRIMARY);
		}
	}

	// mtk80909 enhancement
	@Override
	protected void onDestroy() {
		setResult(RESULT_OK);
		super.onDestroy();
		
		
		if (mCompConfirmDialog != null && mCompConfirmDialog.isShowing()) {
			mCompConfirmDialog.dismiss();
			mCompConfirmDialog = null;
		}
		
        mCellMgr.unregister();
        this.unregisterReceiver( this.mPBKBroadCastReceiver);
        this.unregisterReceiver( this.mFinishReceiver);		
        mSelectedPositions = null;
	}

	private static Uri buildSectionIndexerUri(Uri uri) {
		return uri.buildUpon().appendQueryParameter(
				ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, "true").build();
	}

	private void setupListView() {

		final ListView list = getListView();

		list.setFocusable(true);
		mAdapter = new ContactItemListAdapter(this);
		setListAdapter(mAdapter);
	}

	private void setEmptyText() {
		String text;
		// mtk80909 enhancement
		text = getString(R.string.noContacts);
		getListView().setEmptyView(findViewById(android.R.id.empty));
		mEmptyText = (TextView) findViewById(R.id.emptyText);
		mEmptyText.setText(text);
		mEmptyText.setVisibility(View.GONE);
	}

	void initCompConfirmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.copy_procedure_over);
		builder.setMessage((TextUtils.isEmpty(mErrorNotification) ? "" : mErrorNotification + "\n")
				+ getString(R.string.copied) + mSuccessCount + "\n"
				+ getString(R.string.uncopied) + mFailCount);
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						ContactsMarkListActivity.this.setResult(RESULT_OK);
						nofifyFinished();
						ContactsMarkListActivity.this.finish();
					}
				});
		builder.setCancelable(false);
		mCompConfirmDialog = builder.create();
		mCompConfirmDialog.show();
        mCompConfirmDialog
					.setOnDismissListener(new DialogInterface.OnDismissListener() {
						public void onDismiss(DialogInterface dialog) {
							if (ContactsMarkListActivity.this == null || ContactsMarkListActivity.this.isFinishing()) return;
							ContactsMarkListActivity.this.setResult(RESULT_OK);
						    nofifyFinished();
						    ContactsMarkListActivity.this.finish();
						}
					});
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (mSelectedPositions[position]) {
			mSelectedPositions[position] = false;
			mSelectedPositionsSet.remove(position);
			mSelectedCount = (mSelectedCount < 1) ? 0 : (mSelectedCount - 1);
		} else {
			mSelectedPositions[position] = true;
			mSelectedPositionsSet.add(position);
			mSelectedCount = (mSelectedCount >= mSelectedPositions.length) ? mSelectedPositions.length
					: (mSelectedCount + 1);
		}
		int firstVisiblePosition = l.getFirstVisiblePosition();
		ContactListItemView itemView = (ContactListItemView) (l
				.getChildAt(position - firstVisiblePosition));
		itemView.getCheckBox().toggle();
		
		// mtk80909 enhancement
		mCopyButton.setEnabled(mSelectedCount != 0);
		mSelectAllBox.setChecked(mSelectedCount == mSelectedPositions.length);
		mSelectAllText.setText(mSelectedCount == mSelectedPositions.length ? R.string.deselectall : R.string.selectall);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		super.onCreateOptionsMenu(menu);
//		menu.add(0, ID_MARK_ALL, 0, R.string.select_all);
//		menu.add(0, ID_UNMARK_ALL, 0, R.string.unselect_all);
//		menu.add(0, ID_COPY, 0, R.string.start_copy);
//		return true;
//	}

//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		if (mAdapter == null || mAdapter.getCount() <= 0)
//			return false;
//
//		// mtk80909 for enhancement
//		menu.findItem(ID_COPY).setVisible(false);
//
//		return true;
//	}

	private void updateCheckBoxes(boolean markAll) {
		ListView lv = getListView();
		if (lv == null)
			return;
		int firstVisiblePosition = lv.getFirstVisiblePosition();
		int lastVisiblePosition = lv.getLastVisiblePosition();
		for (int k = firstVisiblePosition; k <= lastVisiblePosition; ++k) {
			ContactListItemView itemView = (ContactListItemView) (getListView()
					.getChildAt(k - firstVisiblePosition));
			itemView.getCheckBox().setChecked(markAll);
		}
		
		// mtk80909 enhancement
		mCopyButton.setEnabled(markAll);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
			return false;
		}

	// mtk80909 enhancement
	private void startCopy() {
		if (mImportVCard) {
            //if (mProgressDialogForReadVCard == null) {
            //    String title = getString(R.string.reading_vcard_title);
            //    // adding a "\n" by mtk80909 for ALPS00220886
            //    String message = getString(R.string.reading_vcard_message) + "\n";
            //    mProgressDialogForReadVCard = new ImportProgressDlg(this);
            //    mProgressDialogForReadVCard.setTitle(title);
            //    mProgressDialogForReadVCard.setMessage(message);
            //    mProgressDialogForReadVCard.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //    mProgressDialogForReadVCard.setOnCancelListener(mVCardReadThread);
            //    mProgressDialogForReadVCard.show();
            //    List<VCardFile> selectedVCardFileList = new ArrayList<VCardFile>();
            //    if (mSelectedPositionsSet != null) {
            //        
            //        int size = mAllVCardFileList.size();
            //        // We'd like to sort the files by its index, so we do not use Set iterator.
            //        for (int i = 0; i < size; i++) {
            //            if (mSelectedPositionsSet.contains(i)) {
            //                selectedVCardFileList.add(mAllVCardFileList.get(i));
            //            }
            //        }
            //    }
            //    mVCardReadThread = new VCardReadThread(selectedVCardFileList);
            //    mVCardReadThread.start();
            //}
            if (this.mImportMultiVCardProgressDlg == null) {
                String title = getString(R.string.reading_vcard_title);
                String subTitle = getString(R.string.importing_vcard_message);
                Log.d(TAG, "startCopy(), before new ImportMultiFileProgressDlg ");
                mImportMultiVCardProgressDlg = new ImportMultiFileProgressDlg(this);
                mImportMultiVCardProgressDlg.setTitle(title);
                mImportMultiVCardProgressDlg.setProgressSubTilte(subTitle);
                mImportMultiVCardProgressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mImportMultiVCardProgressDlg.setOnCancelListener(mVCardReadThread);
                mImportMultiVCardProgressDlg.show();
                
                List<VCardFile> selectedVCardFileList = new ArrayList<VCardFile>();
                if (mSelectedPositionsSet != null) {
                    
                    int size = mAllVCardFileList.size();
                    // We'd like to sort the files by its index, so we do not use Set iterator.
                    for (int i = 0; i < size; i++) {
                        if (mSelectedPositionsSet.contains(i)) {
                            selectedVCardFileList.add(mAllVCardFileList.get(i));
                        }
                    }
                }
                mVCardReadThread = new VCardReadThread(selectedVCardFileList);
                mVCardReadThread.start();
            }
		} else {//import to Sim Card
            if(mCopyDst!=RawContacts.INDICATE_PHONE){
                Log.d(TAG,"mCellMgr.handleCellConn((int)this.mCopyDst, REQUEST_TYPE);");
				mCellMgr.handleCellConn(this.mCopyDst, REQUEST_TYPE);
                return ;
                }
                mCopyProgDialog = new ProgressDialog(this) {
				@Override
				public boolean onKeyDown(int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:
						if (mCopyThread != null && mCopyThread.isAlive()) {
							mCopyThread.mCanceled = true;
						}
						return true;
					}
					return super.onKeyDown(keyCode, event);
				}
			};
			mCopyProgDialog.setTitle(R.string.copy_procedure);
			mCopyProgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mCopyProgDialog.setMax(mSelectedCount);
			mCopyProgDialog.setCancelable(true);
			mCopyProgDialog.setProgress(0);
			mCopyProgDialog.show();
			mCopyProgDialog
					.setOnDismissListener(new DialogInterface.OnDismissListener() {
						public void onDismiss(DialogInterface dialog) {
							if (ContactsMarkListActivity.this == null || ContactsMarkListActivity.this.isFinishing()) return;
							initCompConfirmDialog();
						}
					});
			mCopyThread = new CopyThread();
			mCopyThread.start();
		}
	}

	private boolean isReadyForCopy() {
		final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
		if (null == iTel)
			return false;
		try {
			if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) { // Gemini
//				--simId;
				Log.i(TAG,"iTel.hasIccCardGemini(simId) is " + iTel.hasIccCardGemini(mCopyDst));
				Log.i(TAG,"iTel.isRadioOnGemini(simId) is " + iTel.isRadioOnGemini(mCopyDst));
				Log.i(TAG,"iTel.isFDNEnabledGemini(simId) is " + iTel.isFDNEnabledGemini(mCopyDst));
				Log.i(TAG,"TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(simId) " + (TelephonyManager.SIM_STATE_READY == TelephonyManager.getDefault().getSimStateGemini(mCopyDst)));
				return iTel.hasIccCardGemini(mCopyDst)
						&& iTel.isRadioOnGemini(mCopyDst)
						&& !iTel.isFDNEnabledGemini(mCopyDst)
						&& TelephonyManager.SIM_STATE_READY == TelephonyManager
								.getDefault().getSimStateGemini(mCopyDst);
			} else { // Single SIM
				return iTel.hasIccCard()
						&& iTel.isRadioOn()
						&& !iTel.isFDNEnabled()
						&& TelephonyManager.SIM_STATE_READY == TelephonyManager
								.getDefault().getSimState();
			}
		} catch (RemoteException e) {
			Log.w(TAG, "RemoteException!");
			return false;
		}
	}
	
	private class QueryHandler extends AsyncQueryHandler {
		protected final WeakReference<ContactsMarkListActivity> mActivity;

		public QueryHandler(Context context) {
			super(context.getContentResolver());
			mActivity = new WeakReference<ContactsMarkListActivity>(
					(ContactsMarkListActivity) context);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			Log.i(TAG, "cursor is " + cursor);
			Log.i(TAG, "cursor.getcount is " + cursor.getCount());
			final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
					.getService(Context.TELEPHONY_SERVICE));
			final ContactsMarkListActivity activity = mActivity.get();
			
			if (mQueryDialog != null && mQueryDialog.isShowing()&& !ContactsMarkListActivity.this.isFinishing()) {
				mQueryDialog.dismiss();
				mQueryDialog = null;
			}
			if (mCopySrc >= 0) {
			try {
				if (com.mediatek.featureoption.FeatureOption.MTK_GEMINI_SUPPORT) {
						if (iTel == null || (!iTel.isRadioOnGemini(mCopySrc))) {
					cursor = null;
				}
				} else {
					if (iTel == null || (!iTel.isRadioOn())) {
						cursor = null;
					}
				}	
				
			} catch (RemoteException e) {
				Log.w(TAG, "RemoteException!");
				return;
				}
			}
			
			if (!activity.mAdapter.isUpdateRegistered()) {
				if (cursor != null) cursor.close();
				return;
			}
			
			activity.mAdapter.changeCursor(cursor);
			activity.mSelectedCount = 0;
			activity.mSelectedPositions = new boolean[activity.mAdapter
					.getCount()];
			activity.mEmptyText.setVisibility(View.VISIBLE);
			
			// mtk80909 enhancement
			activity.mCopyButton.setEnabled(false);
			if (cursor == null || cursor.getCount() == 0) {
				activity.mButtonsLayout.setVisibility(View.GONE);
				activity.mSelectAllLayout.setVisibility(View.GONE);
				activity.mSplitLine1.setVisibility(View.GONE);
				activity.mSplitLine2.setVisibility(View.GONE);
				activity.mSrcAndDst.setVisibility(View.GONE);
			} else {
				activity.mButtonsLayout.setVisibility(View.VISIBLE);
				activity.mSelectAllLayout.setVisibility(View.VISIBLE);
				activity.mSelectAllBox.setChecked(false);
				activity.mSelectAllText.setText(R.string.selectall);
				activity.mSplitLine1.setVisibility(View.VISIBLE);
				activity.mSplitLine2.setVisibility(View.VISIBLE);
				activity.mSrcAndDst.setVisibility(View.VISIBLE);
			}
			
			activity.mAdapter.unregisterUpdate();
		}
	}

	private final class ContactItemListAdapter extends CursorAdapter implements
			SectionIndexer, OnScrollListener,
			PinnedHeaderListView.PinnedHeaderAdapter {
		CharSequence mUnknownNameText;
		private boolean mUpdateRegistered = true;

		public ContactItemListAdapter(Context context) {
			super(context, null, false);
			mUnknownNameText = context.getText(android.R.string.unknownName);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			
			final ContactListItemView itemview = (ContactListItemView) view;
			final ContactListItemCache cache = (ContactListItemCache) view.getTag();
			
			if (COPY_FROM_PHONE_USIM_TO_USIM || (!isSrcSimUSIM && isDstSimUSIM)) {
			String name = null;
			String number = null;
				int contactId = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts._ID));
				Log.i(TAG, "contactId is " + contactId);			
			ContentResolver resolver = getContentResolver();
			long rawContactId = ContactsUtils.queryForRawContactId(resolver, contactId);
			Log.i(TAG, "rawContactId is " + rawContactId);
			Cursor c = getContentResolver().query(Data.CONTENT_URI, new String[] { Data.MIMETYPE, Data.DATA1},
					Data.RAW_CONTACT_ID + "=" + rawContactId, null, null);
			if (null != c) {
				while (c.moveToNext()) {
				if (c.getString(0).equals(StructuredName.CONTENT_ITEM_TYPE)) {
					name = c.getString(1);
	//				Log.i(TAG,"In run name is " + name);
					Log.i(TAG,"USIM name is " + name);
				}
				if (TextUtils.isEmpty(name)) {
					name = cursor.getString(PHONE_NUMBER_COLUMN_INDEX);
					Log.i(TAG,"name is null name is " + name);
				}
				}
				c.close();
			}
			
//				name = cursor.getString(PHONE_NUMBER_COLUMN_INDEX);
//				Log.i(TAG,"USIM name is " + name);
//				String namecolumn = cursor.getString(PHONE_DISPLAY_NAME_COLUMN_INDEX);
				
//			} 
//			else {
//				name = cursor.getString(PHONE_DISPLAY_NAME_COLUMN_INDEX);
//				number = cursor.getString(PHONE_NUMBER_COLUMN_INDEX);
//				Log.i(TAG, "SIM name is " + name + " number is " + number);
//			}

			final String label = cursor.getString(PHONE_LABEL_COLUMN_INDEX);
			final int type = cursor.getInt(PHONE_TYPE_COLUMN_INDEX);
			// Log.i(TAG, "name is " + name + " label is " + label + " number is "
			//		+ number + " type is " + type);
			TextView nameView = itemview.getNameTextView();
			nameView.setText(TextUtils.isEmpty(name) ? mUnknownNameText : name);
//				itemview.setLabel(Phone.getTypeLabel(context.getResources(), type, label));
//				cursor.copyStringToBuffer(PHONE_NUMBER_COLUMN_INDEX, cache.dataBuffer);
//				int size = cache.dataBuffer.sizeCopied;
//					Log.i(TAG, "cache.dataBuffer is " + cache.dataBuffer + " size is " + size);
//				itemview.setData(cache.dataBuffer.data, size);				
			} 
			else if (dstSimId == RawContacts.INDICATE_PHONE) {//copy from sim or usim to phone
				Log.i(TAG,"copy from sim or usim to phone");
				String name = cursor.
						getString(ContactsListActivity.SUMMARY_DISPLAY_NAME_PRIMARY_COLUMN_INDEX);
				TextView nameView = itemview.getNameTextView();
				nameView.setText(TextUtils.isEmpty(name) ? mUnknownNameText : name);
			} 
			else if (!isDstSimUSIM) {
				String name = cursor.getString(PHONE_DISPLAY_NAME_COLUMN_INDEX);
				final String label = cursor.getString(PHONE_LABEL_COLUMN_INDEX);
				final int type = cursor.getInt(PHONE_TYPE_COLUMN_INDEX);
				// Log.i(TAG, "name is " + name + " label is " + label + " number is "
				//		+ number + " type is " + type);
				TextView nameView = itemview.getNameTextView();
				nameView.setText(TextUtils.isEmpty(name) ? mUnknownNameText : name);
				itemview.setLabel(Phone.getTypeLabel(context.getResources(), type,
						label));
			cursor.copyStringToBuffer(PHONE_NUMBER_COLUMN_INDEX, cache.dataBuffer);
			int size = cache.dataBuffer.sizeCopied;
				Log.i(TAG, "cache.dataBuffer is " + cache.dataBuffer + " size is "
						+ size);
			itemview.setData(cache.dataBuffer.data, size);
			} else if (mImportVCard) {
				String fileName = cursor.getString(MATRIX_CURSOR_FILE_NAME_INDEX);
				String lastModified = cursor.getString(MATRIX_CURSOR_LAST_MODIFIED_INDEX);
				TextView nameView = itemview.getNameTextView();
				//nameView.setSingleLine(false);
				//nameView.setMaxLines(3);
				nameView.setText(fileName);
				TextView dataView = itemview.getDataView();
				dataView.setText(lastModified);
			} else if (mExportVCard) {
				String name = cursor.getString(PHONE_DISPLAY_NAME_COLUMN_INDEX);
				final String label = cursor.getString(PHONE_LABEL_COLUMN_INDEX);
				final int type = cursor.getInt(PHONE_TYPE_COLUMN_INDEX);
				TextView nameView = itemview.getNameTextView();
				nameView.setText(TextUtils.isEmpty(name) ? mUnknownNameText
						: name);
				cursor.copyStringToBuffer(PHONE_NUMBER_COLUMN_INDEX,
						cache.dataBuffer);
				int size = cache.dataBuffer.sizeCopied;
				Log.i(TAG, "cache.dataBuffer is " + cache.dataBuffer
						+ " size is " + size);
			}
				
								
			CheckBox cb = itemview.getCheckBox();
			cb.setChecked(mSelectedPositions[cursor.getPosition()]);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			final ContactListItemView view = new ContactListItemView(context,
					null);
			// view.setOnCallButtonClickListener(ContactsMarkListActivity.this);
			ContactListItemCache cache = new ContactListItemCache();
			//view.setCheckBox();
			view.setTag(cache);
			return view;
		}

		public int getPositionForSection(int section) {
			// TODO Auto-generated method stub
			return 0;
		}

		public int getSectionForPosition(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		public Object[] getSections() {
			// TODO Auto-generated method stub
			return null;
		}

		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			
		}

		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			
		}

		public void configurePinnedHeader(View header, int position, int alpha) {
			// TODO Auto-generated method stub
			
		}

		public int getPinnedHeaderState(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		public void registerUpdate() {
			Log.i(TAG, "register");
			mCursor.registerContentObserver(mChangeObserver);
			mCursor.registerDataSetObserver(mDataSetObserver);
			mUpdateRegistered = true;
		}

		public void unregisterUpdate() {
			Log.i(TAG, "unregister");
			if (mCursor != null) {
			mCursor.unregisterContentObserver(mChangeObserver);
			mCursor.unregisterDataSetObserver(mDataSetObserver);
			mUpdateRegistered = false;
			}
		}

		public boolean isUpdateRegistered() {
			return mUpdateRegistered;
		}
	}

	private class CopyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			final int msgId = msg.what;
			switch (msgId) {
			case ID_IN_COPY:
                Log.d(TAG, " *******handleMessage(), ID_IN_COPY");
				if (mCopyProgDialog.getProgress() >= mCopyProgDialog.getMax()) {
                    Log.d(TAG, " *******handleMessage(), ID_IN_COPY, call tryToDismissCopyProgDialog()");
					tryToDismissCopyProgDialog();
				} else {
					if (null != mCopyProgDialog)
						mCopyProgDialog.incrementProgressBy(1);
				}
				break;
			case ID_END_COPY:
                Log.d(TAG, " *******handleMessage(), ID_END_COPY");
                
				tryToDismissCopyProgDialog();
			}
		}
	}
	
	private void tryToDismissCopyProgDialog () {
		if (null != mCopyProgDialog && mCopyProgDialog.isShowing()&& !ContactsMarkListActivity.this.isFinishing()) {
			try {
				mCopyProgDialog.dismiss();
				//setResult(RESULT_OK);
				//finish();
			} catch (IllegalArgumentException e) {
				Log.i(TAG,"IllegalArgumentException view no attached to window manager! ");
			}
			mCopyProgDialog = null;
		}
	}

	private class CopyThread extends Thread {
		public boolean mCanceled;
		PowerManager.WakeLock mWakeLock;
		private ContentResolver mResolver = getContentResolver();

		public CopyThread() {
			super();
			Context context = ContactsMarkListActivity.this;
			PowerManager powerManager = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			mWakeLock = powerManager.newWakeLock(
					PowerManager.SCREEN_DIM_WAKE_LOCK
							| PowerManager.ON_AFTER_RELEASE, TAG);
		}

		@Override
		public void run() {
			mWakeLock.acquire();
			mCanceled = false;
			mBeingCopied = true;
			mSuccessCount = 0;

			mErrorNotification = null;
			int sameNameCount = 0;
			// String prevName = ""; // handling duplicated names
			String name = null;
			String number = null;
			Cursor cursor = null;
			int type = -1;
			int displayNameSrc;
			Uri dstUri = null;
			int realLenOfNum = 0;
			int realLenOfAdditionalNum = 0;
			int realLenOfEmail = 0;
			Log.i(TAG,"isDstSimUSIM is " + isDstSimUSIM);
            if (dstSimId == RawContacts.INDICATE_PHONE) {
				dstUri = null;
			} else {
				dstUri = ContactsUtils.getUri(mCopyDst);			
			}			
			if (isDstSimUSIM && (srcSimId == RawContacts.INDICATE_PHONE || isSrcSimUSIM)) {//copy from phone or usim to usim
				Log.i(TAG,"copy from phone to usim dstSimId is " + dstSimId);
				boolean storageFull = false;
					for (Iterator<Integer> it = mSelectedPositionsSet.iterator(); it.hasNext() && !mCanceled; /* No increment */) {
						Log.i(TAG,"mSelectedPositionsSet.size() is " + mSelectedPositionsSet.size());
                        boolean insertUsimFlag = false;
                        Log.d(TAG, "********* insertUsimFlag = false");
						int position = it.next();
						cursor = (Cursor) getListAdapter().getItem(position);
						int contactId = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts._ID));
						Log.i(TAG, "contactId is " + contactId);
					String[] Number = new String[100];
					String[] AdditionalNumber = new String[100];
					String[] Emails = new String[100];
					ContentResolver resolver = getContentResolver();
					long rawContactId = ContactsUtils.queryForRawContactId(resolver, contactId);
					Log.i(TAG, "rawContactId is " + rawContactId);
					Cursor c = getContentResolver().query(Data.CONTENT_URI, new String[] { Data.MIMETYPE, Data.DATA1, Data.IS_ADDITIONAL_NUMBER},
							Data.RAW_CONTACT_ID + "=" + rawContactId, null, null);
					if (null != c) {
						while (c.moveToNext()) {
							Log.i(TAG, "c.getCount() is " + c.getCount() + " mimeType is " + c.getString(0) + " data1 is" + c.getString(1)
									+ " is_additional_number is " + c.getString(2));
							if (c.getString(0).equals(Phone.CONTENT_ITEM_TYPE) && c.getString(2).equals("0")) {
								Number[realLenOfNum] = c.getString(1);
								Log.i(TAG,"Number[" + realLenOfNum + "] is " + Number[realLenOfNum] );
								realLenOfNum++;
							}
							if (c.getString(0).equals(Phone.CONTENT_ITEM_TYPE) && c.getString(2).equals("1")) {//additional number
								AdditionalNumber[realLenOfAdditionalNum] = c.getString(1);
								Log.i(TAG,"AdditionalNumber[" + realLenOfAdditionalNum + "] is " + AdditionalNumber[realLenOfAdditionalNum] );
								realLenOfAdditionalNum++;
							}
							if (c.getString(0).equals(StructuredName.CONTENT_ITEM_TYPE)) {
								name = c.getString(1);
								Log.i(TAG,"In run name is " + name);
							}
							if (c.getString(0).equals(Email.CONTENT_ITEM_TYPE)) {
								Emails[realLenOfEmail] = c.getString(1);
								Log.i(TAG,"Emails[" + realLenOfEmail + "] is " + Emails[realLenOfEmail] );
								realLenOfEmail++;
							}

						}
						c.close();
					}
					
					ContentValues values = new ContentValues();
					int l, m ,n;
					for (l=0, m=0, n=0; l<realLenOfNum || m<realLenOfAdditionalNum || n<realLenOfEmail; l++, m++, n++) {
							values.put("tag", TextUtils.isEmpty(((sameNameCount > 0) ? (name + sameNameCount): name))? "": ((sameNameCount > 0) ? (name + sameNameCount): name));
							values.put("number", TextUtils.isEmpty(Number[l])? "": Number[l].replaceAll("-", ""));
							values.put("emails", TextUtils.isEmpty(Emails[n])? "": Emails[n]);
							values.put("anr", TextUtils.isEmpty(AdditionalNumber[m])? "": AdditionalNumber[m].replaceAll("-", ""));
//							l++;
//							values.put("anr", TextUtils.isEmpty(Number[l])? "": Number[l]);
                            if (TextUtils.isEmpty(Number[l])) {
                                Log.d(TAG, " number empty, break");
                                break;
                            }
							Log.i(TAG,"USIM values single is " + values);
							Uri checkUri = mResolver.insert(dstUri, values);
							Log.i(TAG,"checkUri is " + checkUri);
							List<String> checkUriPathSegs = checkUri.getPathSegments();
							if ("error".equals(checkUriPathSegs.get(0))) { // insert fail
								Log.i(TAG, "error code = " + checkUriPathSegs.get(1));
								if ("-3".equals(checkUriPathSegs.get(1))) {
									mErrorNotification = getString(R.string.storage_full);
									storageFull = true;
                                    Log.d(TAG, "********* storage full");
									break; // SIM card filled up, won't continue copying
								}
                                Log.d(TAG, "********* insert fail");
							} else {
							    insertUsimFlag = true;
                                Log.d(TAG, "********* insertUsimFlag = true");
								String realName = TextUtils.isEmpty(((sameNameCount > 0) ? (name + sameNameCount): name))? null: ((sameNameCount > 0) ? (name + sameNameCount): name);
								ContactsUtils.insertToDB(realName, Number[l], Emails[n], AdditionalNumber[m], mResolver, dstSimId, "USIM" );
								// prevName = name;
								/*++mSuccessCount;
                                Log.d(TAG, "********** before send ID_IN_COPY, mSuccessCount="+ mSuccessCount);
								mCopyHandler.sendEmptyMessage(ID_IN_COPY);*/
							}
					}
					realLenOfNum = 0;
					realLenOfAdditionalNum = 0;
					realLenOfEmail = 0;
					if (storageFull) break;
                    if (insertUsimFlag ) {
                        ++mSuccessCount;
                        Log.d(TAG, "********** before send ID_IN_COPY, mSuccessCount="+ mSuccessCount);
                        mCopyHandler.sendEmptyMessage(ID_IN_COPY);
                    }
                    
					}
					
			} else if ( (!isDstSimUSIM && dstSimId != RawContacts.INDICATE_PHONE)) {//copy to sim
				Log.i(TAG,"!isDstSimUSIM && dstSimId != RawContacts.INDICATE_PHONE copy to sim"+" mCanceled = "+ mCanceled);
                Log.i(TAG,"mSelectedPositionsSet.size() is " + mSelectedPositionsSet.size());

                    for (Iterator<Integer> it = mSelectedPositionsSet.iterator(); it.hasNext() && !mCanceled; /* No increment */) {
                Log.d(TAG,"copy one phone contact to sim ");
                int position = it.next();
				cursor = (Cursor) getListAdapter().getItem(position);
				name = cursor.getString(PHONE_DISPLAY_NAME_COLUMN_INDEX);
				number = cursor.getString(PHONE_NUMBER_COLUMN_INDEX);
				displayNameSrc = cursor.getInt(PHONE_DISPLAY_NAME_SOURCE_INDEX);
				
				// for CR ALPS00031793
				// If there is no structured name, the display name will come from 
				// phone numbers, e-mails and other data fields.
				// At this moment, we don't copy the display name into the SIM card.
				if (displayNameSrc != ContactsContract.DisplayNameSources.STRUCTURED_NAME) {
					name = "";
				}
				
				if (name == null)	name = "";
				if (number == null)	number = "";
				number = number.replaceAll("-", "");	// Revert the formatted number
				if (TextUtils.isEmpty(number) || !Pattern.matches(SIM_NUM_PATTERN, number)) { // Empty number or invalid number
					continue;
				}
				if (!isReadyForCopy()) {
							mErrorNotification = getString(R.string.sim_card_state_illegal);
					break;
				}
				// sameNameCount = (!TextUtils.isEmpty(name) &&
				// name.equals(prevName)) ? (sameNameCount + 1) : 0;

				ContentValues values = new ContentValues();
				values.put("tag", (sameNameCount > 0) ? (name + sameNameCount)
						: name);
				values.put("number", number);
				Uri checkUri = mResolver.insert(dstUri, values);
				List<String> checkUriPathSegs = checkUri.getPathSegments();
				if ("error".equals(checkUriPathSegs.get(0))) { // insert fail
					Log.i(TAG, "error code = " + checkUriPathSegs.get(1));
					if ("-3".equals(checkUriPathSegs.get(1))) {
								mErrorNotification = getString(R.string.storage_full);
						break; // SIM card filled up, won't continue copying
					}
				} else {
						Log.i(TAG,"copy to sim before insertToDb ");
						insertToDb((sameNameCount > 0) ? (name + sameNameCount)
								: name, number, -1);
					// prevName = name;
					++mSuccessCount;
				}
					mCopyHandler.sendEmptyMessage(ID_IN_COPY);
				}
			} else if (!isSrcSimUSIM && (dstSimId == RawContacts.INDICATE_PHONE || isDstSimUSIM)) {//copy from sim to phone
				Log.i(TAG,"copy from sim to phone  or copy from sim to usim ");
				for (Iterator<Integer> it = mSelectedPositionsSet.iterator(); it.hasNext() && !mCanceled; /* No increment */) {
					int position = it.next();
					cursor = (Cursor) getListAdapter().getItem(position);					
					long contactId = cursor.getLong(ContactsListActivity.SUMMARY_ID_COLUMN_INDEX);
					name = cursor.getString(ContactsListActivity.SUMMARY_DISPLAY_NAME_PRIMARY_COLUMN_INDEX);
					Log.i(TAG,"copy from sim to phone  or copy from sim to usim name is " + name);					
					Cursor phoneCursor = queryPhoneNumbers(contactId);
					number = "";
					type = -1;
					if (isDstSimUSIM && !isReadyForCopy()) {
								mErrorNotification = getString(R.string.sim_card_state_illegal);
						break;
					}
					if (phoneCursor != null && phoneCursor.moveToFirst()) {
						number = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER));
						type = phoneCursor.getInt(phoneCursor.getColumnIndex(Phone.TYPE));
						phoneCursor.close();
					}
						ContentValues values = new ContentValues();
					if (!TextUtils.isEmpty(number)) number = number.replaceAll("-", "");
					if (!TextUtils.isEmpty(name) && name.equals(number)) {
							values.put("tag", "");
							values.put("number", number);
							name = null;
						} else {
							values.put("tag", (sameNameCount > 0) ? (name + sameNameCount)
									: name);
							values.put("number", number);
						}
					if (isDstSimUSIM) {						
						Log.i(TAG,"******dstUri is " + dstUri);
						Log.i(TAG,"******values is " + values);
						Uri checkUri = mResolver.insert(dstUri, values);
						List<String> checkUriPathSegs = checkUri.getPathSegments();
						if ("error".equals(checkUriPathSegs.get(0))) { // insert fail
							Log.i(TAG, "copy from sim to phone  or copy from sim to usim error code = " + checkUriPathSegs.get(1));
							if ("-3".equals(checkUriPathSegs.get(1))) {
										mErrorNotification = getString(R.string.storage_full);
								break; // SIM card filled up, won't continue copying
							}
						} else {
								Log.i(TAG,"copy from sim to phone  or copy from sim to usim copy to sim before insertToDb ");
								insertToDb((sameNameCount > 0) ? (name + sameNameCount)
										: name, number, type);
							// prevName = name;
							++mSuccessCount;
							mCopyHandler.sendEmptyMessage(ID_IN_COPY);
						}
						
					} else {
					insertToDb(name, number, type);
					++mSuccessCount;
					mCopyHandler.sendEmptyMessage(ID_IN_COPY);
					}
				}
			} else if (isSrcSimUSIM && dstSimId == RawContacts.INDICATE_PHONE) {//copy from usim to phone
				Log.i(TAG,"copy from usim to phone");
				for (Iterator<Integer> it = mSelectedPositionsSet.iterator(); it.hasNext() && !mCanceled; /* No increment */) {
					int position = it.next();
					String email = null;
					String additionalNumber = null;
					cursor = (Cursor) getListAdapter().getItem(position);					
					long contactId = cursor.getLong(ContactsListActivity.SUMMARY_ID_COLUMN_INDEX);
//					name = cursor.getString(ContactsListActivity.SUMMARY_DISPLAY_NAME_PRIMARY_COLUMN_INDEX);
					
					long rawContactId = ContactsUtils.queryForRawContactId(getContentResolver(), contactId);
					Log.i(TAG, "copy from usim to phone rawContactId is " + rawContactId);
					Cursor c = getContentResolver().query(Data.CONTENT_URI, new String[] { Data.MIMETYPE, Data.DATA1, Data.IS_ADDITIONAL_NUMBER},
							Data.RAW_CONTACT_ID + "=" + rawContactId, null, null);
					if (null != c) {
						while (c.moveToNext()) {
							Log.i(TAG, "copy from usim to phone c.getCount() is " + c.getCount() + " mimeType is " + c.getString(0) + " data1 is" + c.getString(1)
									+ " is_additional_number is " + c.getString(2));
//							if (c.getString(0).equals(Phone.CONTENT_ITEM_TYPE) && c.getString(2).equals("0")) {
//								Number[realLenOfNum] = c.getString(1);
//								Log.i(TAG,"Number[" + realLenOfNum + "] is " + Number[realLenOfNum] );
//								realLenOfNum++;
//							}
							if (c.getString(0).equals(Phone.CONTENT_ITEM_TYPE) && c.getString(2).equals("1")) {//additional number
								additionalNumber = c.getString(1);
								Log.i(TAG,"copy from usim to phone additionalNumber is " + additionalNumber);
							}
							if (c.getString(0).equals(StructuredName.CONTENT_ITEM_TYPE)) {
								name = c.getString(1);
								Log.i(TAG,"In run name is " + name);
							}
							if (c.getString(0).equals(Email.CONTENT_ITEM_TYPE)) {
								email = c.getString(1);
								Log.i(TAG,"copy from usim to phone email is " + email);
							}
							
						}
						c.close();
					}
					Cursor phoneCursor = queryPhoneNumbers(contactId);
					number = "";
					type = -1;
					if (phoneCursor != null && phoneCursor.moveToFirst()) {
						number = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER));
						type = phoneCursor.getInt(phoneCursor.getColumnIndex(Phone.TYPE));
						phoneCursor.close();
					}
					Log.i(TAG,"copy from usim to phone name is " + name);
					Log.i(TAG,"copy from usim to phone number is " + number);
					ContactsUtils.insertToDB(name, number, email, additionalNumber, mResolver, dstSimId, "USIM" );
//					insertToDb(name, number, type);
					++mSuccessCount;
				mCopyHandler.sendEmptyMessage(ID_IN_COPY);
			}
			}
			mFailCount = mSelectedCount - mSuccessCount;
			mBeingCopied = false;
            Log.d(TAG, " **********before send ID_END_COPY, mFailCount="+ mFailCount+ "; mSelectedCount="+mSelectedCount+"; mSuccessCount="+mSuccessCount);
			mCopyHandler.sendEmptyMessage(ID_END_COPY);

			mWakeLock.release();
		}

		private Cursor queryPhoneNumbers(long contactId) {
			Uri baseUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
					contactId);
			Uri dataUri = Uri.withAppendedPath(baseUri,
					Contacts.Data.CONTENT_DIRECTORY);
			Cursor c = getContentResolver().query(
					dataUri,
					new String[] { Phone._ID, Phone.NUMBER, Phone.IS_SUPER_PRIMARY,
							RawContacts.ACCOUNT_TYPE, Phone.TYPE, Phone.LABEL,
							Data.DATA15 }, Data.MIMETYPE + "=?",
					new String[] { Phone.CONTENT_ITEM_TYPE }, null);
			if (c != null && c.moveToFirst()) {
				return c;
			}
			if (c != null) c.close();
			return null;
		}
		
		@Override
		public void finalize() {
			if (mWakeLock != null && mWakeLock.isHeld()) {
				mWakeLock.release();
			}
		}

		private void insertToDb(String name, String number, int type) {
			Log.i(TAG,"name is " + name + " number is " + number + "dstSimId is " + dstSimId);
			ContentValues values = new ContentValues();
			final ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();

			// insert basic information to raw_contacts table
			ContentProviderOperation.Builder builder = ContentProviderOperation
					.newInsert(RawContacts.CONTENT_URI);
			values.put(RawContacts.INDICATE_PHONE_SIM, dstSimId);
				
			if (dstSimId > RawContacts.INDICATE_PHONE) {
			values.put(RawContacts.AGGREGATION_MODE,
					RawContacts.AGGREGATION_MODE_DISABLED);
			}
			
			String myGroupsId = null;
	        if (mAccount != null) {
	            builder.withValue(RawContacts.ACCOUNT_NAME, mAccount.name);
	            builder.withValue(RawContacts.ACCOUNT_TYPE, mAccount.type);

	            // TODO: temporal fix for "My Groups" issue. Need to be refactored.
	            if (ACCOUNT_TYPE_GOOGLE.equals(mAccount.type)) {
	                final Cursor tmpCursor = mResolver.query(Groups.CONTENT_URI, new String[] {
	                        Groups.SOURCE_ID },
	                        Groups.TITLE + "=?", new String[] {
	                        GOOGLE_MY_CONTACTS_GROUP }, null);
	                try {
	                    if (tmpCursor != null && tmpCursor.moveToFirst()) {
	                        myGroupsId = tmpCursor.getString(0);
	                    }
	                } finally {
	                    if (tmpCursor != null) {
	                        tmpCursor.close();
	                    }
	                }
	            }
	        }
			builder.withValues(values);
			operationList.add(builder.build());

			// insert phone number to data table
			if (!TextUtils.isEmpty(number)) {
			Log.i(TAG,"PhoneNumberFormatUtilEx.formatNumber(number) is " + number);
			number = PhoneNumberFormatUtilEx.formatNumber(number);
			builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
			builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
			builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
			builder.withValue(Phone.NUMBER, number);
//				if (type < 0) {
//			builder.withValue(Phone.TYPE, Phone.TYPE_OTHER); 
//				} else {
					builder.withValue(Data.DATA2, 2);
//				}
			operationList.add(builder.build());
			}
			

			// insert name to data table
			if (!TextUtils.isEmpty(name)) {
			builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
			builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
			builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
				builder.withValue(StructuredName.GIVEN_NAME, name);
			operationList.add(builder.build());
			}
			

			try {
				mResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
			} catch (RemoteException e) {
				Log.e(TAG, String
						.format("%s: %s", e.toString(), e.getMessage()));
			} catch (OperationApplicationException e) {
				Log.e(TAG, String
						.format("%s: %s", e.toString(), e.getMessage()));
			}
		}
	}

	// mtk80909 enhancement
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		final int vId = v.getId();
		switch (vId) {
		case R.id.back_button:
			setResult(RESULT_CANCELED);
			finish();
			return;
		case R.id.copy_button:
			if (this.mExportVCard) {
				StringBuilder contactsID = new StringBuilder();
				int curIndex = 0;
				for (Iterator<Integer> it = mSelectedPositionsSet.iterator(); it
						.hasNext()
						&& curIndex < mSelectedCount;) {
					int position = it.next();
					Cursor cursor = (Cursor) getListAdapter().getItem(position);
					if (null == cursor) {
						Log.i(TAG, "OnClick cursor is null");
						continue;
					}
					String id = cursor.getString(PHONE_ID_COLUMN_INDEX);
					if (null == id) {
						id = "";
						Log.i(TAG, "OnClick contactId is null");
						continue;
					}
					if (TextUtils.isEmpty(id)) {
						Log.i(TAG, "OnClick contactId is empty");
						continue;
					}
					if (curIndex++ != 0) {
						contactsID.append("," + id);
					} else {
						contactsID.append(id);
					}
					
				}

				Intent it = new Intent(this, ExportVCardActivity.class);
				it.putExtra("multi_export_type", 1);
				it.putExtra("multi_export_contacts", contactsID.toString());
				this.startActivity(it);
			} else {
				startCopy();
			}
			return;
		case R.id.selectAll_layout: {
            if (mSelectAllDialog == null) {
			mSelectAllDialog = new ProgressDialog(this);
			mSelectAllDialog.setCancelable(false);
			mSelectAllDialog.setMessage(getString(
					R.string.please_wait));
			mSelectAllDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            }
			mSelectAllDialog.show();
			mSelectAllBox.toggle();
			boolean isChecked = mSelectAllBox.isChecked();
			mSelectAllText.setText(isChecked ? R.string.deselectall : R.string.selectall);
			new Thread() {
				@Override
				public void run() {
					for (int k = 0; k < mSelectedPositions.length; ++k) {
						mSelectedPositions[k] = (mSelectAllBox.isChecked()) ? true : false;
						if (mSelectAllBox.isChecked()) mSelectedPositionsSet.add(k);
						else mSelectedPositionsSet.remove(k);
					}
                    if (mSelectAllDialog != null && mSelectAllDialog.isShowing()&& !ContactsMarkListActivity.this.isFinishing()) {
					mSelectAllDialog.dismiss();
			        }

				}
			}.start();
			updateCheckBoxes(isChecked);
			mSelectedCount = (isChecked) ? mSelectedPositions.length : 0;
			return;
		}
		default:
			Log.w(TAG, "Should never rich here");	
		}
}
	
	// mtk80909 enhancement
	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		super.onBackPressed();
	}
	
	// mtk80909 enhancement
    @Override
    protected Dialog onCreateDialog(int resId) {
        switch (resId) {
            case R.id.dialog_searching_vcard: {
                if (mProgressDialogForScanVCard == null) {
                    String title = getString(R.string.searching_vcard_title);
                    String message = getString(R.string.searching_vcard_message);
                    // mtk80909 start
                    mProgressDialogForScanVCard =
                        ProgressDialog.show(this, title, message, true, true, mVCardScanThread);
                    //mProgressDialogForScanVCard.setOnCancelListener(mVCardScanThread);
                    // mtk80909 end
                    if(mVCardScanThread==null){
                         Log.d(TAG,"before start mVCardScanThread==NULL");
                        } else {
                            mVCardScanThread.start();
                        }
                }
                return mProgressDialogForScanVCard;
            }
            case R.id.dialog_sdcard_not_found: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.no_sdcard_title)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.no_sdcard_message)
                    .setOnCancelListener(mCancelListener)
                    .setPositiveButton(android.R.string.ok, mCancelListener);
                return builder.create();
            }
            case R.id.dialog_vcard_not_found: {
                String message = (getString(R.string.scanning_sdcard_failed_message,
                        getString(R.string.fail_reason_no_vcard_file)));
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.scanning_sdcard_failed_title)
                    .setMessage(message)
                    .setOnCancelListener(mCancelListener)
                    .setPositiveButton(android.R.string.ok, mCancelListener);
                return builder.create();
            }
            case R.id.dialog_reading_vcard: {
                if (mProgressDialogForReadVCard == null) {
                    String title = getString(R.string.reading_vcard_title);
                    // adding a "\n" by mtk80909 for ALPS00220886
                    String message = getString(R.string.reading_vcard_message) + "\n";
                    mProgressDialogForReadVCard = new ImportProgressDlg(this);
                    mProgressDialogForReadVCard.setTitle(title);
                    mProgressDialogForReadVCard.setMessage(message);
                    mProgressDialogForReadVCard.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialogForReadVCard.setOnCancelListener(mVCardReadThread);
                    if(mVCardScanThread!=null){
                        mVCardReadThread.start();
                        }
                }
                return mProgressDialogForReadVCard;
            }
            case R.id.dialog_io_exception: {
                String message = (getString(R.string.scanning_sdcard_failed_message,
                        getString(R.string.fail_reason_io_error)));
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.scanning_sdcard_failed_title)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(message)
                    .setOnCancelListener(mCancelListener)
                    .setPositiveButton(android.R.string.ok, mCancelListener);
                return builder.create();
            }
            case R.id.dialog_error_with_message: {
                String message = mErrorMessage;
                if (TextUtils.isEmpty(message)) {
                    Log.e(LOG_TAG, "Error message is null while it must not.");
                    message = getString(R.string.fail_reason_unknown);
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.reading_vcard_failed_title))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(message)
                    .setOnCancelListener(mCancelListener)
                    .setPositiveButton(android.R.string.ok, mCancelListener);
                return builder.create();
            }
        }

        return super.onCreateDialog(resId);
    }
        private PBKLoadReceiver  mPBKBroadCastReceiver = new PBKLoadReceiver();   
        private ClockHandler     mClockHandler= new ClockHandler();
//notify  ImportExportBridageActivity finished;
    private void nofifyFinished()
    {
        Log.d(TAG,"nofifyFinished");
        ImportExportBridgeActivity.mShouldFinish = true;
        Intent  intent = new Intent(ImportExportBridgeActivity.ACTION_SHOULD_FINISHED);//
        this.sendBroadcast(intent);
    }

    private Runnable serviceComplete = new Runnable() {
		public void run() {
			Log.d(TAG, "serviceComplete run");
			
			int nRet = mCellMgr.getResult();
			Log.d(TAG, "serviceComplete result = " + CellConnMgr.resultToString(nRet));
			if (mCellMgr.RESULT_ABORT == nRet) {
				return;
			}

			// **********************************************************

		
			mCopyProgDialog = new ProgressDialog(ContactsMarkListActivity.this) {
				@Override
				public boolean onKeyDown(int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:
						if (mCopyThread != null && mCopyThread.isAlive()) {
							mCopyThread.mCanceled = true;
						}
						return true;
					}
					return super.onKeyDown(keyCode, event);
				}
			};
			mCopyProgDialog.setTitle(R.string.copy_procedure);
			mCopyProgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mCopyProgDialog.setMax(mSelectedCount);
			mCopyProgDialog.setCancelable(true);
			mCopyProgDialog.setProgress(0);
			mCopyProgDialog.show();
			mCopyProgDialog
					.setOnDismissListener(new DialogInterface.OnDismissListener() {
						public void onDismiss(DialogInterface dialog) {
							if (ContactsMarkListActivity.this == null || ContactsMarkListActivity.this.isFinishing()) return;
                            Log.d(TAG, "*********before initCompConfirmDialog() after mCopyProgDialog dismiss");
							initCompConfirmDialog();
						}
					});
			mCopyThread = new CopyThread();
			mCopyThread.start();
			//finish();
		}
	};
    @Override
	protected void onRestoreInstanceState(Bundle state) {
		// TODO Auto-generated method stub
 		Log.d(TAG, "onRestoreInstanceState");
        mCopySrc = state.getInt("mCopySrc",0);
        mCopyDst = state.getInt("mCopyDst",0);
        srcSimId = state.getLong("srcSimId",0);
        dstSimId = state.getLong("dstSimId",0);
        isSrcSimUSIM = state.getBoolean("isSrcSimUSIM",false);
        isDstSimUSIM = state.getBoolean("isDstSimUSIM",false);
        mBeingCopied = state.getBoolean("mBeingCopied",false);
        //mSelectedPositions = state.getBooleanArray("mSelectedPositions",null);
        mSelectedCount = state.getInt("mSelectedCount",0);
        mNeedReview = state.getBoolean("mNeedReview",false);
        //mJustCreated = state.getBoolean("mJustCreated",false);
        mImportVCard = state.getBoolean("mImportVCard",false);
        mExportVCard = state.getBoolean("mExportVCard",false);
		super.onRestoreInstanceState(state);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onSaveInstanceState");

		outState.putInt("mCopySrc", mCopySrc);
        outState.putInt("mCopyDst", mCopyDst);
        outState.putLong("srcSimId", srcSimId);
        outState.putLong("dstSimId", dstSimId);
        outState.putBoolean("isSrcSimUSIM",isSrcSimUSIM);
        outState.putBoolean("isDstSimUSIM",isDstSimUSIM);  
        outState.putBoolean("mBeingCopied",mBeingCopied);  
        outState.putBooleanArray("mSelectedPositions",mSelectedPositions);
        outState.putInt("mSelectedCount",mSelectedCount);
        outState.putBoolean("mNeedReview",mNeedReview);
        outState.putBoolean("mJustCreated",mJustCreated);
        outState.putBoolean("mImportVCard",mImportVCard);
        outState.putBoolean("mExportVCard",mExportVCard);       
     
		super.onSaveInstanceState(outState);
	}
    private CellConnMgr mCellMgr = new CellConnMgr(serviceComplete);
}
