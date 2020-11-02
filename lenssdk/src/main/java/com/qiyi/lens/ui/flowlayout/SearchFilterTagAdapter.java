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
package com.qiyi.lens.ui.flowlayout;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiyi.lens.utils.KeyLog;
import com.qiyi.lenssdk.R;

import java.util.List;

/**
 * Created by shenmingyan on 2017/11/13.
 * Log model for log filter
 */
public class SearchFilterTagAdapter extends TagAdapter<String> {
    private LayoutInflater inflater;


    public SearchFilterTagAdapter(List<String> data, Context context) {
        super(data);
        inflater = LayoutInflater.from(context);
    }


    @Override
    public View getView(FlowLayout parent, final int position, String searchSuggest) {
        View itemView = inflater.inflate(R.layout.lens_search_history_item, parent, false);
        TextView tvHistory = (TextView) itemView.findViewById(R.id.tv_content);
        ImageView ivDeleteIcon = (ImageView) itemView.findViewById(R.id.iv_delete_icon);
        FrameLayout itemLayout = (FrameLayout) itemView.findViewById(R.id.history_item);
        final String content = getData().get(position);
        tvHistory.setText(content);
        ivDeleteIcon.setVisibility(View.VISIBLE);
        itemLayout.setForegroundGravity(Gravity.RIGHT);
        TagDeleteListener tagDeleteListener = new TagDeleteListener(position, content);
        itemLayout.setOnClickListener(tagDeleteListener);
        ivDeleteIcon.setOnClickListener(tagDeleteListener);

        return itemView;
    }


    class TagDeleteListener implements View.OnClickListener {
        int position;
        String content;

        TagDeleteListener(int position, String content) {
            this.position = position;
            this.content = content;
        }

        @Override
        public void onClick(View v) {
            List<String> list = getData();

            if (list != null && list.size() > 0 && position < list.size()) {
                list.remove(position);
                refreshTagData(list);
                String[] ar = new String[list.size()];
                KeyLog.resetLog(list.toArray(ar));
            }
        }
    }

}
