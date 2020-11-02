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
package com.qiyi.lens.ui.abtest;

import android.graphics.Color;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.qiyi.lens.utils.ApplicationLifecycle;
import com.qiyi.lens.utils.UIUtils;
import com.qiyi.lens.utils.configs.ABNTestConfig;

/**
 * use in SP model & AB test model
 */
public class KeyDataAdapter extends BaseAdapter implements View.OnClickListener {

    private String[] keys;
    private KeyValueSubPanelView subPanelView;
    private int textViewPadding = 10;
    private int selectedIndex = -1;
    private ViewGroup mViewRoot;

    KeyDataAdapter(ViewGroup viewGroup) {
        mViewRoot = viewGroup;
        keys = ABNTestConfig.getInstance().getKeys();
        textViewPadding = UIUtils.dp2px(ApplicationLifecycle.getInstance().getContext(), 8);
    }

    @Override
    public int getCount() {
        return keys == null ? 0 : keys.length;
    }

    @Override
    public Object getItem(int position) {
        if (keys != null && position < getCount()) {
            return keys[position];
        }
        return "error";
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            TextView view = new TextView(parent.getContext());

            view.setGravity(Gravity.CENTER);
            view.setMaxLines(1);
            view.setPadding(0, textViewPadding, 0, textViewPadding);
            textView = view;
        } else {
            textView = (TextView) convertView;
        }

        if (selectedIndex == position) {
            textView.setTextColor(Color.RED);
            textView.setTextSize(18);
        } else {
            textView.setTextSize(16);
            textView.setTextColor(Color.BLACK);
        }


        //bind
        textView.setText(keys[position]);
        textView.setId(position);
        textView.setOnClickListener(this);
        return textView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id >= 0 && id < getCount()) {
            //[show panel]
            showSelector(id);
        }

    }

    public void showSelector(int index) {
        if(this.subPanelView == null){
            subPanelView = new KeyValueSubPanelView(mViewRoot);
        }

        String key = keys[index];
        selectedIndex = index;
        Value value = ABNTestConfig.getInstance().getValue(key);
        if (value != null) {
            subPanelView.showData(new Pair<String, Value>(key, value));
            notifyDataSetChanged();
        }
    }
}
