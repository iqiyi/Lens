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

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;
import com.qiyi.lens.LensUtil;
import com.qiyi.lens.transfer.IReporter;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Lens 数据自动上报到后端。
 */
public class LensReporter implements IReporter {
    private static final String DUMP_VIEW_ON_WEB = "dump_view_on_web";
    private Context appContext;

    public LensReporter(Context context) {
        this.appContext = context;
    }

    @Override
    public void report(String type, String data) {
        if (TextUtils.equals(type, DUMP_VIEW_ON_WEB)) {
            if (LensUtil.getRemoteUrl() == null) {
                LensUtil.bindRemote();
            } else {
                reportToWebConsole(data);
            }
        }
    }

    private void reportToWebConsole(String data) {
        Request request = new Request.Builder().url(LensUtil.getRemoteUrl())
                .method("POST", new FormBody.Builder().add("msg", data).build())
                .build();
        Call call = new OkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == 404) {
                    LensUtil.bindRemote();
                    Toast.makeText(appContext, "请重新绑定远端 url", Toast.LENGTH_LONG).show();
                } else if (response.code() == 413) {
                    Toast.makeText(appContext, "日志过大，尝试导出后查看", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
