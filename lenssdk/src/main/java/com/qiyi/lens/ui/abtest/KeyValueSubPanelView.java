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

import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import com.qiyi.lens.ui.abtest.content.EditTextContent;
import com.qiyi.lens.ui.abtest.content.RadioBoxContent;
import com.qiyi.lens.ui.abtest.content.ValueContent;
import com.qiyi.lens.ui.widget.SubPanelView;
import com.qiyi.lenssdk.R;

/*

 */

/**
 * 其实就两种编辑状
 * 1, 固定内容编辑； 选择index
 * 2， 自己edit 内容编辑；
 * <p>
 * sub panel to display all value candidates.
 */
public class KeyValueSubPanelView extends SubPanelView<Pair<String, Value>> {
    private ValueContent valueContent;
    private String _key;

    public KeyValueSubPanelView(ViewGroup root) {
        super(root);
    }


    //[attach content view into parent]
    public void showData(Pair<String, Value> entry) {
        String key = entry.first;
        _key = key;
        super.showData(entry);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.lens_sub_panel_close_btn) {
            dismiss();
        }

    }


    @Override
    protected void loadContentView(Pair<String, Value> value) {
        Value data = value.second;
        loadContentView(data);
    }

    //[viewContainer as parent to load view]
    private void loadContentView(Value value) {

        if (valueContent != null) {
            if (!valueContent.tryLoad(_key, value)) {
                valueContent = null;
            }
        }
        if (valueContent == null) {
            if (value.isSelectableValue()) {
                valueContent = new RadioBoxContent(getPanelRoot(), _key, value);
            } else {
                valueContent = new EditTextContent(getPanelRoot(), _key, value);
                valueContent.setPanel(this);
            }
        }
        valueContent.loadView();
    }



}
