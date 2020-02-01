package ar.rulosoft.custompref;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

import ar.rulosoft.mimanganu.R;

/**
 * Created by Raul on 14/04/2016.
 */
public class SeekBarCustomPreferenceFloat extends DialogPreference {

    private float mFC;
    private float mMin;
    private float mMax;
    private float mStep;
    private float mValue;
    private TextView textSummary;
    private String mSummary;

    public SeekBarCustomPreferenceFloat(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarCustomPreferenceFloat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekBarCustomPreferenceFloat(Context context) {
        this(context, null);
    }

    public SeekBarCustomPreferenceFloat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomDialogPref, defStyleAttr, defStyleRes);
        mMin = a.getFloat(R.styleable.CustomDialogPref_val_min_float, 0);
        mMax = a.getFloat(R.styleable.CustomDialogPref_val_max_float, 9);
        mStep = a.getFloat(R.styleable.CustomDialogPref_val_step, 1.f);
        mFC = -mMin;
        mMax = (mMax - mMin) / mStep;
        mMin = (mMin / mStep);
        a.recycle();
        mSummary = (String) super.getSummary();
        setLayoutResource(R.layout.preference_seekbar_widget_layout);
    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
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
        mValue = Float.parseFloat(getValue);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        SeekBar seekBar = (SeekBar) holder.findViewById(R.id.seekbar);
        textSummary = (TextView) holder.findViewById(android.R.id.summary);
        textSummary.setText(String.format(mSummary, mValue));
        seekBar.setMax((int) mMax);
        seekBar.setProgress(Math.round((mValue + mFC) / mStep));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mValue = (progress * mStep) - mFC;
                textSummary.setText(String.format(mSummary, mValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                persistString("" + mValue);
                notifyChanged();
            }
        });
        holder.itemView.setClickable(false);
    }
}
