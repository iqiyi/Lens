package com.qiyi.lens.ui.title;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiyi.lens.utils.ApplicationLifecycle;
import com.qiyi.lens.utils.UIUtils;
import com.qiyi.lens.utils.iface.IPanel;
import com.qiyi.lenssdk.R;

public class DefaultTitleBinder {

    private int mLatoutRes;
    private TextView mTitleView;
    private TextView mMetaView;
    private IPanel mPanel;
    private ViewGroup mHost;

    public DefaultTitleBinder(IPanel panel, ViewGroup viewGroup) {
        mLatoutRes = R.layout.lens_full_screen_title_bar;
        mPanel = panel;
        mHost = viewGroup;
    }

    public DefaultTitleBinder create() {
        View view = LayoutInflater.from(mHost.getContext())
                .inflate(mLatoutRes, mHost, false);
        if (view != null) {
            View back = view.findViewById(R.id.lens_title_back);
            mTitleView = view.findViewById(R.id.lens_title_info);
            mMetaView = view.findViewById(R.id.lens_title_meta_info);

            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPanel.dismiss();
                }
            });
            mHost.addView(view);
        }
        return this;
    }


    public DefaultTitleBinder title(String title) {
        if (mTitleView != null) {
            UIUtils.setText(mTitleView, title);
        }
        return this;
    }

    public DefaultTitleBinder meta(String meta) {
        if (mMetaView != null) {
            UIUtils.setText(mMetaView, meta);
        }
        return this;
    }

    /**
     * designed hard code height
     *
     * @return
     */
    public int bind() {
        Context context = ApplicationLifecycle.getInstance().getContext();
        return context.getResources().getDimensionPixelSize(R.dimen.lens_default_title_bar_height);
    }

    public void updateTitle(String title) {
        if (mTitleView != null) {
            mTitleView.setText(title == null ? "" : title);
        }
    }

    public void updateMeta(String meta) {
        if (mMetaView != null) {
            mMetaView.setText(meta == null ? "" : meta);
        }
    }
}
