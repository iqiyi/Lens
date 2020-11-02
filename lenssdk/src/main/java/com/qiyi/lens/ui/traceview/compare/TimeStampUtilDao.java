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
package com.qiyi.lens.ui.traceview.compare;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.OSUtils;
import com.qiyi.lens.utils.TimeStampUtil;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TimeStampUtilDao extends SQLiteOpenHelper {
    private static Executor sPool = Executors.newSingleThreadExecutor();
    private static final String TABLE_NAME = "LaunchTime";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_KEYS = "keys";
    private static final String COLUMN_STAMPS = "stamps";
    private static final String COLUMN_CPU_STAMPS = "cpuStamps";
    private static final String COLUMN_TAGS = "tags";
    private static final String COLUMN_THREAD_IDS = "threadIds";
    private static final String COLUMN_SIZE = "size";
    private static final String COLUMN_THREAD_INFO_ARRAY = "threadInfoArray";
    private static final String COLUMN_TOTAL_TIME = "totalTime"; // add at version 4
    private static final String COLUMN_SAVE_TIME = "saveTime";

    public TimeStampUtilDao(@NonNull Context context) {
        super(context, getOrCreateDatabasePath(context), null, 4);
    }

    private static String getOrCreateDatabasePath(@NonNull Context context) {
        if (OSUtils.isPreQ()) {
            String dbPath = Environment.getExternalStorageDirectory() + "/Lens/" + context.getPackageName() + "/Lens.db";
            new File(dbPath).getParentFile().mkdirs();
            return dbPath;
        } else {
            File dbFile = new File(context.getExternalFilesDir("Lens"), "Lens.db");
            return dbFile.getAbsolutePath();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (id INTEGER PRIMARY KEY, keys TEXT,stamps TEXT, cpuStamps TEXT, tags TEXT, threadIds TEXT, size INTEGER, threadInfoArray TEXT, totalTime, INTEGER, saveTime INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD totalTime INTEGER");
        } else {
            db.execSQL("DROP TABLE " + TABLE_NAME);
            onCreate(db);
        }
    }

    public void save(final TimeStampUtil data, final Callback<Void> callback) {
        final Handler handler = new Handler();
        sPool.execute(new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put(COLUMN_KEYS, encode(data.keys));
                values.put(COLUMN_STAMPS, encode(data.stamps));
                values.put(COLUMN_CPU_STAMPS, encode(data.cpuStamps));
                values.put(COLUMN_TAGS, encode(data.tags));
                values.put(COLUMN_THREAD_IDS, encode(data.threadIds));
                values.put(COLUMN_SIZE, data.getSize());
                values.put(COLUMN_THREAD_INFO_ARRAY, encode(data.threadInfoArray));
                values.put(COLUMN_TOTAL_TIME, data.getTotalTime());
                values.put(COLUMN_SAVE_TIME, System.currentTimeMillis());
                getWritableDatabase().insert(TABLE_NAME, null, values);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onResult(null);
                        }
                    }
                });
            }
        });
    }

    public void read(final int id, final Callback<TimeStampUtil> callback) {
        final Handler handler = new Handler();
        sPool.execute(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getReadableDatabase().query(TABLE_NAME, new String[]{COLUMN_KEYS, COLUMN_STAMPS, COLUMN_CPU_STAMPS, COLUMN_TAGS, COLUMN_THREAD_IDS, COLUMN_SIZE, COLUMN_THREAD_INFO_ARRAY}, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
                try {
                    final TimeStampUtil data;
                    if (cursor.moveToNext()) {
                        data = new TimeStampUtil(LensConfig.LAUNCH_TIME_STAMP_NAME);
                        data.keys = decodeIntArray(cursor.getString(cursor.getColumnIndex(COLUMN_KEYS)));
                        data.stamps = decodeLongArray(cursor.getString(cursor.getColumnIndex(COLUMN_STAMPS)));
                        data.cpuStamps = decodeLongArray(cursor.getString(cursor.getColumnIndex(COLUMN_CPU_STAMPS)));
                        data.tags = decodeStringArray(cursor.getString(cursor.getColumnIndex(COLUMN_TAGS)));
                        data.threadIds = decodeLongArray(cursor.getString(cursor.getColumnIndex(COLUMN_THREAD_IDS)));
                        data.threadInfoArray = decodeLongStringHashMap(cursor.getString(cursor.getColumnIndex(COLUMN_THREAD_INFO_ARRAY)));
                        data.setSize(cursor.getInt(cursor.getColumnIndex(COLUMN_SIZE)));
                    } else {
                        data = null;
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResult(data);
                        }
                    });
                } finally {
                    cursor.close();
                }
            }
        });
    }

    public void readAll(final Callback<List<LaunchRecord>> callback) {
        final Handler handler = new Handler();
        sPool.execute(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getReadableDatabase().query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_TOTAL_TIME, COLUMN_SAVE_TIME}, null, null, null, null, COLUMN_SAVE_TIME + " desc");
                final List<LaunchRecord> records = new ArrayList<>();
                try {
                    while (cursor.moveToNext()) {
                        int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                        long time = cursor.getLong(cursor.getColumnIndex(COLUMN_SAVE_TIME));
                        int totalTime = cursor.getInt(cursor.getColumnIndex(COLUMN_TOTAL_TIME));
                        records.add(new LaunchRecord(id, time, totalTime));
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResult(records);
                        }
                    });
                } finally {
                    cursor.close();
                }
            }
        });
    }

    private static int[] decodeIntArray(String input) {
        if (input == null) {
            return null;
        }
        String[] split = input.split(",");
        int[] result = new int[split.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Integer.parseInt(split[i]);
        }
        return result;
    }

    private static long[] decodeLongArray(String input) {
        if (input == null) {
            return null;
        }
        String[] split = input.split(",");
        long[] result = new long[split.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Long.parseLong(split[i]);
        }
        return result;
    }

    private static String[] decodeStringArray(String input) {
        if (input == null) {
            return null;
        }
        String[] split = input.split(",");
        String[] result = new String[split.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = URLDecoder.decode(split[i]);
            if (result[i].equals("null")) {
                result[i] = null;
            }
        }
        return result;
    }

    private static HashMap<Long, String> decodeLongStringHashMap(String input) {
        if (input == null) {
            return null;
        }
        String[] split = input.split(",");
        HashMap<Long, String> map = new HashMap<>(split.length);
        for (int i = 0; i < split.length; i++) {
            String key = split[i].split("=")[0];
            String value = split[i].split("=")[1];
            map.put(Long.parseLong(key), URLDecoder.decode(value));
        }
        return map;
    }

    private static String encode(int[] inputs) {
        if (inputs == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int input : inputs) {
            sb.append(input).append(',');
        }
        return sb.toString();
    }

    private static String encode(long[] inputs) {
        if (inputs == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (long input : inputs) {
            sb.append(input).append(',');
        }
        return sb.toString();
    }

    private static String encode(String[] inputs) {
        if (inputs == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String input : inputs) {
            sb.append(URLEncoder.encode(input == null ? "null" : input)).append(',');
        }
        return sb.toString();
    }

    private static String encode(Map<Long, String> inputs) {
        if (inputs == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Long, String> e : inputs.entrySet()) {
            sb.append(e.getKey()).append('=').append(URLEncoder.encode(e.getValue())).append(',');
        }
        return sb.toString();
    }

    public interface Callback<T> {
        void onResult(T data);
    }
}
