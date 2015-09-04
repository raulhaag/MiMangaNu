package ar.rulosoft.mimanganu.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.services.ChapterDownload;
import ar.rulosoft.mimanganu.services.DownloadPoolService;

public class DownloadAdapter extends ArrayAdapter<ChapterDownload> {

    public static String[] states;
    private static int listItem = R.layout.listitem_descarga;
    public boolean darkTheme;
    ArrayList<ChapterDownload> downloads = new ArrayList<>();
    private LayoutInflater li;

    public DownloadAdapter(Context context, ArrayList<ChapterDownload> objects, boolean darkTheme) {
        super(context, listItem);
        states = context.getResources().getStringArray(R.array.estados_descarga);
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.darkTheme = darkTheme;
    }

    @Override
    public ChapterDownload getItem(int position) {
        return downloads.get(position);
    }

    @Override
    public int getCount() {
        return downloads.size();
    }

    @Override
    public void add(ChapterDownload object) {
        downloads.add(object);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = li.inflate(listItem, null);
            holder = new ViewHolder(convertView, darkTheme);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ChapterDownload item = getItem(position);

        if (item != null) {
            String textInfo = " " + states[item.status.ordinal()];
            holder.textViewName.setText(android.text.Html.fromHtml(item.getChapter().getTitle() + textInfo));
            holder.loadingProgressBar.setMax(item.getChapter().getPages());
            holder.loadingProgressBar.setProgress(item.getProgess());
            holder.buttonImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (DownloadPoolService.quitarDescarga(item.chapter.getId(), getContext())) {
                        remove(item);
                        notifyDataSetChanged();
                    }
                }
            });
        }
        return convertView;
    }

    @Override
    public void remove(ChapterDownload object) {
        downloads.remove(object);
    }

    public void updateAll(ArrayList<ChapterDownload> mDescargas) {
        if (mDescargas != null) {
            for (int i = 0; i < mDescargas.size(); i++) {
                boolean isNew = true;
                ChapterDownload toCompare = mDescargas.get(i);
                for (int j = 0; j < getCount(); j++) {
                    if (getItem(j).getChapter().getId() == toCompare.getChapter().getId()) {
                        isNew = false;
                        ChapterDownload item = getItem(j);
                        item.setProgess(toCompare.getProgess());
                        item.status = toCompare.status;
                        break;
                    }
                }
                if (isNew) {
                    downloads.add(toCompare);
                }
            }
        }
    }

    public static class ViewHolder {
        private TextView textViewName;
        private ProgressBar loadingProgressBar;
        private ImageButton buttonImageView;

        public ViewHolder(View v, boolean darkTheme) {
            this.textViewName = (TextView) v.findViewById(R.id.nombre);
            this.buttonImageView = (ImageButton) v.findViewById(R.id.boton);
            this.loadingProgressBar = (ProgressBar) v.findViewById(R.id.progreso);
            if (darkTheme)
                this.buttonImageView.setImageResource(R.drawable.ic_action_x_dark);
        }
    }

}
