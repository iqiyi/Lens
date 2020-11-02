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

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Looper;
import android.text.Spannable;

import com.qiyi.lens.utils.ColorStringBuilder;
import com.qiyi.lens.utils.LL;
import com.qiyi.lens.utils.TimeStampUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 支持的绘制数据：
 * 1） 线程数据 和时间刻度绘制
 * 2） 方法执行时间间隔数据绘制 ：方法嵌套层级色差数据
 * 3） time stamp 时间刻度方法绘制；
 */
public class TimeStampInfo {

    private int _threadCount;
    private int _timeExpand;
    private List<TimeStamp> _stamps;
    private LinkedList<TimeGap> _gaps;
    private int _maxGapsLevel;
    private ThreadInfo[] threadInfos;

    public TimeStampInfo() {
    }

    //获取执行的线程数量
    int getThreadCount() {
        return _threadCount;
    }

    //[获取时间长度]
    int getTimeExpand() {
        return _timeExpand;
    }


    //--------------set-------------
    public void buildThreadInfo(TimeStampUtil stampUtil) {
        int[] keys = stampUtil.keys;
        String[] tags = stampUtil.tags;
        long[] threadIds = stampUtil.threadIds;
        long[] stamps = stampUtil.stamps;
        long[] cpuStamps = stampUtil.cpuStamps;
        HashMap<Long, String> threadMap = stampUtil.threadInfoArray;

        _threadCount = threadMap.size();
        threadInfos = new ThreadInfo[_threadCount];
        Iterator<Map.Entry<Long, String>> iterator = threadMap.entrySet().iterator();

        int p = 0;
        while (iterator.hasNext()) {
            Map.Entry<Long, String> entry = iterator.next();
            threadInfos[p] = new ThreadInfo(entry.getValue(), entry.getKey());
            threadInfos[p].index = p;
            p++;

        }


        if (_stamps != null) {
            loadThreadStamps(_stamps);
        }


        //[make intervals for those in pairs]
        //[bug here : kkey is not matched]
        int size = keys.length;
//        int[][] pushIds = new int[_threadCount][size];
        int[] indexPointer = new int[_threadCount];
        //[init ]
        for (int i = 0; i < _threadCount; i++) {
            indexPointer[i] = -1;
        }

        LinkedList<TimeGap> gaps = new LinkedList<>();
        int maxLevel = 0;
        int threadIndex;
        int threadIndexPointer;
        HashMap<String, Integer> pushIds = new HashMap<>();
//        int pushIds[] = new int[size];
        for (int i = 0; i < size; i++) {

            ThreadInfo info = getThreadIndex(threadIds[i]);
            if (info == null) continue;

            threadIndex = info.index;

            if (keys[i] == 1) { //[start]
                indexPointer[threadIndex] += 1;
                threadIndexPointer = indexPointer[threadIndex];
                pushIds.put(threadIndex + "_" + tags[i], i);
//                pushIds[threadIndex][threadIndexPointer] = i;
                if (threadIndexPointer > maxLevel) {
                    maxLevel = threadIndexPointer;
                }
            } else if (keys[i] == -1) {//[end : -1 may pop any one in the stack; if key matche

                threadIndexPointer = indexPointer[threadIndex];

                if (threadIndexPointer < 0) {
                    //[erro : might :  not write 1 before call -1]
                    LL.e("TimeStampInfo", "tags: " + Arrays.toString(tags) + " error ： stamp key 1 is not added before -1");
                    continue;
                }

                Integer integer = pushIds.get(threadIndex + "_" + tags[i]);
                if (integer == null) {
                    continue;
                }
                int popId = integer;
                //[bug 如果这里不按照thread 来区分， 会导致level 不准确]
                TimeGap gap = new TimeGap(tags[i], threadIds[i], stamps[popId] - stamps[0],
                        stamps[i] - stamps[0], threadIndexPointer,
                        cpuStamps[popId],
                        cpuStamps[i],
                        stamps[popId]);


                //[remove index]
                indexPointer[threadIndex] -= 1;

                info.addBlock(gap);
                gaps.add(gap);
                gap.functionIndex = gaps.size();
            }
        }

        setTimeGaps(gaps, maxLevel);


    }

    private ThreadInfo getThreadIndex(long threadId) {
        for (ThreadInfo info : threadInfos) {
            if (info.threadId == threadId) {
                return info;
            }
        }
        return null;
    }

    private void loadThreadStamps(List<TimeStamp> list) {
        if (threadInfos != null) {

            for (ThreadInfo info : threadInfos) {
                for (TimeStamp stamp : list) {
                    if (stamp.threadId == info.threadId) {
                        info.addStamp(stamp);
                    }
                }

            }
        }

    }


    public void setTimeExpand(int time) {
        _timeExpand = time;
    }


    public void setTimeStamps(List<TimeStamp> list) {
        _stamps = list;
        loadThreadStamps(list);

    }

    // 重新反转level 方便绘制
    private void setTimeGaps(LinkedList<TimeGap> list, int maxLevel) {

        if (list != null) {
            for (TimeGap gap : list) {
                gap.level = maxLevel - gap.level;
            }
            Collections.sort(list, new Comparator<TimeGap>() {
                @Override
                public int compare(TimeGap o1, TimeGap o2) {
                    return o2.duration - o1.duration;
                }
            });
            _gaps = list;

        }
        _maxGapsLevel = maxLevel;

    }


    private ColorStringBuilder forceLength(ColorStringBuilder builder, String duration, int len) {

        int rm = len - duration.length();
        builder.append(duration);
        while (rm > 0) {
            builder.append(' ');
            rm--;
        }
        return builder;
    }


