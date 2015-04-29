package ar.rulosoft.mimanganu;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fedorvlasov.lazylist.ImageLoader;

import ar.rulosoft.mimanganu.componentes.DatosSerie;
import ar.rulosoft.mimanganu.componentes.Manga;

public class FragmentDetalles extends Fragment {

    Manga m;
    ImageLoader imageLoader;
    DatosSerie datos;
    TextView estado;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rView = inflater.inflate(R.layout.fragment_detalle, container, false);
        datos = (DatosSerie) rView.findViewById(R.id.detalles);
        estado = (TextView) rView.findViewById(R.id.status);

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
            datos.pTitle.setColor(Color.BLACK);
            datos.pTxt.setColor(Color.BLACK);
            String infoExtra = "";
            if (m.isFinalizado()) {
                infoExtra = infoExtra + getResources().getString(R.string.finalizado);
            } else {
                infoExtra = infoExtra + getResources().getString(R.string.en_progreso);
            }
            estado.setText(infoExtra);
            datos.inicializar(m.getTitulo(), m.getSinopsis(), 166, 250);
            imageLoader.DisplayImage(m.getImages(), datos);
        }
    }

}
