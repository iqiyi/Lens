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

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.qiyi.lens.utils.event.EventBus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 关键日志log
 * 内存log  不写入sdcard
 * 使用： KeyLog.addLog();
 */
public class KeyLog {
    private String[] filters;
    private int maxLine;
    private int maxLineInCache = 1000;
    private int maxBriefLine = 5;
    private static KeyLog log;
    private LogArray logArray;
    private int endPosition;
    private LogArray cacheLogArray;
    private boolean isFull;
    public LogAddedCallback callback;
    private File filterFiles;
    private static final String FILTER_FILE = "filer_log.txt";
    private File fileDir;
    public int count = 0;

    //部分缓存全部缓存日志
    private File cacheFile;
    private static String[] CACHE_FILE_NAME_TEMP = {"cache_log_temp.txt", "cache_log_temp1.txt"};
    private static String CACHE_FILE_NAME = "cache_log.txt";
    private int currentCacheFilePos = 0;
    private int cacheLogCount = 0;
    private boolean cacheFileAppend = true;
    private static final long CAPACITY = 40 * 1024/* * 1024*/;
    protected Handler handler;

    private KeyLog(int max, int brf) {
        this.maxLine = max;
        maxBriefLine = brf;
        cacheLogArray = new LogArray(maxLine);
        logArray = new LogArray(maxLineInCache);
    }

    private synchronized void appendLog(final String key) {

        if (key == null || key.length() == 0) {
            return;
        }
        logArray.addString(key);
        cacheLogCount++;
        if (cacheLogCount > 300) {//每隔300条写入一次数据到文件
            writeToCacheFile();
            cacheLogCount = 0;
        }
        if (filters != null && !match(key)) {
            return;
        }
        count++;
        if (count > 300) {
            writeToFilterFile();
            count = 0;
        }
        cacheLogArray.addString(key);
        EventBus.pushData(DataPool.EVENT_DISPLAY_DATA_ARRIVED, null);

    }

