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
package com.qiyi.lens.ui.traceview;

import java.util.HashMap;
import java.util.LinkedList;

public class ThreadInfo {
    String threadName;
    long threadId;
    public int index;
    LinkedList<TimeStamp> stamps = new LinkedList<>();
    LinkedList<TimeGap> blocks = new LinkedList<>();
    private static HashMap<Long, String> threadCreateInfo = new HashMap<>();

    ThreadInfo(String name, long id) {
        threadName = name;
        threadId = id;
    }

    void addStamp(TimeStamp stamp) {

        stamps.add(stamp);
    }


    void addBlock(TimeGap gap) {
        blocks.addFirst(gap);
    }


    // 非线程安全
    public static void putThreadInfo(long tid, String info) {
        threadCreateInfo.put(tid, info);
    }

    static String getThreadInfo(long tid) {
        return threadCreateInfo.get(tid);
    }


}
