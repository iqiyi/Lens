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

import android.graphics.Color;

import androidx.annotation.LayoutRes;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiyi.lens.ui.FloatingPanel;

import java.lang.ref.WeakReference;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public abstract class AbsBlockInfo implements View.OnClickListener {
    private WeakReference<FloatingPanel> weakPanel;

    public abstract void bind(View view);

    public abstract void unBind();

    public FloatingPanel getPanel() {
        if (weakPanel != null) {
            return weakPanel.get();
        }
        return null;
    }

    public AbsBlockInfo(FloatingPanel panel) {
        weakPanel = new WeakReference<>(panel);
    }

    //start a new full screen activity and show Data
    protected void onBlockClicked() {

    }

    protected View inflateView(ViewGroup parent, @LayoutRes int id) {
        return LayoutInflater.from(parent.getContext()).inflate(id, parent, false);
    }

    public View createView(ViewGroup parent) {
        TextView tv = new TextView(parent.getContext());
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(16);
        tv.setMinHeight(22);
        tv.setGravity(Gravity.LEFT);
        tv.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        bindBlockClickEvent(tv);
        return tv;
    }


    protected void bindBlockClickEvent(View view) {
        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onBlockClicked();
    }


}
