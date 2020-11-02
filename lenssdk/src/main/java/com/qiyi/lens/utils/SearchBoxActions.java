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
package com.qiyi.lens.utils;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.qiyi.lenssdk.R;


//对searchBox layout 的封装
public abstract class SearchBoxActions implements View.OnClickListener {
    private View searchAction;
    private View clearAction;
    private EditText editText;
    private Handler handler;

    private Runnable doSearch = new Runnable() {
        @Override
        public void run() {
            String var = getEditText();
            onSearch(var);
        }
    };

    public SearchBoxActions(ViewGroup searchViewBox) {
        handler = new Handler(Looper.getMainLooper());
        searchAction = searchViewBox.findViewById(R.id.search_box_action_search);
        clearAction = searchViewBox.findViewById(R.id.search_box_clear_search_text);
        editText = searchViewBox.findViewById(R.id.search_box_edit_text);
        searchAction.setOnClickListener(this);
        clearAction.setOnClickListener(this);
        editText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleTextChange();
            }
        });
    }

    public abstract void onSearch(String text);

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.search_box_action_search) {
            handler.removeCallbacks(doSearch);
            String var = getEditText();
            onSearch(var);
        } else if (id == R.id.search_box_clear_search_text) {
            handler.removeCallbacks(doSearch);
            editText.setText("");
            onSearch(null);
        }
    }

    private void handleTextChange() {
        handler.removeCallbacks(doSearch);
        handler.postDelayed(doSearch, 1000);
    }

    private String getEditText() {
        if (editText != null) {
            return editText.getText().toString();
        }
        return null;
    }
}
