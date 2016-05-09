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
    private ColorListDialogFragment mParent;
    private String[] mColorCodeList;
    private String[] mColorNameList;

    private int mResource;
    private int mDefValue;
    private float dpiScale;

    public ArrayAdapterColor(ColorListDialogFragment parent, int resource, int defValue) {
        super(parent.getContext(), resource, parent.getCodeList());
        mParent = parent;
        mDefValue = defValue;
        mResource = resource;
        mColorCodeList = parent.getCodeList();
        mColorNameList = parent.getNameList();

        dpiScale = mParent.getContext().getResources().getDisplayMetrics().density;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = ((Activity) mParent.getContext()).getLayoutInflater();
            rowView = inflater.inflate(mResource, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.object = (LinearLayout) rowView.findViewById(R.id.colorObject);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.color_icon);
            viewHolder.text = (TextView) rowView.findViewById(R.id.colorText);
            rowView.setTag(viewHolder);
        }

        // set and fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.text.setText(mColorNameList[position]);

        // Some funky stuff, want to see it? Activate following lines!
        //int weakColor = Color.argb(30,
        //       Color.red(Color.parseColor(mColorCodeList[position])),
        //        Color.green(Color.parseColor(mColorCodeList[position])),
        //        Color.blue(Color.parseColor(mColorCodeList[position])));
        //holder.object.setBackgroundColor(weakColor);


        holder.image.setBackgroundColor(Color.parseColor(mColorCodeList[position]));
        return rowView;
    }

    static class ViewHolder {
        public LinearLayout object;
        public TextView text;
        public ImageView image;
    }
}
