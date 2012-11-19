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

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts;

import com.android.contacts.ui.widget.DontPressWithParentImageView;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import com.mediatek.featureoption.FeatureOption;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * A custom view for an item in the contact list.
 */
public class ContactListItemView extends ViewGroup {

    private static final String TAG = "ContactListItemView";

    private static final int QUICK_CONTACT_BADGE_STYLE =
            com.android.internal.R.attr.quickContactBadgeStyleWindowMedium;

    private final Context mContext;

    private final int mPreferredHeight;
    private final int mVerticalDividerMargin;
    private final int mPaddingTop;
    private final int mPaddingRight;
    private final int mPaddingBottom;
    private final int mPaddingLeft;
    private final int mGapBetweenImageAndText;
    private final int mGapBetweenLabelAndData;
    private final int mCallButtonPadding;
    private final int mPresenceIconMargin;
    private final int mHeaderTextWidth;

    private boolean mHorizontalDividerVisible;
    private Drawable mHorizontalDividerDrawable;
    private int mHorizontalDividerHeight;

    private boolean mVerticalDividerVisible;
    private Drawable mVerticalDividerDrawable;
    private int mVerticalDividerWidth;

    private boolean mHeaderVisible;
    private Drawable mHeaderBackgroundDrawable;
    private int mHeaderBackgroundHeight;
    private TextView mHeaderTextView;

    private QuickContactBadge mQuickContact;
    private ImageView mPhotoView;
    private TextView mNameTextView;
    private DontPressWithParentImageView mCallButton;
    private TextView mLabelView;
    private TextView mDataView;
    private TextView mSnippetView;
    private ImageView mPresenceIcon;
    private ImageView mSnsLogo; // add by Ivan 2010-08-23 20:21
    private TextView mSnsStatus; // add by Ivan 2010-08-23 20:21
    private CheckBox mSelectBox; // add by Han Jiang 2020-9-17

    private int mPhotoViewWidth;
    private int mPhotoViewHeight;
    private int mLine1Height;
    private int mLine2Height;
    private int mLine3Height;
    private int mLine4Height; // add by Ivan 2010-08-23 20:21
    private int mLine4LogoHeight;

    private static final int WIDTH_IN_DIP_TO_REDUCE = 20;

    private OnClickListener mCallButtonClickListener;

    public ContactListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        // Obtain preferred item height from the current theme
        TypedArray a = context.obtainStyledAttributes(null, com.android.internal.R.styleable.Theme);
        mPreferredHeight =
                a.getDimensionPixelSize(android.R.styleable.Theme_listPreferredItemHeight, 0);
        a.recycle();

        Resources resources = context.getResources();
        mVerticalDividerMargin =
                resources.getDimensionPixelOffset(R.dimen.list_item_vertical_divider_margin);
        mPaddingTop =
                resources.getDimensionPixelOffset(R.dimen.list_item_padding_top);
        mPaddingBottom =
                resources.getDimensionPixelOffset(R.dimen.list_item_padding_bottom);
//        mPaddingLeft =
//                resources.getDimensionPixelOffset(R.dimen.list_item_padding_left);
        
