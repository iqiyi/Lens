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
package com.qiyi.lens.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.qiyi.lens.utils.iface.IJsonCompiler;
import com.qiyi.lens.utils.iface.IPanel;
import com.qiyi.lens.utils.iface.IUIVerifyFactory;

public class UIVerify implements IUIVerifyFactory {
    @Override
    public void onJsonBuild(Activity activity, IJsonCompiler json) {
        if (activity != null) {
            json.addPair("activity", activity.getClass().getSimpleName());
            View view = activity.findViewById(android.R.id.content);
            if (view != null) {
                json.addPair("width", view.getWidth());
                json.addPair("height", view.getHeight());
            }
        }

    }

    @Override
    public void onJsonBuildView(View view, IJsonCompiler json) {

    }

    @Override
    public boolean onJsonBuildDrawable(Drawable drawable, IJsonCompiler jsonCompiler) {

        return false;
    }

    @Override
    public View onCreateView(IPanel panel, Context context) {
        View view = new View(context);

        view.setBackgroundColor(Color.WHITE);
        return view;
    }

    // call set data to Server
    @Override
    public void onDataPrepared( String data, String filePath) {

    }
}
