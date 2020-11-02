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
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.qiyi.lens.utils.LL;

public class TableBuilder {

    private TableView layout;
    private int rowCount;
    private int colCount;
    private String[] colNames;
    private String[] rowNames;
    private int rowNameColor;
    private int colNameColor;
    private ItemDataBinder binder;
    private int itemTextSize = 18;
    private int namesTextSize = 18;
    private int itemTextColor = Color.BLACK;
    private int namesTextColor = Color.BLACK;
    private String[] mData;
    private int itemPadding;
    private float centerStroke;
    private float boarderStroke;
    private boolean isExtraColEnabled;
    private int boarderColor;
    private int[] mStretchableColumns;


    public static TableBuilder obtain() {
        return new TableBuilder();
    }

    public TableBuilder setColumnCountRowCount(int columCount, int rowCount) {
        this.rowCount = rowCount;
        this.colCount = columCount;
        return this;
    }

    public TableBuilder setTableView(TableView tableLayout) {
        this.layout = tableLayout;
        return this;

    }

    public TableBuilder setColumnNames(String[] names) {
        this.colNames = names;
        return this;
    }

    public TableBuilder setRowNames(String[] names) {
        this.rowNames = names;
        return this;
    }

    public TableBuilder setColumnNamesColor(int color) {
        this.colNameColor = color;
        return this;
    }

    public TableBuilder setRowNamesColor(int color) {
        this.rowNameColor = color;
        return this;
    }


    public TableBuilder setDataBinder(ItemDataBinder binder) {
        this.binder = binder;
        return this;
    }

    public TableBuilder setItemTextSize(int size) {
        itemTextSize = size;
        return this;
    }

    public TableBuilder setNamesTextSize(int size) {
        namesTextSize = size;
        return this;
    }


    public TableBuilder setNamesTextColor(int color) {
        namesTextColor = color;
        return this;
    }

    public TableBuilder setItemTextColor(int color) {
        itemTextColor = color;
        return this;
    }

    public TableBuilder setBoarderColor(int color) {
        boarderColor = color;
        return this;
    }

    public TableBuilder setStrokeWidth(float center, float boarder) {

        this.centerStroke = center;
        this.boarderStroke = boarder;
        return this;
    }

    public TableBuilder enableExtraCol() {
        isExtraColEnabled = true;
        return this;
    }


    public TableBuilder setData(String[] data) {

        this.mData = data;
        return this;
    }

    public TableBuilder setStretchableColumns(int ... columns) {
        mStretchableColumns = columns;
        return this;
    }



    public TableLayout build(Context context) {

        if (colCount == 0) {
            return layout;
        }

        itemPadding = (int) (context.getResources().getDisplayMetrics().density * 10);
        if (layout == null) {
            layout = new TableView(context);
        } else {
            layout.removeAllViews();
        }

        if (boarderStroke >= 0) {
            layout.setBoarderStrokeWidth(boarderStroke);
        }

        if (centerStroke >= 0) {
            layout.setCenterStrokeWidth(centerStroke);
        }

        boolean hasRowName = false;

        if(mStretchableColumns != null) {
            for (int i : mStretchableColumns) {
                layout.setColumnStretchable(i, true);
            }
        }

        //[update row count by row Names]
        if (rowNames != null && rowNames.length >= rowCount) {
            rowCount = rowNames.length;
            hasRowName = true;
            if (rowNameColor != 0) {
                layout.setRowNameColor(rowNameColor);
            }
        }
        //[update rowCount by row data]
        if (mData != null && mData.length > 0) {
            int dataLen = mData.length;
            int row = dataLen / colCount;
            if (row > rowCount) {
                rowCount = row;
            }

        }

        if (boarderColor != 0) {
            layout.setBoarderColor(boarderColor);
        }



        if (colNames != null && colNames.length >= colCount) {
            colCount = colNames.length;
            //[add col Name]

            TableRow tableRow = new TableRow(context);
            if (hasRowName) {
                tableRow.addView(new TextView(context));//[empty]
            }
            //[-1 row stands for col names]
            inflateRowData(tableRow, colNames, 0, -1);
            layout.addView(tableRow);

            if (colNameColor != 0) {
                tableRow.setBackgroundColor(colNameColor);
            }
        }


        if (rowCount > 0) {
            int p = 0;
            while (p < rowCount) {
                TableRow row = new TableRow(context);
                if (hasRowName) {
                    View itemView = createRowNameView(context, row, p, rowNames[p]);
                    row.addView(itemView);
                }

                inflateRowData(row, mData, p * colCount, p);
                layout.addView(row);
                p++;
            }
        }


        return layout;

    }


    private View createRowNameView(Context context, TableRow rowParent, int rowId, String name) {

        View itemView;
        if (binder != null) {
            itemView = binder.createItemView(rowParent, rowId, -1);
            binder.bindData(name, itemView, rowId, -1);
        } else {
            TextView textView = createDefaultItemView(context, namesTextSize, namesTextColor);
            textView.setText(name);
            itemView = textView;
        }

        return itemView;
    }


    public TextView createDefaultItemView(Context context, int textSize, int textColor) {

        TextView textView = new TextView(context);
        textView.setTextColor(textColor);
        textView.setTextSize(textSize);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(itemPadding, 0, itemPadding, 0);

        return textView;

    }

    public TextView createDefaultItemView(Context context) {
        return createDefaultItemView(context, itemTextSize, itemTextColor);
    }

    public TextView createDefaultNamesView(Context context) {
        return createDefaultItemView(context, namesTextSize, namesTextColor);
    }


