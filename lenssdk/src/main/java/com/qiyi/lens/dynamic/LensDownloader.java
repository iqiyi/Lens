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
package com.qiyi.lens.dynamic;

import android.content.Context;
import android.os.Handler;

/*placeholder，保证 sdk 与 sdk-no-op 之间的切换*/
public class LensDownloader {
    public static LensDownloader get(Context context) {
        return new LensDownloader();
    }

    public void download(String url, String version) {
    }

    public static class OnResultCallback {

        public OnResultCallback() {
        }

        public OnResultCallback(Handler handler) {
        }

        public final void callOnStartCheck() {
        }

        public final void callOnNoUpdate() {
        }

        public final void callOnStartDownload(final String version, final String url) {
        }

        public final void callOnDownloadComplete() {
        }

        public final void callOnProgress(final long total, final long current) {
        }

        public final void callOnError(final Throwable e) {
        }

        protected void onStartCheck() {
        }

        protected void onNoUpdate() {
        }

        protected void onDownloadComplete() {
        }

        protected void onStartDownload(String version, String url) {
        }

        protected void onProgress(long total, long current) {
        }

        protected void onError(Throwable e) {
        }
    }
}
