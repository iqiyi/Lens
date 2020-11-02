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

import java.lang.reflect.Field;

import dalvik.system.PathClassLoader;

/**
 * Lens sdk 的 classloader，当 lens 插件准备就绪后，会无缝替换宿主中的类
 * <p>
 * 核心原理：替换 PathClassLoader 和 BootClassLoader 之间插入 LensClassLoader. PathClassLoader 在使用
 * 任何类时得会从 LensClassLoader 检查获取。LensClassLoader 在查询类时，优先从 OverrideClassLoader 获取
 */
public class LensClassLoader extends PathClassLoader {
    private static LensClassLoader sLensClassLoader;
    private ClassLoader mPathClassLoader;
    private OverrideClassLoader mOverrideClassLoader;

    public LensClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath) {
        super(".", ClassLoader.class.getClassLoader());
        mPathClassLoader = getClass().getClassLoader();
        sLensClassLoader = this;
        mOverrideClassLoader = new OverrideClassLoader(dexPath, optimizedDirectory, librarySearchPath, mPathClassLoader);
    }

    public static ClassLoader get() {
        return sLensClassLoader;
    }

    public void hook() throws NoSuchFieldException, IllegalAccessException {
        Field parentField = ClassLoader.class.getDeclaredField("parent");
        parentField.setAccessible(true);
        parentField.set(mPathClassLoader, this);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // load from boot. such as String / List etc.
        try {
            Class<?> cls = mOverrideClassLoader.loadClassSelf(name);
            if (cls != null) {
                return cls;
            }
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return super.loadClass(name, resolve);
    }
}
