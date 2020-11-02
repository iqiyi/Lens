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

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiyi.lens.ui.viewinfo.json.ViewBackGround;
import com.qiyi.lens.utils.LensConfig;
import com.qiyi.lens.utils.configs.ViewInfoConfig;
import com.qiyi.lens.utils.iface.IViewInfoHandle;
import com.qiyi.lens.utils.ReflectTool;

import java.lang.reflect.Field;
import java.util.LinkedList;

public class ViewFieldInfo extends FieldInfo {
    Object clp;//[layoutParams]
    boolean isListItem;
    private int listIndex;
    private AdapterInfo adapterInfo;
    private boolean isAdapterIndexEnabled;

    public ViewFieldInfo(Object obj, SparseArray hashMap, Invalidate pa) {
        super(obj, hashMap, pa);
    }

    public ViewFieldInfo(Field fld, Object src, SparseArray hs, Invalidate pa) {
        super(fld, src, hs, pa);
    }


    @Override
    public void preBuildSpannables(StringBuilder stringBuilder, LinkedList<SpanableInfo> infos) {
        View view = (View) value;
        String space = makeSpace();

        stringBuilder.append('\n');

        String vid = makeViewID(view);
        if (vid != null) {
            stringBuilder.append(space);
            stringBuilder.append("id = ");
            stringBuilder.append(vid);
            stringBuilder.append('\n');
        }


        stringBuilder.append(space);
        stringBuilder.append("width: ");
        stringBuilder.append(view.getWidth());
        stringBuilder.append("  height: ");
        stringBuilder.append(view.getHeight());

        stringBuilder.append('\n');
        stringBuilder.append(space);
        stringBuilder.append("屏幕中的位置");
        int ar[] = {0, 0};
        view.getLocationOnScreen(ar);
        stringBuilder.append('[');
        stringBuilder.append(ar[0]);
        stringBuilder.append(' ');
        stringBuilder.append(ar[1]);
        stringBuilder.append(' ');
        stringBuilder.append(ar[0] + view.getWidth());
        stringBuilder.append(' ');
        stringBuilder.append(ar[1] + view.getHeight());

        stringBuilder.append(']');


        stringBuilder.append('\n');
        stringBuilder.append(space);
        stringBuilder.append("padding: " + view.getPaddingLeft() + " " + view.getPaddingTop() + " " + view.getPaddingRight() + " " + view.getPaddingBottom());
        stringBuilder.append("\n");
        stringBuilder.append(space);
        stringBuilder.append("scrollX: " + view.getScrollX());
        stringBuilder.append("  scrollY " + view.getScrollY());
        stringBuilder.append("\n");

        stringBuilder.append(space);
        Drawable bg = view.getBackground();

        if (bg == null) {
            stringBuilder.append("background " + "null ");
        } else if (bg instanceof ColorDrawable) {
            ColorDrawable colorDrawable = (ColorDrawable) bg;
            stringBuilder.append("background color : #" + Integer.toHexString(colorDrawable.getColor()));
        } else {
            if (isSimple) {
                stringBuilder.append("background: ");
                makeDetailDrawableInfo(bg, stringBuilder);
            } else {
                stringBuilder.append("background " + bg.getClass().getSimpleName());
            }
        }
        stringBuilder.append("\n");

        if (view instanceof ViewGroup) {
            stringBuilder.append(space);
            stringBuilder.append("childCount" + ((ViewGroup) view).getChildCount());
            stringBuilder.append("\n");
        }

        if (isSimple) {
            if (clp == null) {
                clp = ReflectTool.getField(value, "mLayoutParams");
            }
            stringBuilder.append(space);
            stringBuilder.append(ViewLayoutParamsInfo.toString(clp));
        }

        stringBuilder.append(space);
        stringBuilder.append("Alpha: ");
        stringBuilder.append(view.getAlpha());
        stringBuilder.append('\n');

        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            float size = textView.getTextSize();
            stringBuilder.append(space);
            stringBuilder.append("textSize:" + size);
            stringBuilder.append('\n');
            stringBuilder.append(space);
            stringBuilder.append("textColor: " + Integer.toHexString(textView.getPaint().getColor()));
            stringBuilder.append('\n');

            if (!isSimple) {
                stringBuilder.append(space);
                if (Build.VERSION.SDK_INT >= 16) {
                    stringBuilder.append("maxLine:" + textView.getMaxLines());
                } else {
                    try {
                        stringBuilder.append("maxLine:" + ReflectTool.getField(textView, "mMaximum"));

                    } catch (Exception e) {
                    }

                }

            }

        }


