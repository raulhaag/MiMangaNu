package ar.rulosoft.mimanganu;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;

import com.fedorvlasov.lazylist.FileCache;

import ar.rulosoft.custompref.ColorListDialogFragment;
import ar.rulosoft.custompref.ColorListDialogPref;


public class PreferencesFragment extends PreferenceFragmentCompat {
    private SharedPreferences prefs;
    private FileCache mFileStorage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.fragment_preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).enableHomeButton(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            getActivity().onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment fragment;
        if (preference instanceof ColorListDialogPref) {
            fragment = ColorListDialogFragment.newInstance(preference);
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(),"android.support.v7.preference.PreferenceFragment.DIALOG");
        } else super.onDisplayPreferenceDialog(preference);    }
}

