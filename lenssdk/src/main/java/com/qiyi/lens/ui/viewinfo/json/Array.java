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

public class Array implements IJson {
    private IJson mArray[];

    public Array(IJson[] array) {
        mArray = array;
    }

    @Override
    public String toJson() {
        StringBuilder stringBuilder = new StringBuilder();
        buildJson(stringBuilder);
        return stringBuilder.toString();
    }

    private void buildJson(StringBuilder stringBuilder) {
        if (mArray != null && mArray.length > 0) {
            stringBuilder.append('[');
            for (IJson json : mArray) {
                stringBuilder.append(json.toJson());
                stringBuilder.append(',');
            }
            stringBuilder.setLength(stringBuilder.length() - 1);
            stringBuilder.append(']');
        } else {
            stringBuilder.append("[]");
        }
    }

    @Override
    public void toJson(StringBuilder stringBuilder) {
        buildJson(stringBuilder);
    }
}
