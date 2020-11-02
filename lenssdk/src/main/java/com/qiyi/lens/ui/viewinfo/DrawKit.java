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
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.ColorUtils;

import com.qiyi.lens.utils.UIUtils;

import static com.qiyi.lens.utils.UIUtils.dp2px;
import static com.qiyi.lens.utils.UIUtils.sp2px;

public class DrawKit {
    private final static int TEXT_DIMENSION_COLOR = Color.BLUE;
    private final static int CURRENT_WIDGET_COLOR = 0x30ea2020;
    private final static int SIBLING_WIDGET_COLOR = 0xff81f503;
    private final static int DEBUG_INFO_BG = 0x33242424;
    private final static int RELATIVE_WIDGET_COLOR = SIBLING_WIDGET_COLOR;


    protected Paint textPaint = new Paint();
    Paint textBgPaint = new Paint();
    private Paint selectedPaint = new Paint();
    private Paint borderPaint = new Paint();
    private Path borderPath = new Path();
    private Paint dashLinePaint = new Paint();
    private Path dashLinePath = new Path();
    private Context mContext;
    private int savedColor;
    int screenWidth;
    int screenHeight;
    private TextReallocation allocation;
    private Widget currentWidget, relativeWidget;
    private int mDisplayOffset;
    private int textLineDistance;
    private int halfEndPointWidth;
    int textBgFillingSpace;


    public DrawKit(Context context, TextReallocation textReallocation) {
        mContext = context;
        allocation = textReallocation;
        screenWidth = allocation._swd;
        screenHeight = allocation._sht;
    }


    void init() {
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(sp2px(getContext(), 10));
        textPaint.setColor(Color.RED);
        textPaint.setStrokeWidth(dp2px(getContext(), 1));

        textBgPaint.setAntiAlias(true);
        textBgPaint.setColor(Color.WHITE);
        textBgPaint.setStrokeJoin(Paint.Join.ROUND);

        selectedPaint.setAntiAlias(true);
        selectedPaint.setColor(0x30000000);
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(0xff000000);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp2px(getContext(), 1));

        dashLinePaint = new Paint();
        dashLinePaint.setAntiAlias(true);
        dashLinePaint.setColor(0x90FF0000);
        dashLinePaint.setStyle(Paint.Style.STROKE);
        dashLinePaint.setPathEffect(new DashPathEffect(new float[]{dp2px(getContext(), 4),
                dp2px(getContext(), 8)}, 0));

