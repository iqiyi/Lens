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

import android.os.Debug;

import java.util.Stack;

/**
 * 修复嵌套调用导致的问题
 */
public class LensTimeGapUtil {
    private static ThreadLocal<Stack<Long>> time = new ThreadLocal<>();
    private static ThreadLocal<Stack<Long>> cpuTime = new ThreadLocal<>();

    public static void onMethodIn() {
        long s1 = System.currentTimeMillis();
        long s2 = Debug.threadCpuTimeNanos();
        Stack<Long> stack1 = time.get();
        if (stack1 == null) {
            stack1 = new Stack<>();
            time.set(stack1);
        }
        stack1.push(s1);

        Stack<Long> stack2 = cpuTime.get();
        if (stack2 == null) {
            stack2 = new Stack<>();
            cpuTime.set(stack2);
        }
        stack2.push(s2);

    }

    public static void onMethodExit(String tag, String msg) {
        long lt = System.currentTimeMillis();
        long former = getTime(time);
        if (former == 0) return;
        long cpu = (Debug.threadCpuTimeNanos() - getTime(cpuTime)) / 1000000;
        android.util.Log.d(tag, (lt - former) + " ms " + msg + " cpu: " + cpu);
    }


    private static long getTime(ThreadLocal<Stack<Long>> local) {
        Stack<Long> stack = local.get();
        if (stack != null && !stack.isEmpty()) {
            return stack.pop();
        }
        return 0;
    }
}
