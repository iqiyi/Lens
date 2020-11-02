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
package com.qiyi.lens.ui.exceptionPanel;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiyi.lens.transfer.DataTransferManager;
import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.objectinfo.ObjectInfoPanel;
import com.qiyi.lens.utils.Utils;
import com.qiyi.lens.utils.iface.ObjectDescription;
import com.qiyi.lenssdk.R;

public class ExceptionPanel extends FullScreenPanel implements View.OnClickListener {
    private String displayInfo;
    private Object[] customValues;
    private ViewGroup container;

    public ExceptionPanel(FloatingPanel panel) {
        super(panel);
        setTitle(R.string.lens_exception_title);
    }


    public ExceptionPanel setData(Throwable throwable, Object... vars) {
        if (throwable != null) {
            displayInfo = Utils.throwable2String(throwable).toString();
        }
        customValues = vars;
        return this;
    }

    @Override
    protected View onCreateView(ViewGroup viewGroup) {
        View content = inflateView(R.layout.lens_exception_panel, viewGroup);
        if (displayInfo != null) {
            TextView textView = content.findViewById(R.id.detail_info);
            textView.setText(displayInfo);
        }
        TextView btnPush = content.findViewById(R.id.len_title_bar_operation);

        btnPush.setVisibility(View.VISIBLE);
        btnPush.setText("PUSH");
        btnPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataTransferManager.getInstance().push2Web(displayInfo);
            }
        });

        container = content.findViewById(R.id.lens_current_view_container);

        if (customValues != null) {
            int p = 0;
            for (Object var : customValues) {
                inflateObjectValueView(var, p++, R.layout.lens_item_object_ana_entrance);
            }
        }


        return content;
    }

    private void inflateObjectValueView(Object var, int index, int layoutId) {
        View group = inflateView(layoutId, container);

        TextView view = group.findViewById(R.id.panel_ac_info_activity);
        container.addView(group);
        view.setId(index);
        view.setOnClickListener(this);
        if (var instanceof ObjectDescription) {
            ObjectDescription description = (ObjectDescription) var;

            if (description.value != null) {
                if (description.objectDescription == null || description.objectDescription.length()
                        == 0) {
                    view.setText(description.value.getClass().getSimpleName());
                } else {
                    view.setText(description.toString());
                }
            }
        } else {
            String name = var.getClass().getSimpleName();
            if (name.length() < 2) {
                name = var.getClass().getName();
            }
            view.setText(name);
        }

    }

    @Override //[only dor view field jump ]
    public void onClick(View v) {
        if (customValues != null) {
            int id = v.getId();
            if (id >= 0 && id < customValues.length) {
                Object value = customValues[id];
                showObjectDetail(value);
            }
        }

    }


    private void showObjectDetail(Object value) {
        if (value instanceof ObjectDescription) {
            ObjectInfoPanel.showValue(((ObjectDescription) value).value, takePanel());
        } else {
            ObjectInfoPanel.showValue(value, takePanel());
        }

        dismiss();
    }
}
