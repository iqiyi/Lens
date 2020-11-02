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
package com.qiyi.lens.ui.database;

import androidx.core.content.FileProvider;


public final class LensProvider extends FileProvider {

    private static LensProvider INSTANCE;

    private Databases databases;

    public LensProvider() {
        if (INSTANCE != null) {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean onCreate() {
        INSTANCE = this;
        databases = new Databases(getContext());
        return super.onCreate();
    }


    public static LensProvider get() {
        return INSTANCE;
    }

    public Databases getDatabases() {
        return databases;
    }

}
