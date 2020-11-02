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

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lenssdk.R;

/**
 * 页面设计& 交互流程
 * 1，进入页面展示全部的key；
 * if(has more than one : show select list ;
 * else show result panel;
 * panel can be closed;
 * <p>
 * If AB test is configured,  the AB test entrance will be shown on the floating panel;
 * Click the entrance , this panel will show.
 */
public class ABNTestPanel extends FullScreenPanel {

    public ABNTestPanel(FloatingPanel panel) {
        super(panel);
        setTitle("AB Test");
    }

    @Override
    public View onCreateView(ViewGroup group) {
        return inflateView(R.layout.lens_ab_test_panel, group);
    }

    @Override
    public void onViewCreated(View root) {
        super.onViewCreated(root);
        ListView listView = root.findViewById(R.id.lens_ab_test_panel_list);
//        ViewGroup group = root.findViewById(R.id.lens_ab_test_edit_sub_panel);

        KeyDataAdapter adapter = new KeyDataAdapter((ViewGroup) getDecorView());
        listView.setAdapter(adapter);
        if (adapter.getCount() == 1) {
            adapter.showSelector(0);
        }
    }

}
