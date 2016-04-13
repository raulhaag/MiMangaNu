package ar.rulosoft.mimanganu.adapters;

import android.app.Activity;
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
import ar.rulosoft.mimanganu.services.DownloadsChangesListener;

public class DownloadAdapter extends ArrayAdapter<ChapterDownload> implements DownloadsChangesListener {

    private static String[] states;
    private static int listItem = R.layout.listitem_descarga;
    private ArrayList<ChapterDownload> downloads = new ArrayList<>();
    private LayoutInflater li;
    private boolean darkTheme;
    private Activity mActivity;

    public DownloadAdapter(Context context, Activity activity, boolean darkTheme) {
        super(context, listItem);
        states = context.getResources().getStringArray(R.array.estados_descarga);
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActivity = activity;
        this.darkTheme = darkTheme;
        downloads = DownloadPoolService.chapterDownloads;
        DownloadPoolService.setDownloadsChangesListener(this);
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
            holder.loadingProgressBar.setProgress(item.getProgress());
            holder.buttonImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    DownloadPoolService.removeDownload(item.chapter.getId(), mActivity);
                }
            });
        }
        return convertView;
    }

    public void onPause() {
        DownloadPoolService.setDownloadsChangesListener(null);
    }

    @Override
    public void onProgressChanged(int idx, ChapterDownload cd) {
        downloads.set(idx, cd);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStatusChanged(int idx, ChapterDownload cd) {
        if (idx >= 0) {
            downloads.set(idx, cd);
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onChapterAdded(boolean atStart, ChapterDownload cd) {
        if (atStart) {
            downloads.add(0, cd);
        } else {
            downloads.add(cd);
        }
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onChapterRemoved(int idx) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onChaptersRemoved(ArrayList<ChapterDownload> toRemove) {
        downloads.removeAll(toRemove);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
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
