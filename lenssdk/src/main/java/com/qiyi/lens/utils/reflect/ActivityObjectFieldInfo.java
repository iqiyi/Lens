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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.qiyi.lens.utils.ViewClassifyUtil;
import com.qiyi.lens.utils.configs.ActivityInfoConfig;
import com.qiyi.lens.utils.iface.IFragmentHandle;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * 页面分析功能
 */
public class ActivityObjectFieldInfo extends FieldInfo {

    public ActivityObjectFieldInfo(Object obj, SparseArray hashMap, Invalidate par) {
        super(obj, hashMap, par);
        initHandle();
    }


    public ActivityObjectFieldInfo(Field fld, Object src, SparseArray hs
            , Invalidate pa) {
        super(fld, src, hs, pa);
        initHandle();
    }

    @Override
    public void makeSpannable(StringBuilder stringBuilder, LinkedList<SpanableInfo> spannableInfo) {
        super.makeSpannable(stringBuilder, spannableInfo);
    }

    @Override
    public void preBuildSpannables(StringBuilder stringBuilder, LinkedList<SpanableInfo> spanInfos) {
        if (value != null) {
            String space = makeSpace();
            stringBuilder.append("\n");
            stringBuilder.append(space);
            stringBuilder.append("当前有 " + (list == null ? 0 : list.size()) + " 个Fragments");
        }
    }


