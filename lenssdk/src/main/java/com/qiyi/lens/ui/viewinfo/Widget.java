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
import android.graphics.RectF;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiyi.lenssdk.R;

public class Widget {

    private View view;
    private RectF originRect = new RectF();
    private RectF rect = new RectF();
    private Widget parent;
    private int offset;
    // debug info : bg 242424 ;
    private final static int TEXT_DEBUG_INFO_COLOR = 0xFFFFFFFF;
    private DrawKit mKit;

    Widget(DrawKit drawKit, View view, int offset) {
        this.view = view;
        this.offset = offset;
        initRect(offset);
        originRect.set(rect.left, rect.top, rect.right, rect.bottom);
        mKit = drawKit;
    }

    public View getView() {
        return view;
    }

    public RectF getRect() {
        return rect;
    }

    public RectF getOriginRect() {
        return originRect;
    }

    private void initRect(int offset) {
        int[] position = new int[2];
        view.getLocationOnScreen(position);
        int width = view.getWidth();
        int height = view.getHeight();

        int left = position[0];
        int right = left + width;
        int top = position[1];

        top -= offset;
        int bottom = top + height;

        rect.set(left, top, right, bottom);
    }

    public Widget getParent() {
        if (parent == null) {
            Object parentView = view.getParent();
            if (parentView instanceof View) {
                parent = new Widget(mKit, (View) parentView, offset);
            }
        }
        return parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Widget widget = (Widget) o;
        return view != null ? view.equals(widget.view) : widget.view == null;
    }

    @Override
    public int hashCode() {
        return view != null ? view.hashCode() : 0;
    }


    //[check if x,y is inside current widget ]
    public boolean contains(int x, int y) {
        if (rect.contains(x, y)) {
            View view = getView();
            if (view instanceof ViewGroup) {
                //[if is view group && bg is null : check children]
                if (view.getBackground() == null && ((ViewGroup) view).getChildCount() == 0) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isViewVisible() {
        return view.getVisibility() == View.VISIBLE;
    }


    /**
     * 绘制普通视图基础信息
     */
    public void draw(Canvas canvas) {
        View view = getView();
        Object debugTag = view.getTag(R.id.lens_debug_info_tag);
        if (debugTag instanceof String) {
            Object color = view.getTag(R.id.lens_debug_info_tag_text_color);
            if (color instanceof Integer) {
                mKit.pushColor((Integer) color);
            } else {
                mKit.pushColor(TEXT_DEBUG_INFO_COLOR);
            }
            mKit.drawDebugInfo(canvas, (String) debugTag, getRect());
            mKit.popColor();
        } else if (view instanceof TextView) {
            //draw text view info
            TextView textView = (TextView) view;
            // 绘制行间距
            if (textView.getLineCount() > 1) {

                RectF rect = getRect();
                Layout layout = textView.getLayout();
                if (layout != null) {

                    float lineExtra = (textView.getLineHeight() - textView.getTextSize())/2;
                    float offset = rect.top + Math.abs(layout.getTopPadding());//textView.getPaddingTop() - layout.getTopPadding()
                    float baseline = layout.getLineBaseline(0) + offset;
                    Paint.FontMetrics fontMetrics = textView.getPaint().getFontMetrics();
                    RectF first = new RectF(rect.left, baseline + fontMetrics.ascent + lineExtra , rect.right,
                            baseline +fontMetrics.descent - lineExtra);

                    baseline = layout.getLineBaseline(1) + offset;
                    RectF second = new RectF(rect.left, baseline + fontMetrics.ascent + lineExtra  , rect.right, baseline + fontMetrics.descent - lineExtra);

                    mKit.drawDistance(canvas, first, second);
                    mKit.drawArea(canvas, first, Color.WHITE);
                    mKit.drawArea(canvas, second, Color.WHITE);
                }
            }

        }
    }


}
