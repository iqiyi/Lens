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
package com.qiyi.lens.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.qiyi.lens.obj.SPItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author WangTao
 * <p>
 * Date 2018-11-29
 */
public class SharedPreferencesHelper {

    private static final Object sInitLock = new int[0];
    private static Map<String, SharedPreferencesHelper> sPreferences = new HashMap<>();

    private ConcurrentMap<String, String> mCurrentSharedPreferences =
            new ConcurrentHashMap<>();
    private SharedPreferences mPref;
    private Vector<String> mDirty = new Vector<>();
    private Context mAppContext;

    //使用前须清除 mAllSharedPreferencesFiles.clear()
    private List<String> mAllSharedPreferencesFiles = new ArrayList<>();

    private List<SPItem> spItemList;

    public static SharedPreferencesHelper getInstance(Context context, String prefName) {
        synchronized (sInitLock) {
            if (sPreferences.get(prefName) == null) {
                try {
                    sPreferences.put(prefName, new SharedPreferencesHelper(context, prefName));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sPreferences.get(prefName);
    }

    private static volatile SharedPreferencesHelper instance;

    public static SharedPreferencesHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (SharedPreferencesHelper.class) {
                if (instance == null) {
                    instance = new SharedPreferencesHelper(context);
                }
            }
        }
        return instance;
    }

    private SharedPreferencesHelper(Context context) {
        mAppContext = context.getApplicationContext();
    }

    private SharedPreferencesHelper(Context context, String prefName) {
        mAppContext = context.getApplicationContext();
        initSharedPreferences(prefName);
    }


    public List<SPItem> getSharedPrefs(Context context) {
        final File prefs = new File(context.getApplicationInfo().dataDir, "shared_prefs");
        final String[] list = prefs.list();
        mAllSharedPreferencesFiles.clear();
        spItemList = new ArrayList<>();
        if (list != null) {
            for (String file : list) {
                String prefName = file.substring(0, file.lastIndexOf("."));
                mAllSharedPreferencesFiles.add(prefName);
                SharedPreferences sharedPreferences = mAppContext.getSharedPreferences(prefName, 0);
                Map<String, ?> map = sharedPreferences.getAll();
                if (map != null) {
                    for (Map.Entry<String, ?> entry : map.entrySet()) {
                        if (!mDirty.contains(entry.getKey())) {
                            SPItem item = new SPItem(file, entry.getKey(),
                                    String.valueOf(entry.getValue()));
                            spItemList.add(item);
                        }
                    }
                }
            }
        }
        return spItemList;
    }

    private String getPrefName(String name) {
        if (name != null && name.length() > 0) {
            int index = name.lastIndexOf(".");
            if (index > 0) {
                return name.substring(0, index);
            } else {
                return name;
            }
        }
        return "";
    }

    private void initSharedPreferences(final String prefName) {

        String _prefName = getPrefName(prefName);
        mPref = mAppContext.getSharedPreferences(_prefName, Context.MODE_PRIVATE);

        if (mPref != null) {
            Map<String, ?> map = mPref.getAll();
            if (map != null) {
                for (Map.Entry<String, ?> entry : map.entrySet()) {
                    if (!mDirty.contains(entry.getKey())) {
                        mCurrentSharedPreferences.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                }
            }
        }
    }

    private String doMigrate(String key) {
        // As the change history, like KEY_AD_TIMES is an individual file with multiple
        // data pairs, So it will not do migrate. Other case are for those item which
        // use the key as the file name, and we have confirm they do not need migrate
        return null;
    }

    public boolean hasKey(String key) {
        return mCurrentSharedPreferences.containsKey(key);
    }

    public String getString(String key, String defValue) {
        if (TextUtils.isEmpty(key)) {
            return defValue;
        }

        String temp = mCurrentSharedPreferences.get(key);
        // only check temp with null, due to data maybe ""
        if (temp != null) {
            return temp;
        } else if (mDirty.contains(key)) {
            // Has been removed.
            return defValue;
        } else {
            if (mPref != null) {
                if (mPref.contains(key)) {
                    temp = mPref.getString(key, defValue);
                    mCurrentSharedPreferences.put(key, temp);
                } else {
                    temp = doMigrate(key);
                    // Only check null, shouldn't check ""
                    if (temp == null) {
                        temp = defValue;
                    }
                }
                return temp;
            } else {
                return defValue;
            }
        }
    }

    public boolean getBoolean(String key, boolean defValue) {
        try {
            if (mPref != null) {
                return mPref.getBoolean(key, defValue);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return defValue;
    }

    public int getInt(String key, int defValue) {
        try {
            if (mPref != null) {
                return mPref.getInt(key, defValue);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return defValue;
    }

    public float getFloat(String key, float defValue) {
        float result = defValue;
        try {
            String value = getString(key, null);
            if (value != null) {
                result = Float.parseFloat(value);
            }
        } catch (NumberFormatException ignored) {
        } catch (ClassCastException e) {
            try {
                if (mPref != null) {
                    return mPref.getFloat(key, defValue);
                }
            } catch (Exception ex) {
                e.printStackTrace();
            }
            result = defValue;
        }
        return result;
    }

    public long getLong(String key, long defValue) {
        long result;
        try {
            result = Long.parseLong(getString(key, String.valueOf(defValue)));
        } catch (NumberFormatException nef) {
            result = defValue;
        } catch (ClassCastException e) {
            try {
                if (mPref != null) {
                    return mPref.getLong(key, defValue);
                }
            } catch (Exception ex) {
                e.printStackTrace();
            }
            result = defValue;
        }
        return result;
    }

    public String updateSharedPref(String fileName, String key, String value) {
        try {
            String prefName;
            int index = fileName.lastIndexOf(".");
            if (index > 0) {
                prefName = fileName.substring(0, fileName.lastIndexOf("."));
            } else {
                prefName = fileName;
            }

            SharedPreferences prefs = mAppContext.getSharedPreferences(prefName, Context.MODE_PRIVATE);
//            Object oldValue = prefs.getAll().get(key);
            SharedPreferences.Editor editor = prefs.edit();
//            fillEditor(editor, key, value, oldValue);
            editor.putString(key, value);
            editor.apply();
            mCurrentSharedPreferences.put(key, value);
            return null;
        } catch (Exception exp) {
            return exp.getMessage();
        }
    }

    public void updateSharedPref(String fileName, String key, int value) {
        try {
            String prefName;
            int index = fileName.lastIndexOf(".");
            if (index > 0) {
                prefName = fileName.substring(0, fileName.lastIndexOf("."));
            } else {
                prefName = fileName;
            }

            SharedPreferences prefs = mAppContext.getSharedPreferences(prefName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(key, value);
            editor.apply();
        } catch (Exception exp) {
            exp.getMessage();
        }
    }


    public void updateSharedPref(String fileName, String key, boolean value) {
        try {
            String prefName;
            int index = fileName.lastIndexOf(".");
            if (index > 0) {
                prefName = fileName.substring(0, fileName.lastIndexOf("."));
            } else {
                prefName = fileName;
            }
            SharedPreferences prefs = mAppContext.getSharedPreferences(prefName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(key, value);
            editor.apply();
        } catch (Exception exp) {
            exp.getMessage();
        }
    }

}
