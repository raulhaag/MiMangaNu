package ar.rulosoft.custompref;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import ar.rulosoft.mimanganu.R;

public class PreferencesListDir extends DialogPreference {
    public PreferencesListDir(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_select_directory);
    }
}
