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

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

public interface IUIVerifyFactory {
    //让客户端自行定义JSON 数据的实现
    void onJsonBuild(Activity activity, IJsonCompiler json);
    //让客户端自行定义View传递的内容
    void onJsonBuildView(View view, IJsonCompiler json);

    //客户端分析Drawable
    boolean onJsonBuildDrawable(Drawable drawable, IJsonCompiler jsonCompiler);

    // 创建页面
    View onCreateView(IPanel panel, Context context);

    //当数据处理完成后调用。客户端使用这个数据进行投递
    void onDataPrepared(String data, String filePath);

}
