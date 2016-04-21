package ar.rulosoft.mimanganu.adapters;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by Raul on 30/11/2015.
 *
 */
public class MangasRecAdapterText extends MangaRecAdapterBase {

    public MangasRecAdapterText(ArrayList<Manga> lista, Context context, boolean darkTheme) {
        super(lista,context,darkTheme);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_server_manga, viewGroup, false);
        ViewCompat.setElevation(v, 5f);
        if (onCreateContextMenuListener != null)
            v.setOnCreateContextMenuListener(onCreateContextMenuListener);
        return new MangasHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Manga m = mangas.get(position);
        MangasHolder mHolder = (MangasHolder)holder;
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
            title = (TextView) itemView.findViewById(R.id.manga_title);
            v = itemView;
           /* if (darkTheme) {
                ((CardView) itemView.findViewById(R.id.cardview_server_container))
                        .setCardBackgroundColor(darkBackground);
            }/*/
        }
    }
}
