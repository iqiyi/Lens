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
package com.qiyi.lens.ui.setting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;

import com.qiyi.lens.transfer.DataTransferManager;
import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.utils.ApplicationLifecycle;
import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.SharedPreferenceUtils;
import com.qiyi.lens.utils.event.SettingChangedEvent;
import com.qiyi.lens.utils.event.SettingLogSwitchChangedEvent;
import com.qiyi.lens.utils.event.SettingPanelStatusChangedEvent;
import com.qiyi.lenssdk.BuildConfig;
import com.qiyi.lenssdk.R;

import java.util.Arrays;

/**
 * 设置界面
 */
public class LensSettingsPanel extends FullScreenPanel implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private RadioGroup mWindowRadioGroup;
    private RadioButton mWindowOpen;
    private RadioButton mWindowHide;
    private Context mContext;
    private int mNewRadioStatus;
    private boolean logInfoDefaultStatus, logInfoNewStatus;
    private GridView settingGrid;
    private SwitchAction switchAction;

    public LensSettingsPanel(FloatingPanel panel) {
        super(panel);
        mContext = context;
        setTitle(R.string.lens_panle_ac_setting_title);
        setMeta("v" + BuildConfig.VERSION_NAME);
    }

    @Override
    protected View onCreateView(ViewGroup viewGroup) {
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.lens_setting_grid,
                viewGroup, false);
        findViews(contentView);
        setOnClickListeners();
        initSettings();
        return contentView;
    }

    private void findViews(View view) {

        mWindowRadioGroup = view.findViewById(R.id.lens_open_status_rg);
        mWindowOpen = view.findViewById(R.id.radio_open);
        RadioButton mWindowClose = view.findViewById(R.id.radio_close);
        mWindowHide = view.findViewById(R.id.radio_min);

        // 未持久化
        GridView gridView = view.findViewById(R.id.lens_setting_grid_view);
        switchAction = new SwitchAction();
        ConfigEventCallBack eventCallBack = new ConfigEventCallBack(new DefaultJumpAction(), switchAction);
        gridView.setAdapter(new SettingGridAdapter(mContext).loadData(new SettingDataConfig(), eventCallBack));
        GridView quickEntrance = view.findViewById(R.id.lens_setting_quick_entrance);
        quickEntrance.setAdapter(new SettingGridAdapter(mContext).loadData(new QuickEntranceConfig(), eventCallBack));

        GridView bindEntrance = view.findViewById(R.id.lens_setting_bind_entrance);
        bindEntrance.setAdapter(new ArrayAdapter<>(mContext, R.layout.lens_layout_setting_bind_item,
                Arrays.asList(mContext.getString(R.string.lens_block_bind_remote), "", "")));
        bindEntrance.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    DataTransferManager.getInstance().bindRemote();
                }
            }
        });
    }


    private boolean initAndSet(View view, @IdRes int id, String keySP) {
        View v = view.findViewById(id);
        if (v != null) {
            v.setOnClickListener(this);
            boolean selected = SharedPreferenceUtils.getSharedPreferences(keySP, mContext, false);
            v.setSelected(selected);
            return selected;
        }
        return false;

    }

    private void setOnClickListeners() {
        mWindowRadioGroup.setOnCheckedChangeListener(this);
    }

    /**
     * 读取SharedPreferences，设置开关状态
     */
    private void initSettings() {

        RadioButton button = null;
        int defaultRadio = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_KEY_PANEL_STATUS, mContext, -1);
        if (defaultRadio == ApplicationLifecycle.PANEL_STATE_SHOW) {
            button = mWindowOpen;
        } else if (defaultRadio == ApplicationLifecycle.PANEL_STATE_MIN) {
            button = mWindowHide;
        } else if (defaultRadio == ApplicationLifecycle.PANEL_STATE_CLOSE) {
            button = mWindowOpen;
        }
        if (button != null) {
            button.setChecked(true);
        }


    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void onShow() {
        super.onShow();
    }

    @Override
    protected void onDismiss() {
        //[用于标记是否修改一级面板中的日志按钮是否展示]
        boolean selectStatusChanged = switchAction.switchChanged;
        if (logInfoDefaultStatus != logInfoNewStatus) {
            SettingLogSwitchChangedEvent event = new SettingLogSwitchChangedEvent();
            event.logSwitch = logInfoNewStatus;
            DataPool.obtain().putData(DataPool.EVENT_SETTING_LOG_CHANGED, event);
        }
        if (selectStatusChanged) {
            DataPool.obtain().putData(DataPool.EVENT_SETTING_CHANGED, new SettingChangedEvent());
        }
        if (mNewRadioStatus == ApplicationLifecycle.PANEL_STATE_CLOSE) {
            SettingPanelStatusChangedEvent event = new SettingPanelStatusChangedEvent();
            event.status = mNewRadioStatus;
            DataPool.obtain().putData(DataPool.EVENT_SETTING_CHANGED, event);
        }
    }

    @Override
    public void onClick(View view) {

    }


    private boolean updateViewSP(View view, String sp) {
        boolean selected = !view.isSelected();
        view.setSelected(selected);
        SharedPreferenceUtils.setSharedPreferences(sp, selected, mContext);
        return selected;
    }


    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        if (R.id.radio_open == checkedId) { //展开
            SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_PANEL_STATUS, ApplicationLifecycle.PANEL_STATE_SHOW, mContext);
            mNewRadioStatus = ApplicationLifecycle.PANEL_STATE_SHOW;
        }
        if (R.id.radio_min == checkedId) { //最小化
            SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_PANEL_STATUS, ApplicationLifecycle.PANEL_STATE_MIN, mContext);
            mNewRadioStatus = ApplicationLifecycle.PANEL_STATE_MIN;
        }
        if (R.id.radio_close == checkedId) { //关闭
            SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_KEY_PANEL_STATUS, ApplicationLifecycle.PANEL_STATE_CLOSE, mContext);
            mNewRadioStatus = ApplicationLifecycle.PANEL_STATE_CLOSE;
        }

    }

}
