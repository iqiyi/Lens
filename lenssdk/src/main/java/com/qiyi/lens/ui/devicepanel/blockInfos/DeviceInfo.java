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

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.widget.tableView.TableBuilder;
import com.qiyi.lenssdk.R;

/**
 * 支持实时更新CPU 信息
 */
public class DeviceInfo extends AbsBlockInfo {
    private TableBuilder builder;
    private LinearLayout mContainerLl;

    public DeviceInfo(FloatingPanel panel) {
        super(panel);
    }

    @Override
    public void bind(View textView) {

    }

    public void unBind() {
        builder = null;
    }


    @Override
    public View createView(ViewGroup parent) {
        View root = inflateView(parent, R.layout.lens_block_device_info);
        mContainerLl = root.findViewById(R.id.ll_container);

        Context context = parent.getContext();
        DisplayMetrics df = context.getResources().getDisplayMetrics();
        String[] data = new String[]{
                "CPU", "Memo", "SDK", "--", "--", Build.VERSION.SDK_INT + "",
                "分辨率", "像素密度", "", df.widthPixels + "\n" + df.heightPixels, df.density + "", ""
        };
        builder = TableBuilder.obtain();
        View view = builder
                .setData(data)
                .setItemTextSize(12)
                .setStrokeWidth(3, 5)
                .setStretchableColumns(0,1)
                .setColumnCountRowCount(3, 4)
                .build(context);

        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(-2, -2);
        clp.gravity = Gravity.CENTER_HORIZONTAL;
        view.setLayoutParams(clp);
        mContainerLl.addView(view);
        return root;
    }

}
