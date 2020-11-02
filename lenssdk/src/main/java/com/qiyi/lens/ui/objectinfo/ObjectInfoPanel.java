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
package com.qiyi.lens.ui.objectinfo;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.qiyi.lens.Constants;
import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.PanelManager;
import com.qiyi.lens.utils.configs.DebugInfoConfig;
import com.qiyi.lens.utils.reflect.FieldInfo;
import com.qiyi.lenssdk.R;

public class ObjectInfoPanel extends FullScreenPanel implements TextWatcher, View.OnClickListener {
    private boolean showAllFields;

    private EditText etFilter;
    private TextView swIncludeInheritance;
    private Object object;
    private ObjectFieldListAdapter adapter;
    private View addToWatchList;

    public ObjectInfoPanel(FloatingPanel panel, Object object) {
        super(panel);
        this.object = object;
        adapter = new ObjectFieldListAdapter(object);
    }

    @Override
    protected View onCreateView(ViewGroup viewGroup) {
        View content = inflateView(R.layout.lens_object_info_panel, viewGroup);
        RecyclerView recyclerView = content.findViewById(R.id.rv_field_info);
        recyclerView.setLayoutManager(new LinearLayoutManager(viewGroup.getContext()));
        recyclerView.setAdapter(adapter);
        TextView tvFieldTitle = content.findViewById(R.id.tv_field_title);

        if (object instanceof View) {
            tvFieldTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.lens_right_arrow, 0);
            tvFieldTitle.setBackgroundResource(R.drawable.lens_round_rect_light);
            tvFieldTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FieldEditPanel fieldEditPanel = new FieldEditPanel(null);
                    FieldInfo info = new FieldInfo(object, null, null);
                    fieldEditPanel.setData(null, info);
                    fieldEditPanel.show();
                }
            });
        }


        recyclerView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                return true;
            }
        });
        String title = null;
        if (object != null) {
            title = object.getClass().getName();
        }
        tvFieldTitle.setText(title);
        etFilter = content.findViewById(R.id.et_filter);
        etFilter.addTextChangedListener(this);
        swIncludeInheritance = content.findViewById(R.id.sw_include_inheritance);
        swIncludeInheritance.setOnClickListener(this);
        addToWatchList = content.findViewById(R.id.view_add_to_watch_list);
        if (DebugInfoConfig.getInstance().hasWatched(object.getClass().getName(), object)) {
            addToWatchList.setVisibility(View.GONE);
        } else {
            addToWatchList.setOnClickListener(this);
        }
        return content;
    }

    @Override
    public void onShow() {
        super.onShow();
        PanelManager.getInstance().removePanel(Constants.PANEL_SELECT_VIEW_PANEL);
        showAllFields = !FieldInfo.classFilter(
                object == null ? null : object.getClass().getName());
        refreshFieldData("", showAllFields);
        swIncludeInheritance.setActivated(showAllFields);
    }

    private void refreshFieldData(String keywords, boolean showAllFields) {
        adapter.refreshData(keywords, showAllFields);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        refreshFieldData(s.toString(), showAllFields);
    }

    public static void showValue(Object value, FloatingPanel panel) {
        ObjectInfoPanel objectInfoPanel = new ObjectInfoPanel(panel, value);
        objectInfoPanel.show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sw_include_inheritance) {
            showAllFields = !showAllFields;
            swIncludeInheritance.setActivated(showAllFields);
            refreshFieldData(etFilter.getText().toString(), showAllFields);
        } else if (v.getId() == R.id.view_add_to_watch_list) {
            DebugInfoConfig.getInstance().watchObject(object.getClass().getName(), object);
            addToWatchList.setVisibility(View.GONE);
        }
    }
}
