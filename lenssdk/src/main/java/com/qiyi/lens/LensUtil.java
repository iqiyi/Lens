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
package com.qiyi.lens;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.qiyi.lens.dump.IDebugStatusChanged;
import com.qiyi.lens.dump.ILogDumperFactory;
import com.qiyi.lens.dump.LogDumperHolder;
import com.qiyi.lens.transfer.DataTransferManager;
import com.qiyi.lens.transfer.IRemoteBinder;
import com.qiyi.lens.transfer.IReporter;
import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.exceptionPanel.CrashInterceptor;
import com.qiyi.lens.ui.exceptionPanel.ExceptionPanel;
import com.qiyi.lens.ui.viewinfo.IViewClickHandle;
import com.qiyi.lens.utils.ApplicationLifecycle;
import com.qiyi.lens.utils.HookUtil;
import com.qiyi.lens.utils.KeyLog;
import com.qiyi.lens.utils.KeyLogConfig;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.SharedPreferenceUtils;
import com.qiyi.lens.utils.configs.ABNTestConfig;
import com.qiyi.lens.utils.configs.ActivityInfoConfig;
import com.qiyi.lens.utils.configs.DebugInfoConfig;
import com.qiyi.lens.utils.configs.DisplayConfiguration;
import com.qiyi.lens.utils.configs.NetworkAnalyzeConfig;
import com.qiyi.lens.utils.configs.ViewInfoConfig;
import com.qiyi.lens.utils.iface.ICustomBlockFactory;
import com.qiyi.lens.utils.iface.IFragmentHandle;
import com.qiyi.lens.utils.iface.IHookFrameWork;
import com.qiyi.lens.utils.iface.IJumpAction;
import com.qiyi.lens.utils.iface.IObjectDescriptor;
import com.qiyi.lens.utils.iface.IUIVerifyFactory;
import com.qiyi.lens.utils.iface.IViewInfoHandle;
import com.qiyi.lenssdk.R;

/**
 * lens 封裝對外輸出能力集 入口
 * 0.9.9.12 :  新增支持本地插件方式； release 版本不支持自定义插件方式。
 */
public class LensUtil {
    final static String TAG = "LensUtil";


    public static ConfigBuilder buildConfig() {
        return new ConfigBuilder();
    }

    /**
     * 统计滑动时候的平均帧率
     */
    public static void onFPSScrollStarted() {
        //读取帧率存入帧率数组
//        LensAnalysis.fpsScrollOn(true);
    }

    public static void download(Context context, String url, String version) {
        // no-op implementation only
    }

    @Deprecated
    private static void checkFloatingPermission(final Context context) {
        // 不能删除，保持 no-op 与 sdk 一致
    }


