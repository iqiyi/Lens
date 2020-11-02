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
package com.qiyi.lens.utils.configs;

import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;
import androidx.core.util.Pair;

import com.qiyi.lens.ui.viewinfo.IViewClickHandle;
import com.qiyi.lens.utils.DataPool;
import com.qiyi.lens.utils.event.EventBus;
import com.qiyi.lens.utils.iface.AbsObjectDescriptor;
import com.qiyi.lens.utils.iface.IObjectDescriptor;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DebugInfoConfig {
    private static DebugInfoConfig config = new DebugInfoConfig();

    private Class<? extends IViewClickHandle> viewClickHandle;
    private IObjectDescriptor defaultDescriptor;
    private List<Pair<String, WeakReference>> watchList = new LinkedList<>();
    private List<Pair<Field, WeakReference>> watchFieldList = new LinkedList<>();

    private DebugInfoConfig() {
    }

    public static DebugInfoConfig getInstance() {

        return config;
    }

    public void setViewClickhandle(Class<? extends IViewClickHandle> viewClickHandle) {
        this.viewClickHandle = viewClickHandle;
    }

    public Class<? extends IViewClickHandle> getViewClickHandle() {
        return viewClickHandle;
    }

    public void setDefaultObjectDescriptor(IObjectDescriptor descriptor) {
        this.defaultDescriptor = descriptor;
    }

    public void watchField(String fieldName, Object object) {
        if (object == null) {
            return;
        }
        Field field = getField(fieldName, object);
        watchFieldList.add(new Pair<>(field, new WeakReference(object)));
        EventBus.onDataArrived(null, DataPool.EVENT_WATCH_LIST_ADD);
    }

    @Nullable
    private Field getField(String fieldName, Object object) {
        Class clzz = object.getClass();
        Field field = null;
        while (clzz != Object.class) {
            try {
                field = clzz.getDeclaredField(fieldName);
                if (field != null) break;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                clzz = clzz.getSuperclass();
            }

        }
        return field;
    }

    public void watchObject(Object object) {
        if (object instanceof AbsObjectDescriptor) {
            AbsObjectDescriptor descripter = (AbsObjectDescriptor) object;
            watchObject(descripter.getTag(), descripter);
        } else {
            watchObject("", object);
        }
    }

    public void watchObject(String name, Object object) {
        if (hasWatched(name, object)) return;
        watchList.add(new Pair<String, WeakReference>(name, new WeakReference(object)));
        EventBus.onDataArrived(null, DataPool.EVENT_WATCH_LIST_ADD);
    }

    public boolean hasWatched(String name, Object object) {
        Field field = getField(name, object);
        if (field != null) {
            for (Iterator<Pair<Field, WeakReference>> it = watchFieldList.iterator(); it.hasNext(); ) {
                Pair<Field, WeakReference> pair = it.next();
                if (ObjectsCompat.equals(field, pair.first)
                        && ObjectsCompat.equals(object, pair.second.get())) {
                    return true;
                }
            }
        }

        for (Iterator<Pair<String, WeakReference>> it = watchList.iterator(); it.hasNext(); ) {
            Pair<String, WeakReference> pair = it.next();
            //【对象必须同名 ，而且是统一个对象才不添加】
            if (ObjectsCompat.equals(pair.first, name) && pair.second != null &&
                    ObjectsCompat.equals(pair.second.get(), object)) {
                return true;
            }
        }
        return false;
    }

    public void unwatch(Pair<?, WeakReference> entry) {
        if (entry.first instanceof Field) {
            for (Iterator<Pair<Field, WeakReference>> it = watchFieldList.iterator(); it.hasNext(); ) {
                Pair pair = it.next();
                if (pair == entry) {
                    it.remove();
                    break;
                }
            }
        } else {
            for (Iterator<Pair<String, WeakReference>> it = watchList.iterator(); it.hasNext(); ) {
                Pair pair = it.next();
                if (pair == entry) {
                    it.remove();
                    break;
                }
            }
        }
    }

    public List<Pair<?, WeakReference>> getWatchList() {
        List<Pair<?, WeakReference>> list = new LinkedList<>();
        list.addAll(watchList);
        list.addAll(watchFieldList);
        return list;
    }


    public int getWatchListSize() {
        return watchList.size() + watchFieldList.size();
    }


    public IObjectDescriptor getDefaultDescription() {
        return defaultDescriptor;
    }

}