    private void writeToFilterFile() {
        checkHandler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (filterFiles == null) {
                    filterFiles = createFile(FILTER_FILE);
                }
                if (filterFiles != null) {
                    writeToFile(filterFiles, true, cacheLogArray, count);
                }
            }
        });

    }

    private void writeToCacheFile() {
        checkHandler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (cacheFile == null) {
                    cacheFile = createFile(CACHE_FILE_NAME_TEMP[currentCacheFilePos % 2]);
                }
                if (cacheFile != null) {
                    writeToFile(cacheFile, cacheFileAppend, logArray, cacheLogCount);
                    cacheFileAppend = true;
                }
                if (cacheFile != null && cacheFile.length() > CAPACITY) {
                    currentCacheFilePos++;
                    cacheFile = createFile(CACHE_FILE_NAME_TEMP[currentCacheFilePos % 2]);
                    cacheFileAppend = false;
                }

            }
        });

    }

    private File createFile(String name) {
        if (fileDir == null) {
            return null;
        }
        return FileUtils.createFile(fileDir, name);
    }

    private void writeToFile(final File file, final boolean append, final LogArray array, final int countTotal) {
        checkHandler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(file, append));
                    int end = countTotal;
                    int maxsize = array.size();
                    if (countTotal > maxsize) {
                        end = maxsize;
                    }
                    for (int i = 0; i < end; i++) {
                        writer.write(array.getItem(maxsize - end + i));
                        writer.write("\n");
                    }
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (writer != null) {
                            writer.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    public static void flushAllLogsToFile() {
        if (log != null) {
            log.flushAllLogToFile();
        }
    }

    private synchronized void flushAllLogToFile() {
        if (count != 0) {
            writeToFilterFile();
            count = 0;
        }
        if (cacheLogCount != 0) {
            writeToCacheFile();
            cacheLogCount = 0;
        }
        combineCacheFiles();
    }

    //todo optimize
    private void combineCacheFiles() {
        checkHandler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                File file = createFile(CACHE_FILE_NAME);
                if (file != null) {
                    BufferedWriter writer = null;
                    BufferedReader reader = null;
                    try {
                        writer = new BufferedWriter(new FileWriter(file, false));
                        reader = new BufferedReader(new FileReader(cacheFile));
                        String s = null;
                        for (int i = 0; i < 2; i++) {
                            if (reader == null) continue;
                            while ((s = reader.readLine()) != null) {
                                writer.write(s);
                                writer.write("\n");
                            }
                            if (currentCacheFilePos == 0) {
                                break;
                            } else {
                                FileUtils.closeSafely(reader);
                                File cacheFile1 = createFile(CACHE_FILE_NAME_TEMP[(currentCacheFilePos - 1) % 2]);
                                if (cacheFile1 != null) {
                                    reader = new BufferedReader(new FileReader(cacheFile1));
                                } else {
                                    reader = null;
                                }
                            }
                        }
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        FileUtils.closeSafely(writer);
                        FileUtils.closeSafely(reader);
                    }
                }
            }
        });

    }


    public static void addLog(String key) {
        if (log != null) {
            log.appendLog(key);
        }

    }

    public static String []  resetLog(String[] filters) {
        if (log != null) {
            String [] var = log.setFilters(filters);
            log.resetOutputLog();
            return var;
        }
        return null;
    }

    private synchronized void resetOutputLog() {
        checkHandler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                cacheLogArray.clear();
                logArray.addToOutputString(KeyLog.this, cacheLogArray);
                count = cacheLogArray.size();//重置保存的数据。
                if (filterFiles != null) {
                    writeToFile(filterFiles, false, cacheLogArray, count);
                }
                count = 0;
                if (callback != null) {
                    callback.onLogAdded(cacheLogArray, true);
                }
            }
        });
    }

    public static void config(KeyLogConfig config) {
        log = new KeyLog(config.maxLine, config.briefLine);
        log.setFilters(config.filters);
    }

    public static KeyLog getKeyLogInstance() {
        return log;
    }

    public String [] setFilters(String[] ar) {
        String [] former = filters;
        filters = ar;
        updateFilterData();
        return former;
    }

    public String[] getFilters() {
        return filters;
    }

    private void updateFilterData() {
        cacheLogArray.clear();
        int size = logArray.size();
        for (int i = 0; i < size; i++) {
            String log = logArray.getItem(i);
            if (filters != null && match(log)) {
                cacheLogArray.addString(log);
            }
        }
        EventBus.onDataArrived(null, DataPool.EVENT_DISPLAY_DATA_ARRIVED);
    }

    private boolean match(String key) {
        if (filters == null || filters.length == 0) {
            return true;
        }
        if (key != null && key.length() > 0) {
            for (String filter : filters) {
                if (filter == null) continue;
                if (key.contains(filter)) {
                    return true;
                }
            }
        }
        return false;
    }


    public int size() {
        return isFull ? maxLine : endPosition;
    }

    public void initFilterFilePath(Context context) {
        fileDir = context.getExternalCacheDir();
    }

    public static void clearLog() {
        if (log != null) {
            log.clearAllLogs();
        }
    }

    private synchronized void clearAllLogs() {
        cacheLogArray.clear();
//        outputArray.clear();
    }

    public interface LogAddedCallback {
        void onLogAdded(LogArray logData, boolean refreshNow);
    }

    public static class LogArray {
        String[] lines; //[record data count for each line]
        int linesHead = 0;
        int linesTail = 0;
        boolean hasCycle = false;
        int maxLine = 0;

        public LogArray(int maxLine) {
            this.maxLine = maxLine;
            lines = new String[maxLine];
        }

        void addString(String s) {
            lines[linesTail] = s;
            linesTail++;
            if (linesTail == maxLine) {
                hasCycle = true;
                linesHead = linesTail = 0;
            }
            if (hasCycle) {
                linesHead = linesTail;
            }
        }

        public int size() {
            if (hasCycle) {
                return maxLine;
            } else {
                return linesTail - linesHead;
            }
        }

        public String getItem(int i) {
            if (maxLine - linesHead <= i) {
                return lines[linesHead + i - maxLine];
            } else {
                return lines[linesHead + i];
            }
        }

        public void toString(StringBuilder stringBuilder, int lineCount) {
            int size = size();
            int end = linesHead + size - 1;
            if (size < lineCount) {
                lineCount = size;
            }

            // p is the latest one
            int p = end - lineCount;
            while (p < end) {
                int ps = (p + maxLine) % maxLine;
                stringBuilder.append(lines[ps]);
                stringBuilder.append("\n");
                p++;
            }
        }

        void addToOutputString(KeyLog keyLog, LogArray array) {
            if (linesHead == 0 && linesTail == 0 && !hasCycle) {//缓存为0
                return;
            }
            int i = linesHead;
            while (true) {
                if (!hasCycle && linesTail == 0) {
                    break;
                }
                String s = lines[i];
                if (keyLog.match(s)) {
                    array.addString(s);
                }
                i++;
                if (i == maxLine) {
                    i = 0;
                }
                if (i == linesTail) {
                    break;
                }
            }
        }

        public void clear() {
            linesHead = linesTail = 0;
            hasCycle = false;
        }
    }


    public synchronized void getBriefDisplay(StringBuilder sb) {
        cacheLogArray.toString(sb, maxBriefLine);
    }

    public LogArray getLogArray() {
        return cacheLogArray;
    }


    private void checkHandler() {
        if (handler == null) {
            HandlerThread thread = new HandlerThread("LensLogcatMonitorHandlerThread");
            thread.start();
            handler = new Handler(thread.getLooper());
        }
    }

    //[quit thread]
    static void disable() {
        if (log != null) {
            Handler handler = log.handler;
            if (handler != null) {
                handler.getLooper().quit();
            }
        }
    }

}
