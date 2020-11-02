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
package com.qiyi.lens.utils;

import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.ViewPager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

//[用于统计某个视图树的层级的工具]
public class ViewClassifyUtil {
    private int level = 0;
    private int viewCount;
    //    java.util.LinkedList<String > types = new LinkedList<>();
    private HashMap<String, Integer> typesMap = new HashMap<>();
    private java.util.LinkedList<ViewPager> pagers = new LinkedList<>();
    //    FlexibleIntArray array = new FlexibleIntArray();
    private Var[] vars;
    private View maxLevelView;

    public ViewClassifyUtil(View view) {
        if (view != null) {
            loop(view);
        }
    }

    private void loop(View view) {
        LinkedList<ViewLevel> list = new LinkedList<>();
        list.add(new ViewLevel(view, 1));
        View maxLevelView = null;

        while (!list.isEmpty()) {
            ViewLevel tmp = list.pop();
            viewCount++;
            if (tmp.level > level) {
                level = tmp.level; //[current max level]
                maxLevelView = tmp.view;
            }
            String name = tmp.view.getClass().toString();
            int var = getTypeCount(name);
            typesMap.put(name, var + 1);
//            array.indexAutoIncrease(n);
            if (tmp.view instanceof ViewGroup) {
//                level ++;

                ViewGroup group = (ViewGroup) tmp.view;

                if (group instanceof ViewPager) {
                    pagers.add((ViewPager) group);
                }

                int count = group.getChildCount();
                for (int i = 0; i < count; i++) {
                    View iw = group.getChildAt(i);
                    list.add(new ViewLevel(iw, tmp.level + 1));
                }
            }
        }
        this.maxLevelView = maxLevelView;
        prepareTypeSet();
    }

    private int getTypeCount(String name) {
        Object var = typesMap.get(name);
        if (var != null) {
            return (int) var;
        }
        return 0;
    }


    public int getViewLevel() {
        return level;
    }

    public int getViewCount() {
        return viewCount;
    }

    public int getTypesCount() {
        return typesMap.size();
    }

    public TypeInfo getTypeByIndex(int id) {
        String name = vars[id].name;
        int count = vars[id].count;
        return new TypeInfo(Utils.getSimpleClassName(name), count);
    }


    private void prepareTypeSet() {
        int size = typesMap.size();
        vars = new Var[size];
        Iterator<Map.Entry<String, Integer>> iterator = typesMap.entrySet().iterator();
        int p = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> it = iterator.next();
            vars[p++] = new Var(it.getKey(), it.getValue());
        }
    }


    public View getMaxLevelView() {
        return maxLevelView;
    }

    public static class TypeInfo {
        public String name;
        public int count;

        TypeInfo(String key, int var) {
            this.name = key;
            this.count = var;
        }
    }

    static class ViewLevel {
        View view;
        int level;

        ViewLevel(View view, int i) {
            this.view = view;
            this.level = i;
        }

    }

    static class Var {
        String name;
        int count;

        Var(String n, int c) {
            this.count = c;
            name = n;
        }

    }

    public LinkedList<ViewPager> getPagers() {
        return pagers;
    }
}
