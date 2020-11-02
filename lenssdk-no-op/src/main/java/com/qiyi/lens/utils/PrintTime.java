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

import android.os.Debug;

public class PrintTime {
    public long ct;
    public long st;
    static PrintTime time = new PrintTime();
    public void begin(){
        ct = Debug.threadCpuTimeNanos();
        st = System.currentTimeMillis();
    }

    public void addStamp(String key){
        long ctb = Debug.threadCpuTimeNanos();
        long stb = System.currentTimeMillis();
        android.util.Log.d("PrintTime",key+ " "+ (ctb - ct)/1000000 +" "+
                (stb - st));
        ct = ctb;
        st = stb;
    }
    public static void reset(){
        time.begin();
    }
    public static void stamp(String key){
        time.addStamp(key);
    }
}
