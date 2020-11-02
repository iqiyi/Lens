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
import android.database.sqlite.SQLiteException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseResult {

    /**
     * Maximum length of a BLOB field before we stop trying to interpret it and just
     * return {@link #UNKNOWN_BLOB_LABEL}
     */
    private static final int MAX_BLOB_LENGTH = 512;

    /**
     * Label to use when a BLOB column cannot be converted to a string.
     */
    private static final String UNKNOWN_BLOB_LABEL = "{blob}";

    List<String> columnNames;

    List<List<String>> values;

    Error sqlError;

    public static class Error {
        String message;
        public int code;
    }

    void transformRawQuery() throws SQLiteException {
    }

    void transformSelect(Cursor result) throws SQLiteException {
        columnNames = Arrays.asList(result.getColumnNames());
        values = wrapRows(result);
    }

    void transformInsert(long insertedId) throws SQLiteException {
    }

    void transformUpdateDelete(int count) throws SQLiteException {
    }


    private static List<List<String>> wrapRows(Cursor cursor) {
        List<List<String>> result = new ArrayList<>();

        final int numColumns = cursor.getColumnCount();
        while (cursor.moveToNext()) {
            ArrayList<String> flatList = new ArrayList<>();
            for (int column = 0; column < numColumns; column++) {
                switch (cursor.getType(column)) {
                    case Cursor.FIELD_TYPE_NULL:
                        flatList.add(null);
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        flatList.add(String.valueOf(cursor.getLong(column)));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        flatList.add(String.valueOf(cursor.getDouble(column)));
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        flatList.add(blobToString(cursor.getBlob(column)));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                    default:
                        flatList.add(cursor.getString(column));
                        break;
                }
            }
            result.add(flatList);
        }
        return result;
    }

    private static String blobToString(byte[] blob) {
        if (blob.length <= MAX_BLOB_LENGTH) {
            if (fastIsAscii(blob)) {
                try {
                    return new String(blob, "US-ASCII");
                } catch (UnsupportedEncodingException ignore) {
                }
            }
        }
        return UNKNOWN_BLOB_LABEL;
    }

    private static boolean fastIsAscii(byte[] blob) {
        for (byte b : blob) {
            if ((b & ~0x7f) != 0) {
                return false;
            }
        }
        return true;
    }
}
