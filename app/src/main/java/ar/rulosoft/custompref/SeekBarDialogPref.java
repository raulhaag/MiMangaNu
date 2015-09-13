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

public class SeekBarDialogPref extends DialogPreference {
    private SeekBar mSeekBar;
    private TextView mMessageValue;

    private float dpiScale;

    private int mMin;
    private int mMax;

    private int mValue = 0;

    private String mSummary;

    public SeekBarDialogPref(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CustomDialogPref, defStyleAttr, defStyleRes);
        mMin = a.getInteger(R.styleable.CustomDialogPref_val_min, 0);
        mMax = a.getInteger(R.styleable.CustomDialogPref_val_max, 9);
        a.recycle();

        dpiScale = getContext().getResources().getDisplayMetrics().density;

        mSummary = (String) super.getSummary();
    }

    public SeekBarDialogPref(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarDialogPref(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekBarDialogPref(Context context) {
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
                mMessageValue.setText(String.format(mSummary, mSeekBar.getProgress() + mMin));
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
