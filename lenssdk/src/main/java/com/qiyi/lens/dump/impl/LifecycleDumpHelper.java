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
package com.qiyi.lens.dump.impl;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.qiyi.lens.dump.impl.anotaion.Crash;
import com.qiyi.lens.dump.impl.anotaion.LifecycleDump;
import com.qiyi.lens.ui.exceptionPanel.CrashInterceptor;
import com.qiyi.lens.utils.FileUtils;

import java.util.LinkedList;

/**
 * Lens dump :
 * support lifecycle data dump;
 * including : Activity & Fragment info
 */
public class LifecycleDumpHelper {
    private static LinkedList<String> lists = new LinkedList<>();

    public static int MAX_SIZE = 200;

    @UiThread
    public static void addFragmentLifecycle(Fragment fragment, String var) {
        StringBuilder builder = new StringBuilder();
        builder.append("Fragment:");
        builder.append(fragment.getClass().getName());
        builder.append(" ");
        builder.append(var);
        builder.append('\n');
        lists.addLast(builder.toString());
        assetSize();
    }


    @UiThread
    public static void addActivityLifecycle(Activity activity, String var) {
        StringBuilder builder = new StringBuilder();
        builder.append("Activity:");
        builder.append(activity.getClass().getSimpleName());
        builder.append(' ');
        builder.append(var);
        builder.append('\n');
        lists.addLast(builder.toString());
        assetSize();
    }

    private static void assetSize() {
        if (lists.size() > MAX_SIZE) {
            lists.pollFirst();
        }
    }


    public static void registerFragmentLifecycle(Activity activity) {

        if (activity instanceof FragmentActivity) {
            FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
            fragmentManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentPreAttached(FragmentManager fm, Fragment f, Context context) {
                }

                @Override
                public void onFragmentAttached(FragmentManager fm, Fragment f, Context context) {
                    addFragmentLifecycle(f, "onAttached");
                }

                @Override
                public void onFragmentPreCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
                }

                @Override
                public void onFragmentCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
                    addFragmentLifecycle(f, "onCreated");
                }

                @Override
                public void onFragmentActivityCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
                }

                @Override
                public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
                    addFragmentLifecycle(f, "onViewCreated");
                }

                @Override
                public void onFragmentStarted(FragmentManager fm, Fragment f) {
                    addFragmentLifecycle(f, "onStarted");
                }

                @Override
                public void onFragmentResumed(FragmentManager fm, Fragment f) {
                    addFragmentLifecycle(f, "onResumed");
                }

                @Override
                public void onFragmentPaused(FragmentManager fm, Fragment f) {
                    addFragmentLifecycle(f, "onPaused");
                }

                @Override
                public void onFragmentStopped(FragmentManager fm, Fragment f) {
                    addFragmentLifecycle(f, "onStoped");
                }

                @Override
                public void onFragmentSaveInstanceState(FragmentManager fm, Fragment f, Bundle outState) {
                }

                @Override
                public void onFragmentViewDestroyed(FragmentManager fm, Fragment f) {
                    addFragmentLifecycle(f, "onViewDestoyed");
                }

                @Override
                public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
                    addFragmentLifecycle(f, "onDestoyed");
                }

                @Override
                public void onFragmentDetached(FragmentManager fm, Fragment f) {
                    addFragmentLifecycle(f, "onDetached");
                }
            }, true);
        }

    }


    @LifecycleDump
    public static String dump() {
        if (!lists.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String s : lists) {
                builder.append(s);
            }
            return builder.toString();
        }
        return "";
    }


    @Crash
    public static String dumpCrash() {
        String path = CrashInterceptor.getCrashFilePath();
        String var = "";
        if (path != null) {
            var = FileUtils.file2String(path);
        }

        if (var == null || var.length() == 0) {
            return "NO-DATA";
        }
        return var;
    }
}
