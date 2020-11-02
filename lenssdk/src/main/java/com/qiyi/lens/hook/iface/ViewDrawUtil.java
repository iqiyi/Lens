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
package com.qiyi.lens.hook.iface;

import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.transfer.DataTransferManager;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.TimeStampUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by p_hongjcong on 2017/7/14.
 */
public class ViewDrawUtil {
    /**
     * 通过Hook ViewGroup的dispatchDraw  以弹栈的形式来记录当前的绘制元素和绘制深度
     */
    private static String drawClassName = "";
    private static String objectHashCode = "";
    private static long drawBegin = 0;
    private static long drawEnd = 0;
    private static int drawDeep = 0;
    private static StringBuilder drawPath = null;

    // dispatchDraw的栈深，每当栈弹光，说明一次绘制完成
    private static int stackSize = 0;

    public static synchronized void onViewGroup_dispatchDraw_before(String drawClassName, String objectHashCode) {
        //绘制开始--创建对象
        if (stackSize == 0) {
            ViewDrawUtil.drawClassName = drawClassName;
            ViewDrawUtil.objectHashCode = objectHashCode;
            drawBegin = System.currentTimeMillis();
            drawDeep = 0;
            drawPath = new StringBuilder();
        }
        drawPath.append(stackSize).append(",").append(drawClassName).append(";");
        stackSize++;
        if (stackSize > drawDeep) {
            drawDeep = stackSize;
        }
    }

    public static synchronized void onViewGroup_dispatchDraw_after(int vid) {
        stackSize--;
        TimeStampUtil stampUtil = TimeStampUtil.obtainNullable();
        if (stampUtil != null && stampUtil.isTraceEndView(vid)) {
            stampUtil.end();
            DataPool.pushData(stampUtil, DataPool.DATA_TYPE_LAUNCH_TIME);
            reportLaunchTime(stampUtil, vid);
        }
        // 绘制结束--保存对象
        if (stackSize == 0) {
            drawEnd = System.currentTimeMillis();
        }
    }

    private static void reportLaunchTime(TimeStampUtil stampUtil, int viewId) {
        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append(stampUtil.traceEndViewIndex(viewId)).append("|").append(stampUtil.getTotalTime());
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < stampUtil.getSize(); i++) {
            try {
                if (stampUtil.keys[i] != -1) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("tag", stampUtil.tags[i]);
                    jsonObject.put("time", stampUtil.stamps[i] - stampUtil.stamps[0]);
                    jsonArray.put(jsonObject);
                }
            } catch (JSONException e) {
                // ignore
            }
        }
        dataBuilder.append("|").append(jsonArray.toString());
        DataTransferManager.getInstance().report(LensConfig.SP_KEY_LAUNCH_INFO, dataBuilder.toString());
    }
}
