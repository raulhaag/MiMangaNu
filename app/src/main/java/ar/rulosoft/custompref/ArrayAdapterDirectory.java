package ar.rulosoft.custompref;

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Johndeep on 22.08.15.
 */

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
            viewHolder.object = (LinearLayout) rowView.findViewById(R.id.dirObject);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.dirIcon);
            viewHolder.text = (TextView) rowView.findViewById(R.id.dirText);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        if (position % 2 == 0) {
            holder.object.setBackgroundColor(0x10121212);
        } else {
            holder.object.setBackgroundColor(0);
        }
        holder.text.setText(mList.get(position));
        holder.image.setImageResource(R.drawable.ic_folder);
        holder.image.setColorFilter(0xFF545454);

        return rowView;
    }
}
