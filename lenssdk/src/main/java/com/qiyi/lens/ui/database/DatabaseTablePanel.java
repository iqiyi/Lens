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
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.GridLayoutManager;

import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.UIStateCallBack;
import com.qiyi.lens.ui.abtest.KeyValueSubPanelView;
import com.qiyi.lens.ui.abtest.Value;
import com.qiyi.lens.utils.SimpleTask;
import com.qiyi.lens.utils.UIUtils;
import com.qiyi.lenssdk.R;

import java.util.ArrayList;
import java.util.List;

public class DatabaseTablePanel extends FullScreenPanel implements UIStateCallBack,
        View.OnClickListener {

    public Context mContext;
    private DBRecyclerView mRecyclerView;
    private CommonDBAdapter mAdapter;
    private KeyValueSubPanelView subPanelView;
    private int mKey;
    // true: table's info; false: table's content
    private boolean mode;
    private String table;
    private String primaryKey;
    private GridItem clickedItem;

    public DatabaseTablePanel(FloatingPanel panel) {
        super(panel);
        mContext = context;
        setTitle(R.string.lens_common_title_bar_database);
    }

    @Override
    protected View onCreateView(ViewGroup viewGroup) {
        View content = inflateView(R.layout.lens_database_table_panel, viewGroup);
        LinearLayout container = content.findViewById(R.id.lens_tab_container);
        HorizontalScrollView scrollView = new HorizontalScrollView(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        scrollView.setFillViewport(true);
        mRecyclerView = new DBRecyclerView(getContext());
        mRecyclerView.setBackgroundColor(mContext.getResources().getColor(R.color.recycler_view_bg));
        scrollView.addView(mRecyclerView, params);
        container.addView(scrollView, params);

        return content;
    }

    @Override
    public void onViewCreated(View root) {
        super.onViewCreated(root);
        mAdapter = new CommonDBAdapter();
        mRecyclerView.addItemDecoration(new GridDividerDecoration.Builder()
                .setColor(mContext.getResources().getColor(R.color.lens_divider_light))
                .setThickness(UIUtils.dp2px(mContext, 1f))
                .build());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setListener(new CommonDBAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, BaseItem item) {
                if (item instanceof GridItem) {
                    if (mode) {
                        return;
                    }
                    if (!((GridItem) item).isEnable()) {
                        return;
                    }
                    clickedItem = (GridItem) item;
                    int type = Value.TYPE_STRING;
                    Value value = new Value(new String[]{clickedItem.data}, type, 0);
                    showSubPanel(clickedItem.primaryKeyValue, value);
                }
            }
        });
        mAdapter.setLongClickListener(new CommonDBAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(int position, BaseItem item) {
                if (item instanceof GridItem) {
                    // make sure that only the content can be response for menuEvent
                    return !((GridItem) item).isEnable();
                }
                return false;
            }
        });
        loadData(null);
        setTitle(table);
    }

    void initCondition(NameItem item, int key) {
        mKey = key;
        table = item.data;
        primaryKey = LensProvider.get().getDatabases().getPrimaryKey(mKey, table);
    }


    private void showSubPanel(String key, Value value) {
        if (subPanelView == null) {
//            ViewGroup group = (ViewGroup) findViewById(R.id.lens_ab_test_edit_sub_panel);
            subPanelView = new KeyValueSubPanelView((ViewGroup) getDecorView());
            subPanelView.setOnDismissCallback(new KeyValueSubPanelView.DismissCallback() {
                @Override
                public void onDismiss() {

                }
            });
        }
        subPanelView.showData(new Pair<String, Value>(key, value));
    }

    @Override
    public void onClick(View v) {

    }

    private void loadData(final String condition) {
        mAdapter.clearItems();
        showLoading();
        new SimpleTask<>(new SimpleTask.Callback<Void, DatabaseResult>() {
            @Override
            public DatabaseResult doInBackground(Void[] params) {
                if (mode) {
                    return LensProvider.get().getDatabases().getTableInfo(mKey, table);
                } else {
                    return LensProvider.get().getDatabases().query(mKey, table, condition);
                }
            }

            @Override
            public void onPostExecute(DatabaseResult result) {
                List<BaseItem> data = new ArrayList<>();
                if (result.sqlError == null) {
                    mRecyclerView.setLayoutManager(new GridLayoutManager(
                            getContext(), result.columnNames.size()));
                    int pkIndex = 0;
                    for (int i = 0; i < result.columnNames.size(); i++) {
                        data.add(new GridItem(result.columnNames.get(i), true));
                        if (TextUtils.equals(result.columnNames.get(i), primaryKey)) {
                            pkIndex = i;
                        }
                    }
                    for (int i = 0; i < result.values.size(); i++) {
                        for (int j = 0; j < result.values.get(i).size(); j++) {
                            GridItem item = new GridItem(result.values.get(i).get(j),
                                    result.values.get(i).get(pkIndex),
                                    result.columnNames.get(j));
                            if (!mode && pkIndex == j) {
                                item.setIsPrimaryKey();
                            }
                            data.add(item);
                        }
                    }
                    mAdapter.setItems(data);
                } else {
                    showToast(result.sqlError.message);
                }
                hideLoading();
            }
        }).execute();
    }
}
