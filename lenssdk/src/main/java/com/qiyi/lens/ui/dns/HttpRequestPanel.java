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
package com.qiyi.lens.ui.dns;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.dns.infos.RequestData;
import com.qiyi.lens.ui.dns.infos.UrlCollector;
import com.qiyi.lens.ui.dns.infos.UrlInfo;
import com.qiyi.lens.utils.LL;
import com.qiyi.lens.utils.LocalLinkMovementMethod;
import com.qiyi.lens.utils.configs.NetworkAnalyzeConfig;
import com.qiyi.lens.utils.event.DataCallBack;
import com.qiyi.lens.utils.event.EventBus;
import com.qiyi.lens.utils.reflect.Invalidate;
import com.qiyi.lens.utils.reflect.SpanableInfo;
import com.qiyi.lenssdk.R;

import java.util.LinkedList;

import static android.content.Context.CLIPBOARD_SERVICE;

public class HttpRequestPanel extends FullScreenPanel implements Invalidate, DataCallBack {
    private TextView display;
    private UrlCollector collector;
    private View searchAction;
    private EditText searchInfo;

    public HttpRequestPanel(FloatingPanel panel) {
        super(panel);
    }

    @Override
    public View onCreateView(ViewGroup viewGroup) {
        View content = inflateView(R.layout.lens_network_filter_panel, viewGroup);
        display = content.findViewById(R.id.network_filter_display);
        display.setMovementMethod(LocalLinkMovementMethod.getInstance());
        display.setLinksClickable(false);
        display.setClickable(false);
        display.setTextColor(Color.BLACK);
        display.setTextSize(18);
        collector = DNSSetting.getUrlConnector().build(collector, this);
        display.setText(makeSpannable());
        display.setBackgroundColor(Color.WHITE);


        searchInfo = (EditText) content.findViewById(R.id.search_box_edit_text);
        searchAction = content.findViewById(R.id.search_box_action_search);
        searchAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearch();

            }
        });


        content.findViewById(R.id.search_box_clear_search_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        searchInfo.setText("");
                        searchText("");
                    }
                });

            }
        });


        TextView actionText = content.findViewById(R.id.search_box_action_text);
        actionText.setText("清空");


        String mFilter = NetworkAnalyzeConfig.getInstance().getUrlFilter();
        if (mFilter != null && mFilter.length() > 0) {
            UrlInfo.setFilter(mFilter);
            searchInfo.setText(mFilter);
        }


        content.findViewById(R.id.search_box_action_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DNSSetting.getUrlConnector().clear();
                int size = DNSSetting.getUrlConnector().size();
                collector = null;
                DataPool.obtain().putData(DataPool.DATA_TYPE_NET_FILTER_SIZE, size);
                collector = DNSSetting.getUrlConnector().build(collector, HttpRequestPanel.this);
                invalidate();
            }
        });
        return content;
    }

    private void showUrlResult(RequestData data) {
        HttpResponsePanel panel = new HttpResponsePanel(data, null);
        panel.show();
    }

    @Override
    public void invalidate() {
        Spannable ds = makeSpannable();
        LL.d("datass: " + ds);
        display.setText(ds);
    }

    //[并不全量生成，点击再展开生成 ， 除非有watch 的数据。 设置初始展开]
    public Spannable makeSpannable() {
        if (collector != null) {
//            collector.setExpand(true);
            LinkedList<SpanableInfo> list = new LinkedList<>();
            StringBuilder builder = new StringBuilder();
            collector.makeSpannable(builder, list);
            builder.append("\n ");//[fix out side touch]
            //[make spannales]
            Spannable spannable = new SpannableStringBuilder(builder);
            while (!list.isEmpty()) {
                SpanableInfo info = list.pop();
                if (info.isClickable()) {
                    spannable.setSpan(info.clickSpan, info.star, info.end
                            , Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
            return spannable;

        }
        return new SpannableString("");
    }

    @Override
    public void onShow() {
        super.onShow();
        EventBus.registerEvent(this, DataPool.DATA_TYPE_NET_FILTER_SIZE);
        EventBus.registerEvent(this, DataPool.EVENT_CLICK_URL_TO_COPY);
        EventBus.registerEvent(this, DataPool.EVENT_CLICK_URL_TO_SHARE);
        EventBus.registerEvent(this, DataPool.EVENT_CLICK_URL_TO_DETAIL);
        EventBus.registerEvent(this, DataPool.EVENT_CLICK_URL_TO_ANALYSE);
    }

    public void onDismiss() {
        EventBus.unRegisterEvent(this, DataPool.DATA_TYPE_NET_FILTER_SIZE);
        EventBus.unRegisterEvent(this, DataPool.EVENT_CLICK_URL_TO_COPY);
        EventBus.unRegisterEvent(this, DataPool.EVENT_CLICK_URL_TO_SHARE);
        EventBus.unRegisterEvent(this, DataPool.EVENT_CLICK_URL_TO_DETAIL);
        EventBus.unRegisterEvent(this, DataPool.EVENT_CLICK_URL_TO_ANALYSE);
        getMainHandler().removeCallbacks(refresh);
    }

    @Override
    public void onDataArrived(Object data, int type) {
        switch (type) {
            case DataPool.DATA_TYPE_NET_FILTER_SIZE:
                getMainHandler().removeCallbacks(refresh);
                getMainHandler().postDelayed(refresh, 800);
                break;
            case DataPool.EVENT_CLICK_URL_TO_COPY:
                copyUrl((String) data);
                break;
            case DataPool.EVENT_CLICK_URL_TO_SHARE:
                shareUrl((String) data);
                break;
            case DataPool.EVENT_CLICK_URL_TO_DETAIL:
                showUrlResult((RequestData) data);
                return;
            case DataPool.EVENT_CLICK_URL_TO_ANALYSE:
                //[show url analyse panel]
                URLFormatPanel panel = new URLFormatPanel(null, (String) data);
                panel.show();

                break;
        }
    }

    private void shareUrl(String data) {
        shareText(getContext(), "", "", data);
    }

    /**
     * 分享文字内容
     *
     * @param dlgTitle 分享对话框标题
     * @param subject  主题
     * @param content  分享内容（文字）
     */
    private void shareText(Context context, String dlgTitle, String subject, String content) {
        if (context == null) {
            return;
        }
        if (content == null || "".equals(content)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        if (subject != null && !"".equals(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }

        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // 设置弹出框标题
        if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
            context.startActivity(Intent.createChooser(intent, dlgTitle));
        } else { // 系统默认标题
            context.startActivity(intent);
        }
    }

    private ClipboardManager myClipboard;
    private ClipData myClip;

    private void copyUrl(String data) {
        if (myClipboard == null) {
            myClipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
        }
        myClip = ClipData.newPlainText("text", data);
        myClipboard.setPrimaryClip(myClip);
    }

    private Runnable refresh = new Runnable() {
        @Override
        public void run() {
            if (isAdded) {
                collector = DNSSetting.getUrlConnector().build(collector, HttpRequestPanel.this);
                invalidate();
            }
        }
    };


    private void doSearch() {
        String key = searchInfo.getText().toString();
        if (key.length() == 0) {
            showToast("空的！");
        } else {
            searchText(key);
        }

    }

    private void searchText(String key) {

        UrlInfo.setFilter(key);
        collector.reset();
        invalidate();

    }


}
