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
package com.qiyi.lens.ui.viewinfo.json;

import com.qiyi.lens.utils.iface.IJsonCompiler;

public class JsonCompiler implements IJsonCompiler, IJson {
    private StringBuilder stringBuilder = new StringBuilder();
    private boolean started;


    public JsonCompiler() {
        begin();
    }

    private void begin() {
        if (!started) {
            stringBuilder.setLength(0);
            stringBuilder.append('{');
            started = true;
        }
    }

    private void end() {
        if (started) {

            if (stringBuilder.charAt(stringBuilder.length() - 1) == ',') {
                stringBuilder.setLength(stringBuilder.length() - 1);
            }
            stringBuilder.append('}');
            started = false;
        }
    }

    private void appKey(String key) {
        stringBuilder.append('\"');
        stringBuilder.append(key);
        stringBuilder.append('\"');
    }

    public void addPair(String key, int value) {
        appKey(key);
        stringBuilder.append(':');
        stringBuilder.append(value);
        stringBuilder.append(',');
    }

    public void addPair(String key, float value) {
        appKey(key);
        stringBuilder.append(':');
        stringBuilder.append(value);
        stringBuilder.append(',');
    }

    public void addPair(String key, String value) {
        appKey(key);
        stringBuilder.append(':');
        appKey(value);
        stringBuilder.append(',');
    }

    public void addPair(String key, IJson value) {
        appKey(key);
        stringBuilder.append(':');
        value.toJson(stringBuilder);
        stringBuilder.append(',');
    }

    @Override
    public void addPair(String key, int[] array) {
        appKey(key);
        stringBuilder.append(':');
        stringBuilder.append('[');
        if (array != null && array.length > 0) {
            for (int i : array) {
                stringBuilder.append(i);
                stringBuilder.append(',');
            }
            stringBuilder.setLength(stringBuilder.length() - 1);
        }
        stringBuilder.append(']');
        stringBuilder.append(',');
    }

    public String value() {
        end();
        return stringBuilder.toString();
    }

    @Override
    public String toJson() {
        return value();
    }

    @Override
    public void toJson(StringBuilder builder) {
        builder.append(toJson());
    }
}