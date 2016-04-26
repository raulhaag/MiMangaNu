package ar.rulosoft.custompref;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

import ar.rulosoft.mimanganu.R;

/**
 * Created by Raul on 14/04/2016.
 */
public class SeekBarCustomPreference extends DialogPreference {

    public int mMin;
    public int mMax;
    public int mValue;
    public String mSummary;
    private TextView textSummary;

    public SeekBarCustomPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarCustomPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekBarCustomPreference(Context context) {
        this(context, null);
    }

    public SeekBarCustomPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomDialogPref, defStyleAttr, defStyleRes);
        mMin = a.getInteger(R.styleable.CustomDialogPref_val_min, 0);
        mMax = a.getInteger(R.styleable.CustomDialogPref_val_max, 9);
        a.recycle();
        mSummary = (String) super.getSummary();
    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return  (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);
        String getValue;
        if (restorePersistedValue) {
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

    public void _persistString(String value){
        persistString(value);
        notifyChanged();
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
}
