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
package com.qiyi.lens.ui.devicepanel;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.qiyi.lens.Constants;
import com.qiyi.lens.dump.DumpDisplayLogPanel;
import com.qiyi.lens.dump.DumpTagsPanel;
import com.qiyi.lens.dump.LogDumperHolder;
import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.LogInfoDetailPanel;
import com.qiyi.lens.ui.SharedPreferencePanel;
import com.qiyi.lens.ui.abtest.ABNTestPanel;
import com.qiyi.lens.ui.database.DatabasePanel;
import com.qiyi.lens.ui.setting.LensSettingsPanel;
import com.qiyi.lens.utils.ApplicationLifecycle;
import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.SharedPreferenceUtils;
import com.qiyi.lens.utils.UIUtils;
import com.qiyi.lens.utils.Utils;
import com.qiyi.lens.utils.configs.ABNTestConfig;
import com.qiyi.lens.utils.event.DataCallBack;
import com.qiyi.lens.utils.event.EventBus;
import com.qiyi.lens.utils.event.SettingChangedEvent;
import com.qiyi.lens.utils.event.SettingLogSwitchChangedEvent;
import com.qiyi.lens.utils.event.SettingPanelStatusChangedEvent;
import com.qiyi.lenssdk.R;

import java.lang.ref.WeakReference;

/**
 * 悬浮窗口入口页面
 * 采用模块化设计：通过在设置界面中开启开关，
 */
public class DeviceInfoPanel extends FloatingPanel implements DataCallBack {

    private LinearLayout mLayout;
    private InfoManager mBlockInfoManager;
    private static final String TAB_SETTING = "tab_setting";
    private static final String TAB_LOG = "tab_logs";
    private static final String TAB_SP = "tab_sharedPreferences";

    private ImageButton mSettingBtn;
    private TextView mSecondBtn;
    private TextView mLogTv;
    private View abTest;
    private View DB;
    private WeakReference<Activity> mLastActivity;
    private int lastHideState;

    public DeviceInfoPanel(int wd) {
        super(wd);
        mBlockInfoManager = new InfoManager();
    }

    @Override
    public void reattach(Activity activity) {
        if (getPanelType() == Constants.PANEL_FLOAT_PANEL_WINDOW) return;

        super.reattach(activity);
        if (mLastActivity != null && activity != mLastActivity.get()) {
            // 切换 activity 时视图拾取取消，防止 activity 自动跳转时视图拾取功能故障
            View switchButton = findViewById(R.id.select);
            if (switchButton instanceof Switch && ((Switch) switchButton).isChecked()) {
                switchButton.performClick();
            }
        }
        mLastActivity = new WeakReference<>(activity);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View root = LayoutInflater.from(context).inflate(R.layout.lens_block_tab_info, null);
        mLayout = root.findViewById(R.id.panel_container);
        mSettingBtn = root.findViewById(R.id.float_control_setting);
        mSettingBtn.setTag(TAB_SETTING);
        mSecondBtn = root.findViewById(R.id.tv_block_second);
        mSecondBtn.setTag(TAB_SP);
        mLogTv = root.findViewById(R.id.tv_block_log);
        abTest = root.findViewById(R.id.lens_abt_entrance);
        DB = root.findViewById(R.id.lens_database_entrance);
        DB.setOnClickListener(this);
        View dumpBtn = root.findViewById(R.id.lens_dump_entrance);
        dumpBtn.setVisibility(LogDumperHolder.getInstance().isEnabled() ? View.VISIBLE : View.GONE);
        dumpBtn.setOnClickListener(this);

        if (!ABNTestConfig.getInstance().hasTestData()) {
            abTest.setVisibility(View.GONE);
        } else {
            abTest.setOnClickListener(this);
        }

        updateLogTag();
        mSettingBtn.setOnClickListener(this);
        mLogTv.setOnClickListener(this);
        mSecondBtn.setOnClickListener(this);
        mBlockInfoManager.bindViews(mLayout, this);
        EventBus.registerEvent(this, DataPool.EVENT_SETTING_CHANGED);
        EventBus.registerEvent(this, DataPool.EVENT_SETTING_LOG_CHANGED);

        return root;
    }

