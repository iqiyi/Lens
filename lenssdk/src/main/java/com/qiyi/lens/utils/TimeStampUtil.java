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

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.qiyi.lens.ui.traceview.TimeStamp;
import com.qiyi.lens.ui.traceview.TimeStampInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

/**
 * 线程同步问题
 * 0.2.5： 新增time 默认的time stamp key 能力
 * 主要用于 任务分析的时间戳
 */
public class TimeStampUtil {
    //时间间隔标记：  1:start;-1:end; 0: a stamp mark;
    public int[] keys;
    //[被插入队列中的时间]
    public long[] stamps;
    //CPU 耗时
    public long[] cpuStamps;
    //当前执行stack 中的方法信息
    public String[] tags;
    public long[] threadIds;
    public HashMap<Long, String> threadInfoArray = new HashMap<>();

    private int capacity;

    private int size = 0;
    private String key;
    private int[] endViewIds;
    //0.2.5 用于支持任意一段时间内的耗时情况的查询
    private static String defaultKeyStamp = LensConfig.LAUNCH_TIME_STAMP_NAME;

    public TimeStampUtil(String k) {
        this.key = k;
        capacity = 6;
        size = 0;
        keys = new int[capacity];
        stamps = new long[capacity];
        cpuStamps = new long[capacity];
        tags = new String[capacity];
        threadIds = new long[capacity];

    }

    public void addStamp() {
        String tag = getCallerName();
        stampAdd(0, System.currentTimeMillis(), Debug.threadCpuTimeNanos(), tag);
    }

    public void addStamp(String name) {
//        String tag = getCallerName();
        stampAdd(0, System.currentTimeMillis(), Debug.threadCpuTimeNanos(), name);
    }

    public void addStamp(int key) {
        String tag = getCallerName();
        stampAdd(key, System.currentTimeMillis(), Debug.threadCpuTimeNanos(), tag);
    }

    public void addStamp(int key, long stamp, String name) {
//        String tag = getCallerName();
        stampAdd(key, stamp, Debug.threadCpuTimeNanos(), name);
    }

    public void addStamp(int key, String name, ThreadStampInfo threadInfo) {
        stampAdd(key, name, threadInfo);
    }

    public void addStamp(int key, long stamp) {
        String tag = getCallerName();
        stampAdd(key, stamp, Debug.threadCpuTimeNanos(), tag);
    }

    private String getCallerName() {
        StackTraceElement[] trace = new Throwable().getStackTrace();

        if (trace.length > 0) {

            int i = 0;
            int count = trace.length;
            while (i < count) {
                StackTraceElement traceElement = trace[i];
                if (!traceElement.getClassName().startsWith("com.qiyi.lens.utils")) {
                    return traceElement.getClassName() + "$" + traceElement.getMethodName() + " L" + traceElement.getLineNumber();
                }

                i++;
            }

            return trace[2].getClassName() + "$" + trace[2].getMethodName() + " L" + trace[2].getLineNumber();
        }
        return null;
    }

    private synchronized void stampAdd(final int key, final long stamp, long cpuStamp, String tag) {
        if (size >= capacity) {
            flipOver();
        }

        keys[size] = key;
        stamps[size] = stamp;
        cpuStamps[size] = cpuStamp;
        tags[size] = tag;
        Thread thread = Thread.currentThread();
        threadIds[size] = thread.getId();
        threadInfoArray.put(thread.getId(), thread.getName());

        size++;
    }

    private synchronized void stampAdd(final int key, String tag, ThreadStampInfo threadStamp) {
        if (size >= capacity) {
            flipOver();
        }

        keys[size] = key;
        stamps[size] = threadStamp.currentTime;
        cpuStamps[size] = threadStamp.threadTime;
        tags[size] = tag;
        threadIds[size] = threadStamp.threadId;
        threadInfoArray.put(threadStamp.threadId, threadStamp.threadName);

        size++;
    }

