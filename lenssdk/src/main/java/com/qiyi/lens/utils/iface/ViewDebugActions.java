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
package com.qiyi.lens.utils.iface;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.RestrictTo;

import com.qiyi.lens.LensUtil;
import com.qiyi.lens.ui.BasePanel;
import com.qiyi.lens.ui.devicepanel.blockInfos.ActivityInfo;
import com.qiyi.lens.utils.UIUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class ViewDebugActions {
    private ViewGroup mViewGroup;
    private Map<String, Runnable> mActions = new LinkedHashMap<>();// keep order
    private LinearLayout mLinearLayout;
    private ActivityInfo mActivityInfo;

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public ViewDebugActions(ActivityInfo activityInfo, BasePanel panel) {
        mActivityInfo = activityInfo;
        mViewGroup = (ViewGroup) panel.getDecorView();
        mLinearLayout = new LinearLayout(mViewGroup.getContext());
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setHorizontalGravity(Gravity.END);
    }

    /**
     * @param name   ： 对应的事件按钮名字
     * @param action ：  对应的可执行事件
     */
    public void add(String name, Runnable action) {
        mActions.put(name, action);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void show() {
        Context context = mViewGroup.getContext();
        mLinearLayout.addView(new View(context), ViewGroup.LayoutParams.MATCH_PARENT, UIUtils.getStatusBarHeight(context));
        for (Map.Entry<String, Runnable> entry : mActions.entrySet()) {
            Button button = new Button(context);
            button.setText(entry.getKey());
            final Runnable runnable = entry.getValue();
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    runnable.run();
                }
            });
            mLinearLayout.addView(button, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        mViewGroup.addView(mLinearLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void setViewDebugInfo(View view, String info) {
        LensUtil.setViewDebugInfo(view, info);
    }

    public void setViewDebugInfo(View view, String info, int color) {
        LensUtil.setViewDebugInfo(view, info, color);
    }

    public void watchObject(Object obj) {
        LensUtil.watchObject(obj);
    }


    public void watchObject(String name, Object obj) {
        LensUtil.watchObject(name, obj);
    }

    public void watchField(String name, Object obj) {
        LensUtil.watchField(name, obj);
    }

    public void exitViewDebug() {
        if (mActivityInfo != null && mActivityInfo.getPanel() != null) {
            mActivityInfo.exitViewSelectMode();
            mActivityInfo.getPanel().hide();
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void dismiss() {
        mActions.clear();
        if (mLinearLayout != null) {
            mViewGroup.removeView(mLinearLayout);
            mLinearLayout.removeAllViews();
        }
    }
}
