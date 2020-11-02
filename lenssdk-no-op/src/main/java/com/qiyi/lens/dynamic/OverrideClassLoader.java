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
package com.qiyi.lens.dynamic;

import dalvik.system.DexClassLoader;

class OverrideClassLoader extends DexClassLoader {
    OverrideClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return loadClassSelf(name);
        } catch (ClassNotFoundException e) {
            return super.loadClass(name, resolve);
        }
    }

    /**
     * 从自己 dex 获取 class，不遵循双亲委派查询 parent ClassLoader
     *
     * @param name class name
     * @return the class
     * @throws ClassNotFoundException if not found
     */
    Class<?> loadClassSelf(String name) throws ClassNotFoundException {
        // 一些需要强制使用 host 中的类
        if ("com.qiyi.lens.Lens".equals(name) // 只有 no-op 中的 Lens 有逻辑
                || "com.qiyi.lens.ConfigHolder".equals(name) // 记录初始化配置信息，需要始终使用 no-op 的
                || "com.qiyi.lens.ConfigHolder$WatchConfig".equals(name) // 记录初始化配置信息，需要始终使用 no-op 的
                //---- 以下暴露的 interface，host 中实现了该接口后需要始终使用️ ----//
                // TODO 暴露的 interface 后期考虑转移到单独的 module，sdk 使用 compileOnly 依赖
                || "com.qiyi.lens.dump.DumpResultHandler".equals(name)
                || "com.qiyi.lens.dump.ILogDumperFactory".equals(name)
                || "com.qiyi.lens.ui.viewinfo.IViewClickHandle".equals(name)
                || "com.qiyi.lens.utils.iface.IFragmentHandle".equals(name)
                || "com.qiyi.lens.utils.iface.IObjectDescriptor".equals(name)
                || "com.qiyi.lens.transfer.IReporter".equals(name)
                || "com.qiyi.lens.utils.iface.IViewInfoHandle".equals(name)
                || "com.qiyi.lens.dump.IDebugStatusChanged".equals(name)
                || "com.qiyi.lens.utils.iface.IUIVerifyFactory".equals(name)
            //---- 以上暴露的 interface，host 中实现了该接口后需要始终使用️ ----//
        ) {
            throw new ClassNotFoundException(name);
        }
        /* 按照 ClassLoader 规约，检查是否加载过 */
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            // 如果没有找到，调用自身的findClass查找自身的类
            c = findClass(name);
        }

        if (c == null) {
            throw new ClassNotFoundException(name);
        }

        return c;
    }
}
