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
package com.qiyi.lens.hook;

import android.os.Build;

/**
 * @author p_xcli
 * Created on 2018/1/20.
 */

public class WhiteList {
    /**
     * Whether the OS is powered by YunOS,
     * the list is still far from being completed.
     *
     * @return
     */
    public static boolean isYunOS() {
        return Build.DISPLAY.startsWith("Flyme");
    }
}
