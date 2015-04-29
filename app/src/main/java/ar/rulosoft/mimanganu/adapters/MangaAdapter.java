package ar.rulosoft.mimanganu.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Manga;

public class MangaAdapter extends ArrayAdapter<Manga> {
    private static int listItem = R.layout.listitem_server_manga;
    private LayoutInflater li;

    public MangaAdapter(Context context, List<Manga> items) {
        super(context, listItem, items);
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int posicion, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = li.inflate(listItem, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Manga item = getItem(posicion);

        if (item != null) {
            holder.textViewNomvbre.setText(android.text.Html.fromHtml(item.getTitulo()));
        }
        return convertView;
    }

    public static class ViewHolder {
        private TextView textViewNomvbre;

        public ViewHolder(View v) {
            this.textViewNomvbre = (TextView) v.findViewById(R.id.manga_titulo);
        }
    }
}
