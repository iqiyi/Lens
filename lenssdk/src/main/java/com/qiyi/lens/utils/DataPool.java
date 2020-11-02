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

import com.qiyi.lens.utils.event.EventBus;

/**
 * used for data set & get
 */
public class DataPool {
    //[启动时间数据]
    public static final int DATA_TYPE_LAUNCH_TIME = 1;
    public static final int DATA_TYPE_ACTIVITY = 2;
    //[抓包数据条数]
    public static final int DATA_TYPE_NET_FILTER_SIZE = 3;
    public static final int EVENT_DNS_SET_CHANGE = 4;

    //设置改变通知（不包含日志设置）
    public static final int EVENT_SETTING_CHANGED = 5;
    //日志设置改变通知
    public static final int EVENT_SETTING_LOG_CHANGED = 6;
    //网络抓包 url分享按钮点击通知
    public static final int EVENT_CLICK_URL_TO_SHARE = 7;
    //网络抓包 URl复制按钮点击通知
    public static final int EVENT_CLICK_URL_TO_COPY = 8;
    //网络抓包 url链接点击通知
    public static final int EVENT_CLICK_URL_TO_DETAIL = 9;
    public static final int EVENT_CLICK_URL_TO_ANALYSE = 10;
    public static final int EVENT_DISPLAY_DATA_ARRIVED = 11;
    //对象监控添加
    public static final int EVENT_WATCH_LIST_ADD = 12;
    //SharedPreferences编辑
    public static final int EVENT_EDIT_SHARED_PREFERENCES = 13;

    // 权限请求数通知
    public static final int EVENT_PERMISSION_REQUEST_SIZE = 14;
    public static final int DATA_PERMISSION_REQUEST = 15;

    // 存储SP 数据
    public static final int EVENT_ID_HOOK_SP_SAVE = 16;

    private SparseArray<Object> pool = new SparseArray<>();

    public DataPool() {
    }

    public synchronized void putData(int id, Object data) {
        pool.put(id, data);
        EventBus.onDataArrived(data, id);
    }

    public synchronized Object getData(int id) {
        return pool.get(id);
    }


    public synchronized Object getDataAsset(int id, Class cls) {

        Object obj = pool.get(id);
        if (obj != null && cls.isAssignableFrom(obj.getClass())) {
            return obj;
        }
        return null;
    }

    public static DataPool obtain() {
        return dataPool;
    }

    public synchronized void clear() {
        pool.clear();
    }

    private static DataPool dataPool = new DataPool();

    public static void pushData(Object data, int dataType) {
        dataPool.putData(dataType, data);
    }


}
