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
package com.qiyi.lens.demo.dump;

import android.util.Log;

import com.qiyi.lens.demo.LensApp;
import com.qiyi.lens.demo.StaticDump;
import com.qiyi.lens.dump.AnnotationLogDumper;
import com.qiyi.lens.dump.DumpResultHandler;
import com.qiyi.lens.dump.ILogDumper;
import com.qiyi.lens.dump.ILogDumperFactory;

import org.qiyi.basecore.taskmanager.TaskManager;
import org.qiyi.basecore.taskmanager.TaskRecorder;

import java.lang.annotation.Annotation;

/**
 *  Lens data dump 对接入口
 */
public class MyDumpFactory implements ILogDumperFactory, DumpResultHandler {
    @Override
    public ILogDumper create() {
        return AnnotationLogDumper.create(this)
                .add(Dump.class, LensApp.getInstance(), StaticDump.class)
                .add("TM",TMDump.class, TaskManager.getInstance(), TaskRecorder.class);
    }

    @Override
    public String onResult(Class<? extends Annotation> anno, String lensLog, String dumpLog) {
        Log.d("lens-dump", dumpLog);
        return dumpLog;
    }
}
