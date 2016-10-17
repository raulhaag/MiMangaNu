package ar.rulosoft.mimanganu.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by Raul on 30/11/2015.
 *
 */
public abstract class MangaRecAdapterBase extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected ArrayList<Manga> mangas;
    protected boolean darkTheme = false;
    OnLastItem lastItemListener;
    OnMangaClick mangaClickListener;
    View.OnCreateContextMenuListener onCreateContextMenuListener;
    int darkBackground;

    MangaRecAdapterBase(ArrayList<Manga> lista, Context context, boolean darkTheme) {
        this.darkTheme = darkTheme;
        this.darkBackground = ContextCompat.getColor(context, R.color.background_floating_material_dark);
        mangas = lista;
    }

    public void setLastItemListener(OnLastItem lastItemListener) {
        this.lastItemListener = lastItemListener;
    }

    public void setMangaClickListener(OnMangaClick mangaClickListener) {
        this.mangaClickListener = mangaClickListener;
    }

    public void setOnCreateContextMenuListener(View.OnCreateContextMenuListener onCreateContextMenuListener) {
        this.onCreateContextMenuListener = onCreateContextMenuListener;
    }


    public Manga getItem(int position) {
        return mangas.get(position);
    }

    @Override
    public int getItemCount() {
        return mangas.size();
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

}
