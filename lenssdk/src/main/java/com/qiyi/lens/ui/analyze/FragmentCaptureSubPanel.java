package com.qiyi.lens.ui.analyze;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiyi.lens.ui.viewtree.ViewDraw;
import com.qiyi.lens.ui.widget.SubPanelView;
import com.qiyi.lens.utils.reflect.FragmentInfo;
import com.qiyi.lenssdk.R;

/**
 * 模块： 页面分析
 * 功能： 在页面分析中，对抓取到的 Fragment 弹出 sub panel；并提供展示
 * 「Fragment 对应的View」 的功能入口。
 */
public class FragmentCaptureSubPanel extends SubPanelView<FragmentInfo> {

    private FragmentInfo mFragmentInfo;
    private TextView mTextView;
    private boolean isAttached;

    public FragmentCaptureSubPanel(ViewGroup root) {
        super(root);
    }

    private void doAttach() {
        attachView(R.layout.lens_ac_ana_fragment_info);
        mTextView = (TextView) findViewById(R.id.lens_ac_ana_fragment_info_data);
        isAttached = true;
    }

    @Override
    protected void loadContentView(FragmentInfo value) {
        mFragmentInfo = value;
        if (mFragmentInfo != null) {
            if (!isAttached) {
                doAttach();
            }
            bindData();
        }
    }

    private void bindData(){
        mTextView.setText(mFragmentInfo.getName());
        ViewDraw viewDraw = (ViewDraw) findViewById(R.id.lens_ac_ana_fragment_info_view);
        View fragmentRootView = mFragmentInfo.getFragmentRootView();
        if (fragmentRootView != null && viewDraw != null) {
            viewDraw.setRef(fragmentRootView);
            viewDraw.select(fragmentRootView);
        }
    }


}

