package ar.rulosoft.custompref;

/*
 * Copyright (C) 2013 The Android Open Source Project
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
 *
 * Created by Johndeep on 22.08.15.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.ImageView;

import ar.rulosoft.mimanganu.R;

public class ColorListDialogPref extends DialogPreference {
    ImageView colorBox;
    private Context mContext = getContext();
    private String[] mColorCodeList;
    private String[] mColorNameList;
    //private ShapeDrawable mShapeDraw;
    private String mSummary;
    private int mValue;

    public ColorListDialogPref(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_color_chose_widget);

        /** Get and prepare list of colors */
        mColorCodeList = mContext.getResources().getStringArray(R.array.color_codes);
        mColorNameList = mContext.getResources().getStringArray(R.array.color_names);

        /** Create simple filled circle shape */
        //float dpiScale = mContext.getResources().getDisplayMetrics().density;
        //mShapeDraw = new ShapeDrawable(new RectShape());
        //mShapeDraw.setIntrinsicHeight((int) (36 * dpiScale));
        //mShapeDraw.setIntrinsicWidth((int) (24 * dpiScale));

        /** Disable buttons, we choose color by clicking on it either way */
        super.setPositiveButtonText(null);
        mSummary = (String) super.getSummary();
    }

    public ColorListDialogPref(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorListDialogPref(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorListDialogPref(Context context) {
        this(context, null);
    }


    @Override
    public CharSequence getSummary() {
        final Integer entry = mValue;
        if (mSummary == null) {
            return super.getSummary();
        } else {
            return String.format(mSummary, entry);
        }
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        if (summary == null && mSummary != null) {
            mSummary = null;
        } else if (summary != null && !summary.equals(mSummary)) {
            mSummary = summary.toString();
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        colorBox = (ImageView) holder.findViewById(R.id.color_icon);
        setIconChange();
    }

    public void setIconChange() {
        //mShapeDraw.getPaint().setColor(mValue);
        if (colorBox != null) {
            //colorBox.setImageDrawable(mShapeDraw);
            colorBox.setBackgroundColor(mValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restorePrefValue, Object defaultValue) {
        int getValue;
        if (restorePrefValue) {
            if (defaultValue == null) {
                getValue = getPersistedInt(0);
            } else {
                getValue = getPersistedInt(Integer.valueOf((String) defaultValue));
            }
        } else {
            getValue = Integer.valueOf((String) defaultValue);
        }
        mValue = getValue;
        setIconChange();
    }

    public String[] getCodeList() {
        return mColorCodeList;
    }

    public String[] getNameList() {
        return mColorNameList;
    }

    @Override
    public boolean persistInt(int value) {
        mValue = value;
        return super.persistInt(value);
    }

    public void _notifyChanged() {
        super.notifyChanged();
    }
}
