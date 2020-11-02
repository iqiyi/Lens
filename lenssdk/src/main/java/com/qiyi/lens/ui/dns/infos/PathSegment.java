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

import androidx.annotation.NonNull;

import com.qiyi.lens.utils.FlexibleIntArray;


/**
 * start ： 标记这个segment 的启始位置
 * end :  描述的是从star 到 end 截至的 url；
 * 一个path info 中包含一个segment 信息。
 */

public class PathSegment {
    int start;//[start position ]
    private int end; //[end position of path seg]
    private int finPos = Integer.MAX_VALUE;//[the position where ? is.]
    String url;
    private String path;
    private FlexibleIntArray intArray;


    public PathSegment(int s, int e, String u) {
        this.start = s;
        this.end = e;
        this.url = u;

    }

    public String getPath() {
        if (path == null) {
            int size = size();
            if (size > 0) {
                path = url.substring(start, start + size);
            } else {
                path = "---";
            }
        }
        return path;
    }

    private int getEndPos(int pos) {

        if (intArray != null && intArray.size() > 0) {

            int[] arr = intArray.getArr();
            //[this is to make the next path is loaded]
            int length = intArray.size() - 1;

            for (int i = 0; i < length; i++) {
                if (arr[i] == pos) {
                    return intArray.get(i + 1);
                }
            }
        }

        return -1;
    }

    //[load next more into intArray]

    public boolean hasMore(final int pos) {
        if (pos >= finPos) return false;
        //[check if this is cached]
        int endPos = getEndPos(pos);
        if (endPos > 0) {
            return true;
        }
        if (intArray == null) {
            intArray = new FlexibleIntArray();
            intArray.push(start);
        }
        //[load next path]
        boolean hasMore = false;
        if (pos < finPos) {
            //[try find next segment]
            int len = url.length();
            int p = pos;
            while (p < len) {
                char c = url.charAt(p);
                if (c == '?') {
                    finPos = p;
                    intArray.push(p + 1);
                    end = finPos;
                    hasMore = true;
                    break;
                } else if (c == '/') {
                    //[found a path]
                    intArray.push(p + 1);//[the next start]
                    end = p;
                    hasMore = true;
                    break;
                }

                p++;
            }

            //[case : from start pos loop : not found / or ?]
            if (p == len) {
                if (pos < len) { //[there is data]
                    finPos = len; //[make same ]
                    intArray.push(len + 1);
                    hasMore = true;
                    end = len;
                } else {//[for case / is the last loop]
                    hasMore = false;
                    finPos = len;
                    //[end not change]
                }

            }

        }
        return hasMore;

    }


    //[must be called after the segment has more]
    public int getSegmentSize(int start) {
        int end = getEndPos(start) - 1;
        return end - start;
    }


    public int getStart() {
        return start;
    }


    //[this path segment is equal]
    public boolean segmentMatches(int start, PathSegment segment, int nStar) {

        int p = nStar;
        String nUrl = segment.url;
        int end = start + getSegmentSize(start);
        for (int i = start; i < end; i++) {
            char c = url.charAt(i);
            char nC = nUrl.charAt(p++);
            if (c != nC) {
                return false;
            }

        }

        return true;

    }

    /**
     * 0: equale
     * 1:this is more than that
     * -1: this is smaller than that
     *
     * @return
     */
    public int matches(PathSegment segment) {

        int start = this.start;
        int nStart = segment.start;
        int matchEnd = start;

        while (true) {
            boolean a = hasMore(start);
            boolean b = segment.hasMore(nStart);
            if (!a || !b) break;

            int size = getSegmentSize(start);
            int nSize = segment.getSegmentSize(nStart);

            if (size > 0 && size == nSize && segmentMatches(start, segment, nStart)) {
                matchEnd = start + size;
                //[next]

                start = matchEnd + 1;
                nStart = nStart + nSize + 1;

            } else { //[while not match]

                break;

            }

        }

        return matchEnd - this.start;
    }


    public int size() {


        if (finPos == Integer.MAX_VALUE) {
            int p = start;
            int len = url.length();

            if (intArray == null) {
                intArray = new FlexibleIntArray();
                intArray.push(start);
            } else {
                p = intArray.get(intArray.size() - 1);
            }

            while (p < len) {
                char c = url.charAt(p);
                if (c == '?') {
                    finPos = p;
                    end = p;
                    intArray.push(p + 1);
                    break;

                } else if (c == '/') {
                    end = p;
                    intArray.push(p + 1);

                }
                p++;
            }


            if (p == len) {
                if (start < len) {
                    end = len;
                    finPos = len;
                    intArray.push(len + 1);
                } else {
                    finPos = len;
                }

            }

        }


        if (end > 0) {
            return end - start;
        } else {
            return url.length() - start;
        }
    }


    //[if is set this as an end:  has more will change this end]
//    public void setEnd(int end) {
//
//        path = null;
//    }

    public void setStart(int start) {
        this.start = start;
        path = null;

        if (intArray != null) {
            int index = intArray.indexOf(start);
            if (index > 0) {
                intArray.removeFormer(index);
            }
        }
    }


    public void setFinPos(int fin) {
        finPos = fin;
        end = fin;
        path = null;
    }


    public static PathSegment create(@NonNull String url, @NonNull String host) {
        int lat = url.indexOf(host);
        if (lat > 0) {
            int start = lat + 1 + host.length();
            return create(url, start);

        }

        return null;
    }


    public static PathSegment create(String url, int start) {


        int foud = start;
        int p = start;
        int fin = start;
        int end = url.length();
        while (p < end) {
            char c = url.charAt(p);
            if (c == '?') {
                foud = p;
                fin = p;
                break;
            } else if (c == '/') {
                foud = p;
                fin = Integer.MAX_VALUE;
                break;
            }
            p++;
        }


        if (foud > start) {
            //[found ]
            PathSegment seg = new PathSegment(start, foud, url);
            if (fin != Integer.MAX_VALUE) {
                seg.setFinPos(fin);
            }
            return seg;
        } else {//[found == start make path the rest of url]
            PathSegment seg = new PathSegment(start, url.length(), url);
            seg.finPos = url.length();
            return seg;
        }

    }


}
