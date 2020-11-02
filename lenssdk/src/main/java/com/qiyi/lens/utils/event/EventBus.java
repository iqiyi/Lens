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
package com.qiyi.lens.utils.event;

import android.util.SparseArray;

import java.util.LinkedList;
import java.util.List;

public class EventBus {
    private static SparseArray<List<DataCallBack>> dataArray = new SparseArray<>();

    /**
     * @param callBack ：回调对象
     * @param dataType : 关注的某种数据类型
     */
    public static synchronized void registerEvent(DataCallBack callBack, int dataType) {
        register(dataType, callBack);
    }

    private static void register(int dataType, DataCallBack callBack) {
        List<DataCallBack> list = getDataCallBack(dataType);
        if (list == null) {
            list = new LinkedList<>();
            list.add(callBack);
            dataArray.put(dataType, list);
        } else if (!list.contains(callBack)) {
            list.add(callBack);
        }
    }

    public static synchronized void registerEvents(DataCallBack callBack, int... dataTypes) {
        if (dataTypes != null) {
            for (int dataType : dataTypes) {
                register(dataType, callBack);
            }
        }
    }

    public static synchronized void unRegisterEvent(DataCallBack callBack, int dataType) {
        List<DataCallBack> list = getDataCallBack(dataType);
        if (list != null) {
            list.remove(callBack);
        }
    }

    @Deprecated // Call pushData
    public static synchronized void onDataArrived(Object data, int type) {
        List<DataCallBack> list = getDataCallBack(type);
        if (list != null) {
            for (DataCallBack callBack : list) {
                callBack.onDataArrived(data, type);
            }
        }
    }

    // 推送数据
    public static synchronized void pushData(int type, Object data) {
        List<DataCallBack> list = getDataCallBack(type);
        if (list != null) {
            for (DataCallBack callBack : list) {
                callBack.onDataArrived(data, type);
            }
        }
    }

    private static List<DataCallBack> getDataCallBack(int type) {
        return dataArray.get(type);
    }

}
