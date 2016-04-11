package ar.rulosoft.mimanganu.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import ar.rulosoft.mimanganu.FragmentManga;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

/**
 * This Class checks for new updates.
 *
 * Created by Raul
 */
public class FragmentUpdateSearchTask extends Fragment {
    private SearchForNewsChapters searchForNewsChapters;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public AsyncTask.Status getStatus() {
        return (searchForNewsChapters != null ? searchForNewsChapters.getStatus() : AsyncTask.Status.FINISHED);
    }
/*
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (searchForNewsChapters != null)
            searchForNewsChapters.setActivity((FragmentManga) activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (searchForNewsChapters != null) {
            searchForNewsChapters.setActivity(null);
        }
    }
/*/
    public void updateList(Manga manga, FragmentManga activity) {
        if (searchForNewsChapters == null || searchForNewsChapters.getStatus() == AsyncTask.Status.FINISHED) {
            searchForNewsChapters = new SearchForNewsChapters().setActivity(activity);
            searchForNewsChapters.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, manga);
        }
    }


    public static class SearchForNewsChapters extends AsyncTask<Manga, Void, Integer> {
        static boolean running = false;
        static SearchForNewsChapters actual = null;
        FragmentManga activity;
        int mangaId = 0;
        String msg;
        String orgMsg;
        String errorMsg;

        public SearchForNewsChapters setActivity(final FragmentManga activity) {
            this.activity = activity;
            return this;
        }

        @Override
        protected void onPreExecute() {
            running = true;
            actual = this;
            msg = activity.getResources().getString(R.string.buscandonuevo);
            orgMsg = activity.getActivity().getTitle().toString();
            activity.getActivity().setTitle(msg + " " + orgMsg);
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Manga... params) {
            int result = 0;
            Database.removeOrphanedChapters(activity.getActivity());
            ServerBase s = ServerBase.getServer(params[0].getServerId());
            mangaId = params[0].getId();
            try {
                int diff = s.searchForNewChapters(params[0].getId(), activity.getActivity());
                result += diff;
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    errorMsg = activity.getResources().getString(R.string.error) + ":" + e.getMessage();
                } else {
                    errorMsg = activity.getResources().getString(R.string.error);
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            Manga manga = Database.getFullManga(activity.getActivity(), mangaId);
            if (activity != null) {
                activity.loadChapters(manga.getChapters());
                activity.loadInfo(manga);
                activity.mSwipeRefreshLayout.setRefreshing(false);
                activity.getActivity().setTitle(orgMsg);
                if (result > 0) {
                    Toast.makeText(activity.getActivity(), result + activity.getString(R.string.State_New) + " manga(s)", Toast.LENGTH_SHORT).show();
                } else if (errorMsg != null && errorMsg.length() > 2) {
                    Toast.makeText(activity.getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                }
                running = false;
                actual = null;
            }
        }
    }
}
