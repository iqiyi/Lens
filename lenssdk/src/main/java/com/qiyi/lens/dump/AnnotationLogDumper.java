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
package com.qiyi.lens.dump;

import androidx.annotation.RestrictTo;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于注解的 ILogDumper
 */
public class AnnotationLogDumper implements ILogDumper {
    private List<AnnotationDumpHelper> mAnnotationDumpHelpers = new ArrayList<>();
    private DumpResultHandler mDumpResultHandler;

    public AnnotationLogDumper(DumpResultHandler dumpResultHandler) {
        mDumpResultHandler = dumpResultHandler;
    }

    public static AnnotationLogDumper create(DumpResultHandler handler) {
        return new AnnotationLogDumper(handler);
    }

    public AnnotationLogDumper add(Class<? extends Annotation> anno, Object... dpRoot) {
        if (dpRoot != null) {
            AnnotationDumpHelper annotationDumpHelper = AnnotationDumpHelper.create(anno).addRoot(dpRoot);
            annotationDumpHelper.setDumpResultHandler(mDumpResultHandler);
            mAnnotationDumpHelpers.add(annotationDumpHelper);
        }
        return this;
    }


    public AnnotationLogDumper add(String itemName, Class<? extends Annotation> anno, Object... dpRoot) {
        if (dpRoot != null) {
            AnnotationDumpHelper annotationDumpHelper = AnnotationDumpHelper.create(itemName, anno).addRoot(dpRoot);
            annotationDumpHelper.setDumpResultHandler(mDumpResultHandler);
            mAnnotationDumpHelpers.add(annotationDumpHelper);
        }
        return this;
    }

    @Override
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public String dump() {
        StringBuilder sb = new StringBuilder();
        for (AnnotationDumpHelper annotationDumpHelper : mAnnotationDumpHelpers) {
            String dump = annotationDumpHelper.dump();
            sb.append(dump);
            sb.append('\n');
        }
        return sb.toString();
    }


    public String[] getDumpTags() {
        if (!mAnnotationDumpHelpers.isEmpty()) {
            int size = mAnnotationDumpHelpers.size();
            String ar[] = new String[size];
            int p = 0;
            for (AnnotationDumpHelper annotationDumpHelper : mAnnotationDumpHelpers) {
                ar[p++] = annotationDumpHelper.getDumpTag();
            }
            return ar;
        }
        return null;
    }

    @Override
    public String dump(int index) {
        if (index < 0 || index >= mAnnotationDumpHelpers.size()) {
            return "";
        }
        AnnotationDumpHelper helper = mAnnotationDumpHelpers.get(index);
        return helper.dump();
    }
}
