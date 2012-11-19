package com.android.contacts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.provider.ContactsContract.RawContacts;
import android.provider.Telephony.SIMInfo;
import com.mediatek.featureoption.FeatureOption;
import android.os.ServiceManager;
import com.android.internal.telephony.ITelephony;

import com.android.contacts.model.Sources;
import com.android.contacts.util.AccountSelectionUtil;
import com.android.contacts.util.Constants.SimInfo;
import com.mediatek.CellConnService.CellConnMgr;
import java.io.File;
import android.os.StatFs;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
public class ImportExportBridgeActivity extends ListActivity implements
		View.OnClickListener, DialogInterface.OnClickListener {

	private static final String TAG_LOG = "ImportExportBridgeActivity";
	private static final String TAG_BRIDGE_TYPE = "bridge_type";
	private static final String TAG_FINALVIEW = "isFinalView";
	private static final String TAG_SOURCE_TYPE = "source_type";
	private static final String TAG_SOURCE_SLOTID = "source_slotid";
	private static final String TAG_TARGET_TYPE = "target_type";
	private static final String TAG_TARGET_SLOTID = "target_slotid";

	private static final int ITEM_TYPE_PHONE = 1;
	private static final int ITEM_TYPE_SIM = 2;
	private static final int ITEM_TYPE_SD = 3;
	private static final int ITEM_TYPE_INVALID = -10;

	private static final int PHONE_STATE_NORMAL = 0;
	private static final int PHONE_STATE_PINLOCKED = 1;
	private static final int PHONE_STATE_PUKLOCKED = 2;
	private static final int PHONE_STATE_RADIOOFF = 3;

	private static final int INVALIDPARAM_SLOTID = -1;
	private static final int INVALIDPARAM_SELECTED = -1;
	private static final int INVALIDPARAM_BRIDGETYPE = 0;
	
        public  static final String ACTION_SHOULD_FINISHED = "com.android.contacts.ImportExportBridge.ACTION_SHOULD_FINISHED";
	private static final int REQUEST_TYPE = 304;
	private FinishReceiver  mFinishReceiver = new FinishReceiver();   

    public class FinishReceiver extends BroadcastReceiver {
		@Override
		 public  void onReceive(Context context, Intent intent)
			{
				final String action = intent.getAction();
				Log.i("FinishReceiver", "action is " + action);
				ImportExportBridgeActivity.this.finish();
				mShouldFinish = false;		
				
            }
    }

	private class ViewData {
		private String mViewString;
		private int mViewType;
		private SIMInfo mSimInfo;

		public ViewData() {
			mViewString = "";
			mViewType = 0;
		}

		public ViewData(String viewString, int viewType, SIMInfo simInfo) {
			this.mViewString = viewString;
			this.mViewType = viewType;
			if (null != simInfo) {
				this.mSimInfo = simInfo;
			}
		}

		public void setViewString(String viewString) {
			this.mViewString = viewString;
		}

		public void setViewType(int viewType) {
			this.mViewType = viewType;
		}

		public void setSIMInfo(SIMInfo simInfo) {
			this.mSimInfo = simInfo;
		}

		public String getViewString() {
			return this.mViewString;
		}

		public int getViewType() {
			return this.mViewType;
		}

		public SIMInfo getSIMInfo() {
			return this.mSimInfo;
		}

	}

	private ArrayList<ViewData> mViewDataList = null;

	private int mSelected = INVALIDPARAM_SELECTED;

	private Button mBackButton;
	private Button mActionButton;
	private LinearLayout mBottomView;
	private LinearLayout mTopView;
	private TextView mTipsText;

	private int mViewType = INVALIDPARAM_BRIDGETYPE; // 0 - all; 1 - SIM only
	private boolean mIsFinalView = false; // false - source select view; true -
	// target select view

	private Intent mNextIntent;

	private String[] mViewText = null;

	private int mSourceType = ITEM_TYPE_INVALID;
	private int mSourceSlotId = INVALIDPARAM_SLOTID;

	private int mTargetType = ITEM_TYPE_INVALID;
	private int mTargetSlotId = INVALIDPARAM_SLOTID;

	private Intent mCopyIntent;
	public static boolean mShouldFinish = false;

	private boolean mImportSimOnly = false;
	
	private boolean mUnlockBoth = false;
	
	private int mSaveSelected = INVALIDPARAM_SELECTED;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.d(TAG_LOG, "onCreate");
                this.registerReceiver(this.mFinishReceiver,new IntentFilter(ImportExportBridgeActivity.ACTION_SHOULD_FINISHED));
		mCellMgr.register(this);
		onNewIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Log.d(TAG_LOG, "onNewIntent hashcode = " + intent.hashCode());

		mViewType = intent
				.getIntExtra(TAG_BRIDGE_TYPE, INVALIDPARAM_BRIDGETYPE);
		if (1 == mViewType) {
			mImportSimOnly = true;
		} else {
			this.mImportSimOnly = false;
		}
		mIsFinalView = intent.getBooleanExtra(TAG_FINALVIEW, false);

		Log.d(TAG_LOG, "mIsFinalView = " + mIsFinalView);

		mNextIntent = intent;

		// layout
		setContentView(R.layout.import_export_bridge_layout);

		// top view
		mTopView = (LinearLayout) findViewById(R.id.topview_layout);
		if (null == mTopView)
			return;

		// LayoutParams param = mTopView.getLayoutParams();
		// param.height = LayoutParams.WRAP_CONTENT;
		// mTopView.setLayoutParams(param);

		// mTopView.setVisibility(View.INVISIBLE);

		mTipsText = (TextView) findViewById(R.id.tips);
		if (null == mTipsText)
			return;
		
		if (mIsFinalView) {
			mTipsText.setText(R.string.tips_target);
		} else {
			mTipsText.setText(R.string.tips_source);
		}

		// bottom view
		mBottomView = (LinearLayout) findViewById(R.id.buttonbar_layout);
		if (null == mBottomView)
			return;

		// mBottomView.setVisibility(View.GONE);

		mBackButton = (Button) findViewById(R.id.btn_back);
		if (null == mBackButton)
			return;

		if (mIsFinalView) {
			mBackButton.setVisibility(View.VISIBLE);
			mBackButton.setOnClickListener(this);
		} else {
			mBackButton.setVisibility(View.INVISIBLE);
		}

		mActionButton = (Button) findViewById(R.id.btn_action);
		if (null == mActionButton)
			return;

		mActionButton.setOnClickListener(this);

		mSourceType = intent.getIntExtra(TAG_SOURCE_TYPE, ITEM_TYPE_INVALID);
		mSourceSlotId = intent.getIntExtra(TAG_SOURCE_SLOTID,
				INVALIDPARAM_SLOTID);
		
		mTargetType = intent.getIntExtra(TAG_TARGET_TYPE, ITEM_TYPE_INVALID);
		mTargetSlotId = intent.getIntExtra(TAG_TARGET_SLOTID,
				INVALIDPARAM_SLOTID);
		
		Log.d(TAG_LOG, "onNewIntent mSourceType = " + mSourceType + " and mSourceSlotId = " + mSourceSlotId);
    	Log.d(TAG_LOG, "onNewIntent mTargetType = " + mTargetType + " and mTargetSlotId = " + mTargetSlotId);
		
		mSaveSelected = INVALIDPARAM_SELECTED;
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		// TODO Auto-generated method stub
		Log.d(TAG_LOG, "onRestoreInstanceState");
		mSelected = state.getInt("last_selected");
		Log.d(TAG_LOG, "onRestoreInstanceState last_selected = " + mSelected);
		super.onRestoreInstanceState(state);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		Log.d(TAG_LOG, "onSaveInstanceState");
		outState.putInt("last_selected", mSelected);
       this.saveDstIntentExtra(mSelected);
		super.onSaveInstanceState(outState);
	}

