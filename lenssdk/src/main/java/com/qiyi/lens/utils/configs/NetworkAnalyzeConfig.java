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
package com.qiyi.lens.utils.configs;

import com.qiyi.lens.utils.iface.INetConfig;

import java.util.LinkedList;

public class NetworkAnalyzeConfig {
    private static NetworkAnalyzeConfig config = new NetworkAnalyzeConfig();
    private String urlFilter;
    private String keyFilter;
    private boolean isUrlGrabEnabled;
    private LinkedList<Pair> keyList = new LinkedList<>();
    private String defaultIPHosts;
    private String defaultUrlGrabFilter;
    //get net work config info : first form SP , then from data setting , then from INetConfig
    private INetConfig mNetConfig;

    public static NetworkAnalyzeConfig getInstance() {
        return config;
    }

    public NetworkAnalyzeConfig setDefaultUrlFilter(String reg) {
        this.urlFilter = reg;
        return this;
    }

    public String getUrlFilter() {
        return urlFilter;
    }

    public NetworkAnalyzeConfig setDefaultUrlKeyFilter(String s) {
        keyFilter = s;
        return this;
    }

    public NetworkAnalyzeConfig setUrlGrabEnabled(boolean enabled) {
        isUrlGrabEnabled = enabled;
        return this;
    }

    public boolean isUrlGrabEnabled() {
        return isUrlGrabEnabled;
    }


    public void setNetConfig(INetConfig config) {
        mNetConfig = config;
    }

    public void onRequest(String url) {

        if (!keyList.isEmpty() && url != null) {
            for (Pair key : keyList) {
                String s = key.url;
                if (url.contains(s)) {
                    key.watch.onRequest(url);
                }
            }
        }
    }

    public String getUrlKeyFilter() {
        return keyFilter;
    }

    public NetworkAnalyzeConfig addNetRequestWatch(String key, RequestWatch watch) {
        if (key == null) {
            keyList.clear();
        } else {
            keyList.add(new Pair(key, watch));
        }
        return this;
    }

    public interface RequestWatch {
        public void onRequest(String url);
    }

    private static class Pair {
        String url;
        RequestWatch watch;

        Pair(String r, RequestWatch w) {
            this.url = r;
            this.watch = w;
        }
    }

    public void addDefaultIPHosts(String ipHosts) {
        this.defaultIPHosts = ipHosts;
    }

    public void setUrlFilter(String urls) {
        this.defaultUrlGrabFilter = urls;
    }

    public String getDefaultIPHosts() {
        return this.defaultIPHosts;
    }

    public INetConfig getNetConfig() {
        return mNetConfig;
    }

    public String getDefaultUrlGrabFilter() {
        return this.defaultUrlGrabFilter;
    }

}
