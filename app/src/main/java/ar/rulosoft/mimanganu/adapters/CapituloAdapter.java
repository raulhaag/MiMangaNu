package ar.rulosoft.mimanganu.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import ar.rulosoft.mimanganu.ActivityCapitulos;
import ar.rulosoft.mimanganu.ActivityManga;
import ar.rulosoft.mimanganu.FragmentMisMangas;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Capitulo;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ServicioColaDeDescarga;

public class CapituloAdapter extends ArrayAdapter<Capitulo> {

    public static int TRANSPARENTE = Color.parseColor("#00FFFFFF");
    public static int GRIS = Color.argb(15, 0, 0, 0);
    public static int GRIS_CLARO = Color.argb(30, 0, 0, 0);
    public static int SELECCIONADO = Color.parseColor("#33B5E5");
    private static int listItem = R.layout.listitem_capitulo;
    SparseBooleanArray selected = new SparseBooleanArray();
    ActivityManga activity;
    private LayoutInflater li;

    public CapituloAdapter(Activity context, List<Capitulo> items) {
        super(context, listItem, items);
        activity = (ActivityManga) context;
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(final int posicion, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = li.inflate(listItem, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Capitulo item = getItem(posicion);

        if (item != null) {
            holder.textViewNombre.setText(android.text.Html.fromHtml(item.getTitulo()));
            holder.textViewEstado.setText("");
            switch (item.getEstadoLectura()) {
                case Capitulo.NUEVO:
                    holder.textViewEstado.setText("Nuevo");
                    convertView.setBackgroundColor(TRANSPARENTE);
                    break;
                case Capitulo.LEIDO:
                    convertView.setBackgroundColor(GRIS);
                    break;
                case Capitulo.LEYENDO:
                    convertView.setBackgroundColor(GRIS_CLARO);
                    break;
                default:
                    convertView.setBackgroundColor(TRANSPARENTE);
            }

            if (selected.get(posicion) == true)
                convertView.setBackgroundColor(SELECCIONADO);

            holder.textViewPaginas.setText("       ");
            if (item.getPaginas() > 0) {
                holder.textViewPaginas.setText(item.getPagLeidas() + "/" + item.getPaginas());
            }
            if (item.isDescargado()) {
                holder.imageButton.setImageResource(R.drawable.ic_borrar);
            } else {
                holder.imageButton.setImageResource(R.drawable.ic_bajar);
            }

            holder.imageButton.setTag(item);
            holder.imageButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {
                    Capitulo c = (Capitulo) v.getTag();
                    if (c.isDescargado()) {
                        Manga m = activity.manga;
                        ServerBase s = ServerBase.getServer(m.getServerId());
                        String ruta = ServicioColaDeDescarga.generarRutaBase(s, m, c, activity);
                        FragmentMisMangas.DeleteRecursive(new File(ruta));
                        getItem(posicion).setDescargado(false);
                        Database.UpdateCapituloDescargado(activity, c.getId(), 0);
                        Toast.makeText(activity, activity.getResources().getString(R.string.borrado_imagenes), Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                        // ((ImageView)
                        // v).setImageResource(R.drawable.ic_bajar);
                    } else {
                        new AgregarCola().execute(c);
                    }
                }
            });
        }

        return convertView;
    }

    public void setNewSelection(int position, boolean value) {
        selected.put(position - 1, value);
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (int i = 0; i < getCount(); i++) {
            selected.put(i, true);
        }
        notifyDataSetChanged();
    }

    public void removeSelection(int position) {
        selected.delete(position - 1);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selected.clear();
        notifyDataSetChanged();
    }


    public void setSelectedOrUnselected(int position){
        if(selected.indexOfKey(position - 1) >= 0){
            selected.delete(position - 1);
        }else{
            selected.put(position - 1, true);
        }
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelection() {
        return selected;
    }

    public static class ViewHolder {
        private TextView textViewNombre;
        private TextView textViewEstado;
        private TextView textViewPaginas;
        private ImageView imageButton;

        public ViewHolder(View v) {
            this.textViewNombre = (TextView) v.findViewById(R.id.capitulo_titulo);
            this.textViewEstado = (TextView) v.findViewById(R.id.capitulo_info);
            this.textViewPaginas = (TextView) v.findViewById(R.id.capitulo_paginas);
            this.imageButton = (ImageView) v.findViewById(R.id.boton);
        }
    }

    private class AgregarCola extends AsyncTask<Capitulo, Void, Capitulo> {
        ProgressDialog asyncdialog = new ProgressDialog(activity);
        String error = "";

        @Override
        protected void onPreExecute() {
            asyncdialog.setMessage(activity.getResources().getString(R.string.iniciando));
            asyncdialog.show();
            super.onPreExecute();
        }

        @Override
        protected Capitulo doInBackground(Capitulo... arg0) {
            Capitulo c = arg0[0];
            ServerBase s = ServerBase.getServer(activity.manga.getServerId());
            try {
                if (c.getPaginas() < 1)
                    s.iniciarCapitulo(c);
            } catch (Exception e) {
                error = e.getMessage();
                e.printStackTrace();
            } finally {
                onProgressUpdate();
            }
            return c;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            asyncdialog.dismiss();
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Capitulo result) {
            if (error.length() > 1) {
                Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
            } else {
                asyncdialog.dismiss();
                Database.updateCapitulo(activity, result);
                // ColaDeDescarga.addCola(result);
                // ColaDeDescarga.iniciarCola(activity);
                ServicioColaDeDescarga.agregarDescarga(activity, result, false);
                Toast.makeText(activity, activity.getResources().getString(R.string.agregadodescarga), Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }
    }

    public static void setSELECCIONADO(int SELECCIONADO) {
        CapituloAdapter.SELECCIONADO = SELECCIONADO;
    }
}
