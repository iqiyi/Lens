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
package com.qiyi.lens.ui.traceview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qiyi.lens.utils.ColorStringBuilder;
import com.qiyi.lens.utils.TextWidthFixer;
import com.qiyi.lenssdk.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class TimeGapAdapter extends BaseAdapter {
    private List<TimeGap> data;
    private Context context;
    private TextWidthFixer textFixer = TextWidthFixer.getInstance();
    private NumberFormat stFormat = new DecimalFormat("0.000");
    private String searchKey;
    private long mainThreadId = Looper.getMainLooper().getThread().getId();

    TimeGapAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public TimeGap getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<TimeGap> data) {
        this.data = data;
    }


    void updateSearchKey(String key) {
        searchKey = key;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.lens_trace_view_time_gap_item, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        TimeGap item = getItem(position);
        viewHolder.id.setText(textFixer.fix("#" + getItemId(position), 4, true));
        viewHolder.cost.setText(textFixer.fix(String.valueOf(item.duration), 5));
        viewHolder.cput.setText(textFixer.fix(String.valueOf(item.cpuDuration / 1000_000L), 6));
        viewHolder.st.setText(textFixer.fix(stFormat.format(item.timeStamp / 1000f), 6));

        if (searchKey != null && item.tag.toLowerCase().contains(searchKey)) {
            ColorStringBuilder stringBuilder = new ColorStringBuilder();
            stringBuilder.append("<<< ", Color.GREEN);
            if (item.threadId == mainThreadId) {
                stringBuilder.append('+' + item.tag, Color.BLUE);
            } else {
                stringBuilder.append(item.tag, Color.BLUE);
            }
            viewHolder.name.setText(stringBuilder.build());
        } else if (item.threadId == mainThreadId) {
            viewHolder.name.setText('+' + item.tag);
            viewHolder.name.setTextColor(Color.RED);
        } else {
            viewHolder.name.setText(' ' + item.tag);
            viewHolder.name.setTextColor(context.getResources().getColor(R.color.black));
        }
        return convertView;
    }

    private static class ViewHolder {
        private final TextView id;
        private final TextView cost;
        private final TextView cput;
        private final TextView st;
        private final TextView name;

        ViewHolder(View convertView) {
            id = convertView.findViewById(R.id.lens_id);
            cost = convertView.findViewById(R.id.lens_cost);
            cput = convertView.findViewById(R.id.lens_cput);
            st = convertView.findViewById(R.id.lens_st);
            name = convertView.findViewById(R.id.lens_name);
        }
    }
}
