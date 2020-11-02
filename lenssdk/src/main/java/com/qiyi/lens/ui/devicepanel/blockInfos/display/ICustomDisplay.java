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
package com.qiyi.lens.ui.devicepanel.blockInfos.display;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 数据展示模块
 */
public interface ICustomDisplay {

    //创建自己的视图
    View createView(ViewGroup parent);

    // 返回用于展示数据的TextView
    TextView getDisplay();

    //  返回用于过滤数据的 tags
    String[] getFilterTags();

}
