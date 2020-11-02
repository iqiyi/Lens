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
package com.qiyi.lens;

import android.content.Context;

import com.qiyi.lens.dynamic.LensContext;
import com.qiyi.lenssdk.BuildConfig;

/*placeholder，保证 sdk 与 sdk-no-op 之间的切换*/
public class Lens {

    public static final String VERSION = BuildConfig.VERSION_NAME;

    public static void init(Context context, boolean debug) {
    }

    public static Context wrapContext(Context context) {
        return new LensContext(context, context.getResources());
    }

    public static void showManually(Context context) {
        LensUtil.showManually(context);
    }

    public static boolean isSDKMode() {
        return true;
    }

    public static boolean isDebug() {
        return false;
    }

    public static void addDownloadConfigUrl(String url) {
    }

    public static void addDownloadConfigUrl(int position, String url) {
    }

    public static void setPreferAbi(String abi) {

    }
}
