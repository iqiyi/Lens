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

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;

import androidx.annotation.NonNull;

import com.qiyi.lens.utils.LensReflectionTool;
import com.qiyi.lens.utils.iface.IUIVerifyFactory;

public class ViewBackGround implements IJson {

    private Drawable background;
    private IUIVerifyFactory mFactory;

    public ViewBackGround(IUIVerifyFactory factory, Drawable drawable) {
        background = drawable;
        mFactory = factory;
    }

    @Override
    public String toJson() {
        JsonCompiler compiler = new JsonCompiler();
        compile(background, compiler);
        return compiler.value();
    }

    @Override
    public void toJson(StringBuilder builder) {
        builder.append(toJson());
    }

    private void compile(Drawable background, JsonCompiler compiler) {

        if (mFactory != null && mFactory.onJsonBuildDrawable(background, compiler)) {
            return;
        }

        if (background instanceof ColorDrawable) {
            int color = ((ColorDrawable) background).getColor();
            compiler.addPair("color", color);
            compiler.addPair("type", "color");
        } else if (background instanceof BitmapDrawable) {
            compiler.addPair("type", "image");
        } else if (background instanceof GradientDrawable) {

            Object mGradientState = LensReflectionTool.get().on(background)
                    .fieldName("mGradientState")
                    .get();

            if (mGradientState != null) {
                Object vars = LensReflectionTool.get().on(background)
                        .fieldName("mGradientColors")
                        .get();
                if (vars != null) {
                    compiler.addPair("type", "gradient");
                    int[] mGradientColors = (int[]) vars;
                    compiler.addPair("colorStops", mGradientColors);
                } else {
                    compiler.addPair("type", "color");
                    Object var = LensReflectionTool.fieldChain(null, mGradientState, new String[]{
                            "mSolidColors",
                            "mColors"
                    }, true);
                    if (var != null) {
                        int[] ar = (int[]) var;
                        if (ar.length > 0) {
                            compiler.addPair("color", ar[0]);
                        }
                    }

                }

            }


        } else if (background instanceof InsetDrawable) {
            InsetDrawable drawable = (InsetDrawable) background;
            background = (Drawable) LensReflectionTool.get()
                    .on(drawable)
                    .resolveParent(true)
                    .fieldName("mDrawable")
                    .get();
            if (background != null) {
                compile(background, compiler);
            }
        } else if (background instanceof StateListDrawable) {
            Drawable var = (Drawable) LensReflectionTool.get()
                    .on(background)
                    .fieldName("mCurrDrawable")
                    .get();
            if (var != null) {
                compile(var, compiler);
            }
        } else {
            String var = "unknown";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (background instanceof RippleDrawable) {
                    compiler.addPair("type", "ripple");
                    handleRipple((RippleDrawable) background, compiler);

                }
            }
        }
    }


    private void handleRipple(RippleDrawable rippleDrawable, @NonNull JsonCompiler compiler) {


        LensReflectionTool tool = LensReflectionTool.get();

        Object data = LensReflectionTool.fieldChain(tool, rippleDrawable, new String[]{
                "mLayerState",
                "mColor",
                "mColors"
        }, true);

        if (data != null) {
            int colors[] = (int[]) data;
            if (colors.length > 0) {
                compiler.addPair("ripple-press-color", colors[0]);
            }
        }


        Object d2 = LensReflectionTool.fieldChain(tool, rippleDrawable, new String[]{
                "mLayerState",
                "mChildren"
        }, true);

        if (d2 != null) {
            Object[] vars = (Object[]) d2;
            if (vars.length > 0) {
                Object var = vars[0];// content
                if (var != null) {
                    Drawable v = (Drawable) tool.on(var)
                            .fieldName("mDrawable")
                            .get();
                    JsonCompiler jsonCompiler = new JsonCompiler();
                    compile(v, jsonCompiler);
                    compiler.addPair("content", jsonCompiler);
                }

            }
        }


    }
}
