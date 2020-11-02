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
package com.qiyi.lens.ui.setting;

import android.content.Context;

import com.qiyi.lens.utils.DefaultHookImpl;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lenssdk.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * 剥离出来的设置开关部分
 * 1) Name
 * 2) SP key
 * 3) action : 在SwitchAction 中处理。  客户端优先处理；参考：addCustomBlockEntrance
 * SP 的值在SDK 中自行修改。
 */
public class SettingDataConfig implements SettingGridAdapter.IConfigDataBuilder {
    private String[] spKeys = {
            LensConfig.SP_KEY_DEVICE_INFO, // 设备信息
            LensConfig.SP_LENS_KEY_VIEW_TOUCH_ENABLE, // 触摸i日志
//            LensConfig.SP_KEY_FRAME_INFO, // 帧率信息
            LensConfig.SP_KEY_LOG_INFO, // 日志信息
            LensConfig.SP_KEY_LAUNCH_INFO, // 启动信息
            LensConfig.SP_KEY_NETWORK_INFO, // 网络分析
            LensConfig.SP_KEY_ACTIVITY_INFO,// 页面分析
            LensConfig.SP_KEY_DISPLAY_INFO,// 数据展示
            LensConfig.SP_KEY_WATCH_INFO,// 对象监控
            LensConfig.SP_LENS_KEY_PERMISSION_ENABLE,// 权限监控
            LensConfig.SP_KEY_CRASH_INFO, // 崩溃抓取
            LensConfig.SP_KEY_TEST_HOOK, // HOOK TEST
            null,
            LensConfig.SP_KEY_FLOAT_WINDOW_MODE // 启用lens 浮动窗口模式， 默认关闭
    };

    private int[] resArray = {
            R.string.lens_block_device_info_title,
            R.string.lens_block_view_touch,
//            R.string.lens_block_frame_title,
            R.string.lens_block_log_title,
            R.string.lens_block_launch_title,
            R.string.lens_block_network_title,
            R.string.lens_block_activity_title,
            R.string.lens_block_display,
            R.string.lens_block_watch,
            R.string.lens_block_permission_title,
            R.string.lens_crash_info,
            R.string.lens_test_hook,
            R.string.lens_block_enable_debug,
            R.string.lens_block_floating_pannel
    };


    private String[] spKeysNOHook = {
            LensConfig.SP_KEY_DEVICE_INFO, // 设备信息
//            LensConfig.SP_KEY_FRAME_INFO, // 帧率信息
            LensConfig.SP_KEY_ACTIVITY_INFO,// 页面分析
            LensConfig.SP_KEY_DISPLAY_INFO,// 数据展示
            LensConfig.SP_KEY_WATCH_INFO,// 对象监控
            LensConfig.SP_KEY_CRASH_INFO, // 崩溃抓取
            null,
            LensConfig.SP_KEY_FLOAT_WINDOW_MODE // 启用lens 浮动窗口模式， 默认关闭
    };

    private int[] resArrayNoHook = {
            R.string.lens_block_device_info_title,
//            R.string.lens_block_frame_title,
            R.string.lens_block_activity_title,
            R.string.lens_block_display,
            R.string.lens_block_watch,
            R.string.lens_crash_info,
            R.string.lens_block_enable_debug,
            R.string.lens_block_floating_pannel
    };



    private String[] getKeys(){
        if(LensConfig.getInstance().getHookFrameWorkImpl() instanceof DefaultHookImpl){
            return spKeysNOHook;
        }
        return spKeys;
    }


    private int[] getIds(){
        if(LensConfig.getInstance().getHookFrameWorkImpl() instanceof DefaultHookImpl){
            return resArrayNoHook;
        }
        return resArray;
    }


    @Override
    public EventViewItem[] buildItems(Context context, ConfigEventCallBack changed, int columCount) {
        //当hook 崩溃的时候, 关闭选项 而不是直接不展示;
        String[] spKeys = getKeys();
        int[] resArray = getIds();
        String[] names = LensConfig.getInstance().getCustomBlockNames();
        if (names != null && names.length > 0) {
            // merge with custom
            int size = names.length + resArray.length;
            String[] var = toStringArray(context, resArray);
            String[] comp = new String[size];
            System.arraycopy(var, 0, comp, 0, var.length);
            System.arraycopy(names, 0, comp, var.length, names.length);

            String[] kes = new String[size];
            System.arraycopy(spKeys, 0, kes, 0, spKeys.length);
            //add custom keys]
            int p = spKeys.length;
            int offset = p;
            while (p < size) {
                try {
                    kes[p] = URLEncoder.encode(names[p - offset], "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    kes[p] = names[p];
                }
                p++;
            }
            int[] events = new int[size];
            System.arraycopy(resArray, 0, events, 0, resArray.length);
            return loadItems(context, changed, columCount, kes, comp, events);
        } else {
            return loadItems(context, changed, columCount, spKeys, resArray);
        }

    }


    private String[] toStringArray(Context context, int[] nameArray) {
        String[] names = new String[nameArray.length];
        int p = 0;
        int lens = nameArray.length;
        while (p < lens) {
            names[p] = context.getResources().getString(nameArray[p]);
            p++;
        }
        return names;
    }


    private EventViewItem[] loadItems(Context context, ConfigEventCallBack changed, int columCount,
                                      String[] spKeys, int[] nameArray) {
        String[] names = toStringArray(context, nameArray);
        return loadItems(context, changed, columCount, spKeys, names, nameArray);
    }


    /**
     * @param eventIds : 用于处理对应事件的。内置的部分 event id 就是 string res ； 自定义的部分 -1； 使用string 传递
     */
    private EventViewItem[] loadItems(Context context, ConfigEventCallBack changed, int columCount,
                                      String[] spKeys, String[] resArray, int[] eventIds) {
        int count = Math.min(spKeys.length, resArray.length);
        int off = count % columCount;
        int size = count;
        if (off > 0) {
            size = count + (columCount - off);
        }
        SettingItem[] items = new SettingItem[size];
        int i = 0;
        while (i < count) {
            if (spKeys[i] == null) {// 先特殊处理 不具有通用性的代码
                items[i] = new SDKDebugItem(context, spKeys[i], resArray[i], eventIds[i], changed);
            } else {
                items[i] = new SettingItem(context, spKeys[i], resArray[i], eventIds[i], changed);
            }
            i++;
        }

        return items;
    }


}
