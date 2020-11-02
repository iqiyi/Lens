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
package com.qiyi.lens.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.qiyi.lens.demo.test.fragment.FragmentAC;

import org.qiyi.basecore.taskmanager.TickTask;

import java.io.IOException;
import java.util.LinkedList;

import okhttp3.OkHttpClient;
import okhttp3.Request;


public class MainActivity extends Activity implements AbsListView.OnScrollListener {

    private static final String TAG = "MainActivity";
    int minHeight;
    Handler workHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 获取 sdcard 权限，可以在 debug 模式下读取 sdcard lens 插件
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 110);
        }
        super.onCreate(savedInstanceState);
        minHeight = (int) (this.getResources().getDisplayMetrics().density * 50);
        startWorker();
        setContentView(showListView());
        requet();
    }


    private void startWorker() {
        HandlerThread thread = new HandlerThread("work thread");
        thread.start();
        workHandler = new Handler(thread.getLooper());
    }


    private View showListView() {

        ListView listView = new NListView(this);

        listView.setOnScrollListener(this);
        listView.setAdapter(new BaseAdapter() {
            String[] art = new String[]{"1", "2", "３", "5"};
            LinkedList<String> linkd = new LinkedList<>();

            @Override
            public int getCount() {
                return art.length;
            }

            public int getItemAt(int id) {
                return id;
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            private String makeData(int i) {
                int len = (int) (Math.random() * 100);
                int p = 0;
                StringBuilder stringBuilder = new StringBuilder();
                while (p < len) {
                    int a = (int) (Math.random() * 26);
                    char v;
                    if (i % 2 == 0) v = 'A';
                    else v = 'a';
                    char c = (char) (v + a);
                    stringBuilder.append(c);
                    p++;
                }
                return stringBuilder.toString();
            }

            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position) {
                super.getItemViewType(position);
                if (position == 0) {
                    return 0;
                } else {
                    return 1;
                }
            }

            // used in ViewPick row select demo：
            private View inflate0(View view, ViewGroup viewGroup) {
                if (view == null) {
                    view = LayoutInflater.from(MainActivity.this).
                            inflate(R.layout.test_list_item_view, viewGroup, false);

                    return view;
                }
                // do nothing
                return view;
            }


            @Override
            public View getView(final int i, View view, ViewGroup viewGroup) {

                if (i == 0) {
                    return inflate0(view, viewGroup);
                }

                if (i == 1) {
                    linkd.add("ksj");
                }
                float pp = UIUtils.dp2px(getApplication(), 10);
                Log.d("key log ", "Main AC getView " + i);
                final TextView tv;

                if (view == null) {
                    tv = new MYTextView(MainActivity.this);
                    tv.setTextSize(24);
                    tv.setTextColor(Color.BLACK);
                    tv.setMinHeight(minHeight);
                    int padding = UIUtils.dp2px(MainActivity.this, 3);
                    tv.setPadding(padding, padding, padding, padding);
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (i == 0) {
                                MainActivity.this.startActivity(new Intent(MainActivity.this, TestViewInfoActivity.class));
                            } else if (i == 1) {
                                MainActivity.this.startActivity(new Intent(MainActivity.this, FragmentAC.class));
                            } else if (i == 2) {
                                TextView popupContentView = new TextView(MainActivity.this);
                                popupContentView.setText("popup content");
                                new PopupWindow(popupContentView, 500, 500).showAsDropDown(view);
                            } else if (i == 3) {
                                new AlertDialog.Builder(MainActivity.this).setMessage("Hello").create().show();
                            } else {
                                MainActivity.this.startActivity(new Intent(MainActivity.this, BAC.class));
                            }
//                            throw new NullPointerException("((");
                        }
                    });

                } else {
                    tv = (TextView) view;
                }
                tv.setText(makeData(i));
                return tv;
            }
        });

        return listView;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void onWindowFocusChanged(boolean foxc) {
        super.onWindowFocusChanged(foxc);
    }


    /**
     * 监听滑动状态的改变
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    /**
     * 监听滑动
     */
    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {

    }


    private void requet() {
        new TickTask() {
            @Override
            public void onTick(int loopTime) {
                doRequest();
            }
        }.setMaxLoopTime(11000)
                .setIntervalWithFixedDelay(1000)
                .postAsync();
    }

    OkHttpClient okHttpClient = new OkHttpClient();

    private void doRequest() {
        Request request = new Request.Builder().url("https://www.baidu.com").build();
        try {
            okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