        if (isListItem) {
            stringBuilder.append(space);
            stringBuilder.append("listItemIndex:" + listIndex);
            stringBuilder.append('\n');
        }


    }

    @Override
    public void makeList(LinkedList linkedList) {
        if (!isSimple) {
            super.makeList(linkedList);
        }


        FieldInfo fieldInfo = null;

        if (!isSimple) {
            Object clp = ReflectTool.getField(value, "mLayoutParams");
            if (clp != null) {
                fieldInfo = new ViewLayoutParamsInfo(clp, hashMap, this);
            }
        }

//        FieldInfo fieldInfo = makeFieldInfo("mLayoutParams",value);
        if (fieldInfo != null) {
            fieldInfo.setAsSimple(isSimple);
            if (list == null) list = new LinkedList<>();
            list.add(fieldInfo);
        }
        fieldInfo = makeFieldInfo("mChildren", value);
        if (fieldInfo != null) {
            fieldInfo.setAsSimple(isSimple);
            if (list == null) list = new LinkedList<>();
            list.add(fieldInfo);
        }


        //[make list of a adapter view]

        Class<? extends IViewInfoHandle> handle = ViewInfoConfig.getInstance().getViewInfoHandle();

        if (handle == null) { //[转入 CurrentView 中处理, 使用新的属性 view 面板来展示 对象 属性信息]

            this.adapterInfo = new AdapterInfo(this.value, this.isAdapterIndexEnabled);
            Object listItem = this.adapterInfo.getAdapterItem();
            if (listItem != null) {
                if (this.list == null) {
                    this.list = new LinkedList();
                }

                Info info = ObjectFieldCollector.create(listItem, this.hashMap, this);
                this.list.add(info);
                this.isListItem = true;
            }
        }


    }

    public void makeSpannable(StringBuilder stringBuilder, LinkedList<SpanableInfo> spanableInfos) {
        super.makeSpannable(stringBuilder, spanableInfos);
    }


    @Override
    public String toString() {
        String className = value.getClass().getSimpleName();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(className);
        stringBuilder.append(" ");
        View view = (View) value;
        if (view.getVisibility() != View.GONE) {
            if (view.getVisibility() == View.INVISIBLE) {
                stringBuilder.append("INVISIBLE");
            }
            stringBuilder.append('[');
            stringBuilder.append(view.getLeft() + " " + view.getTop() + "  " + view.getRight() + "  " + view.getBottom());
            stringBuilder.append(']');
        } else {
            stringBuilder.append("GONE");
        }


        return stringBuilder.toString();

    }

    public static String makeViewID(View view) {
        if (view != null) {
            int id = view.getId();
            if (id > 0) {
                Resources resources = view.getContext().getResources();
                if (resources != null) {
                    StringBuilder out = new StringBuilder();
                    try {
                        String pkgname;
                        switch (id & 0xff000000) {
                            case 0x7f000000:
                                pkgname = "app";
                                break;
                            case 0x01000000:
                                pkgname = "android";
                                break;
                            default:
                                pkgname = resources.getResourcePackageName(id);
                                break;
                        }
                        String typename = resources.getResourceTypeName(id);
                        String entryname = resources.getResourceEntryName(id);
                        out.append(" ");
                        out.append(pkgname);
                        out.append(":");
                        out.append(typename);
                        out.append("/");
                        out.append(entryname);
                        return out.toString();
                    } catch (Resources.NotFoundException e) {
                    }
                }
            }

        }

        return null;
    }


    private void makeDetailDrawableInfo(Drawable drawable, StringBuilder stringBuilder) {
        ViewBackGround viewBackGround = new ViewBackGround(LensConfig.getInstance().getUIVeryfyFactory(), drawable);
        viewBackGround.toJson(stringBuilder);
    }

    public void setAdapterIndexEnabled(boolean enabled) {
        this.isAdapterIndexEnabled = enabled;
    }

}
