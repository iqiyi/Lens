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

/**
 * 支持页面分析：
 * Fragment 分析
 */
public interface IFragmentHandle {
    // 往Fragment 中 添加定制的识别内容
    void onFragmentAnalyse(Object fragment, StringBuilder stringBuilder);

    //从Fragment Adapter 中返回对应的Fragment 对象。（尽量必要重新创建 ）
    Object getFragmentInstance(Object adapter, int index);
}
