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

import android.content.Context;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

public class UIUtils {
    public static Rect getRectInView(@Nullable ViewGroup viewGroup, @Nullable View view) {
        int[] ar = new int[2];
        if (viewGroup != null) {
            viewGroup.getLocationInWindow(ar);
        }
        int[] br = new int[2];
        if (view != null) {
            view.getLocationInWindow(br);
            Rect rect = new Rect();
            rect.left = br[0] - ar[0];
            rect.top = br[1] - ar[1];
            rect.right = rect.left + view.getWidth();
            rect.bottom = rect.top + view.getHeight();
            return rect;
        }
        return new Rect();
    }

    public static void changeSiblingsVisibility(View view, int visibility) {
        if (view != null) {
            ViewParent parent = view.getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) parent;
                int count = viewGroup.getChildCount();
                for (int i = 0; i < count; i++) {
                    View child = viewGroup.getChildAt(i);
                    if (child != view) {
                        child.setVisibility(visibility);
                    }
                }
                changeSiblingsVisibility((View) parent, visibility);
            }
        }
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static String px2dp(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        float var = (pxValue / scale);
        int v = (int) (var * 10);
        return String.valueOf(v / 10f);
    }


    public static String autoSize(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        float var = pxValue / scale;
        int v = (int) (var * 10);
        float vs = v / 10f;
        if (Math.abs(var - vs) < 0.2f) {
            return (int) vs + "dp";
        } else {
            if (Math.abs(pxValue - (int) pxValue) < 0.1f) {
                return (int) pxValue + "px";
            }
            return String.format("%.1f", pxValue) + "px";
        }

    }

    public static int dp2px(Context context, float dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5f);
    }

    public static int sp2px(Context context, float sp) {
        return (int) TypedValue.applyDimension(2, sp, context.getResources().getDisplayMetrics());
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static View inflateVew(ViewGroup parent, @LayoutRes int layoutId, boolean attached) {
        Context con;
        if (parent != null) {
            con = parent.getContext();
        } else {
            con = ApplicationLifecycle.getInstance().getContext();
        }
        return LayoutInflater.from(con).inflate(layoutId, parent, attached);
    }

    public static int getYInScreen(View view) {
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        return xy[1];
    }

    public static void setText(TextView textView, String info) {
        if(textView != null){
            textView.setText(Utils.isEmpty(info) ? "" : info);
        }
    }
}
