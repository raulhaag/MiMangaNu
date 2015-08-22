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
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;

public class ColorListPref extends DialogPreference {

    private ListView mListView;
    private int mValue = 0;
    Context mContext = getContext();

    private String mSummary;

    public ColorListPref(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);

        /** In this case, I retrieve the summary, so I can simulate the
         * behavior of the other pref widgets */
        mSummary = (String) super.getSummary();
    }

    public ColorListPref(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorListPref(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorListPref(Context context) {
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

        ArrayList<String> color_list = new ArrayList<>();

        color_list.add("Banane");
        color_list.add("Wurst");
        color_list.add("Kekse");
        color_list.add("Kartoffel");
        color_list.add("Aubergine");

        ArrayAdapter<String> color_adapter = new ArrayAdapterDirectory(mContext,
                R.layout.listitem_color, color_list);

        mListView.setAdapter(color_adapter);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            mValue = (int) mListView.getSelectedItemId();
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