        textLineDistance = dp2px(getContext(), 5);
        halfEndPointWidth = dp2px(getContext(), 2.5f);
        textBgFillingSpace = dp2px(getContext(), 2);


    }

    Context getContext() {
        return mContext;
    }

    void pushColor(int color) {
        savedColor = textPaint.getColor();
        textPaint.setColor(color);

    }

    void popColor() {
        textPaint.setColor(savedColor);
    }


    public void draw(Canvas canvas, Widget currentWidget, Widget relativeWidget, boolean showRelativePosition
            , boolean showSibling) {

        this.currentWidget = currentWidget;
        this.relativeWidget = relativeWidget;

        allocation.clear();
        drawWidgetArea(canvas, showRelativePosition);

        if (showRelativePosition) {
            //[展示相对距离]
            drawRelativePosition(canvas);
        }

        if (showSibling) {
            //[绘制兄弟节点]
            drawSibling(canvas);
        }

        if (!showRelativePosition) {
            displayViewInfo(canvas);
        }


        //[draw debug info]
        if (currentWidget != null) {
            currentWidget.draw(canvas);
        }

        allocation.draw(canvas);
        this.currentWidget = null;
        this.relativeWidget = null;


    }

    private void drawWidgetArea(Canvas canvas, boolean showRelativePosition) {
        if (showRelativePosition && relativeWidget != null) {
            borderPath.reset();
            borderPath.addRect(new RectF(relativeWidget.getOriginRect()), Path.Direction.CW);
            borderPaint.setColor(RELATIVE_WIDGET_COLOR);
            canvas.drawPath(borderPath, borderPaint);

            selectedPaint.setColor(ColorUtils.setAlphaComponent(RELATIVE_WIDGET_COLOR, 48));
            canvas.drawRect(relativeWidget.getRect(), selectedPaint);
        }
        if (currentWidget != null) {
            selectedPaint.setColor(CURRENT_WIDGET_COLOR);
            canvas.drawRect(currentWidget.getRect(), selectedPaint);

            RectF innerRect = new RectF(currentWidget.getRect());

            //[draw wd  & ht]
            pushColor(TEXT_DIMENSION_COLOR);
            drawDimension(canvas, innerRect, textPaint);
            popColor();


            View view = currentWidget.getView();
            innerRect.left = innerRect.left + view.getPaddingLeft();
            innerRect.bottom = innerRect.bottom - view.getPaddingBottom();
            innerRect.right = innerRect.right - view.getPaddingRight();
            innerRect.top = innerRect.top + view.getPaddingTop();

            if (view.getPaddingLeft() != 0 && view.getPaddingRight() != 0
                    && view.getPaddingTop() != 0 && view.getPaddingBottom() != 0) {
                borderPath.reset();
                borderPath.addRect(new RectF(innerRect), Path.Direction.CW);
                int contrastColor = ColorUtils.setAlphaComponent(
                        getContrastColor(CURRENT_WIDGET_COLOR), 255);
                borderPaint.setColor(contrastColor);
                canvas.drawPath(borderPath, borderPaint);
            }

            borderPath.reset();
            borderPaint.setColor(
                    ColorUtils.setAlphaComponent(CURRENT_WIDGET_COLOR, 255));
            borderPath.addRect(new RectF(currentWidget.getOriginRect()), Path.Direction.CW);
            canvas.drawPath(borderPath, borderPaint);

        }
    }


    void drawArea(Canvas canvas, RectF rect, int color) {

        int cl = borderPaint.getColor();
        borderPath.reset();
        borderPath.addRect(new RectF(rect), Path.Direction.CW);
        int contrastColor = ColorUtils.setAlphaComponent(
                getContrastColor(color), 255);
        borderPaint.setColor(contrastColor);
        canvas.drawPath(borderPath, borderPaint);
        borderPaint.setColor(cl);

    }


    private void drawSibling(Canvas canvas) {
        if (currentWidget == null) return;
        Widget parent = currentWidget.getParent();
        if (parent == null) return;
        ViewGroup viewGroup = (ViewGroup) parent.getView();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            Widget child = new Widget(this, viewGroup.getChildAt(i), mDisplayOffset);
            if (child.equals(currentWidget)) continue;
            borderPaint.setColor(
                    ColorUtils.setAlphaComponent(SIBLING_WIDGET_COLOR, 255));
            canvas.drawRect(child.getRect(), borderPaint);
        }
    }


    private void drawRelativePosition(Canvas canvas) {
        boolean doubleNotNull = true;
        Widget[] selectedWidgets = new Widget[]{currentWidget, relativeWidget};
        for (Widget widget : selectedWidgets) {
            if (widget != null) {
                RectF rect = widget.getRect();
                drawDashLine(canvas, 0, rect.top, screenWidth, rect.top);
                drawDashLine(canvas, 0, rect.bottom, screenWidth, rect.bottom);
                drawDashLine(canvas, rect.left, 0, rect.left, screenHeight);
                drawDashLine(canvas, rect.right, 0, rect.right, screenHeight);
            } else {
                doubleNotNull = false;
            }
        }

        if (doubleNotNull) {
            RectF firstRect = currentWidget.getRect(); // A
            RectF secondRect = relativeWidget.getRect(); //B

            drawDistance(canvas, firstRect, secondRect);

        }

    }


    void drawDistance(Canvas canvas, RectF firstRect, RectF secondRect) {
        boolean drawn = false;

        if (secondRect.top > firstRect.bottom) { // A/B
            drawn = true;
            float x = secondRect.left + secondRect.width() / 2;
            drawLineWithText(canvas, x, firstRect.bottom, x, secondRect.top);
        }

        if (firstRect.top > secondRect.bottom) {//B/A
            drawn = true;
            float x = secondRect.left + secondRect.width() / 2;
            drawLineWithText(canvas, x, secondRect.bottom, x, firstRect.top);
        }

        if (secondRect.left > firstRect.right) {//A/B
            drawn = true;
            float y = secondRect.top + secondRect.height() / 2;
            drawLineWithText(canvas, secondRect.left, y, firstRect.right, y);
        }

        if (firstRect.left > secondRect.right) {//B/A
            drawn = true;
            float y = secondRect.top + secondRect.height() / 2;
            drawLineWithText(canvas, secondRect.right, y, firstRect.left, y);
        }


        drawn |= drawNestedAreaLine(canvas, firstRect, secondRect);
        if (!drawn) {
            drawn = drawNestedAreaLine(canvas, secondRect, firstRect);
        }

        if (!drawn) {
            drawNoLimited(canvas, secondRect, firstRect);
        }
    }

    private void drawDashLine(Canvas canvas, float startX, float startY, float endX, float endY) {
        dashLinePath.reset();
        dashLinePath.moveTo(startX, startY);
        dashLinePath.lineTo(endX, endY);
        canvas.drawPath(dashLinePath, dashLinePaint);
    }

    private boolean drawNestedAreaLine(Canvas canvas, RectF firstRect, RectF secondRect) {
        if (secondRect.left >= firstRect.left
                && secondRect.right <= firstRect.right
                && secondRect.top >= firstRect.top
                && secondRect.bottom <= firstRect.bottom) {

            drawLineWithText(canvas, secondRect.left, secondRect.top + secondRect.height() / 2,
                    firstRect.left, secondRect.top + secondRect.height() / 2);

            drawLineWithText(canvas, secondRect.right, secondRect.top + secondRect.height() / 2,
                    firstRect.right, secondRect.top + secondRect.height() / 2);


            drawLineWithText(canvas, secondRect.left + secondRect.width() / 2, secondRect.top,
                    secondRect.left + secondRect.width() / 2, firstRect.top);


            drawLineWithText(canvas, secondRect.left + secondRect.width() / 2, secondRect.bottom,
                    secondRect.left + secondRect.width() / 2, firstRect.bottom);
            return true;
        }
        return false;
    }


    private void drawNoLimited(Canvas canvas, RectF firstRect, RectF secondRect) {

        drawLineWithText(canvas, secondRect.left, secondRect.top + secondRect.height() / 2,
                firstRect.left, secondRect.top + secondRect.height() / 2);

        drawLineWithText(canvas, secondRect.right, secondRect.top + secondRect.height() / 2,
                firstRect.right, secondRect.top + secondRect.height() / 2);

        drawLineWithText(canvas, secondRect.left + secondRect.width() / 2, secondRect.top,
                secondRect.left + secondRect.width() / 2, firstRect.top);

        drawLineWithText(canvas, secondRect.left + secondRect.width() / 2, secondRect.bottom,
                secondRect.left + secondRect.width() / 2, firstRect.bottom);
    }

    private void drawLineWithText(Canvas canvas, float startX, float startY, float endX, float endY, float endPointSpace) {

        if (startX == endX && startY == endY) {
            return;
        }

        if (startX > endX) {
            float tempX = startX;
            startX = endX;
            endX = tempX;
        }
        if (startY > endY) {
            float tempY = startY;
            startY = endY;
            endY = tempY;
        }

        if (startX == endX) {//[draw on y]
            drawLineWithEndPoint(canvas, startX, startY + endPointSpace, endX, endY - endPointSpace);
            String text = UIUtils.autoSize(getContext(), endY - startY);
            TextInfo info = new TextInfo(this, textPaint, text);
            int textWidth = (int) getTextWidth(text);
            info.setRange(startX - textWidth, endX + textWidth, startY, endY);
            float x = startX + textLineDistance;
            float y = startY + (endY - startY) / 2 + getTextHeight(text) / 2;
            allocation.measure(info, x, y);
//            drawText(canvas, text, x, y);
        } else if (startY == endY) {//draw on x
            drawLineWithEndPoint(canvas, startX + endPointSpace, startY, endX - endPointSpace, endY);
            String text = UIUtils.autoSize(getContext(), endX - startX);
            TextInfo info = new TextInfo(this, textPaint, text);
            int textHeight = (int) getTextHeight(text);
            info.setRange(startX, endX, startY - textHeight, endY + textHeight);
            float x = startX + (endX - startX) / 2 - getTextWidth(text) / 2;
            float y = startY - textLineDistance;
            allocation.measure(info, x, y);

//            drawText(canvas, text, startX + (endX - startX) / 2 - getTextWidth(text) / 2, startY - textLineDistance);
        }
    }

    protected void drawText(Canvas canvas, String text, float x, float y) {
        float left = x - textBgFillingSpace;
        float top = y - getTextHeight(text);
        float right = x + getTextWidth(text) + textBgFillingSpace;
        float bottom = y + textBgFillingSpace;
        // ensure text in screen bound
        if (left < 0) {
            right -= left;
            left = 0;
        }
        if (top < 0) {
            bottom -= top;
            top = 0;
        }
        if (bottom > screenHeight) {
            float diff = top - bottom;
            bottom = screenHeight;
            top = bottom + diff;
        }
        if (right > screenWidth) {
            float diff = left - right;
            right = screenWidth;
            left = right + diff;
        }
        canvas.drawRect(left, top, right, bottom, textBgPaint);
        canvas.drawText(text, left + textBgFillingSpace, bottom - textBgFillingSpace, textPaint);
    }

    private void drawLineWithEndPoint(Canvas canvas, float startX, float startY, float endX, float endY) {
        canvas.drawLine(startX, startY, endX, endY, textPaint);
        if (startX == endX) {
            canvas.drawLine(startX - halfEndPointWidth, startY, endX + halfEndPointWidth, startY, textPaint);
            canvas.drawLine(startX - halfEndPointWidth, endY, endX + halfEndPointWidth, endY, textPaint);
        } else if (startY == endY) {
            canvas.drawLine(startX, startY - halfEndPointWidth, startX, endY + halfEndPointWidth, textPaint);
            canvas.drawLine(endX, startY - halfEndPointWidth, endX, endY + halfEndPointWidth, textPaint);
        }
    }

    private void drawLineWithText(Canvas canvas, float startX, float startY, float endX, float endY) {
        drawLineWithText(canvas, startX, startY, endX, endY, 0);
    }

    private float getTextHeight(String text) {
        Rect rect = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }

    private float getTextWidth(String text) {
        return textPaint.measureText(text);
    }

    private int getContrastColor(int sourceColor) {
        return Color.rgb(255 - Color.red(sourceColor),
                255 - Color.green(sourceColor),
                255 - Color.blue(sourceColor));
    }

    private void drawDimension(Canvas canvas, RectF rect, Paint paint) {
        String wds = "wd: " + UIUtils.autoSize(getContext(), rect.width());
        String hts = "ht: " + UIUtils.autoSize(getContext(), rect.height());
        int padding = 10;
        float twd = paint.measureText(wds);
        float tht = paint.measureText(hts);
        float x;
        //[避免都在中间展示不下]
        x = rect.left;

        TextInfo info = new TextInfo(this, textPaint, wds);

        if (x + twd > screenWidth) {
            x = screenWidth - twd - mDisplayOffset;
        }


        Paint.FontMetrics metrics = paint.getFontMetrics();
        float height = metrics.bottom - metrics.top;
        float y;
        if (rect.top - height - padding > mDisplayOffset) {
            y = rect.top - padding;
        } else {
            y = rect.bottom + padding;
        }

        //在整个宽度范围上可以移动
        info.setRange(rect.left, rect.right, y - height, y + height);
        allocation.measure(info, x, y);

//        drawText(canvas, wds, x, y);


        //[draw ht]
        if (rect.right + tht + padding < screenWidth) {
            //[draw at right ]
            x = rect.right + padding;
        } else if (rect.left - tht - padding > 0) {
            x = (int) (rect.left - tht - padding);
        } else {
            x = rect.left + padding;
        }
        y = rect.top + height; //(rect.height() - height)/2 + rect.top;
        info = new TextInfo(this, textPaint, hts);
        info.setRange(x - twd, x + twd, rect.top, rect.bottom);
        allocation.measure(info, x, y);
//        drawText(canvas, hts, x, y);

    }

    /**
     * 绘制原则： 如果可以在视图内部区域展示，就展示到内部， 否则展示到外部合适地方
     *
     * @param viewRect ： 当前视图的VisibleRect 信心
     * @message: 需要绘制的文案
     */
    void drawDebugInfo(Canvas canvas, String message, RectF viewRect) {
        MultiTextInfo info = new MultiTextInfo(this, textPaint, message);
        info.setRange(0, screenWidth, 0, screenHeight);
        allocation.measure(info, viewRect.left, viewRect.top);
    }

    private void displayViewInfo(Canvas canvas) {

    }


    void updateOffset(int offset) {
        mDisplayOffset = offset;
    }


}
