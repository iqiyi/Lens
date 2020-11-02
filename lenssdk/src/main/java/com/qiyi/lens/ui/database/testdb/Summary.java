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
package com.qiyi.lens.ui.database.testdb;

import android.provider.BaseColumns;

import androidx.annotation.IntDef;

import android.util.Pair;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;


@TestDatabase.Table("http_summary")
public class Summary {

    static {
        clear();
    }

    @TestDatabase.Column(value = BaseColumns._ID, primaryKey = true)
    public long id;
    @TestDatabase.Column("status")
    public int status;
    @TestDatabase.Column("code")
    public int code;
    @TestDatabase.Column("url")
    public String url;
    @TestDatabase.Column("query")
    public String query;
    @TestDatabase.Column("host")
    public String host;
    @TestDatabase.Column("method")
    public String method;
    @TestDatabase.Column("protocol")
    public String protocol;
    @TestDatabase.Column("ssl")
    public boolean ssl;
    @TestDatabase.Column("start_time")
    public long start_time;
    @TestDatabase.Column("end_time")
    public long end_time;
    @TestDatabase.Column("request_content_type")
    public String request_content_type;
    @TestDatabase.Column("response_content_type")
    public String response_content_type;
    @TestDatabase.Column("request_size")
    public long request_size;
    @TestDatabase.Column("response_size")
    public long response_size;
    @TestDatabase.Column("request_header")
    public String requestHeader;
    @TestDatabase.Column("response_header")
    public String responseHeader;

    public List<Pair<String, String>> request_header;
    public List<Pair<String, String>> response_header;


    public static List<Summary> queryList() {
        String condition = "order by start_time desc limit " + String.valueOf(1024);
        List<Summary> result = TestDatabase.queryList(Summary.class, null, condition);
        return result;
    }

    public static Summary query(long id) {
        return TestDatabase.queryList(Summary.class, BaseColumns._ID + " = " + String.valueOf(id), "limit 1").get(0);
    }

    public static long insert(Summary summary) {
        return TestDatabase.insert(summary);
    }

    public static void update(Summary summary) {
        TestDatabase.update(summary);
    }

    public static void clear() {
        TestDatabase.delete(Summary.class);
    }

    @IntDef({
            Status.REQUESTING,
            Status.ERROR,
            Status.COMPLETE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {
        int REQUESTING = 0x00;
        int ERROR = 0x01;
        int COMPLETE = 0x02;
    }
}
