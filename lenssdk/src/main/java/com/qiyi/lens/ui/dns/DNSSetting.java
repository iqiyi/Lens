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
package com.qiyi.lens.ui.dns;

import android.content.Context;
import android.os.Build;

import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.SharedPreferenceUtils;
import com.qiyi.lens.utils.StringUtil;
import com.qiyi.lens.utils.Utils;
import com.qiyi.lens.utils.iface.IHookFrameWork;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;

//todo : 这个类目前只是临时的简单实现。 需要调整具体实现方案。


/**
 * 支持功能： ip 直连接 ； DNS 修改  ； 抓去 URL
 * 定义：
 * 1）ip 直连接 或者 NDS 修改 都定义为 DNS setting 使能；
 * 2）抓包 定义为 host 或 逻辑过滤。
 */
public class DNSSetting {

    public static final String SP_KEY_DNS_INFO = "SP_KEY_DNS_INFO";
    public static final String SP_KEY_FILTER_INFO = "SP_KEY_FILTER_INFO";
    public static final String SP_KEY_DNS_ENABLE = "SP_KEY_DNS_ENABLE";
    public static final String SP_KEY_FILTER_ENABLE = "SP_KEY_FILTER_ENABLE";
    public static final String SP_KEY_DIRECT_LINK_ENABLE = "SP_IP_DIRECT_LINK_ENABLE";
    public static final String SP_KEY_FILTER_WHITE_LIST = "SP_KEY_FILTER_WHITE_LIST";

    //[用于存储 DNS setting 的数据结构]
    private static HashMap<String, String> map = new HashMap<>();
    //[用于存取抓取到的 URL 的数据结构。 需要新增抓包过滤项目，需要放置数据过多溢出，设置做多保留的条数。]
//    static LinkedList<HttpUrl> urls = new LinkedList<>();
    private static boolean isIPMapLoaded;
    private static boolean isDNSHooked;
    private static boolean isFilterEnabled;
    private static boolean isFilterHooked;
    private static boolean isTestModeEnabled;
    private static int dnsSettingMode = 0;//[0, nds 设置  1 ： url 替换]
    private static HttpUrlConnection urlConnector = new HttpUrlConnection();
    private static boolean enableWhiteList;

    private static LinkedList<String> filteredMap = new LinkedList<>();
    private static IHookFrameWork hookImpl = LensConfig.getInstance().getHookFrameWorkImpl();

    /**
     * 测试环境使能
     */
    public static void enableDNSSetting(Context context) {
        if (isTestModeEnabled) {
            return;
        }
        isTestModeEnabled = true;

        loadHostIPMaps(context);

        if (dnsSettingMode == 0) {
            hookDNS();
        } else {
            hookURL();
        }

    }


    private static void loadHostIPMaps(Context context) {
        //load data

        if (!isIPMapLoaded) {
            isIPMapLoaded = true;
            String data = SharedPreferenceUtils.getSharedPreferences(SP_KEY_DNS_INFO, context);
            if (!data.isEmpty()) {
                String[] ar = Utils.string2Array(data);
                updateDNSMap(ar);
            }
        }
    }

    private static void hookDNS() {

        if (isDNSHooked) {
            return;
        }

        hookImpl.doHookDefault("com.qiyi.lens.hooks.HookOKHttp");
        isDNSHooked = true;
        //[DNS hook : will need hook this class]
        if (Build.VERSION.SDK_INT != 21) {
            hookImpl.doHookDefault("com.qiyi.lens.hooks.HttpUrlConnectionHook");
            isFilterHooked = true;
        }
    }

    public static void updateDNSMap(String[] ar) {
        map.clear();
        if (ar != null && ar.length > 0) {
            int count = ar.length >> 1;
            count = count << 1;
            for (int i = 0; i < count; ) {

                String s1 = ar[i++];
                String s2 = ar[i++];
                if (Utils.isValidIP(s2) && s1 != null && s1.length() > 0) {
                    map.put(s1, s2);
                }
            }
        }
    }

    public static void updateFilterMap(String[] ar) {
        filteredMap.clear();
        if (ar != null && ar.length > 0) {
            filteredMap.addAll(Arrays.asList(ar));
        }
    }

    public static void enableFilterSetting(Context context) {
        if (isFilterEnabled) {
            return;
        }
        String data = SharedPreferenceUtils.getSharedPreferences(SP_KEY_FILTER_INFO, context);
        if (data.length() > 0) {
            String[] ar = Utils.string2Array(data);
            updateFilterMap(ar);
        }
        enableWhiteList = SharedPreferenceUtils.getSharedPreferences(SP_KEY_FILTER_WHITE_LIST, context, false);
        if (dnsSettingMode == 1) { //[ ip replace]
            addFiltersByIPDirectLink();
        }
        isFilterEnabled = true;
        hookURL();
    }

