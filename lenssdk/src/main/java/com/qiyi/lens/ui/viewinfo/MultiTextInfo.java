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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.qiyi.lens.utils.UIUtils;

/**
 * 多行文本展示的 文本数据类
 */
public class MultiTextInfo extends TextInfo {
    private StaticLayout myStaticLayout;
    private TextPaint paint;

    MultiTextInfo(DrawKit outer, Paint p, String var) {
        super(outer, p, var);
        paint = new TextPaint(p);
        paint.setColor(p.getColor());
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(UIUtils.dp2px(outer.getContext(), 13));
    }

    @Override
    /**
     *  在测量之前， 已经设置了可滚动范围
     */
    public void measure(float x, float y) {
        if (data != null && data.length() > 0) {
            //【使用显示范围来测量】
            int dwd = (int) (rangeXR - rangeXL - host.textBgFillingSpace
                    - host.textBgFillingSpace);

            if (dwd > 10) {
                myStaticLayout = new StaticLayout(data, paint, dwd,
                        Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                left = (int) (x - host.textBgFillingSpace);
                top = (int) y;//(int) (y - getTextHeight(data));
                right = (int) (x + myStaticLayout.getWidth() + host.textBgFillingSpace);
                bottom = (top + myStaticLayout.getHeight() + host.textBgFillingSpace);
                forceInsideScreen();
            }
        }
    }

    @Override
    public int getWidth() {
        if (myStaticLayout == null) {
            return 0;
        } else {
            return super.getWidth();
        }
    }

    @Override
    public int getHeight() {
        if (myStaticLayout == null) {
            return 0;
        } else {
            return super.getHeight();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        int count = canvas.save();
        int color = this.mPaint.getColor();
        mPaint.setColor(0x33000000);
        canvas.drawRect(left, top, right, bottom, mPaint);
        mPaint.setColor(color);
        canvas.translate(left, top);
        myStaticLayout.draw(canvas);
        canvas.restoreToCount(count);
    }
}
