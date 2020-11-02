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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;

public class LensReflectionTool {

    private Method getDeclaredMethod;
    private Method getFieldMethod;
    private static LensReflectionTool mTool = new LensReflectionTool();
    private AbstractMap<String, Method> methodHashMap;
    private AbstractMap<String, Field> fieldMap;

    public LensReflectionTool() {
        methodHashMap = new ConcurrentHashMap<>();
        fieldMap = new ConcurrentHashMap<>();
        try {
            getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
            getDeclaredMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        try {
            getFieldMethod = Class.class.getMethod("getDeclaredField", String.class);
            getFieldMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static LensReflectionTool get() {
        return mTool;
    }

    public Builder on(Object data) {
        return new Builder(methodHashMap, fieldMap, getDeclaredMethod, getFieldMethod, data);
    }

    public static class Builder implements IMethodCall, IFieldGet {
        Object mTarget;
        boolean mResoleParent;
        Method mFindMethod;
        Method mFindFieldMethod;
        Method targetMethod;
        Field targetField;
        AbstractMap<String, Method> methodHashMap;
        AbstractMap<String, Field> fieldsHashMap;

        Builder(AbstractMap<String, Method> map, AbstractMap<String, Field> fields, Method method, Method fieldMethod, Object data) {
            mTarget = data;
            mFindMethod = method;
            methodHashMap = map;
            fieldsHashMap = fields;
            mFindFieldMethod = fieldMethod;
            mResoleParent = true;
        }

        public Builder resolveParent(boolean resolveParent) {
            mResoleParent = resolveParent;
            return this;
        }

        private String getMethodSignature(String name, Class<?>[] args) {
            if (mTarget != null) {
                StringBuilder builder = new StringBuilder();
                builder.append(mTarget.getClass().getName());
                builder.append(';');
                builder.append(name);
                builder.append(';');
                if (args != null) {
                    for (Class<?> cls : args) {
                        if (cls == null) continue;
                        builder.append(cls.getName());
                        builder.append(';');
                    }
                }
                return builder.toString();
            }
            return null;
        }

        /**
         * resolve parent to din target
         */
        public IMethodCall methodSignature(String name, Class<?>... args) {

            String signature = getMethodSignature(name, args);
            if (signature == null) {
                return this;
            }
            targetMethod = methodHashMap.get(signature);
            if (targetMethod == null) {
                Class objectClass = mTarget.getClass();
                if (objectClass == Object.class) return this;
                // build signature by object , name & args
                do {
                    try {
                        targetMethod = (Method) mFindMethod.invoke(objectClass, name, args);
                        if (targetMethod == null) return this;
                        targetMethod.setAccessible(true);
                        methodHashMap.put(signature, targetMethod);
                        return this;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    objectClass = objectClass.getSuperclass();
                } while (objectClass != null && mResoleParent && objectClass != Object.class);
            }
            return this;
        }

        @Override
        public Object call(Object... args) {
            if (targetMethod != null) {
                try {
                    return targetMethod.invoke(mTarget, args);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        private IFieldGet findField(Class<?> objectClass, String name) {
            String signature = getMethodSignature(name, null);
            if (signature == null) {
                return this;
            }

            targetField = fieldsHashMap.get(signature);

            if (targetField == null) {

                if (objectClass == Object.class) return this;
                // build signature by object , name & args

                do {
                    try {
                        targetField = (Field) mFindFieldMethod.invoke(objectClass, name);
                        if (targetField == null) return this;
                        targetField.setAccessible(true);
                        fieldsHashMap.put(signature, targetField);
                        return this;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    objectClass = objectClass.getSuperclass();
                } while (objectClass != null && mResoleParent && objectClass != Object.class);
            }
            return this;
        }

        public IFieldGet staticFieldName(String name) {
            return findField((Class<?>) mTarget, name);
        }

        public IFieldGet fieldName(String name) {
            return findField(mTarget.getClass(), name);
        }

        @Override
        public Object get() {
            if (targetField != null) {
                try {
                    return targetField.get(mTarget);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        public boolean set(Object value) {
            if (targetField != null) {
                try {
                    targetField.set(mTarget, value);
                    return true;
                } catch (Exception ignored) {
                }
            }
            return false;
        }
    }


    public interface IMethodCall {
        Object call(Object... args);
    }

    public interface IFieldGet {
        Object get();

        boolean set(Object value);
    }


    public static Object fieldChain(Object target, String[] names) {
        return fieldChain(get(), target, names, true);
    }

    public static Object fieldChain(LensReflectionTool tool, Object target, String[] names) {
        return fieldChain(tool, target, names, true);
    }

    public static Object fieldChain(LensReflectionTool tool, Object target, String[] names, boolean resolveParent) {
        if (tool == null) {
            tool = get();
        }

        if (target == null || names == null || names.length == 0) return null;

        for (String name : names) {
            if (target == null) return null;
            target = tool.on(target)
                    .resolveParent(resolveParent)
                    .fieldName(name)
                    .get();
        }

        // only when loop finished ,then return new target
        return target;
    }
}
