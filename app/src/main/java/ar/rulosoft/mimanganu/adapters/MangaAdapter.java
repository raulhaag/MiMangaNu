package ar.rulosoft.mimanganu.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Manga;

public class MangaAdapter extends ArrayAdapter<Manga> {
    private static int listItem = R.layout.listitem_server_manga;
    boolean darkTheme;
    private LayoutInflater li;

    public MangaAdapter(Context context, List<Manga> items, boolean darkTheme) {
        super(context, listItem, items);
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.darkTheme = darkTheme;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = li.inflate(listItem, parent, false);
            holder = new ViewHolder(convertView, darkTheme);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Manga item = getItem(position);

        if (item != null) {
            holder.textViewName.setText(android.text.Html.fromHtml(item.getTitle()));
        }
        return convertView;
    }

    public void addAll(ArrayList<Manga> mangasNuevos) {
        for (Manga manga : mangasNuevos) {
            add(manga);
        }
    }

    public static class ViewHolder {
        private TextView textViewName;

        public ViewHolder(View v, boolean darkTheme) {
            this.textViewName = (TextView) v.findViewById(R.id.manga_title);
            if (!darkTheme) {
                this.textViewName.setTextColor(Color.parseColor("#111111"));
            }
        }
    }
}