    @Override
    public void makeList(LinkedList linkedList) {

        if (list == null) {
            list = new LinkedList<>();
        } else {
            list.clear();
        }

        if (value instanceof FragmentActivity) {
            makeSupportFragments();
            return;
        }

        Activity activity = (Activity) value;

        List<Fragment> fragments = null;
        if (Build.VERSION.SDK_INT >= 26) {
            fragments = activity.getFragmentManager().getFragments();
        } else {
            FragmentManager fragmentManager = activity.getFragmentManager();
            try {
                Field field = fragmentManager.getClass().getDeclaredField("mAdded");
                field.setAccessible(true);
                try {
                    Object object = field.get(fragmentManager);
                    if (object instanceof List) {
                        fragments = (List<Fragment>) object;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }


        }
        if (fragments != null && !fragments.isEmpty()) {
//            this.list = new LinkedList<>();
            for (Fragment fragment : fragments) {
                FragmentInfo fragmentInfo = new FragmentInfo(fragment, hashMap, this);
                fragmentInfo.setUtil(util, handle);
                fragmentInfo.setLevel(level);
                list.add(fragmentInfo);
            }
        }

    }

    private void makeSupportFragments() {
        FragmentActivity fragmentActivity = (FragmentActivity) value;
        List<androidx.fragment.app.Fragment> fragments = fragmentActivity.getSupportFragmentManager().getFragments();
        if (!fragments.isEmpty()) {
//            this.fragmentsInfos = new LinkedList<>();
            for (androidx.fragment.app.Fragment fragment : fragments) {
                FragmentInfo fragmentInfo = new FragmentInfo(fragment, hashMap, this);
                fragmentInfo.setUtil(util, handle);
                fragmentInfo.setLevel(level);
                list.add(fragmentInfo);
            }
        }
    }


    public View getContentView() {
        if (value != null) {
            Activity activity = (Activity) value;
            return activity.findViewById(android.R.id.content);
        }
        return null;
    }


    @Override
    public void afterBuildSpannables(StringBuilder stringBuilder, LinkedList<SpanableInfo> infoList) {
        stringBuilder.append("\n视图信息：\n");
        stringBuilder.append("视图层级：\t");
        stringBuilder.append(util.getViewLevel());
        crtf(stringBuilder);
        stringBuilder.append("视图个数\t");
        stringBuilder.append(util.getViewCount());
        crtf(stringBuilder);
        stringBuilder.append("视图分类： 总共有");
        int count = util.getTypesCount();
        stringBuilder.append(count);
        stringBuilder.append("种类型的视图\n");
        stringBuilder.append("\n");
    }


    private ViewClassifyUtil util;

    public void setViewClassifyUtils(ViewClassifyUtil util) {
        this.util = util;
        //[fix null ptr , as this is set after list build]
        if (list != null) {
            for (Info info : list) {
                if (info instanceof FragmentInfo) {
                    ((FragmentInfo) info).setUtil(util, handle);
                }
            }
        }
    }

    public ViewClassifyUtil makeActivityBaseInfo(StringBuilder stringBuilder, ViewClassifyUtil util) {
        stringBuilder.append("界面信息：\n 当前界面：");
        stringBuilder.append(getSimpleName());
        crtf(stringBuilder);
        if (list != null && list.size() > 0) {
            stringBuilder.append("有如下");
            stringBuilder.append(list.size());
            stringBuilder.append("个Fragment");
            crtf(stringBuilder);
            for (Info info : list) {
                stringBuilder.append(((FieldInfo) info).getSimpleName());
                stringBuilder.append("<");
                Object value = ((FieldInfo) info).value;
                makeViewPagerFragments(util, stringBuilder, value);
                stringBuilder.append(">");
                //]
                crtf(stringBuilder);
            }
        }


        stringBuilder.append("视图信息：\n");
//        ViewClassifyUtil util = new ViewClassifyUtil(getContentView());
        stringBuilder.append("视图层级：\t");
        stringBuilder.append(util.getViewLevel());
        crtf(stringBuilder);
        stringBuilder.append("视图个数\t");
        stringBuilder.append(util.getViewCount());
        crtf(stringBuilder);
        stringBuilder.append("视图分类： 总共有");
        int count = util.getTypesCount();
        stringBuilder.append(count);
        stringBuilder.append("种类型的视图\n");

        stringBuilder.append("\n");
        //[]
        return util;
    }


    private View getFragmentRootView(Object value) {
        if (value instanceof Fragment) {
            Fragment fragment = (Fragment) value;
            return fragment.getView();
        } else if (value instanceof androidx.fragment.app.Fragment) {
            androidx.fragment.app.Fragment fragment = (androidx.fragment.app.Fragment) value;
            return fragment.getView();
        }

        return null;
    }


    private int getLevel(View view, ViewGroup parent) {

        int level = 0;
        while (view != parent) {

            ViewParent viewParent = view.getParent();

            if (viewParent instanceof ViewGroup) {
                level++;
                view = (View) viewParent;
            } else {
                return Integer.MAX_VALUE;
            }

        }

        return level;
    }

    private ViewPager findChildPager(ViewGroup group, LinkedList<ViewPager> pagers) {

        ViewPager thePager = null;
        if (pagers != null) {
            int minLevel = Integer.MAX_VALUE;

            for (ViewPager pager : pagers) {

                int level = getLevel(pager, group);
                if (level < minLevel) {
                    thePager = pager;
                    minLevel = level;
                }

            }

        }

        return thePager;

    }


    //[value is a fragment]
    //[如果这个fragment 中间含有 View pager 则继续寻找]
    private void makeViewPagerFragments(ViewClassifyUtil util, StringBuilder stringBuilder, Object value) {
        //[to do 嵌套的fragment 检查]
//        Object value = ((FeildInfo) info).value;
        LinkedList<ViewPager> pagers = util.getPagers();
        if (pagers == null || pagers.isEmpty()) {
            return;
        }


        while (value != null) {

            View view = getFragmentRootView(value);
            value = null;

            if (view instanceof ViewGroup) {

                ViewGroup viewGroup = (ViewGroup) view;
                ViewPager pager = findChildPager(viewGroup, pagers);
                if (pager != null) {

                    PagerAdapter adapter = pager.getAdapter();
//                    if (adapter instanceof FragmentStatePagerAdapter) {
//                    FragmentStatePagerAdapter fragmentStatePagerAdapter = (FragmentStatePagerAdapter) adapter;
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

                    if (fragment instanceof Fragment || fragment instanceof androidx.fragment.app.Fragment) {
                        stringBuilder.append("发现嵌套的fragment,在ViewPager的第 ");
                        stringBuilder.append(index);
                        stringBuilder.append(" 个位置");
                        stringBuilder.append("\n");
                        stringBuilder.append(fragment.getClass().getSimpleName());

                        if (handleClass != null && handle != null) {
                            stringBuilder.append("(");
                            handle.onFragmentAnalyse(fragment, stringBuilder);
                            stringBuilder.append(")");
                        }
                        stringBuilder.append("\n");

                        value = fragment;//getFragmentRootView(fragment);


                    }
                }

//                }
            }

        }


    }


    //[并不全量生成，点击再展开生成 ， 除非有watch 的数据。 设置初始展开]
    public Spannable makeSpannable() {
        LinkedList<SpanableInfo> list = new LinkedList<>();
        StringBuilder builder = new StringBuilder();
        makeSpannable(builder, list);
        builder.append("\n ");//[fix out side touch]
        //[make spannable]
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


    private IFragmentHandle handle;

    private void initHandle() {
        Class<? extends IFragmentHandle> handleClass = ActivityInfoConfig.getInstance().getFragClassHandle();
        if (handleClass != null) {
            try {
                handle = handleClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

}
