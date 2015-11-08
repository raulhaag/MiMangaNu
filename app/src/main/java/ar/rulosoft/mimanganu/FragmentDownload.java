package ar.rulosoft.mimanganu;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import ar.rulosoft.mimanganu.adapters.DownloadAdapter;
import ar.rulosoft.mimanganu.services.DownloadPoolService;

public class FragmentDownload extends Fragment {

    private ListView listDownload;
    private ShowDownloadsTask md;
    private DownloadAdapter downAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rView = inflater.inflate(R.layout.fragment_descargas, container, false);
        listDownload = (ListView) rView.findViewById(R.id.descargas);
        return rView;
    }

    @Override
    public void onResume() {
        downAdapter = new DownloadAdapter(getActivity(), ((ActivityDownloads) getActivity()).darkTheme);
        listDownload.setAdapter(downAdapter);
        md = new ShowDownloadsTask();
        if (Build.VERSION.SDK_INT >= 11)
            md.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            md.execute();
        super.onResume();
    }

    @Override
    public void onPause() {
        md.stop();
        super.onPause();
    }

    private class ShowDownloadsTask extends AsyncTask<Void, Void, Void> {
        boolean follow = true;

        @Override
        protected Void doInBackground(Void... params) {
            while (follow) {
                try {
                    downAdapter.updateAll(DownloadPoolService.chapterDownloads);
                    publishProgress();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downAdapter.notifyDataSetChanged();
                }
            });
            super.onProgressUpdate(values);
        }

        public void stop() {
            follow = false;
        }

    }
}
