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
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import ar.rulosoft.mimanganu.R;

public class ColorListDialogPref extends DialogPreference {
    private Context mContext = getContext();
    private String[] mColorCodeList;
    private String[] mColorNameList;
    ShapeDrawable mShapeDraw;

    private ListView mListView;
    private String mSummary;
    private int mValue;

    public ColorListDialogPref(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);

        /** Get and prepare list of colors */
        mColorCodeList = mContext.getResources().getStringArray(R.array.color_codes);
        mColorNameList = mContext.getResources().getStringArray(R.array.color_names);

        /** Create simple filled circle shape */
        float dpiScale = mContext.getResources().getDisplayMetrics().density;
        mShapeDraw = new ShapeDrawable(new OvalShape());
        mShapeDraw.setIntrinsicHeight((int) (36 * dpiScale));
        mShapeDraw.setIntrinsicWidth((int) (36 * dpiScale));

        /** Disable buttons, we choose color by clicking on it either way */
        super.setPositiveButtonText(null);

        /** In this case, I retrieve the summary, so I can simulate the
         * behavior of the other pref widgets */
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

    public String[] getCodeList() {
        return mColorCodeList;
    }

    public String[] getNameList() {
        return mColorNameList;
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
    protected View onCreateDialogView() {
        mListView = new ListView(getContext());
        return (mListView);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);
        ArrayAdapter<String> color_adapter = new ArrayAdapterColor(this,
                R.layout.listitem_color, mValue);
        mListView.setAdapter(color_adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mValue = Color.parseColor(mColorCodeList[position]);
                onDialogClosed(true);
                setIconChange();
                getDialog().dismiss();
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            Integer pushValue = mValue;
            if (callChangeListener(pushValue)) {
                persistInt(pushValue);
                notifyChanged();
            }
        }
    }

    protected void setIconChange() {
        /** Create Shape and ImageView, set color and push it into the icon, fast and small */
        mShapeDraw.getPaint().setColor(mValue);
        ImageView myColorDraw = new ImageView(mContext);
        myColorDraw.setImageDrawable(mShapeDraw);
        super.setIcon(myColorDraw.getDrawable());
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
}
