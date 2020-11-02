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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Utils {
    public static String genRandomString() {
        int len = 4;
        //(int) (Math.random() * 100);
        int p = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while (p < len) {
            int a = (int) (Math.random() * 26);
            char c = (char) ('A' + a);
            stringBuilder.append(c);
            p++;
        }
        return stringBuilder.toString() + System.currentTimeMillis();
    }


    public static String getSimpleClassName(String s) {
        if (s != null && s.length() > 0) {
            int lst = s.lastIndexOf('.');
            return s.substring(lst + 1);
        }
        return "";
    }

    public static boolean isValidIP(String key) {
        if (key == null || key.length() == 0) {
            return false;
        } else {
            return key.matches("((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))");
        }
    }

    public static String array2String(String[] ar) {
        if (ar == null || ar.length == 0) {
            return "";
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : ar) {
                stringBuilder.append(s);
                stringBuilder.append(',');
            }
            stringBuilder.setLength(stringBuilder.length() - 1);
            return stringBuilder.toString();
        }

    }

    public static String[] string2Array(String data) {
        if (data != null && data.length() > 0) {
            return data.split(",");
        }
        return new String[0];
    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static String getViewVisivility(View view) {
        if (view != null) {
            int vis = view.getVisibility();
            if (vis == View.GONE) {
                return "GONE";
            } else if (vis == View.INVISIBLE) {
                return "INVISIBLE";
            } else {
                return "VISIBLE";
            }
        }
        return "null";
    }


    public static boolean isXiaomiDevice() {
        String manu = Build.MANUFACTURER;
        if ("Xiaomi".equals(manu)) {
            return true;
        }
        return false;
    }


    public static StringBuilder throwable2String(Throwable throwable) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(throwable.toString());
        StackTraceElement[] elements = throwable.getStackTrace();
        int p = 0;
        for (StackTraceElement element : elements) {
            stringBuilder.append(element.toString());
            stringBuilder.append("\n");
            p++;
            if (p > 20) break;
        }
        return stringBuilder;
    }


    public static boolean checkPermission(Activity activity, String permission) {
        if (isGranted(activity, permission)) {
            return true;
        } else { // request permission
            ActivityCompat.requestPermissions(activity, new String[]{permission}, 0);
        }
        return false;
    }

    private static boolean isGranted(Context context, final String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || PackageManager.PERMISSION_GRANTED
                == ContextCompat.checkSelfPermission(context, permission);
    }


    public static boolean hasFloatingWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(ApplicationLifecycle.getInstance().getContext());
        }
        return true;
    }


    public static void requestFloatingPermission() {
        Context context = ApplicationLifecycle.getInstance().getCurrentActivity();
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
