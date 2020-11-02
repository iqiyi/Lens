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
package com.qiyi.lens.ui.exceptionPanel;

import android.os.Handler;
import android.os.Looper;

import com.qiyi.lens.LensUtil;
import com.qiyi.lens.utils.ApplicationLifecycle;
import com.qiyi.lens.utils.FileUtils;
import com.qiyi.lens.utils.Utils;

import java.io.File;

/**
 * todo 待优化：当多次崩溃后，需要禁止掉一些功能等待恢复
 */
public class CrashInterceptor {

    private CrashInterceptor() {
    }

    private static Thread.UncaughtExceptionHandler sUncaughtExceptionHandler;
    private static boolean sInstalled = false;
    private static int MAXTIME = 1;
    private static int crash_time = 0;
    private static long upTime;


    public static synchronized void install(final boolean crashIntercept, boolean taskAnalyse) {


        if (taskAnalyse || crashIntercept) {
            Looper looper = Looper.getMainLooper();
            looper.setMessageLogging(new PrinterProxy());
        }


        // 监听java 崩溃事件
        sUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {

                try {
                    saveCrash2File(throwable);
                } catch (Throwable ignored) {
                }

                if (crashIntercept) {
                    showException(throwable);
                }

                if (sUncaughtExceptionHandler != null) {
                    sUncaughtExceptionHandler.uncaughtException(thread, throwable);
                }
            }
        });

        if (crashIntercept) {
            handleCrashIntercept();
        }

    }

    private static void saveCrash2File(Throwable throwable) {
        String path = getCrashFilePath();
        if (path != null) {
            File file = FileUtils.createFile(path, false);
            if (file != null) {
                String var = Utils.throwable2String(throwable).toString();
                FileUtils.writeStringToFile(file, var);
            }
        }

    }


    /**
     * 因为权限问题, 返回一个可以创建的位置
     *
     * @return path :
     */
    public static String getCrashFilePath() {

        File file = null;
        String path = FileUtils.getFilePath("lens", "crash_info");
        if (path != null) {
            file = FileUtils.createFile(path, true);
        }
        if (file == null) {
            path = FileUtils.getFilePrivate("lens", "crash_info");
            file = FileUtils.createFile(path, false);
            if (file == null) {
                return null;
            }
        }
        return path;
    }


    private static void handleCrashIntercept() {
        if (sInstalled) return;
        sInstalled = true;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Looper.loop();
                    } catch (Throwable e) {
                        e.printStackTrace();
                        if (e instanceof ExitCrashInterceptorException) {
                            return;
                        }
                        if (crash_time < MAXTIME) {
                            showException(e);
                            crash_time++;
                            upTime = System.currentTimeMillis();
                        } else if (System.currentTimeMillis() - upTime > 1000) {
                            crash_time = 0;
                        }
                    }
                }
            }
        });

    }

    public static synchronized void uninstall() {
        if (!sInstalled) {
            return;
        }
        sInstalled = false;
        Thread.setDefaultUncaughtExceptionHandler(sUncaughtExceptionHandler);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                throw new ExitCrashInterceptorException();
            }
        });
    }


    private static void showException(Throwable throwable, Object... vars) {
        if (upTime == 0) {
            exception(throwable, vars);
        } else if (LensUtil.hasFloatingPermission(ApplicationLifecycle.getInstance().getCurrentActivity())) {
            exception(throwable, vars);
        } else {
            System.out.println("程序已经崩溃，由于没有允许悬浮窗权限。因此就只好释放崩溃了");
            throw new RuntimeException(throwable);
        }

    }

    private static void exception(final Throwable throwable, final Object... vars) {
        throwable.printStackTrace();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                new ExceptionPanel(ApplicationLifecycle.getInstance().getPanel()).setData(throwable, vars).show();
            }
        }, 100);
    }
}
