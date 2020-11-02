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

import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.qiyi.lens.ui.viewinfo.CurrentViewInfoPanel;
import com.qiyi.lens.utils.WatchObjInfo;
import com.qiyi.lens.utils.configs.DebugInfoConfig;
import com.qiyi.lens.utils.iface.AbsObjectDescriptor;
import com.qiyi.lens.utils.reflect.FieldInfo;
import com.qiyi.lens.utils.reflect.ObjectFieldCollector;
import com.qiyi.lenssdk.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;

public class WatchObjectListAdapter extends RecyclerView.Adapter<WatchObjectListAdapter.ViewHolder> {

    private WatchObjInfo info;
    private SparseArray sparseArray = new SparseArray();
    private List<Pair<?, WeakReference>> watchList;

    public WatchObjectListAdapter(WatchObjInfo info) {
        this.info = info;
        watchList = DebugInfoConfig.getInstance().getWatchList();
    }


    @Override
    public @NonNull
    ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lens_item_watch_obj,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    /**
     * first has two types:
     *  String & Field
     */
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Pair<?, WeakReference> entry = watchList.get(position);
        if (entry.second == null) return;
        Object realObj = null;
        //[key setting]
        Object object = entry.second.get();
        if (entry.first instanceof Field) {
            if (object != null) {
                FieldInfo fieldInfo = ObjectFieldCollector.create((Field) entry.first, object,
                        new SparseArray(), null);
                holder.tvFieldName.setText(object.getClass().getSimpleName() + "." + fieldInfo.getName());
                realObj = fieldInfo.getValue();
            }
        } else { // for String type
            if (TextUtils.isEmpty((String) entry.first) && object != null) {
                holder.tvFieldName.setText(object.getClass().getSimpleName());
            } else {
                holder.tvFieldName.setText((String) entry.first);
            }
            realObj = object;
        }

        if (realObj != null) { //[value setting]
            String desc = AbsObjectDescriptor.getDescription(realObj);
            if (desc != null) {
                holder.tvFieldValue.setText(desc);
            }


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Object value = getValueAt(holder.getAdapterPosition());

                    if (value != null && View.class.isAssignableFrom(value.getClass())) {
                        showViewPanel(value);
                    } else {
                        showObjeFeilPanle(value);
                    }

                }
            });

        } else {
            holder.tvFieldValue.setText("*对象不可达*");
            holder.itemView.setOnClickListener(null);
        }

        holder.removeWatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DebugInfoConfig.getInstance().unwatch(entry);
                watchList = DebugInfoConfig.getInstance().getWatchList();
                notifyItemRemoved(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return watchList == null ? 0 : watchList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFieldName;
        TextView tvFieldValue;
        View removeWatch;

        ViewHolder(View itemView) {
            super(itemView);
            tvFieldName = itemView.findViewById(R.id.tv_field_name);
            tvFieldValue = itemView.findViewById(R.id.tv_field_value);
            removeWatch = itemView.findViewById(R.id.view_remove);
        }

    }

    //[return real value]
    private Object getValueAt(int position) {

        if (watchList.size() > position) {
            Pair<?, WeakReference> entry = watchList.get(position);

            if (entry.first instanceof Field) {
                if (entry.second != null && entry.second.get() != null) {
                    FieldInfo fieldInfo = ObjectFieldCollector.create((Field) entry.first, entry.second.get(),
                            sparseArray, null);
                    return fieldInfo.getValue();
                }
            } else if (entry.second != null && entry.second.get() instanceof AbsObjectDescriptor) {
                return ((AbsObjectDescriptor) entry.second.get()).getObject();
            } else if (entry.second != null) {
                return entry.second.get();
            }
        }
        return null;
    }


    private void showViewPanel(Object value) {
        CurrentViewInfoPanel currentViewInfoPanel =
                new CurrentViewInfoPanel(info.getPanel());
        currentViewInfoPanel.setDataView((View) value, false);
        currentViewInfoPanel.show();
    }

    //[只有非基本类型才跳转到详情页面。]
    private void showObjeFeilPanle(Object value) {
        if (!FieldInfo.isBasicType(value)) {
            ObjectInfoPanel objectInfoPanel = new ObjectInfoPanel(info.getPanel(), value);
            objectInfoPanel.show();
        }
    }


    public void onDataChange() {
        watchList = DebugInfoConfig.getInstance().getWatchList();
        notifyDataSetChanged();
    }

}