    private void inflateRowData(TableRow row, String[] data, int offset, int rowId) {
        //[asset data]
//        if(data != null && data.length > offset + colCount) {
        int p = 0;
        Context context = row.getContext();
        boolean dataValid = data != null && data.length >= offset + colCount;

        int end = isExtraColEnabled ? colCount + 1 : colCount;

        while (p < end) {

            if (p == colCount) {
                dataValid = false;
            }

            View itemView;
            if (binder != null) {
                itemView = binder.createItemView(row, rowId, p);

                binder.bindData(dataValid ? data[p + offset] : null, itemView, rowId, p);
            } else {

                TextView textView = createDefaultItemView(context, itemTextSize, itemTextColor);
                if (dataValid) {
                    textView.setText(data[offset + p]);
                }

                itemView = textView;

            }
            row.addView(itemView);


            p++;
        }

//        }

    }


    public interface ItemDataBinder {
        //[if view is null , need create view ]
        void bindData(String data, View view, int row, int column);

        View createItemView(ViewGroup parent, int row, int column);
    }


    /**
     * 设置某个单元的颜色:
     * call after build
     *
     * @param row     : data row : row name & column name is not included : row starts form 0
     * @param column: starts form 0
     */

    public void setItemColor(int row, int column, int color) {
        if (layout != null) {
            View view = layout.getChildAt(row, column);
            view.setBackgroundColor(color);
        }


    }


    public void setAllColor(int color) {
        if (layout != null) {
            int count = layout.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = layout.getChildAt(i);
                if (view instanceof ViewGroup) {
                    ViewGroup group = (ViewGroup) view;
                    int n = group.getChildCount();
                    for (int k = 0; k < n; k++) {
                        group.getChildAt(k).setBackgroundColor(color);
                    }
                } else {
                    view.setBackgroundColor(color);
                }
            }
        }
    }


    public void setData(String data, int row, int column) {
        if (layout != null) {
            View view = layout.getChildAt(row, column);
            if (binder != null) {
                binder.bindData(data, view, row, column);
            } else if (view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setText(data);
            }
        }
    }

    public void addRow(String[] data) {

        addRow(null, data);

    }

    /**
     * @param data : len can be more or less than cols count
     */
    public void addRow(String rowName, String[] data) {
        if (layout != null) {
            int rowId = rowCount;
            rowCount++;

            TableRow row = new TableRow(layout.getContext());
            if (rowName != null) {
                View view = createRowNameView(layout.getContext(), row, rowId, rowName);
                row.addView(view);
            }
            inflateRowData(row, data, 0, rowId);
            layout.addView(row);

            //[avoid login er]
            if (mData == null) {
                mData = new String[0];
            }

            int len = mData.length;
            String[] nData = new String[len + colCount];
            int i = 0;
            for (; i < len; i++) {
                nData[i] = mData[i];
            }

            int dataLen = data == null ? 0 : data.length;
            for (int j = 0; j < colCount; j++) {

                if (j < dataLen) {
                    nData[i++] = data[j];
                } else {
                    nData[i++] = "";
                }
            }

            this.mData = nData;
        }

    }


    /**
     * return the table row where this view is inside
     */
    public View getTableRow(View view) {
        while (view.getParent() != layout && view.getParent() != null) {
            view = (View) view.getParent();

        }
        if (view.getParent() == layout) {

            return view;
        }
        return null;
    }


    public int getColCount() {
        return colCount;
    }

    public void deleteRow(View view) {
        while (view.getParent() != layout && view.getParent() != null) {
            view = (View) view.getParent();

        }
        if (view.getParent() == layout && layout != null) {
            int id = layout.indexOfChild(view);
            if (colNames != null && colNames.length > 0) {
                id--;
            }
            layout.removeView(view);

            if (mData != null && mData.length > 0) {
                int len = mData.length;
                String[] nData = new String[len - colCount];
                int lower = id * colCount;
                int i = 0;
                for (; i < lower; i++) {
                    nData[i] = mData[i];
                }

                int p = i + colCount;
                for (int j = p; j < len; j++) {
                    nData[i++] = mData[j];
                }

                this.mData = nData;
            }

        }


    }

    public String[] getData() {
        return mData;
    }


    public void saveData(View view, String[] data) {
        TableRow row = (TableRow) getTableRow(view);
        saveData(row, data);
    }

    //save data after edit: mData cant be null
    public void saveData(TableRow row, String[] data) {
        if (data == null || data.length == 0) return;

        int id = layout.indexOfChild(row);
        if (colNames != null && colNames.length > 0) {
            id--;
        }

        int start = id * colCount;
        int end = start + colCount;

        int dataEnd = mData == null ? 0 : mData.length;

        if (end > dataEnd) return;


        if (mData != null) {
            int count = Math.min(colCount, data.length);
            int p = 0;
            end = start + count;
            for (int i = start; i < end; i++) {
                mData[i] = data[p++];
            }

        }

    }


    public void printData() {
        if (mData != null) {
            StringBuilder sb = new StringBuilder();
            for (String d : mData) {
                sb.append(d);
                sb.append(" , ");
            }
            LL.d("table Data: " + sb.toString());
        }
    }


    public static class DefaultBinder implements ItemDataBinder {
        TableBuilder builder;

        public DefaultBinder(TableBuilder builder) {
            this.builder = builder;
        }

        @Override
        public void bindData(String data, View view, int row, int column) {
            TextView textView = (TextView) view;
            textView.setText(data);
        }

        @Override
        public TextView createItemView(ViewGroup parent, int row, int column) {
            return builder.createDefaultItemView(parent.getContext());
        }
    }
}
