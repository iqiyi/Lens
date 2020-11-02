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
package com.qiyi.lens.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qiyi.lens.ui.flowlayout.LensTagFlowLayout;
import com.qiyi.lens.ui.flowlayout.SearchFilterTagAdapter;
import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.KeyLog;
import com.qiyi.lens.utils.configs.DisplayConfiguration;
import com.qiyi.lens.utils.event.DataCallBack;
import com.qiyi.lens.utils.event.EventBus;
import com.qiyi.lenssdk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志详情况展示页面。 需要优化
 */
public class LogInfoDetailPanel extends FullScreenPanel implements DataCallBack {

    private EditText textSearchFilter;
    private KeyLog keyLog;
    private List<String> filterWords = new ArrayList<String>();
    private SearchFilterTagAdapter filterAdapter;
    private LogContentAdapter stringArrayAdapter;
    private int duration;


    public LogInfoDetailPanel(FloatingPanel panel) {
        super(panel);
        keyLog = KeyLog.getKeyLogInstance();
        duration = DisplayConfiguration.obtain().getRefreshDuration();
    }

    @Override
    public View onCreateView(ViewGroup group) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.lens_log_info_detail_panel_layout, group, false);
        TextView textViewClose = v.findViewById(R.id.text_close);
        textSearchFilter = (EditText) v.findViewById(R.id.edit_search_filter);
        TextView textClear = (TextView) v.findViewById(R.id.clear_log);
        ImageView textAddSearchFilter = (ImageView) v.findViewById(R.id.search_filter_add);
        LensTagFlowLayout searchGridItems = (LensTagFlowLayout) v.findViewById(R.id.search_filter_grid);
        TextView textSave = (TextView) v.findViewById(R.id.save_log);
        filterAdapter = new SearchFilterTagAdapter(filterWords, getContext());
        searchGridItems.setAdapter(filterAdapter);
        String[] array = KeyLog.getKeyLogInstance().getFilters();
        if (array != null && array.length > 0) {
            for (String s : array) {
                if (s != null && !s.isEmpty()) {
                    filterWords.add(s);
                }
            }
        }
        filterAdapter.refreshTagData(filterWords);
        RecyclerView listLog = (RecyclerView) v.findViewById(R.id.log_text);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        manager.setStackFromEnd(true);
        stringArrayAdapter = new LogContentAdapter(getContext());
        stringArrayAdapter.setData(keyLog.getLogArray());
        listLog.setLayoutManager(manager);
        listLog.setAdapter(stringArrayAdapter);
        textViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        textAddSearchFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = textSearchFilter.getText().toString();
                if (!s.isEmpty()) {
                    for (String filter : filterWords) {
                        if (filter.equals(s)) {
                            textSearchFilter.setText("");
                            textSearchFilter.clearFocus();
                            showToast("无需重复添加");
                            return;
                        }
                    }
                    filterWords.add(s);
                }
                textSearchFilter.setText("");
                textSearchFilter.clearFocus();
                filterAdapter.refreshTagData(filterWords);
                KeyLog.resetLog(filterWords.toArray(new String[filterWords.size()]));
            }
        });
        textSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyLog.flushAllLogsToFile();
                showToast("已保存到缓存目录：" + getContext().getCacheDir().toString() + "/");
            }
        });
        textClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyLog.clearLog();
                stringArrayAdapter.notifyDataSetChanged();
            }
        });
        return v;
    }

    @Override
    public void show() {
        super.show();
        KeyLog.resetLog(filterWords.toArray(new String[filterWords.size()]));
    }

    public long time;

    @Override
    public void onShow() {
        super.onShow();
        EventBus.registerEvent(this, DataPool.EVENT_DISPLAY_DATA_ARRIVED);
    }

    @Override
    public void onDismiss() {
        super.onDismiss();
        EventBus.unRegisterEvent(this, DataPool.EVENT_DISPLAY_DATA_ARRIVED);
    }

    private int dataRefreshCount;

    @Override
    public void onDataArrived(Object data, int type) {
        getMainHandler().postDelayed(display, duration);
    }

    public static class LogContentAdapter extends RecyclerView.Adapter<LogContentAdapter.ViewHolder> {

        KeyLog.LogArray logArray;
        Context context;

        public LogContentAdapter(Context context) {
            this.context = context;
        }

        public void setData(KeyLog.LogArray filterWords) {
            this.logArray = filterWords;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.lens_search_filter_word, parent, false));
        }


        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            int count = getItemCount() - 1;
            holder.textView.setText(logArray.getItem(count - position));
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return logArray != null ? logArray.size() : 0;
        }


        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;

            ViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView
                        .findViewById(R.id.text_search_item);
            }
        }
    }


    private Runnable display = new Runnable() {
        @Override
        public void run() {
            getMainHandler().removeCallbacks(display);
            stringArrayAdapter.setData(KeyLog.getKeyLogInstance().getLogArray());
            stringArrayAdapter.notifyDataSetChanged();
        }
    };

}