        mPaddingLeft =
            resources.getDimensionPixelOffset(R.dimen.list_item_padding_left_for_enhancement);
        mPaddingRight =
                resources.getDimensionPixelOffset(R.dimen.list_item_padding_right);
        mGapBetweenImageAndText =
                resources.getDimensionPixelOffset(R.dimen.list_item_gap_between_image_and_text);
        mGapBetweenLabelAndData =
                resources.getDimensionPixelOffset(R.dimen.list_item_gap_between_label_and_data);
        mCallButtonPadding =
                resources.getDimensionPixelOffset(R.dimen.list_item_call_button_padding);
        mPresenceIconMargin =
                resources.getDimensionPixelOffset(R.dimen.list_item_presence_icon_margin);
        mHeaderTextWidth =
            resources.getDimensionPixelOffset(R.dimen.list_item_header_text_width_for_enhancement);
    }

    /**
     * Installs a call button listener.
     */
    public void setOnCallButtonClickListener(OnClickListener callButtonClickListener) {
        mCallButtonClickListener = callButtonClickListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We will match parent's width and wrap content vertically, but make sure
        // height is no less than listPreferredItemHeight.
        int width = resolveSize(0, widthMeasureSpec);
        int height = 0;

        mLine1Height = 0;
        mLine2Height = 0;
        mLine3Height = 0;
        mLine4Height = 0; // add by Ivan 2010-08-23 20:21
        mLine4LogoHeight = 0;

        // Obtain the natural dimensions of the name text (we only care about height)
        mNameTextView.measure(0, 0);

        mLine1Height = mNameTextView.getMeasuredHeight();

        if (isVisible(mLabelView)) {
            mLabelView.measure(0, 0);
            mLine2Height = mLabelView.getMeasuredHeight();
        }

        if (isVisible(mDataView)) {
            mDataView.measure(0, 0);
            mLine2Height = Math.max(mLine2Height, mDataView.getMeasuredHeight());
        }
        if(true == FeatureOption.MTK_SNS_SUPPORT)
        {
        	// add by Ivan 2010-08-23 20:20
        	if (isVisible(mSnsLogo)) {
        	mSnsLogo.measure(0, 0);
            mLine4LogoHeight = mSnsLogo.getMeasuredHeight();
        	}

        	if (isVisible(mSnsStatus)) {
        	mSnsStatus.measure(0, 0);
            mLine4Height = Math.max(mLine4LogoHeight, mSnsStatus.getMeasuredHeight());
        	}
        	// end add
        }
        if (isVisible(mSnippetView)) {
            mSnippetView.measure(0, 0);
            mLine3Height = mSnippetView.getMeasuredHeight();
        }

				// add by Ivan 2010-08-31 20:20
				if(true == FeatureOption.MTK_SNS_SUPPORT)
				{
					height += mLine1Height + mLine2Height + mLine3Height + mLine4Height;
				}
				else
				{
					height += mLine1Height + mLine2Height + mLine3Height;
				}
        

        if (isVisible(mCallButton)) {
            mCallButton.measure(0, 0);
        }
        if (isVisible(mPresenceIcon)) {
            mPresenceIcon.measure(0, 0);
        }

		if (isVisible(mSelectBox)) {
	    	mSelectBox.measure(0, 0);
        }

        ensurePhotoViewSize();

        height = Math.max(height, mPhotoViewHeight);
        height = Math.max(height, mPreferredHeight);

        if (mHeaderVisible) {
            ensureHeaderBackground();
            mHeaderTextView.measure(
                    MeasureSpec.makeMeasureSpec(mHeaderTextWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mHeaderBackgroundHeight, MeasureSpec.EXACTLY));
            height += mHeaderBackgroundDrawable.getIntrinsicHeight();
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int height = bottom - top;
        int width = right - left;

        // Determine the vertical bounds by laying out the header first.
        int topBound = 0;
        int heightWithoutHeader = height;
        if (mHeaderVisible) {
            mHeaderBackgroundDrawable.setBounds(
                    0,
                    0,
                    width,
                    mHeaderBackgroundHeight);
            mHeaderTextView.layout(0, 0, width, mHeaderBackgroundHeight);
            topBound += mHeaderBackgroundHeight;
            heightWithoutHeader -= mHeaderBackgroundHeight;
        }

        // Positions of views on the left are fixed and so are those on the right side.
        // The stretchable part of the layout is in the middle.  So, we will start off
        // by laying out the left and right sides. Then we will allocate the remainder
        // to the text fields in the middle.

        // Left side
        int leftBound = mPaddingLeft;
        View photoView = mQuickContact != null ? mQuickContact : mPhotoView;
        if (photoView != null) {
            // Center the photo vertically
            int photoTop = topBound + (height - topBound - mPhotoViewHeight) / 2;
            photoView.layout(
                    leftBound,
                    photoTop,
                    leftBound + mPhotoViewWidth,
                    photoTop + mPhotoViewHeight);
            leftBound += mPhotoViewWidth + mGapBetweenImageAndText;
        }

        // Right side
        int rightBound = right;
        if (isVisible(mCallButton)) {
            int buttonWidth = mCallButton.getMeasuredWidth();
            rightBound -= buttonWidth;
            mCallButton.layout(
                    rightBound,
                    topBound,
                    rightBound + buttonWidth,
                    height - 1); // mtk80909 height --> height - something
            mVerticalDividerVisible = true;
            ensureVerticalDivider();
            rightBound -= mVerticalDividerWidth;
            mVerticalDividerDrawable.setBounds(
                    rightBound,
                    topBound + mVerticalDividerMargin,
                    rightBound + mVerticalDividerWidth,
                    height - mVerticalDividerMargin);
        } else {
            mVerticalDividerVisible = false;
        }

        if (isVisible(mPresenceIcon)) {
            int iconWidth = mPresenceIcon.getMeasuredWidth();
            rightBound -= mPresenceIconMargin + iconWidth;
            mPresenceIcon.layout(
                    rightBound,
                    topBound,
                    rightBound + iconWidth,
                    height);
        }

        if (mHorizontalDividerVisible) {
            ensureHorizontalDivider();
            mHorizontalDividerDrawable.setBounds(
                    0,
                    height - mHorizontalDividerHeight,
                    width,
                    height);
        }

        topBound += mPaddingTop;
        int bottomBound = height - mPaddingBottom;

        // Text lines, centered vertically
        rightBound -= mPaddingRight;

        // Center text vertically
        
        // add by Ivan 2010-08-31 20:20
        int totalTextHeight = 0;
				if(true == FeatureOption.MTK_SNS_SUPPORT)
				{
					totalTextHeight = mLine1Height + mLine2Height + mLine3Height + mLine4Height;
				}
				else
				{
					totalTextHeight = mLine1Height + mLine2Height + mLine3Height;
				}
        
        int textTopBound = (bottomBound + topBound - totalTextHeight) / 2;

		// mtk80909, 2010-9-17
        boolean isSelectBoxVisible = isVisible(mSelectBox);
        int selectBoxWidth = 0;
        int selectBoxHeight = 0;

        int widthToReduce = 0;
        if (mContext instanceof ContactsListActivity) {
        	final float scale = mContext.getResources().getDisplayMetrics().density;
        	widthToReduce = (((ContactsListActivity)mContext).getBladeView() == null)
        			? 0 : (int)(WIDTH_IN_DIP_TO_REDUCE * scale + 0.5f);
        }
        
        if (isSelectBoxVisible) {
            selectBoxWidth = mSelectBox.getMeasuredWidth();
            selectBoxHeight = mSelectBox.getMeasuredHeight();
            mSelectBox.layout(rightBound - selectBoxWidth - widthToReduce,
			topBound + (heightWithoutHeader - selectBoxHeight) / 2, 
			rightBound - widthToReduce, 
			topBound + (heightWithoutHeader + selectBoxHeight) / 2);
        }

        mNameTextView.layout(leftBound,
                textTopBound,
                rightBound - (isSelectBoxVisible ? selectBoxWidth + widthToReduce : widthToReduce),
                textTopBound + mLine1Height);
        //mNameTextView.setTextColor(Color.parseColor("#FF4C4E4E"));
        int dataLeftBound = leftBound;
        if (isVisible(mLabelView)) {
            //dataLeftBound = Math.min((leftBound + mLabelView.getMeasuredWidth()), (rightBound - (isSelectBoxVisible ? selectBoxWidth : 0)));
            dataLeftBound = Math.min((leftBound + mLabelView.getMeasuredWidth()), (rightBound - (isSelectBoxVisible ? selectBoxWidth + widthToReduce : widthToReduce)));
            
            mLabelView.layout(leftBound,
                    textTopBound + mLine1Height,
                    dataLeftBound,
                    textTopBound + mLine1Height + mLine2Height);
            dataLeftBound += mGapBetweenLabelAndData;
        }

        if (isVisible(mDataView)) {
            mDataView.layout(dataLeftBound,
                    textTopBound + mLine1Height,
                    rightBound - (isSelectBoxVisible ? selectBoxWidth + widthToReduce : widthToReduce),
                    textTopBound + mLine1Height + mLine2Height);
        }
        
        if(true == FeatureOption.MTK_SNS_SUPPORT)
        {
        	// added by Ivan 2010-08-23 20:20
        	if (isVisible(mSnsLogo)) {
            dataLeftBound = leftBound + mSnsLogo.getMeasuredWidth();
            mSnsLogo.layout(leftBound,
                    textTopBound + mLine1Height + (mLine4Height-mLine4LogoHeight)/2 +(isVisible(mLabelView) ? mLine2Height : 0),
                    dataLeftBound,
                    textTopBound + mLine1Height + (mLine4Height-mLine4LogoHeight)/2 + mLine4LogoHeight + (isVisible(mLabelView) ? mLine2Height : 0));
            dataLeftBound += mGapBetweenLabelAndData;
        	}

        	if (isVisible(mSnsStatus)) {
        	mSnsStatus.layout(dataLeftBound,
                    textTopBound + mLine1Height + (isVisible(mLabelView) ? mLine2Height : 0),
                    rightBound - (isSelectBoxVisible ? selectBoxWidth : 0),
                    textTopBound + mLine1Height + mLine4Height + (isVisible(mLabelView) ? mLine2Height : 0));
        	}
        	// end added
      	}
      	
      	// added by Ivan 2010-08-31 20:20 	
      	int itopBound = 0;
      	int ibottomBound = 0;
      	if(true == FeatureOption.MTK_SNS_SUPPORT)
      	{
      		itopBound = textTopBound + mLine1Height + mLine2Height + mLine4Height;
      		ibottomBound = textTopBound + mLine1Height + mLine2Height + mLine3Height + mLine4Height;
      	}
      	else
      	{
      		itopBound = textTopBound + mLine1Height + mLine2Height;
      		ibottomBound = textTopBound + mLine1Height + mLine2Height + mLine3Height;
      		}
        
        if (isVisible(mSnippetView)) {
            mSnippetView.layout(leftBound,
                    itopBound,
                    rightBound - (isSelectBoxVisible ? selectBoxWidth : 0),
                    ibottomBound);
        }
    }

    private boolean isVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    /**
     * Loads the drawable for the vertical divider if it has not yet been loaded.
     */
    private void ensureVerticalDivider() {
        if (mVerticalDividerDrawable == null) {
            mVerticalDividerDrawable = mContext.getResources().getDrawable(
                    com.android.internal.R.drawable.divider_vertical_bright_opaque);
            mVerticalDividerWidth = mVerticalDividerDrawable.getIntrinsicWidth();
        }
    }

    /**
     * Loads the drawable for the horizontal divider if it has not yet been loaded.
     */
    private void ensureHorizontalDivider() {
        if (mHorizontalDividerDrawable == null) {
            mHorizontalDividerDrawable = mContext.getResources().getDrawable(
                    com.android.internal.R.drawable.divider_horizontal_dark_opaque);
            mHorizontalDividerHeight = 0;//mHorizontalDividerDrawable.getIntrinsicHeight();
        }
    }

    /**
     * Loads the drawable for the header background if it has not yet been loaded.
     */
    private void ensureHeaderBackground() {
        if (mHeaderBackgroundDrawable == null) {
            mHeaderBackgroundDrawable = mContext.getResources().getDrawable(
                   com.android.contacts.R.drawable.contact_calllog_dialerseach_result_bg);
            mHeaderBackgroundHeight = mHeaderBackgroundDrawable.getIntrinsicHeight();
        }
    }

    /**
     * Extracts width and height from the style
     */
    private void ensurePhotoViewSize() {
        if (mPhotoViewWidth == 0 && mPhotoViewHeight == 0) {
            TypedArray a = mContext.obtainStyledAttributes(null,
                    com.android.internal.R.styleable.ViewGroup_Layout,
                    QUICK_CONTACT_BADGE_STYLE, 0);
            mPhotoViewWidth = a.getLayoutDimension(
                    android.R.styleable.ViewGroup_Layout_layout_width,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mPhotoViewHeight = a.getLayoutDimension(
                    android.R.styleable.ViewGroup_Layout_layout_height,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            a.recycle();
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        if (mHeaderVisible) {
            mHeaderBackgroundDrawable.draw(canvas);
        }
        if (mHorizontalDividerVisible) {
            mHorizontalDividerDrawable.draw(canvas);
        }
        if (mVerticalDividerVisible) {
            mVerticalDividerDrawable.draw(canvas);
        }
        super.dispatchDraw(canvas);
    }

    /**
     * Sets the flag that determines whether a divider should drawn at the bottom
     * of the view.
     */
    public void setDividerVisible(boolean visible) {
        mHorizontalDividerVisible = visible;
    }

    /**
     * Sets section header or makes it invisible if the title is null.
     */
    public void setSectionHeader(String title) {
        if (!TextUtils.isEmpty(title)) {
            if (mHeaderTextView == null) {
                mHeaderTextView = new TextView(mContext);
                mHeaderTextView.setTypeface(mHeaderTextView.getTypeface(), Typeface.BOLD);
                mHeaderTextView.setTextColor(mContext.getResources()
                        .getColor(com.android.internal.R.color.dim_foreground_dark));
                mHeaderTextView.setTextSize(14);
                mHeaderTextView.setGravity(Gravity.CENTER);
                addView(mHeaderTextView);
            }
            mHeaderTextView.setText(title);
            mHeaderTextView.setVisibility(View.VISIBLE);
            mHeaderVisible = true;
        } else {
            if (mHeaderTextView != null) {
                mHeaderTextView.setVisibility(View.GONE);
            }
            mHeaderVisible = false;
        }
    }

    /**
     * Returns the quick contact badge, creating it if necessary.
     */
    public QuickContactBadge getQuickContact() {
        if (mQuickContact == null) {
            //mQuickContact = new QuickContactBadge(mContext, null, /*QUICK_CONTACT_BADGE_STYLE*/0);
            mQuickContact = new QuickContactBadge(mContext, null, QUICK_CONTACT_BADGE_STYLE);
            mQuickContact.setExcludeMimes(new String[] { Contacts.CONTENT_ITEM_TYPE });
            addView(mQuickContact);
        }
        return mQuickContact;
    }

    /**
     * Returns the photo view, creating it if necessary.
     */
    public ImageView getPhotoView() {
        if (mPhotoView == null) {
            mPhotoView = new ImageView(mContext, null, QUICK_CONTACT_BADGE_STYLE);
            // Quick contact style used above will set a background - remove it
            mPhotoView.setBackgroundDrawable(null);
            addView(mPhotoView);
        }
        return mPhotoView;
    }

    /**
     * Returns the text view for the contact name, creating it if necessary.
     */
    public TextView getNameTextView() {
        if (mNameTextView == null) {
            mNameTextView = new TextView(mContext);
            mNameTextView.setSingleLine(true);
            mNameTextView.setEllipsize(TruncateAt.MARQUEE);
            mNameTextView.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
            mNameTextView.setGravity(Gravity.CENTER_VERTICAL);
            addView(mNameTextView);
        }
        return mNameTextView;
    }

    /**
     * Adds a call button using the supplied arguments as an id and tag.
     */
    public void showCallButton(int id, int tag) {
        if (mCallButton == null) {
            mCallButton = new DontPressWithParentImageView(mContext, null);
            mCallButton.setId(id);
            mCallButton.setOnClickListener(mCallButtonClickListener);
            //mCallButton.setBackgroundResource(R.drawable.call_background);
            mCallButton.setBackgroundColor(Color.TRANSPARENT);
            mCallButton.setImageResource(R.drawable.sym_action_call);
            mCallButton.setPadding(mCallButtonPadding, 0, mCallButtonPadding, 0);
            mCallButton.setScaleType(ScaleType.CENTER);
            addView(mCallButton);
        }

        mCallButton.setTag(tag);
        mCallButton.setVisibility(View.VISIBLE);
    }

    public void hideCallButton() {
        if (mCallButton != null) {
            mCallButton.setVisibility(View.GONE);
        }
    }

    /**
     * Adds or updates a text view for the data label.
     */
    public void setLabel(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            if (mLabelView != null) {
                mLabelView.setVisibility(View.GONE);
            }
        } else {
            getLabelView();
            mLabelView.setText(text);
            mLabelView.setVisibility(VISIBLE);
        }
    }

    /**
     * Adds or updates a text view for the data label.
     */
    public void setLabel(char[] text, int size) {
        if (text == null || size == 0) {
            if (mLabelView != null) {
                mLabelView.setVisibility(View.GONE);
            }
        } else {
            getLabelView();
            mLabelView.setText(text, 0, size);
            mLabelView.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the data label, creating it if necessary.
     */
    public TextView getLabelView() {
        if (mLabelView == null) {
            mLabelView = new TextView(mContext);
            mLabelView.setSingleLine(true);
            mLabelView.setEllipsize(TruncateAt.MARQUEE);
            mLabelView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            mLabelView.setTypeface(mLabelView.getTypeface(), Typeface.BOLD);
            addView(mLabelView);
        }
        return mLabelView;
    }

    // create by Ivan 2010-08-23 20:13
    public ImageView getSnsLogo()
    {
    	//add by huibin 2010-08-31
    	if(false == FeatureOption.MTK_SNS_SUPPORT)
    	{
    		return null;
    	}
    	
    	if(mSnsLogo == null){
    		mSnsLogo = new ImageView(mContext);
    		addView(mSnsLogo);
    	}
    	return mSnsLogo;
    }
    
    // create by Ivan 2010-08-23 20:15
    public TextView getSnsStatus(){
    	//add by huibin 2010-08-31
    	if(false == FeatureOption.MTK_SNS_SUPPORT)
    	{
    		return null;
    	}
    	if (mSnsStatus == null) {
    		mSnsStatus = new TextView(mContext);
    		mSnsStatus.setSingleLine(true);
    		mSnsStatus.setEllipsize(TruncateAt.MARQUEE);
    		mSnsStatus.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
    		mSnsStatus.setTypeface(mSnsStatus.getTypeface(), Typeface.BOLD);
            	addView(mSnsStatus);
        }
        return mSnsStatus;
    }
    
    // create by Ivan 2010-08-23 20:23
    public void setSnsLogo(String snsUrl){
    	//add by huibin 2010-08-31
    	if(false == FeatureOption.MTK_SNS_SUPPORT)
    	{
    		return;
    	}
    	if(snsUrl == null){
    		if(mSnsLogo != null)
    			mSnsLogo.setVisibility(GONE);
    		return;
    	}
    	Bitmap bitmap = null;
	bitmap = BitmapFactory.decodeFile(snsUrl);
	if(bitmap == null){
		if(mSnsLogo != null)
			mSnsLogo.setVisibility(GONE);
    		return;
	}
	getSnsLogo();
    	mSnsLogo.setImageBitmap(bitmap);
    	mSnsLogo.setVisibility(VISIBLE);
    	if(mLabelView != null)
    		mLabelView.setVisibility(View.GONE);
    	if(mDataView != null)
    		mDataView.setVisibility(View.GONE);
    }
    
    // create by Ivan 2010-08-23 20:31
    public void setSnsStatus(String status){
    	//add by huibin 2010-08-31
    	if(false == FeatureOption.MTK_SNS_SUPPORT)
    	{
    		return;
    	}
    	if(status == null){
    		if(mSnsStatus != null){
    			mSnsStatus.setVisibility(GONE);
    		}
    		return;
    	}
    	getSnsStatus();
    	mSnsStatus.setText(status);
    	mSnsStatus.setVisibility(VISIBLE);
    	if(mLabelView != null)
    		mLabelView.setVisibility(View.GONE);
    	if(mDataView != null)
    		mDataView.setVisibility(View.GONE);
    }
    /**
     * Adds or updates a text view for the data element.
     */
    public void setData(char[] text, int size) {
        if (text == null || size == 0) {
            if (mDataView != null) {
                mDataView.setVisibility(View.GONE);
            }
            return;
        } else {
            getDataView();
            mDataView.setText(text, 0, size);
            mDataView.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the data text, creating it if necessary.
     */
    public TextView getDataView() {
        if (mDataView == null) {
            mDataView = new TextView(mContext);
            mDataView.setSingleLine(true);
            mDataView.setEllipsize(TruncateAt.MARQUEE);
            mDataView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            addView(mDataView);
        }
        return mDataView;
    }

    /**
     * Adds or updates a text view for the search snippet.
     */
    public void setSnippet(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            if (mSnippetView != null) {
                mSnippetView.setVisibility(View.GONE);
            }
        } else {
            getSnippetView();
            mSnippetView.setText(text);
            mSnippetView.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the search snippet, creating it if necessary.
     */
    public TextView getSnippetView() {
        if (mSnippetView == null) {
            mSnippetView = new TextView(mContext);
            mSnippetView.setSingleLine(true);
            mSnippetView.setEllipsize(TruncateAt.MARQUEE);
            mSnippetView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            mSnippetView.setTypeface(mSnippetView.getTypeface(), Typeface.BOLD);
            addView(mSnippetView);
        }
        return mSnippetView;
    }

    /**
     * Adds or updates the presence icon view.
     */
    public void setPresence(Drawable icon) {
        if (icon != null) {
            if (mPresenceIcon == null) {
                mPresenceIcon = new ImageView(mContext);
                addView(mPresenceIcon);
            }
            mPresenceIcon.setImageDrawable(icon);
            mPresenceIcon.setScaleType(ScaleType.CENTER);
            mPresenceIcon.setVisibility(View.VISIBLE);
        } else {
            if (mPresenceIcon != null) {
                mPresenceIcon.setVisibility(View.GONE);
            }
        }
    }

    /**
     * set the check box of the view
     */ 
    public void setCheckable(boolean checkable) {
        if (mSelectBox != null) {
            mSelectBox.setVisibility(View.GONE);
        } 
    }
    
    public CheckBox getCheckBox() {
    	if (mSelectBox == null) {
    		mSelectBox = new CheckBox(mContext);
    		mSelectBox.setClickable(false);
    		mSelectBox.setFocusable(false);
    		mSelectBox.setFocusableInTouchMode(false);
    		addView(mSelectBox);
		}
        mSelectBox.setVisibility(View.VISIBLE);
    	return mSelectBox;
    }
}
