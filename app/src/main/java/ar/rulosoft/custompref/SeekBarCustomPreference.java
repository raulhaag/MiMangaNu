/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * https://github.com/consp1racy/android-support-preference
 */

package ar.rulosoft.custompref;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.Map;
import java.util.WeakHashMap;

import ar.rulosoft.mimanganu.R;

/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * https://github.com/consp1racy/android-support-preference/blob/master/library/src/main/java/net/xpece/android/support/preference/SeekBarPreference.java
 */

public class SeekBarCustomPreference extends Preference implements OnSeekBarChangeListener, View.OnKeyListener {

    // A filthy hack so we can update info text while dragging seek bar thumb.
    private static final WeakHashMap<TextView, SeekBarCustomPreference> mInfoViews = new WeakHashMap<>();
    private static final WeakHashMap<String, SeekBarCustomPreference> mKeys = new WeakHashMap<>();
    private int mProgress;
    private int mPreferredMin = 0;
    private int mPreferredMax = 100;
    private boolean mTrackingTouch;
    private CharSequence mInfo;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private int mKeyProgressIncrement;

    public SeekBarCustomPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public SeekBarCustomPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarCustomPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarPreferenceStyle);
    }

    public SeekBarCustomPreference(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomDialogPref, defStyleAttr, defStyleRes);
        setMax(a.getInt(R.styleable.CustomDialogPref_val_max, mPreferredMax));
        setMin(a.getInt(R.styleable.CustomDialogPref_val_min, mPreferredMin));
        a.recycle();
        setLayoutResource(R.layout.preference_seekbar_widget_layout);
    }

    @Override
    public void onBindViewHolder(final PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.itemView.setClickable(false);

        SeekBar seekBar = (SeekBar) holder.findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(mPreferredMax - mPreferredMin);
        seekBar.setProgress(mProgress - mPreferredMin);
        seekBar.setEnabled(isEnabled());

        mKeyProgressIncrement = seekBar.getKeyProgressIncrement();
        holder.itemView.setOnKeyListener(this);

        TextView info = (TextView) holder.findViewById(android.R.id.summary);
        if (info != null) {
            mInfoViews.put(info, this);
            bindInfo(info);
            mKeys.put(getKey(), this);
        }
    }

    private void bindInfo(@NonNull TextView info) {
        info.setText(getSummary());
    }

    public CharSequence getInfo() {
        return mInfo;
    }

    public void setInfo(CharSequence info) {
        if (info == null && this.mInfo != null || info != null && !info.equals(this.mInfo)) {
            mInfo = info;
            onInfoChanged();
        }
    }

    public void onInfoChanged() {
        // DO NOT call notifyChanged()!
        for (Map.Entry<TextView, SeekBarCustomPreference> entry : mInfoViews.entrySet()) {
            TextView tv = entry.getKey();
            SeekBarCustomPreference pref = entry.getValue();
            if (pref == this) {
                bindInfo(tv);
            }
        }
    }

    public TextView getTV() {
        // DO NOT call notifyChanged()!
        for (Map.Entry<TextView, SeekBarCustomPreference> entry : mInfoViews.entrySet()) {
            TextView tv = entry.getKey();
            SeekBarCustomPreference pref = entry.getValue();
            if (pref == this) {
                return tv;
            }
        }
        return null;
    }

    public OnSeekBarChangeListener getOnSeekBarChangeListener() {
        return mOnSeekBarChangeListener;
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        mOnSeekBarChangeListener = listener;
        onInfoChanged();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (isEnabled()) {
            if (event.getAction() != KeyEvent.ACTION_UP) {
                int increment = mKeyProgressIncrement;

                switch (keyCode) {
                    case KeyEvent.KEYCODE_PLUS:
                    case KeyEvent.KEYCODE_EQUALS:
                        setProgress(getProgress() + increment);
                        return true;
                    case KeyEvent.KEYCODE_MINUS:
                        setProgress(getProgress() - increment);
                        return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        increment = -increment;
                        // fallthrough
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        increment = ViewCompat.getLayoutDirection(v) == ViewCompat.LAYOUT_DIRECTION_RTL ? -increment : increment;
                        setProgress(getProgress() + increment);
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setProgress(restoreValue ? Integer.parseInt(getPersistedString(mProgress + ""))
                : (Integer) defaultValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    public int getMin() {
        return mPreferredMin;
    }

    public void setMin(int min) {
        if (min != mPreferredMin) {
            mPreferredMin = min;
            notifyChanged();
        }
    }

    public int getMax() {
        return mPreferredMax;
    }

    public void setMax(int max) {
        if (max != mPreferredMax) {
            mPreferredMax = max;
            notifyChanged();
        }
    }

    private void setProgress(int preferredProgress, boolean notifyChanged) {
        if (preferredProgress > mPreferredMax) {
            preferredProgress = mPreferredMax;
        }
        if (preferredProgress < mPreferredMin) {
            preferredProgress = mPreferredMin;
        }
        if (preferredProgress != mProgress) {
            mProgress = preferredProgress;
            persistString("" + preferredProgress);
//            Log.d("SBP", "preferredProgress=" + preferredProgress);
            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    /**
     * Persist the seekBar's progress value if callChangeListener
     * returns true, otherwise set the seekBar's progress to the stored value
     */
    void syncProgress(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (progress != mProgress - mPreferredMin) {
            if (callChangeListener(progress)) {
                setProgress(progress + mPreferredMin, false);
            } else {
                seekBar.setProgress(mProgress - mPreferredMin);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && !mTrackingTouch) {
            syncProgress(seekBar);
        }
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onProgressChanged(seekBar, progress + mPreferredMin, fromUser);
        }
        if (fromUser) {
            TextView tv = getTV();
            if (tv != null) {
                tv.setText(String.format(super.getSummary().toString(), progress + mPreferredMin));
                for (Map.Entry<String, SeekBarCustomPreference> entry : mKeys.entrySet()) {
                    String k = entry.getKey();
                    SeekBarCustomPreference pref = entry.getValue();
                    if (pref == this) {
                        setKey(k);
                        int persist = progress + mPreferredMin;
                        persistString("" + persist);
                    }
                }
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mTrackingTouch = true;
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mTrackingTouch = false;
        if (seekBar.getProgress() != mProgress) {
            syncProgress(seekBar);
        }
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(seekBar);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        /*
         * Suppose a client uses this preference type without persisting. We
         * must save the instance state so it is able to, for example, survive
         * orientation changes.
         */

        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        // Save the instance state
        final SavedState myState = new SavedState(superState);
        myState.progress = mProgress;
        myState.max = mPreferredMax;
        myState.min = mPreferredMin;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // Restore the instance state
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mPreferredMax = myState.max;
        mPreferredMin = myState.min;
        setProgress(myState.progress, true);
    }

    @Override
    public CharSequence getSummary() {
        final Integer entry = getProgress();
        if (super.getSummary() == null) {
            return super.getSummary();
        } else {
            return String.format(super.getSummary().toString(), entry);
        }
    }

    /**
     * SavedState, a subclass of {@link BaseSavedState}, will store the state
     * of MyPreference, a subclass of Preference.
     * <p/>
     * It is important to always call through to super methods.
     */
    private static class SavedState extends BaseSavedState {
        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        int progress;
        int max;
        int min;

        public SavedState(Parcel source) {
            super(source);

            // Restore the click counter
            progress = source.readInt();
            max = source.readInt();
            min = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            // Save the click counter
            dest.writeInt(progress);
            dest.writeInt(max);
            dest.writeInt(min);
        }
    }

}
