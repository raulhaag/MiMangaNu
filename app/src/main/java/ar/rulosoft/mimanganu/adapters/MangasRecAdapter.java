package ar.rulosoft.mimanganu.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fedorvlasov.lazylist.ImageLoader;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Cover;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;

public class MangasRecAdapter extends MangaRecAdapterBase {
    private ImageLoader imageLoader;
    private Context context;

    public MangasRecAdapter(ArrayList<Manga> list, Context context, View.OnCreateContextMenuListener onCreateContextMenuListener) {
        super(list, context);
        this.onCreateContextMenuListener = onCreateContextMenuListener;
        imageLoader = new ImageLoader(context);
        this.context = context;
    }

    public MangasRecAdapter(ArrayList<Manga> list, Context context) {
        super(list, context);
        imageLoader = new ImageLoader(context);
        this.context = context;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Manga m = mangas.get(position);
        MangasHolder mHolder = (MangasHolder) holder;
        mHolder.serie.setText(m.getTitle());
        ((MangasHolder) holder).serie.setImageResource(R.drawable.image_preview);
        imageLoader.displayImg(m.getImages(), mHolder.serie);
        mHolder.v.setTag(position);
        mHolder.v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mangaClickListener != null) {
                    mangaClickListener.onMangaClick(m);
                }
            }
        });
        ServerBase server = ServerBase.getServer(m.getServerId(), context);
        mHolder.server.setImageResource(server.getIcon());
        if (m.getNews() > 0) {
            mHolder.notif.setVisibility(ImageView.VISIBLE);
        } else {
            mHolder.notif.setVisibility(ImageView.INVISIBLE);
        }
        if (position == getItemCount() - 1 && lastItemListener != null)
            lastItemListener.onRequestedLastItem();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int pos) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.control_tapa_manga, viewGroup, false);
        ViewCompat.setElevation(v, 5f);
        if (onCreateContextMenuListener != null)
            v.setOnCreateContextMenuListener(onCreateContextMenuListener);
        return new MangasHolder(v);
    }

    public class MangasHolder extends RecyclerView.ViewHolder {
        Cover serie;
        View v;
        ImageView server;
        ImageView notif;

        private MangasHolder(View itemView) {
            super(itemView);
            serie = itemView.findViewById(R.id.tapa);
            notif = itemView.findViewById(R.id.notif);
            server = itemView.findViewById(R.id.server);
            v = itemView;
        }
    }
}
