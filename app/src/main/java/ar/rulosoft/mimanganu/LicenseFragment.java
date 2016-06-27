package ar.rulosoft.mimanganu;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import static android.R.color.black;

public class LicenseFragment extends Fragment {

    private TextView lic;
    private static final String TAG = "LicenseFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);
        lic = new TextView(getActivity());
        lic.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);
        lic.setPadding(10, 10, 10, 10);
        if (!MainActivity.darkTheme) {
            lic.setTextColor(getResources().getColor(black));
        }
        ScrollView newScroll = new ScrollView(getActivity());
        newScroll.addView(lic);
        return newScroll;
    }

    @Override
    public void onResume() {
        super.onResume();
        showLicense("MIT.txt");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.view_license, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.licenseViewAPACHE: {
                showLicense("APACHE-LICENSE-2.0.txt");
                break;
            }
            case R.id.licenseViewMIT: {
                showLicense("MIT.txt");
                break;
            }
            case android.R.id.home: {
                getActivity().onBackPressed();
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLicense(String asset) {
        try {
            InputStream is = getActivity().getAssets().open(asset);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            String licenseStr = new String(buffer);
            is.close();
            lic.setText(licenseStr);
        } catch (IOException e) {
            Log.e(TAG, "Error while loading license", e);
        }
    }
}
