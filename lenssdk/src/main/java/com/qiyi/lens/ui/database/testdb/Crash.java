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
package com.qiyi.lens.ui.database.testdb;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.BaseColumns;

import java.io.Serializable;


@TestDatabase.Table("log_crash")
public class Crash implements Serializable {


    @TestDatabase.Column(value = BaseColumns._ID, primaryKey = true)
    public int id;

    @TestDatabase.Column("createTime")
    public long createTime;
    @TestDatabase.Column("startTime")
    public long startTime;
    @TestDatabase.Column("type")
    public String type;
    @TestDatabase.Column("cause")
    public String cause;
    @TestDatabase.Column("stack")
    public String stack;

    @TestDatabase.Column("versionCode")
    public int versionCode;
    @TestDatabase.Column("versionName")
    public String versionName;
    @TestDatabase.Column("sys_sdk")
    public String systemSDK;
    @TestDatabase.Column("sys_version")
    public String systemVersion;
    @TestDatabase.Column("rom")
    public String rom;
    @TestDatabase.Column("cpu")
    public String cpuABI;
    @TestDatabase.Column("phone")
    public String phoneName;
    @TestDatabase.Column("locale")
    public String locale;


    public static void clear() {
        TestDatabase.delete(Crash.class);
    }


    private static int packageCode(Context context) {
        PackageManager manager = context.getPackageManager();
        int code = 0;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }

    private static String packageName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }
}
