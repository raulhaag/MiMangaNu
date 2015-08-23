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
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ar.rulosoft.mimanganu.R;

public class ArrayAdapterColor extends ArrayAdapter<String> {
    private final Context mContext;
    private final String[] mColorCodeList;
    private final String[] mColorNameList;
    private final int mResource;
    private final int mDefValue;

    static class ViewHolder {
        public LinearLayout object;
        public TextView text;
        public ImageView image;
    }

    public ArrayAdapterColor(Context context, int resource, String[] colorList, int defValue) {
        super(context, resource, colorList);
        this.mContext = context;
        this.mDefValue = defValue;
        this.mResource = resource;

        this.mColorNameList = mContext.getResources().getStringArray(R.array.color_names);
        this.mColorCodeList = colorList;
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
            viewHolder.text = (TextView) rowView.findViewById(R.id.colorText);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.colorIcon);
            viewHolder.object = (LinearLayout) rowView.findViewById(R.id.colorObject);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.text.setText(mColorNameList[position]);

        if (mDefValue == position)
            holder.image.setImageResource(R.drawable.ic_colorbox_check);
        else
            holder.image.setImageResource(R.drawable.ic_colorbox);
        holder.image.setColorFilter(Color.parseColor(mColorCodeList[position]));

        return rowView;
    }
}
