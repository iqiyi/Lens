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
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.qiyi.lens.dump.IDebugStatusChanged;
import com.qiyi.lens.dump.ILogDumperFactory;
import com.qiyi.lens.dynamic.ExceptionHandler;
import com.qiyi.lens.dynamic.LensDownloader;
import com.qiyi.lens.transfer.IRemoteBinder;
import com.qiyi.lens.transfer.IReporter;
import com.qiyi.lens.ui.viewinfo.IViewClickHandle;
import com.qiyi.lens.utils.KeyLogConfig;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.SharedPreferenceUtils;
import com.qiyi.lens.utils.configs.ABNTestConfig;
import com.qiyi.lens.utils.configs.DebugInfoConfig;
import com.qiyi.lens.utils.configs.NetworkAnalyzeConfig;
import com.qiyi.lens.utils.iface.ICustomBlockFactory;
import com.qiyi.lens.utils.iface.IFragmentHandle;
import com.qiyi.lens.utils.iface.IHookFrameWork;
import com.qiyi.lens.utils.iface.IJumpAction;
import com.qiyi.lens.utils.iface.IObjectDescriptor;
import com.qiyi.lens.utils.iface.IUIVerifyFactory;
import com.qiyi.lens.utils.iface.IViewInfoHandle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * lens 封裝對外輸出能力集 入口
 */
public class LensUtil {

    public static ConfigBuilder buildConfig() {
        return new ConfigBuilder();
    }

    public static void onFPSScrollStarted() {
    }

    public static void download(Context context, String url, String version) {
        LensDownloader.get(context).downloadDirect(url, version);
    }

    @Deprecated
    private static void checkFloatingPermission(final Context context) {
        // 不能删除，保持 no-op 与 sdk 一致
    }


