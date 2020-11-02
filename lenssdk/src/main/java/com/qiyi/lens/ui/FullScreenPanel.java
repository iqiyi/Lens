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

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.StringRes;

import com.qiyi.lens.Constants;
import com.qiyi.lens.ui.title.DefaultTitleBinder;
import com.qiyi.lens.ui.widget.FullScreenFrameLayout;
import com.qiyi.lens.ui.widget.ViewInAnimation;
import com.qiyi.lens.utils.ApplicationLifecycle;
import com.qiyi.lens.utils.UIUtils;
import com.qiyi.lens.utils.Utils;
import com.qiyi.lenssdk.R;

import java.lang.ref.WeakReference;

/**
 * 全屏面板 基类
 */
public class FullScreenPanel extends BasePanel implements UIStateCallBack {
    private int screenWidth;
    private View mContentView;
    private ViewInAnimation animation;
    // should wait for in animation to finish
    private ViewInAnimation dismissAnimation;
    private int animationOffset;
    protected WeakReference<FloatingPanel> wkFloatPanel;
    private OnDismissListener onDismissListener;
    private ProgressBar progressView;
    private Object resultData;
    private int resultCode;
    private View mLoadingView;
    protected ViewGroup viewGroup;
    protected boolean enableAnimation = true;
    private boolean pendingShowProgress;
    //if metaInfo or titleInfo has been set , this panel will show Title Bar
    private String mMetaInfo;
    private String mTitleInfo;
    private DefaultTitleBinder titleBinder;

    public FullScreenPanel(FloatingPanel panel) {
        wkFloatPanel = new WeakReference<>(panel);
        screenWidth = UIUtils.getScreenWidth(context);
        animationOffset = UIUtils.dp2px(context, 30);
    }

