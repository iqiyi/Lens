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

import com.qiyi.lens.ConfigHolder;
import com.qiyi.lens.utils.iface.IViewInfoHandle;

public class ViewInfoConfig {
    Class<? extends IViewInfoHandle> fragClass;
    private static ViewInfoConfig infoConfig = new ViewInfoConfig();

    public static ViewInfoConfig getInstance() {
        return infoConfig;
    }

    private ViewInfoConfig() {
    }

    public void setViewInfoHandler(Class<? extends IViewInfoHandle> fragmentHandler) {
        this.fragClass = fragmentHandler;
        ConfigHolder.viewInfoHandler = fragmentHandler;
    }

    public Class<? extends IViewInfoHandle> getViewInfoHandle() {
        return this.fragClass;
    }
}
