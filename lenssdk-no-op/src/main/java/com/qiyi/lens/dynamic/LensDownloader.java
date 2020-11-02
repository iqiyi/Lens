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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

import com.qiyi.lens.Lens;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 负责下载 Lens 插件
 * <p>
 * 步骤<br/>
 * 1. 获取 dementor lens-xxx.json 配置文件解析<br/>
 * 2. 根据配置文件检查版本号，必要时下发
 * <p>
 * 此外，还暴露一个 download 接口可以直接下载指定版本与 url 的插件
 */
public class LensDownloader {
    @SuppressLint("StaticFieldLeak")
    private volatile static LensDownloader sLensDownloader;
    private ExecutorService mIoPool = Executors.newSingleThreadExecutor();
    private Context mContext;

    private LensDownloader(Context context) {
        mContext = context;
    }

    public static LensDownloader get(Context context) {
        if (sLensDownloader == null) {
            synchronized (LensDownloader.class) {
                if (sLensDownloader == null) {
                    sLensDownloader = new LensDownloader(context);
                }
            }
        }
        return sLensDownloader;
    }

    public void check() {
        check(new OnResultCallback());
    }

    public void check(final OnResultCallback cb) {
        mIoPool.submit(new Runnable() {
            @Override
            public void run() {
                List<String> remoteConfigUrls = Lens.getDownloadConfigUrls();
                if (remoteConfigUrls == null || remoteConfigUrls.isEmpty()) {
                    return;
                }
                for (String configUrl : remoteConfigUrls) {
                    try {
                        cb.callOnStartCheck();
                        // 获取 json config 文件
                        String json = IoUtils.readURL(configUrl);
                        JSONObject jsonObject = new JSONObject(json);
                        JSONObject data = jsonObject.getJSONObject("data");
                        String remoteVersion = data.getString("version");
                        String lastVersion = Lens.readVersion(mContext, null);
                        // 只有当远程的 version 比之前下载的版本大，才需要下载
                        if (compareVersion(remoteVersion, lastVersion) <= 0) {
                            cb.callOnNoUpdate();
                            break;
                        }
                        String url = data.getString("url");
                        download(url, remoteVersion, cb);
                        break;
                    } catch (Throwable e) {
                        e.printStackTrace();
                        cb.callOnError(e);
                    }
                }
            }
        });
    }

    private void download(String url, String remoteVersion, OnResultCallback cb) throws IOException {
        cb.callOnStartDownload(remoteVersion, url);
        File out;
        if (Lens.inCompatibleMode(remoteVersion)) {
            out = Lens.getCompatiblePluginFile(mContext);
        } else {
            out = Lens.getPluginFile(mContext, remoteVersion);
        }
        IoUtils.saveURL(url, out);
        Lens.saveVersion(mContext, remoteVersion);
        cb.callOnDownloadComplete();
    }

    /**
     * 直接下载指定版本与地址的 lens 插件
     *
     * @param url     path
     * @param version version
     */
    public void downloadDirect(String url, String version) {
        try {
            download(url, version, new OnResultCallback());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int compareVersion(String v1, String v2) {
        if (v1 == null && v2 == null) {
            return 0;
        } else if (v1 == null) {
            return -1;
        } else if (v2 == null) {
            return 1;
        } else if (v1.equals(v2)) {
            return 0;
        }

        String[] versions1 = v1.split("\\.");
        String[] versions2 = v2.split("\\.");
        int index = 0;
        int minLen = Math.min(versions1.length, versions2.length);
        int result;
        int ai, bi;
        while (index < minLen) {
            try {
                // compare as number. 2.14 > 2.2
                ai = Integer.parseInt(versions1[index]);
                bi = Integer.parseInt(versions2[index]);
                result = ai - bi;
            } catch (NumberFormatException e) {
                // compare as String if not number.
                result = versions1[index].compareTo(versions2[index]);
            }
            if (result != 0) {
                return result;
            }
            index++;
        }
        // a contains b or vice versa... longer is greater
        return versions1.length - versions2.length;
    }

    public static class OnResultCallback {
        private Handler mHandler;

        public OnResultCallback() {
            mHandler = new Handler();
        }

        public OnResultCallback(Handler handler) {
            mHandler = handler;
        }

        public final void callOnStartCheck() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onStartCheck();
                }
            });
        }

        public final void callOnNoUpdate() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onNoUpdate();
                }
            });
        }

        public final void callOnStartDownload(final String version, final String url) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onStartDownload(version, url);
                }
            });
        }

        public final void callOnDownloadComplete() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onDownloadComplete();
                }
            });
        }

        public final void callOnProgress(final long total, final long current) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onProgress(total, current);
                }
            });
        }

        public final void callOnError(final Throwable e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onError(e);
                }
            });
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
