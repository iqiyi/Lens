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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qiyi.lens.obj.SPItem;
import com.qiyi.lens.ui.abtest.KeyValueSubPanelView;
import com.qiyi.lens.ui.abtest.Value;
import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.SharedPreferencesHelper;
import com.qiyi.lens.utils.SimpleTask;
import com.qiyi.lens.utils.UIUtils;
import com.qiyi.lens.utils.event.DataCallBack;
import com.qiyi.lens.utils.event.EventBus;
import com.qiyi.lens.utils.event.SharedPrefChangedEvent;
import com.qiyi.lenssdk.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * @author Water
 * 2018-11-14
 */
public class SharedPreferencePanel extends FullScreenPanel implements UIStateCallBack,
        DataCallBack, View.OnClickListener {

    private static final String NO_RESULT = "no_result";
    private SearchResultAdapter mAdapter;
    private String mKey;
    private ClipboardManager myClipboard;
    private SharedPreferencesHelper mSharedPreferencesHelper;
    private KeyValueSubPanelView subPanelView;

    public SharedPreferencePanel(FloatingPanel panel) {
        super(panel);
        setTitle(R.string.lens_SharedPreference);
    }

    private List<String> spFiles = new ArrayList<>(); //使用前须清除 spFiles.clear()

    @Override
    protected View onCreateView(ViewGroup viewGroup) {
        final View content = inflateView(R.layout.lens_sharedpreference_panel, viewGroup);
//        content.findViewById(R.id.len_title_bar_back).setOnClickListener(this);
//        TextView title = content.findViewById(R.id.len_title_bar_title);
//        title.setText(context.getString(R.string.lens_SharedPreference));


        EditText mSearchKey = content.findViewById(R.id.lens_search_sp);
        mSearchKey.setOnClickListener(this);
        mSearchKey.addTextChangedListener(mSearchTextWatcher);
        mSearchKey.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mSearchKey.setImeOptions(EditorInfo.IME_ACTION_DONE);
        content.findViewById(R.id.lens_right_search_icon).setOnClickListener(this);
        ListView mListView = content.findViewById(R.id.lv_result);
        mAdapter = new SearchResultAdapter(context);
        mListView.setAdapter(mAdapter);
        mListView.setDividerHeight(UIUtils.dp2px(context, 5));
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < mAdapter.getCount()) {
                    //copy(result.get(keys[position]));
                    SPItem item = mAdapter.getItem(position);
                    if (item != null) {
                        copy(item.value);
                    }
                }
                return false;
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position >= 0 && position < mAdapter.getCount()) {
                    SPItem item = mAdapter.getItem(position);
                    if (item != null) {
                        Value value = parse(item);
                        showSubPanel(item.key, value);
                    }
                }


            }
        });
        mSharedPreferencesHelper = SharedPreferencesHelper.getInstance(context);
        EventBus.registerEvent(this, DataPool.EVENT_EDIT_SHARED_PREFERENCES);
        return content;
    }

    private void copy(String data) {
        if (myClipboard == null) {
            myClipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
        }
        ClipData myClip = ClipData.newPlainText("text", data);
        myClipboard.setPrimaryClip(myClip);
        showToast(context.getString(R.string.lens_search_sp_result_copy));
    }

    @Override
    protected void onDismiss() {
        super.onDismiss();
        EventBus.unRegisterEvent(this, DataPool.EVENT_EDIT_SHARED_PREFERENCES);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.len_title_bar_back) {
            dismiss();
        }
    }

    private TextWatcher mSearchTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null && !"".equals(s.toString()) && mAdapter != null) {
                mKey = s.toString();
                doSearch(mKey);
            } else if (mAdapter != null) {
                mAdapter.setData(null);
            }
        }
    };

    private void doSearch(final String key) {
        if (mSharedPreferencesHelper == null) {
            mSharedPreferencesHelper = SharedPreferencesHelper.getInstance(context);
        }
        showLoading();
        new SimpleTask<>(new SimpleTask.Callback<Void, List<SPItem>>() {
            @Override
            public List<SPItem> doInBackground(Void[] params) {
                return mSharedPreferencesHelper.getSharedPrefs(context);
            }

            @Override
            public void onPostExecute(List<SPItem> items) {
                List<SPItem> result = fuzzySearch(items, key.toLowerCase());
                mAdapter.setData(result);
                hideLoading();
            }
        }).execute();
    }

    /**
     * 模糊查询
     */
    public List<SPItem> fuzzySearch(List<SPItem> items, String key) {
        if (items != null) {
            List<SPItem> result = new ArrayList<>();
            for (SPItem item : items) {
                if (item == null) {
                    continue;
                }
                if (item.key.toLowerCase().contains(key)) {
                    result.add(item);
                }
            }
            return result;
        } else {
            return null;
        }
    }

    @Override
    public void onDataArrived(Object data, int type) {
        if (data instanceof SharedPrefChangedEvent) {
            final SharedPrefChangedEvent spEvent = (SharedPrefChangedEvent) data;
            new SimpleTask<>(new SimpleTask.Callback<Void, String>() {

                @Override
                public String doInBackground(Void[] params) {
                    return mSharedPreferencesHelper.updateSharedPref(spEvent.item.fileName,
                            spEvent.item.key, spEvent.item.newValue);
                }

                @Override
                public void onPostExecute(String result) {
                    if (TextUtils.isEmpty(result)) {
                        showToast(context.getString(R.string.lens_edit_toast_save_success));
                    } else {
                        showToast(result);
                    }
                    loadData();
                }
            }).execute();
        }
    }

    private void loadData() {
        doSearch(mKey);
    }

    @Override
    public void showToast(String data) {
        Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
    }

    private class SearchResultAdapter extends BaseAdapter {

        private Context context;

        private List<SPItem> searchResult;

        public SearchResultAdapter(Context context) {
            this.context = context;
        }

        public void setData(List<SPItem> data) {
            searchResult = data;
            notifyDataSetChanged();
        }

        public int getCount() {
            return searchResult == null ? 0 : searchResult.size();
        }

        @Override
        public SPItem getItem(int position) {
            return searchResult == null ? null : searchResult.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.lens_sharedpreference_panel_list_item, parent, false
                );
                viewHolder.resultTv = convertView.findViewById(R.id.lens_search_result);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (searchResult == null || searchResult.get(position) == null) {
                return convertView;
            }

            SPItem spItem = searchResult.get(position);
            viewHolder.resultTv.setText(highLightKey(spItem, position));

            return convertView;
        }

        private SpannableString highLightKey(SPItem item, int position) {
            SpannableString spanStr = new SpannableString(
                    SharedPreferencePanel.this.context.getString(R.string.lens_search_sp_result_key_value,
                            item.key,
                            item.value));
            Pattern p = Pattern.compile(mKey);
            Matcher m = p.matcher(spanStr);
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                spanStr.setSpan(new ForegroundColorSpan(Color.RED), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return spanStr;
        }
    }

    private static class ViewHolder {
        TextView resultTv;
    }

    private void showSubPanel(String key, Value value) {
        if (subPanelView == null) {
//            ViewGroup group = (ViewGroup) findViewById(R.id.lens_ab_test_edit_sub_panel);
            subPanelView = new KeyValueSubPanelView((ViewGroup) getDecorView());
            subPanelView.setOnDismissCallback(new KeyValueSubPanelView.DismissCallback() {
                @Override
                public void onDismiss() {
                    if (mKey != null && mKey.length() > 0) {
                        doSearch(mKey);
                    }
                }
            });
        }
        subPanelView.showData(new Pair<String, Value>(key, value));
    }


    private Value parse(SPItem item) {

        String value = item.value;
        int type;
        Value var;

        if (value == null || value.length() == 0) {
            type = Value.TYPE_STRING;
            var = new Value(new String[]{}, type, 0);
        } else if ("true".equals(value)) {
            type = Value.TYPE_BOOLEAN;
            var = new Value(new boolean[]{false, true}, type, 2);
            var.setIndex(1);

        } else if ("false".equals(value)) {
            type = Value.TYPE_BOOLEAN;
            var = new Value(new boolean[]{false, true}, type, 2);

        } else {
            try {

                int p = Integer.parseInt(value);
                var = new Value(new int[]{p}, Value.TYPE_INT, 0);

            } catch (Exception e) {
                type = Value.TYPE_STRING;
                var = new Value(new String[]{value}, type, 0);
            }

        }

        var.setSPName(item.fileName);

        return var;
    }
}
