package ar.rulosoft.mimanganu.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
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
import ar.rulosoft.mimanganu.services.DownloadPoolService;

public class ChapterAdapter extends ArrayAdapter<Chapter> {

    public static int TRANSPARENT = Color.TRANSPARENT;
    public static int COLOR_READ = Color.parseColor("#929292");
    public static int COLOR_READING = Color.parseColor("#424242");
    public static int COLOR_SELECTED = Color.parseColor("#33B5E5");
    private static int listItem = R.layout.listitem_capitulo;
    SparseBooleanArray selected = new SparseBooleanArray();
    ActivityManga activity;
    private ColorStateList defaultColor;
    private LayoutInflater li;
//    private StateListDrawable stateListDrawable;

    public ChapterAdapter(Activity context, List<Chapter> items) {
        super(context, listItem, items);
        activity = (ActivityManga) context;
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static void setColorSelected(int colorSelected) {
        ChapterAdapter.COLOR_SELECTED = colorSelected;
    }

    public static void setColorReading(int colorReading) {
        COLOR_READING = colorReading;
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

            if (defaultColor == null) {
                defaultColor = holder.textViewName.getTextColors();
            }

            switch (item.getReadStatus()) {
                case Chapter.NEW:
                    holder.textViewStatus.setVisibility(View.VISIBLE);
                    holder.textViewName.setTextColor(defaultColor);
                    holder.textViewPages.setTextColor(defaultColor);
                    break;
                case Chapter.READ:
                    holder.textViewName.setTextColor(COLOR_READ);
                    holder.textViewPages.setTextColor(COLOR_READ);
                    break;
                case Chapter.READING:
                    holder.textViewName.setTextColor(COLOR_READING);
                    holder.textViewPages.setTextColor(COLOR_READING);
                    break;
                default:
                    holder.textViewName.setTextColor(defaultColor);
                    holder.textViewPages.setTextColor(defaultColor);
                    break;
            }

            if (selected.get(position)) {
                convertView.setBackgroundColor(COLOR_SELECTED);
                holder.textViewName.setTextColor(Color.WHITE);
                holder.textViewPages.setTextColor(Color.WHITE);
            } else {
                convertView.setBackgroundColor(TRANSPARENT);
            }
            if (item.getPages() > 0) {
                holder.textViewPages.setText(item.getPagesRead() + "/" + item.getPages());
            } else {
                holder.textViewPages.setText("");
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
                        String ruta = DownloadPoolService.generarRutaBase(s, m, c, activity);
                        FragmentMisMangas.DeleteRecursive(new File(ruta));
                        getItem(position).setDownloaded(false);
                        Database.UpdateChapterDownloaded(activity, c.getId(), 0);
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

    public void setSelectedOrUnselected(int position) {
        if (selected.indexOfKey(position - 1) >= 0) {
            selected.delete(position - 1);
        } else {
            selected.put(position - 1, true);
        }
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelection() {
        return selected;
    }

    public Chapter[] getSelectedChapters() {
        Chapter[] chapters = new Chapter[selected.size()];
        for (int j = 0; j < selected.size(); j++) {
            chapters[j] = getItem(selected.keyAt(j));
        }
        return chapters;
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
                if (c.getPages() < 1)
                    s.chapterInit(c);
            } catch (Exception e) {
                error = e.getMessage();
                e.printStackTrace();
            } finally {
                publishProgress();
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
                Database.updateChapter(activity, result);
                DownloadPoolService.agregarDescarga(activity, result, false);
                Toast.makeText(activity, activity.getResources().getString(R.string.agregadodescarga), Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }
    }
}
