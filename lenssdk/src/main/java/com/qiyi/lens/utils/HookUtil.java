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

import android.os.Build;
import android.util.Log;

import com.qiyi.lens.utils.iface.IHookFrameWork;
import com.qiyi.lens.hook.WhiteList;


/**
 * 在这里来封装hook 调用能力
 * 所有hook 相关的东西在这里统一入口调用
 */
public class HookUtil {

    private static final String TAG = "HookUtil";

    private static boolean isViewTouchHooked;

    private static boolean isPermissionHooked;
    private static IHookFrameWork hookImpl = LensConfig.getInstance().getHookFrameWorkImpl();

    /**
     * hook threads task etc...
     * moved form HookMonitor here
     */
    public static void hookTaskInfo() {
        try {
            String clz;
            if (WhiteList.isYunOS()) {
                clz = "com.qiyi.lens.hooks.ViewDrawHookWhiteList";//ViewDrawHookWhiteList.class;
                LL.e("YUN OS");
            } else {
                LL.e("VIewGroup N");
                clz = "com.qiyi.lens.hooks.ViewDrawHookList";
                //ViewDrawHookList.class;
            }
            hookImpl.doHookDefault(clz);
        } catch (Exception e) {
            Log.e(TAG, "lens hook launch time start fail");
            e.printStackTrace();
        }

        HookUtil.hookClass("com.qiyi.lens.hooks.qiyi.JobManagerHook");
        HookUtil.hookClass("com.qiyi.lens.hooks.qiyi.TaskManagerHook");


        //[thread tasks]
        if (Build.VERSION.SDK_INT <= 24) {
            HookUtil.hookClass("com.qiyi.lens.hooks.HookThreadRun6");
        } else {
            HookUtil.hookClass("com.qiyi.lens.hooks.HookThreadRun");
        }

        if (!Utils.isXiaomiDevice()) {
            HookUtil.hookClass("com.qiyi.lens.hooks.HookService");
            if (Build.VERSION.SDK_INT < 26) {
                HookUtil.hookClass("com.qiyi.lens.hooks.HookProvioder");
            } else {
                HookUtil.hookClass("com.qiyi.lens.hooks.HookProvioder8");
            }
//            HookUtil.hookClass("com.qiyi.lens.hooks.HookSharedPreferences");
        }
        HookUtil.hookClass("com.qiyi.lens.hooks.HookFragment");
        // xiaomi 设备上出现hook activity 崩溃
        hookImpl.doHookDefault("com.qiyi.lens.hooks.ActivityHookList");
    }


    /**
     * 调用后，将会启动答应View 触摸事件日志
     * 用开关控制，关闭后，将停止打印。
     * 只打印基类中的事件，如果没有调用基类，则不打印；
     */
    public static void hookViewTouch(boolean printLog) {
        if (!isViewTouchHooked && printLog) {
            isViewTouchHooked = true;
            hookImpl.doHookDefault("com.qiyi.lens.hooks.ViewTouchHook");
            hookImpl.doHookDefault("com.qiyi.lens.hooks.ViewGroupTouchHook");
        }
        //[plugin mode, 下面的方法只针对关闭日志，因此不用考虑插件初始化问题]
        if (isViewTouchHooked) {
            ClassLoader loader = hookImpl.getHookPluginClassLoader();
            if (loader != null) {
                try {// [ bug : 插件化方案，不打印日志的原因，插件异步加载，calss not found]
                    Class cls = Class.forName("com.qiyi.lens.hooks.ViewTouchHook", true, loader);
                    LensReflectionTool.get()
                            .on(cls)
                            .staticFieldName("enableLog")
                            .set(printLog);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    /**
     * 支持直接加载hook 文件
     */
    public static void enableHookTestHooks(boolean enable) {
        if (enable) {
            HookUtil.hookClass("com.qiyi.lens.hooks.HookPMS");
            HookUtil.hookClass("com.qiyi.lens.hooks.HookNetworkInterface");
            // 更详细的hook获取MacAddress方法
//            HookUtil.hookClass("com.qiyi.lens.hooks.HookWifiManagerService");
//            HookUtil.hookClass("com.qiyi.lens.hooks.HookWifiInfo");
//            HookUtil.hookClass("com.qiyi.lens.hooks.HookTelephonyManager");
        }
    }


    public static void hookClass(String className) {
        try {
            hookImpl.doHookDefault(className);
        } catch (Throwable e) {
            LL.e("hook fail " + className);
            e.printStackTrace();
        }

    }


    public static void hookPermission(boolean enable) {
        if (!isPermissionHooked && enable) {
            hookImpl.doHookDefault("com.qiyi.lens.hooks.HookActivityCompat");
            isPermissionHooked = true;
        }
    }

}
