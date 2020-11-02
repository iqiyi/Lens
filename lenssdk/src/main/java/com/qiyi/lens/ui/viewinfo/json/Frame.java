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
package com.qiyi.lens.ui.viewinfo.json;

import android.graphics.Rect;

public class Frame implements IJson {
    private Rect rect;

    public Frame(Rect rect) {
        this.rect = rect;
    }

    @Override
    public String toJson() {
        JsonCompiler compiler = new JsonCompiler();
        compiler.addPair("x", rect.left);
        compiler.addPair("y", rect.top);
        compiler.addPair("width", rect.width());
        compiler.addPair("height", rect.height());
        return compiler.value();
    }

    @Override
    public void toJson(StringBuilder builder) {
        builder.append(toJson());
    }
}
