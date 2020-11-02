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
package com.lens.hook.utils;

import android.content.Context;


public class HookWrapper {


    /**
     * 设置自定义的插件地址
     */
    public static void addCustomHookPluginPath(String path) {

    }

    /**
     * for lens-oop plug : set to false;
     * for lens sdk reliance set to true
     */
    public static void usePluginMode(boolean usePlugin) {

    }

    public static void doHookDefault(String className) {

    }

    public static void setHookPluginInfo(Context context, String dir, String apkFile) {

    }

    public static ClassLoader getPluginClassLoader() {
        return HookWrapper.class.getClassLoader();
    }

    //[第一次hook 需要异步操作，后续都做同步操作。同步问题]
    private static void loadHookPlugin(String className) {
    }

    /**
     * 保证opt 缓存目录创建
     */
    private static void ensureCacheDirectory() {

    }

    /**
     * @return 插件版本号是否验证通过
     */
    private static boolean checkPluginVersion() {
        return false;

    }

    //将打在包内的插件，写出到内部目录；
    private static void outputPluginFile() {

    }

    private static int getStaticFieldInt(Class cls, String name) {

        return 0;
    }

}
