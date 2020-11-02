package com.qiyi.lens.ui.analyze;

import android.view.View;
import android.view.ViewGroup;

import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.widget.tableView.TableView;
import com.qiyi.lens.utils.reflect.ObjectFieldCollector;
import com.qiyi.lenssdk.R;

/**
 * 展示视图分类汇总你数据.
 */
public class ActivityViewCategorizedInfoPanle extends FullScreenPanel {
    private ObjectFieldCollector.Binder mBinder;

    public ActivityViewCategorizedInfoPanle(FloatingPanel panel) {
        super(panel);
        setTitle(R.string.lens_ac_view_cate_title);
    }

    @Override
    public View onCreateView(ViewGroup viewGroup){
        // create panel view here
        TableView mTableView =  new TableView(getContext());
        mTableView.setLayoutParams(new ViewGroup.LayoutParams(-1,-2));
        mBinder.bindViewCategorizeInfo(mTableView);
        return mTableView;
    }

    public void setDataBinder(ObjectFieldCollector.Binder dataBinder){

        mBinder = dataBinder;
    }
}

