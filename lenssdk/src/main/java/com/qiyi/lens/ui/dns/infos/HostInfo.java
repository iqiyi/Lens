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
package com.qiyi.lens.ui.dns.infos;

import android.view.View;

import com.qiyi.lens.utils.reflect.Info;
import com.qiyi.lens.utils.reflect.Invalidate;

import java.util.LinkedList;

public class HostInfo extends HttpInfo {

    String host;

    public String getHost() {
        return host;
    }


    public HostInfo(String key, Invalidate invalidate) {
        super(invalidate);
        this.host = key;
        level = 2;

    }


    private String getPath(String url) {
        int lat = url.indexOf(host);
        if (lat > 0) {
            lat += host.length();
            int end = url.indexOf("?", lat + 1);
            if (end > 0) {
                return url.substring(lat + 1, end);
            }
        }
        return "---";
    }

    private PathInfo getPathInfo(String path) {
        for (Info info : infos) {
            PathInfo pathInfo = (PathInfo) info;
            if (path.equals(pathInfo.getPath())) {
                return pathInfo;
            }
        }
        return null;
    }


    //[find a best match segment]
    private PathInfo getPathInfo(PathSegment segment) {
        for (Info info : infos) {
            if (info instanceof PathInfo) {
                PathInfo pathInfo = (PathInfo) info;
                PathInfo nInfo = pathInfo.matches(segment);
                if (nInfo != null) {
                    return nInfo;
                }
            }
        }
        return null;
    }

    public final void addUrl(RequestData url, Invalidate par) {
        PathSegment segment = PathSegment.create(url.url, host);
        if (segment != null) {//[there is path]

            PathInfo pathInfo = getPathInfo(segment);
            if (pathInfo == null) {
                pathInfo = new PathInfo(segment, parent);
                pathInfo.setLevel(level);
                infos.add(pathInfo);
            }

            pathInfo.addUrl(url, par);
        } else {//[no path info, add  url for this host]
            UrlInfo urlInfo = new UrlInfo(url, par);
            urlInfo.setLevel(level + 1);
            infos.add(urlInfo);
        }

        checkExceed();

    }

    public void addUrls(LinkedList<RequestData> list, Invalidate par) {
        if (list != null && !list.isEmpty()) {
            for (RequestData s : list) {
                addUrl(s, par);
            }
        }
    }

    @Override
    public String toString() {
        return host + " [ " + mNotFilteredSize + " ]项";
    }


    @Override
    public void onClick(View view) {
        isExpand = !isExpand;
        list = infos;
        invalidate();
    }

    /**
     * check if data count exceeded MAX_URLS
     * if exceeded , removed data
     */
    private void checkExceed() {
        if (infos != null && infos.size() > MAX_URLS) {

            for (Info info : infos) {
                if (info instanceof UrlInfo) {
                    infos.remove(info);
                    break;
                }
            }
        }

    }


}
