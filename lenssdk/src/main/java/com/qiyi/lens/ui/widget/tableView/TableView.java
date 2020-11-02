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
package com.qiyi.lens.ui.widget.tableView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import java.util.LinkedList;

/**
 * 不支持单元格合并。 item 长度不等。
 */
public class TableView extends TableLayout {
    boolean boarderEnabled = true;
    java.util.LinkedList<Rect> points;
    Paint paint = new Paint();
    float boarderStrokeWidth = 3;
    float centerStrokeWidth = 2;
    int boarderX;
    int boarderY;
    boolean isRowNameColorEnabled;
    int rowNameX;
    int rowNameColor;
    int boarderColor = Color.BLACK;

    public TableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TableView(Context context) {
        super(context);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {

        if (rowNameX > 0 && isRowNameColorEnabled) {

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(rowNameColor);
            canvas.drawRect(0, 0, rowNameX, boarderY, paint);
        }

        super.dispatchDraw(canvas);


        if (boarderEnabled) {
            if (points != null) {
                paint.setColor(boarderColor);
                paint.setStrokeWidth(centerStrokeWidth);
                for (Rect rect : points) {
                    canvas.drawLine(rect.left, rect.top, rect.right, rect.bottom, paint);
                }
            }
            paint.setStrokeWidth(boarderStrokeWidth);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(0, 0, boarderX, boarderY, paint);
        }

    }

    public void enableBorders() {
        this.boarderEnabled = true;
    }

    @Override
    public void onLayout(boolean changed, int a, int b, int c, int d) {
        super.onLayout(changed, a, b, c, d);
        if (getChildCount() > 0) {
            if (boarderEnabled) {
                if (points == null) {
                    points = new LinkedList<>();
                } else {
                    points.clear();
                }

                makeLines();

            } else if (isRowNameColorEnabled) {
                View view = getChildAt(0);
                int left = view.getLeft();
                if (view instanceof ViewGroup) {
                    ViewGroup group = (ViewGroup) view;
                    int gcount = group.getChildCount();
                    if (gcount > 0) {
                        rowNameX = left + group.getChildAt(0).getLeft();
                    }

                }

            }
        }

    }

    private void makeLines() {

        int count = getChildCount();
        //[not include the boarder]
        int bottom;
        FlexibleIntArray rows = new FlexibleIntArray();

        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            bottom = view.getBottom();
            rows.add(bottom);
        }

        FlexibleIntArray cols = new FlexibleIntArray();

        //[make verticals]
        View view = getChildAt(0);
        int left = view.getLeft();
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int gcount = group.getChildCount();
            for (int j = 0; j < gcount; j++) {
                View item = group.getChildAt(j);
                cols.add(left + item.getRight());
            }

        }


        int mwd = cols.get(cols.size() - 1);
        int mht = rows.get(rows.size() - 1);

        //[rows]
        int rowSize = rows.size() - 1; //[not include the final one]
        int y;
        for (int i = 0; i < rowSize; i++) {
            y = rows.get(i);
            points.add(new Rect(0, y, mwd, y));
        }


        int colSize = cols.size() - 1;
        int x;
        for (int i = 0; i < colSize; i++) {
            x = cols.get(i);
            points.add(new Rect(x, 0, x, mht));
        }

        //[boarder]
        boarderX = mwd;
        boarderY = mht;


        if (isRowNameColorEnabled) {
            rowNameX = cols.get(0);

        }


    }


    public void setBoarderStrokeWidth(float x) {
        boarderStrokeWidth = x;
    }


    public void setCenterStrokeWidth(float x) {
        centerStrokeWidth = x;
    }


    public void setRowNameColor(int color) {
        rowNameColor = color;
        isRowNameColorEnabled = true;
    }


    public View getChildAt(int row, int col) {
        View view = getChildAt(row);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            return group.getChildAt(col);

        } else {
            return view;
        }
    }


    public void setBoarderColor(int color) {
        boarderColor = color;
    }


}
