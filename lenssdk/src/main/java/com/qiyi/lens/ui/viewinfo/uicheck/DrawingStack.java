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

import android.graphics.Region;
import android.view.View;
import android.view.ViewGroup;

import com.qiyi.lens.ui.viewinfo.json.IJson;
import com.qiyi.lens.utils.UIUtils;
import com.qiyi.lens.utils.iface.IUIVerifyFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * 绘制栈： 对于同级别的视图， 会根据绘制顺序，是否完全遮盖等情况，来判定View 是否添加进来。
 * todo：
 * 1） 验证被Child 遮盖的父布局是否是实心
 * 2） 验证图片是否实心。
 */
public class DrawingStack {
    private ViewInfo mViewInfo;
    private int visibleCount;
    int swd;
    int sht;

    public DrawingStack() {
    }

    public ViewInfo getViewInfo() {
        return mViewInfo;
    }

    // data is stored in mViewInfo
    public void loadViews(View parent) {
        loadViews(parent, null);
    }


    void increateVisibleCount() {
        visibleCount++;
    }


    //采用先根遍历 T,R,L
    public IJson[] getVisibleViewData(IUIVerifyFactory factory) {
        IJson[] infos = new IJson[visibleCount];
        LinkedList<ViewInfo> list = new LinkedList<>();
        list.add(mViewInfo);

        int p = 0;
        while (!list.isEmpty()) {

            ViewInfo info = list.pop();
            if (info == null) continue;
            if (info.visible && !info.isTransParent) infos[p++] = info.buildData(factory);
            if (!info.childList.isEmpty()) {
                for (ViewInfo v : info.childList) {
                    list.addFirst(v);
                }
            }
        }

        return infos;
    }


    private Method[] findMethod() throws InvocationTargetException, IllegalAccessException {

        Method getDeclaredMethod = null;
        try {
            getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        Method[] ms = new Method[2];

        if (getDeclaredMethod != null) {
            ms[0] = (Method) getDeclaredMethod.invoke(ViewGroup.class, "isChildrenDrawingOrderEnabled", null);
            ms[1] = (Method) getDeclaredMethod.invoke(ViewGroup.class, "getChildDrawingOrder", new Class[]{int.class, int.class});
            ms[0].setAccessible(true);
            ms[1].setAccessible(true);
        }

        return ms;
    }


    // 后序便利
    public ViewInfo loadViews(View parent, ViewFilter filter) {

        if (filter != null && filter.filterView(parent)) return mViewInfo;

        ViewInfo info = new ViewInfo(parent, null);
        swd = UIUtils.getScreenWidth(parent.getContext()) - 8;
        sht = UIUtils.getScreenHeight(parent.getContext()) - 8;
        info.region = new Region();
        info.stack = this;
        mViewInfo = info;
        LinkedList<ViewInfo> list = new LinkedList<>();
        list.add(info);
        Method[] method = null;
        try {
            method = findMethod();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        while (!list.isEmpty()) {
            ViewInfo viewInfo = list.peekFirst();
            if (!viewInfo.visited && viewInfo.view instanceof ViewGroup) {
                viewInfo.visited = true;//not checking its children any more

                ViewGroup group = (ViewGroup) viewInfo.view;
                int count = group.getChildCount();
                // 从最绘制的开始处理，因此最后会绘制的可以先进行运算处理
                for (int i = 0; i < count; i++) {

                    int pos = i;
                    if (method != null) {
                        pos = getChildDrawingOrder(method, group, i);
                    }

                    View view = group.getChildAt(pos);
                    if (filter != null && filter.filterView(view)) {
                        continue;
                    }
                    ViewInfo vf = new ViewInfo(view, viewInfo);
                    if (!vf.isOutOfScreen()) {
                        list.addFirst(vf);
                    }
                }
            } else { // view :  leaf
                viewInfo.figure();
                list.pollFirst();
            }
        }
        return info;
    }

    private int getChildDrawingOrder(Method[] methods, ViewGroup group, int i) {
        Object var = null;
        int pos = i;
        try {
            var = methods[0].invoke(group);
            if (var != null) {
                boolean enabled = (boolean) var;
                if (enabled)
                    pos = (int) methods[1].invoke(group, group.getChildCount(), i);
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return pos;
    }


    /**
     * filter view
     */
    public interface ViewFilter {
        boolean filterView(View view);
    }
}
