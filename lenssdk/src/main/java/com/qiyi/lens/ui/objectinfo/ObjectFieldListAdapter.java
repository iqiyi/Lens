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
package com.qiyi.lens.ui.objectinfo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qiyi.lens.utils.ApplicationLifecycle;
import com.qiyi.lens.utils.TouchDelegateUtils;
import com.qiyi.lens.utils.UIUtils;
import com.qiyi.lens.utils.configs.DebugInfoConfig;
import com.qiyi.lens.utils.reflect.FieldInfo;
import com.qiyi.lens.utils.reflect.ObjectFieldCollector;
import com.qiyi.lenssdk.R;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObjectFieldListAdapter extends RecyclerView.Adapter<ObjectFieldListAdapter.ViewHolder> {


    private Object object;
    private FieldInfo fieldInfo;
    private LinkedList<FieldInfo> infoList = new LinkedList<>();

    public ObjectFieldListAdapter(Object object) {
        this.object = object;
    }

    public void refreshData(String keywords, boolean showAllField) {
        SparseArray sparseArray = new SparseArray();
        this.fieldInfo = ObjectFieldCollector.create(object, sparseArray, null);
        infoList.clear();
        if (isCollection()) {
            unboxInnerObject(fieldInfo, infoList, sparseArray);
        } else {
            fieldInfo.makeList(infoList, showAllField, false);
        }
        if (!TextUtils.isEmpty(keywords)) {
            infoList = filterItems(keywords, infoList);
        }
        if (!isCollection()) {
            Collections.sort(infoList, new Comparator<FieldInfo>() {
                @Override
                public int compare(FieldInfo o1, FieldInfo o2) {
                    boolean o1IsPublic = Modifier.isPublic(o1.getModifiers());
                    boolean o2IsPublic = Modifier.isPublic(o2.getModifiers());
                    if (o1IsPublic == o2IsPublic) {
                        return o1.getSimpleName().compareTo(o2.getSimpleName());
                    } else if (o1IsPublic) {
                        return -1;
                    } else {
                        return 1;
                    }

                }
            });
        }
        notifyDataSetChanged();
    }

    @Override
    public @NonNull
    ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lens_object_field_item,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final FieldInfo info = infoList.get(position);
        String modifier = Modifier.toString(info.getModifiers());
        String type = info.getType() != null ? info.getReadableTypeName()
                : info.getValue().getClass().getSimpleName();
        String name = isCollection() ? "position: " + position : info.getSimpleName();
        String value = String.valueOf(info.getValue());
        SpannableString kvSpannable = new SpannableString(name + " = " + value);
        kvSpannable.setSpan(new ForegroundColorSpan(0xffd15600),
                0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        kvSpannable.setSpan(new ForegroundColorSpan(0xff7b7b7b), name.length() + 1,
                name.length() + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.fieldName.setText(kvSpannable);
        SpannableString classSpannable = new SpannableString(
                (modifier.length() > 0 ? modifier + " " : "") + type);
        if (modifier.length() > 0) {
            classSpannable.setSpan(new ForegroundColorSpan(0xff4b7c14), 0, modifier.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        if (info.getValue() instanceof String) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Object object = info.getValue();
                    if (object instanceof String) {
                        String data = (String) object;
                        if (data.toLowerCase().startsWith("http")) {
                            Context context = ApplicationLifecycle.getInstance().getCurrentActivity();
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(data));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);

                        }
                    }
                    return true;
                }
            });
        } else if (info.getValue() instanceof View) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //[jump to edit ]
                    jump2EditPanel(info);
                    return true;
                }
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }

        holder.fieldValue.setText(classSpannable);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info.getValue() == null) return;
                if (info.isEditable()) {
                    FieldEditPanel fieldEditPanel = new FieldEditPanel(null);
                    fieldEditPanel.setData(object, info);
                    fieldEditPanel.show();
                } else {
                    ObjectInfoPanel objectInfoPanel = new ObjectInfoPanel(null, info.getValue());
                    objectInfoPanel.show();
                }
            }
        });
        if (hasWatched(info, object)) {
            holder.addToWatchLst.setVisibility(View.GONE);
        } else {
            holder.addToWatchLst.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    watchObject(info, object);
                    notifyItemChanged(position);
                }
            });
            TouchDelegateUtils.expandHitRect(holder.addToWatchLst,
                    UIUtils.dp2px(holder.itemView.getContext(), 3));
            holder.addToWatchLst.setVisibility(View.VISIBLE);
        }
    }


    private void watchObject(FieldInfo info, Object object) {
        if (isCollection()) {
            DebugInfoConfig.getInstance().watchObject(info.getName(), info.getValue());
        } else {
            DebugInfoConfig.getInstance().watchField(info.getName(), object);
        }

    }

    private boolean hasWatched(FieldInfo info, Object object) {
        if (isCollection()) {
            return DebugInfoConfig.getInstance().hasWatched(info.getName(), info.getValue());
        } else {
            return DebugInfoConfig.getInstance().hasWatched(info.getName(), object);
        }
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    private boolean isCollection() {
        return fieldInfo.getValue().getClass().toString().startsWith("class [")
                || fieldInfo.getValue() instanceof Iterable || fieldInfo.getValue() instanceof Map;
    }

    private LinkedList<FieldInfo> filterItems(String keyword, List<FieldInfo> source) {
        LinkedList<FieldInfo> resultList = new LinkedList<>();
        String lowCaseKeyWord = keyword.toLowerCase();
        for (FieldInfo fieldInfo : source) {
            if ((fieldInfo.getType() != null && fieldInfo.getType().toString().toLowerCase().contains(lowCaseKeyWord))
                    || (fieldInfo.getValue() != null && fieldInfo.getValue().toString().toLowerCase().contains(lowCaseKeyWord))
                    || fieldInfo.getSimpleName().toLowerCase().contains(lowCaseKeyWord)) {
                resultList.add(fieldInfo);
            }
        }
        return resultList;
    }

    private void addVar(Object value, SparseArray array) {

        if (value != null) {
            FieldInfo var = ObjectFieldCollector.create(value, array, null);
            infoList.add(var);
        }

    }

    private void unboxInnerObject(FieldInfo fieldInfo, LinkedList<FieldInfo> fieldInfos, SparseArray array) {
        Object objValue = fieldInfo.getValue();
        String clzz = fieldInfo.getValue().getClass().toString();
        if (objValue instanceof Map) {

            Object value = fieldInfo.getValue();
            Map map = (Map) value;

            Set set = map.entrySet();
            for (Object o : set) {
                Map.Entry entry = (Map.Entry) o;
                addVar(entry, array);
            }

            return;
        }
        if (objValue instanceof Object[]) {
            for (Object obj : (Object[]) fieldInfo.getValue()) {
                addVar(obj, array);
            }
        } else if (clzz.startsWith("class [I")) {
            for (int obj : (int[]) fieldInfo.getValue()) {
                addVar(obj, array);
            }
        } else if (clzz.startsWith("class [Z")) {
            for (boolean obj : (boolean[]) fieldInfo.getValue()) {
                addVar(obj, array);
            }
        } else if (clzz.startsWith("class [B")) {
            for (byte obj : (byte[]) fieldInfo.getValue()) {
                addVar(obj, array);
            }
        } else if (clzz.startsWith("class [C")) {
            for (char obj : (char[]) fieldInfo.getValue()) {
                addVar(obj, array);
            }
        } else if (clzz.startsWith("class [S")) {
            for (short obj : (short[]) fieldInfo.getValue()) {
                addVar(obj, array);
            }
        } else if (clzz.startsWith("class [J")) {
            for (long obj : (long[]) fieldInfo.getValue()) {
                addVar(obj, array);
            }
        } else if (clzz.startsWith("class [F")) {
            for (float obj : (float[]) fieldInfo.getValue()) {
                addVar(obj, array);
            }
        } else if (clzz.startsWith("class [D")) {
            for (double obj : (double[]) fieldInfo.getValue()) {
                addVar(obj, array);
            }
        } else if (fieldInfo.getValue() instanceof Iterable) {
            for (Object obj : (Iterable) fieldInfo.getValue()) {
                addVar(obj, array);
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView fieldName;
        TextView fieldValue;
        View addToWatchLst;

        ViewHolder(View itemView) {
            super(itemView);
            fieldName = itemView.findViewById(R.id.fieldName);
            fieldValue = itemView.findViewById(R.id.fieldValue);
            addToWatchLst = itemView.findViewById(R.id.view_add_to_watch_list);
        }
    }

    private void jump2EditPanel(FieldInfo info) {
        FieldEditPanel fieldEditPanel = new FieldEditPanel(null);
        fieldEditPanel.setData(object, info);
        fieldEditPanel.show();
    }
}
