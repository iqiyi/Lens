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
package com.qiyi.lens.ui.abtest;

import android.content.Context;

import com.qiyi.lens.utils.ApplicationLifecycle;
import com.qiyi.lens.utils.SharedPreferencesFactory;
import com.qiyi.lens.utils.SharedPreferencesHelper;

public class Value {
    private int _type;
    private int _size;
    private Object _value;
    private int _index = -1;
    private final String SP_NAME = "lens_sp";
    private String _spName = SP_NAME;//[used for sp set]
    public final static int TYPE_INT = 0,
            TYPE_BOOLEAN = 1,
            TYPE_STRING = 2;


    public Value(Object value, int type, int size) {
        _value = value;
        _size = size;
        _type = type;
    }

    //[if is same value & type]
    public boolean isSameKind(Object value, int type, int size) {
        if (_type == type && _size == size) {
            //[check value]
            if (type == TYPE_INT) {
                return intEqualse((int[]) _value, (int[]) value, size);
            } else if (type == TYPE_STRING) {
                return stringEquals((String[]) _value, (String[]) value, size);
            }
            return true;
        }
        return false;
    }

    private boolean intEqualse(int[] ar, int[] br, int lens) {
        int p = 0;
        while (p < lens) {
            if (ar[p] != br[p]) {
                return false;
            }
            p++;
        }
        return true;
    }

    //[if a is null , then b must be null ,a === b, the return true]
    private boolean stringEquals(String a, String b) {
        return a != null && a.equals(b) || a == null && b == null;
    }

    private boolean stringEquals(String[] ar, String[] br, int lens) {
        int p = 0;
        while (p < lens) {
            if (stringEquals(ar[p], br[p])) {
                return false;
            }
            p++;
        }
        return true;
    }

    public int getIndex(String key) {
        if (_index == -1) {
            Context context = ApplicationLifecycle.getInstance().getContext();
            if (context != null) {

                if (_type == TYPE_BOOLEAN) {

                    boolean bl = SharedPreferencesFactory.getBoolean(context, key, false, _spName);
                    _index = bl ? 1 : 0;

                } else {
                    _index = SharedPreferencesFactory.getInt(context, key, 0, SP_NAME);
                }


            }
        }

        if (_index < 0) {
            _index = 0;
        }

        return _index;
    }

    public void setIndex(String key, int id) {
        _index = id;
        if (_type == TYPE_BOOLEAN) {
            SharedPreferencesHelper.getInstance(ApplicationLifecycle.getInstance().getContext(), SP_NAME).updateSharedPref(_spName, key, id == 1);
        } else {
            SharedPreferencesHelper.getInstance(ApplicationLifecycle.getInstance().getContext(), SP_NAME).updateSharedPref(SP_NAME, key, id);
        }


    }

    public void setIndex(int index) {
        _index = index;
    }

    //[do not check Type & let it crash]
    public int getInt(String key) {
        int index = getIndex(key);
        if (index < _size) {
            return ((int[]) _value)[index];
        }
        return 0;
    }

    //[0 false : 1 true]
    public boolean getBoolean(String key) {
        int index = getIndex(key);
        return index == 1;
    }


    public String getString(String key) {
        int index = getIndex(key);
        String[] args = (String[]) _value;
        if (index < _size) {
            return args[index];
        }
        return "";
    }


    public String getValue(String key) {
        if (_value == null || _size == 0) {
            //[only for int & String]
            String var = "";
            Context context = ApplicationLifecycle.getInstance().getContext();
            if (context != null) {
                if (_type == TYPE_STRING) {
                    var = SharedPreferencesFactory.getString(context, key, "", _spName);
                } else if (_type == TYPE_INT) {
                    var = "" + SharedPreferencesFactory.getInt(context, key, 0, _spName);
                } else {
                    var = "" + SharedPreferencesFactory.getBoolean(context, key, false, _spName);
                }
            }
            return var;
        } else if (_type == TYPE_BOOLEAN) {
            return getBoolean(key) + "";
        } else if (_type == TYPE_STRING) {
            return getString(key) + "";
        } else if (_type == TYPE_INT) {
            return getIndex(key) + "";
        }
        return "";
    }


    //[只由 非可选的类型去调用]
    public void setValue(String key, String value) {
        if (_value == null || _size == 0) {
            //[only for int & String]
            String var = "";
            Context context = ApplicationLifecycle.getInstance().getContext();
            if (context != null) {
                if (_type == TYPE_STRING) {
                    SharedPreferencesHelper.getInstance(context, _spName).updateSharedPref(_spName, key, value);
                } else if (_type == TYPE_INT) {
                    int v;
                    try {
                        v = Integer.parseInt(value);
                    } catch (Exception e) {
                        v = 0;
                    }
                    SharedPreferencesHelper.getInstance(context, _spName).updateSharedPref(_spName, key, v);
                } else if (_type == TYPE_BOOLEAN) {
                    boolean b;
                    try {
                        b = Boolean.parseBoolean(value);
                    } catch (Exception e) {
                        b = false;
                    }
                    SharedPreferencesHelper.getInstance(context, _spName).updateSharedPref(_spName, key, b);
                }
            }
        }
    }

    public boolean isSelectableValue() {
        return _value != null && _size > 1;
    }

    public int size() {
        return _size;
    }


    public String[] toContentVars() {
        if (_size > 0) {
            if (_type == TYPE_STRING) {
                return (String[]) _value;
            } else {

                String[] ss = new String[_size];
                if (_type == TYPE_BOOLEAN) {
                    boolean[] bs = (boolean[]) _value;
                    ss[0] = "" + bs[0];
                    ss[1] = "" + bs[1];
                } else {

                    int[] bs = (int[]) _value;
                    int p = 0;
                    while (p < _size) {
                        ss[p] = "" + bs[p];
                        p++;
                    }
                }

                return ss;
            }

        }
        return null;

    }

    public boolean isNumberType() {
        return _type == TYPE_INT;
    }


    public void setSPName(String name) {
        _spName = name;
    }

    public String getSPName() {
        return _spName;
    }

}
