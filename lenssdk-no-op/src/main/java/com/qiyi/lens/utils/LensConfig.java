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
package com.qiyi.lens.utils;

import com.qiyi.lens.ConfigHolder;
import com.qiyi.lens.utils.iface.IHookFrameWork;
import com.qiyi.lens.utils.iface.IUIVerifyFactory;

/**
 * debug 信息配置项目
 */
public class LensConfig {

    public static final int DEVICE_INFO = 1;
    public static final int FPS_INFO = 2;
    public static final int LOG_INFO = 3;
    public static final int LAUNCH_TIME_INFO = 4;

    //Lens setting SharedPreferences key
    public static final String SP_KEY_DEVICE_INFO = "device_info";
    public static final String SP_KEY_FRAME_INFO = "frame_info";
    public static final String SP_KEY_LOG_INFO = "log_info";
    public static final String SP_KEY_NETWORK_INFO = "network_info";
    public static final String SP_KEY_LAUNCH_INFO = "launch_info";
    public static final String SP_KEY_ACTIVITY_INFO = "activity_info";
    public static final String SP_KEY_VIEW_INFO = "view_info";
    public static final String SP_KEY_FIRST_SHOW_PANEL = "first_show_panel";
    public static final String SP_KEY_PANEL_STATUS = "panel_status";

    public static final String LAUNCH_TIME_STAMP_NAME = "LAUNCH_TIME_STAMP_NAME";
    public static final String TIME_STAMP_NAME = "TIME_STAMP_NAME";

    private boolean isDeviceInfoEnabled;
    private boolean isFPSEnabled;
    private boolean isKeyLogEnabled;
    private boolean isLaunchTimeEnabled;
    private boolean isMemoryAnalyzeEnabled;
    private boolean isNetworkAnalyzeEnabled;

    private boolean activityAnalyzeEnable;
    private int enabledSize = 0;

    private static volatile LensConfig singleton = null;

    public void setActivityAnalyzeEnable(boolean activityAnalyzeEnable) {

    }

    public void setMemoryAnalyzeEnable(boolean memoryAnalyzeEnable) {
    }

    public void setNetworkAnalyzeEnable(boolean networkAnalyzeEnable) {

    }

    public void setLaunchTimeEnabled(boolean launchTimeEnabled) {

    }

    public void setDeviceInfoEnabled(boolean deviceInfoEnabled) {

    }

    public void setFPSEnabled(boolean FPSEnabled) {
    }

    public void setKeyLogEnabled(boolean keyLogEnabled) {
    }

    private LensConfig() {
    }

    public static LensConfig getInstance() {
        if (singleton == null) {
            synchronized (LensConfig.class) {
                if (singleton == null) {
                    singleton = new LensConfig();
                }
            }
        }
        return singleton;
    }

    public boolean isMemoryAnalyzeEnabled() {
        return isMemoryAnalyzeEnabled;
    }

    public boolean isNetworkAnalyzeEnabled() {
        return isNetworkAnalyzeEnabled;
    }

    public boolean isDeviceInfoEnabled() {
        return isDeviceInfoEnabled;
    }

    public boolean isFPSInfoEnabled() {
        return isFPSEnabled;
    }

    public boolean isKeyLogEnabled() {
        return isKeyLogEnabled;
    }

    public boolean isLaunchTimeEnabled() {
        return isLaunchTimeEnabled;
    }

    public boolean isActivityAnalyzeEnabled() {
        return activityAnalyzeEnable;
    }

    public int getDisplayBlockSize() {
        return enabledSize;
    }
    public void setUIVeryifyFactory(Class<? extends IUIVerifyFactory> factory) {
        ConfigHolder.uiVerifyClass = factory;
    }
    public void setHookFrameWorkImpl(IHookFrameWork hookFrameWorkImpl) {
    }
    public IHookFrameWork getHookFrameWorkImpl(){
        return null;
    }

}
