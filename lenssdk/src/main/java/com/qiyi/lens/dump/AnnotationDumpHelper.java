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
package com.qiyi.lens.dump;

import androidx.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnnotationDumpHelper {
    private final Class<? extends Annotation> mAnnotation;
    private final List<Object> mRoots = new ArrayList<>();
    private final List<Object> mChecked = new ArrayList<>();
    private final List<Class<?>> mWhitelist = new ArrayList<>();
    private final StringBuilder mResult = new StringBuilder();
    private DumpResultHandler mDumpResultHandler;
    private String itemName = null;

    public static AnnotationDumpHelper create(Class<? extends Annotation> annotation) {
        return new AnnotationDumpHelper(null, annotation);
    }

    public static AnnotationDumpHelper create(String name, Class<? extends Annotation> annotation) {
        return new AnnotationDumpHelper(name, annotation);
    }

    private AnnotationDumpHelper(String name, Class<? extends Annotation> annotation) {
        mAnnotation = annotation;
        itemName = name;
    }

    public AnnotationDumpHelper addRoot(Object... root) {
        if (root != null) {
            mRoots.addAll(Arrays.asList(root));
        }
        return this;
    }

    //0.5.0 新增: lens 支持细分日志 内存信息获取
    public String getDumpTag() {
        if (itemName == null || itemName.length() == 0) {
            return mAnnotation.getSimpleName();
        } else {
            return itemName;
        }
    }

    public String dump() {
        for (Object root : mRoots) {
            try {
                dump(root);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        String result = mResult.toString();
        if (mDumpResultHandler != null) {
            result = mDumpResultHandler.onResult(mAnnotation, "", result);
        }
        reset();
        return result;
    }

    private void reset() {
        mChecked.clear();
        mWhitelist.clear();
        mResult.setLength(0);
    }

    private void dump(Object root) throws IllegalAccessException, InvocationTargetException {
        if (root == null) {
            return;
        }
        Class rootClass = root instanceof Class ? (Class) root : root.getClass();
        if (mChecked.contains(root) // checked before since java refs recursive
                || mWhitelist.contains(rootClass)  // no @Annotation method. just pass.
                || rootClass.getClassLoader() == Class.class.getClassLoader() // system class. no @Annotation method
        ) {
            return;
        }
        mChecked.add(root);

        // in case of array. dump each element
        if (rootClass.isArray()) {
            dumpArray(root);
        } else {
            dumpObject(root, rootClass);
        }
    }

    private void dumpObject(@NonNull Object root, @NonNull Class rootClass) throws IllegalAccessException, InvocationTargetException {
        boolean found = false;
        for (Method method : rootClass.getMethods()) {
            if (method.isAnnotationPresent(mAnnotation)) {
                boolean isStatic = (method.getModifiers() & Modifier.STATIC) != 0;
                String msg = (String) method.invoke(isStatic ? null : root);
                mResult.append(msg).append('\n');
                found = true;
                break;
            }
        }
        if (!found) {
            mWhitelist.add(rootClass);
        }
        // dump declared fields after self
        for (Field declaredField : rootClass.getDeclaredFields()) {
            declaredField.setAccessible(true);
            boolean isStatic = (declaredField.getModifiers() & Modifier.STATIC) != 0;
            Object value = declaredField.get(isStatic ? null : root);
            dump(value);
        }
    }

    private void dumpArray(Object root) throws IllegalAccessException, InvocationTargetException {
        int length = Array.getLength(root);
        for (int i = 0; i < length; i++) {
            Object target = Array.get(root, i);
            dump(target);
        }
    }

    public void setDumpResultHandler(DumpResultHandler handler) {
        mDumpResultHandler = handler;
    }
}
