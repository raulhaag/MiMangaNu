package ar.rulosoft.mimanganu.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by Raul on 30/11/2015.
 */
public class MangasRecAdapterText extends MangaRecAdapterBase {

    public MangasRecAdapterText(ArrayList<Manga> list, Context context) {
        super(list, context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_server_manga, viewGroup, false);
        if (onCreateContextMenuListener != null)
            v.setOnCreateContextMenuListener(onCreateContextMenuListener);
        return new MangasHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Manga m = mangas.get(position);
        MangasHolder mHolder = (MangasHolder) holder;
        mHolder.title.setText(m.getTitle());
        mHolder.v.setTag(position);
        mHolder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mangaClickListener != null) {
                    mangaClickListener.onMangaClick(m);
                }
            }
        });
        if (position == getItemCount() - 1 && lastItemListener != null)
            lastItemListener.onRequestedLastItem();
    }

    public class MangasHolder extends RecyclerView.ViewHolder {
        View v;
        TextView title;

        public MangasHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.manga_title);
            v = itemView;
        }
    }
}
