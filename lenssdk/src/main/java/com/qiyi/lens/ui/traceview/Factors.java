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
package com.qiyi.lens.ui.traceview;

/**
 * 本类用于支持 view 中的位置变换，坐标计算等
 * 1) scale
 * 2) scrollX;
 */
class Factors {
    int left;
    private int top;
    int right;
    private int _wd;
    private int _ht;
    private float xScale;
    private int _blockHeight;
    private int scaleFactor = 1;
    private int scaleFactorCount = 0;

    private long[] timeRange = new long[2];
    private long totalRange;
    private float scrollExtent;
    private boolean hasScrolled;


    Factors() {
        timeRange[1] = 1000L;
    }

    /**
     * onLayout may call this
     */
    void setTimeTotalRange(int total) {
        totalRange = total;
        timeRange[1] = totalRange;
    }


    public void layout(int pwd, int pht, int pl, int pt, int pr) {
        boolean changed = _wd != pwd || _ht != pht;
        _wd = pwd;
        _ht = pht;
        left = pl;
        top = pt;
        right = left + pwd;

        if (changed) {
            updateXFactors();
        }

    }

    void setBlockHeight(int blockHeight) {
        _blockHeight = blockHeight;
    }

    int getYForThreadByIndex(int index) {
        index += 1;
        return top + _blockHeight * index;
    }


    int getXForStamp(long stamp) {

        stamp -= timeRange[0];//[offset]
        return (int) (left + xScale * stamp);
    }

    // called when layout change | scale rate change
    private void updateXFactors() {
        xScale = scaleFactor * _wd * 1f / totalRange;
    }

    //算法规则： 保证cx 点击位置不变 ；
    private void updateScrollX(float scaleFactorChange) {
        scrollX = scaleFactorChange * (scrollX + cx) - cx;
        //[update offset : and make sure in range : 可能导致出界的原因： 在放大的情况下，移动距离，然后再缩放回来。导致偏离]
        //【scroll Bounds】
        float totalExtentInPx = _wd * scaleFactor;
        scrollExtent = totalExtentInPx - _wd;

        //[check scroll bounds]
        checkScrollRange();

    }


    /**
     * 绘制限制： 返回当前缩放避免，滚动比例下的 可见时间轴 范围；
     */
    public long[] getTimeRange() {
        //[todo figure]
        return timeRange;
    }

    // todo 需要根据当前的dx dy 做放大计算；
    public boolean increaceFactorCount() {
        if (!hasScrolled) {

            scaleFactorCount++;

            scaleFactorCount = scaleFactorCount % 5;
            int former = scaleFactor;
//            scaleFactor = (int) Math.pow(2, scaleFactorCount);
            switch (scaleFactorCount) {
                case 0:
                    scaleFactor = 1;
                    break;
                case 1:
                    scaleFactor = 2;
                    break;
                case 2:
                    scaleFactor = 8;
                    break;
                case 3:
                    scaleFactor = 32;
                    break;
                case 4:
                    scaleFactor = 64;
                    break;
            }
            updateXFactors();
            // scale by dx, dy
            updateScrollX(scaleFactor * 1f / former);
            return true;
        }

        return false;
    }


    // 新增支持手势位置变换
    private float cx; // 支持在点击位置放大
    private float scrollX;// scroll left get > 0: range 0 : N > 0

    public void onDown(float x) {
        cx = x;
//        cx = _wd/2;
        hasScrolled = false;
//        cy = y;
    }

    //[暂时不做这里的支持 todo]
    public boolean canScroll(int dx) {

        return true;
    }


    //[todo handle scroll]
    public void onScroll(float dx) {
        scrollX += dx;
        hasScrolled = true;
        checkScrollRange();

    }

    //[check scroll bounds]
    private void checkScrollRange() {
        if (scrollX < 0) {
            scrollX = 0;
        } else if (scrollX > scrollExtent) {
            scrollX = scrollExtent;
        }
        //[update time range]
        timeRange[0] = (long) (Math.floor(scrollX / xScale));
        timeRange[1] = (long) (Math.ceil(_wd / xScale)) + timeRange[0];
    }


}
