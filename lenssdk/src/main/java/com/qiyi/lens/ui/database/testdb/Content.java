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

@TestDatabase.Table("http_content")
public class Content {

    static {
        clear();
    }

    @TestDatabase.Column(value = BaseColumns._ID, primaryKey = true)
    public long id;
    @TestDatabase.Column("requestBody")
    public String requestBody;
    @TestDatabase.Column("responseBody")
    public String responseBody;

    public static Content query(long id) {
        return TestDatabase.queryList(Content.class, BaseColumns._ID + " = " + String.valueOf(id), "limit 1").get(0);
    }

    public static long insert(Content content) {
        return TestDatabase.insert(content);
    }

    public static void update(Content content) {
        TestDatabase.update(content);
    }

    public static void clear() {
        TestDatabase.delete(Content.class);
    }
}
