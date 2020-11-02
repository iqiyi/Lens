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

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;


public class LensExecutor {

    private volatile static Handler uiHandler;
    private volatile static Handler bgHandler;

    private static void assetUIHandler() {
        if (uiHandler == null) {
            synchronized (LensExecutor.class) {
                if (uiHandler == null) {
                    uiHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
    }

    public static void runOnUIThread(Runnable run) {
        assetUIHandler();
        uiHandler.post(run);
    }


    private static void assetSingleThread() {
        if (bgHandler == null) {
            synchronized (LensExecutor.class) {
                if (bgHandler == null) {
                    HandlerThread thread = new HandlerThread("lens-bg-thread");
                    thread.start();
                    bgHandler = new Handler(thread.getLooper());
                }
            }
        }

    }

    public static void runOnSingleBackThread(Runnable runnable) {
        assetSingleThread();
        bgHandler.post(runnable);
    }

}
