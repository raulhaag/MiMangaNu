package ar.rulosoft.custompref;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import ar.rulosoft.mimanganu.R;

/**
 * Created by Raul on 15/04/2016.
 */
public class ColorListDialogFragment extends PreferenceDialogFragmentCompat {
    private Context mContext = getContext();
    private String[] mColorCodeList;
    private String[] mColorNameList;

    private ListView mListView;
    private String mSummary;
    private int mValue;


    private ColorListDialogPref parent;

    public static ColorListDialogFragment newInstance(Preference preference) {
        ColorListDialogFragment fragment = new ColorListDialogFragment();
        fragment.setParent((ColorListDialogPref) preference);
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected View onCreateDialogView(Context context) {
        mListView = new ListView(context);
        return mListView;
    }

    @Override
    public void onStart() {
        super.onStart();
        ArrayAdapter<String> color_adapter = new ArrayAdapterColor(this, R.layout.listitem_color, mValue);
        mListView.setAdapter(color_adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> container, View view, int position, long id) {
                mValue = Color.parseColor(mColorCodeList[position]);
                parent.persistInt(mValue);
                parent.setIconChange();
                onDialogClosed(true);
                getDialog().dismiss();
            }
        });
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
         if(positiveResult) {
            Integer pushValue = mValue;
           if (parent.callChangeListener(pushValue)) {
               parent.persistInt(pushValue);
               parent._notifyChanged();
            }
        }
    //super.onDialogClosed(positiveResult);
    }

    public String[] getCodeList() {
        return mColorCodeList;
    }

    public String[] getNameList() {
        return mColorNameList;
    }

    public void setParent(ColorListDialogPref parent) {
        this.parent = parent;
        mColorCodeList = parent.getCodeList();
        mColorNameList = parent.getNameList();
    }

}
