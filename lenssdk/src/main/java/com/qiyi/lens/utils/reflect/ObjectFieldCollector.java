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
package com.qiyi.lens.utils.reflect;

import android.app.Activity;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import com.qiyi.lens.ui.widget.tableView.TableBuilder;
import com.qiyi.lens.ui.widget.tableView.TableView;
import com.qiyi.lens.utils.LL;
import com.qiyi.lens.utils.LocalLinkMovementMethod;
import com.qiyi.lens.utils.ViewClassifyUtil;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class ObjectFieldCollector implements Invalidate {


    FieldInfo info;
    //[to check , make sure not add more than once]
    SparseArray<Object> objectHash = new SparseArray();
    // 实现广度优先遍历。来调用makeList， 避免调用占过深问题。
    LinkedList<FieldInfo> infoList = new LinkedList<>();
    DataRefreshCallback dataRefreshCallback;

    public ObjectFieldCollector(Object src) {
        this(src, false);
    }

    public ObjectFieldCollector(Object src, boolean simple) {
        if (src != null) {
            info = create(src, objectHash, this);
            info.setExpand(true);
            info.setAsSimple(simple);
            //new FieldInfo(src ,objectHash);
            infoList.add(info);
            while (!infoList.isEmpty()) {
                FieldInfo info = infoList.pop();
                info.makeList(infoList);
            }
        }
    }

    public void setOnClickListener(Info.OnClickListener listener) {
        if(info != null) {
            info.setOnClickListener(listener);
        }
    }

    public static FieldInfo create(Object data, SparseArray objectHash, Invalidate par) {
        if (data instanceof Activity) {
            return new ActivityObjectFieldInfo(data, objectHash, par);
        } else if (data instanceof View) {
            return new ViewFieldInfo(data, objectHash, par);
        } else {
            return new FieldInfo(data, objectHash, par);
        }
    }

    public static FieldInfo create(Field field, Object src, SparseArray objectHash, Invalidate par) {

//        String data = field.getName();
//        String da= field.getGenericType().toString();
        if (field.getGenericType().toString().startsWith("class [L")) {
            return new ArrayFieldInfo(field, src, objectHash, par);
        } else {
            field.setAccessible(true);
            try {
                Object value = field.get(src);
                if (value instanceof View) {
                    return new ViewFieldInfo(field, src, objectHash, par);
                } else if (value instanceof List) {
                    return new ListFieldInfo(field, src, objectHash, par);
                } else {
                    return new FieldInfo(field, src, objectHash, par);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return new FieldInfo(field, src, objectHash, par);
            }
        }
    }


    //[并不全量生成，点击再展开生成 ， 除非有watch 的数据。 设置初始展开]
    public Spannable makeSpannable() {
        if (info != null) {

            LinkedList<SpanableInfo> list = new LinkedList<>();
            StringBuilder builder = new StringBuilder();
            info.makeSpannable(builder, list);
            builder.append("\n ");//[fix out side touch]
            //[make spannable]
            Spannable spannable = new SpannableStringBuilder(builder);
            while (!list.isEmpty()) {
                SpanableInfo info = list.pop();
                if (info.isClickable()) {
                    spannable.setSpan(info.clickSpan, info.star, info.end
                            , Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
            return spannable;

        }
        return new SpannableString("");
    }


    @Override
    public void invalidate() {
        if (dataRefreshCallback != null) {
            dataRefreshCallback.onDataRefresh();
        }
    }


    public void setDataRefreshCallBack(DataRefreshCallback callBack) {
        dataRefreshCallback = callBack;
    }

    public interface DataRefreshCallback {
        void onDataRefresh();
    }


    public Binder build(ViewClassifyUtil util) {
        return new Binder(info, util);
    }



    public class Binder {
        private ActivityObjectFieldInfo mInfo;
        private ViewClassifyUtil mUtil;

        Binder(FieldInfo info, ViewClassifyUtil util) {
            if (info instanceof ActivityObjectFieldInfo) {
                mInfo = (ActivityObjectFieldInfo) info;
                mInfo.setViewClassifyUtils(util);
            }
            mUtil = util;
        }


        public Binder bindActivityBaseInfo(TextView display) {
            if (mInfo != null ) {
                display.setMovementMethod(LocalLinkMovementMethod.getInstance());
                display.setLinksClickable(false);
                display.setClickable(false);
                Spannable spannable = mInfo.makeSpannable();
                display.setText(spannable);
            } else {
                display.setVisibility(View.GONE);
            }

            return this;
        }


        public Binder bindViewCategorizeInfo(TableView tableView) {
            if (mInfo != null) {
                int count = mUtil.getTypesCount();
                String[] rowNames = new String[count];
                for (int i = 0; i < count; i++) {
                    rowNames[i] = "" + i;
                }

                String[] data = new String[count * 2];
                int p = 0;
                for (int i = 0; i < count; i++) {
                    ViewClassifyUtil.TypeInfo info = mUtil.getTypeByIndex(i);
                    data[p++] = info.name;
                    data[p++] = info.count + "";
                }

                TableBuilder.obtain()
                        .setColumnCountRowCount(2, count)
                        .setColumnNames(new String[]{"视图类型", "个数"})
                        .setRowNames(rowNames)
                        .setColumnNamesColor(0xFFf5f5f5)
                        .setRowNamesColor(0xFFf5f5f5)
                        .setNamesTextSize(25)
                        .setItemTextSize(12)
                        .setData(data)
                        .setTableView(tableView)
                        .setStretchableColumns(1)
                        .build(tableView.getContext());

            } else if (tableView != null) {
                tableView.setVisibility(View.GONE);
            }

            return this;
        }

    }


}
