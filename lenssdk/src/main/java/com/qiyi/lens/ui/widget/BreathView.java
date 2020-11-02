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
package com.qiyi.lens.ui.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.annotation.Nullable;

/**
 * 验证： 正在动画的时候，点击，扩展到show 的时候
 */
public class BreathView extends View {
    final int NEXT_WAKE_UP = 5000;
    int breathCount = 0;
    float density;
    int[] mColors = new int[]{0xFF9B30FF, 0xffEED2EE, 0xFF375BF1, 0xFFC93437, 0xFFF7D23E, 0xFFADFF2F, 0xFF00BFFF};
    Handler handler = new Handler();
    Paint paint = new Paint();
    Paint paintCross = new Paint();
    float baseStoke;
    MDrawable drawable;
    boolean isSleep;
    ValueAnimator breathAnimator;
    float drawingFactor = 1f;
    Runnable breath = new Runnable() {
        @Override
        public void run() {
            startBreath();
        }
    };

    public BreathView(Context context) {
        super(context);
        init(context);
    }

    public BreathView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        breathAnimator = ValueAnimator.ofFloat(0, 1f);
        breathAnimator.setDuration(350);
        breathAnimator.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                //form 0 ~ 1f : makes it : 1->0->1
                return (float) (Math.cos((input + 1) * Math.PI));
            }
        });
        breathAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                drawingFactor = animation.getAnimatedFraction();
                invalidate();
            }
        });

        breathAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                drawingFactor = 1f;
                wakeUp();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                drawingFactor = 1f;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        paint.setAntiAlias(true);
        density = context.getResources().getDisplayMetrics().density;
        baseStoke = density * 2;
        paint.setStrokeWidth(baseStoke);
        setPaint();

    }


    public void onDraw(Canvas canvas) {
        if (!isSleep) {
            drawable.draw(canvas, drawingFactor);
        }
    }


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(breath);
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getVisibility() == View.VISIBLE) {
            wakeUp();
        }
    }

    private void wakeUp() {
        handler.removeCallbacks(breath);
        handler.postDelayed(breath, NEXT_WAKE_UP);
        isSleep = false;

    }


    public void sleep() {
        handler.removeCallbacks(breath);
        breathAnimator.cancel();
        isSleep = true;
    }

    private void startBreath() {
        breathAnimator.start();
        invalidate();
    }


    //当visibility 变换时候:
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            wakeUp();
        } else {
            sleep();
        }
    }

    private void switchPaint() {
        breathCount++;
        setPaint();

    }

    private void setPaint() {
        int p = breathCount % mColors.length;
        paint.setColor(mColors[p]);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        p = (breathCount + 1) % mColors.length;
        paintCross.setColor(mColors[p]);
        paintCross.setStrokeWidth(baseStoke);

    }

    public void setColors(int[] colors) {
        if (colors != null && colors.length > 0) {
            mColors = colors;
        }
    }

    @Override
    public void onLayout(boolean changed, int a, int b, int c, int d) {
        super.onLayout(changed, a, b, c, d);
        if (drawable == null) {
            drawable = new MDrawable();
        }
        drawable.set(getWidth(), getHeight());
    }

    class MDrawable {
        float cx, cy;
        float radius;
        float half;
        //        float left , top, right, bottom;
        RectF box;
        private boolean switchPaint;

        // rate -1, 1
        public void draw(Canvas canvas, float rate) {

            float nf;
            if (rate < 0f) {//  -1, 0
                switchPaint = true;
                nf = -1f * rate;
                paint.setAlpha((int) (200f * nf));
                canvas.drawCircle(cx, cy, radius * nf, paint);
            } else {
                if (switchPaint) {
                    switchPaint = false;
                    switchPaint();
                }

                nf = rate;
                paint.setAlpha((int) (200 * nf));
                canvas.drawCircle(cx, cy, radius * nf, paint);
            }

//            float extra= rate * 6;
//            paintCross.setStrokeWidth( 2 + extra);
            float top = cy;
            float left = cx;
            canvas.drawLine(cx - half, top, cx + half, top, paintCross);
            canvas.drawLine(left, cy - half, left, cy + half, paintCross);
        }

        MDrawable() {

        }

        public void set(int wd, int ht) {
            radius = Math.min(wd, ht);
            radius = radius / 2f;
            radius -= (paint.getStrokeWidth() + 2);

            half = radius / 2f;
            cx = wd >> 1;
            cy = ht >> 1;
            float left = cx - radius;
            float right = cx + radius;
            float top = cy - radius;
            float bottom = cy + radius;
            box = new RectF(left, top, right, bottom);
//                    new RectF(0, 0, wd, ht);


        }
    }

}
