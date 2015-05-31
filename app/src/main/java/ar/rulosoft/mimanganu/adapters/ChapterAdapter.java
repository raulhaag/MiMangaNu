package ar.rulosoft.mimanganu.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
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

import ar.rulosoft.mimanganu.ActivityManga;
import ar.rulosoft.mimanganu.FragmentMisMangas;
import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.servers.ServerBase;
import ar.rulosoft.mimanganu.services.ServicioColaDeDescarga;

public class ChapterAdapter extends ArrayAdapter<Chapter> {

    public static int TRANSPARENT = Color.parseColor("#00FFFFFF");
    public static int GRAY = Color.parseColor("#E0E0E0");
    public static int LIGTH_GRAY = Color.parseColor("#424242");
    public static int SELECTED = Color.parseColor("#33B5E5");
    private static int listItem = R.layout.listitem_capitulo;
    SparseBooleanArray selected = new SparseBooleanArray();
    ActivityManga activity;
    private ColorStateList defaultColor;
    private LayoutInflater li;

    public ChapterAdapter(Activity context, List<Chapter> items) {
        super(context, listItem, items);
        activity = (ActivityManga) context;
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static void setSELECTED(int SELECTED) {
        ChapterAdapter.SELECTED = SELECTED;
    }

    public static void setLigthGray(int ligthGray) {
        LIGTH_GRAY = ligthGray;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = li.inflate(listItem, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Chapter item = getItem(position);

        if (item != null) {
            holder.textViewName.setText(android.text.Html.fromHtml(item.getTitle()));
            holder.textViewStatus.setVisibility(View.GONE);

            if(defaultColor == null) {
                defaultColor = holder.textViewName.getTextColors();
            }

            switch (item.getReadStatus()) {
                case Chapter.NEW:
                    holder.textViewStatus.setVisibility(View.VISIBLE);
                    holder.textViewName.setTextColor(defaultColor);
                    holder.textViewPages.setTextColor(defaultColor);
                    break;
                case Chapter.READED:
                    holder.textViewName.setTextColor(GRAY);
                    holder.textViewPages.setTextColor(GRAY);
                    break;
                case Chapter.READING:
                    holder.textViewName.setTextColor(LIGTH_GRAY);
                    holder.textViewPages.setTextColor(LIGTH_GRAY);
                    break;
                default:
                    holder.textViewName.setTextColor(defaultColor);
                    holder.textViewPages.setTextColor(defaultColor);
                    break;
            }


            if (selected.get(position)) {
                convertView.setBackgroundColor(SELECTED);
                holder.textViewName.setTextColor(Color.WHITE);
                holder.textViewPages.setTextColor(Color.WHITE);
            }
            else {
                convertView.setBackgroundColor(TRANSPARENT);
            }

            holder.textViewPages.setText("       ");
            if (item.getPaginas() > 0) {
                holder.textViewPages.setText(item.getPagesReaded() + "/" + item.getPaginas());
            }
            if (item.isDownloaded()) {
                holder.imageButton.setImageResource(R.drawable.ic_borrar);
            } else {
                holder.imageButton.setImageResource(R.drawable.ic_bajar);
            }

            holder.imageButton.setTag(item);
            holder.imageButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {
                    Chapter c = (Chapter) v.getTag();
                    if (c.isDownloaded()) {
                        Manga m = activity.manga;
                        ServerBase s = ServerBase.getServer(m.getServerId());
                        String ruta = ServicioColaDeDescarga.generarRutaBase(s, m, c, activity);
                        FragmentMisMangas.DeleteRecursive(new File(ruta));
                        getItem(position).setDownloaded(false);
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
        private TextView textViewName;
        private TextView textViewStatus;
        private TextView textViewPages;
        private ImageView imageButton;

        public ViewHolder(View v) {
            this.textViewName = (TextView) v.findViewById(R.id.capitulo_titulo);
            this.textViewStatus = (TextView) v.findViewById(R.id.capitulo_info);
            this.textViewPages = (TextView) v.findViewById(R.id.capitulo_paginas);
            this.imageButton = (ImageView) v.findViewById(R.id.boton);
        }
    }

    private class AgregarCola extends AsyncTask<Chapter, Void, Chapter> {
        ProgressDialog asyncdialog = new ProgressDialog(activity);
        String error = "";

        @Override
        protected void onPreExecute() {
            asyncdialog.setMessage(activity.getResources().getString(R.string.iniciando));
            asyncdialog.show();
            super.onPreExecute();
        }

        @Override
        protected Chapter doInBackground(Chapter... arg0) {
            Chapter c = arg0[0];
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
        protected void onPostExecute(Chapter result) {
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
}
