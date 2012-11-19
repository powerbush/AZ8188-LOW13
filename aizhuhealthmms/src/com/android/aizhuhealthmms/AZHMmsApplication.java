package com.android.aizhuhealthmms;

import android.app.Application;

public class AZHMmsApplication extends Application {

	private ContcatAdapter adapter;
	public static final String ACTION_RECV_NEW_MSG = "recvNewMsg";
	public static final String ACTION_RECV_NEW_MSG_FOR_DIALOG="recvNewMsgForDialog";

	public ContcatAdapter getAdapter() {
		return adapter;
	}
}
