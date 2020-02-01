package ar.rulosoft.custompref;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import ar.rulosoft.mimanganu.R;

public class PreferencesListDir extends DialogPreference {
    public PreferencesListDir(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_select_directory);
    }
}