    private void flipOver() {
        int[] nKeys = new int[capacity + 6];
        String[] nTag = new String[capacity + 6];
        long[] nStamps = new long[capacity + 6];
        long[] nCpuStamps = new long[capacity + 6];
        long[] nIds = new long[capacity + 6];

        for (int i = 0; i < size; i++) {
            nKeys[i] = keys[i];
            nStamps[i] = stamps[i];
            nCpuStamps[i] = cpuStamps[i];
            nTag[i] = tags[i];
            nIds[i] = threadIds[i];

        }

        this.keys = nKeys;
        this.stamps = nStamps;
        this.cpuStamps = nCpuStamps;
        this.tags = nTag;
        this.threadIds = nIds;
        capacity = nKeys.length;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void end() {
        addStamp(0);
        if (map != null) {
            map.remove(key);
        }
    }


    public void stopAndPost() {
        end();
        DataPool.pushData(this, DataPool.DATA_TYPE_LAUNCH_TIME);
    }

    //[
    private static ConcurrentHashMap<String, TimeStampUtil> map;

    public static TimeStampUtil obtain(String key) {
        if (map == null) {
            map = new ConcurrentHashMap<>();
        }
        TimeStampUtil util = map.get(key);
        if (util == null) {
            util = new TimeStampUtil(key);
            map.put(key, util);
        }
        return util;
    }

    public static TimeStampUtil obtainNullable(String key) {
        if (map != null) {
            TimeStampUtil stampUtil = map.get(key);
            if (stampUtil != null) {
                return stampUtil;
            }
        }
        return null;
    }


    public static boolean addNullableStamp(@Nullable String stampName, int key) {
        TimeStampUtil util = obtainNullable(stampName);
        if (util != null) {
            util.addStamp(key);
            return true;
        }

        return false;
    }


    //only to add a stamp here
    public static boolean addNullableStamp(String stampName) {
        TimeStampUtil util = obtainNullable(stampName);
        if (util != null) {
            util.addStamp(0);
            return true;
        }
        return false;
    }


    /**
     * 如果为空就不添加 点
     */
    public static void addNullableLaunchStamp() {
        TimeStampUtil util = obtainNullable(LensConfig.LAUNCH_TIME_STAMP_NAME);
        if (util != null) {
            util.addStamp(0);
        }
    }

    public static void addNullableLaunchStamp(int key) {
        TimeStampUtil util = obtainNullable(LensConfig.LAUNCH_TIME_STAMP_NAME);
        if (util != null) {
            util.addStamp(key);
        }
    }

    /**
     * @param tagName display for a function
     */
    public static boolean addNullableStamp(String stampName, int key, long stamp, String tagName) {
        TimeStampUtil util = obtainNullable(stampName);
        if (util != null) {
            util.addStamp(key, stamp, tagName);
            return true;
        }

        return false;
    }

    /**
     * @param tagName display for a function
     */
    public static boolean addNullableStamp(String stampName, int key, String tagName) {
        TimeStampUtil util = obtainNullable(stampName);
        if (util != null) {
            util.addStamp(key, System.currentTimeMillis(), tagName);
            return true;
        }

        return false;
    }

    public static boolean addNullableStamp(String stampName, int key, String tagName, ThreadStampInfo stampInfo) {
        TimeStampUtil util = obtainNullable(stampName);
        if (util != null) {
            util.addStamp(key, tagName, stampInfo);
            return true;
        }

        return false;
    }


    public long getTotalTime() {
        return stamps[size - 1] - stamps[0];
    }


    //[check form top to bottom]

    /**
     * 算法原理： 从顶向下便利，如果发现方法id 相同，不管多少个都标记为 同一个执行方法域；标记为方法执行间隔时间；
     * 因此有多个的时候，将会标记相隔最久的一个；
     * 原理存在漏洞： 如何区分重入方法；
     * v2: 采用外部传入标记方法区间；方法重入计算执行时间间隔问题
     * test case ：[[[1]]];   [1][1][][1]
     * 方法废弃； 在stamp info 里面重新实现；
     */
    @Deprecated
    private void buildIntervals(StringBuilder stringBuilder) {
        int size = getSize();
        int[] checked = new int[size];
        stringBuilder.append("执行间隔：\n");
        int p = size - 1;
        while (p > 0) {
            String key = tags[p];
            if (key == null || key.length() == 0 || checked[p] == 1) {
                p--;
                continue;
            }

            long interval = 0;
            int k = p - 1;
            while (k > -1) {

                if (key.equals(tags[k])) {
                    interval = stamps[p] - stamps[k];
                    checked[k] = 1;
                }
                k--;
            }

            if (interval > 0) {
                stringBuilder.append(interval);
                stringBuilder.append(": ");
                if (key.startsWith("com.qiyi.lens")) {
                    stringBuilder.append(key.substring(1 + key.lastIndexOf("$")));
                } else {
                    stringBuilder.append(key);
                }
                stringBuilder.append("\n");
            }


            p--;
        }


        stringBuilder.append("\n");


    }

    @Deprecated
    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append("Toatal :");
        builder.append(getTotalTime());
        builder.append("ms\n");

        buildIntervals(builder);

        int p = 0;
        String currentTag = "";
        long begin = stamps[0];
        while (p < size) {
            String tag = tags[p];
            if (tag.equals(currentTag)) {
                builder.append("\t间隔：");
                builder.append(stamps[p] - stamps[p - 1]);
                builder.append("\t");
            } else {
                builder.append(stamps[p] - begin);
                builder.append("\t\t  ");
                if (tag.startsWith("com.qiyi.lens")) {
                    builder.append(tag.substring(1 + tag.lastIndexOf("$")));
                } else {
                    builder.append(tag);
                }

                builder.append("\n");
            }

            p++;
        }
        return builder.toString();
    }
    //]

