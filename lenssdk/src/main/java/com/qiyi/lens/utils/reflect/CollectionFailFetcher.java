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

import com.qiyi.lens.utils.iface.IViewInfoHandle;
import com.qiyi.lens.utils.iface.ObjectDescription;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CollectionFailFetcher {

    private Object value;

    public CollectionFailFetcher(Object value) {
        this.value = value;

    }

    public Object[] getObjectSizeAndIndexAt(IViewInfoHandle handle, Object view, int size, int index) {

        Class cls = value.getClass();
        List data = new LinkedList();
        while (cls != Object.class && cls != null) {

            Field[] flds = cls.getDeclaredFields();
            for (Field field : flds) {
                try {
                    field.setAccessible(true);
                    Object object = field.get(value);
                    if (object instanceof List) {
                        List list = (List) object;
                        if (list.size() == size) {
                            Object indexValue = list.get(index);
                            data.add(indexValue);

                            if (handle != null) {
                                Object[] vars = handle.onViewAnalyse(view, indexValue);
                                if (vars != null) {
                                    Collections.addAll(data, vars);
                                }
                            }

                        }

                    } else if (object instanceof Object[]) {
                        Object[] array = (Object[]) object;
                        if (array.length == size) {
                            data.add(new ObjectDescription(array[index],"index " + index));
                            if (handle != null) {
                                Object[] vars = handle.onViewAnalyse(view, array[index]);
                                if (vars != null) {
                                    data.addAll(Arrays.asList(vars));
                                }
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return null;
                }

            }

            cls = cls.getSuperclass();

        }


        if (!data.isEmpty()) {
            Object[] value = new Object[data.size()];
            data.toArray(value);
            return value;
        }

        return null;
    }
}
