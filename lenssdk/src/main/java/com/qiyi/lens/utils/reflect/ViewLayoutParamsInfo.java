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
package com.qiyi.lens.utils.reflect;

import android.util.SparseArray;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.lang.reflect.Field;

public class ViewLayoutParamsInfo extends FieldInfo {
    public ViewLayoutParamsInfo(Object obj, SparseArray hashMap, Invalidate par) {
        super(obj, hashMap, par);
    }

    public ViewLayoutParamsInfo(Field fld, Object src, SparseArray hs, Invalidate pa) {
        super(fld, src, hs, pa);
    }


    @Override
    public String toString() {
        return toString(value);
    }


    private static void parseGravity(int gra, StringBuilder sb) {
        switch (gra) {
            case Gravity.LEFT:
                sb.append("LEFT");
                break;
            case Gravity.RIGHT:
                sb.append("RIGHT");
                break;
            case Gravity.TOP:
                sb.append("TOP");
                break;
            case Gravity.BOTTOM:
                sb.append("BOTTOM");
                break;
            case Gravity.CENTER:
                sb.append("CENTER");
                break;
            case Gravity.CENTER_HORIZONTAL:
                sb.append("CENTER_HORIZONTAL");
                break;
            case Gravity.CENTER_VERTICAL:
                sb.append("CENTER_VERTICAL");
                break;


        }

    }


    public static String toString(Object value) {
        if (value != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.getClass().getSimpleName());
            ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) value;
            sb.append('[');

            parseLayout(sb, lp.width);
            sb.append(' ');
            parseLayout(sb, lp.height);

            sb.append("]");


            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                sb.append("margin :[");
                sb.append(mlp.leftMargin);
                sb.append(' ');
                sb.append(mlp.topMargin);
                sb.append(' ');
                sb.append(mlp.rightMargin);
                sb.append(' ');
                sb.append(mlp.bottomMargin);
                sb.append(']');
            }

            if (lp instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) lp;
                parseGravity(llp.gravity, sb);
            }

            if (lp instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) lp;
                parseGravity(flp.gravity, sb);
            }

            return sb.toString();

        }

        return "null";
    }


    private static void parseLayout(StringBuilder sb, int param) {
        if (param == ViewGroup.LayoutParams.MATCH_PARENT) {
            sb.append("MATCH ");
        } else if (param == ViewGroup.LayoutParams.WRAP_CONTENT) {
            sb.append("WRAP");
        } else {
            sb.append(param);
        }
    }


}
