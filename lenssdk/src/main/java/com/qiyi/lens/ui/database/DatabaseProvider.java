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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;

import com.qiyi.lens.ui.database.protocol.IProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DatabaseProvider implements IProvider {
    private Context context;

    DatabaseProvider(Context context) {
        this.context = context;
    }

    @Override
    public List<File> getDatabaseFiles() {
        List<File> databaseFiles = new ArrayList<>();
        for (String databaseName : context.databaseList()) {
            databaseFiles.add(context.getDatabasePath(databaseName));
        }
        return databaseFiles;
    }

    @Override
    public SQLiteDatabase openDatabase(File databaseFile) throws SQLiteException {
        return performOpen(databaseFile, checkIfCanOpenWithWAL(databaseFile));
    }

    private int checkIfCanOpenWithWAL(File databaseFile) {
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            File walFile = new File(databaseFile.getParent(), databaseFile.getName() + "-wal");
            if (walFile.exists()) {
                flags |= SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING;
            }
        }
        return flags;
    }

    private SQLiteDatabase performOpen(File databaseFile, int options) {
        int flags = SQLiteDatabase.OPEN_READWRITE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if ((options & SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING) != 0) {
                flags |= SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING;
            }
        }
        return SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath(),
                null, flags);
    }
}
