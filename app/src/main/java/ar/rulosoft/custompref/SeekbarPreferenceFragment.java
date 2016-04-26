package ar.rulosoft.custompref;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import ar.rulosoft.mimanganu.R;

public class SeekbarPreferenceFragment extends PreferenceDialogFragmentCompat {
    SeekBarCustomPreference parent;
    private SeekBar mSeekBar;
    private TextView mMessageValue;
    private float dpiScale;
    private int mMin;
    private int mMax;
    private int mValue = 0;

    public static SeekbarPreferenceFragment newInstance(Preference preference) {
        SeekbarPreferenceFragment fragment = new SeekbarPreferenceFragment();
        fragment.setParent((SeekBarCustomPreference) preference);
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected View onCreateDialogView(Context context) {
        dpiScale = context.getResources().getDisplayMetrics().density;
        LinearLayout mLayout = new LinearLayout(getContext());
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mMin = parent.mMin;
        mMax = parent.mMax;
        mValue = parent.mValue;
        int padding = 15;

        mMessageValue = new TextView(getContext());
        mMessageValue.setTextSize(9 * dpiScale);
        mMessageValue.setText(String.format(parent.mSummary, mValue));
        mMessageValue.setPadding(
                (int) (padding * dpiScale), (int) (padding * dpiScale),
                (int) (padding * dpiScale), (int)(padding/2 * dpiScale));
        mLayout.addView(mMessageValue);

        mSeekBar = new SeekBar(getContext());
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mMessageValue.setText(String.format(parent.mSummary, mSeekBar.getProgress() + mMin));
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
    public void onStart() {
        super.onStart();

    }

    public void setParent(SeekBarCustomPreference parent) {
        this.parent = parent;
    }

    @Override
    public void onDialogClosed(boolean b) {
        if(b) {
            String pushValue = mValue + "";
            if (parent.callChangeListener(pushValue)) {
                parent._persistString(pushValue);
            }
        }
    }

    @Override
    protected void onPrepareDialogBuilder(android.support.v7.app.AlertDialog.Builder builder) {
        builder.setPositiveButton(getContext().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                parent.mValue = mSeekBar.getProgress() + mMin;
                parent._persistString("" + parent.mValue);
            }
        });

        builder.setNegativeButton(getContext().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        super.onPrepareDialogBuilder(builder);
    }

}
