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
package com.qiyi.lens.ui.devicepanel.blockInfos.display;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.LogInfoDetailPanel;
import com.qiyi.lens.ui.devicepanel.blockInfos.AbsBlockInfo;
import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.KeyLog;
import com.qiyi.lens.utils.configs.DisplayConfiguration;
import com.qiyi.lens.utils.event.DataCallBack;
import com.qiyi.lens.utils.event.EventBus;
import com.qiyi.lenssdk.R;

public class DisplayInfo extends AbsBlockInfo implements DataCallBack {
    private TextView mTextView;
    private StringBuilder builder;
    private Handler handler;
    private int duration;
    private ICustomDisplay customDisplay;
    private KeyLog keyLog;
    private String[] logFilter;

    public DisplayInfo(FloatingPanel panel) {
        super(panel);
        builder = new StringBuilder();
        handler = panel.getMainHandler();
        keyLog = KeyLog.getKeyLogInstance();
        duration = DisplayConfiguration.obtain().getRefreshDuration();

    }

    @Override
    public void bind(View view) {

        ICustomDisplay display = DisplayConfiguration.obtain().getCustomDisplay();
        ViewGroup group = view.findViewById(R.id.display_info_custom);

        if (display != null) {
            customDisplay = display;
            String [] filters = display.getFilterTags();
            if(filters != null && filters.length > 0) {
                logFilter = KeyLog.resetLog(filters);
            }
            group = view.findViewById(R.id.display_info_custom);
            View custom = customDisplay.createView(group);
            group.removeAllViews();
            group.addView(custom);
            mTextView = customDisplay.getDisplay();
        } else {
            mTextView = view.findViewById(R.id.tv_display);
        }

        group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogInfoDetailPanel logPanel = new LogInfoDetailPanel(getPanel());
                logPanel.show();
            }
        });

        if (DisplayConfiguration.obtain() != null) {
            int ht = DisplayConfiguration.obtain().getDisplayHeight();
            if (ht > 0) {
                mTextView.getLayoutParams().height = ht;
            }
        }
        EventBus.registerEvent(this, DataPool.EVENT_DISPLAY_DATA_ARRIVED);
    }

    @Override
    public void unBind() {
        EventBus.unRegisterEvent(this, DataPool.EVENT_DISPLAY_DATA_ARRIVED);
        mTextView = null;
        if(logFilter != null) {
            KeyLog.resetLog(logFilter);
        }
    }


    @Override
    public void onDataArrived(Object data, int type) {
        FloatingPanel panel = getPanel();
        if (panel != null) {
//            handler.removeCallbacks(display);
            handler.postDelayed(display, duration);
        }

    }

    private Runnable display = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(display);
            if (mTextView != null) {
                builder.setLength(0);
                keyLog.getBriefDisplay(builder);
                mTextView.setText(builder.toString());
            }
        }
    };

    @Override
    public View createView(ViewGroup group) {
        return inflateView(group, R.layout.lens_block_datadisplay_info);
    }
}
