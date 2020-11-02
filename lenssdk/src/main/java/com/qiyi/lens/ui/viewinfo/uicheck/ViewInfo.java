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
package com.qiyi.lens.ui.viewinfo.uicheck;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.qiyi.lens.ui.viewinfo.json.IJson;
import com.qiyi.lens.ui.viewinfo.json.ImageJson;
import com.qiyi.lens.ui.viewinfo.json.TextJson;
import com.qiyi.lens.ui.viewinfo.json.ViewJson;
import com.qiyi.lens.utils.iface.IUIVerifyFactory;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * bug : View Pager  的下一页面进入到屏幕可显示区域导致视图被选中了；
 */
public class ViewInfo {
    View view;
    private boolean isOpaque;
    boolean isTransParent;
    boolean visited;
    private ViewInfo viewParent;
    Region region;
    // view is hidden , will be deleted from tree
    private boolean isHidden;
    boolean visible;
    LinkedList<ViewInfo> childList = new LinkedList<>();
    private Rect rect;
    DrawingStack stack;

    ViewInfo(View view, ViewInfo parent) {
        this.view = view;
        this.viewParent = parent;
        if (parent != null) {
            parent.addChild(this);
            region = parent.region;
            stack = parent.stack;
        }

        int[] ar = {0, 0};
        view.getLocationOnScreen(ar);
        rect = new Rect(ar[0], ar[1], ar[0] + view.getWidth(), ar[1] + view.getHeight());

    }

    /**
     * view is a child of this viewInfo
     * 在顶部的视图在队列的前面。
     */
    private void addChild(ViewInfo info) {
        childList.addFirst(info);
    }

    // 剔除不在屏幕内部的数据
    boolean isOutOfScreen() {
        Rect rect = getRect();
        return rect.right <= 8 || rect.top >= stack.sht || rect.bottom <= 8 || rect.left >= stack.swd;
    }

    private boolean isViewTransparent() {
        return view instanceof ViewGroup && view.getBackground() == null;
    }

    /**
     * 运算自己是否是实心的
     */
    void figure() {

        // if is hiden
        if (region.quickContains(getRect()) || isOutOfScreen()) {
            // this view is hidden
            visible = false;
            isHidden = true;
            if (!childList.isEmpty()) {
                // if all children is hidden , then this could be hidden
                for (ViewInfo info : childList) {
                    isHidden &= info.isHidden;
                }
            }
            return;
        } else {
            visible = true;
            isOpaque = isViewOpaque(view);
            isTransParent = isViewTransparent();
            if (stack != null && !isTransParent) {
                stack.increateVisibleCount();
            }
            // if children makes it opaque , this view will be hidden, so no need to figure children insects
            if (isOpaque) {
                region.op(getRect(), Region.Op.UNION);
            }
        }
        // remove hidden children
        if (!childList.isEmpty()) {
            Iterator<ViewInfo> iterator = childList.iterator();
            // remove the hidden views
            while (iterator.hasNext()) {
                ViewInfo viewInfo = iterator.next();
                if (viewInfo.isHidden) {
                    iterator.remove();
                }
            }
        }
    }


    public Rect getRect() {
        return rect;
    }

    private boolean isBitmapOpaque(Bitmap bitmap) {
        // check alpha
        int wd = bitmap.getWidth();
        int ht = bitmap.getHeight();

        int px1 = bitmap.getPixel(0, 0);
        int px2 = bitmap.getPixel(wd - 1, ht - 1);
        int px3 = bitmap.getPixel(wd - 1, 0);
        int px4 = bitmap.getPixel(wd - 1, ht - 1);
        int px5 = bitmap.getPixel(wd >> 1, ht >> 1);


        return Color.alpha(px1) < 255 &&
                Color.alpha(px2) < 255 &&
                Color.alpha(px3) < 255 &&
                Color.alpha(px4) < 255 &&
                Color.alpha(px5) < 255;


    }

    // todo verify ImageView
    private boolean isViewOpaque(View view) {
        if (view.getAlpha() < 1f) {
            return false;
        }

        Drawable drawable = view.getBackground();
        if (drawable != null) {
            if (drawable instanceof ColorDrawable) {
                int color = ((ColorDrawable) drawable).getColor();
                if (Color.alpha(color) < 255) {
                    return false;
                }
                return true;
            } else if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                if (bitmap.getConfig() == Bitmap.Config.RGB_565) {
                    return false;
                } else if (bitmap.getConfig() == Bitmap.Config.ARGB_8888) {
                    return isBitmapOpaque(bitmap);
                }
            } else if (drawable instanceof GradientDrawable && Build.VERSION.SDK_INT >= 24) {
                int[] crs = ((GradientDrawable) drawable).getColors();
                if (crs != null && crs.length > 0) {
                    boolean bl = Color.alpha(crs[0]) == 256;
                    for (int var : crs) {
                        bl &= Color.alpha(var) == 256;
                    }
                    return bl;
                }
                return false;
            }
        } else { // check if content could be opaque
            return view.isOpaque();
        }
        return false;
    }

    @Override
    public @NonNull
    String toString() {
        return isOpaque + " " + view.toString();
    }

    IJson buildData(IUIVerifyFactory factory) {
        if (view instanceof ImageView) {
            return new ImageJson(factory, view, rect);
        } else if (view instanceof TextView) {
            return new TextJson(factory, view, rect);
        } else {
            return new ViewJson(factory, view, rect);
        }
    }

}
