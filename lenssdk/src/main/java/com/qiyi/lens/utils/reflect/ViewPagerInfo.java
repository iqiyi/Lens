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

import android.app.Fragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.SparseArray;

import com.qiyi.lens.utils.ViewClassifyUtil;
import com.qiyi.lens.utils.configs.ActivityInfoConfig;
import com.qiyi.lens.utils.iface.IFragmentHandle;

import java.lang.reflect.Field;
import java.util.LinkedList;

public class ViewPagerInfo extends FieldInfo {
    private IFragmentHandle handle;
    private ViewClassifyUtil util;

    public ViewPagerInfo(Object obj, SparseArray hashMap, Invalidate par) {
        super(obj, hashMap, par);
    }

    public ViewPagerInfo(Field fld, Object src, SparseArray hs, Invalidate pa) {
        super(fld, src, hs, pa);
    }

    @Override// [make the child fragment of this pager]
    public void makeList(LinkedList linkedList, boolean allInherited, boolean filterDuplicated) {
        //[figure out current fragment]
//        linkedList.clear();
        if (list == null) {
            list = new LinkedList<>();
        } else {
            list.clear();
        }

        Object fragment = getCurrentFragment();
        FragmentInfo fri = new FragmentInfo(fragment, hashMap, this);
        fri.setUtil(util, handle);
        fri.setLevel(level);
        list.add(fri);

    }


    @Override
    public @NonNull
    String toString() {
        if (value != null) {
            ViewPager pager = (ViewPager) value;
            return pager.getClass().getSimpleName() + " index : " + pager.getCurrentItem();
        }
        return "";
    }

    private Object getCurrentFragment() {

        ViewPager pager = (ViewPager) value;

        PagerAdapter adapter = pager.getAdapter();
        int index = pager.getCurrentItem();

        Class<? extends IFragmentHandle> handleClass = ActivityInfoConfig.getInstance().getFragClassHandle();
        Object fragment = null;
        IFragmentHandle handle = null;
        if (handleClass != null) {
            try {
                handle = handleClass.newInstance();
                fragment = handle.getFragmentInstance(adapter, index);
//                                handle.onFragmentAnalyse(fragment, stringBuilder);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (fragment == null) {
            if (adapter instanceof FragmentStatePagerAdapter) {
                FragmentStatePagerAdapter pagerAdapter = (FragmentStatePagerAdapter) adapter;
                fragment = pagerAdapter.getItem(index);
            } else if (adapter instanceof FragmentPagerAdapter) {
                FragmentPagerAdapter pagerAdapter = (FragmentPagerAdapter) adapter;
                fragment = pagerAdapter.getItem(index);
            }
        }


        //handle.onFragmentAnalyse(fragment, stringBuilder);
        if (fragment instanceof Fragment || fragment instanceof androidx.fragment.app.Fragment) {
            return fragment;
        }

        return null;
    }


    public void setUtils(ViewClassifyUtil util, IFragmentHandle handle) {
        this.handle = handle;
        this.util = util;

    }
}