    //SDK 直接依賴的情況下， 在APP 某個入口直接拉起lens
    public static void showManually(Context context) {
        SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_PANEL_STATUS,
                ApplicationLifecycle.PANEL_STATE_MIN,
                context);
        ApplicationLifecycle.getInstance().show();
    }

    // keep
    private static void quickStart(Context context) {
    }

    public static void watchObject(Object object) {
        DebugInfoConfig.getInstance().watchObject(object);
    }

    public static void watchObject(String name, Object object) {
        DebugInfoConfig.getInstance().watchObject(name, object);
    }


    public static void watchField(String fieldName, Object src) {
        DebugInfoConfig.getInstance().watchField(fieldName, src);
    }

    public static void setViewClickDebugHandle(Class<? extends IViewClickHandle> viewClickHandle) {
        DebugInfoConfig.getInstance().setViewClickhandle(viewClickHandle);
    }

    public static void setDefaultObjectDescriptor(IObjectDescriptor descriptor) {
        DebugInfoConfig.getInstance().setDefaultObjectDescriptor(descriptor);
    }

    public static void setFragmentHandler(Class<? extends IFragmentHandle> fragmentHandler) {
        ActivityInfoConfig.getInstance().setFragmentHandler(fragmentHandler);
    }

    public static void setViewInfoHandler(Class<? extends IViewInfoHandle> fragmentHandler) {
        ViewInfoConfig.getInstance().setViewInfoHandler(fragmentHandler);
    }

    /**
     * default setting must be called before other settings
     */
    public static class ConfigBuilder {

        boolean isDefaultState = false;
        KeyLogConfig mLogConfig;
        boolean isDefaultSet = false;
        boolean isBuilded;

        //[blcokes register]
        boolean keyLogEnabled;
        boolean deviceInfoEnabled;
        boolean fpsEnabled;
        boolean launchTimeEnabled;
        boolean activityAnalyzeEnable;
        boolean networkAnalyzeEnable;
        boolean viewInfoEnable;
        boolean displayEnable;
        boolean watchEnable;
        boolean crashEnable;
        boolean viewTouchLog;
        boolean permissionEnable;
        boolean hookTest;
        //end]
        LensConfig config = LensConfig.getInstance();

        boolean isPluginMode;//[lens 是否以插件方式还在hook 框架]
        //  是否可以打开hook 框架： 如果之前崩溃过，会根据SP 重大额值来限定 禁止开启hook 功能；
        boolean shouldEnableHook;
        String customHookPluginPath;
        IHookFrameWork hookImpl;


        private void checkDefault() {
            if (!isDefaultSet) {
                keyLogEnabled = isDefaultState;
                deviceInfoEnabled = isDefaultState;
                fpsEnabled = isDefaultState;
                launchTimeEnabled = isDefaultState;
                activityAnalyzeEnable = isDefaultState;
                networkAnalyzeEnable = isDefaultState;
                displayEnable = isDefaultSet;
                watchEnable = isDefaultSet;
                hookTest = isDefaultSet;
                isDefaultSet = true;
            }
        }

        public ConfigBuilder defaultOpen(boolean enableAll) {
            isDefaultState = enableAll;

            checkDefault();
            return this;
        }

        public ConfigBuilder enableKeyLog(boolean enable) {
            checkDefault();
            this.keyLogEnabled = enable;
            return this;
        }

        public ConfigBuilder enableKeyLog(KeyLogConfig logConfig) {
            checkDefault();
            this.keyLogEnabled = true;
            mLogConfig = logConfig;
            return this;
        }

        public ConfigBuilder enableFPS(boolean enable) {
            checkDefault();
            fpsEnabled = enable;
            return this;
        }

        public ConfigBuilder enableHookTest(boolean enable) {
            checkDefault();
            hookTest = enable;
            return this;
        }

        public ConfigBuilder enableNetworkAnalyze(boolean enable) {
            checkDefault();
            networkAnalyzeEnable = enable;
            return this;
        }

        public ConfigBuilder enableDeviceInfo(boolean enable) {
            checkDefault();
            deviceInfoEnabled = enable;
            return this;
        }

        public ConfigBuilder enableDisplayInfo(boolean enable) {
            checkDefault();
            displayEnable = enable;
            return this;
        }

        public ConfigBuilder enableLaunchTime(boolean enable) {
            checkDefault();
            launchTimeEnabled = enable;
            return this;
        }

        public ConfigBuilder enableActivityAnalyzer(boolean enable) {
            checkDefault();
            activityAnalyzeEnable = enable;
            return this;
        }

        public ConfigBuilder enableViewInfo(boolean enable) {
            checkDefault();
            viewInfoEnable = enable;
            return this;
        }


        /**
         * 设置hook插件地址
         */
        public ConfigBuilder addCustomHookPluginPath(String path) {
            // todo here : add path
            customHookPluginPath = path;
            return this;
        }


        public ConfigBuilder addCustomJumpEntrance(String[] customNames, IJumpAction action) {
            config.setJumpFactory(action);
            config.setCustomJumpNames(customNames);
            return this;
        }


        public ConfigBuilder addCustomBlockEntrance(String[] blockKeys, ICustomBlockFactory blockFactory) {
            config.setCustomBlockNames(blockKeys);
            config.setCustomBlockFactory(blockFactory);
            return this;
        }

        public ConfigBuilder enableCrashInfo(boolean enable) {
            checkDefault();
            crashEnable = enable;
            return this;
        }

        public ConfigBuilder enablePermission(boolean enable) {
            checkDefault();
            permissionEnable = enable;
            return this;
        }

        public ConfigBuilder initAsPluginMode(boolean plugin) {
            isPluginMode = plugin;
            return this;
        }

        public ConfigBuilder setHookFrameWorkImpl(IHookFrameWork frameWorkImpl) {
            hookImpl = frameWorkImpl;
            return this;
        }

        private void buildHookPlugin(Context context) {
            String cacheDir = context.getFilesDir().toString() + "/opt";
            String pluginFile = context.getFilesDir().toString() + "/lensplug/hookplug.apk";
            hookImpl.setHookPluginInfo(context, cacheDir, pluginFile);
            hookImpl.usePluginMode(isPluginMode);
            config.setHookFrameWorkImpl(hookImpl);
            if (shouldEnableHook) {
                if (launchTimeEnabled) {
                    HookUtil.hookTaskInfo();
                }
                HookUtil.hookViewTouch(viewTouchLog);
                HookUtil.enableHookTestHooks(hookTest);
            }
        }

        public ConfigBuilder build(Context context) {
            isBuilded = true;

            CrashInterceptor.install(crashEnable, launchTimeEnabled);
            if (hookImpl != null) {
                buildHookPlugin(context);
            }

//            LensConfig config = LensConfig.getInstance();
            config.setDeviceInfoEnabled(deviceInfoEnabled);
            config.setFPSEnabled(fpsEnabled);
            config.setNetworkAnalyzeEnable(networkAnalyzeEnable);
            config.setKeyLogEnabled(keyLogEnabled);
            config.setLaunchTimeEnabled(launchTimeEnabled);
            config.setActivityAnalyzeEnable(activityAnalyzeEnable);
            config.setDisplayEnabled(displayEnable);
            config.setWatchEnable(watchEnable);
            config.setHookTestEnabled(hookTest);
            config.setCrashEnabled(crashEnable);
            config.setPermissionEnable(permissionEnable);


            if (mLogConfig == null) {
                mLogConfig = new KeyLogConfig().setMaxLine(100);
            }

            KeyLog.config(mLogConfig);
            KeyLog.getKeyLogInstance().initFilterFilePath(context);
            return this;
        }


        public void forceShow(Context con, int width, int state) {

            Activity activity;
            try {
                activity = (Activity) con;
            } catch (ClassCastException e) {
                activity = null;
            }
            show(Lens.wrapContext(QuickStart.getBaseContext(con)), width, state, activity);
        }

        public void show(Context context, int width, int state) {
            show(context, width, state, null);
        }

        /**
         * @param state : 0: show hiden ; 1 : show ; 2 : closed
         *              closed state is currently not supported
         */
        private void show(Context context, int width, int state, Activity activity) {


//            readSharedPreferenceConfig(context);//根据setting初始化设置
            // 可能在 lens-no-op showManually 时设置了 value，需要继承过来
            shouldEnableHook = hookImpl != null && SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_LENS_CAN_HOOK, context, true);

            state = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_PANEL_STATUS, context, state);
            boolean firstIn = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_FIRST_SHOW_PANEL, context, true);
            if (firstIn) {
                SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_FIRST_SHOW_PANEL, false, context);
                initSettings(context);
                SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_PANEL_STATUS, state, context);
            } else {
                readSharedPreferenceConfig(context);
                state = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_PANEL_STATUS, context, state);
            }
            ApplicationLifecycle applicationLifecycle = ApplicationLifecycle.create(context);
            if (!isBuilded) {
                build(context);
            }
            applicationLifecycle.watchLifeCircle(width, state, activity);
        }

        /**
         * panel is showed as hidden state
         */
        public void showHide(Context context, int width) {
            show(context, width, ApplicationLifecycle.PANEL_STATE_MIN);
        }

        public void showClose(Context context, int width) {
            show(context, width, ApplicationLifecycle.PANEL_STATE_CLOSE);
        }

        public void show(Context context, int width) {
            show(context, width, ApplicationLifecycle.PANEL_STATE_SHOW);
        }


        private void readSharedPreferenceConfig(Context context) {
            deviceInfoEnabled = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_DEVICE_INFO, context, false);
            fpsEnabled = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_FRAME_INFO, context, false);
            keyLogEnabled = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_LOG_INFO, context, false);
            networkAnalyzeEnable = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_NETWORK_INFO, context, false);
            launchTimeEnabled = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_LAUNCH_INFO, context, false);
            activityAnalyzeEnable = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_ACTIVITY_INFO, context, false);
            displayEnable = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_DISPLAY_INFO, context, false);
            watchEnable = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_WATCH_INFO, context, false);
            hookTest = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_TEST_HOOK, context, false);
            crashEnable = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_CRASH_INFO, context, false);
            viewTouchLog = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_LENS_KEY_VIEW_TOUCH_ENABLE, context, false);
            permissionEnable =
                    SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_LENS_KEY_PERMISSION_ENABLE, context, false);

            // 当不可用的时候,  设置为false , 而不是强行的关闭选项
            if (!shouldEnableHook && hookImpl != null) {
                if (keyLogEnabled) {
                    keyLogEnabled = false;
                    SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_LOG_INFO, false, context);
                }
                if (networkAnalyzeEnable) {
                    networkAnalyzeEnable = false;
                    SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_NETWORK_INFO, false, context);
                }

                if (viewTouchLog) {
                    viewTouchLog = false;
                    SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_LENS_KEY_VIEW_TOUCH_ENABLE, false, context);
                }

                if (launchTimeEnabled) {
                    launchTimeEnabled = false;
                    SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_LAUNCH_INFO, false, context);
                }
                if (hookTest) {
                    hookTest = false;
                    SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_TEST_HOOK, false, context);
                }

                SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_LENS_CAN_HOOK, false, context);
            }
        }

        private void initSettings(Context context) {
            SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_DEVICE_INFO, deviceInfoEnabled, context);
            SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_FRAME_INFO, fpsEnabled, context);
            SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_NETWORK_INFO, networkAnalyzeEnable, context);
            SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_LOG_INFO, keyLogEnabled, context);
            SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_LAUNCH_INFO, launchTimeEnabled, context);
            SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_ACTIVITY_INFO, activityAnalyzeEnable, context);
            SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_CRASH_INFO, crashEnable, context);
            SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_LENS_KEY_PERMISSION_ENABLE, permissionEnable, context);
            SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_TEST_HOOK, hookTest, context);
        }
    }

    public static boolean hasFloatingPermission(Context context) {
        return true;
    }

    public static void addDefaultIPHosts(String ipHosts) {
        NetworkAnalyzeConfig.getInstance().addDefaultIPHosts(ipHosts);
    }

    public static void setUrlFilter(String filters) {
        NetworkAnalyzeConfig.getInstance().setUrlFilter(filters);
    }


    public static void setDisplayHeight(int displayHeight) {
        DisplayConfiguration.obtain().setDisplayHeight(displayHeight);
    }


    public static void showException(final Throwable throwable, final Object... vars) {
        if (throwable == null) return;

        throwable.printStackTrace();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            showExceptionPanel(throwable, vars);
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    showExceptionPanel(throwable, vars);
                }
            });
        }
    }

    private static void showExceptionPanel(final Throwable throwable, final Object... vars) {
        throwable.printStackTrace();
        FloatingPanel floatingPanel =
                ApplicationLifecycle.myDefaultPanelState == ApplicationLifecycle.PANEL_STATE_CLOSE
                        ? null : ApplicationLifecycle.getInstance().getPanel();
        new ExceptionPanel(floatingPanel).setData(throwable, vars).show();
    }

    public static void setViewDebugInfo(View view, String info) {
        setViewDebugInfo(view, info, null);
    }

    public static void setViewDebugInfo(View view, String info, Integer color) {
        if (view != null && info != null && info.length() > 0) {
            view.setTag(R.id.lens_debug_info_tag, info);
            view.setTag(R.id.lens_debug_info_tag_text_color, color);
        }
    }

    /**
     * 实现在这里的目的是，让用户直接使用lensUtils 就可以找到方法入口。
     * 同时也不必再暴露 ABTestConfig 类
     *
     * @param key    : key for abN test to key data
     * @param values : the supplied data to get from
     */
    public static void addABTest(String key, int[] values) {
        ABNTestConfig.getInstance().addABTest(key, values);
    }

    //[add a string key]
    public static void addABTest(String key, String[] values) {
        ABNTestConfig.getInstance().addABTest(key, values);
    }

    //add a boolean key
    public static void addABTest(String key) {
        ABNTestConfig.getInstance().addABTest(key);
    }


    public static boolean getABTestBoolean(String key) {
        return ABNTestConfig.getInstance().getBoolean(key);
    }

    public static int getABTestInt(String key) {
        return ABNTestConfig.getInstance().getInt(key);
    }

    public static String getABTestString(String key) {
        return ABNTestConfig.getInstance().getString(key);
    }

    public static void setReporter(IReporter reporter) {
        DataTransferManager.getInstance().setReporter(reporter);
    }

    public static void setRemoteBinder(IRemoteBinder binder) {
        DataTransferManager.getInstance().setRemoteBinder(binder);
    }

    public static String getRemoteUrl() {
        return DataTransferManager.getInstance().getRemoteUrl();
    }

    public static void setRemoteUrl(String remoteUrl) {
        DataTransferManager.getInstance().setRemoteUrl(remoteUrl);
    }

    public static void bindRemote() {
        DataTransferManager.getInstance().bindRemote();
    }

    public static void setDumper(Class<? extends ILogDumperFactory> factory) {
        LogDumperHolder.getInstance().setDumper(factory);
    }


    public static void setDebugStatusChanged(IDebugStatusChanged listener) {
        LogDumperHolder.setDebugStatusChanged(listener);
    }

    //0.2.5 新增API 设置： 获取各种config 能力
    public static NetworkAnalyzeConfig obtainConfigNetwork() {
        return NetworkAnalyzeConfig.getInstance();
    }

    public static DebugInfoConfig obtainConfigDebugInfo() {
        return DebugInfoConfig.getInstance();
    }

    public static ABNTestConfig obtainConfigABNTest() {
        return ABNTestConfig.getInstance();
    }


    public static void addCustomHookClass(Class[] cls) {

    }

    public static void setUIVerifyFactory(Class<? extends IUIVerifyFactory> factory) {
        LensConfig.getInstance().setUIVeryifyFactory(factory);
    }


}
