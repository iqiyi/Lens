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
package com.qiyi.lens.ui.devicepanel;

import android.view.View;
import android.view.ViewGroup;

import com.qiyi.lens.ui.BasePanel;
import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.WatchObjInfo;
import com.qiyi.lens.ui.devicepanel.blockInfos.AbsBlockInfo;
import com.qiyi.lens.ui.devicepanel.blockInfos.ActivityInfo;
import com.qiyi.lens.ui.devicepanel.blockInfos.DeviceInfo;
import com.qiyi.lens.ui.devicepanel.blockInfos.LaunchTimeInfo;
import com.qiyi.lens.ui.devicepanel.blockInfos.MemoryInfoAnalyze;
import com.qiyi.lens.ui.devicepanel.blockInfos.NetworkInfo;
import com.qiyi.lens.ui.devicepanel.blockInfos.PermissionInfo;
import com.qiyi.lens.ui.devicepanel.blockInfos.display.DisplayInfo;

public class InfoManager {
    private LensConfig config;
    private AbsBlockInfo[] blocks;

    public InfoManager() {
        config = LensConfig.getInstance();
    }

    private AbsBlockInfo[] getBlockInfos(FloatingPanel panel) {
        int size = config.getDisplayBlockSize();
        AbsBlockInfo[] infos = new AbsBlockInfo[size];
        int i = 0;
        if (config.isDeviceInfoEnabled()) {
            infos[i++] = new DeviceInfo(panel);
        }

//        if (config.isFPSInfoEnabled()) {
//            infos[i++] = new FrameInfo(panel);
//        }

//        if (config.isKeyLogEnabled()) {
        //infos[i++] = new LogInfo(panel);
//        }

        if (config.isLaunchTimeEnabled()) {
            infos[i++] = new LaunchTimeInfo(panel);

        }

        if (config.isNetworkAnalyzeEnabled()) {
            infos[i++] = new NetworkInfo(panel);
        }

        if (config.isActivityAnalyzeEnabled()) {
            infos[i++] = new ActivityInfo(panel);
        }

        if (config.isMemoryAnalyzeEnabled()) {
            infos[i++] = new MemoryInfoAnalyze(panel);
        }

        if (config.isDisplayEnabled()) {
            infos[i++] = new DisplayInfo(panel);
        }

        if (config.isWatchEnabled()) {
            infos[i++] = new WatchObjInfo(panel);
        }

        if (config.isPermissionEnabled()) {
            infos[i++] = new PermissionInfo(panel);
        }
        return infos;
    }

    private ViewGroup mLayout;
    private BasePanel mBasePanel;

    void bindViews(ViewGroup group, FloatingPanel panel) {
        mLayout = group;
        mBasePanel = panel;
        AbsBlockInfo[] infos = getBlockInfos(panel);
        int len = infos.length;
        for (AbsBlockInfo info : infos) {
            if (info == null) continue;

            View view = info.createView(group);
            if (view != null) {
                addView(group, view);
                info.bind(view);
            }
        }
        this.blocks = infos;
    }

    private void addView(ViewGroup viewGroup, View view) {
        viewGroup.addView(view);
    }

    void removeViews() {
        if (mLayout != null) {
            mLayout.removeAllViews();
        }
    }

    public void unbind() {
        if (blocks != null) {
            for (AbsBlockInfo block : blocks) {
                if (block != null) {
                    block.unBind();
                }
            }
        }

    }


}
