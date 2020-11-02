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

import androidx.annotation.Nullable;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiyi.lenssdk.R;

public class LaunchTimeItemView extends LinearLayout {
    private TextView tagView;

    public LaunchTimeItemView(Context context) {
        super(context);
        init(context);
    }

    public LaunchTimeItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.lens_compare_item_view, this);
        tagView = findViewById(R.id.lens_tag_view);
    }

    @SuppressLint("SetTextI18n")
    public void render(GapGroup gap, char prefix) {
        if (gap != null) {
            setVisibility(VISIBLE);
            if (gap.getCount() > 1) {
                tagView.setText(prefix + gap.getTag() + "(" + gap.getCount() + ")");
            } else {
                tagView.setText(prefix + gap.getTag());
            }
        } else {
            setVisibility(GONE);
        }
    }

    public void strikeThrough() {
        SpannableString spannableString = new SpannableString(tagView.getText().toString());
        StrikethroughSpan colorSpan = new StrikethroughSpan();
        spannableString.setSpan(colorSpan, 1, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        tagView.setText(spannableString);
    }
}
