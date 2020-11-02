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
package com.qiyi.lens.obj;

import java.io.File;
import java.util.Map;

/**
 * used for SharedPreference key value in model : SP
 */
public class SPItem {

    public String key;
    public String value;
    public String newValue; //用于记录编辑后的值
    public String fileName;
    public File descriptor;
    public Map<String, String> data;

    public SPItem(String name, String key, String value) {
        this.fileName = name;
        this.key = key;
        this.value = value;
    }

}
