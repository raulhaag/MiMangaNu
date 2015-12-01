package ar.rulosoft.mimanganu.adapters;

import android.content.Context;
import android.view.ViewGroup;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.componentes.Manga;

/**
 * Created by Raul on 30/11/2015.
 */
public abstract class MangasRecAdapterText extends MangaRecAdapterBase {

    public MangasRecAdapterText(ArrayList<Manga> lista, Context context, boolean darkTheme) {
        super(lista,context,darkTheme);
    }

    @Override
    public MangasRecAdapter.MangasHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(MangasRecAdapter.MangasHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
