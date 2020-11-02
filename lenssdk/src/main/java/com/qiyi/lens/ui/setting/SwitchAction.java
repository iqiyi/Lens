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
package com.qiyi.lens.ui.setting;

import android.content.Context;

import com.qiyi.lens.dump.IDebugStatusChanged;
import com.qiyi.lens.dump.LogDumperHolder;
import com.qiyi.lens.ui.dns.DNSSetting;
import com.qiyi.lens.utils.ApplicationLifecycle;
import com.qiyi.lens.utils.HookUtil;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.SharedPreferenceUtils;
import com.qiyi.lens.utils.Utils;
import com.qiyi.lens.utils.iface.ICustomBlockFactory;
import com.qiyi.lens.utils.iface.ISwitchAction;
import com.qiyi.lenssdk.R;

public class SwitchAction implements ISwitchAction {
    private Context context;
    boolean switchChanged;
    private ICustomBlockFactory factory;

    SwitchAction() {
        context = ApplicationLifecycle.getInstance().getContext();
        factory = LensConfig.getInstance().getCustomBlockFactory();
    }

    private Context getContext() {
        return context;
    }


    private void handleEvent(int vid, boolean selected) {
//        boolean selected;
        if (vid == R.string.lens_block_display) { // 数据展示
//            selected = updateViewSP(view, LensConfig.SP_KEY_DISPLAY_INFO);
            LensConfig.getInstance().setDisplayEnabled(selected);
        } else if (vid == R.string.lens_block_device_info_title) {// 设备信息
//            selected = updateViewSP(view, LensConfig.SP_KEY_DEVICE_INFO);
            LensConfig.getInstance().setDeviceInfoEnabled(selected);
        } else if (vid == R.string.lens_block_frame_title) {// 帧率信息
//            selected = updateViewSP(view, LensConfig.SP_KEY_FRAME_INFO);
            LensConfig.getInstance().setFPSEnabled(selected);
        } else if (vid == R.string.lens_block_log_title) {
//            logInfoNewStatus = selected = updateViewSP(view, LensConfig.SP_KEY_LOG_INFO);
            LensConfig.getInstance().setKeyLogEnabled(selected);
        } else if (vid == R.string.lens_block_launch_title) {
//            selected = updateViewSP(view, LensConfig.SP_KEY_LAUNCH_INFO);
            LensConfig.getInstance().setLaunchTimeEnabled(selected);
        } else if (vid == R.string.lens_block_network_title) {
//            selected = updateViewSP(view, LensConfig.SP_KEY_NETWORK_INFO);
            LensConfig.getInstance().setNetworkAnalyzeEnable(selected);
        } else if (vid == R.string.lens_block_activity_title) {
//            selected = updateViewSP(view, LensConfig.SP_KEY_ACTIVITY_INFO);
            LensConfig.getInstance().setActivityAnalyzeEnable(selected);
            SharedPreferenceUtils.setSharedPreferences(DNSSetting.SP_KEY_FILTER_ENABLE,
                    selected, getContext());
            if (selected) {
                DNSSetting.enableFilterSetting(getContext());
            } else {
                DNSSetting.disableFilterSetting();
            }
        } else if (vid == R.string.lens_block_watch) {
//            selected = updateViewSP(view, LensConfig.SP_KEY_WATCH_INFO);
            LensConfig.getInstance().setWatchEnable(selected);
        } else if (vid == R.string.lens_crash_info) {
//            selected = updateViewSP(view, LensConfig.SP_KEY_CRASH_INFO);
            LensConfig.getInstance().setCrashEnabled(selected);
        } else if (vid == R.string.lens_block_view_touch) {
//            selected = updateViewSP(view, LensConfig.SP_LENS_KEY_VIEW_TOUCH_ENABLE);
            HookUtil.hookViewTouch(selected);
        } else if (vid == R.string.lens_block_enable_debug) {
//            view.setSelected(!view.isSelected());
            IDebugStatusChanged debugStatusChanged = LogDumperHolder.getDebugStatusChanged();
            if (debugStatusChanged != null) {
                debugStatusChanged.onDebugChanged(selected);
            }
        } else if (vid == R.string.lens_block_permission_title) {
//            selected = updateViewSP(view, LensConfig.SP_LENS_KEY_PERMISSION_ENABLE);
            LensConfig.getInstance().setPermissionEnable(selected);
        } else if (vid == R.string.lens_test_hook) {
            HookUtil.enableHookTestHooks(selected);
        } else if (vid == R.string.lens_block_floating_pannel) {
            //  申请浮动窗口权限，并展示浮动窗口
            //todo clf
            if (!Utils.hasFloatingWindowPermission()) {
                Utils.requestFloatingPermission();
                // when activity on resume, will check for permission , then do sth
            }
        }
    }

    @Override
    public void onSwitchChange(String key, int ventId, boolean selected) {
        switchChanged = true;
        if (factory != null && factory.onBlockSwitchChange(key, selected)) {
            return;
        }
        handleEvent(ventId, selected);
    }
}
