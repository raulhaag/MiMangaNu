package ar.rulosoft.mimanganu.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.fedorvlasov.lazylist.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.ControlTapaSerie;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class MisMangasAdaptes extends ArrayAdapter<Manga> {

    private static int resource = R.layout.listitem_mis_mangas;
    Activity c;
    ViewHolder holder;
    private ImageLoader imageLoader;

    public MisMangasAdaptes(Activity context, List<Manga> objects) {
        super(context, resource, objects);
        c = context;
        imageLoader = new ImageLoader(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        if (item == null) {
            LayoutInflater inflater = c.getLayoutInflater();
            item = inflater.inflate(resource, null);
            holder = new ViewHolder();
            holder.serie = (ControlTapaSerie) item.findViewById(R.id.tapa);
            holder.notif = (ImageView) item.findViewById(R.id.notif);
            holder.server = (ImageView) item.findViewById(R.id.server);
            item.setTag(holder);
        } else {
            holder = (ViewHolder) item.getTag();
        }

        Manga s = getItem(position);
        ServerBase server = ServerBase.getServer(s.getServerId());
        holder.serie.setText(s.getTitulo());
        imageLoader.DisplayImage(s.getImages(), holder.serie);
        holder.server.setImageResource(server.getIcon());
        if (s.getNuevos() > 0) {
            holder.notif.setVisibility(ImageView.VISIBLE);
        } else {
            holder.notif.setVisibility(ImageView.INVISIBLE);
        }

        return (item);
    }

    public void addAll(ArrayList<Manga> mangasNuevos) {
        for (Manga manga : mangasNuevos) {
            add(manga);
        }
    }

    static class ViewHolder {
        public ImageView notif;
        public ImageView server;
        public ControlTapaSerie serie;
    }

}
