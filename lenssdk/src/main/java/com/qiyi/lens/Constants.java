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
package com.qiyi.lens;

public class Constants {
    //  使用悬浮窗权限添加的window
    public static int PANEL_FLOAT_PANEL_WINDOW = 1;
    // 在没有悬浮窗权限的情况下，手机用add View 的方式添加
    public static int PANEL_FLOAT_PANEL_VIEW = 2;
    //【用于编辑全屏的面板】
    public static int PANEL_FULL_SCREEN_PANEL = 1 << 16;
    public static int PANEL_SELECT_VIEW_PANEL = PANEL_FULL_SCREEN_PANEL + 1;
    public static int PANEL_SELECT_VIEW_PANEL_WINDOW = PANEL_FULL_SCREEN_PANEL + 2;
    public static int PANEL_LOG_INFO_PANEL = PANEL_FULL_SCREEN_PANEL + 3;
}