    protected void onUpdateView() {
        mBlockInfoManager.removeViews();
        mBlockInfoManager.bindViews(mLayout, this);
        updateLogTag();
    }


    private void updateLogTag() {
        boolean logSwitch = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_LOG_INFO, context, false);
        mLogTv.setVisibility(logSwitch ? View.VISIBLE : View.GONE);
        mLogTv.setTag(TAB_LOG);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mBlockInfoManager.unbind();
        DataPool.obtain().clear();
        EventBus.unRegisterEvent(this, DataPool.EVENT_SETTING_CHANGED);
        EventBus.unRegisterEvent(this, DataPool.EVENT_SETTING_LOG_CHANGED);
    }

    @Override
    protected int getPanelType() {
        return Utils.hasFloatingWindowPermission() ? Constants.PANEL_FLOAT_PANEL_WINDOW : Constants.PANEL_FLOAT_PANEL_VIEW;
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.lens_abt_entrance) {
            //[jump 2 abtest page]
            ABNTestPanel panel = new ABNTestPanel(this);
            panel.show();
            return;
        }

        if (id == R.id.lens_database_entrance) {
            DatabasePanel dbPanel = new DatabasePanel(this);
            dbPanel.show();
            return;
        }

        if (id == R.id.lens_dump_entrance) {
            handleDump(view);
            return;
        }

        if (view.getTag() != null && view.getTag() instanceof String) {
            String tag = (String) view.getTag();
            switch (tag) {
                case TAB_SETTING:
                    LensSettingsPanel settingPanel =
                            new LensSettingsPanel(this);
                    settingPanel.show();
                    break;
                case TAB_LOG:
                    LogInfoDetailPanel logPanel =
                            new LogInfoDetailPanel(this);
                    logPanel.show();
                    break;
                case TAB_SP:
                    SharedPreferencePanel spPanel =
                            new SharedPreferencePanel(this);
                    spPanel.show();
                default:
                    break;
            }
        }
    }

    private void handleDump(final View view) {

        view.setEnabled(false);
        String[] tags = LogDumperHolder.getInstance().getDumpTags();
        if (tags == null) {
            new DumpDisplayLogPanel(this, -1).show();
        } else {
            new DumpTagsPanel(DeviceInfoPanel.this, tags).show();
        }
        view.setEnabled(true);
    }

    @Override
    public void onDataArrived(final Object data, int type) {
        if (data instanceof SettingLogSwitchChangedEvent) {
            final SettingLogSwitchChangedEvent event = (SettingLogSwitchChangedEvent) data;
            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    mLogTv.setVisibility(event.logSwitch ? View.VISIBLE : View.GONE);
                }
            });
        }
        if (data instanceof SettingChangedEvent) {
            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    onUpdateView();
                }
            });
        }
        if (data instanceof SettingPanelStatusChangedEvent) {
            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (((SettingPanelStatusChangedEvent) data).status == ApplicationLifecycle.PANEL_STATE_CLOSE) {
                        dismiss();
                    }
                }
            });
        }
    }

    @Override
    protected WindowManager.LayoutParams getDefaultLayoutParams() {
        WindowManager.LayoutParams wlp = new WindowManager.LayoutParams();
        wlp.width = width();
        wlp.height = -2;
        wlp.x = UIUtils.dp2px(getActivity(),10);
        wlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//WindowManager.LayoutParams.FLAG_FULLSCREEN
        if (Build.VERSION.SDK_INT >= 26) {
            wlp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            wlp.type =
                    //WindowManager.LayoutParams.TYPE_TOAST;
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        //WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        wlp.gravity = Gravity.TOP | Gravity.LEFT;
        wlp.format = PixelFormat.RGBA_8888;
        wlp.y = offset();
        return wlp;
    }

    @Override
    public void onPause() {
        lastHideState = isHidden() ? 1 : 2;
        hide();
    }

    @Override
    public void onResume() {
        if (lastHideState == 2) {
            expand();
        }
    }
}
