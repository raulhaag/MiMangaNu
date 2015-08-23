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
    private ListView mListView;
    private int mValue = 0;
    private String[] mColorCodeList;
    private Context mContext = getContext();

    private String mSummary;

    public ColorListDialogPref(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);

        /** Get and prepare list of colors */
        mColorCodeList = mContext.getResources().getStringArray(R.array.color_codes);

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
        ArrayAdapter<String> color_adapter = new ArrayAdapterColor(mContext,
                R.layout.listitem_color, mColorCodeList, mValue);

        mListView.setAdapter(color_adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mValue = position;
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
        /** Create ImageView, set color and push it into the icon, fast and small */
        ImageView myColorDraw = new ImageView(mContext);
        myColorDraw.setImageResource(R.drawable.ic_colorbox);
        myColorDraw.setColorFilter(Color.parseColor(mColorCodeList[mValue]));
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
