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
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import ar.rulosoft.mimanganu.R;

public class SeekBarDialogFloatPref extends DialogPreference {
    private SeekBar mSeekBar;
    private TextView mMessageValue;

    private int mMin;
    private int mMax;
    private int mType;

    private float dpiScale;

    private int mValue = 0;

    private String mSummary;

    public SeekBarDialogFloatPref(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.dialog_seekbar_pref);

        dpiScale = getContext().getResources().getDisplayMetrics().density;

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CustomDialogPref, defStyleAttr, defStyleRes);
        mMin = a.getInteger(R.styleable.CustomDialogPref_val_min, 0);
        mMax = a.getInteger(R.styleable.CustomDialogPref_val_max, 9);

        /**  mType = 0 - no type, no modification */
        mType = a.getInteger(R.styleable.CustomDialogPref_val_type, 0);
        if (mType == 1) {
            /** mType = 1 - scrollFactor, so modify to have 0.5 steps,
             *              range is from 0.5 to 5.0 */
            mMin = 1;
            mMax = 10;
        }
        a.recycle();

        mSummary = (String) super.getSummary();
    }

    public SeekBarDialogFloatPref(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarDialogFloatPref(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekBarDialogFloatPref(Context context) {
        this(context, null);
    }

    @Override
    public CharSequence getSummary() {
        final Integer entry = mValue;
        if (mSummary == null) {
            return super.getSummary();
        } else {
            return setMessage(mSummary, entry);
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

    private String setMessage(String _summary, int _value) {
        float newValue = _value;
        switch (mType) {
            case 1: {
                newValue = _value * 0.5f;
                return String.format(_summary, newValue);
            }
            default: {
                return String.format(_summary, (int) newValue);
            }
        }
    }

    @Override
    protected View onCreateDialogView() {
        LinearLayout mLayout = new LinearLayout(getContext());
        mLayout.setOrientation(LinearLayout.VERTICAL);

        int padding = 15;

        mMessageValue = new TextView(getContext());
        mMessageValue.setTextSize(9 * dpiScale);
        mMessageValue.setText(String.format(mSummary, mValue));
        mMessageValue.setPadding(
                (int) (padding * dpiScale), (int) (padding * dpiScale),
                (int) (padding * dpiScale), 0);
        mLayout.addView(mMessageValue);

        mSeekBar = new SeekBar(getContext());
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mMessageValue.setText(setMessage(mSummary, mSeekBar.getProgress() + mMin));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mSeekBar.setMax(mMax - mMin);
        mSeekBar.setProgress(mValue - mMin);
        mSeekBar.setPadding(
                (int) (padding * dpiScale * 2), (int) (padding * dpiScale),
                (int) (padding * dpiScale * 2), (int) (padding * dpiScale / 2));
        mLayout.addView(mSeekBar);

        return mLayout;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            mValue = mSeekBar.getProgress() + mMin;
            String pushValue = String.valueOf((mType == 1) ? mValue * 0.5f : mValue);
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
        mValue = (mType == 1) ? (int) (Float.parseFloat(getValue) * 2) : Integer.parseInt(getValue);
    }
}
