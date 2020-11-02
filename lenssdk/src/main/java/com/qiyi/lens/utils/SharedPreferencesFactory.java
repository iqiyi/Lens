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
import android.text.TextUtils;

/**
 * SharedPreferences 工具类
 */
public class SharedPreferencesFactory {

    public static final String DEFAULT_SHARED_PREFERENCE_NAME = "default_sharePreference";
    public static final String DEFAULT_STR = "len_sp_search_default_str";
    public static final int DEFAULT_INT = 161803398;
    public static final float DEFAULT_FLOAT = 0.61803398f;
    public static final long DEFAULT_LONG = 161803398L;

    public static int getInt(Context context, String key, int defaultValue, String sharedPreferencesName) {
        if (null != context && !TextUtils.isEmpty(sharedPreferencesName) && !TextUtils.isEmpty(key)) {
            SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context, sharedPreferencesName);
            if (null != helper) {
                return helper.getInt(key, defaultValue);
            }
        } else {
            return getInt(context, key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * 获取int 在默认的 sharedPreferences 中
     */
    private static int getInt(Context context, String key, int defaultValue) {
        if (null != context && !TextUtils.isEmpty(key)) {
            SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context,
                    DEFAULT_SHARED_PREFERENCE_NAME);
            if (null != helper) {
                return helper.getInt(key, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * 获取某个指定sharedPreferences 的float  data
     */
    public static float getFloat(Context context, String key, float defaultValue, String sharedPreferencesName) {
        if (null != context && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(sharedPreferencesName)) {
            SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context, sharedPreferencesName);
            if (null != helper) {
                return helper.getFloat(key, defaultValue);
            }
        } else {
            return getFloat(context, key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * 获取float 在默认的 sharedPreferences 中
     */
    private static float getFloat(Context context, String key, float defaultValue) {
        if (null != context && !TextUtils.isEmpty(key)) {
            SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context,
                    DEFAULT_SHARED_PREFERENCE_NAME);
            if (null != helper) {
                return helper.getFloat(key, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * 获取某个指定sharedPreferences 的long  data
     */
    public static long getLong(Context context, String key, long defaultValue, String sharedPreferencesName) {
        if (null != context && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(sharedPreferencesName)) {
            SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context, sharedPreferencesName);
            if (null != helper) {
                return helper.getLong(key, defaultValue);
            }
        } else {
            return getLong(context, key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * 获取long 在默认的 sharedPreferences 中
     */
    private static long getLong(Context context, String key, long defaultValue) {
        if (null != context && !TextUtils.isEmpty(key)) {
            SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context,
                    DEFAULT_SHARED_PREFERENCE_NAME);
            if (null != helper) {
                return helper.getLong(key, defaultValue);
            }
        }
        return defaultValue;
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue, String sharedPreferencesName) {
        if (null != context && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(sharedPreferencesName)) {
            SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context, sharedPreferencesName);
            if (null != helper) {
                return helper.getBoolean(key, defaultValue);
            }
        } else {
            return getBoolean(context, key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * 获取boolean 在默认的 sharedPreferences 中
     */
    private static boolean getBoolean(Context context, String key, boolean defaultValue) {
        if (null != context && !TextUtils.isEmpty(key)) {
            SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context,
                    DEFAULT_SHARED_PREFERENCE_NAME);
            if (null != helper) {
                return helper.getBoolean(key, defaultValue);
            }
        }
        return defaultValue;
    }


    public static String getString(Context context, String key, String defaultValue, String sharedPreferencesName) {
        if (null != context && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(sharedPreferencesName)) {
            SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context, sharedPreferencesName);
            if (null != helper) {
                return helper.getString(key, defaultValue);
            }
        } else {
            return getString(context, key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * 获取String 在默认的 sharedPreferences 中
     */
    private static String getString(Context context, String key, String defaultValue) {
        if (null != context && !TextUtils.isEmpty(key)) {
            SharedPreferencesHelper helper = SharedPreferencesHelper.getInstance(context,
                    DEFAULT_SHARED_PREFERENCE_NAME);
            if (null != helper) {
                return helper.getString(key, defaultValue);
            }
        }
        return defaultValue;
    }

}
