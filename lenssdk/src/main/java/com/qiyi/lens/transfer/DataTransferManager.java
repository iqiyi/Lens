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
package com.qiyi.lens.transfer;

import android.text.TextUtils;

import com.qiyi.lens.utils.ApplicationLifecycle;
import com.qiyi.lens.utils.LL;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.SharedPreferenceUtils;

/**
 * transfer data to Lens server
 */
public class DataTransferManager {
    private static final String TAG = "DataTransferManager";
    private final static String DUMP_VIEW_ON_WEB = "dump_view_on_web";
    private static final DataTransferManager INSTANCE = new DataTransferManager();


    private IReporter reporter;
    private IRemoteBinder binder = new DefaultRemoteBinder();
    private String remoteUrl;


    public static DataTransferManager getInstance() {
        return INSTANCE;
    }

    private DataTransferManager() {
    }

    public void setReporter(IReporter reporter) {
        this.reporter = reporter;
    }

    public void setRemoteBinder(IRemoteBinder binder) {
        this.binder = binder;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
        SharedPreferenceUtils.setSharedPreferences(LensConfig.SP_LENS_REMOTE_URL, remoteUrl,
                ApplicationLifecycle.getInstance().getContext());
    }

    public String getRemoteUrl() {
        if (TextUtils.isEmpty(remoteUrl)) {
            remoteUrl = SharedPreferenceUtils.getSharedPreferences(LensConfig.SP_LENS_REMOTE_URL,
                    ApplicationLifecycle.getInstance().getContext(), null);
        }
        return remoteUrl;
    }

    public void report(String type, String data) {
        if (reporter == null) {
            LL.e(TAG, "lens reporter not set");
            return;
        }
        reporter.report(type, data);
    }

    public void push2Web(String data) {
        report(DUMP_VIEW_ON_WEB, data);
    }

    public boolean bindRemote() {
        return binder.bind();
    }

    public boolean hasReporter() {
        return reporter != null;
    }
}
