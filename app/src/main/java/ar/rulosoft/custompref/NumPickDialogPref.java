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
 * Created by Johndeep on 13.08.15.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import ar.rulosoft.mimanganu.R;

public class NumPickDialogPref extends DialogPreference {
    private NumberPicker mNumberPicker;
    private TextView mMessageValue;

    private int mMin;
    private int mMax;

    private boolean mWrapAround;
    private int mValue = 0;

    private String mSummary;

    public NumPickDialogPref(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.dialog_numpicker_pref);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CustomDialogPref, defStyleAttr, defStyleRes);
        mMin = a.getInteger(R.styleable.CustomDialogPref_val_min, 0);
        mMax = a.getInteger(R.styleable.CustomDialogPref_val_max, 9);
        mWrapAround = a.getBoolean(R.styleable.CustomDialogPref_wrap_around, false);
        a.recycle();

        mSummary = (String) super.getSummary();
    }

    public NumPickDialogPref(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NumPickDialogPref(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumPickDialogPref(Context context) {
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
    protected void onBindDialogView(@NonNull View view) {

        mMessageValue = (TextView) view.findViewById(R.id.dialogText);
        mMessageValue.setText(String.format(mSummary, mValue + mMin));

        mNumberPicker = (NumberPicker) view.findViewById(R.id.dialogNumPicker);
        mNumberPicker.setOnScrollListener(new NumberPicker.OnScrollListener() {

            @Override
            public void onScrollStateChange(NumberPicker view, int scrollState) {
                mMessageValue.setText(String.format(mSummary, mNumberPicker.getValue()));
            }
        });
        mNumberPicker.setMinValue(mMin);
        mNumberPicker.setMaxValue(mMax);
        mNumberPicker.setValue(mValue);
        mNumberPicker.setWrapSelectorWheel(mWrapAround);
        mNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            mValue = mNumberPicker.getValue();
            String pushValue = String.valueOf(mValue);
            if (callChangeListener(pushValue)) {
                persistString(pushValue);
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restorePrefValue, Object defaultValue) {
        String getValue;
        if (restorePrefValue) {
            if (defaultValue == null) {
                getValue = getPersistedString("0");
            } else {
                getValue = getPersistedString(String.valueOf(defaultValue));
            }
        } else {
            getValue = String.valueOf(defaultValue);
        }
        mValue = Integer.parseInt(getValue);
    }
}
