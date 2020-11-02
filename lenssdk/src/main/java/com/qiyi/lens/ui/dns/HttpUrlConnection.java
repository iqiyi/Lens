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

import android.os.Looper;

import com.qiyi.lens.ui.dns.infos.HostInfo;
import com.qiyi.lens.ui.dns.infos.RequestData;
import com.qiyi.lens.ui.dns.infos.UrlCollector;
import com.qiyi.lens.utils.KeyLog;
import com.qiyi.lens.utils.reflect.Invalidate;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.HttpUrl;

public class HttpUrlConnection {
    private HashMap<String, LinkedList<RequestData>> urlMap = new HashMap<>();
    private volatile int size = 0;

    //[make sure same instance not add more than once]
    public synchronized void add(HttpUrl url, Map<String, List<String>> headers,
                                 String method, Object body) {
        if (url != null) {
            String host = url.host();
            if (host != null) {
                String ur = url.toString();
                LinkedList<RequestData> linkedList = urlMap.get(host);
                if (linkedList == null) {
                    linkedList = new LinkedList<>();
                    urlMap.put(host, linkedList);
                } else {
                    for (RequestData tmp : linkedList) {
                        // keep use ==
                        if (tmp.url == ur) {
                            return;
                        }
                    }
                }
                linkedList.add(new RequestData(ur, headers, method, body));
            }
            size++;
        }

    }


    public synchronized void add(String url, String host, Map<String, List<String>> map, String method, Object body) {
        if (url != null) {
//            String host = url.host();
            if (host != null) {
                String ur = url.toString();
                LinkedList<RequestData> linkedList = urlMap.get(host);
                if (linkedList == null) {
                    linkedList = new LinkedList<>();
                    urlMap.put(host, linkedList);
                } else {
                    for (RequestData tmp : linkedList) {
                        // keep use ==
                        if (tmp.url == ur) {
                            return;
                        }
                    }
                }
                linkedList.add(new RequestData(ur, map, method, body));
            }
            size++;
        }

    }


    public synchronized UrlCollector build(UrlCollector collection, Invalidate par) {
        if (collection == null) {
            collection = new UrlCollector(par);
        }
        if (!urlMap.isEmpty()) {

            KeyLog.addLog("url bld : is main thread " + (Looper.myLooper() == Looper.getMainLooper()));
            Set<Map.Entry<String, LinkedList<RequestData>>> set = urlMap.entrySet();
            for (Map.Entry<String, LinkedList<RequestData>> entry : set) {
                String key = entry.getKey();

                HostInfo info = collection.getHostInfo(key);
                if (info == null) {
                    info = new HostInfo(key, par);
                    collection.addHostInfo(info);
                }

                info.addUrls(entry.getValue(), par);

            }
        }
        return collection;
    }

    public int size() {
        return size;
    }

    public synchronized void clear() {
        urlMap.clear();
        size = 0;
    }
}
