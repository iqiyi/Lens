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
package com.qiyi.lens.ui.setting;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class SettingGridAdapter extends BaseAdapter {

    private EventViewItem[] data;
    private Context mContext;
    private LayoutInflater inflater;
    private WeakReference<View> firstView;

    SettingGridAdapter(Context context) {
        mContext = context;
        inflater = LayoutInflater.from(context);

    }

    SettingGridAdapter loadData(IConfigDataBuilder builder, ConfigEventCallBack dataChanged) {
        // load data
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        int wd = metrics.widthPixels;
        float dens = metrics.density;
        int colums = (int) (wd / dens / 120);
        if (colums < 3) colums = 3;
        data = builder.buildItems(mContext, dataChanged, colums);
        return this;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.length;
    }

    @Override
    public EventViewItem getItem(int position) {
        return data == null ? null : data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getItemViewType(int position) {
        EventViewItem item = getItem(position);
        if (item == null) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override // grid view  de
    public View getView(int position, View convertView, ViewGroup parent) {

        EventViewItem item = getItem(position);
        if (convertView == null) {
            if (item != null) {
                convertView = item.createView(inflater, parent);
            } else {
                convertView = new TextView(mContext);
                convertView.setBackgroundColor(Color.WHITE);

                // 非常规方案解决对其问题
                if (firstView != null) {
                    View view = firstView.get();
                    convertView.setLayoutParams(new ViewGroup.LayoutParams(-1, view.getMeasuredHeight()));
                }
            }
        }

        if (position == 0 && firstView == null) {
            firstView = new WeakReference<>(convertView);
        }

        TextView textView = (TextView) convertView;
        convertView.setId(position);

        if (item != null) {
            item.bindView(textView);
        }
        return textView;
    }

    public interface IConfigDataBuilder {
        EventViewItem[] buildItems(Context context, ConfigEventCallBack changed, int columCount);
    }
}
