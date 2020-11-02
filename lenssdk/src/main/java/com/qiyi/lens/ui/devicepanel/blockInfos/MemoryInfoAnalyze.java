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
package com.qiyi.lens.ui.devicepanel.blockInfos;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.qiyi.lens.ui.ActivityInfoPanel;
import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.event.DataCallBack;
import com.qiyi.lens.utils.event.EventBus;

/**
 * to display activity info : such as witch activity;
 * how many views inside
 * view levels
 */
public class MemoryInfoAnalyze extends AbsBlockInfo implements DataCallBack {
    private TextView textView;
    private Activity currentActivity;

    public MemoryInfoAnalyze(FloatingPanel panel) {
        super(panel);
    }

    @Override
    public void bind(View view) {
        textView = (TextView) view;
        textView.setTextSize(12);
        currentActivity = (Activity) DataPool.obtain().getDataAsset(DataPool.DATA_TYPE_ACTIVITY, String.class);
        //[fix bug : 热启动时候，stamp util 存在值 但是数据却需要刷新的情况]
        EventBus.registerEvent(this, DataPool.DATA_TYPE_ACTIVITY);
        setData();
    }

    @Override
    public void unBind() {
        textView = null;
        currentActivity = null;
        EventBus.unRegisterEvent(this, DataPool.DATA_TYPE_ACTIVITY);
    }

    @Override
    public void onDataArrived(Object data, int type) {
        if (data instanceof Activity) {
            currentActivity = (Activity) data;
            setData();
        }
    }

    private void setData() {
        if (textView != null && currentActivity != null) {
            textView.setText("当前界面：" + currentActivity.getClass().getName());
        }

    }

    @Override
    public void onBlockClicked() {
        FloatingPanel basePanel = getPanel();
        if (basePanel != null) {
            ActivityInfoPanel panel = new ActivityInfoPanel(basePanel);
            panel.show();
        }
    }

}