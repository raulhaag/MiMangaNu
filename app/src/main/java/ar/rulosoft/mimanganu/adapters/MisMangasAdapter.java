package ar.rulosoft.mimanganu.adapters;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.fedorvlasov.lazylist.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Cover;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class MisMangasAdapter extends ArrayAdapter<Manga> {

    private static int resource = R.layout.control_tapa_manga;
    Activity activity;
    private ImageLoader imageLoader;
    private boolean darkTheme = false;
    private int darkBackground;

    public MisMangasAdapter(Activity activity, List<Manga> objects, boolean darkTheme) {
        super(activity, resource, objects);
        this.darkTheme = darkTheme;
        this.darkBackground = activity.getResources().getColor(R.color.background_floating_material_dark);
        this.activity = activity;
        imageLoader = new ImageLoader(activity);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        ViewHolder holder;
        if (item == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            item = inflater.inflate(resource, null);
            holder = new ViewHolder(item);
            item.setTag(holder);
        } else {
            holder = (ViewHolder) item.getTag();
        }
        Manga m = getItem(position);
        holder.serie.setText(m.getTitle());
        imageLoader.displayImg(m.getImages(), holder.serie);
        ServerBase server = ServerBase.getServer(m.getServerId());
        holder.server.setImageResource(server.getIcon());
        if (m.getNews() > 0) {
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

    public class ViewHolder {
        Cover serie;
        View v;
        ImageView server;
        ImageView notif;

        public ViewHolder(View itemView) {
            serie = (Cover) itemView.findViewById(R.id.tapa);
            notif = (ImageView) itemView.findViewById(R.id.notif);
            server = (ImageView) itemView.findViewById(R.id.server);
            v = itemView;
            if (darkTheme) {
                ((CardView) itemView.findViewById(R.id.cardview_server_container))
                        .setCardBackgroundColor(darkBackground);
            }
        }
    }

}