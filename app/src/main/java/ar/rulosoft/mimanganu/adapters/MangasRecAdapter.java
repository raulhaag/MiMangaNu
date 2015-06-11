package ar.rulosoft.mimanganu.adapters;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fedorvlasov.lazylist.ImageLoader;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Cover;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class MangasRecAdapter extends RecyclerView.Adapter<MangasRecAdapter.MangasHolder> {

    ArrayList<Manga> mangas;
    Context context;
    OnLastItem lastItemListener;
    OnMangaClick mangaClickListener;
    OnCreateContextMenuListener onCreateContextMenuListener;
    private ImageLoader imageLoader;

    public MangasRecAdapter(ArrayList<Manga> lista, Context context) {
        mangas = lista;
        imageLoader = new ImageLoader(context);
        this.context = context;
    }

    public void setLastItemListener(OnLastItem lastItemListener) {
        this.lastItemListener = lastItemListener;
    }

    public void setMangaClickListener(OnMangaClick mangaClickListener) {
        this.mangaClickListener = mangaClickListener;
    }

    public void setOnCreateContextMenuListener(OnCreateContextMenuListener onCreateContextMenuListener) {
        this.onCreateContextMenuListener = onCreateContextMenuListener;
    }

    public Manga getItem(int position) {
        return mangas.get(position);
    }

    @Override
    public int getItemCount() {
        return mangas.size();
    }

    @Override
    public void onBindViewHolder(MangasHolder holder, int position) {
        final Manga m = mangas.get(position);
        holder.serie.setText(m.getTitle());
        imageLoader.DisplayImage(m.getImages(), holder.serie);
        holder.v.setTag(position);
        holder.v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mangaClickListener != null) {
                    mangaClickListener.onMangaClick(m);
                }
            }
        });
        ServerBase server = ServerBase.getServer(m.getServerId());
        holder.server.setImageResource(server.getIcon());
        if (m.getNews() > 0) {
            holder.notif.setVisibility(ImageView.VISIBLE);
        } else {
            holder.notif.setVisibility(ImageView.INVISIBLE);
        }
        if (position == getItemCount() - 1 && lastItemListener != null)
            lastItemListener.onRequestedLastItem();
    }

    @Override
    public MangasHolder onCreateViewHolder(ViewGroup viewGroup, int pos) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tapa_manga, viewGroup, false);
        ViewCompat.setElevation(v, 5f);
        if (onCreateContextMenuListener != null)
            v.setOnCreateContextMenuListener(onCreateContextMenuListener);
        return new MangasHolder(v);
    }

    public void remove(Manga m) {
        mangas.remove(m);
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<Manga> result) {
        mangas.addAll(result);
        notifyDataSetChanged();
    }

    public interface OnLastItem {
        void onRequestedLastItem();
    }

    public interface OnMangaClick {
        void onMangaClick(Manga manga);
    }

    public class MangasHolder extends RecyclerView.ViewHolder {
        Cover serie;
        View v;
        ImageView server;
        ImageView notif;

        public MangasHolder(View itemView) {
            super(itemView);
            serie = (Cover) itemView.findViewById(R.id.tapa);
            notif = (ImageView) itemView.findViewById(R.id.notif);
            server = (ImageView) itemView.findViewById(R.id.server);
            v = itemView;
        }
    }

}
