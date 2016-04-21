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
public class SeekBarCustomPreferenceFloat extends DialogPreference {

    private int mFC;
    private int mMin;
    private int mMax;
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
        mMin = a.getInteger(R.styleable.CustomDialogPref_val_min, 0);
        mMax = a.getInteger(R.styleable.CustomDialogPref_val_max, 9);
        mStep = a.getFloat(R.styleable.CustomDialogPref_val_step, 1.f);
        mFC = -mMin;
        mMax = Math.round((mMax - mMin) / mStep);
        mMin = Math.round(mMin / mStep);
        a.recycle();
        mSummary = (String) super.getSummary();
        setLayoutResource(R.layout.preference_seekbar_widget_layout);
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
        mValue = Float.parseFloat(getValue);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        SeekBar seekBar = (SeekBar) holder.findViewById(R.id.seekbar);
        textSummary = (TextView) holder.findViewById(android.R.id.summary);
        textSummary.setText(String.format(mSummary, mValue));
        seekBar.setMax(mMax);
        seekBar.setProgress(Math.round((mValue + mFC) / mStep) );
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mValue = (progress * mStep) - mFC ;
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
