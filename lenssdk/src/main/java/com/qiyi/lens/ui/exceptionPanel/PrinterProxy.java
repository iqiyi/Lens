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
package com.qiyi.lens.ui.exceptionPanel;

import android.os.Looper;
import android.util.Printer;

import com.qiyi.lens.utils.TimeStampUtil;

/**
 * 先这样, 有需求再实现proxy 吧. 现在先直接替换掉.
 * fix bug: 在lens 启动前就有日志的情况下, 就会直接导致  监听失效
 */
public class PrinterProxy implements Printer {
    private long timeEllipse;
    private String[] ar;
    private boolean started;
    private final String TAG = "Dispatching to ";
    private final int TAG_LENGTH = TAG.length();

    @Override
    public void println(String x) {
        if (x != null && x.length() > 0) {
            char c = x.charAt(0);
            if (c == '<') {
//                int gap = (int) (System.currentTimeMillis() - timeElapsed);
                String name;
                if (ar != null && ar.length > 2) {
                    int len = ar.length;
                    String a = ar[len - 1];
                    String b = ar[len - 2]; // call back info
                    if (b != null && b.length() > 0 && !b.startsWith("null")) {
                        name = b + " " + a;
                    } else if (ar.length > 4) { // target info
                        name = ar[len - 4];
                    } else {
                        int index = x.indexOf(TAG);
                        if (index == -1) {
                            name = x; // 全部信息
                        } else { // dispatching 之后的信息
                            name = x.substring(x.indexOf(TAG) + TAG_LENGTH);
                        }
                    }
                } else {
                    name = " -- ";
                }

                boolean finished = TimeStampUtil.addNullableStamp(1, timeEllipse, name);
                if (finished) {
                    started = true;
                    TimeStampUtil.addNullableStamp(-1, System.currentTimeMillis(), name);
                } else if (started) { //
                    Looper.getMainLooper().setMessageLogging(null);
                }

            } else if (c == '>') {
                timeEllipse = System.currentTimeMillis();
                ar = x.split(" ");

            }


        }

    }
}
