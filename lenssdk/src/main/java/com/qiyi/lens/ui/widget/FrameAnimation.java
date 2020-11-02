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
import android.graphics.Rect;

import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

public class FrameAnimation extends LinearLayout {
    private boolean inAnimation;
    private FrameRect frameRect;
    private OnAnimationEndListener mEndListener;

    public FrameAnimation(Context context) {
        super(context);
    }

    public FrameAnimation(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FrameAnimation(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void executeAnimate(Rect from, Rect to) {
        if (!inAnimation) {
            ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
            animator.setDuration(300);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setVisibility(VISIBLE);
                    setFrame((Float) animation.getAnimatedValue());
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mEndListener != null) {
                        mEndListener.onEnd();
                    }
                    inAnimation = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    inAnimation = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
//            }
            if (frameRect == null) {
                frameRect = new FrameRect(from);
            } else {
                frameRect.reset(from);
            }
            inAnimation = true;
            frameRect.setDest(to);
            animator.start();
        }
    }

    public void startShowAnimation(final OnAnimationEndListener listener) {
        post(new Runnable() {
            @Override
            public void run() {
                Rect from = new Rect(0, 0, 0, 0);
                Rect to = new Rect(0, 0, getWidth(), getHeight());
                executeAnimate(from, to);
                mEndListener = listener;
            }
        });
    }

    public void startFrameHideAnimation(OnAnimationEndListener listener) {
        Rect from = new Rect(0, 0, getWidth(), getHeight());
        Rect to = new Rect(0, 0, 0, 0);
        executeAnimate(from, to);
        mEndListener = listener;
    }

    private void setFrame(float rate) {
        frameRect.update(rate);
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        if (inAnimation) {
            int a = canvas.save();
            frameRect.clip(canvas);
            super.draw(canvas);
            canvas.restoreToCount(a);
        } else {
            super.draw(canvas);
        }
    }

    static class FrameRect {

        Rect temp;
        int da;
        int db;
        int dc;
        int dd;
        int a;
        int b;
        int c;
        int d;

        FrameRect(Rect rect) {
            temp = rect;
        }

        void clip(Canvas canvas) {

            canvas.clipRect(temp.left, temp.top, temp.right, temp.bottom);
        }

        void update(float rate) {
            temp.left = (int) (rate * da + a);
            temp.top = (int) (rate * db + b);
            temp.right = (int) (rate * dc + c);
            temp.bottom = (int) (rate * dd + d);
        }

        void setDest(Rect rect) {
            da = rect.left - temp.left;
            db = rect.top - temp.top;
            dc = rect.right - temp.right;
            dd = rect.bottom - temp.bottom;
            a = temp.left;
            b = temp.top;
            c = temp.right;
            d = temp.bottom;
        }

        void reset(Rect rect) {
            temp.set(0, 0, rect.width(), rect.height());
        }

    }

    public interface OnAnimationEndListener {
        void onEnd();
    }

}
