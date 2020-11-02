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
package com.qiyi.lens.ui.viewinfo;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;


//[sued for drawText]
public class TextInfo {
    static final int MAX_ADJUST = 3;
    String data;
    int left, top, right, bottom;
    int color;
    int bgColor;
    int size;
    private int adjustCount;//[设置最大调整次数]
    //[文本类容可动态跳转区域]
    float rangeXL, rangeXR, rangeYT, rangeYB;
    String dec;
    Paint mPaint;
    DrawKit host;

    TextInfo(DrawKit outer, Paint paint, String var) {
        this.mPaint = paint;
        this.host = outer;
        this.data = var;
    }

    //【设置文本的可以显示的区域】
    void setRange(float xa, float xb, float ya, float yb) {
        this.rangeXL = xa;
        this.rangeXR = xb;
        this.rangeYT = ya;
        this.rangeYB = yb;
    }

    private float getTextHeight(String text) {
        Rect rect = new Rect();
        mPaint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }

    private float getTextWidth(String text) {
        return mPaint.measureText(text);
    }


    /**
     * @param x 期望文案展示的位置
     * @param y 期望文案展示的位置
     *          测量得到 坐标值， 用于绘制
     */
    public void measure(float x, float y) {
        if (data != null && data.length() > 0) {

            left = (int) (x - host.textBgFillingSpace);
            top = (int) (y - getTextHeight(data));
            right = (int) (x + getTextWidth(data) + host.textBgFillingSpace);
            bottom = (int) (y + host.textBgFillingSpace);
            forceInsideScreen();
        }

    }

    void forceInsideScreen() {

        // ensure text in screen bound
        if (left < 0) {
            right -= left;
            left = 0;
        }
        if (top < 0) {
            bottom -= top;
            top = 0;
        }
        if (bottom > host.screenHeight) {
            float diff = top - bottom;
            bottom = host.screenHeight;
            top = (int) (bottom + diff);
        }
        if (right > host.screenWidth) {
            float diff = left - right;
            right = host.screenWidth;
            left = (int) (right + diff);
        }

    }

    public void draw(Canvas canvas) {
        if (data != null && data.length() > 0) {
            canvas.drawRect(left, top, right, bottom, host.textBgPaint);
            canvas.drawText(data, left + host.textBgFillingSpace, bottom - host.textBgFillingSpace,
                    mPaint);
        }
    }


    public int getWidth() {
        return right - left;
    }

    public int getHeight() {
        return bottom - top;
    }


    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }


    /**
     * adjust current node by the new coming one
     * key algorithm for Text location adjustment
     *
     * @param info 实现原理：
     */
    boolean needAdjust(TextInfo info) {
        int dx = info.left - left;
        int dy = info.top - top;
        if (dx < getWidth() && dx > -info.getWidth() && dy < getHeight() && dy > -info.getHeight()) {
            //[ overlapped]
            //[do adjust ]

            // info do adjust
            int deltaX = dx > 0 ? getWidth() - dx : info.getWidth() + dx;
            int deltaY = dy > 0 ? getHeight() - dy : info.getHeight() + dy;
            //[先尝试调整新插入的节点。如果当前的插入节点调整失败，则返回需要调整]
            if (!info.adjust(deltaX, deltaY) && !adjust(-deltaX, -deltaY)) {
                return true;
            }
        }

        return false;
    }


    /**
     * 判定当前info 是否能移动参数距离
     *
     * @return dx 成功或者dy 成功即可
     */
    private boolean adjust(int dx, int dy) {

        if (adjustCount < MAX_ADJUST) {
            int nextLeft = left + dx;
            int nextRight = right + dx;
            if (nextLeft >= rangeXL && nextRight <= rangeXR) {
                //[do adjust]
                left = nextLeft;
                right = nextRight;
                adjustCount++;
                return true;
            }

            int nextTop = top + dy;
            int netBottom = bottom + dy;
            if (nextTop >= rangeYT && netBottom <= rangeYB) {
                top = nextTop;
                bottom = netBottom;
                adjustCount++;
                return true;
            }

        }
        return false;
    }


}
