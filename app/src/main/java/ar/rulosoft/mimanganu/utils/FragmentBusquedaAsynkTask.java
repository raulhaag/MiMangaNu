package ar.rulosoft.mimanganu.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import ar.rulosoft.mimanganu.ActivityManga;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

/**
 * Created by Raul on 22/05/2015.
 */
public class FragmentBusquedaAsynkTask extends Fragment {
    BuscarNuevo buscarNuevo;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    public AsyncTask.Status getStatus() {
        return (buscarNuevo != null ? buscarNuevo.getStatus() : AsyncTask.Status.FINISHED);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (buscarNuevo != null)
            buscarNuevo.setActivity((ActivityManga) activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (buscarNuevo != null) {
            buscarNuevo.setActivity(null);
        }
    }

    public void iniciaTarea(Manga manga, ActivityManga activity) {
        if (buscarNuevo == null || buscarNuevo.getStatus() == AsyncTask.Status.FINISHED) {
            buscarNuevo = new BuscarNuevo().setActivity(activity);
            buscarNuevo.execute(manga);
        }
    }


    public static class BuscarNuevo extends AsyncTask<Manga, Void, Integer> {
        static boolean running = false;
        static BuscarNuevo actual = null;
        ActivityManga activity;
        int mangaId = 0;
        String msg;
        String orgMsg;

        public BuscarNuevo setActivity(final ActivityManga activity) {
            this.activity = activity;
            return this;
        }

        @Override
        protected void onPreExecute() {
            running = true;
            actual = this;
            msg = activity.getResources().getString(R.string.buscandonuevo);
            orgMsg = activity.getTitle().toString();
            activity.setTitle(msg + " " + orgMsg);
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Manga... params) {
            int result = 0;
            Database.removerCapitulosHuerfanos(activity);
            ServerBase s = ServerBase.getServer(params[0].getServerId());
            mangaId = params[0].getId();
            try {
                int diff = s.buscarNuevosCapitulos(params[0].getId(), activity);
                result += diff;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            Manga manga = Database.getFullManga(activity, mangaId);
            if (activity != null) {
                activity.cargarCapitulos(manga.getChapters());
                activity.cargarDatos(manga);
                activity.str.setRefreshing(false);
                activity.setTitle(orgMsg);
                running = false;
                actual = null;
            }
        }
    }
}
