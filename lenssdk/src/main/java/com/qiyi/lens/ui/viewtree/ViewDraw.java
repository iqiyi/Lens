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
package com.qiyi.lens.ui.viewtree;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.view.View;

public class ViewDraw extends View {
    View refView, slView;
    float sx, sy;
    float px, py;
    Paint paint = new Paint();
    Rect viewRect = new Rect();
    private int strokeWidth = 4;


    public ViewDraw(Context context) {
        super(context);
    }

    public ViewDraw(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewDraw(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRef(View ref) {

        this.refView = ref;

    }


    public void select(View select) {
        this.slView = select;
        if (slView != null) {
            int[] ar = new int[2];
            int[] br = new int[2];
            refView.getLocationInWindow(ar);
            slView.getLocationInWindow(br);
            int a = br[0] - ar[0];
            int b = br[1] - ar[1];
            viewRect.set(a + strokeWidth, b + strokeWidth, a + slView.getWidth() - strokeWidth, b + slView.getHeight() - strokeWidth);

            figure();
            invalidate();
        }

    }


    @Override
    public void onLayout(boolean ch, int a, int b, int c, int d) {
        super.onLayout(ch, a, b, c, d);
        figure();
    }

    public void figure() {
//        px = getWidth() >> 1;
//        py = getHeight() >> 1;
        if (refView != null) {
            float sx = 1f * getWidth() / refView.getWidth();
            float sy = 1f * getHeight() / refView.getHeight();


            if (sy < sx) {
                sx = sy;
            }


            if (sx > 1.3f) {
                sx = 1.3f;
            }

            this.sx = sx;
            this.sy = sx;


            px = (getWidth() - refView.getWidth() * sx) / 2;
            py = (getHeight() - refView.getHeight() * sx) / 2;


        }

        paint.setColor(Color.RED);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
    }


    @Override
    public void onDraw(Canvas canvas) {
        int count = canvas.save();
        canvas.scale(sx, sy);
        canvas.translate(px, py);
        if (slView != null) {
            refView.draw(canvas);
            canvas.drawRect(viewRect, paint);
        }
        canvas.restoreToCount(count);
    }
}