    private static void addFiltersByIPDirectLink() {
        LinkedList<String> sr = new LinkedList<>();
        for (String host : filteredMap) {
            String ip = map.get(host);
            if (!filteredMap.contains(ip)) {
                if (ip != null) {
                    sr.add(ip);
                }
            }
        }

        if (!sr.isEmpty()) {
            filteredMap.addAll(sr);
        }

    }

    private static void hookURL() {

        if (!isFilterHooked) {
            hookImpl.doHookDefault("com.qiyi.lens.hooks.HookOKHttpRequest");
            isFilterHooked = true;
            // URL hook : will need hook this class]
            if (Build.VERSION.SDK_INT != 21) {
                hookImpl.doHookDefault("com.qiyi.lens.hooks.HttpUrlConnectionHook");
                isFilterHooked = true;
            }
        }
    }


    public static void disableDNSSetting() {
        if (isTestModeEnabled) {
            map.clear();
        }
        isTestModeEnabled = false;

    }

    public static void disableDNSSetting(Context context) {
        SharedPreferenceUtils.getSharedPreferences(SP_KEY_DNS_ENABLE, context, false);
        disableDNSSetting();
    }

    public static void disableFilterSetting() {
        if (isFilterEnabled) {
            filteredMap.clear();
        }
        isFilterEnabled = false;
    }

    public static HashMap<String, String> dnsMap() {
        return map;
    }

    public static void onRequestUrl(HttpUrl url, Map<String, List<String>> map, String method, Object body) {
        if (!enableWhiteList || filteredMap.contains(url.host())) {
            urlConnector.add(url, map, method, body);
            int size = urlConnector.size();
            DataPool.obtain().putData(DataPool.DATA_TYPE_NET_FILTER_SIZE, size);
        }
    }


    public static void onRequestUrl(String url, Map<String, List<String>> map, String method, Object body) {
        String host = StringUtil.getHost(url);
        if (!enableWhiteList || filteredMap.contains(host)) {
            urlConnector.add(url, host, map, method, body);
            int size = urlConnector.size();
            DataPool.obtain().putData(DataPool.DATA_TYPE_NET_FILTER_SIZE, size);
        }
    }


    public static boolean isDNSEnabled(Context context) {
        return SharedPreferenceUtils.getSharedPreferences(SP_KEY_DNS_ENABLE, context, false);
    }

    public static boolean isFilterEnabled(Context context) {
        return SharedPreferenceUtils.getSharedPreferences(SP_KEY_FILTER_ENABLE, context, false);
    }

    public static boolean isWhiteListEnabled(Context context) {
        return SharedPreferenceUtils.getSharedPreferences(SP_KEY_FILTER_WHITE_LIST, context, false);
    }


    public static boolean isFilterEnabled() {
        return isFilterEnabled;
    }

    public static boolean isDirectLinkEnabled(Context context) {
        dnsSettingMode = SharedPreferenceUtils.getSharedPreferences(SP_KEY_DIRECT_LINK_ENABLE, context, 0);
        return dnsSettingMode == 1;
    }

    public static HttpUrlConnection getUrlConnector() {
        return urlConnector;
    }


    public static void setDNSSettingMode(int mode) {
        dnsSettingMode = mode;

        if (dnsSettingMode == 1 && isFilterEnabled) {
            addFiltersByIPDirectLink();
        }

        if (dnsSettingMode == 0 && !isDNSHooked) {
            hookDNS();
        }
    }


    public static boolean needHookDNS() {
        return dnsSettingMode == 0 && isTestModeEnabled;
    }

    public static boolean isHttpDirectLinkEnabled() {
        return dnsSettingMode == 1 && isTestModeEnabled;
    }

    public static String repalceUrlBySetting(String host, String url) {

        String ip = map.get(host);
        if (ip != null && ip.length() > 0) {
            return url.replace(host, ip);
        }

        return null;
    }

    public static String getDirectLinkIPByHost(String host) {
        return map.get(host);
    }


    /**
     * 加载测试环境的一些配置参数
     */
    public static void loadConfiguration(Context context) {
        isTestModeEnabled = isDNSEnabled(context);
        isDirectLinkEnabled(context);//[mode]
        boolean isFilterEnabled = isFilterEnabled(context);

        if (isTestModeEnabled) {
            loadHostIPMaps(context);
            if (dnsSettingMode == 0) {
                hookDNS();
            } else {
                hookURL();
            }
        }
        if (isFilterEnabled) {
            enableFilterSetting(context);
        }
    }

    public static void setWhiteListEnabled(boolean enable) {
        enableWhiteList = enable;
    }

    public static boolean isWhiteListEnabled() {
        return enableWhiteList;
    }

    public static void forceHookUrl() {
        isFilterHooked = false;
        hookURL();
    }
}
