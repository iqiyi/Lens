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
import androidx.annotation.RestrictTo;


@RestrictTo(RestrictTo.Scope.LIBRARY)
public class SharedPreferenceUtils {//
    /* 修改时必须和 sdk 同步修改 */
    private static final String PREFERENCE_NAME = "Lens";

    public static void set(String propertyName, int propertyValue, Context context) {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putInt(propertyName, propertyValue).apply();
    }

    public static void set(String propertyName, String propertyValue, Context context) {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putString(propertyName, propertyValue).apply();
    }

    public static String getString(String propertyName, String defaultValue, Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).getString(propertyName, defaultValue);
    }
}
