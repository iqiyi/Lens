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
package com.qiyi.lens.ui.permission;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.utils.LocalLinkMovementMethod;
import com.qiyi.lens.utils.event.DataCallBack;
import com.qiyi.lens.utils.event.EventBus;
import com.qiyi.lenssdk.R;

public class PermissionPanel extends FullScreenPanel implements DataCallBack {
    private TextView display;

    public PermissionPanel(FloatingPanel panel) {
        super(panel);
    }

    @Override
    protected View onCreateView(ViewGroup viewGroup) {
        View root = inflateView(R.layout.lens_permission_panel, viewGroup);
        display = root.findViewById(R.id.permission_request_display);
        display.setMovementMethod(LocalLinkMovementMethod.getInstance());
        String data = (String) DataPool.obtain().getData(DataPool.DATA_PERMISSION_REQUEST);
        display.setText(data);
        return root;
    }

    @Override
    public void onShow() {
        super.onShow();
        EventBus.registerEvent(this, DataPool.DATA_PERMISSION_REQUEST);

    }

    public void onDismiss() {
        EventBus.unRegisterEvent(this, DataPool.DATA_PERMISSION_REQUEST);
    }

    @Override
    public void onDataArrived(Object data, int type) {
        if (type == DataPool.DATA_PERMISSION_REQUEST) {
            display.setText((String) data);
        }
    }
}
