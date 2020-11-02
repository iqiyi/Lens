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

import android.util.SparseArray;

import java.lang.reflect.Field;
import java.util.LinkedList;

public class ArrayFieldInfo extends FieldInfo {
    public ArrayFieldInfo(Object obj, SparseArray hashMap, Invalidate par) {
        super(obj, hashMap, par);
    }

    public ArrayFieldInfo(Field fld, Object src, SparseArray hs, Invalidate pa) {
        super(fld, src, hs, pa);
    }

    @Override
    public void preBuildSpannables(StringBuilder stringBuilder, LinkedList<SpanableInfo> infosList) {
        if (value != null) {
            stringBuilder.append("\n");
            String space = makeSpace();
            stringBuilder.append(space);
            stringBuilder.append(value.getClass());
            stringBuilder.append(" ");
            Object[] da = (Object[]) value;
            stringBuilder.append(da.length);
            stringBuilder.append("\n");
        }
    }

    @Override
    public void makeList(LinkedList linkedList) {
        if (value != null) {

            Object[] data = (Object[]) value;
            int size = data.length;
            if (list == null) {
                list = new LinkedList<>();
            } else {
                list.clear();
            }

            for (Object datum : data) {
                if (datum != null) {
                    FieldInfo info = ObjectFieldCollector.create(datum, hashMap, this);
                    info.setAsSimple(isSimple);
                    list.add(info);
                }
            }

        }
    }


}
