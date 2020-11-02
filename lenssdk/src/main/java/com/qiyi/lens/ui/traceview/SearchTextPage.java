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

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.CallSuper;

import com.qiyi.lens.ui.widget.DataViewLoader;
import com.qiyi.lens.utils.SearchBoxActions;
import com.qiyi.lenssdk.R;

/**
 * 搜索按钮，搜索后 高亮显示文字
 */
public abstract class SearchTextPage extends DataViewLoader {
    TimeStampInfo info;
    private TextView textView;// detail info
    private SearchBoxActions actions;
    String searchKey;
    private boolean dataChanged = true;


    public SearchTextPage(TimeStampInfo info) {
        this.info = info;
    }

    @Override
    @CallSuper
    protected void onViewCreated(View rootView) {
        textView = rootView.findViewById(getDescriptionTextViewLayout());
        actions = new SearchBoxActions((ViewGroup) rootView.findViewById(R.id.lens_seach_box_container)) {
            @Override
            public void onSearch(String text) {
                if (text != null) {
                    if (text.length() == 0) {
                        text = null;
                    } else {
                        text = text.toLowerCase();
                    }
                }
                dataChanged = keyChanged(searchKey, text);
                searchKey = text;
                loadSeachResult();
            }
        };

        loadSeachResult();
    }


    abstract CharSequence getDescription();

    int getDescriptionTextViewLayout() {
        return R.id.lens_task_info;
    }

    private void loadSeachResult() {
        if (dataChanged) {
            textView.setText(getDescription());
        }
    }

    @Override
    public int getLayoutRes() {
        return R.layout.lens_time_search_view;
    }


}
