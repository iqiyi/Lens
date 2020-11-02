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
package com.qiyi.lens.utils;

import android.graphics.Rect;

import androidx.annotation.NonNull;

import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.LinkedList;
import java.util.List;

import static com.qiyi.lens.utils.UIUtils.dp2px;


public final class TouchDelegateUtils {

    private TouchDelegateUtils() {
    }

    public static void expandHitRect(@NonNull View target, int size) {
        expandHitRect(target, size, size, size, size);
    }

    public static void expandHitRect(@NonNull final View target, final int l, final int t,
                                     final int r, final int b) {
        final View parent = (View) target.getParent();
        final Rect expandBounds = new Rect();
        expandBounds.left = dp2px(target.getContext(), l);
        expandBounds.top = dp2px(target.getContext(), t);
        expandBounds.right = dp2px(target.getContext(), r);
        expandBounds.bottom = dp2px(target.getContext(), b);
        TouchDelegate touchDelegate = parent.getTouchDelegate();
        if (touchDelegate instanceof TouchDelegateGroup) {
            TouchDelegateGroup touchDelegateGroup = (TouchDelegateGroup) touchDelegate;
            touchDelegateGroup.addTouchDelegate(new TouchDelegateChildren(target, expandBounds));
        } else {
            TouchDelegateGroup delegate = new TouchDelegateGroup(target);
            parent.setTouchDelegate(delegate);
            delegate.addTouchDelegate(new TouchDelegateChildren(target, expandBounds));
        }
    }

    private static class TouchDelegateChildren extends TouchDelegate {

        private View mDelegateView;
        private Rect mBounds;
        private Rect mExpandBounds;
        private boolean mDelegateTargeted;
        private Rect mSlopBounds;
        private int mSlop;

        private TouchDelegateChildren(View delegateView, Rect mExpandBounds) {
            this(new Rect(), mExpandBounds, delegateView);
        }

        private TouchDelegateChildren(Rect bounds, Rect mExpandBounds, View delegateView) {
            super(bounds, delegateView);
            this.mDelegateView = delegateView;
            this.mBounds = bounds;
            this.mExpandBounds = mExpandBounds;
            mSlopBounds = new Rect(bounds);
            mSlop = ViewConfiguration.get(delegateView.getContext()).getScaledTouchSlop();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if (View.GONE == mDelegateView.getVisibility()) {
                return false;
            }
            if (!mDelegateView.isEnabled()) {
                return false;
            }

            mDelegateView.getHitRect(mBounds);
            mBounds.left -= mExpandBounds.left;
            mBounds.top -= mExpandBounds.top;
            mBounds.right += mExpandBounds.right;
            mBounds.bottom += mExpandBounds.bottom;
            mSlopBounds.set(mBounds);
            mSlopBounds.inset(-mSlop, -mSlop);

            int x = (int) event.getX();
            int y = (int) event.getY();
            boolean sendToDelegate = false;
            boolean hit = true;
            boolean handled = false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Rect bounds = mBounds;

                    if (bounds.contains(x, y)) {
                        mDelegateTargeted = true;
                        sendToDelegate = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_MOVE:
                    sendToDelegate = mDelegateTargeted;
                    if (sendToDelegate) {
                        Rect slopBounds = mSlopBounds;
                        if (!slopBounds.contains(x, y)) {
                            hit = false;
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    sendToDelegate = mDelegateTargeted;
                    mDelegateTargeted = false;
                    break;
            }
            if (sendToDelegate) {
                final View delegateView = mDelegateView;

                if (hit) {
                    // Offset event coordinates to be inside the target view
                    event.setLocation(delegateView.getWidth() / 2f, delegateView.getHeight() / 2f);
                } else {
                    // Offset event coordinates to be outside the target view (in case it does
                    // something like tracking pressed state)
                    int slop = mSlop;
                    event.setLocation(-(slop * 2), -(slop * 2));
                }
                handled = delegateView.dispatchTouchEvent(event);
            }
            return handled;
        }
    }

    private static class TouchDelegateGroup extends TouchDelegate {
        private static final Rect USELESS_HACKY_RECT = new Rect();
        private final List<TouchDelegateChildren> mTouchDelegates = new LinkedList<>();
        private TouchDelegateChildren mCurrentTouchDelegate;

        public TouchDelegateGroup(View uselessHackyView) {
            super(USELESS_HACKY_RECT, uselessHackyView);
        }

        public void addTouchDelegate(@NonNull TouchDelegateChildren touchDelegate) {
            mTouchDelegates.add(touchDelegate);
        }

        public void removeTouchDelegate(TouchDelegateChildren touchDelegate) {
            mTouchDelegates.remove(touchDelegate);
            if (mCurrentTouchDelegate == touchDelegate) {
                mCurrentTouchDelegate = null;
            }
        }

        public void clearTouchDelegates() {
            mTouchDelegates.clear();
            mCurrentTouchDelegate = null;
        }

        @Override
        public boolean onTouchEvent(@NonNull MotionEvent event) {
            TouchDelegate delegate = null;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    for (int i = 0; i < mTouchDelegates.size(); i++) {
                        TouchDelegateChildren touchDelegate = mTouchDelegates.get(i);
                        if (touchDelegate.onTouchEvent(event)) {
                            mCurrentTouchDelegate = touchDelegate;
                            return true;
                        }
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    delegate = mCurrentTouchDelegate;
                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    delegate = mCurrentTouchDelegate;
                    mCurrentTouchDelegate = null;
                    break;
                default:
                    break;
            }

            return delegate != null && delegate.onTouchEvent(event);
        }
    }
}
