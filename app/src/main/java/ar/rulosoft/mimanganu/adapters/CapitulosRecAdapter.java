package ar.rulosoft.mimanganu.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Capitulo;

public class CapitulosRecAdapter extends RecyclerView.Adapter<CapitulosRecAdapter.CapituloHolder> {

    private ArrayList<Capitulo> capitulos;

    public CapitulosRecAdapter(ArrayList<Capitulo> capitulos) {
        this.capitulos = capitulos;
    }

    @Override
    public int getItemCount() {
        return capitulos.size();
    }

    @Override
    public void onBindViewHolder(CapituloHolder holder, int pos) {
        Capitulo cap = capitulos.get(pos);
        holder.textViewNombre.setText(android.text.Html.fromHtml(cap.getTitulo()));
        holder.textViewEstado.setText("");
        if (cap.getPaginas() > 0) {
            holder.textViewPaginas.setText(cap.getPagLeidas() + "/" + cap.getPaginas());
        }
        if (cap.isDescargado()) {
            holder.imageButton.setImageResource(R.drawable.ic_borrar);
        } else {
            holder.imageButton.setImageResource(R.drawable.ic_bajar);
        }

        holder.imageButton.setTag(cap);
    }

    @Override
    public CapituloHolder onCreateViewHolder(ViewGroup viewGroup, int pos) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_capitulo, viewGroup, false);
        return new CapituloHolder(v);
    }

    public class CapituloHolder extends RecyclerView.ViewHolder {
        TextView textViewNombre;
        TextView textViewEstado;
        TextView textViewPaginas;
        ImageView imageButton;

        public CapituloHolder(View itemView) {
            super(itemView);
            this.textViewNombre = (TextView) itemView.findViewById(R.id.capitulo_titulo);
            this.textViewEstado = (TextView) itemView.findViewById(R.id.capitulo_info);
            this.textViewPaginas = (TextView) itemView.findViewById(R.id.capitulo_paginas);
            this.imageButton = (ImageView) imageButton.findViewById(R.id.boton);
        }

    }

}
