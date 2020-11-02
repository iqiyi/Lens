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
package com.qiyi.lens.demo;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiyi.lens.ui.devicepanel.blockInfos.display.ICustomDisplay;

/**
 * 定制Lens 数据展示项：
 * KeyLog的信息会自动输出到 这个面板上。
 */
public class Display implements ICustomDisplay {
    private TextView textView;
    @Override
    public View createView(ViewGroup parent) {
        LinearLayout linearLayout = new LinearLayout(parent.getContext());
        linearLayout.setMinimumHeight(200);
        TextView textView = new TextView(parent.getContext());
        textView.setMinHeight(100);
        linearLayout.addView(textView);
        this.textView = textView;
        return linearLayout;
    }

    @Override
    public TextView getDisplay() {
        return textView;
    }

    @Override
    public String[] getFilterTags() {
        return new String[]{"uu"};
    }
}
