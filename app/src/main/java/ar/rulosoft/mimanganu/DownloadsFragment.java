package ar.rulosoft.mimanganu;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import ar.rulosoft.mimanganu.adapters.DownloadAdapter;
import ar.rulosoft.mimanganu.services.DownloadPoolService;


public class DownloadsFragment extends Fragment {
    public boolean darkTheme;
    private ListView list;
    private DownloadAdapter downloadAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_downloads,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        darkTheme = MainActivity.darkTheme;
        list = (ListView) getView().findViewById(R.id.action_view_download);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case R.id.remove_downloaded:
                DownloadPoolService.removeDownloaded();
                downloadAdapter = new DownloadAdapter(getActivity(), getActivity(), darkTheme);
                list.setAdapter(downloadAdapter);
                break;
            case R.id.pause_downloads:
                DownloadPoolService.pauseDownload();
                break;
            case R.id.retry_errors:
                DownloadPoolService.retryError(getActivity());
                break;
            case R.id.resume_downloads:
                DownloadPoolService.resumeDownloads(getActivity());
                break;
            case R.id.remove_all:
                DownloadPoolService.removeAll();
                downloadAdapter = new DownloadAdapter(getActivity(), getActivity(), darkTheme);
                list.setAdapter(downloadAdapter);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        downloadAdapter = new DownloadAdapter(getActivity(), getActivity(), darkTheme);
        ((MainActivity)getActivity()).enableHomeButton(true);
        ((MainActivity)getActivity()).setTitle(getString(R.string.descargas));
        list.setAdapter(downloadAdapter);
    }

    @Override
    public void onPause() {
        downloadAdapter.onPause();
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_downloads, menu);
    }
}
