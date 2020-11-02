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

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.qiyi.lens.utils.LensReflectionTool;

public class AdapterInfo {
    public int adapterIndex;
    public Object adapter;
    public int childIndex;
    private View view;

    public AdapterInfo(Object value, boolean isAdapterIndexEnabled) {
        this.view = (View) value;
        this.figureOutIndex((View) value, isAdapterIndexEnabled);
    }

    private void makeChildIndex(View directView) {
        if (directView == this.view) {
            this.childIndex = 0;
        } else if (directView instanceof ViewGroup) {
            View view;
            for (view = this.view; view.getParent() != directView && view.getParent() instanceof View; view = (View) view.getParent()) {
                // keep empty
            }
            if (view.getParent() == directView) {
                ViewGroup group = (ViewGroup) directView;
                this.childIndex = group.indexOfChild(view);
            }
        } else {
            this.childIndex = -1;
        }

    }

    private void makeAdapterIndex(ViewGroup parent, View value) {
        this.makeChildIndex(value);
        int position;
        if (parent instanceof AbsListView) {
            AbsListView listView = (AbsListView) parent;
            ListAdapter listAdapter = (ListAdapter) listView.getAdapter();
            this.adapter = listAdapter;
            if (listAdapter != null) {
                position = listView.getPositionForView(value);
                this.adapterIndex = position;
            }
        } else if (parent instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) parent;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            this.adapter = adapter;
            if (adapter != null) {
                position = recyclerView.getChildAdapterPosition(value);
                this.adapterIndex = position;
            }
        } else if (parent instanceof ViewPager) {
            ViewPager pager = (ViewPager) parent;
            PagerAdapter adapter = pager.getAdapter();
            this.adapter = adapter;
            if (adapter != null) {
                position = pager.getCurrentItem();
                this.adapterIndex = position;
            }

        }

    }

    public Object getAdapterItem() {
        if (this.adapter != null) {
            Object data = null;
            if (this.adapter instanceof ListAdapter) {
                ListAdapter listAdapter = (ListAdapter) this.adapter;
                data = listAdapter.getItem(this.adapterIndex);
                if (data != null) {
                    return data;
                }
            }
            // return ReflectTool.callMethod(this.adapter, "getItemAt", new Object[]{this.adapterIndex});
            return LensReflectionTool.get().on(adapter)
                    .methodSignature("getItemAt", int.class)
                    .call(this.adapterIndex);
        }
        return null;
    }

    private boolean isAdapterView(Object view) {
        return view instanceof AdapterView || view instanceof RecyclerView || view instanceof ViewPager;
    }

    private void figureOutIndex(View view, boolean isAdapterIndexEnabled) {
        if (view.getParent() instanceof View) {
            if (isAdapterIndexEnabled) {
                ViewParent parent = view.getParent();

                Object current;
                for (current = view; parent != null && !this.isAdapterView(parent); parent = parent.getParent()) {
                    current = parent;
                }

                if (parent instanceof ViewGroup) {
                    this.makeAdapterIndex((ViewGroup) parent, (View) current);
                }
            } else {
                View parent = (View) view.getParent();
                if (this.isAdapterView(parent)) {
                    this.makeAdapterIndex((ViewGroup) parent, view);
                }
            }
        }

    }
}
