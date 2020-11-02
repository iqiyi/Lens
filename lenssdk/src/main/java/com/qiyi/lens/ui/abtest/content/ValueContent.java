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
package com.qiyi.lens.ui.abtest.content;

import android.view.View;
import android.view.ViewGroup;

import com.qiyi.lens.ui.abtest.KeyValueSubPanelView;
import com.qiyi.lens.ui.abtest.Value;

public abstract class ValueContent {
    Value _value;
    ViewGroup _parent;
    String _key;
    KeyValueSubPanelView _panelView;

    public ValueContent(ViewGroup parent, String key, Value value) {
        _value = value;
        _parent = parent;
        _key = key;
    }

    public void setPanel(KeyValueSubPanelView panel) {
        _panelView = panel;
    }

    public abstract void loadView();

    public void detachView() {
        if (_parent != null) {
            _parent.removeAllViews();
        }
    }


    /**
     * check if view can be reused
     */
    public boolean tryLoad(String key, Value value) {

        _key = key;
        boolean a = value.isSelectableValue();
        boolean b = _value.isSelectableValue();

        if (a == b) {
            _value = value;
            loadView();
            return true;
        } else {
            //[not same ]
            detachView();
            return false;
        }
    }

    protected int size() {
        return _value.size();
    }

    protected int getChildCount() {
        if (_parent != null) {
            return _parent.getChildCount();
        }
        return 0;
    }

    protected View getChildAt(int index) {
        if (_parent != null && index < _parent.getChildCount()) {
            return _parent.getChildAt(index);
        }
        return null;
    }

}
