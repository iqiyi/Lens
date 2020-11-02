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
package com.qiyi.lens.demo;

import android.app.Application;
import android.content.Context;

import com.qiyi.lens.Lens;
import com.qiyi.lens.demo.dump.Dump;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.TimeStampUtil;


public class LensApp extends Application {
    private Object mLensApplication;
    private static LensApp sInstance;

    public static LensApp getInstance() {
        return sInstance;
    }

    @Override
    protected void attachBaseContext(Context base) {


        sInstance = this;
        TimeStampUtil.obtain(LensConfig.LAUNCH_TIME_STAMP_NAME).setEndViewId(android.R.id.content);
        super.attachBaseContext(base);
        Lens.setPreferAbi("armeabi");
        Lens.init(this, true);
        try {
            Class<?> lensApplication = getClassLoader().loadClass("com.qiyi.lens.demo.LensApplicationDelegate");
            mLensApplication = lensApplication.getConstructor(Application.class).newInstance(this);
            mLensApplication.getClass().getMethod("attachBaseContext", Context.class).invoke(mLensApplication, base);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mLensApplication.getClass().getMethod("onCreate").invoke(mLensApplication);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Dump
    public String dump() {
        return mLensApplication == null ? "null" : mLensApplication.getClass().getName();
    }
}
