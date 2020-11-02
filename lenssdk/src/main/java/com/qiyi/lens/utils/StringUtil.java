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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    /**
     * count number position in string
     *
     * @param strInput the string to count number position
     * @return int[] int[0]:start number index;int[1]:end number index. -1 if not contain number
     */
    public static int[] countNumberPosition(String strInput) {
        int[] numPosition = {-1, -1};
        if (StringUtil.isNullOrEmpty(strInput)) {
            return numPosition;
        }

        for (int i = 0; i < strInput.length(); i++) {
            if (strInput.charAt(i) >= 48 && strInput.charAt(i) <= 57) {
                if (numPosition[0] > i || numPosition[0] == -1) {
                    numPosition[0] = i;
                }

                if (numPosition[1] < i) {
                    numPosition[1] = i;
                }
            }
        }

        return numPosition;
    }

    /**
     * Check if inputString is null or empty.
     */
    public static boolean isNullOrEmpty(String inputString) {
        if (null == inputString) {
            return true;
        } else
            return inputString.trim().equals("");

    }

    /**
     * Check if all string in strings is null or empty.
     */
    public static boolean isAllNullOrEmpty(String... strings) {
        if (strings == null)
            return true;
        for (String string : strings) {
            if (!isNullOrEmpty(string)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断所有的字符串
     * 都不为空，返回true
     * 有一个为空，返回false
     */
    public static boolean isAllNotNullOrEmpty(String... strings) {
        if (strings == null || strings.length == 0) {
            return false;
        }
        for (String string : strings) {
            if (isNullOrEmpty(string)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get character number of string, a non-ascii character will be count 2 size.
     */
    public static int getCharacterNum(final String content) {
        if (null == content || "".equals(content)) {
            return 0;
        } else {
            return (content.length() + getChineseNum(content));
        }
    }

    /**
     * Get non-ascii character number of string.
     */
    public static int getChineseNum(String s) {
        if (s == null) {
            return 0;
        }
        int num = 0;
        char[] myChar = s.toCharArray();
        for (char c : myChar) {
            if ((char) (byte) c != c) {
                num++;
            }
        }
        return num;
    }

    /**
     * @return empty if inputString is null; otherwise return inputString self.
     */
    public static String getRealOrEmpty(String inputString) {
        return isNullOrEmpty(inputString) ? "" : inputString;
    }

    public static String getMaxLengthString(String string, int maxLenght) {
        if (StringUtil.isNullOrEmpty(string) || string.length() <= maxLenght || maxLenght < 0) {
            return string;
        }
        return string.substring(0, maxLenght);
    }


    public static String getHost(String url) {
        String host = null;

        if (url != null && url.length() > 0) {
            Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");
            Matcher matcher = p.matcher(url);

            if (matcher.find()) {
                host = matcher.group();
            }

        }
        return host;
    }


}
