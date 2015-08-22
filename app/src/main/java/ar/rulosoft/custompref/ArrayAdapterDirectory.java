package ar.rulosoft.custompref;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ar.rulosoft.mimanganu.R;

/**
 * Custom Array Adapter for more control of content, this gives the directory choosing much more
 * style and looks good!
 * <p/>
 * Created by Johndeep on 22.08.15.
 */
public class ArrayAdapterDirectory extends ArrayAdapter<String> {
    private final Context mContext;
    private final ArrayList<String> mList;
    private final int mResource;

    static class ViewHolder {
        public LinearLayout object;
        public TextView text;
        public ImageView image;
    }

    public ArrayAdapterDirectory(Context context, int resource, ArrayList<String> itemList) {
        super(context, resource, itemList);
        this.mContext = context;
        this.mList = itemList;
        this.mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            rowView = inflater.inflate(this.mResource, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) rowView.findViewById(R.id.dirText);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.dirIcon);
            viewHolder.object = (LinearLayout) rowView.findViewById(R.id.dirObject);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        if (position % 2 == 0) {
            holder.object.setBackgroundColor(0x12242424);
        } else {
            holder.object.setBackgroundColor(0);
        }
        holder.text.setText(mList.get(position));
        holder.image.setImageResource(R.drawable.ic_folder);
        holder.image.setColorFilter(0xFF545454);

        return rowView;
    }
}
