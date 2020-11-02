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
package com.qiyi.lens.utils.reflect;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.qiyi.lens.ui.FullScreenPanel;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public abstract class Info extends ClickableSpan implements Invalidate {
    protected WeakReference<Invalidate> parent;
    //[当前属性内容是否为展开状态。]
    protected boolean isExpand;
    protected List<Info> list;
    protected int level = 1;
    private int expandColor = 0xff843900;
    private OnClickListener mOnClickListener;

    public Info(Invalidate par) {
        this.parent = new WeakReference<>(par);
    }

    public Info(WeakReference<Invalidate> p) {
        this.parent = p;
    }

    @Override
    public void onClick(View view) {
        isExpand = !isExpand;
        if (isExpand) {
            if (list == null || list.isEmpty()) {
                makeList(null);
                setOnClickListener(mOnClickListener);
            }
        }
        if(mOnClickListener != null) {
            mOnClickListener.onClick(this);
        }
        invalidate();
    }

    public void invalidate() {
        if (parent != null) {
            Invalidate invalidate = parent.get();
            if (invalidate != null) {
                invalidate.invalidate();
            }
        }
    }

    protected String makeSpace() {
        int p = level;
        StringBuilder s = new StringBuilder();
        while (p > 0) {
            s.append("\t  ");
            p--;
        }
        return s.toString();
    }

    protected final void buildInfoList(List<Info> list, StringBuilder stringBuilder, LinkedList<SpanableInfo> infos) {
        if (list != null && !list.isEmpty()) {
            String space = makeSpace();
            for (Info info : list) {
                stringBuilder.append(space);
                info.makeSpannable(stringBuilder, infos);
            }
        }
    }


    public void setOnClickListener(OnClickListener listener){
        mOnClickListener = listener;
        if(list != null) {
            for (Info info : list) {
                info.setOnClickListener(listener);
            }
        }
    }


    /**
     * StringBuilder builder = new StringBuilder();
     * builder.append(text1).append(text2).append(text3);
     * Spannable spannable = new SpannableStringBuilder(builder);
     * spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF7E00")), text1.length(), text1.length() + text2.length()
     * , Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
     * spannable.setSpan(new AbsoluteSizeSpan(19, true), text1.length(), text1.length() + text2.length()
     * , Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
     *
     * @return
     */
    public void makeSpannable(StringBuilder stringBuilder, LinkedList<SpanableInfo> spannableInfo) {
        if (isNotFiltered()) {
            stringBuilder.append("\n");
            int start = stringBuilder.length();
            String space = makeSpace();
            stringBuilder.append(space);
            stringBuilder.append(isExpand ? "- " : "+ ");
            stringBuilder.append(toString());
            int end = stringBuilder.length();
            if (isExpand) {
                preBuildSpannables(stringBuilder, spannableInfo);
                buildInfoList(list, stringBuilder, spannableInfo);
                afterBuildSpannables(stringBuilder, spannableInfo);
            }
            spannableInfo.add(new SpanableInfo(start, end, this));

        }
    }

    public boolean isBasicType() {
        return false;
    }

    protected final void crtf(StringBuilder s) {
        s.append('\n');
    }

    public void setExpand(boolean expand) {
        this.isExpand = expand;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        if (!isBasicType()) {
            if (isExpand) {
                ds.setColor(expandColor);
            } else {
                ds.setColor(Color.MAGENTA);
            }
        }

    }


    //[to make my own properties , into spannable infos]
    protected abstract void makeList(LinkedList linkedList);

    protected abstract void preBuildSpannables(StringBuilder stringBuilder, LinkedList<SpanableInfo> infosList);

    protected abstract void afterBuildSpannables(StringBuilder stringBuilder, LinkedList<SpanableInfo> infosList);

    public void setExpandColor(int color) {
        expandColor = color;
    }

    public void toast(String data) {
        Invalidate invalidate = parent.get();
        if (invalidate instanceof FullScreenPanel) {
            FullScreenPanel panel = (FullScreenPanel) invalidate;
            panel.showToast(data);
        }
    }

    public void setLevel(int lv) {
        level = lv + 1;
    }

    public boolean isNotFiltered() {
        return true;
    }


    public void reset() {
        isExpand = false;
    }

    public interface OnClickListener{
        void onClick(Info info);
    }
}
