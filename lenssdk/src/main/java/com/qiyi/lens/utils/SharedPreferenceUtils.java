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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceActivity;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * bad implementation
 */
public class SharedPreferenceUtils {
    private static final String TAG = SharedPreferenceUtils.class.getSimpleName();
    /* 修改时必须和 sdk-no-op 同步修改 */
    private static final String PREFERENCE_NAME = "Lens";

    public static String getSharedPreferences(String propertyName, Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).getString(propertyName, "");
    }

    public static String getSharedPreferences(String propertyName, Context context, String defaultValue) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).getString(propertyName, defaultValue);
    }

    public static String getSharedPreferences(String preferenceName, String propertyName, Context context) {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).getString(propertyName, "");
    }

    public static String getSharedPreferences(String preferenceName, String propertyName, Context context, String defaultValue) {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).getString(propertyName, defaultValue);
    }

    public static int getSharedPreferences(String preferenceName, String propertyName, Context context, int defaultValue) {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).getInt(propertyName, defaultValue);
    }

    public static long getSharedPreferences(String preferenceName, String propertyName, Context context, long defaultValue) {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).getLong(propertyName, defaultValue);
    }

    public static boolean getSharedPreferences(String propertyName, Context context, boolean defaultValue) {
        return context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_MULTI_PROCESS).getBoolean(propertyName, defaultValue);
    }

    public static boolean getSharedPreferences(String preferenceName, String propertyName, Context context, boolean defaultValue) {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).getBoolean(propertyName, defaultValue);
    }

    public static long getSharedPreferences(String propertyName, Context context, long defaultValue) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).getLong(propertyName, defaultValue);
    }

    public static float getSharedPreferences(String propertyName, Context context, float defaultValue) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).getFloat(propertyName, defaultValue);
    }

    public static int getSharedPreferences(String propertyName, Context context, int defaultValue) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(propertyName, defaultValue);
    }

    public static double getSharedPreferences(String propertyName, Context context, double defaultValue) {
        String data = getSharedPreferences(propertyName, context, "");
        try {
            return Double.parseDouble(data);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    public static boolean setSharedPreferences(String propertyName, int propertyValue, Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putInt(propertyName, propertyValue).commit();
    }

    public static boolean setSharedPreferences(String preferenceName, String propertyName, boolean propertyValue, Context context) {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).edit().putBoolean(propertyName, propertyValue).commit();
    }

    public static boolean setSharedPreferences(String propertyName, long propertyValue, Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
                .putLong(propertyName, propertyValue).commit();
    }

    public static boolean setSharedPreferences(String propertyName, float propertyValue, Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
                .putFloat(propertyName, propertyValue).commit();
    }

    public static boolean setSharedPreferences(String propertyName, double propertyValue, Context context) {
        return setSharedPreferences(propertyName, String.valueOf(propertyValue), context);
    }

    public static boolean setSharedPreferences(String preferenceName, String propertyName, String propertyValue, Context context) {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).edit()
                .putString(propertyName, propertyValue).commit();
    }

    public static boolean setSharedPreferences(String preferenceName, String propertyName, int propertyValue, Context context) {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).edit()
                .putInt(propertyName, propertyValue).commit();
    }

    public static boolean setSharedPreferences(String preferenceName, String propertyName, long propertyValue, Context context) {
        return context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).edit()
                .putLong(propertyName, propertyValue).commit();
    }

    public static boolean setSharedPreferences(String propertyName, String propertyValue, Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_MULTI_PROCESS).edit()
                .putString(propertyName, propertyValue).commit();
    }

    public static boolean setSharedPreferences(String propertyName, boolean propertyValue, Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_MULTI_PROCESS).edit()
                .putBoolean(propertyName, propertyValue).commit();
    }


    public static ArrayList<String> getSharedPreferenceList(String propertyName, Context context) {
        String regularEx = "#";
        String[] str = null;
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        String values;
        values = sp.getString(propertyName, "");
        if ("".equals(values)) {
            return null;
        }
        str = values.split(regularEx);
        return new ArrayList<String>(Arrays.asList(str));
    }

    public static void setSharedPreferenceList(String propertyName, List<String> values, Context context) {
        String regularEx = "#";
        String str = "";
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        if (values != null && values.size() > 0) {
            for (String value : values) {
                if ("".equals(value)) {
                    str += " ";
                } else {
                    str += value;
                }
                str += regularEx;
            }
            Editor et = sp.edit();
            et.putString(propertyName, str);
            et.commit();
        }
    }


    public static void setSharedPreferenceIntList(String propertyName, List<Integer> values, Context context) {
        List<String> stringList = new ArrayList<String>();
        if (values != null) {
            for (Integer integer : values) {
                stringList.add(String.valueOf(integer));
            }
        }
        setSharedPreferenceList(propertyName, stringList, context);
    }

    /**
     * 存储Map集合
     *
     * @param context 上下文
     * @param key     键
     * @param map     存储的集map
     * @param <K>     指定Map的键
     * @param <V>     指定Map的值
     */
    public static <K extends Serializable, V extends Serializable> void setSharedPreferenceMap(Context context,
                                                                                               String key, Map<K, V> map) {
        try {
            putObject(context, key, map);
        } catch (Exception e) {
        }
    }

    /**
     * 存储对象
     */
    private static void putObject(Context context, String key, Object obj)
            throws IOException {
        if (obj == null) {
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        //将对象放到OutputStream中
        //将对象转换成byte数组，并将其进行base64编码
        String objectStr = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
        baos.close();
        oos.close();
        //存储数据
        setSharedPreferences(key, objectStr, context);
    }

    /**
     * 获取对象
     */
    private static Object getObject(Context context, String key)
            throws IOException, ClassNotFoundException {
        String wordBase64 = getSharedPreferences(key, context);
        //将base64格式字符串还原成byte数组
        if (StringUtil.isNullOrEmpty(wordBase64)) {
            return null;
        }
        byte[] objBytes = Base64.decode(wordBase64.getBytes(), Base64.DEFAULT);
        ByteArrayInputStream bais = new ByteArrayInputStream(objBytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        //将byte数组转换成对象
        Object obj = ois.readObject();
        bais.close();
        ois.close();
        return obj;
    }
}
