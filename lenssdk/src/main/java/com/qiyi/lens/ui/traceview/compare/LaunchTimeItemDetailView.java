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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;

import androidx.annotation.Nullable;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiyi.lens.ui.traceview.TimeGap;
import com.qiyi.lenssdk.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LaunchTimeItemDetailView extends LinearLayout implements View.OnClickListener {
    private ViewGroup container;
    private List<TimeGap> left;
    private List<TimeGap> right;

    public LaunchTimeItemDetailView(Context context) {
        super(context);
        init(context);
    }

    public LaunchTimeItemDetailView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.lens_compare_item_detail, this);
        container = findViewById(R.id.lens_compare_detail_view_container);
        findViewById(R.id.lens_cost_label).setOnClickListener(this);
        findViewById(R.id.lens_cput_label).setOnClickListener(this);
        findViewById(R.id.lens_st_label).setOnClickListener(this);
    }

    public LaunchTimeItemDetailView render(List<TimeGap> left, List<TimeGap> right) {
        this.left = left;
        this.right = right;
        orderBy(new Comparator<TimeGap>() {
            @Override
            public int compare(TimeGap o1, TimeGap o2) {
                return (int) (o1.timeStamp - o2.timeStamp);
            }
        }, false);
        findViewById(R.id.lens_st_label).setSelected(true);
        return this;
    }

    @SuppressLint("SetTextI18n")
    private void orderBy(Comparator<TimeGap> comparator, boolean reverse) {
        container.removeAllViews();
        List<TimeGap> all = new ArrayList<>();
        if (left != null) {
            all.addAll(left);
        }
        if (right != null) {
            all.addAll(right);
        }
        Collections.sort(all, comparator);
        if (reverse) {
            Collections.reverse(all);
        }
        for (int i = 0; i < all.size(); i++) {
            TimeGap timeGap = all.get(i);
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.lens_compare_item_detail_subitem, null);
            TextView idView = itemView.findViewById(R.id.lens_id);
            TextView costView = itemView.findViewById(R.id.lens_cost);
            TextView cputView = itemView.findViewById(R.id.lens_cput);
            TextView stView = itemView.findViewById(R.id.lens_st);
            idView.setText("#" + i);
            costView.setText(String.valueOf(timeGap.duration));
            cputView.setText(String.valueOf(timeGap.cpuDuration / 1000_000));
            stView.setText(String.valueOf(timeGap.timeStamp / 1000f));
            if (right != null && right.contains(timeGap)) {
                strikeThrough(idView, costView, cputView, stView);
            }
            container.addView(itemView);
        }
    }

    private void strikeThrough(TextView... views) {
        for (TextView view : views) {
            SpannableString spannableString = new SpannableString(view.getText().toString());
            StrikethroughSpan span = new StrikethroughSpan();
            spannableString.setSpan(span, 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            view.setText(spannableString);
            view.setTextColor(Color.RED);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.lens_cost_label) {
            orderBy(new Comparator<TimeGap>() {
                @Override
                public int compare(TimeGap o1, TimeGap o2) {
                    return o1.duration - o2.duration;
                }
            }, v.isSelected());
        } else if (v.getId() == R.id.lens_cput_label) {
            orderBy(new Comparator<TimeGap>() {
                @Override
                public int compare(TimeGap o1, TimeGap o2) {
                    return o1.cpuDuration - o2.cpuDuration;
                }
            }, v.isSelected());
        } else if (v.getId() == R.id.lens_st_label) {
            orderBy(new Comparator<TimeGap>() {
                @Override
                public int compare(TimeGap o1, TimeGap o2) {
                    return (int) (o1.timeStamp - o2.timeStamp);
                }
            }, v.isSelected());
        }
        v.setSelected(!v.isSelected());
    }
}