    public static void showManually(final Context context) {
        // 如果走到了 no-op 的 showManually，说明 lens 插件没有运行
        // 检查插件是否已经下载过，否则直接下载
        final TextView textView = new TextView(context);
        final ViewGroup viewGroup;
        if (context instanceof Activity) {
            textView.setText("\n");
            textView.setPadding(10, 0, 0, 0);
            textView.setBackgroundColor(Color.RED);
            textView.setTextColor(Color.WHITE);
            viewGroup = (ViewGroup) ((Activity) context).getWindow().getDecorView();
            viewGroup.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    viewGroup.removeView(textView);
                    return true;
                }
            });
        } else {
            viewGroup = null;
        }

        LensDownloader.get(context).check(new LensDownloader.OnResultCallback() {

            @Override
            protected void onStartCheck() {
                displayMsg("正在检查 Lens 配置");
            }

            @Override
            protected void onNoUpdate() {
                displayMsg("Lens 插件已经最新");
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (viewGroup != null) {
                            viewGroup.removeView(textView);
                        }
                    }
                }, 5000);
                Lens.init(context, Lens.isDebug());
                quickStart(context);
            }

            @Override
            protected void onDownloadComplete() {
                displayMsg("Lens 插件已经完成下载，即将开启 Lens");
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (viewGroup != null) {
                            viewGroup.removeView(textView);
                        }
                    }
                }, 5000);
                SharedPreferenceUtils.set(LensConfig.SP_KEY_PANEL_STATUS, 1, context);
                Lens.init(context, Lens.isDebug());
                quickStart(context);
            }

            @Override
            protected void onStartDownload(String version, String url) {
                displayMsg("开始下载 Lens v" + version);
            }

            @Override
            protected void onError(Throwable e) {
                e.printStackTrace();
                displayMsg("下载 Lens 出错");
                displayMsg(Log.getStackTraceString(e));
            }

            private void displayMsg(String msg) {
                if (context instanceof Activity) {
                    textView.append(msg);
                    textView.append("\n");
                } else {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private static void quickStart(Context context) {
        if (!Lens.isLoadPluginSuccess()) {
            return;
        }
        try {
            // 直接 QuickStart.start(context) 在 5.x 会获取到 no-op 的类，这里使用反射规避
            ClassLoader currentClassLoader = Lens.class.getClassLoader();
            if (currentClassLoader != null && currentClassLoader.getParent() != null) {
                Class<?> quickStartClass = currentClassLoader.getParent().loadClass("com.qiyi.lens.QuickStart");
                Method startMethod = quickStartClass.getDeclaredMethod("start", Context.class);
                startMethod.setAccessible(true);
                startMethod.invoke(null, context);
            }
        } catch (ClassNotFoundException e) {
            ExceptionHandler.throwIfDebug(e);
        } catch (NoSuchMethodException e) {
            ExceptionHandler.throwIfDebug(e);
        } catch (IllegalAccessException e) {
            ExceptionHandler.throwIfDebug(e);
        } catch (InvocationTargetException e) {
            ExceptionHandler.throwIfDebug(e);
        }
    }


    public static void watchObject(Object object) {
        ConfigHolder.watchObjects.add(new ConfigHolder.WatchConfig(null, object));
    }

    public static void watchObject(String name, Object object) {
        ConfigHolder.watchObjects.add(new ConfigHolder.WatchConfig(name, object));
    }

    public static void watchField(String fieldName, Object src) {
        ConfigHolder.watchFields.add(new ConfigHolder.WatchConfig(fieldName, src));
    }

    public static void setViewClickDebugHandle(Class<? extends IViewClickHandle> viewClickHandle) {
        ConfigHolder.viewClickDebugHandle = viewClickHandle;
    }

    public static void setDefaultObjectDescriptor(IObjectDescriptor descriptor) {
        ConfigHolder.defaultObjectDescriptor = descriptor;
    }

    public static void setFragmentHandler(Class<? extends IFragmentHandle> fragmentHandler) {
        ConfigHolder.fragmentHandler = fragmentHandler;
    }

    public static void setViewInfoHandler(Class<? extends IViewInfoHandle> fragmentHandler) {
        ConfigHolder.viewInfoHandler = fragmentHandler;
    }



    public static class ConfigBuilder {


        private void checkDefault(){}

        public ConfigBuilder defaultOpen(boolean enableAll) {
            ConfigHolder.defaultOpen = enableAll;
            return this;
        }

        public ConfigBuilder enableKeyLog(boolean enable) {
            ConfigHolder.enableKeyLog = enable;
            return this;
        }

        public ConfigBuilder enableKeyLog(KeyLogConfig logConfig) {
            return this;
        }

        public ConfigBuilder enableFPS(boolean enable) {
            ConfigHolder.enableFPS = enable;
            return this;
        }

        public ConfigBuilder enableHookTest(boolean enable) {
            ConfigHolder.enableHookTest = enable;
            return this;
        }


        public ConfigBuilder enableNetworkAnalyze(boolean enable) {
            ConfigHolder.enableNetworkAnalyze = enable;
            return this;
        }

        public ConfigBuilder enableDeviceInfo(boolean enable) {
            ConfigHolder.enableDeviceInfo = enable;
            return this;
        }

        public ConfigBuilder enableDisplayInfo(boolean enable) {
            ConfigHolder.enableDisplayInfo = enable;
            return this;
        }

        public ConfigBuilder enableLaunchTime(boolean enable) {
            ConfigHolder.enableLaunchTime = enable;
            return this;
        }




        public ConfigBuilder enableActivityAnalyzer(boolean enable) {
            ConfigHolder.enableActivityAnalyzer = enable;
            return this;
        }

        public ConfigBuilder enableViewInfo(boolean enable) {
            ConfigHolder.enableViewInfo = enable;
            return this;
        }

        public ConfigBuilder addCustomHookPluginPath(String path){
            return this;
        }
        /**
         *
         * @param customNames
         * @param action
         */
        public ConfigBuilder addCustomJumpEntrance(String[] customNames , IJumpAction action){
            return this;
        }

        public ConfigBuilder addCustomBlockEntrance(String[] blockKeys,  ICustomBlockFactory blockFactory){
            return this;
        }

        public ConfigBuilder enableCrashInfo(boolean enable) {
            ConfigHolder.enableCrashInfo = enable;
            return this;
        }



        public ConfigBuilder enablePermission(boolean enable) {
            ConfigHolder.enablePermission = enable;
            return this;
        }

        public ConfigBuilder initAsPluginMode(boolean plugin) {
            ConfigHolder.initAsPluginMode = plugin;
            return this;
        }

        public ConfigBuilder setHookFrameWorkImpl(IHookFrameWork frameWorkImpl) {
            return this;
        }

        private void buildHookPlugin(Context context){
            return;
        }

        public ConfigBuilder build(Context context) {
            buildHookPlugin(context);
            return this;
        }

        public void forceShow(Context con , int width, int state){}

        public void show(Context context, int width, int state) {
            ConfigHolder.width = width;
            ConfigHolder.state = state;
            quickStart(context);
        }

        public void showHide(Context context, int width) {
            ConfigHolder.width = width;
            quickStart(context);
        }

        public void showClose(Context context, int width) {
            ConfigHolder.width = width;
            quickStart(context);
        }

        public void show(Context context, int width) {
            ConfigHolder.width = width;
            quickStart(context);
        }
        private void readSharedPreferenceConfig(Context context){}
        private void initSettings(Context context){}


    }

    public static boolean hasFloatingPermission(Context context) {
        return true;
    }
    public static void addDefaultIPHosts(String ipHosts) {
    }
    public static void setUrlFilter(String filters) {
    }
    public static void setDisplayHeight(int displayHeight) {
    }

    public static void showException(Throwable throwable, Object... vars) {
    }

    private static void showExceptionPanel(final Throwable throwable, final Object... vars){}

    public static void setViewDebugInfo(View view, String info) {
    }

    public static void setViewDebugInfo(View view, String info, Integer color) {
    }

    public static void addABTest(String key, String[] values) {
    }

    public static void addABTest(String key) {
    }

    public static void addABTest(String key, int[] values) {
    }

    public static boolean getABTestBoolean(String key) {
        return false;
    }

    public static int getABTestInt(String key) {
        return 0;
    }

    public static String getABTestString(String key) {
        return "";
    }

    public static Object getABTestObject(String key) {
        return null;
    }

    public static void setReporter(IReporter reporter) {
        ConfigHolder.reporter = reporter;
    }

    public static void setRemoteBinder(IRemoteBinder binder) {
        ConfigHolder.remoteBinder = binder;
    }

    public static String getRemoteUrl() {
        return null;
    }

    public static void setRemoteUrl(String remoteUrl) {
    }

    public static void bindRemote() {
    }

    public static void setDumper(Class<? extends ILogDumperFactory> dumper) {
        ConfigHolder.dumper = dumper;
    }

    public static void setDebugStatusChanged(IDebugStatusChanged listener) {
        ConfigHolder.debugStatusChanged = listener;
    }
    //0.2.5 新增API 设置： 获取各种config 能力
    public static NetworkAnalyzeConfig obtainConfigNextwork(){
        return NetworkAnalyzeConfig.getInstance();
    }
    public static DebugInfoConfig ontainConfigDebugInfo(){
        return DebugInfoConfig.getInstance();
    }
    public static ABNTestConfig obtainConfigABNTest(){
        return ABNTestConfig.getInstance();
    }
    public static void addCustomHookClass(Class cls[]){
    }
    public static void setUIVerifyFactory(Class<? extends IUIVerifyFactory> factory){
        ConfigHolder.uiVerifyClass = factory;
    }
}
