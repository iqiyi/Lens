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
package com.qiyi.lens.ui.traceview.compare;

import android.content.Context;
import android.graphics.Color;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.qiyi.lens.ui.traceview.TimeGap;
import com.qiyi.lenssdk.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LaunchCompareAdapter extends BaseAdapter {
    private Context mContext;
    private List<Pair<GapGroup, GapGroup>> data = new ArrayList<>();
    private String expandTag;
    private List<TimeGap> leftRaw;
    private List<TimeGap> rightRaw;

    public LaunchCompareAdapter(Context context, List<TimeGap> leftRaw, List<TimeGap> rightRaw) {
        mContext = context;
        this.leftRaw = leftRaw;
        this.rightRaw = rightRaw;
    }

    public void setData(List<GapGroup> a, List<GapGroup> b) {
        data.clear();
        a = a == null ? Collections.<GapGroup>emptyList() : a;
        b = b == null ? Collections.<GapGroup>emptyList() : b;
        int maxLength = Math.max(a.size(), b.size());
        for (int i = 0; i < maxLength; i++) {
            GapGroup tga = i < a.size() ? a.get(i) : null;
            GapGroup tgb = i < b.size() ? b.get(i) : null;
            data.add(new Pair<>(tga, tgb));
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Pair<GapGroup, GapGroup> getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.lens_compare_adapter_item, parent, false);
            convertView.setTag(new ViewHolder(convertView));
        }
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        Pair<GapGroup, GapGroup> item = getItem(position);
        boolean add = item.second == null;
        boolean remove = item.first == null;
        boolean diff = item.first != null && !item.first.equals(item.second);
        char prefix = add ? '+' : remove ? '-' : ' ';
        viewHolder.left.render(item.first, prefix);
        viewHolder.right.render(item.second, prefix);
        if (add) {
            convertView.setBackgroundColor(0xffdafbe7);
        } else if (remove) {
            convertView.setBackgroundColor(0xfffcd7dc);
        } else if (diff) {
            convertView.setBackgroundColor(0x30ffff00);
            viewHolder.right.strikeThrough();
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
            viewHolder.right.render(null, ' ');
        }
        String tag = item.first != null ? item.first.getTag() : item.second != null ? item.second.getTag() : "";
        if (tag.equals(expandTag)) {
            viewHolder.expand.setVisibility(View.VISIBLE);
            viewHolder.expand.removeAllViews();
            LaunchTimeItemDetailView view = new LaunchTimeItemDetailView(mContext).render(filterByTag(leftRaw, tag), filterByTag(rightRaw, tag));
            viewHolder.expand.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            viewHolder.expand.requestLayout();
        } else {
            viewHolder.expand.setVisibility(View.GONE);
        }
        return convertView;
    }

    public void toggleExpand(int position) {
        GapGroup first = getItem(position).first;
        GapGroup second = getItem(position).second;
        String tag = first != null ? first.getTag() : second.getTag();
        if (tag.equals(expandTag)) {
            expandTag = null;
        } else {
            expandTag = tag;
        }
    }


    private List<TimeGap> filterByTag(List<TimeGap> list, String tag) {
        List<TimeGap> result = new ArrayList<>();
        for (TimeGap timeGap : list) {
            if (timeGap.tag.equals(tag)) {
                result.add(timeGap);
            }
        }
        return result;
    }

    private static class ViewHolder {
        private LaunchTimeItemView left;
        private LaunchTimeItemView right;
        private ViewGroup expand;

        ViewHolder(View view) {
            left = view.findViewById(R.id.lens_compare_left);
            right = view.findViewById(R.id.lens_compare_right);
            expand = view.findViewById(R.id.lens_expand_view);
        }
    }
}
