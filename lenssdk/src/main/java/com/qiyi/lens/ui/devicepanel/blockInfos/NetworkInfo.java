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

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.qiyi.lens.ui.BasePanel;
import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.dns.DNSSetting;
import com.qiyi.lens.ui.dns.DNSSettingPanel;
import com.qiyi.lens.ui.dns.HttpRequestPanel;
import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.event.DataCallBack;
import com.qiyi.lens.utils.event.EventBus;
import com.qiyi.lenssdk.R;

/**
 * 网络分析
 */
public class NetworkInfo extends AbsBlockInfo implements DataCallBack {

    private static final String TAG_DNS_SETTING = "dns_setting";
    private static final String TAG_NETWORK_MORE = "network_more";

    private TextView mNetworkTest;
    private TextView mFilterCount;

    private DataCallBack dnsSettingChanged = new DataCallBack() {
        @Override
        public void onDataArrived(Object data, int type) {
            boolean bl = (boolean) data;
            mNetworkTest.setVisibility(bl ? View.VISIBLE : View.GONE);
        }
    };

    public NetworkInfo(FloatingPanel panel) {
        super(panel);
    }

    public void bind(View view) {
        EventBus.registerEvent(this, DataPool.DATA_TYPE_NET_FILTER_SIZE);
        EventBus.registerEvent(dnsSettingChanged, DataPool.EVENT_DNS_SET_CHANGE);
    }

    public void unBind() {
        EventBus.unRegisterEvent(this, DataPool.DATA_TYPE_NET_FILTER_SIZE);
        EventBus.unRegisterEvent(dnsSettingChanged, DataPool.EVENT_DNS_SET_CHANGE);
    }

    @Override
    public View createView(ViewGroup parent) {
        View root = inflateView(parent, R.layout.lens_block_network_info);
        mNetworkTest = root.findViewById(R.id.tv_network_test);
        View mMoreBtn = root.findViewById(R.id.network_more);
        mMoreBtn.setTag(TAG_NETWORK_MORE);
        Button mDnsSetting = root.findViewById(R.id.network_dns_setting);
        mDnsSetting.setTag(TAG_DNS_SETTING);
        mFilterCount = root.findViewById(R.id.tv_dns_info);
        if (DNSSetting.isFilterEnabled(parent.getContext())) {
            mFilterCount.setText("正在抓包");
        } else {
            mFilterCount.setText("未开启抓包");
        }
        mMoreBtn.setOnClickListener(this);
        mDnsSetting.setOnClickListener(this);
        if (!DNSSetting.isDNSEnabled(parent.getContext())) {
            mNetworkTest.setVisibility(View.GONE);
        }
        return root;
    }

    @Override
    public void onDataArrived(Object data, int type) {
        BasePanel panel = getPanel();
        if (panel != null) {
            refresh.setData((int) data);
            panel.getMainHandler().removeCallbacks(refresh);
            panel.getMainHandler().postDelayed(refresh, 500);
        }
    }

    private DataRefresh refresh = new DataRefresh();

    class DataRefresh implements Runnable {
        int data;

        public void setData(int d) {
            data = d;
        }

        @Override
        public void run() {
            int size = data;
            mFilterCount.setText("抓到" + size + "条请求");
            mFilterCount.invalidate();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getTag() != null && view.getTag() instanceof String) {
            String tag = (String) view.getTag();
            FloatingPanel basePanel = getPanel();
            switch (tag) {
                case TAG_DNS_SETTING:
                    if (basePanel != null) {
                        DNSSettingPanel panel = new DNSSettingPanel(basePanel);
                        panel.show();
                    }
                    break;
                case TAG_NETWORK_MORE:
                    HttpRequestPanel panel = new HttpRequestPanel(basePanel);
                    panel.show();
                    break;

            }
        }
    }
}