    public void setEndViewId(int id) {
        endViewIds = new int[1];
        endViewIds[0] = id;

    }

    public void setEndViewIds(int[] ids) {
        if (ids != null && ids.length > 0) {
            endViewIds = ids;
        } else {
            endViewIds = null;
        }
    }


    //[defalt return true: if not set]
    public boolean isTraceEndView(int vid) {

        // use ==
        if (endViewIds == null && LensConfig.LAUNCH_TIME_STAMP_NAME == TimeStampUtil.getDefaultKeyStamp()) {
            return true;
        } else if (endViewIds != null) {
            for (int id : endViewIds) {
                if (id == vid) {
                    return true;
                }
            }
        }
        return false;
    }

    public int traceEndViewIndex(int vid) {
        if (endViewIds == null) {
            return -1;
        } else {
            for (int i = 0; i < endViewIds.length; i++) {
                int id = endViewIds[i];
                if (id == vid) {
                    return i;
                }
            }
            return -1;
        }
    }

    public TimeStampInfo buidStampInfo() {
        TimeStampInfo info = new TimeStampInfo();
        int size = getSize();
        if (size > 0) {

            info.setTimeExpand((int) (stamps[size - 1] - stamps[0]));

            //[make time stamps for function for those keys are 0]
            java.util.LinkedList<TimeStamp> stampList = new LinkedList<>();

            for (int i = 0; i < size; i++) {
                if (keys[i] == 0 || keys[i] == 1) {
                    TimeStamp stamp = new TimeStamp(tags[i], threadIds[i], stamps[i] - stamps[0], cpuStamps[i] - cpuStamps[0], stamps[i]);
                    stampList.add(stamp);
                    stamp.functionIndex = stampList.size();
                }
            }

            info.setTimeStamps(stampList);
            //[must be called after stamps been set]
            info.buildThreadInfo(this);

        }
        return info;
    }

    /**
     * 新增提供一些列的不需要提供stamp key 的函数
     */
    public static void setDefaultStampKey(String key) {
        if (key != null && key.length() > 0) {
            defaultKeyStamp = key;
        } else {
            throw new IllegalStateException("default key should not be null or empty " + key);
        }
    }


    @RestrictTo(LIBRARY)
    public static String getDefaultKeyStamp() {
        return defaultKeyStamp;
    }

    public static TimeStampUtil obtain() {
        return obtain(defaultKeyStamp);
    }

    public static TimeStampUtil obtainNullable() {
        return obtainNullable(defaultKeyStamp);
    }

    public static boolean addNullableStamp(int key) {
        return addNullableStamp(defaultKeyStamp, key);
    }


    //only to add a stamp here
    public static boolean addNullableStamp() {
        return addNullableStamp(defaultKeyStamp);
    }

    public static boolean addNullableStamp(int key, long stamp, String tagName) {
        return addNullableStamp(defaultKeyStamp, key, stamp, tagName);
    }

    /**
     * @param tagName display for a function
     */
    public static void addNullableStamp(int key, String tagName) {
        addNullableStamp(defaultKeyStamp, key, tagName);
    }

    public static boolean addNullableStamp(int key, String tagName, ThreadStampInfo stampInfo) {
        return addNullableStamp(defaultKeyStamp, key, tagName, stampInfo);
    }

    public static void startStamp(String tagName) {
        addNullableStamp(1, tagName);
    }

    public static void stopStamp(String tagName) {
        addNullableStamp(-1, tagName);
    }

    public static class ThreadStampInfo {
        long threadId;
        String threadName;
        long threadTime;
        long currentTime;

        public ThreadStampInfo() {
            this(Thread.currentThread());
        }

        public ThreadStampInfo(Thread thread) {
            threadId = thread.getId();
            threadName = thread.getName();
            threadTime = Debug.threadCpuTimeNanos();
            currentTime = System.currentTimeMillis();
        }

        public void setThreadCpuTime(long time) {
            threadTime = time;
        }

        public long getThreadId() {
            return threadId;
        }

        public String getThreadName() {
            return threadName;
        }

        public long getThreadTime() {
            return threadTime;
        }

        public long getCurrentTime() {
            return currentTime;
        }
    }


}
