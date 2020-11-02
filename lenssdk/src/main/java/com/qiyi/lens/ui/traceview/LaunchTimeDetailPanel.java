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
package com.qiyi.lens.ui.traceview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.qiyi.lens.ui.FloatingPanel;
import com.qiyi.lens.ui.FullScreenPanel;
import com.qiyi.lens.ui.traceview.compare.SelectLaunchTimePanel;
import com.qiyi.lens.ui.traceview.compare.TimeStampUtilDao;
import com.qiyi.lens.ui.widget.DataViewLoader;
import com.qiyi.lens.ui.widget.ViewPagerTitleBinder;
import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.OSUtils;
import com.qiyi.lens.utils.TimeStampUtil;
import com.qiyi.lenssdk.R;

/**
 * 启动时间面板
 * a tab style view pager
 */
public class LaunchTimeDetailPanel extends FullScreenPanel implements View.OnClickListener {
    private TimeStampUtil data;

    public LaunchTimeDetailPanel(FloatingPanel panel) {
        super(panel);
        data = (TimeStampUtil) DataPool.obtain().getData(DataPool.DATA_TYPE_LAUNCH_TIME);
    }

    @Override
    public View onCreateView(ViewGroup group) {
        View root = inflateView(R.layout.lens_trace_view, group);
        ViewPager pager = root.findViewById(R.id.len_launch_time_pager);
        ViewGroup tank = root.findViewById(R.id.lens_time_bottom_bar);
        root.findViewById(R.id.lens_save).setOnClickListener(this);
        root.findViewById(R.id.lens_compare).setOnClickListener(this);
        TimeStampInfo detailInfo = data.buidStampInfo();
        pager.setAdapter(new DataAdapter(3, new DataViewLoader[]{
                new TimeGapPage(getContext(), detailInfo),
                new TimeStampPage(detailInfo),
                new ThreadInfoPage(detailInfo)
        }));

        new ViewPagerTitleBinder(pager, tank);
        return root;
    }

    @Override
    public void show() {
        if (data != null) {
            super.show();
        } else {
            Toast.makeText(context, "启动数据为空，无法展示面板", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(final View v) {
        if (OSUtils.isPreQ() && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "为了跨版本对比启动数据，请授予外部存储权限后重试", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }
        if (v.getId() == R.id.lens_save) {
            new TimeStampUtilDao(context).save(data, new TimeStampUtilDao.Callback<Void>() {
                @Override
                public void onResult(Void data) {
                    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
                    ((TextView) v).setText("已保存");
                    v.setEnabled(false);
                }
            });
        } else if (v.getId() == R.id.lens_compare) {
            new SelectLaunchTimePanel().show();
        }
    }

    static class DataAdapter extends PagerAdapter {

        int count;
        DataViewLoader[] mLoaders;

        DataAdapter(int c, DataViewLoader[] loaders) {
            this.count = c;
            mLoaders = loaders;
            if (loaders == null || loaders.length != count) {
                throw new IllegalArgumentException("loader count is not same with demanded where now is : " + (loaders == null ? "null " : loaders.length)
                        + "while demanded is " + (loaders == null ? "null" : loaders.length));
            }
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public boolean isViewFromObject(@Nullable View view, @Nullable Object object) {
            return view == object;
        }

        @Override
        public @NonNull
        Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = mLoaders[position].loadView(container);
            container.removeView(view);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