    public final View createView() {
        viewGroup = new FullScreenFrameLayout(getContext()) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    if (!onBackPressed()) {
                        dismiss();
                    }
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }
        };
        viewGroup.setBackgroundDrawable(new ColorDrawable(0));

        int topPadding = genTitleBinder();

        createContentView(viewGroup, topPadding);

        return viewGroup;
    }


    protected int genTitleBinder() {
        if (!Utils.isEmpty(mMetaInfo) || !Utils.isEmpty(mTitleInfo)) {
            //inflate TitleView
            titleBinder = new DefaultTitleBinder(this, viewGroup)
                    .create()
                    .title(mTitleInfo)
                    .meta(mMetaInfo);
            return titleBinder.bind();

        }
        return 0;
    }


    protected Drawable generateBackgroundDrawable() {
        return new ColorDrawable(Color.WHITE);
    }


    private void createContentView(ViewGroup viewGroup, int topPadding) {
        View view = onCreateView(viewGroup);
        if (view == null) {
            view = new View(viewGroup.getContext());
        }
        if (view.getBackground() == null) {
            view.setBackgroundDrawable(generateBackgroundDrawable());
        }
        mContentView = view;
        // add Content View to container
        FrameLayout.LayoutParams fr = new FrameLayout.LayoutParams(-1, -1);
        fr.topMargin = topPadding;
        viewGroup.addView(view, fr);
    }

    //[to implement by children]
    protected View onCreateView(ViewGroup viewGroup) {
        return null;
    }

    @Override
    @CallSuper
    public void onViewCreated(View root) {
        super.onViewCreated(root);
        root.requestFocus();
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);
        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                int keyCode = keyEvent.getKeyCode();
                if (KeyEvent.KEYCODE_BACK == keyCode
                        && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    dismiss();
                    return true;
                }
                return false;
            }

        });
    }

    protected WindowManager.LayoutParams getDefaultLayoutParams() {
        WindowManager.LayoutParams clp = new WindowManager.LayoutParams();
        clp.width = screenWidth;
        clp.height = -1;
        clp.format = PixelFormat.RGBA_8888;
        clp.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        int curFlags = clp.flags;
        curFlags &= ~(
                WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
                        WindowManager.LayoutParams.FLAG_SPLIT_TOUCH);

        clp.flags = curFlags;
        return clp;
    }

    @Override
    public void dismiss() {
        // fix bug : quickly press back twice , animation runs twice.
        if (enableAnimation && dismissAnimation == null) {

            dismissAnimation = new ViewInAnimation(this, 0, screenWidth, 0, 0) {
                @Override
                public void onEnd() {
                    getDecorView().setVisibility(View.GONE);
                    FullScreenPanel.super.dismiss();
                    onDismiss();
                    dismissAnimation = null;
                }
            };

            //starts only when in animation finished
            if (animation == null) {
                dismissAnimation.start();
            }
        } else {
            super.dismiss();
            onDismiss();
        }
    }


    public FloatingPanel takePanel() {
        FloatingPanel panel = wkFloatPanel.get();
        if (panel != null) {
            wkFloatPanel.clear();
            return panel;
        }
        return null;
    }


    protected FloatingPanel getFloatingPanel() {
        return wkFloatPanel.get();
    }

    @Override
    @CallSuper
    public void onShow() {
        if (enableAnimation) {
            if (animation == null) {
                animation = new ViewInAnimation(this, screenWidth, 0, 0, 0) {
                    @Override
                    public void onEnd() {
                        animation = null;
                        if (dismissAnimation != null) {
                            dismissAnimation.start();
                        }
                    }
                };
                animation.start();
            }
        }

        if (pendingShowProgress) {
            pendingShowProgress = false;
            showProgress();
        }
    }


    //[todo override and release]
    protected void onDismiss() {
        if (onDismissListener != null) {
            onDismissListener.onDismissListener(this);
        }
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public View getContentView() {
        return mContentView;
    }

    public int getWidth() {
        return screenWidth;
    }

    public void showToast(String data) {
        FrameLayout root = (FrameLayout) getDecorView();
        TextView textView = (TextView) inflateView(R.layout.lens_toast, root);
        FrameLayout.LayoutParams clp = (FrameLayout.LayoutParams) textView.getLayoutParams();
        clp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        clp.bottomMargin = UIUtils.dp2px(getContext(), 120);
        textView.setText(data);
        root.addView(textView);
        new ToastAnimation(0, 0, animationOffset, 0).start(textView);
    }

    @Override
    public void showLoading() {
        if (mLoadingView == null) {
            mLoadingView = new ProgressBar(context);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            mLoadingView.setLayoutParams(params);
        }
        if (mLoadingView.getParent() == null) {
            ((FrameLayout) getDecorView()).addView(mLoadingView);
        }
        if (mLoadingView.getVisibility() == View.GONE) {
            mLoadingView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideLoading() {
        if (mLoadingView != null) {
            if (mLoadingView.getVisibility() != View.GONE) {
                mLoadingView.setVisibility(View.GONE);
            }
        }
    }

    static class ToastAnimation extends TranslateAnimation implements Animation.AnimationListener {

        View mView;

        ToastAnimation(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta) {
            super(fromXDelta, toXDelta, fromYDelta, toYDelta);
            this.setAnimationListener(this);
            this.setDuration(1000);
        }

        public void start(View view) {
            this.mView = view;
            mView.startAnimation(this);
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mView != null) {
                mView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ViewGroup group = (ViewGroup) mView.getParent();
                        group.removeView(mView);
                        mView = null;
                    }
                }, 2000);
            }

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    public interface OnDismissListener {
        void onDismissListener(FullScreenPanel panel);
    }

    protected void showProgress() {
        FrameLayout root = (FrameLayout) getDecorView();
        if (root == null) {
            pendingShowProgress = true;
            return;
        }
        if (progressView == null) {
            progressView = (ProgressBar) inflateView(R.layout.lens_progress_bar, (ViewGroup) getDecorView());
            int wd = UIUtils.dp2px(getContext(), 60.0F);
            FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(wd, wd);
            clp.gravity = Gravity.CENTER;
            root.addView(progressView, clp);
        }


    }

    public void dismissProgress() {
        FrameLayout root = (FrameLayout) getDecorView();
        if (root != null && progressView != null) {
            root.removeView(progressView);
        }
    }

    public void setResult(int code, Object data) {
        resultCode = code;
        resultData = data;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Object getResultData() {
        return resultData;
    }

    protected boolean onBackPressed() {
        return false;
    }

    protected int getPanelType() {
        return Constants.PANEL_FULL_SCREEN_PANEL;
    }

    /**
     * panel title to be displayed on top center
     *
     * @param title
     */
    public void setTitle(String title) {
        mTitleInfo = title;
        if (titleBinder != null) {
            titleBinder.updateTitle(title);
        }
    }

    /**
     * panel title to be displayed on top center
     *
     * @param titleRes
     */
    public void setTitle(@StringRes int titleRes) {
        setTitle(getString(titleRes));
    }

    /**
     * Meta info to be displayed on top right
     *
     * @param meta
     */
    public void setMeta(String meta) {
        mMetaInfo = meta;
        if(titleBinder != null) {
            titleBinder.updateMeta(meta);
        }
    }

    /**
     * Meta info to be displayed on top right
     *
     * @param metaRes
     */
    public void setMeta(@StringRes int metaRes) {
        setMeta(getString(metaRes));
    }

    public String getString(@StringRes int id) {
        Context context = ApplicationLifecycle.getInstance().getContext();
        if (context != null) {
            return context.getString(id);
        }
        return "";
    }

}
