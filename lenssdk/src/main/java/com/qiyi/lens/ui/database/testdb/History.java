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

import java.util.List;

@TestDatabase.Table("activity_history")
public class History {

    static {
        clear();
    }

    @TestDatabase.Column(value = BaseColumns._ID, primaryKey = true)
    public int id;
    @TestDatabase.Column("createTime")
    public long createTime;
    @TestDatabase.Column("activity")
    public String activity;
    @TestDatabase.Column("event")
    public String event;

    public static void clear() {
        TestDatabase.delete(History.class);
    }

    public static void insert(History history) {
        TestDatabase.insert(history);
    }

    public static List<History> query() {
        String condition = "order by createTime desc";
        return TestDatabase.queryList(History.class, null, condition);
    }
}
