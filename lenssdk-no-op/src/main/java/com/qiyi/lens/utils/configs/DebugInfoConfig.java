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
import com.qiyi.lens.ui.viewinfo.IViewClickHandle;
import com.qiyi.lens.utils.iface.IObjectDescriptor;
import java.lang.reflect.Field;
public class DebugInfoConfig {
    static DebugInfoConfig config = new DebugInfoConfig();

    private Class< ? extends IViewClickHandle> viewClickHandle;
    private IObjectDescriptor defaultDescriptor;
    private DebugInfoConfig(){}
    public static DebugInfoConfig getInstance(){

        return config;
    }

    public void setViewClickhandle(Class< ? extends IViewClickHandle> viewClickHandle){
        this.viewClickHandle = viewClickHandle;
    }
    public Class< ? extends IViewClickHandle> getViewClickHandle(){
        return viewClickHandle;
    }

    public void setDefaultObjectDescriptor(IObjectDescriptor descriptor) {
        this.defaultDescriptor = descriptor;
    }

    public void watchField(String fieldName, Object object) {
    }

    @Nullable
    private Field getField(String fieldName, Object object) {
        Field field = null;
        return field;
    }

    public void watchObject(Object object) {
    }

    public void watchObject(String name, Object object) {
    }

    public boolean hasWatched(String name, Object object) {
        return false;
    }


    public int getWatchListSize() {
        return 0;
    }

    public IObjectDescriptor getDefaultDescription(){
        return defaultDescriptor;
    }

}
