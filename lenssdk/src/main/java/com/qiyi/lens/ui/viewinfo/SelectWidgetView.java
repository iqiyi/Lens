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
package com.qiyi.lens.ui.viewinfo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiyi.lens.ui.viewinfo.json.IJson;
import com.qiyi.lens.ui.viewinfo.json.ViewJson;
import com.qiyi.lens.ui.viewinfo.uicheck.DrawingStack;
import com.qiyi.lens.ui.viewinfo.uicheck.IRect;

import java.util.LinkedList;

import static com.qiyi.lens.utils.UIUtils.getScreenHeight;
import static com.qiyi.lens.utils.UIUtils.getScreenWidth;

/**
 * 新增文本防止碰撞算法
 */
public class SelectWidgetView extends View {


    protected Widget childWidget, parentWidget;
    private DrawKit drawKit;

    private Widget currentWidget;
    private Widget relativeWidget;

    //[shows relative position with its parent]
    private boolean initState;

    private LinkedList<Widget> targetWidgets;
    private int currentWidgetSelected;
    private boolean widgetLoaded;
    private boolean showRelativePosition;
    private boolean showSibling;
    private int offset;
    private View mViewRoot;

    public SelectWidgetView(Context context, View viewRoot) {
        super(context);
        mViewRoot = viewRoot;
        init();
    }


    private void init() {
        initState = true;
        //
        TextView breifView = new TextView(getContext());
        breifView.setTextColor(Color.GREEN);
        breifView.setTextSize(13);
        breifView.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        int screenWidth = getScreenWidth(getContext());
        int screenHeight = getScreenHeight(getContext());
        targetWidgets = new LinkedList<>();
        drawKit = new DrawKit(getContext(),
                new TextReallocation(this, screenWidth, screenHeight));
        drawKit.init();
    }

    public void setShowRelativePosition(boolean showRelativePosition) {
        this.showRelativePosition = showRelativePosition;
        invalidate();
    }

    public void setShowSibling(boolean showSibling) {
        this.showSibling = showSibling;
        invalidate();
    }

    public void selectParent() {
        if (parentWidget != null) {
            parentWidget = parentWidget.getParent();
        }
        if (showRelativePosition) {
            relativeWidget = parentWidget;
        } else {
            currentWidget = parentWidget;
            ViewInfoHolder.getInstant().setCurrentWidget(currentWidget);
        }
        invalidate();
    }

    private Widget getWidgetByView(View view) {
        if (view != null) {
            return new Widget(drawKit, view, offset);
        }
        return null;
    }

    public void selectListRow(View view) {
        Widget widget = getWidgetByView(view);
        onSelectWidget(widget);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        childWidget = null;
        parentWidget = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawKit.draw(canvas, currentWidget, relativeWidget, initState || showRelativePosition,
                showSibling);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                onActionUp(event);
                break;
            case MotionEvent.ACTION_MOVE:

                break;
        }
        return true;
    }


    private void onActionUp(MotionEvent event) {

        targetWidgets.clear();
        IRect rt = null;
        if (rects != null) {
            float x = event.getX();
            float y = event.getY();
            y += offset;
            int count = rects.length;
            for (int i = count - 1; i >= 0; i--) {
                IRect r = (IRect) rects[i];
                if (r.isInside(x, y)) {
                    rt = r;
                    ViewJson json = (ViewJson) rt;
                    Widget widget = new Widget(drawKit, json.getView(), offset);
                    targetWidgets.add(widget);
//                    break;
                }
            }
        }
        if (!targetWidgets.isEmpty()) {
            // check for next: support click next
            boolean next = false;
            if (currentWidget != null) {
                for (Widget wd : targetWidgets) {
                    if (wd.getView() == currentWidget.getView()) {
                        next = true;
                    }
                }
            }
            if (next) {
                selectNextWidget();
            } else {
                onSelectWidget(targetWidgets.get(0));
            }
        }
    }


    private void onSelectWidget(Widget widget) {
        if (widget != null) {

            parentWidget = widget;
            if (showRelativePosition && currentWidget != null) {
                relativeWidget = widget;
            } else {
                currentWidget = widget;
                ViewInfoHolder.getInstant().setCurrentWidget(currentWidget);

                if (currentWidget != null) {
                    initState = true;
                    relativeWidget = currentWidget.getParent();
                }

            }
            invalidate();
        }
    }


    // 要求子试图可见, 并且父试图也可见. 因为存在子试图超出 父视图 范围的情况
    private boolean isParentVisible(Widget parent, int x, int y) {
        if (parent == null) {
            return true;
        }
        return parent.isViewVisible() && parent.getRect().contains(x, y) &&
                isParentVisible(parent.getParent(), x, y);
    }


    public void selectNextWidget() {
        Widget widget = null;
        if (targetWidgets != null && targetWidgets.size() > 1) {
            currentWidgetSelected++;
            currentWidgetSelected = currentWidgetSelected % targetWidgets.size();
            widget = targetWidgets.get(currentWidgetSelected);
            onSelectWidget(widget);
        }
    }

    DrawingStack stack;
    IJson[] rects;


    private void loadWidgets() {
        if (stack == null) {
            stack = ViewRootLoader.loadByRootView(mViewRoot);
            rects = stack.getVisibleViewData(null);
        }
    }


    public void onLayout(boolean ch, int a, int b, int c, int d) {
        super.onLayout(ch, a, b, c, d);

        if (!widgetLoaded) {
            widgetLoaded = true;
            int[] ar = {0, 0};
            getLocationOnScreen(ar);
            offset = ar[1];
            drawKit.updateOffset(ar[1]);
            post(new Runnable() {
                @Override
                public void run() {
                    loadWidgets();
                }
            });
        }
    }

    public RectF getCurrentViewRect() {
        if (currentWidget != null) {
            return currentWidget.getRect();
        }
        return null;
    }

}
