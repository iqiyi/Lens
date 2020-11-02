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
package com.qiyi.lens.ui.traceview.compare;

import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.traceview.TimeGap;
import com.qiyi.lens.utils.SimpleTextWatcher;
import com.qiyi.lens.utils.TimeStampUtil;
import com.qiyi.lenssdk.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class LaunchTimeComparePanel extends FullScreenPanel implements View.OnClickListener {
    private List<TimeGap> leftRaw;
    private List<TimeGap> rightRaw;
    private List<GapGroup> left;
    private List<GapGroup> right;
    private List<String> leftTags = new ArrayList<>();
    private List<String> rightTags = new ArrayList<>();
    private View diffOnly;
    private View sameOnly;
    private EditText searchInput;
    private LaunchCompareAdapter adapter;

    public LaunchTimeComparePanel(TimeStampUtil l, TimeStampUtil r) {
        super(null);
        List<TimeGap> tl = l.buidStampInfo().getGaps();
        List<TimeGap> tr = r.buidStampInfo().getGaps();
        // sort by time
        Collections.sort(tl, new Comparator<TimeGap>() {
            @Override
            public int compare(TimeGap o1, TimeGap o2) {
                return (int) (o1.startTime - o2.startTime);
            }
        });
        tr = tr == null ? Collections.<TimeGap>emptyList() : tr;
        leftRaw = tl;
        rightRaw = tr;
        preProcess(tl, tr);
    }

    private void preProcess(List<TimeGap> l, List<TimeGap> r) {
        // 去除 tag 的 @xxx，避免对象地址造成的干扰
        preProcessTag(l);
        preProcessTag(r);
        // 按照第一次出现的位置排序，同名 tag 聚集
        List<GapGroup> left = groupSameSort(l);
        for (GapGroup gapGroup : left) {
            leftTags.add(gapGroup.getTag());
        }
        List<GapGroup> right = groupSameSort(r);
        final List<GapGroup> waitInsert = new ArrayList<>();
        for (GapGroup gapGroup : right) {
            if (!leftTags.contains(gapGroup.getTag())) {
                waitInsert.add(gapGroup);
            }
        }
        right.removeAll(waitInsert);
        Collections.sort(waitInsert, new Comparator<GapGroup>() {
            @Override
            public int compare(GapGroup o1, GapGroup o2) {
                return (int) (o1.getTimeStamp() - o2.getTimeStamp());
            }
        });
        // b 按照 a 的顺序排序
        Collections.sort(right, new Comparator<GapGroup>() {
            @Override
            public int compare(GapGroup o1, GapGroup o2) {
                int i1 = leftTags.indexOf(o1.getTag());
                int i2 = leftTags.indexOf(o2.getTag());
                return i1 - i2;
            }
        });

        for (GapGroup gapGroup : right) {
            rightTags.add(gapGroup.getTag());
        }
        // 插入 null
        List<GapGroup> withNullLeft = new ArrayList<>();
        List<GapGroup> withNullRight = new ArrayList<>();
        int i = 0;
        int j = 0;
        int k = 0;
        GapGroup _w = k < waitInsert.size() ? waitInsert.get(k) : null;
        while (i < left.size()) { // right 是 left 子集
            GapGroup _l = left.get(i);
            GapGroup _r = j < right.size() ? right.get(j) : null;
            while (_w != null && _l.getTimeStamp() > _w.getTimeStamp()) {
                k++;
                withNullLeft.add(null);
                withNullRight.add(_w);
                _w = k < waitInsert.size() ? waitInsert.get(k) : null;
            }
            if (_r != null) {
                if (_l.isSameTag(_r)) {
                    withNullLeft.add(_l);
                    withNullRight.add(_r);
                    i++;
                    j++;
                } else {
                    withNullLeft.add(_l);
                    withNullRight.add(null);
                    i++;
                }
            } else if (_l != null) {
                withNullLeft.add(_l);
                withNullRight.add(null);
                i++;
            }
        }
        this.left = withNullLeft;
        this.right = withNullRight;
    }

    private List<GapGroup> groupSameSort(List<TimeGap> list) {
        List<GapGroup> group = new ArrayList<>();
        List<String> groupTags = new ArrayList<>();
        for (TimeGap timeGap : list) {
            if (groupTags.contains(timeGap.tag)) {
                continue;
            }
            groupTags.add(timeGap.tag);
            group.add(new GapGroup(timeGap.tag, countTags(list, timeGap.tag), timeGap.timeStamp));
        }
        return group;
    }

    private int countTags(List<TimeGap> list, String tag) {
        int result = 0;
        for (TimeGap timeGap : list) {
            if (timeGap.tag.equals(tag)) {
                result++;
            }
        }
        return result;
    }

    private void preProcessTag(List<TimeGap> ta) {
        for (TimeGap timeGap : ta) {
            if (timeGap.tag.contains("@")) {
                timeGap.tag = timeGap.tag.substring(0, timeGap.tag.indexOf("@"));
            }
        }
    }

    @Override
    protected View onCreateView(final ViewGroup viewGroup) {
        View root = inflateView(R.layout.lens_compare_panel_layout, viewGroup);
        adapter = new LaunchCompareAdapter(context, leftRaw, rightRaw);
        ListView listView = root.findViewById(R.id.lens_list_view);
        listView.setAdapter(adapter);
        adapter.setData(left, right);
        adapter.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.toggleExpand(position);
                adapter.notifyDataSetChanged();
            }

        });

        searchInput = root.findViewById(R.id.lens_search_box_edit_text);
        diffOnly = root.findViewById(R.id.lens_diff_only);
        sameOnly = root.findViewById(R.id.lens_same_only);
        diffOnly.setOnClickListener(this);
        sameOnly.setOnClickListener(this);
        setupRealTimeSearch();
        return root;
    }

    private void setupRealTimeSearch() {
        final Runnable realTimeSearch = new Runnable() {
            @Override
            public void run() {
                handleOnly(sameOnly.isSelected(), diffOnly.isSelected(), searchInput.getText().toString());
            }
        };
        searchInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                searchInput.removeCallbacks(realTimeSearch);
                searchInput.postDelayed(realTimeSearch, 500);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == diffOnly || v == sameOnly) {
            if (v == diffOnly) sameOnly.setSelected(false);
            if (v == sameOnly) diffOnly.setSelected(false);
            v.setSelected(!v.isSelected());
            handleOnly(sameOnly.isSelected(), diffOnly.isSelected(), searchInput.getText().toString());
        }
    }

    private void handleOnly(final boolean sameOnly, final boolean diffOnly, final String keyword) {
        Filter filter = null;
        if (sameOnly || diffOnly || !TextUtils.isEmpty(keyword)) {
            filter = new Filter() {
                @Override
                public boolean reserve(GapGroup left, GapGroup right) {
                    String tag = left != null ? left.getTag() : right.getTag();
                    boolean matchKeyword = tag.toLowerCase().contains(keyword.toLowerCase());
                    if (matchKeyword && sameOnly) {
                        return left != null && right != null && left.getCount() == right.getCount();
                    } else if (matchKeyword && diffOnly) {
                        return left == null || right == null || left.getCount() != right.getCount();
                    }
                    return matchKeyword;
                }
            };
        }
        handleFilter(filter);
    }

    private void handleFilter(Filter filter) {
        if (filter == null) {
            adapter.setData(left, right);
        } else {
            List<GapGroup> nLeft = new LinkedList<>();
            List<GapGroup> nRight = new LinkedList<>();
            for (int i = 0; i < left.size(); i++) {
                if (filter.reserve(left.get(i), right.get(i))) {
                    nLeft.add(left.get(i));
                    nRight.add(right.get(i));
                }
            }
            adapter.setData(nLeft, nRight);
        }
        adapter.notifyDataSetChanged();
    }

    private interface Filter {
        boolean reserve(GapGroup left, GapGroup right);
    }
}
