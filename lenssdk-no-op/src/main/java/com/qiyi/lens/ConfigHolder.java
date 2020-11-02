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

import androidx.annotation.RestrictTo;

import com.qiyi.lens.dump.IDebugStatusChanged;
import com.qiyi.lens.dump.ILogDumperFactory;
import com.qiyi.lens.transfer.IRemoteBinder;
import com.qiyi.lens.transfer.IReporter;
import com.qiyi.lens.ui.viewinfo.IViewClickHandle;
import com.qiyi.lens.utils.iface.IFragmentHandle;
import com.qiyi.lens.utils.iface.IObjectDescriptor;
import com.qiyi.lens.utils.iface.IUIVerifyFactory;
import com.qiyi.lens.utils.iface.IViewInfoHandle;

import java.util.ArrayList;
import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ConfigHolder {
    public static Class<? extends IViewInfoHandle> viewInfoHandler;
    public static Class<? extends IFragmentHandle> fragmentHandler;
    public static IObjectDescriptor defaultObjectDescriptor;
    public static IReporter reporter;
    public static IRemoteBinder remoteBinder;
    public static Class<? extends IViewClickHandle> viewClickDebugHandle;
    public static Class<? extends ILogDumperFactory> dumper;
    public static boolean defaultOpen;
    public static boolean initAsPluginMode;
    public static boolean enableLaunchTime;
    public static boolean enableHookTest;
    public static boolean enableActivityAnalyzer;
    public static int state;
    public static int width;
    public static boolean enableKeyLog;
    public static boolean enableFPS;
    public static List<WatchConfig> watchObjects = new ArrayList<>();
    public static List<WatchConfig> watchFields = new ArrayList<>();
    public static boolean enableCrashInfo;
    public static boolean enableNetworkAnalyze;
    public static boolean enableDeviceInfo;
    public static boolean enableDisplayInfo;
    public static boolean enableViewInfo;
    public static boolean enablePermission;
    public static IDebugStatusChanged debugStatusChanged;
    public static Class<? extends IUIVerifyFactory> uiVerifyClass;

    public static class WatchConfig {
        public String name;
        public Object object;

        public WatchConfig(String name, java.lang.Object object) {
            this.name = name;
            this.object = object;
        }
    }
}
