/*
 *
 * Copyright (C) 2020 iQIYI (www.iqiyi.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.qiyi.lens.dump;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.widget.WrapGridView;
import com.qiyi.lenssdk.R;

public class DumpTagsPanel extends FullScreenPanel {

    private String[] mTags;

    public DumpTagsPanel(FloatingPanel panel, String[] tags) {
        super(panel);
        this.mTags = tags;
        setTitle("Data Dump");
    }

    @Override
    public View onCreateView(ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        WrapGridView gridView = new WrapGridView(context);
        gridView.setBackgroundColor(0xffEBEBEB);
        // load data
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int wd = metrics.widthPixels;
        float dens = metrics.density;
        int colums = (int) (wd / dens / 120);
        if (colums < 3) colums = 3;
        gridView.setNumColumns(colums);
        gridView.setVerticalSpacing(3);
        gridView.setHorizontalSpacing(3);

        gridView.setAdapter(new Adapter(viewGroup));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openTag(position);
            }
        });
//        viewGroup.setBackgroundColor(Color.WHITE);
        return gridView;
    }


    private void openTag(int position) {
        if (position < mTags.length && position >= 0) {
            new DumpDisplayLogPanel(getFloatingPanel(), position).show();
        }
    }

    class Adapter extends BaseAdapter {


        LayoutInflater inflater;

        Adapter(ViewGroup group) {
            inflater = LayoutInflater.from(group.getContext());
        }

        @Override
        public int getCount() {
            // tags cant be null or length 0 : or else throw crash
            return mTags.length;
        }

        @Override
        public String getItem(int position) {
            return mTags[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.lens_setting_entrance_item, parent, false);
            }
            TextView textView = (TextView) convertView;
            textView.setText(getItem(position));
            return textView;
        }
    }
}