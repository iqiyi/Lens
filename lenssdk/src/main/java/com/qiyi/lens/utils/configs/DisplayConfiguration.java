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

import com.qiyi.lens.ui.devicepanel.blockInfos.display.ICustomDisplay;

public class DisplayConfiguration {

    private int displayHeight;
    private int refreshDuration = 1000;
    private static DisplayConfiguration config;
    private Class<? extends ICustomDisplay> displayClass;

    public static DisplayConfiguration obtain() {
        if (config == null) {
            config = new DisplayConfiguration();
        }
        return config;
    }

    public DisplayConfiguration setDisplayHeight(int height) {
        this.displayHeight = height;
        return this;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }


    public DisplayConfiguration setRefreshDuration(int duration) {
        this.refreshDuration = duration;
        return this;
    }

    public int getRefreshDuration() {
        return refreshDuration;
    }

    public DisplayConfiguration setCustomDisplay(Class<? extends ICustomDisplay> clss) {
        displayClass = clss;
        return this;
    }

    public ICustomDisplay getCustomDisplay() {
        if (displayClass != null) {
            try {
                return displayClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
