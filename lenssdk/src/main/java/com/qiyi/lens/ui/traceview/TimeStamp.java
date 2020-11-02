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

public class TimeStamp {
    long threadId;
    public long timeStamp;
    private long cpuTimeStamp;
    public String tag;
    public int functionIndex;
    public long startTime;

    public TimeStamp(String tag, long tid, long timeStamp, long cpuTimeStamp, long startTime) {
        threadId = tid;
        this.timeStamp = timeStamp;
        this.cpuTimeStamp = cpuTimeStamp;
        this.startTime = startTime;
        this.tag = tag;
    }

    boolean isMainThread(long mainThreadId) {
        return threadId == mainThreadId;
    }


}
