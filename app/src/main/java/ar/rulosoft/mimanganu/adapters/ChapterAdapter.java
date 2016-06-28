package ar.rulosoft.mimanganu.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Database;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.utils.ThemeColors;

public class ChapterAdapter extends ArrayAdapter<Chapter> {
    private static int COLOR_READ = Color.parseColor("#b2b2b2");
    private static int COLOR_READING = Color.parseColor("#121212");
    private static int COLOR_SELECTED = Color.parseColor("#33B5E5");

    private static int buttonDelete = R.drawable.ic_action_delete_light;
    private static int buttonDownload = R.drawable.ic_action_download_light;

    private static int listItem = R.layout.listitem_capitulo;
    private SparseBooleanArray selected = new SparseBooleanArray();
    private Activity activity;
    private ColorStateList defaultColor;
    private LayoutInflater li;
    private ArrayList<Chapter> chapters;
    private boolean can_download;

    public ChapterAdapter(Activity activity, ArrayList<Chapter> items, boolean can_download) {
        super(activity, listItem);
        this.activity = activity;
        this.chapters = items;
        this.can_download = can_download;
        li = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static void setColor(boolean dark_theme, int colorSelected, int colorReading) {
        COLOR_SELECTED = colorSelected;
        COLOR_READING = colorReading;
        if (dark_theme) {
            COLOR_READING = ThemeColors.brightenColor(COLOR_READING, 150);
            COLOR_READ = Color.parseColor("#585858");
            buttonDelete = R.drawable.ic_action_delete_dark;
            buttonDownload = R.drawable.ic_action_download_dark;
        }
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

        final Chapter item = getItem(position);

        if (item != null) {
            holder.textViewName.setText(item.getTitle());
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
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }
            if (item.getPages() > 0) {
                holder.textViewPages.setText(String.format("%d/%d", item.getPagesRead(), item.getPages()));
            } else {
                holder.textViewPages.setText("");
            }
            holder.imageButton.setImageResource(item.isDownloaded() ? buttonDelete : buttonDownload);
            holder.imageButton.setTag(item);
            holder.imageButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(final View v) {
                    Chapter c = (Chapter) v.getTag();
                    if (c.isDownloaded()) {
                        item.freeSpace(getContext());
                        getItem(position).setDownloaded(false);
                        Database.updateChapterDownloaded(activity, c.getId(), 0);
                        Toast.makeText(activity, activity.getResources().getString(R.string.borrado_imagenes), Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                        // ((ImageView)v).setImageResource(R.drawable.ic_bajar);
                    } else {
                        if (can_download) {
                            try {
                                DownloadPoolService.addChapterDownloadPool(activity, c, false);
                                Toast.makeText(activity, activity.getResources().getString(R.string.agregadodescarga), Toast.LENGTH_SHORT).show();
                            }catch (Exception e){
                                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
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

    public void selectTo(int idx) {
        for (int i = idx; i < getCount(); i++) {
            selected.put(i, true);
        }
        notifyDataSetChanged();
    }

    public void selectFrom(int idx) {
        for (int i = 0; i < idx; i++) {
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


    @Override
    public int getCount() {
        return chapters.size();
    }

    @Override
    public Chapter getItem(int position) {
        return chapters.get(position);
    }

    @Override
    public void remove(Chapter object) {
        chapters.remove(object);
        notifyDataSetChanged();
    }

    @Override
    public void insert(Chapter object, int index) {
        chapters.add(index, object);
    }

    @Override
    public void addAll(Chapter... items) {
        //chapters.addAll(items);
        //("Not Supported yet");
    }

    public void sort_chapters(Comparator<Chapter> comparator) {
        try {
            Collections.sort(chapters, comparator);
        }catch (Exception e){
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    @Override
    public void addAll(Collection<? extends Chapter> collection) {
        chapters.addAll(collection);
    }

    @Override
    public void add(Chapter object) {
        chapters.add(object);
    }

    public void replaceData(ArrayList<Chapter> chapters) {
        this.chapters = chapters;
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
}
