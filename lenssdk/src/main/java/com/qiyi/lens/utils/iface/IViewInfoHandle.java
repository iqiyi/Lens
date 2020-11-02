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
package com.qiyi.lens.utils.iface;

import android.view.View;

/**
 * 视图分析接口，设置到Lens 后， 将会在视图拾取选中的情况下回调
 */
public interface IViewInfoHandle {
    // 返回任意对象。 也可返回ObjectDescription 对象，增加自定义的对象描述信息
    Object[] onViewSelect(View selectedView, Object var1, int var2, int var3);

    void onViewAnalyse(Object o, int i, int i1, StringBuilder stringBuilder);

    // 返回任意对象。 也可返回ObjectDescription 对象，增加自定义的对象描述信息
    Object[] onViewAnalyse(Object view, Object value);

    // 当View 选中时候，可以向View Debug Action 中动态田间 按钮与执行事件。 Lens 将会在界面中露出这些按钮，当点击按钮后，将会执行对应的事件。
    void onViewDebug(ViewDebugActions viewDebugActions, View view);
}
