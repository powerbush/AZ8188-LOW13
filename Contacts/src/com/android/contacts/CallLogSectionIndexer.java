package com.android.contacts;

import java.util.Arrays;

import android.util.Log;
import android.widget.SectionIndexer;

public class CallLogSectionIndexer implements SectionIndexer {

	private String TAG = "CallLog_SectIndex";
	private final String[] mSections;
	private final int[] mPositions;
	private final int mCount;

	public CallLogSectionIndexer(String[] sections, int[] counts) {
		if ((sections == null) || (counts == null)) {
			throw new NullPointerException(
					"CallLog_SectIndex: the section or count is null.");
		}
		if (sections.length != counts.length) {
			throw new IllegalArgumentException(
					"CallLog_SectIndex: the section and counts length is not matched.");
		}
		this.mSections = sections;
		int iCountLen = counts.length;
		this.mPositions = new int[iCountLen];
		int position = 0;
		for (int i = 0; i < iCountLen; i++) {
			if (mSections[i] == null) {
				mSections[i] = "#";
			} else {
				mSections[i] = mSections[i].trim();
			}
			mPositions[i] = position;
			position += counts[i];
		}
		mCount = position;
		
//		Log.v(TAG, "CLS: mSections Count:" + mSections.length);
//		for (int i=0; i<mSections.length; i++) {
//			Log.v(TAG, "CLS: mSections[" + i + "]:" + mSections[i]);
//		}
//		Log.v(TAG, "CLS: mPositions Count:" + mPositions.length);
//		for (int i=0; i<mPositions.length; i++) {
//			Log.v(TAG, "CLS: mPositions[" + i + "]:" + mPositions[i]);
//		}
//		Log.v(TAG, "CLS: mCount:" + mCount);
	}

	public Object[] getSections() {
		return mSections;
	}

	public int getPositionForSection(int section) {
		if (section < 0 || section >= mSections.length) {
			return -1;
		}

		return mPositions[section];
	}

	public int getSectionForPosition(int position) {
		if (position < 0 || position >= mCount) {
			return -1;
		}

		int index = Arrays.binarySearch(mPositions, position);
		return index >= 0 ? index : -index - 2;
	}
}