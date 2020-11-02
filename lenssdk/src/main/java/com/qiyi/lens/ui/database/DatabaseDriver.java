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
package com.qiyi.lens.ui.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import com.qiyi.lens.ui.database.protocol.IDriver;
import com.qiyi.lens.ui.database.protocol.IProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DatabaseDriver implements IDriver<DatabaseDescriptor> {

    private static final String[] EXTRA_FILE_SUFFIXES = new String[]{"-journal", "-shm", "-uid", "-wal"};

    private final IProvider mProvider;

    public DatabaseDriver(IProvider mProvider) {
        this.mProvider = mProvider;
    }

    @Override
    public List<DatabaseDescriptor> getDatabaseNames() {
        ArrayList<DatabaseDescriptor> databases = new ArrayList<>();
        List<File> potentialDatabaseFiles = mProvider.getDatabaseFiles();
        Collections.sort(potentialDatabaseFiles);
        Iterable<File> tidiedList = tidyDatabaseList(potentialDatabaseFiles);
        for (File database : tidiedList) {
            databases.add(new DatabaseDescriptor(database));
        }
        return databases;
    }

    private static List<File> tidyDatabaseList(List<File> databaseFiles) {
        //Set<File> originalAsSet = new HashSet<>(databaseFiles);
        List<File> tidiedList = new ArrayList<>();
        for (File databaseFile : databaseFiles) {
            String databaseFilename = databaseFile.getPath();
            //String sansSuffix = removeSuffix(databaseFilename, EXTRA_FILE_SUFFIXES);
            //if (sansSuffix.equals(databaseFilename) || !originalAsSet.contains(new File(sansSuffix))) {
            //tidiedList.add(databaseFile);
            //}
            boolean isDbFile = filterDbFile(databaseFilename, EXTRA_FILE_SUFFIXES);
            if (isDbFile) {
                tidiedList.add(databaseFile);
            }
        }
        return tidiedList;
    }

    private static boolean filterDbFile(String str, String[] suffixesToRemove) {
        boolean isDb = true;
        for (String suffix : suffixesToRemove) {
            if (str.contains(suffix) || !str.endsWith(".db")) {
                isDb = false;
                break;
            }
        }
        return isDb;
    }

    private static String removeSuffix(String str, String[] suffixesToRemove) {
        for (String suffix : suffixesToRemove) {
            if (str.endsWith(suffix)) {
                return str.substring(0, str.length() - suffix.length());
            }
        }
        return str;
    }

    @Override
    public List<String> getTableNames(DatabaseDescriptor databaseDesc) throws SQLiteException {
        SQLiteDatabase database = openDatabase(databaseDesc);
        try {
            Cursor cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type IN (?/*, ?*/)",
                    new String[]{"table"/*, "view"*/});
            try {
                List<String> tableNames = new ArrayList<>();
                while (cursor.moveToNext()) {
                    tableNames.add(cursor.getString(0));
                }
                return tableNames;
            } finally {
                cursor.close();
            }
        } finally {
            database.close();
        }
    }

    @Override
    public void executeSQL(DatabaseDescriptor databaseDesc, String query, DatabaseResult result)
            throws SQLiteException {
        SQLiteDatabase database = openDatabase(databaseDesc);
        try {
            String firstWordUpperCase = getFirstWord(query).toUpperCase();
            switch (firstWordUpperCase) {
                case "UPDATE":
                case "DELETE":
                    executeUpdateDelete(database, query, result);
                    break;
                case "INSERT":
                    executeInsert(database, query, result);
                    break;
                case "SELECT":
                case "PRAGMA":
                case "EXPLAIN":
                    executeSelect(database, query, result);
                    break;
                default:
                    executeRawQuery(database, query, result);
                    break;
            }
        } finally {
            database.close();
        }
    }

    private static String getFirstWord(String s) {
        s = s.trim();
        int firstSpace = s.indexOf(' ');
        return firstSpace >= 0 ? s.substring(0, firstSpace) : s;
    }

    private void executeUpdateDelete(SQLiteDatabase database, String query, DatabaseResult result) {
        SQLiteStatement statement = database.compileStatement(query);
        int count = statement.executeUpdateDelete();
        result.transformUpdateDelete(count);
    }

    private void executeInsert(SQLiteDatabase database, String query, DatabaseResult result) {
        SQLiteStatement statement = database.compileStatement(query);
        long count = statement.executeInsert();
        result.transformInsert(count);
    }

    private void executeSelect(SQLiteDatabase database, String query, DatabaseResult result) {
        Cursor cursor = database.rawQuery(query, null);
        try {
            result.transformSelect(cursor);
        } finally {
            cursor.close();
        }
    }

    private void executeRawQuery(SQLiteDatabase database, String query, DatabaseResult result) {
        database.execSQL(query);
        result.transformRawQuery();
    }

    private SQLiteDatabase openDatabase(DatabaseDescriptor databaseDesc) throws SQLiteException {
        return mProvider.openDatabase(databaseDesc.file);
    }

}
