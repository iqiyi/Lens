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
package com.qiyi.lens.ui.devicepanel.blockInfos;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.qiyi.lens.ui.ActivityInfoPanel;
import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.viewinfo.CurrentViewInfoPanel;
import com.qiyi.lens.ui.viewinfo.SelectViewPanel;
import com.qiyi.lens.ui.viewinfo.ViewInfoHolder;
import com.qiyi.lens.ui.viewinfo.Widget;
import com.qiyi.lens.ui.viewinfo.uicheck.UIUploadPanel;
import com.qiyi.lens.utils.ApplicationLifecycle;
import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.configs.ViewInfoConfig;
import com.qiyi.lens.utils.event.DataCallBack;
import com.qiyi.lens.utils.event.EventBus;
import com.qiyi.lens.utils.iface.IViewInfoHandle;
import com.qiyi.lens.utils.iface.ViewDebugActions;
import com.qiyi.lens.utils.reflect.Info;
import com.qiyi.lens.utils.reflect.ObjectFieldCollector;
import com.qiyi.lens.utils.reflect.SpanableInfo;
import com.qiyi.lenssdk.R;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

/**
 * to display activity info : such as witch activity;
 * how many views inside
 * view levels
 * 页面分析：
 * 新UI验收功能 : 当有选中视图的时候，使用选中的视图进行验收。没有选中的视图时候，使用当前界面的的根view 进行验收。
 */
