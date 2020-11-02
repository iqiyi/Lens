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

import android.content.Context;

import androidx.annotation.NonNull;

import com.qiyi.lens.ui.dns.DNSSetting;
import com.qiyi.lens.ui.exceptionPanel.CrashInterceptor;
import com.qiyi.lens.utils.configs.NetworkAnalyzeConfig;
import com.qiyi.lens.utils.iface.ICustomBlockFactory;
import com.qiyi.lens.utils.iface.IHookFrameWork;
import com.qiyi.lens.utils.iface.IJumpAction;
import com.qiyi.lens.utils.iface.IUIVerifyFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
    public static final String SP_KEY_DISPLAY_INFO = "display_info";
    public static final String SP_KEY_WATCH_INFO = "watch_info";
    public static final String SP_KEY_CRASH_INFO = "crash_info";
    public static final String SP_KEY_TEST_HOOK = "SP_KEY_TEST_HOOK";
    public static final String SP_KEY_FLOAT_WINDOW_MODE = "SP_KEY_FLOAT_WINDOW_MODE";
    public static final String SP_LENS_KEY_VIEW_TOUCH_ENABLE = "SP_LENS_KEY_VIEW_TOUCH_ENABLE";
    public static final String SP_LENS_KEY_PERMISSION_ENABLE = "SP_LENS_KEY_PERMISSION_ENABLE";
    public static final String SP_LENS_CAN_HOOK = "SP_LENS_can_hook";
    public static final String SP_LENS_REMOTE_URL = "SP_LENS_REMOTE_URL";

    //专用于启动时间的统计。
    public static final String LAUNCH_TIME_STAMP_NAME = "LAUNCH_TIME_STAMP_NAME";
    public static final String TIME_STAMP_NAME = "TIME_STAMP_NAME";

    private boolean isDeviceInfoEnabled;
    private boolean isFPSEnabled;
    private boolean isKeyLogEnabled;
    private boolean isLaunchTimeEnabled;
    private boolean isMemoryAnalyzeEnabled;
    private boolean isNetworkAnalyzeEnabled;
    private boolean isDisplayEnabled;
    private boolean isWatchEnabled;
    private boolean isCrashEnabled;
    private boolean isPermissionEnabled;
    private boolean isHookTestEnabled;

    private boolean activityAnalyzeEnable;
    private int enabledSize = 0;

    private static volatile LensConfig singleton = null;

    // 自定义模块的标签
    private String[] blockNames = null;
    private ICustomBlockFactory customBlockFactory = null;
    private String[] jumpNames = null;
    private IJumpAction jumpAction = null;
    private Class<? extends IUIVerifyFactory> uiVerifyFactory;
    private volatile Executor threadPool;
    private IHookFrameWork hookImpl = new DefaultHookImpl();

    public void setActivityAnalyzeEnable(boolean activityAnalyzeEnable) {
        this.activityAnalyzeEnable = activityAnalyzeEnable;
        if (activityAnalyzeEnable) {
            enabledSize++;
        }
    }

    public void setMemoryAnalyzeEnable(boolean memoryAnalyzeEnable) {
        this.isMemoryAnalyzeEnabled = memoryAnalyzeEnable;
    }

    public void setNetworkAnalyzeEnable(boolean networkAnalyzeEnable) {
        this.isNetworkAnalyzeEnabled = networkAnalyzeEnable;
        if (isNetworkAnalyzeEnabled) {
            enabledSize++;
            Context context = ApplicationLifecycle.getInstance().getContext();
            if (NetworkAnalyzeConfig.getInstance().isUrlGrabEnabled()) {
                SharedPreferenceUtils.setSharedPreferences(DNSSetting.SP_KEY_FILTER_ENABLE, true, context);
            }
            DNSSetting.loadConfiguration(context);

        }
    }

    public void setLaunchTimeEnabled(boolean launchTimeEnabled) {
        isLaunchTimeEnabled = launchTimeEnabled;
        if (launchTimeEnabled) {
            enabledSize++;
        }
    }

    public void setDisplayEnabled(boolean dispalyEnable) {
        isDisplayEnabled = dispalyEnable;
        if (dispalyEnable) {
            enabledSize++;
        }
    }

    public void setWatchEnable(boolean watchEnable) {
        isWatchEnabled = watchEnable;
        if (watchEnable) {
            enabledSize++;
        }
    }

    public void setHookTestEnabled(boolean hookTest) {
        isHookTestEnabled = hookTest;
    }


    public boolean isHookTestEnabled() {
        return isHookTestEnabled;
    }

    public void setDeviceInfoEnabled(boolean deviceInfoEnabled) {
        isDeviceInfoEnabled = deviceInfoEnabled;
        if (deviceInfoEnabled) {
            enabledSize++;
        }
    }

    public void setFPSEnabled(boolean FPSEnabled) {
        isFPSEnabled = FPSEnabled;
        if (FPSEnabled) {
            enabledSize++;
        }
    }

    public void setKeyLogEnabled(boolean keyLogEnabled) {
        isKeyLogEnabled = keyLogEnabled;
        if (isKeyLogEnabled) {
            if (hookImpl != null) {
                hookImpl.doHookDefault("com.qiyi.lens.hooks.LogcatHook");
            }
        } else {
            KeyLog.disable();
        }
    }

    public void setCrashEnabled(boolean crashEnabled) {
        isCrashEnabled = crashEnabled;
        if (!crashEnabled) {
            CrashInterceptor.uninstall();
        }
    }

    public void setPermissionEnable(boolean permissionEnabled) {
        isPermissionEnabled = permissionEnabled;
        if (permissionEnabled) {
            enabledSize++;
            HookUtil.hookPermission(true);
        }

    }


    public void setHookFrameWorkImpl(IHookFrameWork hookFrameWorkImpl) {
        hookImpl = hookFrameWorkImpl;
    }


    public void setUIVeryifyFactory(Class<? extends IUIVerifyFactory> factory) {
        uiVerifyFactory = factory;
    }


    public void setCustomBlockFactory(ICustomBlockFactory factory) {
        customBlockFactory = factory;
    }

    public void setCustomBlockNames(String[] names) {
        blockNames = names;
    }

    public void setJumpFactory(IJumpAction factory) {
        jumpAction = factory;
    }

    public void setCustomJumpNames(String[] names) {
        jumpNames = names;
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

    public boolean isPermissionEnabled() {
        return isPermissionEnabled;
    }

    public int getDisplayBlockSize() {
        return enabledSize;
    }

    public boolean isDisplayEnabled() {
        return isDisplayEnabled;
    }

    public boolean isWatchEnabled() {
        return isWatchEnabled;
    }

    public boolean isCrashEnabled() {
        return isCrashEnabled;
    }

    public ICustomBlockFactory getCustomBlockFactory() {
        return customBlockFactory;
    }

    public String[] getCustomBlockNames() {
        return blockNames;
    }

    public IJumpAction getCustomJumpAction() {
        return jumpAction;
    }

    public String[] getCustomJumpNames() {
        return jumpNames;
    }

    public @NonNull IHookFrameWork getHookFrameWorkImpl() {
        return hookImpl;
    }

    public IUIVerifyFactory getUIVeryfyFactory() {
        if (uiVerifyFactory != null) {
            //create new :
            try {
                return uiVerifyFactory.newInstance();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public boolean isUIVerifyEnabled() {
        return uiVerifyFactory != null;
    }


    public void setThreadPool(Executor executor) {
        threadPool = executor;
    }

    public Executor getThreadPool() {
        if (threadPool == null) {
            synchronized (this) {
                if (threadPool == null) {
                    threadPool = Executors.newFixedThreadPool(2);
                }
            }
        }
        return threadPool;
    }
}
