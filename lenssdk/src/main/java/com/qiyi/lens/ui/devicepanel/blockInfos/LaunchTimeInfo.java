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

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.traceview.LaunchTimeDetailPanel;
import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.TimeStampUtil;
import com.qiyi.lens.utils.event.DataCallBack;
import com.qiyi.lens.utils.event.EventBus;
import com.qiyi.lenssdk.R;

import java.util.UUID;

public class LaunchTimeInfo extends AbsBlockInfo implements DataCallBack {

    private TextView mLaunchTimeTv;
    private Button mMoreBtn;
    private TimeStampUtil stampUtil;

    public LaunchTimeInfo(FloatingPanel panel) {
        super(panel);
    }

    @Override
    public void bind(View view) {
        stampUtil = (TimeStampUtil) DataPool.obtain().getDataAsset(DataPool.DATA_TYPE_LAUNCH_TIME,
                TimeStampUtil.class);
        //[fix bug : 热启动时候，stamp util 存在值 但是数据却需要刷新的情况]
        EventBus.registerEvent(this, DataPool.DATA_TYPE_LAUNCH_TIME);
        setData();
    }

    @Override
    public void unBind() {
        mLaunchTimeTv = null;
        EventBus.unRegisterEvent(this, DataPool.DATA_TYPE_LAUNCH_TIME);
    }

    @Override
    public void onDataArrived(Object data, int type) {
        if (data instanceof TimeStampUtil) {
            stampUtil = (TimeStampUtil) data;
            setData();
        }
    }

    private void setData() {
        if (mLaunchTimeTv != null && stampUtil != null) {
            String launchTime = String.format(mLaunchTimeTv.getContext().getString(R.string.lens_block_launch_info),
                    String.valueOf(stampUtil.getTotalTime()));
            mLaunchTimeTv.setText(launchTime);
        }
    }

    @Override
    public void onBlockClicked() {
        FloatingPanel basePanel = getPanel();
        if (basePanel != null) {
            LaunchTimeDetailPanel panel = new LaunchTimeDetailPanel(basePanel);
            panel.show();
        }
    }

    @Override
    public View createView(ViewGroup parent) {
        View root = inflateView(parent, R.layout.lens_block_launch_info);
        mLaunchTimeTv = root.findViewById(R.id.tv_block_launch_info);
        bindBlockClickEvent(mLaunchTimeTv);
        final Switch switchBtn = root.findViewById(R.id.lens_start_analysis);
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!switchBtn.isSelected()) {
                    switchBtn.setText("分析中");
                    TimeStampUtil.setDefaultStampKey(UUID.randomUUID().toString());
                    TimeStampUtil.obtain();
                } else {
                    switchBtn.setText("开始分析");
                    TimeStampUtil.obtain().stopAndPost();
                }
                switchBtn.setSelected(!switchBtn.isSelected());
            }
        });
        return root;
    }

}