public class ActivityInfo extends AbsBlockInfo implements DataCallBack,
        ViewInfoHolder.WidgetSelectCallback, CompoundButton.OnCheckedChangeListener,
        FullScreenPanel.OnDismissListener {

    private TextView mActivityInfoTv;
    private Activity currentActivity;
    private TextView currentWidget;
    private CompoundButton selectWidget;
    private CheckBox showDistanceCheckbox;
    private CheckBox showSiblingCheckbox;
    private SelectViewPanel selectViewPanel;
    private View icBack;
    private View selectRowModel;
    private WeakReference<View> listRowRef;
    private ViewDebugActions mViewDebugActions;
    // 缓存当前被选中的视图。用于UI 验收
    private WeakReference<View> currentSelectionView;

    public ActivityInfo(FloatingPanel panel) {
        super(panel);
    }

    @Override
    public View createView(ViewGroup parent) {
        View root = inflateView(parent, R.layout.lens_block_activity_info);
        mActivityInfoTv = root.findViewById(R.id.tv_activity_info);
        currentWidget = root.findViewById(R.id.currentWidget);
        selectWidget = root.findViewById(R.id.select);
        showDistanceCheckbox = root.findViewById(R.id.show_distance);
        showSiblingCheckbox = root.findViewById(R.id.show_sibling);
        icBack = root.findViewById(R.id.back_to_parent);
        selectRowModel = root.findViewById(R.id.up_to_row);
//        uiPost = root.findViewById(R.id.lens_activity_ui_post);

        root.findViewById(R.id.down_to_next).setOnClickListener(this);
        bindBlockClickEvent(parent);
        return root;
    }

    @Override
    protected void bindBlockClickEvent(View view) {
        currentWidget.setOnClickListener(this);
        selectWidget.setOnClickListener(this);
        showDistanceCheckbox.setOnCheckedChangeListener(this);
        showSiblingCheckbox.setOnCheckedChangeListener(this);
        icBack.setOnClickListener(this);
        mActivityInfoTv.setOnClickListener(this);
        selectRowModel.setOnClickListener(this);
    }

    @Override
    public void bind(View view) {
        currentActivity = (Activity) DataPool.obtain().getDataAsset(DataPool.DATA_TYPE_ACTIVITY, String.class);
        if (currentActivity == null) {
            currentActivity = ApplicationLifecycle.getInstance().getCurrentActivity();
        }
        setData();
        //[fix bug : 热启动时候，stampo util 存在值 但是数据却需要刷新的情况]
        EventBus.registerEvent(this, DataPool.DATA_TYPE_ACTIVITY);
        ViewInfoHolder.getInstant().setWidgetSelectCallback(this);
    }

    @Override
    public void unBind() {
//        mActivityInfoTv = null;
        currentActivity = null;
        EventBus.unRegisterEvent(this, DataPool.DATA_TYPE_ACTIVITY);
        ViewInfoHolder.getInstant().setWidgetSelectCallback(null);
    }

    @Override
    public void onDataArrived(Object data, int type) {
        if (data instanceof Activity) {
            currentActivity = (Activity) data;
            setData();
        }
    }

    private void setData() {
        if (mActivityInfoTv != null && currentActivity != null) {
            String activityInfo = String.format(
                    mActivityInfoTv.getContext().getString(R.string.lens_block_activity_info),
                    getAppLabel(mActivityInfoTv.getContext()), currentActivity.getClass().getName());
            mActivityInfoTv.setText(activityInfo);
        }

    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.select) {
            if (selectViewPanel == null) {
                createSelectViewPanel();
                toggleViewInfoWidget(true);
            } else {
                selectViewPanel.dismiss();
                toggleViewInfoWidget(true);
            }
        } else if (id == R.id.currentWidget) {
            if (ViewInfoHolder.getInstant().getCurrentWidget() != null) {
                CurrentViewInfoPanel panel = new CurrentViewInfoPanel(getPanel());
                panel.setDataView(ViewInfoHolder.getInstant()
                        .getCurrentWidget().getView(), false);
                panel.show();
            }
        } else if (id == R.id.back_to_parent) {
            if (selectViewPanel != null) {
                selectViewPanel.selectParent();
            }
        } else if (id == R.id.tv_activity_info) {
            FloatingPanel basePanel = getPanel();
            if (basePanel != null) {
                ActivityInfoPanel panel =
                        new ActivityInfoPanel(basePanel);
                panel.show();
            }
        } else if (id == R.id.up_to_row) {
            if (selectViewPanel != null && listRowRef.get() != null) {
                selectViewPanel.selectListRow(listRowRef.get());
            }
        } else if (id == R.id.down_to_next) {
            if (selectViewPanel != null) {
                selectViewPanel.selectNext();
            }
        }
    }

    public void exitViewSelectMode() {
        if (selectWidget != null) {
            selectWidget.performClick();
        }
    }

    private void toggleViewInfoWidget(boolean visible) {
        if (visible) {
            icBack.setVisibility(View.VISIBLE);
            showDistanceCheckbox.setVisibility(View.VISIBLE);
            showSiblingCheckbox.setVisibility(View.VISIBLE);
            currentWidget.setVisibility(View.VISIBLE);
        } else {
            icBack.setVisibility(View.GONE);
            showDistanceCheckbox.setVisibility(View.GONE);
            showSiblingCheckbox.setVisibility(View.GONE);
            currentWidget.setVisibility(View.GONE);
            showDistanceCheckbox.setChecked(false);
            showSiblingCheckbox.setChecked(false);
            currentWidget.setText(null);
        }
    }

    private void createSelectViewPanel() {
        selectViewPanel = new SelectViewPanel(getPanel());
        selectViewPanel.setOnDismissListener(this);
        selectViewPanel.show();
        selectWidget.setChecked(true);
        mViewDebugActions = new ViewDebugActions(this, selectViewPanel);
    }

    private String getAppLabel(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo()
                    .packageName, 0);
        } catch (final PackageManager.NameNotFoundException ignored) {
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (selectViewPanel == null && isChecked) {
            createSelectViewPanel();
        }
        if (selectViewPanel != null) {
            if (buttonView.getId() == R.id.show_distance) {
                selectViewPanel.showRelativePos(isChecked);
            } else if (buttonView.getId() == R.id.show_sibling) {
                selectViewPanel.showSibling(isChecked);
            }
        }
    }

    @Override
    public void onDismissListener(FullScreenPanel panel) {
        selectViewPanel = null;
        selectWidget.setChecked(false);
        currentSelectionView = null;
        toggleViewInfoWidget(false);
        ViewInfoHolder.getInstant().setCurrentWidget(null);
    }

    @Override
    public void onWidgetSelect(Widget widget) {
        if (widget == null) return;
        View selectView = widget.getView();
        if (selectView != null) {

            currentSelectionView = new WeakReference<>(selectView);
            checkListRow(selectView);


            // new :  support simple view debug
            onViewDebug(selectView);

            Info info = ObjectFieldCollector.create(selectView, null, null);
            info.setExpand(true);
            StringBuilder stringBuilder = new StringBuilder();
            info.makeSpannable(stringBuilder, new LinkedList<SpanableInfo>());
            currentWidget.setText(stringBuilder.toString());
        }

    }

    private void onViewDebug(View mView) {
        Class<? extends IViewInfoHandle> handle = ViewInfoConfig.getInstance().getViewInfoHandle();
        if (handle != null) {
            try {
                mViewDebugActions.dismiss();
                IViewInfoHandle handler = handle.newInstance();
                handler.onViewDebug(mViewDebugActions, mView);
                mViewDebugActions.show();
            } catch (InstantiationException var10) {
                var10.printStackTrace();
            } catch (IllegalAccessException var11) {
                var11.printStackTrace();
            }
        }
    }

    private void checkListRow(View view) {
        boolean hasRowModel = false;
        View parent;
        while (view != null) {
            ViewParent viewParen = view.getParent();
            if (viewParen instanceof View) {
                parent = (View) view.getParent();
                if (parent instanceof AbsListView || parent instanceof RecyclerView) {
                    hasRowModel = true;
                    listRowRef = new WeakReference<>(view);
                    break;
                }
                view = parent;
            } else {
                break;
            }

        }

        if (selectRowModel != null) {
            selectRowModel.setVisibility(hasRowModel ? View.VISIBLE : View.GONE);
        }
    }
}
