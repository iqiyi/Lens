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

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * when a thread is started , this manager will be notified
 */
public class LensHookListenerManager {

    private static Map<String, ArrayList<LensHookListener>> sHookListeners = new HashMap<>();

    public synchronized static void addHookListener(String methodName, LensHookListener listener) {
        if (TextUtils.isEmpty(methodName) || listener == null) {
            return;
        }
        if (!sHookListeners.containsKey(methodName)) {
            sHookListeners.put(methodName, new ArrayList<LensHookListener>());
        }
        ArrayList<LensHookListener> arrayList = sHookListeners.get(methodName);
        if (arrayList != null) {
            arrayList.add(listener);
        }
    }

    public synchronized static void removeHookListener(String methodName, LensHookListener listener) {
        if (TextUtils.isEmpty(methodName) || listener == null) {
            return;
        }
        ArrayList<LensHookListener> arrayList = sHookListeners.get(methodName);
        if (arrayList != null) {
            arrayList.remove(listener);
        }
    }

    public synchronized static void notifyHookInvoked(String methodName, Object... args) {
        if (TextUtils.isEmpty(methodName)) {
            return;
        }
        ArrayList<LensHookListener> arrayList = sHookListeners.get(methodName);
        if (arrayList != null) {
            for (LensHookListener listener : arrayList) {
                listener.onInvoke(methodName, args);
            }
        }
    }

}
