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
package com.qiyi.lens.ui.dns.infos;

import android.view.View;

import com.qiyi.lens.utils.reflect.Info;
import com.qiyi.lens.utils.reflect.Invalidate;
import com.qiyi.lens.utils.reflect.SpanableInfo;

import java.util.LinkedList;

public class Button extends Info {
    private int eventId;
    private ClickListener listener;

    public Button(int id, Invalidate par) {
        super(par);
        this.eventId = id;
    }

    @Override
    protected void makeList(LinkedList linkedList) {

    }

    @Override
    protected void preBuildSpannables(StringBuilder stringBuilder, LinkedList<SpanableInfo> infosList) {

    }

    @Override
    protected void afterBuildSpannables(StringBuilder stringBuilder, LinkedList<SpanableInfo> infosList) {

    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onClick(eventId);
        }
    }

    public void setOnClickListener(ClickListener clickListener) {
        listener = clickListener;
    }
}
