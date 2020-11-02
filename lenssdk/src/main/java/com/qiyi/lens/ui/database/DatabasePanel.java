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

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.UIStateCallBack;
import com.qiyi.lens.utils.SimpleTask;
import com.qiyi.lenssdk.R;

import java.util.ArrayList;
import java.util.List;

public class DatabasePanel extends FullScreenPanel implements UIStateCallBack, View.OnClickListener {

    private Context mContext;
    private DBRecyclerView mRecyclerView;
    private CommonDBAdapter mAdapter;
    private DatabaseTableListPanel tablePanel;

    private boolean isBase = true;

    public DatabasePanel(FloatingPanel panel) {
        super(panel);
        mContext = context;
        setTitle(R.string.lens_common_title_bar_database);
    }

    @Override
    protected View onCreateView(ViewGroup viewGroup) {
        View content = inflateView(R.layout.lens_database_panel, viewGroup);
        mRecyclerView = content.findViewById(R.id.len_database_recycler_view);
        installRecyclerView(mRecyclerView);
        return content;
    }

    void setBase(boolean base) {
        isBase = base;
    }


    public CommonDBAdapter getAdapter() {
        return mAdapter;
    }

    private void installRecyclerView(RecyclerView recyclerView) {
        recyclerView.setBackgroundColor(mContext.getResources().getColor(R.color.recycler_view_bg));
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        DividerItemDecoration divider = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        GradientDrawable horizontalDrawable = new GradientDrawable();
        horizontalDrawable.setColor(0xffE5E5E5);
        horizontalDrawable.setSize(0, 1);
        divider.setDrawable(horizontalDrawable);
        recyclerView.addItemDecoration(divider);
        mAdapter = new CommonDBAdapter();
        mAdapter.setListener(new CommonDBAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, BaseItem item) {
                if (tablePanel == null) {
                    tablePanel = new DatabaseTableListPanel(getFloatingPanel());
                }
                tablePanel.show();
                tablePanel.refreshTables(item);
            }
        });
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onViewCreated(View root) {
        super.onViewCreated(root);
        if (isBase) { //子类中设置为false 避免loadData()覆盖子类的数据
            loadData();
        }
    }

    private void loadData() {
        showLoading();
        new SimpleTask<>(new SimpleTask.Callback<Void, List<BaseItem>>() {
            @Override
            public List<BaseItem> doInBackground(Void[] params) {
                SparseArray<String> databaseNames = LensProvider.get().getDatabases().getDatabaseNames();
                List<BaseItem> data = new ArrayList<>(databaseNames.size());
                for (int i = 0; i < databaseNames.size(); i++) {
                    data.add(new DBItem(databaseNames.valueAt(i), databaseNames.keyAt(i)));
                }
                return data;
            }

            @Override
            public void onPostExecute(List<BaseItem> result) {
                hideLoading();
                mAdapter.setItems(result);
            }
        }).execute();
    }

    @Override
    public void onClick(View v) {

    }

}