/*	@Override
	protected void onPause() {
		Log.d(TAG_LOG, "onPause");
		if (!mIsFinalView) {
			putSrcIntentExtra(this.mSelected, TAG_SOURCE_TYPE);
		} else {
			putDstIntentExtra(this.mSelected, TAG_TARGET_TYPE);
		}
		super.onPause();
	}*/

	@Override
	public void setIntent(Intent newIntent) {
		// TODO Auto-generated method stub
		Log.d(TAG_LOG, "setIntent");
		super.setIntent(newIntent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG_LOG, "onResume");
		if (!buildViewDataList(mSourceType, mSourceSlotId)) {
			Log.e(TAG_LOG, "onResume buildViewDataList failed!");
			finish();
			return;
		}

		if (!buildViewText()) {
			Log.e(TAG_LOG, "onResume buildViewText failed!");
			finish();
			return;
		}

		if (!setupListView()) {
			Log.e(TAG_LOG, "onResume setupListView failed!");
			finish();
			return;
		}

		if (mSaveSelected != INVALIDPARAM_SELECTED) {
			Log.d(TAG_LOG, "onResume mSaveSelected restore.mSaveSelected="+mSaveSelected);
			this.getListView().setItemChecked(mSaveSelected, true);
			this.mSelected = mSaveSelected;
		} else if (this.mIsFinalView) {
			setCheckState(mTargetType, mTargetSlotId);
		} else {
			setCheckState(mSourceType, mSourceSlotId);
		}

		if (mShouldFinish) {
			mShouldFinish = false;
			finish();
		}
		
		if (mViewDataList.size() == 1) {
			this.getListView().setItemChecked(0, true);
			this.mSelected = 0;
		}
		
		mUnlockBoth = false;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mSaveSelected = this.mSelected;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mCellMgr.unregister();
                this.unregisterReceiver( this.mFinishReceiver);
		super.onDestroy();
	}

	private boolean setupListView() {
		if (null == mViewText) {
			Log.e(TAG_LOG, "setupListView view text is null");
			return false;
		}

		final ListView list = getListView();

		list.setFocusable(true);
		list.setItemsCanFocus(false);
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		//setListAdapter(new ArrayAdapter<String>(this,
		//		android.R.layout.simple_list_item_single_choice, mViewText));
		setListAdapter(new ArrayAdapter<String>(this,
				R.layout.simple_list_item_single_choice, mViewText));
        
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getListView()
				.getLayoutParams();
		params.height = 0;
		params.addRule(RelativeLayout.ABOVE, mBottomView.getId());
		params.addRule(RelativeLayout.BELOW, mTopView.getId());
		getListView().setLayoutParams(params);

		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (INVALIDPARAM_SELECTED != this.mSelected) {
			View last = this.getListView().getChildAt(mSelected);
			((CheckedTextView) last).setChecked(false);
		}
		this.mSelected = position;
		((CheckedTextView) v).setChecked(true);
	}

	/** {@inheritDoc} */
	public void onClick(View v) {
		// TODO Handle the action for each button

		if (v.getId() == R.id.btn_back) {
			mNextIntent.putExtra(TAG_FINALVIEW, false);
			if (this.mIsFinalView) {
				putDstIntentExtra(this.mSelected);
			}
			this.startActivity(mNextIntent);
			return;
		}

		if (v.getId() == R.id.btn_action) {
			if (INVALIDPARAM_SELECTED == this.mSelected) {
				Toast
						.makeText(this, R.string.no_select_alert,
								Toast.LENGTH_SHORT).show();
				return;
			}

			if (!mIsFinalView) {
				putSrcIntentExtra(this.mSelected);
				mNextIntent.putExtra(TAG_FINALVIEW, true);
				this.startActivity(mNextIntent);
			} else {
				mTargetType = this.mViewDataList.get(this.mSelected)
						.getViewType();
				mTargetSlotId = INVALIDPARAM_SLOTID;
				if (ITEM_TYPE_SIM == mTargetType) {
					mTargetSlotId = this.mViewDataList.get(this.mSelected)
							.getSIMInfo().mSlot;
				}

				Log.d(TAG_LOG, "mTargetType = " + mTargetType
						+ " mTargetSlotId = " + mTargetSlotId);
				
				// **********************************************************
				Context myContext = this;
				final Sources sources = Sources.getInstance(myContext);
				final List<Account> accountList = sources.getAccounts(true);
				final int size = accountList.size();
				Account firstAccount = null;
				if (size != 0) {
					firstAccount = accountList.get(0);
				}
				mCopyIntent = new Intent(myContext,
						ContactsMarkListActivity.class);
                mCopyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				switch (mSourceType) {
				case ITEM_TYPE_PHONE:
					if (ITEM_TYPE_SIM == mTargetType) {
						// Phone -> SIM
						int nRet = mCellMgr.handleCellConn(this.mTargetSlotId, REQUEST_TYPE);
						Log.d(TAG_LOG, "Phone -> SIM handleCellConn result value = " + CellConnMgr.resultToString(nRet));
						break;
					} else if (ITEM_TYPE_SD == mTargetType) {
						// Phone -> SD
						Log.d(TAG_LOG, "Phone -> SD");
						if (!checkSDCardAvaliable()) {
							Log.d(TAG_LOG, "Phone -> SD no SD detected");
							new AlertDialog.Builder(this).setMessage(
									R.string.no_sdcard_message).setTitle(
									R.string.no_sdcard_title).setIcon(
									android.R.drawable.ic_dialog_alert)
									.setPositiveButton(android.R.string.ok,
											this).show();
						} else if(isSDCardFull()){//SD card is full
						    new AlertDialog.Builder(this).setMessage(
									R.string.storage_full).setTitle(
									R.string.storage_full).setIcon(
									android.R.drawable.ic_dialog_alert)
									.setPositiveButton(android.R.string.ok,
											this).show();

                        }else {
							mCopyIntent.putExtra("src",
									RawContacts.INDICATE_PHONE);
							mCopyIntent.putExtra("dst", -2);
							startActivityForResult(mCopyIntent, 0);
						}
					}
					break;

				case ITEM_TYPE_SIM:
					if (ITEM_TYPE_PHONE == mTargetType) {
						// SIM -> Phone
						mCellMgr.handleCellConn(this.mSourceSlotId, REQUEST_TYPE);
					} else if (ITEM_TYPE_SIM == mTargetType) {
						// SIM -> SIM
						mUnlockBoth = true;
						mCellMgr.handleCellConn(this.mSourceSlotId, REQUEST_TYPE);
					} else if (ITEM_TYPE_SD == mTargetType) {
						// SIM -> SD (bad behavior)
					}
					break;

				case ITEM_TYPE_SD:
					if (ITEM_TYPE_PHONE == mTargetType) {
						// SD -> Phone
						if (!checkSDCardAvaliable()) {
							Log.d(TAG_LOG, "Phone -> SD no SD detected");
							new AlertDialog.Builder(this).setMessage(
									R.string.no_sdcard_message).setTitle(
									R.string.no_sdcard_title).setIcon(
									android.R.drawable.ic_dialog_alert)
									.setPositiveButton(android.R.string.ok,
											this).show();
						} else {
							mCopyIntent.putExtra("src", -2);
							mCopyIntent.putExtra("dst",
									RawContacts.INDICATE_PHONE);
							if (size > 1) {
								showDialog(R.string.import_from_sdcard);
							} else if (size == 1) {
								mCopyIntent.putExtra("account_name",
										firstAccount.name);
								mCopyIntent.putExtra("account_type",
										firstAccount.type);
								startActivityForResult(mCopyIntent, 0);
							} else {
								startActivityForResult(mCopyIntent, 0);
							}
						}
					} else if (ITEM_TYPE_SIM == mTargetType) {
						// SD -> SIM (bad behavior)
					}
					break;

				default:
					break;
				}

				// ************************************************************
			}
		}
	}

	private boolean buildViewText() {
		this.mViewText = new String[mViewDataList.size()];
		if (null == mViewText) {
			Log.e(TAG_LOG, "buildViewText new view text array list failed");
			return false;
		}

		int i = 0;
		for (ViewData viewData:mViewDataList) {
			mViewText[i++] = viewData.getViewString();
		}

		return true;
	}

	private boolean buildViewDataList(int sourceTextType, int sourceSlotId) {
		if (null != mViewDataList) {
			mViewDataList.removeAll(mViewDataList);
		} else {
			mViewDataList = new ArrayList<ViewData>();
			if (null == mViewDataList) {
				Log.e(TAG_LOG, "buildViewDataList new view data list failed");
				return false;
			}
		}

		if (this.mImportSimOnly) {
			if (this.mIsFinalView) {
				// build view data for phone
				ViewData phoneData = new ViewData(getResources().getString(
						R.string.imexport_bridge_phone), ITEM_TYPE_PHONE, null);
				if (null == phoneData) {
					Log.e(TAG_LOG, "buildViewDataList new phoneData failed");
					return false;
				}
				mViewDataList.add(phoneData);
				return true;
			} else {
				// build view data for SIM list
				List<SIMInfo> simInfoList = SIMInfo.getInsertedSIMList(this);
				if (null == simInfoList) {
					Log
							.e(TAG_LOG,
									"buildViewDataList getInsertedSIMInfo list is null");
					return false;
				}

				for (SIMInfo simInfo:simInfoList) {
					if (this.mIsFinalView && (ITEM_TYPE_SIM == sourceTextType)
							&& (sourceSlotId == simInfo.mSlot)) {
						continue;
					}
					
					String displayName = simInfo.mDisplayName;
					if (null == displayName){
						displayName = new String();
					} else if (TextUtils.isEmpty(displayName)) {
						displayName = "";
					}

					Log.e(TAG_LOG, "buildViewDataList siminfo display name ="
							+ displayName + " and slot = " + simInfo.mSlot);

					ViewData simData = new ViewData(displayName, ITEM_TYPE_SIM, simInfo);
					if (null == simData) {
						Log.e(TAG_LOG, "buildViewDataList new simData failed");
						return false;
					}
					mViewDataList.add(simData);
				}

				return true;
			}
		}

		if (!(this.mIsFinalView && ITEM_TYPE_PHONE == sourceTextType)) {
			// build view data for phone
			ViewData phoneData = new ViewData(getResources().getString(
					R.string.imexport_bridge_phone), ITEM_TYPE_PHONE, null);
			if (null == phoneData) {
				Log.e(TAG_LOG, "buildViewDataList new phoneData failed");
				return false;
			}
			mViewDataList.add(phoneData);
		}

		// forbid SD -> SIM
		if (mIsFinalView && (ITEM_TYPE_SD == sourceTextType)) {
			return true;
		}
		// build view data for SIM list
		List<SIMInfo> simInfoList = SIMInfo.getInsertedSIMList(this);
		if (null == simInfoList) {
			Log.e(TAG_LOG, "buildViewDataList getInsertedSIMInfo list is null");
			return false;
		}
		
		for (SIMInfo simInfo:simInfoList) {
			if (this.mIsFinalView && (ITEM_TYPE_SIM == sourceTextType)
					&& (sourceSlotId == simInfo.mSlot)) {
				continue;
			}

			String displayName = simInfo.mDisplayName;
			if (null == displayName){
				displayName = new String();
			} else if (TextUtils.isEmpty(displayName)) {
				displayName = "";
			}

			Log.e(TAG_LOG, "buildViewDataList siminfo display name ="
					+ displayName + " and slot = " + simInfo.mSlot);
			
			ViewData simData = new ViewData(displayName, ITEM_TYPE_SIM, simInfo);
			if (null == simData) {
				Log.e(TAG_LOG, "buildViewDataList new simData failed");
				return false;
			}
			mViewDataList.add(simData);
		}

		// forbid SIM -> SD
		if (mIsFinalView && (ITEM_TYPE_SIM == sourceTextType)) {
			return true;
		}

		if (!(this.mIsFinalView && ITEM_TYPE_SD == sourceTextType)) {
			// build view data for SD
			ViewData sdData = new ViewData(getResources().getString(
					R.string.imexport_bridge_sd_card), ITEM_TYPE_SD, null);
			if (null == sdData) {
				Log.e(TAG_LOG, "buildViewDataList new sdData failed");
				return false;
			}

			mViewDataList.add(sdData);
		}

		return true;
	}

	
	private void putSrcIntentExtra(int selected) {
		if (INVALIDPARAM_SELECTED == selected
				|| selected >= mViewDataList.size()) {
			Log.e(TAG_LOG, "putSrcIntentExtra invalid param");
			return;
		}

		mNextIntent.putExtra(TAG_SOURCE_TYPE, this.mViewDataList.get(selected)
				.getViewType());
		Log.d(TAG_LOG, "putSrcIntentExtra " + TAG_SOURCE_TYPE + " = " + this.mViewDataList.get(selected)
				.getViewType());

		if (ITEM_TYPE_SIM == this.mViewDataList.get(selected).getViewType()) {
			mNextIntent.putExtra(TAG_SOURCE_SLOTID, mViewDataList.get(selected)
					.getSIMInfo().mSlot);
			Log.d(TAG_LOG, "putSrcIntentExtra " + TAG_SOURCE_SLOTID + " = " + mViewDataList.get(selected)
					.getSIMInfo().mSlot);
		}
	}
	
	private void putDstIntentExtra(int selected) {
		if (INVALIDPARAM_SELECTED == selected
				|| selected >= mViewDataList.size()) {
			Log.e(TAG_LOG, "putDstIntentExtra invalid param");
			return;
		}

		mNextIntent.putExtra(TAG_TARGET_TYPE, this.mViewDataList.get(selected)
				.getViewType());
		Log.d(TAG_LOG, "putDstIntentExtra " + TAG_TARGET_TYPE + " = " + this.mViewDataList.get(selected)
				.getViewType());

		if (ITEM_TYPE_SIM == this.mViewDataList.get(selected).getViewType()) {
			mNextIntent.putExtra(TAG_TARGET_SLOTID, mViewDataList.get(selected)
					.getSIMInfo().mSlot);
			Log.d(TAG_LOG, "putDstIntentExtra " + TAG_TARGET_SLOTID + " = " + mViewDataList.get(selected)
					.getSIMInfo().mSlot);
		}
	}

	private void setCheckState(int type, int slotId) {

		if (null == this.mViewDataList) {
			Log.e(TAG_LOG, "setCheckState mViewDataList is null");
			this.mSelected = INVALIDPARAM_SELECTED;
			return;
		}

		if (ITEM_TYPE_INVALID == type) {
			this.mSelected = INVALIDPARAM_SELECTED;
			return;
		}

		this.mSelected = INVALIDPARAM_SELECTED;
		int i = 0;
		for (ViewData viewData:mViewDataList) {
			if (type == viewData.getViewType()) {
				if ((ITEM_TYPE_SIM != type) || ((ITEM_TYPE_SIM == type) 
					&& (slotId == viewData.getSIMInfo().mSlot))) {
					this.mSelected = i;
					break;
				}
			}
			++i;
		}

		Log.d(TAG_LOG, "setCheckState selected item is " + this.mSelected);
		if (INVALIDPARAM_SELECTED != this.mSelected) {
			this.getListView().setItemChecked(this.mSelected, true);
		}
	}

	private void showAlterDlg(String msg, int idTitle, int idLBtn, int idRBtn) {
		new AlertDialog.Builder(this).setMessage(msg).setTitle(idTitle)
				.setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
						idLBtn, this).setNegativeButton(idRBtn, this).show();
	}

	// This is a method implemented for DialogInterface.OnClickListener.
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			/*Toast.makeText(this, "tap the positive button", Toast.LENGTH_SHORT)
					.show();*/
			return;
		} else if (which == DialogInterface.BUTTON_NEGATIVE) {
			/*Toast.makeText(this, "tap the negative button", Toast.LENGTH_SHORT)
					.show();*/
			return;
		}
	}

	protected Dialog onCreateDialog(int id, Bundle bundle) {
		switch (id) {
		case R.string.import_from_sim:
		case R.string.import_from_sim1:
		case R.string.import_from_sim2:
		case R.string.import_from_sdcard: {
			// mtk80909 enhancement
			return AccountSelectionUtil.getSelectAccountDialog(this, id,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Context myContext = ImportExportBridgeActivity.this;
							final Sources sources = Sources
									.getInstance(myContext);
							final List<Account> accountList = sources
									.getAccounts(true);

							Intent copyIntent = ImportExportBridgeActivity.this.mCopyIntent;
							mCopyIntent.putExtra("account_name", accountList
									.get(which).name);
							mCopyIntent.putExtra("account_type", accountList
									.get(which).type);
							ImportExportBridgeActivity.this
									.startActivityForResult(mCopyIntent, 0);
							dialog.dismiss();
						}
					});

			// return AccountSelectionUtil.getSelectAccountDialog(this, id);
		}
		default:
			return null;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG_LOG, "resultCode = " + resultCode);
		if (resultCode == RESULT_OK) {
			mShouldFinish = true;
		} else {
			mShouldFinish = false;
		}
	}

	private boolean checkSDCardAvaliable() {
		return (Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED));
	}

	private boolean isSDCardFull() {
        String state = Environment.getExternalStorageState(); 
               if(Environment.MEDIA_MOUNTED.equals(state)) { 
                   File sdcardDir = Environment.getExternalStorageDirectory(); 
                   StatFs sf = new StatFs(sdcardDir.getPath());
                   long availCount = sf.getAvailableBlocks(); 
                   if(availCount>0){
                       return false;
                   } else {
                       return true;
                   }
               } 

		return true;
	}
	private Runnable serviceComplete = new Runnable() {
		public void run() {
			Log.d(TAG_LOG, "serviceComplete run");
			
			int nRet = mCellMgr.getResult();
			Log.d(TAG_LOG, "serviceComplete result = " + CellConnMgr.resultToString(nRet));
			if (mCellMgr.RESULT_ABORT == nRet) {
				return;
			}

			// **********************************************************
			Context myContext = getBaseContext();
			final Sources sources = Sources.getInstance(myContext);
			final List<Account> accountList = sources.getAccounts(true);
			final int size = accountList.size();
			Account firstAccount = null;
			if (size != 0) {
				firstAccount = accountList.get(0);
			}
			mCopyIntent = new Intent(myContext, ContactsMarkListActivity.class);
	        mCopyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			switch (mSourceType) {
			case ITEM_TYPE_PHONE:
				if (ITEM_TYPE_SIM == mTargetType) {
					// Phone -> SIM
					Log.d(TAG_LOG, "Phone -> SIM");
					mCopyIntent.putExtra("src", RawContacts.INDICATE_PHONE);
					mCopyIntent.putExtra("dst", mTargetSlotId);
					startActivityForResult(mCopyIntent, 0);
					break;
				}
				break;

			case ITEM_TYPE_SIM:
				if (ITEM_TYPE_PHONE == mTargetType) {
					// SIM -> Phone
					Log.d(TAG_LOG, "SIM -> Phone");
					mCopyIntent.putExtra("src", mSourceSlotId);
					mCopyIntent.putExtra("dst", RawContacts.INDICATE_PHONE);
					if (size > 1) {
						int resId = -1;
						if (mSourceSlotId == 0) {
							resId = R.string.import_from_sim;
						} else if (mSourceSlotId == 1) {
							resId = R.string.import_from_sim1;
						} else if (mSourceSlotId == 2) {
							resId = R.string.import_from_sim2;
						}
						showDialog(resId);
					} else if (size == 1) {
						mCopyIntent.putExtra("account_name", firstAccount.name);
						mCopyIntent.putExtra("account_type", firstAccount.type);
						startActivityForResult(mCopyIntent, 0);
					} else {
						startActivityForResult(mCopyIntent, 0);
					}
				} else if (ITEM_TYPE_SIM == mTargetType) {
					// SIM -> SIM
					Log.d(TAG_LOG, "SIM -> SIM");
					if (!mUnlockBoth) {
						mCopyIntent.putExtra("src", mSourceSlotId);
						mCopyIntent.putExtra("dst", mTargetSlotId);
						startActivityForResult(mCopyIntent, 0);
					} else {
						mCellMgr.handleCellConn(mTargetSlotId, REQUEST_TYPE);
					}
					mUnlockBoth = false;
				}
				break;

			default:
				break;
			}
			
			//finish();
		}
	};
    @Override
    public void onBackPressed(){
        Log.d(TAG_LOG,"onBackPressed");
        if (this.mIsFinalView) {

            mNextIntent.putExtra(TAG_FINALVIEW, false);
			
				putDstIntentExtra(this.mSelected);
			
			this.startActivity(mNextIntent);    
            } else {
                super.onBackPressed();
            }
    }  

	private void saveDstIntentExtra(int selected) {
		if (INVALIDPARAM_SELECTED == selected
				|| selected >= mViewDataList.size()) {
			Log.e(TAG_LOG, "saveDstIntentExtra invalid param");
			return;
		}
        Intent intent = this.getIntent();
		intent.putExtra(TAG_TARGET_TYPE, this.mViewDataList.get(selected)
				.getViewType());
		Log.d(TAG_LOG, "saveDstIntentExtra " + TAG_TARGET_TYPE + " = " + this.mViewDataList.get(selected)
				.getViewType());

		if (ITEM_TYPE_SIM == this.mViewDataList.get(selected).getViewType()) {
			intent.putExtra(TAG_TARGET_SLOTID, mViewDataList.get(selected)
					.getSIMInfo().mSlot);
			Log.d(TAG_LOG, "saveDstIntentExtra " + TAG_TARGET_SLOTID + " = " + mViewDataList.get(selected)
					.getSIMInfo().mSlot);
		}
	}

	private CellConnMgr mCellMgr = new CellConnMgr(serviceComplete);
}
