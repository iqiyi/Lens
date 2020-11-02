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

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.qiyi.lens.Constants;

public class ViewManager {

    public static IViewManager create(BasePanel panel) {
        Activity activity = panel.getActivity();
        if (panel.getPanelType() == Constants.PANEL_FLOAT_PANEL_VIEW || panel.getPanelType() == Constants.PANEL_SELECT_VIEW_PANEL) {
            final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            return new IViewManager() {
                private View contentView;

                @Override
                public void addView(View view, ViewGroup.LayoutParams lp) {
                    if (lp instanceof WindowManager.LayoutParams) {
                        WindowManager.LayoutParams wlp = (WindowManager.LayoutParams) lp;
                        ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(lp.width, lp.height);
                        mlp.topMargin = wlp.y;
                        mlp.leftMargin = wlp.x;
                        lp = mlp;
                    }
                    contentView = view;
                    decorView.addView(view, lp);
                }

                @Override
                public void removeView(View view) {
                    decorView.removeView(view);
                }

                @Override
                public void handleMove(float dx, float dy) {
                    contentView.setX(contentView.getX() + dx);
                    contentView.setY(contentView.getY() + dy);
                }

                @Override
                public void invalidate() {
                    contentView.invalidate();
                }

            };
        } else {
            return new FloatWindowManager(activity);
        }
    }

    public interface IViewManager {
        void addView(View view, ViewGroup.LayoutParams lp);

        void removeView(View view);

        void handleMove(float dx, float dy);

        void invalidate();
    }

    static class FloatWindowManager implements IViewManager {
        private WindowManager.LayoutParams clp;
        private WindowManager windowManager;
        private View rootView;

        public FloatWindowManager(Context context) {
            windowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
        }

        @Override
        public void addView(View view, ViewGroup.LayoutParams lp) {
            windowManager.addView(view, lp);
            clp = (WindowManager.LayoutParams) lp;
            rootView = view;
        }

        @Override
        public void removeView(View view) {
            windowManager.removeView(view);
        }

        @Override
        public void handleMove(float dx, float dy) {
            clp.x += dx;
            clp.y += dy;
            windowManager.updateViewLayout(rootView, clp);
        }

        @Override
        public void invalidate() {
            windowManager.updateViewLayout(rootView, clp);
        }


        void brintToFront() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                windowManager.removeViewImmediate(rootView);
                windowManager.addView(rootView, clp);
            }
        }
    }

}
