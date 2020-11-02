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

import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.View;

import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.reflect.Info;
import com.qiyi.lens.utils.reflect.Invalidate;
import com.qiyi.lens.utils.reflect.SpanableInfo;

import java.util.LinkedList;

public class UrlInfo extends Info implements ClickListener {
    String value;
    private static Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC);
    private Button copy;
    private Button share;
    private Button urlAna;
    private RequestData requestInfo;

    private static String mFilter;

    public UrlInfo(RequestData data, Invalidate par) {
        super(par);
        this.requestInfo = data;
        this.value = data.url;
        level = 4;
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
    public String toString() {
        if (value == null) {
            return "--";
        } else if (value.length() > 80) {
            return value.substring(0, 80) + "...";
        } else {
            return value;
        }
    }

    @Override
    public boolean isBasicType() {
        return false;
    }

    @Override
    public void makeSpannable(StringBuilder stringBuilder, LinkedList<SpanableInfo> spanableInfos) {


        if (isNotFiltered()) {


            stringBuilder.append("\n");
            int start = stringBuilder.length();
            String space = makeSpace();
            stringBuilder.append(space);
            stringBuilder.append(toString());
            int end = stringBuilder.length();
            spanableInfos.add(new SpanableInfo(start, end, this));
            stringBuilder.append("\n");
            stringBuilder.append(space);
            start = stringBuilder.length();
            stringBuilder.append("\t");
            stringBuilder.append("复制");
            stringBuilder.append("\t");
            end = stringBuilder.length();
            if (copy == null) {
                copy = new Button(0, null);
                copy.setOnClickListener(this);
            }
            spanableInfos.add(new SpanableInfo(start, end, copy));

            start = stringBuilder.length();
            stringBuilder.append("\t");
            stringBuilder.append("分享");
            stringBuilder.append("\t");
            end = stringBuilder.length();
            if (share == null) {
                share = new Button(1, null);
                share.setOnClickListener(this);
            }
            spanableInfos.add(new SpanableInfo(start, end, share));


            //[分析]
            start = stringBuilder.length();
            stringBuilder.append("\t");
            stringBuilder.append("分析");
            stringBuilder.append("\t");
            end = stringBuilder.length();
            if (urlAna == null) {
                urlAna = new Button(2, null);
                urlAna.setOnClickListener(this);
            }
            spanableInfos.add(new SpanableInfo(start, end, urlAna));


        }


    }

    public String getValue() {
        return value;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(0xff0000FF);
        ds.setTypeface(typeface);
    }

    @Override
    public void onClick(int id) {
        if (id == 0) {//copy
//            toast("copy clicked");
//            shareText();
            DataPool.obtain().putData(DataPool.EVENT_CLICK_URL_TO_COPY, value);
        } else if (id == 1) {//share
//            toast("share clicked");
            DataPool.obtain().putData(DataPool.EVENT_CLICK_URL_TO_SHARE, value);
//            shareText(copy,"","",value);
        } else if (id == 2) {
            DataPool.obtain().putData(DataPool.EVENT_CLICK_URL_TO_ANALYSE, value);
        }
    }

    @Override
    public void onClick(View view) {
        DataPool.obtain().putData(DataPool.EVENT_CLICK_URL_TO_DETAIL, requestInfo);
        //[onUrlClicked]

    }

    @Override
    public boolean isNotFiltered() {
        if (mFilter == null || mFilter.length() == 0) {
            return true;
        } else if (value == null) {
            return false;
        } else {
            return value.contains(mFilter);
        }

    }

    public static void setFilter(String s) {
        mFilter = s;
    }

    public static boolean hasFilter() {
        return mFilter != null && mFilter.length() > 0;
    }
}
