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

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiyi.lens.ui.analyze.ActivityViewCategorizedInfoPanle;
import com.qiyi.lens.ui.analyze.FragmentCaptureSubPanel;
import com.qiyi.lens.ui.objectinfo.ObjectInfoPanel;
import com.qiyi.lens.ui.viewtree.ViewTreePanel;
import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.ViewClassifyUtil;
import com.qiyi.lens.utils.reflect.FragmentInfo;
import com.qiyi.lens.utils.reflect.Info;
import com.qiyi.lens.utils.reflect.ObjectFieldCollector;
import com.qiyi.lenssdk.R;


/**
 * 界面分析面板
 * 1, 展示界面的AC, 展示界面的fragment 构成。
 * 2，新增 如果界面中有Dialog ， 则展示dialog 的类。
 */
public class ActivityInfoPanel extends FullScreenPanel implements ObjectFieldCollector.DataRefreshCallback,
        View.OnClickListener, FullScreenPanel.OnDismissListener, Info.OnClickListener {
    private Activity activity;
    private ObjectFieldCollector collector;
    private TextView activityBaseInfoDisplay;
    private ViewTreePanel treePanel;
    private ViewClassifyUtil util;
    private ObjectFieldCollector.Binder dataBinder;
    private View viewCategorizedInfoView;
    // 用于展示fragment 信息
    private FragmentCaptureSubPanel mSubViewPanel;

    public ActivityInfoPanel(FloatingPanel panel) {
        super(panel);
        setTitle(R.string.lens_panle_ac_ana_title);
    }

    @Override
    public View onCreateView(ViewGroup group) {

        View content = inflateView(R.layout.lens_activity_info_panel, group);
        activityBaseInfoDisplay = content.findViewById(R.id.panel_ac_info_tv1);
        //    TextView activityFieldInfoDisplay;
        TextView viewTreeInfo = content.findViewById(R.id.panel_ac_info_veiw_tree);
        TextView activityInfo = content.findViewById(R.id.panel_ac_info_activity);
        View maxLevelView = content.findViewById(R.id.lens_panel_ac_max_tree_level);
        viewCategorizedInfoView = content.findViewById(R.id.panel_ac_info_veiw_types);
        activityInfo.setOnClickListener(this);
        viewTreeInfo.setOnClickListener(this);
        maxLevelView.setOnClickListener(this);
        viewCategorizedInfoView.setOnClickListener(this);

        return content;
    }

    private TextView createTextView() {
        TextView view = new TextView(context);
        view.setTextSize(14);
        view.setTextColor(Color.BLACK);
        view.setLinksClickable(false);
        return view;
    }

    @Override
    public void onShow() {
        super.onShow();
        activity = (Activity) DataPool.obtain().getDataAsset(DataPool.DATA_TYPE_ACTIVITY, Activity.class);
        showProgress();
        new Thread() {
            @Override
            public void run() {
                analyze();
            }
        }.start();

    }

    private void analyze() {
        if (collector == null) {
            collector = new ObjectFieldCollector(activity);
            collector.setDataRefreshCallBack(this);
            collector.setOnClickListener(this);
        }

        if (activityBaseInfoDisplay != null) {
            util = new ViewClassifyUtil(activity.findViewById(android.R.id.content));
            activityBaseInfoDisplay.post(new Runnable() {
                @Override
                public void run() {
                    viewCategorizedInfoView.setVisibility(View.VISIBLE);
                    dataBinder = collector.build(util)
                            .bindActivityBaseInfo(activityBaseInfoDisplay);
                }
            });
        }

        getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                dismissProgress();
            }
        });

    }


    @Override
    public void onDismiss() {
        super.onDismiss();
        if (collector != null) {
            collector.setDataRefreshCallBack(null);
            collector = null;
        }
    }


    @Override
    public void onDataRefresh() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                analyze();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.panel_ac_info_veiw_tree) {
            //[show view tree]
            if (treePanel == null) {
                treePanel = new ViewTreePanel(null);
                treePanel.setOnDismissListener(this);
            }

            treePanel.setData(activity.findViewById(android.R.id.content));
            treePanel.show();

        } else if (vid == R.id.panel_ac_info_activity) {
            //[show activity info]
            ObjectInfoPanel objectInfoPanel = new ObjectInfoPanel(null, activity);
            objectInfoPanel.show();
        } else if (vid == R.id.lens_panel_ac_max_tree_level) {
            if (treePanel == null) {
                treePanel = new ViewTreePanel(null);
                treePanel.setOnDismissListener(this);
            }
            treePanel.setMaxLevelView(util.getMaxLevelView(), util.getViewLevel());

            treePanel.show();

        } else if (vid == R.id.panel_ac_info_veiw_types) {
            // new Panel to show view categorized info
            ActivityViewCategorizedInfoPanle panel = new ActivityViewCategorizedInfoPanle(getFloatingPanel());
            panel.setDataBinder(dataBinder);
            panel.show();

        }

    }

    @Override
    public void onDismissListener(FullScreenPanel panel) {
        if (panel == treePanel) {
            if (treePanel.getResultCode() == -1) {
                View view = (View) treePanel.getResultData();
            }
        }

    }

    @Override
    public void onClick(Info info) {
        if (info instanceof FragmentInfo) {
            if (mSubViewPanel == null) {
                mSubViewPanel = new FragmentCaptureSubPanel((ViewGroup) getDecorView());
            }
            mSubViewPanel.showData((FragmentInfo) info);
        }
    }
}
