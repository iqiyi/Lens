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
package com.qiyi.lens.utils.configs;

import com.qiyi.lens.ui.abtest.Value;

import java.util.HashMap;

import static com.qiyi.lens.ui.abtest.Value.TYPE_BOOLEAN;
import static com.qiyi.lens.ui.abtest.Value.TYPE_INT;
import static com.qiyi.lens.ui.abtest.Value.TYPE_STRING;

/**
 * AB test 只用于设置一些基础类型数据 & String （避免非法传入一些object 导致内存泄漏问题）
 */
public class ABNTestConfig {
    private static ABNTestConfig config = new ABNTestConfig();
    private static HashMap<String, Value> map = new HashMap<String, Value>();


    public void addABTest(String key, int[] vars) {
        if (vars != null && key != null) {
            Value value = map.get(key);
            if (value != null) {
                if (!value.isSameKind(vars, TYPE_INT, vars.length)) {
                    throw new IllegalStateException("this key " + key + " is aleady registered");
                }
            } else {
                map.put(key, new Value(vars, TYPE_INT, vars.length));
            }

        }
    }

    public void addABTest(String key, String[] vars) {

        if (vars != null && key != null) {
            Value value = map.get(key);
            if (value != null) {
                if (!value.isSameKind(vars, TYPE_STRING, vars.length)) {
                    throw new IllegalStateException("this key " + key + " is aleady registered");
                }
            } else {
                map.put(key, new Value(vars, TYPE_STRING, vars.length));
            }
        }
    }

    //add for boolean
    public void addABTest(String key) {
        if (key != null) {
            Value value = map.get(key);
            if (value == null) {
                map.put(key, new Value(new boolean[]{false, true}, TYPE_BOOLEAN, 2));
            }
        }

    }

    public static ABNTestConfig getInstance() {
        return config;
    }


    public boolean getBoolean(String key) {

        Value value = map.get(key);
        if (value != null) {
            return value.getBoolean(key);
        }
        return false;
    }

    public int getInt(String key) {
        Value value = map.get(key);
        if (value != null) {
            return value.getInt(key);
        }
        return 0;
    }

    public String getString(String key) {
        Value value = map.get(key);
        if (value != null) {
            return value.getString(key);
        }
        return "";
    }

    public String[] getKeys() {
        int size = map.size();
        String[] vars = new String[size];
        int p = 0;
        for (String key : map.keySet()) {
            vars[p++] = key;
        }
        return vars;
    }

    public Value getValue(String key) {
        if (key != null) {
            return map.get(key);
        }
        return null;
    }


    public boolean hasTestData() {
        return !map.isEmpty();
    }
}
