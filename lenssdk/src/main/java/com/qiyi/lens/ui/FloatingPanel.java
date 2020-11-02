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
package com.qiyi.lens.ui;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.CallSuper;

import com.qiyi.lens.Constants;
import com.qiyi.lens.LensUtil;
import com.qiyi.lens.ui.widget.FrameAnimation;
import com.qiyi.lens.utils.ApplicationLifecycle;
import com.qiyi.lens.utils.TouchDelegateUtils;
import com.qiyi.lens.utils.UIUtils;
import com.qiyi.lenssdk.R;

/**
 * a floating panel to display data info :
 * support:
 * 1) drag and move
 * 2) show() & dismiss()
 */
public abstract class FloatingPanel extends BasePanel implements View.OnClickListener {
    private View contentView;
    private int mWd;
    private boolean isHidden;
    private FrameAnimation frameAnimation;
    private int mTouchSlop;
    private int minYOffset;
    private int myInitState = ApplicationLifecycle.PANEL_STATE_SHOW;
    private View expandBtn;

    public FloatingPanel(int w) {
        mTouchSlop = ViewConfiguration.get(getActivity()).getScaledTouchSlop();
        minYOffset = UIUtils.getStatusBarHeight(context);
        mWd = w;
    }

    protected View createView() {
        return LayoutInflater.from(context).inflate(R.layout.lens_float_panel_frame, null);
    }

    protected void onViewCreated(final View view) {
        frameAnimation = mDecorView.findViewById(R.id.float_panel_main);
        expandBtn = mDecorView.findViewById(R.id.float_control_expand);
        super.onViewCreated(view);
        if (contentView == null) {
            contentView = onCreateView(mDecorView);
        }
        ViewParent viewParent = contentView.getParent();
        if (viewParent instanceof ViewGroup) {
            ((ViewGroup) viewParent).removeView(contentView);
        }
        frameAnimation.addView(contentView, getContentLayoutParams(contentView));

        setDragListener();
        setClickHandler();

        if (myInitState == ApplicationLifecycle.PANEL_STATE_MIN) {
            hide();
        } else if (myInitState == ApplicationLifecycle.PANEL_STATE_CLOSE) {
            dismiss();
        } else {//[show state]
            expandBtn.setVisibility(View.GONE);
            show();
        }
    }


    //[to crate view as content of this floating bar]
    protected abstract View onCreateView(ViewGroup viewGroup);

    /**
     * 支持触摸点击， 拖动功能，但是没有焦点
     *
     * @return
     */
    protected ViewGroup.LayoutParams getDefaultLayoutParams() {
        FrameLayout.LayoutParams wlp = new FrameLayout.LayoutParams(mWd, ViewGroup.LayoutParams.WRAP_CONTENT);
        wlp.gravity = Gravity.TOP | Gravity.LEFT;
        wlp.topMargin = minYOffset;
        return wlp;
    }

    private LinearLayout.LayoutParams getContentLayoutParams(View contentView) {
        if (contentView != null) {
            int wd;
            int ht;
            ViewGroup.LayoutParams clp = contentView.getLayoutParams();
            if (clp instanceof LinearLayout.LayoutParams) {
                return (LinearLayout.LayoutParams) clp;
            } else if (clp != null) {
                wd = clp.width;
                ht = clp.height;
            } else {
                wd = -2;
                ht = -2;
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(wd, ht);

            if (clp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) clp;
                lp.leftMargin = mlp.leftMargin;
                lp.topMargin = mlp.topMargin;
                lp.rightMargin = mlp.rightMargin;
                lp.bottomMargin = mlp.bottomMargin;
            }

            return lp;
        }

        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setDragListener() {
        View.OnTouchListener touchListener = new DragListener();
        frameAnimation.setOnTouchListener(touchListener);
        expandBtn.setOnTouchListener(touchListener);
    }

    private void setClickHandler() {
        View dismissBtn = findViewById(R.id.float_control_dismiss);
        dismissBtn.setOnClickListener(this);
        View hideBtn = findViewById(R.id.float_control_hide);
        hideBtn.setOnClickListener(this);
        expandBtn.setOnClickListener(this);

        // 优化按钮点击区域
        TouchDelegateUtils.expandHitRect(dismissBtn, 8);
        TouchDelegateUtils.expandHitRect(hideBtn, 8);
    }

    @Override
    @CallSuper
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.float_control_hide) {
            hide();
        } else if (id == R.id.float_control_dismiss) {
            dismiss();
        } else if (id == R.id.float_control_expand) {
            expand();
        }
    }

    public void expand() {
        if (isHidden) {
            mDecorView.getLayoutParams().width = mWd;
            viewManager.invalidate();
            frameAnimation.setVisibility(View.INVISIBLE);
            expandBtn.setVisibility(View.GONE);
            frameAnimation.startShowAnimation(new FrameAnimation.OnAnimationEndListener() {
                @Override
                public void onEnd() {
                    frameAnimation.setVisibility(View.VISIBLE);
                    isHidden = false;
                }
            });
        }
    }

    public void hide() {
        if (!isHidden) {
            if(mDecorView.getParent() != null) {
                frameAnimation.startFrameHideAnimation(new FrameAnimation.OnAnimationEndListener() {
                    @Override
                    public void onEnd() {
                        handelHideAction();
                    }
                });
                frameAnimation.invalidate();
            } else {
                handelHideAction();
            }
        }
    }

    private void handelHideAction(){
        frameAnimation.setVisibility(View.GONE);
        expandBtn.setVisibility(View.VISIBLE);
        if(mDecorView.getLayoutParams() != null) {
            mDecorView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            viewManager.invalidate();
        }
        isHidden = true;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void bringToFront() {
        if (mDecorView == null) return;

        if (getPanelType() == Constants.PANEL_FLOAT_PANEL_VIEW) {
            mDecorView.bringToFront();
        } else {
            ViewManager.FloatWindowManager floatingManager = (ViewManager.FloatWindowManager) viewManager;
            floatingManager.brintToFront();
        }
    }

    class DragListener implements View.OnTouchListener {

        private float downX;
        private float downY;
        private boolean fireClick;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int id = event.getAction();
            float x = event.getRawX();
            float y = event.getRawY();
            switch (id) {
                case MotionEvent.ACTION_DOWN:
                    downX = x;
                    downY = y;
                    fireClick = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(x - downX) > mTouchSlop || Math.abs(y - downY) > mTouchSlop) {
                        fireClick = false;
                        viewManager.handleMove(x - downX, y - downY);
                        downX = x;
                        downY = y;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (fireClick) {
                        v.performClick();
                    }
                    break;
            }
            return true;
        }
    }

    public void setInitState(int state) {
        myInitState = state;
    }

    public int width() {
        return isHidden ? -2: mWd;
    }

    public int offset() {
        return minYOffset;
    }
}
