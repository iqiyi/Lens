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

import com.qiyi.lens.utils.LL;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FieldInfo extends Info {

    private static List<String> basicType = new LinkedList<>();
    private static List<String> basicBoxingType = new LinkedList<>();
    private static List<String> classFilters = new LinkedList<>();
    private static List<String> editableType = new LinkedList<>();
    private String name;
    private Type type;
    Object value;
    private int modifiers;

    static {
        basicType.add("float");
        basicType.add("int");
        basicType.add("long");
        basicType.add("double");
        basicType.add("short");
        basicType.add("boolean");
        basicType.add("byte");
        basicType.add("char");
        basicType.add(int[].class.getName());


        basicBoxingType.add(Integer.class.getName());
        basicBoxingType.add(Float.class.getName());
        basicBoxingType.add(Long.class.getName());
        basicBoxingType.add(Double.class.getName());
        basicBoxingType.add(Short.class.getName());
        basicBoxingType.add(Boolean.class.getName());
        basicBoxingType.add(Byte.class.getName());
        basicBoxingType.add(Character.class.getName());

        editableType.addAll(basicType);
        editableType.remove(editableType.size() - 1);
        editableType.add("java.lang.String");

        classFilters.add("android.");
        classFilters.add("dalvik.");
        classFilters.add("java.");
        classFilters.add("javax.");

    }


    SparseArray hashMap; //[used for field only , make sure not to to infinite loop]
    protected boolean isSimple;


    public FieldInfo(Object obj, SparseArray hashMap, Invalidate par) {
        super(par);
        if (obj != null) {
            name = obj.getClass().getSimpleName();
            value = obj;
            this.hashMap = hashMap;
        }
    }

    protected void preBuildSpannables(StringBuilder stringBuilder, LinkedList<SpanableInfo> infoList) {

    }

    protected void afterBuildSpannables(StringBuilder stringBuilder, LinkedList<SpanableInfo> infoList) {

    }

    public static boolean classFilter(String clsName) {
        if (clsName == null) return true;
        for (String s : classFilters) {
            if (clsName.startsWith(s)) {
                return false;
            }
        }
        return true;
    }

    private List<Field> getAllFields(Class c) {
        return getFields(c, false);
    }

    private List<Field> getFields(Class c, boolean filterFramework) {
        List<Field> fields = new LinkedList<>();
        for (; c != null; c = c.getSuperclass()) {
            if (classFilter(c.getName()) || !filterFramework) {
                try {
                    Collections.addAll(fields, c.getDeclaredFields());
                } catch (Throwable ignored) {
                }

            }
        }
        return fields;
    }

    protected FieldInfo makeFieldInfo(String field, Object src) {
        if (src != null) {

            Class cls = src.getClass();

            while (true) {
                try {
                    Field fld = cls.getDeclaredField(field);


                    if (fld != null) {
                        return ObjectFieldCollector.create(fld, src, hashMap, this);
                    } else {
                        return null;
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    if (cls == Object.class) {
                        return null;
                    } else {
                        cls = cls.getSuperclass();
                    }

                }
            }

        }
        return null;
    }

    public void makeList(LinkedList linkedList) {
        makeList(linkedList, false, true);
    }

    public void makeList(LinkedList linkedList, boolean allInherited, boolean filterDuplicated) {
        if (value == null) return;
        Object src = value;

        List<Field> flds = null;
        if (allInherited) {
            flds = getAllFields(src.getClass());
        } else {
            flds = getFields(src.getClass(), true);
        }
        List<Info> infos = new LinkedList<>();
        if (flds != null) {
            for (Field fld : flds) {
                String mdf = Modifier.toString(fld.getModifiers());
                LL.d("MM", mdf);
                if (fld.getName().startsWith("shadow$")) {
                    continue;
                }
                FieldInfo info = ObjectFieldCollector.create(fld, src, hashMap, this);
                info.setLevel(level + 1);
                if (filterDuplicated && info.verify()) {
                    if (linkedList != null && !isBasicType()) {
                        linkedList.add(info);
                    }
                    infos.add(info);
                } else if (!filterDuplicated) {
                    if (linkedList != null && !isBasicType()) {
                        linkedList.add(info);
                    }
                }

            }
        }
        this.list = infos;
    }

    public boolean verify() {

        int hash = 0;

        try {
            hash = value.hashCode();
        } catch (Exception e) {

        }

        if (value != null && hashMap.get(hash) == null) {
            hashMap.put(hash, value); //[mark as occupied , cut off loop ]
            return true;
        }
        return false;
    }

    /**
     * @param fld
     * @param src field of parent src:
     * @param hs
     */
    public FieldInfo(Field fld, Object src, SparseArray hs, Invalidate pa) {
        super(pa);
        hashMap = hs;
        fld.setAccessible(true);
        name = fld.getName();
        type = fld.getGenericType();
        try {
            value = fld.get(src);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        modifiers = fld.getModifiers();
    }

    public boolean isBasicType() {
        if (type != null) {
            if (basicType.contains(type.toString())) {
                return true;
            }
        } else if (value != null) {
            String cls = value.getClass().toString();
            if (basicBoxingType.contains(cls)) {
                return true;
            }
        } else if (value == null) {
            return true;
        }
        return false;
    }

    public boolean isEditable() {
        if (value != null) {
            String clazz = value.getClass().getName();
            if (editableType.contains(clazz)) {
                return true;
            }
        }
        if (type != null) {
            if (editableType.contains(type.toString())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        if (type != null) {
            return name + " = " + value + " : " + type;
        }
        return name + " " + value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    public int getModifiers() {
        return modifiers;
    }

    public String getSimpleName() {
        if (name != null && name.length() > 0) {
            int last = name.lastIndexOf('.');
            return name.substring(last + 1);
        }
        return "";
    }

    public String getReadableTypeName() {
        if (type == null) return "";
        // 处理数组
        switch (type.toString()) {
            case "class [I":
                return "int[]";
            case "class [Z":
                return "boolean[]";
            case "class [B":
                return "byte[]";
            case "class [C":
                return "char[]";
            case "class [S":
                return "short[]";
            case "class [J":
                return "long[]";
            case "class [F":
                return "float[]";
            case "class [D":
                return "double[]";
            default: {
                String name = type.toString();
                if (name.startsWith("class [L")) {
                    return removePackageName(name) + "[]";
                }
            }
        }
        return removePackageName(type.toString());
    }

    private String removePackageName(String name) {

        try {
            if (name != null && name.length() > 0) {
                int firstAngle = name.indexOf('<');
                // 考虑 java.util.ArrayList<java.lang.String> 的情况
                if (firstAngle != -1) {
                    int start = name.lastIndexOf('.', firstAngle);
                    return name.substring(start + 1);
                } else {
                    int lastDot = name.lastIndexOf('.');
                    return name.substring(lastDot + 1);
                }
            }
        } catch (Exception e) {
        }
        return name;
    }

    public void setAsSimple(boolean simple) {
        isSimple = simple;
    }


    public static boolean isBasicType(Object value) {
        if (value == null) {
            return true;
        }
        String type = value.getClass().getName();
        if (basicBoxingType.contains(type) || basicType.contains(type) || "java.lang.String".equals(type)) {
            return true;
        }

        return false;
    }

}
