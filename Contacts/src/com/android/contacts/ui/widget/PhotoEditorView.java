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
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.contacts.ui.widget;

import com.android.contacts.R;
import com.android.contacts.model.EntityDelta;
import com.android.contacts.model.ContactsSource.DataKind;
import com.android.contacts.model.EntityDelta.ValuesDelta;
import com.android.contacts.model.Editor;
import com.android.contacts.ui.ViewIdGenerator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Simple editor for {@link Photo}.
 */
public class PhotoEditorView extends ImageView implements Editor, OnClickListener {
    private static final String TAG = "PhotoEditorView";

    private ValuesDelta mEntry;
    private EditorListener mListener;

    private boolean mHasSetPhoto = false;
    private boolean mReadOnly;

    public PhotoEditorView(Context context) {
        super(context);
    }

    public PhotoEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** {@inheritDoc} */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.setOnClickListener(this);
    }

    /** {@inheritDoc} */
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onRequest(EditorListener.REQUEST_PICK_PHOTO);
        }
    }

    /** {@inheritDoc} */
    public void onFieldChanged(String column, String value) {
        throw new UnsupportedOperationException("Photos don't support direct field changes");
    }

    /** {@inheritDoc} */
    public void setValues(DataKind kind, ValuesDelta values, EntityDelta state, boolean readOnly,
            ViewIdGenerator vig) {
        mEntry = values;
        mReadOnly = readOnly;

        setId(vig.getId(state, kind, values, 0));

        if (values != null) {
            // Try decoding photo if actual entry
            final byte[] photoBytes = values.getAsByteArray(Photo.PHOTO);
            if (photoBytes != null) {
                try {
                final Bitmap photo = BitmapFactory.decodeByteArray(photoBytes, 0,
                        photoBytes.length);

                setScaleType(ImageView.ScaleType.CENTER_CROP);
                setImageBitmap(photo);
                setEnabled(true);
                mHasSetPhoto = true;
                mEntry.setFromTemplate(false);
				} catch (Throwable e) {
					try {
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inSampleSize = 8;
						final Bitmap photo = BitmapFactory.decodeByteArray(photoBytes, 0,
						        photoBytes.length, options);

						setScaleType(ImageView.ScaleType.CENTER_CROP);
						setImageBitmap(photo);
						setEnabled(true);
						mHasSetPhoto = true;
						mEntry.setFromTemplate(false);
					} catch (Throwable e1) {
						//resetDefault();
    					try {
    						BitmapFactory.Options options = new BitmapFactory.Options();
    						options.inSampleSize = 12;
    						final Bitmap photo = BitmapFactory.decodeByteArray(photoBytes, 0,
    						        photoBytes.length, options);
    
    						setScaleType(ImageView.ScaleType.CENTER_CROP);
    						setImageBitmap(photo);
    						setEnabled(true);
    						mHasSetPhoto = true;
    						mEntry.setFromTemplate(false);
    					} catch (Throwable e2) {
						resetDefault();
					}
				}
				}
            } else {
                resetDefault();
            }
        } else {
            resetDefault();
        }
    }

    /**
     * Return true if a valid {@link Photo} has been set.
     */
    public boolean hasSetPhoto() {
        return mHasSetPhoto;
    }

    /**
     * Assign the given {@link Bitmap} as the new value, updating UI and
     * readying for persisting through {@link ValuesDelta}.
     */
    public void setPhotoBitmap(Bitmap photo) {
        if (photo == null) {
            // Clear any existing photo and return
            mEntry.put(Photo.PHOTO, (byte[])null);
            resetDefault();
            return;
        }

        final int size = photo.getWidth() * photo.getHeight() * 4;
        final ByteArrayOutputStream out = new ByteArrayOutputStream(size);

        try {
            photo.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            mEntry.put(Photo.PHOTO, out.toByteArray());
            setScaleType(ImageView.ScaleType.CENTER_CROP);
            setImageBitmap(photo);
            setEnabled(true);
            mHasSetPhoto = true;
            mEntry.setFromTemplate(false);

            // When the user chooses a new photo mark it as super primary
            mEntry.put(Photo.IS_SUPER_PRIMARY, 1);
        } catch (IOException e) {
            Log.w(TAG, "Unable to serialize photo: " + e.toString());
        }
    }

    /**
     * Set the super primary bit on the photo.
     */
    public void setSuperPrimary(boolean superPrimary) {
        mEntry.put(Photo.IS_SUPER_PRIMARY, superPrimary ? 1 : 0);
    }

    protected void resetDefault() {
        // Invalid photo, show default "add photo" place-holder
        setScaleType(ImageView.ScaleType.CENTER);
        if (mReadOnly) {
            setImageResource(R.drawable.ic_contact_picture);
            setEnabled(false);
        } else {
            setImageResource(R.drawable.ic_menu_add_picture);
            setEnabled(true);
        }
        mHasSetPhoto = false;
        mEntry.setFromTemplate(true);
    }

    /** {@inheritDoc} */
    public void setEditorListener(EditorListener listener) {
        mListener = listener;
    }
}
