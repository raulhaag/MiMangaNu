package ar.rulosoft.mimanganu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fedorvlasov.lazylist.ImageLoader;

import ar.rulosoft.mimanganu.componentes.ControlInfo;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class FragmentDetalles extends Fragment {

    Manga m;
    ImageLoader imageLoader;
    ControlInfo datos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rView = inflater.inflate(R.layout.fragment_detalle, container, false);
        datos = (ControlInfo) rView.findViewById(R.id.detalles);
        datos.setColor(((ActivityCapitulos) getActivity()).colors[0]);
        datos.setTitulo("");
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
            imageLoader.DisplayImage(m.getImages(), datos);
        }
    }
}
