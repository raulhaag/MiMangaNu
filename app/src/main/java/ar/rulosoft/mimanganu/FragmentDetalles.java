package ar.rulosoft.mimanganu;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fedorvlasov.lazylist.ImageLoader;

import ar.rulosoft.mimanganu.componentes.ControlInfo;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class FragmentDetalles extends Fragment {

    Manga m;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ImageLoader imageLoader;
    ControlInfo datos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rView = inflater.inflate(R.layout.fragment_detalle, container, false);
        datos = (ControlInfo) rView.findViewById(R.id.detalles);
        mSwipeRefreshLayout = (SwipeRefreshLayout)rView.findViewById(R.id.str);
        int[] colors = ((ActivityCapitulos) getActivity()).colors;
        datos.setColor(colors[0]);
        datos.setTitulo("");
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new CargarDetalles().execute();
            }
        });
        mSwipeRefreshLayout.setColorSchemeColors(colors[0], colors[1]);
        return rView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        imageLoader = new ImageLoader(getActivity());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (datos != null && m != null) {
            String infoExtra = "";
            if (m.isFinalizado()) {
                infoExtra = infoExtra + getResources().getString(R.string.finalizado);
            } else {
                infoExtra = infoExtra + getResources().getString(R.string.en_progreso);
            }
            datos.setEstado(infoExtra);
            datos.setSinopsis(m.getSinopsis());
            datos.setServidor(ServerBase.getServer(m.getServerId()).getServerName());
            datos.setTitulo(m.getTitulo());
            if(m.getAutor().length() > 1){
                datos.setAutor(m.getAutor());
            }else{
                datos.setAutor(getResources().getString(R.string.nodisponible));
            }
            imageLoader.DisplayImage(m.getImages(), datos);
        }
    }

    private class CargarDetalles extends AsyncTask<Void, Void, Void> {

        String error = ".";

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ServerBase.getServer(m.getServerId()).cargarPortada(m,true);
            } catch (Exception e) {
                error = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            String infoExtra = "";
            if (m.isFinalizado()) {
                infoExtra = infoExtra + getResources().getString(R.string.finalizado);
            } else {
                infoExtra = infoExtra + getResources().getString(R.string.en_progreso);
            }
            datos.setEstado(infoExtra);
            datos.setSinopsis(m.getSinopsis());
            datos.setServidor(ServerBase.getServer(m.getServerId()).getServerName());
            if(m.getAutor().length() > 1){
                datos.setAutor(m.getAutor());
            }else{
                datos.setAutor(getResources().getString(R.string.nodisponible));
            }
            imageLoader.DisplayImage(m.getImages(), datos);
            Database.updateManga(getActivity(),m);
            mSwipeRefreshLayout.setRefreshing(false);
        }

    }
}
