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
package com.qiyi.lens.ui.database;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.recyclerview.widget.RecyclerView;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiyi.lenssdk.R;

import java.util.ArrayList;
import java.util.List;

public class CommonDBAdapter extends RecyclerView.Adapter<CommonDBAdapter.ViewPool>
        implements View.OnClickListener, View.OnLongClickListener {

    private List<BaseItem> data = new ArrayList<>();
    private OnItemClickListener listener;
    private OnItemLongClickListener longListener;

    public void setItems(List<? extends BaseItem> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    public void insertItems(List<? extends BaseItem> items, int index) {
        data.addAll(index, items);
        notifyDataSetChanged();
    }

    public void insertItem(BaseItem items, int index) {
        data.add(index, items);
        notifyDataSetChanged();
    }

    public void insertItem(BaseItem items) {
        data.add(items);
        notifyDataSetChanged();
    }

    public void removeItem(int index) {
        data.remove(index);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, getItemCount() - index);
    }

    public List<BaseItem> getItems() {
        return data;
    }

    public <T extends BaseItem> T getItem(int position) {
        return (T) data.get(position);
    }

    public void clearItems() {
        data.clear();
        notifyDataSetChanged();
    }

    public void performClick(int position) {
        if (listener != null) {
            listener.onItemClick(position, data.get(position));
        }
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
    }

    @Override
    public ViewPool onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent,
                false);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return new ViewPool(view);
    }

    @Override
    public void onBindViewHolder(ViewPool holder, int position) {
        holder.itemView.setTag(R.id.db_recycler_adapter_id, position);
        data.get(position).onBinding(position, holder, data.get(position).data);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).getLayout();
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            Object var =  v.getTag(R.id.db_recycler_adapter_id);
            if(var != null) {
                int position = (int) var;
                listener.onItemClick(position, data.get(position));
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (longListener != null) {
            Object var = v.getTag(R.id.db_recycler_adapter_id);
            if(var != null) {
                int position = (int) var;
                return longListener.onItemLongClick(position, data.get(position));
            }
        }
        return false;
    }

    public static final class ViewPool extends RecyclerView.ViewHolder {

        private final SparseArray<View> views;

        public ViewPool(View itemView) {
            super(itemView);
            views = new SparseArray<>();
        }

        public <T extends View> T getView(@IdRes int id) {
            if (id == View.NO_ID) {
                throw new RuntimeException("id is invalid");
            }
            View view = views.get(id);
            if (view == null) {
                view = itemView.findViewById(id);
                views.put(id, view);
            }
            return (T) view;
        }

        public ViewPool setText(@IdRes int id, String text) {
            TextView tv = getView(id);
            tv.setText(text);
            return this;
        }

        public ViewPool setCompoundDrawableLeft(@IdRes int id, @DrawableRes int left) {
            TextView tv = getView(id);
            tv.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
            return this;
        }

        public ViewPool setTextColor(@IdRes int id, @ColorInt int color) {
            TextView tv = getView(id);
            tv.setTextColor(color);
            return this;
        }

        public ViewPool setImageResource(@IdRes int id, @DrawableRes int resId) {
            ImageView tv = getView(id);
            tv.setImageResource(resId);
            return this;
        }

        public ViewPool setTextGravity(@IdRes int id, int gravity) {
            TextView tv = getView(id);
            tv.setGravity(gravity);
            return this;
        }

        public ViewPool setVisibility(@IdRes int id, int visibility) {
            View v = getView(id);
            v.setVisibility(visibility);
            return this;
        }

        public ViewPool setBackgroundColor(@IdRes int id, @ColorInt int color) {
            View v = getView(id);
            v.setBackgroundColor(color);
            return this;
        }

    }

    public interface OnItemClickListener {
        void onItemClick(int position, BaseItem item);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(int position, BaseItem item);
    }
}
