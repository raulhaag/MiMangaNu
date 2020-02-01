package ar.rulosoft.mimanganu.adapters;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by Raul on 30/11/2015.
 */
public abstract class MangaRecAdapterBase extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected ArrayList<Manga> mangas;
    OnLastItem lastItemListener;
    OnMangaClick mangaClickListener;
    View.OnCreateContextMenuListener onCreateContextMenuListener;

    MangaRecAdapterBase(ArrayList<Manga> list, Context context) {
        mangas = list;
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
