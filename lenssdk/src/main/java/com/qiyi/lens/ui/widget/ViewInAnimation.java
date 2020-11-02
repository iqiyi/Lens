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

import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.qiyi.lens.ui.BasePanel;

import static java.lang.System.currentTimeMillis;

/**
 * Created by Carlyle_Lee on 2017/7/21.
 */

public class ViewInAnimation extends Animation {
    private View[] mviews;
    private View mRootView;
    private int[] visibles;
    private BasePanel _panel;
    private long now = 0L;
    private long offset = -1;

    private float mFromXDelta;
    private float mToXDelta;
    private float mFromYDelta;
    private float mToYDelta;
    private int _gra;

    public ViewInAnimation(BasePanel panel, float fromXDelta, float toXDelta, float fromYDelta, float toYDelta) {
        super();
        mFromXDelta = fromXDelta;
        mToXDelta = toXDelta;
        mFromYDelta = fromYDelta;
        mToYDelta = toYDelta;
        _panel = panel;
        mRootView = panel.getDecorView();
        mviews = loadViews((ViewGroup) panel.getDecorView());
//        visible = _view.getVisibility();
//        _view.setVisibility(View.GONE);
        setVisibility(View.GONE);
        this.setDuration(300);
        this.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
//                    _view.setVisibility(View.VISIBLE);
                updateTranslationInfo();
                mviews = null;

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                onEnd();
                _panel = null;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });


    }

    private void setVisibility(int visibility) {
        if(mviews != null) {
            for (View view : mviews) {
                view.setVisibility(visibility);
            }
        }
    }

    private void startAnimation(){
        if(mviews != null) {
            for (View view : mviews) {
                view.setVisibility(View.VISIBLE);
                view.setAnimation(ViewInAnimation.this);
                view.invalidate();
            }
        }

    }

    private View[] loadViews(ViewGroup group) {

        if (group != null) {
            int count = group.getChildCount();
            visibles = new int[count];
            View[] views = new View[count];
            for (int i = 0; i < count; i++) {
                views[i] = group.getChildAt(i);
                visibles[i] = views[i].getVisibility();
            }
            return views;
        }
        return new View[]{group};
    }

    public long getStartOffset() {
        if (now != 0) {
            if (offset < 0) {
                offset = currentTimeMillis() - now;
            }
            return offset;
        }
        return 0;
    }

    public void start() {
        now = currentTimeMillis();
        startAnimation();
        super.start();
    }



    public void startAnimationDelay(Handler handler, int dur) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.VISIBLE);
                now = currentTimeMillis();
                ViewInAnimation.this.start();
            }
        }, dur);
    }

    public void setAdjustTranslateEnabled(int gravity) {
        _gra = gravity;
    }


    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float dx = mFromXDelta;
        float dy = mFromYDelta;
        if (mFromXDelta != mToXDelta) {
            dx = mFromXDelta + ((mToXDelta - mFromXDelta) * interpolatedTime);
        }
        if (mFromYDelta != mToYDelta) {
            dy = mFromYDelta + ((mToYDelta - mFromYDelta) * interpolatedTime);
        }
        t.getMatrix().setTranslate(dx, dy);
    }


    private void updateTranslationInfo() {
        if (_gra != 0) {
            if (_gra == Gravity.BOTTOM) {
                mFromYDelta = mRootView.getHeight();
            } else {
                mFromXDelta = mRootView.getWidth();
            }
        }
    }

    protected void onEnd() {

    }

}
