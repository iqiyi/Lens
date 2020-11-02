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
package com.qiyi.lens.utils;

import android.util.SparseArray;

/**
 * 在字符串前或者后添加空格，补齐长度
 */
public class TextWidthFixer {
    private static TextWidthFixer sInstance = new TextWidthFixer();
    private SparseArray<String> cachedFixer = new SparseArray<>();

    private TextWidthFixer() {
    }

    public static TextWidthFixer getInstance() {
        return sInstance;
    }

    public String fix(String str, int targetSize) {
        return fix(str, targetSize, false);
    }

    public String fix(String str, int targetSize, boolean append) {
        if (str == null || str.length() >= targetSize) {
            return str;
        }
        int diff = targetSize - str.length();
        String fixer = cachedFixer.get(diff);
        if (fixer == null) {
            fixer = createFixer(diff);
            cachedFixer.put(diff, fixer);
        }
        return append ? str + fixer : fixer + str;
    }

    private String createFixer(int diff) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < diff; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
