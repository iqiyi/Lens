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
package com.qiyi.lens.ui.dns;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.widget.tableView.TableBuilder;
import com.qiyi.lens.ui.widget.tableView.TableView;
import com.qiyi.lens.utils.UIUtils;
import com.qiyi.lens.utils.configs.NetworkAnalyzeConfig;
import com.qiyi.lenssdk.R;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 将URL 以key value 的形式展示在表格中， 支持搜素高亮
 */
public class URLFormatPanel extends FullScreenPanel {
    private EditText editText;
    private String url;
    private String[] mData;
    private TableBuilder builder;
    private String mFilter;

    public URLFormatPanel(FloatingPanel panel, String url) {
        super(panel);
        this.url = url;
    }


    @Override
    public View onCreateView(ViewGroup group) {

        return inflateView(R.layout.lens_url_formap, group);
    }


    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);
        TableView tableView = (TableView) findViewById(R.id.url_formap_table);
        View searchBtn = findViewById(R.id.search_box_action_search);
        editText = (EditText) findViewById(R.id.search_box_edit_text);
        TextView infoView = (TextView) findViewById(R.id.url_formap_info);


        String filter = NetworkAnalyzeConfig.getInstance().getUrlKeyFilter();
        if (filter != null && filter.length() > 0) {
            editText.setText(filter);
            mFilter = filter;
        }


        final int padding = UIUtils.dp2px(getContext(), 5);
        final int maxWidth = UIUtils.dp2px(getContext(), 100);
        final int secodColWidth = UIUtils.getScreenWidth(getContext()) - maxWidth - UIUtils.dp2px(getContext(), 50);

        String[] data = url2KeyValue(url);
        if (data != null && data.length > 0) {
            this.mData = data;
            builder = TableBuilder.obtain();
//            tableView.setColumnStretchable(1,true);
            builder.setData(data)
                    .setStrokeWidth(2, 4)
                    .setColumnCountRowCount(2, 0)
                    .setTableView(tableView)
                    .setDataBinder(new TableBuilder.DefaultBinder(builder) {
                        @Override
                        public TextView createItemView(ViewGroup parent, int row, int column) {
                            TextView textView = super.createItemView(parent, row, column);
                            textView.setPadding(padding, 0, padding, 0);
                            TableRow.LayoutParams clp = new TableRow.LayoutParams(maxWidth, -2);
                            if (parent.getLayoutParams() == null) {
                                parent.setLayoutParams(new TableLayout.LayoutParams(-1, -2));
                            }

                            if (column == 0) {
                                textView.setGravity(Gravity.RIGHT);
                                clp.weight = 0;
                            } else {
                                clp.width = secodColWidth;
                                textView.setGravity(Gravity.LEFT);
                                textView.setMaxLines(3);
                            }
                            textView.setLayoutParams(clp);
                            return textView;
                        }
                    })
                    .build(getContext());


            searchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doSearch();
                }
            });


            findViewById(R.id.search_box_action_text).getLayoutParams().width = 0;
            findViewById(R.id.search_box_clear_search_text).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    builder.setAllColor(0);
                    editText.setText("");
                }
            });


            if (mFilter != null) {
                doSearch();
            }

        } else {
            infoView.setVisibility(View.VISIBLE);
            tableView.setVisibility(View.GONE);
            infoView.setText("URL 可能有问题");
        }


    }

    private String[] url2KeyValue(String url) {

        String[] array = null;
        try {
            URL ur = new URL(url);
            String qure = ur.getQuery();
            if (qure != null && qure.length() > 0) {
                String[] ar = qure.split("&");
                if (ar.length > 0) {
                    array = new String[ar.length * 2];
                    int i = 0;
                    for (String a : ar) {

                        int p = a.indexOf('=');
                        if (p > 0) {
                            array[i++] = a.substring(0, p);
                            if (p < a.length() - 1) {
                                array[i++] = a.substring(p + 1);
                            } else {
                                array[i++] = "";
                            }
                        } else {//[key]
                            array[i++] = a;
                            array[i++] = "";
                        }
                    }

                }

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return array;
    }

    private void doSearch() {
        String key = editText.getText().toString();
        if (key.length() == 0) {
            showToast("无效输入");
            return;

        }
        int p = 0;
        boolean found = false;
        for (String k : mData) {
            if (k.contains(key)) {

                highlight(p);
                found = true;
            }
            p++;
        }

        if (!found) {
            showToast("木有找到！！！");
        }

    }

    private void highlight(int index) {

//        builder.setAllColor(0);
        int row = index / 2;
        int col = index % 2;
        builder.setItemColor(row, col, Color.YELLOW);

    }


}
