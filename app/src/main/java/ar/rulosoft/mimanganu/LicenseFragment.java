package ar.rulosoft.mimanganu;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;

import static android.R.color.black;

public class LicenseFragment extends Fragment {

    private TextView mLicenseView;
    private static final String TAG = "LicenseFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        setRetainInstance(true);
        mLicenseView = new TextView(getActivity());
//        mLicenseView.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);
        mLicenseView.setPadding(10, 10, 10, 10);
        if (!MainActivity.darkTheme) {
            mLicenseView.setTextColor(ContextCompat.getColor(getContext(), black));
        }
        ScrollView newScroll = new ScrollView(getActivity());
        newScroll.addView(mLicenseView);
        return newScroll;
    }

    @Override
    public void onResume() {
        super.onResume();
        showLicense("Thanks.txt");
        ((MainActivity) getActivity()).setTitle(getString(R.string.licencia));
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
            case R.id.licenseViewThanks: {
                showLicense("Thanks.txt");
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
            mLicenseView.setText(licenseStr);
        } catch (IOException e) {
            Log.e(TAG, "Error while loading license", e);
        }
    }
}
