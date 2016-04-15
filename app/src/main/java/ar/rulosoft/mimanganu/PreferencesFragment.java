package ar.rulosoft.mimanganu;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;

import com.fedorvlasov.lazylist.FileCache;




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
}

