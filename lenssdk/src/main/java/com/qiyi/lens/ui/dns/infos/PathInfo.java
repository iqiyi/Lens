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

import com.qiyi.lens.utils.reflect.Info;
import com.qiyi.lens.utils.reflect.Invalidate;
import com.qiyi.lens.utils.reflect.SpanableInfo;

import java.lang.ref.WeakReference;
import java.util.LinkedList;


/**
 * path info myst have a path segment info
 * <p>
 * 关于URL 删除： 只有当 某个path info中的URL info 数量超出限制后才删除， 因此不存在节点合并的问题。
 * 不会将一个节点下的所有数据删除光。
 */
public class PathInfo extends HttpInfo {

    private PathSegment mSegment;
    private int urlInfoCount;

    public PathInfo(PathSegment segment, WeakReference<Invalidate> parent) {
        super(parent);
        mSegment = segment;
    }

    @Override
    protected void makeList(LinkedList linkedList) {
        if (list == null) {
            list = new LinkedList<>();
        } else {
            list.clear();
        }


        if (infos != null && !infos.isEmpty()) {
            LinkedList<Info> urls = new LinkedList<>();
            for (Info info : infos) {

                if (info.isNotFiltered()) {

                    if (info instanceof PathInfo) {
                        list.add(info);
                    } else {
                        urls.add(info);
                    }
                }
            }
            list.addAll(urls);

        }


    }

    @Override
    protected void preBuildSpannables(StringBuilder stringBuilder, LinkedList<SpanableInfo> infosList) {

    }

    @Override
    protected void afterBuildSpannables(StringBuilder stringBuilder, LinkedList<SpanableInfo> infosList) {

    }

    public void addUrl(RequestData url, Invalidate par) {


        Info firstUrlInfo = null;

        for (Info info : infos) {
            if (info instanceof UrlInfo) {

                if (firstUrlInfo == null) {
                    firstUrlInfo = info;
                }

                UrlInfo urlInfo = (UrlInfo) info;
                // keep use ==
                if (urlInfo.value == url.url) {
                    return;
                }
            }
        }


        if (urlInfoCount > MAX_URLS) {
            //[remove first url info ]
            infos.remove(firstUrlInfo);
            urlInfoCount--;
        }
        UrlInfo info = new UrlInfo(url, par);
        infos.addLast(info);
        urlInfoCount++;

    }

    public String getPath() {
        return mSegment.getPath();
    }

    @Override
    public String toString() {
        return mSegment.getPath() + " [ " + mNotFilteredSize + " ]条";
    }

    /**
     * 节点可能分裂，当前的节点，仍然不变。分裂后 保留在当前节点内部。
     *
     * @param pathSegment
     * @return
     */
    public PathInfo matches(PathSegment pathSegment) {
        if (mSegment == null) {// [avoid null ptr]
            return null;
        }
        int match = mSegment.matches(pathSegment);
        int segSize = mSegment.size();
        if (match == 0) { //[not match at all]
            return null;

            //[bug 原因： segments 相同，但是前面的schema 不同。因此两者之间的url index 不通用。]
            //[fix : 使用当前的segment 的strat 来设置当前的fin]
            //[新的path 只匹配了部分的路径，需要拆分现在的路径]
        } else if (match < segSize) { //[split , into 2 , add add into this]
            int end = mSegment.getStart() + match;
            mSegment.setFinPos(end);

            PathSegment seg1 = PathSegment.create(mSegment.url, end + 1);
            PathInfo pInfo1 = new PathInfo(seg1, parent);
            pInfo1.setLevel(level);
            pInfo1.addInfos(infos);
            infos.clear();
            infos.add(pInfo1);


            end = pathSegment.getStart() + match;//[使用自身的start 才对]
            pathSegment.setStart(end + 1);
            PathInfo nInfo = new PathInfo(pathSegment, parent);
            nInfo.setLevel(level);
            infos.add(nInfo);

            return nInfo;
        } else if (match == segSize && match == pathSegment.size()) {//[match > ]

            return this;
        } else {//[larger， 需要创建节点 活着比较子节点。]

            int mStart = pathSegment.start;
            pathSegment.setStart(mStart + segSize + 1);

            for (Info info : infos) {
                if (info instanceof PathInfo) {
                    PathInfo pathInfo = (PathInfo) info;

                    PathInfo nPathInfo = pathInfo.matches(pathSegment);
                    if (nPathInfo != null) {
                        return nPathInfo;
                    }
                }
            }


            //[nothing is found , 既然这个节点意见建立，说明下面是有数据的。直接在下面新建节点]
            PathInfo pInfo = new PathInfo(pathSegment, parent);
            pInfo.setLevel(level);
            infos.add(pInfo);

            return pInfo;
        }


    }


    public void addInfos(LinkedList<Info> list) {
        infos.addAll(list);

    }


}