    public Spannable buildDescription() {
        ColorStringBuilder stringBuilder = new ColorStringBuilder();
        stringBuilder.append("Total:" + getTimeExpand() + "ms");
        stringBuilder.append("\n");

        getTimeGapInfo(stringBuilder, null);
        getStampInfo(stringBuilder, null);
        return stringBuilder.build();
    }

    @SuppressLint("DefaultLocale")
    private String format(long duration) {
        long s = duration / 1000L;
        long ss = duration % 1000L;
        return String.format("%02d.%03d", s, ss);
    }


    int getMaxGapsLevel() {
        return _maxGapsLevel;
    }

    ThreadInfo[] getThreadInfos() {
        return threadInfos;
    }


    /**
     * @param key : is search key: if match will be hight lighted
     */
    private void getTimeGapInfo(ColorStringBuilder stringBuilder, String key) {

        if (_gaps != null && _gaps.size() > 0) {
            long mainThreadId = Looper.getMainLooper().getThread().getId();
            stringBuilder.append("TimeGap\n");
            forceLength(stringBuilder, "ID ", 5).append(" \t");
            forceLength(stringBuilder, "COST ", 6).append(" \t");
            forceLength(stringBuilder, "CPU_T ", 6).append(" \t");
            forceLength(stringBuilder, "ST ", 6).append('\n');
            String tag;
            for (TimeGap gap : _gaps) {

                forceLength(stringBuilder, "#" + gap.functionIndex, 5);
                stringBuilder.append(" \t");

                // system time
                forceLength(stringBuilder, "" + gap.duration, 6);
                stringBuilder.append(" \t");

                //CPU time
                forceLength(stringBuilder, "" + gap.cpuDuration / 1000000L, 6);
                stringBuilder.append(" \t");

                forceLength(stringBuilder, format(gap.timeStamp), 6);
                stringBuilder.append(" \t");


                tag = gap.tag;
                if (tag.startsWith("com.qiyi.lens")) {
                    tag = tag.substring(1 + tag.lastIndexOf("$"));
                } else {
                    //[last index of .]
                    int last = tag.lastIndexOf('.');
                    tag = tag.substring(last + 1);
                }

                if (key != null && tag.toLowerCase().contains(key)) {
                    stringBuilder.append("<<< ", Color.GREEN);
                    if (gap.isMainThread(mainThreadId)) {
                        stringBuilder.append('+' + tag, Color.BLUE);
                    } else {
                        stringBuilder.append(tag, Color.BLUE);
                    }
                } else if (gap.isMainThread(mainThreadId)) {
                    stringBuilder.append('+' + tag, Color.RED);
                } else {
                    stringBuilder.append(' ' + tag);
                }


                stringBuilder.append("\n");
            }
        }

    }


    void getStampInfo(ColorStringBuilder stringBuilder, String key) {
        if (_stamps != null && _stamps.size() > 0) {
            long mainThreadId = Looper.getMainLooper().getThread().getId();
            stringBuilder.append("Stamps\n");
            long minTime = _stamps.get(0).startTime;
            //[top to bottom]
            String tag;
            for (TimeStamp gap : _stamps) {

                forceLength(stringBuilder, "#" + gap.functionIndex, 5);
                stringBuilder.append(" \t");

                //forceLength(stringBuilder, ""+gap.timeStamp, 6);
                //stringBuilder.append(" \t");

                forceLength(stringBuilder, format(gap.startTime - minTime), "00.000  ".length());
                stringBuilder.append(" \t");


                tag = gap.tag;
                if (tag.startsWith("com.qiyi.lens")) {
                    tag = tag.substring(1 + tag.lastIndexOf("$"));
                } else {
                    //[last index of .]
                    int last = tag.lastIndexOf('.');
                    tag = tag.substring(last + 1);
                }


                if (key != null && tag.toLowerCase().contains(key)) {
                    stringBuilder.append("<<< ", Color.GREEN);
                    if (gap.isMainThread(mainThreadId)) {
                        stringBuilder.append('+' + tag, Color.BLUE);
                    } else {
                        stringBuilder.append(tag, Color.BLUE);
                    }

                } else if (gap.isMainThread(mainThreadId)) {
                    stringBuilder.append("+" + tag, Color.RED);
                } else {
                    stringBuilder.append(' ' + tag);
                }

                stringBuilder.append("\n");
            }
        }
    }

    CharSequence getThreadInfo(String key) {

        int count = threadInfos == null ? 0 : threadInfos.length;
        //sort by names
        ArrayList<String> names = new ArrayList<>(count);
        if(threadInfos != null) {
            for (ThreadInfo i : threadInfos) {
                if (i.threadName != null) {
                    String var = ThreadInfo.getThreadInfo(i.threadId);
                    if (var != null) {
                        names.add(i.threadName + " : " + var);
                    } else {
                        names.add(i.threadName);
                    }
                }
            }
        }

        Collections.sort(names, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }
        });


        ColorStringBuilder builder = new ColorStringBuilder();


        builder.append("Total Time: ");
        builder.append(getTimeExpand() + "", Color.RED);
        builder.append(" ms\n");

        if (_threadCount > 1) {
            builder.append("execute thread count : " + _threadCount);
            builder.append("\n\n");
        }

        Iterator<String> iterator = names.iterator();
        int id = 1;
        while (iterator.hasNext()) {
            forceLength(builder, "#" + (id++), 5);
            builder.append(" \t");
            String name = iterator.next();
            if (key != null && name.toLowerCase().contains(key)) {
                builder.append("<<< ", Color.GREEN);
                builder.append(name, Color.BLUE);
            } else {
                builder.append(name);
            }
            builder.append('\n');
        }

        return builder.build();
    }

    public LinkedList<TimeGap> getGaps() {
        if (_gaps == null) {
            return new LinkedList<>();
        }
        return new LinkedList<>(_gaps);
    }
}
